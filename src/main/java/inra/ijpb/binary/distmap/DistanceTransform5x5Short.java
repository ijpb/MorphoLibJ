/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
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
package inra.ijpb.binary.distmap;

import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import inra.ijpb.algo.AlgoEvent;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.ChamferWeights;
import inra.ijpb.label.LabelValues;

/**
 * <p>Computes Chamfer distances in a 5x5 neighborhood using a ShortProcessor 
 * object for storing result.</p>
 * 
 * <p>Uses 5x5 chamfer map, with two passes (one forward, one backward).
 * Three different weights are provided for orthogonal, diagonal, and 
 * "chess-knight" moves. Weights equal to (5,7,11) usually give nice results.
 * </p>
 * 
 * <p>
 * Example of use:
 *<pre>{@code
 *	short[] shortWeights = ChamferWeights.CHESSKNIGHT.getShortWeights();
 *	boolean normalize = true;
 *	DistanceTransform dt = new DistanceTransform5x5Short(shortWeights, normalize);
 *	ImageProcessor result = dt.distanceMap(inputImage);
 *	// or:
 *	ImagePlus resultPlus = BinaryImages.distanceMap(imagePlus, shortWeights, normalize);
 *}</pre>
 * 
 * @see inra.ijpb.binary.BinaryImages#distanceMap(ImageProcessor, short[], boolean)
 * @see inra.ijpb.binary.distmap.DistanceTransform5x5Float
 * @see inra.ijpb.binary.distmap.DistanceTransform
 * 
 * @deprecated replaced by ChamferDistanceTransform2DShort (since 1.5.0)
 * 
 * @author David Legland
 */
@Deprecated
public class DistanceTransform5x5Short extends AlgoStub implements DistanceTransform 
{
	// ==================================================
	// Class variables

	/**
	 * The chamfer weights used to propagate distances to neighbor pixels.
	 */
	private short[] weights;

	/**
	 * Flag for dividing final distance map by the value first weight. This
	 * results in distance map values closer to euclidean, but with non integer
	 * values.
	 */
	private boolean normalizeMap = true;


	// ==================================================
	// Constructors 
	
	/**
	 * Default constructor with predefined chamfer weights.
	 */
	public DistanceTransform5x5Short() 
	{
		this(new short[]{5, 7, 11}, true);
	}

	/**
	 * Constructor specifying the chamfer weights and the optional
	 * normalization.
	 * 
	 * @param weights
	 *            a set of chamfer weights with at least three values
	 */
	public DistanceTransform5x5Short(ChamferWeights weights)
	{
		this(weights.getShortWeights(), true);
	}

