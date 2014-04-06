/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.GeodesicReconstruction;
import inra.ijpb.morphology.GeodesicReconstruction3D;

/**
 * Plugin for removing borders in 8-bits grayscale or binary 2D or 3D image.
 */
public class KillBordersPlugin implements PlugIn {

	@Override
	public void run(String arg0) {
		ImagePlus imagePlus = IJ.getImage();
		
		String newName = imagePlus.getShortTitle() + "-killBorders";
		
		ImagePlus resultPlus;
		if (imagePlus.getStackSize() > 1) {
			ImageStack stack = imagePlus.getStack();
			ImageStack result = GeodesicReconstruction3D.killBorders(stack);
			resultPlus = new ImagePlus(newName, result);
			
		} else {
			ImageProcessor image = imagePlus.getProcessor();
			ImageProcessor result = GeodesicReconstruction.killBorders(image);
			resultPlus = new ImagePlus(newName, result);
		}
		
		resultPlus.copyScale(imagePlus);
		resultPlus.show();
		
		if (imagePlus.getStackSize() > 1) {
			resultPlus.setSlice(imagePlus.getSlice());
		}
	}
}
