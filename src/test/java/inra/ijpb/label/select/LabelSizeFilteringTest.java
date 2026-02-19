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
/**
 * 
 */
package inra.ijpb.label.select;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.label.LabelImages;

/**
 * @author dlegland
 *
 */
public class LabelSizeFilteringTest
{
	/**
	 * Test method for
	 * {@link inra.ijpb.label.select.LabelSizeFiltering#process(ij.process.ImageProcessor)}.
	 */
	@Test
	public void testProcessImageProcessor_EQ()
	{
		ByteProcessor image = createLabelImage();
		ImageProcessor result = new LabelSizeFiltering(RelationalOperator.EQ, 5).process(image);
		int[] labels = LabelImages.findAllLabels(result);
		assertEquals(2, labels.length);
	}
	
	/**
	 * Test method for
	 * {@link inra.ijpb.label.select.LabelSizeFiltering#process(ij.process.ImageProcessor)}.
	 */
	@Test
	public void testProcessImageProcessor_NE()
	{
		ByteProcessor image = createLabelImage();
		ImageProcessor result = new LabelSizeFiltering(RelationalOperator.NE, 5).process(image);
		int[] labels = LabelImages.findAllLabels(result);
		assertEquals(2, labels.length);
	}
	
	/**
	 * Test method for
	 * {@link inra.ijpb.label.select.LabelSizeFiltering#process(ij.process.ImageProcessor)}.
	 */
	@Test
	public void testProcessImageProcessor_GT()
	{
		ByteProcessor image = createLabelImage();
		ImageProcessor result = new LabelSizeFiltering(RelationalOperator.GT, 5).process(image);
		int[] labels = LabelImages.findAllLabels(result);
		assertEquals(1, labels.length);
	}
	
	/**
	 * Test method for
	 * {@link inra.ijpb.label.select.LabelSizeFiltering#process(ij.process.ImageProcessor)}.
	 */
	@Test
	public void testProcessImageProcessor_GE()
	{
		ByteProcessor image = createLabelImage();
		ImageProcessor result = new LabelSizeFiltering(RelationalOperator.GE, 5).process(image);
		int[] labels = LabelImages.findAllLabels(result);
		assertEquals(3, labels.length);
	}
	
	/**
	 * Test method for
	 * {@link inra.ijpb.label.select.LabelSizeFiltering#process(ij.process.ImageProcessor)}.
	 */
	@Test
	public void testProcessImageProcessor_LT()
	{
		ByteProcessor image = createLabelImage();
		ImageProcessor result = new LabelSizeFiltering(RelationalOperator.LT, 5).process(image);
		int[] labels = LabelImages.findAllLabels(result);
		assertEquals(1, labels.length);
	}
	
	/**
	 * Test method for
	 * {@link inra.ijpb.label.select.LabelSizeFiltering#process(ij.process.ImageProcessor)}.
	 */
	@Test
	public void testProcessImageProcessor_LE()
	{
		ByteProcessor image = createLabelImage();
		ImageProcessor result = new LabelSizeFiltering(RelationalOperator.LE, 5).process(image);
		int[] labels = LabelImages.findAllLabels(result);
		assertEquals(3, labels.length);
	}
	
	/**
	 * @return a new label image containing four labels: one with 1 pixel, 2 with
	 *         5 pixels, and 1 with 25 pixels.
	 */
	private ByteProcessor createLabelImage()
	{
		ByteProcessor image = new ByteProcessor(10, 10);
		image.set(1, 1, 2);
		for (int i = 3; i < 8; i++)
		{
			image.set(i, 1, 3);
			image.set(1, i, 4);
		}
		for (int y = 3; y < 8; y++)
		{
			for (int x = 3; x < 8; x++)
			{
				image.set(x, y, 5);
			}
		}

		return image;		
	}

	/**
	 * Test method for
	 * {@link inra.ijpb.label.select.LabelSizeFiltering#process(ij.ImageStack)}.
	 */
	@Test
	public void testProcessImageStack_EQ()
	{
		ImageStack image = createLabelStack();
		ImageStack result = new LabelSizeFiltering(RelationalOperator.EQ, 5).process(image);
		int[] labels = LabelImages.findAllLabels(result);
		assertEquals(3, labels.length);
	}

	/**
	 * Test method for
	 * {@link inra.ijpb.label.select.LabelSizeFiltering#process(ij.ImageStack)}.
	 */
	@Test
	public void testProcessImageStack_NE()
	{
		ImageStack image = createLabelStack();
		ImageStack result = new LabelSizeFiltering(RelationalOperator.NE, 5).process(image);
		int[] labels = LabelImages.findAllLabels(result);
		assertEquals(5, labels.length);
	}

	/**
	 * Test method for
	 * {@link inra.ijpb.label.select.LabelSizeFiltering#process(ij.ImageStack)}.
	 */
	@Test
	public void testProcessImageStack_GT()
	{
		ImageStack image = createLabelStack();
		ImageStack result = new LabelSizeFiltering(RelationalOperator.GT, 5).process(image);
		int[] labels = LabelImages.findAllLabels(result);
		assertEquals(4, labels.length);
	}

	/**
	 * Test method for
	 * {@link inra.ijpb.label.select.LabelSizeFiltering#process(ij.ImageStack)}.
	 */
	@Test
	public void testProcessImageStack_GE()
	{
		ImageStack image = createLabelStack();
		ImageStack result = new LabelSizeFiltering(RelationalOperator.GE, 5).process(image);
		int[] labels = LabelImages.findAllLabels(result);
		assertEquals(7, labels.length);
	}

	/**
	 * Test method for
	 * {@link inra.ijpb.label.select.LabelSizeFiltering#process(ij.ImageStack)}.
	 */
	@Test
	public void testProcessImageStack_LT()
	{
		ImageStack image = createLabelStack();
		ImageStack result = new LabelSizeFiltering(RelationalOperator.LT, 5).process(image);
		int[] labels = LabelImages.findAllLabels(result);
		assertEquals(1, labels.length);
	}

	/**
	 * Test method for
	 * {@link inra.ijpb.label.select.LabelSizeFiltering#process(ij.ImageStack)}.
	 */
	@Test
	public void testProcessImageStack_LE()
	{
		ImageStack image = createLabelStack();
		ImageStack result = new LabelSizeFiltering(RelationalOperator.LE, 5).process(image);
		int[] labels = LabelImages.findAllLabels(result);
		assertEquals(4, labels.length);
	}

	/**
	 * @return a new 3D stack containing eight labels: one with 1 voxel, 3 with
	 *         5 voxels, 3 with 25 voxels, and 1 with 125 voxels.
	 */
	private ImageStack createLabelStack()
	{
		ImageStack image = ImageStack.create(10, 10, 10, 8);
		image.setVoxel(1, 1, 1, 2);
		for (int i = 3; i < 8; i++)
		{
			image.setVoxel(i, 1, 1, 3);
			image.setVoxel(1, i, 1, 4);
			image.setVoxel(1, 1, i, 5);
		}
		for (int i = 3; i < 8; i++)
		{
			for (int j = 3; j < 8; j++)
			{
				image.setVoxel(i, j, 1, 6);
				image.setVoxel(i, 1, j, 7);
				image.setVoxel(1, i, j, 8);
			}
		}
		for (int z = 3; z < 8; z++)
		{
			for (int y = 3; y < 8; y++)
			{
				for (int x = 3; x < 8; x++)
				{
					image.setVoxel(x, y, z, 10);
				}
			}
		}
		return image;		
	}

}
