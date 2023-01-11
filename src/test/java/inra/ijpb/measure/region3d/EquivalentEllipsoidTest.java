/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2023 INRA.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
/**
 * 
 */
package inra.ijpb.measure.region3d;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
public class EquivalentEllipsoidTest
{

    /**
     * Test method for {@link inra.ijpb.measure.region3d.EquivalentEllipsoid#equivalentEllipsoids(ij.ImageStack, int[], ij.measure.Calibration)}.
     */
    @Test
    public final void testEquivalentEllipsoids()
    {
        String fileName = getClass().getResource("/files/ellipsoid_A30_B20_C10_T00_P00.tif").getFile();
        ImagePlus imagePlus = IJ.openImage(fileName);
        assertNotNull(imagePlus);
        ImageStack image = imagePlus.getStack();

        Calibration calib = imagePlus.getCalibration();
        Ellipsoid elli = EquivalentEllipsoid.equivalentEllipsoids(image, new int[] {255}, calib)[0];
        
        assertEquals(30, elli.radius1(), .1);
        assertEquals(20, elli.radius2(), .1);
        assertEquals(10, elli.radius3(), .1);
    }

    /**
     * Test method for {@link inra.ijpb.measure.region3d.EquivalentEllipsoid#equivalentEllipsoids(ij.ImageStack, int[], ij.measure.Calibration)}.
     */
    @Test
    public final void testEquivalentEllipsoid2()
    {
        String fileName = getClass().getResource("/files/ellipsoid_A30_B20_C10_T30_P30.tif").getFile();
        ImagePlus imagePlus = IJ.openImage(fileName);
        assertNotNull(imagePlus);
        ImageStack image = imagePlus.getStack();

        Calibration calib = imagePlus.getCalibration();
        Ellipsoid elli = EquivalentEllipsoid.equivalentEllipsoids(image, new int[] {255}, calib)[0];
        
        assertEquals(30, elli.radius1(), .1);
        assertEquals(20, elli.radius2(), .1);
        assertEquals(10, elli.radius3(), .1);
        assertEquals(30, elli.theta(), .1);
        assertEquals(30, elli.phi(), .1);
    }

    /**
     * Test method for {@link inra.ijpb.measure.region3d.EquivalentEllipsoid#equivalentEllipsoids(ij.ImageStack, int[], ij.measure.Calibration)}.
     */
    @Test
    public final void testEquivalentEllipsoid3()
    {
        String fileName = getClass().getResource("/files/ellipsoid_A45_B35_C25_T30_P20_R10.tif").getFile();
        ImagePlus imagePlus = IJ.openImage(fileName);
        assertNotNull(imagePlus);
        ImageStack image = imagePlus.getStack();

        Calibration calib = imagePlus.getCalibration();
        Ellipsoid elli = EquivalentEllipsoid.equivalentEllipsoids(image, new int[] {255}, calib)[0];
        
        assertEquals(45, elli.radius1(), .1);
        assertEquals(35, elli.radius2(), .1);
        assertEquals(25, elli.radius3(), .1);
        assertEquals(30, elli.phi(), 1.0);
        assertEquals(20, elli.theta(), 1.0);
        assertEquals(10, elli.psi(), 1.0);
    }

    /**
     * Test method for
     * {@link inra.ijpb.measure.region3d.EquivalentEllipsoid#equivalentEllipsoids(ij.ImageStack, int[], ij.measure.Calibration)}.
     */
    @Test
    public final void test_SingleVoxelRegion()
    {
        ImageStack image = ImageStack.create(5, 5, 5, 8);
        image.setVoxel(2, 2, 0, 10);
        Calibration calib = new Calibration();
        Ellipsoid elli = EquivalentEllipsoid.equivalentEllipsoids(image, new int[] { 10 }, calib)[0];

        double expectedRadius = Math.sqrt(5.0 / 12.0);
        assertEquals(2.5, elli.center().getX(), 0.1);
        assertEquals(2.5, elli.center().getY(), 0.1);
        assertEquals(0.5, elli.center().getZ(), 0.1);
        assertEquals(expectedRadius, elli.radius1(), 0.1);
        assertEquals(expectedRadius, elli.radius2(), 0.1);
        assertEquals(expectedRadius, elli.radius3(), 0.1);
        assertEquals(0.0, elli.phi(), 1.0);
        assertEquals(0.0, elli.theta(), 1.0);
        assertEquals(0.0, elli.psi(), 1.0);
    }

