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
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import inra.ijpb.geometry.Ellipse;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.region2d.EquivalentEllipse;
import inra.ijpb.util.IJUtils;

/**
 * Computes equivalent ellipse of each region within a label image. The
 * equivalent ellipse of a region is computed such that is has same second order
 * moments as the region.
 * 
 * @see EquivalentEllipsoidPlugin
 * 
 * @author dlegland
 *
 */
public class EquivalentEllipsePlugin implements PlugIn
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
        // create the list of image names
        String[] imageNames = IJUtils.getOpenImageNames();
        if (imageNames.length == 0)
        {
            IJ.error("No image", "Need at least one image to work");
            return;
        }

		
		// name of current image
		String currentImageName = IJ.getImage().getTitle();

		// create the dialog
		GenericDialog gd = new GenericDialog("Equivalent Ellipse");
		gd.addChoice("Label Image:", imageNames, currentImageName);
		gd.addCheckbox("Overlay Ellipse", true);
		gd.addCheckbox("Overlay Axes", true);
		gd.addChoice("Image to overlay:", imageNames, currentImageName);
		gd.showDialog();
		
		if (gd.wasCanceled())
			return;
		
		// set up current parameters
		int labelImageIndex = gd.getNextChoiceIndex();
		ImagePlus labelImage = WindowManager.getImage(labelImageIndex + 1);
		boolean showEllipse = gd.getNextBoolean();
		boolean showAxes = gd.getNextBoolean();
		int resultImageIndex = gd.getNextChoiceIndex();
		
		// check if image is a label image
		if (!LabelImages.isLabelImageType(labelImage))
		{
            IJ.showMessage("Input image should be a label image");
            return;
        }

        // Execute the plugin
		EquivalentEllipse op = new EquivalentEllipse();
		Map<Integer, Ellipse> ellipses = op.analyzeRegions(labelImage);
        ResultsTable results = op.createTable(ellipses);
        
		// show result
    	String tableName = labelImage.getShortTitle() + "-Ellipses"; 
    	results.show(tableName);
    	
		// Check if results must be displayed on an image
		if (showEllipse || showAxes)
		{
			// find image for displaying geometric overlays
			ImagePlus resultImage = WindowManager.getImage(resultImageIndex + 1);
			showResultsAsOverlay(ellipses, resultImage, showEllipse, showAxes);
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
	private void showResultsAsOverlay(Map<Integer, Ellipse> results, ImagePlus target, boolean showEllipse, boolean showAxes)	
	{
		// get spatial calibration of target image
		Calibration calib = target.getCalibration();
		
		// create overlay
		Overlay overlay = new Overlay();
		
		// add each ellipse to the overlay
		for (int label : results.keySet()) 
		{
			// Coordinates of inscribed circle, in pixel coordinates
			Ellipse ellipse = results.get(label);
			ellipse = uncalibrate(ellipse, calib);

			// roi corresponding to ellipse
			if (showEllipse)
			{
				addRoiToOverlay(overlay, createRoi(ellipse), Color.BLUE);
			}
			
			// the two roi corresponding to major axes
			if (showAxes)
			{
				addRoiToOverlay(overlay, createMajorAxisRoi(ellipse), Color.BLUE);
				addRoiToOverlay(overlay, createMinorAxisRoi(ellipse), Color.BLUE);
			}
		}
		
		target.setOverlay(overlay);
	}

	private static final void addRoiToOverlay(Overlay overlay, Roi roi, Color color)
	{
		roi.setStrokeColor(color);
		overlay.add(roi);
	}
	
	/**
	 * Determines the ellipse corresponding to the uncalibrated version of this
	 * ellipse, assuming it was defined in calibrated coordinates.
	 * 
	 * @param ellipse
	 *            the ellipse in calibrated coordinates
	 * @param calib
	 *            the spatial calibration to consider
	 * @return the circle in pixel coordinates
	 */
	private final static Ellipse uncalibrate(Ellipse ellipse, Calibration calib)
	{
		Point2D center = ellipse.center();
		double xc = (center.getX() - calib.xOrigin) / calib.pixelWidth;
		double yc = (center.getY() - calib.yOrigin) / calib.pixelHeight;
		double radius1 = ellipse.radius1() / calib.pixelWidth;
		double radius2 = ellipse.radius2() / calib.pixelWidth;
		return new Ellipse(xc, yc, radius1, radius2, ellipse.orientation());
	}
	
	private final static Roi createRoi(Ellipse ellipse)
	{
		// Coordinates of ellipse, in pixel coordinates
		Point2D center = ellipse.center();
		double xc = center.getX();
		double yc = center.getY();
		
		double r1 = ellipse.radius1();
		double r2 = ellipse.radius2();
		double theta = Math.toRadians(ellipse.orientation());
		
		double cot = Math.cos(theta);
		double sit = Math.sin(theta);
		
		int nVertices = 100;
		float[] xv = new float[nVertices];
		float[] yv = new float[nVertices];
		for (int i = 0; i < nVertices; i++)
		{
			double t = i * Math.PI * 2.0 / nVertices;
			double x = Math.cos(t) * r1;
			double y = Math.sin(t) * r2;
			
			xv[i] = (float) (x * cot - y * sit + xc);
			yv[i] = (float) (x * sit + y * cot + yc);
		}
		
		return new PolygonRoi(xv, yv, nVertices, Roi.POLYGON);
	}
	
	private final static Roi createMajorAxisRoi(Ellipse ellipse)
	{
		// Coordinates of ellipse, in pixel coordinates
		Point2D center = ellipse.center();
		double xc = center.getX();
		double yc = center.getY();
		
		double r1 = ellipse.radius1();
		double theta = Math.toRadians(ellipse.orientation());
		
		double cot = Math.cos(theta);
		double sit = Math.sin(theta);
		
		double x1 = xc + r1 * cot;
		double y1 = yc + r1 * sit;
		double x2 = xc - r1 * cot;
		double y2 = yc - r1 * sit;
		return new Line(x1, y1, x2, y2);
	}

	private final static Roi createMinorAxisRoi(Ellipse ellipse)
	{
		// Coordinates of ellipse, in pixel coordinates
		Point2D center = ellipse.center();
		double xc = center.getX();
		double yc = center.getY();
		
		double r2 = ellipse.radius2();
		double theta = Math.toRadians(ellipse.orientation() + 90);
		
		double cot = Math.cos(theta);
		double sit = Math.sin(theta);
		
		double x1 = xc + r2 * cot;
		double y1 = yc + r2 * sit;
		double x2 = xc - r2 * cot;
		double y2 = yc - r2 * sit;
		return new Line(x1, y1, x2, y2);
	}

}
