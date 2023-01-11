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
package inra.ijpb.segment;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

/**
 * Static methods for thresholding images.
 * 
 * @author David Legland
 *
 */
public class Threshold
{
	/**
	 * Private constructor to prevent class instantiation.
	 */
	private Threshold()
	{
	}

	/**
	 * Creates a new 3D binary image with value 255 when input image has value
	 * between <code>lower</code> and <code>upper</code> values (inclusive).
	 * 
	 * @param image
	 *            the imagePlus that contains the grayscale image
	 * @param lower
	 *            the lower threshold bound (inclusive)
	 * @param upper
	 *            the upper threshold bound (inclusive)
	 * @return a new ImagePlus containing the binarised image
	 */
	public static final ImagePlus threshold(ImagePlus image, double lower, double upper)
	{
		String newName = image.getShortTitle() + "-bin";
		if (image.getStackSize() == 1) 
		{
			ImageProcessor result = threshold(image.getProcessor(), lower, upper);
			return new ImagePlus(newName, result);
		} 
		else
		{
			ImageStack result = threshold(image.getStack(), lower, upper);
			return new ImagePlus(newName, result);
		}
	}
	
	/**
	 * Creates a new binary image with value 255 when input image has value
	 * between <code>lower</code> and <code>upper</code> values (inclusive).
	 * 
	 * @param image
	 *            the input grayscale image
	 * @param lower
	 *            the lower threshold bound (inclusive)
	 * @param upper
	 *            the upper threshold bound (inclusive)
	 * @return a binary image
	 */
	public static final ImageProcessor threshold(ImageProcessor image, double lower, double upper)
	{
		if (image instanceof ColorProcessor) 
		{
			throw new IllegalArgumentException("Requires a gray scale image");
		}
		
		int width = image.getWidth();
		int height = image.getHeight();

		ImageProcessor result = new ByteProcessor(width, height);
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				double value = image.getf(x, y);
				if (value >= lower && value <= upper)
					result.set(x, y, 255);
			}

		}

		return result;
	}

	/**
	 * Creates a new 3D binary image with value 255 when input image has value
	 * between <code>lower</code> and <code>upper</code> values (inclusive).
	 * 
	 * @param image
	 *            the input 3D grayscale image
	 * @param lower
	 *            the lower threshold bound (inclusive)
	 * @param upper
	 *            the upper threshold bound (inclusive)
	 * @return a 3D binary image
	 */
	public static final ImageStack threshold(ImageStack image, double lower, double upper)
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		for (int z = 0; z < sizeZ; z++)
		{
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					double value = image.getVoxel(x, y, z);
					if (value >= lower && value <= upper)
						result.setVoxel(x, y, z, 255);
				}
			}
		}

		return result;
	}

}
