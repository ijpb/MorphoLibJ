/**
 * 
 */
package inra.ijpb.binary;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import inra.ijpb.binary.distmap.DistanceTransform;
import inra.ijpb.binary.distmap.DistanceTransform3D;
import inra.ijpb.binary.distmap.DistanceTransform3DFloat;
import inra.ijpb.binary.distmap.DistanceTransform3DShort;
import inra.ijpb.binary.distmap.DistanceTransform3x3Float;
import inra.ijpb.binary.distmap.DistanceTransform3x3Short;
import inra.ijpb.binary.distmap.DistanceTransform5x5Float;
import inra.ijpb.binary.distmap.DistanceTransform5x5Short;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransform;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransformFloat;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransformFloat5x5;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransformShort;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransformShort5x5;
import inra.ijpb.label.LabelImages;
import inra.ijpb.morphology.FloodFill;
import inra.ijpb.morphology.FloodFill3D;

/**
 * A collection of static methods for operating on binary images (2D/3D).
 * Some of the methods need the LabelImages class. 
 * 
 * @see inra.ijpb.label.LabelImages
 * 
 * @author David Legland
 *
 */
public class BinaryImages 
{
	/**
	 * Private constructor to prevent class instantiation.
	 */
	private BinaryImages()
	{
	}

	/**
	 * Computes the labels in the binary 2D or 3D image contained in the given
	 * ImagePlus, and computes the maximum label to set up the display range of
	 * the resulting ImagePlus.
	 * 
	 * @param imagePlus
	 *            contains the 3D binary image stack
	 * @param conn
	 *            the connectivity, either 4 or 8 for planar images, or 6 or 26
	 *            for 3D images
	 * @param bitDepth
	 *            the number of bits used to create the result image (8, 16 or
	 *            32)
	 * @return an ImagePlus containing the label of each connected component.
	 * @throws RuntimeException
	 *             if the number of labels reaches the maximum number that can
	 *             be represented with this bitDepth
	 * @see inra.ijpb.morphology.FloodFill
	 */
	public final static ImagePlus componentsLabeling(ImagePlus imagePlus, 
			int conn, int bitDepth)
	{
		ImagePlus labelPlus;
		int nLabels;
	
		if (imagePlus.getStackSize() == 1)
		{
			ImageProcessor labels = componentsLabeling(imagePlus.getProcessor(),
					conn, bitDepth);
			labelPlus = new ImagePlus("Labels", labels);
			nLabels = findMax(labels);
		} else 
		{
			ImageStack labels = componentsLabeling(imagePlus.getStack(), conn,
					bitDepth);
			labelPlus = new ImagePlus("Labels", labels);
			nLabels = findMax(labels);
		}
		
		labelPlus.setDisplayRange(0, nLabels);
		return labelPlus;
	}

	/**
	 * Computes the labels of the connected components in the given planar
	 * binary image. The type of result is controlled by the bitDepth option.
	 * 
	 * Uses a Flood-fill type algorithm.
	 * 
	 * @param image
	 *            contains the binary image (any type is accepted)
	 * @param conn
	 *            the connectivity, either 4 or 8
	 * @param bitDepth
	 *            the number of bits used to create the result image (8, 16 or
	 *            32)
	 * @return a new instance of ImageProcessor containing the label of each
	 *         connected component.
	 * @throws RuntimeException
	 *             if the number of labels reaches the maximum number that can
	 *             be represented with this bitDepth
	 * @see inra.ijpb.morphology.FloodFill
	 */
	public final static ImageProcessor componentsLabeling(ImageProcessor image,
			int conn, int bitDepth) 
	{
		// get image size
		int width = image.getWidth();
		int height = image.getHeight();
		int maxLabel;

		// Depending on bitDepth, create result image, and choose max label 
		// number
		ImageProcessor labels;
		switch (bitDepth) {
		case 8: 
			labels = new ByteProcessor(width, height);
			maxLabel = 255;
			break; 
		case 16: 
			labels = new ShortProcessor(width, height);
			maxLabel = 65535;
			break;
		case 32:
			labels = new FloatProcessor(width, height);
			maxLabel = 0x01 << 23 - 1;
			break;
		default:
			throw new IllegalArgumentException(
					"Bit Depth should be 8, 16 or 32.");
		}

		// the label counter
		int nLabels = 0;

		// iterate on image pixels to fin new regions
		for (int y = 0; y < height; y++) 
		{
			IJ.showProgress(y, height);
			for (int x = 0; x < width; x++) 
			{
				if (image.get(x, y) == 0)
					continue;
				if (labels.get(x, y) > 0)
					continue;

				// a new label is found: check current label number  
				if (nLabels == maxLabel)
				{
					throw new RuntimeException("Max number of label reached (" + maxLabel + ")");
				}
				
				// increment label index, and propagate
				nLabels++;
				FloodFill.floodFillFloat(image, x, y, labels, nLabels, conn);
			}
		}
		IJ.showProgress(1);

		labels.setMinAndMax(0, nLabels);
		return labels;
	}

