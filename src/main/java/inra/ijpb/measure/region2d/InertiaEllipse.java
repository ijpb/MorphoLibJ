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
package inra.ijpb.measure.region2d;

import static java.lang.Math.sqrt;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import inra.ijpb.geometry.Ellipse;
import inra.ijpb.label.LabelImages;

/**
 * Compute parameters of inertia ellipse from label images.
 * 
 * @see inra.ijpb.measure.region3d.EquivalentEllipsoid
 * 
 * @deprecated Replaced by EquivalentEllipse (since 1.4.2)
 * @author dlegland
 *
 */
@Deprecated
public class InertiaEllipse extends RegionAnalyzer2D<Ellipse>
{
	// ==================================================
	// Static methods 
	
	/**
	 * Computes inertia ellipse of each region in input label image.
	 * 
	 * @param image
	 *            the input image containing region labels
	 * @param labels
	 *            the array of labels within the image
	 * @param calib
	 *            the calibration of the image
	 * @return an array of Ellipse representing the calibrated coordinates of
	 *         the inertia ellipse of each region
	 */
	public static final Ellipse[] inertiaEllipses(ImageProcessor image,
			int[] labels, Calibration calib)
	{
		return new InertiaEllipse().analyzeRegions(image, labels, calib);
	}
	
	// ==================================================
	// Constructor

	/**
	 * Default constructor
	 */
	public InertiaEllipse()
	{
	}

	
	// ==================================================
	// Implementation of RegionAnalyzer interface

	/**
	 * Utility method that transforms the mapping between labels and inertia
	 * ellipses instances into a ResultsTable that can be displayed with ImageJ.
	 * 
	 * @param map
	 *            the mapping between labels and Inertia Ellipses
	 * @return a ResultsTable that can be displayed with ImageJ.
	 */
	public ResultsTable createTable(Map<Integer, Ellipse> map)
	{
		// Initialize a new result table
		ResultsTable table = new ResultsTable();
	
		// Convert all results that were computed during execution of the
		// "computeGeodesicDistanceMap()" method into rows of the results table
		for (int label : map.keySet())
		{
			// current diameter
			Ellipse ellipse = map.get(label);
			
			// add an entry to the resulting data table
			table.incrementCounter();
			table.addLabel(Integer.toString(label));
			
			// coordinates of centroid
			Point2D center = ellipse.center();
			table.addValue("Ellipse.Center.X", center.getX());
			table.addValue("Ellipse.Center.Y", center.getY());
			
			// ellipse size
			table.addValue("Ellipse.Radius1", ellipse.radius1());
			table.addValue("Ellipse.Radius2", ellipse.radius2());
	
			// ellipse orientation (degrees)
			table.addValue("Ellipse.Orientation", ellipse.orientation());
		}
	
		return table;
	}

	/**
	 * Computes inertia ellipse of each region in input label image.
	 * 
	 * @param image
	 *            the input image containing region labels
	 * @param labels
	 *            the array of labels within the image
	 * @param calib
	 *            the calibration of the image
	 * @return an array of Ellipse representing the calibrated coordinates of
	 *         the inertia ellipse of each region
	 */
	public Ellipse[] analyzeRegions(ImageProcessor image, int[] labels, Calibration calib)
	{
		// size of image
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();

		// Extract spatial calibration
		double sx = 1, sy = 1;
		double ox = 0, oy = 0;
		if (calib != null)
		{
			sx = calib.pixelWidth;
			sy = calib.pixelHeight;
			ox = calib.xOrigin;
			oy = calib.yOrigin;
		}
		
		// create associative array to know index of each label
        HashMap<Integer, Integer> labelIndices = LabelImages.mapLabelIndices(labels);

		// allocate memory for result
		int nLabels = labels.length;
		int[] counts = new int[nLabels];
		double[] cx = new double[nLabels];
		double[] cy = new double[nLabels];
		double[] Ixx = new double[nLabels];
		double[] Iyy = new double[nLabels];
		double[] Ixy = new double[nLabels];

    	fireStatusChanged(this, "Compute centroids");
		// compute centroid of each region
		for (int y = 0; y < sizeY; y++) 
		{
			for (int x = 0; x < sizeX; x++)
			{
				int label = (int) image.getf(x, y);
				if (label == 0)
					continue;

                // do not process labels that are not in the input list 
                if (!labelIndices.containsKey(label))
                    continue;

                int index = labelIndices.get(label);
				cx[index] += x * sx;
				cy[index] += y * sy;
				counts[index]++;
			}
		}

		// normalize by number of pixels in each region
		for (int i = 0; i < nLabels; i++)
		{
			cx[i] = cx[i] / counts[i];
			cy[i] = cy[i] / counts[i];
		}

		// compute centered inertia matrix of each label
    	fireStatusChanged(this, "Compute Inertia Matrices");
		for (int y = 0; y < sizeY; y++) 
		{
			for (int x = 0; x < sizeX; x++)
			{
				int label = (int) image.getf(x, y);
				if (label == 0)
					continue;

				int index = labelIndices.get(label);
				double x2 = x * sx - cx[index];
				double y2 = y * sy - cy[index];
				Ixx[index] += x2 * x2;
				Ixy[index] += x2 * y2;
				Iyy[index] += y2 * y2;
			}
		}

		// normalize by number of pixels in each region
		for (int i = 0; i < nLabels; i++)
		{
			Ixx[i] = Ixx[i] / counts[i] + sx / 12.0;
			Ixy[i] = Ixy[i] / counts[i];
			Iyy[i] = Iyy[i] / counts[i] + sy / 12.0;
		}

		// Create array of result
		Ellipse[] ellipses = new Ellipse[nLabels];
		
		// compute ellipse parameters for each region
    	fireStatusChanged(this, "Compute Ellipses");
		final double sqrt2 = sqrt(2);
		for (int i = 0; i < nLabels; i++) 
		{
			double xx = Ixx[i];
			double xy = Ixy[i];
			double yy = Iyy[i];

			// compute ellipse semi-axes lengths
			double common = sqrt((xx - yy) * (xx - yy) + 4 * xy * xy);
			double ra = sqrt2 * sqrt(xx + yy + common);
			double rb = sqrt2 * sqrt(xx + yy - common);

			// compute ellipse angle and convert into degrees
			double theta = Math.toDegrees(Math.atan2(2 * xy, xx - yy) / 2);

			Point2D center = new Point2D.Double(cx[i] + sx / 2 + ox, cy[i] + sy / 2 + oy);
			ellipses[i] = new Ellipse(center, ra, rb, theta);
		}

		return ellipses;
	}

}

