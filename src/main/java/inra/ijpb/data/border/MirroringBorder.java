/**
 * 
 */
package inra.ijpb.data.border;

import java.awt.Point;

import ij.process.ImageProcessor;

/**
 * Periodic border that considers image is mirrored indefinitely in all
 * directions.
 * @author David Legland
 *
 */
public class MirroringBorder implements BorderManager
{
	ImageProcessor image;

	public MirroringBorder(ImageProcessor image)
	{
		this.image = image;
	}

	/**
	 * @see inra.ijpb.data.border.BorderManager#get(int, int)
	 */
	@Override
	public int get(int x, int y)
	{
		Point p = computeCoords(x, y);
		return this.image.get(p.x, p.y);
	}

	@Override
	public float getf(int x, int y)
	{
		Point p = computeCoords(x, y);
		return this.image.getf(p.x, p.y);
	}

	private Point computeCoords(int x, int y)
	{
		int width = this.image.getWidth();
		int height = this.image.getHeight();
		x = x % (2 * width);
		y = y % (2 * height);
		if (x < 0)
			x = -x - 1;
		if (y < 0)
			y = -y - 1;
		if (x >= width)
			x = 2 * width - 1 - x;
		if (y >= height)
			y = 2 * height - 1 - y;
		return new Point(x, y);
	}
}
