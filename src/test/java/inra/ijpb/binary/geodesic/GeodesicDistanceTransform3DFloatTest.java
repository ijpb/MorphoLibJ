package inra.ijpb.binary.geodesic;

import static org.junit.Assert.*;
import ij.ImageStack;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.binary.ChamferWeights3D;

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

		ChamferWeights3D weights = ChamferWeights3D.CITY_BLOCK; 
		GeodesicDistanceTransform3D gdt = new GeodesicDistanceTransform3DFloat(weights, false);
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

		ChamferWeights3D weights = ChamferWeights3D.CITY_BLOCK; 
		GeodesicDistanceTransform3D gdt = new GeodesicDistanceTransform3DFloat(weights, false);
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

		ChamferWeights3D weights = ChamferWeights3D.CHESSBOARD; 
		GeodesicDistanceTransform3D gdt = new GeodesicDistanceTransform3DFloat(weights, false);
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
