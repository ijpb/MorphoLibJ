/**
 * 
 */
package inra.ijpb.measure;

import static org.junit.Assert.*;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import org.junit.Test;

/**
 * @author David Legland
 *
 */
public class GeometricMeasures2DTest 
{
	/**
	 * Test method for {@link ijt.measure.geometric.GeometricMeasures2D#croftonPerimeter_D2(ij.process.ImageProcessor, double[])}.
	 */
	@Test
	public final void testAnalyzeRegions_D2() 
	{
		// initialize image with a square of side 4 in the middle
		ImageProcessor image = new ByteProcessor(10, 10);
		for (int y = 3; y < 7; y++) 
		{
			for (int x = 3; x < 7; x++) 
			{
				image.set(x, y, 255);
			}
		}

		double[] resol = new double[]{1, 1};
		ResultsTable table = GeometricMeasures2D.analyzeRegions(image, resol, 2);
		assertEquals(1, table.getCounter());
		assertEquals(4 * Math.PI, table.getValue("Perimeter", 0), 1e-10);
	}

	/**
	 * Initialize 20x20 image with a disk of radius 8 in the middle, resulting
	 * in expected perimeter equal to 50.2655. The center of the disk is 
	 * slightly shifted to reduce discretization artifacts.
	 * 
	 * Test method for {@link ijt.measure.geometric.GeometricMeasures2D#croftonPerimeter_D2(ij.process.ImageProcessor, double[])}.
	 */
	@Test
	public final void testAnalyzeRegions_DiskR8_D2() 
	{
		ImageProcessor image = createDiskR8Image();

		double[] resol = new double[]{1, 1};
		ResultsTable table = GeometricMeasures2D.analyzeRegions(image, resol, 2);
		
		// only one particle should have been detected
		assertEquals(1, table.getCounter());

		double exp = 2 * Math.PI * 8;
		// relative error is expected to be lower than 22% for two directions
		assertEquals(exp, table.getValue("Perimeter", 0), exp * .22);
	}

	/**
	 * Initialize 20x20 image with a disk of radius 8 in the middle, resulting
	 * in expected perimeter equal to 50.2655. The center of the disk is 
	 * slightly shifted to reduce discretization artifacts.
	 * 
	 * Test method for {@link ijt.measure.geometric.GeometricMeasures2D#croftonPerimeter_D2(ij.process.ImageProcessor, double[])}.
	 */
	@Test
	public final void testAnalyzeRegions_DiskR8_D4() 
	{
		ImageProcessor image = createDiskR8Image();

		double[] resol = new double[]{1, 1};
		ResultsTable table = GeometricMeasures2D.analyzeRegions(image, resol, 4);
		assertEquals(1, table.getCounter());

		double exp = 2 * Math.PI * 8;
		// relative error is expected to be lower than 5.2% for four directions
		assertEquals(exp, table.getValue("Perimeter", 0), exp * .052);
	}

	/**
	 * Initialize 20x20 image with a disk of radius 8 in the middle, resulting
	 * in expected perimeter equal to 50.2655. The center of the disk is 
	 * slightly shifted to reduce discretization artifacts.
	 * 
	 * Test method for {@link ijt.measure.geometric.GeometricMeasures2D#croftonPerimeter_D2(ij.process.ImageProcessor, double[])}.
	 */
	@Test
	public final void testCroftonPerimeterD2_DiskR8()
	{
		ImageProcessor image = createDiskR8Image();

		double[] resol = new double[]{1, 1};
		int[] labels = new int[]{255};
		double perims[] = GeometricMeasures2D.croftonPerimeterD2(image, labels, resol);
		assertEquals(1, perims.length);

		double exp = 2 * Math.PI * 8;
		// relative error is expected to be lower than 22% for four directions
		assertEquals(exp, perims[0], exp * .22);
	}

