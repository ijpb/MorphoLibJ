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
/**
 * 
 */
package inra.ijpb.label.distmap;

import inra.ijpb.binary.distmap.ChamferMask2D;

/**
 * Specialization of DistanceTransform based on the use of a chamfer mask.
 * 
 * Provides methods for retrieving the mask, and the normalization weight.
 * 
 * @see ChamferMask2D
 * 
 * @author dlegland
 */
public interface ChamferDistanceTransform2D extends DistanceTransform2D
{
	/**
	 * Return the chamfer mask used by this distance transform algorithm.
	 * 
	 * @return the chamfer mask used by this distance transform algorithm.
	 */
	public ChamferMask2D mask();
}
