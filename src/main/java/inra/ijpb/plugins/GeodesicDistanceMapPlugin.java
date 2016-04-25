package inra.ijpb.plugins;

import java.awt.image.IndexColorModel;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.process.LUT;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.binary.ChamferWeights;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransform;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransformFloat;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransformFloat5x5;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransformShort;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransformShort5x5;
import inra.ijpb.util.ColorMaps;

/**
 * Plugin for computing geodesic distance map from binary images using chamfer
 * weights.
 * 
 * @author dlegland
 *
 */
public class GeodesicDistanceMapPlugin implements PlugIn
{
	/*
	 * (non-Javadoc)
	 * 
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
		if (indices == null)
		{
			IJ.error("No image", "Need at least one image to work");
			return;
		}

		// create the list of image names
		String[] imageNames = new String[indices.length];
		for (int i = 0; i < indices.length; i++)
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
		String[] outputTypes = new String[] { "32 bits", "16 bits" };
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
		ImagePlus res;
		if (resultAsFloat)
		{
			res = process(markerImage, maskImage, newName,
					weights.getFloatWeights(), normalizeWeights);
		} else
		{
			res = process(markerImage, maskImage, newName,
					weights.getShortWeights(), normalizeWeights);
		}

		res.show();
	}

	@Deprecated
	public Object[] exec(ImagePlus marker, ImagePlus mask, String newName,
			float[] weights)
	{
		return exec(marker, mask, newName, weights, true);
	}

	/**
	 * Computes the distance propagated from the boundary of the white
	 * particles, within the white phase.
	 * 
	 * @param marker
	 *            the binary marker image from which distances will be
	 *            propagated
	 * @param mask
	 *            the binary mask image that will constrain the propagation
	 * @param newName
	 *            the name of the result image
	 * @param weights
	 *            the set of chamfer weights for computing distances
	 * @param normalize
	 *            specifies whether the resulting distance map should be
	 *            normalized
	 * @return an array of object, containing the name of the new image, and the
	 *         new ImagePlus instance
	 * @deprecated use process method instead
	 */
	@Deprecated
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
		int width = mask.getWidth();
		int height = mask.getHeight();

		// check input and mask have the same size
		if (marker.getWidth() != width || marker.getHeight() != height)
		{
			IJ.showMessage("Error",
					"Input and marker images\nshould have the same size");
			return null;
		}

		// Initialize calculator
		GeodesicDistanceTransform algo = chooseAlgo(weights, normalize);

		// Compute distance on specified images
		ImageProcessor result = algo.geodesicDistanceMap(marker.getProcessor(),
				mask.getProcessor());
		ImagePlus resultPlus = new ImagePlus(newName, result);

