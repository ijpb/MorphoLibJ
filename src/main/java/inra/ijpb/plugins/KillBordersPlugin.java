/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
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
		if (imagePlus.getStackSize() == 1) {
			// Process planar images
			ImageProcessor image = imagePlus.getProcessor();
			ImageProcessor result = GeodesicReconstruction.killBorders(image);
			if (!(result instanceof ColorProcessor))
				result.setLut(image.getLut());
			resultPlus = new ImagePlus(newName, result);
			
		} else {
			// Process 3D stack
			ImageStack image = imagePlus.getStack();
			ImageStack result = GeodesicReconstruction3D.killBorders(image);
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
