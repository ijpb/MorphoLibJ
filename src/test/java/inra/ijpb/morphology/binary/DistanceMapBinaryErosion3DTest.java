/**
 * 
 */
package inra.ijpb.morphology.binary;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.ImageStack;

/**
 * 
 */
public class DistanceMapBinaryErosion3DTest
{

	/**
	 * Test method for {@link inra.ijpb.morphology.binary.DistanceMapBinaryErosion3D#processBinary(ij.ImageStack)}.
	 */
	@Test
	public final void test_twoBalls()
	{
		// create an image composed of two disks, 
		// with centers (8, 8) and (24, 8), 
		// and radius 5 and 3
		ImageStack image = ImageStack.create(30, 16, 16, 8);
		for (int z = 0; z < 16; z++)
		{
			for (int y = 0; y < 16; y++)
			{
				for (int x = 0; x < 30; x++)
				{
					if (Math.hypot(Math.hypot(x - 8, y - 8), z - 8) < 5.5)
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
		
		DistanceMapBinaryErosion3D op = new DistanceMapBinaryErosion3D(2);
		ImageStack res = op.processBinary(image);
		
		assertEquals(30, res.getWidth());
		assertEquals(16, res.getHeight());
		assertEquals(16, res.getSize());
		
		assertEquals(  0, (int) res.getVoxel(0, 0, 0));

		// first ball should shrink
		assertEquals(255, (int) res.getVoxel( 8, 8, 8));
		assertEquals(  0, (int) res.getVoxel( 4, 8, 8));
		assertEquals(255, (int) res.getVoxel( 5, 8, 8));
		assertEquals(255, (int) res.getVoxel(11, 8, 8));
		assertEquals(  0, (int) res.getVoxel(12, 8, 8));
		
		// second ball should shrink
		assertEquals(255, (int) res.getVoxel(24, 8, 8));
		assertEquals(  0, (int) res.getVoxel(23, 8, 8));
		assertEquals(  0, (int) res.getVoxel(25, 8, 8));
	}

}
