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
package inra.ijpb.measure.region3d;

import java.util.HashMap;
import java.util.Map;

import ij.ImageStack;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import inra.ijpb.geometry.Box3D;
import inra.ijpb.label.LabelImages;

/**
 * Compute bounding box of each region within a label or binary image.
 * 
 * @author dlegland
 *
 */
public class BoundingBox3D extends RegionAnalyzer3D<Box3D>
{
	// ==================================================
	// Static methods

	/**
	 * Compute bounding box of each region in input 3D label image and returns
	 * the result as an array of Box3D for each region.
	 * 
	 * @param labelImage
	 *            the input image containing label of particles
	 * @param labels
	 *            an array of unique labels in image
	 * @param calib
	 *            the calibration of the image
	 * @return an array of Box3D instances containing for each region its extent
	 *         in each dimension
	 */
	public static final Box3D[] boundingBoxes(ImageStack labelImage, int[] labels, Calibration calib)
	{
        return new BoundingBox3D().analyzeRegions(labelImage, labels, calib);
	}
	
	// ==================================================
	// Constructor

	/**
	 * Default constructor
	 */
	public BoundingBox3D()
	{
	}

	
	// ==================================================
	// Implementation of RegionAnalyzer interface

	/**
	 * Computes the bounding box of each region within a 3D label image.
	 * 
	 * @param image
	 *            the input image containing label of particles
	 * @param labels
	 *            the array of labels within the image
	 * @param calib
	 *            the calibration of the image
	 * @return an array of Box3D representing the calibrated coordinates of
	 *         the bounding box of each region
	 */
	public Box3D[] analyzeRegions(ImageStack image, int[] labels, Calibration calib)
	{
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
		double[] xmin = new double[nLabels];
		double[] xmax = new double[nLabels];
		double[] ymin = new double[nLabels];
		double[] ymax = new double[nLabels];
		double[] zmin = new double[nLabels];
		double[] zmax = new double[nLabels];
		
		// initialize to extreme values
		for (int i = 0; i < nLabels; i++)
		{
			xmin[i] = Double.POSITIVE_INFINITY;
			xmax[i] = Double.NEGATIVE_INFINITY;
			ymin[i] = Double.POSITIVE_INFINITY;
			ymax[i] = Double.NEGATIVE_INFINITY;
			zmin[i] = Double.POSITIVE_INFINITY;
			zmax[i] = Double.NEGATIVE_INFINITY;
		}

		// compute extreme coordinates of each region
    	fireStatusChanged(this, "Compute bounds");
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

    				xmin[index] = Math.min(xmin[index], x);
    				xmax[index] = Math.max(xmax[index], x + 1);
    				ymin[index] = Math.min(ymin[index], y);
    				ymax[index] = Math.max(ymax[index], y + 1);
    				zmin[index] = Math.min(zmin[index], z);
    				zmax[index] = Math.max(zmax[index], z + 1);
    			}
    		}
    	}
    	
		// create bounding box instances
		Box3D[] boxes = new Box3D[nLabels];
		for (int i = 0; i < nLabels; i++)
		{
			boxes[i] = new Box3D(
					xmin[i] * sx + ox, xmax[i] * sx + ox,
					ymin[i] * sy + oy, ymax[i] * sy + oy, 
					zmin[i] * sz + oz, zmax[i] * sz + oz);
		}
		return boxes;
	}

	/**
	 * Utility method that transforms the mapping between labels and Box3D
	 * instances into a ResultsTable that can be displayed with ImageJ.
	 * 
	 * @param map
	 *            the mapping between labels and Box3D instances
	 * @return a ResultsTable that can be displayed with ImageJ.
	 */
	public ResultsTable createTable(Map<Integer, Box3D> map)
	{
		// Initialize a new result table
		ResultsTable table = new ResultsTable();
	
		// Convert all results that were computed during execution of the
		// "computeGeodesicDistanceMap()" method into rows of the results table
		for (int label : map.keySet())
		{
			// current diameter
			Box3D box = map.get(label);
			
			// add an entry to the resulting data table
			table.incrementCounter();
			table.addLabel(Integer.toString(label));
			
			// coordinates of centroid
			table.addValue("Box.XMin", box.getXMin());
			table.addValue("Box.XMax", box.getXMax());
			table.addValue("Box.YMin", box.getYMin());
			table.addValue("Box.YMax", box.getYMax());
			table.addValue("Box.ZMin", box.getZMin());
			table.addValue("Box.ZMax", box.getZMax());
		}
	
		return table;
	}
}
