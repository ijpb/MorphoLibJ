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

import java.awt.AWTEvent;

import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ImageProcessor;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.binary.distmap.ChamferDistanceTransform2DFloat;
import inra.ijpb.binary.distmap.ChamferDistanceTransform2DShort;
import inra.ijpb.binary.distmap.ChamferMask2D;
import inra.ijpb.binary.distmap.ChamferMasks2D;
import inra.ijpb.binary.distmap.DistanceTransform;

/**
 * Compute distance map, with possibility to choose chamfer weights, result
 * type, and to normalize result or not.
 *
 * Can process either binary images, or label images (that can be coded with 8,
 * 16 or 32 bits).
 * 
 * @author dlegland
 *
 */
public class ChamferDistanceMapPlugin
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

	/** the different weights */
	private ChamferMask2D mask;
	private boolean floatProcessing = false;
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
			String newName = createResultImageName(imagePlus);
			ImagePlus resPlus = new ImagePlus(newName, result);
			resPlus.copyScale(imagePlus);
			resPlus.show();
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
		GenericDialog gd = new GenericDialog("Chamfer Distance Transform");
		gd.addChoice("Distances", ChamferMasks2D.getAllLabels(),
				ChamferMasks2D.BORGEFORS.toString());
		String[] outputTypes = new String[] { "16 bits", "32 bits" };
		gd.addChoice("Output Type", outputTypes, outputTypes[0]);
		gd.addCheckbox("Normalize weights", true);
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
		String maskLabel = gd.getNextChoice();
		floatProcessing = gd.getNextChoiceIndex() == 0;
		normalize = gd.getNextBoolean();

		// identify which mask should be used
		mask = ChamferMasks2D.fromLabel(maskLabel).getMask();

		return flags;
	}

	/**
	 * Called when a dialog widget has been modified: recomputes option values
	 * from dialog content.
	 */
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent evt)
	{
		// set up current parameters
		String maskLabel = gd.getNextChoice();
		floatProcessing = gd.getNextChoiceIndex() == 0;
		normalize = gd.getNextBoolean();

		// identify which weights should be used
		mask = ChamferMasks2D.fromLabel(maskLabel).getMask();
		return true;
	}

	public void setNPasses(int nPasses)
	{
		this.nPasses = nPasses;
	}

	/**
	 * Apply the current filter settings to process the given image.
	 */
	public void run(ImageProcessor image)
	{
		result = process(image, mask, floatProcessing, normalize);

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

	private ImageProcessor process(ImageProcessor image, ChamferMask2D weights,
			boolean processFloat, boolean normalize)
	{
		// Initialize calculator
		DistanceTransform algo;
		if (processFloat)
		{
			algo = new ChamferDistanceTransform2DFloat(weights, normalize);
		}
		else
		{
			algo = new ChamferDistanceTransform2DShort(weights, normalize);
		}
		
		// add monitoring
		DefaultAlgoListener.monitor(algo);

		// Compute distance on specified images
		return algo.distanceMap(image);
	}

	private static String createResultImageName(ImagePlus baseImage)
	{
		return baseImage.getShortTitle() + "-dist";
	}
}