	/**
	 * Computes the labels of the connected components in the given 3D binary
	 * image. The type of result is controlled by the bitDepth option.
	 * 
	 * Uses a Flood-fill type algorithm.
	 * 
	 * @param image
	 *            contains the 3D binary image (any type is accepted)
	 * @param conn
	 *            the connectivity, either 6 or 26
	 * @param bitDepth
	 *            the number of bits used to create the result stack (8, 16 or
	 *            32)
	 * @return a new instance of ImageStack containing the label of each
	 *         connected component.
	 * @throws RuntimeException
	 *             if the number of labels reaches the maximum number that can
	 *             be represented with this bitDepth
	 * @see inra.ijpb.morphology.FloodFill
	 */
	public final static ImageStack componentsLabeling(ImageStack image, int conn,
			int bitDepth) 
	{
		if ( Thread.currentThread().isInterrupted() )					
			return null;
		
		// get image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		IJ.showStatus("Allocate Memory");
		ImageStack labels = ImageStack.create(sizeX, sizeY, sizeZ, bitDepth);

		int maxLabel;
		switch (bitDepth) {
		case 8: 
			maxLabel = 255;
			break; 
		case 16: 
			maxLabel = 65535;
			break;
		case 32:
			maxLabel = 0x01 << 23;
			break;
		default:
			throw new IllegalArgumentException(
					"Bit Depth should be 8, 16 or 32.");
		}

		IJ.showStatus("Compute Labels...");
		int nLabels = 0;
		for (int z = 0; z < sizeZ; z++) 
		{
			IJ.showProgress(z, sizeZ);
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					// Do not process background voxels
					if (image.getVoxel(x, y, z) == 0)
						continue;

					// Do not process voxels already labeled
					if (labels.getVoxel(x, y, z) > 0)
						continue;

					// a new label is found: check current label number  
					if (nLabels == maxLabel)
					{
						throw new RuntimeException("Max number of label reached (" + maxLabel + ")");
					}
					
					// increment label index, and propagate
					nLabels++;
					FloodFill3D.floodFillFloat(image, x, y, z, labels, nLabels, conn);
				}
			}
		}
		
		IJ.showStatus("");
		IJ.showProgress(1);
		return labels;
	}

	/**
	 * Computes maximum value in the input 2D image.
	 * This method is used to compute display range of result ImagePlus.
	 */
	private final static int findMax(ImageProcessor image) 
	{
		// get image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		
		// find maximum value over voxels
		int maxVal = 0;
		for (int y = 0; y < sizeY; y++) 
		{
			IJ.showProgress(y, sizeY);
			for (int x = 0; x < sizeX; x++) 
			{
				maxVal = Math.max(maxVal, image.get(x, y));
			}
		}
		IJ.showProgress(1);
		
		return maxVal;
	}
	
	/**
	 * Computes maximum value in the input 3D image.
	 * This method is used to compute display range of result ImagePlus.
	 */
	private final static int findMax(ImageStack image) 
	{
		// get image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		// find maximum value over voxels
		int maxVal = 0;
		for (int z = 0; z < sizeZ; z++) 
		{
			IJ.showProgress(z, sizeZ);
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					maxVal = Math.max(maxVal, (int) image.getVoxel(x, y, z));
				}
			}
		}
		IJ.showProgress(1);
		
		return maxVal;
	}
	
	/**
	 * Computes the distance map (or distance transform) from a binary image
	 * processor. Distance is computed for each foreground (white) pixel or
	 * voxel, as the chamfer distance to the nearest background (black) pixel or
	 * voxel.
	 * 
	 * @param imagePlus
	 *            an ImagePlus object containing a 3D binary stack
	 * @return a new ImagePlus containing the 3D distance map
	 */
	public static final ImagePlus distanceMap(ImagePlus imagePlus)
	{
		ImagePlus resultPlus;
		String newName = imagePlus.getShortTitle() + "-distMap";
		
		// Dispatch to appropriate function depending on dimension
		if (imagePlus.getStackSize() == 1) 
		{
			// process planar image
			ImageProcessor image = imagePlus.getProcessor();
			ImageProcessor result = distanceMap(image);
			resultPlus = new ImagePlus(newName, result);
		} 
		else
		{
			// process image stack
			ImageStack image = imagePlus.getStack();
			ImageStack result = distanceMap(image);
			resultPlus = new ImagePlus(newName, result);
		}
		
		resultPlus.copyScale(imagePlus);
		return resultPlus;		
	}
	
	/**
	 * <p>
	 * Computes the distance map (or distance transform) from a binary image
	 * processor. Distance is computed for each foreground (white) pixel, as the
	 * chamfer distance to the nearest background (black) pixel.
	 * </p>
	 * 
	 * <p>
	 * This method uses default 5x5 weights, and normalizes the resulting map.
	 * Result is given in a new instance of ShortProcessor.
	 * </p>
	 * 
	 * @param image
	 *            the input binary image
	 * @return a new ImageProcessor containing the distance map result
	 */
	public static final ImageProcessor distanceMap(ImageProcessor image) 
	{
		return distanceMap(image, new short[]{5, 7, 11}, true);
	}
	
	/**
	 * <p>
	 * Computes the distance map from a binary image processor, by specifying
	 * weights and normalization.
	 * </p>
	 * 
	 * <p>
	 * Distance is computed for each foreground (white) pixel, as the chamfer
	 * distance to the nearest background (black) pixel. Result is given as a
	 * new instance of ShortProcessor.
	 * </p>
	 * 
	 * @param image
	 *            the input binary image
	 * @param weights
	 *            an array of chamfer weights, with at least two values
	 * @param normalize
	 *            indicates whether the resulting distance map should be
	 *            normalized (divide distances by the first chamfer weight)
	 * @return the distance map obtained after applying the distance transform
	 */
	public static final ShortProcessor distanceMap(ImageProcessor image,
			short[] weights, boolean normalize)
	{
		DistanceTransform algo;
		switch (weights.length) {
		case 2:
			algo = new DistanceTransform3x3Short(weights, normalize);
			break;
		case 3:
			algo = new DistanceTransform5x5Short(weights, normalize);
			break;
		default:
			throw new IllegalArgumentException(
					"Requires weight array with 2 or 3 elements");
		}

		return (ShortProcessor) algo.distanceMap(image);
	}

	/**
	 * <p>
	 * Computes the distance map from a binary image processor, by specifying
	 * weights and normalization.
	 * </p>
	 * 
	 * <p>
	 * Distance is computed for each foreground (white) pixel, as the chamfer
	 * distance to the nearest background (black) pixel. Result is given in a
	 * new instance of FloatProcessor.
	 * </p>
	 * 
	 * @param image
	 *            the input binary image
	 * @param weights
	 *            an array of chamfer weights, with at least two values
	 * @param normalize
	 *            indicates whether the resulting distance map should be
	 *            normalized (divide distances by the first chamfer weight)
	 * @return the distance map obtained after applying the distance transform
	 */
	public static final FloatProcessor distanceMap(ImageProcessor image,
			float[] weights, boolean normalize) 
	{
		DistanceTransform algo;
		switch (weights.length) 
		{
		case 2:
			algo = new DistanceTransform3x3Float(weights, normalize);
			break;
		case 3:
			algo = new DistanceTransform5x5Float(weights, normalize);
			break;
		default:
			throw new IllegalArgumentException(
					"Requires weight array with 2 or 3 elements");
		}
		
		return (FloatProcessor) algo.distanceMap(image);
	}

	/**
	 * Computes the distance map from a binary 3D image. 
	 * Distance is computed for each foreground (white) pixel, as the 
	 * chamfer distance to the nearest background (black) pixel.
	 * 
	 * @param image
	 *            the input binary image
	 * @return the distance map obtained after applying the distance transform
	 */
	public static final ImageStack distanceMap(ImageStack image)
	{
		float[] weights = new float[]{3.0f, 4.0f, 5.0f};
		DistanceTransform3D algo = new DistanceTransform3DFloat(weights);
		return algo.distanceMap(image);
	}
	
	/**
	 * Computes the distance map from a binary 3D image. 
	 * Distance is computed for each foreground (white) pixel, as the 
	 * chamfer distance to the nearest background (black) pixel.
	 * 
	 * @param image
	 *            the input binary image
	 * @param weights
	 *            an array of chamfer weights, with at least three values
	 * @param normalize
	 *            indicates whether the resulting distance map should be
	 *            normalized (divide distances by the first chamfer weight)
	 * @return the distance map obtained after applying the distance transform
	 */
	public static final ImageStack distanceMap(ImageStack image,
			short[] weights, boolean normalize)
	{
		DistanceTransform3D	algo = new DistanceTransform3DShort(weights, normalize);
			
		return algo.distanceMap(image);
	}
	
	/**
	 * Computes the distance map from a binary 3D image. 
	 * Distance is computed for each foreground (white) pixel, as the 
	 * chamfer distance to the nearest background (black) pixel.
	 * 
	 * @param image
	 *            the input 3D binary image
	 * @param weights
	 *            an array of chamfer weights, with at least three values
	 * @param normalize
	 *            indicates whether the resulting distance map should be
	 *            normalized (divide distances by the first chamfer weight)
	 * @return the distance map obtained after applying the distance transform
	 */
	public static final ImageStack distanceMap(ImageStack image, 
			float[] weights, boolean normalize)
	{
		DistanceTransform3D algo = new DistanceTransform3DFloat(weights, normalize);
		return algo.distanceMap(image);
	}
	
	/**
	 * Computes the geodesic distance transform (or geodesic distance map) of a
	 * binary image of marker, constrained to a binary mask.
	 * Returns the result in a new instance of ShortProcessor.
	 * 
	 * @param marker
	 *            the binary image of marker
	 * @param mask
	 *            the binary image of mask
	 * @return the geodesic distance map in a new ImageProcessor
	 */
	public static final ImageProcessor geodesicDistanceMap(ImageProcessor marker,
			ImageProcessor mask) 
	{
		return geodesicDistanceMap(marker, mask, new short[]{5, 7, 11}, true);
	}
	
	/**
	 * Computes the geodesic distance transform (or geodesic distance map) of a
	 * binary image of marker, constrained to a binary mask.
	 * Returns the result in a new instance of ShortProcessor.
	 * 
	 * @param marker
	 *            the binary image of marker
	 * @param mask
	 *            the binary image of mask
	 * @param weights
	 *            an array of chamfer weights, with at least two values
	 * @param normalize
	 *            indicates whether the resulting distance map should be
	 *            normalized (divide distances by the first chamfer weight)
	 * @return the geodesic distance map in a new ImageProcessor
	 */
	public static final ImageProcessor geodesicDistanceMap(ImageProcessor marker,
			ImageProcessor mask, short[] weights, boolean normalize) 
	{
		GeodesicDistanceTransform algo;
		switch (weights.length) 
		{
		case 2:
			algo = new GeodesicDistanceTransformShort(weights, normalize);
			break;
		case 3:
			algo = new GeodesicDistanceTransformShort5x5(weights, normalize);
			break;
		default:
			throw new IllegalArgumentException(
					"Requires weight array with 2 or 3 elements");
		}
		
		return algo.geodesicDistanceMap(marker, mask);
	}
	
	/**
	 * Computes the geodesic distance transform (or geodesic distance map) of a
	 * binary image of marker, constrained to a binary mask. 
	 * Returns the result in a new instance of FloatProcessor.
	 * 
	 * @param marker
	 *            the binary image of marker
	 * @param mask
	 *            the binary image of mask
	 * @param weights
	 *            an array of chamfer weights, with at least two values
	 * @param normalize
	 *            indicates whether the resulting distance map should be
	 *            normalized (divide distances by the first chamfer weight)
	 * @return the geodesic distance map in a new ImageProcessor
	 */
	public static final ImageProcessor geodesicDistanceMap(ImageProcessor marker,
			ImageProcessor mask, float[] weights, boolean normalize) 
	{
		GeodesicDistanceTransform algo;
		switch (weights.length) 
		{
		case 2:
			algo = new GeodesicDistanceTransformFloat(weights, normalize);
			break;
		case 3:
			algo = new GeodesicDistanceTransformFloat5x5(weights, normalize);
			break;
		default:
			throw new IllegalArgumentException(
					"Requires weight array with 2 or 3 elements");
		}
		
		return algo.geodesicDistanceMap(marker, mask);
	}
	

	/**
	 * Applies size opening on a binary 2D or 3D image. The method creates a new
	 * binary image that contains only particles with at least the specified
	 * number of pixels.
	 * 
	 * @param imagePlus
	 *            the binary 2D or 3D image containing individual particles
	 * @param minElementCount
	 *            the minimum number of pixels or voxels required to keep a
	 *            particle
	 * @return a new binary image containing only the selected particles
	 * 
	 * @see inra.ijpb.label.LabelImages#sizeOpening(ImagePlus, int)
	 */
	public static final ImagePlus sizeOpening(ImagePlus imagePlus,
			int minElementCount) 
	{
		ImagePlus resultPlus;
		String newName = imagePlus.getShortTitle() + "-sizeOpening";
        
        if (imagePlus.getStackSize() == 1) 
        {
            ImageProcessor image = imagePlus.getProcessor();
            ImageProcessor result = areaOpening(image, minElementCount);
            resultPlus = new ImagePlus(newName, result);    		
        }
        else
        {
            ImageStack image = imagePlus.getStack();
            ImageStack result = LabelImages.volumeOpening(image, minElementCount);
        	result.setColorModel(image.getColorModel());
            resultPlus = new ImagePlus(newName, result);
        }
        
        // keep spatial calibration
		resultPlus.copyScale(imagePlus);
		return resultPlus;
	}

	/**
	 * Applies area opening on a binary image: creates a new binary image that
	 * contains only particles with at least the specified number of pixels.
	 * 
	 * @param image
	 *            the binary image containing individual particles
	 * @param nPixelMin
	 *            the minimum number of pixels required to keep a particle
	 * @return a new binary image containing only the selected particles
	 * 
	 * @see inra.ijpb.label.LabelImages#areaOpening(ImageProcessor, int)
	 */
	public static final ImageProcessor areaOpening(ImageProcessor image,
			int nPixelMin) 
	{
		// Labeling
		ImageProcessor labelImage = componentsLabeling(image, 4, 16);

		// keep only necessary labels and binarize
		return binarize(LabelImages.areaOpening(labelImage, nPixelMin));
	}
	
	
	/**
	 * Applies area opening on a binary image: creates a new binary image that
	 * contains only particle with at least the specified number of voxels.
	 * 
	 * @param image
	 *            the 3D binary image containing individual particles
	 * @param nVoxelMin
	 *            the minimum number of voxels required to keep a particle
	 * @return a new binary image containing only the selected particles
	 *
	 * @see inra.ijpb.label.LabelImages#volumeOpening(ImageStack, int)
	 */
	public static final ImageStack volumeOpening(ImageStack image, int nVoxelMin) 
	{
		// Labeling
		ImageStack labelImage = componentsLabeling(image, 6, 16);

		// keep only necessary labels and binarize
		return binarize(LabelImages.volumeOpening(labelImage, nVoxelMin));
	}
	
	
	/**
	 * Returns a binary image that contains only the largest region.
	 * 
	 * @param imagePlus
	 *            an instance of imagePlus that contains a binary image
	 * @return a new ImagePlus containing only the largest region from original
	 *         image
	 */
	public static final ImagePlus keepLargestRegion(ImagePlus imagePlus) 
	{
		ImagePlus resultPlus;
		String newName = imagePlus.getShortTitle() + "-largest";
		
		// Dispatch to appropriate function depending on dimension
		if (imagePlus.getStackSize() == 1) 
		{
			// process planar image
			ImageProcessor image = imagePlus.getProcessor();
			ImageProcessor result = keepLargestRegion(image);
			resultPlus = new ImagePlus(newName, result);
		} 
		else 
		{
			// process image stack
			ImageStack image = imagePlus.getStack();
			ImageStack result = keepLargestRegion(image);
			resultPlus = new ImagePlus(newName, result);
		}
		
		resultPlus.copyScale(imagePlus);
		return resultPlus;
	}
	
	/**
	 * Returns a binary image that contains only the largest region.
	 * 
	 * @param image
	 *            a binary image containing several individual regions
	 * @return a new binary image containing only the largest region from
	 *         original image
	 */
	public static final ImageProcessor keepLargestRegion(ImageProcessor image) 
	{
		ImageProcessor labelImage = componentsLabeling(image, 4, 16);
		ImageProcessor result = binarize(LabelImages.keepLargestLabel(labelImage));
		result.setLut(image.getLut());
		return result;
	}
	
	/**
	 * Returns a binary image that contains only the largest label.
	 * 
	 * @param image
	 *            a binary 3D image containing several individual regions
	 * @return a new binary image containing only the largest region from
	 *         original image
	 */
	public static final ImageStack keepLargestRegion(ImageStack image) 
	{
		ImageStack labelImage = componentsLabeling(image, 6, 16);
		ImageStack result = binarize(LabelImages.keepLargestLabel(labelImage));
		result.setColorModel(image.getColorModel());
		return result;
	}


	/**
	 * Returns a binary image in which the largest region has been replaced by
	 * background. Works for both 2D and 3D images.
	 * 
	 * @param imagePlus
	 *            a binary image containing several individual particles
	 */
	public static final void removeLargestRegion(ImagePlus imagePlus) 
	{
		// Dispatch to appropriate function depending on dimension
		if (imagePlus.getStackSize() == 1) 
		{
			imagePlus.setProcessor(removeLargestRegion(imagePlus.getProcessor()));
		} 
		else 
		{
			imagePlus.setStack(removeLargestRegion(imagePlus.getStack()));
		}
		
	}

	/**
	 * Returns a binary image in which the largest region has been replaced by
	 * the background value.
	 * 
	 * @param image
	 *            a binary image containing several individual particles
	 * @return a new binary image containing all the regions from original image
	 *         but the largest one
	 */
	public static final ImageProcessor removeLargestRegion(ImageProcessor image) 
	{
		ImageProcessor labelImage = componentsLabeling(image, 4, 16);
		LabelImages.removeLargestLabel(labelImage);
		ImageProcessor result = binarize(labelImage);
		result.setLut(image.getLut());
		return result;

	}

	/**
	 * Returns a binary image in which the largest region has been replaced by
	 * the background value.
	 * 
	 * @param image
	 *            a binary 3D image containing several individual particles
	 * @return a new binary image containing all the regions from original image
	 *         but the largest one
	 */
	public static final ImageStack removeLargestRegion(ImageStack image) 
	{
		ImageStack labelImage = componentsLabeling(image, 6, 16);
		LabelImages.removeLargestLabel(labelImage);
		ImageStack result = binarize(labelImage);
		result.setColorModel(image.getColorModel());
		return result;
	}
	
	/**
	 * Converts a grayscale 2D or 3D image into a binary image by setting
	 * non-zero elements to 255.
	 * 
	 * @param imagePlus
	 *            an ImagePlus object containing a gray scale image
	 * @return a binary image containing 255 for all non-zero elements of
	 *         original image
	 */
	public static final ImagePlus binarize(ImagePlus imagePlus) 
	{
		// Dispatch to appropriate function depending on dimension
		ImagePlus resultPlus;
		String title = imagePlus.getShortTitle() + "-bin";
		if (imagePlus.getStackSize() == 1)
		{
			ImageProcessor result = binarize(imagePlus.getProcessor());
			resultPlus = new ImagePlus(title, result);
		}
		else 
		{
			ImageStack result = binarize(imagePlus.getStack());
			resultPlus = new ImagePlus(title, result);
		}
		return resultPlus;
	}

	/**
	 * Converts a grayscale 2D image into a binary 2D image by setting non-zero
	 * pixels to 255.
	 * 
	 * @param image
	 *            a gray scale image
	 * @return a binary image containing 255 for all non-zero elements of
	 *         original image
	 	 */
	public static final ImageProcessor binarize(ImageProcessor image) 
	{
		int width = image.getWidth();
		int height = image.getHeight();
		ImageProcessor result = new ByteProcessor(width, height);
		
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++) 
			{
				if (image.get(x, y) > 0) 
					result.set(x, y, 255);
			}
		}
		
		return result;
	}
	
	/**
	 * Converts a grayscale 3D image into a binary 3D image by setting non-zero
	 * voxels to 255.
	 * 
	 * @param image
	 *            a gray scale 3D image
	 * @return a binary image containing 255 for all non-zero elements of
	 *         original image
	 */
	public static final ImageStack binarize(ImageStack image)
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		
		for (int z = 0; z < sizeZ; z++)
		{
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					if (image.getVoxel(x, y, z) > 0) 
						result.setVoxel(x, y, z, 255);
				}
			}
		}
		
		return result;
	}
}
