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
public class ConvexityTest
{

	/**
	 * Test method for {@link inra.ijpb.measure.region2d.Convexity#convexify(ij.process.ImageProcessor)}.
	 */
	@Test
	public final void testConvexify_simple()
	{
		ImageProcessor image = new ByteProcessor(8, 8);
		image.set(1, 1, 255);
		image.set(5, 1, 255);
		image.set(1, 5, 255);
		image.set(5, 5, 255);
		
		ImageProcessor convex = Convexity.convexify(image);
		
		assertEquals(255, convex.get(1, 1));
		assertEquals(255, convex.get(5, 1));
		assertEquals(255, convex.get(1, 5));
		assertEquals(255, convex.get(5, 5));
		assertEquals(255, convex.get(3, 1));
		assertEquals(255, convex.get(1, 3));
		assertEquals(255, convex.get(3, 3));
	}

	/**
	 * Test method for {@link inra.ijpb.measure.region2d.Convexity#analyzeRegions(ij.process.ImageProcessor, int[], ij.measure.Calibration)}.
	 */
	@Test
	public final void testAnalyzeRegionsImageProcessorIntArrayCalibration()
	{
		ImageProcessor image = new ByteProcessor(8, 8);
		image.set(1, 1, 255);
		image.set(5, 1, 255);
		image.set(1, 5, 255);
		image.set(5, 5, 255);
		int[] labels = new int[] {255};
		Calibration calib = new Calibration();
		
		Convexity algo = new Convexity();
		Convexity.Result[] results = algo.analyzeRegions(image, labels, calib);
		assertEquals(results.length, 1);
		
		Convexity.Result res1 = results[0];
		assertEquals(4, res1.area, .01);
		assertEquals(25, res1.convexArea, .01);
		assertEquals(4.0/25.0, res1.convexity, .01);
	}

    /**
     * Test method for {@link inra.ijpb.measure.region2d.Convexity#analyzeRegions(ij.process.ImageProcessor, int[], ij.measure.Calibration)}.
     */
    @Test
    public final void testAnalyzeRegionsImageProcessorIntArrayCalibration_FourLabels()
    {
        // create a 8x8 image with four regions corresponding to the corners of a 3x3 square
        ImageProcessor image = new ByteProcessor(8, 8);
        for (int y = 0; y < 4; y+=2)
        {
            for (int x = 0; x < 4; x+=2)
            {
                image.set(x + 1, y + 1, 2);
                image.set(x + 4, y + 1, 3);
                image.set(x + 1, y + 4, 4);
                image.set(x + 4, y + 4, 7);
            }
        }
        
        int[] labels = new int[] {2, 3, 4, 7};
        Calibration calib = new Calibration();
        
        Convexity algo = new Convexity();
        Convexity.Result[] results = algo.analyzeRegions(image, labels, calib);
        assertEquals(results.length, 4);
        
        for (int i = 0; i < 4; i++)
        {
            Convexity.Result res = results[0];
            assertEquals(4, res.area, .01);
            assertEquals(9, res.convexArea, .01);
            assertEquals(4.0/9.0, res.convexity, .01);
        }
    }

}
