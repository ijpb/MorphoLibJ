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
