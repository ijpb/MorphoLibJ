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
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.GeodesicReconstruction;
import inra.ijpb.morphology.GeodesicReconstruction3D;
import inra.ijpb.util.IJUtils;

/**
 * Plugin for removing borders in 8-bits grayscale or binary 2D or 3D image.
 */
public class KillBordersPlugin implements PlugIn
{

	@Override
	public void run(String arg0) 
	{
		ImagePlus imagePlus = IJ.getImage();
		
		String newName = imagePlus.getShortTitle() + "-killBorders";
		
		ImagePlus resultPlus;
		long t0 = System.currentTimeMillis(); 
		if (imagePlus.getStackSize() == 1) 
		{
			// Process planar images
			ImageProcessor image = imagePlus.getProcessor();
			ImageProcessor result = GeodesicReconstruction.killBorders(image);
			if (!(result instanceof ColorProcessor))
				result.setLut(image.getLut());
			resultPlus = new ImagePlus(newName, result);
			
		}
		else 
		{
			// Process 3D stack
			ImageStack image = imagePlus.getStack();
			ImageStack result = GeodesicReconstruction3D.killBorders(image);
			result.setColorModel(image.getColorModel());
			resultPlus = new ImagePlus(newName, result);
		} 
		long elapsedTime = System.currentTimeMillis() - t0;
		
		resultPlus.copyScale(imagePlus);
		resultPlus.show();
		
		if (imagePlus.getStackSize() > 1) 
		{
			resultPlus.setSlice(imagePlus.getSlice());
		}

		IJUtils.showElapsedTime("Kill Borders", elapsedTime, imagePlus);
	}
}
