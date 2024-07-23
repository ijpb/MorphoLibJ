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
package inra.ijpb.label.filter;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.ImageStack;
import inra.ijpb.binary.distmap.ChamferMask3D;

/**
 * @author dlegland
 *
 */
public class ChamferLabelDilation3DShortTest
{

    /**
     * Test method for {@link inra.ijpb.label.filter.ChamferLabelDilation3DShort#process(ij.ImageStack)}.
     */
    @Test
    public final void testProcess_TwoRegions()
    {
        // generate a test image with two regions separated by a two-voxel wide
        // background
        ImageStack labels = ImageStack.create(12, 9, 9, 8);
        labels.setVoxel(4, 4, 4, 5);
        labels.setVoxel(7, 4, 4, 6);
        
        ChamferLabelDilation3DShort algo = new ChamferLabelDilation3DShort(ChamferMask3D.BORGEFORS, 2.0);
        ImageStack result = algo.process(labels);
        
        // result should have same bit depth
        assertEquals(8, result.getBitDepth());
        // result should have same size
        assertEquals(labels.getWidth(), result.getWidth());
        assertEquals(labels.getHeight(), result.getHeight());
        assertEquals(labels.getSize(), result.getSize());
        
        // check region with label 5
        assertEquals(5, result.getVoxel(4, 4, 4), 0.01);
        assertEquals(5, result.getVoxel(5, 4, 4), 0.01);
        assertEquals(5, result.getVoxel(2, 4, 4), 0.01);
        assertEquals(0, result.getVoxel(1, 4, 4), 0.01);
        
        // check region with label 6
        assertEquals(6, result.getVoxel(7, 4, 4), 0.01);
        assertEquals(6, result.getVoxel(6, 4, 4), 0.01);
        assertEquals(6, result.getVoxel(9, 4, 4), 0.01);
        assertEquals(0, result.getVoxel(10, 4, 4), 0.01);
    }

}
