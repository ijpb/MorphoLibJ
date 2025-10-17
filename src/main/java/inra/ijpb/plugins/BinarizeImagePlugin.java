/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import inra.ijpb.binary.BinaryImages;

/**
 * Converts an image into a binary image, by setting to true all the pixels or
 * voxels of the input image that are (strictly) greater than zero.
 */
public class BinarizeImagePlugin implements PlugIn
{

	@Override
	public void run(String arg)
	{
		ImagePlus imagePlus = IJ.getImage();
		ImagePlus resultPlus = BinaryImages.binarize(imagePlus);

		// copy settings
		resultPlus.copyScale(imagePlus);
		resultPlus.setDisplayRange(imagePlus.getDisplayRangeMin(), imagePlus.getDisplayRangeMax());
		// selectedPlus.setLut( imagePlus.getProcessor().getLut() );

		// display and adapt visible slice
		resultPlus.show();
		if (imagePlus.getStackSize() > 1)
		{
			resultPlus.setSlice(imagePlus.getCurrentSlice());
		}
	}

}
