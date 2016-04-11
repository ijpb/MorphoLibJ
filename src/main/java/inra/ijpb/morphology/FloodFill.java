/**
 * 
 */
package inra.ijpb.morphology;

import static java.lang.Math.max;
import static java.lang.Math.min;
import ij.ImageStack;
import ij.process.ImageProcessor;

import java.awt.Point;
import java.util.ArrayList;

/**
 * <p>
 * Implements various flood-fill algorithms, for planar images. Rewritten from
 * ij.process.FloodFiller. Also support floating point images. The FloodFill3D
 * class provides support for 3D flood-fill algorithms.
 * </p>
 * 
 * <p>
 * Check also "http://en.wikipedia.org/wiki/Flood_fill".
 * </p>
 * 
 * @see FloodFill3D
 * @see MinimaAndMaxima
 * @see inra.ijpb.morphology.extrema.RegionalExtremaByFlooding
 * @see inra.ijpb.binary.BinaryImages#componentsLabeling(ImageProcessor, int,
 *      int)
 * 
 * @author David Legland
 */
public class FloodFill
{
	/**
	 * Private constructor to prevent class instantiation.
	 */
	private FloodFill()
	{
	}

	/**
	 * Replaces all the neighbor pixels of (x,y) that have the same values by
	 * the specified integer value, using the 4-connectivity.
	 * 
	 * @param image
	 *            the image in which floodfill will be propagated
	 * @param x
	 *            the x-coordinate of the seed pixel
	 * @param y
	 *            the y-coordinate of the seed pixel
	 * @param value
	 *            the new value of the connected component at (x,y)
	 * @param conn
	 *            the connectivity to use, either 4 or 8
	 */
	public final static void floodFill(ImageProcessor image, int x, int y,
			int value, int conn)
	{
		if (conn == 4)
			floodFillC4(image, x, y, value);
		else if (conn == 8)
			floodFillC8(image, x, y, value);
		else
			throw new IllegalArgumentException("Connectivity must be either 4 or 8, not " + conn);
	}
	
	/**
	 * Replaces all the neighbor pixels of (x,y) that have the same values by
	 * the specified floating-point value, using the 4-connectivity.
	 * 
	 * @param image
	 *            the image in which floodfill will be propagated
	 * @param x
	 *            the x-coordinate of the seed pixel
	 * @param y
	 *            the y-coordinate of the seed pixel
	 * @param value
	 *            the new value of the connected component at (x,y)
	 */
	private final static void floodFillC4(ImageProcessor image, int x, int y,
			int value)
	{
		// get image size
		int width = image.getWidth();
		int height = image.getHeight();
		
		// get old value
		int oldValue = image.getPixel(x, y);
		
		// test if already the right value 
		if (oldValue == value) 
			return ;
		
		// initialize the stack with original pixel
		ArrayList<Point> stack = new ArrayList<Point>();
		stack.add(new Point(x, y));
		
		boolean inScanLine;
		
		// process all items in stack
		while (!stack.isEmpty())
		{
			// Extract current position
			Point p = stack.remove(stack.size()-1);
			x = p.x;
			y = p.y;
			
			// process only pixel with the same value
			if (image.get(x, y) != oldValue) 
				continue;
			
			// x extremities of scan-line
			int x1 = x; 
			int x2 = x;
			
			// find start of scan-line
			while (x1 > 0 && image.getPixel(x1-1, y) == oldValue)
				x1--;
			
			// find end of scan-line
			while (x2 < width - 1 && image.getPixel(x2+1, y) == oldValue)
				x2++;
			
			// fill current scan-line
			fillLine(image, y, x1, x2, value);

			// find scan-lines above the current one
			if (y > 0)
			{
				inScanLine = false;
				for (int i = x1; i <= x2; i++)
				{
					int val = image.get(i, y - 1);
					if (!inScanLine && val == oldValue)
					{
						stack.add(new Point(i, y - 1));
						inScanLine = true;
					} 
					else if (inScanLine && val != oldValue)
					{
						inScanLine = false;
					}
				}
			}

			// find scan-lines below the current one
			if (y < height - 1)
			{
				inScanLine = false;
				for (int i = x1; i <= x2; i++)
				{
					int val = image.getPixel(i, y + 1);
					if (!inScanLine && val == oldValue)
					{
						stack.add(new Point(i, y + 1));
						inScanLine = true;
					}
					else if (inScanLine && val != oldValue)
					{
						inScanLine = false;
					}
				}
			}
		}
	}

