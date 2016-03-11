/**
 * 
 */
package inra.ijpb.binary.geodesic;

import ij.process.ImageProcessor;
import inra.ijpb.algo.Algo;

/**
 * Interface for computing Geodesic distance transforms (or geodesic distance
 * maps) from binary images.
 *
 * @see inra.ijpb.binary.distmap.DistanceTransform
 * 
 * @author David Legland
 */
public interface GeodesicDistanceTransform extends Algo
{
	/**
	 * Computes the geodesic distance transform (or geodesic distance map) of a
	 * binary image of marker, constrained to a binary mask.
	 * 
	 * @param marker
	 *            the binary image of marker
	 * @param mask
	 *            the binary image of mask
	 * @return the geodesic distance map in a new ImageProcessor
	 */
	public ImageProcessor geodesicDistanceMap(ImageProcessor marker,
			ImageProcessor mask);
}
