/**
 * 
 */
package inra.ijpb.morphology.binary;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.process.ByteProcessor;

/**
 * 
 */
public class DistanceMapBinaryOpeningTest
{

	@Test
	public final void test_twoDisks_radius3()
	{
		// create an image composed of two disks, 
		// with centers (8, 8) and (24, 8), 
		// and radius 6 and 3
		ByteProcessor image = new ByteProcessor(30, 16);
		for (int y = 0; y < 16; y++)
		{
			for (int x = 0; x < 30; x++)
			{
				if (Math.hypot(x - 8, y - 8) < 6.5)
				{
					image.set(x, y, 255);
				}
				if (Math.hypot(x - 24, y - 8) < 2.5)
				{
					image.set(x, y, 255);
				}
			}
		}
		
		DistanceMapBinaryOpening op = new DistanceMapBinaryOpening(3);
		ByteProcessor res = op.processBinary(image);
		
		assertEquals(30, res.getWidth());
		assertEquals(16, res.getHeight());
		
		// first disk is preserved
		assertEquals(  0, res.get(0, 0));
		assertEquals(255, res.get(8, 8));
		assertEquals(  0, res.get(0, 8));
		assertEquals(255, res.get(2, 8));
		assertEquals(  0, res.get(16, 8));
		assertEquals(255, res.get(14, 8));
		
		// second disk should totally disappear
		assertEquals(  0, res.get(24, 8));
	}

}