	/**
	 * Replaces all the pixels in the 8-neighborhood of (x,y) that have the same
	 * values by the specified value.
	 * 
	 * @param image
	 *            the image in which floodfill will be propagated
	 * @param x
	 *            the x-coordinate of the seed pixel
	 * @param y
	 *            the y-coordinate of the seed pixel
	 * @param value
	 *            the new value of the connected component at (x,y)
	 */
	private final static void floodFillC8(ImageProcessor image, int x, int y,
			int value)
	{
		// get image size
		int width = image.getWidth();
		int height = image.getHeight();

		// get old value
		int oldValue = image.getPixel(x, y);

		// test if already the right value
		if (oldValue == value)
			return;

		// initialize the stack with original pixel
		ArrayList<Point> stack = new ArrayList<Point>();
		stack.add(new Point(x, y));

		boolean inScanLine;

		// process all items in stack
		while (!stack.isEmpty())
		{
			// Extract current position
			Point p = stack.remove(stack.size() - 1);
			x = p.x;
			y = p.y;

			// process only pixel with the same value
			if (image.get(x, y) != oldValue)
				continue;

			// x extremities of scan-line
			int x1 = x;
			int x2 = x;

			// find start of scan-line
			while (x1 > 0 && image.getPixel(x1 - 1, y) == oldValue)
				x1--;

			// find end of scan-line
			while (x2 < width - 1 && image.getPixel(x2 + 1, y) == oldValue)
				x2++;

			// fill current scan-line
			fillLine(image, y, x1, x2, value);

			// find scan-lines above the current one
			if (y > 0)
			{
				inScanLine = false;
				for (int i = Math.max(x1 - 1, 0); i <= Math.min(x2 + 1, width - 1); i++)
				{
					int val = image.get(i, y - 1);
					if (!inScanLine && val == oldValue)
					{
						stack.add(new Point(i, y - 1));
						inScanLine = true;
					} else if (inScanLine && val != oldValue)
						inScanLine = false;
				}
			}

			// find scan-lines below the current one
			if (y < height - 1)
			{
				inScanLine = false;
				for (int i = Math.max(x1 - 1, 0); i <= Math.min(x2 + 1, width - 1); i++)
				{
					int val = image.getPixel(i, y + 1);
					if (!inScanLine && val == oldValue)
					{
						stack.add(new Point(i, y + 1));
						inScanLine = true;
					} 
					else if (inScanLine && val != oldValue)
					{
						inScanLine = false;
					}
				}
			}
		}
	}

	/**
	 * Replaces all the neighbor pixels of (x,y) that have the same values by
	 * the specified integer value, using the 4-connectivity.
	 * 
	 * @param image
	 *            the image in which floodfill will be propagated
	 * @param x
	 *            the x-coordinate of the seed pixel
	 * @param y
	 *            the y-coordinate of the seed pixel
	 * @param value
	 *            the new value of the connected component at (x,y)
	 * @param conn
	 *            the connectivity to use, either 4 or 8
	 */
	public final static void floodFill(ImageProcessor image, int x, int y,
			float value, int conn)
	{
		if (conn == 4)
			floodFillC4(image, x, y, value);
		else if (conn == 8)
			floodFillC8(image, x, y, value);
		else
			throw new IllegalArgumentException("Connectivity must be either 4 or 8, not " + conn);
	}
	
