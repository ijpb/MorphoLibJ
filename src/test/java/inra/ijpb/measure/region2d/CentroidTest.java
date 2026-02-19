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
