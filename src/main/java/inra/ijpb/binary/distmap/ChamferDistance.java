/**
 * 
 */
package inra.ijpb.binary.distmap;

import ij.process.ImageProcessor;

/**
 * Interface for computing distance maps from binary images.
 */
public interface ChamferDistance {
	/**
	 * Computes the distance map from a binary image processor. 
	 * Distance is computed for each foreground (white) pixel, as the 
	 * chamfer distance to the nearest background (black) pixel.
	 */
	public ImageProcessor distanceMap(ImageProcessor image);
}
