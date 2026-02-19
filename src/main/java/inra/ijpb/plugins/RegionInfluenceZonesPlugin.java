/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2026 INRA.
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
/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import inra.ijpb.binary.distmap.ChamferMask2D;
import inra.ijpb.binary.distmap.ChamferMask3D;
import inra.ijpb.label.filter.LabelMapInfluenceZones2DShort;
import inra.ijpb.label.filter.LabelMapInfluenceZones3DShort;
import inra.ijpb.util.IJUtils;

/**
 * Computes influence zones of each region within a label map image.
 * 
 * Each pixel of the influence zone image is associated to the label of the
 * closest region.
 * 
 * @see DilateLabelsPlugin
 */
public class RegionInfluenceZonesPlugin implements PlugIn
{

	@Override
	public void run(String arg)
	{
		// retrieve current image
		ImagePlus imagePlus = IJ.getImage();
		
        
        String newName = imagePlus.getShortTitle() + "-Zones";
		ImagePlus resultPlus;

		// apply operator on current image
        long t0 = System.currentTimeMillis();
		if (imagePlus.getStackSize() == 1)
		{
			// create algorithm
			ChamferMask2D mask = ChamferMask2D.BORGEFORS; 
			LabelMapInfluenceZones2DShort algo = new LabelMapInfluenceZones2DShort(mask);

			// apply operator to image data
			ImageProcessor image = imagePlus.getProcessor();
			ImageProcessor result = algo.process(image);
			
			// propagate meta data
			result.setMinAndMax(image.getMin(), image.getMax());
			result.setColorModel(image.getColorModel());

			// create result image plus
			resultPlus = new ImagePlus(newName, result);
		}
		else
		{
			// create algorithm
			ChamferMask3D mask = ChamferMask3D.SVENSSON_3_4_5_7; 
			LabelMapInfluenceZones3DShort algo = new LabelMapInfluenceZones3DShort(mask);
			
			// apply operator to image data
			ImageStack image = imagePlus.getStack();
			ImageStack result = algo.process(image);
			
			// propagate metadata
			result.setColorModel(image.getColorModel());

			// create result image plus
			resultPlus = new ImagePlus(newName, result);

			// update display range
	    	double min = imagePlus.getDisplayRangeMin();
	    	double max = imagePlus.getDisplayRangeMax();
	    	resultPlus.setDisplayRange(min, max);
		}
		long elapsedTime = System.currentTimeMillis() - t0;
		
		
		// display result image
		resultPlus.show();
		resultPlus.copyScale(imagePlus);
		
		if (imagePlus.getStackSize() > 1) 
		{
			resultPlus.setSlice(imagePlus.getCurrentSlice());
		}
		
		// display elapsed time
		IJUtils.showElapsedTime("Region Influence Zones", elapsedTime, imagePlus);

	}

}
