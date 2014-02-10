package inra.ijpb.binary;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.Color;

/**
 * 
 */

/**
 * @author David Legland
 *
 */
public class BinaryOverlayPlugin implements PlugIn {
	
	// ====================================================
	// Global Constants
	
	/**
	 * List of available color names
	 */
	public final static String[] colorNames = {
			"Red", 
			"Green", 
			"Blue", 
			"Cyan", 
			"Magenta", 
			"Yellow", 
			"White", 
			"Black", 
	}; 
	
	/**
	 * List of colors
	 */
	public final static Color[] colors = {
		Color.RED, 
		Color.GREEN, 
		Color.BLUE, 
		Color.CYAN, 
		Color.MAGENTA, 
		Color.YELLOW, 
		Color.WHITE, 
		Color.BLACK, 
	};
	

	// ====================================================
	// Calling functions 
	
	public void run(String arg) {
		// Open a dialog to choose:
		// - a reference image (grayscale, binary, or color)
		// - a binary image (coded as uint8)
		// - a target color
		int[] indices = WindowManager.getIDList();
		if (indices==null) {
			IJ.error("No image", "Need at least one image to work");
			return;
		}
		
		// create the list of image names
		String[] imageNames = new String[indices.length];
		for (int i = 0; i < indices.length; i++) {
			imageNames[i] = WindowManager.getImage(indices[i]).getTitle();
		}
		
		// name of selected image
		String selectedImageName = IJ.getImage().getTitle();
		
		// create the dialog
		GenericDialog gd = new GenericDialog("Binary Overlay");
		gd.addChoice("Reference Image:", imageNames, selectedImageName);
		gd.addChoice("Binary Mask:", imageNames, selectedImageName);
		gd.addChoice("Overlay Color:", colorNames, colorNames[0]);
		
		gd.showDialog();
		
		if (gd.wasCanceled())
			return;
		
		// Extract reference image
		int refImageIndex = gd.getNextChoiceIndex();
		ImagePlus refImage = WindowManager.getImage(refImageIndex+1);
		
		// Extract mask image
		int maskIndex = gd.getNextChoiceIndex();
		ImagePlus maskImage = WindowManager.getImage(maskIndex+1);

		// Extract overlay color
		int colorIndex = gd.getNextChoiceIndex();
		Color color = colors[colorIndex];
		
		// Control reference image type
		int type = refImage.getType();
		if (type != ImagePlus.GRAY8 && type != ImagePlus.COLOR_RGB) {
			IJ.showMessage("Input image should be \neither a GRAY8 image\n or a color image.");
			return;
		}
		
		ImagePlus resultPlus = binaryOverlay(refImage, maskImage, color);
		resultPlus.show();
		
		if (refImage.getStackSize() > 1) {
			resultPlus.setSlice(refImage.getSlice());
		}
	}

	public Object[] exec(ImagePlus refImage, ImagePlus maskImage, 
			String resultName, Color overlayColor) {
		
		// Check validity of parameters
		if (refImage==null) {
			System.err.println("Reference image not specified");
			return null;
		}
		if (maskImage==null) {
			System.err.println("Mask image not specified");
			return null;
		}
		if (resultName==null) 
			resultName = createResultImageName(refImage);
		if (overlayColor==null) {
			System.err.println("Color not specified");
			return null;
		}
		
		// size of image
		int width 	= refImage.getWidth();
		int height 	= refImage.getHeight();
		
		// check input and mask have the same size
		if(maskImage.getWidth()!=width || maskImage.getHeight()!=height) {
			IJ.showMessage("Error", 
					"Input and mask images\nshould have the same size");
			return null;
		}

		ImageProcessor result;
		
		// Control reference image type
		int type = refImage.getType();
		if (type == ImagePlus.GRAY8) {
			// Call method for grayscale images
			result = createOverlayOverGray8(refImage.getProcessor(),
					maskImage.getProcessor(), overlayColor);
			
		} else if (type == ImagePlus.COLOR_RGB) {
			// Call method for RGB images
			result = createOverlayOverRGB(refImage.getProcessor(),
					maskImage.getProcessor(), overlayColor);
			
		} else {
			return null;
		}
		
		
		ImagePlus resultImage = new ImagePlus(resultName, result);
				
		// create result array
		return new Object[]{resultName, resultImage};
	}

