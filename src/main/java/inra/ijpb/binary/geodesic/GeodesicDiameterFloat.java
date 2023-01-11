/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2023 INRA.
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
package inra.ijpb.binary.geodesic;

import ij.IJ;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.binary.ChamferWeights;
import inra.ijpb.label.LabelImages;
import inra.ijpb.label.LabelValues;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
 * @deprecated since 1.3.5, use inra.ijpb.measure.region2d.GeodesicDiameter instead 
 * @see inra.ijpb.measure.region2d.GeodesicDiameter
 * 
 * @author dlegland
 *
 */
@Deprecated
public class GeodesicDiameterFloat extends AlgoStub implements GeodesicDiameter
{
	// ==================================================
	// Class variables
	
	/**
	 * The weights for orthogonal, diagonal, and eventually chess-knight moves
	 * neighbors
	 */
	float[] weights;

	/**
	 * The algorithm used for computing geodesic distances.
	 */
	GeodesicDistanceTransform calculator;

	/**
	 * An array of shifts corresponding to the weights, for computing geodesic
	 * longest paths.
	 */
	int[][] shifts;
	
	/**
	 * Keep a reference to the label image to dispatch the process into several methods.
	 */
	ImageProcessor labelImage;
	
	/**
	 * The array that stores Chamfer distances. The content of the array will
	 * change during the different steps of the algorithms.
	 */
	ImageProcessor distanceMap;

	
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
		this(chamferWeights.getFloatWeights());
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
		chooseDistanceCalculator();
	}
	
	/**
	 * Choose the appropriate instance of GeodesicDistanceTransform depending on
	 * the length of the weight array.
	 */
	private void chooseDistanceCalculator()
	{
		// Create calculator for propagating distances
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
		this.labelImage = labelImage;
		
		// extract labels
        this.fireStatusChanged(this, "Count labels in image");
        int[] labels = LabelImages.findAllLabels(labelImage);
		int nbLabels = labels.length;
		
		this.fireStatusChanged(this, "Compute binary masks");

		// Initialize mask as binarisation of labels
		ImageProcessor mask = BinaryImages.binarize(labelImage);
		
		// Initialize marker as complement of all labels
		ImageProcessor marker = BinaryImages.binarizeBackground(labelImage);

		this.fireStatusChanged(this, "Initializing pseudo geodesic centers...");

		// first distance propagation to find an arbitrary center
		distanceMap = calculator.geodesicDistanceMap(marker, mask);
		
		// Extract position of maxima
		LabelValues.PositionValuePair[] circles = LabelValues.findMaxValues(distanceMap, labelImage, labels);
		
		// Create new marker image with position of maxima
		marker.setValue(0);
		marker.fill();
		for (int i = 0; i < nbLabels; i++) 
		{
			Point center = circles[i].getPosition();
			if (center.x == -1)
			{
				IJ.showMessage("Particle Not Found", 
						"Could not find maximum for particle label " + i);
				continue;
			}
			marker.set(center.x, center.y, 255);
		}
		
		
		this.fireStatusChanged(this, "Computing first geodesic extremities...");

		// Second distance propagation from first maximum
		distanceMap = calculator.geodesicDistanceMap(marker, mask);

		// find position of maximal value,
		// this is expected to correspond to a geodesic extremity 
		Point[] pos1 = LabelValues.findPositionOfMaxValues(distanceMap, labelImage, labels);
		
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
		distanceMap = calculator.geodesicDistanceMap(marker, mask);
		
		// compute max distance constrained to each label,
		LabelValues.PositionValuePair[] extremities = LabelValues.findMaxValues(distanceMap, labelImage, labels);

        // Store results in a new result table
        ResultsTable table = new ResultsTable();
		for (int i = 0; i < nbLabels; i++) 
		{
		    // Small conversion to normalize with weights,
			double radius = circles[i].getValue() / weights[0];
			Point center = circles[i].getPosition();
		    // ensure minimal length of 1 for particles composed of only one pixel
			double value = extremities[i].getValue() / weights[0] + 1;
			Point extremPos = extremities[i].getPosition();
			
			// add an entry to the resulting data table
			table.incrementCounter();
			table.addValue("Label", labels[i]);
			table.addValue("Geod. Diam", value);
			table.addValue("Radius", radius);
			table.addValue("Geod. Elong.", Math.max(value / (radius * 2), 1.0));
			table.addValue("xi", center.x);
			table.addValue("yi", center.y);
			table.addValue("x1", pos1[i].x);
			table.addValue("y1", pos1[i].y);
			table.addValue("x2", extremPos.x);
			table.addValue("y2", extremPos.y);
		}

		this.fireStatusChanged(this, "");
		return table;
	}
	

	/**
	 * Computes the geodesic path of each particle within the given label image.
	 * 
	 * Assumes the geodesic distance map has already been computed after a call
	 * to the "analyzeImage" method.
	 * 
	 * @return A map that associate to each integer label the list of positions
	 *         that constitutes the geodesic path
	 */
	public Map<Integer, List<Point>> longestGeodesicPaths() 
	{
		// extract labels
        int[] labels = LabelImages.findAllLabels(labelImage);

		// find position of minimal value,
		// this is expected to correspond to a geodesic extremity 
		Point[] pos1 = LabelValues.findPositionOfMinValues(distanceMap, labelImage, labels);
		
		// compute position of furthest points
		Point[] pos2 = LabelValues.findPositionOfMaxValues(distanceMap, labelImage, labels);
		
		// Initialize a new result table
		Map<Integer, List<Point>> result = new TreeMap<Integer, List<Point>>();

		// compute paths starting from points with larger distance value
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
					pos = findLowestNeighborPosition(pos);
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
	 * @param pos
	 *            the position of the reference pixel
	 * @param shifts
	 *            the list of integer shifts around the reference pixel
	 * @return the position of the neighbor with smallest value
	 */
	private Point findLowestNeighborPosition(Point pos)
	{
		int refLabel = (int) labelImage.getf(pos.x, pos.y);
		Point nextPos = pos;
		float minDist = distanceMap.getf(pos.x, pos.y);
		
		// iterate over neighbors of current pixel
		for (int i = 0; i < shifts.length; i++)
		{
			// Compute neighbor coordinates
			int x = pos.x + shifts[i][0];
			int y = pos.y + shifts[i][1];
			
			// check neighbor is within image bounds
			if (x < 0 || x >= distanceMap.getWidth())
			{
				continue;
			}
			if (y < 0 || y >= distanceMap.getHeight())
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
			throw new RuntimeException("Could not find a neighbor with smaller value");
		}
		
		return nextPos;
	}
}
