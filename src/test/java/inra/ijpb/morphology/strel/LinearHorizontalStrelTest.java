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
package inra.ijpb.morphology.strel;

import static org.junit.Assert.assertEquals;

import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Strel;

import org.junit.Test;

public class LinearHorizontalStrelTest {

	@Test
	public void testGetSize() {
		Strel strel = new LinearHorizontalStrel(5);
		int[] size = strel.getSize();
		assertEquals(size[0], 5);
		assertEquals(size[1], 1);
	}

	@Test
	public void testGetMask() {
		Strel strel = new LinearHorizontalStrel(5);
		int[][] mask = strel.getMask();
		
		assertEquals(mask.length, 1);
		assertEquals(mask[0].length, 5);
	}

	@Test
	public void testGetShifts() {
		Strel strel = new LinearHorizontalStrel(5);
		int[][] shifts = strel.getShifts();
		
		assertEquals(shifts.length, 5);
		assertEquals(shifts[0].length, 2);
	}

	@Test
	public void testReverse() {
		Strel strel = new LinearHorizontalStrel(5);
		int[] size = strel.getSize();
		Strel strel2 = strel.reverse();
		int[] size2 = strel2.getSize();
		assertEquals(size[0], size2[0]);
		assertEquals(size[1], size2[1]);
	}

	@Test
	public void testErosion_Square4x4() {
		ImageProcessor image = createImage_Square4x4();
		Strel strel = new LinearHorizontalStrel(3);
		
		ImageProcessor result = strel.erosion(image);

		for (int y = 3; y < 7; y++) {
			assertEquals(0, result.get(3, y));
			assertEquals(255, result.get(4, y));
			assertEquals(255, result.get(5, y));
			assertEquals(0, result.get(6, y));
		}
	}

	@Test
	public void testDilation_Square4x4() {
		ImageProcessor image = createImage_Square4x4();
		Strel strel = new LinearHorizontalStrel(3);
		
		ImageProcessor result = strel.dilation(image);

		for (int y = 3; y < 7; y++) {
			assertEquals(0, result.get(1, y));
			assertEquals(255, result.get(2, y));
			assertEquals(255, result.get(7, y));
			assertEquals(0, result.get(8, y));
		}
	}

	@Test
	public void testClosing() {
		ImageProcessor image = createImage_Square10x10();
		Strel strel = new LinearHorizontalStrel(5);
		
		ImageProcessor result = strel.closing(image);
		
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				assertEquals(image.get(x, y), result.get(x, y));
			}			
		}
	}

	@Test
	public void testOpening() {
		ImageProcessor image = createImage_Square10x10();
		Strel strel = new LinearHorizontalStrel(5);
		
		ImageProcessor result = strel.opening(image);
		
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				assertEquals(image.get(x, y), result.get(x, y));
			}			
		}
	}
	
	/**
	 * Test method for {@link inra.ijpb.morphology.strel.LinearHorizontalStrel#dilation(ij.ImageStack)}.
	 */
	@Test
	public void testDilation_3D() 
	{
		ImageStack image = createIsolatedVoxelImage();
		
		LinearHorizontalStrel strel = LinearHorizontalStrel.fromDiameter(5);
		ImageStack result = strel.dilation(image);
		
		assertEquals(255, result.getVoxel(5, 5, 5), .01);
		assertEquals(255, result.getVoxel(3, 5, 5), .01);
		assertEquals(255, result.getVoxel(7, 5, 5), .01);
		assertEquals(  0, result.getVoxel(2, 5, 5), .01);
		assertEquals(  0, result.getVoxel(8, 5, 5), .01);
	}

	private ImageProcessor createImage_Square4x4 () {
		ImageProcessor image = new ByteProcessor(10, 10);
		image.setValue(0);
		image.fill();
		
		for (int y = 3; y < 7; y++) {
			for (int x = 3; x < 7; x++) {
				image.set(x, y, 255);
			}			
		}
		
		return image;
	}

	private ImageProcessor createImage_Square10x10 () {
		ImageProcessor image = new ByteProcessor(30, 30);
		image.setValue(0);
		image.fill();
		
		for (int y = 10; y < 20; y++) {
			for (int x = 10; x < 20; x++) {
				image.set(x, y, 255);
			}			
		}
		
		return image;
	}

	private static final ImageStack createIsolatedVoxelImage()
	{
		ImageStack image = ImageStack.create(10, 10, 10, 8);
		image.setVoxel(5, 5, 5, 255);
		return image;
	}

}
