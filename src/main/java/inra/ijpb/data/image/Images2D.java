/**
 * 
 */
package inra.ijpb.data.image;

import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import inra.ijpb.data.IntBounds2D;

/**
 * A collection of static methods for working on 2D images. 
 * 
 * @see Images3D
 * 
 * @author David Legland
 *
 */
public class Images2D
{
	public static final ImageProcessor createProcessor(int width, int height, int bitDepth)
	{
		switch (bitDepth)
		{
		case 8: return new ByteProcessor(width, height);
		case 16: return new ShortProcessor(width, height);
		case 24: return new ColorProcessor(width, height);
		case 32: return new FloatProcessor(width, height);
		default: 
			throw new RuntimeException("Bit depth is expected to be 8, 16, 24 or 32, not: " + bitDepth);
		}
	}

	public static final ImageProcessor crop(ImageProcessor image, IntBounds2D bounds)
	{
		// Compute size of result, taking into account border
		int sizeX2 = bounds.getWidth();
		int sizeY2 = bounds.getHeight();

		// allocate memory for result image
		ImageProcessor result = createProcessor(sizeX2, sizeY2, image.getBitDepth());

		int dx = bounds.getXMin();
		int dy = bounds.getYMin();

		// fill result with binary label
		for (int y = 0; y <= sizeY2; y++)
		{
			int y2 = y + dy;
			if (y2 < 0 || y2 > sizeY2) continue;

			for (int x = 0; x <= sizeX2; x++)
			{
				int x2 = x + dx;
				if (x2 < 0 || x2 > sizeX2) continue;
				result.setf(x, y, image.getf(x2, y2));
			}
		}

		return result;
	}

	/**
	 * Private constructor to prevent class instantiation.
	 */
	private Images2D()
	{
	}
}
