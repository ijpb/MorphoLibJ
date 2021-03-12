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

import ij.ImageStack;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.ChamferWeights3D;
import inra.ijpb.binary.ChamferWeights3D.FloatOffset;
import inra.ijpb.data.image.Image3D;
import inra.ijpb.data.image.Images3D;

/**
 * Computation of geodesic distance transform for 3D images, using floating point computation.
 * 
 * @author dlegland
 *
 */
public class GeodesicDistanceTransform3DFloat extends AlgoStub implements GeodesicDistanceTransform3D
{
	private final static int DEFAULT_MASK_LABEL = 255;

	// ==================================================
	// Class variables
	
	float[] weights;
	
	/**
	 * Flag for dividing final distance map by the value first weight. 
	 * This results in distance map values closer to euclidean, but with non integer values. 
	 */
	boolean normalizeMap = true;
	
	/** 
	 * The value assigned to result pixels that do not belong to the mask. 
	 * Default is Float.MAX_VALUE.
	 */
	float backgroundValue = Float.POSITIVE_INFINITY;
	
	int maskLabel = DEFAULT_MASK_LABEL;

	ImageStack maskProc;
	Image3D result;
	
	int sizeX;
	int sizeY;
	int sizeZ;

	boolean modif;

	
	// ==================================================
	// Constructors
	
	public GeodesicDistanceTransform3DFloat(float[] weights)
	{
		this.weights = weights;
	}

	public GeodesicDistanceTransform3DFloat(float[] weights, boolean normalizeMap)
	{
		this.weights = weights;
		this.normalizeMap = normalizeMap;
	}

	public GeodesicDistanceTransform3DFloat(ChamferWeights3D weights, boolean normalizeMap)
	{
		this.weights = weights.getFloatWeights();
		this.normalizeMap = normalizeMap;
	}


	// ==================================================
	// Methods
	
	/* (non-Javadoc)
	 * @see inra.ijpb.binary.geodesic.GeodesicDistanceTransform3D#geodesicDistanceMap(ij.ImageStack, ij.ImageStack)
	 */
	@Override
	public ImageStack geodesicDistanceMap(ImageStack marker, ImageStack mask)
	{
		this.maskProc = mask;
		
		this.sizeX = mask.getWidth();
		this.sizeY = mask.getHeight();
		this.sizeZ = mask.getSize();
		
		fireStatusChanged(this, "Initialization..."); 
		
		// create new empty image, and fill it with black
		ImageStack resultStack = ImageStack.create(sizeX, sizeY, sizeZ, 32);
		this.result = Images3D.createWrapper(resultStack);
		
		// initialize empty image with either 0 (foreground) or Inf (background)
		for (int z = 0; z < sizeZ; z++)
		{
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					if (marker.getVoxel(x, y, z) == 0)
					{
						result.setValue(x, y, z, backgroundValue);
					}
				}
			}
		}
		
		// Iterate forward and backward passes until no more modification occur
		int iter = 0;
		do 
		{
			modif = false;

			// forward iteration
			fireStatusChanged(this, "Forward iteration " + iter);
			forwardIteration();

			// backward iteration
			fireStatusChanged(this, "Backward iteration " + iter); 
			backwardIteration();

			// Iterate while pixels have been modified
			iter++;
		} while (modif);

		// Normalize values by the first weight value
		if (this.normalizeMap) 
		{
			fireStatusChanged(this, "Normalize map"); 
			for (int z = 0; z < sizeZ; z++)
			{
				for (int y = 0; y < sizeY; y++)
				{
					for (int x = 0; x < sizeX; x++)
					{
						double val = result.getValue(x, y, z) / weights[0];
						result.setValue(x, y, z, val);
					}
				}
			}
		}
		
		return resultStack;
	}

	private void forwardIteration()
	{
		// compute offsets of the neighborhood in forward direction
		Collection<FloatOffset> offsets = ChamferWeights3D.getForwardOffsets(weights);
		
		// iterate over voxels
		for (int z = 0; z < sizeZ; z++)
		{
			fireProgressChanged(this, z, sizeZ);
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					// process only voxels within the mask
					if (maskProc.getVoxel(x, y, z) != maskLabel)
					{
						continue;
					}
					
					double value = result.getValue(x, y, z);
					double ref = value;
					
					// iterate over voxels in forward neighborhood to find minimum value
					for (FloatOffset offset : offsets)
					{
						int x2 = x + offset.dx;
						int y2 = y + offset.dy;
						int z2 = z + offset.dz;
						
						if (x2 < 0 || x2 >= sizeX)
							continue;
						if (y2 < 0 || y2 >= sizeY)
							continue;
						if (z2 < 0 || z2 >= sizeZ)
							continue;
						
						double newVal = result.getValue(x2, y2, z2) + offset.weight;
						value = Math.min(value, newVal);
					}
					
					if (value < ref)
					{
						modif = true;
						result.setValue(x, y, z, value);
					}
				}
			}
		}
		
		fireProgressChanged(this, 1, 1);
	}

	private void backwardIteration()
	{
		// compute offsets of the neighborhood in backward direction
		Collection<FloatOffset> offsets = ChamferWeights3D.getBackwardOffsets(weights);
		
		// iterate over voxels
		for (int z = sizeZ-1; z >= 0; z--)
		{
			fireProgressChanged(this, sizeZ-1-z, sizeZ);
			for (int y = sizeY - 1; y >= 0; y--)
			{
				for (int x = sizeX - 1; x >= 0; x--)
				{
					// process only voxels within the mask
					if (maskProc.getVoxel(x, y, z) != maskLabel)
					{
						continue;
					}
					
					double value = result.getValue(x, y, z);
					double ref = value;
					
					// iterate over voxels in backward neighborhood to find minimum value
					for (FloatOffset offset : offsets)
					{
						int x2 = x + offset.dx;
						int y2 = y + offset.dy;
						int z2 = z + offset.dz;
						
						if (x2 < 0 || x2 >= sizeX)
							continue;
						if (y2 < 0 || y2 >= sizeY)
							continue;
						if (z2 < 0 || z2 >= sizeZ)
							continue;
						
						double newVal = result.getValue(x2, y2, z2) + offset.weight;
						value = Math.min(value, newVal);
					}
					
					if (value < ref)
					{
						modif = true;
						result.setValue(x, y, z, value);
					}
				}
			}
		}	
		
		fireProgressChanged(this, 1, 1);
	}
}
