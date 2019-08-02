/**
 * 
 */
package inra.ijpb.morphology.strel;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.ImageStack;

/**
 * @author dlegland
 *
 */
public class LinearDepthStrel3DTest {

	/**
	 * Test method for {@link inra.ijpb.morphology.strel.LinearDepthStrel3D#inPlaceDilation(ij.ImageStack)}.
	 */
	@Test
	public void testDilation() 
	{
		ImageStack image = createIsolatedVoxelImage();
		
		LinearDepthStrel3D strel = LinearDepthStrel3D.fromDiameter(7);
		ImageStack result = strel.dilation(image);
		
		assertEquals(255, result.getVoxel(5, 5, 5), .01);
		assertEquals(255, result.getVoxel(5, 5, 2), .01);
		assertEquals(255, result.getVoxel(5, 5, 8), .01);
		assertEquals(  0, result.getVoxel(5, 5, 1), .01);
		assertEquals(  0, result.getVoxel(5, 5, 9), .01);
	}

	private static final ImageStack createIsolatedVoxelImage()
	{
		ImageStack image = ImageStack.create(10, 10, 10, 8);
		image.setVoxel(5, 5, 5, 255);
		return image;
	}
}
