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
import inra.ijpb.color.CommonColors;
import inra.ijpb.data.image.ColorImages;
import inra.ijpb.util.IJUtils;

import java.awt.Color;


/**
 * @author David Legland
 *
 */
public class BinaryOverlayPlugin implements PlugIn {

	// ====================================================
	// Calling functions 
	
	public void run(String arg) 
    {
        // create the list of image names
        String[] imageNames = IJUtils.getOpenImageNames();
        if (imageNames.length == 0)
        {
            IJ.error("No image", "Need at least one image to work");
            return;
        }

        // name of current image
        String currentImageName = IJ.getImage().getTitle();
		
		// create the dialog
		GenericDialog gd = new GenericDialog("Binary Overlay");
		gd.addChoice("Reference Image:", imageNames, currentImageName);
		gd.addChoice("Binary Mask:", imageNames, currentImageName);
		gd.addChoice("Overlay Color:", CommonColors.getAllLabels(), CommonColors.RED.getLabel());
		
		gd.showDialog();
		
		if (gd.wasCanceled())
			return;
		
		// Extract reference image
		int refImageIndex = gd.getNextChoiceIndex();
		ImagePlus refImage = WindowManager.getImage(refImageIndex+1);
		
		// Extract mask image
		int maskIndex = gd.getNextChoiceIndex();
		ImagePlus maskImage = WindowManager.getImage(maskIndex+1);

		// Extract overlay color
		String colorName = gd.getNextChoice();
		Color color = CommonColors.fromLabel(colorName).getColor();
		
		// Call binary overlay conversion
		ImagePlus resultPlus = ColorImages.binaryOverlay(refImage, maskImage, color);
		resultPlus.show();
		
		// set up display 
		if (refImage.getStackSize() > 1) {
			resultPlus.setSlice(refImage.getCurrentSlice());
		}
	}
}
