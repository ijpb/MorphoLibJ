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
package inra.ijpb.label.conncomp;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.ImageStack;

/**
 * @author dlegland
 *
 */
public class LabelBoundariesLabeling3DTest
{

    /**
     * Test method for {@link inra.ijpb.label.conncomp.LabelBoundariesLabeling3D#process(ij.ImageStack)}.
     */
    @Test
    public final void testProcess_threeRegions()
    {
        ImageStack image = ImageStack.create(10, 10, 10, 8);
        for (int k = 0; k < 3; k++)
        {
            for (int j = 0; j < 3; j++)
            {
                for (int i = 0; i < 3; i++)
                {
                    image.setVoxel(i+2, j+2, k+2, 3);
                    image.setVoxel(i+5, j+2, k+2, 6);
                }
                for (int i = 0; i < 6; i++)
                {
                    image.setVoxel(i+2, j+5, k+2, 8);
                }
            }
            
            for (int j = 0; j < 6; j++)
            {
                for (int i = 0; i < 6; i++)
                {
                    image.setVoxel(i+2, j+2, k+5, 9);
                }
            }
        }
        
        LabelBoundariesLabeling3D algo = new LabelBoundariesLabeling3D();
        ImageStack res = algo.process(image).boundaryLabelMap;
        
        assertEquals(image.getWidth(), res.getWidth());
        assertEquals(image.getHeight(), res.getHeight());
        assertEquals(image.getSize(), res.getSize());
        
        // horizontal transect at  y = 3 and z = 3
        assertEquals(0, (int) res.getVoxel(0, 3, 3));
        assertNotEquals(0, (int) res.getVoxel(2, 3, 3));
        assertNotEquals(0, (int) res.getVoxel(5, 3, 3));
        assertNotEquals(0, (int) res.getVoxel(8, 3, 3));
        assertEquals(0, (int) res.getVoxel(9, 3, 3));

        // horizontal transect at  y = 6 and z = 3
        assertEquals(0, (int) res.getVoxel(0, 6, 3));
        assertNotEquals(0, (int) res.getVoxel(1, 6, 3));
        assertNotEquals(0, (int) res.getVoxel(2, 6, 3));
        assertNotEquals(0, (int) res.getVoxel(7, 6, 3));
        assertNotEquals(0, (int) res.getVoxel(8, 6, 3));
        assertEquals(0, (int) res.getVoxel(9, 6, 3));
        
        // vertical transect at  x = 3 and y = 3
        assertEquals(0, (int) res.getVoxel(3, 3, 0));
        assertNotEquals(0, (int) res.getVoxel(3, 3, 1));
        assertNotEquals(0, (int) res.getVoxel(3, 3, 2));
        assertEquals(0, (int) res.getVoxel(3, 3, 3));
        assertNotEquals(0, (int) res.getVoxel(3, 3, 4));
        assertNotEquals(0, (int) res.getVoxel(3, 3, 5));
        assertEquals(0, (int) res.getVoxel(3, 3, 6));
        assertNotEquals(0, (int) res.getVoxel(3, 3, 7));
        assertNotEquals(0, (int) res.getVoxel(3, 3, 8));
        assertEquals(0, (int) res.getVoxel(3, 3, 9));
    }

}
