/**
 * 
 */
package inra.ijpb.measure;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.measure.Calibration;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * @author dlegland
 *
 */
public class IntrinsicVolumes2DTest
{
	/**
	 * Test method for {@link inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D#areas(ij.process.ImageProcessor, int[], ij.measure.Calibration)}.
	 */
	@Test
	public final void testAreas_fourRectangles()
	{
		// initialize an image with several rectangles of different sizes
		// labels are: 2, 4, 5, 9
		ImageProcessor image = new ByteProcessor(10, 10);
		image.set(1, 1, 2);
		for (int i = 0; i < 6; i++)
		{
			image.set(1, i+3, 4);
			image.set(i+3, 1, 5);
		}
		for (int i = 0; i < 6; i++)
		{
			for (int j = 0; j < 6; j++)
			{
				image.set(i+3, j+3, 9);
			}
		}
		
		int[] labels = new int[] {2, 4, 5, 9};
		Calibration calib = new Calibration();
		double[] areaList = IntrinsicVolumes2D.areas(image, labels, calib);
		
		assertEquals(4, areaList.length);
		assertEquals(1.0, areaList[0], .01);
		assertEquals(6.0, areaList[1], .01);
		assertEquals(6.0, areaList[2], .01);
		assertEquals(36.0, areaList[3], .01);
	}
	
