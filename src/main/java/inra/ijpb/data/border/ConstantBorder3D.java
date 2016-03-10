/**
 * 
 */
package inra.ijpb.data.border;

import ij.ImageStack;

/**
 * Returns either image pixel when position is inside image bounds, or a
 * constant value when position is outside of image bounds.
 * 
 * @author David Legland
 *
 */
public class ConstantBorder3D implements BorderManager3D
{

	ImageStack image;
	int value;

	public ConstantBorder3D(ImageStack image, int value)
	{
		this.image = image;
		this.value = value;
	}

	/**
	 * Returns either image pixel when position is inside image bounds, or a
	 * constant value when position is outside of image bounds.
	 * 
	 * @see inra.ijpb.data.border.BorderManager#get(int, int)
	 */
	@Override
	public int get(int x, int y, int z)
	{
		if (x < 0)
			return this.value;
		if (y < 0)
			return this.value;
		if (z < 0)
			return this.value;
		if (x >= this.image.getWidth())
			return this.value;
		if (y >= this.image.getHeight())
			return this.value;
		if (z >= this.image.getSize())
			return this.value;
		return (int) this.image.getVoxel(x, y, z);
	}

}
