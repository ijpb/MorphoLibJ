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
package inra.ijpb.morphology.strel;

import static org.junit.Assert.*;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import org.junit.Test;

import inra.ijpb.morphology.Strel;


public class DiskStrelTest {

	@Test
	public void testGetSize() {
		Strel se = DiskStrel.fromDiameter(5);
		int[] size = se.getSize();
		assertEquals(5, size[0]);
		assertEquals(5, size[1]);
	}

	/**
	 * Dilates a single pixel by a 3x3 disk, and check the shape of the result.
	 * The result should be a 3x3 square (approximation of 3x3 disk)
	 */
	@Test
	public void testDilate_SinglePixel_Radius() {
		Strel strel = DiskStrel.fromRadius(1);
		
		ImageProcessor image = new ByteProcessor(10, 10);
		image.setValue(0);
		image.fill();
		image.set(5, 5, 255);
		ImageProcessor result = strel.dilation(image);

		// Check all values inside square
		for (int y = 4; y < 7; y++)
			for (int x = 4; x < 7; x++)
				assertEquals(255, result.get(x, y));
	}
	
	/**
	 * Dilates a single pixel by a disk with radius 0, and check the shape of the result.
	 * The result should be the same as the input image.
	 */
	@Test
	public void testDilate_SinglePixel_Radius0() {
		Strel strel = DiskStrel.fromRadius(0);
		
		ImageProcessor image = new ByteProcessor(10, 10);
		image.setValue(0);
		image.fill();
		image.set(5, 5, 255);
		ImageProcessor result = strel.dilation(image);

		// Input image should be equal to output image
		for (int y = 0; y < 9; y++)
			for (int x = 0; x < 9; x++)
				assertEquals(image.get(x, y), result.get(x, y));
	}
	
	/**
	 * Dilates a single pixel by a disk with diameter 4. 
	 * The result should be larger than dilation with diameter 3.
	 */
	@Test
	public void testDilate_SinglePixel_EvenDiameter() {
		Strel disk3 = DiskStrel.fromDiameter(3);
		Strel disk4 = DiskStrel.fromDiameter(4);
		
		ImageProcessor image = new ByteProcessor(10, 10);
		image.setValue(0);
		image.fill();
		image.set(5, 5, 255);
		
		ImageProcessor result3 = disk3.dilation(image);
		ImageProcessor result4 = disk4.dilation(image);

		// Check result3 <= result4
		boolean different = false;
		for (int y = 0; y < 10; y++)
		{
			for (int x = 0; x < 10; x++)
			{
				int res3 = result3.get(x, y);
				int res4 = result4.get(x, y);
				assertTrue(res3 <= res4);
				
				if (res3 != res4)
				{
					different = true;
				}
			}
		}
		
		assertTrue(different);
	}

	/**
	 * Erodes a single pixel by a disk with radius 0, and checks the shape of the result.
	 * The result should be the same as the input image.
	 */
	@Test
	public void testErode_SinglePixel_Radius0() 
	{
		ImageProcessor image = new ByteProcessor(10, 10);
		image.setValue(255);
		image.fill();
		image.set(5, 5, 0);
		
		Strel strel = DiskStrel.fromRadius(0);

		ImageProcessor result = strel.erosion(image);

		// Input image should be equal to output image
		for (int y = 0; y < 9; y++)
			for (int x = 0; x < 9; x++)
				assertEquals(image.get(x, y), result.get(x, y));
	}
	
}
