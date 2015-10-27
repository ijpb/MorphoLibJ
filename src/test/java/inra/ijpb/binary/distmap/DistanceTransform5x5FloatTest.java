package inra.ijpb.binary.distmap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.binary.ChamferWeights;

import org.junit.Test;

public class DistanceTransform5x5FloatTest {

	@Test
	public final void testDistanceMap_ChessBoard() {
		ByteProcessor image = new ByteProcessor(12, 10);
		image.setBackgroundValue(0);
		image.fill();
		for (int y = 2; y < 8; y++) {
			for (int x = 2; x < 10; x++) {
				image.set(x, y, 255);
			}
		}
		
		float[] weights = ChamferWeights.CHESSBOARD.getFloatWeights();
		DistanceTransform5x5Float algo = new DistanceTransform5x5Float(weights, true);
		ImageProcessor result = algo.distanceMap(image);
		
		assertNotNull(result);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(3, result.getf(4, 4), 1e-12);
	}
	
	@Test
	public final void testDistanceMap_UntilCorners_CityBlock() {
		ByteProcessor image = new ByteProcessor(7, 7);
		image.setValue(255);
		image.fill();
		image.set(4, 4, 0);
		
		float[] weights = ChamferWeights.CITY_BLOCK.getFloatWeights();
		DistanceTransform5x5Float algo = new DistanceTransform5x5Float(weights, false);
		ImageProcessor result = algo.distanceMap(image);
		
		assertNotNull(result);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(8, result.getf(0, 0), .01);
		assertEquals(6, result.getf(6, 0), .01);
		assertEquals(6, result.getf(0, 6), .01);
		assertEquals(4, result.getf(6, 6), .01);
		assertEquals(5, result.getf(0, 5), .01);
	}

	@Test
	public final void testDistanceMap_UntilCorners_Chessboard() {
		ByteProcessor image = new ByteProcessor(7, 7);
		image.setValue(255);
		image.fill();
		image.set(4, 4, 0);
		
		float[] weights = ChamferWeights.CHESSBOARD.getFloatWeights();
		DistanceTransform5x5Float algo = new DistanceTransform5x5Float(weights, false);
		ImageProcessor result = algo.distanceMap(image);
		
		assertNotNull(result);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(4, result.getf(0, 0), .01);
		assertEquals(4, result.getf(6, 0), .01);
		assertEquals(4, result.getf(0, 6), .01);
		assertEquals(2, result.getf(6, 6), .01);
		
		assertEquals(4, result.getf(0, 5), .01);
	}
	
	@Test
	public final void testDistanceMap_UntilCorners_Weights23() {
		ByteProcessor image = new ByteProcessor(7, 7);
		image.setValue(255);
		image.fill();
		image.set(4, 4, 0);
		
		float[] weights = ChamferWeights.WEIGHTS_23.getFloatWeights();
		DistanceTransform5x5Float algo = new DistanceTransform5x5Float(weights, false);
		ImageProcessor result = algo.distanceMap(image);
		
		assertNotNull(result);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(12, result.getf(0, 0), .01);
		assertEquals(10, result.getf(6, 0), .01);
		assertEquals(10, result.getf(0, 6), .01);
		assertEquals(6, result.getf(6, 6), .01);
		
		assertEquals(9, result.getf(0, 5), .01);
	}
	
	@Test
	public final void testDistanceMap_UntilCorners_Borgefors34() {
		ByteProcessor image = new ByteProcessor(7, 7);
		image.setValue(255);
		image.fill();
		image.set(4, 4, 0);
		
		float[] weights = ChamferWeights.BORGEFORS.getFloatWeights();
		DistanceTransform5x5Float algo = new DistanceTransform5x5Float(weights, false);
		ImageProcessor result = algo.distanceMap(image);
		
		assertNotNull(result);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(16, result.getf(0, 0), .01);
		assertEquals(14, result.getf(6, 0), .01);
		assertEquals(14, result.getf(0, 6), .01);
		assertEquals(8, result.getf(6, 6), .01);
		
		assertEquals(13, result.getf(0, 5), .01);
	}
	
	/**
	 * Another test for chessknight weigths, to fix a bug that incorrectly
	 * checked image bounds.
	 */
	@Test
	public final void testDistanceMap_UntilCorners_ChessKnight2() {
		ByteProcessor image = new ByteProcessor(9, 9);
		image.setValue(255);
		image.fill();
		image.set(6, 6, 0);
		
		float[] weights = ChamferWeights.CHESSKNIGHT.getFloatWeights();
		DistanceTransform5x5Float algo = new DistanceTransform5x5Float(weights, false);
		ImageProcessor result = algo.distanceMap(image);
		
		assertNotNull(result);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(42, result.getf(0, 0), .01);
		assertEquals(32, result.getf(8, 0), .01);
		assertEquals(32, result.getf(0, 8), .01);
		assertEquals(14, result.getf(8, 8), .01);
		
		assertEquals(30, result.getf(0, 6), .01);
	}
}
