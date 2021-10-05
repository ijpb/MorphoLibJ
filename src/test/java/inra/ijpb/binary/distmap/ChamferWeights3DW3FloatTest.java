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
public class ChamferWeights3DW3FloatTest
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

		ChamferMask3D weights = CommonChamferMasks3D.QUASI_EUCLIDEAN.getChamferWeights();
		DistanceTransform3D algo = new ChamferDistanceTransform3DFloat(weights, true);
		
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

		ChamferMask3D weights = CommonChamferMasks3D.QUASI_EUCLIDEAN.getChamferWeights();
		DistanceTransform3D algo = new ChamferDistanceTransform3DShort(weights, true);
		
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
