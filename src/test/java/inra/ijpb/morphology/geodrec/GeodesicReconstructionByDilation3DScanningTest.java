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

public class GeodesicReconstructionByDilation3DScanningTest {

	@Test
	public final void testCubicMeshC26() {
		ImageStack mask = createCubicMeshImage();
		
		ImageStack marker = ImageStack.create(20, 20, 20, 8);
		marker.setVoxel(5, 5, 5, 255);
		
		GeodesicReconstructionByDilation3DScanning algo = new GeodesicReconstructionByDilation3DScanning();
		algo.setConnectivity(26);
//		algo.verbose = true;
		
		long t0 = System.currentTimeMillis();
		ImageStack result = algo.applyTo(marker, mask);
		long t1 = System.currentTimeMillis();
		double dt = (t1 - t0) / 1000.0;
		System.out.println("Elapsed time: " + dt + " s");
		
		assertEquals(255, result.getVoxel(5, 15, 5), .01);
	}

	@Test
	public final void testCubicMeshC6() {
		ImageStack mask = createCubicMeshImage();
		
		ImageStack marker = ImageStack.create(20, 20, 20, 8);
		marker.setVoxel(5, 5, 5, 255);
		
		GeodesicReconstructionByDilation3DScanning algo = new GeodesicReconstructionByDilation3DScanning();
		algo.setConnectivity(6);
//		algo.verbose = true;
		
		long t0 = System.currentTimeMillis();
		ImageStack result = algo.applyTo(marker, mask);
		long t1 = System.currentTimeMillis();
		double dt = (t1 - t0) / 1000.0;
		System.out.println("Elapsed time: " + dt + " s");
		
		assertEquals(255, result.getVoxel(5, 15, 5), .01);
	}

	@Test
	public final void testCubicHollowMesh() {
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
		
		GeodesicReconstructionByDilation3DScanning algo = new GeodesicReconstructionByDilation3DScanning();
//		algo.verbose = true;
		
		long t0 = System.currentTimeMillis();
		ImageStack result = algo.applyTo(marker, mask);
		long t1 = System.currentTimeMillis();
		double dt = (t1 - t0) / 1000.0;
		System.out.println("Elapsed time: " + dt + " s");
		
		assertEquals(0, result.getVoxel(5, 15, 5), .01);
	}

	@Test
	public final void testCochleaVolumeC26() {
		String fileName = getClass().getResource("/files/bat-cochlea-volume.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		assertNotNull(imagePlus);

		assertTrue(imagePlus.getStackSize() > 0);

		ImageStack mask = imagePlus.getStack();
		int width = mask.getWidth();
		int height = mask.getHeight();
		int depth = mask.getSize();
		int bitDepth = mask.getBitDepth();
		ImageStack marker = ImageStack.create(width, height, depth, bitDepth);

		marker.setVoxel(20, 80, 50, 255);

		GeodesicReconstructionByDilation3DScanning algo = new GeodesicReconstructionByDilation3DScanning();
		algo.setConnectivity(26);
		algo.verbose = false;

		long t0 = System.currentTimeMillis();
		ImageStack result = algo.applyTo(marker, mask);
		long t1 = System.currentTimeMillis();

		double dt = (t1 - t0) / 1000.0;
		System.out.println("Elapsed time: " + dt + " s");
		
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
	public final void compareTimingC6() {
		String fileName = getClass().getResource("/files/bat-cochlea-volume.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);		
		assertNotNull(imagePlus);
		ImageStack mask = imagePlus.getStack();
		
		// Ensure regularity of the mask
		mask = Morphology.opening(mask, CubeStrel.fromRadius(1));

		// create marker image
		int width = mask.getWidth();
		int height = mask.getHeight();
		int depth = mask.getSize();
		int bitDepth = mask.getBitDepth();
		ImageStack marker = ImageStack.create(width, height, depth, bitDepth);
		marker.setVoxel(20, 80, 50, 255);

		long t0, t1;
		double dt;
		
		GeodesicReconstructionByDilation3DScanning algo1 = new GeodesicReconstructionByDilation3DScanning();
		algo1.setConnectivity(6);
		algo1.verbose = false;

		t0 = System.currentTimeMillis();
		algo1.applyTo(marker, mask);
		t1 = System.currentTimeMillis();

		dt = (t1 - t0) / 1000.0;
		System.out.println("Algo scanning C6 int: " + dt + " s");
		
		GeodesicReconstructionByDilation3DGray8Scanning algo2 = new GeodesicReconstructionByDilation3DGray8Scanning();
		algo2.setConnectivity(6);
		algo2.verbose = false;

		t0 = System.currentTimeMillis();
		algo2.applyTo(marker, mask);
		t1 = System.currentTimeMillis();

		dt = (t1 - t0) / 1000.0;
		System.out.println("Algo scanning C6 gray8: " + dt + " s");
		
		marker = marker.convertToFloat();
		mask = mask.convertToFloat();

		t0 = System.currentTimeMillis();
		algo1.applyTo(marker, mask);
		t1 = System.currentTimeMillis();

		dt = (t1 - t0) / 1000.0;
		System.out.println("Algo scanning C6 double: " + dt + " s");
	}
	
	@Test
	public final void compareTimingC26() {
		String fileName = getClass().getResource("/files/bat-cochlea-volume.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);		
		assertNotNull(imagePlus);
		ImageStack mask = imagePlus.getStack();
		
		// Ensure regularity of the mask
		mask = Morphology.opening(mask, CubeStrel.fromRadius(1));

		// create marker image
		int width = mask.getWidth();
		int height = mask.getHeight();
		int depth = mask.getSize();
		int bitDepth = mask.getBitDepth();
		ImageStack marker = ImageStack.create(width, height, depth, bitDepth);
		marker.setVoxel(20, 80, 50, 255);

		GeodesicReconstructionByDilation3DScanning algo1 = new GeodesicReconstructionByDilation3DScanning();
		algo1.setConnectivity(26);
		algo1.verbose = false;

		long t0 = System.currentTimeMillis();
		algo1.applyTo(marker, mask);
		long t1 = System.currentTimeMillis();

		double dt = (t1 - t0) / 1000.0;
		System.out.println("Algo scanning C26 int: " + dt + " s");
		
		GeodesicReconstruction3DAlgo algo2 = new GeodesicReconstructionByDilation3DGray8Scanning();
		algo2.setConnectivity(26);
//		algo2.verbose = false;

		t0 = System.currentTimeMillis();
		algo2.applyTo(marker, mask);
		t1 = System.currentTimeMillis();

		dt = (t1 - t0) / 1000.0;
		System.out.println("Algo scanning C26 gray8: " + dt + " s");
		
		marker = marker.convertToFloat();
		mask = mask.convertToFloat();
		
		t0 = System.currentTimeMillis();
		algo1.applyTo(marker, mask);
		t1 = System.currentTimeMillis();

		dt = (t1 - t0) / 1000.0;
		System.out.println("Algo scanning C26 double: " + dt + " s");
		
	}



	@Test
	public final void testCochleaVolumeC6() {
		String fileName = getClass().getResource("/files/bat-cochlea-volume.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		
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

		GeodesicReconstructionByDilation3DScanning algo = new GeodesicReconstructionByDilation3DScanning();
		algo.setConnectivity(6);
		algo.verbose = false;

		long t0 = System.currentTimeMillis();
		ImageStack result = algo.applyTo(marker, mask);
		long t1 = System.currentTimeMillis();

		double dt = (t1 - t0) / 1000.0;
		System.out.println("Elapsed time: " + dt + " s");
		
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
}
