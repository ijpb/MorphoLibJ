/**
 * 
 */
package inra.ijpb.morphology.geodrec;

import ij.process.ImageProcessor;

/**
 * @author David Legland
 *
 */
public interface GeodesicReconstructionAlgo {
	
	/**
	 * Applies the geodesic reconstruction algorithm to the input marker and
	 * mask images.
	 * @param marker image used to initialize the reconstruction
	 * @param mask image used to constrain the reconstruction
	 * @return the geodesic reconstruction of marker image constrained by mask image
	 */
	public ImageProcessor applyTo(ImageProcessor marker, ImageProcessor mask);
	
	public int getConnectivity();
	public void setConnectivity(int adj);
}
