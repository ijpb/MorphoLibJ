/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import inra.ijpb.data.image.Images3D;

/**
 * Simple plugin to replace a value by another one, for 2D/3D images, for 
 * gray8, gray16 and float images.
 * 
 * @author David Legland
 *
 */
public class ReplaceValuePlugin implements PlugIn {

	@Override
	public void run(String arg0) {
		ImagePlus imagePlus = IJ.getImage();
		
		GenericDialog gd = new GenericDialog("Replace Value");
		gd.addNumericField("Initial Value", 1, 0);
		gd.addNumericField("Final Value", 0, 0);
		gd.showDialog();
		
		if (gd.wasCanceled())
			return;
		
		double initialValue = gd.getNextNumber();
		double finalValue = gd.getNextNumber();

		Images3D.replaceValue(imagePlus, initialValue, finalValue);
		imagePlus.updateAndDraw();
	}
}
