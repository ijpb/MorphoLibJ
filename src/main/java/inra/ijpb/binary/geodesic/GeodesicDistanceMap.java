/**
 * 
 */
package inra.ijpb.binary.geodesic;

import ij.process.ImageProcessor;

/**
 * Interface for computing Geodesic distance maps (also known as geodesic
 * distance transform) from binary images.
 *
 * @author David Legland
 */
public interface GeodesicDistanceMap
{
	/**
	 * Computes the geodesic distance transform (or geodesic distance map) of a
	 * binary image of marker, constrained to a binary mask.
	 */
	public ImageProcessor geodesicDistanceMap(ImageProcessor marker,
			ImageProcessor mask);
}