	/**
	 * Initialize 20x20 image with a disk of radius 8 in the middle, resulting
	 * in expected perimeter equal to 50.2655. The center of the disk is 
	 * slightly shifted to reduce discretization artifacts.
	 * 
	 * Test method for {@link ijt.measure.geometric.GeometricMeasures2D#croftonPerimeterD4(ij.process.ImageProcessor, double[])}.
	 */
	@Test
	public final void testCroftonPerimeterD4_DiskR8() 
	{
		ImageProcessor image = createDiskR8Image();

		double[] resol = new double[]{1, 1};
		int[] labels = new int[]{255};
		double perims[] = GeometricMeasures2D.croftonPerimeterD4(image, labels, resol);
		assertEquals(1, perims.length);

		double exp = 2 * Math.PI * 8;
		// relative error is expected to be lower than 5.2% for four directions
		assertEquals(exp, perims[0], exp * .052);
	}

	private final ImageProcessor createDiskR8Image() 
	{
		ImageProcessor image = new ByteProcessor(20, 20);
		for (int y = 0; y < 20; y++) 
		{
			for (int x = 0; x < 20; x++)
			{
				double d = Math.hypot(x - 10.12, y - 10.23);
				if (d <= 8) 
					image.set(x, y, 255);
			}
		}
		return image;
	}
	
	/**
	 * Test method for {@link ijt.measure.geometric.GeometricMeasures2D#particleArea(ij.process.ImageProcessor, int)}.
	 */
	@Test
	public final void testParticleArea() 
	{
		// initialize image with a square of side 4 in the middle
		ImageProcessor image = new ByteProcessor(10, 10);
		for (int y = 3; y < 7; y++)
		{
			for (int x = 3; x < 7; x++)
			{
				image.set(x, y, 255);
			}
		}

		int area = GeometricMeasures2D.particleArea(image, 255);
		assertEquals(16, area);
	}
	
	/**
	 * Test method for {@link ijt.measure.geometric.GeometricMeasures2D#inertiaEllipse(ij.process.ImageProcessor)}.
	 */
	@Test
	public final void testInertiaEllipse() 
	{
		String fileName = getClass().getResource("/files/ellipse_A40_B20_T30.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		assertNotNull(imagePlus);
		ImageProcessor image = (ImageProcessor) imagePlus.getProcessor();
		
		ResultsTable table = GeometricMeasures2D.inertiaEllipse(image);
		
		assertEquals(49.5, table.getValue("XCentroid", 0), .1);
		assertEquals(49.5, table.getValue("YCentroid", 0), .1);
		assertEquals(40, table.getValue("Radius1", 0), .2);
		assertEquals(20, table.getValue("Radius2", 0), .2);
		assertEquals(30, table.getValue("Orientation", 0), 1);
	}
	
	/**
//	 * Test method for {@link ijt.measure.geometric.GeometricMeasures2D#maxInscribedCircle(ij.process.ImageProcessor)}.
	 */
	@Test
	public final void testMaxInscribedCircle() 
	{
		ImageProcessor image = new ByteProcessor(14, 9);
		image.set(1, 1, 1);
		fillRect(image, 3, 4, 1, 2, 2);		// radius 2
		fillRect(image, 1, 1+3, 4, 4+3, 3); // radius 4
		fillRect(image, 6, 6+6, 1, 1+6, 4); // radius 7
		
		ResultsTable table = GeometricMeasures2D.maxInscribedCircle(image);
		
		assertEquals(1, table.getValue("Radius", 0), .1);
		assertEquals(1, table.getValue("Radius", 1), .1);
		assertEquals(2, table.getValue("Radius", 2), .1);
		assertEquals(4, table.getValue("Radius", 3), .1);
	}

	private static final void fillRect(ImageProcessor image, int xmin,
			int xmax, int ymin, int ymax, double value)
	{
		for (int y = ymin; y <= ymax; y++)
		{
			for (int x = xmin; x <= xmax; x++) 
			{
				image.setf(x, y, (float) value);
			}
		}
		
	}
}
