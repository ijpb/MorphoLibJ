/**
 * 
 */
package inra.ijpb.binary.distmap;

import ij.ImagePlus;
import ij.ImageStack;

/**
 * Interface for computing distance maps from binary 3D images.
 */
public interface ChamferDistance3D {

	/**
	 * Computes the distance map from a binary image, initialized with a new
	 * name. Distance is computed for each foreground (white) pixel, as the 
	 * chamfer distance to the nearest background (black) pixel.
	 * 
	 * @deprecated replaced by inra.ijpb.binary.BinaryImages.distanceMap(ImagePlus)
	 */
	@Deprecated
	public ImagePlus distanceMap(ImagePlus image, String newName);
	
	/**
	 * Computes the distance map from a binary image processor. 
	 * Distance is computed for each foreground (white) pixel, as the 
	 * chamfer distance to the nearest background (black) pixel.
	 */
	public ImageStack distanceMap(ImageStack image);
}
