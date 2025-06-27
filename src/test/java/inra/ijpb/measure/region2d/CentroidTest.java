/**
 * 
 */
package inra.ijpb.measure.region2d;

import static org.junit.Assert.*;

import java.awt.geom.Point2D;

import org.junit.Test;

import ij.process.ByteProcessor;

/**
 * 
 */
public class CentroidTest
{

	/**
	 * Test method for {@link inra.ijpb.measure.region2d.Centroid#analyzeRegions(ij.ImagePlus)}.
	 */
	@Test
	public final void testAnalyzeRegions_rectangles()
	{
		ByteProcessor array = createImage_rectangles();
//		ImageUtils.print(array);
		
		Centroid op = new Centroid();
		int[] labels = new int[] {11, 14, 41, 44, 17, 71, 77};
		Point2D[] centroids = op.analyzeRegions(array, labels, null);
		
		assertEquals(labels.length, centroids.length);
		
		// region composed of a single pixel -> centroid in the middle of the pixel
		assertEquals(1.5, centroids[0].getX(), 0.01);
		assertEquals(1.5, centroids[0].getY(), 0.01);
		
		// region of 2-by-2 pixels -> integer coordinates of centroid
		assertEquals(4.0, centroids[3].getX(), 0.01);
		assertEquals(4.0, centroids[3].getY(), 0.01);
		
		// region of 3-by-3 pixels -> centroid in the middle of the center pixel
		assertEquals(7.5, centroids[6].getX(), 0.01);
		assertEquals(7.5, centroids[6].getY(), 0.01);
	}

	private static final ByteProcessor createImage_rectangles()
	{
		ByteProcessor array = new ByteProcessor(10, 10);
		array.set(1, 1, 11);
		
		for (int i = 0; i < 2; i++)
		{
			array.set(i+3, 1, 41);
			array.set(1, i+3, 14);
			
			array.set(i+3, 3, 44);
			array.set(i+3, 4, 44);
		}
		
		for (int i = 0; i < 3; i++)
		{
			array.set(i+6, 1, 71);
			array.set(1, i+6, 17);
			
			array.set(i+6, 6, 77);
			array.set(i+6, 7, 77);
			array.set(i+6, 8, 77);
		}
		
		return array;
	}
}
