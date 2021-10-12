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
import inra.ijpb.binary.distmap.ChamferMask3D;
import inra.ijpb.binary.distmap.ChamferMasks3D;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransform3D;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransform3DFloat;
import inra.ijpb.color.ColorMaps;
import inra.ijpb.data.image.Images3D;

import java.awt.image.IndexColorModel;

/**
 * Plugin for computing geodesic distance map from binary images using chamfer
 * weights.
 * 
 * @author dlegland
 *
 */
public class GeodesicDistanceMap3D implements PlugIn
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
		GenericDialog gd = new GenericDialog("Geodesic Distance Map 3D");

		gd.addChoice("Marker Image", imageNames, IJ.getImage().getTitle());
		gd.addChoice("Mask Image", imageNames, IJ.getImage().getTitle());
		// Set Chessknight weights as default
		gd.addChoice("Distances", ChamferMasks3D.getAllLabels(),
				ChamferMasks3D.BORGEFORS.toString());
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
		ChamferMask3D chamferMask = ChamferMasks3D.fromLabel(weightLabel).getMask();
		boolean normalize = gd.getNextBoolean();

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
		String newName = maskImage.getShortTitle() + "-geodDist";
		ImagePlus res = process(markerImage, maskImage, newName, chamferMask, normalize);

		res.show();
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
			ChamferMask3D chamferMask, boolean normalize)
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
		
		// check input and mask have the same size
		if (marker.getWidth() != mask.getWidth() || marker.getHeight() != mask.getHeight())
		{
			IJ.showMessage("Error",
					"Input and marker images\nshould have the same size");
			return null;
		}

		// Initialize calculator
		GeodesicDistanceTransform3D algo;
		algo = new GeodesicDistanceTransform3DFloat(chamferMask, normalize);
		DefaultAlgoListener.monitor(algo);

		// Compute distance on specified images
		ImageStack result = algo.geodesicDistanceMap(marker.getStack(), mask.getStack());
		ImagePlus resultPlus = new ImagePlus(newName, result);

		// setup display options
		double[] minMax = Images3D.findMinAndMax(resultPlus);
		resultPlus.setLut(createFireLUT(minMax[1]));
		resultPlus.setDisplayRange(0, minMax[1]);
		
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

}
