/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2017 INRA.
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

public class GeodesicReconstruction3DHybrid1Image3DTest
{
    @Test
    public final void testDilationCubicMeshC6()
    {
        ImageStack mask = createCubicMeshImage();

        ImageStack marker = ImageStack.create(20, 20, 20, 8);
        marker.setVoxel(5, 5, 5, 255);

        GeodesicReconstruction3DHybrid1Image3D algo = new GeodesicReconstruction3DHybrid1Image3D();
        algo.setConnectivity(6);

        ImageStack result = algo.applyTo(marker, mask);

        assertEquals(255, result.getVoxel(5, 15, 5), .01);
    }

    @Test
    public final void testErosionCubicMeshC6()
    {
        ImageStack mask = createCubicMeshImage();
        invertGray8Stack(mask);

        ImageStack marker = ImageStack.create(20, 20, 20, 8);
        marker.setVoxel(5, 5, 5, 255);
        invertGray8Stack(marker);

        GeodesicReconstruction3DHybrid1Image3D algo = new GeodesicReconstruction3DHybrid1Image3D();
        algo.setReconstructionType(GeodesicReconstructionType.BY_EROSION);
        algo.setConnectivity(6);

        ImageStack result = algo.applyTo(marker, mask);

        assertEquals(0, result.getVoxel(5, 15, 5), .01);
        assertEquals(255, result.getVoxel(0, 0, 0), .01);

        int sizeX = mask.getWidth();
        int sizeY = mask.getHeight();
        int sizeZ = mask.getSize();

        for (int z = 0; z < sizeZ; z++)
        {
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    assertEquals(result.getVoxel(x, y, z), mask.getVoxel(x, y, z), .01);
                }
            }
        }

    }

    @Test
    public final void testDilationCubicMeshC26()
    {
        ImageStack mask = createCubicMeshImage();

        ImageStack marker = ImageStack.create(20, 20, 20, 8);
        marker.setVoxel(5, 5, 5, 255);

        GeodesicReconstruction3DHybrid1Image3D algo = new GeodesicReconstruction3DHybrid1Image3D();
        algo.setConnectivity(26);

        ImageStack result = algo.applyTo(marker, mask);

        assertEquals(255, result.getVoxel(5, 15, 5), .01);
    }

    @Test
    public final void testDilationThinCubicMeshC26()
    {
        ImageStack mask = createThinCubicMeshImage();

        ImageStack marker = ImageStack.create(20, 20, 20, 8);
        marker.setVoxel(5, 5, 5, 255);

        GeodesicReconstruction3DHybrid1Image3D algo = new GeodesicReconstruction3DHybrid1Image3D();
        algo.setConnectivity(26);

        ImageStack result = algo.applyTo(marker, mask);

        assertEquals(255, result.getVoxel(5, 15, 5), .01);
    }

    @Test
    public final void testDilationCubicHollowMesh()
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

        GeodesicReconstruction3DHybrid1Image3D algo = new GeodesicReconstruction3DHybrid1Image3D();

        ImageStack result = algo.applyTo(marker, mask);

        assertEquals(0, result.getVoxel(5, 15, 5), .01);
        assertStackEquals(mask, result);
    }

    @Test
    public final void testDilationCochleaVolumeC6()
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

        GeodesicReconstruction3DHybrid1Image3D algo = new GeodesicReconstruction3DHybrid1Image3D();
        algo.setConnectivity(6);

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
    public final void testDilationCochleaVolumeC26()
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

        GeodesicReconstruction3DHybrid1Image3D algo = new GeodesicReconstruction3DHybrid1Image3D();
        algo.setConnectivity(26);

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

    private ImageStack createThinCubicMeshImage()
    {
        int sizeX = 20;
        int sizeY = 20;
        int sizeZ = 20;
        int bitDepth = 8;

        // create empty stack
        ImageStack stack = ImageStack.create(sizeX, sizeY, sizeZ, bitDepth);

        // First, the edges in the x direction
        for (int z = 5; z <= 5; z++)
        {
            for (int y = 5; y <= 5; y++)
            {
                for (int x = 5; x <= 15; x++)
                {
                    stack.setVoxel(x, y, z, 255);
                    stack.setVoxel(x, y, z + 10, 255);
                }
            }
        }

        // then, the edges in the y direction
        for (int z = 5; z <= 5; z++)
        {
            for (int x = 5; x <= 5; x++)
            {
                for (int y = 5; y <= 15; y++)
                {
                    stack.setVoxel(x + 10, y, z, 255);
                    stack.setVoxel(x, y, z + 10, 255);
                    stack.setVoxel(x + 10, y, z + 10, 255);
                }
            }
        }

        // Finally, the edges in the z direction
        for (int y = 5; y <= 5; y++)
        {
            for (int x = 5; x <= 5; x++)
            {
                for (int z = 5; z <= 15; z++)
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

    private final void invertGray8Stack(ImageStack image)
    {
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        int sizeZ = image.getSize();

        for (int z = 0; z < sizeZ; z++)
        {
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    image.setVoxel(x, y, z, 255 - image.getVoxel(x, y, z));
                }
            }
        }
    }

    private final void assertStackEquals(ImageStack image, ImageStack image2)
    {
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        int sizeZ = image.getSize();

        assertEquals(sizeX, image2.getWidth());
        assertEquals(sizeY, image2.getHeight());
        assertEquals(sizeZ, image2.getSize());

        for (int z = 0; z < sizeZ; z++)
        {
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    assertEquals(image.getVoxel(x, y, z), image2.getVoxel(x, y, z), .01);
                }
            }
        }
    }
}
