/**
 * 
 */
package inra.ijpb.morphology;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.Color;

/**
 * Utility methods for label images (stored as 8-, 16- or 32-bits).
 * 
 * @author David Legland
 *
 */
public class LabelImages {
	/**
	 * Creates a new Color image from a label image, a LUT, and a
	 * color for background.
	 * 
	 * @param imagePlus a 2D or 3D image containing labels and 0 for background
	 * @param lut the array of color components for each label 
	 * @param bgColor the background color
	 * @return a new Color image
	 */
	public final static ImagePlus labelToRgb(ImagePlus imagePlus, byte[][] lut, Color bgColor) {
		ImagePlus resultPlus;
		String newName = imagePlus.getShortTitle() + "-rgb";
		
		// Dispatch to appropriate function depending on dimension
		if (imagePlus.getStackSize() == 1) {
			// process planar image
			ImageProcessor image = imagePlus.getProcessor();
			ImageProcessor result = labelToRgb(image, lut, bgColor);
			resultPlus = new ImagePlus(newName, result);
		} else {
			// process imaeg stack
			ImageStack image = imagePlus.getStack();
			ImageStack result = labelToRgb(image, lut, bgColor);
			resultPlus = new ImagePlus(newName, result);
		}
		
		resultPlus.copyScale(imagePlus);
		return resultPlus;
	}
	
	/**
	 * Creates a new Color image from a label planar image, a LUT, and a 
	 * color for background.
	 * 
	 * @param image an ImageProcessor with label values and 0 for background
	 * @param lut the array of color components for each label 
	 * @param bgColor the background color
	 * @return a new instance of ColorProcessor
	 */
	public final static ColorProcessor labelToRgb(ImageProcessor image, byte[][] lut, Color bgColor) {
		int width = image.getWidth();
		int height = image.getHeight();
		
		int bgColorCode = bgColor.getRGB();
		
		ColorProcessor result = new ColorProcessor(width, height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int index = image.get(x, y);
				if (index == 0) {
					result.set(x, y, bgColorCode);
				} else {
					byte[] rgb = lut[index - 1];
					int color = (int) ((rgb[0] & 0xFF) << 16
							| (rgb[1] & 0xFF) << 8 | (rgb[2] & 0xFF));
					result.set(x, y, color);
				}
			}
		}
		
		return result;
	}

	/**
	 * Creates a new Color image stack from a label image stack, a LUT, and a
	 * color for background.
	 * 
	 * @param image an ImageStack with label values and 0 for background
	 * @param lut the array of color components for each label 
	 * @param bgColor the background color
	 * @return a new instance of ImageStack containing color processors
	 */
	public final static ImageStack labelToRgb(ImageStack image, byte[][] lut, Color bgColor) {
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 24);
		
		int bgColorCode = bgColor.getRGB();
		
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					int index = (int) image.getVoxel(x, y, z);
					if (index == 0) {
						result.setVoxel(x, y, z, bgColorCode);
					} else {
						byte[] rgb = lut[index - 1];
						int color = (int) ((rgb[0] & 0xFF) << 16
								| (rgb[1] & 0xFF) << 8 | (rgb[2] & 0xFF));
						result.setVoxel(x, y, z, color);
					}
				}
			}
		}
		
		return result;
	}
}
