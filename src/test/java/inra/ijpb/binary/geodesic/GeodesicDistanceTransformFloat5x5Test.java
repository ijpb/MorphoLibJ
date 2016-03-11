package inra.ijpb.binary.geodesic;

import static org.junit.Assert.assertEquals;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

import org.junit.Test;

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

		float[] weights = new float[] { 5, 7, 11 };
		GeodesicDistanceTransform algo = new GeodesicDistanceTransformFloat5x5(
				weights, true);
		ImageProcessor map = algo.geodesicDistanceMap(marker, mask);

		assertEquals(250.8, map.getf(190, 210), .01);
	}

}
