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
import java.util.ArrayList;

/**
 * Utility methods for computing Feret diameters
 * @author dlegland
 *
 */
public class FeretDiameters
{
	/**
	 * Computes Maximum Feret diameter of a set of points.
	 * 
	 * Note: it is often a good idea to compute convex hull before computing
	 * Feret diameter.
	 * 
	 * @param points
	 *            a collection of planar points
	 * @return the maximum Feret diameter of the point set
	 */
	public final static PointPair2D maxFeretDiameter(ArrayList<? extends Point2D> points)
	{
		double distMax = Double.NEGATIVE_INFINITY;
		PointPair2D maxDiam = null;
		
		int n = points.size();
		for (int i1 = 0; i1 < n - 1; i1++)
		{
			Point2D p1 = points.get(i1);
			for (int i2 = i1 + 1; i2 < n; i2++)
			{
				Point2D p2 = points.get(i2);
		
				double dist = p1.distance(p2);
				if (dist > distMax)
				{
					maxDiam = new PointPair2D(p1, p2);
					distMax = dist;
				}
			}
		}
	
		return maxDiam;
	}
	
	/**
	 * Computes Minimum Feret diameter of a set of points and returns both the
	 * diameter and the corresponding angle. 
	 * 
	 * Uses a naive algorithm (complexity of O(n^2)).
	 *
	 * Note: it is often more efficient to compute the convex hull of the set of
	 * points before computing minimum Feret diameter.
	 * 
	 * @param points
	 *            a collection of planar points
	 * @return the minimum Feret diameter of the point set
	 */
	public final static AngleDiameterPair minFeretDiameter(ArrayList<? extends Point2D> points)
	{
		// first compute convex hull to simplify
		int n = points.size();

		// initialize result
		double widthMin = Double.POSITIVE_INFINITY;
		double angleMin = 0;
		StraightLine2D line;

		for (int i = 0; i < n; i++)
		{
			Point2D p1 = points.get(i);
			Point2D p2 = points.get((i + 1) % n);
			
			// avoid degenerated lines
			if (p1.distance(p2) < 1e-12)
			{
				continue;
			}

			// Compute the width for this polygon edge
			line = new StraightLine2D(p1, p2);
			double width = 0;
			for (Point2D p : points)
			{
				double dist = line.distance(p);
				width = Math.max(width, dist);
			}
			
			// check if smallest width
			if (width < widthMin)
			{
				widthMin = width;
				double dx = p2.getX() - p1.getX();
				double dy = p2.getY() - p1.getY();
				angleMin = Math.atan2(dy, dx);
			}
		}
				
		return new AngleDiameterPair(angleMin - Math.PI/2, widthMin);				
	}
}
