/**
 * 
 */
package inra.ijpb.measure.region2d;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import ij.ImagePlus;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.RegionAnalyzer;

/**
 * Measures the area of each region within a binary or label image.
 * 
 * @author dlegland
 *
 */
public class Area extends AlgoStub implements RegionAnalyzer<Double>
{
	// ==================================================
	// Static methods
	
	/**
	 * Counts the number of pixel that composes the particle with the specified
	 * label.
	 * 
	 * @param image
	 *            the input image containing label of particles
	 * @param label
	 *            the label of the region to process
	 * @return the number of pixels of the specified region
	 */
	public static final int countRegionPixels(ImageProcessor image, int label) 
	{
		int width = image.getWidth();
		int height = image.getHeight();

		int count = 0;

		// count all pixels belonging to the particle
		for (int y = 0; y < height; y++) 
		{
			for (int x = 0; x < width; x++) 
			{
				if (((int) image.getf(x, y)) == label)
				{
					count++;
				}
			}
		}
		return count;
	}


	// ==================================================
	// Constructor

	/**
	 * Default constructor
	 */
	public Area()
	{
	}

	
	// ==================================================
	// Computation methods
	
	public Map<Integer, Double> analyzeRegions(ImageProcessor labelImage, Calibration calib)
	{
		int[] labels = LabelImages.findAllLabels(labelImage);
		double[] values = analyzeRegions(labelImage, labels, calib);
		
		// convert the arrays into a map of index-value pairs
		Map<Integer, Double> map = new TreeMap<Integer, Double>();
		for (int i = 0; i < labels.length; i++)
		{
			map.put(labels[i], values[i]);
		}
		
		return map;
	}

	/**
	 * Computes area of each region in input label image, taking into account
	 * the spatial calibration.
	 * 
	 * @param image
	 *            the input image containing label of particles
	 * @param labels
	 *            the array of labels within the image
	 * @param calib
	 *            the calibration of the image
	 * @return an array containing for each region, the area of the
	 *         corresponding region
	 */
	public double[] analyzeRegions(ImageProcessor image, int[] labels, Calibration calib)
	{
		// create associative array to know index of each label
		int nLabels = labels.length;
		HashMap<Integer, Integer> labelIndices = LabelImages.mapLabelIndices(labels);
	
		// compute area of a single pixel
		double pixelArea = calib.pixelWidth * calib.pixelHeight;
		
		// initialize result
		double[] areaList = new double[nLabels];
	
		// size of image
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
	
		// iterate on image pixel configurations
		fireStatusChanged(this, "Measure area...");
		for (int y = 0; y < sizeY; y++) 
		{
			fireProgressChanged(this, y, sizeY);
			for (int x = 0; x < sizeX; x++) 
			{
				int label = (int) image.getf(x, y);
				// do not consider background
				if (label == 0)
					continue;
				
				// retrieve label index from label value
				int labelIndex = labelIndices.get(label);
				
				// update measure for current label
				areaList[labelIndex] += pixelArea;
			}
		}
	
		// clear logs
		fireStatusChanged(this, "");
		fireProgressChanged(this, 1, 1);
		
		// return results
		return areaList;
	}


	// ==================================================
	// Implementation of RegionAnalyzer interface
	
	public ResultsTable computeTable(ImagePlus labelPlus)
	{
		return createTable(analyzeRegions(labelPlus));
	}
	
	/**
	 * Utility method that transforms the mapping between labels and Crofton
	 * perimeter values into a ResultsTable that can be displayed with ImageJ.
	 * 
	 * @param map
	 *            the mapping between labels and crofton perimeter
	 * @return a ResultsTable that can be displayed with ImageJ.
	 */
	public ResultsTable createTable(Map<Integer, Double> map)
	{
		// Initialize a new result table
		ResultsTable table = new ResultsTable();
	
		// Convert all results that were computed during execution of the
		// "computeGeodesicDistanceMap()" method into rows of the results table
		for (int label : map.keySet())
		{
			// add an entry to the resulting data table
			table.incrementCounter();
			table.addLabel(Integer.toString(label));
			
			table.addValue("Perimeter", map.get(label));
		}
	
		return table;
	}

	public Map<Integer, Double> analyzeRegions(ImagePlus labelPlus)
	{
		return analyzeRegions(labelPlus.getProcessor(), labelPlus.getCalibration());
	}
	
}
