package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import inra.ijpb.binary.geodesic.GeodesicDiameterFloat;
import inra.ijpb.binary.geodesic.GeodesicDiameterShort;
import inra.ijpb.binary.ChamferWeights;

import java.awt.Color;

/**
 * 
 */

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
		GenericDialog gd = new GenericDialog("Geodesic Lengths");
		gd.addChoice("Label Image:", imageNames, selectedImageName);
		// Set Chessknight weights as default
		gd.addChoice("Distances", ChamferWeights.getAllLabels(), 
				ChamferWeights.CHESSKNIGHT.toString());
		gd.addCheckbox("Show Overlay Result", true);
		gd.addChoice("Image to overlay:", imageNames, selectedImageName);
		gd.showDialog();
		
		if (gd.wasCanceled())
			return;
		
		// set up current parameters
		int labelImageIndex = gd.getNextChoiceIndex();
		ImagePlus labelImage = WindowManager.getImage(labelImageIndex+1);
		ChamferWeights weights = ChamferWeights.fromLabel(gd.getNextChoice());
		// check image types
		if (labelImage.getType() != ImagePlus.GRAY8
				&& labelImage.getType() != ImagePlus.GRAY16)
		{
			IJ.showMessage("Input image should be a label image");
			return;
		}
		
		// Execute the plugin
		String newName = createResultImageName(labelImage);

		Object[] results;
		boolean useIntegers = false;
		if (useIntegers) 
		{
			results = exec(labelImage, newName, weights.getShortWeights());
		} 
		else 
		{
			results = exec(labelImage, newName, weights.getFloatWeights());
		}

		// Check if results must be displayed on an image
		if (gd.getNextBoolean()) 
		{
			// Extract result table
			ResultsTable table = (ResultsTable) results[1];
			
			// New image for displaying geometric overlays
			int resultImageIndex = gd.getNextChoiceIndex();
			ImagePlus resultImage = WindowManager.getImage(resultImageIndex+1);
			
			showResultsAsOverlay(resultImage, table);
		}
	}

	
	/**
	 * Compute geodesic length of particles using floating point weights. 
	 */
	public Object[] exec(ImagePlus labels, String newName, float[] weights) 
	{
		// Check validity of parameters
		if (labels==null) 
			return null;
		if (newName==null) 
			newName = createResultImageName(labels);
		if (weights==null) 
			return null;
		
		ResultsTable table = computeGeodesicLengthTable(labels.getProcessor(),
				weights);

		// create string for indexing results
		String tableName = labels.getShortTitle() + "-Geodesics"; 
		
		// show result
		table.show(tableName);
				
		// return the created array
		return new Object[]{tableName, table};
	}
	
	/**
	 * Compute geodesic length of particles using integer weights.
	 */
	public Object[] exec(ImagePlus labels, String newName, short[] weights) 
	{
		// Check validity of parameters
		if (labels==null) 
			return null;
		if (newName==null) 
			newName = createResultImageName(labels);
		if (weights==null) 
			return null;
		
		ResultsTable table = computeGeodesicLengthTable(labels.getProcessor(),
				weights);
		
		// create string for indexing results
		String tableName = labels.getShortTitle() + "-Geodesics"; 
		
		// show result
		table.show(tableName);
		
		// return the created array
		return new Object[]{"Geodesic Lengths", table};
	}
	
	/**
	 * Display the result of geodesic parameter extraction as overlay on a 
	 * given image.
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
	
	
	// ====================================================
	// Computing functions 
	
	/**
	 * Compute geodesic length of particles, using imageProcessor as input and
	 * weights as array of float
	 */
	public Object[] exec(ImageProcessor labels, float[] weights) 
	{
		ResultsTable table = computeGeodesicLengthTable(labels, weights);
		
		// show result
		table.show("Geodesic Diameters");
		
		// return the created array
		return new Object[]{"Geodesic Diameters", table};
	}
	
	/**
	 * Compute the table of geodesic parameters, when the weights are given as
	 * floating point values.
	 */
	private ResultsTable computeGeodesicLengthTable(ImageProcessor labels, 
			float[] weights)
	{
		GeodesicDiameterFloat algo = new GeodesicDiameterFloat(weights);
		ResultsTable table = algo.analyzeImage(labels);
		return table;
	}

	/**
	 * Compute the table of geodesic parameters, when the weights are given as
	 * integer values.
	 */
	private ResultsTable computeGeodesicLengthTable(ImageProcessor labels, 
			short[] weights)
	{
		GeodesicDiameterShort algo = 
			new GeodesicDiameterShort(weights);
		ResultsTable table = algo.analyzeImage(labels);
		return table;
	}

	private static String createResultImageName(ImagePlus baseImage) 
	{
		return baseImage.getShortTitle() + "-diam";
	}
}
