/**
 * 
 */
package inra.ijpb.morphology;

import ij.process.ImageProcessor;
import inra.ijpb.morphology.geodrec.GeodesicReconstructionAlgo;
import inra.ijpb.morphology.geodrec.GeodesicReconstructionHybrid;
import inra.ijpb.morphology.geodrec.GeodesicReconstructionType;

/**
 * <p>
 * Geodesic reconstruction for grayscale or binary images. Most algorithms works
 * for any data type.
 * </p>
 * 
 * <p>
 * This class provides a collection of static methods for commonly used
 * operations, such as border removal or holes filling.
 * </p>
 * 
 * <p>
 * Example of use:
 * <pre><code>
 * ImageProcessor mask = IJ.getImage().getProcessor();
 * ImageProcessor marker = mask.createProcessor(mask.getWidth(), mask.getHeight());
 * marker.set(20, 10, 255); 
 * ImageProcessor rec = GeodesicReconstruction.reconstructByDilation(marker, mask, 4);
 * ImagePlus res = new ImagePlus("Geodesic Reconstruction", rec);
 * res.show(); 
 * </code></pre>
 * 
 * @see GeodesicReconstruction3D
 * 
 * @author David Legland
 */
public abstract class GeodesicReconstruction 
{
	/**
	 * Private constructor to prevent class instantiation.
	 */
	private GeodesicReconstruction()
	{
	}

	/**
	 * Removes the border of the input image, by performing a geodesic
	 * reconstruction initialized with image boundary.
	 * 
	 * @see #fillHoles(ImageProcessor)
	 * 
	 * @param image the image to process
	 * @return a new image with borders removed
	 */
	public final static ImageProcessor killBorders(ImageProcessor image) 
	{
		// Image size
		int width = image.getWidth();
		int height = image.getHeight();
		
		// Initialize marker image with zeros everywhere except at borders
		ImageProcessor markers = image.duplicate();
		for (int y = 1; y < height-1; y++) 
		{
			for (int x = 1; x < width-1; x++) 
			{
				markers.set(x, y, 0);
			}
		}
		
		// Reconstruct image from borders to find touching structures
		ImageProcessor result = reconstructByDilation(markers, image);
		
		// removes result from original image
		for (int y = 0; y < height; y++) 
		{
			for (int x = 0; x < width; x++) 
			{
				int val = image.get(x, y) - result.get(x, y);
				result.set(x, y, Math.max(val, 0));
			}
		}
		
		return result;
	}

	/**
	 * Fills the holes in the input image, by (1) inverting the image, (2) 
	 * performing a geodesic reconstruction initialized with inverted image
	 * boundary and (3) by inverting the result.
	 * 
	 * @see #killBorders(ImageProcessor)
	 * 
	 * @param image the image to process
	 * @return a new image with holes filled
	 */
	public final static ImageProcessor fillHoles(ImageProcessor image) 
	{
		// Image size
		int width = image.getWidth();
		int height = image.getHeight();

		// Initialize marker image with white everywhere except at borders
		ImageProcessor markers = image.duplicate();
		for (int y = 1; y < height-1; y++) 
		{
			for (int x = 1; x < width-1; x++) 
			{
				markers.set(x, y, 255);
			}
		}
		
		// Reconstruct image from borders to find touching structures
		return reconstructByErosion(markers, image);
	}

	/**
	 * Static method to computes the geodesic reconstruction by dilation of the
	 * marker image under the mask image.
	 *
	 * @param marker
	 *            input marker image
	 * @param mask
	 *            mask image
	 * @return the result of geodesic reconstruction
	 */
	public final static ImageProcessor reconstructByDilation(ImageProcessor marker,
			ImageProcessor mask) 
	{
		GeodesicReconstructionAlgo algo = new GeodesicReconstructionHybrid(
				GeodesicReconstructionType.BY_DILATION);
		return algo.applyTo(marker, mask);
	}

	/**
	 * Static method to computes the geodesic reconstruction by dilation of 
	 * the marker image under the mask image.
	 *
	 * @param marker input marker image
	 * @param mask mask image
	 * @param connectivity planar connectivity (4 or 8)
	 * @return the result of geodesic reconstruction
	 */
	public final static ImageProcessor reconstructByDilation(ImageProcessor marker,
			ImageProcessor mask, int connectivity) 
	{
		GeodesicReconstructionAlgo algo = new GeodesicReconstructionHybrid(
				GeodesicReconstructionType.BY_DILATION, connectivity);
		return algo.applyTo(marker, mask);
	}

	/**
	 * Static method to computes the geodesic reconstruction by erosion of the
	 * marker image over the mask image.
	 *
	 * @param marker input marker image
	 * @param mask mask image
	 * @return the result of geodesic reconstruction
	 */
	public final static ImageProcessor reconstructByErosion(ImageProcessor marker,
			ImageProcessor mask) 
	{
		GeodesicReconstructionAlgo algo = new GeodesicReconstructionHybrid(
				GeodesicReconstructionType.BY_EROSION);
		return algo.applyTo(marker, mask);
	}

	/**
	 * Static method to computes the geodesic reconstruction by erosion of the
	 * marker image over the mask image.
	 *
	 * @param marker input marker image
	 * @param mask mask image
	 * @param connectivity planar connectivity (4 or 8)
	 * @return the result of geodesic reconstruction
	 */
	public final static ImageProcessor reconstructByErosion(ImageProcessor marker,
			ImageProcessor mask, int connectivity)
	{
		GeodesicReconstructionAlgo algo = new GeodesicReconstructionHybrid(
				GeodesicReconstructionType.BY_EROSION, connectivity);
		return algo.applyTo(marker, mask);
	}
}
