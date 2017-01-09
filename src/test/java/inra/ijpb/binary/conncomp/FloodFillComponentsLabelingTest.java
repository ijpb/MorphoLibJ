package inra.ijpb.binary.conncomp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class FloodFillComponentsLabelingTest
{
	/**
	 * Default settings are 4 connectivity, 16 bits image.
	 */
	@Test
	public void testFloodFillComponentsLabeling_Default()
	{
		ByteProcessor image = createFiveSquaresImage();
		
		FloodFillComponentsLabeling algo = new FloodFillComponentsLabeling();
		ImageProcessor result = algo.computeLabels(image);
		
		assertEquals(16, result.getBitDepth());
		assertEquals(5, result.get(7, 7));
	}

	/**
	 * Using 4 connectivity should result in five connected components.
	 */
	@Test
	public void testFloodFillComponentsLabeling_C4_Byte()
	{
		ByteProcessor image = createFiveSquaresImage();
		
		FloodFillComponentsLabeling algo = new FloodFillComponentsLabeling(4, 8);
		ImageProcessor result = algo.computeLabels(image);
		
		assertEquals(8, result.getBitDepth());
		assertEquals(5, result.get(7, 7));
	}


	/**
	 * Using 8 connectivity should result in one connected component.
	 */
	@Test
	public void testFloodFillComponentsLabeling_C8_Short()
	{
		ByteProcessor image = createFiveSquaresImage();
		
		FloodFillComponentsLabeling algo = new FloodFillComponentsLabeling(8, 16);
		ImageProcessor result = algo.computeLabels(image);
		
		assertEquals(16, result.getBitDepth());
		assertEquals(1, result.get(7, 7));
	}
	
	/**
	 * Create a 10-by-01 byte image containing five square touching by corners.
	 * 
	 * Expected number of connected components is five for 4 connectivity, and
	 * one for 8 connectivity.
	 * 
	 * @return an image containing five squares touching by corners
	 */
	private final static ByteProcessor createFiveSquaresImage()
	{
		ByteProcessor image = new ByteProcessor(10, 10);
		for (int y = 0; y < 2; y++)
		{
			for (int x = 0; x < 2; x++)
			{
				image.set(x + 2, y + 2, 255);
				image.set(x + 6, y + 2, 255);
				image.set(x + 4, y + 4, 255);
				image.set(x + 2, y + 6, 255);
				image.set(x + 6, y + 6, 255);
			}
		}
		return image;
	}
}
