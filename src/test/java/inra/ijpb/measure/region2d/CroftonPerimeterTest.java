/**
 * 
 */
package inra.ijpb.measure.region2d;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.measure.Calibration;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * @author dlegland
 *
 */
public class CroftonPerimeterTest
{

	
	/**
	 * Initialize 20x20 image with a disk of radius 8 in the middle, resulting
	 * in expected perimeter equal to 50.2655. The center of the disk is 
	 * slightly shifted to reduce discretization artifacts.
	 * 
	 * Test method for {@link ijt.measure.geometric.GeometricMeasures2D#croftonPerimeter_D2(ij.process.ImageProcessor, double[])}.
	 */
	@Test
	public final void testAnalyzeRegionsImageProcessorIntArrayCalibration_D2_DiskR8()
	{
		ImageProcessor image = createDiskR8Image();
		int[] labels = new int[]{255};
		CroftonPerimeter algo = new CroftonPerimeter(2);
		
		double perims[] = algo.analyzeRegions(image, labels, new Calibration());

		assertEquals(1, perims.length);

		double exp = 2 * Math.PI * 8;
		// relative error is expected to be lower than 22% for four directions
		assertEquals(exp, perims[0], exp * .22);
	}

	/**
	 * Initialize 20x20 image with a disk of radius 8 in the middle, resulting
	 * in expected perimeter equal to 50.2655. The center of the disk is 
	 * slightly shifted to reduce discretization artifacts.
	 * 
	 * Test method for {@link ijt.measure.geometric.GeometricMeasures2D#croftonPerimeterD4(ij.process.ImageProcessor, double[])}.
	 */
	@Test
	public final void testAnalyzeRegionsImageProcessorIntArrayCalibration_D4_DiskR8() 
	{
		ImageProcessor image = createDiskR8Image();
		int[] labels = new int[]{255};
		CroftonPerimeter algo = new CroftonPerimeter(4);
		
		double perims[] = algo.analyzeRegions(image, labels, new Calibration());

		assertEquals(1, perims.length);

		double exp = 2 * Math.PI * 8;
		// relative error is expected to be lower than 5.2% for four directions
		assertEquals(exp, perims[0], exp * .052);
	}

	private final ImageProcessor createDiskR8Image() 
	{
		ImageProcessor image = new ByteProcessor(20, 20);
		for (int y = 0; y < 20; y++) 
		{
			for (int x = 0; x < 20; x++)
			{
				double d = Math.hypot(x - 10.12, y - 10.23);
				if (d <= 8) 
					image.set(x, y, 255);
			}
		}
		return image;
	}
	
}
