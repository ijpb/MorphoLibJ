/**
 * 
 */
package inra.ijpb.measure;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import ij.ImagePlus;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.geometry.Polygons2D;
import inra.ijpb.label.LabelImages;

/**
 * Collection of static methods for computing min and max Feret Diameters.
 * 
 * @author dlegland
 *
 */
public class MaxFeretDiameter extends AlgoStub
{
	// ==================================================
	// Static methods 
	
	/**
	 * Converts the result of maximum Feret diameters computation to a
	 * ResultsTable that can be displayed within ImageJ.
	 * 
	 * @param maxDiamsPairs
	 *            the map of PointPair for each label within a label image
	 * @return a ResultsTable instance
	 */
	public final static ResultsTable asTable(Map<Integer, PointPair> maxDiamsMap)
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
	
	
	// ==================================================
	// Constructor

	/**
	 * Default constructor
	 */
	public MaxFeretDiameter()
	{
	}

	
	// ==================================================
	// Computation methods 

	/**
	 * Computes maximum Feret Diameter for each label of the input label image.
	 * 
	 * @param image
	 *            a label image (8, 16 or 32 bits)
	 * @return a ResultsTable containing oriented box parameters
	 */
	public Map<Integer, PointPair> process(ImagePlus imagePlus)
	{
		// Extract spatial calibration
		Calibration calib = imagePlus.getCalibration();

		// Compute calibrated Feret diameter
		Map<Integer, PointPair> result = process(imagePlus.getProcessor(), calib);
		return result;
	}
	
	
	/**
	 * Computes maximum Feret Diameter for each label of the input label image.
	 * 
	 * Computes diameter between corners of image pixels, so the result is
	 * always greater than or equal to one.
	 * 
	 * @param image
	 *            a label image (8, 16 or 32 bits)
	 * @param calib
	 *            the spatial calibration of the image
	 * @return a ResultsTable containing oriented box parameters
	 */
	public Map<Integer, PointPair> process(ImageProcessor image, Calibration calib)
	{
		// Check validity of parameters
		if (image == null)
			return null;

		// Extract spatial calibration of image
		double sx = 1, sy = 1;
		double ox = 0, oy = 1;
		if (calib != null)
		{
			sx = calib.pixelWidth;
			sy = calib.pixelHeight;
			ox = calib.xOrigin;
			oy = calib.yOrigin;
		}
		
		// extract particle labels
		fireStatusChanged(this, "Find Labels");
		int[] labels = LabelImages.findAllLabels(image);
		int nLabels = labels.length;

        // For each label, create a list of corner points
		fireStatusChanged(this, "Find Label Corner Points");
        ArrayList<Point2D>[] labelCornerPointsArray = RegionBoundaries.regionsCornersArray(image, labels);
                
        // Compute the oriented box of each set of corner points
        Map<Integer, PointPair> labelMaxDiamMap = new TreeMap<Integer, PointPair>();
        fireStatusChanged(this, "Compute feret Diameters");
        for (int i = 0; i < nLabels; i++)
        {
        	this.fireProgressChanged(this, i, nLabels);
        	int label = labels[i];
        	
        	ArrayList<Point2D> hull = labelCornerPointsArray[i];
    		// calibrate coordinates of hull vertices
    		for (int iv = 0; iv < hull.size(); iv++)
    		{
    			Point2D vertex = hull.get(iv);
    			vertex = new Point2D.Double(vertex.getX() * sx + ox, vertex.getY() * sy + oy);
    			hull.set(iv, vertex);
    		}

    		// compute Feret diameter of calibrated hull
        	labelMaxDiamMap.put(label, maxFeretDiameter(hull));
        }
        
        fireProgressChanged(this, 1, 1);
        fireStatusChanged(this, "");
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
	public final static PointPair processBinary(ImageProcessor image, double[] calib)
	{
		ArrayList<Point2D> points = RegionBoundaries.binaryParticleCorners(image);
		ArrayList<Point2D> convHull = Polygons2D.convexHull_jarvis(points);

		// calibrate coordinates of convex hull vertices
		for (int i = 0; i < convHull.size(); i++)
		{
			Point2D vertex = convHull.get(i);
			vertex = new Point2D.Double(vertex.getX() * calib[0], vertex.getY() * calib[1]);
			convHull.set(i, vertex);
		}
		
		// compute Feret diameter of calibrated vertices
		return maxFeretDiameter(convHull);
	}
	
	
	// ==================================================
	// Utility classes 

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
}
