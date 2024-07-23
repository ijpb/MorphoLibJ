/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.geometry.OrientedBox2D;

/**
 * @author dlegland
 *
 */
public class OrientedBoundingBox2DTest
{

	/**
	 * Test method for {@link inra.ijpb.measure.region2d.OrientedBoundingBox2D#analyzeRegions(ij.process.ImageProcessor, int[], ij.measure.Calibration)}.
	 */
	@Test
	public final void testAnalyzeRegions_rectangles()
	{
		ImageProcessor image = new ByteProcessor(16, 10);
		for (int x = 1; x < 11; x++)
		{
			image.set(x, 1, 3);
			image.set(x, 2, 3);
			image.set(x, 3, 3);
			image.set(x, 4, 3);
			image.set(x, 6, 4);
			image.set(x, 7, 4);
		}
		for (int y = 1; y < 8; y++)
		{
			image.set(13, y, 5);
			image.set(14, y, 5);
		}
		
		int[] labels = new int[] {3, 4, 5};
		Calibration calib = new Calibration();

		OrientedBoundingBox2D algo = new OrientedBoundingBox2D();
		OrientedBox2D[] boxes = algo.analyzeRegions(image, labels, calib);
		
		assertEquals(3, boxes.length);
		
		// first box
		OrientedBox2D box1 = boxes[0];
		assertEquals(10.0, box1.length(), .05);
		assertEquals( 4.0, box1.width(), .05);
		assertEquals( 0.0, box1.orientation(), .05);

		// third box
		OrientedBox2D box2 = boxes[1];
		assertEquals(10.0, box2.length(), .05);
		assertEquals( 2.0, box2.width(), .05);
		assertEquals( 0.0, box2.orientation(), .05);

		// second box
		OrientedBox2D box3 = boxes[2];
		assertEquals( 7.0, box3.length(), .05);
		assertEquals( 2.0, box3.width(), .05);
		assertEquals(90.0, box3.orientation(), .05);
	}


	/**
	 * Test method for {@link inra.ijpb.measure.region2d.OrientedBoundingBox2D#analyzeRegions(ij.process.ImageProcessor, int[], ij.measure.Calibration)}.
	 */
	@Test
	public final void testAnalyzeRegions_circles()
	{
		String fileName = getClass().getResource("/files/circles.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		assertNotNull(imagePlus);
		
		ImageProcessor image = imagePlus.getProcessor();
		int[] labels = new int[] {255};
		Calibration calib = new Calibration();

		OrientedBoundingBox2D algo = new OrientedBoundingBox2D();
		OrientedBox2D[] boxes = algo.analyzeRegions(image, labels, calib);
		
		assertEquals(1, boxes.length);
		OrientedBox2D box = boxes[0];
		
		// Length of oriented box
		assertEquals(272.23, box.length(), .05);
		// width of oriented box
		assertEquals(108.86, box.width(), .05);
	}


	@Test
	public final void testAnalyzeRegions_leafEricSmall()
	{
		String fileName = getClass().getResource("/files/CA_QK_004_H1_small_seg.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		assertNotNull(imagePlus);

		OrientedBoundingBox2D algo = new OrientedBoundingBox2D();
		Map<Integer, OrientedBox2D> boxes = algo.analyzeRegions(imagePlus);
		
		assertEquals(1, boxes.size());
		
		// first box
		OrientedBox2D box1 = boxes.get(255);
		assertTrue(box1.length() > 4.2);
		assertTrue(box1.width() > 2.0);
	}


}