		// create result array
		return new Object[] { newName, resultPlus };
	}

	/**
	 * Computes the distance propagated from the boundary of the white
	 * particles, within the black phase.
	 * 
	 * @param marker
	 *            the binary marker image from which distances will be
	 *            propagated
	 * @param mask
	 *            the binary mask image that will constrain the propagation
	 * @param newName
	 *            the name of the result image
	 * @param weights
	 *            the set of chamfer weights for computing distances
	 * @param normalize
	 *            specifies whether the resulting distance map should be
	 *            normalized
	 * @return an array of object, containing the name of the new image, and the
	 *         new ImagePlus instance
	 * @deprecated use process method instead
	 */
	@Deprecated
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
		int width = mask.getWidth();
		int height = mask.getHeight();

		// check input and mask have the same size
		if (marker.getWidth() != width || marker.getHeight() != height)
		{
			IJ.showMessage("Error",
					"Input and marker images\nshould have the same size");
			return null;
		}

		// Initialize calculator
		GeodesicDistanceTransform algo;
		if (weights.length == 2)
		{
			algo = new GeodesicDistanceTransformShort(weights, normalize);
		} else
		{
			algo = new GeodesicDistanceTransformShort5x5(weights, normalize);
		}

		// Compute distance on specified images
		ImageProcessor result = algo.geodesicDistanceMap(marker.getProcessor(),
				mask.getProcessor());
		ImagePlus resultPlus = new ImagePlus(newName, result);

		// create result array
		return new Object[] { newName, resultPlus };
	}

	/**
	 * Computes the distance propagated from the boundary of the white
	 * particles, within the white phase.
	 * 
	 * @param markerPlus
	 *            the binary marker image from which distances will be
	 *            propagated
	 * @param maskPlus
	 *            the binary mask image that will constrain the propagation
	 * @param newName
	 *            the name of the result image
	 * @param weights
	 *            the set of chamfer weights for computing distances
	 * @param normalize
	 *            specifies whether the resulting distance map should be
	 *            normalized
	 * @return an array of object, containing the name of the new image, and the
	 *         new ImagePlus instance
	 */
	public ImagePlus process(ImagePlus markerPlus, ImagePlus maskPlus,
			String newName, float[] weights, boolean normalize)
	{
		// Check validity of parameters
		if (markerPlus == null)
		{
			throw new IllegalArgumentException("Marker image not specified");
		}
		if (maskPlus == null)
		{
			throw new IllegalArgumentException("Mask image not specified");
		}
		if (newName == null)
			newName = createResultImageName(maskPlus);
		if (weights == null)
		{
			throw new IllegalArgumentException("Weights not specified");
		}

		// size of image
		int width = maskPlus.getWidth();
		int height = maskPlus.getHeight();

		// check input and mask have the same size
		if (markerPlus.getWidth() != width || markerPlus.getHeight() != height)
		{
			IJ.showMessage("Error",
					"Input and marker images\nshould have the same size");
			return null;
		}

		// Initialize calculator
		GeodesicDistanceTransform algo = chooseAlgo(weights, normalize);
		DefaultAlgoListener.monitor(algo);
    	

		// Compute distance on specified images
		ImageProcessor marker = markerPlus.getProcessor();
		ImageProcessor mask = maskPlus.getProcessor();
		ImageProcessor result = algo.geodesicDistanceMap(marker, mask);
		ImagePlus resultPlus = new ImagePlus(newName, result);
		
		// setup display options
		double maxVal = result.getMax();
		resultPlus.setLut(createFireLUT(maxVal));
		resultPlus.setDisplayRange(0, maxVal);
		
		// return result image
		return resultPlus;
	}

	public ImageProcessor process(ImageProcessor marker, ImageProcessor mask,
			float[] weights, boolean normalize)
	{
		if (weights == null)
		{
			throw new IllegalArgumentException("Weights not specified");
		}

		// size of image
		int width = mask.getWidth();
		int height = mask.getHeight();

		// check input and mask have the same size
		if (marker.getWidth() != width || marker.getHeight() != height)
		{
			IJ.showMessage("Error",
					"Input and marker images\nshould have the same size");
			return null;
		}

		// Initialize calculator
		GeodesicDistanceTransform algo = chooseAlgo(weights, normalize);
		DefaultAlgoListener.monitor(algo);
    	

		// Compute distance on specified images
		ImageProcessor result = algo.geodesicDistanceMap(marker, mask);

		// create result image
		return result;
	}

	/**
	 * Computes the distance propagated from the boundary of the white
	 * particles, within the black phase.
	 * 
	 * @param marker
	 *            the binary marker image from which distances will be
	 *            propagated
	 * @param mask
	 *            the binary mask image that will constrain the propagation
	 * @param newName
	 *            the name of the result image
	 * @param weights
	 *            the set of chamfer weights for computing distances
	 * @param normalize
	 *            specifies whether the resulting distance map should be
	 *            normalized
	 * @return an array of object, containing the name of the new image, and the
	 *         new ImagePlus instance
	 */
	public ImagePlus process(ImagePlus marker, ImagePlus mask, String newName,
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
		int width = mask.getWidth();
		int height = mask.getHeight();

		// check input and mask have the same size
		if (marker.getWidth() != width || marker.getHeight() != height)
		{
			IJ.showMessage("Error",
					"Input and marker images\nshould have the same size");
			return null;
		}

		// Initialize calculator
		GeodesicDistanceTransform algo;
		if (weights.length == 2)
		{
			algo = new GeodesicDistanceTransformShort(weights, normalize);
		} else
		{
			algo = new GeodesicDistanceTransformShort5x5(weights, normalize);
		}
		DefaultAlgoListener.monitor(algo);

		// Compute distance on specified images
		ImageProcessor result = algo.geodesicDistanceMap(marker.getProcessor(),
				mask.getProcessor());
		ImagePlus resultPlus = new ImagePlus(newName, result);

		// setup display options
		double maxVal = result.getMax();
		resultPlus.setLut(createFireLUT(maxVal));
		resultPlus.setDisplayRange(0, maxVal);
		
		// create result array
		return resultPlus;
	}

	/**
	 * Choose the appropriate algorithm depending on the length of the weight
	 * array.
	 * 
	 * @param weights
	 *            the array of chamfer weights
	 * @param normalize
	 *            a boolean indicating whether the map should be normalized
	 * @return an instance of GeodesicDistanceTransform operator
	 */
	private GeodesicDistanceTransform chooseAlgo(float[] weights, boolean normalize)
	{
		if (weights.length == 2)
		{
			return new GeodesicDistanceTransformFloat(weights, normalize);
		} 
		else
		{
			return new GeodesicDistanceTransformFloat5x5(weights, normalize);
		}
	}
	
	private LUT createFireLUT(double maxVal)
	{
		byte[][] lut = ColorMaps.createFireLut(256);
		byte[] red = new byte[256];
		byte[] green = new byte[256];
		byte[] blue = new byte[256];
		for (int i = 0; i < 256; i++)
		{
			red[i] 		= lut[i][0];
			green[i] 	= lut[i][1];
			blue[i] 	= lut[i][2];
		}
		IndexColorModel cm = new IndexColorModel(8, 256, red, green, blue);
		return new LUT(cm, 0, maxVal);
	}
	
	private static String createResultImageName(ImagePlus baseImage)
	{
		return baseImage.getShortTitle() + "-geoddist";
	}
}
