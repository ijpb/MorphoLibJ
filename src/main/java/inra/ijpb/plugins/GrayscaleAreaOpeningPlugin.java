/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
package inra.ijpb.plugins;

import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.AttributeFiltering;

import java.awt.AWTEvent;

/**
 * Select binary particles in a planar image based on number of pixels.
 * This version also provides preview of result.
 * 
 * @see AreaOpeningPlugin
 * 
 * @author David Legland
 */
public class GrayscaleAreaOpeningPlugin implements ExtendedPlugInFilter, DialogListener 
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
    
		return flags;
	}
	
	@Override
	public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr)
	{
		// Create the configuration dialog
		GenericDialog gd = new GenericDialog("Gray Scale Area Opening");

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
		this.result = AttributeFiltering.areaOpening(image, this.minPixelCount); 
		
		if (previewing)
		{
			// Iterate over pixels to change value of reference image
			for (int i = 0; i < image.getPixelCount(); i++)
			{
				image.set(i, result.get(i));
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
