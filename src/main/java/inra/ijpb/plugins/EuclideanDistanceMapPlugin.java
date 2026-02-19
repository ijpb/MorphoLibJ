/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2026 INRA.
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

import java.awt.AWTEvent;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ImageProcessor;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.data.image.Images3D;
import inra.ijpb.label.distmap.SaitoToriwakiDistanceTransform2DFloat;
import inra.ijpb.label.distmap.SaitoToriwakiDistanceTransform3DFloat;
import inra.ijpb.util.IJUtils;

/**
 * Compute distance map, with possibility to choose the chamfer mask, the result
 * type (integer or floating point), and to normalize result or not.
 *
 * Can process either binary images, or label images (that can be coded with 8,
 * 16 or 32 bits).
 * 
 * @see inra.ijpb.label.distmap.DistanceTransform2D
 * @see inra.ijpb.binary.distmap.ChamferMask2D
 * @see inra.ijpb.label.distmap.ChamferDistanceTransform2DShort
 * @see inra.ijpb.label.distmap.ChamferDistanceTransform2DFloat
 * 
 * @author dlegland
 */
public class EuclideanDistanceMapPlugin
		implements ExtendedPlugInFilter, DialogListener
{
	/** Apparently, it's better to store flags in plugin */
	private int flags = DOES_8G | DOES_16 | DOES_32 | KEEP_PREVIEW | FINAL_PROCESSING;
	PlugInFilterRunner pfr;
	int nPasses;
	boolean previewing = false;

	/** need to keep the instance of ImagePlus */
	private ImagePlus imagePlus;

	/** keep the original image, to restore it after the preview */
	private ImageProcessor baseImage;

//	private boolean floatProcessing = false;
	private boolean normalize = false;

	/** Keep instance of result image */
	private ImageProcessor result;

	/**
	 * Called at the beginning of the process to know if the plugin can be run
	 * with current image, and at the end to finalize.
	 */
	public int setup(String arg, ImagePlus imp)
	{
		// Special case of plugin called to finalize the process
		if (arg.equals("final"))
		{
			// replace the preview image by the original image
			imagePlus.setProcessor(baseImage);
			imagePlus.draw();

			// Create a new ImagePlus with the filter result
			ImagePlus resPlus = process(imagePlus, normalize);
			resPlus.show();
			
			if (imagePlus.getStackSize() > 1)
			{
				resPlus.setSlice(imagePlus.getCurrentSlice());
			}
			return DONE;
		}

		return flags;
	}

	public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr)
	{
		// Store user data
		this.imagePlus = imp;
		this.baseImage = imp.getProcessor().duplicate();
		this.pfr = pfr;

		// Create a new generic dialog with appropriate options
		GenericDialog gd = new GenericDialog("Euclidean Distance Transform");
		gd.addCheckbox("Normalize", true);
		gd.addPreviewCheckbox(pfr);
		gd.addDialogListener(this);
		previewing = true;
		gd.addHelp("https://imagej.net/MorphoLibJ");
		gd.showDialog();
		previewing = false;

		// test cancel
		if (gd.wasCanceled())
			return DONE;

		// set up current parameters
		normalize = gd.getNextBoolean();

		return flags;
	}

	/**
	 * Called when a dialog widget has been modified: recomputes option values
	 * from dialog content.
	 */
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent evt)
	{
		// set up current parameters
		normalize = gd.getNextBoolean();

		return true;
	}

	public void setNPasses(int nPasses)
	{
		this.nPasses = nPasses;
	}

	/**
	 * Uses the current settings of the algorithm to process the current (2D)
	 * image.
	 * 
	 * This method is used to compute the "preview" image.
	 */
	public void run(ImageProcessor image)
	{
		result = process(image, normalize);

		if (previewing)
		{
			// Fill up the values of original image with values of the result
			double valMax = result.getMax();
			for (int i = 0; i < image.getPixelCount(); i++)
			{
				image.set(i, (int) (255 * result.getf(i) / valMax));
			}
			image.resetMinAndMax();
			if (image.isInvertedLut())
				image.invertLut();
		}
	}

	/**
	 * Computes the result of Euclidean distance map algorithm on the specified
	 * image.
	 * 
	 * @param imagePlus
	 *            the image to use as a binary mask
	 * @param normalize
	 *            the option to compute Euclidean distance (true) or squared
	 *            Euclidean distance (false)
	 * @return the Eucldiean distance map, in a 32-bits floating point 2D or 3D
	 *         image.
	 */
	private ImagePlus process(ImagePlus imagePlus, boolean normalize)
	{
		String newName = createResultImageName(imagePlus);

    	long t0 = System.currentTimeMillis();
    	
		ImagePlus resultPlus;
		if (imagePlus.getStackSize() == 1)
		{
			// compute array of pixel dimensions
			Calibration calib = imagePlus.getCalibration();
			double[] spacings = new double[] { calib.pixelWidth, calib.pixelHeight };
			
			// Initialize calculator
			SaitoToriwakiDistanceTransform2DFloat algo = new SaitoToriwakiDistanceTransform2DFloat(normalize);
			DefaultAlgoListener.monitor(algo);

			// Compute distance on specified images
			ImageProcessor res = algo.distanceMap(imagePlus.getProcessor(), spacings);

			res.resetMinAndMax();
			if (res.isInvertedLut())
				res.invertLut();

			// Create a new ImagePlus with the filter result
			resultPlus = new ImagePlus(newName, res);
		}
		else
		{
			// compute array of pixel dimensions
			Calibration calib = imagePlus.getCalibration();
			double[] spacings = new double[] { calib.pixelWidth, calib.pixelHeight, calib.pixelDepth };
			
			// Initialize calculator
			SaitoToriwakiDistanceTransform3DFloat algo = new SaitoToriwakiDistanceTransform3DFloat(normalize);
			DefaultAlgoListener.monitor(algo);

			// Compute distance on specified images
			ImageStack res = algo.distanceMap(imagePlus.getStack(), spacings);

			// Create a new ImagePlus with the filter result
			resultPlus = new ImagePlus(newName, res);
			
			// calibrate display range of distances
			double[] distExtent = Images3D.findMinAndMax(resultPlus);
			resultPlus.setDisplayRange(0, distExtent[1]);
		}
		
		// Display elapsed time
		long t1 = System.currentTimeMillis();
		IJUtils.showElapsedTime("Euclidean distance map", t1 - t0, imagePlus);
		
		resultPlus.copyScale(imagePlus);
		return resultPlus;
	}
	
	private ImageProcessor process(ImageProcessor image, boolean normalize)
	{
		// Initialize calculator
		SaitoToriwakiDistanceTransform2DFloat algo = new SaitoToriwakiDistanceTransform2DFloat(normalize);
		
		// compute array of pixel dimensions
		Calibration calib = imagePlus.getCalibration();
		double[] spacings = new double[] { calib.pixelWidth, calib.pixelHeight };
			
		// add monitoring
		DefaultAlgoListener.monitor(algo);

		// Compute distance on specified images
		return algo.distanceMap(image, spacings);
	}
	
	
	private static String createResultImageName(ImagePlus baseImage)
	{
		return baseImage.getShortTitle() + "-dist";
	}
}
