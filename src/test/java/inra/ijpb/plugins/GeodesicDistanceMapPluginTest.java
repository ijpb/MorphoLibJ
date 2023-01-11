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
package inra.ijpb.plugins;

import static org.junit.Assert.*;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import inra.ijpb.binary.distmap.ChamferMask2D;

import org.junit.Test;

/**
 * @author dlegland
 *
 */
public class GeodesicDistanceMapPluginTest
{

	/**
	 * Test method for {@link inra.ijpb.plugins.GeodesicDistanceMapPlugin#process(ij.ImagePlus, ij.ImagePlus, java.lang.String, float[], boolean)}.
	 */
	@Test
	public void testProcess_Float()
	{
		ImagePlus maskPlus = IJ.openImage(getClass().getResource("/files/circles.tif").getFile());
		ImageProcessor mask = maskPlus.getProcessor();
		ImageProcessor marker = mask.duplicate();
		marker.fill();
		marker.set(30, 30, 255);
		ImagePlus markerPlus = new ImagePlus("marker", marker);
		
		GeodesicDistanceMapPlugin plugin = new GeodesicDistanceMapPlugin();
		ChamferMask2D chamferMask = ChamferMask2D.BORGEFORS;
		ImagePlus mapPlus = plugin.process(markerPlus, maskPlus, "map", chamferMask, true, true);
		ImageProcessor map = mapPlus.getProcessor();
	
		assertEquals(259.66, map.getf(190, 213), .01);
	}

	/**
	 * Test method for {@link inra.ijpb.plugins.GeodesicDistanceMapPlugin#process(ij.ImagePlus, ij.ImagePlus, java.lang.String, short[], boolean)}.
	 */
	@Test
	public void testProcess_Short()
	{
		ImagePlus maskPlus = IJ.openImage(getClass().getResource("/files/circles.tif").getFile());
		ImageProcessor mask = maskPlus.getProcessor();
		ImageProcessor marker = mask.duplicate();
		marker.fill();
		marker.set(30, 30, 255);
		ImagePlus markerPlus = new ImagePlus("marker", marker);
		
		GeodesicDistanceMapPlugin plugin = new GeodesicDistanceMapPlugin();
		ChamferMask2D chamferMask = ChamferMask2D.BORGEFORS;
		ImagePlus mapPlus = plugin.process(markerPlus, maskPlus, "map", chamferMask, false, true);
		ImageProcessor map = mapPlus.getProcessor();
				
		assertEquals(260, map.get(190, 213));
	}

}
