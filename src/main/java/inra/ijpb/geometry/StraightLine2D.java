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
package inra.ijpb.geometry;

import java.awt.geom.Point2D;

/**
 * A straight line in the plane.
 * 
 * Defined by an origin and a direction.
 * 
 * @author dlegland
 *
 */
public class StraightLine2D
{
	double x0;
	double y0;
	double dx;
	double dy;
	
	/**
	 * Creates a new straight line from an origin and a direction vector.
	 * 
	 * @param origin
	 *            the origin of the line
	 * @param direction
	 *            the direction vector of the line
	 */
	public StraightLine2D(Point2D origin, Vector2D direction)
	{
		this.x0 = origin.getX();
		this.y0 = origin.getY();
		this.dx = direction.getX();
		this.dy = direction.getY();
	}

	/**
	 * Creates a new straight line from two points belonging to the line
	 * 
	 * @param source
	 *            the first point (used as origin)
	 * @param target
	 *            the second point (used to compute the direction vector)
	 */
	public StraightLine2D(Point2D source, Point2D target)
	{
		this.x0 = source.getX();
		this.y0 = source.getY();
		this.dx = target.getX() - this.x0;
		this.dy = target.getY() - this.y0;
	}
	
	/**
	 * @return the origin of this line
	 */
	public Point2D getOrigin()
	{
		return new Point2D.Double(this.x0, this.y0);
	}
	
	/**
	 * @return the direction vector of this line
	 */
	public Vector2D getDirection()
	{
		return new Vector2D(this.dx, this.dy);
	}
	
	/**
	 * Computes the distance between this line and a query point.
	 * 
	 * @param point
	 *            the query point
	 * @return the distance between the point and the line
	 */
	public double distance(Point2D point)
	{
		// squared norm of direction, to check of validity
		double delta = (this.dx * this.dx + this.dy * this.dy);
		if (delta < 1e-12)
		{
			throw new RuntimeException("Direction vector of line is too small");
		}

		// difference of coordinates between point and line origin 
		double xDiff  = point.getX() - this.x0;
		double yDiff  = point.getY() - this.y0;

		// compute position of points projected on the supporting line, by using
		// normalized dot product
		double pos = (xDiff * this.dx + yDiff * this.dy) / delta;

		// compute distance between point and its projection on the edge
		double dist = Math.hypot(pos * this.dx - xDiff, pos * this.dy - yDiff);
		return dist;
	}
}
