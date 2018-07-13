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


import java.awt.Color;
import java.util.Map;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import inra.ijpb.geometry.Box2D;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.region2d.BoundingBox;

public class BoundingBoxPlugin implements PlugIn
{
    // ====================================================
    // Global Constants
    
    
    // ====================================================
    // Class variables
 
	// ====================================================
    // Calling functions 
    
	/* (non-Javadoc)
     * @see ij.plugin.PlugIn#run(java.lang.String)
     */
	public void run(String args)
	{
		// Open a dialog to choose:
		// - a label image
		// - a set of weights
		int[] indices = WindowManager.getIDList();
		if (indices==null)
		{
			IJ.error("No image", "Need at least one image to work");
			return;
		}
		
		// create the list of image names
		String[] imageNames = new String[indices.length];
		for (int i=0; i<indices.length; i++)
		{
			imageNames[i] = WindowManager.getImage(indices[i]).getTitle();
		}
		
		// name of selected image
		String selectedImageName = IJ.getImage().getTitle();

		// create the dialog
		GenericDialog gd = new GenericDialog("Bounding Box");
		gd.addChoice("Label Image:", imageNames, selectedImageName);
		gd.addCheckbox("Show Overlay Result", true);
		gd.addChoice("Image to overlay:", imageNames, selectedImageName);
		gd.showDialog();
		
		if (gd.wasCanceled())
			return;
		
		// set up current parameters
		int labelImageIndex = gd.getNextChoiceIndex();
		ImagePlus labelImage = WindowManager.getImage(labelImageIndex + 1);
		boolean showOverlay = gd.getNextBoolean();
		int resultImageIndex = gd.getNextChoiceIndex();
		
		// check if image is a label image
		if (!LabelImages.isLabelImageType(labelImage))
		{
            IJ.showMessage("Input image should be a label image");
            return;
        }

        // Execute the plugin
		BoundingBox op = new BoundingBox();
		Map<Integer, Box2D> boxes = op.analyzeRegions(labelImage);
        ResultsTable results = op.createTable(boxes);
        
		// show result
    	String tableName = labelImage.getShortTitle() + "-BBox"; 
    	results.show(tableName);
    	
		// Check if results must be displayed on an image
		if (showOverlay)
		{
			// find image for displaying geometric overlays
			ImagePlus resultImage = WindowManager.getImage(resultImageIndex + 1);
			showResultsAsOverlay(boxes, resultImage);
		}
    }
	
	/**
	 * Display the result of maximal inscribed circle extraction as overlay on a
	 * given image.
	 * 
	 * @param target
	 *            the ImagePlus used to display result
	 * @param table
	 *            the ResultsTable containing columns "xi", "yi" and "Radius"
	 * @param the
	 *            resolution in each direction
	 */
	private void showResultsAsOverlay(Map<Integer, Box2D> results, ImagePlus target)	
	{
		// get spatial calibration of target image
		Calibration calib = target.getCalibration();
		
		// create overlay
		Overlay overlay = new Overlay();
		Roi roi;
		
		// add each box to the overlay
		for (int label : results.keySet()) 
		{
			// Coordinates of inscribed circle, in pixel coordinates
			Box2D box = results.get(label);
			box = uncalibrate(box, calib);
			roi = createRoi(box);
			
			// draw inscribed circle
			roi.setStrokeColor(Color.BLUE);
			overlay.add(roi);
		}
		
		target.setOverlay(overlay);
	}

	/**
	 * Determines the box corresponding to the uncalibrated version of this
	 * box, assuming it was defined in calibrated coordinates.
	 * 
	 * @param box
	 *            the box in calibrated coordinates
	 * @param calib
	 *            the spatial calibration to consider
	 * @return the circle in pixel coordinates
	 */
	private final static Box2D uncalibrate(Box2D box, Calibration calib)
	{
		
		double xmin = (box.getXMin() - calib.xOrigin) / calib.pixelWidth;
		double xmax = (box.getXMax() - calib.xOrigin) / calib.pixelWidth;
		double ymin = (box.getYMin() - calib.yOrigin) / calib.pixelHeight;
		double ymax = (box.getYMax() - calib.yOrigin) / calib.pixelHeight;
		return new Box2D(xmin, xmax, ymin, ymax);
	}
	
	private final static Roi createRoi(Box2D box)
	{
		// Coordinates of box, in pixel coordinates
		double xmin = box.getXMin();
		double xmax = box.getXMax();
		double ymin = box.getYMin();
		double ymax = box.getYMax();
		
		return new Roi(xmin, ymin, xmax - xmin, ymax - ymin);
	}
}
