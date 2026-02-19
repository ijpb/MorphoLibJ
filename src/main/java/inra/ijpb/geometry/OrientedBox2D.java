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

import java.awt.geom.Point2D;

/**
 * An oriented Box in 2 dimensions, used to store results of oriented bounding box.
 * 
 * @author dlegland
 *
 */
public class OrientedBox2D
{
	// ==================================================
	// Class variables
	
	double xc;
	double yc;
	double length;
	double width;
	
	/**
	 * The orientation of this box, in degrees counted counter-clockwise.
	 */
	double orientation;
	
	// ==================================================
	// Constructors
	
	/**
	 * Default constructor for OrientedBox2D.
	 * 
	 * @param center
	 *            the center of the box
	 * @param length
	 *            the box length
	 * @param width
	 *            the box width
	 * @param orientation
	 *            the orientation of the box, in degrees counter-clockwise
	 */
	public OrientedBox2D(Point2D center, double length, double width, double orientation)
	{
		this(center.getX(), center.getY(), length, width, orientation);
	}
	
	/**
	 * Default constructor for OrientedBox2D, that specifies center as two
	 * coordinates.
	 * 
	 * @param xc
	 *            the x-coordinate of the box center
	 * @param yc
	 *            the y-coordinate of the box center
	 * @param length
	 *            the box length
	 * @param width
	 *            the box width
	 * @param orientation
	 *            the orientation of the box, in degrees counter-clockwise
	 */
	public OrientedBox2D(double xc, double yc, double length, double width, double orientation)
	{
		this.xc = xc;
		this.yc = yc;
		this.length = length;
		this.width = width;
		this.orientation = orientation;
	}
	
	// ==================================================
	// generic methods

	/**
	 * Computes the area of this  box.
	 * 
	 * @return the area of the box.
	 */
	public double area()
	{
		return length * width;
	}
	
	// ==================================================
	// accessors

	/**
	 * @return the center of the box
	 */
	public Point2D center()
	{
		return new Point2D.Double(xc, yc);
	}
	
	/**
	 * @return the length of the box
	 */
	public double length()
	{
		return this.length;
	}
	
	/**
	 * @return the width of the box
	 */
	public double width()
	{
		return this.width;
	}
	
	/**
	 * @return the orientation of the box, in degrees
	 */
	public double orientation()
	{
		return this.orientation;
	}
}
