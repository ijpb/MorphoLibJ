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
import java.awt.geom.Point2D;
import java.util.Map;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import inra.ijpb.geometry.PointPair2D;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.region2d.MaxFeretDiameter;
import inra.ijpb.util.IJUtils;

/**
 * Computes Maximum Feret Diameters of a binary or label image.
 * 
 * Mainly used for debugging puproses
 *  
 * @author dlegland
 *
 */
public class MaxFeretDiameterPlugin implements PlugIn
{
    // ====================================================
    // Global Constants
    
    
    // ====================================================
    // Class variables
    
   /**
     * When this options is set to true, information messages are displayed on
     * the console. 
     */
    public boolean debug  = false;
    
    
    // ====================================================
    // Calling functions 
    
	/* (non-Javadoc)
     * @see ij.plugin.PlugIn#run(java.lang.String)
     */
    @Override
	public void run(String arg)
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
		// - image to display results
		GenericDialog gd = new GenericDialog("Max. Feret Diameter");
		gd.addChoice("Label Image:", imageNames, currentImageName);
		// Set Chessknight weights as default
		gd.addCheckbox("Show Overlay Result", true);
		gd.addChoice("Image to overlay:", imageNames, currentImageName);
		gd.showDialog();
		
		if (gd.wasCanceled())
			return;
		
		// set up current parameters
		int labelImageIndex = gd.getNextChoiceIndex();
		ImagePlus labelPlus = WindowManager.getImage(labelImageIndex+1);
        boolean overlay = gd.getNextBoolean();
		int resultImageIndex = gd.getNextChoiceIndex();
		
		// check if image is a label image
		if (!LabelImages.isLabelImageType(labelPlus))
		{
			IJ.showMessage("Input image should be a label image");
			return;
		}
		
		// Compute max Feret diameters
		MaxFeretDiameter op = new MaxFeretDiameter();
		Map<Integer, PointPair2D> maxDiamsMap = op.analyzeRegions(labelPlus);
		
		// Display the result Table
        ResultsTable results = op.createTable(maxDiamsMap);

        // create string for indexing results
		String tableName = labelPlus.getShortTitle() + "-FeretDiams"; 
    
		// show result
		results.show(tableName);

		// Optionally display overlay
		if (overlay)
		{
			ImagePlus resultImage = WindowManager.getImage(resultImageIndex + 1);
			drawDiameters(resultImage, maxDiamsMap);
		}
    }

    /**
	 * Draws the diameters on a target image.
	 * 
	 * @param target
	 *            the image to draw the diameters on
	 * @param geodDiams
	 *            the diameters to draw
	 */
	public void drawDiameters(ImagePlus target, Map<Integer, PointPair2D> geodDiams)
	{
		Overlay overlay = new Overlay();
		Calibration calib = target.getCalibration();
		
		for (PointPair2D result : geodDiams.values())
		{
			Roi roi = createDiametersRoi(result, calib);
		    roi.setStrokeColor(Color.BLUE);
		    overlay.add(roi);
		}

		target.setOverlay(overlay);
	}

	private Roi createDiametersRoi(PointPair2D pointPair, Calibration calib)
	{
		if (pointPair == null)
    	{
    		return null;
    	}

		Point2D p1 = calibToPixel(pointPair.p1, calib);
		Point2D p2 = calibToPixel(pointPair.p2, calib);
		
		// Convert to Polyline ROI
		float[] x = new float[2];
        float[] y = new float[2];
        x[0] = (float) p1.getX();
        y[0] = (float) p1.getY();
        x[1] = (float) p2.getX();
        y[1] = (float) p2.getY();
        return new PolygonRoi(x, y, 2, Roi.POLYLINE);
	}
	
	private Point2D calibToPixel(Point2D point, Calibration calib)
	{
		double x = (point.getX() - calib.xOrigin) / calib.pixelWidth;
		double y = (point.getY() - calib.yOrigin) / calib.pixelHeight;
		return new Point2D.Double(x, y);
	}
}