	/**
	 * Replaces all the pixels in the 4-neighborhood of (x,y) that have the same
	 * values as the pixel in (x,y) by the specified floating point value, using
	 * the 4-connectivity. Should work the same way for all type of images.
	 * 
	 * @param image
	 *            the image in which floodfill will be propagated
	 * @param x
	 *            the x-coordinate of the seed pixel
	 * @param y
	 *            the y-coordinate of the seed pixel
	 * @param value
	 *            the new value of the connected component at (x,y)
	 */
	private final static void floodFillC4(ImageProcessor image, int x, int y,
			float value)
	{
		// get image size
		int width = image.getWidth();
		int height = image.getHeight();
		
		// get old value
		float oldValue = image.getf(x, y);
		
		// test if already the right value 
		if (oldValue == value) 
			return ;
		
		// initialize the stack with original pixel
		ArrayList<Point> stack = new ArrayList<Point>();
		stack.add(new Point(x, y));
		
		boolean inScanLine;
		
		// process all items in stack
		while (!stack.isEmpty())
		{
			// Extract current position
			Point p = stack.remove(stack.size() - 1);
			x = p.x;
			y = p.y;

			// process only pixel of the same value
			if (image.getf(x, y) != oldValue)
				continue;

			// x extremities of scan-line
			int x1 = x;
			int x2 = x;

			// find start of scan-line
			while (x1 > 0 && image.getf(x1 - 1, y) == oldValue)
				x1--;

			// find end of scan-line
			while (x2 < width - 1 && image.getf(x2 + 1, y) == oldValue)
				x2++;

			// fill current scan-line
			fillLine(image, y, x1, x2, value);

			// find scan-lines above the current one
			if (y > 0)
			{
				inScanLine = false;
				for (int i = x1; i <= x2; i++)
				{
					float val = image.getf(i, y - 1);
					if (!inScanLine && val == oldValue)
					{
						stack.add(new Point(i, y - 1));
						inScanLine = true;
					}
					else if (inScanLine && val != oldValue)
					{
						inScanLine = false;
					}
				}
			}

			// find scan-lines below the current one
			if (y < height - 1)
			{
				inScanLine = false;
				for (int i = x1; i <= x2; i++)
				{
					float val = image.getf(i, y + 1);
					if (!inScanLine && val == oldValue)
					{
						stack.add(new Point(i, y + 1));
						inScanLine = true;
					} 
					else if (inScanLine && val != oldValue)
					{
						inScanLine = false;
					}
				}
			}
		}
	}


	/**
	 * Replaces all the pixels in the 8-neighborhood of (x,y) that have the same
	 * values as the pixel in (x,y) by the specified value. Should work for all
	 * integer based images: ByteProcessor, ShortProcessor and ColorProcessor.
	 * 
	 * @param image
	 *            the image in which floodfill will be propagated
	 * @param x
	 *            the x-coordinate of the seed pixel
	 * @param y
	 *            the y-coordinate of the seed pixel
	 * @param value
	 *            the new value of the connected component at (x,y)
	 */
	private final static void floodFillC8(ImageProcessor image, int x, int y,
			float value)
	{
		// get image size
		int width = image.getWidth();
		int height = image.getHeight();

		// get old value
		float oldValue = image.getf(x, y);

		// test if already the right value
		if (oldValue == value)
			return;

		// initialize the stack with original pixel
		ArrayList<Point> stack = new ArrayList<Point>();
		stack.add(new Point(x, y));

		boolean inScanLine;

		// process all items in stack
		while (!stack.isEmpty())
		{
			// Extract current position
			Point p = stack.remove(stack.size() - 1);
			x = p.x;
			y = p.y;

			// process only pixel with the same value
			if (image.getf(x, y) != oldValue)
				continue;

			// x extremities of scan-line
			int x1 = x;
			int x2 = x;

			// find start of scan-line
			while (x1 > 0 && image.getf(x1 - 1, y) == oldValue)
				x1--;

			// find end of scan-line
			while (x2 < width - 1 && image.getf(x2 + 1, y) == oldValue)
				x2++;

			// fill current scan-line
			fillLine(image, y, x1, x2, value);

			// find scan-lines above the current one
			if (y > 0)
			{
				inScanLine = false;
				for (int i = Math.max(x1 - 1, 0); i <= Math.min(x2 + 1, width - 1); i++)
				{
					float val = image.getf(i, y - 1);
					if (!inScanLine && val == oldValue)
					{
						stack.add(new Point(i, y - 1));
						inScanLine = true;
					} 
					else if (inScanLine && val != oldValue)
					{
						inScanLine = false;
					}
				}
			}

			// find scan-lines below the current one
			if (y < height - 1)
			{
				inScanLine = false;
				for (int i = Math.max(x1 - 1, 0); i <= Math.min(x2 + 1, width - 1); i++)
				{
					float val = image.getf(i, y + 1);
					if (!inScanLine && val == oldValue)
					{
						stack.add(new Point(i, y + 1));
						inScanLine = true;
					} 
					else if (inScanLine && val != oldValue)
					{
						inScanLine = false;
					}
				}
			}
		}
	}

