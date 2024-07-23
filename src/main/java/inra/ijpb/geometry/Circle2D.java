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

import java.awt.geom.Point2D;

/**
 * Contains data for representing circle in the 2D plane. Used for the
 * computation of largest inscribed circle.
 * 
 * 
 * @see inra.ijpb.measure.region2d.LargestInscribedCircle
 * 
 * @author dlegland
 *
 */
public class Circle2D
{
	// ==================================================
	// Class variables
	
	/**
	 * The inertia center of the region.
	 */
	private final Point2D center;
	
	/**
	 * The radius of the circle.
	 */
	private final double radius;

	
	// ==================================================
	// Constructors
	
	/**
	 * Creates a new Circle from a center and a radius.
	 * 
	 * @param center
	 *            the center of the circle
	 * @param radius
	 *            the radius of the circle
	 */
	public Circle2D(Point2D center, double radius)
	{
		this.center = center;
		this.radius = radius;
	}
	
	// ==================================================
	// Generic methods
	
	/**
	 * @return the area of the domain enclosed by the circle (equal to PI*R^2)
	 */
	public double area()
	{
		return Math.PI * this.radius * this.radius;
	}
	
	/**
	 * @return the perimeter of the circle (equal to 2*PI*R).
	 */
	public double perimeter()
	{
		return 2 * Math.PI * this.radius;
	}

	
	// ==================================================
	// Accesors
	
	/**
	 * @return the center of the circle
	 */
	public Point2D getCenter()
	{
		return center;
	}
	
	/**
	 * @return the radius of the circle
	 */
	public double getRadius()
	{
		return radius;
	}
}
