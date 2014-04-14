package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import inra.ijpb.data.image.ColorImages;

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
		
		ImagePlus resultPlus = ColorImages.binaryOverlay(refImage, maskImage, color);
		resultPlus.show();
		
		if (refImage.getStackSize() > 1) {
			resultPlus.setSlice(refImage.getSlice());
		}
	}
}
