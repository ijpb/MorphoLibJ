/**
 * 
 */
package inra.ijpb.binary.conncomp;

import ij.ImageStack;
import inra.ijpb.algo.Algo;

/**
 * Computes labels corresponding to connected components in input 3D image.
 *
 * @see ConnectedComponentsLabeling
 * 
 * @author dlegland
 */
public interface ConnectedComponentsLabeling3D extends Algo
{
	/**
	 * Computes labels corresponding to connected components in input image.
	 * 
	 * @param binaryImage
	 *            binary image containing components
	 * @return the label image of the components
	 */
	public ImageStack computeLabels(ImageStack binaryImage);
}
