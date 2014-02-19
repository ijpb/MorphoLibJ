/**
 * 
 */
package inra.ijpb.measure;

import static org.junit.Assert.*;
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

}
