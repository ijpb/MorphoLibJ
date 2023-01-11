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


import ij.IJ;
import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.MinimaAndMaxima;

import java.awt.AWTEvent;

/**
 * Plugin for computing regional minima and maxima in grayscale images.
 * Works for planar gray8 images.
 *
 * @see MinimaAndMaxima
 * @author David Legland
 *
 */
public class RegionalMinAndMaxPlugin implements ExtendedPlugInFilter, DialogListener {

	/**
	 * A customized enumeration to choose between regional minima or maxima.
	 */
	public enum Operation {
		/** Regional maxima */
		REGIONAL_MAXIMA("Regional Maxima", "rmax"),
		/** Regional minima */
		REGIONAL_MINIMA("Regional Minima", "rmin");
		
		private final String label;
		private final String suffix;
		
		private Operation(String label, String suffix) {
			this.label = label;
			this.suffix = suffix;
		}
		
		/**
		 * Process to image given as argument.
		 * 
		 * @param image
		 *            the image to process
		 * @param connectivity
		 *            the connectivity to use
		 * @return the maxima or minima within input image
		 */
		public ImageProcessor apply(ImageProcessor image, int connectivity) {
			if (this == REGIONAL_MAXIMA)
				return MinimaAndMaxima.regionalMaxima(image, connectivity);
			if (this == REGIONAL_MINIMA)
				return MinimaAndMaxima.regionalMinima(image, connectivity);
			
			throw new RuntimeException(
					"Unable to process the " + this + " morphological operation");
		}
		
		/**
		 * Process to image given as argument.
		 * 
		 * @param image
		 *            the image to process
		 * @param connectivity
		 *            the connectivity to use
		 * @return the maxima or minima within input image
		 */
		public ImageProcessor apply(ImageProcessor image, Connectivity2D connectivity) {
			if (this == REGIONAL_MAXIMA)
				return MinimaAndMaxima.regionalMaxima(image, connectivity.getValue());
			if (this == REGIONAL_MINIMA)
				return MinimaAndMaxima.regionalMinima(image, connectivity.getValue());
			
			throw new RuntimeException(
					"Unable to process the " + this + " morphological operation");
		}

		public String toString() {
			return this.label;
		}
		
		/**
		 * Returns the suffix added to processed images.
		 * 
		 * @return the suffix added to processed images.
		 */
		public String getSuffix() {
			return this.suffix;
		}
		
		/**
		 * Returns all the labels for this enumeration.
		 * 
		 * @return all the labels for this enumeration.
		 */
		public static String[] getAllLabels(){
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
		 *            the name of the operation
		 * @return the operation corresponding to the name
		 * @throws IllegalArgumentException
		 *             if operation name is not recognized.
		 */
		public static Operation fromLabel(String opLabel) {
			if (opLabel != null)
				opLabel = opLabel.toLowerCase();
			for (Operation op : Operation.values()) {
				String cmp = op.label.toLowerCase();
				if (cmp.equals(opLabel))
					return op;
			}
			throw new IllegalArgumentException("Unable to parse Operation with label: " + opLabel);
		}
	};

	
	/** keep flags in plugin */
	private int flags = DOES_ALL | KEEP_PREVIEW | FINAL_PROCESSING;
	
	PlugInFilterRunner pfr;
	int nPasses;
	boolean previewing = false;
	
	/** keep the instance of ImagePlus */ 
	private ImagePlus imagePlus;
	
	/** keep the original image, to restore it after the preview */
	private ImageProcessor baseImage;
	
	/** Keep instance of result image */
	private ImageProcessor result;

	
	ImagePlus image = null;
	Operation op = Operation.REGIONAL_MINIMA;
	Connectivity2D connectivity = Connectivity2D.C4;
	
	/**
	*/
	public int setup(String arg, ImagePlus imp) {
		
		// about...
		if (arg.equals("about")) {
			showAbout(); 
			return DONE;
		}

		// Called at the end for cleaning up the results
		if (arg.equals("final")) {
			// replace the preview image by the original image 
			imagePlus.setProcessor(baseImage);
			imagePlus.draw();
			
			// as result is binary, choose inverted LUT 
			result.invertLut();
			
			// Create a new ImagePlus with the result
			String newName = createResultImageName(imagePlus);
			ImagePlus resPlus = new ImagePlus(newName, result);
			resPlus.copyScale(imagePlus);
			resPlus.show();
			return DONE;
		}
		
		return flags;
	}
	
	
	public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
		// Normal setup
    	this.imagePlus = imp;
    	this.baseImage = imp.getProcessor().duplicate();

		// Create the configuration dialog
		GenericDialog gd = new GenericDialog("Regional Min & Max");
		
		gd.addChoice("Operation", Operation.getAllLabels(), 
				Operation.REGIONAL_MINIMA.label);
		gd.addChoice("Connectivity", Connectivity2D.getAllLabels(), connectivity.name());
		
		gd.addPreviewCheckbox(pfr);
		gd.addDialogListener(this);
        previewing = true;
        gd.addHelp("https://imagej.net/MorphoLibJ");
        gd.showDialog();
        previewing = false;
        
        if (gd.wasCanceled())
        	return DONE;
			
    	parseDialogParameters(gd);
			
		// clean up an return 
		gd.dispose();
		return flags;
	}

    public boolean dialogItemChanged(GenericDialog gd, AWTEvent evt) {
    	parseDialogParameters(gd);
    	return true;
    }

    private void parseDialogParameters(GenericDialog gd) {
		// extract chosen parameters
		this.op 			= Operation.fromLabel(gd.getNextChoice());
		this.connectivity 	= Connectivity2D.fromLabel(gd.getNextChoice());
    }

    public void setNPasses (int nPasses) {
    	this.nPasses = nPasses;
    }
    
	@Override
	public void run(ImageProcessor image) {
		// Create structuring element of the given size
		// Execute core of the plugin
		result = op.apply(image, connectivity);

    	if (previewing) {
    		// Fill up the values of original image with inverted values of the 
    		// (binary) result
    		double valMax = result.getMax();
    		for (int i = 0; i < image.getPixelCount(); i++) {
    			image.set(i, 255 - (int) (255 * result.getf(i) / valMax));
    		}
        }
	}
	
	// About...
	private void showAbout() {
		IJ.showMessage("Morphological Filters",
				"Fast Grayscale Morphological Filtering,\n" +
				"http://imagejdocu.tudor.lu/doku.php?id=plugin:morphology:fast_morphological_filters:start\n" +
				"\n" +
				"by David Legland\n" +
				"(david.legland@grignon.inra.fr)");
	}
	
	/**
	 * Creates the name for result image, by adding a suffix to the base name
	 * of original image.
	 */
	private String createResultImageName(ImagePlus baseImage) {
		return baseImage.getShortTitle() + "-" + op.getSuffix();
	}

}
