/**
 * 
 */
package inra.ijpb.data.border;

import ij.ImageStack;

/**
 * Assess pixel outside image bounds have same value as the closest pixel on
 * image border.
 * 
 * @author David Legland
 *
 */
public class ReplicatedBorder3D implements BorderManager3D
{

	ImageStack image;

	public ReplicatedBorder3D(ImageStack image)
	{
		this.image = image;
	}

	/**
	 * Forces both of x and y to be between 0 and the corresponding image size,
	 * and returns the corresponding image value.
	 * 
	 * @see inra.ijpb.data.border.BorderManager#get(int, int)
	 */
	@Override
	public int get(int x, int y, int z)
	{
		if (x < 0)
			x = 0;
		if (y < 0)
			y = 0;
		if (z < 0)
			z = 0;
		x = Math.min(x, image.getWidth() - 1);
		y = Math.min(y, image.getHeight() - 1);
		z = Math.min(z, image.getSize() - 1);
		return (int) this.image.getVoxel(x, y, z);
	}

}
