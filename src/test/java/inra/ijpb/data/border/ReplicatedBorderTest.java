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
package inra.ijpb.data.border;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * @author dlegland
 *
 */
public class ReplicatedBorderTest
{
    
    /**
     * Test method for {@link inra.ijpb.data.border.ReplicatedBorder#get(int, int)}.
     */
    @Test
    public final void testAddBorders()
    {
        ImageProcessor image = createTestImage();
        
        BorderManager border = new ReplicatedBorder(image);
        
        ImageProcessor res = border.addBorders(image, 1, 2, 3, 4);
        
        assertEquals(8+3, res.getWidth());
        assertEquals(6+7, res.getHeight());
        
        assertEquals( 0, border.get( 0,  0));
        assertEquals( 7, border.get(10,  0));
        assertEquals(50, border.get( 0, 12));
        assertEquals(57, border.get(10, 12));
    }
    
    /**
     * Test method for {@link inra.ijpb.data.border.ReplicatedBorder#get(int, int)}.
     */
    @Test
    public final void testGet()
    {
        ImageProcessor image = createTestImage();
        
        BorderManager border = new ReplicatedBorder(image);
        
        assertEquals( 0, border.get(-1, -1));
        assertEquals( 7, border.get( 8, -1));
        assertEquals(50, border.get(-1,  6));
        assertEquals(57, border.get( 8,  6));
    }
    
    private ImageProcessor createTestImage()
    {
        ImageProcessor image = new ByteProcessor(8, 6);
        for (int y = 0; y < 6; y++)
        {
            for (int x = 0; x < 8; x++)
            {
                image.set(x, y, y * 10 + x);
            }
        }
        return image;
    }
    
}
