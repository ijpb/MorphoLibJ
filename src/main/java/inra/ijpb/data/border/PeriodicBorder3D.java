/**
 * 
 */
package inra.ijpb.data.border;

import ij.ImageStack;

/**
 * Periodic border that considers image is repeated indefinitely in all
 * directions.
 * 
 * @author David Legland
 *
 */
public class PeriodicBorder3D implements BorderManager3D
{

	ImageStack image;

	public PeriodicBorder3D(ImageStack image)
	{
		this.image = image;
	}

	/**
	 * @see inra.ijpb.data.border.BorderManager#get(int, int)
	 */
	@Override
	public int get(int x, int y, int z)
	{
		x = x % image.getWidth();
		y = y % image.getHeight();
		z = z % image.getSize();
		if (x < 0)
			x += image.getWidth();
		if (y < 0)
			y += image.getHeight();
		if (z < 0)
			z += image.getSize();
		return (int) this.image.getVoxel(x, y, z);
	}

}
