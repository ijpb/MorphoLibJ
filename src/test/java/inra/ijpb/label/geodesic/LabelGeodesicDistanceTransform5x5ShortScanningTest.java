/**
 * 
 */
package inra.ijpb.label.geodesic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;


/**
 * @author dlegland
 *
 */
public class LabelGeodesicDistanceTransform5x5ShortScanningTest
{
	/**
	 * Test method for {@link inra.ijpb.label.distmap.LabelGeodesicDistanceTransform5x5Short#geodesicDistanceMap(ij.process.ImageProcessor)}.
	 */
	@Test
	public final void testGeodesicDistanceMap_SingleRect_Borgefors()
	{
		// mask is a 8-by-4 rectangle
		ImageProcessor mask = new ByteProcessor(10, 6);
		for (int y = 1; y < 5; y++)
		{
			for (int x = 1; x < 9; x++)
			{
				mask.set(x, y, 255);
			}
		}
		
		// marker is a single pixel within the rectangle
		ImageProcessor marker = new ByteProcessor(10, 6);
		marker.set(3, 2, 255);
		
		short[] weights = new short[] { 3, 4 };
		LabelGeodesicDistanceTransform algo = new LabelGeodesicDistanceTransform5x5ShortScanning(
				weights, false);
		
		ImageProcessor map = algo.geodesicDistanceMap(marker, mask);
		
		assertEquals(0, map.get(3, 2));
		assertEquals(3, map.get(4, 2));
		assertEquals(4, map.get(4, 3));
		
		assertEquals(7, map.get(1, 1));
		assertEquals(17, map.get(8, 4));
	}

	
	/**
	 * Test method for {@link inra.ijpb.label.distmap.LabelGeodesicDistanceTransform5x5Short#geodesicDistanceMap(ij.process.ImageProcessor)}.
	 */
	@Test
	public final void testGeodesicDistanceMap_SingleRect_ChessKnight()
	{
		// mask is a 8-by-4 rectangle
		ImageProcessor mask = new ByteProcessor(10, 6);
		for (int y = 1; y < 5; y++)
		{
			for (int x = 1; x < 9; x++)
			{
				mask.set(x, y, 255);
			}
		}
		
		// marker is a single pixel within the rectangle
		ImageProcessor marker = new ByteProcessor(10, 6);
		marker.set(3, 2, 255);
		
		short[] weights = new short[] { 5, 7, 11 };
		LabelGeodesicDistanceTransform algo = new LabelGeodesicDistanceTransform5x5ShortScanning(weights, false);
		
		ImageProcessor map = algo.geodesicDistanceMap(marker, mask);
		
		assertEquals(0, map.get(3, 2));
		assertEquals(5, map.get(4, 2));
		assertEquals(7, map.get(4, 3));
		
		assertEquals(11, map.get(1, 1));
		assertEquals(27, map.get(8, 4));
	}

	/**
	 * Test method for {@link inra.ijpb.label.distmap.LabelGeodesicDistanceTransform5x5Short#geodesicDistanceMap(ij.process.ImageProcessor)}.
	 */
	@Test
	public final void testGeodesicDistanceMap_Hilbert_Borgefors()
	{
		// mask is a 8-by-4 rectangle
		ImageProcessor mask = createHilbertMask();
		
		// marker is a single pixel within the rectangle
		ImageProcessor marker = new ByteProcessor(12, 12);
		marker.set(1, 4, 255);
		
		short[] weights = new short[] { 3, 4 };
		LabelGeodesicDistanceTransform algo = new LabelGeodesicDistanceTransform5x5ShortScanning(weights, false);
		
		ImageProcessor map = algo.geodesicDistanceMap(marker, mask);
		
		assertEquals(0, map.get(1, 4));
		assertEquals(111, map.get(1, 7));
	}

	/**
	 * Test method for {@link inra.ijpb.label.distmap.LabelGeodesicDistanceTransform5x5Short#geodesicDistanceMap(ij.process.ImageProcessor)}.
	 */
	@Test
	public final void testGeodesicDistanceMap_Hilbert_ChessKnight()
	{
		// mask is a 8-by-4 rectangle
		ImageProcessor mask = createHilbertMask();
		
		// marker is a single pixel within the rectangle
		ImageProcessor marker = new ByteProcessor(12, 12);
		marker.set(1, 4, 255);
		
		short[] weights = new short[] { 5, 7, 11 };
		LabelGeodesicDistanceTransform algo = new LabelGeodesicDistanceTransform5x5ShortScanning(weights, false);
		
		ImageProcessor map = algo.geodesicDistanceMap(marker, mask);
		
		assertEquals(0, map.get(1, 4));
		assertEquals(177, map.get(1, 7));
	}


	/**
	 * Test method for {@link inra.ijpb.label.distmap.LabelGeodesicDistanceTransform5x5Short#geodesicDistanceMap(ij.process.ImageProcessor)}.
	 */
	@Test
	public final void testGeodesicDistanceMap_ChessKnight()
	{
		ImagePlus maskPlus = IJ.openImage(getClass().getResource("/files/circles.tif").getFile());
		ImageProcessor mask = maskPlus.getProcessor();
		ImageProcessor marker = mask.duplicate();
		marker.fill();
		marker.set(30, 30, 255);

		short[] weights = new short[] { 5, 7, 11 };
		LabelGeodesicDistanceTransform algo = new LabelGeodesicDistanceTransform5x5ShortScanning(
				weights, true);
		ImageProcessor map = algo.geodesicDistanceMap(marker, mask);

		assertEquals(250, map.get(190, 210));
	}

	private ImageProcessor createHilbertMask()
	{
		ImageProcessor mask = new ByteProcessor(12, 12);
		for (int i = 0; i < 4; i++)
		{
			// "upper" part of the curve
			mask.set(1, i + 1, 255);
			mask.set(i + 1, 1, 255);
			mask.set(4, i + 1, 255);
			mask.set(i + 4, 4, 255);
			mask.set(7, i + 1, 255);
			mask.set(i + 7, 1, 255);
			
			// "lower" part of the curve
			mask.set(i + 7, 10, 255);
			mask.set(7, i + 7, 255);
			mask.set(i + 4, 7, 255);
			mask.set(4, i + 7, 255);
			mask.set(i + 1, 10, 255);
			mask.set(1, i + 7, 255);
		}
		
		// the right part of the curve
		for (int i = 0; i < 10; i++)
		{
			mask.set(10, i + 1, 255);
		}
		
		return mask;
	}
}