    /**
     * Test method for
     * {@link inra.ijpb.measure.region3d.EquivalentEllipsoid#equivalentEllipsoids(ij.ImageStack, int[], ij.measure.Calibration)}.
     */
    @Test
    public final void test_EmptyRegion()
    {
        // generate an 8-bits image with a single region with label 10 
        ImageStack image = ImageStack.create(5, 5, 5, 8);
        image.setVoxel(2, 2, 0, 10);
        Calibration calib = new Calibration();
        
        // compute for another region not in the image
        Ellipsoid elli = EquivalentEllipsoid.equivalentEllipsoids(image, new int[] { 20 }, calib)[0];

        double expectedRadius = 0.0;
        assertEquals(expectedRadius, elli.radius1(), 0.1);
        assertEquals(expectedRadius, elli.radius2(), 0.1);
        assertEquals(expectedRadius, elli.radius3(), 0.1);
        assertEquals(0.0, elli.phi(), 1.0);
        assertEquals(0.0, elli.theta(), 1.0);
        assertEquals(0.0, elli.psi(), 1.0);
    }

    /**
     * Test method for
     * {@link inra.ijpb.measure.region3d.EquivalentEllipsoid#equivalentEllipsoids(ij.ImageStack, int[], ij.measure.Calibration)}.
     */
    @Test
    public final void test_SmallRegionWithAzimutGreaterThan90Degrees()
    {
        // generate an 8-bits image with a single region with label 10 
        ImageStack image = ImageStack.create(6, 8, 4, 8);
        // encode the region as a series of (yi, xi0, xi1) triplets
        int[][] ranges = new int[][]{{1, 3, 3}, {2, 2, 4}, {3, 1, 4}, {4, 3, 4}, {5, 2, 4}, {6, 4, 4}};
        for (int[] range : ranges)
        {
            int y = range[0];
            int x0 = range[1];
            int x1 = range[2];
            for (int x = x0; x <= x1; x++)
            {
                image.setVoxel(x, y, 0, 10);
            }
        }
        Calibration calib = new Calibration();
        
        // compute for another region not in the image
        Ellipsoid elli = EquivalentEllipsoid.equivalentEllipsoids(image, new int[] { 10 }, calib)[0];

        assertTrue(elli.radius1() > 0.0);
        assertTrue(elli.radius2() > 0.0);
        assertTrue(elli.radius3() > 0.0);
        assertTrue(elli.phi() > -90.0);
        assertTrue(elli.phi() <  90.0);
        assertTrue(elli.theta() > -90.0);
        assertTrue(elli.theta() <  90.0);
        assertEquals(0.0, elli.psi(), 1.0);
    }

    /**
     * Test method for {@link inra.ijpb.measure.region3d.EquivalentEllipsoid#equivalentEllipsoids(ij.ImageStack, int[], ij.measure.Calibration)}.
     */
    @Test
    public final void testRandomProlateEllipsoid3()
    {
        String fileName = getClass().getResource("/files/random_prolate_ellipsoid_01.tif").getFile();
        ImagePlus imagePlus = IJ.openImage(fileName);
        assertNotNull(imagePlus);
        ImageStack image = imagePlus.getStack();

        Calibration calib = imagePlus.getCalibration();
        Ellipsoid elli = EquivalentEllipsoid.equivalentEllipsoids(image, new int[] {255}, calib)[0];
        
        // Compare with values obtained with Matlab equivalent function
        // https://github.com/mattools/matImage/blob/master/matImage/imMeasures/imEquivalentEllipsoid.m
        assertEquals(25, elli.center().getX(), .5);
        assertEquals(25, elli.center().getY(), .5);
        assertEquals(25, elli.center().getZ(), .5);
        assertEquals(20, elli.radius1(), .5);
        assertEquals(5, elli.radius2(), .5);
        assertEquals(5, elli.radius3(), .5);
        assertEquals(-32.97, elli.phi(), .2);
        assertEquals(-51.98, elli.theta(), .2);
        assertEquals(163.05, elli.psi(), .2);
    }

