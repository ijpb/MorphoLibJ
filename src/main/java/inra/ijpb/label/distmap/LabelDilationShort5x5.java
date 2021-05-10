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

import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.ChamferWeights;

/**
 * Apply a dilation by a specified radius to each label of a label map by
 * constraining the dilation. Labels can not dilate over existing labels.
 * 
 * Can be applied to label map encoded with 8 or 16 bits integers, or 32 bit
 * floats.
 * 
 * @see inra.ijpb.binary.ChamferWeights3D
 * @see inra.ijpb.label.distmap.LabelDilation3D4WShort
 * 
 * @author dlegland
 * 
 */
public class LabelDilationShort5x5 extends AlgoStub 
{
	// ==================================================
	// Class variables
	
	short[] weights = new short[]{5, 7, 11};

	
	// ==================================================
	// Constructors 
	
	public LabelDilationShort5x5(ChamferWeights weights) 
	{
		this(weights.getShortWeights());
	}

	public LabelDilationShort5x5(short[] weights) 
	{
		this.weights = weights;
		
		// ensure the number of weight is at least 3.
		if (weights.length < 3) 
		{
			short[] newWeights = new short[3];
			newWeights[0] = weights[0];
			newWeights[1] = weights[1];
			newWeights[2] = (short) (weights[0] + weights[1]);
			this.weights = newWeights;
		}
	}

	// ==================================================
	// Methods 
	
	/**
	 * Computes dilation of labels within label image by a specified radius.
	 * Labels can not dilate over existing labels.
	 * 
	 * The function returns a new ImageProcessor the same size and the same type
	 * as the input, with values greater than or equal to zero.
	 *
	 * @param labelImage
	 *            the original label map
	 * @param distMax
	 *            the dilation radius, in pixels. In practice, dilation is
	 *            computed with a radius +0.5.
	 * @return a new label image where each label is dilated over background
	 *         pixels.
	 */
	public ImageProcessor process(ImageProcessor labelImage, double distMax)
	{
		// use max distance relative to chamfer weights
		double maxDist = distMax * this.weights[0];
		
		fireStatusChanged(this, "Initialization..."); 
		// the instance of ImageProcessor storing the result label map
		ImageProcessor res = labelImage.duplicate();
		// the distance map to the closest label
		ImageProcessor distMap = initialize(labelImage);

		// forward iteration
		fireStatusChanged(this, "Forward iteration");
		forwardIteration(res, distMap, maxDist);

		// backward iteration
		fireStatusChanged(this, "Backward iteration"); 
		backwardIteration(res, distMap, maxDist);
		
		return res;
	}

	private ShortProcessor initialize(ImageProcessor marker)
	{
		// size of image
		int sizeX = marker.getWidth();
		int sizeY = marker.getHeight();
		
		ShortProcessor distMap = new ShortProcessor(sizeX, sizeY);
		distMap.setValue(0);
		distMap.fill();

		// initialize empty image with either 0 (foreground) or Inf (background)
		for (int y = 0; y < sizeY; y++) 
		{
			for (int x = 0; x < sizeX; x++) 
			{
				int val = (int) marker.getf(x, y);
				distMap.set(x, y, val == 0 ? Short.MAX_VALUE : 0);
			}
		}

		return distMap;
	}
	
	private void forwardIteration(ImageProcessor res, ImageProcessor distMap, double distMax) 
	{
		// size of image
		int sizeX = res.getWidth();
		int sizeY = res.getHeight();

		// Initialize pairs of offset and weights
		int[] dx = new int[]{-1, +1,  -2, -1,  0, +1, +2,  -1};
		int[] dy = new int[]{-2, -2,  -1, -1, -1, -1, -1,   0};
		
		short[] dw = new short[] { 
				weights[2], weights[2], 
				weights[2], weights[1], weights[0], weights[1], weights[2], 
				weights[0] };
		
		// Iterate over pixels
		for (int y = 0; y < sizeY; y++)
		{
			this.fireProgressChanged(this, y, sizeY);
			for (int x = 0; x < sizeX; x++)
			{
				// current distance value
				int currentDist = distMap.get(x, y);

				// do not process within labels of original image
				if (currentDist == 0)
					continue;
				
				// init data for current pixel
				int minDist = currentDist;
				int closestLabel = 0;
				
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
					
					// distance from neighbor
					int dist = distMap.get(x2, y2) + dw[i];
					
					if (dist < minDist)
					{
						minDist = dist;
						closestLabel = (int) res.getf(x2, y2);
					}
				}
				
				// update current pixel if necessary
				if (minDist < currentDist && minDist < distMax)
				{
					distMap.set(x, y, minDist);
					res.setf(x, y, closestLabel);
				}
			}
		}
		
		this.fireProgressChanged(this, sizeY, sizeY);
	}

	private void backwardIteration(ImageProcessor res, ImageProcessor distMap, double distMax)
	{
		// size of image
		int sizeX = res.getWidth();
		int sizeY = res.getHeight();

		// Initialize pairs of offset and weights
		int[] dx = new int[]{+1, -1,  +2, +1,  0, -1, -2,  +1};
		int[] dy = new int[]{+2, +2,  +1, +1, +1, +1, +1,   0};
		
		short[] dw = new short[] { 
				weights[2], weights[2], 
				weights[2], weights[1], weights[0], weights[1], weights[2], 
				weights[0] };
		
		// Iterate over pixels
		for (int y = sizeY-1; y >= 0; y--)
		{
			this.fireProgressChanged(this, sizeY-1-y, sizeY);
			for (int x = sizeX-1; x >= 0; x--)
			{
				// current distance value
				int currentDist = distMap.get(x, y);

				// do not process within labels of original image
				if (currentDist == 0)
					continue;
				
				// init data for current pixel
				int minDist = currentDist;
				int closestLabel = 0;
				
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
					
					// distance from neighbor
					int dist = distMap.get(x2, y2) + dw[i];
					
					if (dist < minDist)
					{
						minDist = dist;
						closestLabel = (int) res.getf(x2, y2);
					}
				}
				
				// update current pixel if necessary
				if (minDist < currentDist && minDist < distMax)
				{
					distMap.set(x, y, minDist);
					res.setf(x, y, closestLabel);
				}
			}
		}
		
		this.fireProgressChanged(this, sizeY, sizeY);
	}
}