	/**
	 * Assigns in <code>labelImage</code> all the neighbor pixels of (x,y) that
	 * have the same pixel value in <code>image</code>, the specified new label
	 * value (<code>value</code>), using the specified connectivity.
	 * 
	 * @param inputImage
	 *            original image to read the pixel values from
	 * @param x
	 *            x- coordinate of the seed pixel
	 * @param y
	 *            y- coordinate of the seed pixel
	 * @param outputImage
	 *            the label image to fill in
	 * @param value
	 *            filling value
	 * @param conn
	 *            connectivity to use (4 or 8)
	 */
	public final static void floodFill(ImageProcessor inputImage, int x,
			int y, ImageProcessor outputImage, int value, int conn)
	{

		// the shifts to look for new markers to start lines
		int dx1 = 0;
		int dx2 = 0;
		if (conn == 8)
		{
			dx1 = -1;
			dx2 = +1;
		}
		
		// get image size
		int width = inputImage.getWidth();
		int height = inputImage.getHeight();
		
		// get old value
		int oldValue = inputImage.getPixel(x, y);
				
		// initialize the stack with original pixel
		ArrayList<Point> stack = new ArrayList<Point>();
		stack.add(new Point(x, y));
		
		boolean inScanLine;
		
		// process all items in stack
		while (!stack.isEmpty()) 
		{
			// Extract current position
			Point p = stack.remove(stack.size()-1);
			x = p.x;
			y = p.y;
			
			// process only pixel of the same value
			if (inputImage.get(x, y) != oldValue) 
				continue;
			
			// x extremities of scan-line
			int x1 = x; 
			int x2 = x;
			
			// find start of scan-line
			while (x1 > 0 && inputImage.get(x1-1, y) == oldValue)
				x1--;
			
			// find end of scan-line
			while (x2 < width - 1 && inputImage.get(x2+1, y) == oldValue)
				x2++;
			
			// fill current scan-line
			fillLine(outputImage, y, x1, x2, value);
			
			
			// find scan-lines above the current one
			if (y > 0)
			{
				inScanLine = false;
				for (int i = max(x1 + dx1, 0); i <= min(x2 + dx2, width - 1); i++)
				{
					int val = inputImage.get(i, y - 1);
					int lab = (int) outputImage.get(i, y - 1);
					if (!inScanLine && val == oldValue && lab != value)
					{
						stack.add(new Point(i, y - 1));
						inScanLine = true;
					} 
					else if (inScanLine && val != oldValue)
					{
						inScanLine = false;
					}
				}
			}

			// find scan-lines below the current one
			if (y < height - 1)
			{
				inScanLine = false;
				for (int i = max(x1 + dx1, 0); i <= min(x2 + dx2, width - 1); i++)
				{
					int val = inputImage.getPixel(i, y + 1);
					int lab = (int) outputImage.get(i, y + 1);
					if (!inScanLine && val == oldValue && lab != value)
					{
						stack.add(new Point(i, y + 1));
						inScanLine = true;
					} 
					else if (inScanLine && val != oldValue)
					{
						inScanLine = false;
					}
				}
			}
		}
	}

