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
