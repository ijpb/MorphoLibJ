/**
 * 
 */
package inra.ijpb.binary.distmap;

import ij.ImagePlus;
import ij.process.ImageProcessor;

/**
 * Interface for computing distance maps from binary images.
 */
public interface ChamferDistance {

	/**
	 * Computes the distance map from a binary image, initialized with a new
	 * name. Distance is computed for each foreground (white) pixel, as the 
	 * chamfer distance to the nearest background (black) pixel.
	 */
	public ImagePlus distanceMap(ImagePlus image, String newName);
	
	/**
	 * Computes the distance map from a binary image processor. 
	 * Distance is computed for each foreground (white) pixel, as the 
	 * chamfer distance to the nearest background (black) pixel.
	 */
	public ImageProcessor distanceMap(ImageProcessor image);
}
