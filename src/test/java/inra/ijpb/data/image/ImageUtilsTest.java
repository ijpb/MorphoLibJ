/**
 * 
 */
package inra.ijpb.data.image;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * @author dlegland
 *
 */
public class ImageUtilsTest
{
    /**
     * Test method for {@link inra.ijpb.data.image.ImageUtils#fillRect(ij.process.ImageProcessor, int, int, int, int, double)}.
     */
    @Test
    public final void testFillRect()
    {
        // create empty image
        ImageProcessor image = new ByteProcessor(15, 10);
        
        // fill two rectangular regions
        ImageUtils.fillRect(image, 1, 1, 3, 6, 20);
        ImageUtils.fillRect(image, 5, 5, 8, 2, 50);
        
        // corners of first rectangle
        assertEquals(20, image.get(1, 1));
        assertEquals(20, image.get(3, 1));
        assertEquals(20, image.get(1, 6));
        assertEquals(20, image.get(3, 6));
        // outside first rectangle
        assertEquals(0, image.get(4, 1));
        assertEquals(0, image.get(1, 7));
        
        // corners of second rectangle
        assertEquals(50, image.get(5, 5));
        assertEquals(50, image.get(12, 5));
        assertEquals(50, image.get(5, 6));
        assertEquals(50, image.get(12, 6));
        // outside second rectangle
        assertEquals(0, image.get(13, 5));
        assertEquals(0, image.get(5, 7));
    }

    /**
     * Test method for {@link inra.ijpb.data.image.ImageUtils#fillRect3d(ij.ImageStack, int, int, int, int, int, int, double)}.
     */
    @Test
    public final void testFillRect3d()
    {
        // create empty image
        ImageStack image = ImageStack.create(15, 10, 5, 8);
        
        // fill two rectangular regions
        ImageUtils.fillRect3d(image, 1, 1, 2, 3, 6, 2, 20);
        ImageUtils.fillRect3d(image, 5, 5, 2, 8, 2, 2, 50);

        // corners of first rectangle
        assertEquals(20, image.getVoxel(1, 1, 2), 0.01);
        assertEquals(20, image.getVoxel(3, 1, 2), 0.01);
        assertEquals(20, image.getVoxel(1, 6, 2), 0.01);
        assertEquals(20, image.getVoxel(3, 6, 2), 0.01);
        assertEquals(20, image.getVoxel(1, 1, 3), 0.01);
        assertEquals(20, image.getVoxel(3, 1, 3), 0.01);
        assertEquals(20, image.getVoxel(1, 6, 3), 0.01);
        assertEquals(20, image.getVoxel(3, 6, 3), 0.01);
        // outside first rectangle
        assertEquals(0, image.getVoxel(4, 1, 2), 0.01);
        assertEquals(0, image.getVoxel(1, 7, 2), 0.01);
        
        // corners of second rectangle
        assertEquals(50, image.getVoxel( 5, 5, 2), 0.01);
        assertEquals(50, image.getVoxel(12, 5, 2), 0.01);
        assertEquals(50, image.getVoxel( 5, 6, 2), 0.01);
        assertEquals(50, image.getVoxel(12, 6, 2), 0.01);
        // outside second rectangle
        assertEquals(0, image.getVoxel(13, 5, 2), 0.01);
        assertEquals(0, image.getVoxel( 5, 7, 2), 0.01);
    }

}
