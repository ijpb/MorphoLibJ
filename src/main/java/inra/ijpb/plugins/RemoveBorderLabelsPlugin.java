/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import inra.ijpb.morphology.LabelImages;

/**
 * Removes all labels that touch the border, and replace them with value 0.
 * 
 * @author David Legland
 *
 */
public class RemoveBorderLabelsPlugin implements PlugIn {

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg0) {
		ImagePlus imagePlus = IJ.getImage();
		
		ImagePlus resultPlus = imagePlus.duplicate();
		LabelImages.removeBorderLabels(resultPlus);
		
		resultPlus.setTitle(imagePlus.getShortTitle() + "-killBorders");
		
		// Display with same settings as original image
		resultPlus.show();
		if (imagePlus.getStackSize() > 1) {
			resultPlus.setZ(imagePlus.getZ());
			resultPlus.setSlice(imagePlus.getSlice());
		}
	}
}
