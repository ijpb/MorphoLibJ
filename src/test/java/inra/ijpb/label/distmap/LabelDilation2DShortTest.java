/**
 * 
 */
package inra.ijpb.label.distmap;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.binary.distmap.ChamferMask2D;

/**
 * @author dlegland
 *
 */
@Deprecated
public class LabelDilation2DShortTest
{

	/**
	 * Test method for {@link inra.ijpb.label.distmap.LabelDilation2DShort#process(ij.process.ImageProcessor, double)}.
	 */
	@Test
	public final void testProcess()
	{
		// Generate a label image with four regions with labels 3, 4, 5 and 6,
		// separated by a 2-pixels wide background.
		ByteProcessor labels = new ByteProcessor(14, 14);
		for(int i = 0; i < 2; i++)
		{
			for(int j = 0; j < 2; j++)
			{
				labels.set(4 + i, 4 + j, 3);
				labels.set(8 + i, 4 + j, 4);
				labels.set(4 + i, 8 + j, 5);
				labels.set(8 + i, 8 + j, 6);
			}
		}
		
		LabelDilation2DShort algo = new LabelDilation2DShort(ChamferMask2D.BORGEFORS);
		ImageProcessor result = algo.process(labels, 3.5);
		
//		IJUtils.printImage(result);
		
		// result should have same bit depth as input label
		assertEquals(8, result.getBitDepth());
		// check label region 1
		assertEquals(3, result.get(4, 2));
		assertEquals(3, result.get(2, 4));
		assertEquals(3, result.get(6, 4));
		assertEquals(3, result.get(4, 6));
		// check label region 2
		assertEquals(4, result.get(8, 2));
		assertEquals(4, result.get(11, 4));
		assertEquals(4, result.get(7, 4));
		assertEquals(4, result.get(8, 6));
		// check label region 3
		assertEquals(5, result.get(4, 11));
		assertEquals(5, result.get(2, 9));
		assertEquals(5, result.get(6, 9));
		assertEquals(5, result.get(4, 7));
		// check label region 4
		assertEquals(6, result.get(8, 11));
		assertEquals(6, result.get(11, 9));
		assertEquals(6, result.get(7, 9));
		assertEquals(6, result.get(8, 7));
	}
}
