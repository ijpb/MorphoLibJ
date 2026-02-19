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

import ij.ImageStack;

/**
 * 
 */
public class DistanceMapBinaryErosion3DTest
{

	/**
	 * Test method for {@link inra.ijpb.morphology.binary.DistanceMapBinaryErosion3D#processBinary(ij.ImageStack)}.
	 */
	@Test
	public final void test_twoBalls()
	{
		// create an image composed of two disks, 
		// with centers (8, 8) and (24, 8), 
		// and radius 5 and 3
		ImageStack image = ImageStack.create(30, 16, 16, 8);
		for (int z = 0; z < 16; z++)
		{
			for (int y = 0; y < 16; y++)
			{
				for (int x = 0; x < 30; x++)
				{
					if (Math.hypot(Math.hypot(x - 8, y - 8), z - 8) < 5.5)
					{
						image.setVoxel(x, y, z, 255);
					}
					if (Math.hypot(Math.hypot(x - 24, y - 8), z - 8) < 2.5)
					{
						image.setVoxel(x, y, z, 255);
					}
				}
			}
		}
		
		DistanceMapBinaryErosion3D op = new DistanceMapBinaryErosion3D(2);
		ImageStack res = op.processBinary(image);
		
		assertEquals(30, res.getWidth());
		assertEquals(16, res.getHeight());
		assertEquals(16, res.getSize());
		
		assertEquals(  0, (int) res.getVoxel(0, 0, 0));

		// first ball should shrink
		assertEquals(255, (int) res.getVoxel( 8, 8, 8));
		assertEquals(  0, (int) res.getVoxel( 4, 8, 8));
		assertEquals(255, (int) res.getVoxel( 5, 8, 8));
		assertEquals(255, (int) res.getVoxel(11, 8, 8));
		assertEquals(  0, (int) res.getVoxel(12, 8, 8));
		
		// second ball should shrink
		assertEquals(255, (int) res.getVoxel(24, 8, 8));
		assertEquals(  0, (int) res.getVoxel(23, 8, 8));
		assertEquals(  0, (int) res.getVoxel(25, 8, 8));
	}

}
