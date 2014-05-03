/**
 * 
 */
package inra.ijpb.binary;

import java.util.ArrayList;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.binary.distmap.ChamferDistance;
import inra.ijpb.binary.distmap.ChamferDistance3x3Float;
import inra.ijpb.binary.distmap.ChamferDistance3x3Short;
import inra.ijpb.binary.distmap.ChamferDistance5x5Float;
import inra.ijpb.binary.distmap.ChamferDistance5x5Short;
import inra.ijpb.morphology.LabelImages;

/**
 * A collection of static methods for operating on binary images (2D/3D).
 * Some of the methods need the LabelImages class. 
 * 
 * @see inra.ijpb.morphology.LabelImages
 * 
 * @author David Legland
 *
 */
public class BinaryImages {

	/**
	 * Computes the distance map from a binary image processor. Distance is
	 * computed for each foreground (white) pixel, as the chamfer distance to
	 * the nearest background (black) pixel. This method uses default 5x5
	 * weights, and normalizes the resulting map. Result is given in a new
	 * instance of ShortProcessor.
	 */
	public static final ImageProcessor distanceMap(ImageProcessor image) {
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
		ChamferDistance algo;
		switch (weights.length) {
		case 2:
			algo = new ChamferDistance3x3Short(weights, normalize);
			break;
		case 3:
			algo = new ChamferDistance5x5Short(weights, normalize);
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
	public static final ImageProcessor distanceMap(ImageProcessor image, float[] weights, boolean normalize) {
		ChamferDistance algo;
		switch (weights.length) {
		case 2:
			algo = new ChamferDistance3x3Float(weights, normalize);
			break;
		case 3:
			algo = new ChamferDistance5x5Float(weights, normalize);
			break;
		default:
			throw new IllegalArgumentException(
					"Requires weight array with 2 or 3 elements");
		}
		
		return algo.distanceMap(image);
	}

	/**
	 * Applies area opening on a binary image: creates a new binary image that
	 * contains only particle with at least the specified number of pixels.
	 */
	public static final ImageProcessor areaOpening(ImageProcessor image, int nPixelMin) {
		// Labeling
		ImageProcessor labelImage = ConnectedComponents.computeLabels(image, 4, 16);
		
		// compute area of each label
		int[] labels = LabelImages.findAllLabels(labelImage);
		int[] areas = LabelImages.pixelCount(labelImage, labels);
		
		// find labels with sufficient area
		ArrayList<Integer> labelsToKeep = new ArrayList<Integer>(labels.length);
		for (int i = 0; i < labels.length; i++) {
			if (areas[i] >= nPixelMin) {
				labelsToKeep.add(labels[i]);
			}
		}
		
		// Convert array list into int array
		int[] labels2 = new int[labelsToKeep.size()];
		for (int i = 0; i < labelsToKeep.size(); i++) {
			labels2[i] = labelsToKeep.get(i);
		}
		
		// keep only necessary labels and binarize
		return binarize(LabelImages.keepLabels(labelImage, labels2));
	}
	
	
	/**
	 * Applies area opening on a binary image: creates a new binary image that
	 * contains only particle with at least the specified number of pixels.
	 */
	public static final ImageStack volumeOpening(ImageStack image, int nVoxelMin) {
		// Labeling
		ImageStack labelImage = ConnectedComponents.computeLabels(image, 6, 16);
		
		// compute area of each label
		int[] labels = LabelImages.findAllLabels(labelImage);
		int[] vols = LabelImages.voxelCount(labelImage, labels);
		
		// find labels with sufficient area
		ArrayList<Integer> labelsToKeep = new ArrayList<Integer>(labels.length);
		for (int i = 0; i < labels.length; i++) {
			if (vols[i] >= nVoxelMin) {
				labelsToKeep.add(labels[i]);
			}
		}
		
		// Convert array list into int array
		int[] labels2 = new int[labelsToKeep.size()];
		for (int i = 0; i < labelsToKeep.size(); i++) {
			labels2[i] = labelsToKeep.get(i);
		}
		
		// keep only necessary labels and binarize
		return binarize(LabelImages.keepLabels(labelImage, labels2));
	}
	
	
	/**
	 * Returns a binary image that contains only the largest region.
	 * 
	 * @param imagePlus an instance of imagePlus that contains a binary image
	 */
	public static final ImagePlus keepLargestRegion(ImagePlus imagePlus) {
		ImagePlus resultPlus;
		String newName = imagePlus.getShortTitle() + "-largest";
		
		// Dispatch to appropriate function depending on dimension
		if (imagePlus.getStackSize() == 1) {
			// process planar image
			ImageProcessor image = imagePlus.getProcessor();
			ImageProcessor result = keepLargestRegion(image);
			resultPlus = new ImagePlus(newName, result);
		} else {
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
	public static final ImageProcessor keepLargestRegion(ImageProcessor image) {
		ImageProcessor labelImage = ConnectedComponents.computeLabels(image, 4, 16);
		ImageProcessor result = binarize(LabelImages.keepLargestLabel(labelImage));
		result.setLut(image.getLut());
		return result;
	}
	
	/**
	 * Returns a binary image that contains only the largest label.
	 * @param image a binary image
	 */
	public static final ImageStack keepLargestRegion(ImageStack image) {
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
	public static final void removeLargestRegion(ImagePlus imagePlus) {
		// Dispatch to appropriate function depending on dimension
		if (imagePlus.getStackSize() == 1) {
			imagePlus.setProcessor(removeLargestRegion(imagePlus.getProcessor()));
		} else {
			imagePlus.setStack(removeLargestRegion(imagePlus.getStack()));
		}
		
	}

	/**
	 * Returns a binary image in which the largest region has been replaced by
	 * the background value.
	 */
	public static final ImageProcessor removeLargestRegion(ImageProcessor image) {
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
	public static final ImageStack removeLargestRegion(ImageStack image) {
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
	public static final ImagePlus binarize(ImagePlus imagePlus) {
		// Dispatch to appropriate function depending on dimension
		ImagePlus resultPlus;
		String title = imagePlus.getShortTitle() + "-bin";
		if (imagePlus.getStackSize() == 1) {
			ImageProcessor result = binarize(imagePlus.getProcessor());
			resultPlus = new ImagePlus(title, result);
		} else {
			ImageStack result = binarize(imagePlus.getStack());
			resultPlus = new ImagePlus(title, result);
		}
		return resultPlus;
	}

	/**
	 * Converts a grayscale 2D image into a binary 2D image by setting non-zero
	 * pixels to 255.
	 */
	public static final ImageProcessor binarize(ImageProcessor image) {
		int width = image.getWidth();
		int height = image.getHeight();
		ImageProcessor result = new ByteProcessor(width, height);
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
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
	public static final ImageStack binarize(ImageStack image) {
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					if (image.getVoxel(x, y, z) > 0) 
						result.setVoxel(x, y, z, 255);
				}
			}
		}
		
		return result;
	}

}
