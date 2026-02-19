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

import static org.junit.Assert.*;

import org.junit.Test;

import ij.process.ByteProcessor;

/**
 * 
 */
public class DistanceMapBinaryErosionTest
{

	/**
	 * Test method for {@link inra.ijpb.morphology.binary.DistanceMapBinaryErosion#processBinary(ij.process.ByteProcessor)}.
	 */
	@Test
	public final void test_twoDisks_radius2()
	{
		// create an image composed of two disks, 
		// with centers (8, 8) and (24, 8), 
		// and radius 5 and 3
		ByteProcessor image = new ByteProcessor(30, 16);
		for (int y = 0; y < 16; y++)
		{
			for (int x = 0; x < 30; x++)
			{
				if (Math.hypot(x - 8, y - 8) < 5.5)
				{
					image.set(x, y, 255);
				}
				if (Math.hypot(x - 24, y - 8) < 2.5)
				{
					image.set(x, y, 255);
				}
			}
		}
		
		DistanceMapBinaryErosion op = new DistanceMapBinaryErosion(2);
		ByteProcessor res = op.processBinary(image);
		
		assertEquals(30, res.getWidth());
		assertEquals(16, res.getHeight());
		
		assertEquals(  0, res.get(0, 0));

		// first disk should shrink
		assertEquals(255, res.get(8, 8));
		assertEquals(  0, res.get(0, 8));
		assertEquals(255, res.get(5, 8));
		assertEquals(255, res.get(11, 8));
		assertEquals(  0, res.get(12, 8));
		
		// second disk should shrink
		assertEquals(255, res.get(24, 8));
		assertEquals(  0, res.get(23, 8));
		assertEquals(  0, res.get(25, 8));
	}

}
