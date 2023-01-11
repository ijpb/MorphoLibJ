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

import java.util.Map;

import ij.ImageStack;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import inra.ijpb.data.Cursor3D;
import inra.ijpb.geometry.Point3D;
import inra.ijpb.geometry.Sphere;
import inra.ijpb.label.LabelImages;
import inra.ijpb.label.LabelValues;

/**
 * Computes the largest inscribed ball for each region of a label or binary
 * image.
 * 
  * @see inra.ijpb.measure.region2d.LargestInscribedCircle
 * 
* @author dlegland
 *
 */
public class LargestInscribedBall extends RegionAnalyzer3D<Sphere>
{
	// ==================================================
	// Static methods
	
	/**
	 * Computes largest inscribed ball of each region with a 3D label image.
	 * 
	 * The regions must be disjoint.
	 * 
	 * @param labelImage
	 *            the 3D input image containing label of particles
	 * @param labels
	 *            the set of labels within the image
	 * @param calib
	 *            the spatial calibration of the image
	 * @return an array of Sphere representing the inscribed balls of each
	 *         region, in calibrated coordinates
	 */
	public static final Sphere[] largestInscribedBalls(ImageStack labelImage, int[] labels, Calibration calib)
	{
		return new LargestInscribedBall().analyzeRegions(labelImage, labels, calib);
	}
	
	// ==================================================
	// Constructors
	
    /**
     * Empty constructor.
     */
    public LargestInscribedBall()
	{
	}
	

	// ==================================================
	// Implementation of RegionAnalyzer interface

	/**
	 * Utility method that transforms the mapping between labels and inscribed
	 * ball instances into a ResultsTable that can be displayed with ImageJ.
	 * 
	 * @param map
	 *            the mapping between labels and inscribed ball
	 * @return a ResultsTable that can be displayed with ImageJ.
	 */
	public ResultsTable createTable(Map<Integer, Sphere> map)
	{
		// Initialize a new result table
		ResultsTable table = new ResultsTable();
	
		// Convert all results that were computed during execution of the
		// "computeGeodesicDistanceMap()" method into rows of the results table
		for (int label : map.keySet())
		{
			// current diameter
			Sphere ball = map.get(label);
			
			// add an entry to the resulting data table
			table.incrementCounter();
			table.addLabel(Integer.toString(label));
			
			// coordinates of ball center
			table.addValue("InscrBall.Center.X", ball.center().getX());
			table.addValue("InscrBall.Center.Y", ball.center().getY());
			table.addValue("InscrBall.Center.Z", ball.center().getZ());
			
			// ball radius
			table.addValue("InscrBall.Radius", ball.radius());
		}
	
		return table;
	}
	
	/**
	 * Computes largest inscribed ball of each region with a 3D label image.
	 * 
	 * The regions must be disjoint.
	 * 
	 * @param labelImage
	 *            the 3D input image containing label of particles
	 * @param labels
	 *            the set of labels within the image
	 * @param calib
	 *            the spatial calibration of the image
	 * @return an array of Sphere representing the inscribed balls of each
	 *         region, in calibrated coordinates
	 */
	public Sphere[] analyzeRegions(ImageStack labelImage, int[] labels, Calibration calib)
    {
    	// compute max label within image
    	int nLabels = labels.length;
    	
		// first distance propagation to find an arbitrary center
    	fireStatusChanged(this, "Compute distance map");
		ImageStack distanceMap = LabelImages.distanceMap(labelImage);

		// Extract position of maxima
		fireStatusChanged(this, "Find inscribed balls center");
		Cursor3D[] posCenter;
		posCenter = LabelValues.findPositionOfMaxValues(distanceMap, labelImage, labels);
		float[] radii = getValues(distanceMap, posCenter);

		// Create result data table
		fireStatusChanged(this, "Create ball data");
        Sphere[] balls = new Sphere[nLabels];
		for (int i = 0; i < nLabels; i++) 
		{
			double xc = posCenter[i].getX() * calib.pixelWidth + calib.xOrigin;
			double yc = posCenter[i].getY() * calib.pixelHeight + calib.yOrigin;
			double zc = posCenter[i].getZ() * calib.pixelDepth + calib.zOrigin;
			Point3D center = new Point3D(xc, yc, zc);
			balls[i] = new Sphere(center, radii[i] * calib.pixelWidth);
		}

		return balls;
    }
	
	/**
	 * Get values in input image for each specified position.
	 */
	private final static float[] getValues(ImageStack image, 
			Cursor3D[] positions) 
	{
		float[] values = new float[positions.length];
		
		// iterate on positions
		for (int i = 0; i < positions.length; i++) 
		{
			values[i] = (float) image.getVoxel((int) positions[i].getX(),
					(int) positions[i].getY(), (int) positions[i].getZ());
		}
				
		return values;
	}
}
