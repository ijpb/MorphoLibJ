/**
 * 
 */
package inra.ijpb.morphology;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.morphology.attrfilt.AreaOpening;
import inra.ijpb.morphology.attrfilt.AreaOpeningQueue;
import inra.ijpb.morphology.attrfilt.SizeOpening3D;
import inra.ijpb.morphology.attrfilt.SizeOpening3DQueue;

/**
 * Several static methods for computation of attribute filtering (opening,
 * thinning...) on gray level images.
 * 
 * @author dlegland
 *
 */
public class AttributeFiltering
{
	/**
	 * Applies grayscale size opening on input image, by retaining only the
	 * connected components that contain at least the specified number of elements
	 * (pixels or voxels).
	 * 
	 * @param imagePlus
	 *            instance of ImagePlus containing the input 2D or 3D image
	 * @param minSize
	 *            the minimum number of pixels or voxels at a given gray level
	 * @return the result of grayscale size opening on the input image
	 */
	public static final ImagePlus sizeOpening(ImagePlus imagePlus, int minSize)
	{
		ImagePlus resultPlus;
		if (imagePlus.getStackSize() == 1)
		{
			ImageProcessor image = imagePlus.getProcessor();
			ImageProcessor result = areaOpening(image, minSize);
			String newName = imagePlus.getShortTitle() + "-areaOpen";
			resultPlus = new ImagePlus(newName, result);
		}
		else
		{
			ImageStack image = imagePlus.getStack();
			ImageStack result = volumeOpening(image, minSize);
			String newName = imagePlus.getShortTitle() + "-sizeOpen";
			resultPlus = new ImagePlus(newName, result);
		}
		

		if (imagePlus.getStackSize() > 1)
		{
			resultPlus.setSlice(imagePlus.getSlice());
		}
		return resultPlus;
	}

	/**
	 * Applies grayscale area opening on input image, by retaining only the
	 * connected components that contain at least the specified number of pixels.
	 * 
	 * @param image
	 *            input grayscale image
	 * @param minArea
	 *            the minimum number of pixels at a given gray level
	 * @return the result of grayscale size opening on the input image
	 */
	public static final ImageProcessor areaOpening(ImageProcessor image, int minArea)
	{
		AreaOpening algo = new AreaOpeningQueue();
		DefaultAlgoListener.monitor(algo);
		return algo.process(image, minArea);
	}

	/**
	 * Applies grayscale volume opening on input 3D image, by retaining only the
	 * connected components that contain at least the specified number of voxels.
	 * 
	 * @param image
	 *            input 3D grayscale image
	 * @param minVolume
	 *            the minimum number of voxels at a given gray level
	 * @return the result of grayscale size opening on the input image
	 */
	public static final ImageStack volumeOpening(ImageStack image, int minVolume)
	{
		SizeOpening3D algo = new SizeOpening3DQueue();
		DefaultAlgoListener.monitor(algo);
		return algo.process(image, minVolume);
	}
}
