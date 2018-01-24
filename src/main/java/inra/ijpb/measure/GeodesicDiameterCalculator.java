/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2017 INRA.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
package inra.ijpb.measure;

import ij.IJ;
import ij.measure.ResultsTable;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.binary.ChamferWeights;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransform;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransformFloat5x5;
import inra.ijpb.label.LabelImages;
import inra.ijpb.label.LabelValues;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Computes geodesic diameter of a set of labeled particles or regions, using 
 * floating point values (32 bits) for propagating chamfer distances.
 * 
 * This version uses optimized algorithm, that propagates distances of all
 * particles during each pass. This reduces computation overhead due to 
 * iteration over particles.
 * 
 * <p>
 * Example of use:
 *<pre>{@code
 *	GeodesicDiameterCalculator algo = new GeodesicDiameterCalculator(ChamferWeights.CHESSKNIGHT);
 *	Map<Integer,Double> geodDiams = algo.geodesicDiameter(inputLabelImage);
 *	for (int label : geodDiams.keySet())
 *  {
 *      double diam = geodDiams.get(label);
 *      System.out.printl(String.format("geod. diam. of label %d is %5.2f", label, diam);
 *  }
 *}</pre>
 *
 * @see inra.ijpb.binary.geodesic.GeodesicDiameterFloat
 * @see inra.ijpb.binary.geodesic.GeodesicDistanceTransform
 * 
 * @author David Legland
 *
 */
public class GeodesicDiameterCalculator extends AlgoStub
{
	// ==================================================
	// Class variables
	
	/**
	 * The algorithm used for computing geodesic distances.
	 */
	GeodesicDistanceTransform geodesicDistanceTransform;

	/**
	 * An array of shifts corresponding to the weights, for computing geodesic
	 * longest paths.
	 * 
	 * Assumes computation of distance in a 5-by-5 neighborhood.
	 */
	int[][] shifts = new int[][]{
				  {-1, -2}, {0, -2}, {+1, -2},  
		{-2, -1}, {-1, -1}, {0, -1}, {+1, -1}, {+2, -1}, 
		{-2,  0}, {-1,  0},          {+1,  0}, {+2,  0},  
		{-2, +1}, {-1, +1}, {0, +1}, {+1, +1}, {+2, +1},  
		          {-1, +2}, {0, +2}, {+1, +2},  
	};

	
	/**
	 * Keep a reference to the label image to dispatch the process into several methods.
	 */
	ImageProcessor labelImage;
	
	/**
	 * The list of labels within the label image.
	 */
	int[] labels;
	
	/**
	 * The array that stores Chamfer distances. The content of the array will
	 * change during the different steps of the algorithms.
	 */
	ImageProcessor distanceMap = null;

	/**
	 * The largest inscribed circle within each label, computed during the first
	 * step of the algorithm.
	 * 
	 * @see #computeGeodesicDistanceMap()
	 */
	LabelValues.PositionValuePair[] innerCircles = null;

	/**
	 * The position of the first geodesic extremity for each label, computed
	 * during the second step of the algorithm.
	 * 
	 * @see #computeGeodesicDistanceMap()
	 */
	Point[] firstGeodesicExtremities = null;

	/**
	 * The position of the second geodesic extremity for each label, computed
	 * during the thrid step of the algorithm.
	 * 
	 * @see #computeGeodesicDistanceMap()
	 */
	LabelValues.PositionValuePair[] secondGeodesicExtremities = null;
	
	
	// ==================================================
	// Constructors 
	
	/**
	 * Creates a new geodesic diameter computation operator.
	 * 
	 * @param weights
	 *            an instance of ChamferWeights, which provides the float values
	 *            used for propagating distances
	 */
	public GeodesicDiameterCalculator(ChamferWeights weights)
	{
		this(new GeodesicDistanceTransformFloat5x5(weights, true));
	}
	
	/**
	 * Creates a new geodesic diameter computation operator.
	 * 
	 * @param weights
	 *            the array of weights for orthogonal, diagonal, and eventually
	 *            chess-knight moves neighbors
	 */
	public GeodesicDiameterCalculator(float[] weights) 
	{
		this(new GeodesicDistanceTransformFloat5x5(weights, true));
	}
	
	/**
	 * Creates a new geodesic diameter computation operator.
	 * 
	 * @param gdt
	 *            the instance of Geodesic Distance Transform calculator used
	 *            for propagating distances
	 */
	public GeodesicDiameterCalculator(GeodesicDistanceTransform gdt) 
	{
		this.geodesicDistanceTransform = gdt;
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
	 * @return a the geodesic diameter of each particle within the label image
	 */
	public Map<Integer, Double> geodesicDiameter(ImageProcessor labelImage)
	{
		validateGeodesicDistanceMap(labelImage);

		int[] labels = LabelImages.findAllLabels(labelImage);
		double[] geodDiams = geodesicDiameter(labelImage, labels);
		
		// convert the arrays into a map of index-value pairs
		Map<Integer, Double> map = new TreeMap<Integer, Double>();
		for (int i = 0; i < labels.length; i++)
		{
			map.put(labels[i], geodDiams[i]);
		}
		
		return map;
	}
	
	/**
	 * Computes the geodesic diameter of each particle within the given label
	 * image.
	 * 
	 * @param labelImage
	 *            a label image, containing either the label of a particle or
	 *            region, or zero for background
	 * @param labels
	 *            the list of labels to process
	 * @return a the geodesic diameter of each particle within the label image
	 */
	public double[] geodesicDiameter(ImageProcessor labelImage, int[] labels)
	{
		// check if the computation of the distance map is needed or not
		validateGeodesicDistanceMap(labelImage);
		
		// Get the maximum distance within each label, 
		// and add 1 to take into account pixel thickness.
		double[] values = new double[this.labels.length];
		for (int i = 0; i < this.labels.length; i++)
		{
			values[i] = secondGeodesicExtremities[i].getValue() + 1;
		}
		
		return values;
	}
	
	
	public ResultsTable geodesicDiameterResults(ImageProcessor labelImage)
	{
		// check if the computation of the distance map is needed or not
		validateGeodesicDistanceMap(labelImage);
		
		// Initialize a new result table
		ResultsTable table = new ResultsTable();

		// Convert all results that were computed during execution of the
		// "computeGeodesicDistanceMap()" method into rows of the results table
		for (int i = 0; i < this.labels.length; i++)
		{
			int label = this.labels[i];
			
			// coordinates of max inscribed circle
			double radius = innerCircles[i].getValue();
			Point center = innerCircles[i].getPosition();
			
		    // coordinate of first geodesic extremity 
			Point pos1 = firstGeodesicExtremities[i];
			
			// coordinate and value of second geodesic extremity 
			double value = secondGeodesicExtremities[i].getValue() + 1;
			Point pos2 = secondGeodesicExtremities[i].getPosition();
			
			// add an entry to the resulting data table
			table.incrementCounter();
			table.addValue("Label", label);
			table.addValue("Geod. Diam", value);
			table.addValue("Radius", radius);
			table.addValue("Geod. Elong.", Math.max(value / (radius * 2), 1.0));
			table.addValue("xi", center.x);
			table.addValue("yi", center.y);
			table.addValue("x1", pos1.x);
			table.addValue("y1", pos1.y);
			table.addValue("x2", pos2.x);
			table.addValue("y2", pos2.y);
		}

		return table;

	}
	
	/**
	 * Computes the geodesic path of each particle within the given label image.
	 * 
	 * Assumes the geodesic distance map has already been computed after a call
	 * to the "analyzeImage" method.
	 *
	 * @param labelImage
	 *            the image containing labels
	 *
	 * @return A map that associate to each integer label the list of positions
	 *         that constitutes the geodesic path
	 */
	public Map<Integer, List<Point>> longestGeodesicPaths(ImageProcessor labelImage) 
	{
		// check if the computation of the distance map is needed or not
		validateGeodesicDistanceMap(labelImage);
		
		// Initialize a new result table
		Map<Integer, List<Point>> pathMap = new TreeMap<Integer, List<Point>>();

		// compute paths starting from points with larger distance value
		for (int i = 0; i < labels.length; i++)
		{
			int label = labels[i];
			
			// Current first geodesic extremity 
			// (corresponding to the minimum of the geodesic distance map)
			Point pos1 = this.firstGeodesicExtremities[i];
			
			// Create new path
			List<Point> path = new ArrayList<Point>();
			
			// if the geodesic diameter of the current label is infinite, it is
			// not possible to create a path
			// -> use an empty path
			if (Double.isInfinite(this.secondGeodesicExtremities[i].getValue()))
			{
				pathMap.put(label, path);
				continue;
			}
			
			// initialize path with position of second geodesic extremity
			// (corresponding to the maximum of the geodesic distance map)
			Point pos = this.secondGeodesicExtremities[i].getPosition();
			path.add(pos);
			
			// iterate over neighbors of current position until we reach the minimum value
			while (!pos.equals(pos1))
			{
				pos = findLowestNeighborPosition(pos);
				path.add(pos);
			}
			
			pathMap.put(label, path);
		}

		return pathMap;
	}
	
	private void validateGeodesicDistanceMap(ImageProcessor labelImage)
	{
		if (this.labelImage != labelImage)
		{
			// store input image
			this.labelImage = labelImage;
			this.labels = LabelImages.findAllLabels(labelImage);
			
			// updates the distance map
			computeGeodesicDistanceMap();
		}
	}
	
	/**
	 * Compute the geodesic distance map from the label image stored in this
	 * instance.
	 * 
	 * The geodesic distance map correspond to the geodesic distance
	 * (constrained to the label image) to the geodesic extremity computed for
	 * each label.
	 * 
	 * Principle:
	 * <ol>
	 * <li> Identifies centers from distance map</li>
	 * <li> Identifies first geodesic extremity by propagating geodesic distances from center </li>
	 * <li> Propagates geodesic distance from the fist geodesic extremity</li>
	 * </ol> 
	 */
	private void computeGeodesicDistanceMap()
	{
		// number of labels to process
		int nLabels = this.labels.length;
	
		// Create new marker image
		int sizeX = labelImage.getWidth();
		int sizeY = labelImage.getHeight();
		ImageProcessor marker = new ByteProcessor(sizeX, sizeY);
		
		// Compute distance map from label borders to identify centers 
		this.fireStatusChanged(this, "Initializing pseudo geodesic centers...");
		this.distanceMap = BinaryImages.distanceMap(labelImage);
	
		// Extract position of maxima
		this.innerCircles = LabelValues.findMaxValues(distanceMap, labelImage, labels);
		
		// initialize marker image with position of maxima
		marker.setValue(0);
		marker.fill();
		for (int i = 0; i < nLabels; i++) 
		{
			Point center = this.innerCircles[i].getPosition();
			if (center.x == -1)
			{
				IJ.showMessage("Particle Not Found", 
						"Could not find maximum for particle label " + labels[i]);
				continue;
			}
			marker.set(center.x, center.y, 255);
		}
	
		this.fireStatusChanged(this, "Computing first geodesic extremities...");
	
		// Second distance propagation from first maximum
		distanceMap = geodesicDistanceTransform.geodesicDistanceMap(marker, labelImage);
		
		// find position of maximal value for each label
		// this is expected to correspond to a geodesic extremity 
		this.firstGeodesicExtremities = LabelValues.findPositionOfMaxValues(distanceMap, labelImage, labels);
		
		// Create new marker image with position of maxima
		marker.setValue(0);
		marker.fill();
		for (int i = 0; i < nLabels; i++)
		{
			if (this.firstGeodesicExtremities[i].x == -1) 
			{
				IJ.showMessage("Particle Not Found", 
						"Could not find maximum for particle label " + labels[i]);
				continue;
			}
			marker.set(firstGeodesicExtremities[i].x, firstGeodesicExtremities[i].y, 255);
		}
		
		this.fireStatusChanged(this, "Computing second geodesic extremities...");
	
		// third distance propagation from second maximum
		this.distanceMap = geodesicDistanceTransform.geodesicDistanceMap(marker, labelImage);
		// also computes position of maxima
		this.secondGeodesicExtremities = LabelValues.findMaxValues(distanceMap, labelImage, labels);

		this.fireStatusChanged(this, "");
	}

	/**
	 * Finds the position of the pixel in the neighborhood of pos that have the
	 * smallest distance and that belongs to the same label as initial position.
	 * 
	 * @param pos
	 *            the position of the reference pixel
	 * @param shifts
	 *            the list of integer shifts around the reference pixel
	 * @return the position of the neighbor with smallest value
	 */
	private Point findLowestNeighborPosition(Point pos)
	{
		int refLabel = (int) labelImage.getf(pos.x, pos.y);
		float minDist = distanceMap.getf(pos.x, pos.y);
		
		// size of image
		int sizeX = distanceMap.getWidth();
		int sizeY = distanceMap.getHeight();
		
		// iterate over neighbors of current pixel
		Point nextPos = pos;
		for (int[] shift : this.shifts)
		{
			// Compute neighbor coordinates
			int x = pos.x + shift[0];
			int y = pos.y + shift[1];
			
			// check neighbor is within image bounds
			if (x < 0 || x >= sizeX)
			{
				continue;
			}
			if (y < 0 || y >= sizeY)
			{
				continue;
			}
		
			// ensure we stay within the same label
			if (((int) labelImage.getf(x, y)) != refLabel)
			{
				continue;
			}
			
			// compute neighbor value, and compare with current min
			float dist = distanceMap.getf(x, y);
			if (dist < minDist)
			{
				minDist = dist;
				nextPos = new Point(x, y);
			}
		}

		if (nextPos.equals(pos))
		{
			throw new RuntimeException("Could not find a neighbor with smaller value at (" + pos.x + "," + pos.y + ")");
		}
		
		return nextPos;
	}
}
