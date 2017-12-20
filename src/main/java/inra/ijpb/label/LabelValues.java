package inra.ijpb.label;

import ij.process.ImageProcessor;

/**
 * Utility methods for combining label images and intneisty images.
 * 
 * @author David Legland
 *
 */
public class LabelValues
{
	/**
	 * Private constructor to prevent class instantiation.
	 */
	private LabelValues()
	{
	}
	
	/**
	 * Computes the largest value within the <code>valueImage</code>, using the
	 * strictly positive values in <code>labelImage</code> as mask. Can be used
	 * to setup min/max display value after computing a distance transform.
	 * 
	 * @param valueImage
	 *            the image containing values (e.g. a distance map)
	 * @param labelImage
	 *            the image containing strictly positive values for foreground
	 * @return the maximum value within the value image restricted to the label
	 *         image
	 */
	public static final double maxValueWithinLabels(ImageProcessor valueImage, ImageProcessor labelImage)
	{
		// get image size
		int sizeX = valueImage.getWidth();
		int sizeY = valueImage.getHeight();
		
		// check image dimensions
		if (labelImage.getWidth() != sizeX || labelImage.getHeight() != sizeY)
		{
			throw new IllegalArgumentException("Both images must have same dimensions");
		}

		// iterate over values
		double maxValue = Double.NEGATIVE_INFINITY;
		for (int y = 0; y < sizeY; y++)
		{
			for (int x = 0; x < sizeX; x++)
			{
				// check that current pixel is within a label
				if (labelImage.getf(x, y) > 0)
				{
					maxValue = Math.max(maxValue, valueImage.getf(x, y));
				}
			}
		}
		
		return maxValue;
	}
}
