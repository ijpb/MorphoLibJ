package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.binary.geodesic.GeodesicDiameterFloat;
import inra.ijpb.binary.geodesic.GeodesicDiameterShort;
import inra.ijpb.binary.ChamferWeights;
import inra.ijpb.label.LabelImages;

import java.awt.Color;
import java.awt.Point;
import java.util.List;
import java.util.Map;

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
		ResultsTable table = process(labelImage, weights.getFloatWeights());
		long finalTime = System.nanoTime();
		
		// Final time, displayed in milli-sseconds
		float elapsedTime = (finalTime - start) / 1000000.0f;

		// display the result table
		String tableName = labelPlus.getShortTitle() + "-GeodDiameters"; 
		table.show(tableName);

		int gdIndex = table.getColumnIndex("Geod. Diam");
		double[] geodDiamArray = table.getColumnAsDoubles(gdIndex);
		
		boolean validPaths = true;
		for (double geodDiam : geodDiamArray)
		{
			if (Float.isInfinite((float) geodDiam))
			{
				IJ.showMessage("Geodesic Diameter Warning", "Some geodesic diameters are infinite,\n"
						+ "meaning that some particles are not connected.\n" + "Maybe labeling was not performed?");
				validPaths = false;
				break;
			}
		}
		
		if (validPaths)
		{
			// compute the path that is associated to each label
			Map<Integer, List<Point>> pathMap = computePaths(labelImage, weights.getFloatWeights());
			
			// Check if results must be displayed on an image
			if (overlayPaths) 
			{
				// New image for displaying geometric overlays
				ImagePlus resultImage = WindowManager.getImage(resultImageIndex+1);
				drawPaths(resultImage, pathMap);
			}
			
			if (createPathRois)
			{
				createPathRois(labelPlus, pathMap);
			}
		}

//		// Check if results must be displayed on an image
//		if (gd.getNextBoolean() && validPaths) 
//		{
//			// New image for displaying geometric overlays
//			ImagePlus resultImage = WindowManager.getImage(resultImageIndex+1);
//			
//			Map<Integer, List<Point>> pathMap = computePaths(labelImage, weights.getFloatWeights());
//			drawPaths(resultImage, pathMap);
//		}
			
		IJ.showStatus(String.format("Elapsed time: %8.2f ms", elapsedTime));	
	}

	
	/**
	 * Computes the geodesic diameter of each particle using floating point weights.
	 * 
	 * @param labels
	 *            the label image representing the particles
	 * @param newName
	 *            the name of the image to be created
	 * @param weights
	 *            the array of weights for the orthogonal, diagonal directions
	 * @return an array of objects
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
		
		ResultsTable table = process(labels.getProcessor(),
				weights);

		// create string for indexing results
		String tableName = labels.getShortTitle() + "-Geodesics"; 
		
		// show result
		table.show(tableName);
				
		// return the created array
		return new Object[]{tableName, table};
	}
	
	/**
	 * Computes the geodesic diameter of each particle using integer weights.
	 * 
	 * @param labels
	 *            the label image representing the particles
	 * @param newName
	 *            the name of the image to be created
	 * @param weights
	 *            the array of weights for the orthogonal, diagonal directions
	 * @return an array of objects
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
		
		ResultsTable table = process(labels.getProcessor(), weights);
		
		// create string for indexing results
		String tableName = labels.getShortTitle() + "-Geodesics"; 
		
		// show result
		table.show(tableName);
		
		// return the created array
		return new Object[]{"Geodesic Lengths", table};
	}
	
	
	// ====================================================
	// Computing functions 
	
	/**
	 * Compute the table of geodesic parameters, when the weights are given as
	 * floating point values.
	 * 
	 * @param labels
	 *            the label image of the particles
	 * @param weights
	 *            the weights to use
	 * @return a new ResultsTable object containing the geodesic diameter of
	 *         each label
	 */
	public ResultsTable process(ImageProcessor labels, float[] weights)
	{
		GeodesicDiameterFloat algo = new GeodesicDiameterFloat(weights);
		DefaultAlgoListener.monitor(algo);
		ResultsTable table = algo.analyzeImage(labels);
		return table;
	}

	/**
	 * Compute the table of geodesic parameters, when the weights are given as
	 * integer values.
	 * 
	 * @param labels
	 *            the label image of the particles
	 * @param weights
	 *            the weights to use
	 * @return a new ResultsTable object containing the geodesic diameter of
	 *         each label
	 */
	public ResultsTable process(ImageProcessor labels, short[] weights)
	{
		GeodesicDiameterShort algo = new GeodesicDiameterShort(weights);
		DefaultAlgoListener.monitor(algo);
		ResultsTable table = algo.analyzeImage(labels);
		return table;
	}

//	private Map<Integer, List<Point>> computePaths(ImageProcessor labels, short[] weights)
//	{
//		GeodesicDiameterShort algo = new GeodesicDiameterShort(weights);
//		DefaultAlgoListener.monitor(algo);
//		return algo.longestGeodesicPaths(labels);
//	}

	private Map<Integer, List<Point>> computePaths(ImageProcessor labels, float[] weights)
	{
		GeodesicDiameterFloat algo = new GeodesicDiameterFloat(weights);
		DefaultAlgoListener.monitor(algo);
		return algo.longestGeodesicPaths(labels);
	}

	// ====================================================
	// Computing functions 
	
	/**
	 * Compute geodesic length of particles, using imageProcessor as input and
	 * weights as array of float
	 * 
	 * @param labels
	 *            the label image of the particles
	 * @param weights
	 *            the weights to use
	 * @return an array of objects (image name, ImagePlus object)
	 * @deprecated use process method instead
	 */
	@Deprecated
	public Object[] exec(ImageProcessor labels, float[] weights) 
	{
		ResultsTable table = process(labels, weights);
		
		// show result
		table.show("Geodesic Diameters");
		
		// return the created array
		return new Object[]{"Geodesic Diameters", table};
	}


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

	public void drawPaths(ImagePlus target, Map<Integer, List<Point>> pathMap)
	{
		Overlay overlay = new Overlay();
		Roi roi;
		
		for (List<Point> path : pathMap.values())
		{
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
			roi = new PolygonRoi(x, y, n, Roi.POLYLINE);
			
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
	public void createPathRois(ImagePlus target, Map<Integer, List<Point>> pathMap)
	{
		// get instance of ROI MAnager
		RoiManager manager = RoiManager.getRoiManager();
		
		int index = 0;
		for (List<Point> path : pathMap.values())
		{
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
			Roi roi = new PolygonRoi(x, y, n, Roi.POLYLINE);
		
			manager.add(target, roi, index++);
		}
	}

	private static String createResultImageName(ImagePlus baseImage) 
	{
		return baseImage.getShortTitle() + "-diam";
	}
}
