package inra.ijpb.binary.distmap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.binary.ChamferWeights;

import org.junit.Test;

public class DistanceTransform5x5ShortTest {

	@Test
	public final void testDistanceMap_ChessBoard() 
	{
		ByteProcessor image = new ByteProcessor(12, 10);
		image.setBackgroundValue(0);
		image.setValue(0);
		image.fill();
		for (int y = 2; y < 8; y++)
		{
			for (int x = 2; x < 10; x++)
			{
				image.set(x, y, 255);
			}
		}
		
		short[] weights = ChamferWeights.CHESSBOARD.getShortWeights();
		DistanceTransform5x5Short algo = new DistanceTransform5x5Short(weights, true);
		ImageProcessor result = algo.distanceMap(image);
		
		assertNotNull(result);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(3, result.get(4, 4));
	}
	
	@Test
	public final void testDistanceMap_UntilCorners_CityBlock()
	{
		ByteProcessor image = new ByteProcessor(7, 7);
		image.setValue(255);
		image.fill();
		image.set(4, 4, 0);
		
		
		short[] weights = ChamferWeights.CITY_BLOCK.getShortWeights();
		DistanceTransform5x5Short algo = new DistanceTransform5x5Short(weights, false);
		ImageProcessor result = algo.distanceMap(image);
		
		assertNotNull(result);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(8, result.get(0, 0));
		assertEquals(6, result.get(6, 0));
		assertEquals(6, result.get(0, 6));
		assertEquals(4, result.get(6, 6));
		
		assertEquals(5, result.get(0, 5));
	}

	@Test
	public final void testDistanceMap_UntilCorners_Chessboard() 
	{
		ByteProcessor image = new ByteProcessor(7, 7);
		image.setValue(255);
		image.fill();
		image.set(4, 4, 0);
		
		
		short[] weights = ChamferWeights.CHESSBOARD.getShortWeights();
		DistanceTransform5x5Short algo = new DistanceTransform5x5Short(weights, false);
		ImageProcessor result = algo.distanceMap(image);
		
		assertNotNull(result);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(4, result.get(0, 0));
		assertEquals(4, result.get(6, 0));
		assertEquals(4, result.get(0, 6));
		assertEquals(2, result.get(6, 6));
		
		assertEquals(4, result.get(0, 5));
	}
	
	@Test
	public final void testDistanceMap_UntilCorners_Weights23() 
	{
		ByteProcessor image = new ByteProcessor(7, 7);
		image.setValue(255);
		image.fill();
		image.set(4, 4, 0);
		
		
		short[] weights = ChamferWeights.WEIGHTS_23.getShortWeights();
		DistanceTransform5x5Short algo = new DistanceTransform5x5Short(weights, false);
		ImageProcessor result = algo.distanceMap(image);
		
		assertNotNull(result);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(12, result.get(0, 0));
		assertEquals(10, result.get(6, 0));
		assertEquals(10, result.get(0, 6));
		assertEquals(6, result.get(6, 6));
		
		assertEquals(9, result.get(0, 5));
	}
	
	@Test
	public final void testDistanceMap_UntilCorners_Borgefors34() 
	{
		ByteProcessor image = new ByteProcessor(7, 7);
		image.setValue(255);
		image.fill();
		image.set(4, 4, 0);
		
		
		short[] weights = ChamferWeights.BORGEFORS.getShortWeights();
		DistanceTransform5x5Short algo = new DistanceTransform5x5Short(weights, false);
		ImageProcessor result = algo.distanceMap(image);
		
		assertNotNull(result);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(16, result.get(0, 0));
		assertEquals(14, result.get(6, 0));
		assertEquals(14, result.get(0, 6));
		assertEquals(8, result.get(6, 6));
		
		assertEquals(13, result.get(0, 5));
	}
	
	@Test
	public final void testDistanceMap_UntilCorners_ChessKnight()
	{
		ByteProcessor image = new ByteProcessor(7, 7);
		image.setValue(255);
		image.fill();
		image.set(4, 4, 0);
		
		
		short[] weights = ChamferWeights.CHESSKNIGHT.getShortWeights();
		DistanceTransform5x5Short algo = new DistanceTransform5x5Short(weights, false);
		ImageProcessor result = algo.distanceMap(image);
		
		assertNotNull(result);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(28, result.get(0, 0));
		assertEquals(22, result.get(6, 0));
		assertEquals(22, result.get(0, 6));
		assertEquals(14, result.get(6, 6));
		
		assertEquals(20, result.get(0, 4));
	}
	
	/**
	 * Another test for chess-knight weights, to fix a bug that incorrectly
	 * checked image bounds.
	 */
	@Test
	public final void testDistanceMap_UntilCorners_ChessKnight2()
	{
		ByteProcessor image = new ByteProcessor(9, 9);
		image.setValue(255);
		image.fill();
		image.set(6, 6, 0);
		
		short[] weights = ChamferWeights.CHESSKNIGHT.getShortWeights();
		DistanceTransform5x5Short algo = new DistanceTransform5x5Short(weights, false);
		ImageProcessor result = algo.distanceMap(image);
		
		assertNotNull(result);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(42, result.get(0, 0));
		assertEquals(32, result.get(8, 0));
		assertEquals(32, result.get(0, 8));
		assertEquals(14, result.get(8, 8));
		
		assertEquals(30, result.get(0, 6));
	}
}
