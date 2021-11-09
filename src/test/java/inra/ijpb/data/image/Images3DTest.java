/**
 * 
 */
package inra.ijpb.data.image;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.ImageStack;

/**
 * @author dlegland
 *
 */
public class Images3DTest
{

	/**
	 * Test method for {@link inra.ijpb.data.image.Images3D#fill(ij.ImageStack, double)}.
	 */
	@Test
	public final void testFill()
	{
		ImageStack image = ImageStack.create(10, 10, 10, 8);
		Images3D.fill(image, 120.0);
		
		assertEquals(120, (int) image.getVoxel(0, 0, 0));
		assertEquals(120, (int) image.getVoxel(5, 5, 5));
		assertEquals(120, (int) image.getVoxel(9, 9, 9));
	}
	
	/**
	 * Test method for {@link inra.ijpb.data.image.Images3D#invert(ij.ImageStack)}.
	 */
	@Test
	public final void testInvert()
	{
		ImageStack image = ImageStack.create(10, 10, 10, 8);
		image.setVoxel(5, 5, 5, 100);
		image.setVoxel(6, 6, 6, 255);
		assertEquals(0, (int) image.getVoxel(0, 0, 0));
		
		Images3D.invert(image);
		
		assertEquals(255, (int) image.getVoxel(0, 0, 0));
		assertEquals(255, (int) image.getVoxel(9, 9, 9));
		assertEquals(155, (int) image.getVoxel(5, 5, 5));
		assertEquals(  0, (int) image.getVoxel(6, 6, 6));
	}

}
