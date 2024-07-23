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

import ij.process.ImageProcessor;
import inra.ijpb.algo.Algo;

/**
 * Area opening for gray level images.
 * 
 * @author dlegland
 *
 */
public interface AreaOpening extends Algo
{
	/**
	 * Applies area opening on the input grayscale image and returns the result.
	 * 
	 * @param image
	 *            the image to process.
	 * @param minArea
	 *            the minimal number of pixels the regions need to keep.
	 * @return the result of area opening.
	 */
	public ImageProcessor process(ImageProcessor image, int minArea);
}
