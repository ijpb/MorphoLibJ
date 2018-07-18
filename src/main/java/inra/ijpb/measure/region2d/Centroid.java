/**
 * 
 */
package inra.ijpb.measure.region2d;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import inra.ijpb.label.LabelImages;

/**
 * Computes centroid position of regions within binary or label images.
 * 
 * @author dlegland
 *
 */
public class Centroid extends RegionAnalyzer2D<Point2D>
{
	// ==================================================
	// Static methods
	
	/**
	 * Computes centroid of each label in input image and returns the result as
	 * an array of double for each label.
	 * 
	 * @param labelImage
	 *            the input image containing label of particles
	 * @param labels
	 *            the array of unique labels in image the number of directions
	 *            to process, either 2 or 4
	 * @param calib
	 *            the calibration of the image
	 * @return an array containing for each label, the coordinates of the
	 *         centroid, in pixel coordinates
	 */
	public static final Point2D[] centroids(ImageProcessor labelImage, int[] labels, Calibration calib) 
	{
		return new Centroid().analyzeRegions(labelImage, labels, calib);
	}
	
	/**
	 * Computes centroid of each label in input image and returns the result as
	 * an array of double for each label. This version does not take into
	 * account the spatial calibration, and returns the centroids in pixel
	 * coordinates.
	 * 
	 * @param labelImage
	 *            the input image containing label of particles
	 * @param labels
	 *            the array of unique labels in image the number of directions
	 *            to process, either 2 or 4
	 * @return an array containing for each label, the coordinates of the
	 *         centroid, in pixel coordinates
	 */
	public static final double[][] centroids(ImageProcessor labelImage, int[] labels) 
	{
		// create associative array to know index of each label
		int nLabels = labels.length;
        HashMap<Integer, Integer> labelIndices = LabelImages.mapLabelIndices(labels);

		// allocate memory for result
		int[] counts = new int[nLabels];
		double[][] centroids = new double[nLabels][2];

		// compute centroid of each region
		int sizeX = labelImage.getWidth();
		int sizeY = labelImage.getHeight();
		for (int y = 0; y < sizeY; y++) 
		{
			for (int x = 0; x < sizeX; x++)
			{
				int label = (int) labelImage.getf(x, y);
				if (label == 0)
					continue;

				// do not process labels that are not in the input list 
				if (!labelIndices.containsKey(label))
					continue;
				
				int index = labelIndices.get(label);
				centroids[index][0] += x;
				centroids[index][1] += y;
				counts[index]++;
			}
		}

		// normalize by number of pixels in each region
		for (int i = 0; i < nLabels; i++)
		{
			centroids[i][0] /= counts[i];
			centroids[i][1] /= counts[i];
		}

		return centroids;
	}
	

	// ==================================================
	// Constructor

	/**
	 * Default constructor
	 */
	public Centroid()
	{
	}

	
	// ==================================================
	// Implementation of RegionAnalyzer interface

	/**
	 * Utility method that transforms the mapping between labels and inertia
	 * ellipses instances into a ResultsTable that can be displayed with ImageJ.
	 * 
	 * @param map
	 *            the mapping between labels and Inertia Point2Ds
	 * @return a ResultsTable that can be displayed with ImageJ.
	 */
	public ResultsTable createTable(Map<Integer, Point2D> map)
	{
		// Initialize a new result table
		ResultsTable table = new ResultsTable();
	
		// Convert all results that were computed during execution of the
		// "computeGeodesicDistanceMap()" method into rows of the results table
		for (int label : map.keySet())
		{
			// current diameter
			Point2D point = map.get(label);
			
			// add an entry to the resulting data table
			table.incrementCounter();
			table.addLabel(Integer.toString(label));
			
			// coordinates of centroid
			table.addValue("Centroid.X", point.getX());
			table.addValue("Centroid.Y", point.getY());
		}
	
		return table;
	}

	
	/**
	 * Computes centroid of each region in input label image.
	 * 
	 * @param image
	 *            the input image containing label of particles
	 * @param labels
	 *            the array of labels within the image
	 * @param calib
	 *            the calibration of the image
	 * @return an array of Point2D representing the calibrated centroid coordinates 
	 */
	public Point2D[] analyzeRegions(ImageProcessor image, int[] labels, Calibration calib)
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

    	fireStatusChanged(this, "Compute centroids");
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
    	Point2D[] points = new Point2D[nLabels];
		for (int i = 0; i < nLabels; i++)
		{
			points[i] = new Point2D.Double(cx[i] / counts[i] + ox, cy[i] / counts[i] + oy);
		}

		return points;
	}
}
