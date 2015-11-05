package inra.ijpb.binary.distmap;

import static java.lang.Math.min;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoEvent;
import inra.ijpb.algo.AlgoStub;

/**
 * Computes Chamfer distances in a 3x3 neighborhood using a float array
 * for storing result.
 * 
 * @author David Legland
 * 
 */
public class DistanceTransform3x3Float extends AlgoStub implements
		DistanceTransform 
{
	private final static int DEFAULT_MASK_LABEL = 255;

	float[] weights;

	int width;
	int height;

	ImageProcessor maskProc;

	int maskLabel = DEFAULT_MASK_LABEL;

	/** 
	 * The value assigned to result pixels that do not belong to the input
	 * image.
	 * Default is Float.MAX_VALUE.
	 */
	float backgroundValue = Float.MAX_VALUE;
	
	/**
	 * Flag for dividing final distance map by the value first weight. 
	 * This results in distance map values closer to euclidean, but with non integer values. 
	 */
	boolean normalizeMap = true;
	
	/**
	 * The inner array of values that will store the distance map. The content
	 * of the array is updated during forward and backward iterations.
	 */
	float[][] buffer;

	/**
	 * Default constructor that specifies the chamfer weights.
	 * @param weights an array of two weights for orthogonal and diagonal directions
	 */
	public DistanceTransform3x3Float(float[] weights) 
	{
		this.weights = weights;
	}

	/**
	 * Constructor specifying the chamfer weights and the optional normalization.
	 * @param weights
	 *            an array of two weights for orthogonal and diagonal directions
	 * @param normalize
	 *            flag indicating whether the final distance map should be
	 *            normalized by the first weight
	 */
	public DistanceTransform3x3Float(float[] weights, boolean normalize) 
	{
		this.weights = weights;
		this.normalizeMap = normalize;
	}

	/**
	 * @return the backgroundValue
	 */
	public float getBackgroundValue()
	{
		return backgroundValue;
	}

	/**
	 * @param backgroundValue the backgroundValue to set
	 */
	public void setBackgroundValue(float backgroundValue)
	{
		this.backgroundValue = backgroundValue;
	}

	/**
	 * Computes the distance map of the distance to the nearest boundary pixel.
	 * The function returns a new Float processor the same size as the input,
	 * with values greater or equal to zero. 
	 */
	public FloatProcessor distanceMap(ImageProcessor mask)
	{
		// size of image
		width = mask.getWidth();
		height = mask.getHeight();
		
		// update mask
		this.maskProc = mask;

		// create the result image
		FloatProcessor result = new FloatProcessor(width, height);
		result.setValue(0);
		result.fill();
		
		this.fireStatusChanged(new AlgoEvent(this, "Initialization"));
		
		// initialize empty image with either 0 (background) or Inf (foreground)
		buffer = result.getFloatArray();
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				int val = mask.get(i, j) & 0x00ff;
				buffer[i][j] = val == 0 ? 0 : backgroundValue;
			}
		}
		
		// Two iterations are enough to compute distance map to boundary
		this.fireStatusChanged(new AlgoEvent(this, "Forward Scan"));
		forwardIteration();
		this.fireStatusChanged(new AlgoEvent(this, "Backward Scan"));
		backwardIteration();

		// Normalize values by the first weight
		if (this.normalizeMap) 
		{
			this.fireStatusChanged(new AlgoEvent(this, "Normalization"));
			for (int i = 0; i < width; i++) 
			{
				for (int j = 0; j < height; j++) 
				{
					if (maskProc.getPixel(i, j) != 0) 
					{
						buffer[i][j] /= this.weights[0];
					}
				}
			}
		}
		// update the result image processor
		result.setFloatArray(buffer);
		
		this.fireStatusChanged(new AlgoEvent(this, ""));

		// Compute max value within the mask
		float maxVal = 0;
		for (int i = 0; i < width; i++)
		{
			for (int j = 0; j < height; j++) 
			{
				if (maskProc.getPixel(i, j) != 0)
					maxVal = Math.max(maxVal, buffer[i][j]);
			}
		}
		
		// calibrate min and max values of result image processor
		result.setMinAndMax(0, maxVal);

		// Forces the display to non-inverted LUT
		if (result.isInvertedLut())
			result.invertLut();
		
		return result;
	}

	private void forwardIteration() 
	{
		// variables declaration
		float ortho;
		float diago;
		float newVal;

		// Process first line: consider only the pixel on the left
		for (int i = 1; i < width; i++) 
		{
			if (maskProc.getPixel(i, 0) != maskLabel)
				continue;
			ortho = buffer[i - 1][0];
			updateIfNeeded(i, 0, ortho + weights[0]);
		}

		// Process all other lines
		for (int j = 1; j < height; j++) 
		{
			this.fireProgressChanged(this, j, height);

			// process first pixel of current line: consider pixels up and
			// upright
			if (maskProc.getPixel(0, j) == maskLabel) 
			{
				ortho = buffer[0][j - 1];
				diago = buffer[1][j - 1];
				newVal = min(ortho + weights[0], diago + weights[1]);
				updateIfNeeded(0, j, newVal);
			}

			// Process pixels in the middle of the line
			for (int i = 1; i < width - 1; i++)
			{
				// process only pixels inside structure
				if (maskProc.getPixel(i, j) != maskLabel)
					continue;

				// minimum distance of neighbor pixels
				ortho = min(buffer[i - 1][j], buffer[i][j - 1]);
				diago = min(buffer[i - 1][j - 1], buffer[i + 1][j - 1]);

				// compute new distance of current pixel
				newVal = min(ortho + weights[0], diago + weights[1]);

				// modify current pixel if needed
				updateIfNeeded(i, j, newVal);
			}

			// process last pixel of current line: consider pixels left,
			// up-left, and up
			if (maskProc.getPixel(width - 1, j) == maskLabel) 
			{
				ortho = min(buffer[width - 2][j], buffer[width - 1][j - 1]);
				diago = buffer[width - 2][j - 1];
				newVal = min(ortho + weights[0], diago + weights[1]);
				updateIfNeeded(width - 1, j, newVal);
			}

		} // end of forward iteration
	}

	private void backwardIteration()
	{
		// variables declaration
		float ortho;
		float diago;
		float newVal;

		// Process last line: consider only the pixel just after (on the right)
		for (int i = width - 2; i >= 0; i--) 
		{
			if (maskProc.getPixel(i, height - 1) != maskLabel)
				continue;

			ortho = buffer[i + 1][height - 1];
			updateIfNeeded(i, height - 1, ortho + weights[0]);
		}

		// Process regular lines
		for (int j = height - 2; j >= 0; j--)
		{
			this.fireProgressChanged(this, height-1-j, height);

			// process last pixel of the current line: consider pixels
			// down and down-left
			if (maskProc.getPixel(width - 1, j) == maskLabel)
			{
				ortho = buffer[width - 1][j + 1];
				diago = buffer[width - 2][j + 1];
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
				ortho = min(buffer[i + 1][j], buffer[i][j + 1]);
				diago = min(buffer[i - 1][j + 1], buffer[i + 1][j + 1]);

				// compute new distance of current pixel
				newVal = min(ortho + weights[0], diago + weights[1]);

				// modify current pixel if needed
				updateIfNeeded(i, j, newVal);
			}

			// process first pixel of current line: consider pixels right,
			// down-right and down
			if (maskProc.getPixel(0, j) == maskLabel) 
			{
				// curVal = array[0][j];
				ortho = min(buffer[1][j], buffer[0][j + 1]);
				diago = buffer[1][j + 1];
				newVal = min(ortho + weights[0], diago + weights[1]);
				updateIfNeeded(0, j, newVal);
			}

		} // end of backward iteration
		this.fireProgressChanged(this, height, height);
	}

	/**
	 * Update the pixel at position (i,j) with the value newVal. If newVal is
	 * greater or equal to current value at position (i,j), do nothing.
	 */
	private void updateIfNeeded(int i, int j, float newVal) 
	{
		float value = buffer[i][j];
		if (newVal < value)
		{
			buffer[i][j] = newVal;
		}
	}
}
