package inra.ijpb.morphology;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.label.LabelImages;

import org.junit.Test;

public class LabelImagesTest {

	@Test
	public final void testFindAllLabelsImageProcessor() {
		String fileName = getClass().getResource("/files/blobs-lbl.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		ImageProcessor image = imagePlus.getProcessor();
		
		int[] labels = LabelImages.findAllLabels(image);
		for (int i = 0; i < labels.length; i++) {
			assertFalse(labels[i] == 0);
			assertEquals(i + 1, labels[i]);
		}
	}

	@Test
	public final void testFindAllLabels_FloatProcessor() {
		String fileName = getClass().getResource("/files/blobs-lbl32.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		ImageProcessor image = imagePlus.getProcessor();
		
		int[] labels = LabelImages.findAllLabels(image);
		for (int i = 0; i < labels.length; i++) {
			assertFalse(labels[i] == 0);
			assertEquals(i + 1, labels[i]);
		}
	}

	@Test
	public final void testRemoveLargestLabelImageProcessor() {
		String fileName = getClass().getResource("/files/blobs-lbl.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		ImageProcessor image = imagePlus.getProcessor();
		
		ImageProcessor largestLabel = LabelImages.keepLargestLabel(image);
		assertEquals(0, largestLabel.get(0, 0));
		assertEquals(0, largestLabel.get(200, 200));
		assertEquals(255, largestLabel.get(90, 160));
	}

	@Test
	public final void testAreaOpeningImageProcessor() 
	{
		// Create input image: four regions, with sizes 1, 5, 5, and 25
		ByteProcessor image = new ByteProcessor(10, 10);
		image.set(1, 1, 1);
		for (int i = 3; i < 8; i++) 
		{
			image.set(i, 1, 2);
			image.set(1, i, 3);
		}
		for (int y = 3; y < 8; y++) 
		{
			for (int x = 3; x < 8; x++) 
			{
				image.set(x, y, 4);
			}
		}
		
		// Remove only the first region
		ImageProcessor sizeOpen3 = LabelImages.areaOpening(image, 3);
		assertEquals(0, sizeOpen3.get(1, 1));
		assertEquals(2, sizeOpen3.get(5, 1));
		assertEquals(3, sizeOpen3.get(1, 5));
		assertEquals(4, sizeOpen3.get(5, 5));
		
		// Remove the first 3 region
		ImageProcessor sizeOpen10 = LabelImages.areaOpening(image, 10);
		assertEquals(0, sizeOpen10.get(1, 1));
		assertEquals(0, sizeOpen10.get(1, 5));
		assertEquals(0, sizeOpen10.get(5, 1));
		assertEquals(4, sizeOpen10.get(5, 5));
	}

}
