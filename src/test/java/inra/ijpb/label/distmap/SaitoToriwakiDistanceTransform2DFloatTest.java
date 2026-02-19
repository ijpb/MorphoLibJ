/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2026 INRA.
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
