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
package inra.ijpb.measure.region3d;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.ResultsTable;
import inra.ijpb.measure.region3d.MorphometricFeatures3D.Feature;

/**
 * 
 */
public class MorphometricFeatures3DTest
{

    /**
     * Test method for {@link inra.ijpb.measure.region2d.MorphometricFeatures2D#containsAny(inra.ijpb.measure.region2d.MorphometricFeatures2D.Feature[])}.
     */
    @Test
    public final void testContainsAny()
    {
        // creates a new instance to select the features to compute.
        MorphometricFeatures3D morpho = new MorphometricFeatures3D()
                .add(Feature.VOLUME)
                .add(Feature.SURFACE_AREA)
                .add(Feature.CENTROID);
        
        assertTrue(morpho.containsAny(Feature.EQUIVALENT_ELLIPSOID, Feature.EULER_NUMBER, Feature.VOLUME));
        assertFalse(morpho.containsAny(Feature.EQUIVALENT_ELLIPSOID, Feature.EULER_NUMBER, Feature.SPHERICITY));
    }

    /**
     * Test method for {@link inra.ijpb.measure.region2d.MorphometricFeatures2D#computeTable(ij.ImagePlus)}.
     */
    @Test
    public final void testComputeTable()
    {
        // create a small test image with five regions 
        ImageStack stack = ImageStack.create(8, 8, 8, 8);
        stack.setVoxel(1, 1, 1, 1);
        for (int i = 3; i < 7; i++)
        {
            stack.setVoxel(i, 1, 1, 3);
            stack.setVoxel(1, i, 1, 5);
            stack.setVoxel(1, 1, i, 7);
        }
        for (int i = 3; i < 7; i++)
        {
            for (int j = 3; j < 7; j++)
            {
                for (int k = 3; k < 7; k++)
                {
                    stack.setVoxel(i, j, k, 9);
                }
            }
        }
        ImagePlus imagePlus = new ImagePlus("image", stack);
        
        // creates a new instance to select the features to compute.
        MorphometricFeatures3D morpho = new MorphometricFeatures3D()
                .add(Feature.VOLUME)
                .add(Feature.SURFACE_AREA)
                .add(Feature.CENTROID);
        ResultsTable table = morpho.computeTable(imagePlus);
        
        // expect 5 labels, and many columns
        assertEquals(5, table.getCounter());
    }

    /**
     * Test method for {@link inra.ijpb.measure.region3d.MorphometricFeatures3D#computeTable(ij.ImagePlus)}.
     */
    @Test
    public final void testComputeTable_Centroid()
    {
        ImagePlus image = createEightRegionsImage(); 
        MorphometricFeatures3D morpho = new MorphometricFeatures3D()
                .add(Feature.CENTROID);
        
        ResultsTable table = morpho.computeTable(image);
        
        assertEquals(8, table.getCounter());
        assertEquals(2, table.getLastColumn());
    }
    
    /**
     * Test method for {@link inra.ijpb.measure.region3d.MorphometricFeatures3D#computeTable(ij.ImagePlus)}.
     */
    @Test
    public final void testComputeTable_Sphericity()
    {
        ImagePlus image = createEightRegionsImage(); 
        MorphometricFeatures3D morpho = new MorphometricFeatures3D()
                .add(Feature.SPHERICITY);
        
        ResultsTable table = morpho.computeTable(image);
        
        assertEquals(8, table.getCounter());
        assertEquals(0, table.getLastColumn());
    }
    
    /**
     * Test method for {@link inra.ijpb.measure.region3d.MorphometricFeatures3D#computeTable(ij.ImagePlus)}.
     */
    @Test
    public final void testComputeTable_EllipsoidElongations()
    {
        ImagePlus image = createEightRegionsImage(); 
        MorphometricFeatures3D morpho = new MorphometricFeatures3D()
                .add(Feature.ELLIPSOID_ELONGATIONS);
        
        ResultsTable table = morpho.computeTable(image);
        
        assertEquals(8, table.getCounter());
        assertEquals(2, table.getLastColumn());
    }
    
    private ImagePlus createEightRegionsImage()
    {
        ImageStack array = ImageStack.create(9, 9, 9, 8);
        
        // create a singlee-voxel region
        array.setVoxel(1, 1, 1, 1);
        
        // create three 1-by-5 voxels line regions
        for (int i = 0; i < 5; i++)
        {
            array.setVoxel(i+2, 1, 1, 3);
            array.setVoxel(1, i+2, 1, 5);
            array.setVoxel(1, 1, i+2, 11);
        }
        
        // create three 5-by-5 voxels plane regions
        for (int i = 0; i < 5; i++)
        {
            for (int j = 0; j < 5; j++)
            {
                array.setVoxel(i+2, j+2, 1, 7);
                array.setVoxel(i+2, 1, j+2, 13);
                array.setVoxel(1, i+2, j+2, 17);
            }
        }
        
        // create a single 5-by-5-by-5 voxels cubic regions
        for (int i = 0; i < 5; i++)
        {
            for (int j = 0; j < 5; j++)
            {
                for (int k = 0; k < 5; k++)
                {
                array.setVoxel(i+2, j+2, k+2, 19);
                }
            }
        }
        
        return new ImagePlus("labels", array);
    }
}
