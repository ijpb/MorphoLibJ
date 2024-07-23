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

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.binary.distmap.ChamferMask2D;
import inra.ijpb.binary.distmap.ChamferMasks2D;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.region2d.GeodesicDiameter;
import inra.ijpb.util.IJUtils;

/**
 * Plugin for computing geodesic distances of labeled particles using chamfer
 * weights.
 * @author dlegland
 *
 */
public class GeodesicDiameterPlugin implements PlugIn
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
		gd.addChoice("Label Image:", imageNames, currentImageName);
		// Set Chess Knight weights as default
		gd.addChoice("Distances", ChamferMasks2D.getAllLabels(), 
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
		
		// Create and configure the class for computing geodesic diameter
		GeodesicDiameter algo = new GeodesicDiameter(chamferMask);
		algo.setComputePaths(overlayPaths || createPathRois);
		DefaultAlgoListener.monitor(algo);
		
		// Compute geodesic diameters, using floating-point calculations
		long start = System.nanoTime();
		Map<Integer, GeodesicDiameter.Result> geodDiams = algo.analyzeRegions(labelPlus);

		// Elapsed time, displayed in milli-seconds
		long finalTime = System.nanoTime();
		float elapsedTime = (finalTime - start) / 1000000.0f;

		// display the result table
		ResultsTable table = algo.createTable(geodDiams);  
		String tableName = labelPlus.getShortTitle() + "-GeodDiameters"; 
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
    			drawPaths(resultImage, geodDiams);
    		}

    		if (createPathRois)
    		{
    			createPathRois(labelPlus, geodDiams);
    		}
		}
		
		IJUtils.showElapsedTime("Geodesic Diameter", elapsedTime, labelPlus); 
	}


	// ====================================================
	// Computing functions 
	
	/**
	 * Displays the geodesic paths onto the given image.
	 * 
	 * @param target
	 *            target image
	 * @param geodDiams
	 *            the diameters to draw
	 */
	public static final void drawPaths(ImagePlus target, Map<Integer, GeodesicDiameter.Result> geodDiams)
	{
		Overlay overlay = new Overlay();
		Calibration calib = target.getCalibration();
		
		for (GeodesicDiameter.Result result : geodDiams.values())
		{
			Roi roi = createPathRoi(result.path, calib);
		    roi.setStrokeColor(Color.RED);
		    overlay.add(roi);
		}

		target.setOverlay(overlay);
	}
	
	/**
	 * Adds the specified paths to the list of ROI of the image plus.
	 * 
	 * @param target The ImagePlus that will be associated with ROIS
	 * @param geodDiams the list of paths
	 */
	public static final void createPathRois(ImagePlus target, Map<Integer, GeodesicDiameter.Result> geodDiams)
	{
		// get instance of ROI Manager
		RoiManager manager = RoiManager.getRoiManager();
		Calibration calib = target.getCalibration();
		
		// add each path to the ROI Manager
		int index = 0;
		for (GeodesicDiameter.Result result : geodDiams.values())
		{
		    manager.add(target, createPathRoi(result.path, calib), index++);
		}
	}

	private static final Roi createPathRoi(List<Point2D> path, Calibration calib)
	{
		if (path == null)
    	{
    		return null;
    	}
    	
        if (path.size() > 1)
        {
            // Polyline path
            int n = path.size();
            float[] x = new float[n];
            float[] y = new float[n];
            int i = 0;
            for (Point2D pos : path)
            {
            	pos = calibToPixel(pos, calib);
                x[i] = (float) (pos.getX() + 0.5f);
                y[i] = (float) (pos.getY() + 0.5f);
                i++;
            }
            return new PolygonRoi(x, y, n, Roi.POLYLINE);
        }
        else if (path.size() == 1)
        {
            // case of single point particle
            Point2D pos = calibToPixel(path.get(0), calib);
            return new PointRoi(pos.getX() + 0.5, pos.getY() + 0.5);
        }
        else
        {
        	throw new RuntimeException("Can not manage empty paths");
        }
	}
	
	private static final Point2D calibToPixel(Point2D point, Calibration calib)
	{
		double x = (point.getX() - calib.xOrigin) / calib.pixelWidth;
		double y = (point.getY() - calib.yOrigin) / calib.pixelHeight;
		return new Point2D.Double(x, y);
	}
}
