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
 * <p>
 * Geodesic reconstruction for 8-bits grayscale or binary stacks.
 * </p>
 * 
 * <p>
 * This class provides a collection of static methods for commonly used 
 * operations on 3D images, such as border removal or holes filling. 
 * </p>
 * 
 * <p>
 * Example of use:
 * <pre><code>
 * ImageStack mask = IJ.getImage().getStack();
 * int bitDepth = mask.getBitDepth();
 * ImageStack marker = ImageStack.create(mask.getWidth(), mask.getHeight(), mask.getSize(), bitDepth);
 * marker.set(30, 20, 10, 255); 
 * ImageStack rec = GeodesicReconstruction3D.reconstructByDilation(marker, mask, 6);
 * ImagePlus res = new ImagePlus("Geodesic Reconstruction", rec);
 * res.show(); 
 * </code></pre>
 * 
 * @see GeodesicReconstruction
 * @author David Legland
 */
public abstract class GeodesicReconstruction3D 
{
	/**
	 * Private constructor to prevent class instantiation.
	 */
	private GeodesicReconstruction3D()
	{
	}

	/**
	 * Removes the border of the input image, by performing a geodesic 
	 * reconstruction initialized with image boundary.
	 *  
	 * @see #fillHoles(ImageStack)
	 * 
	 * @param image the image to process
	 * @return a new image with borders removed
	 */
	public final static ImageStack killBorders(ImageStack image)
	{
		// Image size
		int width = image.getWidth();
		int height = image.getHeight();
		int depth = image.getSize();

		// Initialize marker image with zeros everywhere except at borders
		ImageStack markers = image.duplicate();
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
		ImageStack result = reconstructByDilation(markers, image);

		// removes result from original image
		for (int z = 0; z < depth; z++)
		{
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					double val = image.getVoxel(x, y, z) - result.getVoxel(x, y, z);
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
	 * 
	 * @see #killBorders(ImageStack)
	 * 
	 * @param image the image to process
	 * @return a new image with holes filled
	 */
	public final static ImageStack fillHoles(ImageStack image) 
	{
		// Image size
		int width = image.getWidth();
		int height = image.getHeight();
		int depth = image.getSize();
		
		// Initialize marker image with white everywhere except at borders
		ImageStack markers = image.duplicate();
		for (int z = 1; z < depth-1; z++) 
		{
			for (int y = 1; y < height-1; y++) 
			{
				for (int x = 1; x < width-1; x++) 
				{
					markers.setVoxel(x, y, z, Float.MAX_VALUE);
				}
			}
		}
		
		// Reconstruct image from borders to find touching structures
		return reconstructByErosion(markers, image);
	}

	/**
	 * Static method to computes the geodesic reconstruction by dilation of 
	 * the marker image under the mask image.
	 * 
	 * @param marker input marker image
	 * @param mask mask image
	 * @return the result of 3D geodesic reconstruction
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
	 * 
	 * @param marker input marker image
	 * @param mask mask image
	 * @param connectivity 3d connectivity (6 or 26)
	 * @return the result of 3D geodesic reconstruction
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
	 * @param marker input marker image
	 * @param mask mask image
	 * @param connectivity 3d connectivity (6 or 26)
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
	 * 
	 * @param marker input marker image
	 * @param mask mask image
	 * @return the result of 3D geodesic reconstruction
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

	 * @param marker input marker image
	 * @param mask mask image
	 * @param connectivity 3d connectivity (6 or 26)
	 * @return the result of 3D geodesic reconstruction
	 */
	public final static ImageStack reconstructByErosion(ImageStack marker,
			ImageStack mask, int connectivity)
	{
		
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
