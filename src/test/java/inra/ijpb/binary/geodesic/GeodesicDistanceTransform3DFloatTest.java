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
package inra.ijpb.binary.geodesic;

import static org.junit.Assert.*;
import ij.ImageStack;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.binary.distmap.ChamferMask3D;

import org.junit.Test;

public class GeodesicDistanceTransform3DFloatTest
{
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
