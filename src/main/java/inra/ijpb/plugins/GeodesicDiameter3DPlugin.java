package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.binary.ChamferWeights3D;
import inra.ijpb.binary.geodesic.GeodesicDiameter3DFloat;
import inra.ijpb.label.LabelImages;

/**
 * 
 */

/**
 * Plugin for computing geodesic distances of 3D particles from label images
 * using chamfer weights.
 * 
 * @author dlegland
 *
 */
public class GeodesicDiameter3DPlugin implements PlugIn
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
		gd.addChoice("Label Image (3D):", imageNames, selectedImageName);
		// Set Chessknight weights as default
		gd.addChoice("Distances", ChamferWeights3D.getAllLabels(), 
				ChamferWeights3D.WEIGHTS_3_4_5_7.toString());
//		gd.addCheckbox("Show Overlay Result", true);
//		gd.addChoice("Image to overlay:", imageNames, selectedImageName);
//		gd.addCheckbox("Export to ROI Manager", true);
		gd.showDialog();
		
		if (gd.wasCanceled())
			return;
		
		// set up current parameters
		int labelImageIndex = gd.getNextChoiceIndex();
		ImagePlus labelPlus = WindowManager.getImage(labelImageIndex+1);
		ChamferWeights3D weights = ChamferWeights3D.fromLabel(gd.getNextChoice());
//		boolean overlayPaths = gd.getNextBoolean();
//		int resultImageIndex = gd.getNextChoiceIndex();
//		boolean createPathRois = gd.getNextBoolean();
		
		// check if image is a label image
		if (!LabelImages.isLabelImageType(labelPlus))
		{
			IJ.showMessage("Input image should be a label image");
			return;
		}
		
		// extract label ImageProcessor
		ImageStack labelImage = labelPlus.getStack();
		
		// Compute geodesic diameters, using floating-point calculations
		long start = System.nanoTime();
		ResultsTable table = process(labelImage, weights.getFloatWeights());
		long finalTime = System.nanoTime();
		
		// Final time, displayed in milli-sseconds
		float elapsedTime = (finalTime - start) / 1000000.0f;

		// display the result table
		String tableName = labelPlus.getShortTitle() + "-GeodDiameters"; 
		table.show(tableName);

		IJ.showStatus(String.format("Elapsed time: %8.2f ms", elapsedTime));	

		// extract column corresponding to geodesic diameter
		int gdIndex = table.getColumnIndex("Geod. Diam.");
		double[] geodDiamArray = table.getColumnAsDoubles(gdIndex);
		
		// Check validity of resulting geodesic diameters
		for (double geodDiam : geodDiamArray)
		{
			if (Float.isInfinite((float) geodDiam))
			{
				IJ.showMessage("Geodesic Diameter Warning", "Some geodesic diameters are infinite,\n"
						+ "meaning that some particles are not connected.\n" + "Maybe labeling was not performed?");
				break;
			}
		}
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
	public ResultsTable process(ImageStack labels, float[] weights)
	{
		GeodesicDiameter3DFloat algo = new GeodesicDiameter3DFloat(weights);
		DefaultAlgoListener.monitor(algo);
		ResultsTable table = algo.process(labels);
		return table;
	}
}
