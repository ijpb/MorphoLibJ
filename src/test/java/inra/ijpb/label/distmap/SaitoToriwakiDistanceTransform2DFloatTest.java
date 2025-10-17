/**
 * 
 */
package inra.ijpb.label.distmap;

import static org.junit.Assert.*;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import org.junit.Test;

/**
 * 
 */
public class SaitoToriwakiDistanceTransform2DFloatTest
{

	/**
	 * Test method for {@link inra.ijpb.label.distmap.SaitoToriwakiDistanceTransform2DFloat#distanceMap(ij.process.ImageProcessor, double[])}.
	 */
	@Test
	public final void testDistanceMap_centeredRectangle2d()
	{
        // Create a black image with a white 8-by-6 rectangle in the middle
        ImageProcessor array = new ByteProcessor(12, 10);
        for (int y = 2; y < 8; y++)
        {
            for (int x = 2; x < 10; x++)
            {
                array.set(x, y, 255);
            }
        }

        SaitoToriwakiDistanceTransform2DFloat algo = new SaitoToriwakiDistanceTransform2DFloat();
        FloatProcessor result = algo.distanceMap(array);
        
        assertNotNull(result);
        assertEquals(array.getWidth(), result.getWidth());
        assertEquals(array.getHeight(), result.getHeight());
        assertEquals(3, result.getf(4, 4), 0.001);
	}

	/**
	 * Test method for {@link inra.ijpb.label.distmap.SaitoToriwakiDistanceTransform2DFloat#distanceMap(ij.process.ImageProcessor, double[])}.
	 */
	@Test
	public final void testDistanceMap_fromCorners()
	{
        // Create a white image, with only the corners missing
        ImageProcessor array = new ByteProcessor(13, 9);
        for (int y = 0; y < 9; y++)
        {
            for (int x = 0; x < 13; x++)
            {
                array.set(x, y, 255);
            }
        }
        array.set(0, 0, 0);
        array.set(12, 0, 0);
        array.set(0, 8, 0);
        array.set(12, 8, 0);

        SaitoToriwakiDistanceTransform2DFloat algo = new SaitoToriwakiDistanceTransform2DFloat();
        FloatProcessor result = algo.distanceMap(array);
        
        assertNotNull(result);
        assertEquals(array.getWidth(), result.getWidth());
        assertEquals(array.getHeight(), result.getHeight());
        assertEquals(Math.hypot(4, 6), result.getf(6, 4), 0.001);
	}

	/**
	 * Test method for {@link inra.ijpb.label.distmap.SaitoToriwakiDistanceTransform2DFloat#distanceMap(ij.process.ImageProcessor, double[])}.
	 */
	@Test
	public final void testDistanceMap_fromCenter()
	{
        // Create a white image, with a black pixel in the middle
        ImageProcessor array = new ByteProcessor(13, 9);
        for (int y = 0; y < 9; y++)
        {
            for (int x = 0; x < 13; x++)
            {
                array.set(x, y, 255);
            }
        }
        array.set(6, 4, 0);

        SaitoToriwakiDistanceTransform2DFloat algo = new SaitoToriwakiDistanceTransform2DFloat();
        FloatProcessor result = algo.distanceMap(array);
        
        assertNotNull(result);
        assertEquals(array.getWidth(), result.getWidth());
        assertEquals(array.getHeight(), result.getHeight());
        assertEquals(Math.hypot(4, 6), result.getf(0, 0), 0.001);
        assertEquals(Math.hypot(4, 6), result.getf(12, 0), 0.001);
        assertEquals(Math.hypot(4, 6), result.getf(0, 8), 0.001);
        assertEquals(Math.hypot(4, 6), result.getf(12, 8), 0.001);
	}

}
