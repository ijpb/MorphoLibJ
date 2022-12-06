/**
 * 
 */
package inra.ijpb.data.border;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.ImageStack;

/**
 * @author dlegland
 *
 */
public class ReplicatedBorder3DTest
{
    
    /**
     * Test method for {@link inra.ijpb.data.border.ReplicatedBorder3D#get(int, int)}.
     */
    @Test
    public final void testAddBorders()
    {
        ImageStack image = createTestImage();
        
        BorderManager3D border = new ReplicatedBorder3D(image);
        
        ImageStack res = border.addBorders(image, 1, 2, 3, 4, 5, 6);
        
        assertEquals(8+3, res.getWidth());
        assertEquals(6+7, res.getHeight());
        assertEquals(4+11, res.getSize());
        
        assertEquals( 0, border.get( 0,  0,  0));
        assertEquals( 7, border.get(10,  0,  0));
        assertEquals(50, border.get( 0, 12,  0));
        assertEquals(57, border.get(10, 12,  0));
        assertEquals( 0, border.get( 0,  0, 14));
        assertEquals( 7, border.get(10,  0, 14));
        assertEquals(50, border.get( 0, 12, 14));
        assertEquals(57, border.get(10, 12, 14));
    }
    
    /**
     * Test method for {@link inra.ijpb.data.border.ReplicatedBorder3D#get(int, int)}.
     */
    @Test
    public final void testGet()
    {
        ImageStack image = createTestImage();
        
        BorderManager3D border = new ReplicatedBorder3D(image);
        
        assertEquals( 0, border.get(-1, -1, -1));
        assertEquals( 7, border.get( 8, -1, -1));
        assertEquals(50, border.get(-1,  6, -1));
        assertEquals(57, border.get( 8,  6, -1));
        assertEquals( 0, border.get(-1, -1,  4));
        assertEquals( 7, border.get( 8, -1,  4));
        assertEquals(50, border.get(-1,  6,  4));
        assertEquals(57, border.get( 8,  6,  4));
    }
    
    private ImageStack createTestImage()
    {
        ImageStack image = ImageStack.create(8, 6, 4, 8);
        for (int z = 0; z < 4; z++)
        {
            for (int y = 0; y < 6; y++)
            {
                for (int x = 0; x < 8; x++)
                {
                    image.setVoxel(x, y, z, y * 10 + x);
                }
            }
        }
        return image;
    }
    
}
