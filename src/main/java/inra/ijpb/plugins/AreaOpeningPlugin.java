/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
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
        
        ImagePlus resultPlus;
        String newName = imagePlus.getShortTitle() + "-sizeOpen";
        
        if (isPlanar) 
        {
            ImageProcessor image = imagePlus.getProcessor();
            ImageProcessor result = BinaryImages.areaOpening(image, nPixelMin);
            if (!(result instanceof ColorProcessor))
    			result.setLut(image.getLut());
            resultPlus = new ImagePlus(newName, result);    		
        }
        else
        {
            ImageStack image = imagePlus.getStack();
            ImageStack result = BinaryImages.volumeOpening(image, nPixelMin);
        	result.setColorModel(image.getColorModel());
            resultPlus = new ImagePlus(newName, result);
        }
        
		resultPlus.copyScale(imagePlus);
        resultPlus.show();
		
		if (imagePlus.getStackSize() > 1) {
			resultPlus.setSlice(imagePlus.getSlice());
		}
	}
	

}
