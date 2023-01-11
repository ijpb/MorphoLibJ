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
package inra.ijpb.morphology.geodrec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;

public class GeodesicReconstructionByDilation3DGray8Test
{
    @Test
    public final void testApplyTo()
    {
        GeodesicReconstructionByDilation3DGray8 algo = new GeodesicReconstructionByDilation3DGray8();

        String fileName = getClass().getResource("/files/bat-cochlea_sub25.tif").getFile();
        ImagePlus imagePlus = IJ.openImage(fileName);
        assertNotNull(imagePlus);

        assertTrue(imagePlus.getStackSize() > 0);

        ImageStack mask = imagePlus.getStack();
        int width = mask.getWidth();
        int height = mask.getHeight();
        int depth = mask.getSize();
        int bitDepth = mask.getBitDepth();
        ImageStack marker = ImageStack.create(width, height, depth, bitDepth);

        marker.setVoxel(5, 21, 12, 255);

        ImageStack result = algo.applyTo(marker, mask);

        for (int z = 0; z < depth; z++)
        {
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
                    assertEquals(result.getVoxel(x, y, z), mask.getVoxel(x, y, z), .01);
                }
            }
        }

    }

}
