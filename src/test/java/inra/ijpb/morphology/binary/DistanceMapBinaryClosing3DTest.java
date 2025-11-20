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
public class DistanceMapBinaryClosing3DTest
{

	/**
	 * Test method for {@link inra.ijpb.morphology.binary.DistanceMapBinaryClosing3D#processBinary(ij.ImageStack)}.
	 */
	@Test
	public final void test_ball_radius2()
	{
		// create an image composed of two disks, 
		// with centers (8, 8) and (24, 8), 
		// and radius 5 and 3
		ImageStack image = ImageStack.create(20, 20, 20, 8);
		for (int z = 0; z < 20; z++)
		{
			for (int y = 0; y < 20; y++)
			{
				for (int x = 0; x < 20; x++)
				{
					if (Math.hypot(Math.hypot(x - 8, y - 8), z - 8) < 5.5)
					{
						image.setVoxel(x, y, z, 255);
					}
				}
			}
		}
		
		DistanceMapBinaryClosing3D op = new DistanceMapBinaryClosing3D(2);
		ImageStack res = op.processBinary(image);
		
		assertEquals(20, res.getWidth());
		assertEquals(20, res.getHeight());
		assertEquals(20, res.getSize());
		
		assertEquals(  0, (int) res.getVoxel(0, 0, 0));

		// first ball should shrink
		assertEquals(255, (int) res.getVoxel( 8, 8, 8));
		assertEquals(  0, (int) res.getVoxel( 2, 8, 8));
		assertEquals(255, (int) res.getVoxel( 3, 8, 8));
		assertEquals(255, (int) res.getVoxel(13, 8, 8));
		assertEquals(  0, (int) res.getVoxel(14, 8, 8));
		
		assertEquals(  0, (int) res.getVoxel( 8,  2, 8));
		assertEquals(255, (int) res.getVoxel( 8,  3, 8));
		assertEquals(255, (int) res.getVoxel( 8, 13, 8));
		assertEquals(  0, (int) res.getVoxel( 8, 14, 8));
		
		assertEquals(  0, (int) res.getVoxel( 8,  8,  2));
		assertEquals(255, (int) res.getVoxel( 8,  8,  3));
		assertEquals(255, (int) res.getVoxel( 8,  8, 13));
		assertEquals(  0, (int) res.getVoxel( 8,  8, 14));
	}

}
