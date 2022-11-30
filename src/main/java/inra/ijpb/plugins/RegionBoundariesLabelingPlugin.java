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
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import inra.ijpb.label.conncomp.LabelBoundariesLabeling2D;

/**
 * @author David Legland
 *
 */
public class RegionBoundariesLabelingPlugin implements PlugIn {

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg0) {
		ImagePlus imagePlus = IJ.getImage();
		
		ImageStack stack = imagePlus.getStack();
		
		ImagePlus resultPlus;
		if( stack.getSize() > 1 )
		{
		    throw new RuntimeException("Requires a 2D image as input");
//			ImageStack boundaries = LabelImages.labelBoundaries(stack);
//		
//			String newName = imagePlus.getShortTitle() + "-bnd";
//			resultPlus = new ImagePlus(newName, boundaries);
		}
		else
		{
//			ImageProcessor boundaries = LabelImages.labelBoundaries(
//					stack.getProcessor( 1 ) );
			LabelBoundariesLabeling2D algo = new LabelBoundariesLabeling2D();
			ImageProcessor boundaries = algo.process(imagePlus.getProcessor()).boundaryLabelMap;

			String newName = imagePlus.getShortTitle() + "-bnd";
			resultPlus = new ImagePlus( newName, boundaries );
		}
		// Update meta information of result image
		resultPlus.copyScale(imagePlus);
		
		// Display with same settings as original image
		resultPlus.show();
		if (imagePlus.getStackSize() > 1) {
			resultPlus.setZ(imagePlus.getZ());
			resultPlus.setSlice(imagePlus.getCurrentSlice());
		}
	}

}
