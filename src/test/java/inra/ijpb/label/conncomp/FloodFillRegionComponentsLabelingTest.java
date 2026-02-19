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
package inra.ijpb.label.conncomp;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.data.image.ImageUtils;

/**
 * @author dlegland
 *
 */
public class FloodFillRegionComponentsLabelingTest
{
    /**
     * Test method for {@link inra.ijpb.label.conncomp.FloodFillRegionComponentsLabeling#computeLabels(ij.process.ImageProcessor, int)}.
     */
    @Test
    public final void testComputeLabels_Default()
    {
        ByteProcessor image = createFiveSquaresImage();
        
        FloodFillRegionComponentsLabeling algo = new FloodFillRegionComponentsLabeling();
        ImageProcessor result = algo.computeLabels(image, 3);
        
        assertEquals(16, result.getBitDepth());
        assertEquals(1, result.get(2, 2));
        assertEquals(0, result.get(6, 6));
    }
    
    /**
     * Test method for {@link inra.ijpb.label.conncomp.FloodFillRegionComponentsLabeling#computeLabels(ij.process.ImageProcessor, int)}.
     */
    @Test
    public final void testComputeLabels_BackgroundRing()
    {
        ByteProcessor image = new ByteProcessor(10, 10);
        ImageUtils.fillRect(image, 2, 2, 6, 6, 10);
        ImageUtils.fillRect(image, 4, 4, 2, 2, 0);
        
        FloodFillRegionComponentsLabeling algo = new FloodFillRegionComponentsLabeling();
        ImageProcessor result = algo.computeLabels(image, 0);
        
        assertEquals(16, result.getBitDepth());
        // value within initial region should be set to background
        assertEquals(0, result.get(2, 2));
        assertEquals(0, result.get(6, 6));
        // first region corresponding the outside of the initial region
        assertEquals(1, result.get(1, 1));
        // second region corresponding the inside of the initial region
        assertEquals(2, result.get(5, 5));
    }
    
    /**
     * Create a 10-by-10 byte image containing five 2-by-2 squares touching by corners.
     * The squares are associated to labels 3, 5, 6, 8, 9
     * 
     * Expected number of connected components is five for 4 connectivity, and
     * one for 8 connectivity.
     * 
     * @return an image containing five squares touching by corners
     */
    private final static ByteProcessor createFiveSquaresImage()
    {
        ByteProcessor image = new ByteProcessor(10, 10);
        for (int y = 0; y < 2; y++)
        {
            for (int x = 0; x < 2; x++)
            {
                image.set(x + 2, y + 2, 3);
                image.set(x + 6, y + 2, 5);
                image.set(x + 4, y + 4, 6);
                image.set(x + 2, y + 6, 8);
                image.set(x + 6, y + 6, 9);
            }
        }
        return image;
    }
}
