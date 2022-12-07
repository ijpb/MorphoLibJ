/**
 * 
 */
package inra.ijpb.label.conncomp;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * @author dlegland
 *
 */
public class LabelBoundariesLabeling2DTest
{

    /**
     * Test method for {@link inra.ijpb.label.conncomp.LabelBoundariesLabeling2D#process(ij.process.ImageProcessor)}.
     */
    @Test
    public final void testProcess_threeRegions()
    {
        ByteProcessor image = new ByteProcessor(10, 10);
        for (int j = 0; j < 3; j++)
        {
            for (int i = 0; i < 3; i++)
            {
                image.set(i+2, j+2, 3);
                image.set(i+5, j+2, 6);
            }
            for (int i = 0; i < 6; i++)
            {
                image.set(i+2, j+5, 8);
            }
        }
        
        LabelBoundariesLabeling2D algo = new LabelBoundariesLabeling2D();
        ImageProcessor res = algo.process(image).boundaryLabelMap;
        
        assertEquals(image.getWidth(), res.getWidth());
        assertEquals(image.getHeight(), res.getHeight());
        
        assertEquals(0, res.get(0,0));
        assertNotEquals(0, res.get(2,3));
        assertNotEquals(0, res.get(5,3));
        assertNotEquals(0, res.get(8,3));
        assertEquals(0, res.get(9,3));

        assertEquals(0, res.get(0,6));
        assertNotEquals(0, res.get(1,6));
        assertNotEquals(0, res.get(2,6));
        assertNotEquals(0, res.get(7,6));
        assertNotEquals(0, res.get(8,6));
        assertEquals(0, res.get(9,6));
    }

}
