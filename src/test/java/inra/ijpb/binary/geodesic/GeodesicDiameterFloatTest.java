/**
 * 
 */
package inra.ijpb.binary.geodesic;

import static org.junit.Assert.*;

import java.awt.Point;
import java.util.List;
import java.util.Map;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import inra.ijpb.binary.ChamferWeights;

import org.junit.Test;

/**
 * @author dlegland
 *
 */
public class GeodesicDiameterFloatTest
{
	/**
	 * Test method for {@link inra.ijpb.binary.geodesic.GeodesicDiameterFloat#analyzeImage(ij.process.ImageProcessor)}.
	 */
	@Test
	public void testAnalyzeImage_Circle()
	{
		ImagePlus imagePlus = IJ.openImage(getClass().getResource("/files/circles.tif").getFile());
		ImageProcessor image = imagePlus.getProcessor();
	
		GeodesicDiameterFloat algo = new GeodesicDiameterFloat(ChamferWeights.CHESSKNIGHT);
		ResultsTable table = algo.analyzeImage(image);

		assertEquals(1, table.getCounter());
	}

	/**
	 * Test method for {@link inra.ijpb.binary.geodesic.GeodesicDiameterFloat#analyzeImage(ij.process.ImageProcessor)}.
	 */
	@Test
	public void testAnalyzeImage_Grains()
	{
		ImagePlus imagePlus = IJ.openImage(getClass().getResource("/files/grains-WTH-areaOpen-lbl2.tif").getFile());
		ImageProcessor image = imagePlus.getProcessor();
	
		// Need to use weights in 3-by-3 neighborhood, to avoid propagating distances to another grain 
		GeodesicDiameterFloat algo = new GeodesicDiameterFloat(ChamferWeights.BORGEFORS);
		ResultsTable table = algo.analyzeImage(image);

		assertEquals(71, table.getCounter());
	}

	/**
	 * Test method for {@link inra.ijpb.binary.geodesic.GeodesicDiameterFloat#longestGeodesicPaths(ij.process.ImageProcessor)}.
	 */
	@Test
	public void testLongestGeodesicPaths_Circles()
	{
		ImagePlus imagePlus = IJ.openImage(getClass().getResource("/files/circles.tif").getFile());
		ImageProcessor image = imagePlus.getProcessor();
	
		GeodesicDiameterFloat algo = new GeodesicDiameterFloat(ChamferWeights.CHESSKNIGHT);
		Map<Integer, List<Point>> pathMap = algo.longestGeodesicPaths(image);

		assertEquals(1, pathMap.size());
	}

	/**
	 * Test method for {@link inra.ijpb.binary.geodesic.GeodesicDiameterFloat#longestGeodesicPaths(ij.process.ImageProcessor)}.
	 */
	@Test
	public void testLongestGeodesicPaths_Grains()
	{
		ImagePlus imagePlus = IJ.openImage(getClass().getResource("/files/grains-WTH-areaOpen-lbl2.tif").getFile());
		ImageProcessor image = imagePlus.getProcessor();
	
		// Need to use weights in 3-by-3 neighborhood, to avoid propagating distances to another grain 
		GeodesicDiameterFloat algo = new GeodesicDiameterFloat(ChamferWeights.BORGEFORS);
		Map<Integer, List<Point>> pathMap = algo.longestGeodesicPaths(image);

		assertEquals(71, pathMap.size());
	}

}
