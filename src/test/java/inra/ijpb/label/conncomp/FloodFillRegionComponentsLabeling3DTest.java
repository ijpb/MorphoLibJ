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

import ij.ImageStack;
import inra.ijpb.data.image.ImageUtils;

/**
 * @author dlegland
 *
 */
public class FloodFillRegionComponentsLabeling3DTest
{
    /**
     * Test method for {@link inra.ijpb.label.conncomp.FloodFillRegionComponentsLabeling3D#computeLabels(ij.ImageStack, int)}.
     */
    @Test
    public final void testComputeLabels_Default()
    {
        ImageStack image = createNineCubesImage();
        
        FloodFillRegionComponentsLabeling3D algo = new FloodFillRegionComponentsLabeling3D();
        ImageStack result = algo.computeLabels(image, 3);
        
        assertEquals(16, result.getBitDepth());
        assertEquals(0, result.getVoxel(3, 3, 3), .1);
        assertEquals(1, result.getVoxel(7, 3, 3), .1);
        assertEquals(0, result.getVoxel(3, 7, 3), .1);
        assertEquals(0, result.getVoxel(3, 3, 7), .1);
    }
    
    /**
     * Test method for {@link inra.ijpb.label.conncomp.FloodFillRegionComponentsLabeling3D#computeLabels(ij.ImageStack, int)}.
     */
    @Test
    public final void testComputeLabels_Background_HollowCube()
    {
        ImageStack image = ImageStack.create(10, 10, 10, 8);
        ImageUtils.fillRect3d(image, 2, 2, 2, 6, 6, 6, 10);
        ImageUtils.fillRect3d(image, 4, 4, 4, 2, 2, 2, 0);
        
        FloodFillRegionComponentsLabeling3D algo = new FloodFillRegionComponentsLabeling3D();
        ImageStack result = algo.computeLabels(image, 0);
        
        assertEquals(16, result.getBitDepth());
        // value within initial region should be set to background
        assertEquals(0, result.getVoxel(2, 2, 2), .1);
        // first region corresponding the outside of the initial region
        assertEquals(1, result.getVoxel(1, 1, 1), .1);
        // second region corresponding the inside of the initial region
        assertEquals(2, result.getVoxel(5, 5, 5), .1);
    }
    
    /**
     * Create a 10-by-10-by-10 byte stack containing nine cubes touching by
     * corners.
     * 
     * Expected number of connected components is nine for 6 (and 18)
     * connectivity, and one for 26 connectivity.
     * 
     * @return an image containing nine cubes touching by corners
     */
    private final static ImageStack createNineCubesImage()
    {
        ImageStack image = ImageStack.create(10, 10, 10, 8);
        for (int z = 0; z < 2; z++)
        {
            for (int y = 0; y < 2; y++)
            {
                for (int x = 0; x < 2; x++)
                {
                    image.setVoxel(x + 2, y + 2, z + 2, 1);
                    image.setVoxel(x + 2, y + 6, z + 2, 2);
                    image.setVoxel(x + 6, y + 2, z + 2, 3);
                    image.setVoxel(x + 6, y + 6, z + 2, 4);
                    image.setVoxel(x + 4, y + 4, z + 4, 5);
                    image.setVoxel(x + 2, y + 2, z + 6, 6);
                    image.setVoxel(x + 2, y + 6, z + 6, 7);
                    image.setVoxel(x + 6, y + 2, z + 6, 8);
                    image.setVoxel(x + 6, y + 6, z + 6, 9);
                }
            }
        }
        return image;
    }
}
