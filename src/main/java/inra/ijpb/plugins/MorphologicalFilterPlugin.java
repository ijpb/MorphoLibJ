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
	
	Operation op = Operation.DILATION;
	Strel.Shape shape = Strel.Shape.SQUARE;
	int radius = 2;
	boolean showStrel;
	
	/**
	 * Setup function is called in the beginning of the process, but also at the
	 * end. It is also used for displaying "about" frame.
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
		// Normal setup
    	this.imagePlus = imp;
    	this.baseImage = imp.getProcessor().duplicate();

		// Create the configuration dialog
		GenericDialog gd = new GenericDialog("Morphological Filters");
		
		gd.addChoice("Operation", Operation.getAllLabels(), 
				this.op.toString());
		gd.addChoice("Element", Strel.Shape.getAllLabels(), 
				this.shape.toString());
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
    	
    	// if preview checkbox was unchecked, replace the preview image by the original image
    	if (wasPreview && !this.previewing)
    	{
    		resetPreview();
    	}
    	return true;
    }

    private void parseDialogParameters(GenericDialog gd) 
    {
		// extract chosen parameters
		this.op 		= Operation.fromLabel(gd.getNextChoice());
		this.shape 		= Strel.Shape.fromLabel(gd.getNextChoice());
		this.radius 	= (int) gd.getNextNumber();		
		this.showStrel 	= gd.getNextBoolean();
		this.previewing = gd.getPreviewCheckbox().getState();
    }
    
    public void setNPasses (int nPasses) 
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
		result = op.apply(this.baseImage, strel);
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
				"(david.legland@inra.fr)\n" + 
				"by Ignacio Arganda-Carreras\n" +
		        "(iargandacarreras@gmail.com)" + 
		        "\n" + 
		        "Project page:\n" + 
                "https://github.com/ijpb/MorphoLibJ\n" 
                );
	}

	private void resetPreview()
	{
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
	 * @param image
	 *            the input image (grayscale or color)
	 * @param op
	 *            the operation to apply
	 * @param strel
	 *            the structuring element to use for the operation
	 * @return the result of morphological operation applied to the input image
	 */
	public ImagePlus process(ImagePlus image, Operation op, Strel strel)
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
	 * Creates the name for result image, by adding a suffix to the base name
	 * of original image.
	 */
	private String createResultImageName(ImagePlus baseImage) 
	{
		return baseImage.getShortTitle() + "-" + op.toString();
	}
}
