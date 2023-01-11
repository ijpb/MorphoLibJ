/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2023 INRA.
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

import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.morphology.attrfilt.AreaOpeningQueue;
import inra.ijpb.morphology.attrfilt.BoxDiagonalOpeningQueue;

import java.awt.AWTEvent;

/**
 * Select binary particles in a planar image based on number of pixels.
 * This version also provides preview of result.
 * 
 * @see AreaOpeningPlugin
 * 
 * @author David Legland
 */
public class GrayscaleAttributeFilteringPlugin implements ExtendedPlugInFilter, DialogListener 
{
	enum Operation
	{
		CLOSING("Closing"), 
		OPENING("Opening"),
		TOP_HAT("Top Hat"),
		BOTTOM_HAT("Bottom Hat");
		
		String label;
		
		Operation(String label)
		{
			this.label = label;
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
	};

	enum Attribute
	{
		AREA("Area"), 
		BOX_DIAGONAL("Box Diagonal");
		
		String label;
		
		Attribute(String label)
		{
			this.label = label;
		}
		
		public static String[] getAllLabels()
		{
			int n = Attribute.values().length;
			String[] result = new String[n];
			
			int i = 0;
			for (Attribute att : Attribute.values())
				result[i++] = att.label;
			
			return result;
		}
		
		/**
		 * Determines the Attribute type from its label.
		 * 
		 * @param opLabel
		 *            the label of the Attribute
		 * @return the parsed Attribute
		 * @throws IllegalArgumentException
		 *             if label is not recognized.
		 */
		public static Attribute fromLabel(String attrLabel)
		{
			if (attrLabel != null)
				attrLabel = attrLabel.toLowerCase();
			for (Attribute op : Attribute.values()) 
			{
				String cmp = op.label.toLowerCase();
				if (cmp.equals(attrLabel))
					return op;
			}
			throw new IllegalArgumentException("Unable to parse Attribute with label: " + attrLabel);
		}
	};


	/** keep flags in plugin */
	private int flags = DOES_ALL | KEEP_PREVIEW | FINAL_PROCESSING | NO_CHANGES;
	
	PlugInFilterRunner pfr;
	int nPasses;
	boolean previewing = false;

	
	/** keep the instance of ImagePlus */ 
	private ImagePlus imagePlus;
	
	/** keep the original image, to restore it after the preview */
	private ImageProcessor baseImage;
	
	/** Keep instance of result image */
	private ImageProcessor result;

	
	Operation operation = Operation.OPENING;
	Attribute attribute = Attribute.AREA; 
	int minimumValue = 100;
	Connectivity2D connectivity = Connectivity2D.C4;
	
	
	@Override
	public int setup(String arg, ImagePlus imp)
	{
		// Called at the end for cleaning up the results
		if (arg.equals("final")) 
		{
			// replace the preview image by the original image 
			resetPreview();
			imagePlus.updateAndDraw();
			
			// Create a new ImagePlus with the result
			String newName = imagePlus.getShortTitle() + "-attrFilt";
			ImagePlus resPlus = new ImagePlus(newName, result);
			
			// copy spatial calibration and display settings 
			resPlus.copyScale(imagePlus);
			result.setColorModel(baseImage.getColorModel());
			resPlus.show();
			return DONE;
		}
		
		// Normal setup
    	this.imagePlus = imp;
    	this.baseImage = imp.getProcessor().duplicate();
    
		return flags;
	}
	
	@Override
	public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr)
	{
		// Create the configuration dialog
		GenericDialog gd = new GenericDialog("Gray Scale Attribute Filtering");

		gd.addChoice("Operation", Operation.getAllLabels(), Operation.OPENING.label);
		gd.addChoice("Attribute", Attribute.getAllLabels(), Attribute.AREA.label);
		gd.addNumericField("Minimum Value", 100, 0, 10, "pixels");
		gd.addChoice("Connectivity", Connectivity2D.getAllLabels(), Connectivity2D.C4.name());
		
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
	
	@Override
	public void run(ImageProcessor image)
	{
		// Identify image to process (original, or inverted)
		ImageProcessor image2 = baseImage;
		if (this.operation == Operation.CLOSING || this.operation == Operation.BOTTOM_HAT)
		{
			image2 = image2.duplicate();
			image2.invert();
		}
		
		// switch depending on attribute to use
		if (attribute == Attribute.AREA)
		{
			AreaOpeningQueue algo = new AreaOpeningQueue();
			algo.setConnectivity(this.connectivity.getValue());
			DefaultAlgoListener.monitor(algo);
			this.result = algo.process(image2, this.minimumValue);
		}
		else
		{
			BoxDiagonalOpeningQueue algo = new BoxDiagonalOpeningQueue();
			algo.setConnectivity(this.connectivity.getValue());
			DefaultAlgoListener.monitor(algo);
			this.result = algo.process(image2, this.minimumValue);
		}
		
		// For top-hat and bottom-hat, we consider difference with original image
		if (this.operation == Operation.TOP_HAT ||
				this.operation == Operation.BOTTOM_HAT)
		{
			double maxDiff = 0;
			for (int i = 0; i < image.getPixelCount(); i++)
			{
				float diff = Math.abs(this.result.getf(i) - image2.getf(i));
				this.result.setf(i, diff);
				maxDiff = Math.max(diff, maxDiff);
			}
			
			this.result.setMinAndMax(0, maxDiff);
		}
		
		// For closing, invert back the result
		else if (this.operation == Operation.CLOSING)
		{
			this.result.invert();
		}

		if (previewing)
		{
			// Iterate over pixels to change value of reference image
			for (int i = 0; i < image.getPixelCount(); i++)
			{
				image.setf(i, result.getf(i));
			}
			image.setMinAndMax(result.getMin(), result.getMax());
		}
	}
	
	@Override
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e)
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
		image.resetMinAndMax();
		imagePlus.updateAndDraw();
	}
	
	/**
	 * Extract chosen parameters
	 * @param gd the instance of GenericDialog used to parse parameters 
	 */
    private void parseDialogParameters(GenericDialog gd) 
    {
		this.operation 		= Operation.fromLabel(gd.getNextChoice());
		this.attribute 		= Attribute.fromLabel(gd.getNextChoice());
		this.minimumValue	= (int) gd.getNextNumber();
		this.connectivity 	= Connectivity2D.fromLabel(gd.getNextChoice());
		this.previewing 	= gd.getPreviewCheckbox().getState();
    }

	@Override
	public void setNPasses(int nPasses)
	{
		this.nPasses = nPasses;
	}
}
