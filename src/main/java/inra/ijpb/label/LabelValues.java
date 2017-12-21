package inra.ijpb.label;

import java.awt.Point;
import java.util.Map;

import ij.process.ImageProcessor;

/**
 * Utility methods for combining label images and intensity images.
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
	
	
	/**
	 * Find maximum value in intensity image for each label.
	 * 
	 * @param image
	 *            the intensity image
	 * @param labelImage
	 *            the image containing labels
	 * @param labels
	 *            the list of labels for which the values are computed
	 * @return an array of values corresponding to the maximum value within each
	 *         label
	 */
	public static final double[] maxValues(ImageProcessor image,
			ImageProcessor labelImage, int[] labels)
	{
		// Compute value of greatest label
		int nLabels = labels.length;
		
		// init index of each label
		Map<Integer, Integer> labelIndices = LabelImages.mapLabelIndices(labels);
				
		// Init value of maximum for each label
		double[] maxValues = new double[nLabels];
		for (int i = 0; i < nLabels; i++)
			maxValues[i] = Double.NEGATIVE_INFINITY;
		
		// iterate on image pixels
		int sizeX = labelImage.getWidth();
		int sizeY = labelImage.getHeight();
		for (int y = 0; y < sizeY; y++) 
		{
			for (int x = 0; x < sizeX; x++) 
			{
				int label = (int) labelImage.getf(x, y);
				
				// do not process pixels that do not belong to particle
				if (label == 0)
					continue;

				if (labelIndices.containsKey(label))
				{
					int index = labelIndices.get(label);
					
					// update values and positions
					double value = image.getf(x, y);
					if (value > maxValues[index])
						maxValues[index] = value;
				}
			}
		}
				
		return maxValues;
	}
	
	/**
	 * For each label, finds the position of the point belonging to label region
	 * defined by <code>labelImage</code> and with maximal value in intensity
	 * image <code>valueImage</code>.
	 * 
	 * @param valueImage
	 *            the intensity image containing values to compare
	 * @param labelImage
	 *            the intensity image containing label of each pixel
	 * @param labels
	 *            the list of labels in the label image
	 * @return the position of maximum value in intensity image for each label
	 */
	public static final PositionValuePair[] findMaxValues(ImageProcessor valueImage, 
			ImageProcessor labelImage, int[] labels)
	{
		// Create associative map between each label and its index
		Map<Integer,Integer> labelIndices = LabelImages.mapLabelIndices(labels);
		
		// Init Position and value of maximum for each label
		int nLabels = labels.length;
		PositionValuePair[] pairs = new PositionValuePair[nLabels]; 
		for (int i = 0; i < nLabels; i++) 
		{
			pairs[i] = new PositionValuePair(new Point(-1, -1), Double.NEGATIVE_INFINITY);
		}
		
		// iterate on image pixels
		int sizeX = labelImage.getWidth();
		int sizeY = labelImage.getHeight();
		for (int y = 0; y < sizeY; y++) 
		{
			for (int x = 0; x < sizeX; x++) 
			{
				int label = (int) labelImage.getf(x, y);
				
				// do not process pixels that do not belong to any particle
				if (label == 0)
					continue;
				if (!labelIndices.containsKey(label))
					continue;

				// get position-value pair corresponding to current label
				int index = labelIndices.get(label);
				PositionValuePair pair = pairs[index];
				
				// update values and positions
				double value = valueImage.getf(x, y);
				if (value > pair.value) 
				{
					pair.position.setLocation(x, y);
					pair.value = value;
				}
			}
		}
				
		return pairs;
	}
	
	/**
	 * For each label, finds the position of the point belonging to label region
	 * defined by <code>labelImage</code> and with minimal value in intensity
	 * image <code>valueImage</code>.
	 * 
	 * @param valueImage
	 *            the intensity image containing values to compare
	 * @param labelImage
	 *            the intensity image containing label of each pixel
	 * @param labels
	 *            the list of labels in the label image
	 * @return the position of minimum value in intensity image for each label
	 */
	public static final PositionValuePair[] findMinValues(ImageProcessor valueImage, 
			ImageProcessor labelImage, int[] labels)
	{
		// Create associative map between each label and its index
		Map<Integer,Integer> labelIndices = LabelImages.mapLabelIndices(labels);
		
		// Init Position and value of maximum for each label
		int nLabels = labels.length;
		PositionValuePair[] pairs = new PositionValuePair[nLabels]; 
		for (int i = 0; i < nLabels; i++) 
		{
			pairs[i] = new PositionValuePair(new Point(-1, -1), Double.POSITIVE_INFINITY);
		}
		
		// iterate on image pixels
		int sizeX = labelImage.getWidth();
		int sizeY = labelImage.getHeight();
		for (int y = 0; y < sizeY; y++) 
		{
			for (int x = 0; x < sizeX; x++) 
			{
				int label = (int) labelImage.getf(x, y);
				
				// do not process pixels that do not belong to any particle
				if (label == 0)
					continue;
				if (!labelIndices.containsKey(label))
					continue;

				// get position-value pair corresponding to current label
				int index = labelIndices.get(label);
				PositionValuePair pair = pairs[index];
				
				// update values and positions
				double value = valueImage.getf(x, y);
				if (value < pair.value) 
				{
					pair.position.setLocation(x, y);
					pair.value = value;
				}
			}
		}
				
		return pairs;
	}


	/**
	 * For each label, finds the position of the point belonging to label region
	 * defined by <code>labelImage</code> and with maximal value in intensity
	 * image <code>valueImage</code>.
	 * 
	 * @param valueImage
	 *            the intensity image containing values to compare
	 * @param labelImage
	 *            the intensity image containing label of each pixel
	 * @param labels
	 *            the list of labels in the label image
	 * @return the position of maximum value in intensity image for each label
	 */
	public static final Point[] findPositionOfMaxValues(ImageProcessor valueImage, 
			ImageProcessor labelImage, int[] labels)
	{
		// Create associative map between each label and its index
		Map<Integer,Integer> labelIndices = LabelImages.mapLabelIndices(labels);
		
		// Init Position and value of maximum for each label
		int nLabels = labels.length;
		Point[] posMax 	= new Point[nLabels];
		float[] maxValues = new float[nLabels];
		for (int i = 0; i < nLabels; i++) 
		{
			maxValues[i] = Float.NEGATIVE_INFINITY;
			posMax[i] = new Point(-1, -1);
		}
		
		// iterate on image pixels
		int width 	= labelImage.getWidth();
		int height 	= labelImage.getHeight();
		for (int y = 0; y < height; y++) 
		{
			for (int x = 0; x < width; x++) 
			{
				int label = (int) labelImage.getf(x, y);
				
				// do not process pixels that do not belong to any particle
				if (label == 0)
					continue;

				int index = labelIndices.get(label);
				
				// update values and positions
				float value = valueImage.getf(x, y);
				if (value > maxValues[index]) 
				{
					posMax[index].setLocation(x, y);
					maxValues[index] = value;
				}
			}
		}
				
		return posMax;
	}

	/**
	 * For each label, finds the position of the point belonging to label region
	 * defined by <code>labelImage</code> and with minimal value in intensity
	 * image <code>valueImage</code>.
	 * 
	 * @param valueImage
	 *            the intensity image containing values to compare
	 * @param labelImage
	 *            the intensity image containing label of each pixel
	 * @param labels
	 *            the list of labels in the label image
	 * @return the position of minimum value in intensity image for each label
	 */
	public static final Point[] findPositionOfMinValues(ImageProcessor valueImage, 
			ImageProcessor labelImage, int[] labels)
	{
		// Create associative map between each label and its index
		Map<Integer,Integer> labelIndices = LabelImages.mapLabelIndices(labels);
		
		// Init Position and value of minimum for each label
		int nLabels = labels.length;
		Point[] posMax 	= new Point[nLabels];
		float[] maxValues = new float[nLabels];
		for (int i = 0; i < nLabels; i++) 
		{
			maxValues[i] = Float.POSITIVE_INFINITY;
			posMax[i] = new Point(-1, -1);
		}
		
		// iterate on image pixels
		int width 	= labelImage.getWidth();
		int height 	= labelImage.getHeight();
		for (int y = 0; y < height; y++) 
		{
			for (int x = 0; x < width; x++) 
			{
				int label = (int) labelImage.getf(x, y);
				
				// do not process pixels that do not belong to any particle
				if (label == 0)
					continue;

				int index = labelIndices.get(label);
				
				// update values and positions
				float value = valueImage.getf(x, y);
				if (value < maxValues[index]) 
				{
					posMax[index].setLocation(x, y);
					maxValues[index] = value;
				}
			}
		}
				
		return posMax;
	}

	public static class PositionValuePair
	{
		Point position;
		double value;
		
		public PositionValuePair(Point position, double value)
		{
			this.position = position;
			this.value = value;
		}
		
		public Point getPosition()
		{
			return position;
		}
		
		public double getValue()
		{
			return value;
		}
	}
}
