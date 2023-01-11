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

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Reconstruction;
import inra.ijpb.morphology.Reconstruction3D;
import inra.ijpb.util.IJUtils;

/**
 * Plugin for filling holes (dark holes within bright structures) in 8-bits 
 * grayscale or binary 2D/3D images.
 */
public class FillHolesPlugin implements PlugIn
{

	ImagePlus imp;
	
	@Override
	public void run(String arg0) 
	{
		ImagePlus imagePlus = IJ.getImage();
		
		String newName = imagePlus.getShortTitle() + "-fillHoles";
		
		long t0 = System.currentTimeMillis();
		
		ImagePlus resultPlus;
		if (imagePlus.getStackSize() > 1)
		{
			ImageStack stack = imagePlus.getStack();
			ImageStack result = Reconstruction3D.fillHoles(stack);
			resultPlus = new ImagePlus(newName, result);
			
		} 
		else 
		{
			ImageProcessor image = imagePlus.getProcessor();
			ImageProcessor result = Reconstruction.fillHoles(image);
			resultPlus = new ImagePlus(newName, result);
		}
		long elapsedTime = System.currentTimeMillis() - t0;
		
		resultPlus.show();
		resultPlus.copyScale(imagePlus);
		
		if (imagePlus.getStackSize() > 1) 
		{
			resultPlus.setSlice(imagePlus.getCurrentSlice());
		}
		
		 IJUtils.showElapsedTime("Fill Holes", elapsedTime, imagePlus);
	}

}
