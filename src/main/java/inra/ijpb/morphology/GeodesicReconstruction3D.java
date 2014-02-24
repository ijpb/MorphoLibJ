/**
 * 
 */
package inra.ijpb.morphology;

import ij.ImageStack;
import inra.ijpb.morphology.geodrec.GeodesicReconstruction3DAlgo;
import inra.ijpb.morphology.geodrec.GeodesicReconstructionByDilation3DGray8Scanning;
import inra.ijpb.morphology.geodrec.GeodesicReconstructionByDilation3DScanning;
import inra.ijpb.morphology.geodrec.GeodesicReconstructionByErosion3DGray8Scanning;
import inra.ijpb.morphology.geodrec.GeodesicReconstructionByErosion3DScanning;


/**
 * Geodesic reconstruction for 8-bits grayscale or binary stacks.
 * 
 * This class defines the interface for implementations of geodesic 
 * reconstruction algorithms,   
 * and provides a collection of static methods for commonly used 
 * operations, such as border removal or holes filling. 
 * 
 * @author David Legland
 *
 */
public abstract class GeodesicReconstruction3D {

	/**
	 * Removes the border of the input image, by performing a geodesic 
	 * reconstruction initialized with image boundary. 
	 * @see #fillHoles(ImageStack)
	 */
	public final static ImageStack killBorders(ImageStack stack) {
		// Image size
		int width = stack.getWidth();
		int height = stack.getHeight();
		int depth = stack.getSize();
		
		// Initialize marker image zeros everywhere except at borders
		ImageStack markers = stack.duplicate();
		for (int z = 1; z < depth-1; z++) {
			for (int y = 1; y < height-1; y++) {
				for (int x = 1; x < width-1; x++) {
					markers.setVoxel(x, y, z, 0);
				}
			}
		}
		// Reconstruct image from borders to find touching structures
		ImageStack result = reconstructByDilation(markers, stack);
		
		// removes result from original image
		for (int z = 1; z < depth-1; z++) {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					double val = stack.getVoxel(x, y, z)-result.getVoxel(x, y, z);
					result.setVoxel(x, y, z, Math.max(val, 0));
				}
			}
		}
		
		return result;
	}

	/**
	 * Fills the holes in the input image, by (1) inverting the image, (2) 
	 * performing a geodesic reconstruction initialized with inverted image
	 * boundary and (3) by inverting the result.
	 * @see #killBorders(ImageStack)
	 */
	public final static ImageStack fillHoles(ImageStack stack) {
		// Image size
		int width = stack.getWidth();
		int height = stack.getHeight();
		int depth = stack.getSize();
		
		// Initialize marker image with white everywhere except at borders
		ImageStack markers = stack.duplicate();
		for (int z = 1; z < depth-1; z++) {
			for (int y = 1; y < height-1; y++) {
				for (int x = 1; x < width-1; x++) {
					markers.setVoxel(x, y, z, Float.MAX_VALUE);
				}
			}
		}
		
		// Reconstruct image from borders to find touching structures
		return reconstructByErosion(markers, stack);
	}

	/**
	 * Static method to computes the geodesic reconstruction by dilation of 
	 * the marker image under the mask image.
	 */
	public final static ImageStack reconstructByDilation(ImageStack marker,
			ImageStack mask) {
		GeodesicReconstruction3DAlgo algo;
		if (marker.getBitDepth() == 8 && mask.getBitDepth() == 8) {
			algo = new GeodesicReconstructionByDilation3DGray8Scanning();
		} else {
			algo = new GeodesicReconstructionByDilation3DScanning();
		}
		return algo.applyTo(marker, mask);
	}

	/**
	 * Static method to computes the geodesic reconstruction by dilation of 
	 * the marker image under the mask image.
	 */
	public final static ImageStack reconstructByDilation(ImageStack marker,
			ImageStack mask, int connectivity) {
		GeodesicReconstruction3DAlgo algo;
		if (marker.getBitDepth() == 8 && mask.getBitDepth() == 8) {
			algo = new GeodesicReconstructionByDilation3DGray8Scanning(connectivity);
		} else {
			algo = new GeodesicReconstructionByDilation3DScanning(connectivity);
		}
		return algo.applyTo(marker, mask);
	}

	/**
	 * Static method to computes the geodesic reconstruction by dilation of 
	 * the marker image under the mask image, but restricted to a binary mask.
	 * 
	 * @param marker input image
	 * @param mask mask image
	 * @param connectivity 3d connnectivity
	 * @param binaryMask binary mask to restrict area of application
	 * @return geodesic reconstruction by dilation of input image
	 */
	public final static ImageStack reconstructByDilation(
			ImageStack marker,
			ImageStack mask, 
			int connectivity,
			ImageStack binaryMask ) 
	{
		//TODO: add support for non gray8 stacks
		GeodesicReconstruction3DAlgo algo = new GeodesicReconstructionByDilation3DGray8Scanning(
				connectivity);
		return algo.applyTo( marker, mask, binaryMask );
	}
	
	
	/**
	 * Static method to computes the geodesic reconstruction by erosion of the
	 * marker image over the mask image.
	 */
	public final static ImageStack reconstructByErosion(ImageStack marker,
			ImageStack mask) {
		GeodesicReconstruction3DAlgo algo;
		if (marker.getBitDepth() == 8 && mask.getBitDepth() == 8) {
			algo = new GeodesicReconstructionByErosion3DGray8Scanning();
		} else {
			algo = new GeodesicReconstructionByErosion3DScanning();
		}
		return algo.applyTo(marker, mask);
	}

	/**
	 * Static method to computes the geodesic reconstruction by erosion of the
	 * marker image over the mask image.
	 */
	public final static ImageStack reconstructByErosion(ImageStack marker,
			ImageStack mask, int connectivity) {
		GeodesicReconstruction3DAlgo algo;
		if (marker.getBitDepth() == 8 && mask.getBitDepth() == 8) {
			algo = new GeodesicReconstructionByErosion3DGray8Scanning(connectivity);
		} else {
			algo = new GeodesicReconstructionByErosion3DScanning(connectivity);
		}
		return algo.applyTo(marker, mask);
	}
	
}
