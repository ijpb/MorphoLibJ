package inra.ijpb.binary.geodesic;

import ij.IJ;
import ij.measure.ResultsTable;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.ChamferWeights;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Computes geodesic diameter of a set of labeled particles or regions, using 
 * floating point values for propagating chamfer distances.
 * 
 * This version uses optimized algorithm, that propagates distances of all
 * particles during each pass. This reduces computation overhead due to 
 * iteration over particles.
 * 
 * <p>
 * Example of use:
 *<pre>{@code
 *	GeodesicDiameterFloat gd = new GeodesicDiameterFloat(ChamferWeights.CHESSKNIGHT);
 *	ResultsTable table = gd.analyseImage(inputLabelImage);
 *	table.show("Geodesic Diameter");
 *}</pre>
 *
 * @see inra.ijpb.binary.geodesic.GeodesicDiameterShort
 * @author dlegland
 *
 */
public class GeodesicDiameterFloat extends AlgoStub implements GeodesicDiameter
{
	// ==================================================
	// Class variables
	
	/**
	 * The weights for orthogonal, diagonal, and eventually chess-knight moves
	 * neighbors
	 */
	float[] weights;
	
	// ==================================================
	// Constructors 
	
	/**
	 * Creates a new geodesic diameter computation operator.
	 * 
	 * @param chamferWeights
	 *            an instance of ChamferWeights, which provides the float values
	 *            used for propagating distances
	 */
	public GeodesicDiameterFloat(ChamferWeights chamferWeights)
	{
		this.weights = chamferWeights.getFloatWeights();
	}
	
	/**
	 * Creates a new geodesic diameter computation operator.
	 * 
	 * @param weights
	 *            the array of weights for orthogonal, diagonal, and eventually
	 *            chess-knight moves neighbors
	 */
	public GeodesicDiameterFloat(float[] weights)
	{
		this.weights = weights;
	}
	

	// ==================================================
	// General methods 

	/**
	 * Computes the geodesic diameter of each particle within the given label
	 * image.
	 * 
	 * @param labelImage
	 *            a label image, containing either the label of a particle or
	 *            region, or zero for background
	 * @return a ResultsTable containing for each label the geodesic diameter of
	 *         the corresponding particle
	 */
	public ResultsTable analyzeImage(ImageProcessor labelImage)
	{
		// Check validity of parameters
		if (labelImage==null) return null;
		
		// compute max label within image
		int[] labels = findAllLabels(labelImage);
		int nbLabels = labels.length;
		
		// Create calculator for propagating distances
		GeodesicDistanceTransform calculator;
		if (weights.length == 3)
		{
			calculator = new GeodesicDistanceTransformFloat5x5(weights, false);
		} 
		else 
		{
			calculator = new GeodesicDistanceTransformFloat(weights, false);
		}
			
		// Initialize a new result table
		ResultsTable table = new ResultsTable();

		// The array that stores Chamfer distances 
		ImageProcessor distance;
		
		Point[] posCenter;
		Point[] pos1;
		Point[] pos2;
		
		// Initialize mask as binarisation of labels
		ImageProcessor mask = binariseImage(labelImage);
		
		// Initialize marker as complement of all labels
		ImageProcessor marker = createMarkerOutsideLabels(labelImage);

		this.fireStatusChanged(this, "Initializing pseudo geodesic centers...");

		// first distance propagation to find an arbitrary center
		distance = calculator.geodesicDistanceMap(marker, mask);
		
		// Extract position of maxima
		posCenter = findPositionOfMaxValues(distance, labelImage, labels);
		
		float[] radii = findMaxValues(distance, labelImage, labels);
		
		// Create new marker image with position of maxima
		marker.setValue(0);
		marker.fill();
		for (int i = 0; i < nbLabels; i++) 
		{
			if (posCenter[i].x == -1) 
			{
				IJ.showMessage("Particle Not Found", 
						"Could not find maximum for particle label " + i);
				continue;
			}
			marker.set(posCenter[i].x, posCenter[i].y, 255);
		}
		
		
		this.fireStatusChanged(this, "Computing first geodesic extremities...");

		// Second distance propagation from first maximum
		distance = calculator.geodesicDistanceMap(marker, mask);

		// find position of maximal value,
		// this is expected to correspond to a geodesic extremity 
		pos1 = findPositionOfMaxValues(distance, labelImage, labels);
		
		// Create new marker image with position of maxima
		marker.setValue(0);
		marker.fill();
		for (int i = 0; i < nbLabels; i++) 
		{
			if (pos1[i].x == -1) 
			{
				IJ.showMessage("Particle Not Found", 
						"Could not find maximum for particle label " + i);
				continue;
			}
			marker.set(pos1[i].x, pos1[i].y, 255);
		}
		
		this.fireStatusChanged(this, "Computing second geodesic extremities...");

		// third distance propagation from second maximum
		distance = calculator.geodesicDistanceMap(marker, mask);
		
		// compute max distance constrained to each label,
		float[] values = findMaxValues(distance, labelImage, labels);
		//System.out.println("value: " + value);
		pos2 = findPositionOfMaxValues(distance, labelImage, labels);
		
		// Small conversion to normalize with weights
		for (int i = 0; i < nbLabels; i++) 
		{
			// convert to pixel distance
			double radius = ((double) radii[i]) / weights[0];
			double value = ((double) values[i]) / weights[0];
			
			// add an entry to the resulting data table
			table.incrementCounter();
			table.addValue("Label", labels[i]);
			table.addValue("Geod. Diam", value);
			table.addValue("Radius", radius);
			table.addValue("Geod. Elong.", Math.max(value / (radius * 2), 1.0));
			table.addValue("xi", posCenter[i].x);
			table.addValue("yi", posCenter[i].y);
			table.addValue("x1", pos1[i].x);
			table.addValue("y1", pos1[i].y);
			table.addValue("x2", pos2[i].x);
			table.addValue("y2", pos2[i].y);
		}

		return table;
	}
	
