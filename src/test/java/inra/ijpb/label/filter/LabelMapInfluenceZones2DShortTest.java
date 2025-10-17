/**
 * 
 */
package inra.ijpb.label.filter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * 
 */
public class LabelMapInfluenceZones2DShortTest
{

	/**
	 * Test method for {@link inra.ijpb.label.filter.LabelMapInfluenceZones2DShort#process(ij.process.ImageProcessor)}.
	 */
	@Test
	public final void testProcess_fourRegions()
	{
		ImageProcessor array = new ByteProcessor(16, 16);
		int[] labels = new int[] {3, 4, 7, 9};
		array.set(7, 7, labels[0]);
		array.set(8, 7, labels[1]);
		array.set(7, 8, labels[2]);
		array.set(8, 8, labels[3]);
		
		ImageProcessor res = new LabelMapInfluenceZones2DShort().process(array);
		
		assertEquals(labels[0], res.get( 0,  0));
		assertEquals(labels[1], res.get(15,  0));
		assertEquals(labels[2], res.get( 0, 15));
		assertEquals(labels[3], res.get(15, 15));
	}

}
