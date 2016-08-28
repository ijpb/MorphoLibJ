package inra.ijpb.morphology.geodrec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import inra.ijpb.morphology.Morphology;
import inra.ijpb.morphology.strel.CubeStrel;

import org.junit.Test;

public class GeodesicReconstruction3DHybrid0Gray16Test {

	@Test
	public final void testDilationCubicMeshC6() {
		ImageStack mask = createCubicMeshImage();
		
		ImageStack marker = ImageStack.create(20, 20, 20, 16);
		marker.setVoxel(5, 5, 5, 1000);
		
		GeodesicReconstruction3DHybrid0Gray16 algo = new GeodesicReconstruction3DHybrid0Gray16();
		algo.setConnectivity(6);

		ImageStack result = algo.applyTo(marker, mask);
		
		assertEquals(1000, result.getVoxel(5, 15, 5), .01);
		assertEquals(1000, result.getVoxel(15, 15, 15), .01);
	}

	@Test
	public final void testErosionCubicMeshC6() {
		ImageStack mask = createCubicMeshImage();
		invertGray16Stack(mask);
		
		ImageStack marker = ImageStack.create(20, 20, 20, 16);
		marker.setVoxel(5, 5, 5, 0x00FFFF);
		invertGray16Stack(marker);
		
		GeodesicReconstruction3DHybrid0Gray16 algo = new GeodesicReconstruction3DHybrid0Gray16();
		algo.setReconstructionType(GeodesicReconstructionType.BY_EROSION);
		algo.setConnectivity(6);
		
		ImageStack result = algo.applyTo(marker, mask);
		
		assertEquals(0, result.getVoxel(5, 15, 5), .01);
		assertEquals(0x00FFFF, result.getVoxel(0, 0, 0), .01);

		int sizeX = mask.getWidth();
		int sizeY = mask.getHeight();
		int sizeZ = mask.getSize();
		
		for(int z = 0; z < sizeZ; z++) {
			for(int y = 0; y < sizeY; y++) {
				for(int x = 0; x < sizeX; x++) {
					assertEquals(result.getVoxel(x, y, z),
							mask.getVoxel(x, y, z), .01);
				}
			}
		}

	}

	@Test
	public final void testDilationCubicMeshC26() {
		ImageStack mask = createCubicMeshImage();
		
		ImageStack marker = ImageStack.create(20, 20, 20, 16);
		marker.setVoxel(5, 5, 5, 0x00FFFF);
		
		GeodesicReconstruction3DHybrid0Gray16 algo = new GeodesicReconstruction3DHybrid0Gray16();
		algo.setConnectivity(26);
		
		ImageStack result = algo.applyTo(marker, mask);
		
		assertEquals(0x00FFFF, result.getVoxel(5, 15, 5), .01);
	}

	@Test
	public final void testDilationThinCubicMeshC6() {
		ImageStack mask = createThinCubicMeshImage();
		
		ImageStack marker = ImageStack.create(5, 5, 5, 16);
		marker.setVoxel(0, 0, 0, 0x00FFFF);
		
		GeodesicReconstruction3DHybrid0Gray16 algo = new GeodesicReconstruction3DHybrid0Gray16();
		algo.setConnectivity(6);
		
		ImageStack result = algo.applyTo(marker, mask);
		
		assertEquals(0x00FFFF, result.getVoxel(0, 4, 0), .01);
	}

	@Test
	public final void testDilationThinCubicMeshC26() {
		ImageStack mask = createThinCubicMeshImage();
		
		ImageStack marker = ImageStack.create(5, 5, 5, 16);
		marker.setVoxel(0, 0, 0, 0x00FFFF);
		
		GeodesicReconstruction3DHybrid0Gray16 algo = new GeodesicReconstruction3DHybrid0Gray16();
		algo.setConnectivity(26);

		ImageStack result = algo.applyTo(marker, mask);
	
		assertEquals(0x00FFFF, result.getVoxel(0, 4, 0), .01);
	}

