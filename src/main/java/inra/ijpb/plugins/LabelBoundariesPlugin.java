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
public class LabelBoundariesPlugin implements PlugIn {

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg0) {
		ImagePlus imagePlus = IJ.getImage();
		
		ImageStack stack = imagePlus.getStack();
		
		ImageStack boundaries = LabelImages.labelBoundaries(stack);
		String newName = imagePlus.getShortTitle() + "-bnd";
		ImagePlus resultPlus = new ImagePlus(newName, boundaries);
		
		// udpate meta information of result image
		resultPlus.copyScale(imagePlus);
		
		// Display with same settings as original image
		resultPlus.show();
		if (imagePlus.getStackSize() > 1) {
			resultPlus.setZ(imagePlus.getZ());
			resultPlus.setSlice(imagePlus.getSlice());
		}
	}

}
