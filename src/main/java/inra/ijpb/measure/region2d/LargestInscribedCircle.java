/**
 * 
 */
package inra.ijpb.measure.region2d;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Map;
import java.util.TreeMap;

import ij.ImagePlus;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.geometry.Circle2D;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.RegionAnalyzer;

/**
 * Computes the largest inscribed circle for each region of a label or binary
 * image.
 * 
 * @author dlegland
 *
 */
public class LargestInscribedCircle extends AlgoStub implements RegionAnalyzer<Circle2D>
{
	// ==================================================
	// Static methods
	
	
	// ==================================================
	// Constructors
	
	public LargestInscribedCircle()
	{
	}
	
	// ==================================================
	// Implementation of RegionAnalyzer interface

	public ResultsTable computeTable(ImagePlus labelPlus)
	{
		return createTable(analyzeRegions(labelPlus));
	}

	/**
	 * Utility method that transforms the mapping between labels and inscribed
	 * circle instances into a ResultsTable that can be displayed with ImageJ.
	 * 
	 * @param map
	 *            the mapping between labels and Inertia Ellipses
	 * @return a ResultsTable that can be displayed with ImageJ.
	 */
	public ResultsTable createTable(Map<Integer, Circle2D> map)
	{
		// Initialize a new result table
		ResultsTable table = new ResultsTable();
	
		// Convert all results that were computed during execution of the
		// "computeGeodesicDistanceMap()" method into rows of the results table
		for (int label : map.keySet())
		{
			// current diameter
			Circle2D circle = map.get(label);
			
			// add an entry to the resulting data table
			table.incrementCounter();
			table.addLabel(Integer.toString(label));
			
			// coordinates of circle center
			table.addValue("InscrCircle.Center.X", circle.getCenter().getX());
			table.addValue("InscrCircle.Center.Y", circle.getCenter().getY());
			
			// circle radius
			table.addValue("InscrCircle.Radius", circle.getRadius());
		}
	
		return table;
	}
	
	
	public Map<Integer, Circle2D> analyzeRegions(ImagePlus labelPlus)
	{
		return analyzeRegions(labelPlus.getProcessor(), labelPlus.getCalibration());
	}

	
	// ==================================================
	// Computation methods

	/**
	 * Computes largest inscribed disk of each particle. Particles must be
	 * disjoint.
	 * 
	 * @param labelImage
	 *            the input image containing label of particles
	 * @param calib
	 *            the spatial calibration of the image
	 * @return a ResultsTable with as many rows as the number of unique labels
	 *         in label image, and columns "Label", "xi", "yi" and "Radius".
	 */
	public Map<Integer, Circle2D> analyzeRegions(ImageProcessor labelImage, Calibration calib)
    {
    	// compute all labels within image
    	int[] labels = LabelImages.findAllLabels(labelImage);
    	
    	// compute largest inscribed circles
    	Circle2D[] circles = analyzeRegions(labelImage, labels, calib);
    	
    	// convert circle array into label-circle map
    	Map<Integer, Circle2D> map = new TreeMap<Integer, Circle2D>();
    	for (int i = 0; i < labels.length; i++)
    	{
    		map.put(labels[i], circles[i]);
    	}
    	
    	return map;
    }
	
	
	/**
	 * Computes largest inscribed disk of each particle. Particles must be
	 * disjoint.
	 * 
	 * @param labelImage
	 *            the input image containing label of particles
	 * @param labels
	 *            the set of labels within the image
	 * @param calib
	 *            the spatial calibration of the image
	 * @return a ResultsTable with as many rows as the number of unique labels
	 *         in label image, and columns "Label", "xi", "yi" and "Radius".
	 */
	public Circle2D[] analyzeRegions(ImageProcessor labelImage, int[] labels, Calibration calib)
    {
    	// compute max label within image
    	int nLabels = labels.length;
    	
		// first distance propagation to find an arbitrary center
    	fireStatusChanged(this, "Compute distance map");
		ImageProcessor distanceMap = BinaryImages.distanceMap(labelImage);
		
		// Extract position of maxima
		fireStatusChanged(this, "Find inscribed disks center");
		Point[] posCenter;
		posCenter = findPositionOfMaxValues(distanceMap, labelImage, labels);
		float[] radii = getValues(distanceMap, posCenter);

		// Create result data table
		Circle2D[] circles = new Circle2D[nLabels];
		for (int i = 0; i < nLabels; i++) 
		{
			double xc = posCenter[i].x * calib.pixelWidth + calib.xOrigin;
			double yc = posCenter[i].y * calib.pixelHeight + calib.yOrigin;
			Point2D center = new Point2D.Double(xc, yc);
			circles[i] = new Circle2D(center, radii[i] * calib.pixelWidth);
		}

		return circles;
    }
	
	/**
	 * Returns the set of values from input image for each specified position.
	 * 
	 * @param image
	 *            the input image
	 * @param positions
	 *            the set of positions
	 * @return the array of values corresponding to each position
	 */
	private final static float[] getValues(ImageProcessor image, 
			Point[] positions) 
	{
		// allocate memory
		float[] values = new float[positions.length];
		
		// iterate on positions
		for (int i = 0; i < positions.length; i++) 
		{
			values[i] = image.getf(positions[i].x, positions[i].y);
		}
				
		return values;
	}
    
	/**
	 * Find one position of maximum value within each label.
	 * 
	 * @param image
	 *            the input image containing the value (for example a distance 
	 *            map)
	 * @param labelImage
	 *            the input image containing label of particles
	 * @param labels
	 *            the set of labels contained in the label image
	 *            
	 */
	private final static Point[] findPositionOfMaxValues(ImageProcessor image,
			ImageProcessor labelImage, int[] labels)
	{
		int width 	= labelImage.getWidth();
		int height 	= labelImage.getHeight();
		
		// Compute value of greatest label
		int nbLabel = labels.length;
		int maxLabel = 0;
		for (int i = 0; i < nbLabel; i++)
		{
			maxLabel = Math.max(maxLabel, labels[i]);
		}
		
		// init index of each label
		// to make correspondence between label value and label index
		int[] labelIndex = new int[maxLabel+1];
		for (int i = 0; i < nbLabel; i++)
		{
			labelIndex[labels[i]] = i;
		}
		
		// Init Position and value of maximum for each label
		Point[] posMax 	= new Point[nbLabel];
		int[] maxValues = new int[nbLabel];
		for (int i = 0; i < nbLabel; i++) 
		{
			maxValues[i] = -1;
			posMax[i] = new Point(-1, -1);
		}
		
		// store current value
		int value;
		int index;
		
		// iterate on image pixels
		for (int y = 0; y < height; y++) 
		{
			for (int x = 0; x < width; x++) 
			{
				int label = (int) labelImage.getf(x, y);
				
				// do not process pixels that do not belong to particle
				if (label==0)
					continue;

				index = labelIndex[label];
				
				// update values and positions
				value = image.get(x, y);
				if (value > maxValues[index])
				{
					posMax[index].setLocation(x, y);
					maxValues[index] = value;
				}
			}
		}
				
		return posMax;
	}
}
