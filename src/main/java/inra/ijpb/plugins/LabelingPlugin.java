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
import inra.ijpb.binary.BinaryImages;

/**
 * Computes label image of connected components in a binary planar image or 3D
 * stack. The dialog provides an option to choose data type of output image.
 * 
 * @author David Legland
 * 
 */
public class LabelingPlugin implements PlugIn 
{
	// Widget labels and corresponding values of output type option
	private final static String[] resultBitDepthLabels = {"8 bits", "16 bits", "float"};
	private final static int[] resultBitDepthList = {8, 16, 32}; 

	@Override
	public void run(String arg)
	{
		ImagePlus imagePlus = IJ.getImage();
		
		boolean isPlanar = imagePlus.getStackSize() == 1;
		
		// Display dialog options
		GenericDialog gd = new GenericDialog("Connected Components Labeling");
		String[] connLabels = isPlanar ? Connectivity2D.getAllLabels() : Connectivity3D.getAllLabels();
		gd.addChoice("Connectivity", connLabels, connLabels[0]);
		gd.addChoice("Type of result", resultBitDepthLabels, resultBitDepthLabels[1]);
		
		// wait for user answer
		gd.showDialog();
		if (gd.wasCanceled()) 
			return;

		// parses dialog options
		String str = gd.getNextChoice();
		int bitDepth = resultBitDepthList[gd.getNextChoiceIndex()];
		int connValue = isPlanar ? Connectivity2D.fromLabel(str).getValue()
				: Connectivity3D.fromLabel(str).getValue();

		// Compute components labeling
		ImagePlus resultPlus;
		try
		{ 	
			resultPlus = BinaryImages.componentsLabeling(imagePlus, connValue, bitDepth);
		} 
		catch(RuntimeException ex)
		{
			IJ.error("Components Labeling Error", ex.getMessage() + "\nTry with larger data type (short or float)");
			return;
		}
		
		// update meta information of result image
		String newName = imagePlus.getShortTitle() + "-lbl";
		resultPlus.setTitle(newName);
		resultPlus.copyScale(imagePlus);
		
		// Display with same settings as original image
		resultPlus.show();
		
		// For 2D images, select the same visible slice as original image
		if (!isPlanar)
		{
			resultPlus.setZ(imagePlus.getZ());
			resultPlus.setSlice(imagePlus.getCurrentSlice());
		}
	}

}
