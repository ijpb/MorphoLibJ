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
package inra.ijpb.morphology.binary;

import ij.ImageStack;

/**
 * Simple definition of an operator that transforms a 3D binary image into
 * another 3D binary image.
 * 
 * @see BinaryImageOperator
 * 
 * @author dlegland
 */
public interface BinaryImageOperator3D
{
	/**
	 * Applies the operator to a 3D binary image, and returns the result into a new
	 * 3D binary image.
	 * 
	 * @param image
	 *            the 3D (binary) image to process
	 * @return the result of the processing as a 3D binary image.
	 */
	public ImageStack processBinary(ImageStack image);
}