	@Test
	public final void testDilationCubicHollowMesh() {
		ImageStack mask = createCubicHollowMeshImage();
		
		ImageStack marker = ImageStack.create(20, 20, 20, 16);
		for (int z = 0; z < 20; z++) {
			for (int y = 0; y < 20; y++) {
				for (int x = 0; x < 20; x++) {
					marker.setVoxel(x, y, z, 0x00FFFF);
				}
			}
		}
		marker.setVoxel(5, 5, 5, 0);
		
		GeodesicReconstruction3DHybrid0Gray16 algo = new GeodesicReconstruction3DHybrid0Gray16();

		ImageStack result = algo.applyTo(marker, mask);
		
		assertEquals(0, result.getVoxel(5, 15, 5), .01);
		assertStackEquals(mask, result);
	}

	@Test
	public final void testDilationHilbertCurveC6() 
	{
		String fileName = getClass().getResource("/files/hilbert3d.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		
		assertNotNull(imagePlus);
	
		assertTrue(imagePlus.getStackSize() > 0);
	
		ImageStack mask = imagePlus.getStack();
		mask = convertToGray16Stack(mask);
		
		int width = mask.getWidth();
		int height = mask.getHeight();
		int depth = mask.getSize();
		int bitDepth = mask.getBitDepth();
		ImageStack marker = ImageStack.create(width, height, depth, bitDepth);
	
		marker.setVoxel(3, 0, 0, 0x00FFFF);
	
		GeodesicReconstruction3DHybrid0Gray16 algo = new GeodesicReconstruction3DHybrid0Gray16();
		algo.setConnectivity(6);

		ImageStack result = algo.applyTo(marker, mask);
			
		for (int z = 0; z < depth; z++)
		{
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					assertEquals(result.getVoxel(x, y, z),
							mask.getVoxel(x, y, z), .01);
				}
			}
		}
	}

	@Test
	public final void testDilationHilbertCurveC26() 
	{
		String fileName = getClass().getResource("/files/hilbert3d.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		
		assertNotNull(imagePlus);
	
		assertTrue(imagePlus.getStackSize() > 0);
	
		ImageStack mask = imagePlus.getStack();
		mask = convertToGray16Stack(mask);

		int width = mask.getWidth();
		int height = mask.getHeight();
		int depth = mask.getSize();
		int bitDepth = mask.getBitDepth();
		ImageStack marker = ImageStack.create(width, height, depth, bitDepth);
	
		marker.setVoxel(3, 0, 0, 0x00FFFF);
	
		GeodesicReconstruction3DHybrid0Gray16 algo = new GeodesicReconstruction3DHybrid0Gray16();
		algo.setConnectivity(26);
		algo.verbose = false;
	
		ImageStack result = algo.applyTo(marker, mask);
		
		for (int z = 0; z < depth; z++)
		{
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					assertEquals(result.getVoxel(x, y, z), mask.getVoxel(x, y, z), .01);
				}
			}
		}
	}

	@Test
	public final void testDilationCochleaVolumeC6() 
	{
		String fileName = getClass().getResource("/files/bat-cochlea-volume.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		
		assertNotNull(imagePlus);
	
		assertTrue(imagePlus.getStackSize() > 0);
	
		ImageStack mask = imagePlus.getStack();
		mask = convertToGray16Stack(mask);
			
		// Ensure regularity of the mask
		mask = Morphology.opening(mask, CubeStrel.fromRadius(1));
		
		int width = mask.getWidth();
		int height = mask.getHeight();
		int depth = mask.getSize();
		ImageStack marker = ImageStack.create(width, height, depth, 16);
	
		marker.setVoxel(20, 80, 50, 0x00FFFF);
	
		GeodesicReconstruction3DHybrid0Gray16 algo = new GeodesicReconstruction3DHybrid0Gray16();
		algo.setConnectivity(6);
		algo.verbose = false;
	
		ImageStack result = algo.applyTo(marker, mask);
			
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
	public final void testErosionCochleaVolumeC6() {
		String fileName = getClass().getResource("/files/bat-cochlea-volume.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		
		assertNotNull(imagePlus);	
		assertTrue(imagePlus.getStackSize() > 0);
	
		ImageStack mask = imagePlus.getStack();
		// Ensure regularity of the mask
		mask = Morphology.opening(mask, CubeStrel.fromRadius(1));
		mask = convertToGray16Stack(mask);
		invertGray16Stack(mask);
		
		int width = mask.getWidth();
		int height = mask.getHeight();
		int depth = mask.getSize();
		int bitDepth = mask.getBitDepth();
		ImageStack marker = ImageStack.create(width, height, depth, bitDepth);
	
		for (int z = 0; z < depth; z++) 
		{
			for (int y = 0; y < height; y++) 
			{
				for (int x = 0; x < width; x++) 
				{
					marker.setVoxel(x, y, z, 0x00FFFF);
				}
			}
		}
		marker.setVoxel(20, 80, 50, 0);
	
		GeodesicReconstruction3DHybrid0Gray16 algo = 
				new GeodesicReconstruction3DHybrid0Gray16(GeodesicReconstructionType.BY_EROSION);
		algo.setConnectivity(6);

		ImageStack result = algo.applyTo(marker, mask);
			
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
	public final void testDilationCochleaVolumeC26() {
		String fileName = getClass().getResource("/files/bat-cochlea-volume.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		assertNotNull(imagePlus);

		assertTrue(imagePlus.getStackSize() > 0);

		ImageStack mask = imagePlus.getStack();
		mask = convertToGray16Stack(mask);
		
		int width = mask.getWidth();
		int height = mask.getHeight();
		int depth = mask.getSize();
		int bitDepth = mask.getBitDepth();
		ImageStack marker = ImageStack.create(width, height, depth, bitDepth);

		// Ensure regularity of the mask
//		mask = Morphology.closing(mask, CubeStrel.fromRadius(1));

		marker.setVoxel(20, 80, 50, 0x00FFFF);

		GeodesicReconstruction3DHybrid0Gray16 algo = new GeodesicReconstruction3DHybrid0Gray16();
		algo.setConnectivity(26);

		ImageStack result = algo.applyTo(marker, mask);
		
		for(int z = 0; z < depth; z++) {
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					if (Math.abs(result.getVoxel(x, y, z) - mask.getVoxel(x, y, z)) > .1) 
					{
						System.out.println("x=" + x + " y=" + y + " z=" + z);
						System.out.println("  mask = " + (int) mask.getVoxel(x, y, z));
						System.out.println("  res  = " + (int) result.getVoxel(x, y, z));
						assertTrue(false);
					}
				}
			}
		}
		
	}
	
	@Test
	public final void testErosionCochleaVolumeC26() {
		String fileName = getClass().getResource("/files/bat-cochlea-volume.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		
		assertNotNull(imagePlus);	
		assertTrue(imagePlus.getStackSize() > 0);
	
		ImageStack mask = imagePlus.getStack();
		mask = convertToGray16Stack(mask);
		
		// Ensure regularity of the mask
		mask = Morphology.opening(mask, CubeStrel.fromRadius(1));
		invertGray16Stack(mask);
		
		int width = mask.getWidth();
		int height = mask.getHeight();
		int depth = mask.getSize();
		int bitDepth = mask.getBitDepth();
		ImageStack marker = ImageStack.create(width, height, depth, bitDepth);
		marker.setVoxel(20, 80, 50, 0x00FFFF);
		invertGray16Stack(marker);
	
		GeodesicReconstruction3DHybrid0Gray16 algo = 
				new GeodesicReconstruction3DHybrid0Gray16(GeodesicReconstructionType.BY_EROSION);
		algo.setConnectivity(26);

		ImageStack result = algo.applyTo(marker, mask);
		
		for(int z = 0; z < depth; z++) {
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					if (Math.abs(result.getVoxel(x, y, z) - mask.getVoxel(x, y, z)) > .1) 
					{
						System.out.println("x=" + x + " y=" + y + " z=" + z);
						System.out.println("  mask = " + (int) mask.getVoxel(x, y, z));
						System.out.println("  res  = " + (int) result.getVoxel(x, y, z));
						assertTrue(false);
					}
				}
			}
		}
	}


	private ImageStack createCubicMeshImage() 
	{
		int sizeX = 20;
		int sizeY = 20;
		int sizeZ = 20;
		int bitDepth = 16;
		
		// create empty stack
		ImageStack stack = ImageStack.create(sizeX, sizeY, sizeZ, bitDepth);
		
		// number of voxels between edges and 'tube' borders 
		int gap = 2;

		// First, the edges in the x direction
		for (int z = 5 - gap - 1; z <= 5 + gap + 1; z++) {
			for (int y = 5 - gap - 1; y <= 5 + gap + 1; y++) {
				for (int x = 5 - gap - 1; x <= 15 + gap + 1; x++) {
					stack.setVoxel(x, y, z, 0x00FFFF);
					stack.setVoxel(x, y, z+10, 0x00FFFF);
				}				
			}
		}
		
		// then, the edges in the y direction
		for (int z = 5 - gap - 1; z <= 5 + gap + 1; z++) {
			for (int x = 5 - gap - 1; x <= 5 + gap + 1; x++) {
				for (int y = 5 - gap - 1; y <= 15 + gap + 1; y++) {
					stack.setVoxel(x + 10, y, z, 0x00FFFF);
					stack.setVoxel(x, y, z+10, 0x00FFFF);
					stack.setVoxel(x+10, y, z+10, 0x00FFFF);
				}				
			}
		}

		// Finally, the edges in the z direction
		for (int y = 5 - gap - 1; y <= 5 + gap + 1; y++) {
			for (int x = 5 - gap - 1; x <= 5 + gap + 1; x++) {
				for (int z = 5 - gap - 1; z <= 15 + gap + 1; z++) {
					stack.setVoxel(x, y+10, z, 0x00FFFF);
					stack.setVoxel(x+10, y+10, z, 0x00FFFF);
				}				
			}
		}
		
		return stack;
	}

	/**
	 * Creates an image of cube edges, similar to this:  
	 * 
	 *    *---------*
	 *   **        **
	 *  * *       * *
	 * ***********  *
	 * |  *      |  *
	 * |  *------|--* 
	 * | /       | *
	 * |/        |*
	 * ***********
	 * 
	 * Typical planes are as follow:
	 *   z = 0        z=1,2,3       z = 4
	 *  X * * * *    . . . . .    * * * * *
	 *  . . . . *    . . . . .    * . . . *
	 *  . . . . *    . . . . .    * . . . *
	 *  . . . . *    . . . . .    * . . . *
	 *  Z . . . *    * . . . *    * . . . *
	 *  
	 *  (reconstruction starts from the X, and terminates at the Z)
	 */
	private ImageStack createThinCubicMeshImage() {
		int sizeX = 5;
		int sizeY = 5;
		int sizeZ = 5;
		int bitDepth = 16;
		
		// create empty stack
		ImageStack stack = ImageStack.create(sizeX, sizeY, sizeZ, bitDepth);
		
		// First, the edges in the x direction
		for (int x = 0; x < 5; x++) {
			stack.setVoxel(x, 0, 0, 0x00FFFF);
			stack.setVoxel(x, 0, 4, 0x00FFFF);
		}				
		
		// then, the edges in the y direction
		for (int y = 0; y < 5; y++) {
			stack.setVoxel(4, y, 0, 0x00FFFF);
			stack.setVoxel(0, y, 4, 0x00FFFF);
			stack.setVoxel(4, y, 4, 0x00FFFF);
		}				

		// Finally, the edges in the z direction
		for (int z = 0; z < 5; z++) {
			stack.setVoxel(0, 4, z, 0x00FFFF);
			stack.setVoxel(4, 4, z, 0x00FFFF);
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
	
	private final ImageStack convertToGray16Stack(ImageStack image)
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 16);

		for (int z = 0; z < sizeZ; z++)
		{
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					result.setVoxel(x, y, z, image.getVoxel(x, y, z));
				}
			}
		}
		
		return result;
	}

	private final void invertGray16Stack(ImageStack image) {
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		int MAX_VAL = 0x00FFFF;
		
		for (int z = 0; z < sizeZ; z++)
		{
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					image.setVoxel(x, y, z, MAX_VAL - image.getVoxel(x, y, z));
				}
			}
		}
	}
	
	private final void assertStackEquals(ImageStack image, ImageStack image2) {
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		assertEquals(sizeX, image2.getWidth());
		assertEquals(sizeY, image2.getHeight());
		assertEquals(sizeZ, image2.getSize());
		
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					assertEquals(image.getVoxel(x, y, z),
							image2.getVoxel(x, y, z), .01);
				}
			}
		}
	}

}
