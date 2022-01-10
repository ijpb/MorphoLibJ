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
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.binary.distmap.ChamferMask3D;
import inra.ijpb.binary.distmap.ChamferMasks3D;
import inra.ijpb.label.distmap.DistanceTransform3D;
import inra.ijpb.label.distmap.ChamferDistanceTransform3DFloat;
import inra.ijpb.label.distmap.ChamferDistanceTransform3DShort;
import inra.ijpb.data.image.Images3D;
import inra.ijpb.util.IJUtils;

/**
 * Compute distance map, with possibility to choose chamfer weights, result 
 * type, and to normalize result or not.
 *
 * @author dlegland
 *
 */
public class ChamferDistanceMap3DPlugin implements PlugIn 
{
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg) {
		
		ImagePlus imagePlus = WindowManager.getCurrentImage();
		if (imagePlus == null) 
		{
			IJ.error("No image", "Need at least one image to work");
			return;
		}
		
		// Create a new generic dialog with appropriate options
    	GenericDialog gd = new GenericDialog("Chamfer Distance Map 3D");
    	gd.addChoice("Distances", ChamferMasks3D.getAllLabels(), 
    			ChamferMasks3D.SVENSSON_3_4_5_7.toString());			
		String[] outputTypes = new String[] { "16 bits", "32 bits" };
    	gd.addChoice("Output Type", outputTypes, outputTypes[0]);
    	gd.addCheckbox("Normalize weights", true);	
        gd.showDialog();
        
    	// test cancel  
    	if (gd.wasCanceled())
    		return;

    	// set up current parameters
    	String weightLabel = gd.getNextChoice();
    	boolean floatProcessing = gd.getNextChoiceIndex() == 1;
    	boolean normalize = gd.getNextBoolean();

    	// identify which weights should be used
    	ChamferMasks3D weightsOption = ChamferMasks3D.fromLabel(weightLabel);
		ChamferMask3D weights = weightsOption.getMask();

    	long t0 = System.currentTimeMillis();

		// Choose the appropriate algorithm based on output type and number of
		// chamfer weights
    	DistanceTransform3D algo;
    	if (floatProcessing)
    	{
    		algo = new ChamferDistanceTransform3DFloat(weights, normalize);
    	} 
    	else
    	{
    		algo = new ChamferDistanceTransform3DShort(weights, normalize);
        }
		DefaultAlgoListener.monitor(algo);
    	
    	ImageStack image = imagePlus.getStack();
    	ImageStack res = algo.distanceMap(image);

		if (res == null)
			return;

		String newName = imagePlus.getShortTitle() + "-dist";
		ImagePlus resPlus = new ImagePlus(newName, res);
		
		// calibrate display range of distances
		double[] distExtent = Images3D.findMinAndMax(resPlus);
		resPlus.setDisplayRange(0, distExtent[1]);
		
		// keep spatial calibration
		resPlus.copyScale(imagePlus);

		// Display the result image
		resPlus.show();
		resPlus.setSlice(imagePlus.getCurrentSlice());

		// Display elapsed time
		long t1 = System.currentTimeMillis();
		IJUtils.showElapsedTime("distance map", t1 - t0, imagePlus);
	}
}
