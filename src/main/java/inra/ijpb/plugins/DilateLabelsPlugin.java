/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2017 INRA.
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
import inra.ijpb.binary.ChamferWeights;
import inra.ijpb.binary.ChamferWeights3D;
import inra.ijpb.label.distmap.LabelDilation3D4WShort;
import inra.ijpb.label.distmap.LabelDilationShort5x5;
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
        double distMax = gd.getNextNumber() + 0.5;
        
        String newName = imagePlus.getShortTitle() + "-dilated";
		ImagePlus resultPlus;

		// apply operator on current image
        long t0 = System.currentTimeMillis();
		if (imagePlus.getStackSize() == 1)
		{
			// Process 2D image
			LabelDilationShort5x5 algo = new LabelDilationShort5x5(ChamferWeights.CHESSKNIGHT);
			DefaultAlgoListener.monitor(algo);
			ImageProcessor image = imagePlus.getProcessor();
			ImageProcessor result = algo.process(image, distMax);
			result.setMinAndMax(image.getMin(), image.getMax());
            result.setColorModel(image.getColorModel());
			resultPlus = new ImagePlus(newName, result);
		} 
		else 
		{
			// Process 3D image
			ImageStack image = imagePlus.getStack();
			LabelDilation3D4WShort algo = new LabelDilation3D4WShort(ChamferWeights3D.WEIGHTS_3_4_5_7);
			DefaultAlgoListener.monitor(algo);
			ImageStack result = algo.process(imagePlus.getStack(), distMax);
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
