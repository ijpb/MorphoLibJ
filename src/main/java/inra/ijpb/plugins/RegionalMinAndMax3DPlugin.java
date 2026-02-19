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
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import inra.ijpb.morphology.MinimaAndMaxima;
import inra.ijpb.morphology.MinimaAndMaxima3D;
import inra.ijpb.util.IJUtils;

/**
 * Plugin for computing regional minima and maxima in grayscale images.
 * Works for planar gray8 images.
 *
 * @see MinimaAndMaxima
 * @author David Legland
 *
 */
public class RegionalMinAndMax3DPlugin implements PlugIn {

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
		public ImageStack apply(ImageStack image, int connectivity) {
			if (this == REGIONAL_MAXIMA)
				return MinimaAndMaxima3D.regionalMaxima(image, connectivity);
			if (this == REGIONAL_MINIMA)
				return MinimaAndMaxima3D.regionalMinima(image, connectivity);
			
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

	

	@Override
	public void run(String arg) {
		ImagePlus imagePlus = IJ.getImage();
		
		if (imagePlus.getStackSize() == 1) {
			IJ.error("Requires a Stack");
		}
		
		ImageStack stack = imagePlus.getStack();
		
		// Create the configuration dialog
		GenericDialog gd = new GenericDialog("Regional Min & Max 3D");
		gd.addChoice("Operation", Operation.getAllLabels(), 
				Operation.REGIONAL_MINIMA.label);
		gd.addChoice("Connectivity", Connectivity3D.getAllLabels(), Connectivity3D.C6.name());
//		gd.addHelp("http://imagejdocu.tudor.lu/doku.php?id=plugin:morphology:fast_morphological_filters:start");
        gd.showDialog();
        if (gd.wasCanceled())
        	return;
        
		// extract chosen parameters
		Operation op = Operation.fromLabel(gd.getNextChoice());
		Connectivity3D conn = Connectivity3D.fromLabel(gd.getNextChoice());
        
		long t0 = System.currentTimeMillis();
		
		ImageStack result = op.apply(stack, conn.getValue());

		String newName = createResultImageName(imagePlus, op);
		ImagePlus resultPlus = new ImagePlus(newName, result);
		resultPlus.copyScale(imagePlus);
		resultPlus.show();
		
		resultPlus.setSlice(imagePlus.getCurrentSlice());

		long t1 = System.currentTimeMillis();
		IJUtils.showElapsedTime(op.toString(), t1 - t0, imagePlus);
	}
		
	/**
	 * Creates the name for result image, by adding a suffix to the base name
	 * of original image.
	 */
	private String createResultImageName(ImagePlus baseImage, Operation op) {
		return baseImage.getShortTitle() + "-" + op.getSuffix();
	}


}
