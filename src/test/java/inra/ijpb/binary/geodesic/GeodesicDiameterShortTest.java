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

import static org.junit.Assert.*;

import java.awt.Point;
import java.util.List;
import java.util.Map;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.binary.ChamferWeights;

import org.junit.Test;

/**
 * @author dlegland
 *
 */
@Deprecated
public class GeodesicDiameterShortTest
{
	/**
	 * Test method for {@link inra.ijpb.binary.geodesic.GeodesicDiameterShort#analyzeImage(ij.process.ImageProcessor)}, 
	 * operating on a 9-by-3 rectangle.
	 */
	@Test
	public void testAnalyzeImage_Rect_ChessKnight()
	{
		ImageProcessor image = new ByteProcessor(11, 5);
		for (int y = 0; y < 3; y++)
		{
			for (int x = 0; x < 9; x++)
			{
				image.set(x + 1, y + 1, 255);
			}
		}
	
		GeodesicDiameterShort algo = new GeodesicDiameterShort(ChamferWeights.CHESSKNIGHT);
		ResultsTable table = algo.analyzeImage(image);

		assertEquals(1, table.getCounter());
		double exp = (42.0 / 5.0) + 1;
		assertEquals(exp, table.getValueAsDouble(1, 0), .1);
	}

	/**
	 * Test method for {@link inra.ijpb.binary.geodesic.GeodesicDiameterShort#analyzeImage(ij.process.ImageProcessor)}.
	 */
	@Test
	public void testAnalyzeImage_Circle()
	{
		ImagePlus imagePlus = IJ.openImage(getClass().getResource("/files/circles.tif").getFile());
		ImageProcessor image = imagePlus.getProcessor();
	
		GeodesicDiameterShort algo = new GeodesicDiameterShort(ChamferWeights.CHESSKNIGHT);
		ResultsTable table = algo.analyzeImage(image);

		assertEquals(1, table.getCounter());
	}

	/**
	 * Test method for {@link inra.ijpb.binary.geodesic.GeodesicDiameterShort#analyzeImage(ij.process.ImageProcessor)}.
	 */
	@Test
	public void testAnalyzeImage_Grains()
	{
		ImagePlus imagePlus = IJ.openImage(getClass().getResource("/files/grains-WTH-areaOpen-lbl2.tif").getFile());
		ImageProcessor image = imagePlus.getProcessor();
	
		// Need to use weights in 3-by-3 neighborhood, to avoid propagating distances to another grain 
		GeodesicDiameterShort algo = new GeodesicDiameterShort(ChamferWeights.BORGEFORS);
		ResultsTable table = algo.analyzeImage(image);

		assertEquals(71, table.getCounter());
	}

	/**
	 * Test method for {@link inra.ijpb.binary.geodesic.GeodesicDiameterShort#longestGeodesicPaths(ij.process.ImageProcessor)}.
	 */
	@Test
	public void testLongestGeodesicPaths_Rect()
	{
		ImageProcessor image = new ByteProcessor(10, 3);
		for (int x = 1; x < 8; x++)
		{
			image.set(x, 1, 255);
		}

		GeodesicDiameterShort algo = new GeodesicDiameterShort(ChamferWeights.BORGEFORS);
		algo.analyzeImage(image);
		Map<Integer, List<Point>> pathMap = algo.longestGeodesicPaths();

		assertEquals(1, pathMap.size());
		List<Point> path1 = pathMap.get(255);
		assertEquals(7, path1.size());
	}


	/**
	 * Test method for {@link inra.ijpb.binary.geodesic.GeodesicDiameterShort#longestGeodesicPaths(ij.process.ImageProcessor)}.
	 */
	@Test
	public void testLongestGeodesicPaths_Circles()
	{
		ImagePlus imagePlus = IJ.openImage(getClass().getResource("/files/circles.tif").getFile());
		ImageProcessor image = imagePlus.getProcessor();
	
		GeodesicDiameterShort algo = new GeodesicDiameterShort(ChamferWeights.CHESSKNIGHT);
		algo.analyzeImage(image);
		Map<Integer, List<Point>> pathMap = algo.longestGeodesicPaths();

		assertEquals(1, pathMap.size());
	}

    /**
     * Test method for {@link inra.ijpb.binary.geodesic.GeodesicDiameterShort#longestGeodesicPaths(ij.process.ImageProcessor)}.
     */
    @Test
    public void testLongestGeodesicPaths_Grains()
    {
        ImagePlus imagePlus = IJ.openImage(getClass().getResource("/files/grains-WTH-areaOpen-lbl2.tif").getFile());
        ImageProcessor image = imagePlus.getProcessor();
    
        // Need to use weights in 3-by-3 neighborhood, to avoid propagating distances to another grain 
        GeodesicDiameterShort algo = new GeodesicDiameterShort(ChamferWeights.BORGEFORS);
		algo.analyzeImage(image);
		Map<Integer, List<Point>> pathMap = algo.longestGeodesicPaths();

        assertEquals(71, pathMap.size());
    }

    /**
     * Test method for {@link inra.ijpb.binary.geodesic.GeodesicDiameterShort#longestGeodesicPaths(ij.process.ImageProcessor)}.
     */
    @Test
    public void testLongestGeodesicPaths_LargeLabels()
    {
        ImagePlus imagePlus = IJ.openImage(getClass().getResource("/files/particles_largeLabels.tif").getFile());
        ImageProcessor image = imagePlus.getProcessor();
    
        // Need to use weights in 3-by-3 neighborhood, to avoid propagating distances to another grain 
        GeodesicDiameterShort algo = new GeodesicDiameterShort(ChamferWeights.BORGEFORS);
		algo.analyzeImage(image);
		Map<Integer, List<Point>> pathMap = algo.longestGeodesicPaths();

        assertEquals(6, pathMap.size());
        
        List<Point> lastPath = pathMap.get(104544);
        assertEquals(1, lastPath.size());
        Point p = lastPath.get(0);
        assertEquals(30, p.x);
        assertEquals(32, p.y);
    }

}
