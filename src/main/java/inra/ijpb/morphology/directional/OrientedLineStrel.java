/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2017 INRA.
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
package inra.ijpb.morphology.directional;

import static java.lang.Math.*;
import ij.process.ImageProcessor;
import inra.ijpb.data.border.BorderManager;
import inra.ijpb.data.border.MirroringBorder;
import inra.ijpb.morphology.Strel;
import inra.ijpb.morphology.strel.AbstractStrel;

/**
 * A linear structuring element, defined by a length and an orientation.
 * 
 * @author David Legland
 *
 */
public class OrientedLineStrel extends AbstractStrel implements Strel
{
	/** The length of the line, in pixels */
	double length;

	/** orientation of the line, in degrees, counted CCW from horizontal. */
	double theta;

	/**
	 * Stores the shape of this structuring element as an array of shifts with
	 * respect to the central pixel.
	 * The array has N-by-2 elements, where N is the number of pixels composing the strel.
	 */
	int[][] shifts;

	/**
	 * Creates an new instance of linear structuring element. The number of
	 * pixels composing the line may differ from the specified length due to
	 * rounding effects.
	 * 
	 * @param length
	 *            the (approximate) length of the structuring element.
	 * @param angleInDegrees
	 *            the angle with the horizontal of the structuring element
	 */
	public OrientedLineStrel(double length, double angleInDegrees)
	{
		this.length = length;
		this.theta = angleInDegrees;

		this.computeShifts();
	}

	/**
	 * Computes the position of the pixels that constitutes this structuring
	 * element.
	 */
	private void computeShifts()
	{
		// Components of direction vector
		double thetaRads = Math.toRadians(this.theta);
		double dx = Math.cos(thetaRads);
		double dy = Math.sin(thetaRads);

		// length of projected line
		double dMax = max(abs(dx), abs(dy));
		double projLength = this.length * dMax;

		// half-size and size of the mask
		int n2 = (int) ceil((projLength - 1) / 2);
		int n = 2 * n2 + 1;

		// allocate memory for shifts array
		this.shifts = new int[n][2];

		// compute position of line pixels
		if (abs(dx) >= abs(dy))
		{
			// process horizontal lines
			for (int i = -n2; i <= n2; i++)
			{
				shifts[i + n2][0] = i;
				shifts[i + n2][1] = (int) round((double) i * dy / dx);
			}
		} 
		else
		{
			// process vertical lines
			for (int i = -n2; i <= n2; i++)
			{
				shifts[i + n2][1] = i;
				shifts[i + n2][0] = (int) round((double) i * dx / dy);
			}
		}	
	}
	
	/**
	 * Returns the size of the structuring element, as an array of size in
	 * each direction.
	 * @return the size of the structuring element
	 */
	public int[] getSize() 
	{
		int n = this.shifts.length;
		return new int[]{n, n};
	}

	/**
	 * Returns the structuring element as a mask. Each value is either 0 or 255. 
	 * @return the mask of the structuring element
	 */
	public int[][] getMask() 
	{
		int n = this.shifts.length;
		int[][] mask = new int[n][n];
		
		// precompute offsets
		int[] offsets = this.getOffset();
		int ox = offsets[0];
		int oy = offsets[1];
		
		// fill up the mask
		for (int i = 0; i < n; i++)
		{
			mask[this.shifts[i][1] + oy][this.shifts[i][0] + ox] = 255;
		}
		
		return mask;
	}
	
	/**
	 * Returns the offset in the mask.
	 * @return the offset in the mask
	 */
	public int[] getOffset() 
	{
		int offset = (this.shifts.length - 1) / 2;
		return new int[]{offset, offset};
	}
	
	/**
	 * Returns the structuring element as a set of shifts.
	 * @return a set of shifts
	 */
	public int[][] getShifts() 
	{
		return this.shifts;
	}

	@Override
	public ImageProcessor dilation(ImageProcessor image)
	{
		// Allocate memory for result
		ImageProcessor result = image.duplicate();

		BorderManager bm = new MirroringBorder(image);

		// Iterate on image pixels
		for (int y = 0; y < image.getHeight(); y++)
		{
			for (int x = 0; x < image.getWidth(); x++)
			{
				// reset accumulator
				double res = Double.MIN_VALUE;
	
				// iterate on neighbors
				for (int i = 0; i < shifts.length; i++) 
				{
					double value = bm.getf(x + shifts[i][0], y + shifts[i][1]);
					res = Math.max(res, value);
				}
				
				// compute result
				result.setf(x, y, (float) res);
			}			
		}
		
		return result;
	}

	@Override
	public ImageProcessor erosion(ImageProcessor image)
	{
		// Allocate memory for result
		ImageProcessor result = image.duplicate();
		
		BorderManager bm = new MirroringBorder(image);
		
		// Iterate on image pixels
		for (int y = 0; y < image.getHeight(); y++)
		{
			for (int x = 0; x < image.getWidth(); x++)
			{
				// reset accumulator
				double res = Double.MAX_VALUE;

				// iterate on neighbors
				for (int i = 0; i < shifts.length; i++)
				{
					double value = bm.getf(x + shifts[i][0], y + shifts[i][1]);
					res = Math.min(res, value);
				}
				
				// compute result
				result.setf(x, y, (float) res);
			}			
		}
		
		return result;
	}

	@Override
	public ImageProcessor closing(ImageProcessor image)
	{
		return this.erosion(this.dilation(image));
	}

	@Override
	public ImageProcessor opening(ImageProcessor image)
	{
		return this.dilation(this.erosion(image));
	}

	/**
	 * Returns this structuring element, as oriented line structuring elements
	 * are symmetric by definition.
	 */
	@Override
	public Strel reverse() 
	{
		return this;
	}
}