	/**
	 * Assigns in <code>labelImage</code> all the neighbor pixels of (x,y) that
	 * have the same pixel value in <code>image</code>, the specified new label
	 * value (<code>value</code>), using the specified connectivity.
	 * 
	 * @param inputImage
	 *            original image to read the pixel values from
	 * @param x
	 *            x- coordinate of the seed pixel
	 * @param y
	 *            y- coordinate of the seed pixel
	 * @param outputImage
	 *            the label image to fill in
	 * @param value
	 *            filling value
	 * @param conn
	 *            connectivity to use (4 or 8)
	 */
	public final static void floodFillFloat(ImageProcessor inputImage, int x,
			int y, ImageProcessor outputImage, float value, int conn)
	{
		// the shifts to look for new markers to start lines
		int dx1 = 0;
		int dx2 = 0;
		if (conn == 8) 
		{
			dx1 = -1;
			dx2 = +1;
		}
		
		// get image size
		int width = inputImage.getWidth();
		int height = inputImage.getHeight();
		
		// get old value
		float oldValue = inputImage.getf(x, y);
		
		// initialize the stack with original pixel
		ArrayList<Point> stack = new ArrayList<Point>();
		stack.add(new Point(x, y));
		
		
		boolean inScanLine;
		
		// process all items in stack
		while (!stack.isEmpty()) 
		{
			// Extract current position
			Point p = stack.remove(stack.size()-1);
			x = p.x;
			y = p.y;
			
			// process only pixel of the same value
			if (inputImage.getf(x, y) != oldValue) 
				continue;
			
			// x extremities of scan-line
			int x1 = x; 
			int x2 = x;
			
			// find start of scan-line
			while (x1 > 0 && inputImage.getf(x1-1, y) == oldValue)
				x1--;
			
			// find end of scan-line
			while (x2 < width - 1 && inputImage.getf(x2+1, y) == oldValue)
				x2++;
			
			// fill current scan-line
			fillLine(outputImage, y, x1, x2, value);
			
			// find scan-lines above the current one
			if (y > 0)
			{
				inScanLine = false;
				for (int i = max(x1 + dx1, 0); i <= min(x2 + dx2, width - 1); i++)
				{
					float val = inputImage.getf(i, y - 1);
					float lab = outputImage.getf(i, y - 1);
					if (!inScanLine && val == oldValue && lab != value)
					{
						stack.add(new Point(i, y - 1));
						inScanLine = true;
					} 
					else if (inScanLine && val != oldValue)
					{
						inScanLine = false;
					}
				}
			}

			// find scan-lines below the current one
			if (y < height - 1)
			{
				inScanLine = false;
				for (int i = max(x1 + dx1, 0); i <= min(x2 + dx2, width - 1); i++)
				{
					float val = inputImage.getf(i, y + 1);
					float lab = outputImage.getf(i, y + 1);
					if (!inScanLine && val == oldValue && lab != value)
					{
						stack.add(new Point(i, y + 1));
						inScanLine = true;
					} 
					else if (inScanLine && val != oldValue)
					{
						inScanLine = false;
					}
				}
			}
		}
	}

	/**
	 * In the input image, replaces all pixels in row <code>y</code> located
	 * between <code>x1</code> and <code>x2</code> (inclusive) by the given
	 * value.
	 * 
	 * @param ip
	 *            the input image to modify
	 * @param y
	 *            the index of the row to modify
	 * @param x1
	 *            the column index of the first pixel to modify
	 * @param x2
	 *            the column index of the first pixel to modify
	 * @param value
	 *            the new value of the pixels
	 */
	private final static void fillLine(ImageProcessor ip, int y, int x1,
			int x2, int value)
	{
		if (x1 > x2)
		{
			int t = x1;
			x1 = x2;
			x2 = t;
		}
		
		for (int x = x1; x <= x2; x++)
			ip.set(x, y, value);
	}

	/**
	 * Fill in the horizontal line define by y-coordinate and the two x 
	 * coordinate extremities (inclusive), with the specified integer value.
	 * the value x1 must be lower than or equal the value x2. 
	 */
	private final static void fillLine(ImageProcessor ip, int y, int x1,
			int x2, float value)
	{
		for (int x = x1; x <= x2; x++)
			ip.setf(x, y, value);
	}