	/**
	 * Test method for {@link inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D#eulerNumber(ij.process.ImageProcessor, int)}.
	 */
	@Test
	public final void testEulerNumber_singleSquareC4()
	{
		// create a binary image containing a square
		ImageProcessor image = new ByteProcessor(10, 10);
		for (int i = 0; i < 6; i++)
		{
			for (int j = 0; j < 6; j++)
			{
				image.set(i+2, j+2, 255);
			}
		}
		
		int euler = IntrinsicVolumes2D.eulerNumber(image, 4);
		assertEquals(1, euler);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D#eulerNumber(ij.process.ImageProcessor, int)}.
	 */
	@Test
	public final void testEulerNumber_singleSquareC8()
	{
		// create a binary image containing a square
		ImageProcessor image = new ByteProcessor(8, 8);
		for (int i = 2; i < 6; i++)
		{
			for (int j = 2; j < 6; j++)
			{
				image.set(i, j, 255);
			}
		}
		
		int euler = IntrinsicVolumes2D.eulerNumber(image, 8);
		assertEquals(1, euler);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D#eulerNumber(ij.process.ImageProcessor, int)}.
	 */
	@Test
	public final void testEulerNumber_fullSquareC4()
	{
		// create a binary image containing a square
		ImageProcessor image = new ByteProcessor(6, 6);
		for (int i = 0; i < 6; i++)
		{
			for (int j = 0; j < 6; j++)
			{
				image.set(i, j, 255);
			}
		}
		
		int euler = IntrinsicVolumes2D.eulerNumber(image, 4);
		assertEquals(1, euler);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D#eulerNumber(ij.process.ImageProcessor, int)}.
	 */
	@Test
	public final void testEulerNumber_fullSquareC8()
	{
		// create a binary image containing a square
		ImageProcessor image = new ByteProcessor(6, 6);
		for (int i = 0; i < 6; i++)
		{
			for (int j = 0; j < 6; j++)
			{
				image.set(i, j, 255);
			}
		}
		
		int euler = IntrinsicVolumes2D.eulerNumber(image, 8);
		assertEquals(1, euler);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D#eulerNumber(ij.process.ImageProcessor, int)}.
	 */
	@Test
	public final void testEulerNumber_torusC4()
	{
		// create a binary image containing a square
		ImageProcessor image = new ByteProcessor(11, 11);
		for (int i = 2; i < 9; i++)
		{
			for (int j = 2; j < 9; j++)
			{
				image.set(i, j, 255);
			}
		}
		for (int i = 4; i < 7; i++)
		{
			for (int j = 4; j < 7; j++)
			{
				image.set(i, j, 0);
			}
		}
		
		int euler = IntrinsicVolumes2D.eulerNumber(image, 4);
		assertEquals(0, euler);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D#eulerNumber(ij.process.ImageProcessor, int)}.
	 */
	@Test
	public final void testEulerNumber_torusC8()
	{
		// create a binary image containing a square
		ImageProcessor image = new ByteProcessor(11, 11);
		for (int i = 2; i < 9; i++)
		{
			for (int j = 2; j < 9; j++)
			{
				image.set(i, j, 255);
			}
		}
		for (int i = 4; i < 7; i++)
		{
			for (int j = 4; j < 7; j++)
			{
				image.set(i, j, 0);
			}
		}
		
		int euler = IntrinsicVolumes2D.eulerNumber(image, 8);
		assertEquals(0, euler);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D#eulerNumber(ij.process.ImageProcessor, int)}.
	 */
	@Test
	public final void testEulerNumber_crossC4()
	{
		// create a binary image containing a square
		ImageProcessor image = new ByteProcessor(10, 10);
		for (int i = 2; i < 8; i++)
		{
			image.set(i, 4, 255);
			image.set(i, 5, 255);
			image.set(4, i, 255);
			image.set(5, i, 255);
		}
		
		int euler = IntrinsicVolumes2D.eulerNumber(image, 4);
		assertEquals(1, euler);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D#eulerNumber(ij.process.ImageProcessor, int)}.
	 */
	@Test
	public final void testEulerNumber_crossC8()
	{
		// create a binary image containing a square
		ImageProcessor image = new ByteProcessor(10, 10);
		for (int i = 2; i < 8; i++)
		{
			image.set(i, 4, 255);
			image.set(i, 5, 255);
			image.set(4, i, 255);
			image.set(5, i, 255);
		}
		
		int euler = IntrinsicVolumes2D.eulerNumber(image, 8);
		assertEquals(1, euler);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D#eulerNumber(ij.process.ImageProcessor, int)}.
	 */
	@Test
	public final void testEulerNumber_crossTouchingBordersC4()
	{
		// create a binary image containing a square
		ImageProcessor image = new ByteProcessor(6, 6);
		for (int i = 0; i < 6; i++)
		{
			image.set(i, 2, 255);
			image.set(i, 3, 255);
			image.set(2, i, 255);
			image.set(3, i, 255);
		}
		
		int euler = IntrinsicVolumes2D.eulerNumber(image, 4);
		assertEquals(1, euler);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D#eulerNumber(ij.process.ImageProcessor, int)}.
	 */
	@Test
	public final void testEulerNumber_crossTouchingBordersC8()
	{
		// create a binary image containing a square
		ImageProcessor image = new ByteProcessor(6, 6);
		for (int i = 0; i < 6; i++)
		{
			image.set(i, 2, 255);
			image.set(i, 3, 255);
			image.set(2, i, 255);
			image.set(3, i, 255);
		}
		
		int euler = IntrinsicVolumes2D.eulerNumber(image, 8);
		assertEquals(1, euler);
	}
	
	/**
	 * Test method for {@link inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D#perimeter(ij.process.ImageProcessor, Calibration, int)}.
	 */
	@Test
	public final void testPerimeter_smallSquare_D2()
	{
		// create a binary image containing a square
		ImageProcessor image = new ByteProcessor(8, 8);
		for (int y = 2; y < 6; y++)
		{
			for (int x = 2; x < 6; x++)
			{
				image.set(x, y, 255);
			}
		}
		
		double perim = IntrinsicVolumes2D.perimeter(image, new Calibration(), 2);
		assertEquals(12.5664, perim, .01);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D#perimeter(ij.process.ImageProcessor, Calibration, int)}.
	 */
	@Test
	public final void testPerimeter_smallSquare_D4()
	{
		// create a binary image containing a square
		ImageProcessor image = new ByteProcessor(8, 8);
		for (int y = 2; y < 6; y++)
		{
			for (int x = 2; x < 6; x++)
			{
				image.set(x, y, 255);
			}
		}
		
		double perim = IntrinsicVolumes2D.perimeter(image, new Calibration(), 4);
		assertEquals(14.0582, perim, .01);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D#perimeter(ij.process.ImageProcessor, Calibration, int)}.
	 */
	@Test
	public final void testPerimeter_disk_D2()
	{
		double radius = 16.0;

		// create a binary image containing a square
		ImageProcessor image = new ByteProcessor(40, 40);
		for (int y = 0; y < 40; y++)
		{
			double y2 = (y - 20.2);
			for (int x = 0; x < 40; x++)
			{
				double x2 = (x - 20.3);
				image.set(x, y, Math.hypot(x2, y2) < radius ? 255 : 0);
			}
		}
		
		// compute perimeter with default (1,1) calibration
		Calibration calib = new Calibration();
		double perim = IntrinsicVolumes2D.perimeter(image, calib, 2);
		
		// check to expected value with a tolerance of 5 percents
		double exp = 2 * Math.PI * radius;
		assertEquals(exp, perim, exp * 0.05);
	}
	
	/**
	 * Test method for {@link inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D#perimeter(ij.process.ImageProcessor, Calibration, int)}.
	 */
	@Test
	public final void testPerimeter_disk_D4()
	{
		double radius = 16.0;

		// create a binary image containing a square
		ImageProcessor image = new ByteProcessor(40, 40);
		for (int y = 0; y < 40; y++)
		{
			double y2 = (y - 20.2);
			for (int x = 0; x < 40; x++)
			{
				double x2 = (x - 20.3);
				image.set(x, y, Math.hypot(x2, y2) < radius ? 255 : 0);
			}
		}
		
		// compute perimeter with default (1,1) calibration
		Calibration calib = new Calibration();
		double perim = IntrinsicVolumes2D.perimeter(image, calib, 4);
		
		// check to expected value with a tolerance of 5 percents
		double exp = 2 * Math.PI * radius;
		assertEquals(exp, perim, exp * 0.05);
	}
	

}
