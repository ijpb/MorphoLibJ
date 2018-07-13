/**
 * 
 */
package inra.ijpb.measure.region2d;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ij.measure.Calibration;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * @author dlegland
 *
 */
public class AreaTest
{

	/**
	 * Test method for {@link inra.ijpb.measure.region2d.Area#countRegionPixels(ij.process.ImageProcessor, int)}.
	 */
	@Test
	public final void testCountRegionPixels()
	{
		// initialize image with a square of side 4 in the middle
		ImageProcessor image = new ByteProcessor(10, 10);
		for (int y = 3; y < 7; y++)
		{
			for (int x = 3; x < 7; x++)
			{
				image.set(x, y, 255);
			}
		}

		int area = Area.countRegionPixels(image, 255);
		assertEquals(16, area);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.region2d.Area#analyzeRegions(ij.process.ImageProcessor, int[], ij.measure.Calibration)}.
	 */
	@Test
	public final void testAnalyzeRegionsImageProcessorIntArrayCalibration()
	{
		// initialize an image with several rectangles of different sizes
		// labels are: 2, 4, 5, 9
		ImageProcessor image = new ByteProcessor(10, 10);
		image.set(1, 1, 2);
		for (int i = 0; i < 6; i++)
		{
			image.set(1, i+3, 4);
			image.set(i+3, 1, 5);
		}
		for (int i = 0; i < 6; i++)
		{
			for (int j = 0; j < 6; j++)
			{
				image.set(i+3, j+3, 9);
			}
		}

		
		int[] labels = new int[] {2, 4, 5, 9};
		Calibration calib = new Calibration();
		Double[] areaList = new Area().analyzeRegions(image, labels, calib);
		
		assertEquals(4, areaList.length);
		assertEquals(1.0, areaList[0], .01);
		assertEquals(6.0, areaList[1], .01);
		assertEquals(6.0, areaList[2], .01);
		assertEquals(36.0, areaList[3], .01);
	}

}
