/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2023 INRA.
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
}
