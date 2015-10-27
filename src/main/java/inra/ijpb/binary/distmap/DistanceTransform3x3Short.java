package inra.ijpb.binary.distmap;

import static java.lang.Math.min;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

/**
 * Computes Chamfer distances in a 3x3 neighborhood using ShortProcessor object
 * for storing result.
 * 
 * @author David Legland
 * 
 */
public class DistanceTransform3x3Short implements DistanceTransform 
{
	private final static int DEFAULT_MASK_LABEL = 255;

	short[] weights;

	int width;
	int height;

	ImageProcessor maskProc;

	int maskLabel = DEFAULT_MASK_LABEL;

	/**
	 * The value assigned to result pixels that do not belong to the input
	 * image. Default is short.MAX_VALUE.
	 */
	short backgroundValue = Short.MAX_VALUE;

	/**
	 * Flag for dividing final distance map by the value first weight. This
	 * results in distance map values closer to euclidean, but with non integer
	 * values.
	 */
	boolean normalizeMap = true;

	/**
	 * The inner buffer that will store the distance map. The content of the
	 * buffer is updated during forward and backward iterations.
	 */
	ShortProcessor buffer;

	/**
	 * Default constructor that specifies the chamfer weights.
	 * 
	 * @param weights
	 *            an array of two weights for orthogonal and diagonal directions
	 */
	public DistanceTransform3x3Short(short[] weights)
	{
		this.weights = weights;
	}

	/**
	 * Constructor specifying the chamfer weights and the optional
	 * normalization.
	 * 
	 * @param weights
	 *            an array of two weights for orthogonal and diagonal directions
	 * @param normalize
	 *            flag indicating whether the final distance map should be
	 *            normalized by the first weight
	 */
	public DistanceTransform3x3Short(short[] weights, boolean normalize)
	{
		this.weights = weights;
		this.normalizeMap = normalize;
	}

	/**
	 * @return the backgroundValue
	 */
	public short getBackgroundValue()
	{
		return backgroundValue;
	}

	/**
	 * @param backgroundValue
	 *            the backgroundValue to set
	 */
	public void setBackgroundValue(short backgroundValue) 
	{
		this.backgroundValue = backgroundValue;
	}

	/**
	 * Computes the distance map of the distance to the nearest boundary pixel.
	 * The function returns a new short processor the same size as the input,
	 * with values greater or equal to zero.
	 */
	public ShortProcessor distanceMap(ImageProcessor mask) 
	{

		// size of image
		width = mask.getWidth();
		height = mask.getHeight();

		// update mask
		this.maskProc = mask;

		// create new empty image, and fill it with black
		buffer = new ShortProcessor(width, height);
		buffer.setValue(0);
		buffer.fill();

		// initialize empty image with either 0 (background) or Inf (foreground)
		for (int i = 0; i < width; i++)
		{
			for (int j = 0; j < height; j++) 
			{
				int val = mask.get(i, j) & 0x00ff;
				buffer.set(i, j, val == 0 ? 0 : backgroundValue);
			}
		}

		// Two iterations are enough to compute distance map to boundary
		forwardIteration();
		backwardIteration();

		// Normalize values by the first weight
		if (this.normalizeMap)
		{
			for (int i = 0; i < width; i++)
			{
				for (int j = 0; j < height; j++)
				{
					if (maskProc.getPixel(i, j) != 0)
					{
						buffer.set(i, j, buffer.get(i, j) / weights[0]);
					}
				}
			}
		}

		// Compute max value within the mask
		short maxVal = 0;
		for (int i = 0; i < width; i++)
		{
			for (int j = 0; j < height; j++)
			{
				if (maskProc.getPixel(i, j) != 0)
					maxVal = (short) Math.max(maxVal, buffer.get(i, j));
			}
		}
		
		// calibrate min and max values of result imaeg processor
		buffer.setMinAndMax(0, maxVal);

		// Forces the display to non-inverted LUT
		if (buffer.isInvertedLut())
			buffer.invertLut();

		return buffer;
	}

