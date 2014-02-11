/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import inra.ijpb.binary.ConnectedComponents;

/**
 * Computes label image of connected components in a binary planar image or 3D
 * stack. The dialog provides an option to choose data type of output image.
 * 
 * @author David Legland
 * 
 */
public class LabelingPlugin implements PlugIn {

	private final static String[] conn2DLabels = {"4", "8"};
	private final static int[] conn2DValues = {4, 8};
	private final static String[] conn3DLabels = {"6", "26"};
	private final static int[] conn3DValues = {6, 26};
	
	private final static String[] resultBitDepthLabels = {"8 bits", "16 bits", "float"};
	private final static int[] resultBitDepthList = {8, 16, 32}; 

	@Override
	public void run(String arg) {
		ImagePlus imagePlus = IJ.getImage();
		
		GenericDialog gd = new GenericDialog("Labeling");
		int nSlices = imagePlus.getStackSize();
		String[] connLabels = nSlices == 1 ? conn2DLabels : conn3DLabels;
		gd.addChoice("Connectivity", connLabels, connLabels[0]);
		gd.addChoice("Type of result", resultBitDepthLabels, resultBitDepthLabels[1]);
		gd.showDialog();
		
		if (gd.wasCanceled()) 
			return;

		int connIndex = gd.getNextChoiceIndex();
		int bitDepth = resultBitDepthList[gd.getNextChoiceIndex()];

		ImagePlus resultPlus;
		if (nSlices == 1) {
			// Process planar image
			int conn = conn2DValues[connIndex];
			ImageProcessor image = imagePlus.getProcessor();
			ImageProcessor labelImage = ConnectedComponents.computeLabels(image, conn, bitDepth);
			resultPlus = new ImagePlus("labels", labelImage);
			resultPlus.show();
			
		} else {
			// Process 3D image stack
			int conn = conn3DValues[connIndex];
			resultPlus = ConnectedComponents.computeLabels(imagePlus, conn, bitDepth);
			resultPlus.show();
			resultPlus.setZ(imagePlus.getZ());
			resultPlus.setSlice(imagePlus.getSlice());
		}
	}

}
