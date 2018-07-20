/**
 * 
 */
package inra.ijpb.measure;

import static org.junit.Assert.assertEquals;

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
	 * Test method for {@link inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D#areaDensity(ij.process.ImageProcessor)}.
	 */
	@Test
	public final void testAreaDensity_OhserMecklich()
	{
		ImageProcessor image = createOhserMuecklichImage();
		
		double density = IntrinsicVolumes2D.areaDensity(image);
		
		assertEquals(0.3008, density, .001);
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
	 * Test method for {@link inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D#eulerNumbers(ij.process.ImageProcessor, int[], int)}.
	 */
	@Test
	public final void testEulerNumbers_smallParticles_C4()
	{
		// create a binary image containing a square
		ImageProcessor image = new ByteProcessor(10, 10);
		image.set(0, 0, 1);

		image.set(1, 0, 4);

		image.set(1, 1, 7);
		image.set(2, 1, 7);
		image.set(1, 2, 7);
		image.set(2, 2, 7);

		image.set(7, 7, 2);
		image.set(8, 7, 2);
		image.set(9, 7, 2);
		image.set(7, 8, 2);
		image.set(9, 8, 2);
		image.set(7, 9, 2);
		image.set(8, 9, 2);
		image.set(9, 9, 2);

		int[] labels = new int[] {1, 2, 4, 7};
		double[] eulerNumbers = IntrinsicVolumes2D.eulerNumbers(image, labels, 4);
		assertEquals(1, eulerNumbers[0], .01);
		assertEquals(0, eulerNumbers[1], .01);
		assertEquals(1, eulerNumbers[2], .01);
		assertEquals(1, eulerNumbers[3], .01);
	}
	
	/**
	 * Test method for {@link inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D#eulerNumbers(ij.process.ImageProcessor, int[], int)}.
	 */
	@Test
	public final void testEulerNumbers_smallParticles_C8()
	{
		// create a binary image containing a square
		ImageProcessor image = new ByteProcessor(10, 10);
		image.set(0, 0, 1);

		image.set(1, 0, 4);

		image.set(1, 1, 7);
		image.set(2, 1, 7);
		image.set(1, 2, 7);
		image.set(2, 2, 7);

		image.set(7, 7, 2);
		image.set(8, 7, 2);
		image.set(9, 7, 2);
		image.set(7, 8, 2);
		image.set(9, 8, 2);
		image.set(7, 9, 2);
		image.set(8, 9, 2);
		image.set(9, 9, 2);

		int[] labels = new int[] {1, 2, 4, 7};
		double[] eulerNumbers = IntrinsicVolumes2D.eulerNumbers(image, labels, 8);
		assertEquals(1, eulerNumbers[0], .01);
		assertEquals(0, eulerNumbers[1], .01);
		assertEquals(1, eulerNumbers[2], .01);
		assertEquals(1, eulerNumbers[3], .01);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D#eulerNumberDensity(ij.process.ImageProcessor, int)}.
	 */
	@Test
	public final void testEulerNumberDensity_OhserMecklich_C4()
	{
		ImageProcessor image = createOhserMuecklichImage();
		Calibration calib = new Calibration();
		
		double density = IntrinsicVolumes2D.eulerNumberDensity(image, calib, 4);
		
		assertEquals(0.0444, density, .001);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D#eulerNumberDensity(ij.process.ImageProcessor, int)}.
	 */
	@Test
	public final void testEulerNumberDensity_OhserMecklich_C8()
	{
		ImageProcessor image = createOhserMuecklichImage();
		Calibration calib = new Calibration();
		
		double density = IntrinsicVolumes2D.eulerNumberDensity(image, calib, 8);
		
		assertEquals(0.0267, density, .001);
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

	/**
	 * Test method for {@link inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D#perimeter(ij.process.ImageProcessor, Calibration, int)}.
	 */
	@Test
	public final void testPerimeters_smallSquare_D2()
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
		
		// compute perimeter with default (1,1) calibration
		Calibration calib = new Calibration();
		int[] labels = new int[] {255};
		double[] perims = IntrinsicVolumes2D.perimeters(image, labels, calib, 2);
		
		assertEquals(12.5664, perims[0], .01);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D#perimeters(ij.process.ImageProcessor, int[], Calibration, int)}.
	 */
	@Test
	public final void testPerimeters_disks_D2()
	{
		// define several disks of increasing radius
		double xc1 = 25.2, yc1 = 25.3, r1 = 11.0;
		double xc2 = 75.2, yc2 = 25.3, r2 = 16.0;
		double xc3 = 21.2, yc3 = 75.3, r3 = 21.0;
		// the last one touching borders
		double xc4 = 73.2, yc4 = 73.3, r4 = 26.0;

		// create a binary image containing a square
		ImageProcessor image = new ByteProcessor(100, 100);
		for (int y = 0; y < 100; y++)
		{
			for (int x = 0; x < 100; x++)
			{
				if(Math.hypot(x-xc1, y-yc1) < r1) image.set(x, y, 1);
				if(Math.hypot(x-xc2, y-yc2) < r2) image.set(x, y, 2);
				if(Math.hypot(x-xc3, y-yc3) < r3) image.set(x, y, 3);
				if(Math.hypot(x-xc4, y-yc4) < r4) image.set(x, y, 4);
			}
		}
		
		// compute perimeter with default (1,1) calibration
		Calibration calib = new Calibration();
		int[] labels = new int[] {1, 2, 3, 4};
		double[] perims = IntrinsicVolumes2D.perimeters(image, labels, calib, 2);
		
		// check to expected values with a tolerance of 5 percents
		double exp1 = 2 * Math.PI * r1;
		assertEquals(exp1, perims[0], exp1 * 0.05);
		double exp2 = 2 * Math.PI * r2;
		assertEquals(exp2, perims[1], exp2 * 0.05);
		double exp3 = 2 * Math.PI * r3;
		assertEquals(exp3, perims[2], exp3 * 0.05);
		double exp4 = 2 * Math.PI * r4;
		assertEquals(exp4, perims[3], exp4 * 0.05);
	}
	
	/**
	 * Test method for {@link inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D#perimeters(ij.process.ImageProcessor, int[], Calibration, int)}.
	 */
	@Test
	public final void testPerimeters_disks_D4()
	{
		// define several disks of increasing radius
		double xc1 = 25.2, yc1 = 25.3, r1 = 11.0;
		double xc2 = 75.2, yc2 = 25.3, r2 = 16.0;
		double xc3 = 21.2, yc3 = 75.3, r3 = 21.0;
		// the last one touching borders
		double xc4 = 73.2, yc4 = 73.3, r4 = 26.0;

		// create a binary image containing a square
		ImageProcessor image = new ByteProcessor(100, 100);
		for (int y = 0; y < 100; y++)
		{
			for (int x = 0; x < 100; x++)
			{
				if(Math.hypot(x-xc1, y-yc1) < r1) image.set(x, y, 1);
				if(Math.hypot(x-xc2, y-yc2) < r2) image.set(x, y, 2);
				if(Math.hypot(x-xc3, y-yc3) < r3) image.set(x, y, 3);
				if(Math.hypot(x-xc4, y-yc4) < r4) image.set(x, y, 4);
			}
		}
		
		// compute perimeter with default (1,1) calibration
		Calibration calib = new Calibration();
		int[] labels = new int[] {1, 2, 3, 4};
		double[] perims = IntrinsicVolumes2D.perimeters(image, labels, calib, 4);
		
		// check to expected values with a tolerance of 5 percents
		double exp1 = 2 * Math.PI * r1;
		assertEquals(exp1, perims[0], exp1 * 0.05);
		double exp2 = 2 * Math.PI * r2;
		assertEquals(exp2, perims[1], exp2 * 0.05);
		double exp3 = 2 * Math.PI * r3;
		assertEquals(exp3, perims[2], exp3 * 0.05);
		double exp4 = 2 * Math.PI * r4;
		assertEquals(exp4, perims[3], exp4 * 0.05);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D#eulerNumberDensity(ij.process.ImageProcessor, int)}.
	 */
	@Test
	public final void testPerimeterDensity_OhserMecklich_D2()
	{
		ImageProcessor image = createOhserMuecklichImage();
		Calibration calib = new Calibration();
		
		double density = IntrinsicVolumes2D.perimeterDensity(image, calib, 2);
		
		assertEquals(0.5, density, .05);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D#eulerNumberDensity(ij.process.ImageProcessor, int)}.
	 */
	@Test
	public final void testPerimeterDensity_OhserMecklich_D4()
	{
		ImageProcessor image = createOhserMuecklichImage();
		Calibration calib = new Calibration();
		
		double density = IntrinsicVolumes2D.perimeterDensity(image, calib, 4);
		
		assertEquals(0.5, density, .05);
	}


	/**
	 * Generate the sample image provided as example in the Book "Statistical
	 * Analysis of microstructures in material sciences", from J. Ohser and F.
	 * Muecklich.
	 * 
	 * @return a sample image
	 */
	public final ImageProcessor createOhserMuecklichImage()
	{
		int[][] data = new int[][] {
			{0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0}, 
			{0, 1, 0, 0,  1, 1, 1, 1,  0, 0, 0, 0,  0, 0, 0, 1}, 
			{0, 1, 1, 0,  0, 1, 1, 1,  0, 0, 0, 0,  1, 1, 0, 0}, 
			{0, 1, 1, 1,  0, 1, 1, 1,  0, 1, 1, 0,  0, 0, 1, 0}, 
			{0, 0, 0, 0,  0, 1, 1, 1,  0, 0, 0, 1,  0, 0, 1, 0}, 
			{0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 1, 0}, 
			{0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 1, 1, 0}, 
			{0, 0, 0, 0,  0, 0, 1, 1,  1, 1, 0, 0,  1, 0, 1, 0}, 
			{0, 0, 0, 0,  0, 0, 1, 0,  1, 0, 0, 1,  1, 0, 0, 0}, 
			{0, 0, 0, 0,  1, 0, 1, 0,  1, 0, 0, 1,  1, 0, 1, 0}, 
			{0, 1, 0, 0,  1, 0, 1, 1,  1, 0, 1, 0,  1, 0, 1, 0}, 
			{0, 1, 0, 1,  1, 0, 0, 0,  0, 0, 1, 0,  0, 0, 1, 0}, 
			{0, 1, 1, 1,  0, 0, 0, 0,  0, 0, 1, 1,  1, 1, 1, 0}, 
			{0, 0, 1, 1,  0, 0, 0, 0,  0, 1, 1, 1,  1, 1, 0, 0}, 
			{0, 0, 0, 0,  0, 0, 0, 0,  0, 1, 0, 0,  0, 0, 0, 0}, 
			{0, 0, 0, 0,  0, 1, 1, 0,  0, 0, 0, 0,  0, 0, 0, 0}, 
		};

		int sizeX = 16;
		int sizeY = 16;
		ByteProcessor image = new ByteProcessor(sizeX, sizeY);
		for (int y = 0; y < sizeY; y++)
		{
			for (int x = 0; x < sizeY; x++)
			{
				image.set(x, y, data[y][x] > 0 ? 255 : 0);
			}
		}
		
		return image;
	}
}
