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
public class ExtendedMinAndMax3DPlugin implements PlugIn {

	/**
	 * A customized enumeration to choose between extended minima or maxima.
	 */
	public enum Operation {
		EXTENDED_MAXIMA("Extended Maxima", "emax"),
		EXTENDED_MINIMA("Extended Minima", "emin");
		
		private final String label;
		private final String suffix;
		
		private Operation(String label, String suffix) {
			this.label = label;
			this.suffix = suffix;
		}
		
		public ImageStack apply(ImageStack image, int dynamic) {
			if (this == EXTENDED_MAXIMA)
				return MinimaAndMaxima3D.extendedMaxima(image, dynamic);
			if (this == EXTENDED_MINIMA)
				return MinimaAndMaxima3D.extendedMinima(image, dynamic);
			
			throw new RuntimeException(
					"Unable to process the " + this + " morphological operation");
		}
		
		public ImageStack apply(ImageStack image, int dynamic, Connectivity3D connectivity) {
			if (this == EXTENDED_MAXIMA)
				return MinimaAndMaxima3D.extendedMaxima(image, dynamic, connectivity.getValue());
			if (this == EXTENDED_MINIMA)
				return MinimaAndMaxima3D.extendedMinima(image, dynamic, connectivity.getValue());
			
			throw new RuntimeException(
					"Unable to process the " + this + " morphological operation");
		}

		public ImageStack apply(ImageStack image, int dynamic, int connectivity) {
			if (this == EXTENDED_MAXIMA)
				return MinimaAndMaxima3D.extendedMaxima(image, dynamic, connectivity);
			if (this == EXTENDED_MINIMA)
				return MinimaAndMaxima3D.extendedMinima(image, dynamic, connectivity);
			
			throw new RuntimeException(
					"Unable to process the " + this + " morphological operation");
		}

		public String toString() {
			return this.label;
		}
		
		public String getSuffix() {
			return this.suffix;
		}
		
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
		 * @return the Operation object corresponding to the name
		 * @throws IllegalArgumentException
		 *             if label is not recognized.
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
		
		if ( IJ.getVersion().compareTo("1.48a") < 0 )
		{
			IJ.error( "Regional Minima and Maxima", "ERROR: detected ImageJ version " + IJ.getVersion()  
					+ ".\nThis plugin requires version 1.48a or superior, please update ImageJ!" );
			return;
		}
		
		ImagePlus imagePlus = IJ.getImage();
		
		if (imagePlus.getStackSize() == 1) {
			IJ.error("Requires a Stack");
			return;
		}
		
		ImageStack stack = imagePlus.getStack();
		int sizeX = stack.getWidth();
		int sizeY = stack.getHeight();
		int sizeZ = stack.getSize();
		boolean isGray8 = stack.getBitDepth() == 8;
		double minValue, maxValue;
		if (isGray8) {
			minValue = 1;
			maxValue = 255;
		} else {
			minValue = Double.MAX_VALUE;
			maxValue = Double.MIN_VALUE;
			for (int z = 0; z < sizeZ; z++) {
				for (int y = 0; y < sizeY; y++) {
					for (int x = 0; x < sizeX; x++) {
						double val = stack.getVoxel(x, y, z);
						minValue = Math.min(minValue, val);
						maxValue = Math.max(maxValue, val);
					}
				}
			}
		}
		
		// Create the configuration dialog
		GenericDialog gd = new GenericDialog("Extended Min & Max 3D");
		gd.addChoice("Operation", Operation.getAllLabels(), 
				Operation.EXTENDED_MINIMA.label);
		gd.addSlider("Dynamic", minValue, maxValue, 10);		
		gd.addChoice("Connectivity", Connectivity3D.getAllLabels(), Connectivity3D.C6.name());
//		gd.addHelp("http://imagejdocu.tudor.lu/doku.php?id=plugin:morphology:fast_morphological_filters:start");
        gd.showDialog();
        if (gd.wasCanceled())
        	return;
        
		long t0 = System.currentTimeMillis();
		
		// extract chosen parameters
		Operation op = Operation.fromLabel(gd.getNextChoice());
		int dynamic = (int) gd.getNextNumber();
		Connectivity3D conn = Connectivity3D.fromLabel(gd.getNextChoice());
        
		ImageStack result = op.apply(stack, dynamic, conn);
		
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
