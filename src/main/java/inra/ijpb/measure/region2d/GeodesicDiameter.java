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
package inra.ijpb.measure.region2d;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ij.IJ;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.binary.ChamferWeights;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransform;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransformFloat5x5;
import inra.ijpb.label.LabelImages;
import inra.ijpb.label.LabelValues;
import inra.ijpb.label.LabelValues.PositionValuePair;

/**
 * <p>
 * Computes geodesic diameter of a set of labeled particles or regions, using an
 * inner instance of {@link inra.ijpb.binary.geodesic.GeodesicDistanceTransform} for propagating chamfer distances.
 * </p>
 * 
 * <p>
 * The result of the algorithm is stored in a collection of
 * {@link GeodesicDiameter.Result} instances, that returns the diameter,
 * coordinates of extreme points, and eventually the geodesic path corresponding
 * to each region.
 * </p>
 * 
 * <p>
 * This version uses optimized algorithm, that propagates distances of all
 * particles during each pass. This reduces computation overhead due to
 * iteration over particles.
 * </p>
 * 
 * <p>
 * Example of use:
 * 
 * <pre>
 * {@code
 *  GeodesicDiameter algo = new GeodesicDiameter(ChamferWeights.CHESSKNIGHT);
 *  Map<Integer,GeodesicDiameter.Result> geodDiams = algo.process(inputLabelImage);
 *  for (int label : geodDiams.keySet())
 *  {
 *      double diam = geodDiams.get(label).diameter;
 *      System.out.printl(String.format("geod. diam. of label %d is %5.2f", label, diam);
 *  }
 *}
 * </pre>
 *
 * @see inra.ijpb.binary.geodesic.GeodesicDistanceTransform
 * @see inra.ijpb.measure.region2d.GeodesicDiameter.Result
 * 
 * @since 1.3.5
 * 
 * @author David Legland
 *
 */
public class GeodesicDiameter extends RegionAnalyzer2D<GeodesicDiameter.Result>
{
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
	// Specific methods

	/**
	 * Computes the geodesic diameter of each particle within the given label
	 * image.
	 * 
	 * @param labelImage
	 *            a label image, containing either the label of a particle or
	 *            region, or zero for background
	 * @return a the geodesic diameter of each particle within the label image
	 */
	public Map<Integer, Result> analyzeRegions(ImageProcessor labelImage)
	{
		int[] labels = LabelImages.findAllLabels(labelImage);
		Result[] geodDiams = analyzeRegions(labelImage, labels, new Calibration());
		
		// convert the arrays into a map of index-value pairs
		Map<Integer, Result> map = new TreeMap<Integer, Result>();
		for (int i = 0; i < labels.length; i++)
		{
			map.put(labels[i], geodDiams[i]);
		}
		
		return map;
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
	// Implementation of the RegionAnalyzer interface 

	/**
	 * Utility method that transforms the mapping between labels and result
	 * instances into a ResultsTable that can be displayed with ImageJ.
	 * 
	 * @param map
	 *            the mapping between labels and results
	 * @return a ResultsTable that can be displayed with ImageJ.
	 */
	@Override
	public ResultsTable createTable(Map<Integer, Result> map)
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
			table.addLabel(Integer.toString(label));
			table.addValue("GeodesicDiameter", res.diameter);
			
			// coordinates of max inscribed circle
			table.addValue("Radius", res.innerRadius);
			table.addValue("InitPoint.X", res.initialPoint.getX());
			table.addValue("InitPoint.Y", res.initialPoint.getY());
			table.addValue("GeodesicElongation", Math.max(res.diameter / (res.innerRadius * 2), 1.0));
			
		    // coordinate of first and second geodesic extremities 
			table.addValue("Extremity1.X", res.firstExtremity.getX());
			table.addValue("Extremity1.Y", res.firstExtremity.getY());
			table.addValue("Extremity2.X", res.secondExtremity.getX());
			table.addValue("Extremity2.Y", res.secondExtremity.getY());
		}
	
		return table;
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
	 * @param calib
	 *            the spatial caliration of the image
	 * @return a the geodesic diameter of each particle within the label image
	 */
	public Result[] analyzeRegions(ImageProcessor labelImage, int[] labels, Calibration calib)
	{
		// Intitial check-up
		if (calib.pixelWidth != calib.pixelHeight)
		{
			throw new RuntimeException("Requires image with square pixels");
		}

		// number of labels to process
		int nLabels = labels.length;
		
		// Create new marker image
		int sizeX = labelImage.getWidth();
		int sizeY = labelImage.getHeight();
		ImageProcessor marker = new ByteProcessor(sizeX, sizeY);
		
		// Compute distance map from label borders to identify centers
		// (The distance map correctly processes adjacent borders)
		this.fireStatusChanged(this, "Initializing pseudo geodesic centers...");
		float[] weights = ChamferWeights.CHESSKNIGHT.getFloatWeights();
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
			// and add sqrt(2) to take into account maximum pixel thickness.
			res.diameter = secondGeodesicExtremities[i].getValue() + Math.sqrt(2);

			// also keep references to characteristic points
			res.initialPoint = innerCircles[i].getPosition();
			res.innerRadius = innerCircles[i].getValue();
			res.firstExtremity = firstGeodesicExtremities[i];
			res.secondExtremity = secondGeodesicExtremities[i].getPosition();
			
			// store the result
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
		
		// calibrate the results
		if (calib.scaled())
		{
			this.fireStatusChanged(this, "Re-calibrating results");
			for (int i = 0; i < nLabels; i++)
			{
				result[i] = result[i].recalibrate(calib);
			}
		}
		
		// returns the results
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
	
	/**
	 * Inner class used for representing results of geodesic diameters
	 * computations. Each instance corresponds to a single region / particle.
	 * 
	 * @author dlegland
	 *
	 */
	public class Result
	{
		/** The geodesic diameter of the region */
		public double diameter;

		/**
		 * The initial point used for propagating distances, corresponding the
		 * center of one of the minimum inscribed circles.
		 */
		public Point2D initialPoint;

		/**
		 * The radius of the largest inner circle. Value may depends on the chamfer weihgts.
		 */
		public double innerRadius;

		/**
		 * The first geodesic extremity found by the algorithm.
		 */
		public Point2D firstExtremity;

		/**
		 * The second geodesic extremity found by the algorithm.
		 */
		public Point2D secondExtremity;

		/**
		 * The largest geodesic path within the particle, joining the first and
		 * the second geodesic extremities. Its computation is optional.
		 */
		public List<Point2D> path = null;

		/**
		 * Computes the result corresponding to the spatial calibration. The
		 * current result instance is not modified.
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
			res.secondExtremity = calibrate(this.secondExtremity, calib);
			
			// calibrate the geodesic path if any
			if (this.path != null)
			{
				List<Point2D> newPath = new ArrayList<Point2D>(this.path.size());
				for (Point2D point : this.path)
				{
					newPath.add(calibrate(point, calib));
				}
				res.path = newPath;
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
