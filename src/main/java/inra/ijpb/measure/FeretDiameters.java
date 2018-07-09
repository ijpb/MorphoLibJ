/**
 * 
 */
package inra.ijpb.measure;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import ij.IJ;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import inra.ijpb.geometry.Polygons2D;
import inra.ijpb.geometry.StraightLine2D;
import inra.ijpb.geometry.Vector2D;
import inra.ijpb.label.LabelImages;

/**
 * @author dlegland
 *
 */
public class FeretDiameters
{
	private final static double TWO_PI = Math.PI * 2;
	
	/**
	 * Converts the result of maximum feret diamters computation to a
	 * ResultsTable that can be displayed within ImageJ.
	 * 
	 * @param maxDiamsPairs
	 *            the map of PointPair for each label within a label image
	 * @return a ResultsTable instance
	 */
	public final static ResultsTable maxFeretDiametersTable(Map<Integer, PointPair> maxDiamsMap)
	{
		// Create data table
		ResultsTable table = new ResultsTable();

		// compute ellipse parameters for each region
		for (int label : maxDiamsMap.keySet()) 
		{
			table.incrementCounter();
			table.addLabel(Integer.toString(label));
			
			// add coordinates of origin pixel (IJ coordinate system)
			PointPair maxDiam = maxDiamsMap.get(label);
			table.addValue("Diameter", maxDiam.diameter());
			table.addValue("Orientation", Math.toDegrees(maxDiam.angle()));
			table.addValue("P1.x", maxDiam.p1.getX());
			table.addValue("P1.y", maxDiam.p1.getY());
			table.addValue("P2.x", maxDiam.p2.getX());
			table.addValue("p2.y", maxDiam.p2.getY());
		}
		
		// return the created array
		return table;
	}
	
	/**
	 * Computes maximum Feret Diameter for each label of the input label image.
	 * 
	 * @param image
	 *            a label image (8, 16 or 32 bits)
	 * @return a ResultsTable containing oriented box parameters
	 */
	public final static Map<Integer, PointPair> maxFeretDiameters(ImageProcessor image)
	{
		// Check validity of parameters
		if (image == null)
			return null;

		// extract particle labels
		IJ.showStatus("Find Labels");
		int[] labels = LabelImages.findAllLabels(image);
		int nLabels = labels.length;

        // For each label, create a list of corner points
		IJ.showStatus("Find Label Corner Points");
//        HashMap<Integer, ArrayList<Point2D>> labelCornerPoints = computeLabelsCorners(image, labels);
        ArrayList<Point2D>[] labelCornerPointsArray = RegionBoundaries.regionsCornersArray(image, labels);
                
        // Compute the oriented box of each set of corner points
        Map<Integer, PointPair> labelMaxDiamMap = new TreeMap<Integer, PointPair>();
		IJ.showStatus("Compute feret Diameters");
        for (int i = 0; i < nLabels; i++)
        {
        	IJ.showProgress(i, nLabels);
        	int label = labels[i];
        	
//        	AngleDiameterPair maxFeretDiam = maxFeretDiameter(labelCornerPointsArray[i]);
        	labelMaxDiamMap.put(label, maxFeretDiameter(labelCornerPointsArray[i]));
        }
        
        IJ.showProgress(1);
        IJ.showStatus("");
        return labelMaxDiamMap;
	}
	
	/**
	 * Computes Maximum Feret diameter from a single particle in a binary image.
	 * 
	 * Computes diameter between corners of image pixels, so the result is
	 * always greater than or equal to one.
	 * 
	 * @param image
	 *            a binary image representing the particle.
	 * @return the maximum Feret diameter of the particle
	 */
	public final static PointPair maxFeretDiameterSingle(ImageProcessor image)
	{
//		ArrayList<Point> points = boundaryPoints(image);
//		ArrayList<Point> convHull = Polygons2D.convexHull_jarvis_int(points);
		ArrayList<Point2D> points = RegionBoundaries.binaryParticleCorners(image);
		ArrayList<Point2D> convHull = Polygons2D.convexHull_jarvis(points);

		return maxFeretDiameter(convHull);
	}
	
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
	public final static PointPair maxFeretDiameter(ArrayList<? extends Point2D> points)
	{
		double distMax = Double.NEGATIVE_INFINITY;
		PointPair maxDiam = null;
		
		for (Point2D p1 : points)
		{
			for (Point2D p2 : points)
			{
				double dist = p1.distance(p2);
				if (dist > distMax)
				{
					maxDiam = new PointPair(p1, p2);
					distMax = dist;
				}
			}
		}
	
		return maxDiam;
	}
	
	/**
	 * Computes Minimum Feret diameter from a single particle in a binary image.
	 * 
	 * @param image
	 *            a binary image representing the particle.
	 * @return the minimum Feret diameter of the particle
	 */
	public final static AngleDiameterPair minFeretDiameterSingle(ImageProcessor image)
	{
		ArrayList<Point2D> points = RegionBoundaries.binaryParticleCorners(image);
		ArrayList<Point2D> convHull = Polygons2D.convexHull_jarvis(points);
	
		return minFeretDiameter(convHull);
	}


	/**
	 * Computes Minimum Feret diameter of a set of points and returns both the
	 * diameter and the corresponding angle.
	 * 
	 * @param points
	 *            a collection of planar points
	 * @return the minimum Feret diameter of the point set
	 */
	public final static AngleDiameterPair minFeretDiameter(ArrayList<? extends Point2D> points)
	{
		return minFeretDiameterNaive(points);
	}
	 
