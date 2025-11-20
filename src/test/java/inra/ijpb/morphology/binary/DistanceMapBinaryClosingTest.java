/**
 * 
 */
package inra.ijpb.morphology.binary;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ij.process.ByteProcessor;

/**
 * 
 */
public class DistanceMapBinaryClosingTest
{

	/**
	 * Test method for {@link inra.ijpb.morphology.binary.DistanceMapBinaryClosing#processBinary(ij.process.ByteProcessor)}.
	 */
	@Test
	public final void test_disk_radius2()
	{
		// create an image composed of a single disk, with radius 5
		ByteProcessor image = new ByteProcessor(20, 20);
		for (int y = 0; y < 20; y++)
		{
			for (int x = 0; x < 20; x++)
			{
				if (Math.hypot(x - 8, y - 8) < 5.5)
				{
					image.set(x, y, 255);
				}
			}
		}
		
		DistanceMapBinaryClosing op = new DistanceMapBinaryClosing(2);
		ByteProcessor res = op.processBinary(image);
		
		assertEquals(20, res.getWidth());
		assertEquals(20, res.getHeight());
		
		assertEquals(  0, res.get(0, 0));

		// disk should stay the same
		assertEquals(255, res.get( 8,  8));
		assertEquals(  0, res.get( 2,  8));
		assertEquals(255, res.get( 3,  8));
		assertEquals(255, res.get(13,  8));
		assertEquals(  0, res.get(14,  8));
		assertEquals(  0, res.get( 8,  2));
		assertEquals(255, res.get( 8,  3));
		assertEquals(255, res.get( 8, 13));
		assertEquals(  0, res.get( 8, 14));
	}

}
