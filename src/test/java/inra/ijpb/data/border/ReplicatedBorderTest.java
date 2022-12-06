/**
 * 
 */
package inra.ijpb.data.border;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * @author dlegland
 *
 */
public class ReplicatedBorderTest
{
    
    /**
     * Test method for {@link inra.ijpb.data.border.ReplicatedBorder#get(int, int)}.
     */
    @Test
    public final void testAddBorders()
    {
        ImageProcessor image = createTestImage();
        
        BorderManager border = new ReplicatedBorder(image);
        
        ImageProcessor res = border.addBorders(image, 1, 2, 3, 4);
        
        assertEquals(8+3, res.getWidth());
        assertEquals(6+7, res.getHeight());
        
        assertEquals( 0, border.get( 0,  0));
        assertEquals( 7, border.get(10,  0));
        assertEquals(50, border.get( 0, 12));
        assertEquals(57, border.get(10, 12));
    }
    
    /**
     * Test method for {@link inra.ijpb.data.border.ReplicatedBorder#get(int, int)}.
     */
    @Test
    public final void testGet()
    {
        ImageProcessor image = createTestImage();
        
        BorderManager border = new ReplicatedBorder(image);
        
        assertEquals( 0, border.get(-1, -1));
        assertEquals( 7, border.get( 8, -1));
        assertEquals(50, border.get(-1,  6));
        assertEquals(57, border.get( 8,  6));
    }
    
    private ImageProcessor createTestImage()
    {
        ImageProcessor image = new ByteProcessor(8, 6);
        for (int y = 0; y < 6; y++)
        {
            for (int x = 0; x < 8; x++)
            {
                image.set(x, y, y * 10 + x);
            }
        }
        return image;
    }
    
}
