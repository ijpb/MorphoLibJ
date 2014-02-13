package inra.ijpb.morphology;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import inra.ijpb.morphology.strel.CubeStrel;

import org.junit.Test;

/**
 * Test the various static methods in GeodesicReconstruction3D class.
 * Many tests are already performed in geodrec subpackage, but this class
 * tests only global behavious, whereas geodrec focus on differences between
 * algorithms. 
 */
public class GeodesicReconstruction3DTest {

	@Test
	public final void test_reconstructByDilation_CubicMeshC26() {
		ImageStack mask = createCubicMeshImage();
		
		ImageStack marker = ImageStack.create(20, 20, 20, 8);
		marker.setVoxel(5, 5, 5, 255);
		
		ImageStack result = GeodesicReconstruction3D.reconstructByDilation(marker, mask, 26);
		
		assertEquals(255, result.getVoxel(5, 15, 5), .01);
	}

	@Test
	public final void test_reconstructByDilation_LeveledCubeGraphC6() {
		ImageStack mask = createLeveledCubeGraphImage();
		ImageStack marker = ImageStack.create(11, 11, 11, 8);
		marker.setVoxel(1, 1, 1, 255);
		
		ImageStack result = GeodesicReconstruction3D.reconstructByDilation(marker, mask, 6);
		
		assertEquals(255, result.getVoxel(1, 1, 1), .01);
		assertEquals(224, result.getVoxel(9, 1, 1), .01);
		assertEquals(192, result.getVoxel(9, 9, 1), .01);
		assertEquals(160, result.getVoxel(9, 9, 9), .01);
		assertEquals(128, result.getVoxel(9, 1, 9), .01);
		assertEquals( 96, result.getVoxel(1, 1, 9), .01);
		assertEquals( 64, result.getVoxel(1, 9, 9), .01);
		assertEquals( 32, result.getVoxel(1, 9, 1), .01);
	}

	@Test
	public final void test_reconstructByDilation_LeveledCubeGraphC26() {
		ImageStack mask = createLeveledCubeGraphImage();
		ImageStack marker = ImageStack.create(11, 11, 11, 8);
		marker.setVoxel(1, 1, 1, 255);
		
		ImageStack result = GeodesicReconstruction3D.reconstructByDilation(marker, mask, 26);
		
		assertEquals(255, result.getVoxel(1, 1, 1), .01);
		assertEquals(224, result.getVoxel(9, 1, 1), .01);
		assertEquals(192, result.getVoxel(9, 9, 1), .01);
		assertEquals(160, result.getVoxel(9, 9, 9), .01);
		assertEquals(128, result.getVoxel(9, 1, 9), .01);
		assertEquals( 96, result.getVoxel(1, 1, 9), .01);
		assertEquals( 64, result.getVoxel(1, 9, 9), .01);
		assertEquals( 32, result.getVoxel(1, 9, 1), .01);
	}

	@Test
	public final void test_reconstructByDilation_CubicMeshC6() {
		ImageStack mask = createCubicMeshImage();
		
		ImageStack marker = ImageStack.create(20, 20, 20, 8);
		marker.setVoxel(5, 5, 5, 255);
		
		ImageStack result = GeodesicReconstruction3D.reconstructByDilation(marker, mask, 6);
		
		assertEquals(255, result.getVoxel(5, 15, 5), .01);
	}

	@Test
	public final void test_reconstructByDilation_CubicHollowMesh() {
		ImageStack mask = createCubicHollowMeshImage();
		
		ImageStack marker = ImageStack.create(20, 20, 20, 8);
		for (int z = 0; z < 20; z++) {
			for (int y = 0; y < 20; y++) {
				for (int x = 0; x < 20; x++) {
					marker.setVoxel(x, y, z, 255);
				}
			}
		}
		marker.setVoxel(5, 5, 5, 0);
		
		ImageStack result = GeodesicReconstruction3D.reconstructByDilation(marker, mask, 6);

		assertEquals(0, result.getVoxel(5, 15, 5), .01);
	}

