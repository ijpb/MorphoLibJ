/**
 * 
 */
package inra.ijpb.binary;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.LabelImages;

/**
 * A collection of static methods for operating on binary images (2D/3D).
 * Some of the methods need the LabelImages class. 
 * 
 * @author David Legland
 *
 */
public class BinaryImages {

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
		return binarize(LabelImages.keepLargestLabel(labelImage));
	}
	
	/**
	 * Returns a binary image that contains only the largest label.
	 * @param image a binary image
	 */
	public static final ImageStack keepLargestRegion(ImageStack image) {
		ImageStack labelImage = ConnectedComponents.computeLabels(image, 6, 16);
		return binarize(LabelImages.keepLargestLabel(labelImage));
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
		return binarize(labelImage);
	}

	/**
	 * Returns a binary image in which the largest region has been replaced by
	 * the background value.
	 */
	public static final ImageStack removeLargestRegion(ImageStack image) {
		ImageStack labelImage = ConnectedComponents.computeLabels(image, 6, 16);
		LabelImages.removeLargestLabel(labelImage);
		return binarize(labelImage);
	}
	
	/**
	 * Converts a grayscale 2D image into a binary 2D image.
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
	 * Converts a grayscale 3D image into a binary 3D image.
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
