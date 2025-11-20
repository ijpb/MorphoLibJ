/**
 * 
 */
package inra.ijpb.morphology.binary;

import ij.ImageStack;

/**
 * Simple definition of an operator that transforms a 3D binary image into
 * another 3D binary image.
 * 
 * @see BinaryImageOperator
 * 
 * @author dlegland
 */
public interface BinaryImageOperator3D
{
	/**
	 * Applies the operator to a 3D binary image, and returns the result into a new
	 * 3D binary image.
	 * 
	 * @param image
	 *            the 3D (binary) image to process
	 * @return the result of the processing as a 3D binary image.
	 */
	public ImageStack processBinary(ImageStack image);
}
