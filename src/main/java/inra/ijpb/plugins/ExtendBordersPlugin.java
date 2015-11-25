package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import inra.ijpb.data.border.BorderManager;

/**
 * 
 */

/**
 * @author David Legland
 *
 */
public class ExtendBordersPlugin implements PlugIn {

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg) {
		// Get current image, and show error msg if no one is open
		ImagePlus image = IJ.getImage();
		
		int imageType = image.getType();
		
		// Open a dialog to choose the different parameters
		GenericDialog gd = new GenericDialog("Add Border");
		gd.addNumericField("Left", 0, 0);
		gd.addNumericField("Right", 0, 0);
		gd.addNumericField("Top", 0, 0);
		gd.addNumericField("Bottom", 0, 0);
		
		switch (imageType) {
		case ImagePlus.GRAY8:
			gd.addChoice("Fill Value", BorderManager.Type.getAllLabels(),
					BorderManager.Type.REPLICATED.toString());
			break;
		}

		gd.showDialog();
		
		if (gd.wasCanceled())
			return;
		
		// Extract the different border values
		int left 	= (int) gd.getNextNumber();
		int right 	= (int) gd.getNextNumber();
		int top 	= (int) gd.getNextNumber();
		int bottom 	= (int) gd.getNextNumber();
		
		// parse border manager type
		String label = gd.getNextChoice();
		BorderManager.Type borderType = BorderManager.Type.fromLabel(label);

		// create border manager for input image
		ImageProcessor proc = image.getProcessor();
		BorderManager border = borderType.createBorderManager(proc);

		// Execute core of the plugin
		ImageProcessor res = process(proc, left, right, top, bottom, border);
		ImagePlus resPlus = new ImagePlus(image.getShortTitle()+"-ext", res);

		// display result image
		resPlus.copyAttributes(image);
		resPlus.show();
	}
	
	@Deprecated
	public ImagePlus exec(ImagePlus image, 
			int left, int right, int top, int bottom, BorderManager border) {
		ImageProcessor proc = image.getProcessor();
		ImageProcessor result = process(proc, left, right, top, bottom, border);
		return new ImagePlus(image.getTitle(), result);
	}

	/**
	 * Adds the specified number of pixels around the input image, and returns
	 * the resulting image. Assumes reference image contains a BYTE Processor.
	 * 
	 * @param image
	 *            the input image
	 * @param left
	 *            the number of pixels to add to the left
	 * @param right
	 *            the number of pixels to add to the right
	 * @param top
	 *            the number of pixels to add on top of image
	 * @param bottom
	 *            the number of pixels to at the bottom of image
	 * @param border
	 *            an instance of BorderManager that specifies the value of
	 *            pixels to be added
	 * @return a new image with extended borders
	 */
	public static final ImageProcessor process(ImageProcessor image, 
			int left, int right, int top, int bottom, BorderManager border)
	{
		// get image dimensions
		int width = image.getWidth(); 
		int height = image.getHeight(); 
		
		// compute result dimensions
		int width2 = width + left + right;
		int height2 = height + top + bottom;
		ImageProcessor result = image.createProcessor(width2 , height2);
		
		// Iterate on image pixels, and choose result value depending on mask
		for (int x = 0; x < width2; x++) {
			for (int y = 0; y < height2; y++) {
				result.set(x, y, border.get(x-left, y-top));
			}
		}
		
		return result;
	}
}
