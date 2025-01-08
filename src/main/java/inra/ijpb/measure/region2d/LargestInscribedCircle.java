/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
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
/**
 * 
 */
package inra.ijpb.measure.region2d;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Map;

import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.geometry.Circle2D;
import inra.ijpb.label.LabelImages;

/**
 * Computes the largest inscribed circle for each region of a label or binary
 * image.
 * 
 * @see inra.ijpb.measure.region3d.LargestInscribedBall
 * 
 * @author dlegland
 *
 */
public class LargestInscribedCircle extends RegionAnalyzer2D<Circle2D>
{
	// ==================================================
	// Static methods
	
	/**
	 * Computes largest inscribed disk of each region within a label image.
	 * Regions must be disjoint.
	 * 
	 * @param labelImage
	 *            the input image containing region labels
	 * @param labels
	 *            the set of labels within the image
	 * @param calib
	 *            the spatial calibration of the image
	 * @return an array of Circle2D representing the inscribed circles of each
	 *         region, in calibrated coordinates
	 */
	public static final Circle2D[] largestInscribedCircles(ImageProcessor labelImage, int[] labels, Calibration calib)
	{
		return new LargestInscribedCircle().analyzeRegions(labelImage, labels, calib);
	}
	
	// ==================================================
	// Constructors

	/**
	 * Default empty constructor.
	 */
	public LargestInscribedCircle()
	{
	}

	// ==================================================
	// Implementation of RegionAnalyzer interface

	/**
	 * Utility method that transforms the mapping between labels and inscribed
	 * circle instances into a ResultsTable that can be displayed with ImageJ.
	 * 
	 * @param map
	 *            the mapping between labels and Circle2D instances
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
	
	/**
	 * Computes largest inscribed disk of each region within a label image.
	 * Regions must be disjoint.
	 * 
	 * @param labelImage
	 *            the input image containing region labels
	 * @param labels
	 *            the set of labels within the image
	 * @param calib
	 *            the spatial calibration of the image
	 * @return an array of Circle2D representing the inscribed circles of each
	 *         region, in calibrated coordinates
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
	private final static float[] getValues(ImageProcessor image, Point[] positions)
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
		
		// keep correspondences between label value and label index
		Map<Integer, Integer> labelIndices = LabelImages.mapLabelIndices(labels);
		
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
				
				// do not process background pixels
				if (label == 0) continue;
				// process only specified labels
				if (!labelIndices.containsKey(label)) continue;
				
				index = labelIndices.get(label);
				
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
