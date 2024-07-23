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

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * @author dlegland
 *
 */
public class LabelBoundariesLabeling2DTest
{

    /**
     * Test method for {@link inra.ijpb.label.conncomp.LabelBoundariesLabeling2D#process(ij.process.ImageProcessor)}.
     */
    @Test
    public final void testProcess_threeRegions()
    {
        ByteProcessor image = new ByteProcessor(10, 10);
        for (int j = 0; j < 3; j++)
        {
            for (int i = 0; i < 3; i++)
            {
                image.set(i+2, j+2, 3);
                image.set(i+5, j+2, 6);
            }
            for (int i = 0; i < 6; i++)
            {
                image.set(i+2, j+5, 8);
            }
        }
        
        LabelBoundariesLabeling2D algo = new LabelBoundariesLabeling2D();
        ImageProcessor res = algo.process(image).boundaryLabelMap;
        
        assertEquals(image.getWidth(), res.getWidth());
        assertEquals(image.getHeight(), res.getHeight());
        
        assertEquals(0, res.get(0,0));
        assertNotEquals(0, res.get(2,3));
        assertNotEquals(0, res.get(5,3));
        assertNotEquals(0, res.get(8,3));
        assertEquals(0, res.get(9,3));

        assertEquals(0, res.get(0,6));
        assertNotEquals(0, res.get(1,6));
        assertNotEquals(0, res.get(2,6));
        assertNotEquals(0, res.get(7,6));
        assertNotEquals(0, res.get(8,6));
        assertEquals(0, res.get(9,6));
    }

}
