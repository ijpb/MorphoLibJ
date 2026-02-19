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

import java.util.Map;

import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import inra.ijpb.geometry.PointPair2D;

/**
 * @author dlegland
 *
 */
public class MaxFeretDiameterTest
{

	/**
	 * Test method for {@link inra.ijpb.measure.region2d.MaxFeretDiameter#analyzeRegions(ij.ImagePlus)}.
	 */
	@Test
	public void testAnalyzeRegions_circles()
	{
		ImagePlus imagePlus = IJ.openImage(getClass().getResource("/files/circles.tif").getFile());
	
		MaxFeretDiameter algo = new MaxFeretDiameter();
		
		Map<Integer, PointPair2D> maxFeretDiams = algo.analyzeRegions(imagePlus);

		assertEquals(1, maxFeretDiams.size());
		
		PointPair2D diam = maxFeretDiams.get(255);
		
		assertEquals(272.7, diam.diameter(), .2);
		
		ResultsTable table = algo.createTable(maxFeretDiams);
		assertEquals(1, table.size());
	}

	/**
	 * Test method for {@link inra.ijpb.measure.region2d.MaxFeretDiameter#analyzeRegions(ij.ImagePlus)}.
	 */
	@Test
	public void testAnalyzeRegions_riceGrains()
	{
		ImagePlus imagePlus = IJ.openImage(getClass().getResource("/files/grains-med-WTH-lbl.tif").getFile());
	
		MaxFeretDiameter algo = new MaxFeretDiameter();
		Map<Integer, PointPair2D> maxFeretDiams = algo.analyzeRegions(imagePlus);
		
		assertEquals(96, maxFeretDiams.size());
		
		ResultsTable table = algo.createTable(maxFeretDiams);
		assertEquals(96, table.size());
	}
}
