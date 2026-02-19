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
import ij.plugin.PlugIn;
import inra.ijpb.binary.BinaryImages;

/**
 * Converts an image into a binary image, by setting to true all the pixels or
 * voxels of the input image that are (strictly) greater than zero.
 */
public class BinarizeImagePlugin implements PlugIn
{

	@Override
	public void run(String arg)
	{
		ImagePlus imagePlus = IJ.getImage();
		ImagePlus resultPlus = BinaryImages.binarize(imagePlus);

		// copy settings
		resultPlus.copyScale(imagePlus);
		resultPlus.setDisplayRange(imagePlus.getDisplayRangeMin(), imagePlus.getDisplayRangeMax());
		// selectedPlus.setLut( imagePlus.getProcessor().getLut() );

		// display and adapt visible slice
		resultPlus.show();
		if (imagePlus.getStackSize() > 1)
		{
			resultPlus.setSlice(imagePlus.getCurrentSlice());
		}
	}

}