	/**
	 * Replaces all the pixels in the 6-neighborhood of (x,y,z) that have the
	 * same values as the pixel in (x,y,z) by the specified value. Should work
	 * for all integer based 3D images.
	 * 
	 * @param image
	 *            the 3D image in which floodfill will be propagated
	 * @param x
	 *            the x-coordinate of the seed voxel
	 * @param y
	 *            the y-coordinate of the seed voxel
	 * @param z
	 *            the z-coordinate of the seed voxel
	 * @param value
	 *            the new value of the connected component at (x,y,z)
	 * @param conn
	 * 			  the connectivity to use, either 6 or 26
	 * @dperecated use FloodFill3D.floodFill instead.
	 */
	@Deprecated
	public final static void floodFill(ImageStack image, int x, int y, int z,
			int value, int conn)
	{
		FloodFill3D.floodFill(image, x, y, z, value, conn);
	}

	/**
	 * Replaces all the pixels in the 6-neighborhood of (x,y,z) that have the
	 * same values as the pixel in (x,y,z) by the specified floating point
	 * value. Should work the same way for all type of images.
	 * 
	 * @param image
	 *            the 3D image in which floodfill will be propagated
	 * @param x
	 *            the x-coordinate of the seed voxel
	 * @param y
	 *            the y-coordinate of the seed voxel
	 * @param z
	 *            the z-coordinate of the seed voxel
	 * @param value
	 *            the new value of the connected component at (x,y,z)
	 * @param conn
	 * 			  the connectivity to use, either 6 or 26
	 * @deprecated use FLoodFill3D.floodFill instead 
	 */
	@Deprecated
	public final static void floodFill(ImageStack image, int x, int y, int z,
			double value, int conn)
	{
		FloodFill3D.floodFill(image, x, y, z, value, conn);
	}


	/**
	 * Assign to all the neighbor voxels of (x,y,z) that have the same voxel
	 * value in <code>image</code>, the specified new label value (
	 * <code>value</code>) in <code>labelImage</code>, using the specified
	 * connectivity.
	 * 
	 * @param inputImage
	 *            original image to read the voxel values from (should be
	 *            integer based)
	 * @param x
	 *            x- coordinate of the seed voxel
	 * @param y
	 *            y- coordinate of the seed voxel
	 * @param z
	 *            z- coordinate of the seed voxel
	 * @param outputImage
	 *            output image to fill (should be integer based)
	 * @param value
	 *            filling value
	 * @param conn
	 *            connectivity to use (6 or 26)
	 * @deprecated use FloodFill3D.floodFill instead            
	 */
	@Deprecated 
	public final static void floodFill(ImageStack inputImage, int x, int y,
			int z, ImageStack outputImage, int value, int conn)
	{
		FloodFill3D.floodFill(inputImage, x, y, z, outputImage, value, conn);
	}

	/**
	 * Assigns to all the neighbor voxels of (x,y,z) that have the same voxel
	 * value in <code>image</code>, the specified new label value (
	 * <code>value</code>) in <code>labelImage</code>, using the specified
	 * connectivity.
	 * 
	 * @param inputImage
	 *            original image to read the voxel values from
	 * @param x
	 *            x- coordinate of the seed voxel
	 * @param y
	 *            y- coordinate of the seed voxel
	 * @param z
	 *            z- coordinate of the seed voxel
	 * @param outputImage
	 *            output label image (to fill)
	 * @param value
	 *            filling value
	 * @param conn
	 *            connectivity to use (6 or 26)
	 * @deprecated use FloodFill3D.floodFillFloat instead
	 */
	@Deprecated
	public final static void floodFillFloat(ImageStack inputImage, int x,
			int y, int z, ImageStack outputImage, float value, int conn)
	{
		FloodFill3D.floodFillFloat(inputImage, x, y, z, outputImage, value, conn);
	}
}
