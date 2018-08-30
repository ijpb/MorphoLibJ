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
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.measure.region2d.Convexity;
import inra.ijpb.util.IJUtils;

/**
 * Computes the Convex equivalent of a binary image.
 * 
 * @see inra.ijpb.measure.region2d.Convexity
 */
public class ConvexifyPlugin implements PlugIn
{

    @Override
    public void run(String arg0)
    {
        ImagePlus imagePlus = IJ.getImage();
        
        // Check Input data validity
        ImagePlus resultPlus;
        if (imagePlus.getStackSize() > 1)
        {
            IJ.showMessage("Invalid Input", "Requires a binary 2D image as input");
            return;
        }
        ImageProcessor image = imagePlus.getProcessor();
        if (image instanceof ColorProcessor)
        {
            IJ.showMessage("Invalid Input", "Requires a binary 2D image as input");
            return;
        }

        // Process image
        long t0 = System.currentTimeMillis();
        ImageProcessor result = Convexity.convexify(image);
        long elapsedTime = System.currentTimeMillis() - t0;

        // Copy input image meta-data
        result.setLut(image.getLut());
        String newName = imagePlus.getShortTitle() + "-convex";
        resultPlus = new ImagePlus(newName, result);

        resultPlus.copyScale(imagePlus);
        resultPlus.show();

        // Show elapsed time
        IJUtils.showElapsedTime("Convexify", elapsedTime, imagePlus);
    }
}
