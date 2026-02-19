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
import inra.ijpb.data.image.Image3D;

import java.util.ArrayList;

/**
 * <p>
 * Implements various flood-fill algorithms for 3D stacks.
 * Rewritten from ij.process.FloodFiller and adapted to 3D stacks. Also support
 * floating point stacks.
 * </p>
 * 
 * <p>
 * Check also "http://en.wikipedia.org/wiki/Flood_fill".
 * </p>
 * 
 * @see FloodFill
 * @see MinimaAndMaxima3D
 * @see inra.ijpb.morphology.extrema.RegionalExtrema3DByFlooding
 * @see inra.ijpb.binary.BinaryImages#componentsLabeling(ImageStack, int, int)
 * 
 * @author David Legland
 */
public class FloodFill3D
{
	/**
	 * Replaces all the pixels in the 6-neighborhood of (x0,y0,z0) that have the
	 * same values as the pixel in (x0,y0,z0) by the specified value. Should work
	 * for all integer based 3D images.
	 * 
	 * @param image
	 *            the 3D image in which floodfill will be propagated
	 * @param x0
	 *            the x-coordinate of the seed voxel
	 * @param y0
	 *            the y-coordinate of the seed voxel
	 * @param z0
	 *            the z-coordinate of the seed voxel
	 * @param value
	 *            the new value of the connected component at (x,y,z)
	 * @param conn
	 * 			  the connectivity to use, either 6 or 26
	 */
	public final static void floodFill(ImageStack image, int x0, int y0, int z0,
			int value, int conn)
	{
		if (conn == 6)
			floodFillC6(image, x0, y0, z0, value);
		else if (conn == 26)
			floodFillC26(image, x0, y0, z0, value);
		else
			throw new IllegalArgumentException("Connectivity must be either 6 or 26, not " + conn);
	}
	
