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
package inra.ijpb.binary.geodesic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.binary.distmap.ChamferMask2D;

public class GeodesicDistanceTransformFloatTest
{
	@Test
	public void testGeodesicDistanceMap_UShape_Borgefors()
	{
		ImageProcessor mask = new ByteProcessor(10, 8);
		mask.setValue(255);
		mask.fill();
		for(int y = 0; y < 6; y++)
		{
			for (int x = 3; x < 7; x++)
			{
				mask.set(x, y, 0);
			}
		}
		
		ImageProcessor marker = new ByteProcessor(10, 8);
		marker.set(0, 0, 255);
		
		ChamferMask2D chamferMask = ChamferMask2D.BORGEFORS;
		GeodesicDistanceTransform algo = new GeodesicDistanceTransformFloat(
				chamferMask, true);

		ImageProcessor map = algo.geodesicDistanceMap(marker, mask);

		assertEquals(17.0, map.getf(9, 0), 0.01);
	}
	
	@Test
	public void testGeodesicDistanceMap_UIShape_Borgefors()
	{
		ImageProcessor mask = new ByteProcessor(16, 8);
		mask.setValue(255);
		mask.fill();
		for(int y = 0; y < 6; y++)
		{
			for (int x = 3; x < 7; x++)
			{
				mask.set(x, y, 0);
			}
		}
		for(int y = 0; y < 8; y++)
		{
			for (int x = 10; x < 13; x++)
			{
				mask.set(x, y, 0);
			}
		}
		
		ImageProcessor marker = new ByteProcessor(16, 8);
		marker.set(0, 0, 255);
		
		ChamferMask2D chamferMask = ChamferMask2D.BORGEFORS;
		GeodesicDistanceTransform algo = new GeodesicDistanceTransformFloat(
				chamferMask, true);

		ImageProcessor map = algo.geodesicDistanceMap(marker, mask);

		assertEquals(17.0, map.getf(9, 0), 0.01);
		assertTrue(Float.isNaN(map.getf(5, 0)));
		assertTrue(Float.isInfinite(map.getf(15, 0)));
	}
	
	@Test
	public void testGeodesicDistanceMap_UShape_FloatArray()
	{
		ImageProcessor mask = new ByteProcessor(10, 8);
		mask.setValue(255);
		mask.fill();
		for(int y = 0; y < 6; y++)
		{
			for (int x = 3; x < 7; x++)
			{
				mask.set(x, y, 0);
			}
		}
		
		ImageProcessor marker = new ByteProcessor(10, 8);
		marker.set(0, 0, 255);
		
		ChamferMask2D chamferMask = ChamferMask2D.fromWeights(new float[] {3f, 4f});
		GeodesicDistanceTransform algo = new GeodesicDistanceTransformFloat(
				chamferMask, true);

		ImageProcessor map = algo.geodesicDistanceMap(marker, mask);

		assertEquals(17.0, map.getf(9, 0), 0.01);
		// pixels outside mask should have Float.NaN value 
		assertTrue(Float.isNaN(map.getf(4, 0)));
	}
	
	@Test
	public void testGeodesicDistanceMap_UShape_ChessKnight()
	{
		ImageProcessor mask = new ByteProcessor(10, 8);
		mask.setValue(255);
		mask.fill();
		for(int y = 0; y < 6; y++)
		{
			for (int x = 3; x < 7; x++)
			{
				mask.set(x, y, 0);
			}
		}
		
		ImageProcessor marker = new ByteProcessor(10, 8);
		marker.set(0, 0, 255);
		
		ChamferMask2D chamferMask = ChamferMask2D.CHESSKNIGHT;
		GeodesicDistanceTransform algo = new GeodesicDistanceTransformFloat(
				chamferMask, true);

		ImageProcessor map = algo.geodesicDistanceMap(marker, mask);

		// should obtain 81/5 = 16.4
		assertEquals(16.2, map.getf(9, 0), 0.01);
	}
	
	@Test
	public void testGeodesicDistanceMap_Circles_Borgefors()
	{
		ImagePlus maskPlus = IJ.openImage(getClass().getResource("/files/circles.tif").getFile());
		ImageProcessor mask = maskPlus.getProcessor();
		ImageProcessor marker = mask.duplicate();
		marker.fill();
		marker.set(30, 30, 255);

		GeodesicDistanceTransform algo = new GeodesicDistanceTransformFloat(
				ChamferMask2D.BORGEFORS, true);
		
		ImageProcessor map = algo.geodesicDistanceMap(marker, mask);

		assertEquals(259, map.getf(190, 211), .01);
	}

	@Test
	public void testGeodesicDistanceMap_Circles_ChessKnight()
	{
		ImagePlus maskPlus = IJ.openImage(getClass().getResource("/files/circles.tif").getFile());
		ImageProcessor mask = maskPlus.getProcessor();
		ImageProcessor marker = mask.duplicate();
		marker.fill();
		marker.set(30, 30, 255);

		GeodesicDistanceTransform algo = new GeodesicDistanceTransformFloat(
				ChamferMask2D.CHESSKNIGHT, true);
		ImageProcessor map = algo.geodesicDistanceMap(marker, mask);

		// expect 250.8
		assertEquals(250.8, map.getf(190, 210), 0.01);
	}

}
