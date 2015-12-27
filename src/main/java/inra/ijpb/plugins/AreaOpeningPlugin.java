/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ImageProcessor;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.label.LabelImages;

import java.awt.AWTEvent;
import java.util.HashMap;

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

	private ImageProcessor labelImage;
	private HashMap<Integer, Integer> labelMap;
	private int[] pixelCountArray;
	
	int minPixelCount = 100;
	
	
	@Override
	public int setup(String arg, ImagePlus imp)
	{
		this.imagePlus = imp;
		// Called at the end for cleaning up the results
		if (arg.equals("final")) 
		{
			// replace the preview image by the original image 
			imagePlus.setProcessor(baseImage);
			imagePlus.draw();
			
			// Execute core of the plugin
			result = BinaryImages.areaOpening(baseImage, this.minPixelCount);
			// as result is binary, choose inverted LUT 
			result.invertLut();
			
			// Create a new ImagePlus with the result
			String newName = imagePlus.getShortTitle() + "-areaOpen";
			ImagePlus resPlus = new ImagePlus(newName, result);
			
			// copy spatial calibration and display settings 
			resPlus.copyScale(imagePlus);
			result.setColorModel(baseImage.getColorModel());
			resPlus.show();
			return DONE;
		}
		
		// Normal setup
    	this.baseImage = imp.getProcessor().duplicate();
    	
		// pre-compute label image and pixel count
		ImageProcessor image = imagePlus.getProcessor();
		try 
		{
			this.labelImage = BinaryImages.componentsLabeling(image, 4, 16);
		}
		catch(RuntimeException ex)
		{
			IJ.error("Too many particles", ex.getMessage());
			return flags;
		}

		int[] labels = LabelImages.findAllLabels(labelImage);
		this.labelMap = LabelImages.mapLabelIndices(labels);
		this.pixelCountArray = LabelImages.pixelCount(labelImage, labels);
		
		return flags;
	}
	
	@Override
	public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr)
	{
		// Create the configuration dialog
		GenericDialog gd = new GenericDialog("Area Opening");

		gd.addNumericField("Pixel Number", 100, 0, 10, "pixels");
		
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
		if (previewing)
		{
			// Iterate over pixels to change value of reference image
			boolean keepPixel;
			for (int i = 0; i < image.getPixelCount(); i++)
			{
				keepPixel = false;
				int label = (int) this.labelImage.get(i);
				if (label > 0) 
				{
					int index = this.labelMap.get(label); 
					keepPixel = this.pixelCountArray[index] > this.minPixelCount;
				}
				image.set(i, keepPixel ? 255 : 0);
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
		this.minPixelCount	= (int) gd.getNextNumber();
    }

	@Override
	public void setNPasses(int nPasses)
	{
		this.nPasses = nPasses;
	}

}
