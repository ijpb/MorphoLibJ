/**
 * 
 */
package inra.ijpb.measure.region2d;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.process.ByteProcessor;

/**
 * @author dlegland
 *
 */
public class BinaryConfigurationsHistogram2DTest
{
	/**
	 * Test method for {@link inra.ijpb.measure.region2d.BinaryConfigurationsHistogram2D#processInnerFrame(ij.process.ImageProcessor)}.
	 */
	@Test
	public final void testProcessInnerFrame_OhserMuecklich()
	{
		int[][] data = new int[][] {
			{0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0}, 
			{0, 1, 0, 0,  1, 1, 1, 1,  0, 0, 0, 0,  0, 0, 0, 1}, 
			{0, 1, 1, 0,  0, 1, 1, 1,  0, 0, 0, 0,  1, 1, 0, 0}, 
			{0, 1, 1, 1,  0, 1, 1, 1,  0, 1, 1, 0,  0, 0, 1, 0}, 
			{0, 0, 0, 0,  0, 1, 1, 1,  0, 0, 0, 1,  0, 0, 1, 0}, 
			{0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 1, 0}, 
			{0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 1, 1, 0}, 
			{0, 0, 0, 0,  0, 0, 1, 1,  1, 1, 0, 0,  1, 0, 1, 0}, 
			{0, 0, 0, 0,  0, 0, 1, 0,  1, 0, 0, 1,  1, 0, 0, 0}, 
			{0, 0, 0, 0,  1, 0, 1, 0,  1, 0, 0, 1,  1, 0, 1, 0}, 
			{0, 1, 0, 0,  1, 0, 1, 1,  1, 0, 1, 0,  1, 0, 1, 0}, 
			{0, 1, 0, 1,  1, 0, 0, 0,  0, 0, 1, 0,  0, 0, 1, 0}, 
			{0, 1, 1, 1,  0, 0, 0, 0,  0, 0, 1, 1,  1, 1, 1, 0}, 
			{0, 0, 1, 1,  0, 0, 0, 0,  0, 1, 1, 1,  1, 1, 0, 0}, 
			{0, 0, 0, 0,  0, 0, 0, 0,  0, 1, 0, 0,  0, 0, 0, 0}, 
			{0, 0, 0, 0,  0, 1, 1, 0,  0, 0, 0, 0,  0, 0, 0, 0}, 
		};
		int sizeX = 16;
		int sizeY = 16;
		ByteProcessor image = new ByteProcessor(sizeX, sizeY);
		for (int y = 0; y < sizeY; y++)
		{
			for (int x = 0; x < sizeY; x++)
			{
				image.set(x, y, data[y][x] > 0 ? 255 : 0);
			}
		}
		
		int[] histo = new BinaryConfigurationsHistogram2D().processInnerFrame(image);
		
		// check size of histogram
		assertEquals(16, histo.length);
		
		// check all configurations have been counted
		int sum = 0;
		for (int i = 0; i < 16; i++)
		{
			sum += histo[i];
		}
		assertEquals(15*15, sum);
		
		// Compare with pre-computed values 
		// (adapted from Ohser and Muecklich, p. 131. Pixel positions 1 and 2 are switched)
		int[] exp = new int[] {70, 12, 13, 12,  13, 21, 2, 5,  16, 2, 19, 5,  11, 5, 7, 12};
		for (int i = 0; i < 16; i++)
		{
			assertEquals(exp[i], histo[i]);
		}
	}

}
