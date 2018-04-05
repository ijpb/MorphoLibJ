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
import java.awt.Point;
import java.util.List;
import java.util.Map;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.binary.ChamferWeights;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.GeodesicDiameter;
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
		GenericDialog gd = new GenericDialog("Geodesic Diameter");
		gd.addChoice("Label Image:", imageNames, selectedImageName);
		// Set Chessknight weights as default
		gd.addChoice("Distances", ChamferWeights.getAllLabels(), 
				ChamferWeights.CHESSKNIGHT.toString());
		gd.addCheckbox("Show Overlay Result", true);
		gd.addChoice("Image to overlay:", imageNames, selectedImageName);
		gd.addCheckbox("Export to ROI Manager", true);
		gd.showDialog();
		
		if (gd.wasCanceled())
			return;
		
		// set up current parameters
		int labelImageIndex = gd.getNextChoiceIndex();
		ImagePlus labelPlus = WindowManager.getImage(labelImageIndex+1);
		ChamferWeights weights = ChamferWeights.fromLabel(gd.getNextChoice());
        boolean overlayPaths = gd.getNextBoolean();
		int resultImageIndex = gd.getNextChoiceIndex();
		boolean createPathRois = gd.getNextBoolean();
		
		// check if image is a label image
		if (!LabelImages.isLabelImageType(labelPlus))
		{
			IJ.showMessage("Input image should be a label image");
			return;
		}
		ImageProcessor labelImage = labelPlus.getProcessor();
		
		// Compute geodesic diameters, using floating-point calculations
		long start = System.nanoTime();
		
		GeodesicDiameter algo = new GeodesicDiameter(weights);
		algo.setComputePaths(overlayPaths || createPathRois);
		DefaultAlgoListener.monitor(algo);
		
		Map<Integer, GeodesicDiameter.Result> geodDiams = algo.process(labelImage);

		long finalTime = System.nanoTime();
		
		// Final time, displayed in milli-sseconds
		float elapsedTime = (finalTime - start) / 1000000.0f;

		// display the result table
		ResultsTable table = GeodesicDiameter.asTable(geodDiams);  
		String tableName = labelPlus.getShortTitle() + "-GeodDiameters"; 
		table.show(tableName);

//		int gdIndex = table.getColumnIndex("Geod. Diam");
//		double[] geodDiamArray = table.getColumnAsDoubles(gdIndex);
		
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
    		
//    		// compute the path that is associated to each label
//    		Map<Integer, List<Point>> pathMap = null;
//    		try
//    		{
//    			pathMap = algo.longestGeodesicPaths(labelImage);
//    		}
//    		catch (Exception ex)
//    		{
//    			IJ.handleException(ex);
//    			IJ.error("Geodesic Diameter Error", 
//    					"Could not compute geodesic paths.\nTry using Borgefors weights.");
//    			return;
//    		}

    		// Check if results must be displayed on an image
    		if (overlayPaths) 
    		{
    			// New image for displaying geometric overlays
    			ImagePlus resultImage = WindowManager.getImage(resultImageIndex+1);
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
	 * Display the result of geodesic parameter extraction as overlay on a given
	 * image.
	 * 
	 * @param target
	 *            the imagePlus used to display result
	 * @param table
	 *            the ResultsTable obtained from geodesicDimaeter analysis
	 */
	public void showResultsAsOverlay(ImagePlus target, ResultsTable table) 
	{
		
		Overlay overlay = new Overlay();
		
		Roi roi;
		
		int count = table.getCounter();
		for (int i = 0; i < count; i++) {
			// Coordinates of inscribed circle
			double xi = table.getValue("xi", i);
			double yi = table.getValue("yi", i);
			double ri = table.getValue("Radius", i);
			
			// draw inscribed circle
			int width = (int) Math.round(2 * ri);
			roi = new OvalRoi((int) (xi - ri), (int) (yi - ri), width, width);
			roi.setStrokeColor(Color.BLUE);
			overlay.add(roi);
			
			// Display label
			roi = new TextRoi((int)xi, (int)yi, Integer.toString(i + 1));
			roi.setStrokeColor(Color.BLUE);
			overlay.add(roi);
			
			// Draw geodesic diameter 
			int x1 = (int) table.getValue("x1", i);
			int y1 = (int) table.getValue("y1", i);
			int x2 = (int) table.getValue("x2", i);
			int y2 = (int) table.getValue("y2", i);
			roi = new Line(x1, y1, x2, y2);
			roi.setStrokeColor(Color.RED);
			overlay.add(roi);			
		}
		
		target.setOverlay(overlay);
	}

	public void drawPaths(ImagePlus target, Map<Integer, GeodesicDiameter.Result> geodDiams)
	{
		Overlay overlay = new Overlay();
		
		for (GeodesicDiameter.Result result : geodDiams.values())
		{
			Roi roi = createPathRoi(result.path);
		    roi.setStrokeColor(Color.RED);
		    overlay.add(roi);
		}

		target.setOverlay(overlay);
	}
	
	/**
	 * Adds the specified paths to the list of ROI of the image plus.
	 * 
	 * @param target The ImagePlus that will be associated with ROIS
	 * @param pathMap the list of paths
	 */
	public void createPathRois(ImagePlus target, Map<Integer, GeodesicDiameter.Result> geodDiams)
	{
		// get instance of ROI Manager
		RoiManager manager = RoiManager.getRoiManager();
		
		// add each path to the ROI Manager
		int index = 0;
		for (GeodesicDiameter.Result result : geodDiams.values())
		{
		    manager.add(target, createPathRoi(result.path), index++);
		}
	}

	private Roi createPathRoi(List<Point> path)
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
            for (Point pos : path)
            {
                x[i] = pos.x + .5f;
                y[i] = pos.y + .5f;
                i++;
            }
            return new PolygonRoi(x, y, n, Roi.POLYLINE);
        }
        else if (path.size() == 1)
        {
            // case of single point particle
            Point p = path.get(0);
            return new PointRoi(p.x + .5, p.y + .5);
        }
        else
        {
        	throw new RuntimeException("Can not manage empty paths");
        }
	}
}
