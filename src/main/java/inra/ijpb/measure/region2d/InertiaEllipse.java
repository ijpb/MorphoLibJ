/**
 * 
 */
package inra.ijpb.measure.region2d;

import static java.lang.Math.sqrt;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import ij.ImagePlus;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import inra.ijpb.label.LabelImages;

/**
 * Compute parameters of inertia ellipse from label images.
 * 
 * @author dlegland
 *
 */
public class InertiaEllipse
{
	// ==================================================
	// Static methods 
	
	/**
	 * Utility method that transforms the mapping between labels and inertia
	 * ellipses instances into a ResultsTable that can be displayed with ImageJ.
	 * 
	 * @param map
	 *            the mapping between labels and Inertia Ellipses
	 * @return a ResultsTable that can be displayed with ImageJ.
	 */
	public static ResultsTable asTable(Map<Integer, InertiaEllipse> map)
	{
		// Initialize a new result table
		ResultsTable table = new ResultsTable();
	
		// Convert all results that were computed during execution of the
		// "computeGeodesicDistanceMap()" method into rows of the results table
		for (int label : map.keySet())
		{
			// current diameter
			InertiaEllipse ellipse = map.get(label);
			
			// add an entry to the resulting data table
			table.incrementCounter();
			table.addValue("Label", label);
			
			// coordinates of centroid
			table.addValue("Centroid.X", ellipse.center.getX());
			table.addValue("Centroid.Y", ellipse.center.getY());
			
			// ellipse size
			table.addValue("Ellipse.Radius1", ellipse.radius1);
			table.addValue("Ellipse.Radius2", ellipse.radius1);

			// ellipse orientation (degrees)
			table.addValue("Ellipse.Orientation", ellipse.orientation);
		}
	
		return table;
	}	
	
	public final static Map<Integer, InertiaEllipse> compute(ImagePlus labelPlus)
	{
		return compute(labelPlus.getProcessor(), labelPlus.getCalibration());
	}

	public final static Map<Integer, InertiaEllipse> compute(ImageProcessor labelImage, Calibration calib)
	{
		int[] labels = LabelImages.findAllLabels(labelImage);
		InertiaEllipse[] ellipses = compute(labelImage, labels, calib);
		
		// convert the arrays into a map of index-value pairs
		Map<Integer, InertiaEllipse> map = new TreeMap<Integer, InertiaEllipse>();
		for (int i = 0; i < labels.length; i++)
		{
			map.put(labels[i], ellipses[i]);
		}
		
		return map;
	}
	
	/**
	 * Computes inertia ellipse of each region in input label image.
	 * 
	 * @param image
	 *            the input image containing label of particles
	 * @param labels
	 *            the array of labels within the image
	 * @param calib
	 *            the calibration of the image
	 * @return an ResultsTable containing for each label, the parameters of the
	 *         inertia ellipsoid, in pixel coordinates
	 */
	public final static InertiaEllipse[] compute(ImageProcessor image, int[] labels, Calibration calib)
	{
		// Check validity of parameters
		if (image == null)
			return null;

		// size of image
		int width = image.getWidth();
		int height = image.getHeight();

		// Extract spatial calibration
		double sx = 1, sy = 1;
		double ox = 0, oy = 0;
		if (calib != null)
		{
			sx = calib.pixelWidth;
			sy = calib.pixelHeight;
			ox = calib.xOrigin;
			oy = calib.yOrigin;
		}
		
		// create associative array to know index of each label
        HashMap<Integer, Integer> labelIndices = LabelImages.mapLabelIndices(labels);

		// allocate memory for result
		int nLabels = labels.length;
		int[] counts = new int[nLabels];
		double[] cx = new double[nLabels];
		double[] cy = new double[nLabels];
		double[] Ixx = new double[nLabels];
		double[] Iyy = new double[nLabels];
		double[] Ixy = new double[nLabels];

		// compute centroid of each region
		for (int y = 0; y < height; y++) 
		{
			for (int x = 0; x < width; x++)
			{
				int label = (int) image.getf(x, y);
				if (label == 0)
					continue;

				int index = labelIndices.get(label);
				cx[index] += x * sx;
				cy[index] += y * sy;
				counts[index]++;
			}
		}

		// normalize by number of pixels in each region
		for (int i = 0; i < nLabels; i++)
		{
			cx[i] = cx[i] / counts[i];
			cy[i] = cy[i] / counts[i];
		}

		// compute centered inertia matrix of each label
		for (int y = 0; y < height; y++) 
		{
			for (int x = 0; x < width; x++)
			{
				int label = image.get(x, y);
				if (label == 0)
					continue;

				int index = labelIndices.get(label);
				double x2 = x * sx - cx[index];
				double y2 = y * sy - cy[index];
				Ixx[index] += x2 * x2;
				Ixy[index] += x2 * y2;
				Iyy[index] += y2 * y2;
			}
		}

		// normalize by number of pixels in each region
		for (int i = 0; i < nLabels; i++)
		{
			Ixx[i] = Ixx[i] / counts[i] + sx / 12.0;
			Ixy[i] = Ixy[i] / counts[i];
			Iyy[i] = Iyy[i] / counts[i] + sy / 12.0;
		}

		// Create array of result
		InertiaEllipse[] ellipses = new InertiaEllipse[nLabels];
		
		// compute ellipse parameters for each region
		final double sqrt2 = sqrt(2);
		for (int i = 0; i < nLabels; i++) 
		{
			double xx = Ixx[i];
			double xy = Ixy[i];
			double yy = Iyy[i];

			// compute ellipse semi-axes lengths
			double common = sqrt((xx - yy) * (xx - yy) + 4 * xy * xy);
			double ra = sqrt2 * sqrt(xx + yy + common);
			double rb = sqrt2 * sqrt(xx + yy - common);

			// compute ellipse angle and convert into degrees
			double theta = Math.toDegrees(Math.atan2(2 * xy, xx - yy) / 2);

			Point2D center = new Point2D.Double(cx[i] + sx / 2 + ox, cy[i] + sy / 2 + oy);
			ellipses[i] = new InertiaEllipse(center, ra, rb, theta);
		}

		return ellipses;
	}


	
	// ==================================================
	// Class variables
	
	/**
	 * The inertia center of the region.
	 */
	Point2D center;
	
	double radius1;
	
	double radius2;
	
	/**
	 * The orientation of the main axis, in degrees.
	 */
	double orientation;
	
	
	// ==================================================
	// Constructors
	
	public InertiaEllipse(Point2D center, double r1, double r2, double theta)
	{
		this.center = center;
		this.radius1 = r1;
		this.radius2 = r2;
		this.orientation = theta;
	}
	
}

