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
/**
 * 
 */
package inra.ijpb.plugins;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;

/**
 * @author dlegland
 *
 */
public class AnalyzeRegionsTest
{
    /**
     * Test method for {@link inra.ijpb.plugins.AnalyzeRegions#process(ij.ImagePlus, inra.ijpb.plugins.AnalyzeRegions.Features)}.
     */
    @Test
    public final void testProcess_labeledBlobs()
    {
        // open test image
        ImagePlus imagePlus = IJ.openImage(AnalyzeRegionsTest.class.getResource("/files/blobs-lbl.tif").getFile());
        
        // creates a new Features instance to select the features to compute.  
        AnalyzeRegions.Features features = new AnalyzeRegions.Features();
        features.setAll(false);
        features.area = true;
        features.perimeter = true;
        features.centroid = true;
        
        // compute the features, and returns the corresponding table
        ResultsTable table = AnalyzeRegions.process(imagePlus, features);
        
        // expect 64 labels, and four columns (two for the centroid)
        assertEquals(64, table.getCounter());
        assertEquals(3, table.getLastColumn());
    }
    
    /**
     * Test method for {@link inra.ijpb.plugins.AnalyzeRegions#process(ij.ImagePlus, inra.ijpb.plugins.AnalyzeRegions.Features)}.
     */
    @Test
    public final void testProcess_labeledBlobs_allFeatures()
    {
        // open test image
        ImagePlus imagePlus = IJ.openImage(AnalyzeRegionsTest.class.getResource("/files/blobs-lbl.tif").getFile());
        
        // creates a new Features instance to select the features to compute.  
        AnalyzeRegions.Features features = new AnalyzeRegions.Features();
        features.setAll(true);
        
        // compute the features, and returns the corresponding table
        ResultsTable table = AnalyzeRegions.process(imagePlus, features);
        
        // expect 64 labels, and many columns
        assertEquals(64, table.getCounter());
    }
    
}
