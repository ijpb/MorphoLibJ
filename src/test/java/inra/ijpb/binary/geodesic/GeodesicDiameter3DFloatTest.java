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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.ResultsTable;
import inra.ijpb.binary.distmap.ChamferMask3D;

/**
 * @author dlegland
 *
 */
@Deprecated
public class GeodesicDiameter3DFloatTest
{
	/**
	 * Test method for {@link inra.ijpb.binary.geodesic.GeodesicDiameterFloat#analyzeImage(ij.process.ImageProcessor)}.
	 */
	@Test
	public void testAnalyzeImage_BatCochlea()
	{
		String fileName = getClass().getResource("/files/bat-cochlea_sub25.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		
		assertNotNull(imagePlus);
	
		assertTrue(imagePlus.getStackSize() > 0);
	
		ImageStack image = imagePlus.getStack();
	
		GeodesicDiameter3D algo = new GeodesicDiameter3DFloat(ChamferMask3D.BORGEFORS);
		ResultsTable table = algo.process(image);

		assertEquals(1, table.getCounter());
	}
}
