/**
 * 
 */
package inra.ijpb.plugins;

import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ImageProcessor;
import inra.ijpb.binary.BinaryImages;

import java.awt.AWTEvent;

/**
 * Select binary particles in a planar image based on number of pixels.
 * This version also provides preview of result.
 * 
 * @see AreaOpeningPlugin
 * 
 * @author David Legland
 */
public class AreaOpeningPlugin implements ExtendedPlugInFilter, DialogListener 
{
	/** keep flags in plugin */
	private int flags = DOES_ALL | KEEP_PREVIEW | FINAL_PROCESSING;
	
	PlugInFilterRunner pfr;
	int nPasses;
	boolean previewing = false;
	
	/** keep the instance of ImagePlus */ 
	private ImagePlus imagePlus;
	
	/** keep the original image, to restore it after the preview */
	private ImageProcessor baseImage;
	
	/** Keep instance of result image */
	private ImageProcessor result;

	
	int pixelNumber = 100;
//	boolean keepLargest = false;
	
	
	@Override
	public int setup(String arg, ImagePlus imp)
	{
		// Called at the end for cleaning up the results
		if (arg.equals("final")) 
		{
			// replace the preview image by the original image 
			imagePlus.setProcessor(baseImage);
			imagePlus.draw();
			
			// as result is binary, choose inverted LUT 
			result.invertLut();
			
			// Create a new ImagePlus with the result
			String newName = imagePlus.getShortTitle() + "areaOpen";
			ImagePlus resPlus = new ImagePlus(newName, result);
			resPlus.copyScale(imagePlus);
			resPlus.show();
			return DONE;
		}
		
		return flags;
	}
	
	@Override
	public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr)
	{
		// Normal setup
    	this.imagePlus = imp;
    	this.baseImage = imp.getProcessor().duplicate();

		// Create the configuration dialog
		GenericDialog gd = new GenericDialog("Area Opening");

		gd.addNumericField("Pixel Number", 100, 0, 10, "pixels");
//		gd.addCheckbox("Keep_Largest", false);
		
		gd.addPreviewCheckbox(pfr);
		gd.addDialogListener(this);
        previewing = true;
        gd.showDialog();
        previewing = false;
        
        if (gd.wasCanceled())
        	return DONE;
			
    	parseDialogParameters(gd);
			
		// clean up an return 
		gd.dispose();
		return flags;
	}
	
	@Override
	public void run(ImageProcessor image)
	{
		// Execute core of the plugin
		result = BinaryImages.areaOpening(image, this.pixelNumber);

		if (previewing)
		{
			// Fill up the values of original image with the (binary) result
			double valMax = result.getMax();
			for (int i = 0; i < image.getPixelCount(); i++)
			{
				image.set(i, (int) (255 * result.getf(i) / valMax));
			}
		}
	}
	
	@Override
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e)
	{
		parseDialogParameters(gd);
		return true;
	}
	
    private void parseDialogParameters(GenericDialog gd) {
		// extract chosen parameters
		this.pixelNumber	= (int) gd.getNextNumber();
//		this.keepLargest 	= gd.getNextBoolean();
    }

	@Override
	public void setNPasses(int nPasses)
	{
		this.nPasses = nPasses;
	}

}
