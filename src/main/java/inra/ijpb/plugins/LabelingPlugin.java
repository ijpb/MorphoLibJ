/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import inra.ijpb.binary.BinaryImages;

/**
 * Computes label image of connected components in a binary planar image or 3D
 * stack. The dialog provides an option to choose data type of output image.
 * 
 * @author David Legland
 * 
 */
public class LabelingPlugin implements PlugIn 
{
	// Widget strings and corresponding values of connectivity option
	private final static String[] conn2DLabels = {"4", "8"};
	private final static int[] conn2DValues = {4, 8};
	private final static String[] conn3DLabels = {"6", "26"};
	private final static int[] conn3DValues = {6, 26};
	
	// Widget labels and corresponding values of output type option
	private final static String[] resultBitDepthLabels = {"8 bits", "16 bits", "float"};
	private final static int[] resultBitDepthList = {8, 16, 32}; 

	@Override
	public void run(String arg)
	{
		ImagePlus imagePlus = IJ.getImage();
		
		boolean isPlanar = imagePlus.getStackSize() == 1;
		
		// Display dialog options
		GenericDialog gd = new GenericDialog("Labeling");
		String[] connLabels = isPlanar ? conn2DLabels : conn3DLabels;
		gd.addChoice("Connectivity", connLabels, connLabels[0]);
		gd.addChoice("Type of result", resultBitDepthLabels, resultBitDepthLabels[1]);
		
		// wait for user answer
		gd.showDialog();
		if (gd.wasCanceled()) 
			return;

		// parses dialog options
		int connIndex = gd.getNextChoiceIndex();
		int bitDepth = resultBitDepthList[gd.getNextChoiceIndex()];
		int conn = isPlanar ? conn2DValues[connIndex] : conn3DValues[connIndex];

		// Compute components labeling
		ImagePlus resultPlus;
		try
		{ 	
			resultPlus = BinaryImages.componentsLabeling(imagePlus, conn, bitDepth);
		} 
		catch(RuntimeException ex)
		{
			IJ.error("Components Labeling Error", ex.getMessage() + "\nTry with larger data type (short or float)");
			return;
		}
		
		// udpate meta information of result image
		String newName = imagePlus.getShortTitle() + "-lbl";
		resultPlus.setTitle(newName);
		resultPlus.copyScale(imagePlus);
		
		// Display with same settings as original image
		resultPlus.show();
		
		// For 2D images, select the same visible slice as original image
		if (!isPlanar)
		{
			resultPlus.setZ(imagePlus.getZ());
			resultPlus.setSlice(imagePlus.getSlice());
		}
	}

}
