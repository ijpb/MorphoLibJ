package inra.ijpb.label.geodesic;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.binary.ChamferWeights;

public class GeodesicDiameterCalculatorTest
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
		
		GeodesicDiameterCalculator algo = new GeodesicDiameterCalculator(ChamferWeights.BORGEFORS);
		double[] geodDiams = algo.geodesicDiameter(labelImage, new int[]{1, 2, 3, 4, 5});
		
		for (int i = 0; i < 5; i++)
		{
			assertEquals((26.0/3.0)+1.0, geodDiams[i], .1);
		}
	}

	/**
	 * Test method for {@link inra.ijpb.label.geodesic.GeodesicDiameterCalculator#geodesicDiameter(ij.process.ImageProcessor)}.
	 */
	@Test
	public void testGeodesicDiameter_Circle_ChessKnight()
	{
		ImagePlus imagePlus = IJ.openImage(getClass().getResource("/files/circles.tif").getFile());
		ImageProcessor image = imagePlus.getProcessor();
	
		GeodesicDiameterCalculator algo = new GeodesicDiameterCalculator(ChamferWeights.CHESSKNIGHT);
		Map<Integer,Double> geodDiams = algo.geodesicDiameter(image);

		assertEquals(1, geodDiams.size());
	}

	/**
	 * Test method for {@link inra.ijpb.label.geodesic.GeodesicDiameterCalculator#geodesicDiameter(ij.process.ImageProcessor)}.
	 */
	@Test
	public void testGeodesicDiameter_Grains_ChessKnight()
	{
		ImagePlus imagePlus = IJ.openImage(getClass().getResource("/files/grains-WTH-areaOpen-lbl2.tif").getFile());
		ImageProcessor image = imagePlus.getProcessor();
	
		// Need to use weights in 3-by-3 neighborhood, to avoid propagating distances to another grain 
		GeodesicDiameterCalculator algo = new GeodesicDiameterCalculator(ChamferWeights.CHESSKNIGHT);
		Map<Integer,Double> geodDiams = algo.geodesicDiameter(image);

		assertEquals(71, geodDiams.size());
	}

//	/**
//	 * Test method for {@link inra.ijpb.label.geodesic.GeodesicDiameter#longestGeodesicPaths(ij.process.ImageProcessor)}.
//	 */
//	@Test
//	public void testLongestGeodesicPaths_Rect()
//	{
//		ImageProcessor image = new ByteProcessor(10, 3);
//		for (int x = 1; x < 8; x++)
//		{
//			image.set(x, 1, 255);
//		}
//
//		GeodesicDiameterFloat algo = new GeodesicDiameterFloat(ChamferWeights.BORGEFORS);
//		algo.analyzeImage(image);
//		Map<Integer, List<Point>> pathMap = algo.longestGeodesicPaths();
//
//		assertEquals(1, pathMap.size());
//		List<Point> path1 = pathMap.get(255);
//		assertEquals(7, path1.size());
//	}
//
//
//	/**
//	 * Test method for {@link inra.ijpb.label.geodesic.GeodesicDiameter#longestGeodesicPaths(ij.process.ImageProcessor)}.
//	 */
//	@Test
//	public void testLongestGeodesicPaths_Circles()
//	{
//		ImagePlus imagePlus = IJ.openImage(getClass().getResource("/files/circles.tif").getFile());
//		ImageProcessor image = imagePlus.getProcessor();
//	
//		GeodesicDiameterFloat algo = new GeodesicDiameterFloat(ChamferWeights.CHESSKNIGHT);
//		algo.analyzeImage(image);
//		Map<Integer, List<Point>> pathMap = algo.longestGeodesicPaths();
//
//		assertEquals(1, pathMap.size());
//	}
//
//    /**
//     * Test method for {@link inra.ijpb.label.geodesic.GeodesicDiameter#longestGeodesicPaths(ij.process.ImageProcessor)}.
//     */
//    @Test
//    public void testLongestGeodesicPaths_Grains()
//    {
//        ImagePlus imagePlus = IJ.openImage(getClass().getResource("/files/grains-WTH-areaOpen-lbl2.tif").getFile());
//        ImageProcessor image = imagePlus.getProcessor();
//    
//        // Need to use weights in 3-by-3 neighborhood, to avoid propagating distances to another grain 
//        GeodesicDiameterFloat algo = new GeodesicDiameterFloat(ChamferWeights.BORGEFORS);
//		algo.analyzeImage(image);
//		Map<Integer, List<Point>> pathMap = algo.longestGeodesicPaths();
//
//        assertEquals(71, pathMap.size());
//    }
//
//    /**
//     * Test method for {@link inra.ijpb.label.geodesic.GeodesicDiameter#longestGeodesicPaths(ij.process.ImageProcessor)}.
//     */
//    @Test
//    public void testLongestGeodesicPaths_LargeLabels()
//    {
//        ImagePlus imagePlus = IJ.openImage(getClass().getResource("/files/particles_largeLabels.tif").getFile());
//        ImageProcessor image = imagePlus.getProcessor();
//    
//        // Need to use weights in 3-by-3 neighborhood, to avoid propagating distances to another grain 
//        GeodesicDiameterFloat algo = new GeodesicDiameterFloat(ChamferWeights.BORGEFORS);
//		algo.analyzeImage(image);
//		Map<Integer, List<Point>> pathMap = algo.longestGeodesicPaths();
//
//        assertEquals(6, pathMap.size());
//        
//        List<Point> lastPath = pathMap.get(104544);
//        assertEquals(1, lastPath.size());
//        Point p = lastPath.get(0);
//        assertEquals(30, p.x);
//        assertEquals(32, p.y);
//    }

}
