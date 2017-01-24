/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Strel;
import inra.ijpb.morphology.directional.OrientedLineStrelFactory;

/**
 * @author dlegland
 *
 */
public class OrientationFiltering implements PlugInFilter 
{

	// ====================================================
	// Global Constants

	/**
	 * When this options is set to true, information messages are displayed on
	 * the console, and the number of counts for each direction is included in
	 * results table. 
	 */
	public boolean debug = false;

	ImagePlus imagePlus;


	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	@Override
	public int setup(String name, ImagePlus imp) 
	{
		if (imp == null) {
			IJ.noImage();
			return DONE;
		}

		this.imagePlus = imp;
		return DOES_ALL | NO_CHANGES;
	}


	// ====================================================
	// Class variables

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	@Override
	public void run(ImageProcessor image) 
	{
		GenericDialog gd = new GenericDialog("Orientation Filtering");
		gd.addChoice("Operation", new String[]{"Opening", "Closing"}, "Opening");
		gd.addNumericField("Line Length", 21, 0);
		gd.addNumericField("Orientation Number", 32, 0);
		gd.showDialog();
		
		if (gd.wasCanceled())
		{
			return;
		}

		int opIndex = gd.getNextChoiceIndex();
		int lineLength = (int) gd.getNextNumber();
		int dirNumber = (int) gd.getNextNumber();
		
		int width = image.getWidth();
		int height = image.getHeight();
		ImageStack result = ImageStack.create(width, height, dirNumber, image.getBitDepth());

		OrientedLineStrelFactory strelFactory = new OrientedLineStrelFactory(lineLength);
		
		IJ.showStatus("Orientation filtering");
		
		for (int i = 0; i < dirNumber; i++)
		{
			IJ.showProgress(i, dirNumber);
			
			double theta = ((double) i) * 180.0 / dirNumber;
			Strel strel = strelFactory.createStrel(theta);
			
			ImageProcessor res;
			if (opIndex == 0)
			{
				res = strel.opening(image);
			}
			else
			{
				res = strel.closing(image);
			}
			
			result.setProcessor(res, i + 1);
		}
		
		IJ.showStatus("");
		IJ.showProgress(1, 1);
		
		ImagePlus resultPlus = new ImagePlus("orientationStack", result);
		resultPlus.show();
	}
}
