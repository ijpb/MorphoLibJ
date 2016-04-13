/**
 * 
 */
package inra.ijpb.morphology.attrfilt;

import static org.junit.Assert.assertEquals;
import ij.ImageStack;

import org.junit.Test;

/**
 * @author dlegland
 *
 */
public class SizeOpening3DQueueTest
{

	/**
	 * Test method for {@link inra.ijpb.morphology.attrfilt.SizeOpening3DQueue#process(ij.ImageStack, int)}.
	 */
	@Test
	public void testProcess()
	{
		ImageStack image = ImageStack.create(6, 6, 6, 8);
		for (int z = 1; z < 5; z++)
		{
			for (int y = 1; y < 5; y++)
			{				
				for (int x = 1; x < 5; x++)
				{
					image.setVoxel(x, y, z, 10);
				}
			}
		}
		
		for (int z = 2; z < 5; z++)
		{
			for (int y = 2; y < 5; y++)
			{				
				for (int x = 2; x < 5; x++)
				{
					image.setVoxel(x, y, z, 20);
				}
			}
		}
		
		image.setVoxel(2, 2, 2, 50);
		image.setVoxel(4, 2, 2, 60);
		image.setVoxel(2, 4, 2, 70);
		image.setVoxel(4, 4, 2, 80);
		image.setVoxel(2, 2, 4, 90);
		image.setVoxel(4, 2, 4, 110);
		image.setVoxel(2, 4, 4, 120);
		image.setVoxel(4, 4, 4, 130);
		
		SizeOpening3D algo = new SizeOpening3DQueue();
		
		ImageStack result = algo.process(image, 4);
		
		assertEquals(10, result.getVoxel(1, 1, 1), .1);
		assertEquals(10, result.getVoxel(4, 1, 1), .1);
		assertEquals(10, result.getVoxel(1, 4, 1), .1);
		assertEquals(10, result.getVoxel(4, 4, 1), .1);
		assertEquals(10, result.getVoxel(1, 1, 4), .1);
		assertEquals(10, result.getVoxel(4, 1, 4), .1);
		assertEquals(10, result.getVoxel(1, 4, 4), .1);
		
		assertEquals(20, result.getVoxel(2, 2, 2), .1);
		assertEquals(20, result.getVoxel(4, 4, 4), .1);
	}

}
