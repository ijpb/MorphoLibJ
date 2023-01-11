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
public class MinimaAndMaxima3DTest {
	
	@Test
	public final void testRegionalMaxima_CubicMeshC6() {
		// load the reference image, and get its size
		ImageStack image = createCubicMeshImage();
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		// create test image: add a band with value 127 in the middle
		int zMid = sizeZ / 2;
		for (int y = 0; y < sizeY; y++) {
			for (int x = 0; x < sizeX; x++) {
				double val = image.getVoxel(x, y, zMid);
				if (val == 255)
					image.setVoxel(x, y, zMid, 127);
			}
		}
		
		ImageStack maxima = MinimaAndMaxima3D.regionalMaxima(image);
		
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					int v0 = (int) image.getVoxel(x, y, z);
					int v = (int) maxima.getVoxel(x, y, z);
					if (v0 == 255)
						assertEquals(255, v);
					else
						assertEquals(0, v);
				}
			}
		}
	}
	
	@Test
	public final void testRegionalMaxima_CubicMeshC26() {
		// load the reference image, and get its size
		ImageStack image = createCubicMeshImage();
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		// create test image: add a band with value 127 in the middle
		int zMid = sizeZ / 2;
		for (int y = 0; y < sizeY; y++) {
			for (int x = 0; x < sizeX; x++) {
				double val = image.getVoxel(x, y, zMid);
				if (val == 255)
					image.setVoxel(x, y, zMid, 127);
			}
		}
		
		ImageStack maxima = MinimaAndMaxima3D.regionalMaxima(image, 26);
		
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					int v0 = (int) image.getVoxel(x, y, z);
					int v = (int) maxima.getVoxel(x, y, z);
					if (v0 == 255)
						assertEquals(255, v);
					else
						assertEquals(0, v);
				}
			}
		}
	}

	private ImageStack createCubicMeshImage() {
		int sizeX = 20;
		int sizeY = 20;
		int sizeZ = 20;
		int bitDepth = 8;
		
		// create empty stack
		ImageStack stack = ImageStack.create(sizeX, sizeY, sizeZ, bitDepth);
		
		// number of voxels between edges and 'tube' borders 
		int gap = 2;

		// First, the edges in the x direction
		for (int z = 5 - gap - 1; z <= 5 + gap + 1; z++) {
			for (int y = 5 - gap - 1; y <= 5 + gap + 1; y++) {
				for (int x = 5 - gap - 1; x <= 15 + gap + 1; x++) {
					stack.setVoxel(x, y, z, 255);
					stack.setVoxel(x, y, z+10, 255);
				}				
			}
		}
		
		// then, the edges in the y direction
		for (int z = 5 - gap - 1; z <= 5 + gap + 1; z++) {
			for (int x = 5 - gap - 1; x <= 5 + gap + 1; x++) {
				for (int y = 5 - gap - 1; y <= 15 + gap + 1; y++) {
					stack.setVoxel(x + 10, y, z, 255);
					stack.setVoxel(x, y, z+10, 255);
					stack.setVoxel(x+10, y, z+10, 255);
				}				
			}
		}

		// Finally, the edges in the z direction
		for (int y = 5 - gap - 1; y <= 5 + gap + 1; y++) {
			for (int x = 5 - gap - 1; x <= 5 + gap + 1; x++) {
				for (int z = 5 - gap - 1; z <= 15 + gap + 1; z++) {
					stack.setVoxel(x, y+10, z, 255);
					stack.setVoxel(x+10, y+10, z, 255);
				}				
			}
		}
		
		return stack;
	}

	@Test
	public final void testRegionalMaxima_CubeGraph_C6() {
		// load the reference image, and get its size
		ImageStack image = createLeveledCubeGraphImage();
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		// create test image: add a band with value 127 in the middle
		int zMid = sizeZ / 2;
		for (int y = 0; y < sizeY; y++) {
			for (int x = 0; x < sizeX; x++) {
				double val = image.getVoxel(x, y, zMid);
				if (val == 255)
					image.setVoxel(x, y, zMid, 127);
			}
		}
		
		ImageStack maxima = MinimaAndMaxima3D.regionalMaxima(image, 6);
		
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					int v0 = (int) image.getVoxel(x, y, z);
					int v = (int) maxima.getVoxel(x, y, z);
					if (v0 == 255)
						assertEquals(255, v);
					else
						assertEquals(0, v);
				}
			}
		}
	}

	@Test
	public final void testRegionalMaxima_CubeGraph_C26() {
		// load the reference image, and get its size
		ImageStack image = createLeveledCubeGraphImage();
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		// create test image: add a band with value 127 in the middle
		int zMid = sizeZ / 2;
		for (int y = 0; y < sizeY; y++) {
			for (int x = 0; x < sizeX; x++) {
				double val = image.getVoxel(x, y, zMid);
				if (val == 255)
					image.setVoxel(x, y, zMid, 127);
			}
		}
		
		ImageStack maxima = MinimaAndMaxima3D.regionalMaxima(image, 26);
		
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					int v0 = (int) image.getVoxel(x, y, z);
					int v = (int) maxima.getVoxel(x, y, z);
					if (v0 == 255)
						assertEquals(255, v);
					else
						assertEquals(0, v);
				}
			}
		}
	}

	@Test
	public final void testRegionalMinima_CubeGraph_C6() {
		// load the reference image, and get its size
		ImageStack image = createInvertedLeveledCubeGraphImage();
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		// create test image: add a band with value 127 in the middle
		int zMid = sizeZ / 2;
		for (int y = 0; y < sizeY; y++) {
			for (int x = 0; x < sizeX; x++) {
				double val = image.getVoxel(x, y, zMid);
				if (val == 255)
					image.setVoxel(x, y, zMid, 127);
			}
		}
		
		ImageStack minima = MinimaAndMaxima3D.regionalMinima(image, 6);
		
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					int v0 = (int) image.getVoxel(x, y, z);
					int v = (int) minima.getVoxel(x, y, z);
					if (v0 == 0)
						assertEquals(255, v);
					else
						assertEquals(0, v);
				}
			}
		}
	}

	@Test
	public final void testRegionalMinima_CubeGraph_C26() {
		// load the reference image, and get its size
		ImageStack image = createInvertedLeveledCubeGraphImage();
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		// create test image: add a band with value 127 in the middle
		int zMid = sizeZ / 2;
		for (int y = 0; y < sizeY; y++) {
			for (int x = 0; x < sizeX; x++) {
				double val = image.getVoxel(x, y, zMid);
				if (val == 255)
					image.setVoxel(x, y, zMid, 127);
			}
		}
		
		ImageStack minima = MinimaAndMaxima3D.regionalMinima(image, 26);
		
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					int v0 = (int) image.getVoxel(x, y, z);
					int v = (int) minima.getVoxel(x, y, z);
					if (v0 == 0)
						assertEquals(255, v);
					else
						assertEquals(0, v);
				}
			}
		}
	}

	@Test
	public final void testRegionalMaxima_BatCochlea() 
	{
		// load the reference image, and get its size
		ImageStack image = readBatCochleaStack();
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		// create test image:
		// use cochlea volume, but add a band with value 127 in the middle
		int zMid = sizeZ / 2;
		for (int y = 0; y < sizeY; y++) {
			for (int x = 0; x < sizeX; x++) {
				double val = image.getVoxel(x, y, zMid);
				if (val == 255)
					image.setVoxel(x, y, zMid, 127);
			}
		}
		
		ImageStack maxima = MinimaAndMaxima3D.regionalMaxima(image);
		
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					int v0 = (int) image.getVoxel(x, y, z);
					int v = (int) maxima.getVoxel(x, y, z);
					if (v0 == 255)
						assertEquals(255, v);
					else
						assertEquals(0, v);
				}
			}
		}
	}
	
	@Test
	public final void testExtendedMaxima_CubeGraph_C6() {
		// load the reference image, and get its size
		ImageStack image = createLeveledCubeGraphImage();
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		// create test image: add a band with value 127 in the middle
		int zMid = sizeZ / 2;
		for (int y = 0; y < sizeY; y++) {
			for (int x = 0; x < sizeX; x++) {
				double val = image.getVoxel(x, y, zMid);
				if (val == 255)
					image.setVoxel(x, y, zMid, 127);
			}
		}
		
		ImageStack maxima = MinimaAndMaxima3D.extendedMaxima(image, 10, 6);
		
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					int v0 = (int) image.getVoxel(x, y, z);
					int v = (int) maxima.getVoxel(x, y, z);
					if (v0 == 255)
						assertEquals(255, v);
					else
						assertEquals(0, v);
				}
			}
		}
	}

	private ImageStack createLeveledCubeGraphImage() {
		ImageStack stack = createCubeGraphImage();
		stack.setVoxel(5, 1, 1, 224);
		stack.setVoxel(9, 5, 1, 192);
		stack.setVoxel(9, 9, 5, 160);
		stack.setVoxel(9, 5, 9, 128);
		stack.setVoxel(5, 1, 9,  96);
		stack.setVoxel(1, 5, 9,  64);
		stack.setVoxel(1, 9, 5,  32);

		return stack;
	}

	private ImageStack createInvertedLeveledCubeGraphImage() {
		ImageStack stack = createCubeGraphImage();
		for (int z = 0; z < stack.getSize(); z++) {
			for (int y = 0; y < stack.getHeight(); y++) {
				for (int x = 0; x < stack.getWidth(); x++) {
					stack.setVoxel(x, y, z, 255 - stack.getVoxel(x, y, z));
				}
			}
		}
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
	private ImageStack createCubeGraphImage() {
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
	
    private ImageStack readBatCochleaStack()
    {
        String fileName = getClass().getResource("/files/bat-cochlea_sub25.tif").getFile();
        ImagePlus imagePlus = IJ.openImage(fileName);
        
        // some check-ups
        assertNotNull(imagePlus);
        assertTrue(imagePlus.getStackSize() > 0);

        return imagePlus.getStack();
    }
}
