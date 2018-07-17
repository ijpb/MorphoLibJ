/**
 * 
 */
package inra.ijpb.measure.region2d;

import java.util.ArrayList;
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
 * Computes perimeter of regions within label images using discrete version of
 * the Crofton formula.
 * 
 * @author dlegland
 *
 */
public class CroftonPerimeter extends AlgoStub implements RegionAnalyzer<Double>
{
	/**
	 * The number of direction to use for computing intersections. Must be
	 * either 2 or 4.
	 */
	int nDirs = 4;
	
	/**
	 * 
	 */
	boolean countBorder;
	
	// ==================================================
	// Constructor

	/**
	 * Default constructor
	 */
	public CroftonPerimeter()
	{
	}

	/**
	 * Constructor that specifies the number of directions to use. Must be 2 or
	 * 4.
	 * 
	 * @param nDirs
	 *            the number of directions for computing perimeter. Must be 2 or
	 *            4.
	 */
	public CroftonPerimeter(int nDirs)
	{
		if (nDirs != 2 && nDirs != 4)
		{
			throw new IllegalArgumentException("Number of directions must be either 2 or 4");
		}
		
		this.nDirs = nDirs;
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

	public Map<Integer, Double> analyzeRegions(ImageProcessor labelImage, Calibration calib)
	{
		int[] labels = LabelImages.findAllLabels(labelImage);
		Double[] perims = analyzeRegions(labelImage, labels, calib);
		
		// convert the arrays into a map of index-value pairs
		Map<Integer, Double> map = new TreeMap<Integer, Double>();
		for (int i = 0; i < labels.length; i++)
		{
			map.put(labels[i], perims[i]);
		}
		
		return map;
	}
	/**
	 * Computes perimeter of each region in input label image.
	 * 
	 * @param image
	 *            the input image containing label of particles
	 * @param labels
	 *            the array of labels within the image
	 * @param calib
	 *            the calibration of the image
	 * @return an array containing for each region, an estimate of the region perimeter
	 */
	public Double[] analyzeRegions(ImageProcessor image, int[] labels, Calibration calib)
	{
		// create associative array to know index of each label
		int nLabels = labels.length;
		HashMap<Integer, Integer> labelIndices = LabelImages.mapLabelIndices(labels);

		// pre-compute LUT corresponding to resolution and number of directions
		fireStatusChanged(this, "Compute LUT...");
		double[] lut = computePerimeterLut(calib);

		// initialize result
		Double[] perimeters = new Double[nLabels];
		for (int i = 0; i< nLabels; i++)
		{ 
			perimeters[i] = new Double(0.0);
		}

		// size of image
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();

		// for each configuration of 2x2 pixels, we identify the labels
		ArrayList<Integer> localLabels = new ArrayList<Integer>(8);

		// iterate on image pixel configurations
		fireStatusChanged(this, "Measure perimeter...");
		for (int y = 0; y < sizeY - 1; y++) 
		{
			fireProgressChanged(this, y, sizeY);
			for (int x = 0; x < sizeX - 1; x++) 
			{

				// identify labels in current config
				localLabels.clear();
				for (int y2 = y; y2 <= y + 1; y2++) 
				{
					for (int x2 = x; x2 <= x + 1; x2++)
					{
						int label = (int) image.getf(x2, y2);
						// do not consider background
						if (label == 0)
							continue;
						// keep only one instance of each label
						if (!localLabels.contains(label))
							localLabels.add(label);
					}
				}

				// if no label in local configuration contribution is zero
				if (localLabels.size() == 0)
				{
					continue;
				}

				// For each label, compute binary confi
				for (int label : localLabels) {
					// Compute index of local configuration
					int index = 0;
					index += (int) image.getf(x, y) == label ? 1 : 0;
					index += (int) image.getf(x + 1, y) == label ? 2 : 0;
					index += (int) image.getf(x, y + 1) == label ? 4 : 0;
					index += (int) image.getf(x + 1, y + 1) == label ? 8 : 0;

					// retrieve label index from label value
					int labelIndex = labelIndices.get(label);

					// update measure for current label
					perimeters[labelIndex] += lut[index];
				}
			}
		}

		fireStatusChanged(this, "");
		fireProgressChanged(this, 1, 1);
		
		return perimeters;
	}
	
	/**
	 * Computes the Look-up table that is used to compute perimeter. The result
	 * is an array with 16 entries, each entry corresponding to a binary 2-by-2
	 * configuration of pixels.
	 * 
	 * @param calib
	 *            the calibration of the image
	 * @return an array containing for each 2-by-2 configuration index, the
	 *         corresponding contribution to perimeter estimate
	 */
	private double[] computePerimeterLut(Calibration calib)
	{
		// distances between a pixel and its neighbors.
		// di refer to orthogonal neighbors
		// dij refer to diagonal neighbors
		double d1 = calib.pixelWidth;
		double d2 = calib.pixelHeight;
		double d12 = Math.hypot(d1, d2);
		double area = d1 * d2;

		// weights associated to each direction, computed only for four
		// directions
		double[] weights = null;
		if (nDirs == 4)
		{
			weights = computeDirectionWeightsD4(d1, d2);
		}

		// initialize output array (2^(2*2) = 16 configurations in 2D)
		final int nConfigs = 16;
		double[] tab = new double[nConfigs];

		// loop for each tile configuration
		for (int i = 0; i < nConfigs; i++)
		{
			// create the binary image representing the 2x2 tile
			boolean[][] im = new boolean[2][2];
			im[0][0] = (i & 1) > 0;
			im[0][1] = (i & 2) > 0;
			im[1][0] = (i & 4) > 0;
			im[1][1] = (i & 8) > 0;

			// contributions for isothetic directions
			double ke1, ke2;

			// contributions for diagonal directions
			double ke12;

			// iterate over the 4 pixels within the configuration
			for (int y = 0; y < 2; y++)
			{
				for (int x = 0; x < 2; x++)
				{
					if (!im[y][x])
						continue;

					// divides by two to convert intersection count to projected
					// diameter
					ke1 = im[y][1 - x] ? 0 : (area / d1) / 2;
					ke2 = im[1 - y][x] ? 0 : (area / d2) / 2;

					if (nDirs == 2) 
					{
						// Count only orthogonal directions
						// divides by two for average, and by two for
						// multiplicity
						tab[i] += (ke1 + ke2) / 4;

					} 
					else if (nDirs == 4) 
					{
						// compute contribution of diagonal directions
						ke12 = im[1 - y][1 - x] ? 0 : (area / d12) / 2;

						// Decomposition of Crofton formula on 4 directions,
						// taking into account multiplicities
						tab[i] += ((ke1 / 2) * weights[0] + (ke2 / 2)
								* weights[1] + ke12 * weights[2]);
					}
				}
			}

			// Add a normalisation constant
			tab[i] *= Math.PI;
		}

		return tab;
	}
	
	/**
	 * Computes a set of weights for the four main directions (orthogonal plus
	 * diagonal) in discrete image. The sum of the weights equals 1.
	 * 
	 * @param dx the spatial calibration in the x direction
	 * @param dy the spatial calibration in the y direction
	 * @return the set of normalized weights
	 */
	private static final double[] computeDirectionWeightsD4(double dx, double dy) 
	{

		// angle of the diagonal
		double theta = Math.atan2(dy, dx);

		// angular sector for direction 1 ([1 0])
		double alpha1 = theta / Math.PI;

		// angular sector for direction 2 ([0 1])
		double alpha2 = (Math.PI / 2.0 - theta) / Math.PI;

		// angular sector for directions 3 and 4 ([1 1] and [-1 1])
		double alpha34 = .25;

		// concatenate the different weights
		return new double[] { alpha1, alpha2, alpha34, alpha34 };
	}

}
