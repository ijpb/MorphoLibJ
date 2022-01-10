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
package inra.ijpb.measure.region3d;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import inra.ijpb.binary.distmap.ChamferMask3D;
import inra.ijpb.measure.region3d.GeodesicDiameter3D.Result;


/**
 * @author dlegland
 *
 */
public class GeodesicDiameter3DTest
{
	/**
	 * Test method for {@link inra.ijpb.measure.region3d.GeodesicDiameter3D#analyzeImage(ij.ImageStack)}.
	 */
	@Test
	public void testAnalyzeRegions_Cube()
	{
		ImageStack image = ImageStack.create(12, 12, 12, 8);
		for (int z = 1; z < 10; z++)
		{
			for (int y = 1; y < 10; y++)
			{
				for (int x = 1; x < 10; x++)
				{
					image.setVoxel(x, y, z, 1);
				}
			}
		}
	
		GeodesicDiameter3D algo = new GeodesicDiameter3D(ChamferMask3D.BORGEFORS);
		Calibration calib = new Calibration();
		Result[] res = algo.analyzeRegions(image, new int[] {1}, calib);

		assertEquals(1, res.length);
		Result res0 = res[0];
		
		// expected value is 8*5/3 + sqrt(3) ~= 15.06
		assertEquals(15.06, res0.diameter, 0.01);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.region3d.GeodesicDiameter3D#analyzeImage(ij.ImageStack)}.
	 */
	@Test
	public void testComputeTable_Cube()
	{
		ImageStack image = ImageStack.create(12, 12, 12, 8);
		for (int z = 1; z < 10; z++)
		{
			for (int y = 1; y < 10; y++)
			{
				for (int x = 1; x < 10; x++)
				{
					image.setVoxel(x, y, z, 1);
				}
			}
		}
		
		ImagePlus imagePlus = new ImagePlus("Cube", image);
		
		GeodesicDiameter3D algo = new GeodesicDiameter3D(ChamferMask3D.BORGEFORS);
		
		ResultsTable table = algo.computeTable(imagePlus);

		assertEquals(1, table.getCounter());
		
		// value of label
		assertEquals("1", table.getLabel(0));
		// expected value is 8*5/3 + sqrt(3) ~= 15.06
		assertEquals(15.06, table.getValue("GeodesicDiameter", 0), 0.1);
		assertEquals(5.0, table.getValue("Radius", 0), 0.1);
	}

//	/**
//	 * Test method for {@link inra.ijpb.measure.region3d.GeodesicDiameter3D#analyzeImage(ij.ImageStack)}.
//	 */
//	@Test
//	public void testAnalyzeImage_BatCochlea()
//	{
//		String fileName = getClass().getResource("/files/bat-cochlea-volume.tif").getFile();
//		ImagePlus imagePlus = IJ.openImage(fileName);
//		
//		assertNotNull(imagePlus);
//	
//		assertTrue(imagePlus.getStackSize() > 0);
//	
//		ImageStack image = imagePlus.getStack();
//	
//		GeodesicDiameter3D algo = new GeodesicDiameter3D(ChamferMask3D.BORGEFORS);
//		ResultsTable table = algo.computeTable(imagePlus);
//
//		assertEquals(1, table.getCounter());
//	}

}
