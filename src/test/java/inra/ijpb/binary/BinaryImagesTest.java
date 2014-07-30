package inra.ijpb.binary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import org.junit.Test;

public class BinaryImagesTest {

	@Test
	public final void testDistanceMapImageProcessor() {
		ImageProcessor image = createBinarySquareImage();

		ImageProcessor result = BinaryImages.distanceMap(image);

		assertNotNull(result);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(3, result.get(4, 4), 1e-12);
	}

	@Test
	public final void testDistanceMapImageProcessorShortArrayBoolean() {
		ImageProcessor image = createBinarySquareImage();

		short[] weights = new short[]{3, 4};
		ImageProcessor result = BinaryImages.distanceMap(image, weights, true);

		assertNotNull(result);
		assertTrue(result instanceof ShortProcessor);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(3, result.get(4, 4), 1e-12);
	}

	@Test
	public final void testDistanceMapImageProcessorFloatArrayBoolean() {
		ImageProcessor image = createBinarySquareImage();

		float[] weights = new float[]{3f, 4f};
		ImageProcessor result = BinaryImages.distanceMap(image, weights, true);
		
//		for (int y = 0; y < image.getHeight(); y++) {
//			for (int x = 0; x < image.getWidth(); x++) {
//				System.out.print(" " + result.getf(x, y));
//			}
//			System.out.println("");
//		}

		assertNotNull(result);
		assertTrue(result instanceof FloatProcessor);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(3, result.getf(4, 4), 1e-12);
	}

	@Test
	public final void testAreaOpeningImageProcessor() 
	{
		// Create input image: four regions, with sizes 1, 5, 5, and 25
		ByteProcessor image = new ByteProcessor(10, 10);
		image.set(1, 1, 255);
		for (int i = 3; i < 8; i++) 
		{
			image.set(1, i, 255);
			image.set(i, 1, 255);
		}
		for (int y = 3; y < 8; y++) 
		{
			for (int x = 3; x < 8; x++) 
			{
				image.set(x, y, 255);
			}
		}
		
		// Remove only the first region
		ImageProcessor sizeOpen3 = BinaryImages.areaOpening(image, 3);
		assertEquals(0, sizeOpen3.get(1, 1));
		assertEquals(255, sizeOpen3.get(1, 5));
		assertEquals(255, sizeOpen3.get(5, 1));
		assertEquals(255, sizeOpen3.get(5, 5));
		
		// Remove the first 3 region
		ImageProcessor sizeOpen10 = BinaryImages.areaOpening(image, 10);
		assertEquals(0, sizeOpen10.get(1, 1));
		assertEquals(0, sizeOpen10.get(1, 5));
		assertEquals(0, sizeOpen10.get(5, 1));
		assertEquals(255, sizeOpen10.get(5, 5));
	}
	
	/**
	 * Creates a new binary image of a square.
	 * @return
	 */
	private final ImageProcessor createBinarySquareImage() {
		ByteProcessor image = new ByteProcessor(10, 10);
		image.setBackgroundValue(0);
		image.fill();
		for (int y = 2; y < 8; y++) {
			for (int x = 2; x < 8; x++) {
				image.set(x, y, 255);
			}
		}
		return image;
	}
}
