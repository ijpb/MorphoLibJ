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
/**
 * 
 */
package inra.ijpb.label.distmap;

import java.util.Collection;

import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import inra.ijpb.algo.AlgoEvent;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.distmap.ChamferMask2D;
import inra.ijpb.binary.distmap.ChamferMask2D.ShortOffset;
import inra.ijpb.label.LabelValues;

/**
 * Computes distance transform by propagating distances using a ChamferMask2D,
 * and using 16-bits integer computation.
 * 
 * For binary images, an equivalent algorithm exists within the
 * inra.ijpb.binary.distmap package.
 * 
 * <p>
 * Example of use:
 * 
 * <pre>
 * <code>
 * ChamferMask2D mask = ChamferMask2D.BORGEFORS();
 * boolean normalize = true;
 * DistanceTransform dt = new ChamferDistanceTransform2DShort(mask, normalize);
 * ImageProcessor result = dt.distanceMap(inputImage);
 * </code>
 * </pre>
 * 
 * @see ChamferDistanceTransform2DFloat
 * @author David Legland
 * 
 */
public class ChamferDistanceTransform2DShort extends AlgoStub implements ChamferDistanceTransform2D
{
	// ==================================================
	// Class variables

	/**
	 * The chamfer mask used to propagate distances to neighbor pixels.
	 */
	ChamferMask2D mask;
	
	/**
	 * Flag for dividing final distance map by the value first weight. 
	 * This results in distance map values closer to Euclidean, but with 
	 * non integer values. 
	 */
	boolean normalize = true;
	
	
	// ==================================================
	// Constructors 
	
	/**
	 * Creates a new algorithm for computing distance maps based on a chamfer
	 * mask.
	 * 
	 * @param mask
	 *            the chamfer mask to use for propagating distances
	 */
	public ChamferDistanceTransform2DShort(ChamferMask2D mask)
	{
		this.mask = mask;
	}
	
	/**
	 * Creates a new algorithm for computing distance maps based on a chamfer
	 * mask.
	 * 
	 * @param mask
	 *            the chamfer mask to use for propagating distances
	 * @param normalize
	 *            whether distance map should be normalized by the weight
	 *            associated to orthogonal shifts
	 */
	public ChamferDistanceTransform2DShort(ChamferMask2D mask, boolean normalize)
	{
		this.mask = mask;
		this.normalize = normalize;
	}
	

	// ==================================================
	// Implementation of the ChamferDistanceTransform2D interface
	
	@Override
	public ChamferMask2D mask()
	{
		return this.mask;
	}

	
	// ==================================================
	// Implementation of the DistanceTransform interface 
	
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
	@Override
	public ShortProcessor distanceMap(ImageProcessor labelImage) 
	{
		ShortProcessor distMap = initializeResult(labelImage);
		
		// Two iterations are enough to compute distance map to boundary
		forwardScan(distMap, labelImage);
		backwardScan(distMap, labelImage);

		// Normalize values by the first weight
		if (this.normalize)
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
	
	/**
	 * Fill result image with zero for background voxels, and Short.MAX for
	 * foreground voxels.
	 */
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

		// size of image
		int sizeX = labelImage.getWidth();
		int sizeY = labelImage.getHeight();
		Collection<ShortOffset> offsets = mask.getForwardOffsets();
		
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
					
					if ((int) labelImage.getf(x2, y2) != label)
					{
						// Update with distance to nearest different label
					    newDist = Math.min(newDist, offset.weight);
					}
					else
					{
						// Increment distance
						newDist = Math.min(newDist, distMap.get(x2, y2) + offset.weight);
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

		// size of image
		int sizeX = labelImage.getWidth();
		int sizeY = labelImage.getHeight();
		Collection<ShortOffset> offsets = mask.getBackwardOffsets();
		
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
					
					if ((int) labelImage.getf(x2, y2) != label)
					{
						// Update with distance to nearest different label
					    newDist = Math.min(newDist, offset.weight);
					}
					else
					{
						// Increment distance
						newDist = Math.min(newDist, distMap.get(x2, y2) + offset.weight);
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

		// retrieve the minimum weight
		double w0 = mask.getShortNormalizationWeight();
		
		// normalize each pixel
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
