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
