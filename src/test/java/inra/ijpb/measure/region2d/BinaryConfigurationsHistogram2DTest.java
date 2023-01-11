/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2023 INRA.
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
package inra.ijpb.measure.region2d;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.process.ByteProcessor;

/**
 * @author dlegland
 *
 */
public class BinaryConfigurationsHistogram2DTest
{
	/**
	 * Test method for {@link inra.ijpb.measure.region2d.BinaryConfigurationsHistogram2D#processInnerFrame(ij.process.ImageProcessor)}.
	 */
	@Test
	public final void testProcessInnerFrame_OhserMuecklich()
	{
		int[][] data = new int[][] {
			{0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0}, 
			{0, 1, 0, 0,  1, 1, 1, 1,  0, 0, 0, 0,  0, 0, 0, 1}, 
			{0, 1, 1, 0,  0, 1, 1, 1,  0, 0, 0, 0,  1, 1, 0, 0}, 
			{0, 1, 1, 1,  0, 1, 1, 1,  0, 1, 1, 0,  0, 0, 1, 0}, 
			{0, 0, 0, 0,  0, 1, 1, 1,  0, 0, 0, 1,  0, 0, 1, 0}, 
			{0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 1, 0}, 
			{0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 1, 1, 0}, 
			{0, 0, 0, 0,  0, 0, 1, 1,  1, 1, 0, 0,  1, 0, 1, 0}, 
			{0, 0, 0, 0,  0, 0, 1, 0,  1, 0, 0, 1,  1, 0, 0, 0}, 
			{0, 0, 0, 0,  1, 0, 1, 0,  1, 0, 0, 1,  1, 0, 1, 0}, 
			{0, 1, 0, 0,  1, 0, 1, 1,  1, 0, 1, 0,  1, 0, 1, 0}, 
			{0, 1, 0, 1,  1, 0, 0, 0,  0, 0, 1, 0,  0, 0, 1, 0}, 
			{0, 1, 1, 1,  0, 0, 0, 0,  0, 0, 1, 1,  1, 1, 1, 0}, 
			{0, 0, 1, 1,  0, 0, 0, 0,  0, 1, 1, 1,  1, 1, 0, 0}, 
			{0, 0, 0, 0,  0, 0, 0, 0,  0, 1, 0, 0,  0, 0, 0, 0}, 
			{0, 0, 0, 0,  0, 1, 1, 0,  0, 0, 0, 0,  0, 0, 0, 0}, 
		};
		int sizeX = 16;
		int sizeY = 16;
		ByteProcessor image = new ByteProcessor(sizeX, sizeY);
		for (int y = 0; y < sizeY; y++)
		{
			for (int x = 0; x < sizeY; x++)
			{
				image.set(x, y, data[y][x] > 0 ? 255 : 0);
			}
		}
		
		int[] histo = new BinaryConfigurationsHistogram2D().processInnerFrame(image);
		
		// check size of histogram
		assertEquals(16, histo.length);
		
		// check all configurations have been counted
		int sum = 0;
		for (int i = 0; i < 16; i++)
		{
			sum += histo[i];
		}
		assertEquals(15*15, sum);
		
		// Compare with pre-computed values 
		// (adapted from Ohser and Muecklich, p. 131. Pixel positions 1 and 2 are switched)
		int[] exp = new int[] {70, 12, 13, 12,  13, 21, 2, 5,  16, 2, 19, 5,  11, 5, 7, 12};
		for (int i = 0; i < 16; i++)
		{
			assertEquals(exp[i], histo[i]);
		}
	}

}
