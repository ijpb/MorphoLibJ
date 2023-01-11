/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2023 INRA.
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
package inra.ijpb.morphology.strel;

import ij.plugin.filter.RankFilters;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * Disk structuring element. This class is a wrapper for the ImageJ native
 * RankFilters() method, that uses disk neighborhood.
 * 
 * @author David Legland
 *
 */
public class DiskStrel extends AbstractInPlaceStrel implements InPlaceStrel
{
	// ===================================================================
	// Class variables
	
	/**
	 * The radius of the disk structuring element, in pixels.
	 */
	double radius;
	
	
	// ===================================================================
	// Constructors
	
	/**
	 * Creates a structuring element with a circular shape of the given radius.
	 * 
	 * @param radius
	 *            the radius of the structuring element, in pixels
	 * @return a new structuring element with disk shape and specified radius
	 */
	public final static DiskStrel fromRadius(int radius)
	{
		return new DiskStrel(radius);
	}
	
	/**
	 * Creates a structuring element with a circular shape of the given
	 * diameter. The diameter is converted to a radius with following relation:
	 * <pre><code>
	 * radius = (diameter - 1) / 2
	 * </code></pre>
	 * 
	 * @param diam
	 *            the diameter of the structuring element, in pixels
	 * @return a new structuring element with disk shape and specified diameter
	 */
	public final static DiskStrel fromDiameter(int diam)
	{
		double radius = ((double) diam - 1.0) / 2;
		return new DiskStrel(radius);
	}
	
	/**
	 * Private constructor of Disk structuring element.
	 * 
	 * @param radius
	 *            the radius of the structuring element, in pixels
	 */
	private DiskStrel(double radius)
	{
		this.radius = radius;
	}
	
	
	// ===================================================================
	// Implementation of Strel interface 
	
	/* (non-Javadoc)
	 * @see ijt.filter.morphology.Strel#getSize()
	 */
	@Override
	public int[] getSize()
	{
		int radiusInt = (int) Math.round(radius);
		int diam = 2 * radiusInt + 1;
		return new int[]{diam, diam};
	}

	/* (non-Javadoc)
	 * @see ijt.filter.morphology.Strel#getMask()
	 */
	@Override
	public int[][] getMask() 
	{
		// Create an empty image with just a white pixel in the middle
		int intRadius = (int) Math.round(radius);
		int size = 2 * intRadius + 1;
		ImageProcessor img = new ByteProcessor(size, size);
		img.set(intRadius, intRadius, 255);
		
		// apply dilation
		this.inPlaceDilation(img);
		
		// convert to int array
		int[][] mask = new int[size][size];
		for (int y = 0; y < size; y++) 
		{
			for (int x = 0; x < size; x++)
			{
				mask[y][x] = img.get(x, y);
			}
		}

		return mask;
	}

	/* (non-Javadoc)
	 * @see ijt.filter.morphology.Strel#getOffset()
	 */
	@Override
	public int[] getOffset()
	{
		int intRadius = (int) Math.round(radius);
		return new int[]{intRadius, intRadius};
	}

	/* (non-Javadoc)
	 * @see ijt.filter.morphology.Strel#getShifts()
	 */
	@Override
	public int[][] getShifts()
	{
		int intRadius = (int) Math.round(radius);
		int[][] mask = getMask();
		int size = 2 * intRadius + 1;
		
		int n = 0;
		for (int y = 0; y < size; y++)
		{
			for (int x = 0; x < size; x++)
			{
				if (mask[y][x] > 0)
					n++;
			}
		}
		
		int[][] offsets = new int[n][2];
		int i = 0;
		for (int y = 0; y < size; y++)
		{
			for (int x = 0; x < size; x++)
			{
				if (mask[y][x] > 0)
				{
					offsets[i][0] = x;
					offsets[i][1] = y;
					i++;
				}
			}
		}
		
		return offsets;
	}

	/* (non-Javadoc)
	 * @see ijt.filter.morphology.Strel#reverse()
	 */
	@Override
	public DiskStrel reverse()
	{
		return new DiskStrel(radius);
	}

	/**
	 * Performs in-place dilation with a disk structuring element by calling the
	 * ImageJ native RankFilters algorithm, using RankFilters.MAX option.
	 * 
	 * @param image the image to process
	 */
	@Override
	public void inPlaceDilation(ImageProcessor image)
	{
		if (radius > 0.5)
		{
			new RankFilters().rank(image, radius, RankFilters.MAX);
		}
	}

	/**
	 * Performs in-place erosion with a disk structuring element by calling the
	 * ImageJ native RankFilters algorithm, using RankFilters.MIN option.
	 * 
	 * @param image the image to process
	 */
	@Override
	public void inPlaceErosion(ImageProcessor image)
	{
		if (radius > 0.5)
		{
			new RankFilters().rank(image, radius, RankFilters.MIN);
		}
	}
}
