/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2017 INRA.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
package inra.ijpb.label.distmap;

import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoEvent;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.ChamferWeights;

/**
 * Computes Chamfer distances within a label image in a 3x3 neighborhood using
 * FloatProcessor object for storing result.
 * 
 * @author David Legland
 */
public class LabelDistanceTransform3x3Float extends AlgoStub implements LabelDistanceTransform 
{
	// ==================================================
	// Class variables
	
	private float[] weights;

	private int width;
	private int height;

	private ImageProcessor labelImage;

	/**
	 * Flag for dividing final distance map by the value first weight. This
	 * results in distance map values closer to euclidean, but with non integer
	 * values.
	 */
	private boolean normalizeMap = true;

	/**
	 * The inner buffer that stores the distance map. The content of the
	 * buffer is updated during forward and backward iterations.
	 */
	private FloatProcessor distMap;


	// ==================================================
	// Constructors 
	
	/**
	 * Default constructor that specifies the chamfer weights.
	 * 
	 * @param weights
	 *            an array of two weights for orthogonal and diagonal directions
	 */
	public LabelDistanceTransform3x3Float(ChamferWeights weights)
	{
		this.weights = weights.getFloatWeights();
	}

	/**
	 * Default constructor that specifies the chamfer weights.
	 * 
	 * @param weights
	 *            an array of two weights for orthogonal and diagonal directions
	 */
	public LabelDistanceTransform3x3Float(float[] weights)
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
	public LabelDistanceTransform3x3Float(ChamferWeights weights, boolean normalize)
	{
		this.weights = weights.getFloatWeights();
		this.normalizeMap = normalize;
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
	public LabelDistanceTransform3x3Float(float[] weights, boolean normalize)
	{
		this.weights = weights;
		this.normalizeMap = normalize;
	}

	
	// ==================================================
	// Methods 
	
	/**
	 * Computes the distance map of the distance to the nearest pixel with a different value.
	 * The function returns a new short processor the same size as the input,
	 * with values greater or equal to zero.
	 * 
	 * @param image a label image with black pixels (0) as foreground
	 * @return a new instance of FloatProcessor containing: <ul>
	 * <li> 0 for each background pixel </li>
	 * <li> the (strictly positive) distance to the nearest background pixel otherwise</li>
	 * </ul>
	 */
	public FloatProcessor distanceMap(ImageProcessor image) 
	{
		// size of image
		width = image.getWidth();
		height = image.getHeight();

		// update mask
		this.labelImage = image;

		// create new empty image, and fill it with black
		distMap = new FloatProcessor(width, height);
		distMap.setValue(0);
		distMap.fill();

		this.fireStatusChanged(new AlgoEvent(this, "Initialization"));
		
		// initialize empty image with either 0 (background) or Inf (foreground)
		for (int y = 0; y < height; y++) 
		{
			for (int x = 0; x < width; x++)
			{
				int label = (int) image.getf(x, y);
				distMap.setf(x, y, label == 0 ? 0 : Float.POSITIVE_INFINITY);
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
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					if (labelImage.getPixel(x, y) != 0)
					{
						distMap.setf(x, y, distMap.getf(x, y) / weights[0]);
					}
				}
			}
		}

		this.fireStatusChanged(new AlgoEvent(this, ""));

		// Compute max value within the mask
		double maxVal = 0;
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				int label = (int) labelImage.getf(x, y);
				if (label != 0)
					maxVal = Math.max(maxVal, distMap.getf(x, y));
			}
		}
		
		// calibrate min and max values of result image processor
		distMap.setMinAndMax(0, maxVal);

		// Forces the display to non-inverted LUT
		if (distMap.isInvertedLut())
			distMap.invertLut();

		return distMap;
	}

	private void forwardIteration() 
	{
		int[] dx = new int[]{-1, 0, +1, -1};
		int[] dy = new int[]{-1, -1, -1, 0};
		float[] dw = new float[]{weights[1], weights[0], weights[1], weights[0]};
		
		// Iterate over pixels
		for (int y = 0; y < height; y++)
		{
			this.fireProgressChanged(this, y, height);
			for (int x = 0; x < width; x++)
			{
				// get current label
				int label = (int) labelImage.getf(x, y);
				
				// do not process background pixels
				if (label == 0)
					continue;
				
				// current distance value
				float currentDist = distMap.getf(x, y);
				float newDist = currentDist;
				
				// iterate over neighbors
				for (int i = 0; i < dx.length; i++)
				{
					// compute neighbor coordinates
					int x2 = x + dx[i];
					int y2 = y + dy[i];
					
					// check bounds
					if (x2 < 0 || x2 >= width)
						continue;
					if (y2 < 0 || y2 >= height)
						continue;
					
					if ((int) labelImage.getf(x2, y2) != label)
					{
						// Update with distance to nearest different label
						newDist = dw[i];
					}
					else
					{
						// Increment distance
						newDist = Math.min(newDist, distMap.getf(x2, y2) + dw[i]);
					}
				}
				
				if (newDist < currentDist) 
				{
					distMap.setf(x, y, newDist);
				}
			}
		}
		
		this.fireProgressChanged(this, height, height);
	}

	private void backwardIteration() 
	{
		int[] dx = new int[]{+1, 0, -1, +1};
		int[] dy = new int[]{+1, +1, +1, 0};
		float[] dw = new float[]{weights[1], weights[0], weights[1], weights[0]};
		
		// Iterate over pixels
		for (int y = height-1; y >= 0; y--)
		{
			this.fireProgressChanged(this, height-1-y, height);
			for (int x = width-1; x >= 0; x--)
			{
				// get current label
				int label = (int) labelImage.getf(x, y);
				
				// do not process background pixels
				if (label == 0)
					continue;
				
				// current distance value
				float currentDist = distMap.getf(x, y);
				float newDist = currentDist;
				
				// iterate over neighbors
				for (int i = 0; i < dx.length; i++)
				{
					// compute neighbor coordinates
					int x2 = x + dx[i];
					int y2 = y + dy[i];
					
					// check bounds
					if (x2 < 0 || x2 >= width)
						continue;
					if (y2 < 0 || y2 >= height)
						continue;
					
					if ((int) labelImage.getf(x2, y2) != label)
					{
						// Update with distance to nearest different label
						newDist = dw[i];
					}
					else
					{
						// Increment distance
						newDist = Math.min(newDist, distMap.getf(x2, y2) + dw[i]);
					}
				}
				
				if (newDist < currentDist) 
				{
					distMap.setf(x, y, newDist);
				}
			}
		}
		
		this.fireProgressChanged(this, height, height);
	}
}
