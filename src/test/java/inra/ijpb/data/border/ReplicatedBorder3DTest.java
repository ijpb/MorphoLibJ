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
package inra.ijpb.data.border;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.ImageStack;

/**
 * @author dlegland
 *
 */
public class ReplicatedBorder3DTest
{
    
    /**
     * Test method for {@link inra.ijpb.data.border.ReplicatedBorder3D#get(int, int)}.
     */
    @Test
    public final void testAddBorders()
    {
        ImageStack image = createTestImage();
        
        BorderManager3D border = new ReplicatedBorder3D(image);
        
        ImageStack res = border.addBorders(image, 1, 2, 3, 4, 5, 6);
        
        assertEquals(8+3, res.getWidth());
        assertEquals(6+7, res.getHeight());
        assertEquals(4+11, res.getSize());
        
        assertEquals( 0, border.get( 0,  0,  0));
        assertEquals( 7, border.get(10,  0,  0));
        assertEquals(50, border.get( 0, 12,  0));
        assertEquals(57, border.get(10, 12,  0));
        assertEquals( 0, border.get( 0,  0, 14));
        assertEquals( 7, border.get(10,  0, 14));
        assertEquals(50, border.get( 0, 12, 14));
        assertEquals(57, border.get(10, 12, 14));
    }
    
    /**
     * Test method for {@link inra.ijpb.data.border.ReplicatedBorder3D#get(int, int)}.
     */
    @Test
    public final void testGet()
    {
        ImageStack image = createTestImage();
        
        BorderManager3D border = new ReplicatedBorder3D(image);
        
        assertEquals( 0, border.get(-1, -1, -1));
        assertEquals( 7, border.get( 8, -1, -1));
        assertEquals(50, border.get(-1,  6, -1));
        assertEquals(57, border.get( 8,  6, -1));
        assertEquals( 0, border.get(-1, -1,  4));
        assertEquals( 7, border.get( 8, -1,  4));
        assertEquals(50, border.get(-1,  6,  4));
        assertEquals(57, border.get( 8,  6,  4));
    }
    
    private ImageStack createTestImage()
    {
        ImageStack image = ImageStack.create(8, 6, 4, 8);
        for (int z = 0; z < 4; z++)
        {
            for (int y = 0; y < 6; y++)
            {
                for (int x = 0; x < 8; x++)
                {
                    image.setVoxel(x, y, z, y * 10 + x);
                }
            }
        }
        return image;
    }
    
}
