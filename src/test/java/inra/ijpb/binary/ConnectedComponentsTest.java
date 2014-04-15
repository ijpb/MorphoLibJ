package inra.ijpb.binary;

import static org.junit.Assert.*;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import inra.ijpb.binary.ConnectedComponents;

import org.junit.Test;

public class ConnectedComponentsTest {

	@Test
	public final void testComputeLabelsByteImageStack() {
		String fileName = getClass().getResource("/files/matrix2x2x2.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		assertNotNull(imagePlus);
		assertTrue(imagePlus.getStackSize() > 0);

		// load the reference image, and get its size
		ImageStack image = imagePlus.getStack();

		ImageStack labels = ConnectedComponents.computeLabels(image, 6, 8);
		
		assertEquals(0, (int) labels.getVoxel(0, 0, 0));
		assertEquals(1, (int) labels.getVoxel(2, 2, 2));
		assertEquals(0, (int) labels.getVoxel(4, 4, 4));
		assertEquals(8, (int) labels.getVoxel(7, 7, 7));
		assertEquals(0, (int) labels.getVoxel(8, 8, 8));
	}

}
