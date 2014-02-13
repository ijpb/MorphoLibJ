/**
 * 
 */
package inra.ijpb.morphology.geodrec;

import ij.process.ImageProcessor;

/**
 * Defines the interface for geodesic reconstructions algorithms applied to
 * planar images.
 * 
 * @author David Legland
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
	
	/**
	 * Returns the chosen connectivity of the algorithm, either 4 or 8. 
	 */
	public int getConnectivity();

	/**
	 * Changes the connectivity of the algorithm to either 4 or 8. 
	 */
	public void setConnectivity(int adj);
}
