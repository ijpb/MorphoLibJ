/**
 * 
 */
package inra.ijpb.geometry;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import ij.gui.PolygonRoi;
import ij.gui.Roi;

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
	 * Computes the centroid of a polygon defined by an ordered list of
	 * vertices.
	 * 
	 * @param vertices
	 *            the ordered list of vertices
	 * @return the centroid of the polygon.
	 */
	public static final Point2D centroid(ArrayList<? extends Point2D> vertices)
	{
		// accumulators
		double sumC = 0;
		double sumX = 0;
		double sumY = 0;
		
		// iterate on vertex pairs
		int n = vertices.size();
		for (int i = 1; i <= n; i++)
		{
			Point2D p1 = vertices.get(i - 1);
			Point2D p2 = vertices.get(i % n);
			double x1 = p1.getX();
			double y1 = p1.getY();
			double x2 = p2.getX();
			double y2 = p2.getY();
			double common = x1 * y2 - x2 * y1;
			
			sumX += (x1 + x2) * common;
			sumY += (y1 + y2) * common;
			sumC += common;
		}
		
		// the area is the sum of the common factors divided by 2, 
		// but we need to divide by 6 for centroid computation, 
		// resulting in a factor 3.
		sumC *= 6 / 2;
		return new Point2D.Double(sumX / sumC, sumY / sumC);
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
	public static final ArrayList<Point> convexHull_jarvis_int(ArrayList<Point> points)
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
			
			ymin = Math.min(ymin, vertex.y);
			if (vertex.y == ymin && vertex.x < xmin)
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

	/**
	 * Converts an ordered list of vertices into an ImageJ Polygon ROI.
	 * 
	 * @param vertices
	 *            an ordered list of vertices
	 * @return the corresponding PolygonRoi
	 */
	public static PolygonRoi createPolygonRoi(ArrayList<Point2D> vertices)
	{
		// allocate memory for data arrays
		int n = vertices.size();
		float[] px = new float[n];
		float[] py = new float[n];
		
		// extract coordinates
		for (int i = 0; i < n; i++)
		{
			Point2D p = vertices.get(i);
			px[i] = (float) p.getX();
			py[i] = (float) p.getY();
		}

		// create ROI data structure
		return new PolygonRoi(px, py, n, Roi.POLYGON);
	}

}