	@Test
	public final void test_reconstructByDilation_CochleaVolumeC26() {
		ImagePlus imagePlus = IJ.openImage("files/bat-cochlea-volume.tif");
		assertNotNull(imagePlus);

		assertTrue(imagePlus.getStackSize() > 0);

		ImageStack mask = imagePlus.getStack();
		int width = mask.getWidth();
		int height = mask.getHeight();
		int depth = mask.getSize();
		int bitDepth = mask.getBitDepth();
		ImageStack marker = ImageStack.create(width, height, depth, bitDepth);

		marker.setVoxel(20, 80, 50, 255);

		ImageStack result = GeodesicReconstruction3D.reconstructByDilation(marker, mask, 26);
		
		for(int z = 0; z < depth; z++) {
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					assertEquals(result.getVoxel(x, y, z),
							mask.getVoxel(x, y, z), .01);
				}
			}
		}
		
	}
	
	@Test
	public final void test_reconstructByDilation_CochleaVolumeC6() {
		ImagePlus imagePlus = IJ.openImage("files/bat-cochlea-volume.tif");
		assertNotNull(imagePlus);

		assertTrue(imagePlus.getStackSize() > 0);

		ImageStack mask = imagePlus.getStack();
		// Ensure regularity of the mask
		mask = Morphology.opening(mask, CubeStrel.fromRadius(1));
		
		int width = mask.getWidth();
		int height = mask.getHeight();
		int depth = mask.getSize();
		int bitDepth = mask.getBitDepth();
		ImageStack marker = ImageStack.create(width, height, depth, bitDepth);

		marker.setVoxel(20, 80, 50, 255);

		ImageStack result = GeodesicReconstruction3D.reconstructByDilation(marker, mask, 6);
		
		for(int z = 0; z < depth; z++) {
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					assertEquals(result.getVoxel(x, y, z),
							mask.getVoxel(x, y, z), .01);
				}
			}
		}
	}
	
	@Test
	public final void test_reconstructByErosion_LeveledCubeGraphC6() {
		ImageStack mask = createInvertedLeveledCubeGraphImage();

		ImageStack marker = ImageStack.create(11, 11, 11, 8);
		fillStack(marker, 255);
		marker.setVoxel(1, 1, 1, 0);
		
		ImageStack result = GeodesicReconstruction3D.reconstructByErosion(marker, mask, 6);
		
		assertEquals(  0, result.getVoxel(1, 1, 1), .01);
		assertEquals( 32, result.getVoxel(9, 1, 1), .01);
		assertEquals( 64, result.getVoxel(9, 9, 1), .01);
		assertEquals( 96, result.getVoxel(9, 9, 9), .01);
		assertEquals(128, result.getVoxel(9, 1, 9), .01);
		assertEquals(160, result.getVoxel(1, 1, 9), .01);
		assertEquals(192, result.getVoxel(1, 9, 9), .01);
		assertEquals(224, result.getVoxel(1, 9, 1), .01);
	}

	@Test
	public final void test_reconstructByErosion_LeveledCubeGraphC26() {
		ImageStack mask = createInvertedLeveledCubeGraphImage();
		ImageStack marker = ImageStack.create(11, 11, 11, 8);
		fillStack(marker, 255);
		marker.setVoxel(1, 1, 1, 0);
		
		ImageStack result = GeodesicReconstruction3D.reconstructByErosion(marker, mask, 26);
		
		assertEquals(  0, result.getVoxel(1, 1, 1), .01);
		assertEquals( 32, result.getVoxel(9, 1, 1), .01);
		assertEquals( 64, result.getVoxel(9, 9, 1), .01);
		assertEquals( 96, result.getVoxel(9, 9, 9), .01);
		assertEquals(128, result.getVoxel(9, 1, 9), .01);
		assertEquals(160, result.getVoxel(1, 1, 9), .01);
		assertEquals(192, result.getVoxel(1, 9, 9), .01);
		assertEquals(224, result.getVoxel(1, 9, 1), .01);
	}

	@Test
	public final void test_reconstructByErosion_CochleaVolumeC6() {
		// Open test image
		ImagePlus imagePlus = IJ.openImage("files/bat-cochlea-volume.tif");
		assertNotNull(imagePlus);
		assertTrue(imagePlus.getStackSize() > 0);
		ImageStack mask = imagePlus.getStack();

		// get image size
		int width = mask.getWidth();
		int height = mask.getHeight();
		int depth = mask.getSize();
		int bitDepth = mask.getBitDepth();

		// Ensure regularity of the mask
		mask = Morphology.opening(mask, CubeStrel.fromRadius(1));
		
		// invert stack image
		for(int z = 0; z < depth; z++) {
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					mask.setVoxel(x, y, z, 255 - mask.getVoxel(x, y, z));
				}
			}
		}

		// initialize marker image: 255 everywhere except a a given position (the germ)
		ImageStack marker = ImageStack.create(width, height, depth, bitDepth);
		for(int z = 0; z < depth; z++) {
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					marker.setVoxel(x, y, z, 255);
				}
			}
		}
		marker.setVoxel(20, 80, 50, 0);
		
		ImageStack result = GeodesicReconstruction3D.reconstructByErosion(marker, mask, 6);
		
		// Check images equality
		for(int z = 0; z < depth; z++) {
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					assertEquals(result.getVoxel(x, y, z),
							mask.getVoxel(x, y, z), .01);
				}
			}
		}
		
	}

	@Test
	public final void test_reconstructByErosion_CochleaVolumeC26() {
		// Open test image
		ImagePlus imagePlus = IJ.openImage("files/bat-cochlea-volume.tif");
		assertNotNull(imagePlus);
		assertTrue(imagePlus.getStackSize() > 0);
		ImageStack mask = imagePlus.getStack();

		// get image size
		int width = mask.getWidth();
		int height = mask.getHeight();
		int depth = mask.getSize();
		int bitDepth = mask.getBitDepth();

		// invert stack image
		for(int z = 0; z < depth; z++) {
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					mask.setVoxel(x, y, z, 255 - mask.getVoxel(x, y, z));
				}
			}
		}

		// initialize marker image: 255 everywhere except a a given position (the germ)
		ImageStack marker = ImageStack.create(width, height, depth, bitDepth);
		for(int z = 0; z < depth; z++) {
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					marker.setVoxel(x, y, z, 255);
				}
			}
		}
		marker.setVoxel(20, 80, 50, 0);
		
		ImageStack result = GeodesicReconstruction3D.reconstructByErosion(marker, mask, 26);
		
		// Check images equality
		for(int z = 0; z < depth; z++) {
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					assertEquals(result.getVoxel(x, y, z),
							mask.getVoxel(x, y, z), .01);
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

	private ImageStack createCubicHollowMeshImage() {
		// create filled cubic mesh
		ImageStack stack = createCubicMeshImage();

		// number of voxels between edges and 'tube' borders 
		int gap = 2;
		
		// First, the edges in the x direction
		for (int z = 5 - gap; z <= 5 + gap; z++) {
			for (int y = 5 - gap; y <= 5 + gap; y++) {
				for (int x = 5 - gap; x <= 15 + gap; x++) {
					stack.setVoxel(x, y, z, 0);
					stack.setVoxel(x, y, z+10, 0);
				}				
			}
		}
		
		// then, the edges in the y direction
		for (int z = 5 - gap; z <= 5 + gap; z++) {
			for (int x = 5 - gap; x <= 5 + gap; x++) {
				for (int y = 5 - gap; y <= 15 + gap; y++) {
					stack.setVoxel(x + 10, y, z, 0);
					stack.setVoxel(x, y, z+10, 0);
					stack.setVoxel(x+10, y, z+10, 0);
				}				
			}
		}

		// Finally, the edges in the z direction
		for (int y = 5 - gap ; y <= 5 + gap; y++) {
			for (int x = 5 - gap; x <= 5 + gap; x++) {
				for (int z = 5 - gap; z <= 15 + gap; z++) {
					stack.setVoxel(x, y+10, z, 0);
					stack.setVoxel(x+10, y+10, z, 0);
				}				
			}
		}
		
		return stack;
	}
	
	private void fillStack(ImageStack stack, int value) {
		for (int z = 0; z < stack.getSize(); z++) {
			for (int y = 0; y < stack.getHeight(); y++) {
				for (int x = 0; x < stack.getWidth(); x++) {
					stack.setVoxel(x, y, z, value);
				}
			}
		}
	}
}
