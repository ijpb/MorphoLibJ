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

import java.util.Map;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.binary.distmap.ChamferMask2D;
import inra.ijpb.binary.distmap.ChamferMasks2D;
import inra.ijpb.binary.skeleton.ImageJSkeleton;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.region2d.GeodesicDiameter;
import inra.ijpb.morphology.Strel;
import inra.ijpb.morphology.filter.Dilation;
import inra.ijpb.util.IJUtils;

/**
 * First computes the skeleton of each region, then computes the geodesic diameter of the skeleton(s).
 * A dilation step may be added before computing diameter.
 * 
 * @author dlegland
 *
 */
public class SkeletonGeodesicDiameterPlugin implements PlugIn
{
	// ====================================================
	// Calling functions 
	
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg0) 
	{
        // create the list of image names
        String[] imageNames = IJUtils.getOpenImageNames();
        if (imageNames.length == 0)
        {
            IJ.error("No image", "Need at least one image to work");
            return;
        }

		// name of current image
		String currentImageName = IJ.getImage().getTitle();
		
		// Open a dialog to choose:
		// - a label image
		// - a set of weights
		GenericDialog gd = new GenericDialog("Geodesic Diameter");
		gd.addChoice("Input Label Image:", imageNames, currentImageName);
        gd.addCheckbox("Dilate skeleton", false);
		// Set Chess Knight weights as default
		gd.addChoice("Chamfer Mask:", ChamferMasks2D.getAllLabels(), 
				ChamferMasks2D.CHESSKNIGHT.toString());
		gd.addCheckbox("Show Overlay Result", true);
		gd.addChoice("Image to overlay:", imageNames, currentImageName);
		gd.addCheckbox("Export to ROI Manager", true);
		gd.showDialog();
		
		if (gd.wasCanceled())
			return;
		
		// set up current parameters
		int labelImageIndex = gd.getNextChoiceIndex();
		ImagePlus labelPlus = WindowManager.getImage(labelImageIndex + 1);
		boolean dilateSkeleton = gd.getNextBoolean();
        ChamferMask2D chamferMask = ChamferMasks2D.fromLabel(gd.getNextChoice()).getMask();
        boolean overlayPaths = gd.getNextBoolean();
		int resultImageIndex = gd.getNextChoiceIndex();
		boolean createPathRois = gd.getNextBoolean();
		
		// check if image is a label image
		if (!LabelImages.isLabelImageType(labelPlus))
		{
			IJ.showMessage("Input image should be a label image");
			return;
		}
		
		ImageProcessor labelMap = labelPlus.getProcessor();
		
		// compute skeleton image
        ImageJSkeleton skeletonize = new ImageJSkeleton();
		DefaultAlgoListener.monitor(skeletonize);
		ImageProcessor skeleton = skeletonize.process(labelMap);
		
		// optional dilation, using small structuring element
		if (dilateSkeleton)
		{
		    Strel strel = Strel.Shape.SQUARE.fromDiameter(3);
		    skeleton = new Dilation(strel).process(skeleton);
		    
		    // apply mask with original region(s)
		    for (int y = 0; y < labelMap.getHeight(); y++)
		    {
	            for (int x = 0; x < labelMap.getWidth(); x++)
	            {
	                int label = (int) skeleton.getf(x, y);
	                if (label == 0) continue;
	                if (((int) labelMap.getf(x, y)) != label)
	                {
	                    skeleton.set(x, y, 0);
	                }
	            }
		    }
		}
        
		// Create and configure the class for computing geodesic diameter
		GeodesicDiameter algo = new GeodesicDiameter(chamferMask);
		algo.setComputePaths(overlayPaths || createPathRois);
		DefaultAlgoListener.monitor(algo);
		
		// Compute geodesic diameters, using floating-point calculations
		long start = System.nanoTime();
		Map<Integer, GeodesicDiameter.Result> geodDiams = algo.analyzeRegions(skeleton, labelPlus.getCalibration());

		// Elapsed time, displayed in milli-seconds
		long finalTime = System.nanoTime();
		float elapsedTime = (finalTime - start) / 1000000.0f;

		// display the result table
		ResultsTable table = algo.createTable(geodDiams);  
		String tableName = labelPlus.getShortTitle() + "-SkelGeodDiam"; 
		table.show(tableName);

		// Optional processing of geodesic paths
		if (overlayPaths || createPathRois)
		{
    		boolean validPaths = true;
    		for (GeodesicDiameter.Result geodDiam : geodDiams.values())
    		{
    			if (Double.isInfinite(geodDiam.diameter))
    			{
    				validPaths = false;
    				break;
    			}
    		}
		
    		if (!validPaths)
    		{
				IJ.showMessage("Geodesic Diameter Warning", "Some geodesic diameters are infinite,\n"
						+ "meaning that some particles are not connected.\n" 
						+ "Maybe labeling was not performed, or label image was cropped?");
    		}

    		// Check if results must be displayed on an image
    		if (overlayPaths) 
    		{
    			// New image for displaying geometric overlays
    			ImagePlus resultImage = WindowManager.getImage(resultImageIndex + 1);
    			GeodesicDiameterPlugin.drawPaths(resultImage, geodDiams);
    		}

    		if (createPathRois)
    		{
    		    GeodesicDiameterPlugin.createPathRois(labelPlus, geodDiams);
    		}
		}
		
		IJUtils.showElapsedTime("Skel. Geod. Diam.", elapsedTime, labelPlus); 
	}
}
