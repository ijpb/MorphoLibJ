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
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Morphology;
import inra.ijpb.morphology.Strel3D;
import inra.ijpb.morphology.Morphology.Operation;
import inra.ijpb.morphology.strel.Cross3DStrel;
import inra.ijpb.util.IJUtils;

/**
 * A simple plugin to test the Cross 3D strel.
 * 
 * @see inra.ijpb.morphology.strel.Cross3DStrel
 * @author David Legland
 *
 */

public class MorphologicalFilterCross3DPlugin implements PlugIn {

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg) {
		ImagePlus imagePlus = WindowManager.getCurrentImage();
		if (imagePlus == null) {
			IJ.error("No image", "Need at least one image to work");
			return;
		}
		
		// create the dialog
		GenericDialog gd = new GenericDialog("Morphological Filter");
		
		gd.addChoice("Operation", Operation.getAllLabels(), 
				Operation.DILATION.toString());
		gd.addCheckbox("Show Element", false);
		
		// Could also add an option for the type of operation
		gd.showDialog();
		
		if (gd.wasCanceled())
			return;
		
		long t0 = System.currentTimeMillis();

		// extract chosen parameters
		Operation op = Operation.fromLabel(gd.getNextChoice());
		boolean showStrel = gd.getNextBoolean();
		
		// Create structuring element of the given size
		Strel3D strel = new Cross3DStrel();
		strel.showProgress(true);
		
		// Eventually display the structuring element used for processing 
		if (showStrel) {
			showStrelImage(strel);
		}
		
		// Execute core of the plugin
		ImagePlus resPlus = process(imagePlus, op, strel);

		if (resPlus == null)
			return;

		// Display the result image
		resPlus.show();
		resPlus.setSlice(imagePlus.getCurrentSlice());

		// Display elapsed time
		long t1 = System.currentTimeMillis();
		IJUtils.showElapsedTime(op.toString(), t1 - t0, imagePlus);
//		IJ.showStatus("Elapsed time: " + (t1 - t0) / 1000. + "s");
	}


	/**
	 * Displays the current structuring element in a new ImagePlus. 
	 * @param strel the structuring element to display
	 */
	private void showStrelImage(Strel3D strel) {
		// Size of the strel image (little bit larger than strel)
		int[] dim = strel.getSize();
		int width = dim[0] + 20; 
		int height = dim[1] + 20;
		
		// Creates strel image by dilating a point
		ImageProcessor maskProcessor = new ByteProcessor(width, height);
		maskProcessor.set(width / 2, height / 2, 255);
		ImageStack stack = new ImageStack();
		stack.addSlice(maskProcessor);
		stack = Morphology.dilation(stack, strel);
		maskProcessor = stack.getProcessor(1);
		
		// Forces the display to inverted LUT (display a black over white)
		if (!maskProcessor.isInvertedLut())
			maskProcessor.invertLut();
		
		// Display strel image
		ImagePlus maskImage = new ImagePlus("Element", maskProcessor);
		maskImage.show();
	}

	public ImagePlus process(ImagePlus image, Operation op, Strel3D strel) {
		// Check validity of parameters
		if (image == null)
			return null;
		
		// extract the input stack
		ImageStack inputStack = image.getStack();

		// apply morphological operation
		ImageStack resultStack = op.apply(inputStack, strel);

		// create the new image plus from the processor
		ImagePlus resultPlus = new ImagePlus(op.toString(), resultStack);
		resultPlus.copyScale(image);
		
		// return the created array
		return resultPlus;
	}
	
}
