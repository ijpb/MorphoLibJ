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

import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ImageProcessor;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.binary.ChamferWeights;
import inra.ijpb.binary.distmap.DistanceTransform;
import inra.ijpb.binary.distmap.DistanceTransform3x3Float;
import inra.ijpb.binary.distmap.DistanceTransform3x3Short;
import inra.ijpb.binary.distmap.DistanceTransform5x5Float;
import inra.ijpb.binary.distmap.DistanceTransform5x5Short;
import inra.ijpb.label.distmap.LabelDistanceTransform;
import inra.ijpb.label.distmap.LabelDistanceTransform3x3Float;
import inra.ijpb.label.distmap.LabelDistanceTransform3x3Short;
import inra.ijpb.label.distmap.LabelDistanceTransform5x5Float;
import inra.ijpb.label.distmap.LabelDistanceTransform5x5Short;

import java.awt.AWTEvent;

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
public class ChamferDistanceMapPlugin implements ExtendedPlugInFilter,
		DialogListener
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
	private ChamferWeights weights; 
	private boolean floatProcessing 	= false;
	private boolean normalize 	= false;
	
	/** Keep instance of result image */
	private ImageProcessor result;
	
	/**
	 * Called at the beginning of the process to know if the plugin can be run
	 * with current image, and at the end to finalize.
	 */
	public int setup(String arg, ImagePlus imp)
	{
		// Special case of plugin called to finalize the process
		if (arg.equals("final")) {
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
    	GenericDialog gd = new GenericDialog("Distance Map");
    	gd.addChoice("Distances", ChamferWeights.getAllLabels(), 
    			ChamferWeights.BORGEFORS.toString());			
    	String[] outputTypes = new String[]{"32 bits", "16 bits"};
    	gd.addChoice("Output Type", outputTypes, outputTypes[0]);
    	gd.addCheckbox("Normalize weights", true);	
    	gd.addPreviewCheckbox(pfr);
    	gd.addDialogListener(this);
        previewing = true;
		gd.addHelp("http://imagejdocu.tudor.lu/doku.php?id=plugin:morphology:fast_morphological_filters:start");
        gd.showDialog();
        previewing = false;
        
    	// test cancel  
    	if (gd.wasCanceled())
    		return DONE;

    	// set up current parameters
    	String weightLabel = gd.getNextChoice();
    	floatProcessing = gd.getNextChoiceIndex() == 0;
    	normalize = gd.getNextBoolean();

    	// identify which weights should be used
    	weights = ChamferWeights.fromLabel(weightLabel);

    	return flags;
    }
    
    /**
     * Called when a dialog widget has been modified: recomputes option values
     * from dialog content. 
     */
    public boolean dialogItemChanged(GenericDialog gd, AWTEvent evt)
    {
    	// set up current parameters
    	String weightLabel = gd.getNextChoice();
    	floatProcessing = gd.getNextChoiceIndex() == 0;
    	normalize = gd.getNextBoolean();

    	// identify which weights should be used
    	weights = ChamferWeights.fromLabel(weightLabel);
        return true;
    }

    public void setNPasses (int nPasses)
    {
    	this.nPasses = nPasses;
    }
    
    /**
     * Apply the current filter settings to process the given image. 
     */
    public void run(ImageProcessor image) 
    {
    	if (floatProcessing)
    	{
    		result = processFloat(image, weights.getFloatWeights(), normalize);
		} else 
		{
			result = processShort(image, weights.getShortWeights(), normalize);
		}
    	
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
    
	private ImageProcessor processFloat(ImageProcessor image, float[] weights,
			boolean normalize) 
	{
    	// Initialize calculator
    	LabelDistanceTransform algo;
    	if (weights.length == 2) {
    		algo = new LabelDistanceTransform3x3Float(weights, normalize);
    	} else {
    		algo = new LabelDistanceTransform5x5Float(weights, normalize);
    	}

    	// add monitoring
    	DefaultAlgoListener.monitor(algo);
    	
    	// Compute distance on specified images
    	return algo.distanceMap(image);
    }

    private ImageProcessor processShort(ImageProcessor image, short[] weights, boolean normalize) {
    	// Initialize calculator
    	LabelDistanceTransform algo;
    	if (weights.length == 2) {
    		algo = new LabelDistanceTransform3x3Short(weights, normalize);
    	} else {
    		algo = new LabelDistanceTransform5x5Short(weights, normalize);
    	}

    	// add monitoring
    	DefaultAlgoListener.monitor(algo);

    	// Compute distance on specified images
    	return algo.distanceMap(image);
    }
   
		
	/**
	 * Computes the distance propagation from the boundary of the particles.
	 * Background is assumed to be 0.
	 * 
	 * @param image
	 *            the binary image to process
	 * @param newName
	 *            the name of the new image
	 * @param weights
	 *            the array of weights in orthogonal, diagonal and eventually
	 *            chessknight moves directions
	 * @param normalize
	 *            boolean flag indicating whether resulting map should be
	 *            normalized
	 * @return an array of objects containing the new name, and the result image
	 */
    @Deprecated
	public Object[] exec(ImagePlus image, String newName, float[] weights, boolean normalize) {
		// Check validity of parameters
		if (image == null) {
			System.err.println("Mask image not specified");
			return null;
		}
		
		if (newName == null)
			newName = createResultImageName(image);
		if (weights == null) {
			System.err.println("Weights not specified");
			return null;
		}
	
		// Initialize calculator
		DistanceTransform calc;
		if (weights.length == 2) {
			calc = new DistanceTransform3x3Float(weights, normalize);
		} else {
			calc = new DistanceTransform5x5Float(weights, normalize);
		}
		// Compute distance on specified images
		ImageProcessor result = calc.distanceMap(image.getProcessor());
		ImagePlus resultPlus = new ImagePlus(newName, result);
		
		// create result array
		return new Object[]{newName, resultPlus};
	}
	
	/**
	 * Compute the distance propagation from the boundary of the white particles. 
	 * 
	 * @param image
	 *            the binary image to process
	 * @param newName
	 *            the name of the new image
	 * @param weights
	 *            the array of weights in orthogonal, diagonal and eventually
	 *            chessknight moves directions
	 * @param normalize
	 *            boolean flag indicating whether resulting map should be
	 *            normalized
	 * @return an array of objects containing the new name, and the result image
	 */
    @Deprecated
	public Object[] exec(ImagePlus image, String newName, short[] weights, boolean normalize) {
		// Check validity of parameters
		if (image == null) {
			System.err.println("Mask image not specified");
			return null;
		}
		
		if (newName == null)
			newName = createResultImageName(image);
		if (weights == null) {
			System.err.println("Weights not specified");
			return null;
		}
	
		// Initialize calculator
		DistanceTransform calc;
		if (weights.length == 2) {
			calc = new DistanceTransform3x3Short(weights, normalize);
		} else {
			calc = new DistanceTransform5x5Short(weights, normalize);
		}
		
		// Compute distance on specified images
		ImageProcessor result = calc.distanceMap(image.getProcessor());
		ImagePlus resultPlus = new ImagePlus(newName, result);
		
		// create result array
		return new Object[]{newName, resultPlus};
	}
	
	private static String createResultImageName(ImagePlus baseImage) {
		return baseImage.getShortTitle() + "-dist";
	}
}
