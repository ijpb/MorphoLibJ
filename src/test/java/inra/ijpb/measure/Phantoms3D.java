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
package inra.ijpb.measure;

import ij.ImageStack;
import ij.measure.Calibration;
import inra.ijpb.geometry.Point3D;

/**
 * A collection of static methods for generating images representing common
 * geometries (balls, ellipsoids...)
 * 
 * @author dlegland
 *
 */
public class Phantoms3D
{
	public static final void fillBall(ImageStack image, Calibration calib, Point3D center, double radius, double value)
	{
		// get image dimensions
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		// iterate over voxels
		for (int z = 0; z < sizeZ; z++)
		{
			double z2 = z * calib.pixelDepth + calib.zOrigin;
			for (int y = 0; y < sizeY; y++)
			{
				double y2 = y * calib.pixelHeight + calib.yOrigin;
				for (int x = 0; x < sizeX; x++)
				{
					double x2 = x * calib.pixelWidth+ calib.xOrigin;
					if (center.distance(new Point3D(x2, y2, z2)) <= radius)
					{
						image.setVoxel(x, y, z, value);
					}
				}
			}
		}
	}
}
