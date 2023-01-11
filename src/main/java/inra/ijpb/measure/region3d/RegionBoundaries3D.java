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

import ij.ImageStack;
import inra.ijpb.geometry.Point3D;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Utility functions for computing position of boundary points/corners of
 * regions within 3D binary or label images.
 * 
 * @author dlegland
 *
 */
public class RegionBoundaries3D
{
	/**
	 * private constructor to prevent instantiations.
	 */
	private RegionBoundaries3D()
	{
	}

	/**
	 * Returns a set of points located at the corners of a binary particle.
	 * Point coordinates are integer (ImageJ locates pixels in a [0 1]^d area.
	 * 
	 * @param image
	 *            a binary image representing the particle
	 * @param labels
	 *            the list of labels to process
	 * @return a list of points that can be used for convex hull computation
	 */
	public final static Map<Integer, ArrayList<Point3D>> regionsCorners(ImageStack image, int[] labels)
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
        // For each label, create an empty list of corner points
        Map<Integer, ArrayList<Point3D>> labelCornerPoints = new TreeMap<Integer, ArrayList<Point3D>>();
        for (int label : labels)
        {
        	labelCornerPoints.put(label, new ArrayList<Point3D>());
        }
		
		// for each row, add corner point for first and last pixel of each run-length
		for (int z = 0; z < sizeZ; z++)
		{
			for (int y = 0; y < sizeY; y++)
			{
				// start from background
				int currentLabel = 0;

				// Identify transition inside and outside the each label
				for (int x = 0; x < sizeX; x++)
				{
					int pixel = (int) image.getVoxel(x, y, z);
					if (pixel != currentLabel)
					{
						// creates the four corners corresponding to the
						// transition between previous and current voxels
						ArrayList<Point3D> newPoints = new ArrayList<Point3D>(4);
						newPoints.add(new Point3D(x, y, z));
						newPoints.add(new Point3D(x, y + 1, z));
						newPoints.add(new Point3D(x, y, z + 1));
						newPoints.add(new Point3D(x, y + 1, z + 1));

						// if leave a region, add a new corner points for the end of the region
						if (currentLabel > 0 && labelCornerPoints.containsKey(currentLabel))
						{
							ArrayList<Point3D> corners = labelCornerPoints.get(currentLabel);
							for (Point3D p : newPoints)
							{
								if (!corners.contains(p))
								{
									corners.add(p);
								}
							}
						}

						// transition into a new region
						if (pixel > 0 && labelCornerPoints.containsKey(pixel))
						{
							ArrayList<Point3D> corners = labelCornerPoints.get(pixel);
							for (Point3D p : newPoints)
							{
								if (!corners.contains(p))
								{
									corners.add(p);
								}
							}
						}
					}
					currentLabel = pixel;
				}

				// if particle touches right border, add another point
				if (currentLabel > 0 &&  labelCornerPoints.containsKey(currentLabel))
				{
                    // creates the four corners corresponding to the
					// transition between current label and background
					ArrayList<Point3D> newPoints = new ArrayList<Point3D>(4);
					newPoints.add(new Point3D(sizeX, y, z));
					newPoints.add(new Point3D(sizeX, y + 1, z));
					newPoints.add(new Point3D(sizeX, y, z + 1));
					newPoints.add(new Point3D(sizeX, y + 1, z + 1));
					
					ArrayList<Point3D> corners = labelCornerPoints.get(currentLabel);
					for (Point3D p : newPoints)
					{
						if (!corners.contains(p))
						{
							corners.add(p);
						}
					}
				}
			}
		}
		
		return labelCornerPoints;
	}
	
	/**
	 * Returns a set of points located at the corners of a binary particle.
	 * Point coordinates are integer (ImageJ locates pixels in a [0 1]^d area.
	 * 
	 * @param image
	 *            a binary image representing the particle
	 * @param labels
	 *            the list of labels to process
	 * @return for each label, an array of points
	 */
	public final static ArrayList<Point3D>[] regionsCornersArray(ImageStack image, int[] labels)
	{
		// Compute corner points for each label
		Map<Integer, ArrayList<Point3D>> cornerPointsMap = regionsCorners(image, labels);
		
		// allocate array
		int nLabels = labels.length;
		@SuppressWarnings("unchecked")
		ArrayList<Point3D>[] labelCornerPoints = (ArrayList<Point3D>[]) new ArrayList<?>[nLabels];
		
		// convert map to array
		for (int i = 0; i < nLabels; i++)
		{
			labelCornerPoints[i] = cornerPointsMap.get(labels[i]);
		}		
		
		return labelCornerPoints;
	}
}
