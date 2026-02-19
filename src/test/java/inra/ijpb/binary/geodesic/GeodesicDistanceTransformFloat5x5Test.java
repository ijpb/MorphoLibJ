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
package inra.ijpb.binary.geodesic;

import static org.junit.Assert.assertEquals;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import inra.ijpb.binary.distmap.ChamferMask2D;

import org.junit.Test;

@Deprecated
public class GeodesicDistanceTransformFloat5x5Test
{

	@Test
	public void testGeodesicDistanceMap_Borgefors()
	{
		ImagePlus maskPlus = IJ.openImage(getClass().getResource("/files/circles.tif").getFile());
		ImageProcessor mask = maskPlus.getProcessor();
		ImageProcessor marker = mask.duplicate();
		marker.fill();
		marker.set(30, 30, 255);

		ChamferMask2D chamferMask = ChamferMask2D.CHESSKNIGHT;
		GeodesicDistanceTransform algo = new GeodesicDistanceTransformFloat(chamferMask, true);
		ImageProcessor map = algo.geodesicDistanceMap(marker, mask);

		assertEquals(250.8, map.getf(190, 210), .01);
	}

}
