/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
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

import org.junit.Test;

import ij.ImageStack;
import ij.measure.Calibration;

/**
 * @author dlegland
 *
 */
public class InterfaceSurfaceAreaTest
{

    /**
     * Test method for {@link inra.ijpb.measure.region3d.InterfaceSurfaceArea#process(ij.ImageStack, int, int, ij.measure.Calibration)}.
     */
    @Test
    public final void testProcess_SingleVoxel()
    {
        ImageStack stack = ImageStack.create(5, 5, 5, 8);
        for (int z = 0; z < 5; z++)
        {
            for (int y = 0; y < 5; y++)
            {
                for (int x = 0; x < 5; x++)
                {
                    stack.setVoxel(x, y, z, 1);
                }
            }
        }
        stack.setVoxel(2, 2, 2, 2);
   
        Calibration calib = new Calibration();
        
        InterfaceSurfaceArea algo = new InterfaceSurfaceArea();
        
        // labels 1-2  -> R = 1, S ~= 3.14 
        double S12 = algo.process(stack, 1, 2, calib);
        double expS12 = Math.PI * 4 * .5 * .5; 
        assertEquals(expS12, S12, expS12 * .05);
    }

    /**
     * Test method for {@link inra.ijpb.measure.region3d.InterfaceSurfaceArea#process(ij.ImageStack, int, int, ij.measure.Calibration)}.
     */
    @Test
    public final void testProcess_ThreeBalls()
    {
        ImageStack stack = createThreeNestedBalls();
        Calibration calib = new Calibration();
        
        InterfaceSurfaceArea algo = new InterfaceSurfaceArea();
        
        // labels 1-2  -> R = 5, S ~= 314.16
        double S12 = algo.process(stack, 1, 2, calib);
        double expS12 = Math.PI * 4 * 5 * 5; 
        assertEquals(expS12, S12, expS12 * .05);

        // labels 2-3  -> R = 10, S ~= 1 256.6
        double S23 = algo.process(stack, 2, 3, calib);
        double expS23 = Math.PI * 4 * 10 * 10; 
        assertEquals(expS23, S23, expS23 * .05);
        
        // labels 1-3 or 2-0  -> no interface
        assertEquals(0, algo.process(stack, 1, 3, calib), 0.05);
        assertEquals(0, algo.process(stack, 2, 0, calib), 0.05);
    }

    private static final ImageStack createThreeNestedBalls()
    {
        int sizeX = 50, sizeY = 50, sizeZ = 50;
        ImageStack stack = ImageStack.create(sizeX, sizeY, sizeZ, 8);
        
        int r1 = 5;
        int r2 = 10;
        int r3 = 15;
        
        double xc = 20.123;
        double yc = 20.234;
        double zc = 20.345;
        
        for (int z = 0; z < sizeZ; z++)
        {
            double z2 = z - zc;
            for (int y = 0; y < sizeY; y++)
            {
                double y2 = y - yc;
                double h1 = Math.hypot(y2, z2);
                for (int x = 0; x < sizeX; x++)
                {
                    double x2 = x - xc;
                    double h = Math.hypot(h1, x2);
                    
                    if (h < r1)
                    {
                        stack.setVoxel(x, y, z, 1);
                    }
                    else if (h < r2)
                    {
                        stack.setVoxel(x, y, z, 2);
                    }
                    else if (h < r3)
                    {
                        stack.setVoxel(x, y, z, 3);
                    }
                }
            }
        }
        
        return stack;
    }
}
