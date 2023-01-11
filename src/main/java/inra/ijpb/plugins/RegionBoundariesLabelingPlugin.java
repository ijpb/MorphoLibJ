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

import java.awt.Color;
import java.awt.image.ColorModel;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import inra.ijpb.color.ColorMaps;
import inra.ijpb.color.ColorMaps.CommonLabelMaps;
import inra.ijpb.label.conncomp.LabelBoundariesLabeling2D;
import inra.ijpb.label.conncomp.LabelBoundariesLabeling3D;

/**
 * @author David Legland
 *
 */
public class RegionBoundariesLabelingPlugin implements PlugIn 
{
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg0) 
	{
		ImagePlus imagePlus = IJ.getImage();
		
        String newName = imagePlus.getShortTitle() + "-bnd";
		ImageStack stack = imagePlus.getStack();
		
        // create default label Color Model
        byte[][] colorMap = CommonLabelMaps.GLASBEY_BRIGHT.computeLut(255, false);
        ColorModel cm = ColorMaps.createColorModel(colorMap, Color.BLACK);
        
		ImagePlus resultPlus;
        if (stack.getSize() == 1)
        {
            LabelBoundariesLabeling2D algo = new LabelBoundariesLabeling2D();
            LabelBoundariesLabeling2D.Result res = algo.process(imagePlus.getProcessor());
            ImageProcessor boundaries = res.boundaryLabelMap;
            
            resultPlus = new ImagePlus(newName, boundaries);
            
            // uses colored colormap
            resultPlus.getProcessor().setColorModel(cm);
            resultPlus.setDisplayRange(0, Math.max(res.boundaries.size(), 255));

            IJ.log("Boundary Set result:");
            IJ.log(res.boundaries.toString());
        }
        else
        {
            LabelBoundariesLabeling3D algo = new LabelBoundariesLabeling3D();
            LabelBoundariesLabeling3D.Result res = algo.process(imagePlus.getStack());
            ImageStack boundaries = res.boundaryLabelMap;
            
            resultPlus = new ImagePlus(newName, boundaries);
            
            // uses colored colormap
            resultPlus.getProcessor().setColorModel(cm);
            resultPlus.getStack().setColorModel(cm);
            resultPlus.setDisplayRange(0, Math.max(res.boundaries.size(), 255));

            IJ.log("Boundary Set result:");
            IJ.log(res.boundaries.toString());
        }
		
		// Update meta information of result image
		resultPlus.copyScale(imagePlus);
		
		// Display with same settings as original image
		resultPlus.show();
		if (imagePlus.getStackSize() > 1)
		{
			resultPlus.setZ(imagePlus.getZ());
			resultPlus.setSlice(imagePlus.getCurrentSlice());
		}
	}

}
