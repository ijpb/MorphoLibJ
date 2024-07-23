/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
package inra.ijpb.binary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import org.junit.Test;

public class BinaryImagesTest {
	/**
	 * Checks that the maximum number of labels is greater than 2^8 when labels
	 * are coded with byte.
	 */
	@Test
	public final void testComputeLabels_Byte() 
	{
		ImageProcessor image = new ByteProcessor(300, 300);
		for (int y = 0; y < 15; y++)
		{
			for (int x = 0; x < 15; x++)
			{
				image.set(20 * x + 1, 20 * y + 1, 255);
			}
		}
		
		ImageProcessor labels = BinaryImages.componentsLabeling(image, 4, 8);
		
		assertEquals(15*15, labels.get(281, 281), .1);
	}

	/**
	 * Checks that the maximum number of labels is greater than 2^16 when labels
	 * are coded with short.
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
		
		ImageProcessor labels = BinaryImages.componentsLabeling(image, 4, 32);
		
		assertEquals(300 * 300, labels.getf(599, 599), .1);
	}

	/**
	 * Checks that the maximum number of labels is reached when computing too
	 * many labels (using bytes).
	 */
	@Test(expected=RuntimeException.class)
	public final void testComputeLabelsManyLabels_Byte() 
	{
		// create an image with 300x300 = 90000 labels
		ImageProcessor image = new ByteProcessor(600, 600);
		for (int y = 0; y < 300; y++)
		{
			for (int x = 0; x < 300; x++)
			{
				image.set(2 * x + 1, 2 * y + 1, 255);
			}
		}
		
		// should throw an exception
		BinaryImages.componentsLabeling(image, 4, 8);
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
		ImageStack labels = BinaryImages.componentsLabeling(image, 6, 8);
		
		// check labels and empty regions
		assertEquals(0, (int) labels.getVoxel(0, 0, 0));
		assertEquals(1, (int) labels.getVoxel(2, 2, 2));
		assertEquals(0, (int) labels.getVoxel(4, 4, 4));
		assertEquals(8, (int) labels.getVoxel(7, 7, 7));
		assertEquals(0, (int) labels.getVoxel(8, 8, 8));
	}
	
	@Test
	public final void testDistanceMapImageProcessor() {
		ImageProcessor image = createBinarySquareImage();

		ImageProcessor result = BinaryImages.distanceMap(image);

		assertNotNull(result);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(3, result.get(4, 4), 1e-12);
	}

	@Test
	public final void testDistanceMapImageProcessorShortArrayBoolean() {
		ImageProcessor image = createBinarySquareImage();

		short[] weights = new short[]{3, 4};
		ImageProcessor result = BinaryImages.distanceMap(image, weights, true);

		assertNotNull(result);
		assertTrue(result instanceof ShortProcessor);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(3, result.get(4, 4), 1e-12);
	}

	@Test
	public final void testDistanceMapImageProcessorFloatArrayBoolean() {
		ImageProcessor image = createBinarySquareImage();

		float[] weights = new float[]{3f, 4f};
		ImageProcessor result = BinaryImages.distanceMap(image, weights, true);
		
//		for (int y = 0; y < image.getHeight(); y++) {
//			for (int x = 0; x < image.getWidth(); x++) {
//				System.out.print(" " + result.getf(x, y));
//			}
//			System.out.println("");
//		}

		assertNotNull(result);
		assertTrue(result instanceof FloatProcessor);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(3, result.getf(4, 4), 1e-12);
	}

	@Test
	public final void testAreaOpeningImageProcessor() 
	{
		// Create input image: four regions, with sizes 1, 5, 5, and 25
		ByteProcessor image = new ByteProcessor(10, 10);
		image.set(1, 1, 255);
		for (int i = 3; i < 8; i++) 
		{
			image.set(1, i, 255);
			image.set(i, 1, 255);
		}
		for (int y = 3; y < 8; y++) 
		{
			for (int x = 3; x < 8; x++) 
			{
				image.set(x, y, 255);
			}
		}
		
		// Remove only the first region
		ImageProcessor sizeOpen3 = BinaryImages.areaOpening(image, 3);
		assertEquals(0, sizeOpen3.get(1, 1));
		assertEquals(255, sizeOpen3.get(1, 5));
		assertEquals(255, sizeOpen3.get(5, 1));
		assertEquals(255, sizeOpen3.get(5, 5));
		
		// Remove the first 3 region
		ImageProcessor sizeOpen10 = BinaryImages.areaOpening(image, 10);
		assertEquals(0, sizeOpen10.get(1, 1));
		assertEquals(0, sizeOpen10.get(1, 5));
		assertEquals(0, sizeOpen10.get(5, 1));
		assertEquals(255, sizeOpen10.get(5, 5));
	}
	
	/**
	 * Creates a new binary image of a square.
	 * @return
	 */
	private final ImageProcessor createBinarySquareImage() {
		ByteProcessor image = new ByteProcessor(10, 10);
		image.setBackgroundValue(0);
		image.fill();
		for (int y = 2; y < 8; y++) {
			for (int x = 2; x < 8; x++) {
				image.set(x, y, 255);
			}
		}
		return image;
	}
}
