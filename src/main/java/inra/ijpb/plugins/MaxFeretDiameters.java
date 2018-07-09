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
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.FeretDiameters;
import inra.ijpb.measure.FeretDiameters.PointPair;

/**
 * Computes Maximum Feret Diameters of a binary or label image.
 * 
 * Mainly used for debugging puproses
 *  
 * @author dlegland
 *
 */
public class MaxFeretDiameters implements PlugIn
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
		// Open a dialog to choose:
		// - a label image
		// - image to display results
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
		GenericDialog gd = new GenericDialog("Max. Feret Diameter");
		gd.addChoice("Label Image:", imageNames, selectedImageName);
		// Set Chessknight weights as default
		gd.addCheckbox("Show Overlay Result", true);
		gd.addChoice("Image to overlay:", imageNames, selectedImageName);
//		gd.addCheckbox("Export to ROI Manager", true);
		gd.showDialog();
		
		if (gd.wasCanceled())
			return;
		
		// set up current parameters
		int labelImageIndex = gd.getNextChoiceIndex();
		ImagePlus labelPlus = WindowManager.getImage(labelImageIndex+1);
        boolean overlay = gd.getNextBoolean();
		int resultImageIndex = gd.getNextChoiceIndex();
//		boolean createPathRois = gd.getNextBoolean();
		
		// check if image is a label image
		if (!LabelImages.isLabelImageType(labelPlus))
		{
			IJ.showMessage("Input image should be a label image");
			return;
		}
		
		// Extract spatial calibration
		Calibration cal = labelPlus.getCalibration();
		double[] resol = new double[]{1, 1};
		if (cal.scaled())
		{
			resol[0] = cal.pixelWidth;
			resol[1] = cal.pixelHeight;
			if (resol[0] != resol[1])
			{
				IJ.error("Calibration Error", "Requires Images to have same calibration for X and Y axes");
				return;
			}
		}

		// Compute max Feret diameters
		ImageProcessor labelImage = labelPlus.getProcessor();
		Map<Integer, PointPair> maxDiamsMap = FeretDiameters.maxFeretDiameters(labelImage);
		
		// Display the result Table
        ResultsTable results = FeretDiameters.maxFeretDiametersTable(maxDiamsMap);

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
    
	public void drawDiameters(ImagePlus target, Map<Integer, PointPair> geodDiams)
	{
		Overlay overlay = new Overlay();
		Calibration calib = target.getCalibration();
		
		for (PointPair result : geodDiams.values())
		{
			Roi roi = createDiametersRoi(result, calib);
		    roi.setStrokeColor(Color.RED);
		    overlay.add(roi);
		}

		target.setOverlay(overlay);
	}

	private Roi createDiametersRoi(PointPair pointPair, Calibration calib)
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
	
//    /**
//	 * Main body of the plugin.
//	 * 
//	 * @param imagePlus
//	 *            the image to analyze
//	 * @return the instance of ResultsTable containing ellipse parameters for
//	 *         each label
//	 */
//    public ResultsTable process(ImagePlus imagePlus) {
//        // Check validity of parameters
//        if (imagePlus==null) 
//            return null;
//
//        if (debug) {
//        	System.out.println("Compute Feret Diameters on image '" 
//        			+ imagePlus.getTitle());
//        }
//        
//        // Extract spatial calibration
//        Calibration cal = imagePlus.getCalibration();
//        double[] resol = new double[]{1, 1};
//        if (cal.scaled())
//        {
//        	resol[0] = cal.pixelWidth;
//        	resol[1] = cal.pixelHeight;
//        	if (resol[0] != resol[1])
//        	{
//        		IJ.error("Calibration Error", "Requires Images to have same calibration for X and Y axes");
//        	}
//        }
//
//        ImageProcessor image = imagePlus.getProcessor();
//        Map<Integer, PointPair> maxDiamsMap = FeretDiameters.maxFeretDiameters(image);
//        
//        // Create data table
//		ResultsTable table = new ResultsTable();
//
//		// compute ellipse parameters for each region
//		for (int label : maxDiamsMap.keySet()) 
//		{
//			table.incrementCounter();
//			table.addLabel(Integer.toString(label));
//			
//			// add coordinates of origin pixel (IJ coordinate system)
//			PointPair maxDiam = maxDiamsMap.get(label);
//			table.addValue("Diameter", maxDiam.diameter());
//			table.addValue("Orientation", Math.toDegrees(maxDiam.angle()));
//			table.addValue("p1.x", maxDiam.p1.getX());
//			table.addValue("p1.y", maxDiam.p1.getY());
//			table.addValue("p2.x", maxDiam.p2.getX());
//			table.addValue("p2.y", maxDiam.p2.getY());
//		}
//		
//		// return the created array
//		return table;
//    }
}
