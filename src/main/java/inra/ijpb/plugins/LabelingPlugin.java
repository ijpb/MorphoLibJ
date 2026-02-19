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

import java.awt.Color;
import java.awt.image.ColorModel;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.binary.conncomp.FloodFillComponentsLabeling;
import inra.ijpb.binary.conncomp.FloodFillComponentsLabeling3D;
import inra.ijpb.color.ColorMaps;
import inra.ijpb.color.ColorMaps.CommonLabelMaps;
import inra.ijpb.util.IJUtils;

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

        // initializations
        String newName = imagePlus.getShortTitle() + "-lbl";
        ImagePlus resultPlus;
        
        // create default label Color Model
        byte[][] colorMap = CommonLabelMaps.GLASBEY_BRIGHT.computeLut(255, false);
        ColorModel cm = ColorMaps.createColorModel(colorMap, Color.BLACK);
        
        // Compute components labeling
        long t0 = System.nanoTime();
        try
        {
            // Dispatch processing depending on input image dimensionality
            if (imagePlus.getStackSize() == 1)
            {
                FloodFillComponentsLabeling algo = new FloodFillComponentsLabeling(connValue, bitDepth);
                DefaultAlgoListener.monitor(algo);
                FloodFillComponentsLabeling.Result res = algo.computeResult(imagePlus.getProcessor());
                resultPlus = new ImagePlus(newName, res.labelMap);
                // uses colored colormap
                resultPlus.getProcessor().setColorModel(cm);
                resultPlus.setDisplayRange(0, Math.max(res.nLabels, 255));
            } 
            else
            {
                FloodFillComponentsLabeling3D algo = new FloodFillComponentsLabeling3D(connValue, bitDepth);
                DefaultAlgoListener.monitor(algo);
                FloodFillComponentsLabeling3D.Result res = algo.computeResult(imagePlus.getStack());
                resultPlus = new ImagePlus(newName, res.labelMap);
                // uses colored colormap
                resultPlus.getProcessor().setColorModel(cm);
                resultPlus.getStack().setColorModel(cm);
                resultPlus.setDisplayRange(0, Math.max(res.nLabels, 255));
            }
        } 
        catch (RuntimeException ex)
        {
            IJ.error("Components Labeling Error", ex.getMessage()
                    + "\nTry with larger data type (short or float)");
            return;
        }
        long t1 = System.nanoTime();
        
        // Display with same spatial calibration as original image
        resultPlus.copyScale(imagePlus);
        resultPlus.show();
        
        // For 3D images, select the same visible slice as original image
        if (!isPlanar)
        {
            resultPlus.setZ(imagePlus.getZ());
            resultPlus.setSlice(imagePlus.getCurrentSlice());
        }
        
        // show elapsed time
        IJUtils.showElapsedTime("Labeling", t1 - t0, resultPlus);
    }

}
