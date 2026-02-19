/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2026 INRA.
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
package inra.ijpb.label;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.awt.Color;

import org.junit.Assert;
import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.color.ColorMaps.CommonLabelMaps;
import inra.ijpb.data.image.ImageUtils;

public class LabelImagesTest
{
    @Test
    public final void test_cropLabel_ImagePlus2D()
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
        ImagePlus imagePlus = new ImagePlus("image", image);
        
        ImagePlus cropPlus = LabelImages.cropLabel(imagePlus, 4, 1);

        assertEquals(5, cropPlus.getWidth());
        assertEquals(5, cropPlus.getHeight());

        Calibration calib = cropPlus.getCalibration();
        assertEquals(4, calib.xOrigin, 0.01);
        assertEquals(4, calib.yOrigin, 0.01);
    }
    
    @Test
    public final void test_cropLabel_ImagePlus3D()
    {
        // create a byte processor containing four labels
        ImageStack image = ImageStack.create(8, 8, 8, 8);
        for (int z = 0; z < 3; z++)
        {
            for (int y = 0; y < 3; y++)
            {
                for (int x = 0; x < 3; x++)
                {
                    image.setVoxel(x + 1, y + 1, z + 1, 1);
                    image.setVoxel(x + 5, y + 1, z + 1, 2);
                    image.setVoxel(x + 1, y + 5, z + 1, 3);
                    image.setVoxel(x + 5, y + 5, z + 1, 4);
                    image.setVoxel(x + 1, y + 1, z + 5, 5);
                    image.setVoxel(x + 5, y + 1, z + 5, 6);
                    image.setVoxel(x + 1, y + 5, z + 5, 7);
                    image.setVoxel(x + 5, y + 5, z + 5, 8);
                }
            }
        }
        ImagePlus imagePlus = new ImagePlus("image", image);
        
        ImagePlus cropPlus = LabelImages.cropLabel(imagePlus, 8, 1);
        assertEquals(5, cropPlus.getWidth());
        assertEquals(5, cropPlus.getHeight());
        assertEquals(5, cropPlus.getImageStackSize());
        
        Calibration calib = cropPlus.getCalibration();
        assertEquals(4, calib.xOrigin, 0.01);
        assertEquals(4, calib.yOrigin, 0.01);
        assertEquals(4, calib.zOrigin, 0.01);
    }
    
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
	public final void testFindAllLabels_ByteProcessor()
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

	/**
	 * We want labels in increasing order, whatever the order they are detected
	 * in the image.
	 */
	@Test
	public final void testFindAllLabels_Byte_IncreasingOrder()
	{
		ByteProcessor image = new ByteProcessor(6, 6);
		image.set(1, 1, 8);
		image.set(3, 1, 6);
		image.set(4, 1, 6);
		
		image.set(1, 3, 4);
		image.set(3, 3, 2);
		image.set(3, 3, 2);
		image.set(1, 4, 4);
		image.set(3, 4, 2);
		image.set(4, 4, 2);

		int[] labels = LabelImages.findAllLabels(image);
		int[] expectedLabels = new int[]{2, 4, 6, 8};
		for (int i = 0; i < labels.length; i++)
		{
			assertFalse(labels[i] == 0);
			assertEquals(expectedLabels[i], labels[i]);
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

	
    @Test
    public final void testMergeLabelsWithGap_ImageProcessor()
    {
        int[][] data = new int[][] {
            {2, 2, 2, 2, 0, 3, 3, 3, 3}, 
            {2, 2, 2, 2, 0, 3, 3, 3, 3}, 
            {2, 2, 2, 0, 6, 0, 3, 3, 3}, 
            {2, 2, 0, 6, 6, 6, 0, 3, 3}, 
            {0, 0, 6, 6, 6, 6, 6, 0, 0}, 
            {5, 5, 0, 6, 6, 6, 0, 8, 8}, 
            {5, 5, 5, 0, 6, 0, 8, 8, 8}, 
            {5, 5, 5, 5, 0, 8, 8, 8, 8}, 
            {5, 5, 5, 5, 0, 8, 8, 8, 8}, 
        };
        
        ImageProcessor labelImage = new ByteProcessor(9, 9);
        for (int y = 0; y < 9; y++)
        {
            for (int x = 0; x < 9; x++)
            {
                labelImage.set(x, y, data[y][x]);
            }
        }
        
        float[] labels = new float[] {3.0f, 5.0f, 6.0f};
        float refLabel = 3.0f;
        LabelImages.mergeLabelsWithGap(labelImage, labels, refLabel, 4);

        assertEquals(2, labelImage.get(0, 0));
        assertEquals(8, labelImage.get(8, 8));

        assertEquals(3, labelImage.get(8, 0));
        assertEquals(3, labelImage.get(4, 4));
        assertEquals(3, labelImage.get(0, 8));

        assertEquals(3, labelImage.get(5, 2));
        assertEquals(3, labelImage.get(2, 5));
    }

    @Test
    public final void testMergeLabelsWithGap_3D_cubic()
    {
        ImageStack image = ImageStack.create(9, 9, 9, 8);
        for (int z = 0; z < 4; z++)
        {
            for (int y = 0; y < 4; y++)
            {
                for (int x = 0; x < 4; x++)
                {
                    image.setVoxel(x, y, z, 2);
                    image.setVoxel(x + 5, y, z, 3);
                    image.setVoxel(x, y + 5, z, 5);
                    image.setVoxel(x + 5, y + 5, z, 7);
                    image.setVoxel(x, y, z + 5, 8);
                    image.setVoxel(x + 5, y, z + 5, 10);
                    image.setVoxel(x, y + 5, z + 5, 11);
                    image.setVoxel(x + 5, y + 5, z + 5, 12);
                }
            }
        }

        float[] labels = new float[] {3.0f, 5.0f, 7.0f};
        float refLabel = 3.0f;
        LabelImages.mergeLabelsWithGap(image, labels, refLabel, 6);

        assertEquals(2, image.getVoxel(0, 0, 0), .01);
        assertEquals(12, image.getVoxel(8, 8, 8), .01);

        assertEquals(3, image.getVoxel(7, 0, 0), .01);
        assertEquals(3, image.getVoxel(0, 7, 0), .01);
        assertEquals(3, image.getVoxel(7, 7, 0), .01);

        // should remove boundary between labels 2-3 and 3-5
        assertEquals(3, image.getVoxel(7, 5, 0), .01);
        assertEquals(3, image.getVoxel(5, 7, 0), .01);

        // should keep boundary with region with label 2
        assertEquals(0, image.getVoxel(4, 0, 0), .01);
        assertEquals(0, image.getVoxel(0, 4, 0), .01);
    }

	@Test
	public final void testDilateLabels_FourSquares()
	{
		ImageProcessor labelMap = new ByteProcessor(8, 8);
		labelMap.set(3, 3, 3);
		labelMap.set(4, 3, 4);
		labelMap.set(3, 4, 7);
		labelMap.set(4, 4, 8);
		
		ImageProcessor res = LabelImages.dilateLabels(labelMap, 10);
		
		assertEquals(8, res.getWidth());
		assertEquals(8, res.getHeight());
		assertEquals(3, res.get(0, 0));
		assertEquals(4, res.get(7, 0));
		assertEquals(7, res.get(0, 7));
		assertEquals(8, res.get(7, 7));
	}

    @Test
    public final void testApplyLut_2D()
    {
        ImageProcessor labelMap = new ByteProcessor(12, 10);
        ImageUtils.fillRect(labelMap, 1, 1, 4, 3, 2);
        ImageUtils.fillRect(labelMap, 6, 1, 4, 3, 3);
        ImageUtils.fillRect(labelMap, 1, 5, 4, 3, 6);
        ImageUtils.fillRect(labelMap, 6, 5, 4, 3, 7);
        double[] values = new double[] {2.3, 4.5, 6.7, 8.9};
        
        ImageProcessor res = LabelImages.applyLut(labelMap, values);
        
        // check result size
        assertEquals(12, res.getWidth());
        assertEquals(10, res.getHeight());
        
        // check propagation of labels
        assertEquals(2.3, res.getf( 2, 2), 0.01);
        assertEquals(4.5, res.getf( 7, 2), 0.01);
        assertEquals(6.7, res.getf( 2, 6), 0.01);
        assertEquals(8.9, res.getf( 7, 6), 0.01);
    }
    
    @Test
    public final void testApplyLut_3D()
    {
        ImageStack labelMap = ImageStack.create(12, 10, 8, 8);
        ImageUtils.fillRect3d(labelMap, 1, 1, 2, 4, 3, 3, 2);
        ImageUtils.fillRect3d(labelMap, 6, 1, 2, 4, 3, 3, 3);
        ImageUtils.fillRect3d(labelMap, 1, 5, 2, 4, 3, 3, 6);
        ImageUtils.fillRect3d(labelMap, 6, 5, 2, 4, 3, 3, 7);
        double[] values = new double[] {2.3, 4.5, 6.7, 8.9};
        
        ImageStack res = LabelImages.applyLut(labelMap, values);
        
        // check result size
        assertEquals(12, res.getWidth());
        assertEquals(10, res.getHeight());
        assertEquals(8, res.getSize());
        
        // check propagation of labels
        assertEquals(2.3, res.getVoxel(2, 2, 2), 0.01);
        assertEquals(4.5, res.getVoxel(7, 2, 2), 0.01);
        assertEquals(6.7, res.getVoxel(2, 6, 2), 0.01);
        assertEquals(8.9, res.getVoxel(7, 6, 2), 0.01);
    }
    
	@Test
	public final void testDilateLabels_SeparatedLabels()
	{
		ImageProcessor labelMap = new ByteProcessor(20, 20);
		labelMap.set( 8,  8, 3);
		labelMap.set(12,  8, 4);
		labelMap.set( 8, 12, 7);
		labelMap.set(12, 12, 8);
		
		ImageProcessor res = LabelImages.dilateLabels(labelMap, 5);
		
		// check result size
		assertEquals(20, res.getWidth());
		assertEquals(20, res.getHeight());
		
		// check propagation of labels
		assertEquals(3, res.get( 5,  5));
		assertEquals(4, res.get(15,  5));
		assertEquals(7, res.get( 5, 15));
		assertEquals(8, res.get(15, 15));
		
		// corners should have value 0 due to limitation in distance
		assertEquals(0, res.get( 0,  0));
		assertEquals(0, res.get(19,  0));
		assertEquals(0, res.get( 0, 19));
		assertEquals(0, res.get(19, 19));
	}
	
	@Test
	public final void testReplaceLabels_2D_int()
	{
		ImageProcessor labelMap = new ByteProcessor(20, 20);
		labelMap.set( 8,  8, 3);
		labelMap.set(12,  8, 4);
		labelMap.set( 8, 12, 7);
		labelMap.set(12, 12, 8);
		
		LabelImages.replaceLabels(labelMap, new int[] {3, 7}, 4);
		
		// check propagation of labels
		assertEquals(4, labelMap.get( 8,  8));
		assertEquals(4, labelMap.get(12,  8));
		assertEquals(4, labelMap.get( 8, 12));
		assertEquals(8, labelMap.get(12, 12));
	}
	
	@Test
	public final void testReplaceLabels_2D_float()
	{
		ImageProcessor labelMap = new FloatProcessor(20, 20);
		labelMap.setf( 8,  8, 3);
		labelMap.setf(12,  8, 4);
		labelMap.setf( 8, 12, 7);
		labelMap.setf(12, 12, 8);
		
		LabelImages.replaceLabels(labelMap, new float[] {3f, 7f}, 4f);
		
		// check propagation of labels
		assertEquals(4, labelMap.getf( 8,  8), 0.01);
		assertEquals(4, labelMap.getf(12,  8), 0.01);
		assertEquals(4, labelMap.getf( 8, 12), 0.01);
		assertEquals(8, labelMap.getf(12, 12), 0.01);
	}
	
	@Test
	public final void testReplaceLabels_3D_int()
	{
		ImageStack labelMap = ImageStack.create(10, 10, 10, 8);
		labelMap.setVoxel( 4, 4, 4,  3);
		labelMap.setVoxel( 6, 4, 4,  4);
		labelMap.setVoxel( 4, 6, 4,  7);
		labelMap.setVoxel( 6, 6, 4,  8);
		labelMap.setVoxel( 4, 4, 6, 10);
		labelMap.setVoxel( 6, 4, 6, 11);
		labelMap.setVoxel( 4, 6, 6, 15);
		labelMap.setVoxel( 6, 6, 6, 16);
		
		LabelImages.replaceLabels(labelMap, new int[] {3, 7, 11}, 4);
		
		// check propagation of labels
		assertEquals( 4, labelMap.getVoxel( 4, 4, 4), 0.01);
		assertEquals( 4, labelMap.getVoxel( 6, 4, 4), 0.01);
		assertEquals( 4, labelMap.getVoxel( 4, 6, 4), 0.01);
		assertEquals( 8, labelMap.getVoxel( 6, 6, 4), 0.01);
		assertEquals(10, labelMap.getVoxel( 4, 4, 6), 0.01);
		assertEquals( 4, labelMap.getVoxel( 6, 4, 6), 0.01);
		assertEquals(15, labelMap.getVoxel( 4, 6, 6), 0.01);
		assertEquals(16, labelMap.getVoxel( 6, 6, 6), 0.01);
	}
	
	@Test
	public final void testReplaceLabels_3D_float()
	{
		ImageStack labelMap = ImageStack.create(10, 10, 10, 8);
		labelMap.setVoxel( 4, 4, 4,  3);
		labelMap.setVoxel( 6, 4, 4,  4);
		labelMap.setVoxel( 4, 6, 4,  7);
		labelMap.setVoxel( 6, 6, 4,  8);
		labelMap.setVoxel( 4, 4, 6, 10);
		labelMap.setVoxel( 6, 4, 6, 11);
		labelMap.setVoxel( 4, 6, 6, 15);
		labelMap.setVoxel( 6, 6, 6, 16);
		
		LabelImages.replaceLabels(labelMap, new float[] {3f, 7f, 11f}, 4f);
		
		// check propagation of labels
		assertEquals( 4, labelMap.getVoxel( 4, 4, 4), 0.01);
		assertEquals( 4, labelMap.getVoxel( 6, 4, 4), 0.01);
		assertEquals( 4, labelMap.getVoxel( 4, 6, 4), 0.01);
		assertEquals( 8, labelMap.getVoxel( 6, 6, 4), 0.01);
		assertEquals(10, labelMap.getVoxel( 4, 4, 6), 0.01);
		assertEquals( 4, labelMap.getVoxel( 6, 4, 6), 0.01);
		assertEquals(15, labelMap.getVoxel( 4, 6, 6), 0.01);
		assertEquals(16, labelMap.getVoxel( 6, 6, 6), 0.01);
	}
	
}
