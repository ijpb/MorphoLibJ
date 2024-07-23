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
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.binary.BinaryImages;

/**
 * Select binary particles in a 2D or 3D image based on number of pixels.
 * 
 * @see AreaOpeningPlugin
 * 
 * @author David Legland
 *
 */
public class SizeOpeningPlugin implements PlugIn
{
	@Override
	public void run(String arg0)
	{
		ImagePlus imagePlus = IJ.getImage();
        
        // create the dialog, with operator options
		boolean isPlanar = imagePlus.getStackSize() == 1; 
		String title = "Size Opening 2D/3D";
        GenericDialog gd = new GenericDialog(title);
        String label = isPlanar ? "Min Pixel Number:" : "Min Voxel Number:";
        gd.addNumericField(label, 100, 0);
        gd.showDialog();
        
        // If cancel was clicked, do nothing
        if (gd.wasCanceled())
            return;

        int nPixelMin = (int) gd.getNextNumber();
        
        ImagePlus resultPlus;
        String newName = imagePlus.getShortTitle() + "-sizeOpen";
        
        if (isPlanar) 
        {
            ImageProcessor image = imagePlus.getProcessor();
            ImageProcessor result = null;
    		try 
    		{
    			result = BinaryImages.areaOpening(image, nPixelMin);
    		}
    		catch(RuntimeException ex)
    		{
    			IJ.error("Too many particles", ex.getMessage());
    			return;
    		}

            if (!(result instanceof ColorProcessor))
    			result.setLut(image.getLut());
            resultPlus = new ImagePlus(newName, result);    		
        }
        else
        {
            ImageStack image = imagePlus.getStack();
            ImageStack result = null;
    		try 
    		{
    			result = BinaryImages.volumeOpening(image, nPixelMin);
    		}
    		catch(RuntimeException ex)
    		{
    			IJ.error("Too many particles", ex.getMessage());
    			return;
    		}
        	result.setColorModel(image.getColorModel());
            resultPlus = new ImagePlus(newName, result);
        }
        
		resultPlus.copyScale(imagePlus);
        resultPlus.show();
		
		if (imagePlus.getStackSize() > 1) {
			resultPlus.setSlice(imagePlus.getCurrentSlice());
		}
	}
	

}
