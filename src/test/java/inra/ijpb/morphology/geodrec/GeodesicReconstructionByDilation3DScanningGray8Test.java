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
package inra.ijpb.morphology.geodrec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;

public class GeodesicReconstructionByDilation3DScanningGray8Test
{

    @Test
    public final void testCubicMeshC26()
    {
        ImageStack mask = createCubicMeshImage();

        ImageStack marker = ImageStack.create(20, 20, 20, 8);
        marker.setVoxel(5, 5, 5, 255);

        GeodesicReconstructionByDilation3DScanningGray8 algo = new GeodesicReconstructionByDilation3DScanningGray8();
        algo.setConnectivity(26);

        ImageStack result = algo.applyTo(marker, mask);

        assertEquals(255, result.getVoxel(5, 15, 5), .01);
    }

    @Test
    public final void testCubicMeshC6()
    {
        ImageStack mask = createCubicMeshImage();

        ImageStack marker = ImageStack.create(20, 20, 20, 8);
        marker.setVoxel(5, 5, 5, 255);

        GeodesicReconstructionByDilation3DScanningGray8 algo = new GeodesicReconstructionByDilation3DScanningGray8();
        algo.setConnectivity(6);

        ImageStack result = algo.applyTo(marker, mask);

        assertEquals(255, result.getVoxel(5, 15, 5), .01);
    }

    @Test
    public final void testCubicHollowMesh()
    {
        ImageStack mask = createCubicHollowMeshImage();

        ImageStack marker = ImageStack.create(20, 20, 20, 8);
        for (int z = 0; z < 20; z++)
        {
            for (int y = 0; y < 20; y++)
            {
                for (int x = 0; x < 20; x++)
                {
                    marker.setVoxel(x, y, z, 255);
                }
            }
        }
        marker.setVoxel(5, 5, 5, 0);

        GeodesicReconstructionByErosion3DScanningGray8 algo = new GeodesicReconstructionByErosion3DScanningGray8();

        ImageStack result = algo.applyTo(marker, mask);

        assertEquals(0, result.getVoxel(5, 15, 5), .01);
    }

    @Test
    public final void testCochleaVolumeC26()
    {
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

        GeodesicReconstructionByDilation3DScanningGray8 algo = new GeodesicReconstructionByDilation3DScanningGray8();
        algo.setConnectivity(26);
        algo.verbose = false;

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

    @Test
    public final void testCochleaVolumeC6()
    {
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

        GeodesicReconstructionByDilation3DScanningGray8 algo = new GeodesicReconstructionByDilation3DScanningGray8();
        algo.setConnectivity(6);
        algo.verbose = false;

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

    private ImageStack createCubicMeshImage()
    {
        int sizeX = 20;
        int sizeY = 20;
        int sizeZ = 20;
        int bitDepth = 8;

        // create empty stack
        ImageStack stack = ImageStack.create(sizeX, sizeY, sizeZ, bitDepth);

        // number of voxels between edges and 'tube' borders
        int gap = 2;

        // First, the edges in the x direction
        for (int z = 5 - gap - 1; z <= 5 + gap + 1; z++)
        {
            for (int y = 5 - gap - 1; y <= 5 + gap + 1; y++)
            {
                for (int x = 5 - gap - 1; x <= 15 + gap + 1; x++)
                {
                    stack.setVoxel(x, y, z, 255);
                    stack.setVoxel(x, y, z + 10, 255);
                }
            }
        }

        // then, the edges in the y direction
        for (int z = 5 - gap - 1; z <= 5 + gap + 1; z++)
        {
            for (int x = 5 - gap - 1; x <= 5 + gap + 1; x++)
            {
                for (int y = 5 - gap - 1; y <= 15 + gap + 1; y++)
                {
                    stack.setVoxel(x + 10, y, z, 255);
                    stack.setVoxel(x, y, z + 10, 255);
                    stack.setVoxel(x + 10, y, z + 10, 255);
                }
            }
        }

        // Finally, the edges in the z direction
        for (int y = 5 - gap - 1; y <= 5 + gap + 1; y++)
        {
            for (int x = 5 - gap - 1; x <= 5 + gap + 1; x++)
            {
                for (int z = 5 - gap - 1; z <= 15 + gap + 1; z++)
                {
                    stack.setVoxel(x, y + 10, z, 255);
                    stack.setVoxel(x + 10, y + 10, z, 255);
                }
            }
        }

        return stack;
    }

    private ImageStack createCubicHollowMeshImage()
    {
        // create filled cubic mesh
        ImageStack stack = createCubicMeshImage();

        // number of voxels between edges and 'tube' borders
        int gap = 2;

        // First, the edges in the x direction
        for (int z = 5 - gap; z <= 5 + gap; z++)
        {
            for (int y = 5 - gap; y <= 5 + gap; y++)
            {
                for (int x = 5 - gap; x <= 15 + gap; x++)
                {
                    stack.setVoxel(x, y, z, 0);
                    stack.setVoxel(x, y, z + 10, 0);
                }
            }
        }

        // then, the edges in the y direction
        for (int z = 5 - gap; z <= 5 + gap; z++)
        {
            for (int x = 5 - gap; x <= 5 + gap; x++)
            {
                for (int y = 5 - gap; y <= 15 + gap; y++)
                {
                    stack.setVoxel(x + 10, y, z, 0);
                    stack.setVoxel(x, y, z + 10, 0);
                    stack.setVoxel(x + 10, y, z + 10, 0);
                }
            }
        }

        // Finally, the edges in the z direction
        for (int y = 5 - gap; y <= 5 + gap; y++)
        {
            for (int x = 5 - gap; x <= 5 + gap; x++)
            {
                for (int z = 5 - gap; z <= 15 + gap; z++)
                {
                    stack.setVoxel(x, y + 10, z, 0);
                    stack.setVoxel(x + 10, y + 10, z, 0);
                }
            }
        }

        return stack;
    }
}
