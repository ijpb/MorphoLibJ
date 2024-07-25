/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.process.LUT;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.binary.distmap.ChamferMask2D;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransform;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransformFloat;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransformFloatHybrid;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransformShortHybrid;
import inra.ijpb.color.ColorMaps;
import inra.ijpb.plugins.options.ChamferMask2DOption;
import inra.ijpb.util.IJUtils;

import java.awt.image.IndexColorModel;

/**
 * Plugin for computing geodesic distance map from binary marker and constrained
 * to binary or label images using chamfer distances propagation.
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
        // create the list of image names
        String[] imageNames = IJUtils.getOpenImageNames();
        if (imageNames.length == 0)
        {
            IJ.error("No image", "Need at least one image to work");
            return;
        }
        String currentImageName = IJ.getImage().getTitle();

        ChamferMask2DOption maskOption = new ChamferMask2DOption(ChamferMask2D.CHESSKNIGHT);
        
		// Open a dialog to choose:
		// - marker image
		// - mask image
		// - set of weights
		GenericDialog gd = new GenericDialog("Geodesic Distance Map");
		gd.addChoice("Marker Image", imageNames, currentImageName);
		gd.addChoice("Mask Image", imageNames, currentImageName);
		// Set Chessknight weights as default
		maskOption.populateDialog(gd, "Distances");
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
		
		// identify which weights should be used
		ChamferMask2D chamferMask = maskOption.parseValue(gd);
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
        long t0 = System.currentTimeMillis();
		ImagePlus res = process(markerImage, maskImage, newName,
				chamferMask, resultAsFloat, normalizeWeights);
        long t1 = System.currentTimeMillis();

		res.show();
		
        // Display elapsed time
        IJUtils.showElapsedTime("distance map", t1 - t0, markerImage);
	}

	/**
	 * Computes the distance within the mask, starting from the marker image.
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
	 * @return the geodesic distance map
	 */
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

		// Compute distance on specified images
		ImagePlus resultPlus = BinaryImages.geodesicDistanceMap(marker, mask);

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

		// Compute distance on specified images
		ImageProcessor result = BinaryImages.geodesicDistanceMap(marker.getProcessor(), mask.getProcessor(), weights, normalize);
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
	@Deprecated
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
		GeodesicDistanceTransform algo = new GeodesicDistanceTransformFloat(ChamferMask2D.fromWeights(weights), normalize);
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

	/**
	 * Computes the distance within the mask, starting from the marker image.
	 * 
	 * @param marker
	 *            the binary marker image from which distances will be
	 *            propagated
	 * @param mask
	 *            the binary mask image that will constrain the propagation
	 * @param weights
	 *            the set of chamfer weights for computing distances
	 * @param normalize
	 *            specifies whether the resulting distance map should be
	 *            normalized
	 * @return the geodesic distance map
	 */
	@Deprecated
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
		GeodesicDistanceTransform algo = new GeodesicDistanceTransformFloat(ChamferMask2D.fromWeights(weights), normalize);
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
	 * @param floatComputation
	 *            specifies whether floating point computation should be used
	 * @param normalize
	 *            specifies whether the resulting distance map should be
	 *            normalized
	 * @return an array of object, containing the name of the new image, and the
	 *         new ImagePlus instance
	 */
	public ImagePlus process(ImagePlus marker, ImagePlus mask, String newName,
			ChamferMask2D weights, boolean floatComputation, boolean normalize)
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
		if (floatComputation)
		{
			algo = new GeodesicDistanceTransformFloatHybrid(weights, normalize);
		}
		else
		{
            algo = new GeodesicDistanceTransformShortHybrid(weights, normalize);
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
