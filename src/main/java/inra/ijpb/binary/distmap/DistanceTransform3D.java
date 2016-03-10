/**
 * 
 */
package inra.ijpb.binary.distmap;

import ij.ImageStack;
import inra.ijpb.algo.Algo;

/**
 * Interface for computing distance maps from binary 3D images.
 */
public interface DistanceTransform3D extends Algo
{
	/**
	 * Computes the distance map from a 3D binary image. 
	 * Distance is computed for each foreground (white) pixel, as the 
	 * chamfer distance to the nearest background (black) pixel.
	 * 
	 * @param image a 3D binary image with white pixels (255) as foreground
	 * @return a new 3D image containing: <ul>
	 * <li> 0 for each background pixel </li>
	 * <li> the distance to the nearest background pixel otherwise</li>
	 * </ul>
	 */
	public ImageStack distanceMap(ImageStack image);
}