	/**
	 * Default constructor that specifies the chamfer weights.
	 * @param weights an array of two weights for orthogonal and diagonal directions
	 */
	public DistanceTransform5x5Short(short[] weights) 
	{
		this(weights, true);
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
	public DistanceTransform5x5Short(ChamferWeights weights, boolean normalize)
	{
		this(weights.getShortWeights(), normalize);
	}

	/**
	 * Constructor specifying the chamfer weights and the optional normalization.
	 * @param weights
	 *            an array of two weights for orthogonal and diagonal directions
	 * @param normalize
	 *            flag indicating whether the final distance map should be
	 *            normalized by the first weight
	 */
	public DistanceTransform5x5Short(short[] weights, boolean normalize)
	{
		if (weights.length < 3) 
		{
			short[] newWeights = new short[3];
			newWeights[0] = weights[0];
			newWeights[1] = weights[1];
			newWeights[2] =(short) (weights[0] + weights[1]);
			weights = newWeights;
		}
		this.weights = weights;
		this.normalizeMap = normalize;
	}

	// ==================================================
	// Methods 
	
	/**
	 * Computes the distance map of the distance to the nearest pixel with a
	 * different value. The function returns a new short processor the same size
	 * as the input, with values greater or equal to zero.
	 * 
	 * @param labelImage
	 *            a label image with black pixels (0) as foreground
	 * @return a new instance of ShortProcessor containing:
	 *         <ul>
	 *         <li>0 for each background pixel</li>
	 *         <li>the (strictly positive) distance to the nearest background
	 *         pixel otherwise</li>
	 *         </ul>
	 */
	public ShortProcessor distanceMap(ImageProcessor labelImage) 
	{
		ShortProcessor distMap = initializeResult(labelImage);
		
		// Two iterations are enough to compute distance map to boundary
		forwardScan(distMap, labelImage);
		backwardScan(distMap, labelImage);

		// Normalize values by the first weight
		if (this.normalizeMap)
		{
			normalizeResult(distMap, labelImage);
		}

		// Compute max value within the mask for setting min/max of ImageProcessor
		double maxVal = LabelValues.maxValueWithinLabels(distMap, labelImage);
		distMap.setMinAndMax(0, maxVal);

		// Forces the display to non-inverted LUT
		if (distMap.isInvertedLut())
			distMap.invertLut();

		this.fireStatusChanged(new AlgoEvent(this, ""));

		return distMap;
	}

	
	// ==================================================
	// Inner computation methods 
	
	private ShortProcessor initializeResult(ImageProcessor labelImage)
	{
		this.fireStatusChanged(new AlgoEvent(this, "Initialization"));

		// size of image
		int sizeX = labelImage.getWidth();
		int sizeY = labelImage.getHeight();

		// create new empty image, and fill it with black
		ShortProcessor distMap = new ShortProcessor(sizeX, sizeY);
		distMap.setValue(0);
		distMap.fill();

		// initialize empty image with either 0 (background) or Inf (foreground)
		for (int y = 0; y < sizeY; y++) 
		{
			for (int x = 0; x < sizeX; x++)
			{
				int label = (int) labelImage.getf(x, y);
				distMap.set(x, y, label == 0 ? 0 : Short.MAX_VALUE);
			}
		}
		
		return distMap;
	}
	
	private void forwardScan(ShortProcessor distMap, ImageProcessor labelImage) 
	{
		this.fireStatusChanged(new AlgoEvent(this, "Forward Scan"));

		// Initialize pairs of offset and weights
		int[] dx = new int[]{-1, +1,  -2, -1,  0, +1, +2,  -1};
		int[] dy = new int[]{-2, -2,  -1, -1, -1, -1, -1,   0};
		short[] dw = new short[] { 
				weights[2], weights[2], 
				weights[2], weights[1], weights[0], weights[1], weights[2], 
				weights[0] };
		
		//  size of image
		int sizeX = labelImage.getWidth();
		int sizeY = labelImage.getHeight();

		// Iterate over pixels
		for (int y = 0; y < sizeY; y++)
		{
			this.fireProgressChanged(this, y, sizeY);
			for (int x = 0; x < sizeX; x++)
			{
				// get current label
				int label = (int) labelImage.getf(x, y);
				
				// do not process background pixels
				if (label == 0)
					continue;
				
				// current distance value
				int currentDist = distMap.get(x, y);
				int newDist = currentDist;
				
				// iterate over neighbors
				for (int i = 0; i < dx.length; i++)
				{
					// compute neighbor coordinates
					int x2 = x + dx[i];
					int y2 = y + dy[i];
					
					// check bounds
					if (x2 < 0 || x2 >= sizeX)
						continue;
					if (y2 < 0 || y2 >= sizeY)
						continue;
					
					if ((int) labelImage.getf(x2, y2) != label)
					{
						// Update with distance to nearest different label
						newDist = Math.min(newDist, dw[i]);
					}
					else
					{
						// Increment distance
						newDist = Math.min(newDist, distMap.get(x2, y2) + dw[i]);
					}
				}
				
				if (newDist < currentDist) 
				{
					distMap.set(x, y, newDist);
				}
			}
		}
		
		this.fireProgressChanged(this, sizeY, sizeY);
	}

	private void backwardScan(ShortProcessor distMap, ImageProcessor labelImage) 
	{
		this.fireStatusChanged(new AlgoEvent(this, "Backward Scan"));

		// Initialize pairs of offset and weights
		int[] dx = new int[]{+1, -1,  +2, +1,  0, -1, -2,  +1};
		int[] dy = new int[]{+2, +2,  +1, +1, +1, +1, +1,   0};
		short[] dw = new short[] { 
				weights[2], weights[2], 
				weights[2], weights[1], weights[0], weights[1], weights[2], 
				weights[0] };
		
		//  size of image
		int sizeX = labelImage.getWidth();
		int sizeY = labelImage.getHeight();

		// Iterate over pixels
		for (int y = sizeY-1; y >= 0; y--)
		{
			this.fireProgressChanged(this, sizeY-1-y, sizeY);
			for (int x = sizeX-1; x >= 0; x--)
			{
				// get current label
				int label = (int) labelImage.getf(x, y);
				
				// do not process background pixels
				if (label == 0)
					continue;
				
				// current distance value
				int currentDist = distMap.get(x, y);
				int newDist = currentDist;
				
				// iterate over neighbors
				for (int i = 0; i < dx.length; i++)
				{
					// compute neighbor coordinates
					int x2 = x + dx[i];
					int y2 = y + dy[i];
					
					// check bounds
					if (x2 < 0 || x2 >= sizeX)
						continue;
					if (y2 < 0 || y2 >= sizeY)
						continue;
					
					if ((int) labelImage.getf(x2, y2) != label)
					{
						// Update with distance to nearest different label
					    newDist = Math.min(newDist, dw[i]);
					}
					else
					{
						// Increment distance
						newDist = Math.min(newDist, distMap.get(x2, y2) + dw[i]);
					}
				}
				
				if (newDist < currentDist) 
				{
					distMap.set(x, y, newDist);
				}
			}
		}
		
		this.fireProgressChanged(this, sizeY, sizeY);
	}

	private void normalizeResult(ShortProcessor distMap, ImageProcessor labelImage)
	{
		this.fireStatusChanged(new AlgoEvent(this, "Normalization"));
		
		// size of image
		int sizeX = labelImage.getWidth();
		int sizeY = labelImage.getHeight();

		// normalization weight
		double w0 = weights[0];
		
		for (int y = 0; y < sizeY; y++)
		{
			for (int x = 0; x < sizeX; x++)
			{
				if ((int) labelImage.getf(x, y) > 0)
				{
					distMap.set(x, y, (int) Math.round( ((double) distMap.get(x, y)) / w0));
				}
			}
		}
	}
}
