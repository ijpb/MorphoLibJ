/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import inra.ijpb.morphology.LabelImages;

/**
 * @author David Legland
 *
 */
public class LabelAreaOpeningPlugin implements PlugIn {

	@Override
	public void run(String arg0) {
		ImagePlus imagePlus = IJ.getImage();
        
        // create the dialog, with operator options
		boolean isPlanar = imagePlus.getStackSize() == 1; 
		String title = isPlanar ? "Area Opening" : "Volume Opening";
        GenericDialog gd = new GenericDialog(title);
        String label = isPlanar ? "Min Pixel Number:" : "Min Voxel Number:";
        gd.addNumericField(label, 100, 0);
        gd.showDialog();
        
        // If cancel was clicked, do nothing
        if (gd.wasCanceled())
            return;

        int nPixelMin = (int) gd.getNextNumber();
        
        // Apply size opening using IJPB library
        ImagePlus resultPlus = LabelImages.sizeOpening(imagePlus, nPixelMin);

        // Display image, and choose same slice as original
		resultPlus.show();
		if (imagePlus.getStackSize() > 1) {
			resultPlus.setSlice(imagePlus.getSlice());
		}
	}
	

}
