/**
 * 
 */
package inra.ijpb.binary;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.binary.distmap.DistanceTransform;
import inra.ijpb.binary.distmap.DistanceTransform3D;
import inra.ijpb.binary.distmap.DistanceTransform3DFloat;
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
	 * Computes the distance map from a binary image processor. Distance is
	 * computed for each foreground (white) pixel, as the chamfer distance to
	 * the nearest background (black) pixel. This method uses default 5x5
	 * weights, and normalizes the resulting map. Result is given in a new
	 * instance of ShortProcessor.
	 */
	public static final ImageProcessor distanceMap(ImageProcessor image) 
	{
		return distanceMap(image, new short[]{5, 7, 11}, true);
	}
	
	/**
	 * Computes the distance map from a binary image processor, by specifying
	 * weights and normalization.
	 * 
	 * Distance is computed for each foreground (white) pixel, as the chamfer
	 * distance to the nearest background (black) pixel. Result is given as a
	 * new instance of ShortProcessor.
	 */
	public static final ImageProcessor distanceMap(ImageProcessor image,
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

		return algo.distanceMap(image);
	}

	/**
	 * Computes the distance map from a binary image processor, by specifying
	 * weights and normalization. 
	 * 
	 * Distance is computed for each foreground (white) pixel, as the 
	 * chamfer distance to the nearest background (black) pixel.
	 * Result is given in a new instance of FloatProcessor.
	 */
	public static final ImageProcessor distanceMap(ImageProcessor image,
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
		
		return algo.distanceMap(image);
	}

	/**
	 * Computes the distance map from a binary 3D image. 
	 * Distance is computed for each foreground (white) pixel, as the 
	 * chamfer distance to the nearest background (black) pixel.
	 */
	public static final ImageStack distanceMap(ImageStack image)
	{
		float[] weights = new float[]{3.0f, 4.0f, 5.0f};
		DistanceTransform3D algo = new DistanceTransform3DFloat(weights);
		return algo.distanceMap(image);
	}
	
	/**
	 * Computes the geodesic distance transform (or geodesic distance map) of a
	 * binary image of marker, constrained to a binary mask.
	 * Returns the result in a new instance of ShortProcessor.
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
	 * @see inra.ijpb.label.LabelImages#areaOpening(ImageProcessor, int)
	 */
	public static final ImageProcessor areaOpening(ImageProcessor image,
			int nPixelMin) 
	{
		// Labeling
		ImageProcessor labelImage = ConnectedComponents.computeLabels(image, 4, 16);

		// keep only necessary labels and binarize
		return binarize(LabelImages.areaOpening(labelImage, nPixelMin));
	}
	
	
	/**
	 * Applies area opening on a binary image: creates a new binary image that
	 * contains only particle with at least the specified number of voxels.
	 *
	 * @see inra.ijpb.label.LabelImages#volumeOpening(ImageStack, int)
	 */
	public static final ImageStack volumeOpening(ImageStack image, int nVoxelMin) 
	{
		// Labeling
		ImageStack labelImage = ConnectedComponents.computeLabels(image, 6, 16);

		// keep only necessary labels and binarize
		return binarize(LabelImages.volumeOpening(labelImage, nVoxelMin));
	}
	
	
	/**
	 * Returns a binary image that contains only the largest region.
	 * 
	 * @param imagePlus an instance of imagePlus that contains a binary image
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
	 * @param image a binary image
	 */
	public static final ImageProcessor keepLargestRegion(ImageProcessor image) 
	{
		ImageProcessor labelImage = ConnectedComponents.computeLabels(image, 4, 16);
		ImageProcessor result = binarize(LabelImages.keepLargestLabel(labelImage));
		result.setLut(image.getLut());
		return result;
	}
	
	/**
	 * Returns a binary image that contains only the largest label.
	 * @param image a binary image
	 */
	public static final ImageStack keepLargestRegion(ImageStack image) 
	{
		ImageStack labelImage = ConnectedComponents.computeLabels(image, 6, 16);
		ImageStack result = binarize(LabelImages.keepLargestLabel(labelImage));
		result.setColorModel(image.getColorModel());
		return result;
	}


	/**
	 * Returns a binary image in which the largest region has been replaced by
	 * background. Works for both 2D and 3D images.
	 * 
	 * @param imagePlus an instance of imagePlus that contains a binary image
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
	 */
	public static final ImageProcessor removeLargestRegion(ImageProcessor image) 
	{
		ImageProcessor labelImage = ConnectedComponents.computeLabels(image, 4, 16);
		LabelImages.removeLargestLabel(labelImage);
		ImageProcessor result = binarize(labelImage);
		result.setLut(image.getLut());
		return result;

	}

	/**
	 * Returns a binary image in which the largest region has been replaced by
	 * the background value.
	 */
	public static final ImageStack removeLargestRegion(ImageStack image) 
	{
		ImageStack labelImage = ConnectedComponents.computeLabels(image, 6, 16);
		LabelImages.removeLargestLabel(labelImage);
		ImageStack result = binarize(labelImage);
		result.setColorModel(image.getColorModel());
		return result;
	}
	
	/**
	 * Converts a grayscale 2D or 3D image into a binary image by setting 
	 * non-zero elements to 255.
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
