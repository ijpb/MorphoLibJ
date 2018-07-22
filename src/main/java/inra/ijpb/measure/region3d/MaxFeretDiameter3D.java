/**
 * 
 */
package inra.ijpb.measure.region3d;

import java.util.ArrayList;
import java.util.Map;

import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.ImageStack;
import inra.ijpb.geometry.Point3D;
import inra.ijpb.geometry.PointPair3D;

/**
 * Computes maximum Feret Diameter for each region of a 3D binary or label
 * image.
 * 
 * @author dlegland
 *
 */
public class MaxFeretDiameter3D extends RegionAnalyzer3D<PointPair3D>
{
	// ==================================================
	// Static methods 
	
	public final static PointPair3D[] maxFeretDiameters(ImageStack image, int[] labels, Calibration calib)
	{
		return new MaxFeretDiameter3D().analyzeRegions(image, labels, calib);
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
	public final static PointPair3D maxFeretDiameter(ArrayList<? extends Point3D> points)
	{
		double distMax = Double.NEGATIVE_INFINITY;
		PointPair3D maxDiam = null;
		
		for (Point3D p1 : points)
		{
			for (Point3D p2 : points)
			{
				double dist = p1.distance(p2);
				if (dist > distMax)
				{
					maxDiam = new PointPair3D(p1, p2);
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
	public MaxFeretDiameter3D()
	{
	}

	// ==================================================
	// Implementation of RegionAnalyzer interface

	/**
	 * Converts the result of maximum Feret diameters computation to a
	 * ResultsTable that can be displayed within ImageJ.
	 * 
	 * @param maxDiamsMap
	 *            the map of PointPair3D for each label within a label image
	 * @return a ResultsTable instance
	 */
	public ResultsTable createTable(Map<Integer, PointPair3D> maxDiamsMap)
	{
		// Create data table
		ResultsTable table = new ResultsTable();
	
		// compute ellipse parameters for each region
		for (int label : maxDiamsMap.keySet()) 
		{
			table.incrementCounter();
			table.addLabel(Integer.toString(label));
			
			// add coordinates of origin pixel (IJ coordinate system)
			PointPair3D maxDiam = maxDiamsMap.get(label);
			table.addValue("Diameter", maxDiam.diameter());
			table.addValue("P1.x", maxDiam.p1.getX());
			table.addValue("P1.y", maxDiam.p1.getY());
			table.addValue("P1.z", maxDiam.p1.getZ());
			table.addValue("P2.x", maxDiam.p2.getX());
			table.addValue("p2.y", maxDiam.p2.getY());
			table.addValue("p2.z", maxDiam.p2.getZ());
		}
		
		// return the created array
		return table;
	}
	
	/**
	 * Computes maximum Feret Diameter for each label of the input label image.
	 * 
	 * Computes diameter between corners of image pixels, so the result is
	 * always greater than or equal to one.
	 * 
	 * @param image
	 *            a label image (8, 16 or 32 bits)
	 * @param labels
	 *            the set of labels within the image
	 * @param calib
	 *            the spatial calibration of the image
	 * @return an array of PointPair3D representing the coordinates of extreme
	 *         points, in calibrated coordinates.
	 */
	public PointPair3D[] analyzeRegions(ImageStack image, int[] labels, Calibration calib)
	{
		// Check validity of parameters
		if (image == null)
			return null;

		// Extract spatial calibration of image
		double sx = 1, sy = 1, sz = 1;
		double ox = 0, oy = 0, oz = 0;
		if (calib != null)
		{
			sx = calib.pixelWidth;
			sy = calib.pixelHeight;
			sz = calib.pixelDepth;
			ox = calib.xOrigin;
			oy = calib.yOrigin;
			oz = calib.zOrigin;
		}
		
		int nLabels = labels.length;

        // For each label, create a list of corner points
		fireStatusChanged(this, "Find Label Corner Points");
        ArrayList<Point3D>[] labelCornerPointsArray = RegionBoundaries3D.regionsCornersArray(image, labels);
                
        // Compute the oriented box of each set of corner points
        PointPair3D[] labelMaxDiams = new PointPair3D[nLabels];
        fireStatusChanged(this, "Compute feret Diameters");
        for (int i = 0; i < nLabels; i++)
        {
        	this.fireProgressChanged(this, i, nLabels);
        	
        	ArrayList<Point3D> hull = labelCornerPointsArray[i];
    		// calibrate coordinates of hull vertices
    		for (int iv = 0; iv < hull.size(); iv++)
    		{
    			Point3D vertex = hull.get(iv);
    			vertex = new Point3D(vertex.getX() * sx + ox, vertex.getY() * sy + oy, vertex.getZ() * sz + oz);
    			hull.set(iv, vertex);
    		}

    		// compute Feret diameter of calibrated hull
        	labelMaxDiams[i] = maxFeretDiameter(hull);
        }
        
        fireProgressChanged(this, 1, 1);
        fireStatusChanged(this, "");
        return labelMaxDiams;
	}
	
	
//	/**
//	 * Computes Maximum Feret diameter from a single region in a binary image.
//	 * 
//	 * Computes diameter between corners of image pixels, so the result is
//	 * always greater than or equal to one.
//	 * 
//	 * @param image
//	 *            a binary image representing the particle.
//	 * @param calib
//	 *            the spatial calibration
//	 * @return the maximum Feret diameter of the binary region
//	 */
//	public PointPair3D analyzeBinary(ImageStack image, double[] calib)
//	{
//		ArrayList<Point2D> points = RegionBoundaries.binaryParticleCorners(image);
//		ArrayList<Point2D> convHull = Polygons2D.convexHull_jarvis(points);
//
//		// calibrate coordinates of convex hull vertices
//		for (int i = 0; i < convHull.size(); i++)
//		{
//			Point2D vertex = convHull.get(i);
//			vertex = new Point2D.Double(vertex.getX() * calib[0], vertex.getY() * calib[1]);
//			convHull.set(i, vertex);
//		}
//		
//		// compute Feret diameter of calibrated vertices
//		return maxFeretDiameter(convHull);
//	}
}
