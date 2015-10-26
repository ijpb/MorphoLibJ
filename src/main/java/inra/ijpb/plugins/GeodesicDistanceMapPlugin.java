package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import inra.ijpb.binary.ChamferWeights;
import inra.ijpb.binary.geodesic.GeodesicDistanceMap;
import inra.ijpb.binary.geodesic.GeodesicDistanceMapFloat;
import inra.ijpb.binary.geodesic.GeodesicDistanceMapFloat5x5;
import inra.ijpb.binary.geodesic.GeodesicDistanceMapShort;
import inra.ijpb.binary.geodesic.GeodesicDistanceMapShort5x5;

/**
 * Plugin for computing geodesic distance map from binary images using chamfer weights.
 * 
 * @author dlegland
 *
 */
public class GeodesicDistanceMapPlugin implements PlugIn
{
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg0) 
	{
		// Open a dialog to choose:
		// - marker image
		// - mask image
		// - set of weights
		int[] indices = WindowManager.getIDList();
		if (indices==null) {
			IJ.error("No image", "Need at least one image to work");
			return;
		}
		
		// create the list of image names
		String[] imageNames = new String[indices.length];
		for (int i=0; i<indices.length; i++) 
		{
			imageNames[i] = WindowManager.getImage(indices[i]).getTitle();
		}
		
		// create the dialog
		GenericDialog gd = new GenericDialog("Geodesic Distance Map");
		
		gd.addChoice("Marker Image", imageNames, IJ.getImage().getTitle());
		gd.addChoice("Mask Image", imageNames, IJ.getImage().getTitle());
		// Set Chessknight weights as default
		gd.addChoice("Distances", ChamferWeights.getAllLabels(), 
				ChamferWeights.CHESSKNIGHT.toString());
		String[] outputTypes = new String[]{"32 bits", "16 bits"};
		gd.addChoice("Output Type", outputTypes, outputTypes[0]);
		gd.addCheckbox("Normalize weights", true);	
		
		gd.showDialog();
		
		if (gd.wasCanceled())
			return;
		
		// set up current parameters
		int markerImageIndex = gd.getNextChoiceIndex();
		ImagePlus markerImage = WindowManager.getImage(markerImageIndex + 1);
		int maskImageIndex = gd.getNextChoiceIndex();
		ImagePlus maskImage = WindowManager.getImage(maskImageIndex + 1);
		String weightLabel = gd.getNextChoice();
		// identify which weights should be used
		ChamferWeights weights = ChamferWeights.fromLabel(weightLabel);
		boolean resultAsFloat = gd.getNextChoiceIndex() == 0;
		boolean normalizeWeights = gd.getNextBoolean();

		// check image types
		if (markerImage.getType() != ImagePlus.GRAY8) 
		{
			IJ.showMessage("Marker image should be binary");
			return;
		}
		if (maskImage.getType() != ImagePlus.GRAY8) 
		{
			IJ.showMessage("Mask image should be binary");
			return;
		}
		
		// Execute core of the plugin
		String newName = createResultImageName(maskImage);
		Object[] res;
    	if (resultAsFloat) 
    	{
    		res = exec(markerImage, maskImage, newName, weights.getFloatWeights(), normalizeWeights);
		}
    	else
		{
			res = exec(markerImage, maskImage, newName, weights.getShortWeights(), normalizeWeights);
		}

		// show new image if needed
		if (res!=null) 
		{
			ImagePlus image = (ImagePlus) res[1];
			image.show();
		}
	}
		
	public Object[] exec(ImagePlus marker, ImagePlus mask, String newName, float[] weights) 
	{
		return exec(marker, mask, newName, weights, true);
	}
	
	/**
	 * Computes the distance propagated from the boundary of the white
	 * particles, within the black phase.
	 */
	public Object[] exec(ImagePlus marker, ImagePlus mask, String newName,
			float[] weights, boolean normalize) 
	{
		// Check validity of parameters
		if (marker == null) 
		{
			throw new IllegalArgumentException("Marker image not specified");
		}
		if (mask == null) 
		{
			throw new IllegalArgumentException("Mask image not specified");
		}
		if (newName == null)
			newName = createResultImageName(mask);
		if (weights == null)
		{
			throw new IllegalArgumentException("Weights not specified");
		}
	
		// size of image
		int width 	= mask.getWidth();
		int height 	= mask.getHeight();
		
		// check input and mask have the same size
		if (marker.getWidth() != width || marker.getHeight() != height) 
		{
			IJ.showMessage("Error",
					"Input and marker images\nshould have the same size");
			return null;
		}
		
		// Initialize calculator
		GeodesicDistanceMap calc;
		if (weights.length == 2) 
		{
			calc = new GeodesicDistanceMapFloat(weights, normalize);
		} 
		else
		{
			calc = new GeodesicDistanceMapFloat5x5(weights, normalize);
		}
		
		// Compute distance on specified images
		ImageProcessor result = calc.geodesicDistanceMap(marker.getProcessor(), mask.getProcessor());
		ImagePlus resultPlus = new ImagePlus(newName, result);
				
		// create result array
		return new Object[]{newName, resultPlus};
	}
	
	/**
	 * Computes the distance propagated from the boundary of the white
	 * particles, within the black phase.
	 */
	public Object[] exec(ImagePlus marker, ImagePlus mask, String newName,
			short[] weights, boolean normalize) 
	{
		// Check validity of parameters
		if (marker == null) 
		{
			throw new IllegalArgumentException("Marker image not specified");
		}
		if (mask == null)
		{
			throw new IllegalArgumentException("Mask image not specified");
		}
		if (newName == null)
			newName = createResultImageName(mask);
		if (weights == null) 
		{
			throw new IllegalArgumentException("Weights not specified");
		}
	
		// size of image
		int width 	= mask.getWidth();
		int height 	= mask.getHeight();
		
		// check input and mask have the same size
		if (marker.getWidth() != width || marker.getHeight() != height) 
		{
			IJ.showMessage("Error",
					"Input and marker images\nshould have the same size");
			return null;
		}
		
		// Initialize calculator
		GeodesicDistanceMap calc;
		if (weights.length == 2) 
		{
			calc = new GeodesicDistanceMapShort(weights, normalize);
		} 
		else
		{
			calc = new GeodesicDistanceMapShort5x5(weights, normalize);
		}
		
		// Compute distance on specified images
		ImageProcessor result = calc.geodesicDistanceMap(marker.getProcessor(), mask.getProcessor());
		ImagePlus resultPlus = new ImagePlus(newName, result);
				
		// create result array
		return new Object[]{newName, resultPlus};
	}
	
	private static String createResultImageName(ImagePlus baseImage)
	{
		return baseImage.getShortTitle() + "-geoddist";
	}
}
