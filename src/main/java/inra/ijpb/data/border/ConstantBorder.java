/**
 * 
 */
package inra.ijpb.data.border;

import ij.process.ImageProcessor;

/**
 * Returns either image pixel when position is inside image bounds, 
 * or a constant value when position is outside of image bounds.
 * @author David Legland
 *
 */
public class ConstantBorder implements BorderManager
{

	ImageProcessor image;
	float value;

	public ConstantBorder(ImageProcessor image, float value)
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
	public int get(int x, int y)
	{
		if (x < 0)
			return (int) this.value;
		if (y < 0)
			return (int) this.value;
		if (x >= this.image.getWidth())
			return (int) this.value;
		if (y >= this.image.getHeight())
			return (int) this.value;
		return this.image.get(x, y);
	}

	/**
	 * Returns either image pixel when position is inside image bounds, or a
	 * constant value when position is outside of image bounds.
	 * 
	 * @see inra.ijpb.data.border.BorderManager#getf(int, int)
	 */
	@Override
	public float getf(int x, int y)
	{
		if (x < 0)
			return this.value;
		if (y < 0)
			return this.value;
		if (x >= this.image.getWidth())
			return this.value;
		if (y >= this.image.getHeight())
			return this.value;
		return this.image.getf(x, y);
	}

}
