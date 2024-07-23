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
package inra.ijpb.measure.region2d;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import inra.ijpb.measure.region2d.MorphometricFeatures2D.Feature;

/**
 * 
 */
public class MorphometricFeatures2DTest
{

    /**
     * Test method for {@link inra.ijpb.measure.region2d.MorphometricFeatures2D#containsAny(inra.ijpb.measure.region2d.MorphometricFeatures2D.Feature[])}.
     */
    @Test
    public final void testContainsAny()
    {
        // creates a new instance to select the features to compute.
        MorphometricFeatures2D morpho = new MorphometricFeatures2D()
                .add(Feature.AREA)
                .add(Feature.PERIMETER)
                .add(Feature.CENTROID);
        
        assertTrue(morpho.containsAny(Feature.EQUIVALENT_ELLIPSE, Feature.EULER_NUMBER, Feature.AREA));
        assertFalse(morpho.containsAny(Feature.EQUIVALENT_ELLIPSE, Feature.EULER_NUMBER, Feature.CONVEXITY));
    }

    /**
     * Test method for {@link inra.ijpb.measure.region2d.MorphometricFeatures2D#computeTable(ij.ImagePlus)}.
     */
    @Test
    public final void testComputeTable()
    {
        // open test image
        ImagePlus imagePlus = IJ.openImage(MorphometricFeatures2DTest.class.getResource("/files/blobs-lbl.tif").getFile());
        
        // creates a new instance to select the features to compute.
        MorphometricFeatures2D morpho = new MorphometricFeatures2D()
                .add(Feature.AREA)
                .add(Feature.PERIMETER)
                .add(Feature.CENTROID);
        ResultsTable table = morpho.computeTable(imagePlus);
        
        // expect 64 labels, and many columns
        assertEquals(64, table.getCounter());
    }

}
