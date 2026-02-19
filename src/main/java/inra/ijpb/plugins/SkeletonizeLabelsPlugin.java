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
import ij.process.ImageProcessor;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.binary.skeleton.ImageJSkeleton;
import inra.ijpb.label.LabelImages;
import inra.ijpb.util.IJUtils;

/**
 * Computes the skeleton of each region within a 2D label map.
 * 
 * @author dlegland
 *
 */
public class SkeletonizeLabelsPlugin implements PlugIn
{
	@Override
	public void run(String arg)
	{
		// retrieve current image
		ImagePlus imagePlus = IJ.getImage();
		
		if (imagePlus.getStackSize() > 1)
		{
			IJ.showMessage("Requires a planar image as input");
			return;
		}
		
		// check if image is a label image
		if (!LabelImages.isLabelImageType(imagePlus))
		{
			IJ.showMessage("Input image should be a label image");
			return;
		}
		
		ImageProcessor labelMap = imagePlus.getProcessor();
		
		// compute skeleton image
		ImageJSkeleton skeletonize = new ImageJSkeleton();
		DefaultAlgoListener.monitor(skeletonize);
		long start = System.nanoTime();
		ImageProcessor skeleton = skeletonize.process(labelMap);
		
		// Keep same color model
		skeleton.setColorModel(labelMap.getColorModel());
		
		// Elapsed time, displayed in milli-seconds
		long finalTime = System.nanoTime();
		float elapsedTime = (finalTime - start) / 1000000.0f;
		
		// create resulting image
		String title = imagePlus.getShortTitle() + "-skel";
		ImagePlus skeletonPlus = new ImagePlus(title, skeleton);

		// Display with same spatial calibration as original image
		skeletonPlus.copyScale(imagePlus);
		skeletonPlus.show();
		
		IJUtils.showElapsedTime("Skeletonize Labels", elapsedTime, imagePlus); 
	}

}
