/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2023 INRA.
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
package inra.ijpb.label.filter;

import java.util.Collection;

import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.distmap.ChamferMask2D;
import inra.ijpb.binary.distmap.ChamferMask2D.ShortOffset;

/**
 * Apply a dilation by a specified radius to each label of a label map by
 * constraining the dilation. Labels can not dilate over existing labels.
 * 
 * Can be applied to label map encoded with 8 or 16 bits integers, or 32 bit
 * floats.
 * 
 * @see inra.ijpb.binary.distmap.ChamferMask2D
 * @see inra.ijpb.label.filter.ChamferLabelDilation3DShort
 * 
 * @author dlegland
 * 
 */
public class ChamferLabelDilation2DShort extends AlgoStub 
{
	// ==================================================
	// Class variables
	
    /**
     * The chamfer mask used to propagate distances.
     */
	ChamferMask2D mask;
    
    /**
     * The radius of dilation of labels. In practice, the distance is propagated
     * up to radius + 0.5.
     */
	double radius;

	
	// ==================================================
	// Constructors 
	
	/**
     * Creates a new image processor for dilating labels using the specified
     * chamfer mask, using 16-bits integer computation.
     * 
     * @param mask
     *            the Chamfer mask to use.
     * @param radius
     *            the radius of dilation of labels. In practice, the distance is
     *            propagated up to radius + 0.5.
     */
	public ChamferLabelDilation2DShort(ChamferMask2D mask, double radius)
	{
		this.mask = mask;
		this.radius = radius;
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
	 * @return a new label image where each label is dilated over background
	 *         pixels.
	 */
	public ImageProcessor process(ImageProcessor labelImage)
	{
		// use max distance relative to chamfer weights
		double w0 = mask.getNormalizationWeight();
		double maxDist = (radius + 0.5) * w0;
		
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
		Collection<ShortOffset> offsets = mask.getForwardOffsets();

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
				for (ShortOffset offset : offsets)
				{
					// compute neighbor coordinates
					int x2 = x + offset.dx;
					int y2 = y + offset.dy;
					
					// check bounds
					if (x2 < 0 || x2 >= sizeX)
						continue;
					if (y2 < 0 || y2 >= sizeY)
						continue;
					
					// distance from neighbor
					int dist = distMap.get(x2, y2) + offset.weight;
					
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
		Collection<ShortOffset> offsets = mask.getBackwardOffsets();

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
				for (ShortOffset offset : offsets)
				{
					// compute neighbor coordinates
					int x2 = x + offset.dx;
					int y2 = y + offset.dy;
					
					// check bounds
					if (x2 < 0 || x2 >= sizeX)
						continue;
					if (y2 < 0 || y2 >= sizeY)
						continue;
					
					// distance from neighbor
					int dist = distMap.get(x2, y2) + offset.weight;
					
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
