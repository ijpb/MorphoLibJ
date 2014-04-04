package inra.ijpb.morphology;

import static org.junit.Assert.*;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

import org.junit.Test;

public class LabelImagesTest {

	@Test
	public final void testFindAllLabelsImageProcessor() {
		String fileName = getClass().getResource("/files/blobs-lbl.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		ImageProcessor image = imagePlus.getProcessor();
		
		int[] labels = LabelImages.findAllLabels(image);
		for (int i = 0; i < labels.length; i++)
			assertFalse(labels[i] == 0);
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

}
