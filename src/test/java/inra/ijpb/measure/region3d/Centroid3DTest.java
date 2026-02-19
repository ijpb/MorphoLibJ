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
package inra.ijpb.measure.region3d;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ij.ImageStack;
import inra.ijpb.geometry.Point3D;

/**
 * 
 */
public class Centroid3DTest
{

	/**
	 * Test method for {@link inra.ijpb.measure.region3d.Centroid3D#analyzeRegions(ij.ImageStack, int[], ij.measure.Calibration)}.
	 */
	@Test
	public final void testAnalyzeRegionsImageStackIntArrayCalibration()
	{
		ImageStack array = createImage_boxes();
//		ImageUtils.print(array);
		
		Centroid3D op = new Centroid3D();
		int[] labels = new int[] {111, 144, 414, 441, 444, 177, 717, 771, 777};
		Point3D[] centroids = op.analyzeRegions(array, labels, null);
		
		assertEquals(labels.length, centroids.length);
		
		// region composed of a single pixel -> centroid in the middle of the pixel
		assertEquals(1.5, centroids[0].getX(), 0.01);
		assertEquals(1.5, centroids[0].getY(), 0.01);
		assertEquals(1.5, centroids[0].getZ(), 0.01);
		
		// region of 2-by-2 pixels -> integer coordinates of centroid
		assertEquals(4.0, centroids[4].getX(), 0.01);
		assertEquals(4.0, centroids[4].getY(), 0.01);
		assertEquals(4.0, centroids[4].getZ(), 0.01);
		
		// region of 3-by-3 pixels -> centroid in the middle of the center voxel
		assertEquals(7.5, centroids[8].getX(), 0.01);
		assertEquals(7.5, centroids[8].getY(), 0.01);
		assertEquals(7.5, centroids[8].getZ(), 0.01);
	}

	private static final ImageStack createImage_boxes()
	{
		ImageStack array = ImageStack.create(10, 10, 10, 16);
		array.setVoxel(1, 1, 1, 111);
		
		for (int i = 0; i < 2; i++)
		{
			array.setVoxel(i+3, 1, 1, 411);
			array.setVoxel(1, i+3, 1, 141);
			array.setVoxel(1, 1, i+3, 114);
			
			array.setVoxel(i+3, 3, 3, 444);
			array.setVoxel(i+3, 4, 3, 444);
			array.setVoxel(i+3, 3, 4, 444);
			array.setVoxel(i+3, 4, 4, 444);
		}
		
		for (int i = 0; i < 3; i++)
		{
			array.setVoxel(i+6, 1, 1, 711);
			array.setVoxel(1, i+6, 1, 171);
			array.setVoxel(1, 1, i+6, 117);
			
			for (int j = 0; j < 3; j++)
				for (int k = 0; k < 3; k++)
					array.setVoxel(i+6, j+6, k+6, 777);
		}
		
		return array;
	}
}