	/**
	 * Computes Minimum Feret diameter of a set of points, using the rotating
	 * caliper algorithm.
	 * 
	 * @param points
	 *            a collection of planar points
	 * @return the minimum Feret diameter of the point set
	 */
	public final static AngleDiameterPair minFeretDiameterRotatingCaliper(ArrayList<? extends Point2D> points)
	{
		// first compute convex hull to simplify
		ArrayList<Point2D> convHull = Polygons2D.convexHull_jarvis(points);
		int n = convHull.size();
		
		// find index of extreme vertices in vertical direction
		int indA = 0;
		int indB = 0;
		double yMin = Double.MAX_VALUE;
		double yMax = Double.MIN_VALUE;
		for (int i = 0; i < n; i++)
		{
			Point2D p = convHull.get(i);
			double y = p.getY();
			if (y < yMin)
			{
				yMin = y;
				indA = i;
			}
			if (y > yMax)
			{
				yMax = y;
				indB = i;
			}
		}
		
		// Caliper A points along the positive x-axis
		Vector2D caliperA = new Vector2D(1, 0);
		// Caliper B points along the negative x-axis
		Vector2D caliperB = new Vector2D(-1, 0);
		
		// initialize result
		double width;
		double widthMin = Double.POSITIVE_INFINITY;
		double angleMin = 0;
		StraightLine2D line;
		
		// Find the direction with minimum width (rotating caliper algorithm)
		double rotatedAngle = 0;
		while (rotatedAngle < Math.PI)
		{
		    // compute the direction vector corresponding to first edge
		    int indA2 = (indA + 1) % n;
		    Point2D pA1 = convHull.get(indA);
		    Point2D pA2 = convHull.get(indA2);
		    Vector2D vectorA = new Vector2D(pA2.getX() - pA1.getX(), pA2.getY() - pA1.getY());
			
		    // compute the direction vector corresponding to second edge
		    int indB2 = (indB + 1) % n;
		    Point2D pB1 = convHull.get(indB);
		    Point2D pB2 = convHull.get(indB2);
		    Vector2D vectorB = new Vector2D(pB2.getX() - pB1.getX(), pB2.getY() - pB1.getY());
		    
		    // Determine the angle between each caliper and the next adjacent edge
		    // in the polygon 
		    double angleA = (Vector2D.angle(caliperA, vectorA) + TWO_PI) % TWO_PI;
		    double angleB = (Vector2D.angle(caliperB, vectorB) + TWO_PI) % TWO_PI;
		    
		    // increment rotatedAngle by the smallest of these angles
		    double angleIncrement = Math.min(angleA, angleB);
		    rotatedAngle += angleIncrement;
		    
		    // compute current width, and update opposite vertex
		    if (angleA < angleB)
		    {
		        line = new StraightLine2D(pA1, pA2);
		        width = line.distance(pB1);
		        indA = indA2;
		    }
		    else
		    {
		        line = new StraightLine2D(pB1, pB2);
		        width = line.distance(pA1);
		        indB = indB2;
		    }

		    // update minimum width and corresponding angle if needed
		    if (width < widthMin)
		    {
		        widthMin = width;
		        angleMin = rotatedAngle;
		    }
		}

		return new AngleDiameterPair(angleMin - Math.PI/2, widthMin);				
	}
	
	/**
	 * Computes Minimum Feret diameter of a set of points, using the rotating
	 * caliper algorithm.
	 * 
	 * @param points
	 *            a collection of planar points
	 * @return the minimum Feret diameter of the point set
	 */
	public final static AngleDiameterPair minFeretDiameterNaive(ArrayList<? extends Point2D> points)
	{
		// first compute convex hull to simplify
		ArrayList<Point2D> convHull = Polygons2D.convexHull_jarvis(points);
		int n = convHull.size();

		// initialize result
		double widthMin = Double.POSITIVE_INFINITY;
		double angleMin = 0;
		StraightLine2D line;

		for (int i = 0; i < n; i++)
		{
			Point2D p1 = convHull.get(i);
			Point2D p2 = convHull.get((i + 1) % n);
			
			// avoid degenerated lines
			if (p1.distance(p2) < 1e-12)
			{
				continue;
			}

			// Compute the width for this polygon edge
			line = new StraightLine2D(p1, p2);
			double width = 0;
			for (Point2D p : convHull)
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
	

	/**
	 * Data structure used to return result of Maximum Feret diameters computation. 
	 *
	 * Simply contains the reference to each extremity.
	 */
	public static class PointPair
	{
		public final Point2D p1;
		public final Point2D p2;
		
		public PointPair(Point2D p1, Point2D p2)
		{
			this.p1 = p1;
			this.p2 = p2;
		}
		
		public double diameter()
		{
			double dx = p2.getX() - p1.getX();
			double dy = p2.getY() - p1.getY();
			return Math.hypot(dx,  dy);
		}
		
		public double angle()
		{
			double dx = p2.getX() - p1.getX();
			double dy = p2.getY() - p1.getY();
			return Math.atan2(dy, dx);
		}
	}
	
	/**
	 * Data structure used to return result of Feret diameters computation. Can
	 * be used to return the result of minimum or maximum diameters computation.
	 * 
	 * @author dlegland
	 *
	 */
	public static class AngleDiameterPair
	{
		/** Angle in radians */
		public double angle;

		/** Diameter computed in the direction of the angle */
		public double diameter;
		
		public AngleDiameterPair(double angle, double diameter)
		{
			this.angle = angle;
			this.diameter = diameter;
		}
	}
}
