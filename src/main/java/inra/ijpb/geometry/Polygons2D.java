/**
 * 
 */
package inra.ijpb.geometry;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * A set of static methods operating on polygons.
 * 
 * @author dlegland
 *
 */
public class Polygons2D
{
	/**
	 * Private constructor to prevent instantiation.
	 */
	private Polygons2D()
	{
	}

	/**
	 * Uses the gift wrap algorithm with floating point values to find the
	 * convex hull and returns it as a list of points.
	 * 
	 * Uses Jarvis algorithm, also known as "Gift wrap" algorithm.
	 * 
	 * Code from ij.gui.PolygonRoi.getConvexHull(), adapted to return a polygon
	 * oriented counter-clockwise.
	 * 
	 * Uses floating point computation with specific processing of aligned
	 * vertices.
	 * 
	 * @param points
	 *            a set of points coordinates in the 2D space
	 * @return the convex hull of the points, as a list of ordered vertices
	 */
	public static final Polygon2D convexHull(ArrayList<? extends Point2D> points)
	{
		// Get polygon info
		int n = points.size();
		
		// index of left-most vertex of horizontal line with smallest y
		int pStart = 0;
		double ymin = java.lang.Double.MAX_VALUE;
		double smallestX = java.lang.Double.MAX_VALUE;

		// Iterate over vertices to identify index of point with lowest y-coord.
		for (int i = 0; i < n; i++)
		{
			Point2D vertex = points.get(i);
			double y = vertex.getY();

			// update lowest vertex index
			if (y < ymin)
			{
				ymin = y;
				pStart = i;
				smallestX = vertex.getX();
			}
			else if (y == ymin)
			{
				double x = vertex.getX();
				if (x < smallestX)
				{
					smallestX = x;
					pStart = i;
				}
			}
		}
		
		// convex hull
		Polygon2D convHull = new Polygon2D();
		
		// p1: index of current hull vertex
		// p2: index of current candidate for next hull vertex
		// p3: index of iterator on point set
		
		int ip1 = pStart;
		do
		{
			// coordinates of current convex hull vertex
			Point2D p1 = points.get(ip1);
			double x1 = p1.getX();
			double y1 = p1.getY();
			
			// coordinates of next vertex candidate
			int ip2 = (ip1 + 1) % n;
			Point2D p2 = points.get(ip2);
			double x2 = p2.getX();
			double y2 = p2.getY();
	
			// find the next "wrapping" vertex by computing oriented angle
			int ip3 = (ip2 + 1) % n;
			do
			{
				Point2D p3 = points.get(ip3);
				double x3 = p3.getX();
				double y3 = p3.getY();
				
				// if V1-V2-V3 is oriented CW, use V3 as next wrapping candidate
				double det = x1 * (y2 - y3) - y1 * (x2 - x3) + (y3 * x2 - y2 * x3);
				if (det < 0)
				{
					if (det < -1e-12)
					{
						// regular corner
						x2 = x3;
						y2 = y3;
						ip2 = ip3;
					}
					else
					{
						// specific processing for aligned points
						// ensure vertices 1,2,3 are aligned in this order
						double x12 = x2 - x1;
						double y12 = y2 - y1;
						double x13 = x3 - x1;
						double y13 = y3 - y1;
						if ((x12 * x13 + y12 * y13) > (x13 * x13 + y13 * y13))
						{
							x2 = x3;
							y2 = y3;
							ip2 = ip3;
						}
					}
				}
				ip3 = (ip3 + 1) % n;
			} while (ip3 != ip1);
			
			convHull.addVertex(new Point2D.Double(x1, y1));
			ip1 = ip2;
		} while (ip1 != pStart);
	
		return convHull;
	}

	/**
	 * Uses the gift wrap algorithm with integer values to find the convex hull
	 * of a list of vertices, and returns it as an ordered list of points.
	 * 
	 * Code from ij.gui.PolygonRoi.getConvexHull(), adapted to return a list of
	 * vertices oriented counter-clockwise.
	 * 
	 * @param points
	 *            a set of points with integer coordinates in the 2D space
	 * @return the convex hull of the points, as a list of ordered vertices with
	 *         integer coordinates
	 */
	public static final ArrayList<Point> convexHull_int(ArrayList<Point> points)
	{
		// create array for storing polygon coordinates
		int n = points.size();
		int[] xCoords = new int[n];
		int[] yCoords = new int[n];
		
		// minimum bound in vertical direction
		int ymin = Integer.MAX_VALUE;

		// index of left-most vertex of horizontal line with smallest y
		int pStart = 0;
		int xmin = Integer.MAX_VALUE;

		// Iterate over points to extract 1) vertex coordinates and 2) index of
		// vertex with lowest y-coord
		for (int i = 0; i < n; i++)
		{
			Point vertex = points.get(i);
			xCoords[i] = vertex.x;
			yCoords[i] = vertex.y;
			
			if (vertex.y < ymin)
			{
				ymin = vertex.y;
				pStart = i;
				xmin = vertex.x;
			}
			else if (vertex.y == ymin && vertex.x < xmin)
			{
				xmin = vertex.x;
				pStart = i;
			}
		}
		
		// create structure for storing convex hull coordinates
		ArrayList<Point> hull = new ArrayList<Point>();
		
		// p1: index of current hull vertex
		// p2: index of current candidate for next hull vertex
		// p3: index of iterator on point set
		
		int p1 = pStart;
		do
		{
			// coordinates of current convex hull vertex
			int x1 = xCoords[p1];
			int y1 = yCoords[p1];
			
			// coordinates of next vertex candidate
			int p2 = (p1 + 1) % n;
			int x2 = xCoords[p2];
			int y2 = yCoords[p2];
	
			// find the next "wrapping" vertex by computing oriented angle
			int p3 = (p2 + 1) % n;
			do
			{
				int x3 = xCoords[p3];
				int y3 = yCoords[p3];
				
				// if V1-V2-V3 is oriented CW, use V3 as next wrapping candidate
				int det = x1 * (y2 - y3) - y1 * (x2 - x3) + (y3 * x2 - y2 * x3);
				if (det < 0)
				{
					x2 = x3;
					y2 = y3;
					p2 = p3;
				}
				p3 = (p3 + 1) % n;
			} while (p3 != p1);
			
			hull.add(new Point(x1, y1));
			p1 = p2;
		} while (p1 != pStart);
	
		return hull;
	}
}
