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
package inra.ijpb.binary.geodesic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ij.ImageStack;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.binary.distmap.ChamferMask3D;

public class GeodesicDistanceTransform3DFloatTest
{
	/**
	 * Creates a 3D cube.
	 * Computes geodesic distance map between two extreme corners.
	 */
	@Test
	public void testGeodesicDistanceMap_Cube_ChessBoard()
	{
		ImageStack mask = createCubeImage();
		ImageStack marker = ImageStack.create(12, 12, 12, 8);
		marker.setVoxel(1, 1, 1, 255);

		ChamferMask3D chamferMask = ChamferMask3D.CHESSBOARD; 
		GeodesicDistanceTransform3D gdt = new GeodesicDistanceTransform3DFloat(chamferMask, false);
		DefaultAlgoListener.monitor(gdt);
		
		ImageStack distMap = gdt.geodesicDistanceMap(marker, mask);
		
		double cornerValue = distMap.getVoxel(10, 10, 10);
		assertEquals(9, cornerValue, .1);
	}
	

	/**
	 * Creates a 3D cube.
	 * Computes geodesic distance map between two extreme corners.
	 */
	@Test
	public void testGeodesicDistanceMap_Cube_CityBlock()
	{
		ImageStack mask = createCubeImage();
		ImageStack marker = ImageStack.create(12, 12, 12, 8);		
		marker.setVoxel(1, 1, 1, 255);

		ChamferMask3D chamferMask = ChamferMask3D.CITY_BLOCK; 
		GeodesicDistanceTransform3D gdt = new GeodesicDistanceTransform3DFloat(chamferMask, false);
		DefaultAlgoListener.monitor(gdt);
		
		ImageStack distMap = gdt.geodesicDistanceMap(marker, mask);
		
		double cornerValue = distMap.getVoxel(10, 10, 10);
		assertEquals(27, cornerValue, .1);
	}
	
	/**
	 * Creates a 3D cube.
	 * Computes geodesic distance map between two extreme corners.
	 */
	@Test
	public void testGeodesicDistanceMap_FullImage_CityBlock()
	{
		ImageStack mask = createFullImage();
		ImageStack marker = ImageStack.create(12, 12, 12, 8);		
		marker.setVoxel(1, 1, 1, 255);

		ChamferMask3D chamferMask = ChamferMask3D.CITY_BLOCK; 
		GeodesicDistanceTransform3D gdt = new GeodesicDistanceTransform3DFloat(chamferMask, false);
		DefaultAlgoListener.monitor(gdt);
		
		ImageStack distMap = gdt.geodesicDistanceMap(marker, mask);
		
		double cornerValue = distMap.getVoxel(11, 11, 11);
		assertEquals(30, cornerValue, .1);
	}
	
	@Test
	public void testGeodesicDistanceMap_Labels_Borgefors()
	{
		// initialize mask image (as a label map)
		ImageStack labels = ImageStack.create(12, 12, 12, 8);
		for (int z = 0; z < 5; z++)
		{
			for (int y = 0; y < 5; y++)
			{
				for (int x = 0; x < 5; x++)
				{
					labels.setVoxel(x + 1, y + 1, z + 1, 3);
					labels.setVoxel(x + 6, y + 1, z + 1, 4);
					labels.setVoxel(x + 1, y + 6, z + 1, 5);
					labels.setVoxel(x + 6, y + 6, z + 1, 6);
					labels.setVoxel(x + 1, y + 1, z + 6, 7);
					labels.setVoxel(x + 6, y + 1, z + 6, 8);
					labels.setVoxel(x + 1, y + 6, z + 6, 9);
					labels.setVoxel(x + 6, y + 6, z + 6, 10);
				}
			}
		}
		
		// initialize marker image
		ImageStack markers = ImageStack.create(12, 12, 12, 8);
		markers.setVoxel(1, 1, 1, 255);
		markers.setVoxel(6, 1, 1, 255);
		markers.setVoxel(1, 6, 1, 255);
		markers.setVoxel(6, 6, 1, 255);
		markers.setVoxel(1, 1, 6, 255);
		markers.setVoxel(6, 1, 6, 255);
		markers.setVoxel(1, 6, 6, 255);
		markers.setVoxel(6, 6, 6, 255);
		
		// Compute map
		ChamferMask3D chamferMask = ChamferMask3D.BORGEFORS; 
		GeodesicDistanceTransform3D gdt = new GeodesicDistanceTransform3DFloat(chamferMask, true);
		ImageStack map = gdt.geodesicDistanceMap(markers, labels);

		// expect 0.0 at marker position
		assertEquals(0.0, map.getVoxel(1, 1, 1), 0.01);
		assertEquals(0.0, map.getVoxel(6, 1, 1), 0.01);
		assertEquals(0.0, map.getVoxel(1, 6, 1), 0.01);
		assertEquals(0.0, map.getVoxel(6, 6, 1), 0.01);
		assertEquals(0.0, map.getVoxel(1, 1, 6), 0.01);
		assertEquals(0.0, map.getVoxel(6, 1, 6), 0.01);
		assertEquals(0.0, map.getVoxel(1, 6, 6), 0.01);
		assertEquals(0.0, map.getVoxel(6, 6, 6), 0.01);
		// expect 4*5/3 ~= 6.66 at square corners
		assertEquals(6.66, map.getVoxel( 5,  5,  5), 0.01);
		assertEquals(6.66, map.getVoxel(10,  5,  5), 0.01);
		assertEquals(6.66, map.getVoxel( 5, 10,  5), 0.01);
		assertEquals(6.66, map.getVoxel(10, 10,  5), 0.01);
		assertEquals(6.66, map.getVoxel( 5,  5, 10), 0.01);
		assertEquals(6.66, map.getVoxel(10,  5, 10), 0.01);
		assertEquals(6.66, map.getVoxel( 5, 10, 10), 0.01);
		assertEquals(6.66, map.getVoxel(10, 10, 10), 0.01);
		// expect NaN in background
		assertTrue(Double.isNaN(map.getVoxel( 0,  0,  0)));
		assertTrue(Double.isNaN(map.getVoxel(11,  0,  0)));
		assertTrue(Double.isNaN(map.getVoxel( 0, 11,  0)));
		assertTrue(Double.isNaN(map.getVoxel(11, 11,  0)));
		assertTrue(Double.isNaN(map.getVoxel( 0,  0, 11)));
		assertTrue(Double.isNaN(map.getVoxel(11,  0, 11)));
		assertTrue(Double.isNaN(map.getVoxel( 0, 11, 11)));
		assertTrue(Double.isNaN(map.getVoxel(11, 11, 11)));
	}
	
