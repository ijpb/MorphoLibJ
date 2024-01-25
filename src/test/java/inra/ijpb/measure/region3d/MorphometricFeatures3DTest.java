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

}
