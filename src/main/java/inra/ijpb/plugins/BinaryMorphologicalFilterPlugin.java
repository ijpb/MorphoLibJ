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
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.morphology.BinaryMorphology;
import inra.ijpb.morphology.binary.DistanceMapBinaryClosing;
import inra.ijpb.morphology.binary.DistanceMapBinaryDilation;
import inra.ijpb.morphology.binary.DistanceMapBinaryErosion;
import inra.ijpb.morphology.binary.DistanceMapBinaryOpening;

/**
 * Plugin for computing various morphological filters on binary images.
 *
 * @author David Legland
 *
 */
public class BinaryMorphologicalFilterPlugin implements ExtendedPlugInFilter,
		DialogListener 
{
	/** Apparently, it's better to store flags in plugin */
	private int flags = DOES_8G | KEEP_PREVIEW | FINAL_PROCESSING | NO_CHANGES;
	
	PlugInFilterRunner pfr;
	int nPasses;
	boolean previewing = false;
	
	/** need to keep the instance of ImagePlus */ 
	private ImagePlus imagePlus;
	
	/** keep the original image, to restore it after the preview */
	private ByteProcessor baseImage;
	
	/** Keep instance of result image */
	private ByteProcessor result;

	/** an instance of ImagePlus to display the Strel */
	private ImagePlus strelDisplay = null;
	
	Operation op = Operation.DILATION;
	int radius = 2;
	boolean showStrel;
	
	
	enum Operation
	{
		EROSION("Erosion"),
		DILATION("Dilation"),
		OPENING("Opening"),
		CLOSING("Closing");
		
		String label;

		Operation(String label)
		{
			this.label = label;
		}
		
		public ByteProcessor process(ByteProcessor image, double radius)
		{
			switch (this)
			{
			case EROSION: 
			{
				DistanceMapBinaryErosion algo = new DistanceMapBinaryErosion(radius);
				DefaultAlgoListener.monitor(algo);
				return algo.processBinary(image);
			}
			case DILATION: 
			{
				DistanceMapBinaryDilation algo = new DistanceMapBinaryDilation(radius);
				DefaultAlgoListener.monitor(algo);
				return algo.processBinary(image);
			}
			case OPENING: 
			{
				DistanceMapBinaryOpening algo = new DistanceMapBinaryOpening(radius);
				DefaultAlgoListener.monitor(algo);
				return algo.processBinary(image);
			}
			case CLOSING: 
			{
				DistanceMapBinaryClosing algo = new DistanceMapBinaryClosing(radius);
				DefaultAlgoListener.monitor(algo);
				return algo.processBinary(image);
			}

			default: 
			{
				throw new RuntimeException("Unknown type");
			}
			}
		}

		public String toString() 
		{
			return this.label;
		}
		
		public static String[] getAllLabels()
		{
			int n = Operation.values().length;
			String[] result = new String[n];
			
			int i = 0;
			for (Operation op : Operation.values())
				result[i++] = op.label;
			
			return result;
		}
		
		/**
		 * Determines the operation type from its label.
		 * 
		 * @param opLabel
		 *            the label of the operation
		 * @return the parsed Operation
		 * @throws IllegalArgumentException
		 *             if label is not recognized.
		 */
		public static Operation fromLabel(String opLabel)
		{
			if (opLabel != null)
				opLabel = opLabel.toLowerCase();
			for (Operation op : Operation.values()) 
			{
				String cmp = op.label.toLowerCase();
				if (cmp.equals(opLabel))
					return op;
			}
			throw new IllegalArgumentException("Unable to parse Operation with label: " + opLabel);
		}
	}
	
	
	/**
	 * Setup function is called in the beginning of the process, but also at the
	 * end. It is also used for displaying "about" frame.
	 */
	public int setup(String arg, ImagePlus imp) 
	{
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
    	this.baseImage = (ByteProcessor) imp.getProcessor().duplicate();

		// Create the configuration dialog
		GenericDialog gd = new GenericDialog("Morphological Filters");
		
		gd.addChoice("Operation", Operation.getAllLabels(), this.op.toString());
		gd.addNumericField("Radius (in pixels)", this.radius, 0);
		gd.addCheckbox("Show Element", false);
		gd.addPreviewCheckbox(pfr);
		gd.addDialogListener(this);
        previewing = true;
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
		// Eventually display the structuring element used for processing 
		if (showStrel) 
		{
			showStrelImage();
		}
		
		// Execute core of the plugin on the original image
		result = op.process(this.baseImage, radius);

    	if (previewing) 
    	{
    		// Fill up the values of original image with values of the result
    		for (int i = 0; i < image.getPixelCount(); i++)
    		{
    			image.set(i, result.get(i));
    		}
    		image.resetMinAndMax();
        }
	}

	private void resetPreview()
	{
		ImageProcessor image = this.imagePlus.getProcessor();
		for (int i = 0; i < image.getPixelCount(); i++)
			image.set(i, this.baseImage.get(i));
		
		imagePlus.updateAndDraw();
	}
	
	/**
	 * Displays the current structuring element in a new ImagePlus. 
	 */
	private void showStrelImage()
	{
		// Size of the strel image (little bit larger than strel)
		int intRadius = (int) Math.ceil(radius);
		int width = 2 * intRadius + 20; 
		int height = 2 * intRadius + 20;
		
		// Creates strel image by dilating a point
		ByteProcessor strelImage = new ByteProcessor(width, height);
		strelImage.set(width / 2, height / 2, 255);
		strelImage = BinaryMorphology.dilationDisk(strelImage, this.radius);
		
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
	 */
	public ImagePlus process(ImagePlus image, Operation op, double radius)
	{
		// Check validity of parameters
		if (image == null)
			return null;
		
		// extract the input processor
		ImageProcessor inputProcessor = image.getProcessor();
		if (!(inputProcessor instanceof ByteProcessor))
		{
			throw new RuntimeException("Input image must be binary");
		}
		
		// apply morphological operation
		ImageProcessor resultProcessor = op.process((ByteProcessor) inputProcessor, radius);
		
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
