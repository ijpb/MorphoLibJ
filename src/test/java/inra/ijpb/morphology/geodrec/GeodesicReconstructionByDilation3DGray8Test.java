package inra.ijpb.morphology.geodrec;

import static org.junit.Assert.*;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import inra.ijpb.morphology.geodrec.GeodesicReconstructionByDilation3DGray8;

import org.junit.Test;

public class GeodesicReconstructionByDilation3DGray8Test {

	@Test
	public final void testApplyTo() {
		GeodesicReconstructionByDilation3DGray8 algo = new GeodesicReconstructionByDilation3DGray8();

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

}
