package inra.ijpb.label;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.awt.Color;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.label.LabelImages;
import inra.ijpb.util.ColorMaps.CommonLabelMaps;

import org.junit.Assert;
import org.junit.Test;

public class LabelImagesTest
{
	@Test
	public final void testLabelToRGB_ImageProcessorByteArrayColor()
	{
		// create a byte processor containing four labels
		ImageProcessor image = new ByteProcessor(10, 10);
		for (int y = 0; y < 3; y++)
		{
			for (int x = 0; x < 3; x++)
			{
				image.set(x + 1, y + 1, 1);
				image.set(x + 5, y + 1, 2);
				image.set(x + 1, y + 5, 3);
				image.set(x + 5, y + 5, 4);
			}
		}
		
		// create LUT and background color
		byte[][] lut = CommonLabelMaps.GOLDEN_ANGLE.computeLut(4, false);
		Color bgColor = Color.WHITE;
		
		// compute color image from labels
		ColorProcessor colorImage = LabelImages.labelToRgb(image, lut, bgColor);

		Assert.assertNotEquals(0, colorImage.get(2, 2));
		Assert.assertNotEquals(0, colorImage.get(6, 2));
		Assert.assertNotEquals(0, colorImage.get(2, 6));
		Assert.assertNotEquals(0, colorImage.get(6, 6));
	}

	@Test
	public final void testFindAllLabelsImageProcessor()
	{
		String fileName = getClass().getResource("/files/blobs-lbl.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		ImageProcessor image = imagePlus.getProcessor();

		int[] labels = LabelImages.findAllLabels(image);
		for (int i = 0; i < labels.length; i++)
		{
			assertFalse(labels[i] == 0);
			assertEquals(i + 1, labels[i]);
		}
	}

	@Test
	public final void testFindAllLabels_FloatProcessor()
	{
		String fileName = getClass().getResource("/files/blobs-lbl32.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		ImageProcessor image = imagePlus.getProcessor();

		int[] labels = LabelImages.findAllLabels(image);
		for (int i = 0; i < labels.length; i++)
		{
			assertFalse(labels[i] == 0);
			assertEquals(i + 1, labels[i]);
		}
	}

	@Test
	public final void testKeepLargestLabelImageProcessor()
	{
		String fileName = getClass().getResource("/files/blobs-lbl.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		ImageProcessor image = imagePlus.getProcessor();

		ImageProcessor largestLabel = LabelImages.keepLargestLabel(image);
		
		// background pixel should remain unchanged
		assertEquals(0, largestLabel.get(0, 0));
		// small labels should disappear
		assertEquals(0, largestLabel.get(204, 204));
		// largest label should be set to true
		assertEquals(255, largestLabel.get(90, 160));
	}


	/**
	 * Should thrown an exception
	 */
	@Test(expected=RuntimeException.class)
	public final void testKeepLargestLabelImageProcessor_EmptyImage()
	{
		ImageProcessor image = new ByteProcessor(100, 100);
		@SuppressWarnings("unused")
		ImageProcessor largestLabel = LabelImages.keepLargestLabel(image);
	}

	/**
	 * Should thrown an exception
	 */
	@Test(expected=RuntimeException.class)
	public final void testKeepLargestLabelImageStack_EmptyImage()
	{
		ImageStack image = ImageStack.create(20, 20, 20, 8);
		@SuppressWarnings("unused")
		ImageStack largestLabel = LabelImages.keepLargestLabel(image);
	}

	@Test
	public final void testRemoveLargestLabelImageProcessor()
	{
		String fileName = getClass().getResource("/files/blobs-lbl.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		ImageProcessor image = imagePlus.getProcessor();

		LabelImages.removeLargestLabel(image);
		// background pixel should remain unchanged
		assertEquals(0, image.get(0, 0));
		// small labels should remain unchanged
		assertEquals(50, image.get(204, 204));
		// largest label should disappear
		assertEquals(0, image.get(90, 160));
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
