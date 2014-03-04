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
public class GeometricMeasures2DTest {

	/**
	 * Test method for {@link ijt.measure.geometric.GeometricMeasures2D#croftonPerimeter_D2(ij.process.ImageProcessor, double[])}.
	 */
	@Test
	public final void testComputeCroftonPerimeter_D2() {
		// initialize image with a square of side 4 in the middle
		ImageProcessor image = new ByteProcessor(10, 10);
		for (int y = 3; y < 7; y++) {
			for (int x = 3; x < 7; x++) {
				image.set(x, y, 255);
			}
		}

		double[] resol = new double[]{1, 1};
		ResultsTable table = GeometricMeasures2D.croftonPerimeter(image, resol, 2);
		assertEquals(1, table.getCounter());
		assertEquals(4 * Math.PI, table.getValueAsDouble(1, 0), 1e-10);
	}

	/**
	 * Test method for {@link ijt.measure.geometric.GeometricMeasures2D#particleArea(ij.process.ImageProcessor, int)}.
	 */
	@Test
	public final void testComputeParticleArea() {
		// initialize image with a square of side 4 in the middle
		ImageProcessor image = new ByteProcessor(10, 10);
		for (int y = 3; y < 7; y++) {
			for (int x = 3; x < 7; x++) {
				image.set(x, y, 255);
			}
		}

		int area = GeometricMeasures2D.particleArea(image, 255);
		assertEquals(16, area);
	}
	
	/**
	 * Test method for {@link ijt.measure.geometric.GeometricMeasures2D#particleArea(ij.process.ImageProcessor, int)}.
	 */
	@Test
	public final void testInertiaEllipse() {
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
}
