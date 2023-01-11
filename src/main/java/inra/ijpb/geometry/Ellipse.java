/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2023 INRA.
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
 * Represents a 2D ellipse.
 * 
 * @author dlegland
 *
 */
public class Ellipse
{
    // ==================================================
    // Static methods
    
    /**
     * Initializes center array from ellipse array.
     *
     * @param ellipses
     *            an array of ellipses
     * @return the array of points corresponding to the centers of the ellipses.
     */
    public static final Point2D[] centers(Ellipse[] ellipses)
    {
        Point2D[] centroids = new Point2D[ellipses.length];
        for (int i = 0; i < ellipses.length; i++)
        {
            centroids[i] = ellipses[i].center();
        }
        return centroids;
    }
    
	// ==================================================
	// Class variables
	
	/**
	 * The inertia center of the region.
	 */
	private final Point2D center;
	
	/**
	 * The length of the semi-major axis
	 */
	private final double radius1;

	/**
	 * The length of the semi-minor axis
	 */
	private final double radius2;

	/**
	 * The orientation of the main axis, in degrees
	 */
	private final double orientation;

	
	// ==================================================
	// Constructors
	
	/**
	 * Creates a new ellipse.
	 * 
	 * @param center
	 *            the center of the ellipse
	 * @param radius1
	 *            the length of the major semi-axis
	 * @param radius2
	 *            the length of the minor semi-axis
	 * @param theta
	 *            the orientation of the ellipse, in degrees
	 */
	public Ellipse(Point2D center, double radius1, double radius2, double theta)
	{
		this.center = center;
		this.radius1 = radius1;
		this.radius2 = radius2;
		this.orientation = theta;
	}
	
	/**
	 * Creates a new ellipse.
	 * 
	 * @param xc
	 *            the x-coordinate of the center of the ellipse
	 * @param yc
	 *            the y-coordinate of the center of the ellipse
	 * @param radius1
	 *            the length of the major semi-axis
	 * @param radius2
	 *            the length of the minor semi-axis
	 * @param theta
	 *            the orientation of the ellipse, in degrees
	 */
	public Ellipse(double xc, double yc, double radius1, double radius2, double theta)
	{
		this.center = new Point2D.Double(xc, yc);
		this.radius1 = radius1;
		this.radius2 = radius2;
		this.orientation = theta;
	}
	
	// ==================================================
	// Generic methods

	/**
	 * @return the area of the domain enclosed by this ellipse
	 */
	public double area()
	{
		return Math.PI * this.radius1 * this.radius2;
	}
	
	
	// ==================================================
	// Accesors
	
	/**
	 * @return the center of this ellipse
	 */
	public Point2D center()
	{
		return center;
	}
	
	/**
	 * @return the length of the major semi-axis
	 */
	public double radius1()
	{
		return radius1;
	}

	/**
	 * @return the length of the minor semi-axis
	 */
	public double radius2()
	{
		return radius2;
	}

	/**
	 * @return the orientation of the ellipse, in degrees
	 */
	public double orientation()
	{
		return orientation;
	}

	
	// ==================================================
	// Override some of Object methods
	
	@Override
	public String toString()
	{
		return String.format("Ellipse(%f,%f,%f,%f,%f)", center.getX(), center.getY(), radius1, radius2, orientation);
	}
}
