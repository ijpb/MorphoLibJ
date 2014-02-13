package inra.ijpb.binary.distmap;

import static org.junit.Assert.*;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.plugins.ChamferDistanceMapPlugin;

import org.junit.Test;

public class ChamferDistance3x3FloatTest {

	@Test
	public final void testDistanceMapImageProcessor() {
		ByteProcessor image = new ByteProcessor(12, 10);
		image.setBackgroundValue(0);
		image.fill();
		for (int y = 2; y < 8; y++) {
			for (int x = 2; x < 10; x++) {
				image.set(x, y, 255);
			}
		}
		
		float[] weights = ChamferDistanceMapPlugin.Weights.CHESSBOARD.getFloatWeights();
		ChamferDistance3x3Float algo = new ChamferDistance3x3Float(weights, true);
		ImageProcessor result = algo.distanceMap(image);
		
		assertNotNull(result);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(3, result.getf(4, 4), 1e-12);
	}
}
