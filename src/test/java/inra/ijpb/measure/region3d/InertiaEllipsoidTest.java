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

    /**
     * Test method for {@link inra.ijpb.measure.region3d.InertiaEllipsoid#inertiaEllipsoids(ij.ImageStack, int[], ij.measure.Calibration)}.
     */
    @Test
    public final void testInertiaEllipsoid2()
    {
        String fileName = getClass().getResource("/files/ellipsoid_A30_B20_C10_T30_P30.tif").getFile();
        ImagePlus imagePlus = IJ.openImage(fileName);
        assertNotNull(imagePlus);
        ImageStack image = imagePlus.getStack();

        Calibration calib = imagePlus.getCalibration();
        Ellipsoid elli = InertiaEllipsoid.inertiaEllipsoids(image, new int[] {255}, calib)[0];
        
        assertEquals(30, elli.radius1(), .1);
        assertEquals(20, elli.radius2(), .1);
        assertEquals(10, elli.radius3(), .1);
        assertEquals(30, elli.theta(), .1);
        assertEquals(30, elli.phi(), .1);
    }

    /**
     * Test method for {@link inra.ijpb.measure.region3d.InertiaEllipsoid#inertiaEllipsoids(ij.ImageStack, int[], ij.measure.Calibration)}.
     */
    @Test
    public final void testInertiaEllipsoid3()
    {
        String fileName = getClass().getResource("/files/ellipsoid_A45_B35_C25_T30_P20_R10.tif").getFile();
        ImagePlus imagePlus = IJ.openImage(fileName);
        assertNotNull(imagePlus);
        ImageStack image = imagePlus.getStack();

        Calibration calib = imagePlus.getCalibration();
        Ellipsoid elli = InertiaEllipsoid.inertiaEllipsoids(image, new int[] {255}, calib)[0];
        
        assertEquals(45, elli.radius1(), .1);
        assertEquals(35, elli.radius2(), .1);
        assertEquals(25, elli.radius3(), .1);
        assertEquals(30, elli.phi(), 1.0);
        assertEquals(20, elli.theta(), 1.0);
        assertEquals(10, elli.psi(), 1.0);
    }
}
