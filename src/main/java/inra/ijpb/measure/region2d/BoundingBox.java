/**
 * 
 */
package inra.ijpb.measure.region2d;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import ij.ImagePlus;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.geometry.Box2D;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.RegionAnalyzer;

/**
 * Compute bounding box of each region within a label or binary image.
 * 
 * @author dlegland
 *
 */
public class BoundingBox extends AlgoStub implements RegionAnalyzer<Box2D>
{
	// ==================================================
	// Static methods

	/**
	 * Compute bounding box of each label in input stack and returns the result
	 * as an array of double for each label.
	 * 
	 * @param labelImage
	 *            the input image containing label of particles
	 * @param labels an array of unique labels in image
	 * @return a data table containing for each labeled particle the extent in
	 *         each dimension
	 */
	public final static double[][] boundingBox(ImageProcessor labelImage, int[] labels)
	{
        // create associative array to know index of each label
        HashMap<Integer, Integer> labelIndices = LabelImages.mapLabelIndices(labels);

        // initialize result
		int nLabels = labels.length;
		double[][] boxes = new double[nLabels][6];
		for (int i = 0; i < nLabels; i++)
		{
			boxes[i][0] = Double.POSITIVE_INFINITY;
			boxes[i][1] = Double.NEGATIVE_INFINITY;
			boxes[i][2] = Double.POSITIVE_INFINITY;
			boxes[i][3] = Double.NEGATIVE_INFINITY;
		}

		
		// size of image
		int sizeX = labelImage.getWidth();
		int sizeY = labelImage.getHeight();

		// iterate on image voxels to update bounding boxes
		for (int y = 0; y < sizeY; y++)
		{
			for (int x = 0; x < sizeX; x++)
			{
				int label = labelImage.get(x, y);
				// do not consider background
				if (label == 0)
					continue;
				int labelIndex = labelIndices.get(label);

				// update bounding box of current label
				boxes[labelIndex][0] = min(boxes[labelIndex][0], x);
				boxes[labelIndex][1] = max(boxes[labelIndex][1], x);
				boxes[labelIndex][2] = min(boxes[labelIndex][2], y);
				boxes[labelIndex][3] = max(boxes[labelIndex][3], y);
			}
		}
        
		return boxes;
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
	// Computation methods 


	public Map<Integer, Box2D> analyzeRegions(ImageProcessor labelImage, Calibration calib)
	{
		int[] labels = LabelImages.findAllLabels(labelImage);
		Box2D[] boxes = analyzeRegions(labelImage, labels, calib);
		
		// convert the arrays into a map of index-value pairs
		Map<Integer, Box2D> map = new TreeMap<Integer, Box2D>();
		for (int i = 0; i < labels.length; i++)
		{
			map.put(labels[i], boxes[i]);
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

	// ==================================================
	// Implementation of RegionAnalyzer interface

	public ResultsTable computeTable(ImagePlus labelPlus)
	{
		return createTable(analyzeRegions(labelPlus));
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

	public Map<Integer, Box2D> analyzeRegions(ImagePlus labelPlus)
	{
		return analyzeRegions(labelPlus.getProcessor(), labelPlus.getCalibration());
	}

}
