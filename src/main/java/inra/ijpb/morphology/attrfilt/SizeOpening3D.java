/**
 * 
 */
package inra.ijpb.morphology.attrfilt;

import ij.ImageStack;
import inra.ijpb.algo.Algo;

/**
 * Size opening for 3D gray level images. Remove all cross sections whose size
 * is smaller than the specified number of voxels.
 *
 * 
 * @author dlegland
 *
 */
public interface SizeOpening3D extends Algo
{
	public ImageStack process(ImageStack image, int minVolume);
}
