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
package inra.ijpb.morphology.strel;

import ij.ImageStack;
import inra.ijpb.morphology.Strel3D;

/**
 * A structuring element that can performs erosion or dilation directly in the
 * original image stack. As InPlaceStrel do not require memory allocation, 
 * they result in faster execution.
 * 
 * @see SeparableStrel3D
 * @author David Legland
 *
 */
public interface InPlaceStrel3D extends Strel3D {

	/**
	 * Performs dilation of the stack given as argument, and stores the result
	 * in the same image. 
	 * @param stack the input image stack to dilate
	 */
	public void inPlaceDilation(ImageStack stack);

	/**
	 * Performs erosion of the image given as argument, and stores the result
	 * in the same image. 
	 * @param stack the input image stack to erode
	 */
	public void inPlaceErosion(ImageStack stack);
	
	/**
	 * The reverse structuring element of an InPlaceStrel is also an
	 * InPlaceStrel.
	 */
	public InPlaceStrel3D reverse();
}
