/**
 * 
 */
package inra.ijpb.morphology;

import ij.ImageStack;
import inra.ijpb.morphology.geodrec.GeodesicReconstruction3DAlgo;
import inra.ijpb.morphology.geodrec.GeodesicReconstruction3DHybrid0Float;
import inra.ijpb.morphology.geodrec.GeodesicReconstruction3DHybrid0Gray8;
import inra.ijpb.morphology.geodrec.GeodesicReconstructionByDilation3DGray8Scanning;
import inra.ijpb.morphology.geodrec.GeodesicReconstructionByDilation3DScanning;
import inra.ijpb.morphology.geodrec.GeodesicReconstructionByErosion3DScanning;
import inra.ijpb.morphology.geodrec.GeodesicReconstructionType;


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
	public final static ImageStack killBorders(ImageStack stack)
	{
		// Image size
		int width = stack.getWidth();
		int height = stack.getHeight();
		int depth = stack.getSize();

		// Initialize marker image zeros everywhere except at borders
		ImageStack markers = stack.duplicate();
		for (int z = 1; z < depth - 1; z++)
		{
			for (int y = 1; y < height - 1; y++)
			{
				for (int x = 1; x < width - 1; x++)
				{
					markers.setVoxel(x, y, z, 0);
				}
			}
		}
		// Reconstruct image from borders to find touching structures
		ImageStack result = reconstructByDilation(markers, stack);

		// removes result from original image
		for (int z = 0; z < depth; z++)
		{
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					double val = stack.getVoxel(x, y, z) - result.getVoxel(x, y, z);
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
			ImageStack mask)
	{
		GeodesicReconstruction3DAlgo algo;
		if (marker.getBitDepth() == 8 && mask.getBitDepth() == 8)
		{
			algo = new GeodesicReconstruction3DHybrid0Gray8(
					GeodesicReconstructionType.BY_DILATION);
		} 
		else if (marker.getBitDepth() == 32 && mask.getBitDepth() == 32)
		{
			algo = new GeodesicReconstruction3DHybrid0Float(
					GeodesicReconstructionType.BY_DILATION);
		} 
		else
		{
			algo = new GeodesicReconstructionByDilation3DScanning();
		}
		return algo.applyTo(marker, mask);
	}

	/**
	 * Static method to computes the geodesic reconstruction by dilation of 
	 * the marker image under the mask image.
	 */
	public final static ImageStack reconstructByDilation(ImageStack marker,
			ImageStack mask, int connectivity)
	{
		GeodesicReconstruction3DAlgo algo;
		if (marker.getBitDepth() == 8 && mask.getBitDepth() == 8)
		{
			algo = new GeodesicReconstruction3DHybrid0Gray8(
					GeodesicReconstructionType.BY_DILATION, connectivity);
		} 
		else if (marker.getBitDepth() == 32 && mask.getBitDepth() == 32)
		{
			algo = new GeodesicReconstruction3DHybrid0Float(
					GeodesicReconstructionType.BY_DILATION, connectivity);
		} 
		else
		{
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
			ImageStack mask)
	{
		GeodesicReconstruction3DAlgo algo;
		if (marker.getBitDepth() == 8 && mask.getBitDepth() == 8)
		{
			algo = new GeodesicReconstruction3DHybrid0Gray8(
					GeodesicReconstructionType.BY_EROSION);

		} 
		else if (marker.getBitDepth() == 32 && mask.getBitDepth() == 32)
		{
			algo = new GeodesicReconstruction3DHybrid0Float(
					GeodesicReconstructionType.BY_EROSION);
		} 
		else
		{
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
		
		if ( Thread.currentThread().isInterrupted() )					
			return null;
		
		GeodesicReconstruction3DAlgo algo;
		if (marker.getBitDepth() == 8 && mask.getBitDepth() == 8)
		{
			algo = new GeodesicReconstruction3DHybrid0Gray8(
					GeodesicReconstructionType.BY_EROSION, connectivity);
		}
		else if (marker.getBitDepth() == 32 && mask.getBitDepth() == 32)
		{
			algo = new GeodesicReconstruction3DHybrid0Float(
					GeodesicReconstructionType.BY_EROSION, connectivity);
		} 
		else
		{
			algo = new GeodesicReconstructionByErosion3DScanning(connectivity);
		}
		return algo.applyTo(marker, mask);
	}
	
}
