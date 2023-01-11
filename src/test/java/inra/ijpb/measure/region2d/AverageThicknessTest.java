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
package inra.ijpb.measure.region2d;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ij.measure.Calibration;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.measure.region2d.AverageThickness.Result;

/**
 * @author dlegland
 *
 */
public class AverageThicknessTest
{

    /**
     * Test method for {@link inra.ijpb.measure.region2d.AverageThickness#analyzeRegions(ij.process.ImageProcessor, int[], ij.measure.Calibration)}.
     */
    @Test
    public final void testAnalyzeRegionsImageProcessorIntArrayCalibration()
    {
        ImageProcessor image = new ByteProcessor(12, 8);
        for (int y = 2; y <= 6; y++)
        {
            for (int x = 2; x <= 10; x++)
            {
                image.set(x, y, 255);
            }
        }
        
        AverageThickness op = new AverageThickness();
        
        int[] labels = new int[] {255};
        Result[] results = op.analyzeRegions(image, labels, new Calibration());
        
        assertEquals(1, results.length);
        
        Result res = results[0];
        assertTrue(res.meanDist > 2);
        assertTrue(res.meanDist <= 3);
    }

    @Test
    public void test_process_ThinLines_TouchingLabels()
    {
        // Create a synthetic image containing 5 bars
        // bar 1: x =  0 ->  2,  y = 0 -> 9, label 2
        // bar 2: x =  3 -> 11,  y = 0 -> 2, label 3
        // bar 3: x =  3 -> 11,  y = 3 -> 5, label 4
        // bar 4: x =  3 -> 11,  y = 6 -> 8, label 5
        // bar 5: x = 12 -> 14,  y = 0 -> 9, label 7
        ImageProcessor image = new ByteProcessor(15, 9);
        for (int i = 0; i < 9; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                image.set(j, i, 2);
                image.set(i+3, j, 3);
                image.set(i+3, j+3, 4);
                image.set(i+3, j+6, 5);
                image.set(j+12, i, 7);
            }
        }
//        System.out.println("Image:");
//        IJUtils.printImage(image);
        
        AverageThickness op = new AverageThickness();
        
        int[] labels = new int[] {2, 3, 4, 5, 7};
        Result[] results = op.analyzeRegions(image, labels, new Calibration());
        
        assertEquals(5, results.length);
        
        for (int i = 0; i < labels.length; i++)
        {
            Result res = results[i];
            assertTrue(res.meanDist > 1.5);
            assertTrue(res.meanDist < 2.5);
        }
    }
}
