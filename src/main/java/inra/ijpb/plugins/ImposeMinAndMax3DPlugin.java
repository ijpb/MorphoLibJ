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
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.ImageStack;
import inra.ijpb.morphology.MinimaAndMaxima3D;
import inra.ijpb.util.IJUtils;

/**
 * Plugin for imposing minima or maxima on a grayscale 3D image, using a specific
 * connectivity. 
 */
public class ImposeMinAndMax3DPlugin implements PlugIn {

	/**
	 * A customized enumeration to choose between minima or maxima imposition.
	 */
	public enum Operation {
		/** Imposition of minima */
		IMPOSE_MINIMA("Impose Minima"),
		/** Imposition of maxima */
		IMPOSE_MAXIMA("Impose Maxima");
		
		private final String label;
		
		private Operation(String label) {
			this.label = label;
		}
		
		/**
		 * Process to image given as argument.
		 * 
		 * @param image
		 *            the image to process
		 * @param markers
		 *            the marker image of minima or maxima
		 * @return an image with same extrema as the marker image
		 */
		public ImageStack applyTo(ImageStack image,
				ImageStack markers) {
			if (this == IMPOSE_MINIMA)
				return MinimaAndMaxima3D.imposeMinima(image, markers);
			if (this == IMPOSE_MAXIMA)
				return MinimaAndMaxima3D.imposeMaxima(image, markers);
						
			throw new RuntimeException(
					"Unable to process the " + this + " operation");
		}
		
		/**
		 * Process to image given as argument.
		 * 
		 * @param image
		 *            the image to process
		 * @param markers
		 *            the marker image of minima or maxima
		 * @param conn
		 *            the connectivity to use
		 * @return an image with same extrema as the marker image
		 */
		public ImageStack applyTo(ImageStack image,
				ImageStack markers, int conn) {
			if (this == IMPOSE_MINIMA)
				return MinimaAndMaxima3D.imposeMinima(image, markers, conn);
			if (this == IMPOSE_MAXIMA)
				return MinimaAndMaxima3D.imposeMaxima(image, markers, conn);
						
			throw new RuntimeException(
					"Unable to process the " + this + " operation");
		}
		
		public String toString() {
			return this.label;
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


	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg) {
		
		if ( IJ.getVersion().compareTo("1.48a") < 0 )
		{
			IJ.error( "Impose Min and Max 3D", "ERROR: detected ImageJ version " + IJ.getVersion()  
					+ ".\nThis plugin requires version 1.48a or superior, please update ImageJ!" );
			return;
		}
		
		// Open a dialog to choose:
		// - mask image
		// - marker image
		int[] indices = WindowManager.getIDList();
		if (indices == null) {
			IJ.error("No image", "Need at least one image to work");
			return;
		}
	
		// create the list of image names
		String[] imageNames = new String[indices.length];
		for (int i = 0; i < indices.length; i++) {
			imageNames[i] = WindowManager.getImage(indices[i]).getTitle();
		}
		
		// create the dialog
		GenericDialog gd = new GenericDialog("Impose Min & Max 3D");
		
		gd.addChoice("Original Image", imageNames, IJ.getImage().getTitle());
		gd.addChoice("Marker Image", imageNames, IJ.getImage().getTitle());
		gd.addChoice("Operation", 
				Operation.getAllLabels(), 
				Operation.IMPOSE_MINIMA.label);
		gd.addChoice("Connectivity", Connectivity3D.getAllLabels(), Connectivity3D.C6.name());
		gd.showDialog();
		
		if (gd.wasCanceled())
			return;

		// set up current parameters
		int refImageIndex = gd.getNextChoiceIndex();
		ImagePlus refImage = WindowManager.getImage(refImageIndex + 1);
		int markerImageIndex = gd.getNextChoiceIndex();
		ImagePlus markerImage = WindowManager.getImage(markerImageIndex + 1);
		Operation op = Operation.fromLabel(gd.getNextChoice());
		Connectivity3D conn = Connectivity3D.fromLabel(gd.getNextChoice());
        
		// Extract image processors
		ImageStack refStack = refImage.getStack();
		ImageStack markerStack = markerImage.getStack();
		
		long t0 = System.currentTimeMillis();
		
		// Compute geodesic reconstruction
		ImageStack recProc = op.applyTo(refStack, markerStack, conn.getValue());
		
		// Keep same color model as 
		recProc.setColorModel(refStack.getColorModel());

		// create resulting image
		String newName = createResultImageName(refImage);
		ImagePlus resultImage = new ImagePlus(newName, recProc);
		resultImage.copyScale(markerImage);
		resultImage.show();
		resultImage.setSlice(refImage.getCurrentSlice());

		long t1 = System.currentTimeMillis();
		IJUtils.showElapsedTime(op.toString(), t1 - t0, refImage);
	}

	private static String createResultImageName(ImagePlus baseImage) {
		return baseImage.getShortTitle() + "-imp";
	}
	
}
