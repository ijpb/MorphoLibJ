/**
 * 
 */
package inra.ijpb.binary.distmap;

import ij.process.ImageProcessor;
import inra.ijpb.algo.Algo;

/**
 * Interface for computing distance maps from binary images.
 * 
 * <p>
 * Example of use:
 * <pre><code>
 *	short[] shortWeights = ChamferWeights.CHESSKNIGHT.getShortWeights();
 *	boolean normalize = true;
 *	DistanceTransform dt = new DistanceTransform5x5Short(shortWeights, normalize);
 *	ImageProcessor result = dt.distanceMap(inputImage);
 *	// or:
 *	ImagePlus resultPlus = BinaryImages.distanceMap(imagePlus, shortWeights, normalize);
 * </code></pre>
 * 
 * @see inra.ijpb.binary.BinaryImages#distanceMap(ImageProcessor, short[], boolean)
 * @see inra.ijpb.binary.BinaryImages#distanceMap(ImageProcessor, float[], boolean)
 */
public interface DistanceTransform extends Algo {
	/**
	 * Computes the distance map from a binary image processor. 
	 * Distance is computed for each foreground (white) pixel, as the 
	 * chamfer distance to the nearest background (black) pixel.
	 */
	public ImageProcessor distanceMap(ImageProcessor image);
}
