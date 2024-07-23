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
package inra.ijpb.measure.region2d;

import static org.junit.Assert.assertEquals;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.binary.distmap.ChamferMask2D;

public class GeodesicDiameterTest
{
	@Test
	public final void testGeodesicDiameter_FiveTouchingRects_Borgefors()
	{
		ByteProcessor labelImage = new ByteProcessor(17, 11);
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				labelImage.set(i +  1, j + 1, 1); 
				labelImage.set(j +  4, i + 1, 2); 
				labelImage.set(j +  4, i + 4, 3); 
				labelImage.set(j +  4, i + 7, 4); 
				labelImage.set(i + 13, j + 1, 5); 
			}
		}
		
		GeodesicDiameter algo = new GeodesicDiameter(ChamferMask2D.BORGEFORS);
		int[] labels = new int[]{1, 2, 3, 4, 5};
		GeodesicDiameter.Result[] geodDiams = algo.analyzeRegions(labelImage, labels, new Calibration());
		
		for (int i = 0; i < 5; i++)
		{
			assertEquals((26.0/3.0)+1.41, geodDiams[i].diameter, .1);
		}
	}

	/**
	 * Test method for {@link inra.ijpb.label.geodesic.GeodesicDiameter#analyzeRegions(ij.process.ImageProcessor)}.
	 */
	@Test
	public void testGeodesicDiameter_Circle_ChessKnight()
	{
		ImagePlus imagePlus = IJ.openImage(getClass().getResource("/files/circles.tif").getFile());
		ImageProcessor image = imagePlus.getProcessor();
	
		GeodesicDiameter algo = new GeodesicDiameter(ChamferMask2D.CHESSKNIGHT);
		Map<Integer, GeodesicDiameter.Result> geodDiams = algo.analyzeRegions(image);

		assertEquals(1, geodDiams.size());
	}

	/**
	 * Test method for {@link inra.ijpb.label.geodesic.GeodesicDiameter#analyzeRegions(ij.process.ImageProcessor)}.
	 */
	@Test
	public void testGeodesicDiameter_Grains_ChessKnight()
	{
		ImagePlus imagePlus = IJ.openImage(getClass().getResource("/files/grains-WTH-areaOpen-lbl2.tif").getFile());
		ImageProcessor image = imagePlus.getProcessor();
	
		// Need to use weights in 3-by-3 neighborhood, to avoid propagating distances to another grain 
		GeodesicDiameter algo = new GeodesicDiameter(ChamferMask2D.CHESSKNIGHT);
		Map<Integer, GeodesicDiameter.Result> geodDiams = algo.analyzeRegions(image);

		assertEquals(71, geodDiams.size());
	}

	/**
	 * Test method for {@link inra.ijpb.label.geodesic.GeodesicDiameter#longestGeodesicPaths(ij.process.ImageProcessor)}.
	 */
	@Test
	public void testLongestGeodesicPaths_Rect()
	{
		ImageProcessor image = new ByteProcessor(10, 3);
		for (int x = 1; x < 8; x++)
		{
			image.set(x, 1, 255);
		}

		GeodesicDiameter algo = new GeodesicDiameter(ChamferMask2D.BORGEFORS);
		algo.setComputePaths(true);
		
		Map<Integer, GeodesicDiameter.Result> geodDiams = algo.analyzeRegions(image);

		assertEquals(1, geodDiams.size());
		List<Point2D> path1 = geodDiams.get(255).path;
		assertEquals(4, path1.size());
	}


	/**
	 * Test method for {@link inra.ijpb.label.geodesic.GeodesicDiameter#longestGeodesicPaths(ij.process.ImageProcessor)}.
	 */
	@Test
	public void testLongestGeodesicPaths_Circles()
	{
		ImagePlus imagePlus = IJ.openImage(getClass().getResource("/files/circles.tif").getFile());
		ImageProcessor image = imagePlus.getProcessor();
	
		GeodesicDiameter algo = new GeodesicDiameter(ChamferMask2D.BORGEFORS);
		algo.setComputePaths(true);
		
		Map<Integer, GeodesicDiameter.Result> geodDiams = algo.analyzeRegions(image);

		assertEquals(1, geodDiams.size());
	}

    /**
     * Test method for {@link inra.ijpb.label.geodesic.GeodesicDiameter#longestGeodesicPaths(ij.process.ImageProcessor)}.
     */
    @Test
    public void testLongestGeodesicPaths_Grains()
    {
        ImagePlus imagePlus = IJ.openImage(getClass().getResource("/files/grains-WTH-areaOpen-lbl2.tif").getFile());
        ImageProcessor image = imagePlus.getProcessor();
    
		GeodesicDiameter algo = new GeodesicDiameter(ChamferMask2D.BORGEFORS);
		algo.setComputePaths(true);
		
		Map<Integer, GeodesicDiameter.Result> geodDiams = algo.analyzeRegions(image);

        assertEquals(71, geodDiams.size());
    }

    /**
     * Test method for {@link inra.ijpb.label.geodesic.GeodesicDiameter#longestGeodesicPaths(ij.process.ImageProcessor)}.
     */
    @Test
    public void testLongestGeodesicPaths_LargeLabels()
    {
        ImagePlus imagePlus = IJ.openImage(getClass().getResource("/files/particles_largeLabels.tif").getFile());
        ImageProcessor image = imagePlus.getProcessor();
    
		GeodesicDiameter algo = new GeodesicDiameter(ChamferMask2D.BORGEFORS);
		algo.setComputePaths(true);
		
		Map<Integer, GeodesicDiameter.Result> geodDiams = algo.analyzeRegions(image);

        assertEquals(6, geodDiams.size());
        
        List<Point2D> lastPath = geodDiams.get(104544).path;
        assertEquals(1, lastPath.size());
        Point2D p = lastPath.get(0);
        assertEquals(30.0, p.getX(), .01);
        assertEquals(32.0, p.getY(), .01);
    }

	/**
	 * Test method for {@link inra.ijpb.label.geodesic.GeodesicDiameter#longestGeodesicPaths(ij.process.ImageProcessor)}.
	 * 
	 * Tests the special case where some labels are disconnecgted (ex:
	 * result of a crop on a label image), but we want to draw the path of the
	 * fully connected labels anyway. 
	 */
    @Test
    public void testLongestGeodesicPaths_DisconnectedLabels()
    {
		ByteProcessor labelImage = new ByteProcessor(10, 8);
		// first label is not connected 
		for (int j = 0; j < 2; j++)
		{
			for (int i = 0; i < 2; i++)
			{
				labelImage.set(i + 2, j, 1);
				labelImage.set(i + 6, j, 1);
			}
		}

		// another label (3-by-3) is connected 
		for (int j = 0; j < 3; j++)
		{
			for (int i = 0; i < 3; i++)
			{
				labelImage.set(i + 1, j + 3, 2);
			}
		}
		
		// another label (3-by-3) is connected and touches another label
		for (int j = 0; j < 3; j++)
		{
			for (int i = 0; i < 3; i++)
			{
				labelImage.set(i + 5, j + 2, 3);
			}
		}
		
		// another label (5-by-2) is connected and touches border 
		// (there is no label 4)
		for (int j = 0; j < 2; j++)
		{
			for (int i = 0; i < 5; i++)
			{
				labelImage.set(i + 5, j + 6, 5);
			}
		}
		
		// Resulting label image:
		//
		// 0 0 1 1 0 0 1 1 0 0
		// 0 0 1 1 0 0 1 1 0 0
		// 0 0 0 0 0 3 3 3 0 0
		// 0 2 2 2 0 3 3 3 0 0
		// 0 2 2 2 0 3 3 3 0 0
		// 0 2 2 2 0 0 0 0 0 0
		// 0 0 0 0 0 5 5 5 5 5
		// 0 0 0 0 0 5 5 5 5 5
		
		GeodesicDiameter algo = new GeodesicDiameter(ChamferMask2D.BORGEFORS);
		algo.setComputePaths(true);
		
		GeodesicDiameter.Result[] geodDiams = algo.analyzeRegions(labelImage, new int[]{1, 2, 3, 5}, new Calibration());
		
		double[] exp = new double[]{Double.POSITIVE_INFINITY, 4.08, 4.08, 5.74}; 
		for (int i = 0; i < 4; i++)
		{
			assertEquals(exp[i], geodDiams[i].diameter, .1);
		}
    }

}
