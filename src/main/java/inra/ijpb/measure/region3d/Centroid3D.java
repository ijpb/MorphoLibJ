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
/**
 * 
 */
package inra.ijpb.measure.region3d;

import java.util.HashMap;
import java.util.Map;

import ij.ImageStack;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import inra.ijpb.geometry.Point3D;
import inra.ijpb.label.LabelImages;

/**
 * Computes centroid position of regions within 3D binary or label images.
 * 
 * @author dlegland
 *
 */
public class Centroid3D extends RegionAnalyzer3D<Point3D>
{
	// ==================================================
	// Static methods
	
	/**
	 * Compute centroid of each region in input 3D label image and returns the
	 * result as an array of Point3D.
	 * 
	 * @param labelImage
	 *            the input image containing region labels
	 * @param labels
	 *            the array of unique labels in image
	 * @param calib
	 *            the calibration of the image
	 * @return an array of Point3D instances, corresponding to the centroid of
	 *         each region
	 */
	public final static Point3D[] centroids(ImageStack labelImage, int[] labels, Calibration calib) 
	{
		return new Centroid3D().analyzeRegions(labelImage, labels, calib);
	}
	
	/**
	 * Computes centroid of each label in input image and returns the result as
	 * an array of double for each label.
	 * 
	 * This version does not take into account the spatial calibration, and
	 * returns the centroids in pixel coordinates.
	 * 
	 * @param labelImage
	 *            the input image containing label of particles
	 * @param labels
	 *            the array of unique labels in image the number of directions
	 *            to process, either 2 or 4
	 * @return an array containing for each label, the coordinates of the
	 *         centroid, in pixel coordinates
	 */
	public static final double[][] centroids(ImageStack labelImage, int[] labels) 
	{
		// create associative array to know index of each label
		int nLabels = labels.length;
        HashMap<Integer, Integer> labelIndices = LabelImages.mapLabelIndices(labels);

		// allocate memory for result
		int[] counts = new int[nLabels];
		double[][] centroids = new double[nLabels][3];

		// compute centroid of each region
		int sizeX = labelImage.getWidth();
		int sizeY = labelImage.getHeight();
		int sizeZ = labelImage.getSize();
		
		for (int z = 0; z < sizeZ; z++) 
		{
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++)
				{
					int label = (int) labelImage.getVoxel(x, y, z);
					if (label == 0)
						continue;

					// do not process labels that are not in the input list 
					if (!labelIndices.containsKey(label))
						continue;

					int index = labelIndices.get(label);
					centroids[index][0] += x;
					centroids[index][1] += y;
					centroids[index][2] += z;
					counts[index]++;
				}
			}
		}
		
		// normalize by number of voxels in each region
		for (int i = 0; i < nLabels; i++)
		{
			centroids[i][0] /= counts[i];
			centroids[i][1] /= counts[i];
			centroids[i][2] /= counts[i];
		}

		return centroids;
	}
	// ==================================================
	// Constructor

	/**
	 * Default constructor
	 */
	public Centroid3D()
	{
	}

	
	// ==================================================
	// Implementation of RegionAnalyzer interface

	/**
	 * Utility method that transforms the mapping between labels and Point3D
	 * instances into a ResultsTable that can be displayed with ImageJ.
	 * 
	 * @param map
	 *            the mapping between labels and Point3Ds
	 * @return a ResultsTable that can be displayed with ImageJ.
	 */
	public ResultsTable createTable(Map<Integer, Point3D> map)
	{
		// Initialize a new result table
		ResultsTable table = new ResultsTable();
	
		// Convert all results that were computed during execution of the
		// "computeGeodesicDistanceMap()" method into rows of the results table
		for (int label : map.keySet())
		{
			// current diameter
			Point3D point = map.get(label);
			
			// add an entry to the resulting data table
			table.incrementCounter();
			table.addLabel(Integer.toString(label));
			
			// coordinates of centroid
			table.addValue("Centroid.X", point.getX());
			table.addValue("Centroid.Y", point.getY());
			table.addValue("Centroid.Z", point.getZ());
		}
	
		return table;
	}

	
	/**
	 * Computes centroid of each region in input label image.
	 * 
	 * @param image
	 *            the input image containing label of particles
	 * @param labels
	 *            the array of labels within the image
	 * @param calib
	 *            the calibration of the image
	 * @return an array of Point3D representing the calibrated centroid coordinates 
	 */
	public Point3D[] analyzeRegions(ImageStack image, int[] labels, Calibration calib)
	{
		// Check validity of parameters
		if (image == null)
			return null;

		// size of image
		int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        int sizeZ = image.getSize();

		// Extract spatial calibration
		double sx = 1, sy = 1, sz = 1;
		double ox = 0, oy = 0, oz = 0;
		if (calib != null)
		{
			sx = calib.pixelWidth;
			sy = calib.pixelHeight;
			sz = calib.pixelDepth;
			ox = calib.xOrigin;
			oy = calib.yOrigin;
			oz = calib.zOrigin;
		}
		
		// create associative array to know index of each label
        HashMap<Integer, Integer> labelIndices = LabelImages.mapLabelIndices(labels);

		// allocate memory for result
		int nLabels = labels.length;
		int[] counts = new int[nLabels];
		double[] cx = new double[nLabels];
		double[] cy = new double[nLabels];
		double[] cz = new double[nLabels];

    	fireStatusChanged(this, "Compute centroids");
		// compute centroid of each region
    	for (int z = 0; z < sizeZ; z++) 
    	{
    		for (int y = 0; y < sizeY; y++) 
    		{
    			for (int x = 0; x < sizeX; x++)
    			{
    				int label = (int) image.getVoxel(x, y, z);
    				if (label == 0)
    					continue;

                    // do not process labels that are not in the input list 
                    if (!labelIndices.containsKey(label))
                        continue;

                    int index = labelIndices.get(label);
    				cx[index] += x * sx;
    				cy[index] += y * sy;
    				cz[index] += z * sz;
    				counts[index]++;
    			}
    		}
    	}
		// normalize by number of pixels in each region
    	Point3D[] points = new Point3D[nLabels];
		for (int i = 0; i < nLabels; i++)
		{
			points[i] = new Point3D(cx[i] / counts[i] + ox, cy[i] / counts[i] + oy, cz[i] / counts[i] + oz);
		}

		return points;
	}
}
