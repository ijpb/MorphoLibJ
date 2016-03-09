/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import inra.ijpb.binary.BinaryImages;

/**
 * Removes all the regions in a binary 2D or 3D image but the largest one. 
 * This can be used to automatically select the main structure of interest
 * in the image. 
 * Displays the result in a new ImagePlus.
 *
 */
public class KeepLargestRegionPlugin implements PlugIn 
{
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg0) 
	{
		ImagePlus imagePlus = IJ.getImage();
		
		ImagePlus resultPlus;
		try 
		{
			resultPlus = BinaryImages.keepLargestRegion(imagePlus);
		}
		catch(RuntimeException ex)
		{
			// can throw an exception if no region is found
			IJ.error("MorphoLibJ Error", ex.getMessage());
			return;
		}
		
		// Display with same settings as original image
		resultPlus.show();
		if (imagePlus.getStackSize() > 1) 
		{
			resultPlus.setZ(imagePlus.getZ());
			resultPlus.setSlice(imagePlus.getSlice());
		}
	}
}
