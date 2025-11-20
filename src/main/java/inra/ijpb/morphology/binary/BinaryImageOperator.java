/**
 * 
 */
package inra.ijpb.morphology.binary;

import ij.process.ByteProcessor;

/**
 * Simple definition of an operator that transforms a binary image into another
 * binary image.
 * 
 * @see BinaryImageOperator3D
 * 
 * @author dlegland
 */
public interface BinaryImageOperator
{
	/**
	 * Applies the operator to a binary image, and returns the result into a new
	 * binary image.
	 * 
	 * @param image
	 *            the (binary) image to process
	 * @return the result of the processing as a binary image.
	 */
	public ByteProcessor processBinary(ByteProcessor image);
}
