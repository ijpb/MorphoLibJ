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
package inra.ijpb.morphology.attrfilt;

import static org.junit.Assert.assertEquals;
import ij.ImageStack;

import org.junit.Test;

/**
 * @author dlegland
 *
 */
public class SizeOpening3DQueueTest
{

	/**
	 * Test method for {@link inra.ijpb.morphology.attrfilt.SizeOpening3DQueue#process(ij.ImageStack, int)}.
	 */
	@Test
	public void testProcess()
	{
		ImageStack image = ImageStack.create(6, 6, 6, 8);
		for (int z = 1; z < 5; z++)
		{
			for (int y = 1; y < 5; y++)
			{				
				for (int x = 1; x < 5; x++)
				{
					image.setVoxel(x, y, z, 10);
				}
			}
		}
		
		for (int z = 2; z < 5; z++)
		{
			for (int y = 2; y < 5; y++)
			{				
				for (int x = 2; x < 5; x++)
				{
					image.setVoxel(x, y, z, 20);
				}
			}
		}
		
		image.setVoxel(2, 2, 2, 50);
		image.setVoxel(4, 2, 2, 60);
		image.setVoxel(2, 4, 2, 70);
		image.setVoxel(4, 4, 2, 80);
		image.setVoxel(2, 2, 4, 90);
		image.setVoxel(4, 2, 4, 110);
		image.setVoxel(2, 4, 4, 120);
		image.setVoxel(4, 4, 4, 130);
		
		SizeOpening3D algo = new SizeOpening3DQueue();
		
		ImageStack result = algo.process(image, 4);
		
		assertEquals(10, result.getVoxel(1, 1, 1), .1);
		assertEquals(10, result.getVoxel(4, 1, 1), .1);
		assertEquals(10, result.getVoxel(1, 4, 1), .1);
		assertEquals(10, result.getVoxel(4, 4, 1), .1);
		assertEquals(10, result.getVoxel(1, 1, 4), .1);
		assertEquals(10, result.getVoxel(4, 1, 4), .1);
		assertEquals(10, result.getVoxel(1, 4, 4), .1);
		
		assertEquals(20, result.getVoxel(2, 2, 2), .1);
		assertEquals(20, result.getVoxel(4, 4, 4), .1);
	}

}