	@Test
	public void testGeodesicDistanceMap_LabelsHollowCube_Borgefors()
	{
		// initialize mask image for first label
		ImageStack labels = ImageStack.create(12, 12, 12, 8);
		for (int z = 1; z < 11; z++)
		{
			for (int y = 1; y < 11; y++)
			{
				for (int x = 1; x < 11; x++)
				{
					labels.setVoxel(x, y, z, 2);
				}
			}
		}
		// add a second label within the first one
		for (int z = 2; z < 10; z++)
		{
			for (int y = 2; y < 10; y++)
			{
				for (int x = 2; x < 10; x++)
				{
					labels.setVoxel(x, y, z, 5);
				}
			}
		}
		
		// initialize marker image
		ImageStack markers = ImageStack.create(12, 12, 12, 8);
		markers.setVoxel(1, 1, 1, 255);
		markers.setVoxel(2, 2, 2, 255);
		
		// Compute map
		ChamferMask3D chamferMask = ChamferMask3D.BORGEFORS; 
		GeodesicDistanceTransform3D gdt = new GeodesicDistanceTransform3DFloat(chamferMask, true);
		ImageStack map = gdt.geodesicDistanceMap(markers, labels);

		assertEquals(0.0, map.getVoxel(1, 1, 1), 0.01);
		assertEquals(0.0, map.getVoxel(2, 2, 2), 0.01);
		
		assertEquals((8*4 + 5 + 8*3)/3, map.getVoxel(10, 10, 10), 0.5);
	}


//	/**
//	 * Creates an extruded ring along the z dimension.
//	 * Computes geodesic distance map between two extreme corners
//	 */
//	@Test
//	public void testGeodesicDistanceMap_Ring()
//	{
//		ImageStack mask = ImageStack.create(11, 11, 5, 8);
//		for (int z = 1; z <= 3; z++)
//		{
//			for (int x = 1; x <= 9; x++)
//			{
//				for (int y = 1; y <= 3; y++)
//				{
//					mask.setVoxel(x, y, z, 255);
//				}
//				for (int y = 7; y <= 9; y++)
//				{
//					mask.setVoxel(x, y, z, 255);
//				}
//			}
//			
//			for (int y = 4; y <= 6; y++)
//			{
//				for (int x = 1; x <= 3; x++)
//				{
//					mask.setVoxel(x, y, z, 255);
//				}
//				for (int x = 7; x <= 9; x++)
//				{
//					mask.setVoxel(x, y, z, 255);
//				}
//			}
//		}
//		
//		
//	}

	/**
	 * Creates a new 3D image with size 12x12x12, containing voxels with values
	 * 255 in a cube region at (1,10)x(1,10)x(1,10).
	 * 
	 * @return a test image containing a cube
	 */
	private final static ImageStack createCubeImage()
	{
		ImageStack image = ImageStack.create(12, 12, 12, 8);
		for (int z = 1; z <= 10; z++)
		{
			for (int y = 1; y <= 10; y++)
			{
				for (int x = 1; x <= 10; x++)
				{
					image.setVoxel(x, y, z, 255);
				}
			}
		}
		
		return image;
	}

	private final static ImageStack createFullImage()
	{
		ImageStack image = ImageStack.create(12, 12, 12, 8);
		for (int z = 0; z < 12; z++)
		{
			for (int y = 0; y < 12; y++)
			{
				for (int x = 0; x < 12; x++)
				{
					image.setVoxel(x, y, z, 255);
				}
			}
		}
		
		return image;
	}
}
