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
package inra.ijpb.binary.distmap;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.ImageStack;

/**
 * @author dlegland
 *
 */
public class ChamferMask3DW5Test
{
	@Test
	public void testDistanceMap()
	{
		// create 3D image containing a cube 
		ImageStack image = ImageStack.create(20, 20, 20, 8);
		for (int z = 2; z < 19; z++)
		{
			for (int y = 2; y < 19; y++)
			{
				for (int x = 2; x < 19; x++)
				{
					image.setVoxel(x, y, z, 255);
				}
			}
		}

		ChamferMask3D mask = new ChamferMask3DW5(8, 11, 14, 18, 20);
		DistanceTransform3D algo = new ChamferDistanceTransform3DShort(mask, true);
		
		ImageStack result = algo.distanceMap(image);
		assertEquals(16, result.getBitDepth());
		
//		System.out.println("result:");
//		for (int x = 0; x < 100; x++)
//		{
//			System.out.print(((int)result.getVoxel(x, 50, 50)) + " ");
//		}*
		double middle = result.getVoxel(10, 10, 10);
		assertEquals(9, middle, .1);
	}

	@Test
	public void testDistanceMap_FromCenter()
	{
		// create 3D image containing a cube 
		ImageStack image = ImageStack.create(11, 11, 11, 8);
		for (int z = 0; z < 11; z++)
		{
			for (int y = 0; y < 11; y++)
			{
				for (int x = 0; x < 11; x++)
				{
					image.setVoxel(x, y, z, 255);
				}
			}
		}
		image.setVoxel(5, 5, 5, 0);
		
		ChamferMask3D mask = new ChamferMask3DW5(8, 11, 14, 18, 20);
		DistanceTransform3D algo = new ChamferDistanceTransform3DShort(mask, true);
		
		ImageStack result = algo.distanceMap(image);
		assertEquals(16, result.getBitDepth());
		
		assertEquals(1, result.getVoxel(4, 5, 5), 0.1);
		assertEquals(1, result.getVoxel(6, 5, 5), 0.1);
		assertEquals(1, result.getVoxel(4, 4, 5), 0.1); // rounding of 11/8
		assertEquals(Math.round(14.0 / 8.0), result.getVoxel(4, 4, 4), 0.1);

		// Test some voxels at the cube corners
		int exp = (int) Math.round(5 * 14.0 / 8.0);
		assertEquals(exp, result.getVoxel( 0,  0,  0), .01);
		assertEquals(exp, result.getVoxel(10,  0,  0), .01);
		assertEquals(exp, result.getVoxel( 0, 10,  0), .01);
		assertEquals(exp, result.getVoxel(10, 10,  0), .01);
		assertEquals(exp, result.getVoxel( 0,  0, 10), .01);
		assertEquals(exp, result.getVoxel(10,  0, 10), .01);
		assertEquals(exp, result.getVoxel( 0, 10, 10), .01);
		assertEquals(exp, result.getVoxel(10, 10, 10), .01);
	}
}
