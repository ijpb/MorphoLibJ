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
package inra.ijpb.binary.geodesic;

import java.util.Collection;

import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.distmap.ChamferMask2D;
import inra.ijpb.binary.distmap.ChamferMask2D.FloatOffset;

/**
 * Computation of Chamfer geodesic distances using floating point array for
 * storing result, and 3-by-3 chamfer masks.
 * 
 * @author David Legland
 * 
 */
public class GeodesicDistanceTransformFloat extends AlgoStub implements
		GeodesicDistanceTransform
{
	// ==================================================
	// Class variables
	
	public static final float MAX_DIST = Float.POSITIVE_INFINITY;
	public static final float BACKGROUND = Float.NaN;
	
	/**
	 * The chamfer mask used for propagating distances from the marker.
	 */
	ChamferMask2D mask;

	/**
	 * Flag for dividing final distance map by the value first weight. 
	 * This results in distance map values closer to Euclidean distance. 
	 */
	boolean normalizeMap = true;

	
	// ==================================================
	// Constructors 
	
	public GeodesicDistanceTransformFloat(ChamferMask2D mask)
	{
		this.mask = mask;
	}

	@Deprecated
	public GeodesicDistanceTransformFloat(float[] weights)
	{
		this.mask = ChamferMask2D.fromWeights(weights);
	}

	public GeodesicDistanceTransformFloat(ChamferMask2D mask, boolean normalizeMap)
	{
		this.mask = mask;
		this.normalizeMap = normalizeMap;
	}

	@Deprecated
	public GeodesicDistanceTransformFloat(float[] weights, boolean normalizeMap)
	{
		this.mask = ChamferMask2D.fromWeights(weights);
		this.normalizeMap = normalizeMap;
	}

	// ==================================================
	// Methods 
	
	/**
	 * Computes the geodesic distance function for each pixel in mask label
	 * image, using the given binary marker image. Mask and marker should be
	 * ImageProcessor the same size and containing integer values.
	 * 
	 * The function returns a new FloatProcessor the same size as the input,
	 * with values greater or equal to zero.
	 *
	 * @param marker
	 *            the binary marker image
	 * @param labelImage
	 *            the label image used as mask
	 * @return the geodesic distance map from the marker image within each label
	 *         of the mask
	 * @see inra.ijpb.binary.geodesic.GeodesicDistanceTransform#geodesicDistanceMap(ij.process.ImageProcessor,
	 *      ij.process.ImageProcessor)
	 */
	@Override
	public ImageProcessor geodesicDistanceMap(ImageProcessor marker, ImageProcessor labelImage)
	{
		// size of image
		int sizeX = labelImage.getWidth();
		int sizeY = labelImage.getHeight();
		
		// create new empty image, and fill it with black
		fireStatusChanged(this, "Initialization..."); 
		FloatProcessor distMap = initialize(marker, labelImage);

		int iter = 0;
		boolean modif = true;
		while(modif)
		{
			// forward iteration
			fireStatusChanged(this, "Forward iteration " + iter);
			modif = forwardIteration(distMap, labelImage);

			// backward iteration
			fireStatusChanged(this, "Backward iteration " + iter); 
			modif = modif || backwardIteration(distMap, labelImage);

			// Iterate while pixels have been modified
			iter++;
		};

		// Normalize values by the first weight
		if (this.normalizeMap) 
		{
			fireStatusChanged(this, "Normalize map"); 
			normalizeMap(distMap, labelImage);
		}
		
		// Compute max value within the mask
		fireStatusChanged(this, "Normalize display"); 
		float maxVal = 0;
		for (int x = 0; x < sizeX; x++)
		{
			for (int y = 0; y < sizeY; y++)
			{
				int label = (int) labelImage.getf(x, y);
				if (label > 0)
				{
					float val = distMap.getf(x, y);
					if (val != MAX_DIST)
					{
						maxVal = Math.max(maxVal, val);
					}
				}
			}
		}
		// System.out.println("max value: " + Float.toString(maxVal));

		// update and return resulting Image processor
		distMap.setMinAndMax(0, maxVal);
		// Forces the display to non-inverted LUT
		if (distMap.isInvertedLut())
		{
			distMap.invertLut();
		}
		
		return distMap;
	}

	private FloatProcessor initialize(ImageProcessor marker, ImageProcessor labelImage)
	{
		// size of image
		int sizeX = labelImage.getWidth();
		int sizeY = labelImage.getHeight();
		
		FloatProcessor distMap = new FloatProcessor(sizeX, sizeY);
		distMap.setValue(0);
		distMap.fill();

		// initialize empty image with either 0 (foreground) or NaN (background)
		for (int y = 0; y < sizeY; y++) 
		{
			for (int x = 0; x < sizeX; x++) 
			{
				int label = (int) labelImage.getf(x, y);
				if (label == 0)
				{
					distMap.setf(x, y, BACKGROUND);
				}
				else
				{
					distMap.setf(x, y, marker.get(x, y) == 0 ? MAX_DIST : 0);
				}
			}
		}

		return distMap;
	}
	
	private boolean forwardIteration(FloatProcessor distMap, ImageProcessor labelImage) 
	{
		// size of image
		int sizeX = labelImage.getWidth();
		int sizeY = labelImage.getHeight();
		Collection<FloatOffset> offsets = mask.getForwardFloatOffsets();
		
		// Iterate over pixels
		boolean modif = false;
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
				double currentDist = distMap.getf(x, y);
				double newDist = currentDist;
				
				// iterate over neighbors
				for (FloatOffset offset : offsets)
				{
					// compute neighbor coordinates
					int x2 = x + offset.dx;
					int y2 = y + offset.dy;
					
					// check bounds
					if (x2 < 0 || x2 >= sizeX)
						continue;
					if (y2 < 0 || y2 >= sizeY)
						continue;
					
					if (((int) labelImage.getf(x2, y2)) == label)
					{
						// Increment distance
						newDist = Math.min(newDist, distMap.getf(x2, y2) + offset.weight);
					}
				}
				
				if (newDist < currentDist) 
				{
					distMap.setf(x, y, (float) newDist);
					modif = true;
				}
			}
		}
		
		this.fireProgressChanged(this, sizeY, sizeY);
		return modif;
	}

	private boolean backwardIteration(FloatProcessor distMap, ImageProcessor labelImage)
	{
		// size of image
		int sizeX = labelImage.getWidth();
		int sizeY = labelImage.getHeight();
		Collection<FloatOffset> offsets =  mask.getBackwardFloatOffsets();
		
		// Iterate over pixels
		boolean modif = false;
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
				double currentDist = distMap.getf(x, y);
				double newDist = currentDist;
				
				// iterate over neighbors
				for (FloatOffset offset : offsets)
				{
					// compute neighbor coordinates
					int x2 = x + offset.dx;
					int y2 = y + offset.dy;
					
					// check bounds
					if (x2 < 0 || x2 >= sizeX)
						continue;
					if (y2 < 0 || y2 >= sizeY)
						continue;
					
					if (((int) labelImage.getf(x2, y2)) == label)
					{
						// Increment distance
						newDist = Math.min(newDist, distMap.getf(x2, y2) + offset.weight);
					}
				}
				
				if (newDist < currentDist) 
				{
					distMap.setf(x, y, (float) newDist);
					modif = true;
				}
			}
		}
		
		this.fireProgressChanged(this, sizeY, sizeY);
		return modif;
	}
	
	private void normalizeMap(FloatProcessor distMap, ImageProcessor labelImage)
	{
		// size of image
		int sizeX = distMap.getWidth();
		int sizeY = distMap.getHeight();

		// retrieve the minimum weight
		double w0 = Double.POSITIVE_INFINITY;
		for (FloatOffset offset : this.mask.getFloatOffsets())
		{
			w0 = Math.min(w0, offset.weight);
		}
		
		for (int y = 0; y < sizeY; y++)
		{
			for (int x = 0; x < sizeX; x++)
			{
				if (((int) labelImage.getf(x, y)) == 0)
				{
					continue;
				}

				float val = distMap.getf(x, y);
				if (val != MAX_DIST)
				{
					distMap.setf(x, y, (float) (val / w0));
				}
			}
		}
	}
}
