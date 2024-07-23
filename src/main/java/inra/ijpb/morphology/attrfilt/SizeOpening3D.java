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
package inra.ijpb.morphology.attrfilt;

import ij.ImageStack;
import inra.ijpb.algo.Algo;

/**
 * Size opening for 3D gray level images. Remove all cross sections whose size
 * is smaller than the specified number of voxels.
 *
 * 
 * @author dlegland
 *
 */
public interface SizeOpening3D extends Algo
{
	/**
	 * Applies size opening on the input 3D grayscale image and returns the result.
	 * 
	 * @param image
	 *            the image to process.
	 * @param minVolume
	 *            the minimal number of voxels the regions need to keep.
	 * @return the result of size opening.
	 */
	public ImageStack process(ImageStack image, int minVolume);
}
