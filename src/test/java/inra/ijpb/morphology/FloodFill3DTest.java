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
package inra.ijpb.morphology;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;

import org.junit.Test;

/**
 * @author David Legland
 *
 */
public class FloodFill3DTest {

	@Test
	public final void testFloodFill_BatCochlea_C26() {
		String fileName = getClass().getResource("/files/bat-cochlea-volume.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		assertNotNull(imagePlus);
		assertTrue(imagePlus.getStackSize() > 0);

		// load the reference image, and get its size
		ImageStack image = imagePlus.getStack();
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		// flood fill from an arbitrary point
		int value = 83;
		ImageStack result = image.duplicate();
		FloodFill3D.floodFill(result, 90, 30, 50, value, 26);
		
		int vRef = (int) image.getVoxel(81, 100, 0);
		assertEquals(255, vRef);
		
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					int v0 = (int) image.getVoxel(x, y, z);
					int v = (int) result.getVoxel(x, y, z);
//					String msg = String.format("x=%d, y=%d, z=%d", x, y, z);
					if (v0 == 255)
						assertEquals(value, v);
					else
						assertEquals(0, v);
				}
			}
		}
	}

	@Test
	public final void testFloodFill_Cross3d_C6() {
		// Create test image
		int sizeX = 5;
		int sizeY = 5;
		int sizeZ = 5;
		ImageStack image = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		int val0 = 50;
		// three axes
		for (int i = 0; i < 5; i++) {
			image.setVoxel(i, 2, 2, val0);
			image.setVoxel(2, i, 2, val0);
			image.setVoxel(2, 2, i, val0);
		}
		
		ImageStack result = image.duplicate();
		int newVal = 37;
		FloodFill3D.floodFill(result, 1, 2, 2, newVal, 6);
		
		// Test each of the branches
		assertEquals(newVal, (int) result.getVoxel(0, 2, 2));
		assertEquals(newVal, (int) result.getVoxel(4, 2, 2));
		assertEquals(newVal, (int) result.getVoxel(2, 0, 2));
		assertEquals(newVal, (int) result.getVoxel(2, 4, 2));
		assertEquals(newVal, (int) result.getVoxel(2, 2, 0));
		assertEquals(newVal, (int) result.getVoxel(2, 2, 4));
	}

	@Test
	public final void testFloodFill_Cross3d_C6Float() {
		// Create test image
		int sizeX = 5;
		int sizeY = 5;
		int sizeZ = 5;
		ImageStack image = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		int val0 = 50;
		// three axes
		for (int i = 0; i < 5; i++) {
			image.setVoxel(i, 2, 2, val0);
			image.setVoxel(2, i, 2, val0);
			image.setVoxel(2, 2, i, val0);
		}
		
		ImageStack result = image.duplicate().convertToFloat();
		double newVal = 37.2;
		FloodFill3D.floodFill(result, 1, 2, 2, newVal, 6);
		
		// Test each of the branches
		assertEquals(newVal, result.getVoxel(0, 2, 2), .01);
		assertEquals(newVal, result.getVoxel(4, 2, 2), .01);
		assertEquals(newVal, result.getVoxel(2, 0, 2), .01);
		assertEquals(newVal, result.getVoxel(2, 4, 2), .01);
		assertEquals(newVal, result.getVoxel(2, 2, 0), .01);
		assertEquals(newVal, result.getVoxel(2, 2, 4), .01);
	}

	@Test
	public final void testFloodFillPair_Cross3d_C6Float() {
		// Create test image
		int sizeX = 5;
		int sizeY = 5;
		int sizeZ = 5;
		ImageStack image = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		int val0 = 50;
		// three axes
		for (int i = 0; i < 5; i++) {
			image.setVoxel(i, 2, 2, val0);
			image.setVoxel(2, i, 2, val0);
			image.setVoxel(2, 2, i, val0);
		}
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		
		float newVal = 37;
		FloodFill3D.floodFillFloat(image, 1, 2, 2, result, newVal, 6);
		
//		printStack(result);
		
		// Test each of the branches
		assertEquals(newVal, result.getVoxel(0, 2, 2), .01);
		assertEquals(newVal, result.getVoxel(4, 2, 2), .01);
		assertEquals(newVal, result.getVoxel(2, 0, 2), .01);
		assertEquals(newVal, result.getVoxel(2, 4, 2), .01);
		assertEquals(newVal, result.getVoxel(2, 2, 0), .01);
		assertEquals(newVal, result.getVoxel(2, 2, 4), .01);
	}

	@Test
	public final void testFloodFill_Cross3d_C26() {
		ImageStack image = createCornerCross();
		int newVal = 37;
		FloodFill3D.floodFill(image, 2, 4, 4, newVal, 26);
		
//		printStack(result);
		
		// Test each of the branches
		assertEquals(newVal, (int) image.getVoxel(0, 4, 4));
		assertEquals(newVal, (int) image.getVoxel(8, 4, 4));
		assertEquals(newVal, (int) image.getVoxel(4, 0, 4));
		assertEquals(newVal, (int) image.getVoxel(4, 8, 4));
		assertEquals(newVal, (int) image.getVoxel(4, 4, 0));
		assertEquals(newVal, (int) image.getVoxel(4, 4, 8));
	}
	
	@Test
	public final void testFloodFill_Cross3d_C26Float() {
		ImageStack image = createCornerCross().convertToFloat();
		double newVal = 37.2;
		FloodFill3D.floodFill(image, 2, 4, 4, newVal, 26);
		
//		printStack(result);
		
		// Test each of the branches
		assertEquals(newVal, image.getVoxel(0, 4, 4), .01);
		assertEquals(newVal, image.getVoxel(8, 4, 4), .01);
		assertEquals(newVal, image.getVoxel(4, 0, 4), .01);
		assertEquals(newVal, image.getVoxel(4, 8, 4), .01);
		assertEquals(newVal, image.getVoxel(4, 4, 0), .01);
		assertEquals(newVal, image.getVoxel(4, 4, 8), .01);
	}
	
	@Test
	public final void testFloodFillPair_Cross3d_C26Float() {
		ImageStack image = createCornerCross();
//		System.out.println("input image:");
//		printStack(image);
		
		ImageStack result = ImageStack.create(image.getWidth(), image.getHeight(), image.getSize(), 8);
		
		float newVal = 120;
		FloodFill3D.floodFillFloat(image, 2, 4, 4, result, newVal, 26);
		
//		System.out.println("output image:");
//		printStack(result);
		
		// Test each of the branches
		assertEquals(newVal, result.getVoxel(0, 4, 4), .01);
		assertEquals(newVal, result.getVoxel(8, 4, 4), .01);
		assertEquals(newVal, result.getVoxel(4, 0, 4), .01);
		assertEquals(newVal, result.getVoxel(4, 8, 4), .01);
		assertEquals(newVal, result.getVoxel(4, 4, 0), .01);
		assertEquals(newVal, result.getVoxel(4, 4, 8), .01);
	}
	
	/**
	 * Creates a stack representing a cross with branches touching only by corners.
	 */
	public ImageStack createCornerCross() {
		// Create test image
		int sizeX = 9;
		int sizeY = 9;
		int sizeZ = 9;
		ImageStack image = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		int val0 = 50;
		// Center voxel
		image.setVoxel(4, 4, 4, val0);
		// eight corners
		image.setVoxel(3, 3, 3, val0);
		image.setVoxel(3, 3, 5, val0);
		image.setVoxel(3, 5, 3, val0);
		image.setVoxel(3, 5, 5, val0);
		image.setVoxel(5, 3, 3, val0);
		image.setVoxel(5, 3, 5, val0);
		image.setVoxel(5, 5, 3, val0);
		image.setVoxel(5, 5, 5, val0);
		// six branches
		for (int i = 0; i < 3; i++) {
			image.setVoxel(i, 4, 4, val0);
			image.setVoxel(i + 6, 4, 4, val0);
			image.setVoxel(4, i, 4, val0);
			image.setVoxel(4, i + 6, 4, val0);
			image.setVoxel(4, 4, i, val0);
			image.setVoxel(4, 4, i + 6, val0);
		}

		return image;
	}
	
	@Test
	public final void testFloodFill_BatCochlea_C26Float() {
		String fileName = getClass().getResource("/files/bat-cochlea-volume.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		assertNotNull(imagePlus);
		assertTrue(imagePlus.getStackSize() > 0);

		// load the reference image, and get its size
		ImageStack image = imagePlus.getStack();
		image = image.convertToFloat();
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		// flood fill from an arbitrary point
		double value = 312.5;
		ImageStack result = image.duplicate();
		FloodFill3D.floodFill(result, 90, 30, 50, value, 26);
		
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					double v0 = image.getVoxel(x, y, z);
					double v = result.getVoxel(x, y, z);
					if (v0 == 255)
						assertEquals(value, v, .01);
					else
						assertEquals(0, v, .01);
				}
			}
		}
	}

	public void printStack(ImageStack stack) {
		int width = stack.getWidth();
		int height = stack.getHeight();
		int depth = stack.getSize();
		
		for (int z = 0; z < depth; z++) {
			System.out.println("slice " + z + ":");
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					System.out.print(String.format("%3d", (int) stack.getVoxel(x, y, z)) + " ");
				}
				System.out.println("");			
			}
		}
	}

}
