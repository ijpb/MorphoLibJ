/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import inra.ijpb.binary.BinaryImages;

/**
 * Removes the largest region of a binary 2D or 3D image. 
 * Displays the result in a new ImagePlus.
 * 
 * @author David Legland
 *
 */
public class RemoveLargestRegionPlugin implements PlugIn {

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg0) {
		ImagePlus imagePlus = IJ.getImage();
		
		ImagePlus resultPlus = imagePlus.duplicate();
		BinaryImages.removeLargestRegion(resultPlus);
		String newName = imagePlus.getShortTitle() + "-killLargest";
		resultPlus.setTitle(newName);
		
		// Display with same settings as original image
		resultPlus.show();
		if (imagePlus.getStackSize() > 1) {
			resultPlus.setZ(imagePlus.getZ());
			resultPlus.setSlice(imagePlus.getSlice());
		}
		
	
	}
}
