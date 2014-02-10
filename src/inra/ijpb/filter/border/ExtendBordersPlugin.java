package inra.ijpb.filter.border;



import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import inra.ijpb.filter.border.BorderManager;

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
		ImagePlus res = exec(image, left, right, top, bottom, border);

		// show new image if needed
		if (res != null) {
			res.show();
		}
		
	}
	public ImagePlus exec(ImagePlus image, 
			int left, int right, int top, int bottom, BorderManager border) {
		ImageProcessor proc = image.getProcessor();
		ImageProcessor result = process(proc, left, right, top, bottom, border);
		return new ImagePlus(image.getTitle(), result);
	}

	/**
	 * Assumes reference image contains a GRAY Processor.
	 */
	public static ImageProcessor process(ImageProcessor image, 
			int left, int right, int top, int bottom, BorderManager border) {
		
		int width = image.getWidth(); 
		int height = image.getHeight(); 
		
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
