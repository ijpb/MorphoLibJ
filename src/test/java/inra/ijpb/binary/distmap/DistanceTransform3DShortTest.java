package inra.ijpb.binary.distmap;

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

}
