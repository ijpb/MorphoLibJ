/**
 * 
 */
package inra.ijpb.measure.region2d;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Map;

import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import inra.ijpb.geometry.FeretDiameters;
import inra.ijpb.geometry.PointPair2D;
import inra.ijpb.geometry.Polygon2D;
import inra.ijpb.geometry.Polygons2D;

/**
 * Computes maximum Feret Diameter for each region of a binary or label image.
 * 
 * @author dlegland
 *
 */
public class MaxFeretDiameter extends RegionAnalyzer2D<PointPair2D>
{
	// ==================================================
	// Static methods 
	
	public final static PointPair2D[] maxFeretDiameters(ImageProcessor image, int[] labels, Calibration calib)
	{
		return new MaxFeretDiameter().analyzeRegions(image, labels, calib);
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
	// Implementation of RegionAnalyzer interface

	/**
	 * Converts the result of maximum Feret diameters computation to a
	 * ResultsTable that can be displayed within ImageJ.
	 * 
	 * @param maxDiamsMap
	 *            the map of PointPair2D for each label within a label image
	 * @return a ResultsTable instance
	 */
	public ResultsTable createTable(Map<Integer, PointPair2D> maxDiamsMap)
	{
		// Create data table
		ResultsTable table = new ResultsTable();
	
		// compute ellipse parameters for each region
		for (int label : maxDiamsMap.keySet()) 
		{
			table.incrementCounter();
			table.addLabel(Integer.toString(label));
			
			// add coordinates of origin pixel (IJ coordinate system)
			PointPair2D maxDiam = maxDiamsMap.get(label);
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
	 * Computes diameter between corners of image pixels, so the result is
	 * always greater than or equal to one.
	 * 
	 * @param image
	 *            a label image (8, 16 or 32 bits)
	 * @param labels
	 *            the set of labels within the image
	 * @param calib
	 *            the spatial calibration of the image
	 * @return an array of PointPair2D representing the coordinates of extreme
	 *         points, in calibrated coordinates.
	 */
	public PointPair2D[] analyzeRegions(ImageProcessor image, int[] labels, Calibration calib)
	{
		// Check validity of parameters
		if (image == null)
			return null;

		// Extract spatial calibration of image
		double sx = 1, sy = 1;
		double ox = 0, oy = 0;
		if (calib != null)
		{
			sx = calib.pixelWidth;
			sy = calib.pixelHeight;
			ox = calib.xOrigin;
			oy = calib.yOrigin;
		}
		
		int nLabels = labels.length;

        // For each label, create a list of corner points
		fireStatusChanged(this, "Find Label Corner Points");
        ArrayList<Point2D>[] labelCornerPointsArray = RegionBoundaries.regionsCornersArray(image, labels);
                
        // Compute the oriented box of each set of corner points
        PointPair2D[] labelMaxDiams = new PointPair2D[nLabels];
        fireStatusChanged(this, "Compute feret Diameters");
        for (int i = 0; i < nLabels; i++)
        {
        	this.fireProgressChanged(this, i, nLabels);
        	
        	ArrayList<Point2D> hull = labelCornerPointsArray[i];
    		// calibrate coordinates of hull vertices
    		for (int iv = 0; iv < hull.size(); iv++)
    		{
    			Point2D vertex = hull.get(iv);
    			vertex = new Point2D.Double(vertex.getX() * sx + ox, vertex.getY() * sy + oy);
    			hull.set(iv, vertex);
    		}

    		// compute Feret diameter of calibrated hull
        	labelMaxDiams[i] = FeretDiameters.maxFeretDiameter(hull);
        }
        
        fireProgressChanged(this, 1, 1);
        fireStatusChanged(this, "");
        return labelMaxDiams;
	}
	
	
	/**
	 * Computes Maximum Feret diameter from a single region in a binary image.
	 * 
	 * Computes diameter between corners of image pixels, so the result is
	 * always greater than or equal to one.
	 * 
	 * @param image
	 *            a binary image representing the particle.
	 * @param calib
	 *            the spatial calibration
	 * @return the maximum Feret diameter of the binary region
	 */
	public PointPair2D analyzeBinary(ImageProcessor image, double[] calib)
	{
		ArrayList<Point2D> points = RegionBoundaries.binaryParticleCorners(image);
		Polygon2D convHull = Polygons2D.convexHull(points);

		// calibrate coordinates of convex hull vertices
		for (int i = 0; i < convHull.vertexNumber(); i++)
		{
			Point2D vertex = convHull.getVertex(i);
			vertex = new Point2D.Double(vertex.getX() * calib[0], vertex.getY() * calib[1]);
			convHull.setVertex(i, vertex);
		}
		
		// compute Feret diameter of calibrated vertices
		return FeretDiameters.maxFeretDiameter(convHull.vertices());
	}
}
