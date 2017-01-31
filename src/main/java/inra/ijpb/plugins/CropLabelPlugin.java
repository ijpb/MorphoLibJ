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
 * Crops a label image and binarize it to contain only the specified label.
 */
public class CropLabelPlugin implements PlugIn {

    // ====================================================
    // Global Constants

    // ====================================================
    // Class variables
    
   /**
     * When this options is set to true, information messages are displayed on
     * the console. 
     */
    public boolean debug  = false;
    
    
    // ====================================================
    // Calling functions 
   

	/* (non-Javadoc)
     * @see ij.plugin.PlugIn#run(java.lang.String)
     */
    public void run(String args) 
    {
        ImagePlus imagePlus = IJ.getImage();
		
        // create the dialog, with operator options
        GenericDialog gd = new GenericDialog("Crop Label");
        gd.addNumericField("Label", 1, 0);
        gd.addNumericField("Border pixels", 1, 0);
        gd.showDialog();
        
        // If cancel was clicked, do nothing
        if (gd.wasCanceled())
            return;
        
        // extract label index, and number of pixel border to add
        int label = (int) gd.getNextNumber();
        int border = (int) gd.getNextNumber();

        ImagePlus croppedPlus = LabelImages.cropLabel(imagePlus, label, border);

        // display and adapt settings
        croppedPlus.show();
        if (imagePlus.getStackSize() > 1) 
        {
        	croppedPlus.setSlice(croppedPlus.getStackSize() / 2);
        }
    }
}
