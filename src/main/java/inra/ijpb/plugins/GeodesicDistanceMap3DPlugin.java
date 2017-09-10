/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2017 INRA.
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
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.LUT;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.binary.ChamferWeights3D;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransform3D;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransform3DFloat;
import inra.ijpb.data.image.Images3D;
import inra.ijpb.util.ColorMaps;

import java.awt.image.IndexColorModel;

/**
 * Plugin for computing geodesic distance map from binary 3D images using
 * chamfer weights.
 * 
 * @author dlegland
 *
 */
public class GeodesicDistanceMap3DPlugin implements PlugIn
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
		// Set default weights
		gd.addChoice("Distances", ChamferWeights3D.getAllLabels(),
				ChamferWeights3D.WEIGHTS_3_4_5_7.toString());
//		String[] outputTypes = new String[] { "32 bits", "16 bits" };
//		gd.addChoice("Output Type", outputTypes, outputTypes[0]);
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
		ChamferWeights3D weights = ChamferWeights3D.fromLabel(weightLabel);
//		boolean resultAsFloat = gd.getNextChoiceIndex() == 0;
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
//		if (resultAsFloat)
//		{
			res = process(markerImage, maskImage, newName,
					weights.getFloatWeights(), normalizeWeights);
//		} else
//		{
//			res = process(markerImage, maskImage, newName,
//					weights.getShortWeights(), normalizeWeights);
//		}

		res.show();
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

		ImageStack result = process(markerPlus.getStack(), maskPlus.getStack(), weights, normalize);
		ImagePlus resultPlus = new ImagePlus(newName, result);

		// copy calibration settings
		resultPlus.copyScale(maskPlus);
		resultPlus.setSlice(markerPlus.getCurrentSlice());

		// setup display options
		double maxVal = findMaxWithinMask(resultPlus, maskPlus);
		resultPlus.setLut(createFireLUTBlackEnding(maxVal));
		// keep some gap between max val and infinity
		resultPlus.setDisplayRange(0, maxVal + 2);
		
		// return result image
		return resultPlus;
	}

	public ImageStack process(ImageStack marker, ImageStack mask,
			float[] weights, boolean normalize)
	{
		if (weights == null)
		{
			throw new IllegalArgumentException("Weights not specified");
		}

		// check input and mask have the same size
		if (!Images3D.isSameType(marker, mask))
		{
			IJ.showMessage("Error",
					"Input and marker images\nshould have the same size");
			return null;
		}

		// Initialize calculator
		GeodesicDistanceTransform3D algo = new GeodesicDistanceTransform3DFloat(weights, normalize);
		DefaultAlgoListener.monitor(algo);
    	

		// Compute distance on specified images
		ImageStack result = algo.geodesicDistanceMap(marker, mask);

		// create result image
		return result;
	}
	
	private double findMaxWithinMask(ImagePlus image, ImagePlus mask)
	{
		int width = image.getWidth();
		int height = image.getHeight();
		
		ImageStack stack = image.getStack();
		ImageStack maskStack = mask.getStack();
		
		double maxValue = Double.NEGATIVE_INFINITY;
		
		for (int z = 0; z < stack.getSize(); z++)
		{
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					if (maskStack.getVoxel(x, y, z) == 0)
					{
						continue;
					}
					maxValue = Math.max(maxValue, stack.getVoxel(x, y, z));
					
				}
			}
		}
		
		return maxValue;
	}

	private LUT createFireLUTBlackEnding(double maxVal)
	{
		byte[] red = new byte[256];
		byte[] green = new byte[256];
		byte[] blue = new byte[256];

		// initialize LUT with Fire colors
		byte[][] lut = ColorMaps.createJetLut(255);
		for (int i = 0; i < 255; i++)
		{
			red[i] 		= lut[i][0];
			green[i] 	= lut[i][1];
			blue[i] 	= lut[i][2];
		}
		
		// use black as last color (background)
		red[255] = 0;
		green[255] = 0;
		blue[255] = 0;

		// create color model
		IndexColorModel cm = new IndexColorModel(8, 256, red, green, blue);
		return new LUT(cm, 0, maxVal);
	}
	
	private static String createResultImageName(ImagePlus baseImage)
	{
		return baseImage.getShortTitle() + "-geoddist";
	}
}
