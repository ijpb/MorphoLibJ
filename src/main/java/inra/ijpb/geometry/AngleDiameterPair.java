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
package inra.ijpb.geometry;

/**
 * Data structure used to return result of Feret diameters computation. Can be
 * used to return the result of minimum or maximum diameters computation.
 * 
 * @author dlegland
 *
 */
public class AngleDiameterPair
{
	/** Angle in radians */
	public double angle;

	/** Diameter computed in the direction of the angle */
	public double diameter;

	/**
	 * Default constructor, using angle in degrees and diameter.
	 * 
	 * @param angle
	 *            the orientation angle, in degrees
	 * @param diameter
	 *            the diameter along the direction
	 */
	public AngleDiameterPair(double angle, double diameter)
	{
		this.angle = angle;
		this.diameter = diameter;
	}

}
