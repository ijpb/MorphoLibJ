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
/**
 * 
 */
package inra.ijpb.label.distmap;

import ij.ImageStack;
import inra.ijpb.data.image.ImageUtils;

/**
 * A collection of static method for creating test images shared by different
 * test cases.
 */
public class TestImages
{
	/**
	 * Creates a test image composed of eight cuboids with various sizes.
	 * 
	 * @return a test image composed of eight cuboids with various sizes.
	 */
	public static final ImageStack createStack_eightCuboids()
	{
		ImageStack stack = ImageStack.create(15, 15, 15, 8);
		ImageUtils.fillRect3d(stack, 1, 1, 1, 3, 3, 3, 3);
		ImageUtils.fillRect3d(stack, 5, 1, 1, 9, 3, 3, 4);
		ImageUtils.fillRect3d(stack, 1, 5, 1, 3, 9, 3, 7);
		ImageUtils.fillRect3d(stack, 5, 5, 1, 9, 9, 3, 8);
		ImageUtils.fillRect3d(stack, 1, 1, 5, 3, 3, 9, 11);
		ImageUtils.fillRect3d(stack, 5, 1, 5, 9, 3, 9, 12);
		ImageUtils.fillRect3d(stack, 1, 5, 5, 3, 9, 9, 15);
		ImageUtils.fillRect3d(stack, 5, 5, 5, 9, 9, 9, 17);

		return stack;
	}

	/**
	 * Creates a test image composed of eight adjacent cubes that also touch the
	 * image borders.
	 * 
	 * @return a test image composed of eight adjacent cubes.
	 */
	public static final ImageStack createStack_eightAdjacentCubes()
	{
		ImageStack stack = ImageStack.create(10, 10, 10, 8);
		ImageUtils.fillRect3d(stack, 0, 0, 0, 5, 5, 5, 3);
		ImageUtils.fillRect3d(stack, 5, 0, 0, 5, 5, 5, 4);
		ImageUtils.fillRect3d(stack, 0, 5, 0, 5, 5, 5, 7);
		ImageUtils.fillRect3d(stack, 5, 5, 0, 5, 5, 5, 8);
		ImageUtils.fillRect3d(stack, 0, 0, 5, 5, 5, 5, 11);
		ImageUtils.fillRect3d(stack, 5, 0, 5, 5, 5, 5, 12);
		ImageUtils.fillRect3d(stack, 0, 5, 5, 5, 5, 5, 15);
		ImageUtils.fillRect3d(stack, 5, 5, 5, 5, 5, 5, 17);

		return stack;
	}
	
	/**
	 * private constructor to prevent instantiation.
	 */
	private TestImages()
	{
	}
}
