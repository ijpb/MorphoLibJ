/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2026 INRA.
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
import ij.plugin.PlugIn;
import inra.ijpb.label.LabelImages;

/**
 * Removes all the labels in a 2D/3D image but the largest one. 
 * The result is displayed in a new ImagePlus.
 * This can be used to automatically select the region of interest within the image. 
 * 
 * @author David Legland
 *
 */
public class KeepLargestLabelPlugin implements PlugIn {

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg0) {
		ImagePlus imagePlus = IJ.getImage();
		
		ImagePlus resultPlus;
		try 
		{
			resultPlus = LabelImages.keepLargestLabel(imagePlus);
		}
		catch(RuntimeException ex)
		{
			// can throw an exception if no region is found
			IJ.error("MorphoLibJ Error", ex.getMessage());
			return;
		}
		
		// Display with same settings as original image
		resultPlus.show();
		if (imagePlus.getStackSize() > 1) {
			resultPlus.setZ(imagePlus.getZ());
			resultPlus.setSlice(imagePlus.getCurrentSlice());
		}
	}
}
