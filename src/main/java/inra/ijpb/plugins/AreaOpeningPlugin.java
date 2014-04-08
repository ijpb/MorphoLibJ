/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.binary.BinaryImages;

/**
 * @author David Legland
 *
 */
public class AreaOpeningPlugin implements PlugIn {

	@Override
	public void run(String arg0) {
		ImagePlus imagePlus = IJ.getImage();
        
        // create the dialog, with operator options
        GenericDialog gd = new GenericDialog("Area Opening");
        gd.addNumericField("Min Pixel Number:", 100, 0);
        gd.showDialog();
        
        // If cancel was clicked, do nothing
        if (gd.wasCanceled())
            return;

        int nPixelMin = (int) gd.getNextNumber();
        
        ImageProcessor image = imagePlus.getProcessor();
        ImageProcessor result = BinaryImages.areaOpening(image, nPixelMin);
        
        if (!(result instanceof ColorProcessor))
			result.setLut(image.getLut());
		
        String newName = imagePlus.getShortTitle() + "-areaOpen";
        ImagePlus resultPlus = new ImagePlus(newName, result);
        
        resultPlus.show();
		resultPlus.copyScale(imagePlus);
		
		if (imagePlus.getStackSize() > 1) {
			resultPlus.setSlice(imagePlus.getSlice());
		}
	}
	

}
