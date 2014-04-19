/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import inra.ijpb.binary.geodesic.TortuosityShort;

/**
 * @author David Legland
 *
 */
public class TortuosityMapPlugin implements PlugIn {

	// ====================================================
	// Global Constants

	/**
	 * List of available distance pairs
	 */
	public final static String[] weightNames = {
			"Chessboard (1,1)", 
			"City-Block (1,2)", 
			"Quasi-Euclidean (1,1.41)", 
			"Borgefors (3,4)", 
			"Distance 2-3 (2,3)", 
			"Distance 5-7 (5,7)", 
	}; 
	
	/**
	 *  Array of weights, in the same order than the array of names.
	 */
	public final static float[][] floatWeights = {
			{1f, 1f}, 
			{1f, 2f}, 
			{1f, (float)Math.sqrt(2)}, 
			{3f, 4f}, 
			{2f, 3f}, 
			{5f, 7f}, 
	};
	
	/**
	 *  Array of weights, in the same order than the array of names.
	 */
	public final static short[][] shortWeights = {
			{1, 1}, 
			{1, 2}, 
			{10, 14}, // this one is only approximate
			{3, 4}, 
			{2, 3}, 
			{5, 7}, 
	};
	
	
	// ====================================================
	// Plugin interface

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg0) {
		
		// Open a dialog to choose:
		// - mask image
		// - marker image
		// - set of weights
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
		
		// create the dialog
		GenericDialog gd = new GenericDialog("Tortuosity Map");
		
		gd.addChoice("Mask Image", imageNames, IJ.getImage().getTitle());
		 // Set Borgefors weights as default
		gd.addChoice("Distances", weightNames, weightNames[3]);	
		gd.showDialog();
		
		if (gd.wasCanceled())
			return;
		
		// set up current parameters
		int imageIndex = gd.getNextChoiceIndex();
		ImagePlus image = WindowManager.getImage(imageIndex + 1);
		int weightIndex = gd.getNextChoiceIndex();
		short[] weights = shortWeights[weightIndex];

		// check image types
		if (image.getType() != ImagePlus.GRAY8) {
			IJ.showMessage("Mask image should be binary");
			return;
		}
		
		// Execute core of the plugin
		
//		// create default mask
//		ImagePlus marker = createMarkerImageFromMask(mask);
//		
		// init default weights
		
		String newName = createResultImageName(image);
		Object[] res = exec(image, newName, weights);
		
		// show new image if needed
		if (res != null) {
			ImagePlus map = (ImagePlus) res[1];
			map.show();
		}
	}

	public Object[] exec(ImagePlus image, String newName, short[] weights) {
		// Compute map on image processor
		ImageProcessor tortMap = exec(image.getProcessor(), weights);
		
		// Create result image plus
		ImagePlus result = new ImagePlus(newName, tortMap);
				
		// create result array
		return new Object[]{newName, result};
	}
	
	/**
	 * Compute geodesic length of particles, using imageProcessor as input and
	 * weights as array of float
	 */
	public ImageProcessor exec(ImageProcessor image, short[] weights) {
		TortuosityShort tortCalc = new TortuosityShort(weights);
		return tortCalc.tortuosityMap(image);
	}


	private static String createResultImageName(ImagePlus baseImage) {
		return baseImage.getShortTitle() + "-tortuosity";
	}
	
}
