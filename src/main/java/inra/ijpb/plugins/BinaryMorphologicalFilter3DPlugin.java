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
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.morphology.BinaryMorphology;
import inra.ijpb.morphology.binary.DistanceMapBinaryClosing3D;
import inra.ijpb.morphology.binary.DistanceMapBinaryDilation3D;
import inra.ijpb.morphology.binary.DistanceMapBinaryErosion3D;
import inra.ijpb.morphology.binary.DistanceMapBinaryOpening3D;
import inra.ijpb.util.IJUtils;

/**
 * Morphological filtering for 3D binary images, using ball-shaped structuring
 * element with user-specified radius.
 * 
 * Morphological operations are computed using thresholding distance maps.
 * 
 * @see inra.ijpb.morphology.BinaryMorphology
 * 
 * @author David Legland
 *
 */

public class BinaryMorphologicalFilter3DPlugin implements PlugIn 
{
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
		
		public ImageStack process(ImageStack image, double radius)
		{
			switch(this) 
			{
			case EROSION:
			{
				DistanceMapBinaryErosion3D algo = new DistanceMapBinaryErosion3D(radius);
				DefaultAlgoListener.monitor(algo);
				return algo.processBinary(image);
			}
			case DILATION:
			{
				DistanceMapBinaryDilation3D algo = new DistanceMapBinaryDilation3D(radius);
				DefaultAlgoListener.monitor(algo);
				return algo.processBinary(image);
			}
			case OPENING:
			{
				DistanceMapBinaryOpening3D algo = new DistanceMapBinaryOpening3D(radius);
				DefaultAlgoListener.monitor(algo);
				return algo.processBinary(image);
			}
			case CLOSING:
			{
				DistanceMapBinaryClosing3D algo = new DistanceMapBinaryClosing3D(radius);
				DefaultAlgoListener.monitor(algo);
				return algo.processBinary(image);
			}
			default:
			{
				throw new RuntimeException("Can not manange this Operation: " + label);
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
	
	
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg) 
	{
		if ( IJ.getVersion().compareTo("1.48a") < 0 )
		{
			IJ.error( "Morphological Filter 3D", "ERROR: detected ImageJ version " + IJ.getVersion()  
					+ ".\nThis plugin requires version 1.48a or superior, please update ImageJ!" );
			return;
		}
		
		ImagePlus imagePlus = WindowManager.getCurrentImage();
		if (imagePlus == null) 
		{
			IJ.error("No image", "Need at least one image to work");
			return;
		}
		
		// create the dialog
		GenericDialog gd = new GenericDialog("Morphological Filters (3D)");
		
		gd.addChoice("Operation", Operation.getAllLabels(), Operation.DILATION.toString());
		gd.addNumericField("Radius (in voxels)", 2, 1);
		gd.addCheckbox("Show Element", false);
		
		// Could also add an option for the type of operation
		gd.showDialog();
		
		if (gd.wasCanceled())
			return;
		
		long t0 = System.currentTimeMillis();

		// extract chosen parameters
		Operation op = Operation.fromLabel(gd.getNextChoice());
		double radius = gd.getNextNumber();
		boolean showStrel = gd.getNextBoolean();
		
		// Eventually display the structuring element used for processing 
		if (showStrel)
		{
			showStrelImage(radius);
		}
		
		// Execute core of the plugin
		ImagePlus resPlus = process(imagePlus, op, radius);

		if (resPlus == null)
			return;

		// Display the result image
		resPlus.show();
		resPlus.setSlice(imagePlus.getCurrentSlice());

		// Display elapsed time
		long t1 = System.currentTimeMillis();
		IJUtils.showElapsedTime(op.toString(), t1 - t0, imagePlus);
	}


	/**
	 * Displays the current structuring element in a new ImagePlus. 
	 * @param strel the 3D structuring element to display
	 */
	private void showStrelImage(double radius) 
	{
		// Size of the strel image (little bit larger than strel)
		int intRadius = (int) Math.ceil(radius);
		int sizeX = 2 * intRadius + 20; 
		int sizeY = 2 * intRadius + 20;
		int sizeZ = 2 * intRadius + 20;
		
		// Creates strel image by dilating a point
		ImageStack stack = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		stack.setVoxel(sizeX / 2, sizeY / 2, sizeZ / 2, 255);
		stack = BinaryMorphology.dilationBall(stack, radius);
		
		// Display strel image
		ImagePlus strelImage = new ImagePlus("Structuring Element", stack);
		strelImage.setSlice(((sizeZ - 1) / 2) + 1);
		strelImage.show();
	}

	/**
	 * Applies the specified morphological operation to the image, using a ball
	 * structuring element.
	 * 
	 * @param image
	 *            the input image (grayscale or color)
	 * @param op
	 *            the operation to apply
	 * @param radius
	 *            the radius of the ball used as structuring element
	 * @return the result of morphological operation applied to the input image
	 */
	public ImagePlus process(ImagePlus image, Operation op, double radius) 
	{
		// Check validity of parameters
		if (image == null)
			return null;
		
		// extract the input stack
		ImageStack inputStack = image.getStack();

		// apply morphological operation
		ImageStack resultStack = op.process(inputStack, radius);

		// create the new image plus from the processor
		String newName = image.getShortTitle() + "-" + op.toString();
		ImagePlus resultPlus = new ImagePlus(newName, resultStack);
		resultPlus.copyScale(image);
		
		// return the created array
		return resultPlus;
	}
}
