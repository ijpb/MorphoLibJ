/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import inra.ijpb.label.LabelImages;

/**
 * Removes all the labels in a 2D/3D image but the largest one. 
 * The result is displayed in a new ImagePlus.
 * This can be used to automatically select the region of interest within the image. 
 * 
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
		
		ImagePlus resultPlus;
		try 
		{
			resultPlus = LabelImages.keepLargestLabel(imagePlus);
		}
		catch(RuntimeException ex)
		{
			// can throw an exception if no region is found
			IJ.error("MorphoLibJ Error", ex.getMessage());
			return;
		}
		
		// Display with same settings as original image
		resultPlus.show();
		if (imagePlus.getStackSize() > 1) {
			resultPlus.setZ(imagePlus.getZ());
			resultPlus.setSlice(imagePlus.getSlice());
		}
	}
}
