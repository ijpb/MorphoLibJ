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

    /**
     * Test method for {@link inra.ijpb.measure.region3d.InertiaEllipsoid#inertiaEllipsoids(ij.ImageStack, int[], ij.measure.Calibration)}.
     */
    @Test
    public final void testCuboid_X30_Y20_Z10()
    {
        ImageStack image = createCenteredCuboid(50, 50, 50, 30, 20, 10);
        
        Calibration calib = new Calibration();
        Ellipsoid elli = InertiaEllipsoid.inertiaEllipsoids(image, new int[] {255}, calib)[0];
        
        assertEquals( 0, ensureAngleWithin0And180(elli.phi()),   1.0);
        assertEquals( 0, ensureAngleWithin0And180(elli.theta()), 1.0);
        assertEquals( 0, ensureAngleWithin0And180(elli.psi() ),  1.0);
    }

    /**
     * Test method for {@link inra.ijpb.measure.region3d.InertiaEllipsoid#inertiaEllipsoids(ij.ImageStack, int[], ij.measure.Calibration)}.
     */
    @Test
    public final void testCuboid_X30_Y10_Z20()
    {
        ImageStack image = createCenteredCuboid(50, 50, 50, 30, 10, 20);
        
        Calibration calib = new Calibration();
        Ellipsoid elli = InertiaEllipsoid.inertiaEllipsoids(image, new int[] {255}, calib)[0];
        
        assertEquals( 0, ensureAngleWithin0And180(elli.phi()),   1.0);
        assertEquals( 0, ensureAngleWithin0And180(elli.theta()), 1.0);
        assertEquals(90, ensureAngleWithin0And180(elli.psi() ),  1.0);
    }
    
    /**
     * Test method for {@link inra.ijpb.measure.region3d.InertiaEllipsoid#inertiaEllipsoids(ij.ImageStack, int[], ij.measure.Calibration)}.
     */
    @Test
    public final void testCuboid_X20_Y30_Z10()
    {
        ImageStack image = createCenteredCuboid(50, 50, 50, 20, 30, 10);
        
        Calibration calib = new Calibration();
        Ellipsoid elli = InertiaEllipsoid.inertiaEllipsoids(image, new int[] {255}, calib)[0];
        
        assertEquals(90, ensureAngleWithin0And180(elli.phi()),   1.0);
        assertEquals( 0, ensureAngleWithin0And180(elli.theta()), 1.0);
        assertEquals( 0, ensureAngleWithin0And180(elli.psi() ),  1.0);
    }
    
    /**
     * Test method for {@link inra.ijpb.measure.region3d.InertiaEllipsoid#inertiaEllipsoids(ij.ImageStack, int[], ij.measure.Calibration)}.
     */
    @Test
    public final void testCuboid_X10_Y30_Z20()
    {
        ImageStack image = createCenteredCuboid(50, 50, 50, 10, 30, 20);
        
        Calibration calib = new Calibration();
        Ellipsoid elli = InertiaEllipsoid.inertiaEllipsoids(image, new int[] {255}, calib)[0];
        
        assertEquals(90, ensureAngleWithin0And180(elli.phi()),   1.0);
        assertEquals( 0, ensureAngleWithin0And180(elli.theta()), 1.0);
        assertEquals(90, ensureAngleWithin0And180(elli.psi() ),  1.0);
    }
    
    /**
     * Test method for {@link inra.ijpb.measure.region3d.InertiaEllipsoid#inertiaEllipsoids(ij.ImageStack, int[], ij.measure.Calibration)}.
     */
    @Test
    public final void testCuboid_X10_Y20_Z30()
    {
        ImageStack image = createCenteredCuboid(50, 50, 50, 10, 20, 30);
        
        Calibration calib = new Calibration();
        Ellipsoid elli = InertiaEllipsoid.inertiaEllipsoids(image, new int[] {255}, calib)[0];
        
        assertEquals( 0, ensureAngleWithin0And180(elli.phi()),   1.0);
        assertEquals(90, ensureAngleWithin0And180(elli.theta()), 1.0);
        assertEquals( 0, ensureAngleWithin0And180(elli.psi() ),  1.0);
    }
    
    /**
     * Test method for {@link inra.ijpb.measure.region3d.InertiaEllipsoid#inertiaEllipsoids(ij.ImageStack, int[], ij.measure.Calibration)}.
     */
    @Test
    public final void testCuboid_X20_Y10_Z30()
    {
        ImageStack image = createCenteredCuboid(50, 50, 50, 20, 10, 30);
        
        Calibration calib = new Calibration();
        Ellipsoid elli = InertiaEllipsoid.inertiaEllipsoids(image, new int[] {255}, calib)[0];
        
        assertEquals( 0, ensureAngleWithin0And180(elli.phi()),   1.0);
        assertEquals(90, ensureAngleWithin0And180(elli.theta()), 1.0);
        assertEquals(90, ensureAngleWithin0And180(elli.psi() ),  1.0);
    }
    

    private ImageStack createCenteredCuboid(int dimX, int dimY, int dimZ, int sizeX, int sizeY, int sizeZ)
    {
        ImageStack image = ImageStack.create(dimX, dimY, dimZ, 8);

        int hwx = sizeX / 2;
        int hwy = sizeY / 2;
        int hwz = sizeZ / 2;
        
        for (int z = dimZ/2 - hwz; z < dimZ/2 + hwz; z++)
        {
            for (int y = dimY/2 - hwy; y < dimY/2 + hwy; y++)
            {
                for (int x = dimX/2 - hwx; x < dimX/2 + hwx; x++)
                {
                    image.setVoxel(x, y, z, 255);
                }
            }
        }
        
        return image;
    }
    
    private static final double ensureAngleWithin0And180(double angleInDegrees)
    {
        return ((angleInDegrees % 180) + 180 ) % 180;
    }
}
