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
package inra.ijpb.morphology.geodrec;

/**
 * Enumeration of the two different types of geodesic reconstruction. 
 * @author David Legland
 *
 */
public enum GeodesicReconstructionType 
{
	BY_DILATION,
	BY_EROSION;
	
	private GeodesicReconstructionType()
	{
	}
	
	/**
	 * Returns the sign that can be used in algorithms generic for dilation 
	 * and erosion.
	 * @return +1 for dilation, and -1 for erosion
	 */
	public int getSign() 
	{
		switch (this)
		{
		case BY_DILATION:
			return +1;
		case BY_EROSION:
			return -1;
		default:
			throw new RuntimeException("Unknown case: " + this.toString());
		}
	}
}
