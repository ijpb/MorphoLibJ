/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import inra.ijpb.morphology.LabelImages;

/**
 * Removes all the labels in a 2D/3D image but the largest one. 
 * The result is displayed in a new ImagePlus. 
 * This can be used to automatically remove regions that are assumed to be
 * irrelevant (e.g. background label).
 * 
 * @author David Legland
 *
 */
public class RemoveLargestLabelPlugin implements PlugIn {

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg0) {
		ImagePlus imagePlus = IJ.getImage();
		
		ImagePlus resultPlus = imagePlus.duplicate();
		LabelImages.removeLargestLabel(resultPlus);
		
		// Display with same settings as original image
		resultPlus.show();
		if (imagePlus.getStackSize() > 1) {
			resultPlus.setZ(imagePlus.getZ());
			resultPlus.setSlice(imagePlus.getSlice());
		}
	}
}
