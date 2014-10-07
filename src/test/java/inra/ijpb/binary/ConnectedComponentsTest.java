package inra.ijpb.binary;

import static org.junit.Assert.*;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.binary.ConnectedComponents;

import org.junit.Test;

public class ConnectedComponentsTest {

	/**
	 * Checks that the maximum number of labels is greater than 2^16.
	 */
	@Test
	public final void testComputeLabelsManyLabels() 
	{
		ImageProcessor image = new ByteProcessor(600, 600);
		for (int y = 0; y < 300; y++)
		{
			for (int x = 0; x < 300; x++)
			{
				image.set(2 * x + 1, 2 * y + 1, 255);
			}
		}
		
		ImageProcessor labels = ConnectedComponents.computeLabels(image, 4, 32);
		
		assertEquals(300 * 300, labels.getf(599, 599), .1);
	}

	/**
	 * Test labeling algorithm on 3D stack.
	 */
	@Test
	public final void testComputeLabelsByteImageStack()
	{
		// load the reference image, that contains height cubes with size 2x2x2
		String fileName = getClass().getResource("/files/matrix2x2x2.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		assertNotNull(imagePlus);
		assertTrue(imagePlus.getStackSize() > 0);

		// compute labels of the binary image
		ImageStack image = imagePlus.getStack();
		ImageStack labels = ConnectedComponents.computeLabels(image, 6, 8);
		
		// check labels and empty regions
		assertEquals(0, (int) labels.getVoxel(0, 0, 0));
		assertEquals(1, (int) labels.getVoxel(2, 2, 2));
		assertEquals(0, (int) labels.getVoxel(4, 4, 4));
		assertEquals(8, (int) labels.getVoxel(7, 7, 7));
		assertEquals(0, (int) labels.getVoxel(8, 8, 8));
	}

}
