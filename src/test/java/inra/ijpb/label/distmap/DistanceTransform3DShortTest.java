/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2017 INRA.
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
package inra.ijpb.label.distmap;

import static org.junit.Assert.*;
import ij.ImageStack;
import inra.ijpb.binary.ChamferWeights3D;

import org.junit.Test;

public class DistanceTransform3DShortTest
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
		short[] weights = ChamferWeights3D.BORGEFORS.getShortWeights();
		DistanceTransform3D algo = new DistanceTransform3DShort(weights, true);
		
		ImageStack result = algo.distanceMap(image);
		assertEquals(16, result.getBitDepth());
		
//		System.out.println("result:");
//		for (int x = 0; x < 100; x++)
//		{
//			System.out.print(((int)result.getVoxel(x, 50, 50)) + " ");
//		}
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

		short[] weights = ChamferWeights3D.BORGEFORS.getShortWeights();
		DistanceTransform3D algo = new DistanceTransform3DShort(weights, true);
		
		ImageStack result = algo.distanceMap(image);
		assertEquals(16, result.getBitDepth());
		
		assertEquals(1, result.getVoxel( 4, 5, 5), .1);
		assertEquals(1, result.getVoxel(6, 5, 5), .1);
		assertEquals(4/3, result.getVoxel( 4,  4, 5), .1);
		assertEquals(Math.floor(5./3.), result.getVoxel( 4,  4,  4), .1);
		
		// Test some voxels at the cube corners
		int exp = (int) Math.floor(5.0 * 5.0 / 3.0);
		assertEquals(exp, result.getVoxel( 0,  0,  0), .01);
		assertEquals(exp, result.getVoxel(10,  0,  0), .01);
		assertEquals(exp, result.getVoxel( 0, 10,  0), .01);
		assertEquals(exp, result.getVoxel(10, 10,  0), .01);
		assertEquals(exp, result.getVoxel( 0,  0, 10), .01);
		assertEquals(exp, result.getVoxel(10,  0, 10), .01);
		assertEquals(exp, result.getVoxel( 0, 10, 10), .01);
		assertEquals(exp, result.getVoxel(10, 10, 10), .01);
	}

	/**
	 * Test propagation of distance maps within touching labels
	 */
	@Test
	public void testDistanceMap_Labels()
	{
		// create 3D image containing eight cubes with labels between 1 and 8 
		ImageStack image = ImageStack.create(11, 11, 11, 8);
		for (int z = 0; z < 3; z++)
		{
			for (int y = 0; y < 3; y++)
			{
				for (int x = 0; x < 3; x++)
				{
					image.setVoxel(x+1, y+1, z+1, 1);
					image.setVoxel(x+4, y+1, z+1, 2);
					image.setVoxel(x+1, y+4, z+1, 3);
					image.setVoxel(x+4, y+4, z+1, 4);
					image.setVoxel(x+1, y+1, z+4, 5);
					image.setVoxel(x+4, y+1, z+4, 6);
					image.setVoxel(x+1, y+4, z+4, 7);
					image.setVoxel(x+4, y+4, z+4, 8);
				}
			}
		}

		ChamferWeights3D weights = ChamferWeights3D.BORGEFORS;
		DistanceTransform3D algo = new DistanceTransform3DShort(weights, true);
		
		ImageStack result = algo.distanceMap(image);
		assertEquals(16, result.getBitDepth());
		
		assertEquals(2, result.getVoxel(2, 2, 2), .1);
		assertEquals(2, result.getVoxel(5, 2, 2), .1);
		assertEquals(2, result.getVoxel(2, 5, 2), .1);
		assertEquals(2, result.getVoxel(5, 5, 2), .1);
		assertEquals(2, result.getVoxel(2, 2, 5), .1);
		assertEquals(2, result.getVoxel(5, 2, 5), .1);
		assertEquals(2, result.getVoxel(2, 5, 5), .1);
		assertEquals(2, result.getVoxel(5, 5, 5), .1);
		
		assertEquals(1, result.getVoxel(3, 3, 3), .1);
		assertEquals(1, result.getVoxel(4, 3, 3), .1);
		assertEquals(1, result.getVoxel(3, 4, 3), .1);
		assertEquals(1, result.getVoxel(4, 4, 3), .1);
		assertEquals(1, result.getVoxel(3, 3, 4), .1);
		assertEquals(1, result.getVoxel(4, 3, 4), .1);
		assertEquals(1, result.getVoxel(3, 4, 4), .1);
		assertEquals(1, result.getVoxel(4, 4, 4), .1);
	}

}
