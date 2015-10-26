/**
 * 
 */
package inra.ijpb.binary.distmap;

import ij.ImageStack;

/**
 * Interface for computing distance maps from binary 3D images.
 */
public interface ChamferDistance3D 
{
	/**
	 * Computes the distance map from a binary image processor. 
	 * Distance is computed for each foreground (white) pixel, as the 
	 * chamfer distance to the nearest background (black) pixel.
	 */
	public ImageStack distanceMap(ImageStack image);
}
