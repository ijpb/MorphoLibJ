package inra.ijpb.measure;

import static org.junit.Assert.assertEquals;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.binary.ChamferWeights;

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
		
		GeodesicDiameter algo = new GeodesicDiameter(ChamferWeights.BORGEFORS);
		GeodesicDiameter.Result[] geodDiams = algo.process(labelImage, new int[]{1, 2, 3, 4, 5});
		
		for (int i = 0; i < 5; i++)
		{
			assertEquals((26.0/3.0)+1.0, geodDiams[i].diameter, .1);
		}
	}

	/**
	 * Test method for {@link inra.ijpb.label.geodesic.GeodesicDiameter#process(ij.process.ImageProcessor)}.
	 */
	@Test
	public void testGeodesicDiameter_Circle_ChessKnight()
	{
		ImagePlus imagePlus = IJ.openImage(getClass().getResource("/files/circles.tif").getFile());
		ImageProcessor image = imagePlus.getProcessor();
	
		GeodesicDiameter algo = new GeodesicDiameter(ChamferWeights.CHESSKNIGHT);
		Map<Integer,GeodesicDiameter.Result> geodDiams = algo.process(image);

		assertEquals(1, geodDiams.size());
	}

	/**
	 * Test method for {@link inra.ijpb.label.geodesic.GeodesicDiameter#process(ij.process.ImageProcessor)}.
	 */
	@Test
	public void testGeodesicDiameter_Grains_ChessKnight()
	{
		ImagePlus imagePlus = IJ.openImage(getClass().getResource("/files/grains-WTH-areaOpen-lbl2.tif").getFile());
		ImageProcessor image = imagePlus.getProcessor();
	
		// Need to use weights in 3-by-3 neighborhood, to avoid propagating distances to another grain 
		GeodesicDiameter algo = new GeodesicDiameter(ChamferWeights.CHESSKNIGHT);
		Map<Integer,GeodesicDiameter.Result> geodDiams = algo.process(image);

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

		GeodesicDiameter algo = new GeodesicDiameter(ChamferWeights.BORGEFORS);
		algo.setComputePaths(true);
		
		Map<Integer,GeodesicDiameter.Result> geodDiams = algo.process(image);

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
	
		GeodesicDiameter algo = new GeodesicDiameter(ChamferWeights.BORGEFORS);
		algo.setComputePaths(true);
		
		Map<Integer,GeodesicDiameter.Result> geodDiams = algo.process(image);

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
    
		GeodesicDiameter algo = new GeodesicDiameter(ChamferWeights.BORGEFORS);
		algo.setComputePaths(true);
		
		Map<Integer,GeodesicDiameter.Result> geodDiams = algo.process(image);

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
    
		GeodesicDiameter algo = new GeodesicDiameter(ChamferWeights.BORGEFORS);
		algo.setComputePaths(true);
		
		Map<Integer,GeodesicDiameter.Result> geodDiams = algo.process(image);

        assertEquals(6, geodDiams.size());
        
        List<Point2D> lastPath = geodDiams.get(104544).path;
        assertEquals(1, lastPath.size());
        Point2D p = lastPath.get(0);
        assertEquals(30, p.getX(), .01);
        assertEquals(32, p.getY(), .01);
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
		
		GeodesicDiameter algo = new GeodesicDiameter(ChamferWeights.BORGEFORS);
		algo.setComputePaths(true);
		
		GeodesicDiameter.Result[] geodDiams = algo.process(labelImage, new int[]{1, 2, 3, 5});
		
		double[] exp = new double[]{Double.POSITIVE_INFINITY, 3.66, 3.66, 5.33}; 
		for (int i = 0; i < 4; i++)
		{
			assertEquals(exp[i], geodDiams[i].diameter, .1);
		}
    }

}
