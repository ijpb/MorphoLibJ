/**
 * 
 */
package inra.ijpb.morphology.geodrec;

import ij.ImageStack;
import inra.ijpb.algo.Algo;

/**
 * Defines the interface for geodesic reconstructions algorithms applied to
 * 3D stack images.
 * 
 * @author David Legland
 */
public interface GeodesicReconstruction3DAlgo extends Algo {
	
	/**
	 * Applies the geodesic reconstruction algorithm to the input marker and
	 * mask images.
	 * @param marker image used to initialize the reconstruction
	 * @param mask image used to constrain the reconstruction
	 * @return the geodesic reconstruction of marker image constrained by mask image
	 */
	public ImageStack applyTo(ImageStack marker, ImageStack mask);
	
	/**
	 * Applies the geodesic reconstruction algorithm to the input marker and
	 * mask images, restricted by a binary mask.
	 * 
	 * @param marker image used to initialize the reconstruction
	 * @param mask image used to constrain the reconstruction
	 * @param binaryMask binary mask to restrict the region of application
	 * @return the geodesic reconstruction of marker image constrained by mask image
	 */
	public ImageStack applyTo(ImageStack marker, ImageStack mask, ImageStack binaryMask );
	
	/**
	 * Returns the chosen connectivity of the algorithm, either 6 or 26. 
	 */
	public int getConnectivity();

	/**
	 * Changes the connectivity of the algorithm to either 6 or 26. 
	 */
	public void setConnectivity(int conn);
}
