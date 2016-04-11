/**
 * 
 */
package inra.ijpb.binary.conncomp;

import ij.process.ImageProcessor;
import inra.ijpb.algo.Algo;

/**
 * Computes labels corresponding to connected components in input image.
 *
 * @see ConnectedComponentsLabeling3D
 * 
 * @author dlegland
 *
 */
public interface ConnectedComponentsLabeling extends Algo
{
	/**
	 * Computes labels corresponding to connected components in input image.
	 * 
	 * @param binaryImage
	 *            binary image containing components
	 * @return the label image of the components
	 */
	public ImageProcessor computeLabels(ImageProcessor binaryImage);
}
