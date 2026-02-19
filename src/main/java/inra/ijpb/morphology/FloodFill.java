/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2026 INRA.
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
     * Replaces all the neighbor pixels of (x0,y0) that have the same values by
     * the specified integer value, using the specified connectivity.
     * 
     * @param image
     *            the image in which floodfill will be propagated
     * @param x0
     *            the x-coordinate of the seed pixel
     * @param y0
     *            the y-coordinate of the seed pixel
     * @param value
     *            the new value of the connected component at (x,y)
     * @param conn
     *            the connectivity to use, either 4 or 8
     */
	public final static void floodFill(ImageProcessor image, int x0, int y0,
			int value, int conn)
	{
		if (conn == 4)
			floodFillC4(image, x0, y0, value);
		else if (conn == 8)
			floodFillC8(image, x0, y0, value);
		else
			throw new IllegalArgumentException("Connectivity must be either 4 or 8, not " + conn);
	}
	
    /**
     * Replaces all the pixels connected to (x0,y0) that have the same values by
     * the specified floating-point value, using the 4-connectivity.
     * 
     * @param image
     *            the image in which floodfill will be propagated
     * @param x0
     *            the x-coordinate of the seed pixel
     * @param y0
     *            the y-coordinate of the seed pixel
     * @param value
     *            the new value of the connected component at (x,y)
     */
	private final static void floodFillC4(ImageProcessor image, int x0, int y0,
			int value)
	{
		// get image size
		int width = image.getWidth();
		int height = image.getHeight();
		
		// get old value
		int oldValue = image.getPixel(x0, y0);
		
		// test if already the right value 
		if (oldValue == value) 
			return ;
		
		// initialize the stack with original pixel
		ArrayList<Point> stack = new ArrayList<Point>();
		stack.add(new Point(x0, y0));
		
		// process all items in stack
		while (!stack.isEmpty())
		{
			// Extract current position
			Point p = stack.remove(stack.size()-1);
			x0 = p.x;
			y0 = p.y;
			
			// process only pixel with the same value
			if (image.get(x0, y0) != oldValue) 
				continue;
			
			// x extremities of scan-line
			int x1 = x0; 
			int x2 = x0;
			
			// find start of scan-line
			while (x1 > 0 && image.getPixel(x1-1, y0) == oldValue)
				x1--;
			
			// find end of scan-line
			while (x2 < width - 1 && image.getPixel(x2+1, y0) == oldValue)
				x2++;
			
			// fill current scan-line
            for (int x = x1; x <= x2; x++)
            {
                image.set(x, y0, value);
            }

			// find scan-lines above the current one
			if (y0 > 0)
			{
			    boolean inScanLine = false;
				for (int i = x1; i <= x2; i++)
				{
					int val = image.get(i, y0 - 1);
					if (!inScanLine && val == oldValue)
					{
						stack.add(new Point(i, y0 - 1));
						inScanLine = true;
					} 
					else if (inScanLine && val != oldValue)
					{
						inScanLine = false;
					}
				}
			}

			// find scan-lines below the current one
			if (y0 < height - 1)
			{
			    boolean inScanLine = false;
				for (int i = x1; i <= x2; i++)
				{
					int val = image.getPixel(i, y0 + 1);
					if (!inScanLine && val == oldValue)
					{
						stack.add(new Point(i, y0 + 1));
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
     * Replaces all the pixels connected to (x0,y0) that have the same values by
     * the specified floating-point value, using the 8-connectivity.
     * 
	 * @param image
	 *            the image in which floodfill will be propagated
	 * @param x0
	 *            the x-coordinate of the seed pixel
	 * @param y0
	 *            the y-coordinate of the seed pixel
	 * @param value
	 *            the new value of the connected component at (x,y)
	 */
	private final static void floodFillC8(ImageProcessor image, int x0, int y0,
			int value)
	{
		// get image size
		int width = image.getWidth();
		int height = image.getHeight();

		// get old value
		int oldValue = image.getPixel(x0, y0);

		// test if already the right value
		if (oldValue == value)
			return;

		// initialize the stack with original pixel
		ArrayList<Point> stack = new ArrayList<Point>();
		stack.add(new Point(x0, y0));

		// process all items in stack
		while (!stack.isEmpty())
		{
			// Extract current position
			Point p = stack.remove(stack.size() - 1);
			x0 = p.x;
			y0 = p.y;

			// process only pixel with the same value
			if (image.get(x0, y0) != oldValue)
				continue;

			// x extremities of scan-line
			int x1 = x0;
			int x2 = x0;

			// find start of scan-line
			while (x1 > 0 && image.getPixel(x1 - 1, y0) == oldValue)
				x1--;

			// find end of scan-line
			while (x2 < width - 1 && image.getPixel(x2 + 1, y0) == oldValue)
				x2++;

			// fill current scan-line
			for (int x = x1; x <= x2; x++)
            {
                image.set(x, y0, value);
            }

			// find scan-lines above the current one
			if (y0 > 0)
			{
			    boolean inScanLine = false;
				for (int i = Math.max(x1 - 1, 0); i <= Math.min(x2 + 1, width - 1); i++)
				{
					int val = image.get(i, y0 - 1);
					if (!inScanLine && val == oldValue)
					{
						stack.add(new Point(i, y0 - 1));
						inScanLine = true;
					} else if (inScanLine && val != oldValue)
						inScanLine = false;
				}
			}

			// find scan-lines below the current one
			if (y0 < height - 1)
			{
			    boolean inScanLine = false;
				for (int i = Math.max(x1 - 1, 0); i <= Math.min(x2 + 1, width - 1); i++)
				{
					int val = image.getPixel(i, y0 + 1);
					if (!inScanLine && val == oldValue)
					{
						stack.add(new Point(i, y0 + 1));
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
	 * the specified integer value, using the specified connectivity.
	 * 
	 * @param image
	 *            the image in which floodfill will be propagated
	 * @param x0
	 *            the x-coordinate of the seed pixel
	 * @param y0
	 *            the y-coordinate of the seed pixel
	 * @param value
	 *            the new value of the connected component at (x,y)
	 * @param conn
	 *            the connectivity to use, either 4 or 8
	 */
	public final static void floodFill(ImageProcessor image, int x0, int y0,
			float value, int conn)
	{
		if (conn == 4)
			floodFillC4(image, x0, y0, value);
		else if (conn == 8)
			floodFillC8(image, x0, y0, value);
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
	 * @param x0
	 *            the x-coordinate of the seed pixel
	 * @param y0
	 *            the y-coordinate of the seed pixel
	 * @param value
	 *            the new value of the connected component at (x,y)
	 */
	private final static void floodFillC4(ImageProcessor image, int x0, int y0,
			float value)
	{
		// get image size
		int width = image.getWidth();
		int height = image.getHeight();
		
		// get old value
		float oldValue = image.getf(x0, y0);
		
		// test if already the right value 
		if (oldValue == value) 
			return ;
		
		// initialize the stack with original pixel
		ArrayList<Point> stack = new ArrayList<Point>();
		stack.add(new Point(x0, y0));
		
		// process all items in stack
		while (!stack.isEmpty())
		{
			// Extract current position
			Point p = stack.remove(stack.size() - 1);
			x0 = p.x;
			y0 = p.y;

			// process only pixel of the same value
			if (image.getf(x0, y0) != oldValue)
				continue;

			// x extremities of scan-line
			int x1 = x0;
			int x2 = x0;

			// find start of scan-line
			while (x1 > 0 && image.getf(x1 - 1, y0) == oldValue)
				x1--;

			// find end of scan-line
			while (x2 < width - 1 && image.getf(x2 + 1, y0) == oldValue)
				x2++;

			// fill current scan-line
            for (int x = x1; x <= x2; x++)
            {
                image.setf(x, y0, value);
            }

			// find scan-lines above the current one
			if (y0 > 0)
			{
			    boolean inScanLine = false;
				for (int i = x1; i <= x2; i++)
				{
					float val = image.getf(i, y0 - 1);
					if (!inScanLine && val == oldValue)
					{
						stack.add(new Point(i, y0 - 1));
						inScanLine = true;
					}
					else if (inScanLine && val != oldValue)
					{
						inScanLine = false;
					}
				}
			}

			// find scan-lines below the current one
			if (y0 < height - 1)
			{
			    boolean inScanLine = false;
				for (int i = x1; i <= x2; i++)
				{
					float val = image.getf(i, y0 + 1);
					if (!inScanLine && val == oldValue)
					{
						stack.add(new Point(i, y0 + 1));
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
	 * Replaces all the pixels in the 8-neighborhood of (x0,y0) that have the same
	 * values as the pixel in (x0,y0) by the specified value. Should work for all
	 * integer based images: ByteProcessor, ShortProcessor and ColorProcessor.
	 * 
	 * @param image
	 *            the image in which floodfill will be propagated
	 * @param x0
	 *            the x-coordinate of the seed pixel
	 * @param y0
	 *            the y-coordinate of the seed pixel
	 * @param value
	 *            the new value of the connected component at (x,y)
	 */
	private final static void floodFillC8(ImageProcessor image, int x0, int y0,
			float value)
	{
		// get image size
		int width = image.getWidth();
		int height = image.getHeight();

		// get old value
		float oldValue = image.getf(x0, y0);

		// test if already the right value
		if (oldValue == value)
			return;

		// initialize the stack with original pixel
		ArrayList<Point> stack = new ArrayList<Point>();
		stack.add(new Point(x0, y0));

		// process all items in stack
		while (!stack.isEmpty())
		{
			// Extract current position
			Point p = stack.remove(stack.size() - 1);
			x0 = p.x;
			y0 = p.y;

			// process only pixel with the same value
			if (image.getf(x0, y0) != oldValue)
				continue;

			// x extremities of scan-line
			int x1 = x0;
			int x2 = x0;

			// find start of scan-line
			while (x1 > 0 && image.getf(x1 - 1, y0) == oldValue)
				x1--;

			// find end of scan-line
			while (x2 < width - 1 && image.getf(x2 + 1, y0) == oldValue)
				x2++;

			// fill current scan-line
			for (int x = x1; x <= x2; x++)
            {
                image.setf(x, y0, value);
            }

			// find scan-lines above the current one
			if (y0 > 0)
			{
			    boolean inScanLine = false;
				for (int i = Math.max(x1 - 1, 0); i <= Math.min(x2 + 1, width - 1); i++)
				{
					float val = image.getf(i, y0 - 1);
					if (!inScanLine && val == oldValue)
					{
						stack.add(new Point(i, y0 - 1));
						inScanLine = true;
					} 
					else if (inScanLine && val != oldValue)
					{
						inScanLine = false;
					}
				}
			}

			// find scan-lines below the current one
			if (y0 < height - 1)
			{
			    boolean inScanLine = false;
				for (int i = Math.max(x1 - 1, 0); i <= Math.min(x2 + 1, width - 1); i++)
				{
					float val = image.getf(i, y0 + 1);
					if (!inScanLine && val == oldValue)
					{
						stack.add(new Point(i, y0 + 1));
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
     * Assigns in <code>outputImage</code> all the pixels connected to (x0,y0)
     * that have the same pixel value in <code>image</code>, the specified new
     * value (<code>value</code>), using the specified connectivity.
     * 
     * @param inputImage
     *            original image to read the pixel values from
     * @param x0
     *            the x-coordinate of the seed pixel
     * @param y0
     *            the y-coordinate of the seed pixel
     * @param outputImage
     *            the label image to fill in
     * @param value
     *            filling value
     * @param conn
     *            connectivity to use (4 or 8)
     */
	public final static void floodFill(ImageProcessor inputImage, int x0,
			int y0, ImageProcessor outputImage, int value, int conn)
	{
		// the shifts to look for new markers to start lines
        final int dx1 = conn == 4 ? 0 : -1;
        final int dx2 = conn == 4 ? 0 : +1;
		
		// get image size
		int sizeX = inputImage.getWidth();
		int sizeY = inputImage.getHeight();
		
		// get old value
		int oldValue = inputImage.getPixel(x0, y0);
				
		// initialize the stack with seed pixel
		ArrayList<Point> stack = new ArrayList<Point>();
		stack.add(new Point(x0, y0));
		
		// process all items in stack
		while (!stack.isEmpty()) 
		{
			// retrieve coordinates of current marker pixel
			Point p = stack.remove(stack.size()-1);
			int px = p.x;
			int py = p.y;
			
			// process only pixel of the same value
			if (inputImage.get(px, py) != oldValue) 
				continue;
			
			// x extremities of scan-line
			int x1 = px; 
			int x2 = px;
			
			// find start of scan-line
			while (x1 > 0 && inputImage.get(x1-1, py) == oldValue)
				x1--;
			
			// find end of scan-line
			while (x2 < sizeX - 1 && inputImage.get(x2+1, py) == oldValue)
				x2++;
			
			// fill current scan-line
			for (int x = x1; x <= x2; x++)
			{
			    outputImage.set(x, py, value);
			}
			
			// find scan-lines above the current one
			if (py > 0)
			{
			    boolean inScanLine = false;
				for (int i = max(x1 + dx1, 0); i <= min(x2 + dx2, sizeX - 1); i++)
				{
					int val = inputImage.get(i, py - 1);
					int lab = (int) outputImage.get(i, py - 1);
					if (!inScanLine && val == oldValue && lab != value)
					{
						stack.add(new Point(i, py - 1));
						inScanLine = true;
					} 
					else if (inScanLine && val != oldValue)
					{
						inScanLine = false;
					}
				}
			}

			// find scan-lines below the current one
			if (py < sizeY - 1)
			{
			    boolean inScanLine = false;
				for (int i = max(x1 + dx1, 0); i <= min(x2 + dx2, sizeX - 1); i++)
				{
					int val = inputImage.getPixel(i, py + 1);
					int lab = (int) outputImage.get(i, py + 1);
					if (!inScanLine && val == oldValue && lab != value)
					{
						stack.add(new Point(i, py + 1));
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
	 * Assigns in <code>labelImage</code> all the neighbor pixels of (x0,y0) that
	 * have the same pixel value in <code>image</code>, the specified new label
	 * value (<code>value</code>), using the specified connectivity.
	 * 
	 * @param inputImage
	 *            original image to read the pixel values from
     * @param x0
     *            the x-coordinate of the seed pixel
     * @param y0
     *            the y-coordinate of the seed pixel
	 * @param outputImage
	 *            the label image to fill in
	 * @param value
	 *            filling value
	 * @param conn
	 *            connectivity to use (4 or 8)
	 */
	public final static void floodFillFloat(ImageProcessor inputImage, int x0,
			int y0, ImageProcessor outputImage, float value, int conn)
	{
        // the shifts to look for new markers to start lines
        final int dx1 = conn == 4 ? 0 : -1;
        final int dx2 = conn == 4 ? 0 : +1;
		
		// get image size
		int sizeX = inputImage.getWidth();
		int sizeY = inputImage.getHeight();
		
		// get old value
		float oldValue = inputImage.getf(x0, y0);
		
		// initialize the stack with seed pixel
		ArrayList<Point> stack = new ArrayList<Point>();
		stack.add(new Point(x0, y0));
		
		// process all items in stack
		while (!stack.isEmpty()) 
		{
            // retrieve coordinates of current marker pixel
			Point p = stack.remove(stack.size()-1);
			int px = p.x;
			int py = p.y;
			
			// process only pixel of the same value
			if (inputImage.getf(px, py) != oldValue) 
				continue;
			
			// x extremities of scan-line
			int x1 = px; 
			int x2 = px;
			
			// find start of scan-line
			while (x1 > 0 && inputImage.getf(x1-1, py) == oldValue)
				x1--;
			
			// find end of scan-line
			while (x2 < sizeX - 1 && inputImage.getf(x2+1, py) == oldValue)
				x2++;
			
			// fill current scan-line
			for (int x = x1; x <= x2; x++)
			{
			    outputImage.setf(x, py, value);
			}
		     
			// find scan-lines above the current one
			if (py > 0)
			{
				boolean inScanLine = false;
				for (int i = max(x1 + dx1, 0); i <= min(x2 + dx2, sizeX - 1); i++)
				{
					float val = inputImage.getf(i, py - 1);
					float lab = outputImage.getf(i, py - 1);
					if (!inScanLine && val == oldValue && lab != value)
					{
						stack.add(new Point(i, py - 1));
						inScanLine = true;
					} 
					else if (inScanLine && val != oldValue)
					{
						inScanLine = false;
					}
				}
			}

			// find scan-lines below the current one
			if (py < sizeY - 1)
			{
				boolean inScanLine = false;
				for (int i = max(x1 + dx1, 0); i <= min(x2 + dx2, sizeX - 1); i++)
				{
					float val = inputImage.getf(i, py + 1);
					float lab = outputImage.getf(i, py + 1);
					if (!inScanLine && val == oldValue && lab != value)
					{
						stack.add(new Point(i, py + 1));
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
	 * @deprecated use FloodFill3D.floodFill instead.
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

    /**
     * Private constructor to prevent class instantiation.
     */
    private FloodFill()
    {
    }
}
