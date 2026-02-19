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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.data.image.ImageUtils;
import inra.ijpb.geometry.Circle2D;

/**
 * @author dlegland
 *
 */
public class LargestInscribedCircleTest
{
	/**
	 * Test method for {@link inra.ijpb.measure.region2d.LargestInscribedCircle#analyzeRegions(ij.ImagePlus)}.
	 */
	@Test
	public final void testAnalyzeRegionsImagePlus_Rectangles()
	{
		ImageProcessor image = new ByteProcessor(14, 9);
		image.set(1, 1, 1);
		ImageUtils.fillRect(image, 3, 1, 2, 2, 2); // diameter 2
		ImageUtils.fillRect(image, 1, 4, 4, 4, 3); // diameter 4
		ImageUtils.fillRect(image, 6, 1, 7, 7, 4); // diameter 7
		
		LargestInscribedCircle op = new LargestInscribedCircle();
		ResultsTable table = op.createTable(op.analyzeRegions(image, new Calibration()));
		
		assertEquals(1, table.getValue("InscrCircle.Radius", 0), .1);
		assertEquals(1, table.getValue("InscrCircle.Radius", 1), .1);
		assertEquals(2, table.getValue("InscrCircle.Radius", 2), .1);
		assertEquals(4, table.getValue("InscrCircle.Radius", 3), .1);
	}
	
	/**
	 * Test method for {@link inra.ijpb.measure.region2d.LargestInscribedCircle#analyzeRegions(ij.ImagePlus)}.
	 */
	@Test
	public final void testAnalyzeRegionsImagePlus_Rectangles_notAllLabels()
	{
		ImageProcessor image = new ByteProcessor(14, 9);
		image.set(1, 1, 1);
		ImageUtils.fillRect(image, 3, 1, 2, 2, 2); // diameter 2
		ImageUtils.fillRect(image, 1, 4, 4, 4, 3); // diameter 4
		ImageUtils.fillRect(image, 6, 1, 7, 7, 4); // diameter 7
		
		LargestInscribedCircle op = new LargestInscribedCircle();
		Circle2D[] circles = op.analyzeRegions(image, new int[] {2, 3}, new Calibration());
		assertEquals(2, circles.length);
		assertEquals(1.0, circles[0].getRadius(), 0.01);
		assertEquals(2.0, circles[1].getRadius(), 0.01);
	}
}
