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
