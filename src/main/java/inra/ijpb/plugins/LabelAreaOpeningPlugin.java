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
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import inra.ijpb.label.LabelImages;

/**
 * @author David Legland
 *
 */
public class LabelAreaOpeningPlugin implements PlugIn {

	@Override
	public void run(String arg0) {
		ImagePlus imagePlus = IJ.getImage();
        
        // create the dialog, with operator options
		boolean isPlanar = imagePlus.getStackSize() == 1; 
		String title = "Label Size Opening";
        GenericDialog gd = new GenericDialog(title);
        String label = isPlanar ? "Min Pixel Number:" : "Min Voxel Number:";
        gd.addNumericField(label, 100, 0);
        gd.showDialog();
        
        // If cancel was clicked, do nothing
        if (gd.wasCanceled())
            return;

        int nPixelMin = (int) gd.getNextNumber();
        
        // Apply size opening using IJPB library
        ImagePlus resultPlus = LabelImages.sizeOpening(imagePlus, nPixelMin);

        // Display image, and choose same slice as original
		resultPlus.show();
		if (imagePlus.getStackSize() > 1) {
			resultPlus.setSlice(imagePlus.getCurrentSlice());
		}
	}
	

}
