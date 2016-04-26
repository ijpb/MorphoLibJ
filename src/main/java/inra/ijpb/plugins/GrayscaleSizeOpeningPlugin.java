/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.AttributeFiltering;

/**
 * Removes connected components whose size is inferior to the given number of
 * elements, in a 2D or 3D grayscale image.
 * 
 * @see AreaOpeningPlugin
 * 
 * @author David Legland
 *
 */
public class GrayscaleSizeOpeningPlugin implements PlugIn
{
	@Override
	public void run(String arg0)
	{
		ImagePlus imagePlus = IJ.getImage();
        
        // create the dialog, with operator options
		boolean isPlanar = imagePlus.getStackSize() == 1; 
		String title = isPlanar ? "Grayscale Area Opening" : " Grayscale Volume Opening";
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
        	ImageProcessor result = AttributeFiltering.areaOpening(image, nPixelMin);
            resultPlus = new ImagePlus(newName, result);    		
        }
        else
        {
            ImageStack image = imagePlus.getStack();
            ImageStack result = AttributeFiltering.volumeOpening(image, nPixelMin);
            resultPlus = new ImagePlus(newName, result);
        }
        
		resultPlus.copyScale(imagePlus);
        resultPlus.show();
		
		if (imagePlus.getStackSize() > 1) {
			resultPlus.setSlice(imagePlus.getSlice());
		}
	}
	

}