	/**
	 * Replaces all the pixels in the 6-neighborhood of (x0,y0,z0) that have the
	 * same values as the pixel in (x0,y0,z0) by the specified value. Should work
	 * for all integer based 3D images.
	 * 
	 * @param image
	 *            the 3D image in which floodfill will be propagated
	 * @param x0
	 *            the x-coordinate of the seed voxel
	 * @param y0
	 *            the y-coordinate of the seed voxel
	 * @param z0
	 *            the z-coordinate of the seed voxel
	 * @param value
	 *            the new value of the connected component at (x,y,z)
	 */
	private final static void floodFillC6(ImageStack image, int x0, int y0, int z0,
			int value)
	{
		// get image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		// get old value
		int oldValue = (int) image.getVoxel(x0, y0, z0);
		
		// test if already the right value 
		if (oldValue == value) 
			return ;
		
		// initialize the stack with original pixel
		ArrayList<Cursor3D> stack = new ArrayList<Cursor3D>();
		stack.add(new Cursor3D(x0, y0, z0));
		
		// process all items in stack
		while (!stack.isEmpty()) 
		{
			// Extract current position
			Cursor3D p = stack.remove(stack.size() - 1);
			int px = p.x;
			int py = p.y;
			int pz = p.z;
			
			// process only pixel of the same value
			if (image.getVoxel(px, py, pz) != oldValue) 
				continue;
			
			// x extremities of scan-line
			int x1 = px; 
			int x2 = px;
			
			// find start of scan-line
			while (x1 > 0 && image.getVoxel(x1-1, py, pz) == oldValue)
				x1--;
			
			// find end of scan-line
			while (x2 < sizeX - 1 && image.getVoxel(x2+1, py, pz) == oldValue)
				x2++;
			
			// fill current scan-line
            for (int x = x1; x <= x2; x++)
            {
                image.setVoxel(x, py, pz, value);
            }
			
			// find scan-lines above the current one
			if (py > 0)
			{
			    boolean inScanLine = false;
				for (int i = x1; i <= x2; i++)
				{
					int val = (int) image.getVoxel(i, py - 1, pz);
					if (!inScanLine && val == oldValue)
					{
						stack.add(new Cursor3D(i, py - 1, pz));
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
				for (int i = x1; i <= x2; i++)
				{
					int val = (int) image.getVoxel(i, py + 1, pz);
					if (!inScanLine && val == oldValue)
					{
						stack.add(new Cursor3D(i, py + 1, pz));
						inScanLine = true;
					} 
					else if (inScanLine && val != oldValue)
					{
						inScanLine = false;
					}
				}
			}

			// find scan-lines in front of the current one
			if (pz > 0)
			{
			    boolean inScanLine = false;
				for (int i = x1; i <= x2; i++)
				{
					int val = (int) image.getVoxel(i, py, pz - 1);
					if (!inScanLine && val == oldValue)
					{
						stack.add(new Cursor3D(i, py, pz - 1));
						inScanLine = true;
					} 
					else if (inScanLine && val != oldValue)
					{
						inScanLine = false;
					}
				}
			}

			// find scan-lines behind the current one
			if (pz < sizeZ - 1)
			{
			    boolean inScanLine = false;
				for (int i = x1; i <= x2; i++)
				{
					int val = (int) image.getVoxel(i, py, pz + 1);
					if (!inScanLine && val == oldValue)
					{
						stack.add(new Cursor3D(i, py, pz + 1));
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
	 * Replaces all the pixels in the 26-neighborhood of (x0,y0,z0) that have the
	 * same values as the pixel in (x0,y0,z0) by the specified value. Should work
	 * for all integer based 3D images.
	 * 
	 * @param image
	 *            the 3D image in which floodfill will be propagated
	 * @param x0
	 *            the x-coordinate of the seed voxel
	 * @param y0
	 *            the y-coordinate of the seed voxel
	 * @param z0
	 *            the z-coordinate of the seed voxel
	 * @param value
	 *            the new value of the connected component at (x,y,z)
	 */
	private final static void floodFillC26(ImageStack image, int x0, int y0,
			int z0, int value)
	{
		// get image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		// get old value
		int oldValue = (int) image.getVoxel(x0, y0, z0);
		
		// test if already the right value 
		if (oldValue == value) 
			return ;
		
		// initialize the stack with original pixel
		ArrayList<Cursor3D> stack = new ArrayList<Cursor3D>();
		stack.add(new Cursor3D(x0, y0, z0));
		
		// process all items in stack
		while (!stack.isEmpty())
		{
			// Extract current position
			Cursor3D p = stack.remove(stack.size()-1);
			int px = p.x;
			int py = p.y;
			int pz = p.z;
			
			// process only pixel with the same value
			if (image.getVoxel(px, py, pz) != oldValue) 
				continue;
			
			// x extremities of scan-line
			int x1 = px; 
			int x2 = px;
			
			// find start of scan-line
			while (x1 > 0 && image.getVoxel(x1-1, py, pz) == oldValue)
				x1--;
			
			// find end of scan-line
			while (x2 < sizeX - 1 && image.getVoxel(x2+1, py, pz) == oldValue)
				x2++;
			
			// fill current scan-line
            for (int x = x1; x <= x2; x++)
            {
                image.setVoxel(x, py, pz, value);
            }
			
			// check the eight X-lines around the current one
			for (int z2 = max(pz - 1, 0); z2 <= min(pz + 1, sizeZ - 1); z2++)
			{
				for (int y2 = max(py - 1, 0); y2 <= min(py + 1, sizeY - 1); y2++)
				{
					// do not process the middle line
					if (y2 == pz && y2 == py)
						continue;

					boolean inScanLine = false;
					for (int i = max(x1 - 1, 0); i <= min(x2 + 1, sizeX - 1); i++)
					{
						int val = (int) image.getVoxel(i, y2, z2);
						if (!inScanLine && val == oldValue)
						{
							stack.add(new Cursor3D(i, y2, z2));
							inScanLine = true;
						} 
						else if (inScanLine && val != oldValue)
						{
							inScanLine = false;
						}
					}
				}
			} // end of iteration on neighbor lines

		} // end of iteration on position stack

	}

	/**
	 * Replaces all the pixels in the 6-neighborhood of (x0,y0,z0) that have the
	 * same values as the pixel in (x0,y0,z0) by the specified floating point
	 * value. Should work the same way for all type of images.
	 * 
	 * @param image
	 *            the 3D image in which floodfill will be propagated
	 * @param x0
	 *            the x-coordinate of the seed voxel
	 * @param y0
	 *            the y-coordinate of the seed voxel
	 * @param z0
	 *            the z-coordinate of the seed voxel
	 * @param value
	 *            the new value of the connected component at (x,y,z)
	 * @param conn
	 * 			  the connectivity to use, either 6 or 26
	 */
	public final static void floodFill(ImageStack image, int x0, int y0, int z0,
			double value, int conn)
	{
		if (conn == 6)
			floodFillC6(image, x0, y0, z0, value);
		else if (conn == 26)
			floodFillC26(image, x0, y0, z0, value);
		else
			throw new IllegalArgumentException("Connectivity must be either 6 or 26, not " + conn);
	}

	/**
	 * Replaces all the pixels in the 6-neighborhood of (x0,y0,z0) that have the
	 * same values as the pixel in (x0,y0,z0) by the specified floating point
	 * value. Should work the same way for all type of images.
	 * 
	 * @param image
	 *            the 3D image in which floodfill will be propagated
	 * @param x0
	 *            the x-coordinate of the seed voxel
	 * @param y0
	 *            the y-coordinate of the seed voxel
	 * @param z0
	 *            the z-coordinate of the seed voxel
	 * @param value
	 *            the new value of the connected component at (x0,y0,z0)
	 */
	private final static void floodFillC6(ImageStack image, int x0, int y0, int z0,
			double value)
	{
		// get image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		// get old value
		double oldValue = image.getVoxel(x0, y0, z0);
		
		// test if already the right value 
		if (oldValue == value) 
			return ;
		
		// initialize the stack with original pixel
		ArrayList<Cursor3D> stack = new ArrayList<Cursor3D>();
		stack.add(new Cursor3D(x0, y0, z0));
		
		// process all items in stack
		while (!stack.isEmpty()) 
		{
			// Extract current position
			Cursor3D p = stack.remove(stack.size()-1);
			int px = p.x;
			int py = p.y;
			int pz = p.z;
			
			// process only pixel of the same value
			if (image.getVoxel(px, py, pz) != oldValue) 
				continue;
			
			// x extremities of scan-line
			int x1 = px; 
			int x2 = px;
			
			// find start of scan-line
			while (x1 > 0 && image.getVoxel(x1-1, py, pz) == oldValue)
				x1--;
			
			// find end of scan-line
			while (x2 < sizeX - 1 && image.getVoxel(x2+1, py, pz) == oldValue)
				x2++;
			
			// fill current scan-line
            for (int x = x1; x <= x2; x++)
            {
                image.setVoxel(x, py, pz, value);
            }
			
			// find scan-lines above the current one
			if (py > 0) 
			{
			    boolean inScanLine = false;
				for (int i = x1; i <= x2; i++)
				{
					double val = image.getVoxel(i, py - 1, pz);
					if (!inScanLine && val == oldValue)
					{
						stack.add(new Cursor3D(i, py - 1, pz));
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
				for (int i = x1; i <= x2; i++)
				{
					double val = image.getVoxel(i, py + 1, pz);
					if (!inScanLine && val == oldValue)
					{
						stack.add(new Cursor3D(i, py + 1, pz));
						inScanLine = true;
					}
					else if (inScanLine && val != oldValue)
					{
						inScanLine = false;
					}
				}
			}

			// find scan-lines in front of the current one
			if (pz > 0)
			{
			    boolean inScanLine = false;
				for (int i = x1; i <= x2; i++)
				{
					double val = image.getVoxel(i, py, pz - 1);
					if (!inScanLine && val == oldValue)
					{
						stack.add(new Cursor3D(i, py, pz - 1));
						inScanLine = true;
					}
					else if (inScanLine && val != oldValue)
					{
						inScanLine = false;
					}
				}
			}

			// find scan-lines behind the current one
			if (pz < sizeZ - 1)
			{
			    boolean inScanLine = false;
				for (int i = x1; i <= x2; i++)
				{
					double val = image.getVoxel(i, py, pz + 1);
					if (!inScanLine && val == oldValue)
					{
						stack.add(new Cursor3D(i, py, pz + 1));
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
	 * Replaces all the pixels in the 26-neighborhood of (x0,y0,z0) that have the
	 * same values as the pixel in (x0,y0,z0) by the specified value. This version
	 * is dedicated to floating-point stacks.
	 * 
	 * @param image
	 *            the 3D image in which floodfill will be propagated
	 * @param x0
	 *            the x-coordinate of the seed voxel
	 * @param y0
	 *            the y-coordinate of the seed voxel
	 * @param z0
	 *            the z-coordinate of the seed voxel
	 * @param value
	 *            the new value of the connected component at (x0,y0,z0)
	 */
	private final static void floodFillC26(ImageStack image, int x0, int y0,
			int z0, double value)
	{
		// get image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		// get old value
		double oldValue = image.getVoxel(x0, y0, z0);
		
		// test if already the right value 
		if (oldValue == value) 
			return ;
		
		// initialize the stack with original pixel
		ArrayList<Cursor3D> stack = new ArrayList<Cursor3D>();
		stack.add(new Cursor3D(x0, y0, z0));
		
		// process all items in stack
		while (!stack.isEmpty())
		{
			// Extract current position
			Cursor3D p = stack.remove(stack.size()-1);
			int px = p.x;
			int py = p.y;
			int pz = p.z;
			
			// process only pixel with the same value
			if (image.getVoxel(px, py, pz) != oldValue) 
				continue;
			
			// x extremities of scan-line
			int x1 = px; 
			int x2 = px;
			
			// find start of scan-line
			while (x1 > 0 && image.getVoxel(x1-1, py, pz) == oldValue)
				x1--;
			
			// find end of scan-line
			while (x2 < sizeX - 1 && image.getVoxel(x2+1, py, pz) == oldValue)
				x2++;
			
			// fill current scan-line
			for (int x = x1; x <= x2; x++)
            {
                image.setVoxel(x, py, pz, value);
            }
					
			// check the eight x-lines around the current one
			for (int z2 = max(pz - 1, 0); z2 <= min(pz + 1, sizeZ - 1); z2++)
			{
				for (int y2 = max(py - 1, 0); y2 <= min(py + 1, sizeY - 1); y2++)
				{
					// do not process the middle line
					if (y2 == pz && y2 == py)
						continue;

					boolean inScanLine = false;
					for (int i = max(x1 - 1, 0); i <= min(x2 + 1, sizeX - 1); i++)
					{
						double val = image.getVoxel(i, y2, z2);
						if (!inScanLine && val == oldValue)
						{
							stack.add(new Cursor3D(i, y2, z2));
							inScanLine = true;
						} 
						else if (inScanLine && val != oldValue)
						{
							inScanLine = false;
						}
					}
					
				}
			} // end of iteration on neighbor lines
		} // end of iteration on position stack

	}

	/**
     * Assign to all the neighbor voxels of (x0,y0,z0) that have the same voxel
     * value in <code>inputImage</code>, the specified new value (
     * <code>value</code>) in <code>outputImage</code>, using the specified
     * connectivity.
     * 
     * @param inputImage
     *            the original image to read the voxel values from (should be
     *            integer based)
     * @param x0
     *            the x-coordinate of the seed voxel
     * @param y0
     *            the y-coordinate of the seed voxel
     * @param z0
     *            the z-coordinate of the seed voxel
     * @param outputImage
     *            the output image to fill (should be integer based)
     * @param value
     *            the fill value
     * @param conn
     *            the connectivity to use (6 or 26)
     */
	public final static void floodFill(ImageStack inputImage, int x0, int y0,
			int z0, ImageStack outputImage, int value, int conn)
	{
		switch (conn)
		{
		case 6:
			floodFillC6(inputImage, x0, y0, z0, outputImage, value);
			return;
		case 26:
			floodFillC26(inputImage, x0, y0, z0, outputImage, value);
			return;
		default:
			throw new IllegalArgumentException(
					"Connectivity must be either 6 or 26, not " + conn);
		}
		
	}

	/**
	 * Assign to all the neighbor voxels of (x0,y0,z0) that have the same voxel
	 * value in <code>inputImage</code>, the specified new value (
	 * <code>value</code>) in <code>outputImage</code>, using the 6-connectivity.
	 * 
	 * @param inputImage
	 *            the original image to read the voxel values from
	 * @param x0
	 *            the x-coordinate of the seed voxel
	 * @param y0
	 *            the y-coordinate of the seed voxel
	 * @param z0
	 *            the z-coordinate of the seed voxel
	 * @param outputImage
	 *            the output image (to fill)
	 * @param value
	 *            the fill value
	 */
	private final static void floodFillC6(ImageStack inputImage, int x0,
			int y0, int z0, ImageStack outputImage, int value)
	{
		// get image size
		int sizeX = inputImage.getWidth();
		int sizeY = inputImage.getHeight();
		int sizeZ = inputImage.getSize();

		// get old value
		int oldValue = (int) inputImage.getVoxel(x0, y0, z0);

		// initialize the stack with original pixel
		ArrayList<Cursor3D> stack = new ArrayList<Cursor3D>();
		stack.add(new Cursor3D(x0, y0, z0));

		// process all items in stack
		while (!stack.isEmpty()) 
		{
			// Extract current position
			Cursor3D p = stack.remove(stack.size()-1);
			int px = p.x;
			int py = p.y;
			int pz = p.z;

			// process only pixel of the same value
			if ((int) inputImage.getVoxel(px, py, pz) != oldValue)
				continue;

			// x extremities of scan-line
			int x1 = px; 
			int x2 = px;

			// find start of scan-line
			while (x1 > 0 && (int) inputImage.getVoxel(x1 - 1, py, pz) == oldValue)
				x1--;

			// find end of scan-line
			while (x2 < sizeX - 1 && (int) inputImage.getVoxel(x2 + 1, py, pz) == oldValue)
				x2++;

			// fill current scan-line
            for (int x = x1; x <= x2; x++)
            {
                outputImage.setVoxel(x, py, pz, value);
            }

			// search bounds on x axis for neighbor lines
			int x1l = max(x1, 0);
			int x2l = min(x2, sizeX - 1);

			// find scan-lines above the current one
			if (py > 0)
			{
			    boolean inScanLine = false;
				for (int i = x1l; i <= x2l; i++)
				{
					int val = (int) inputImage.getVoxel(i, py - 1, pz);
					int lab = (int) outputImage.getVoxel(i, py - 1, pz);

					if (!inScanLine && val == oldValue && lab != value)
					{
						stack.add(new Cursor3D(i, py - 1, pz));
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
				for (int i = x1l; i <= x2l; i++)
				{
					int val = (int) inputImage.getVoxel(i, py + 1, pz);
					int lab = (int) outputImage.getVoxel(i, py + 1, pz);

					if (!inScanLine && val == oldValue && lab != value)
					{
						stack.add(new Cursor3D(i, py + 1, pz));
						inScanLine = true;
					} 
					else if (inScanLine && val != oldValue)
					{
						inScanLine = false;
					}
				}
			}

			// find scan-lines in front of the current one
			if (pz > 0)
			{
			    boolean inScanLine = false;
				for (int i = x1l; i <= x2l; i++)
				{
					int val = (int) inputImage.getVoxel(i, py, pz - 1);
					int lab = (int) outputImage.getVoxel(i, py, pz - 1);

					if (!inScanLine && val == oldValue && lab != value)
					{
						stack.add(new Cursor3D(i, py, pz - 1));
						inScanLine = true;
					}
					else if (inScanLine && val != oldValue)
					{
						inScanLine = false;
					}
				}
			}

			// find scan-lines behind the current one
			if (pz < sizeZ - 1)
			{
			    boolean inScanLine = false;
				for (int i = x1l; i <= x2l; i++)
				{
					int val = (int) inputImage.getVoxel(i, py, pz + 1);
					int lab = (int) outputImage.getVoxel(i, py, pz + 1);

					if (!inScanLine && val == oldValue && lab != value)
					{
						stack.add(new Cursor3D(i, py, pz + 1));
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
	 * Assign to all the neighbor voxels of (x0,y0,z0) that have the same voxel
	 * value in <code>inputImage</code>, the specified new value (
	 * <code>value</code>) in <code>outputImage</code>, using the
	 * 26-connectivity.
	 * 
	 * @param inputImage
	 *            original image to read the voxel values from
	 * @param x0
	 *            the x-coordinate of the seed voxel
	 * @param y0
	 *            the y-coordinate of the seed voxel
	 * @param z0
	 *            the z-coordinate of the seed voxel
	 * @param outputImage
	 *            the output label image (to fill)
	 * @param value
	 *            the fill value
	 */
	private final static void floodFillC26(ImageStack inputImage, int x0,
			int y0, int z0, ImageStack outputImage, int value)
	{
		// get image size
		int sizeX = inputImage.getWidth();
		int sizeY = inputImage.getHeight();
		int sizeZ = inputImage.getSize();
		
		// get old value
		int oldValue = (int) inputImage.getVoxel(x0, y0, z0);
				
		// initialize the stack with original pixel
		ArrayList<Cursor3D> stack = new ArrayList<Cursor3D>();
		stack.add(new Cursor3D(x0, y0, z0));
		
		// process all items in stack
		while (!stack.isEmpty()) 
		{
			// Extract current position
			Cursor3D p = stack.remove(stack.size()-1);
			int px = p.x;
			int py = p.y;
			int pz = p.z;
			
			// process only pixel of the same value
			if ((int) inputImage.getVoxel(px, py, pz) != oldValue)
				continue;
			
			// x extremities of scan-line
			int x1 = px; 
			int x2 = px;
			
			// find start of scan-line
			while (x1 > 0 && (int) inputImage.getVoxel(x1-1, py, pz) == oldValue)
				x1--;
			
			// find end of scan-line
			while (x2 < sizeX - 1 && (int) inputImage.getVoxel(x2+1, py, pz) == oldValue)
				x2++;
		
			// fill current scan-line
			for (int x = x1; x <= x2; x++)
			{
			    outputImage.setVoxel(x, py, pz, value);
			}
		     
			// search bounds on x axis for neighbor lines
			int x1l = max(x1 - 1, 0);
			int x2l = min(x2 + 1, sizeX - 1);

			// check the eight X-lines around the current one
			for (int z2 = max(pz - 1, 0); z2 <= min(pz + 1, sizeZ - 1); z2++)
			{
				for (int y2 = max(py - 1, 0); y2 <= min(py + 1, sizeY - 1); y2++)
				{
					// do not process the middle line
					if (z2 == pz && y2 == py)
						continue;
					
					boolean inScanLine = false;
					for (int i = x1l; i <= x2l; i++)
					{
						int val = (int) inputImage.getVoxel(i, y2, z2);
						int lab = (int) outputImage.getVoxel(i, y2, z2);
						
						if (!inScanLine && val == oldValue && lab != value)
						{
							stack.add(new Cursor3D(i, y2, z2));
							inScanLine = true;
						} 
						else if (inScanLine && val != oldValue)
						{
							inScanLine = false;
						}	
					}
				}
			} // end of iteration on neighbor lines

		}
	}

	/**
     * Assigns to all the neighbor voxels of (x0,y0,z0) that have the same voxel
     * value in <code>inputImage</code>, the specified new value
     * (<code>value</code>) in <code>outputImage</code>, using the specified
     * connectivity.
     * 
     * @param inputImage
     *            the original image to read the voxel values from
     * @param x0
     *            the x-coordinate of the seed voxel
     * @param y0
     *            the y-coordinate of the seed voxel
     * @param z0
     *            the z-coordinate of the seed voxel
     * @param outputImage
     *            the output image (to fill)
     * @param value
     *            the fill value
     * @param conn
     *            the connectivity to use (6 or 26)
     */
	public final static void floodFillFloat(ImageStack inputImage, int x0,
			int y0, int z0, ImageStack outputImage, float value, int conn)
	{
		switch (conn)
		{
		case 6:
			floodFillFloatC6(inputImage, x0, y0, z0, outputImage, value);
			return;
		case 26:
			floodFillFloatC26(inputImage, x0, y0, z0, outputImage, value);
			return;
		default:
			throw new IllegalArgumentException(
					"Connectivity must be either 6 or 26, not " + conn);
		}
	}
	
	/**
     * Assigns to all the neighbor voxels of (x0,y0,z0) that have the same voxel
     * value in <code>inputImage</code>, the specified new value (
     * <code>value</code>) in <code>outputImage</code>, using the 6-connectivity.
     * 
     * @param inputImage
     *            the original image to read the voxel values from
     * @param x0
     *            the x-coordinate of the seed voxel
     * @param y0
     *            the y-coordinate of the seed voxel
     * @param z0
     *            the z-coordinate of the seed voxel
     * @param outputImage
     *            the output image (to fill)
     * @param value
     *            the fill value
     */
    private final static void floodFillFloatC6(ImageStack inputImage, int x0,
    		int y0, int z0, ImageStack outputImage, float value)
    {
    	// get image size
    	int sizeX = inputImage.getWidth();
    	int sizeY = inputImage.getHeight();
    	int sizeZ = inputImage.getSize();
    
    	// get old value
    	double oldValue = inputImage.getVoxel(x0, y0, z0);
    
    	// initialize the stack with original pixel
    	ArrayList<Cursor3D> stack = new ArrayList<Cursor3D>();
    	stack.add(new Cursor3D(x0, y0, z0));
    
    	boolean inScanLine;
    
    	// process all items in stack
    	while (!stack.isEmpty()) 
    	{
    		// Extract current position
    		Cursor3D p = stack.remove(stack.size()-1);
    		int px = p.x;
    		int py = p.y;
    		int pz = p.z;
    
    		// process only pixel of the same value
    		if (inputImage.getVoxel(px, py, pz) != oldValue)
    			continue;
    
    		// x extremities of scan-line
    		int x1 = px; 
    		int x2 = px;
    
    		// find start of scan-line
    		while (x1 > 0 && inputImage.getVoxel(x1 - 1, py, pz) == oldValue)
    			x1--;
    
    		// find end of scan-line
    		while (x2 < sizeX - 1 && inputImage.getVoxel(x2 + 1, py, pz) == oldValue)
    			x2++;
    
    		// fill current scan-line
            for (int x = x1; x <= x2; x++)
            {
                outputImage.setVoxel(x, py, pz, value);
            }
    
    		// search bounds on x axis for neighbor lines
    		int x1l = max(x1, 0);
    		int x2l = min(x2, sizeX - 1);
    
    		// find scan-lines above the current one
    		if (py > 0) 
    		{
    			inScanLine = false;
    			for (int i = x1l; i <= x2l; i++)
    			{
    				double val = inputImage.getVoxel(i, py - 1, pz);
    				double lab = outputImage.getVoxel(i, py - 1, pz);
    
    				if (!inScanLine && val == oldValue && lab != value) 
    				{
    					stack.add(new Cursor3D(i, py - 1, pz));
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
    			inScanLine = false;
    			for (int i = x1l; i <= x2l; i++) 
    			{
    				double val = inputImage.getVoxel(i, py + 1, pz);
    				double lab = outputImage.getVoxel(i, py + 1, pz);
    
    				if (!inScanLine && val == oldValue && lab != value) 
    				{
    					stack.add(new Cursor3D(i, py + 1, pz));
    					inScanLine = true;
    				}
    				else if (inScanLine && val != oldValue)
    				{
    					inScanLine = false;
    				}
    			}
    		}
    
    		// find scan-lines in front of the current one
    		if (pz > 0)
    		{
    			inScanLine = false;
    			for (int i = x1l; i <= x2l; i++) 
    			{
    				double val = inputImage.getVoxel(i, py, pz - 1);
    				double lab = outputImage.getVoxel(i, py, pz - 1);
    
    				if (!inScanLine && val == oldValue && lab != value) 
    				{
    					stack.add(new Cursor3D(i, py, pz - 1));
    					inScanLine = true;
    				}
    				else if (inScanLine && val != oldValue)
    				{
    					inScanLine = false;
    				}
    			}
    		}
    
    		// find scan-lines behind the current one
    		if (pz < sizeZ - 1)
    		{
    			inScanLine = false;
    			for (int i = x1l; i <= x2l; i++)
    			{
    				double val = inputImage.getVoxel(i, py, pz + 1);
    				double lab = outputImage.getVoxel(i, py, pz + 1);
    
    				if (!inScanLine && val == oldValue && lab != value)
    				{
    					stack.add(new Cursor3D(i, py, pz + 1));
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
     * Assigns to all the neighbor voxels of (x0,y0,z0) that have the same voxel
     * value in <code>inputImage</code>, the specified new value (
     * <code>value</code>) in <code>outputImage</code>, using the
     * 26-connectivity.
     * 
     * @param inputImage
     *            the original image to read the voxel values from
     * @param x0
     *            the x-coordinate of the seed voxel
     * @param y0
     *            the y-coordinate of the seed voxel
     * @param z0
     *            the z-coordinate of the seed voxel
     * @param outputImage
     *            the output label image (to fill)
     * @param value
     *            the fill value
     */
    private final static void floodFillFloatC26(ImageStack inputImage, int x0,
    		int y0, int z0, ImageStack outputImage, float value)
    {
    	// get image size
    	int sizeX = inputImage.getWidth();
    	int sizeY = inputImage.getHeight();
    	int sizeZ = inputImage.getSize();
    	
    	// get old value
    	double oldValue = inputImage.getVoxel(x0, y0, z0);
    			
    	// initialize the stack with original pixel
    	ArrayList<Cursor3D> stack = new ArrayList<Cursor3D>();
    	stack.add(new Cursor3D(x0, y0, z0));
    	
    	boolean inScanLine;
    	
    	// process all items in stack
    	while (!stack.isEmpty())
    	{
    		// Extract current position
    		Cursor3D p = stack.remove(stack.size()-1);
    		int px = p.x;
    		int py = p.y;
    		int pz = p.z;
    		
    		// process only pixel of the same value
    		if (inputImage.getVoxel(px, py, pz) != oldValue)
    			continue;
    		
    		// x extremities of scan-line
    		int x1 = px; 
    		int x2 = px;
    		
    		// find start of scan-line
    		while (x1 > 0 && inputImage.getVoxel(x1-1, py, pz) == oldValue)
    			x1--;
    		
    		// find end of scan-line
    		while (x2 < sizeX - 1 && inputImage.getVoxel(x2+1, py, pz) == oldValue)
    			x2++;
    	
    		// fill current scan-line
            for (int x = x1; x <= x2; x++)
            {
                outputImage.setVoxel(x, py, pz, value);
            }
    		
    		// search bounds on x axis for neighbor lines
    		int x1l = max(x1 - 1, 0);
    		int x2l = min(x2 + 1, sizeX - 1);
    
    		// check the eight X-lines around the current one
    		for (int z2 = max(pz - 1, 0); z2 <= min(pz + 1, sizeZ - 1); z2++) 
    		{
    			for (int y2 = max(py - 1, 0); y2 <= min(py + 1, sizeY - 1); y2++) 
    			{
    				// do not process the middle line
    				if (z2 == pz && y2 == py)
    					continue;
    				
    				inScanLine = false;
    				for (int i = x1l; i <= x2l; i++) 
    				{
    					double val = inputImage.getVoxel(i, y2, z2);
    					double lab = outputImage.getVoxel(i, y2, z2);
    					
    					if (!inScanLine && val == oldValue && lab != value) 
    					{
    						stack.add(new Cursor3D(i, y2, z2));
    						inScanLine = true;
    					}
    					else if (inScanLine && val != oldValue)
    					{
    						inScanLine = false;
    					}
    				}
    				
    			}
    		} // end of iteration on neighbor lines
    	}
    }

    /**
	 * Assigns to all the neighbor voxels of (x0,y0,z0) that have the same voxel
	 * value in <code>inputImage</code>, the specified new value (
	 * <code>value</code>) in <code>outputImage</code>, using the specified
	 * connectivity.
	 * 
	 * @param inputImage
	 *            the original image to read the voxel values from
	 * @param x0
	 *            the x-coordinate of the seed voxel
	 * @param y0
	 *            the y-coordinate of the seed voxel
	 * @param z0
	 *            the z-coordinate of the seed voxel
	 * @param outputImage
	 *            the output image (to fill)
	 * @param value
	 *            the fill value
	 * @param conn
	 *            the connectivity to use (6 or 26)
	 */
	public final static void floodFillFloat(Image3D inputImage, int x0,
			int y0, int z0, Image3D outputImage, float value, int conn)
	{
		switch (conn)
		{
		case 6:
			floodFillFloatC6(inputImage, x0, y0, z0, outputImage, value);
			return;
		case 26:
			floodFillFloatC26(inputImage, x0, y0, z0, outputImage, value);
			return;
		default:
			throw new IllegalArgumentException(
					"Connectivity must be either 6 or 26, not " + conn);
		}
	}
	
	/**
	 * Assigns to all the neighbor voxels of (x0,y0,z0) that have the same voxel
	 * value in <code>inputImage</code>, the specified new value (
	 * <code>value</code>) in <code>outputImage</code>, using the 6-connectivity.
	 * 
	 * @param inputImage
	 *            the original image to read the voxel values from
	 * @param x0
	 *            the x-coordinate of the seed voxel
	 * @param y0
	 *            the y-coordinate of the seed voxel
	 * @param z0
	 *            the z-coordinate of the seed voxel
	 * @param outputImage
	 *            the output image (to fill)
	 * @param value
	 *            the fill value
	 */
	private final static void floodFillFloatC6(Image3D inputImage, int x0,
			int y0, int z0, Image3D outputImage, float value)
	{
		// get image size
		int sizeX = inputImage.getSize(0);
		int sizeY = inputImage.getSize(1);
		int sizeZ = inputImage.getSize(2);

		// get old value
		double oldValue = inputImage.getValue(x0, y0, z0);

		// initialize the stack with original pixel
		ArrayList<Cursor3D> stack = new ArrayList<Cursor3D>();
		stack.add(new Cursor3D(x0, y0, z0));

		boolean inScanLine;

		// process all items in stack
		while (!stack.isEmpty()) 
		{
			// Extract current position
			Cursor3D p = stack.remove(stack.size()-1);
			int px = p.x;
			int py = p.y;
			int pz = p.z;

			// process only pixel of the same value
			if (inputImage.getValue(px, py, pz) != oldValue)
				continue;

			// x extremities of scan-line
			int x1 = px; 
			int x2 = px;

			// find start of scan-line
			while (x1 > 0 && inputImage.getValue(x1 - 1, py, pz) == oldValue)
				x1--;

			// find end of scan-line
			while (x2 < sizeX - 1 && inputImage.getValue(x2 + 1, py, pz) == oldValue)
				x2++;

			// fill current scan-line
            for (int x = x1; x <= x2; x++)
            {
                outputImage.setValue(x, py, pz, value);
            }

			// search bounds on x axis for neighbor lines
			int x1l = max(x1, 0);
			int x2l = min(x2, sizeX - 1);

			// find scan-lines above the current one
			if (py > 0) 
			{
				inScanLine = false;
				for (int i = x1l; i <= x2l; i++)
				{
					double val = inputImage.getValue(i, py - 1, pz);
					double lab = outputImage.getValue(i, py - 1, pz);

					if (!inScanLine && val == oldValue && lab != value) 
					{
						stack.add(new Cursor3D(i, py - 1, pz));
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
				inScanLine = false;
				for (int i = x1l; i <= x2l; i++) 
				{
					double val = inputImage.getValue(i, py + 1, pz);
					double lab = outputImage.getValue(i, py + 1, pz);

					if (!inScanLine && val == oldValue && lab != value) 
					{
						stack.add(new Cursor3D(i, py + 1, pz));
						inScanLine = true;
					}
					else if (inScanLine && val != oldValue)
					{
						inScanLine = false;
					}
				}
			}

			// find scan-lines in front of the current one
			if (pz > 0)
			{
				inScanLine = false;
				for (int i = x1l; i <= x2l; i++) 
				{
					double val = inputImage.getValue(i, py, pz - 1);
					double lab = outputImage.getValue(i, py, pz - 1);

					if (!inScanLine && val == oldValue && lab != value) 
					{
						stack.add(new Cursor3D(i, py, pz - 1));
						inScanLine = true;
					}
					else if (inScanLine && val != oldValue)
					{
						inScanLine = false;
					}
				}
			}

			// find scan-lines behind the current one
			if (pz < sizeZ - 1)
			{
				inScanLine = false;
				for (int i = x1l; i <= x2l; i++)
				{
					double val = inputImage.getValue(i, py, pz + 1);
					double lab = outputImage.getValue(i, py, pz + 1);

					if (!inScanLine && val == oldValue && lab != value)
					{
						stack.add(new Cursor3D(i, py, pz + 1));
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
	 * Assigns to all the neighbor voxels of (x0,y0,z0) that have the same voxel
	 * value in <code>inputImage</code>, the specified new value (
	 * <code>value</code>) in <code>outputImage</code>, using the
	 * 26-connectivity.
	 * 
	 * @param inputImage
	 *            the original image to read the voxel values from
	 * @param x0
	 *            the x-coordinate of the seed voxel
	 * @param y0
	 *            the y-coordinate of the seed voxel
	 * @param z0
	 *            the z-coordinate of the seed voxel
	 * @param outputImage
	 *            the output image (to fill)
	 * @param value
	 *            the fill value
	 */
	private final static void floodFillFloatC26(Image3D inputImage, int x0,
			int y0, int z0, Image3D outputImage, float value)
	{
		// get image size
		int sizeX = inputImage.getSize(0);
		int sizeY = inputImage.getSize(1);
		int sizeZ = inputImage.getSize(2);
		
		// get old value
		double oldValue = inputImage.getValue(x0, y0, z0);
				
		// initialize the stack with original pixel
		ArrayList<Cursor3D> stack = new ArrayList<Cursor3D>();
		stack.add(new Cursor3D(x0, y0, z0));
		
		boolean inScanLine;
		
		// process all items in stack
		while (!stack.isEmpty())
		{
			// Extract current position
			Cursor3D p = stack.remove(stack.size()-1);
			int px = p.x;
			int py = p.y;
			int pz = p.z;
			
			// process only pixel of the same value
			if (inputImage.getValue(px, py, pz) != oldValue)
				continue;
			
			// x extremities of scan-line
			int x1 = px; 
			int x2 = px;
			
			// find start of scan-line
			while (x1 > 0 && inputImage.getValue(x1-1, py, pz) == oldValue)
				x1--;
			
			// find end of scan-line
			while (x2 < sizeX - 1 && inputImage.getValue(x2+1, py, pz) == oldValue)
				x2++;
		
			// fill current scan-line
			for (int x = x1; x <= x2; x++)
			{
			    outputImage.setValue(x, py, pz, value);
			}
			
			// search bounds on x axis for neighbor lines
			int x1l = max(x1 - 1, 0);
			int x2l = min(x2 + 1, sizeX - 1);

			// check the eight X-lines around the current one
			for (int z2 = max(pz - 1, 0); z2 <= min(pz + 1, sizeZ - 1); z2++) 
			{
				for (int y2 = max(py - 1, 0); y2 <= min(py + 1, sizeY - 1); y2++) 
				{
					// do not process the middle line
					if (z2 == pz && y2 == py)
						continue;
					
					inScanLine = false;
					for (int i = x1l; i <= x2l; i++) 
					{
						double val = inputImage.getValue(i, y2, z2);
						double lab = outputImage.getValue(i, y2, z2);
						
						if (!inScanLine && val == oldValue && lab != value) 
						{
							stack.add(new Cursor3D(i, y2, z2));
							inScanLine = true;
						}
						else if (inScanLine && val != oldValue)
						{
							inScanLine = false;
						}
					}
					
				}
			} // end of iteration on neighbor lines
		}
	}
	
	/**
     * Private constructor to prevent class instantiation.
     */
    private FloodFill3D()
    {
    }

    /**
	 * Defines a position within a 3D stack.
	 * Needs to be a static class to be called by static methods.
	 */
	private static class Cursor3D 
	{
		int x;
		int y;
		int z;
		
		public Cursor3D(int x, int y, int z) 
		{
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
}