	/**
	 * Computes the geodesic path of each particle within the given label image.
	 * 
	 * @param labelImage
	 *            a label image, containing either the label of a particle or
	 *            region, or zero for background
	 * @return A map that associate to each integer label the list of positions
	 *         that constitutes the geodesic path
	 */
	public Map<Integer, List<Point>> longestGeodesicPaths(ImageProcessor labelImage) 
	{
		// Check validity of parameters
		if (labelImage==null) return null;
		
		int[] labels = findAllLabels(labelImage);
		int nbLabels = labels.length;

		// Create calculator for propagating distances
		GeodesicDistanceTransform calculator;
		int[][] shifts;
		if (weights.length == 3)
		{
			calculator = new GeodesicDistanceTransformFloat5x5(weights, false);
			shifts = new int[][]{
					          {-1, -2}, {0, -2}, {+1, -2},  
					{-2, -1}, {-1, -1}, {0, -1}, {+1, -1}, {+2, -1}, 
					{-2,  0}, {-1,  0},          {+1,  0}, {+2,  0},  
					{-2, +1}, {-1, +1}, {0, +1}, {+1, +1}, {+2, +1},  
					          {-1, +2}, {0, +2}, {+1, +2},  
			};
		} 
		else 
		{
			calculator = new GeodesicDistanceTransformFloat(weights, false);
			shifts = new int[][]{
					{-1, -1}, {0, -1}, {+1, -1}, 
					{-1,  0},          {+1,  0}, 
					{-1, +1}, {0, +1}, {+1, +1}, 
			};
		}

		// Initialize a new result table
		Map<Integer, List<Point>> result = new TreeMap<Integer, List<Point>>();

		// The array that stores Chamfer distances 
		ImageProcessor distance;
		
		Point[] posCenter;
		Point[] pos1;
		Point[] pos2;
		
		// Initialize mask as binarisation of labels
		ImageProcessor mask = binariseImage(labelImage);
		
		// Initialize marker as complement of all labels
		ImageProcessor marker = createMarkerOutsideLabels(labelImage);

		this.fireStatusChanged(this, "Initializing pseudo geodesic centers...");

		// first distance propagation to find an arbitrary center
		distance = calculator.geodesicDistanceMap(marker, mask);
		
		// Extract position of maxima
		posCenter = findPositionOfMaxValues(distance, labelImage, labels);
		
		// Create new marker image with position of maxima
		marker.setValue(0);
		marker.fill();
		for (int i = 0; i < nbLabels; i++) 
		{
			if (posCenter[i].x == -1)
			{
				IJ.showMessage("Particle Not Found", 
						"Could not find maximum for particle label " + i);
				continue;
			}
			marker.set(posCenter[i].x, posCenter[i].y, 255);
		}
		
		
		this.fireStatusChanged(this, "Computing first geodesic extremities...");

		// Second distance propagation from first maximum
		distance = calculator.geodesicDistanceMap(marker, mask);

		// find position of maximal value,
		// this is expected to correspond to a geodesic extremity 
		pos1 = findPositionOfMaxValues(distance, labelImage, labels);
		
		// Create new marker image with position of maxima
		marker.setValue(0);
		marker.fill();
		for (int i = 0; i < nbLabels; i++)
		{
			if (pos1[i].x == -1) 
			{
				IJ.showMessage("Particle Not Found", 
						"Could not find maximum for particle label " + i);
				continue;
			}
			marker.set(pos1[i].x, pos1[i].y, 255);
		}
		
		this.fireStatusChanged(this, "Computing second geodesic extremities...");

		// third distance propagation from second maximum
		distance = calculator.geodesicDistanceMap(marker, mask);
		
		// compute paths starting from points with larger distance value
		pos2 = findPositionOfMaxValues(distance, labelImage, labels);
		for (int i = 0; i < labels.length; i++)
		{
			int label = labels[i];
			
			List<Point> path = new ArrayList<Point>();
			path.add(pos2[i]);
			
			Point pos = pos2[i];
			try
			{
				while (!pos.equals(pos1[i]))
				{
					pos = findNextPosition(distance, pos, shifts, labelImage);
					path.add(pos);
				}
			}
			catch(Exception ex)
			{
				throw new RuntimeException(String.format("Could not compute path for label %d, at position (%d, %d)",
						label, pos.x, pos.y));
			}
			
			result.put(label, path);
		}

		return result;
	}

