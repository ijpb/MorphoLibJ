/**
 * 
 */
package inra.ijpb.morphology.geodrec;

import ij.ImageStack;

/**
 * @author David Legland
 *
 */
public interface GeodesicReconstruction3DAlgo {
	
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
	
	public int getConnectivity();
	public void setConnectivity(int conn);
}
