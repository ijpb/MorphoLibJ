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
import java.awt.geom.Point2D;
import java.util.Map;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import inra.ijpb.geometry.Circle2D;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.GeometricMeasures2D;
import inra.ijpb.measure.region2d.LargestInscribedCircle;

public class MaxInscribedCirclePlugin implements PlugIn 
{
    // ====================================================
    // Global Constants

    // ====================================================
    // Class variables
    
    // ====================================================
    // Calling functions 
    
	@Override
	public void run(String arg0) 
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
		GenericDialog gd = new GenericDialog("Max. Inscribed Circle");
		gd.addChoice("Label Image:", imageNames, selectedImageName);
//		// Set Chessknight weights as default
//		gd.addChoice("Distances", ChamferWeights.getAllLabels(), 
//				ChamferWeights.CHESSKNIGHT.toString());
		gd.addCheckbox("Show Overlay Result", true);
		gd.addChoice("Image to overlay:", imageNames, selectedImageName);
		gd.showDialog();
		
		if (gd.wasCanceled())
			return;
		
		// set up current parameters
		int labelImageIndex = gd.getNextChoiceIndex();
		ImagePlus labelImage = WindowManager.getImage(labelImageIndex+1);
//		ChamferWeights weights = ChamferWeights.fromLabel(gd.getNextChoice());
		boolean showOverlay = gd.getNextBoolean();
		int resultImageIndex = gd.getNextChoiceIndex();
		
		// check if image is a label image
		if (!LabelImages.isLabelImageType(labelImage))
		{
            IJ.showMessage("Input image should be a label image");
            return;
        }
        
		// Execute the plugin
		Map<Integer, Circle2D> results = LargestInscribedCircle.compute(labelImage);
       
        // Display plugin result as table
		ResultsTable table = LargestInscribedCircle.asTable(results);
		String tableName = labelImage.getShortTitle() + "-MaxInscribedCircle"; 
		table.show(tableName);
		
		// Check if results must be displayed on an image
		if (showOverlay)
		{
			// find image for displaying geometric overlays
			ImagePlus resultImage = WindowManager.getImage(resultImageIndex + 1);
			showResultsAsOverlay(results, resultImage);
		}
	}
	   
    /**
	 * Main body of the plugin.
	 * 
	 * @param imagePlus
	 *            the image to process
	 * @param weights
	 *            the set of weights for propagating distances
	 * @return an array of objects with results
	 * @deprecated replaced by process method
	 */
	@Deprecated
    public Object[] exec(ImagePlus imagePlus, short[] weights) 
    {
        // Check validity of parameters
        if (imagePlus==null) 
            return null;
        ResultsTable results = LargestInscribedCircle.asTable(LargestInscribedCircle.compute(imagePlus));
		return new Object[]{"Morphometry", results};
    }
    
	/**
	 * Main body of the plugin.
	 * 
	 * @deprecated use LargestInscribedCircle class instead 
	 * 
	 * @param imagePlus
	 *            the image to process
	 * @param weights
	 *            the set of weights for propagating distances
	 * @return an array of objects with results
	 */
	@Deprecated
    public ResultsTable process(ImagePlus imagePlus, short[] weights) 
    {
        // Check validity of parameters
        if (imagePlus==null) 
            return null;

        ImageProcessor image = imagePlus.getProcessor();
        
        // Extract spatial calibration
        double[] resol = getPixelSize(imagePlus);

        ResultsTable results = GeometricMeasures2D.maximumInscribedCircle(image, 
        		resol);
        
		// return the created array
		return results;
    }
    
    private static final double[] getPixelSize(ImagePlus imagePlus)
    {
    	// Extract spatial calibration
        Calibration cal = imagePlus.getCalibration();
        double[] resol = new double[]{1, 1};
        if (cal.scaled()) 
        {
        	resol[0] = cal.pixelWidth;
        	resol[1] = cal.pixelHeight;
        }
        return resol;
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
	private void showResultsAsOverlay(Map<Integer, Circle2D> results, ImagePlus target)	
	{
		// get spatial calibration of target image
		Calibration calib = target.getCalibration();
		
		// create overlay
		Overlay overlay = new Overlay();
		Roi roi;
		
		// add each circle to the overlay
		for (int label : results.keySet()) 
		{
			// Coordinates of inscribed circle, in pixel coordinates
			Circle2D circle = uncalibrate(results.get(label), calib);
			Point2D center = circle.getCenter();
			double xi = center.getX();
			double yi = center.getY();
			double ri = circle.getRadius();
			
			// draw inscribed circle
			int width = (int) Math.round(2 * ri);
			roi = new OvalRoi((int) (xi - ri), (int) (yi - ri), width, width);
			roi.setStrokeColor(Color.BLUE);
			overlay.add(roi);
			
			// Display label
			roi = new TextRoi((int) xi, (int) yi, Integer.toString(label));
			roi.setStrokeColor(Color.BLUE);
			overlay.add(roi);
		}
		
		target.setOverlay(overlay);
	}

	/**
	 * Determines the circle corresponding to the uncalibrated version of this
	 * circle, assuming it was defined in calibrated coordinates.
	 * 
	 * @param circle
	 *            the circle in calibrated coordinates
	 * @param calib
	 *            the spatial calibration to consider
	 * @return the circle in pixel coordinates
	 */
	private final static Circle2D uncalibrate(Circle2D circle, Calibration calib)
	{
		Point2D center = circle.getCenter();
		double xc = (center.getX() - calib.xOrigin) / calib.pixelWidth;
		double yc = (center.getY() - calib.yOrigin) / calib.pixelHeight;
		double radius = circle.getRadius() / calib.pixelWidth;
		return new Circle2D(new Point2D.Double(xc, yc), radius);
	}
	

}