	private void forwardIteration() 
	{
		// variables declaration
		int ortho;
		int diago;
		int newVal;

		// Process first line: consider only the pixel on the left
		for (int i = 1; i < width; i++) 
		{
			if (maskProc.get(i, 0) != maskLabel)
				continue;
			ortho = buffer.get(i - 1, 0) + weights[0];
			updateIfNeeded(i, 0, ortho);
		}

		// Process all other lines
		for (int j = 1; j < height; j++) 
		{
			// process first pixel of current line: consider pixels up and
			// upright
			if (maskProc.get(0, j) == maskLabel) 
			{
				ortho = buffer.get(0, j - 1);
				diago = buffer.get(1, j - 1);
				newVal = min(ortho + weights[0], diago + weights[1]);
				updateIfNeeded(0, j, newVal);
			}

			// Process pixels in the middle of the line
			for (int i = 1; i < width - 1; i++) 
			{
				// process only pixels inside structure
				if (maskProc.get(i, j) != maskLabel)
					continue;

				// minimum distance of neighbor pixels
				ortho = min(buffer.get(i - 1, j), buffer.get(i, j - 1));
				diago = min(buffer.get(i - 1, j - 1), buffer.get(i + 1, j - 1));

				// compute new distance of current pixel
				newVal = min(ortho + weights[0], diago + weights[1]);

				// modify current pixel if needed
				updateIfNeeded(i, j, newVal);
			}

			// process last pixel of current line: consider pixels left,
			// up-left, and up
			if (maskProc.getPixel(width - 1, j) == maskLabel) 
			{
				ortho = min(buffer.get(width - 2, j),
						buffer.get(width - 1, j - 1));
				diago = buffer.get(width - 2, j - 1);
				newVal = min(ortho + weights[0], diago + weights[1]);
				updateIfNeeded(width - 1, j, newVal);
			}

		} // end of forward iteration
	}

	private void backwardIteration() 
	{
		// variables declaration
		int ortho;
		int diago;
		int newVal;

		// Process last line: consider only the pixel just after (on the right)
		for (int i = width - 2; i >= 0; i--)
		{
			if (maskProc.getPixel(i, height - 1) != maskLabel)
				continue;

			newVal = buffer.get(i + 1, height - 1) + weights[0];
			updateIfNeeded(i, height - 1, newVal);
		}

		// Process regular lines
		for (int j = height - 2; j >= 0; j--) 
		{
			// process last pixel of the current line: consider pixels
			// down and down-left
			if (maskProc.getPixel(width - 1, j) == maskLabel) 
			{
				ortho = buffer.get(width - 1, j + 1);
				diago = buffer.get(width - 2, j + 1);
				newVal = min(ortho + weights[0], diago + weights[1]);
				updateIfNeeded(width - 1, j, newVal);
			}

			// Process pixels in the middle of the current line
			for (int i = width - 2; i > 0; i--) 
			{
				// process only pixels inside structure
				if (maskProc.getPixel(i, j) != maskLabel)
					continue;

				// minimum distance of neighbor pixels
				ortho = min(buffer.get(i + 1, j), buffer.get(i, j + 1));
				diago = min(buffer.get(i - 1, j + 1), buffer.get(i + 1, j + 1));

				// compute new distance of current pixel
				newVal = min(ortho + weights[0], diago + weights[1]);

				// modify current pixel if needed
				updateIfNeeded(i, j, newVal);
			}

			// process first pixel of current line: consider pixels right,
			// down-right and down
			if (maskProc.getPixel(0, j) == maskLabel) 
			{
				ortho = min(buffer.get(1, j), buffer.get(0, j + 1));
				diago = buffer.get(1, j + 1);
				newVal = min(ortho + weights[0], diago + weights[1]);
				updateIfNeeded(0, j, newVal);
			}
		} // end of backward iteration
	}

	/**
	 * Update the pixel at position (i,j) with the value newVal. If newVal is
	 * greater or equal to current value at position (i,j), do nothing.
	 */
	private void updateIfNeeded(int i, int j, int newVal) 
	{
		int value = buffer.get(i, j);
		if (newVal < value) 
		{
			buffer.set(i, j, newVal);
		}
	}
}
