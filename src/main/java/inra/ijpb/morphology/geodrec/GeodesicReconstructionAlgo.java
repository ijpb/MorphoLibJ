/**
 * 
 */
package inra.ijpb.morphology.geodrec;

import ij.process.ImageProcessor;

/**
 * <p>
 * Defines the interface for geodesic reconstructions algorithms applied to
 * planar images.
 * </p>
 * 
 * <p>
 * There are currently four implementation of geodesic reconstruction for planar
 * images:
 * <ul>
 * <li>GeodesicReconstructionByDilation: implements reconstruction by dilation,
 * using scanning algorithm</li>
 * <li>GeodesicReconstructionByErosion: implements reconstruction by erosion,
 * using scanning algorithm</li>
 * <li>GeodesicReconstructionScanning: implements reconstruction by dilation or
 * erosion, using scanning algorithm.</li>
 * <li>GeodesicReconstructionHybrid: implements reconstruction by dilation or
 * erosion, using a classical forward pass, a backward pass that initialize a
 * processing queue, and processes each pixel in the queue until it is empty.</li>
 * </ul>
 * 
 * The most versatile one is the "Hybrid" version.
 * 
 * @author David Legland
 */
public interface GeodesicReconstructionAlgo 
{
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
	public void setConnectivity(int conn);
}
