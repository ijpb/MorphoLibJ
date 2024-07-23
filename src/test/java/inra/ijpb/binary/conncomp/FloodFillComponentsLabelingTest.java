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
package inra.ijpb.binary.conncomp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class FloodFillComponentsLabelingTest
{
	/**
	 * Default settings are 4 connectivity, 16 bits image.
	 */
	@Test
	public void testFloodFillComponentsLabeling_Default()
	{
		ByteProcessor image = createFiveSquaresImage();
		
		FloodFillComponentsLabeling algo = new FloodFillComponentsLabeling();
		ImageProcessor result = algo.computeLabels(image);
		
		assertEquals(16, result.getBitDepth());
		assertEquals(5, result.get(7, 7));
	}

	/**
	 * Using 4 connectivity should result in five connected components.
	 */
	@Test
	public void testFloodFillComponentsLabeling_C4_Byte()
	{
		ByteProcessor image = createFiveSquaresImage();
		
		FloodFillComponentsLabeling algo = new FloodFillComponentsLabeling(4, 8);
		ImageProcessor result = algo.computeLabels(image);
		
		assertEquals(8, result.getBitDepth());
		assertEquals(5, result.get(7, 7));
	}


	/**
	 * Using 8 connectivity should result in one connected component.
	 */
	@Test
	public void testFloodFillComponentsLabeling_C8_Short()
	{
		ByteProcessor image = createFiveSquaresImage();
		
		FloodFillComponentsLabeling algo = new FloodFillComponentsLabeling(8, 16);
		ImageProcessor result = algo.computeLabels(image);
		
		assertEquals(16, result.getBitDepth());
		assertEquals(1, result.get(7, 7));
	}
	
	/**
	 * Create a 10-by-01 byte image containing five square touching by corners.
	 * 
	 * Expected number of connected components is five for 4 connectivity, and
	 * one for 8 connectivity.
	 * 
	 * @return an image containing five squares touching by corners
	 */
	private final static ByteProcessor createFiveSquaresImage()
	{
		ByteProcessor image = new ByteProcessor(10, 10);
		for (int y = 0; y < 2; y++)
		{
			for (int x = 0; x < 2; x++)
			{
				image.set(x + 2, y + 2, 255);
				image.set(x + 6, y + 2, 255);
				image.set(x + 4, y + 4, 255);
				image.set(x + 2, y + 6, 255);
				image.set(x + 6, y + 6, 255);
			}
		}
		return image;
	}
}
