/**
 * 
 */
package inra.ijpb.data.border;

import ij.ImageStack;

/**
 * Periodic border that considers image is mirrored indefinitely in all
 * directions.
 * 
 * @author David Legland
 *
 */
public class MirroringBorder3D implements BorderManager3D
{

	ImageStack image;

	public MirroringBorder3D(ImageStack image)
	{
		this.image = image;
	}

	/**
	 * @see inra.ijpb.data.border.BorderManager#get(int, int)
	 */
	@Override
	public int get(int x, int y, int z)
	{
		int width = this.image.getWidth();
		int height = this.image.getHeight();
		int depth = this.image.getSize();
		x = x % (2 * width);
		y = y % (2 * height);
		z = z % (2 * depth);
		if (x < 0)
			x = -x - 1;
		if (y < 0)
			y = -y - 1;
		if (z < 0)
			z = -z - 1;
		if (x >= width)
			x = 2 * width - 1 - x;
		if (y >= height)
			y = 2 * height - 1 - y;
		if (z >= depth)
			z = 2 * depth - 1 - y;
		return (int) this.image.getVoxel(x, y, z);
	}

}
