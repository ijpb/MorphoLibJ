/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2023 INRA.
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

import java.awt.AWTEvent;

import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ImageProcessor;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.morphology.directional.DirectionalFilter;
import inra.ijpb.morphology.directional.DirectionalFilter.Operation;
import inra.ijpb.morphology.directional.DirectionalFilter.Type;

/**
 * Plugin for computing directional filtering.
 * 
 * @author dlegland
 *
 */
public class DirectionalFilteringPlugin implements ExtendedPlugInFilter, DialogListener 
{
	/** store flags to indicating which images can be processed */
	private int flags = DOES_ALL | KEEP_PREVIEW | FINAL_PROCESSING;
	
	PlugInFilterRunner pfr;
	int nPasses;
	boolean previewing = false;
	
	/** keep the instance of ImagePlus */ 
	private ImagePlus imagePlus;
	
	/** keep the original image, to restore it after the preview */
	private ImageProcessor baseImage;
	
	/** the instance of result image */
	private ImageProcessor result;

	Type type = Type.MAX;
	Operation op = Operation.OPENING;
	int lineLength = 20;
	int nDirections = 32;

	
	@Override
	public int setup(String arg, ImagePlus imp)
	{
		// Called at the end for cleaning up the results
		if (arg.equals("final")) 
		{
			// replace the preview image by the original image
			imagePlus.setProcessor(baseImage);
			imagePlus.draw();
			
			// Create a new ImagePlus with the result
			String newName = imagePlus.getShortTitle() + "-directional";
			ImagePlus resPlus = new ImagePlus(newName, result);
			
			// calibrate result image and display
			resPlus.copyScale(imagePlus);
			resPlus.show();
			return DONE;
		}
		
		// return flags indicating which image can be processed
		return flags;
	}

	@Override
	public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr)
	{
		// Normal setup
    	this.imagePlus = imp;
    	this.baseImage = imp.getProcessor().duplicate();

		// Create the configuration dialog
		GenericDialog gd = new GenericDialog("Directional Filtering");
		
		gd.addChoice("Type", Type.getAllLabels(), this.type.toString());
		gd.addChoice("Operation", Operation.getAllLabels(), this.op.toString());
		gd.addNumericField("Line Length", this.lineLength, 0, 8, "pixels");
		gd.addNumericField("Direction Number", this.nDirections, 0);
		
		gd.addPreviewCheckbox(pfr);
		gd.addDialogListener(this);
        previewing = true;
        gd.showDialog();
        previewing = false;
        
        if (gd.wasCanceled()) 
        {
			return DONE;
        }
        
    	parseDialogParameters(gd);
			
		// clean up an return 
		gd.dispose();
		return flags;
	}

	@Override
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e)
	{
		parseDialogParameters(gd);
    	return true;
    }

	private void parseDialogParameters(GenericDialog gd) {
		// extract chosen parameters
		this.type 			= Type.fromLabel(gd.getNextChoice());
		this.op 			= Operation.fromLabel(gd.getNextChoice());
		this.lineLength 	= (int) gd.getNextNumber();
		this.nDirections 	= (int) gd.getNextNumber();
	}

	@Override
	public void run(ImageProcessor image)
	{
		DirectionalFilter filter = new DirectionalFilter(this.type, this.op, this.lineLength, this.nDirections);
		DefaultAlgoListener.monitor(filter);
		
		this.result = filter.process(image);

		if (previewing)
		{
			// Fill up the values of original image with values of the result
			for (int i = 0; i < image.getPixelCount(); i++)
			{
    			image.set(i, result.get(i));
    		}
        }
	}

	@Override
	public void setNPasses(int nPasses)
	{
		this.nPasses = nPasses;
	}

}
