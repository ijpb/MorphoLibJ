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
package inra.ijpb.shape;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.data.image.ImageUtils;

/**
 * @author dlegland
 *
 */
public class ImageShapeTest
{
    
    /**
     * Test method for {@link inra.ijpb.shape.ImageShape#subsample(ij.process.ImageProcessor, int)}.
     */
    @Test
    public final void testSubsample_2D()
    {
        ImageProcessor image = new ByteProcessor(10, 8);
        ImageUtils.fill(image, (x,y) -> y * 10.0 + x);
        
        ImageProcessor res = ImageShape.subsample(image, 2);
        
        assertEquals(image.getBitDepth(), res.getBitDepth());
        assertEquals(image.getWidth() / 2, res.getWidth());
        assertEquals(image.getHeight() / 2, res.getHeight());
        
        assertEquals( 0, res.get(0, 0));
        assertEquals( 8, res.get(4, 0));
        assertEquals(60, res.get(0, 3));
        assertEquals(68, res.get(4, 3));
    }
    
    /**
     * Test method for {@link inra.ijpb.shape.ImageShape#subsample(ij.process.ImageProcessor, int, int)}.
     */
    @Test
    public final void testSubsample_2D_aniso()
    {
        ImageProcessor image = new ByteProcessor(20, 9);
        ImageUtils.fill(image, (x,y) -> x * 10.0 + y);
        
        ImageProcessor res = ImageShape.subsample(image, 2, 3);
        
        assertEquals(image.getBitDepth(), res.getBitDepth());
        assertEquals(image.getWidth() / 2, res.getWidth());
        assertEquals(image.getHeight() / 3, res.getHeight());
        
        assertEquals(  0, res.get(0, 0));
        assertEquals(180, res.get(9, 0));
        assertEquals(  6, res.get(0, 2));
        assertEquals(186, res.get(9, 2));
    }
    
    /**
     * Test method for {@link inra.ijpb.shape.ImageShape#subsample(ij.ImageStack, int)}.
     */
    @Test
    public final void testSubsample3D()
    {
        ImageStack image = ImageStack.create(10, 8, 6, 16);
        ImageUtils.fill(image, (x, y, z) -> z * 100.0 + y * 10.0 + x);
        
        ImageStack res = ImageShape.subsample(image, 2);
        
        assertEquals(image.getBitDepth(), res.getBitDepth());
        assertEquals(image.getWidth() / 2, res.getWidth());
        assertEquals(image.getHeight() / 2, res.getHeight());
        assertEquals(image.getSize() / 2, res.getSize());
        
        assertEquals(  0, res.getVoxel(0, 0, 0), 0.1);
        assertEquals(  8, res.getVoxel(4, 0, 0), 0.1);
        assertEquals( 60, res.getVoxel(0, 3, 0), 0.1);
        assertEquals( 68, res.getVoxel(4, 3, 0), 0.1);
        assertEquals(400, res.getVoxel(0, 0, 2), 0.1);
        assertEquals(408, res.getVoxel(4, 0, 2), 0.1);
        assertEquals(460, res.getVoxel(0, 3, 2), 0.1);
        assertEquals(468, res.getVoxel(4, 3, 2), 0.1);
    }
    
    /**
     * Test method for {@link inra.ijpb.shape.ImageShape#subsample(ij.ImageStack, int, int, int)}.
     */
    @Test
    public final void testSubsample_3D_aniso()
    {
        ImageStack image = ImageStack.create(10, 8, 6, 16);
        ImageUtils.fill(image, (x, y, z) -> z * 100.0 + y * 10.0 + x);
        
        ImageStack res = ImageShape.subsample(image, 2, 4, 3);
        
        assertEquals(image.getBitDepth(), res.getBitDepth());
        assertEquals(image.getWidth() / 2, res.getWidth());
        assertEquals(image.getHeight() / 4, res.getHeight());
        assertEquals(image.getSize() / 3, res.getSize());
        
        assertEquals(  0, res.getVoxel(0, 0, 0), 0.1);
        assertEquals(  8, res.getVoxel(4, 0, 0), 0.1);
        assertEquals( 40, res.getVoxel(0, 1, 0), 0.1);
        assertEquals( 48, res.getVoxel(4, 1, 0), 0.1);
        assertEquals(300, res.getVoxel(0, 0, 1), 0.1);
        assertEquals(308, res.getVoxel(4, 0, 1), 0.1);
        assertEquals(340, res.getVoxel(0, 1, 1), 0.1);
        assertEquals(348, res.getVoxel(4, 1, 1), 0.1);
    }
    
}