    /**
     * Test method for {@link inra.ijpb.measure.region3d.EquivalentEllipsoid#equivalentEllipsoids(ij.ImageStack, int[], ij.measure.Calibration)}.
     */
    @Test
    public final void testCuboid_X30_Y20_Z10()
    {
        ImageStack image = createCenteredCuboid(50, 50, 50, 30, 20, 10);
        
        Calibration calib = new Calibration();
        Ellipsoid elli = EquivalentEllipsoid.equivalentEllipsoids(image, new int[] {255}, calib)[0];
        
        assertEquals( 0, ensureAngleWithin0And180(elli.phi()),   1.0);
        assertEquals( 0, ensureAngleWithin0And180(elli.theta()), 1.0);
        assertEquals( 0, ensureAngleWithin0And180(elli.psi() ),  1.0);
    }

    /**
     * Test method for {@link inra.ijpb.measure.region3d.EquivalentEllipsoid#equivalentEllipsoids(ij.ImageStack, int[], ij.measure.Calibration)}.
     */
    @Test
    public final void testCuboid_X30_Y10_Z20()
    {
        ImageStack image = createCenteredCuboid(50, 50, 50, 30, 10, 20);
        
        Calibration calib = new Calibration();
        Ellipsoid elli = EquivalentEllipsoid.equivalentEllipsoids(image, new int[] {255}, calib)[0];
        
        assertEquals( 0, ensureAngleWithin0And180(elli.phi()),   1.0);
        assertEquals( 0, ensureAngleWithin0And180(elli.theta()), 1.0);
        assertEquals(90, ensureAngleWithin0And180(elli.psi() ),  1.0);
    }
    
    /**
     * Test method for {@link inra.ijpb.measure.region3d.EquivalentEllipsoid#equivalentEllipsoids(ij.ImageStack, int[], ij.measure.Calibration)}.
     */
    @Test
    public final void testCuboid_X20_Y30_Z10()
    {
        ImageStack image = createCenteredCuboid(50, 50, 50, 20, 30, 10);
        
        Calibration calib = new Calibration();
        Ellipsoid elli = EquivalentEllipsoid.equivalentEllipsoids(image, new int[] {255}, calib)[0];
        
        assertEquals(90, ensureAngleWithin0And180(elli.phi()),   1.0);
        assertEquals( 0, ensureAngleWithin0And180(elli.theta()), 1.0);
        assertEquals( 0, ensureAngleWithin0And180(elli.psi() ),  1.0);
    }
    
    /**
     * Test method for {@link inra.ijpb.measure.region3d.EquivalentEllipsoid#equivalentEllipsoids(ij.ImageStack, int[], ij.measure.Calibration)}.
     */
    @Test
    public final void testCuboid_X10_Y30_Z20()
    {
        ImageStack image = createCenteredCuboid(50, 50, 50, 10, 30, 20);
        
        Calibration calib = new Calibration();
        Ellipsoid elli = EquivalentEllipsoid.equivalentEllipsoids(image, new int[] {255}, calib)[0];
        
        assertEquals(90, ensureAngleWithin0And180(elli.phi()),   1.0);
        assertEquals( 0, ensureAngleWithin0And180(elli.theta()), 1.0);
        assertEquals(90, ensureAngleWithin0And180(elli.psi() ),  1.0);
    }
    
    /**
     * Test method for {@link inra.ijpb.measure.region3d.EquivalentEllipsoid#equivalentEllipsoids(ij.ImageStack, int[], ij.measure.Calibration)}.
     */
    @Test
    public final void testCuboid_X10_Y20_Z30()
    {
        ImageStack image = createCenteredCuboid(50, 50, 50, 10, 20, 30);
        
        Calibration calib = new Calibration();
        Ellipsoid elli = EquivalentEllipsoid.equivalentEllipsoids(image, new int[] {255}, calib)[0];
        
        assertEquals( 0, ensureAngleWithin0And180(elli.phi()),   1.0);
        assertEquals(90, ensureAngleWithin0And180(elli.theta()), 1.0);
        assertEquals( 0, ensureAngleWithin0And180(elli.psi() ),  1.0);
    }
    
    /**
     * Test method for {@link inra.ijpb.measure.region3d.EquivalentEllipsoid#equivalentEllipsoids(ij.ImageStack, int[], ij.measure.Calibration)}.
     */
    @Test
    public final void testCuboid_X20_Y10_Z30()
    {
        ImageStack image = createCenteredCuboid(50, 50, 50, 20, 10, 30);
        
        Calibration calib = new Calibration();
        Ellipsoid elli = EquivalentEllipsoid.equivalentEllipsoids(image, new int[] {255}, calib)[0];
        
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
