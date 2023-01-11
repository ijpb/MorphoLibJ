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
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.color.CommonColors;
import inra.ijpb.color.ColorMaps.CommonLabelMaps;
import inra.ijpb.label.LabelImages;

import java.awt.Color;

/**
 * Creates a new Color Image that associate a given color to each label of the input image.
 * Opens a dialog to choose a colormap, a background color, and a shuffle option. 
 * Preview option is available.
 * Note that when shuffle is activated, result may be different from preview.
 *  
 * @author David Legland
 *
 */
public class LabelToRgbPlugin implements PlugIn
{
	@Override
	public void run(String arg)
	{
		ImagePlus imagePlus = IJ.getImage();

		int maxLabel = computeMaxLabel(imagePlus);
		
		// Create a new generic dialog with appropriate options
    	GenericDialog gd = new GenericDialog("Labels To RGB");
    	gd.addChoice("Colormap", CommonLabelMaps.getAllLabels(), 
    			CommonLabelMaps.GOLDEN_ANGLE.getLabel());
    	gd.addChoice("Background", CommonColors.getAllLabels(), CommonColors.WHITE.getLabel());
    	gd.addCheckbox("Shuffle", true);
    	gd.showDialog();
		
    	// test cancel  
    	if (gd.wasCanceled()) 
    		return;

    	// Create a new LUT from info in dialog
		String lutName = gd.getNextChoice();
		String bgColorName = gd.getNextChoice();
		Color bgColor = CommonColors.fromLabel(bgColorName).getColor();
		boolean shuffleLut = gd.getNextBoolean();

		// Create a new LUT from info in dialog
		byte[][] lut = CommonLabelMaps.fromLabel(lutName).computeLut(maxLabel, shuffleLut);
    	
		// Create a new RGB image from index image and LUT options
		ImagePlus resPlus = LabelImages.labelToRgb(imagePlus, lut, bgColor);
    	
		// dispay result image
		resPlus.copyScale(imagePlus);
		resPlus.show();
    	if (imagePlus.getStackSize() > 1) 
    	{
    		resPlus.setSlice(imagePlus.getCurrentSlice());
    	}
	}

	/**
	 * Computes the maximum value in the input image or stack, in order to 
	 * initialize colormap with the appropriate number of colors. 
	 */
	private final static int computeMaxLabel(ImagePlus imagePlus) 
	{
		if (imagePlus.getImageStackSize() == 1) 
		{
			return computeMaxLabel(imagePlus.getProcessor());
		}
		else 
		{
			 return computeMaxLabel(imagePlus.getStack());
		}
	}

	private static final int computeMaxLabel(ImageProcessor image) 
	{
		int labelMax = 0;
		if (image instanceof FloatProcessor)
		{
			for (int i = 0; i < image.getPixelCount(); i++) 
			{
				labelMax = Math.max(labelMax, (int) image.getf(i));
			}
		} 
		else
		{
			for (int i = 0; i < image.getPixelCount(); i++) 
			{
				labelMax = Math.max(labelMax, image.get(i));
			}
		}
		
		return labelMax;
	}
	
	private static final int computeMaxLabel(ImageStack image) 
	{
		int labelMax = 0;
		for (int i = 1; i <= image.getSize(); i++) 
		{
			ImageProcessor slice = image.getProcessor(i);
			labelMax = Math.max(labelMax, computeMaxLabel(slice));
		}
		
		return labelMax;
	}
}
