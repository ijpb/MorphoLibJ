/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.PlugIn;
import inra.ijpb.morphology.LabelImages;

/**
 * @author David Legland
 *
 */
public class KeepLargestLabelPlugin implements PlugIn {

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg0) {
		ImagePlus imagePlus = IJ.getImage();
		
		ImagePlus resultPlus = imagePlus.duplicate();
		
		ImageStack result = LabelImages.keepLargestLabel(imagePlus.getStack());
		
		// create image plus for storing the result
		String newName = imagePlus.getShortTitle() + "-largest";
		resultPlus = new ImagePlus(newName, result);
		resultPlus.copyScale(imagePlus);
		
		// Display with same settings as original image
		resultPlus.show();
		if (imagePlus.getStackSize() > 1) {
			resultPlus.setZ(imagePlus.getZ());
			resultPlus.setSlice(imagePlus.getSlice());
		}
	}
}
