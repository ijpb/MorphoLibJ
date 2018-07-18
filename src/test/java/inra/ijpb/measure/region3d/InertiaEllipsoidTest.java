/**
 * 
 */
package inra.ijpb.measure.region3d;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import inra.ijpb.geometry.Ellipsoid;

/**
 * @author dlegland
 *
 */
public class InertiaEllipsoidTest
{

	/**
	 * Test method for {@link inra.ijpb.measure.region3d.InertiaEllipsoid#inertiaEllipsoids(ij.ImageStack, int[], ij.measure.Calibration)}.
	 */
	@Test
	public final void testInertiaEllipsoids()
	{
		String fileName = getClass().getResource("/files/ellipsoid_A30_B20_C10_T00_P00.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		assertNotNull(imagePlus);
		ImageStack image = imagePlus.getStack();

		Calibration calib = imagePlus.getCalibration();
		Ellipsoid elli = InertiaEllipsoid.inertiaEllipsoids(image, new int[] {255}, calib)[0];
		
		assertEquals(30, elli.radius1(), .1);
		assertEquals(20, elli.radius2(), .1);
		assertEquals(10, elli.radius3(), .1);
	}
}
