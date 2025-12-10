/**
 * 
 */
package inra.ijpb.label.distmap;

import ij.ImageStack;
import inra.ijpb.data.image.ImageUtils;

/**
 * A collection of static method for creating test images shared by different
 * test cases.
 */
public class TestImages
{
	/**
	 * Creates a test image composed of eight cuboids with various sizes.
	 * 
	 * @return a test image composed of eight cuboids with various sizes.
	 */
	public static final ImageStack createStack_eightCuboids()
	{
		ImageStack stack = ImageStack.create(15, 15, 15, 8);
		ImageUtils.fillRect3d(stack, 1, 1, 1, 3, 3, 3, 3);
		ImageUtils.fillRect3d(stack, 5, 1, 1, 9, 3, 3, 4);
		ImageUtils.fillRect3d(stack, 1, 5, 1, 3, 9, 3, 7);
		ImageUtils.fillRect3d(stack, 5, 5, 1, 9, 9, 3, 8);
		ImageUtils.fillRect3d(stack, 1, 1, 5, 3, 3, 9, 11);
		ImageUtils.fillRect3d(stack, 5, 1, 5, 9, 3, 9, 12);
		ImageUtils.fillRect3d(stack, 1, 5, 5, 3, 9, 9, 15);
		ImageUtils.fillRect3d(stack, 5, 5, 5, 9, 9, 9, 17);

		return stack;
	}

	/**
	 * Creates a test image composed of eight adjacent cubes that also touch the
	 * image borders.
	 * 
	 * @return a test image composed of eight adjacent cubes.
	 */
	public static final ImageStack createStack_eightAdjacentCubes()
	{
		ImageStack stack = ImageStack.create(10, 10, 10, 8);
		ImageUtils.fillRect3d(stack, 0, 0, 0, 5, 5, 5, 3);
		ImageUtils.fillRect3d(stack, 5, 0, 0, 5, 5, 5, 4);
		ImageUtils.fillRect3d(stack, 0, 5, 0, 5, 5, 5, 7);
		ImageUtils.fillRect3d(stack, 5, 5, 0, 5, 5, 5, 8);
		ImageUtils.fillRect3d(stack, 0, 0, 5, 5, 5, 5, 11);
		ImageUtils.fillRect3d(stack, 5, 0, 5, 5, 5, 5, 12);
		ImageUtils.fillRect3d(stack, 0, 5, 5, 5, 5, 5, 15);
		ImageUtils.fillRect3d(stack, 5, 5, 5, 5, 5, 5, 17);

		return stack;
	}
	
	/**
	 * private constructor to prevent instantiation.
	 */
	private TestImages()
	{
	}
}
