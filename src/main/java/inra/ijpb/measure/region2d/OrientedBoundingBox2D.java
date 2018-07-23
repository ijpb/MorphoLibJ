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
import inra.ijpb.geometry.OrientedBox2D;
import inra.ijpb.geometry.Polygons2D;
import inra.ijpb.geometry.StraightLine2D;

/**
 * @author dlegland
 *
 */
public class OrientedBoundingBox2D extends RegionAnalyzer2D<OrientedBox2D>
{
	// ====================================================
	// Static methods

	/**
	 * Computes the object-oriented bounding box of a set of points.
	 * 
	 * @param points
	 *            a list of points (not necessarily ordered)
	 * @return the oriented box of this set of points.
	 */
	public static final OrientedBox2D orientedBoundingBox(ArrayList<? extends Point2D> points)
	{
		ArrayList<Point2D> convexHull = Polygons2D.convexHull_jarvis(points);
		
		// compute convex hull centroid
		Point2D center = Polygons2D.centroid(convexHull);
		double cx = center.getX();
		double cy = center.getY();
		
		AngleDiameterPair minFeret = minFeretDiameter(convexHull);
		
		// recenter the convex hull
		ArrayList<Point2D> centeredHull = new ArrayList<Point2D>(convexHull.size());
		for (Point2D p : convexHull)
		{
			centeredHull.add(new Point2D.Double(p.getX() - cx, p.getY() - cy));
		}
		
		// orientation of the main axis
		// pre-compute trigonometric functions
		double cot = Math.cos(minFeret.angle);
		double sit = Math.sin(minFeret.angle);

		// compute elongation in direction of rectangle length and width
		double xmin = Double.MAX_VALUE;
		double ymin = Double.MAX_VALUE;
		double xmax = Double.MIN_VALUE;
		double ymax = Double.MIN_VALUE;
		for (Point2D p : centeredHull)
		{
			// coordinates of current point
			double x = p.getX(); 
			double y = p.getY();
			
			// compute rotated coordinates
			double x2 = x * cot + y * sit; 
			double y2 = - x * sit + y * cot;
			
			// update bounding box
			xmin = Math.min(xmin, x2);
			ymin = Math.min(ymin, y2);
			xmax = Math.max(xmax, x2);
			ymax = Math.max(ymax, y2);
		}
		
		// position of the center with respect to the centroid compute before
		double dl = (xmax + xmin) / 2;
		double dw = (ymax + ymin) / 2;

		// change coordinates from rectangle to user-space
		double dx  = dl * cot - dw * sit;
		double dy  = dl * sit + dw * cot;

		// coordinates of oriented box center
		cx += dx;
		cy += dy;

		// size of the rectangle
		double length = ymax - ymin;
		double width  = xmax - xmin;
		
		// store angle in degrees, between 0 and 180
		double angle = (Math.toDegrees(minFeret.angle) + 270) % 180;

		// Store results in a new instance of OrientedBox2D
		return new OrientedBox2D(cx, cy, length, width, angle);
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
		// TODO: create specific class
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

	// ====================================================
	// Constructor

	/**
	 * Default constructor.
	 */
	public OrientedBoundingBox2D()
	{
	}
	
	// ====================================================
	// Implementation of RegionAnalyzer2D interface

	@Override
	public ResultsTable createTable(Map<Integer, OrientedBox2D> results)
	{
		// Create data table
		ResultsTable table = new ResultsTable();

		// compute ellipse parameters for each region
		int nDigits = ((int) Math.log10(results.size())) + 1;
		for (int label : results.keySet()) 
		{
			table.incrementCounter();
			table.addLabel(String.format("lbl-%0" + nDigits +"d", label));
			
			// add new row containing parameters of oriented box
			OrientedBox2D obox = results.get(label);
			Point2D center = obox.center();
			table.addValue("Box.Center.X", 	center.getX());
			table.addValue("Box.Center.Y",	center.getY());
			table.addValue("Box.Length", 	obox.length());
			table.addValue("Box.Width", 	obox.width());
			table.addValue("Box.Orientation", obox.orientation());
		}

		return table;
	}

	@Override
	public OrientedBox2D[] analyzeRegions(ImageProcessor image, int[] labels,
			Calibration calib)
	{
		// TODO: take into account spatial calibration
		
        // For each label, create a list of corner points
		this.fireStatusChanged(this, "Find Label Corner Points");
        ArrayList<Point2D>[] labelCornerPointsArray = RegionBoundaries.regionsCornersArray(image, labels);
                
        // allocate memory for result
		int nLabels = labels.length;
        OrientedBox2D[] boxes = new OrientedBox2D[nLabels];

        // Compute the oriented box of each set of corner points
		this.fireStatusChanged(this, "Compute oriented boxes");
        for (int i = 0; i < nLabels; i++)
        {
        	this.fireProgressChanged(this, i, nLabels);
        	boxes[i] = orientedBoundingBox(labelCornerPointsArray[i]);
        }
        
		this.fireStatusChanged(this, "");
    	this.fireProgressChanged(this, 1, 1);
        
        return boxes;
	}

	
	// ====================================================
	// Inner class used for storing result of Minimum Feret Diameter

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
