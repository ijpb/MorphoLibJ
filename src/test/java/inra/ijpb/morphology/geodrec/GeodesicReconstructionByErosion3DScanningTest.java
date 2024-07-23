/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
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
package inra.ijpb.morphology.geodrec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import inra.ijpb.data.image.Images3D;

public class GeodesicReconstructionByErosion3DScanningTest {

	@Test
	public final void test_batCochlea()
	{
		// Open test image
		String fileName = getClass().getResource("/files/bat-cochlea_sub25.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		assertNotNull(imagePlus);
		assertTrue(imagePlus.getStackSize() > 0);
		ImageStack mask = imagePlus.getStack();
		
		// get image size
		int sizeX = mask.getWidth();
		int sizeY = mask.getHeight();
		int sizeZ = mask.getSize();
		int bitDepth = mask.getBitDepth();

		// invert stack image
		mask = Images3D.complement(mask, 255);

		// initialize marker image: 255 everywhere except a a given position (the germ)
		ImageStack marker = ImageStack.create(sizeX, sizeY, sizeZ, bitDepth);
		Images3D.fill(marker, 255);
        marker.setVoxel(5, 21, 12, 0);
		
		// create reconstruction algorithm
		GeodesicReconstructionByErosion3DScanning algo = new GeodesicReconstructionByErosion3DScanning();
		algo.verbose = false;

		// run algo and compute elapsed time
//		long t0 = System.currentTimeMillis();
		ImageStack result = algo.applyTo(marker, mask);
//		long t1 = System.currentTimeMillis();
//		double dt = (t1 - t0) / 1000.0;
//		System.out.println("Elapsed time: " + dt + " s");

		// Check images equality
        for (int z = 0; z < sizeZ; z++)
        {
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
					double vRes = result.getVoxel(x, y, z);
					double vMask = mask.getVoxel(x, y, z);
					assertEquals(String.format(Locale.ENGLISH, "At position (%d,%d,%d)", x, y, z), 
							vRes, vMask, .01);
				}
			}
		}
		
	}

	@Test
	public void test_InvertedLeveledCubeGraph() 
	{
		ImageStack mask = createInvertedLeveledCubeGraphImage();
		mask = mask.convertToFloat();

		ImageStack marker = ImageStack.create(11, 11, 11, 8);
		Images3D.fill(marker, 255);
		marker.setVoxel(1, 1, 1, 0);
		marker = marker.convertToFloat();
		
//		System.out.println("\n=== Mask Image ===");
//		Images3D.print(mask);
//		System.out.println("\n=== Marker Image ===");
//		Images3D.print(marker);
		
		GeodesicReconstructionByErosion3DScanning algo = new GeodesicReconstructionByErosion3DScanning(6);
		ImageStack result = algo.applyTo(marker, mask);

//		System.out.println("\n=== Result Image ===");
//		Images3D.print(result);
		
		assertEquals(  0, result.getVoxel(1, 1, 1), .01);
		assertEquals( 32, result.getVoxel(9, 1, 1), .01);
		assertEquals( 64, result.getVoxel(9, 9, 1), .01);
		assertEquals( 96, result.getVoxel(9, 9, 9), .01);
		assertEquals(128, result.getVoxel(9, 1, 9), .01);
		assertEquals(160, result.getVoxel(1, 1, 9), .01);
		assertEquals(192, result.getVoxel(1, 9, 9), .01);
		assertEquals(224, result.getVoxel(1, 9, 1), .01);
	}

    private static final ImageStack createInvertedLeveledCubeGraphImage()
    {
        ImageStack stack = createCubeGraphImage();
        stack = Images3D.complement(stack, 255);
        
		stack.setVoxel(5, 1, 1,  32);
		stack.setVoxel(9, 5, 1,  64);
		stack.setVoxel(9, 9, 5,  96);
		stack.setVoxel(9, 5, 9, 128);
		stack.setVoxel(5, 1, 9, 160);
		stack.setVoxel(1, 5, 9, 192);
		stack.setVoxel(1, 9, 5, 224);

		return stack;
	}
	
	/**
	 * Creates a 3D image containing thin cube mesh.
	 */
	private static final ImageStack createCubeGraphImage() {
		int sizeX = 11;
		int sizeY = 11;
		int sizeZ = 11;
		int bitDepth = 8;
		
		// create empty stack
		ImageStack stack = ImageStack.create(sizeX, sizeY, sizeZ, bitDepth);
		
		// coordinates of the cube edges
		int x1 = 1;
		int x2 = 9;
		int y1 = 1;
		int y2 = 9;
		int z1 = 1;
		int z2 = 9;
		
		// First, the edges in the x direction
		for (int x = x1; x <= x2; x++) {
			stack.setVoxel(x, y1, z1, 255);
			stack.setVoxel(x, y1, z2, 255);
		}				
		
		// then, the edges in the y direction
		for (int y = y1; y <= y2; y++) {
			stack.setVoxel(x2, y, z1, 255);
			stack.setVoxel(x1, y, z2, 255);
			stack.setVoxel(x2, y, z2, 255);
		}				

		// Finally, the edges in the z direction
		for (int z = z1; z <= z2; z++) {
			stack.setVoxel(x1, y2, z, 255);
			stack.setVoxel(x2, y2, z, 255);
		}				
		
		return stack;
	}
	
}