	/**
	 * Finds the position of the pixel in the neighborhood of pos that have the
	 * smallest distance and that belongs to the same label as initial position.
	 * 
	 * @param distMap
	 *            the distance map
	 * @param pos
	 *            the position of the reference pixel
	 * @param shifts
	 *            the list of integer shifts around the reference pixel
	 * @param LabelImage
	 *            the array label to check we stay in the same label...
	 * @return the position of the neighbor with smallest value
	 */
	private Point findNextPosition(ImageProcessor distMap, Point pos, int[][] shifts, ImageProcessor labelImage)
	{
		int refLabel = labelImage.get(pos.x, pos.y);
		Point nextPos = pos;
		float minDist = distMap.getf(pos.x, pos.y);
		
		// iterate over neighbors of current pixel
		for (int i = 0; i < shifts.length; i++)
		{
			// Compute neighbor coordinates
			int x = pos.x + shifts[i][0];
			int y = pos.y + shifts[i][1];
			
			// check neighbor is within image bounds
			if (x < 0 || x >= distMap.getWidth())
			{
				continue;
			}
			if (y < 0 || y >= distMap.getHeight())
			{
				continue;
			}
		
			// ensure we stay within the same label
			if (labelImage.get(x, y) != refLabel)
			{
				continue;
			}
			
			// compute neighbor value, and compare with current min
			float dist = distMap.getf(x, y);
			if (dist < minDist)
			{
				minDist = dist;
				nextPos = new Point(x, y);
			}
		}

		if (nextPos.equals(pos))
		{
			throw new RuntimeException("Could not find a neighbor with smaller value");
		}
		
		return nextPos;
	}
	
	/**
	 * Creates a new binary image with same 0 value, and value 255 for each
	 * non-zero pixel of the original image.
	 */
	private ImageProcessor binariseImage(ImageProcessor mask)
	{
		// Extract image size
		int width = mask.getWidth();
		int height = mask.getHeight();
		
		// Create result image
		ImageProcessor marker = new ByteProcessor(width, height);
		
		// Fill result image to either 255 or 0.
		for(int y = 0; y < height; y++) 
		{
			for(int x = 0; x < width; x++) 
			{				
				marker.set(x, y, mask.get(x, y)==0 ? 0 : 255);
			}
		}
		
		// Return result
		return marker;
	}

