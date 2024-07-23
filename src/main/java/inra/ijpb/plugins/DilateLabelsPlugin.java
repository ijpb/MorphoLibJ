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

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.binary.distmap.ChamferMask2D;
import inra.ijpb.binary.distmap.ChamferMask3D;
import inra.ijpb.label.filter.ChamferLabelDilation2DShort;
import inra.ijpb.label.filter.ChamferLabelDilation3DShort;
import inra.ijpb.util.IJUtils;

/**
 * Dilate all labels within a label image.
 */
public class DilateLabelsPlugin implements PlugIn
{
	@Override
	public void run(String arg0) 
	{
		ImagePlus imagePlus = IJ.getImage();
		
		// open a dialog to choose options
		GenericDialog gd = new GenericDialog("Dilate Labels");
		gd.addNumericField("Radius", 2.0, 1);
        
        // If cancel was clicked, do nothing
        gd.showDialog();
        if (gd.wasCanceled())
            return;

        // parse user options
        double radius = gd.getNextNumber();
        
        String newName = imagePlus.getShortTitle() + "-dilated";
		ImagePlus resultPlus;

		// apply operator on current image
        long t0 = System.currentTimeMillis();
		if (imagePlus.getStackSize() == 1)
		{
			// Process 2D image
			ChamferLabelDilation2DShort algo = new ChamferLabelDilation2DShort(ChamferMask2D.CHESSKNIGHT, radius);
			DefaultAlgoListener.monitor(algo);
			ImageProcessor image = imagePlus.getProcessor();
			ImageProcessor result = algo.process(image);
			
			// update display settings
			result.setMinAndMax(image.getMin(), image.getMax());
            result.setColorModel(image.getColorModel());
			resultPlus = new ImagePlus(newName, result);
		} 
		else 
		{
			// Process 3D image
			ImageStack image = imagePlus.getStack();
			ChamferLabelDilation3DShort algo = new ChamferLabelDilation3DShort(ChamferMask3D.SVENSSON_3_4_5_7, radius);
			DefaultAlgoListener.monitor(algo);
			ImageStack result = algo.process(imagePlus.getStack());
			result.setColorModel(image.getColorModel());
			resultPlus = new ImagePlus(newName, result);
			
	        // update display range
	    	double min = imagePlus.getDisplayRangeMin();
	    	double max = imagePlus.getDisplayRangeMax();
	    	resultPlus.setDisplayRange(min, max);
		}
		long elapsedTime = System.currentTimeMillis() - t0;
		
    	// display result
		resultPlus.show();
		resultPlus.copyScale(imagePlus);
		
		if (imagePlus.getStackSize() > 1) 
		{
			resultPlus.setSlice(imagePlus.getCurrentSlice());
		}
		
		// display elapsed time
		IJUtils.showElapsedTime("Dilate Labels", elapsedTime, imagePlus);
	}
}
