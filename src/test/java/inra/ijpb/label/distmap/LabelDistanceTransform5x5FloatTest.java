/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2017 INRA.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
package inra.ijpb.label.distmap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.binary.ChamferWeights;

import org.junit.Test;

public class LabelDistanceTransform5x5FloatTest {

	@Test
	public final void testDistanceMap_ChessBoard() 
	{
		ByteProcessor image = new ByteProcessor(12, 10);
		image.setBackgroundValue(0);
		image.fill();
		for (int y = 2; y < 8; y++) {
			for (int x = 2; x < 10; x++) {
				image.set(x, y, 255);
			}
		}
		
		float[] weights = ChamferWeights.CHESSBOARD.getFloatWeights();
		LabelDistanceTransform5x5Float algo = new LabelDistanceTransform5x5Float(weights, true);
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
		
		float[] weights = ChamferWeights.CITY_BLOCK.getFloatWeights();
		LabelDistanceTransform5x5Float algo = new LabelDistanceTransform5x5Float(weights, false);
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
	public final void testDistanceMap_UntilCorners_Chessboard()
	{
		ByteProcessor image = new ByteProcessor(7, 7);
		image.setValue(255);
		image.fill();
		image.set(4, 4, 0);
		
		float[] weights = ChamferWeights.CHESSBOARD.getFloatWeights();
		LabelDistanceTransform5x5Float algo = new LabelDistanceTransform5x5Float(weights, false);
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
	public final void testDistanceMap_UntilCorners_Weights23() 
	{
		ByteProcessor image = new ByteProcessor(7, 7);
		image.setValue(255);
		image.fill();
		image.set(4, 4, 0);
		
		float[] weights = ChamferWeights.WEIGHTS_23.getFloatWeights();
		LabelDistanceTransform5x5Float algo = new LabelDistanceTransform5x5Float(weights, false);
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
	public final void testDistanceMap_UntilCorners_Borgefors34() 
	{
		ByteProcessor image = new ByteProcessor(7, 7);
		image.setValue(255);
		image.fill();
		image.set(4, 4, 0);
		
		float[] weights = ChamferWeights.BORGEFORS.getFloatWeights();
		LabelDistanceTransform5x5Float algo = new LabelDistanceTransform5x5Float(weights, false);
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
		
		float[] weights = ChamferWeights.CHESSKNIGHT.getFloatWeights();
		LabelDistanceTransform5x5Float algo = new LabelDistanceTransform5x5Float(weights, false);
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
