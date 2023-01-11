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
package inra.ijpb.binary.distmap;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.ImageStack;

/**
 * @author dlegland
 *
 */
public class ChamferMask3DW3FloatTest
{
	@Test
	public void testDistanceMap_FromCenter_Float()
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

		ChamferMask3D mask = ChamferMask3D.QUASI_EUCLIDEAN;
		DistanceTransform3D algo = new ChamferDistanceTransform3DFloat(mask, true);
		
		ImageStack result = algo.distanceMap(image);
		assertEquals(32, result.getBitDepth());
		
		// orthogonal neighbors
		assertEquals(1.0, result.getVoxel(4, 5, 5), 0.1);
		assertEquals(1.0, result.getVoxel(6, 5, 5), 0.1);
		assertEquals(1.0, result.getVoxel(5, 4, 5), 0.1);
		assertEquals(1.0, result.getVoxel(5, 6, 5), 0.1);
		assertEquals(1.0, result.getVoxel(5, 5, 4), 0.1);
		assertEquals(1.0, result.getVoxel(5, 5, 6), 0.1);

		// square-diagonal neighbors
		assertEquals(Math.sqrt(2), result.getVoxel(4, 4, 5), 0.1);
		assertEquals(Math.sqrt(2), result.getVoxel(6, 6, 5), 0.1);
		assertEquals(Math.sqrt(2), result.getVoxel(4, 5, 4), 0.1);
		assertEquals(Math.sqrt(2), result.getVoxel(6, 5, 6), 0.1);
		assertEquals(Math.sqrt(2), result.getVoxel(4, 5, 4), 0.1);
		assertEquals(Math.sqrt(2), result.getVoxel(6, 5, 6), 0.1);

		// cube-diagonal neighbors
		assertEquals(Math.sqrt(3), result.getVoxel(4, 4, 4), 0.1);
		assertEquals(Math.sqrt(3), result.getVoxel(6, 6, 6), 0.1);
		assertEquals(Math.sqrt(3), result.getVoxel(4, 4, 6), 0.1);
		assertEquals(Math.sqrt(3), result.getVoxel(4, 6, 4), 0.1);
		assertEquals(Math.sqrt(3), result.getVoxel(6, 4, 4), 0.1);

		// Test some voxels at the cube corners
		double exp = 5.0 * Math.sqrt(3);
		assertEquals(exp, result.getVoxel( 0,  0,  0), .01);
		assertEquals(exp, result.getVoxel(10,  0,  0), .01);
		assertEquals(exp, result.getVoxel( 0, 10,  0), .01);
		assertEquals(exp, result.getVoxel(10, 10,  0), .01);
		assertEquals(exp, result.getVoxel( 0,  0, 10), .01);
		assertEquals(exp, result.getVoxel(10,  0, 10), .01);
		assertEquals(exp, result.getVoxel( 0, 10, 10), .01);
		assertEquals(exp, result.getVoxel(10, 10, 10), .01);
	}
	
	@Test
	public void testDistanceMap_FromCenter_Short()
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

		ChamferMask3D mask = ChamferMask3D.QUASI_EUCLIDEAN;
		DistanceTransform3D algo = new ChamferDistanceTransform3DShort(mask, true);
		
		ImageStack result = algo.distanceMap(image);
		assertEquals(16, result.getBitDepth());
		
		// orthogonal neighbors
		assertEquals(1.0, result.getVoxel(4, 5, 5), 0.1);
		assertEquals(1.0, result.getVoxel(6, 5, 5), 0.1);
		assertEquals(1.0, result.getVoxel(5, 4, 5), 0.1);
		assertEquals(1.0, result.getVoxel(5, 6, 5), 0.1);
		assertEquals(1.0, result.getVoxel(5, 5, 4), 0.1);
		assertEquals(1.0, result.getVoxel(5, 5, 6), 0.1);

		// square-diagonal neighbors
		// expect approximation of sqrt(2) ~= 1
		assertEquals(1.0, result.getVoxel(4, 4, 5), 0.1);
		assertEquals(1.0, result.getVoxel(6, 6, 5), 0.1);
		assertEquals(1.0, result.getVoxel(4, 5, 4), 0.1);
		assertEquals(1.0, result.getVoxel(6, 5, 6), 0.1);
		assertEquals(1.0, result.getVoxel(4, 5, 4), 0.1);
		assertEquals(1.0, result.getVoxel(6, 5, 6), 0.1);

		// cube-diagonal neighbors
		// expect approximation of sqrt(3) ~= 2
		assertEquals(2.0, result.getVoxel(4, 4, 4), 0.1);
		assertEquals(2.0, result.getVoxel(6, 6, 6), 0.1);
		assertEquals(2.0, result.getVoxel(4, 4, 6), 0.1);
		assertEquals(2.0, result.getVoxel(4, 6, 4), 0.1);
		assertEquals(2.0, result.getVoxel(6, 4, 4), 0.1);

		// Test some voxels at the cube corners
		double exp = Math.round(5.0 * Math.sqrt(3));
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
