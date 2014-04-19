package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

import inra.ijpb.binary.geodesic.GeodesicDistanceMap;
import inra.ijpb.binary.geodesic.GeodesicDistanceMapFloat;
import inra.ijpb.binary.geodesic.GeodesicDistanceMapFloat5x5;
import inra.ijpb.binary.geodesic.GeodesicDistanceMapShort;
import inra.ijpb.binary.geodesic.GeodesicDistanceMapShort5x5;
import inra.ijpb.binary.geodesic.GeodesicDistanceMap.Weights;

/**
 * Plugin for computing geodesic distances using chamfer weights.
 * @author dlegland
 *
 */
public class GeodesicDistanceMapPlugin implements PlugIn {

	
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
		for (int i=0; i<indices.length; i++) {
			imageNames[i] = WindowManager.getImage(indices[i]).getTitle();
		}
		
		// create the dialog
		GenericDialog gd = new GenericDialog("Geodesic Distance Map");
		
		gd.addChoice("Mask Image", imageNames, IJ.getImage().getTitle());
		gd.addChoice("Marker Image", imageNames, IJ.getImage().getTitle());
		// Set Chessknight weights as default
		gd.addChoice("Distances", Weights.getAllLabels(), 
    			Weights.CHESSKNIGHT.toString());
		String[] outputTypes = new String[]{"32 bits", "16 bits"};
		gd.addChoice("Output Type", outputTypes, outputTypes[0]);
		gd.addCheckbox("Normalize weights", true);	
		
		gd.showDialog();
		
		if (gd.wasCanceled())
			return;
		
		// set up current parameters
		int maskImageIndex = gd.getNextChoiceIndex();
		ImagePlus maskImage = WindowManager.getImage(maskImageIndex + 1);
		int markerImageIndex = gd.getNextChoiceIndex();
		ImagePlus markerImage = WindowManager.getImage(markerImageIndex + 1);
		String weightLabel = gd.getNextChoice();
		// identify which weights should be used
    	Weights weights = Weights.fromLabel(weightLabel);
//		float[] weights = weightValues[weightIndex];
		boolean resultAsFloat = gd.getNextChoiceIndex() == 0;
		boolean normalizeWeights = gd.getNextBoolean();

		// check image types
		if (maskImage.getType() != ImagePlus.GRAY8) {
			IJ.showMessage("Mask image should be binary");
			return;
		}
		if (markerImage.getType() != ImagePlus.GRAY8) {
			IJ.showMessage("Marker image should be binary");
			return;
		}
		
		// Execute core of the plugin
		
		// init default weights
		
		String newName = createResultImageName(maskImage);
		Object[] res;
    	if (resultAsFloat) {
    		res = exec(maskImage, markerImage, newName, weights.getFloatWeights(), normalizeWeights);
		} else {
			res = exec(maskImage, markerImage, newName, weights.getShortWeights(), normalizeWeights);
		}

		// show new image if needed
		if (res!=null) {
			ImagePlus image = (ImagePlus) res[1];
			image.show();
		}
	}
		
	public Object[] exec(ImagePlus mask, ImagePlus marker, String newName, float[] weights) {
		return exec(mask, marker, newName, weights, true);
	}
	
	/**
	 * Computes the distance propagated from the boundary of the white
	 * particles, within the black phase.
	 */
	public Object[] exec(ImagePlus mask, ImagePlus marker, String newName,
			float[] weights, boolean normalize) {
		// Check validity of parameters
		if (mask == null) {
			throw new IllegalArgumentException("Mask image not specified");
		}
		if (marker == null) {
			throw new IllegalArgumentException("Marker image not specified");
		}
		if (newName == null)
			newName = createResultImageName(mask);
		if (weights == null) {
			throw new IllegalArgumentException("Weights not specified");
		}
	
		// size of image
		int width 	= mask.getWidth();
		int height 	= mask.getHeight();
		
		// check input and mask have the same size
		if (marker.getWidth() != width || marker.getHeight() != height) {
			IJ.showMessage("Error",
					"Input and marker images\nshould have the same size");
			return null;
		}
		
		// Initialize calculator
		GeodesicDistanceMap calc;
		if (weights.length == 2) {
			calc = new GeodesicDistanceMapFloat(weights, normalize);
		} else {
			calc = new GeodesicDistanceMapFloat5x5(weights, normalize);
		}
		
		// Compute distance on specified images
		ImagePlus result = calc.geodesicDistanceMap(mask, marker, newName);
				
		// create result array
		return new Object[]{newName, result};
	}
	
	/**
	 * Computes the distance propagated from the boundary of the white
	 * particles, within the black phase.
	 */
	public Object[] exec(ImagePlus mask, ImagePlus marker, String newName,
			short[] weights, boolean normalize) {
		// Check validity of parameters
		if (mask == null) {
			throw new IllegalArgumentException("Mask image not specified");
		}
		if (marker == null) {
			throw new IllegalArgumentException("Marker image not specified");
		}
		if (newName == null)
			newName = createResultImageName(mask);
		if (weights == null) {
			throw new IllegalArgumentException("Weights not specified");
		}
	
		// size of image
		int width 	= mask.getWidth();
		int height 	= mask.getHeight();
		
		// check input and mask have the same size
		if (marker.getWidth() != width || marker.getHeight() != height) {
			IJ.showMessage("Error",
					"Input and marker images\nshould have the same size");
			return null;
		}
		
		// Initialize calculator
		GeodesicDistanceMap calc;
		if (weights.length == 2) {
			calc = new GeodesicDistanceMapShort(weights, normalize);
		} else {
			calc = new GeodesicDistanceMapShort5x5(weights, normalize);
		}
		
		// Compute distance on specified images
		ImagePlus result = calc.geodesicDistanceMap(mask, marker, newName);
				
		// create result array
		return new Object[]{newName, result};
	}
	
	private static String createResultImageName(ImagePlus baseImage) {
		return baseImage.getShortTitle() + "-dist";
	}
}
