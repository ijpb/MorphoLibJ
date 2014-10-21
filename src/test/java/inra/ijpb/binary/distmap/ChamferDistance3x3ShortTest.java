package inra.ijpb.binary.distmap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.binary.ChamferWeights;

import org.junit.Test;

public class ChamferDistance3x3ShortTest {

	@Test
	public final void testDistanceMapImageProcessor() {
		ByteProcessor image = new ByteProcessor(12, 10);
		image.setBackgroundValue(0);
		image.setValue(0);
		image.fill();
		for (int y = 2; y < 8; y++) {
			for (int x = 2; x < 10; x++) {
				image.set(x, y, 255);
			}
		}
		
		short[] weights = ChamferWeights.CHESSBOARD.getShortWeights();
		ChamferDistance3x3Short algo = new ChamferDistance3x3Short(weights, true);
		ImageProcessor result = algo.distanceMap(image);
		
		assertNotNull(result);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(3, result.get(4, 4));
	}
	
	@Test
	public final void testDistanceMap_UntilCorners_CityBlock() {
		ByteProcessor image = new ByteProcessor(7, 7);
		image.setValue(255);
		image.fill();
		image.set(4, 4, 0);
		
		
		short[] weights = ChamferWeights.CITY_BLOCK.getShortWeights();
		ChamferDistance3x3Short algo = new ChamferDistance3x3Short(weights, false);
		ImageProcessor result = algo.distanceMap(image);
		
		assertNotNull(result);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(8, result.get(0, 0));
		assertEquals(6, result.get(6, 0));
		assertEquals(6, result.get(0, 6));
		assertEquals(4, result.get(6, 6));
	}

	@Test
	public final void testDistanceMap_UntilCorners_Chessboard() {
		ByteProcessor image = new ByteProcessor(7, 7);
		image.setValue(255);
		image.fill();
		image.set(4, 4, 0);
		
		
		short[] weights = ChamferWeights.CHESSBOARD.getShortWeights();
		ChamferDistance3x3Short algo = new ChamferDistance3x3Short(weights, false);
		ImageProcessor result = algo.distanceMap(image);
		
		assertNotNull(result);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(4, result.get(0, 0));
		assertEquals(4, result.get(6, 0));
		assertEquals(4, result.get(0, 6));
		assertEquals(2, result.get(6, 6));
	}
	
	@Test
	public final void testDistanceMap_UntilCorners_Weights23() {
		ByteProcessor image = new ByteProcessor(7, 7);
		image.setValue(255);
		image.fill();
		image.set(4, 4, 0);
		
		
		short[] weights = ChamferWeights.WEIGHTS_23.getShortWeights();
		ChamferDistance3x3Short algo = new ChamferDistance3x3Short(weights, false);
		ImageProcessor result = algo.distanceMap(image);
		
		assertNotNull(result);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(12, result.get(0, 0));
		assertEquals(10, result.get(6, 0));
		assertEquals(10, result.get(0, 6));
		assertEquals(6, result.get(6, 6));
	}
	
	@Test
	public final void testDistanceMap_UntilCorners_Borgefors34() {
		ByteProcessor image = new ByteProcessor(7, 7);
		image.setValue(255);
		image.fill();
		image.set(4, 4, 0);
		
		
		short[] weights = ChamferWeights.BORGEFORS.getShortWeights();
		ChamferDistance3x3Short algo = new ChamferDistance3x3Short(weights, false);
		ImageProcessor result = algo.distanceMap(image);
		
		assertNotNull(result);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(16, result.get(0, 0));
		assertEquals(14, result.get(6, 0));
		assertEquals(14, result.get(0, 6));
		assertEquals(8, result.get(6, 6));
	}
}
