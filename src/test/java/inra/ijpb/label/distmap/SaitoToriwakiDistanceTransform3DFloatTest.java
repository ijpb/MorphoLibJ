/**
 * 
 */
package inra.ijpb.label.distmap;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.ImageStack;
import inra.ijpb.data.image.Images3D;

/**
 * 
 */
public class SaitoToriwakiDistanceTransform3DFloatTest
{

	/**
	 * Test method for {@link inra.ijpb.label.distmap.SaitoToriwakiDistanceTransform3DFloat#distanceMap(ij.ImageStack)}.
	 */
	@Test
	public final void testDistanceMap_centeredRectangle()
	{
        // Create a black image with a white 8-by-6 rectangle in the middle
        ImageStack array = ImageStack.create(14, 12, 10, 8);
        for (int z = 2; z < 8; z++)
        {
            for (int y = 2; y < 10; y++)
            {
                for (int x = 2; x < 12; x++)
                {
                    array.setVoxel(x, y, z, 255);
                }
            }
        }

        DistanceTransform3D algo = new SaitoToriwakiDistanceTransform3DFloat();
        ImageStack result = algo.distanceMap(array);
        
        assertNotNull(result);
        assertEquals(array.getWidth(), result.getWidth());
        assertEquals(array.getHeight(), result.getHeight());
        assertEquals(array.getSize(), result.getSize());
        assertEquals(3, result.getVoxel(4, 4, 4), 0.001);
	}

	/**
	 * Test method for {@link inra.ijpb.label.distmap.SaitoToriwakiDistanceTransform3DFloat#distanceMap(ij.ImageStack)}.
	 */
	@Test
	public final void testDistanceMap_fromCorners()
	{
        // Create a white image, with only the corners missing
        ImageStack array = ImageStack.create(13, 11, 9, 8);
        Images3D.fill(array, 255);
        array.setVoxel(0, 0, 0, 0);
        array.setVoxel(12, 0, 0, 0);
        array.setVoxel(0, 10, 0, 0);
        array.setVoxel(12, 10, 0, 0);
        array.setVoxel(0, 0, 8, 0);
        array.setVoxel(12, 0, 8, 0);
        array.setVoxel(0, 10, 8, 0);
        array.setVoxel(12, 10, 8, 0);

        DistanceTransform3D algo = new SaitoToriwakiDistanceTransform3DFloat();
        ImageStack result = algo.distanceMap(array);
        
        assertNotNull(result);
        assertEquals(array.getWidth(), result.getWidth());
        assertEquals(array.getHeight(), result.getHeight());
        assertEquals(array.getSize(), result.getSize());
        
        double exp = Math.hypot(Math.hypot(6, 5), 4);
        assertEquals(exp, result.getVoxel(6, 5, 4), 0.001);
	}
	
	/**
	 * Test method for {@link inra.ijpb.label.distmap.SaitoToriwakiDistanceTransform3DFloat#distanceMap(ij.ImageStack)}.
	 */
	@Test
	public final void testDistanceMap_fromCenter()
	{
        // Create a white image, with only the corners missing
        ImageStack array = ImageStack.create(13, 11, 9, 8);
        Images3D.fill(array, 255);
        array.setVoxel(6, 5, 4, 0);

        DistanceTransform3D algo = new SaitoToriwakiDistanceTransform3DFloat();
        ImageStack result = algo.distanceMap(array);
        
        assertNotNull(result);
        assertEquals(array.getWidth(), result.getWidth());
        assertEquals(array.getHeight(), result.getHeight());
        assertEquals(array.getSize(), result.getSize());
        
        double exp = Math.hypot(Math.hypot(6, 5), 4);
        assertEquals(exp, result.getVoxel(0, 0, 0), 0.001);
        assertEquals(exp, result.getVoxel(12, 0, 0), 0.001);
        assertEquals(exp, result.getVoxel(0, 10, 0), 0.001);
        assertEquals(exp, result.getVoxel(12, 10, 0), 0.001);
        assertEquals(exp, result.getVoxel(0, 0, 8), 0.001);
        assertEquals(exp, result.getVoxel(12, 0, 8), 0.001);
        assertEquals(exp, result.getVoxel(0, 10, 8), 0.001);
        assertEquals(exp, result.getVoxel(12, 10, 8), 0.001);
	}
}