	public final static ImagePlus binaryOverlay(ImagePlus imagePlus, 
			ImagePlus maskPlus, Color color) {
		
		String newName = createResultImageName(imagePlus);
		
		if (imagePlus.getStackSize() == 1) {
			ImageProcessor image = imagePlus.getProcessor();
			ImageProcessor mask = maskPlus.getProcessor();
			ImageProcessor result = binaryOverlay(image, mask, color);
			return new ImagePlus(newName, result);
		} else {
			ImageStack image = imagePlus.getStack();
			ImageStack mask = maskPlus.getStack();
			ImageStack result = binaryOverlay(image, mask, color);
			return new ImagePlus(newName, result);
		}
	}
	
	public final static ImageProcessor binaryOverlay(ImageProcessor refImage, 
			ImageProcessor mask, Color color) {
		if (refImage instanceof ColorProcessor) 
			return createOverlayOverRGB(refImage, mask, color);
		else
			return createOverlayOverGray8(refImage, mask, color);
	}
	
	public final static ImageStack binaryOverlay(ImageStack refImage, 
			ImageStack mask, Color color) {
		int sizeX = refImage.getWidth(); 
		int sizeY = refImage.getHeight(); 
		int sizeZ = refImage.getSize();
		
		int bitDepth = refImage.getBitDepth();
		
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 24);

		int value;
		int rgbValue = color.getRGB();
		
		// Iterate on image voxels, and choose result value depending on mask
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					if (mask.getVoxel(x, y, z) > 0) {
						// Apply the color code of chosen color
						result.setVoxel(x, y, z, rgbValue);
						continue;
					}
					
					if (bitDepth == 8 || bitDepth == 16 || bitDepth == 32) {
						// convert grayscale to equivalent color
						value = (int) refImage.getVoxel(x, y, z);
						value = (value & 0x00FF) << 16 | (value & 0x00FF) << 8 | (value & 0x00FF);
						result.setVoxel(x, y, z, value);
					} else if (bitDepth == 24) {
						// directly copy color code (after double conversion through double...)
						result.setVoxel(x, y, z, refImage.getVoxel(x, y, z));
					} 
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Assumes reference image contains a GRAY Processor.
	 */
	private final static ImageProcessor createOverlayOverGray8(ImageProcessor refImage, 
			ImageProcessor mask, Color color) {
		
		int width = refImage.getWidth(); 
		int height = refImage.getHeight(); 
		ColorProcessor result = new ColorProcessor(width, height);
		
		int value;
		int rgbValue = color.getRGB();
		
		// Iterate on image pixels, and choose result value depending on mask
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if(mask.get(x, y) == 0) {
					// choose value from reference image
					value = refImage.get(x, y);
					// convert grayscale to equivalent color
					value = (value & 0x00FF) << 16 | (value & 0x00FF) << 8 | (value & 0x00FF);
					result.set(x, y, value);

				} else {
					// set value to chosen color
					result.set(x, y, rgbValue);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Assumes reference image contains a ColorProcessor.
	 */
	private final static ImageProcessor createOverlayOverRGB(ImageProcessor refImage, 
			ImageProcessor mask, Color color) {
		
		int width = refImage.getWidth(); 
		int height = refImage.getHeight(); 
		ColorProcessor result = new ColorProcessor(width, height);
		
		int value;
		int rgbValue = color.getRGB();
		
		// Iterate on image pixels, and choose result value depending on mask
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if(mask.get(x, y) == 0) {
					// choose RGB value directly from reference image
					value = refImage.get(x, y);
					result.set(x, y, value);

				} else {
					// set value to chosen color
					result.set(x, y, rgbValue);
				}
			}
		}
		
		return result;
	}
	
	private static String createResultImageName(ImagePlus baseImage) {
		return baseImage.getShortTitle()+"-ovr";
	}
}
