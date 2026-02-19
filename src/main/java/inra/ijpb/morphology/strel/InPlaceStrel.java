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
package inra.ijpb.morphology.strel;

import ij.process.ImageProcessor;
import inra.ijpb.morphology.Strel;

/**
 * A structuring element that can performs erosion or dilation directly in the
 * original image buffer. As InPlaceStrel do not require memory allocation, 
 * they result in faster execution.
 * 
 * @see SeparableStrel
 * @author David Legland
 *
 */
public interface InPlaceStrel extends Strel, InPlaceStrel3D {

	/**
	 * Performs dilation of the image given as argument, and stores the result
	 * in the same image. 
	 * @param image the input image to dilate
	 */
	public void inPlaceDilation(ImageProcessor image);

	/**
	 * Performs erosion of the image given as argument, and stores the result
	 * in the same image. 
	 * @param image the input image to erode
	 */
	public void inPlaceErosion(ImageProcessor image);
	
	/**
	 * The reverse structuring element of an InPlaceStrel is also an
	 * InPlaceStrel.
	 */
	public InPlaceStrel reverse();
}