	private int[] findAllLabels(ImageProcessor image)
	{
		int width 	= image.getWidth();
		int height 	= image.getHeight();
		
		TreeSet<Integer> labels = new TreeSet<Integer> ();
		
		// iterate on image pixels
		for (int y = 0; y < height; y++) 
			for (int x = 0; x < width; x++) 
				labels.add(image.get(x, y));
		
		// remove 0 if it exists
		if (labels.contains(0))
			labels.remove(0);
		
		// convert to array
		int[] array = new int[labels.size()];
		Iterator<Integer> iterator = labels.iterator();
		for (int i = 0; i < labels.size(); i++) 
			array[i] = iterator.next();
		
		return array;
	}

	/**
	 * Create the binary image with value 255 for mask pixels equal to 0, 
	 * and value 0 for any other value of mask.
	 */
	private ImageProcessor createMarkerOutsideLabels(ImageProcessor mask) 
	{
		// Extract image size
		int width = mask.getWidth();
		int height = mask.getHeight();
		
		// Create result image
		ImageProcessor marker = new ByteProcessor(width, height);
		
		// Fill result image to either 255 or 0.
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {				
				marker.set(x, y, mask.get(x, y) == 0 ? 255 : 0);
			}
		}
		
		// Return result
		return marker;
	}

	/**
	 * Find one position for each label. 
	 */
	private Point[] findPositionOfMaxValues(ImageProcessor image, 
			ImageProcessor labelImage, int[] labels)
	{
		int width 	= labelImage.getWidth();
		int height 	= labelImage.getHeight();
		
		// Compute value of greatest label
		int nbLabel = labels.length;
		int maxLabel = 0;
		for (int i = 0; i < nbLabel; i++)
			maxLabel = Math.max(maxLabel, labels[i]);
		
		// init index of each label
		// to make correspondence between label value and label index
		int[] labelIndex = new int[maxLabel+1];
		for (int i = 0; i < nbLabel; i++)
			labelIndex[labels[i]] = i;
				
		// Init Position and value of maximum for each label
		Point[] posMax 	= new Point[nbLabel];
		float[] maxValues = new float[nbLabel];
		for (int i = 0; i < nbLabel; i++) 
		{
			maxValues[i] = -1;
			posMax[i] = new Point(-1, -1);
		}
		
		// store current value
		float value;
		int index;
		
		// iterate on image pixels
		for (int y = 0; y < height; y++) 
		{
			for (int x = 0; x < width; x++) 
			{
				int label = labelImage.get(x, y);
				
				// do not process pixels that do not belong to particle
				if (label==0)
					continue;

				index = labelIndex[label];
				
				// update values and positions
				value = image.getf(x, y);
				if (value > maxValues[index]) 
				{
					posMax[index].setLocation(x, y);
					maxValues[index] = value;
				}
			}
		}
				
		return posMax;
	}

	/**
	 * Finds maximum value within each label.
	 */
	private float[] findMaxValues(ImageProcessor image, 
			ImageProcessor labelImage, int[] labels) 
	{
		int width 	= labelImage.getWidth();
		int height 	= labelImage.getHeight();
		
		// Compute value of greatest label
		int nbLabel = labels.length;
		int maxLabel = 0;
		for (int i = 0; i < nbLabel; i++)
			maxLabel = Math.max(maxLabel, labels[i]);
		
		// init index of each label
		// to make correspondence between label value and label index
		int[] labelIndex = new int[maxLabel+1];
		for (int i = 0; i < nbLabel; i++)
			labelIndex[labels[i]] = i;
				
		// Init Position and value of maximum for each label
		float[] maxValues = new float[nbLabel];
		for (int i = 0; i < nbLabel; i++)
			maxValues[i] = Float.MIN_VALUE;
		
		// store current value
		float value;
		int index;
		
		// iterate on image pixels
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++) 
			{
				int label = labelImage.get(x, y);
				
				// do not process pixels that do not belong to particle
				if (label == 0)
					continue;

				index = labelIndex[label];
				
				// update values and positions
				value = image.getf(x, y);
				if (value > maxValues[index])
					maxValues[index] = value;
			}
		}
				
		return maxValues;
	}

}
