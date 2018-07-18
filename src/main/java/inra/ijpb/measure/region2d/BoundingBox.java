/**
 * 
 */
package inra.ijpb.measure.region2d;

import java.util.HashMap;
import java.util.Map;

import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import inra.ijpb.geometry.Box2D;
import inra.ijpb.label.LabelImages;

/**
 * Compute bounding box of each region within a label or binary image.
 * 
 * @author dlegland
 *
 */
public class BoundingBox extends RegionAnalyzer2D<Box2D>
{
	// ==================================================
	// Static methods

	/**
	 * Compute bounding box of each region in input image and returns the result
	 * as an array of Box2D for each label.
	 * 
	 * @param labelImage
	 *            the input image containing label of region
	 * @param labels
	 *            an array of unique labels in image
	 * @param calib
	 *            the calibration of the image
	 * @return an array containing the bounding boxes of each region
	 */
	public final static Box2D[] boundingBoxes(ImageProcessor labelImage, int[] labels, Calibration calib)
	{
        return new BoundingBox().analyzeRegions(labelImage, labels, calib);
	}
	
	// ==================================================
	// Constructor

	/**
	 * Default constructor
	 */
	public BoundingBox()
	{
	}

	
	// ==================================================
	// Implementation of RegionAnalyzer interface

	/**
	 * Computes inertia ellipse of each region in input label image.
	 * 
	 * @param image
	 *            the input image containing label of particles
	 * @param labels
	 *            the array of labels within the image
	 * @param calib
	 *            the calibration of the image
	 * @return an array of Box2D representing the calibrated coordinates of
	 *         the inertia ellipse of each region
	 */
	public Box2D[] analyzeRegions(ImageProcessor image, int[] labels, Calibration calib)
	{
		// size of image
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();

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
		double[] xmin = new double[nLabels];
		double[] xmax = new double[nLabels];
		double[] ymin = new double[nLabels];
		double[] ymax = new double[nLabels];
		
		// initialize to extreme values
		for (int i = 0; i < nLabels; i++)
		{
			xmin[i] = Double.POSITIVE_INFINITY;
			xmax[i] = Double.NEGATIVE_INFINITY;
			ymin[i] = Double.POSITIVE_INFINITY;
			ymax[i] = Double.NEGATIVE_INFINITY;
		}

		// compute extreme coordinates of each region
    	fireStatusChanged(this, "Compute bounds");
		for (int y = 0; y < sizeY; y++) 
		{
			for (int x = 0; x < sizeX; x++)
			{
				int label = (int) image.getf(x, y);
				if (label == 0)
					continue;

				int index = labelIndices.get(label);
				
				xmin[index] = Math.min(xmin[index], x);
				xmax[index] = Math.max(xmax[index], x);
				ymin[index] = Math.min(ymin[index], y);
				ymax[index] = Math.max(ymax[index], y);
			}
		}

		// create bounding box instances
		Box2D[] boxes = new Box2D[nLabels];
		for (int i = 0; i < nLabels; i++)
		{
			boxes[i] = new Box2D(
					xmin[i] * sx + ox, (xmax[i] + 1) * sx + ox,
					ymin[i] * sy + oy, (ymax[i] + 1) * sy + oy);
		}
		return boxes;
	}

	/**
	 * Utility method that transforms the mapping between labels and inertia
	 * boxes instances into a ResultsTable that can be displayed with ImageJ.
	 * 
	 * @param map
	 *            the mapping between labels and Inertia Box2Ds
	 * @return a ResultsTable that can be displayed with ImageJ.
	 */
	public ResultsTable createTable(Map<Integer, Box2D> map)
	{
		// Initialize a new result table
		ResultsTable table = new ResultsTable();
	
		// Convert all results that were computed during execution of the
		// "computeGeodesicDistanceMap()" method into rows of the results table
		for (int label : map.keySet())
		{
			// current diameter
			Box2D box = map.get(label);
			
			// add an entry to the resulting data table
			table.incrementCounter();
			table.addLabel(Integer.toString(label));
			
			// coordinates of centroid
			table.addValue("Box2D.XMin", box.getXMin());
			table.addValue("Box2D.XMax", box.getXMax());
			table.addValue("Box2D.YMin", box.getYMin());
			table.addValue("Box2D.YMax", box.getYMax());
		}
	
		return table;
	}
}
