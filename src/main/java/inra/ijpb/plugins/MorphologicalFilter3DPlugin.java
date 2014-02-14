/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Morphology;
import inra.ijpb.morphology.Strel3D;
import inra.ijpb.morphology.Morphology.Operation;
import inra.ijpb.util.IJUtils;

/**
 * 
 */

/**
 * Various morphological filters for 3D images.
 * 
 * @author David Legland
 *
 */

public class MorphologicalFilter3DPlugin implements PlugIn {

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg) {
		ImagePlus imagePlus = WindowManager.getCurrentImage();
		if (imagePlus == null) {
			IJ.error("No image", "Need at least one image to work");
			return;
		}
		
		// create the dialog
		GenericDialog gd = new GenericDialog("Morphological Filter");
		
		gd.addChoice("Operation", Operation.getAllLabels(), 
				Operation.DILATION.toString());
		gd.addChoice("Element", Strel3D.Shape.getAllLabels(), 
				Strel3D.Shape.CUBE.toString());
		gd.addNumericField("Radius (in pixels)", 2, 0);
		gd.addCheckbox("Show Element", false);
		
		// Could also add an option for the type of operation
		gd.showDialog();
		
		if (gd.wasCanceled())
			return;
		
		long t0 = System.currentTimeMillis();

		// extract chosen parameters
		Operation op = Operation.fromLabel(gd.getNextChoice());
		Strel3D.Shape type = Strel3D.Shape.fromLabel(gd.getNextChoice());
		int radius = (int) gd.getNextNumber();		
		boolean showStrel = gd.getNextBoolean();
		
		// Create structuring element of the given size
		Strel3D strel = type.fromRadius(radius);
		strel.showProgress(true);
		
		// Eventually display the structuring element used for processing 
		if (showStrel) {
			showStrelImage(strel);
		}
		
		// Execute core of the plugin
		ImagePlus resPlus = exec(imagePlus, op, strel);

		if (resPlus == null)
			return;

		// Display the result image
		resPlus.show();
		resPlus.setSlice(imagePlus.getSlice());

		// Display elapsed time
		long t1 = System.currentTimeMillis();
		IJUtils.showElapsedTime(op.toString(), t1 - t0, imagePlus);
//		IJ.showStatus("Elapsed time: " + (t1 - t0) / 1000. + "s");
	}


	/**
	 * Displays the current structuring element in a new ImagePlus. 
	 * @param strel the structuring element to display
	 */
	private void showStrelImage(Strel3D strel) {
		// Size of the strel image (little bit larger than strel)
		int[] dim = strel.getSize();
		int width = dim[0] + 20; 
		int height = dim[1] + 20;
		
		// Creates strel image by dilating a point
		ImageProcessor maskProcessor = new ByteProcessor(width, height);
		maskProcessor.set(width / 2, height / 2, 255);
		ImageStack stack = new ImageStack();
		stack.addSlice(maskProcessor);
		stack = Morphology.dilation(stack, strel);
		maskProcessor = stack.getProcessor(1);
		
		// Forces the display to inverted LUT (display a black over white)
		if (!maskProcessor.isInvertedLut())
			maskProcessor.invertLut();
		
		// Display strel image
		ImagePlus maskImage = new ImagePlus("Element", maskProcessor);
		maskImage.show();
	}

	
	/**
	 */
	public ImagePlus exec(ImagePlus image, Operation op, Strel3D strel) {
		// Check validity of parameters
		if (image == null)
			return null;
		
		// extract the input stack
		ImageStack inputStack = image.getStack();

		// apply morphological operation
		ImageStack resultStack = op.apply(inputStack, strel);

		// create the new image plus from the processor
		ImagePlus resultPlus = new ImagePlus(op.toString(), resultStack);
		resultPlus.copyScale(image);
		
		// return the created array
		return resultPlus;
	}
	
}
