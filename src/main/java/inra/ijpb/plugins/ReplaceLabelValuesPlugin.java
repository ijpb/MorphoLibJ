/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import inra.ijpb.label.LabelImages;

/**
 * Replaces the value of selected label(s) by a given value, 
 * for 2D/3D images, for gray8, gray16 and float images. 
 * Can specify multiple labels.
 * 
 * @author David Legland
 *
 */
public class ReplaceLabelValuesPlugin implements PlugIn 
{

	@Override
	public void run(String arg0) 
	{
		ImagePlus imagePlus = IJ.getImage();
		
		GenericDialog gd = new GenericDialog("Remove Labels");
		gd.addStringField("Label(s)", "1", 12);
		gd.addMessage("Separate label values by \",\"");
		gd.addNumericField("Final Value", 0, 0);
		gd.addMessage("Replacing by value 0\n will remove labels");
		gd.showDialog();
		
		if (gd.wasCanceled())
			return;
		
		String labelListString = gd.getNextString();
		double finalValue = gd.getNextNumber();

		float[] labelArray = parseLabels(labelListString);
		
		// replace values in original image
		LabelImages.replaceLabels(imagePlus, labelArray, (float) finalValue);
		imagePlus.updateAndDraw();
	}
	
	   private static final float[] parseLabels(String string) 
	    {
	    	String[] tokens = string.split("[, ]+");
	    	int n = tokens.length;
	    	
	    	float[] labels = new float[n];
	    	for (int i = 0; i < n; i++)
	    	{
	    		labels[i] = (float) Double.parseDouble(tokens[i]);
	    	}
	    	return labels;
	    }
}
