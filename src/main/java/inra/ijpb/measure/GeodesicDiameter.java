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

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
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
import inra.ijpb.label.LabelValues.PositionValuePair;

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
 *	GeodesicDiameter algo = new GeodesicDiameter(ChamferWeights.CHESSKNIGHT);
 *	Map<Integer,GeodesicDiameter.Result> geodDiams = algo.process(inputLabelImage);
 *	for (int label : geodDiams.keySet())
 *  {
 *      double diam = geodDiams.get(label).diameter;
 *      System.out.printl(String.format("geod. diam. of label %d is %5.2f", label, diam);
 *  }
 *}</pre>
 *
 * @see inra.ijpb.binary.geodesic.GeodesicDiameterFloat
 * @see inra.ijpb.binary.geodesic.GeodesicDistanceTransform
 * 
 * @since 1.3.5
 * 
 * @author David Legland
 *
 */public class GeodesicDiameter extends AlgoStub
{
	// ==================================================
	// Static methods 
	
	public static ResultsTable asTable(Map<Integer, Result> map)
	{
		// Initialize a new result table
		ResultsTable table = new ResultsTable();
	
		// Convert all results that were computed during execution of the
		// "computeGeodesicDistanceMap()" method into rows of the results table
		for (int label : map.keySet())
		{
			// current diameter
			Result res = map.get(label);
			
			// add an entry to the resulting data table
			table.incrementCounter();
			table.addValue("Label", label);
			table.addValue("Geod. Diam", res.diameter);
			
			// coordinates of max inscribed circle
			table.addValue("Radius", res.innerRadius);
			table.addValue("Geod. Elong.", Math.max(res.diameter / (res.innerRadius * 2), 1.0));
			table.addValue("xi", res.initialPoint.getX());
			table.addValue("yi", res.initialPoint.getY());
			
		    // coordinate of first and second geodesic extremities 
			table.addValue("x1", res.firstExtremity.getX());
			table.addValue("y1", res.firstExtremity.getY());
			table.addValue("x2", res.secondExtremity.getX());
			table.addValue("y2", res.secondExtremity.getY());
		}
	
		return table;
	
	}

	
	// ==================================================
	// Class variables 
	
	/**
	 * The algorithm used for computing geodesic distances.
	 */
	GeodesicDistanceTransform geodesicDistanceTransform;

	boolean computePaths = false;
	
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

	
	// ==================================================
	// Constructors 

	/**
	 * Empty constructor with default settings.
	 */
	public GeodesicDiameter()
	{
		this(new GeodesicDistanceTransformFloat5x5(ChamferWeights.CHESSKNIGHT.getFloatWeights(), true));
	}
	
	/**
	 * Creates a new geodesic diameter computation operator.
	 * 
	 * @param weights
	 *            the array of weights for orthogonal, diagonal, and eventually
	 *            chess-knight moves neighbors
	 */
	public GeodesicDiameter(ChamferWeights weights) 
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
	public GeodesicDiameter(float[] weights) 
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
	public GeodesicDiameter(GeodesicDistanceTransform gdt) 
	{
		this.geodesicDistanceTransform = gdt;
	}
	

	// ==================================================
	// Setters/Getters
	
	public boolean getComputePaths()
	{
		return this.computePaths;
	}
	
	public void setComputePaths(boolean bool)
	{
		this.computePaths = bool;
	}

	public void setChamferWeights(float[] weights)
	{
		this.geodesicDistanceTransform = new GeodesicDistanceTransformFloat5x5(weights, true);
	}

	
	// ==================================================
	// General methods 
	
	/**
	 * Computes the geodesic diameter of each particle within the given label
	 * image.
	 * 
	 * @param labelImagePlus
	 *            a label image, containing either the label of a particle or
	 *            region, or zero for background
	 * @return a the geodesic diameter of each particle within the label image
	 */
	public Map<Integer, Result> process(ImagePlus labelImagePlus)
	{
		// Extract image processor, and compute geodesic diameter in pixel units
		ImageProcessor labelImage = labelImagePlus.getProcessor();
		int[] labels = LabelImages.findAllLabels(labelImage);
		Result[] geodDiams = process(labelImage, labels);
		
		// check spatial calibration
		Calibration calib = labelImagePlus.getCalibration();
		if (calib.pixelWidth != calib.pixelHeight)
		{
			throw new RuntimeException("Requires image with square pixels");
		}
		
		// convert the arrays into a map of index-value pairs
		Map<Integer, Result> map = new TreeMap<Integer, Result>();
		for (int i = 0; i < labels.length; i++)
		{
			// convert to user units
			map.put(labels[i], geodDiams[i].recalibrate(calib));
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
	 * @return a the geodesic diameter of each particle within the label image
	 */
	public Map<Integer, Result> process(ImageProcessor labelImage)
	{
		int[] labels = LabelImages.findAllLabels(labelImage);
		Result[] geodDiams = process(labelImage, labels);
		
		// convert the arrays into a map of index-value pairs
		Map<Integer, Result> map = new TreeMap<Integer, Result>();
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
	public Result[] process(ImageProcessor labelImage, int[] labels)
	{
		int nLabels = labels.length;
		float[] weights = ChamferWeights.CHESSKNIGHT.getFloatWeights();
		
		// Create new marker image
		int sizeX = labelImage.getWidth();
		int sizeY = labelImage.getHeight();
		ImageProcessor marker = new ByteProcessor(sizeX, sizeY);
		
		// Compute distance map from label borders to identify centers 
		this.fireStatusChanged(this, "Initializing pseudo geodesic centers...");
		ImageProcessor distanceMap = BinaryImages.distanceMap(labelImage, weights, true);
	
		// Extract position of maxima
		PositionValuePair[] innerCircles = LabelValues.findMaxValues(distanceMap, labelImage, labels);
		
		// initialize marker image with position of maxima
		marker.setValue(0);
		marker.fill();
		for (int i = 0; i < nLabels; i++) 
		{
			Point center = innerCircles[i].getPosition();
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
		Point[] firstGeodesicExtremities = LabelValues.findPositionOfMaxValues(distanceMap, labelImage, labels);
		
		// Create new marker image with position of maxima
		marker.setValue(0);
		marker.fill();
		for (int i = 0; i < nLabels; i++)
		{
			if (firstGeodesicExtremities[i].x == -1) 
			{
				IJ.showMessage("Particle Not Found", 
						"Could not find maximum for particle label " + labels[i]);
				continue;
			}
			marker.set(firstGeodesicExtremities[i].x, firstGeodesicExtremities[i].y, 255);
		}
		
		this.fireStatusChanged(this, "Computing second geodesic extremities...");
	
		// third distance propagation from second maximum
		distanceMap = geodesicDistanceTransform.geodesicDistanceMap(marker, labelImage);
		
		// also computes position of maxima
		PositionValuePair[] secondGeodesicExtremities = LabelValues.findMaxValues(distanceMap, labelImage, labels);
		
		// Create array of results and populate with computed values
		GeodesicDiameter.Result[] result = new GeodesicDiameter.Result[nLabels];
		for (int i = 0; i < nLabels; i++)
		{
			Result res = new Result();
					
			// Get the maximum distance within each label, 
			// and add 1 to take into account pixel thickness.
			res.diameter = secondGeodesicExtremities[i].getValue() + 1;

			// also keep references to characteristic points
			res.initialPoint = innerCircles[i].getPosition();
			res.innerRadius = innerCircles[i].getValue();
			res.firstExtremity = firstGeodesicExtremities[i];
			res.secondExtremity = secondGeodesicExtremities[i].getPosition();
			
			result[i] = res;
		}
		
		if (computePaths)
		{
			this.fireStatusChanged(this, "Computing geodesic paths...");

			// compute paths starting from points with larger distance value
			for (int i = 0; i < nLabels; i++)
			{
				// Current first geodesic extremity 
				// (corresponding to the minimum of the geodesic distance map)
				Point2D pos1 = result[i].firstExtremity;
				
				// Create new path
				List<Point2D> path = new ArrayList<Point2D>();
				
				// if the geodesic diameter of the current label is infinite, it is
				// not possible to create a path
				// -> use an empty path
				if (Double.isInfinite(result[i].diameter))
				{
					result[i].path = path;
					continue;
				}
				
				// initialize path with position of second geodesic extremity
				// (corresponding to the maximum of the geodesic distance map)
				Point pos = (Point) result[i].secondExtremity;
				path.add(pos);
				
				// iterate over neighbors of current position until we reach the minimum value
				while (!pos.equals(pos1))
				{
					pos = findLowestNeighborPosition(labelImage, distanceMap, pos);
					path.add(pos);
				}
				
				result[i].path = path;
			}

		}
		return result;
	}
	

	/**
	 * Finds the position of the pixel in the neighborhood of pos that have the
	 * smallest distance and that belongs to the same label as initial position.
	 * 
	 * @param pos
	 *            the position of the reference pixel
	 * @return the position of the neighbor with smallest value
	 */
	private Point findLowestNeighborPosition(ImageProcessor labelImage, ImageProcessor distanceMap, Point pos)
	{
		int refLabel = (int) labelImage.getf(pos.x, pos.y);
		float minDist = distanceMap.getf(pos.x, pos.y);

		// size of image
		int sizeX = distanceMap.getWidth();
		int sizeY = distanceMap.getHeight();


		// iterate over neighbors of current pixel
		Point nextPos = pos;
		for (int[] shift : shifts)
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

	
	

	// ==================================================
	// Inner class used for representing computation results
	
	public class Result
	{
		public double diameter;

		public Point2D initialPoint;

		public double innerRadius;

		public Point2D firstExtremity;

		public Point2D secondExtremity;

		public List<Point2D> path = null;

		/**
		 * Computes the result corresponding to the spatial calibration.
		 * 
		 * @param calib
		 *            the spatial calibration of an image
		 * @return the result after applying the spatial calibration
		 */
		public Result recalibrate(Calibration calib)
		{
			double size = calib.pixelWidth;
			Result res = new Result();
			
			// calibrate the diameter
			res.diameter = this.diameter * size;

			// calibrate inscribed disk
			res.initialPoint = calibrate(this.initialPoint, calib); 
			res.innerRadius = this.innerRadius * size;

			// calibrate geodesic extremities
			res.firstExtremity = calibrate(this.firstExtremity, calib); 
			res.secondExtremity = calibrate(this.firstExtremity, calib);
			
			// calibrate the geodesic path if any
			if (this.path != null)
			{
				List<Point2D> path = new ArrayList<Point2D>(this.path.size());
				for (Point2D point : this.path)
				{
					path.add(calibrate(point, calib));
				}
				res.path = path;
			}
			
			// return the calibrated result
			return res;
		}
		
		private Point2D calibrate(Point2D point, Calibration calib)
		{
			return new Point2D.Double(
					point.getX() * calib.pixelWidth + calib.xOrigin, 
					point.getY() * calib.pixelHeight + calib.yOrigin);
		}
		
	}
}
