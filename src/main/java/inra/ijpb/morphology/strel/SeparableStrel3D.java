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

import inra.ijpb.morphology.Strel3D;

import java.util.Collection;

/**
 * Interface for structuring elements that can be decomposed into several 
 * "simpler" structuring elements. It is assumed that elementary structuring
 * elements can performs in place dilation or erosion (i.e. the implements the
 * InPlaceStrel interface).
 * 
 * @see InPlaceStrel
 * @author David Legland
 *
 */
public interface SeparableStrel3D extends Strel3D {

	/**
	 * Decompose this separable structuring element into a set of smaller
	 * structuring elements that can be used to accelerate processing.
	 * @return a set of elementary structuring elements
	 */
	public Collection<InPlaceStrel3D> decompose();
	
	/**
	 * The reversed structuring element of a separable strel is also separable.
	 */
	public SeparableStrel3D reverse();
}
