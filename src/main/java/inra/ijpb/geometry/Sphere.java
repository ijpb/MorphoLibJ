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
 * Contains data for representing sphere in the 3D space. Used for the
 * computation of largest inscribed ball.
 * 
 * 
 * @see inra.ijpb.measure.region3d.LargestInscribedBall
 * 
 * @author dlegland
 *
 */
public class Sphere
{
	// ==================================================
	// Class variables
	
	/**
	 * The center of the sphere.
	 */
	private final Point3D center;
	
	/**
	 * The radius of the sphere.
	 */
	private final double radius;

	
	// ==================================================
	// Constructors
	
	/**
	 * Creates a new sphere from a center and a radius.
	 * 
	 * @param center
	 *            the center of the sphere
	 * @param radius
	 *            the radius of the sphere
	 */
	public Sphere(Point3D center, double radius)
	{
		this.center = center;
		this.radius = radius;
	}
	
	// ==================================================
	// Generic methods
	
	/**
	 * @return the volume of the domain enclosed by the sphere (equal to (4*pi/3) * r^3 )
	 */
	public double volume()
	{
		return 4 * Math.PI * this.radius * this.radius * this.radius / 3;
	}
	
	/**
	 * @return the surface area of the sphere (equal to 4*pi*r)
	 */
	public double surfaceArea()
	{
		return 4 * Math.PI * this.radius * this.radius;
	}

	
	// ==================================================
	// Accesors
	
	/**
	 * @return the center of the sphere
	 */
	public Point3D center()
	{
		return center;
	}
	
	/**
	 * @return the radius of the sphere
	 */
	public double radius()
	{
		return radius;
	}
}
