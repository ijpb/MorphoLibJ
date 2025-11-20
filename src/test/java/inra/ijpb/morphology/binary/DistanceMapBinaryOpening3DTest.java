/**
 * 
 */
package inra.ijpb.morphology.binary;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ij.ImageStack;

/**
 * 
 */
public class DistanceMapBinaryOpening3DTest
{

	@Test
	public final void test_twoBallss_radius3()
	{
		// create an image composed of two disks, 
		// with centers (8, 8) and (24, 8), 
		// and radius 6 and 3
		ImageStack image = ImageStack.create(30, 16, 16, 8);
		for (int z = 0; z < 16; z++)
		{
			for (int y = 0; y < 16; y++)
			{
				for (int x = 0; x < 30; x++)
				{
					if (Math.hypot(Math.hypot(x - 8, y - 8), z - 8) < 6.5)
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

		DistanceMapBinaryOpening3D op = new DistanceMapBinaryOpening3D(3);
		ImageStack res = op.processBinary(image);
		
		assertEquals(30, res.getWidth());
		assertEquals(16, res.getHeight());
		assertEquals(16, res.getSize());
		
		// first disk is preserved
		assertEquals(  0, (int) res.getVoxel(0, 0, 0));
		assertEquals(255, (int) res.getVoxel(8, 8, 8));
		assertEquals(  0, (int) res.getVoxel(0, 8, 8));
		assertEquals(255, (int) res.getVoxel(2, 8, 8));
		assertEquals(  0, (int) res.getVoxel(16, 8, 8));
		assertEquals(255, (int) res.getVoxel(14, 8, 8));
		
		// second disk should totally disappear
		assertEquals(  0, (int) res.getVoxel(24, 8, 8));
	}

}
