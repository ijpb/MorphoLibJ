package inra.ijpb.morphology.geodrec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import inra.ijpb.morphology.geodrec.GeodesicReconstructionByErosion3DGray8;

import org.junit.Test;

public class GeodesicReconstructionByErosion3DGray8Test {

	@Test
	public final void testApplyTo() {
		// Open test image
		String fileName = getClass().getResource("/files/bat-cochlea-volume.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
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
		
		// create reconstruction algorithm
		GeodesicReconstructionByErosion3DGray8 algo = new GeodesicReconstructionByErosion3DGray8();
		algo.verbose = true;

		// run algo and compute elapsed time
		long t0 = System.currentTimeMillis();
		ImageStack result = algo.applyTo(marker, mask);
		long t1 = System.currentTimeMillis();

		double dt = (t1 - t0) / 1000.0;
		System.out.println("Elapsed time: " + dt + " s");
		
		// Check images equality
		for(int z = 0; z < depth; z++) {
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					assertEquals(result.getVoxel(x, y, z),
							mask.getVoxel(x, y, z), .01);
//					double vRes = result.getVoxel(x, y, z);
//					double vMask = mask.getVoxel(x, y, z);
//					assertEquals(String.format(Locale.ENGLISH, "At position (%d,%d;%d)", x, y, z), 
//							vRes, vMask, .01);
				}
			}
		}
		
	}

}
