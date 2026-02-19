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
package inra.ijpb.morphology.binary;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ij.process.ByteProcessor;

/**
 * 
 */
public class DistanceMapBinaryClosingTest
{

	/**
	 * Test method for {@link inra.ijpb.morphology.binary.DistanceMapBinaryClosing#processBinary(ij.process.ByteProcessor)}.
	 */
	@Test
	public final void test_disk_radius2()
	{
		// create an image composed of a single disk, with radius 5
		ByteProcessor image = new ByteProcessor(20, 20);
		for (int y = 0; y < 20; y++)
		{
			for (int x = 0; x < 20; x++)
			{
				if (Math.hypot(x - 8, y - 8) < 5.5)
				{
					image.set(x, y, 255);
				}
			}
		}
		
		DistanceMapBinaryClosing op = new DistanceMapBinaryClosing(2);
		ByteProcessor res = op.processBinary(image);
		
		assertEquals(20, res.getWidth());
		assertEquals(20, res.getHeight());
		
		assertEquals(  0, res.get(0, 0));

		// disk should stay the same
		assertEquals(255, res.get( 8,  8));
		assertEquals(  0, res.get( 2,  8));
		assertEquals(255, res.get( 3,  8));
		assertEquals(255, res.get(13,  8));
		assertEquals(  0, res.get(14,  8));
		assertEquals(  0, res.get( 8,  2));
		assertEquals(255, res.get( 8,  3));
		assertEquals(255, res.get( 8, 13));
		assertEquals(  0, res.get( 8, 14));
	}

}
