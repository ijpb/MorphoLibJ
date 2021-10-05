/**
 * 
 */
package inra.ijpb.binary.distmap;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * @author dlegland
 *
 */
public class ChamferDistanceTransform2DFloatTest
{

	/**
	 * Test method for {@link inra.ijpb.binary.distmap.ChamferDistanceTransform2DFloat#distanceMap(ij.process.ImageProcessor)}.
	 */
	@Test
	public final void testDistanceMap()
	{
		ByteProcessor image = new ByteProcessor(12, 10);
		image.setBackgroundValue(0);
		image.fill();
		for (int y = 2; y < 8; y++)
		{
			for (int x = 2; x < 10; x++)
			{
				image.set(x, y, 255);
			}
		}

		ChamferMask2D weights = ChamferMask2D.CHESSBOARD;
		DistanceTransform algo = new ChamferDistanceTransform2DFloat(weights, true);
		ImageProcessor result = algo.distanceMap(image);

		assertNotNull(result);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(3, result.getf(4, 4), 1e-12);
	}

	@Test
	public final void testDistanceMap_UntilCorners_CityBlock() 
	{
		ByteProcessor image = new ByteProcessor(7, 7);
		image.setValue(255);
		image.fill();
		image.set(4, 4, 0);

		ChamferMask2D weights = ChamferMask2D.CITY_BLOCK;
		DistanceTransform algo = new ChamferDistanceTransform2DFloat(weights, false);
		ImageProcessor result = algo.distanceMap(image);

		assertNotNull(result);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(8, result.getf(0, 0), .01);
		assertEquals(6, result.getf(6, 0), .01);
		assertEquals(6, result.getf(0, 6), .01);
		assertEquals(4, result.getf(6, 6), .01);
	}

	@Test
	public final void testDistanceMap_UntilCorners_Chessboard() 
	{
		ByteProcessor image = new ByteProcessor(7, 7);
		image.setValue(255);
		image.fill();
		image.set(4, 4, 0);

		ChamferMask2D weights = ChamferMask2D.CHESSBOARD;
		DistanceTransform algo = new ChamferDistanceTransform2DFloat(weights, false);
		ImageProcessor result = algo.distanceMap(image);

		assertNotNull(result);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(4, result.getf(0, 0), .01);
		assertEquals(4, result.getf(6, 0), .01);
		assertEquals(4, result.getf(0, 6), .01);
		assertEquals(2, result.getf(6, 6), .01);
	}

	@Test
	public final void testDistanceMap_UntilCorners_Weights23() 
	{
		ByteProcessor image = new ByteProcessor(7, 7);
		image.setValue(255);
		image.fill();
		image.set(4, 4, 0);

		ChamferMask2D weights = new ChamferMask2DW2(2, 3);
		DistanceTransform algo = new ChamferDistanceTransform2DFloat(weights, false);
		ImageProcessor result = algo.distanceMap(image);

		assertNotNull(result);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(12, result.getf(0, 0), .01);
		assertEquals(10, result.getf(6, 0), .01);
		assertEquals(10, result.getf(0, 6), .01);
		assertEquals(6, result.getf(6, 6), .01);
	}

	@Test
	public final void testDistanceMap_UntilCorners_Borgefors34() 
	{
		ByteProcessor image = new ByteProcessor(7, 7);
		image.setValue(255);
		image.fill();
		image.set(4, 4, 0);

		ChamferMask2D weights = ChamferMask2D.BORGEFORS;
		DistanceTransform algo = new ChamferDistanceTransform2DFloat(weights, false);
		ImageProcessor result = algo.distanceMap(image);

		assertNotNull(result);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(16, result.getf(0, 0), .01);
		assertEquals(14, result.getf(6, 0), .01);
		assertEquals(14, result.getf(0, 6), .01);
		assertEquals(8, result.getf(6, 6), .01);
	}
	
	@Test
	public final void testDistanceMap_UntilCorners_ChessKnight() 
	{
		ByteProcessor image = new ByteProcessor(7, 7);
		image.setValue(255);
		image.fill();
		image.set(4, 4, 0);

		ChamferMask2D weights = ChamferMask2D.CHESSKNIGHT;
		DistanceTransform algo = new ChamferDistanceTransform2DFloat(weights, false);
		ImageProcessor result = algo.distanceMap(image);

		assertNotNull(result);
		assertEquals(image.getWidth(), result.getWidth());
		assertEquals(image.getHeight(), result.getHeight());
		assertEquals(10, result.getf(4, 6), .01);
		assertEquals(14, result.getf(6, 6), .01);
		assertEquals(28, result.getf(0, 0), .01);
		assertEquals(22, result.getf(6, 0), .01);
		assertEquals(22, result.getf(0, 6), .01);
	}
	
	/**
	 * Test method for {@link inra.ijpb.label.distmap.LabelDistanceTransform3x3Float#distanceMap(ij.process.ImageProcessor)}.
	 */
	@Test
	public final void testDistanceMap_TouchingLabels()
	{
		ByteProcessor image = new ByteProcessor(8, 8);
		for (int y = 0; y < 3; y++)
		{
			for (int x = 0; x < 3; x++)
			{
				image.set(x+1, y+1, 1);
				image.set(x+4, y+1, 2);
				image.set(x+1, y+4, 3);
				image.set(x+4, y+4, 4);
			}
		}
		
		ChamferMask2D weights = ChamferMask2D.BORGEFORS;
		DistanceTransform dt = new ChamferDistanceTransform2DFloat(weights, true);
		ImageProcessor distMap = dt.distanceMap(image);

		// value 0 in backgrounf
		assertEquals(0, distMap.getf(0, 0), .1);
		assertEquals(0, distMap.getf(5, 0), .1);
		assertEquals(0, distMap.getf(7, 7), .1);

		// value equal to 2 in the middle of the labels
		assertEquals(2, distMap.getf(2, 2), .1);
		assertEquals(2, distMap.getf(5, 2), .1);
		assertEquals(2, distMap.getf(2, 5), .1);
		assertEquals(2, distMap.getf(5, 5), .1);
		
		// value equal to 1 on the border of the labels
		assertEquals(1, distMap.getf(1, 3), .1);
		assertEquals(1, distMap.getf(3, 3), .1);
		assertEquals(1, distMap.getf(4, 3), .1);
		assertEquals(1, distMap.getf(6, 3), .1);
		assertEquals(1, distMap.getf(1, 6), .1);
		assertEquals(1, distMap.getf(3, 6), .1);
		assertEquals(1, distMap.getf(4, 6), .1);
		assertEquals(1, distMap.getf(6, 6), .1);
	}

}
