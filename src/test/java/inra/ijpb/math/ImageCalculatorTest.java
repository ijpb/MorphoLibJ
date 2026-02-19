/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2026 INRA.
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
package inra.ijpb.math;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/**
 * @author dlegland
 *
 */
public class ImageCalculatorTest
{
    
    /**
     * Test method for {@link inra.ijpb.math.ImageCalculator#combineImages(ij.process.ImageProcessor, ij.process.ImageProcessor, inra.ijpb.math.ImageCalculator.Operation)}.
     */
    @Test
    public final void testCombineImages_ByteByte_MAX()
    {
        int width = 30;
        int height = 20;
        ImageProcessor image1 = new ByteProcessor(width, height);
        ImageProcessor image2 = new ByteProcessor(width, height);
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                image1.set(i, j, i);
                image2.set(i, j, j);
            }
        }
        
        ImageCalculator.Operation op = ImageCalculator.Operation.MAX;
        ImageProcessor result = ImageCalculator.combineImages(image1, image2, op);
        
        assertTrue(result instanceof ByteProcessor);
        assertEquals(30, result.getWidth());
        assertEquals(20, result.getHeight());
        assertEquals(29, result.get(29, 19));
        assertEquals(29, result.get(29, 0));
        assertEquals(19, result.get(0, 19));
    }
    
    /**
     * Test method for {@link inra.ijpb.math.ImageCalculator#combineImages(ij.process.ImageProcessor, ij.process.ImageProcessor, inra.ijpb.math.ImageCalculator.Operation)}.
     */
    @Test
    public final void testCombineImages_FloatFloat_MAX()
    {
        int width = 30;
        int height = 20;
        ImageProcessor image1 = new FloatProcessor(width, height);
        ImageProcessor image2 = new FloatProcessor(width, height);
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                image1.set(i, j, i);
                image2.set(i, j, j);
            }
        }
        
        ImageCalculator.Operation op = ImageCalculator.Operation.MAX;
        ImageProcessor result = ImageCalculator.combineImages(image1, image2, op);
        
        assertTrue(result instanceof FloatProcessor);
        assertEquals(30, result.getWidth());
        assertEquals(20, result.getHeight());
        assertEquals(29, result.get(29, 19));
        assertEquals(29, result.get(29, 0));
        assertEquals(19, result.get(0, 19));
    }
    
    /**
     * Test method for {@link inra.ijpb.math.ImageCalculator#combineImages(ij.process.ImageProcessor, ij.process.ImageProcessor, inra.ijpb.math.ImageCalculator.Operation)}.
     */
    @Test
    public final void testCombineImages_FloatFloat_Times()
    {
        int width = 30;
        int height = 20;
        ImageProcessor image1 = new FloatProcessor(width, height);
        ImageProcessor image2 = new FloatProcessor(width, height);
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                image1.setf(i, j, (float) (i*3.2));
                image2.setf(i, j, (float) (j*4.3));
            }
        }
        
        ImageCalculator.Operation op = ImageCalculator.Operation.TIMES;
        ImageProcessor result = ImageCalculator.combineImages(image1, image2, op);
        
        assertTrue(result instanceof FloatProcessor);
        assertEquals(30, result.getWidth());
        assertEquals(20, result.getHeight());
        
        assertEquals(29*3.2*19*4.3, result.getf(29, 19), .001);
        assertEquals(0, result.getf(29, 0), .001);
        assertEquals(0, result.getf(0, 19), .001);
    }
    
    /**
     * Test method for {@link inra.ijpb.math.ImageCalculator#combineImages(ij.process.ImageProcessor, ij.process.ImageProcessor, inra.ijpb.math.ImageCalculator.Operation)}.
     */
    @Test
    public final void testCombineImages_FloatFloat_Times_Self()
    {
        int width = 30;
        int height = 20;
        ImageProcessor image1 = new FloatProcessor(width, height);
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                image1.setf(i, j, (float) (i+j));
            }
        }
        
        ImageCalculator.Operation op = ImageCalculator.Operation.TIMES;
        ImageProcessor result = ImageCalculator.combineImages(image1, image1, op);
        
        assertTrue(result instanceof FloatProcessor);
        assertEquals(30, result.getWidth());
        assertEquals(20, result.getHeight());
        
        assertEquals(48f*48f, result.getf(29, 19), .001);
        assertEquals(29f*29f, result.getf(29, 0), .001);
        assertEquals(19f*19f, result.getf(0, 19), .001);
    }
    
}
