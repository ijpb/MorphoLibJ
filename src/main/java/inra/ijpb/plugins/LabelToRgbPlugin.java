/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.label.LabelImages;
import inra.ijpb.util.ColorMaps.CommonLabelMaps;
import inra.ijpb.util.CommonColors;

import java.awt.Color;

/**
 * Creates a new Color Image that associate a given color to each label of the input image.
 * Opens a dialog to choose a colormap, a background color, and a shuffle option. 
 * Preview option is available.
 * Note that when shuffle is activated, result may be different from preview.
 *  
 * @author David Legland
 *
 */
public class LabelToRgbPlugin implements PlugIn
{
	@Override
	public void run(String arg)
	{
		ImagePlus imagePlus = IJ.getImage();

		int maxLabel = computeMaxLabel(imagePlus);
		
		// Create a new generic dialog with appropriate options
    	GenericDialog gd = new GenericDialog("Label To RGB");
    	gd.addChoice("Colormap", CommonLabelMaps.getAllLabels(), 
    			CommonLabelMaps.JET.getLabel());
    	gd.addChoice("Background", CommonColors.getAllLabels(), CommonColors.WHITE.getLabel());
    	gd.addCheckbox("Shuffle", true);
    	gd.showDialog();
		
    	// test cancel  
    	if (gd.wasCanceled()) 
    		return;

    	// Create a new LUT from info in dialog
		String lutName = gd.getNextChoice();
		String bgColorName = gd.getNextChoice();
		Color bgColor = CommonColors.fromLabel(bgColorName).getColor();
		boolean shuffleLut = gd.getNextBoolean();

		// Create a new LUT from info in dialog
		byte[][] lut = CommonLabelMaps.fromLabel(lutName).computeLut(maxLabel, shuffleLut);
    	
		// Create a new RGB image from index image and LUT options
		ImagePlus resPlus = LabelImages.labelToRgb(imagePlus, lut, bgColor);
    	
		// dispay result image
		resPlus.copyScale(imagePlus);
		resPlus.show();
    	if (imagePlus.getStackSize() > 1) 
    	{
    		resPlus.setSlice(imagePlus.getSlice());
    	}
	}

	/**
	 * Computes the maximum value in the input image or stack, in order to 
	 * initialize colormap with the appropriate number of colors. 
	 */
	private final static int computeMaxLabel(ImagePlus imagePlus) 
	{
		if (imagePlus.getImageStackSize() == 1) 
		{
			return computeMaxLabel(imagePlus.getProcessor());
		}
		else 
		{
			 return computeMaxLabel(imagePlus.getStack());
		}
	}

	private static final int computeMaxLabel(ImageProcessor image) 
	{
		int labelMax = 0;
		if (image instanceof FloatProcessor)
		{
			for (int i = 0; i < image.getPixelCount(); i++) 
			{
				labelMax = Math.max(labelMax, (int) image.getf(i));
			}
		} 
		else
		{
			for (int i = 0; i < image.getPixelCount(); i++) 
			{
				labelMax = Math.max(labelMax, image.get(i));
			}
		}
		
		return labelMax;
	}
	
	private static final int computeMaxLabel(ImageStack image) 
	{
		int labelMax = 0;
		for (int i = 1; i <= image.getSize(); i++) 
		{
			ImageProcessor slice = image.getProcessor(i);
			labelMax = Math.max(labelMax, computeMaxLabel(slice));
		}
		
		return labelMax;
	}
}
