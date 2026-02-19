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
package inra.ijpb.geometry;

/**
 * A point in the 3-dimensional Euclidean space, defined by x, y, and z coordinates.
 * 
 * @author dlegland
 *
 */
public class Point3D
{
	private double x;

	private double y;

	private double z;
	
	/**
	 * Creates a new point located at the origin.
	 */
	public Point3D()
	{
		this(0, 0, 0);
	}
	
	/**
	 * Creates a new point at the specified coordinates.
	 * 
	 * @param x
	 *            the X-coordinate of the point
	 * @param y
	 *            the Y-coordinate of the point
	 * @param z
	 *            the Z-coordinate of the point
	 */
	public Point3D(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * Computes the euclidean distance to another point.
	 * 
	 * @param point
	 *            the other point
	 * @return the distance between this point and the query point
	 */
	public double distance(Point3D point)
	{
		double dx = (point.x - x);
		double dy = (point.y - y);
		double dz = (point.z - z);
		return Math.hypot(Math.hypot(dx, dy), dz);
	}
	
	/**
	 * @return the X-coordinate of the point
	 */
	public double getX()
	{
		return x;
	}

	/**
	 * @return the Y-coordinate of the point
	 */
	public double getY()
	{
		return y;
	}

	/**
	 * @return the Z-coordinate of the point
	 */
	public double getZ()
	{
		return z;
	}
}
