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


import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.morphology.Morphology;
import inra.ijpb.morphology.Morphology.Operation;
import inra.ijpb.morphology.Strel;

import java.awt.AWTEvent;
import java.awt.Checkbox;

/**
 * Plugin for computing various morphological filters on gray scale or color
 * images.
 *
 * @author David Legland
 *
 */
public class MorphologicalFilterPlugin implements ExtendedPlugInFilter,
		DialogListener 
{
	/** Apparently, it's better to store flags in plugin */
	private int flags = DOES_ALL | KEEP_PREVIEW | FINAL_PROCESSING | NO_CHANGES;
	
	PlugInFilterRunner pfr;
	int nPasses;
	boolean previewing = false;
	
	/** need to keep the instance of ImagePlus */ 
	private ImagePlus imagePlus;
	
	/** keep the original image, to restore it after the preview */
	private ImageProcessor baseImage;
	
	/** Keep instance of result image */
	private ImageProcessor result;

	/** an instance of ImagePlus to display the Strel */
	private ImagePlus strelDisplay = null;
	
	/**
	 * The morphological operation to apply.
	 */
	Operation op = Operation.DILATION;
	/**
	 * The shape of the structuring element.
	 */
	Strel.Shape shape = Strel.Shape.SQUARE;
	
	/**
	 * The size of the structuring element, in pixels.
	 */
	int radius = 2;
	
	/**
	 * Whether an image showing the structuring element must be displayed or not.
	 */
	boolean showStrel;
	
	/**
	 * Setup function is called in the beginning of the process, but also at the
	 * end. It is also used for displaying "about" frame.
	 *
	 * When the plugin is applied to a stack, it processes all ImageProcessor
	 * instances within the stack, and displays the result as a new Stack.
	 * 
	 */
	public int setup(String arg, ImagePlus imp) 
	{
		// about...
		if (arg.equals("about")) 
		{
			showAbout(); 
			return DONE;
		}

		// Called at the end for cleaning the results
		if (arg.equals("final"))
		{
			// replace the preview image by the original image
			resetPreview();
			imagePlus.updateAndDraw();

			Strel strel = shape.fromRadius(radius);
			ImagePlus resPlus = process(imagePlus, this.op, strel);
			
			// display result image in a new frame
			resPlus.show();
			if (imagePlus.getStackSize() > 1) 
			{
				resPlus.setZ(imagePlus.getZ());
				resPlus.setSlice(imagePlus.getCurrentSlice());
			}
			
			return DONE;
		}

		if (imp.getStackSize() == 1)
		{
			return flags | KEEP_PREVIEW;
		}
		else
		{
			return flags;
		}
	}
	
	public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr)
	{
		// Normal setup
    	this.imagePlus = imp;
    	this.baseImage = imp.getProcessor().duplicate();

		// Create the configuration dialog
		GenericDialog gd = new GenericDialog("Morphological Filters");
		
		gd.addChoice("Operation", Operation.getAllLabels(), this.op.toString());
		gd.addChoice("Element", Strel.Shape.getAllLabels(), this.shape.toString());
		gd.addNumericField("Radius (in pixels)", this.radius, 0);
		gd.addCheckbox("Show Element", false);
		gd.addPreviewCheckbox(pfr);
		gd.addDialogListener(this);
		previewing = true;
		gd.addHelp("http://imagej.net/MorphoLibJ#Morphological_filters");
		gd.showDialog();
		previewing = false;

		if (gd.wasCanceled())
		{
			resetPreview();
			return DONE;
		}

		parseDialogParameters(gd);

		// clean up an return 
		gd.dispose();
		return flags;
	}

	public boolean dialogItemChanged(GenericDialog gd, AWTEvent evt)
	{
		boolean wasPreview = this.previewing;
		parseDialogParameters(gd);

		// if preview checkbox was unchecked, replace the preview image by the
		// original image
		if (wasPreview && !this.previewing)
		{
			resetPreview();
		}
		return true;
	}

	private void parseDialogParameters(GenericDialog gd)
	{
		// extract chosen parameters
		this.op = Operation.fromLabel(gd.getNextChoice());
		this.shape = Strel.Shape.fromLabel(gd.getNextChoice());
		this.radius = (int) gd.getNextNumber();
		this.showStrel = gd.getNextBoolean();
		// in the case of hyperstack images, the preview checkbox is not
		// displayed
		Checkbox checkBox = gd.getPreviewCheckbox();
		if (checkBox != null) this.previewing = checkBox.getState();
	}

	public void setNPasses(int nPasses)
	{
		this.nPasses = nPasses;
	}

	@Override
	public void run(ImageProcessor image)
	{
		// Create structuring element of the given size
		Strel strel = shape.fromRadius(radius);
		
		// add some listeners
		DefaultAlgoListener.monitor(strel);
		
		// Eventually display the structuring element used for processing 
		if (showStrel) 
		{
			showStrelImage(strel);
		}
		
		// Execute core of the plugin on the original image
		result = processSlice(this.baseImage, op, strel);
		if (!(result instanceof ColorProcessor))
			result.setLut(this.baseImage.getLut());

		if (previewing)
		{
			// Fill up the values of original image with values of the result
			for (int i = 0; i < image.getPixelCount(); i++)
			{
				image.setf(i, result.getf(i));
			}
			image.resetMinAndMax();
		}
	}

	// About...
	private void showAbout()
	{
		IJ.showMessage("MorphoLibJ",
				"MorphoLibJ,\n" +
				"http://imagej.net/MorphoLibJ\n" +
				"\n" +
				"by David Legland\n" +
				"(david.legland@inrae.fr)\n" + 
				"by Ignacio Arganda-Carreras\n" +
				"(iargandacarreras@gmail.com)" + 
				"\n" + 
				"Project page:\n" + 
				"https://github.com/ijpb/MorphoLibJ\n" 
				);
	}

	private void resetPreview()
	{
		// do not preview for composite images
		if (this.imagePlus.isComposite())
		{
			IJ.log("composite image -> abort reset preview");
			return;
		}

		ImageProcessor image = this.imagePlus.getProcessor();
		if (image instanceof FloatProcessor)
		{
			for (int i = 0; i < image.getPixelCount(); i++)
				image.setf(i, this.baseImage.getf(i));
		}
		else
		{
			for (int i = 0; i < image.getPixelCount(); i++)
				image.set(i, this.baseImage.get(i));
		}
		imagePlus.updateAndDraw();
	}
	
	/**
	 * Displays the current structuring element in a new ImagePlus. 
	 * @param strel the structuring element to display
	 */
	private void showStrelImage(Strel strel)
	{
		// Size of the strel image (little bit larger than strel)
		int[] dim = strel.getSize();
		int width = dim[0] + 20; 
		int height = dim[1] + 20;
		
		// Creates strel image by dilating a point
		ImageProcessor strelImage = new ByteProcessor(width, height);
		strelImage.set(width / 2, height / 2, 255);
		strelImage = Morphology.dilation(strelImage, strel);
		
		// Forces the display to inverted LUT (display a black over white)
		if (!strelImage.isInvertedLut())
			strelImage.invertLut();
		
		// Display strel image
		if (strelDisplay == null)
		{
			strelDisplay = new ImagePlus("Structuring Element", strelImage);
		} 
		else 
		{
			strelDisplay.setProcessor(strelImage);
		}
		strelDisplay.show();
	}

	/**
	 * Applies the specified morphological operation with specified structuring
	 * element to the input image.
	 * 
	 * @param image
	 *            the input image (grayscale or color)
	 * @param op
	 *            the operation to apply
	 * @param strel
	 *            the structuring element to use for the operation
	 * @return the result of morphological operation applied to the input image
	 * @deprecated use the process method instead
	 */
	@Deprecated
	public ImagePlus exec(ImagePlus image, Operation op, Strel strel)
	{
		// Check validity of parameters
		if (image == null)
			return null;
		
		// extract the input processor
		ImageProcessor inputProcessor = image.getProcessor();
		
		// apply morphological operation
		ImageProcessor resultProcessor = op.apply(inputProcessor, strel);
		
		// Keep same color model
		resultProcessor.setColorModel(inputProcessor.getColorModel());
		
		// create the new image plus from the processor
		ImagePlus resultImage = new ImagePlus(op.toString(), resultProcessor);
		resultImage.copyScale(image);
					
		// return the created array
		return resultImage;
	}
	
	/**
	 * Applies the specified morphological operation with specified structuring
	 * element to the input image.
	 * 
	 * @param imagePlus
	 *            the input image
	 * @param op
	 *            the operation to apply
	 * @param strel
	 *            the structuring element to use for the operation
	 * @return the result of morphological operation applied to the input image
	 */
	public ImagePlus process(ImagePlus imagePlus, Operation op, Strel strel)
	{
		// Check validity of parameters
		if (imagePlus == null)
			return null;
		
		ImagePlus resPlus = imagePlus.duplicate();
		if (imagePlus.getStackSize() == 1)
		{
			// Create a new ImagePlus with the filter result
			ImageProcessor img = imagePlus.getProcessor();
			ImageProcessor res = processSlice(img, op, strel);
			resPlus.setProcessor(res);
		}
		else
		{
			ImageStack stack = imagePlus.getStack();
			ImageStack resStack = processStack(stack, op, strel);
			resPlus.setStack(resStack);
		}
		
		resPlus.setTitle(createResultImageName(imagePlus));
		resPlus.copyScale(imagePlus);
		
		// return the created array
		return resPlus;
	}
	
	/**
	 * In the case of stacks, processes each slice of the stack and returns a
	 * result the same size as the input stack.
	 * 
	 * @param image
	 *            the image stack to process
	 * @param op
	 *            the operator to apply
	 * @param strel
	 *            the structuring element
	 * @return the result of operation applied on input image
	 */
	private static ImageStack processStack(ImageStack stack, Operation op, Strel strel)
	{
		ImageStack resStack = stack.duplicate();

		// apply morphological operation to each slice of the stack
		for (int i = 1; i <= stack.size(); i++)
		{
			ImageProcessor slice = stack.getProcessor(i);
			ImageProcessor resSlice = processSlice(slice, op, strel);
			resStack.setProcessor(resSlice, i);
		}

		// return the created array
		return resStack;
	}
	
	/**
	 * Applies the specified morphological operation with specified structuring
	 * element to the input image.
	 * 
	 * @param image
	 *            the input image (grayscale or color)
	 * @param op
	 *            the operation to apply
	 * @param strel
	 *            the structuring element to use for the operation
	 * @return the result of morphological operation applied to the input image
	 */
	private static ImageProcessor processSlice(ImageProcessor image, Operation op, Strel strel)
	{
		// Check validity of parameters
		if (image == null)
			return null;
		
		// apply morphological operation
		ImageProcessor res = op.apply(image, strel);
		
		// Keep same color model
		res.setColorModel(image.getColorModel());
		
		// return the created array
		return res;
	}

	/**
	 * Creates the name for result image, by adding a suffix to the base name of
	 * original image.
	 */
	private String createResultImageName(ImagePlus baseImage) 
	{
		return baseImage.getShortTitle() + "-" + op.toString();
	}
}
