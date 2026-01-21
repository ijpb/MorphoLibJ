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
package inra.ijpb.binary.geodesic;

import java.util.Collection;

import ij.ImageStack;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.distmap.ChamferMask3D;
import inra.ijpb.binary.distmap.ChamferMask3D.FloatOffset;
import inra.ijpb.data.image.Image3D;
import inra.ijpb.data.image.Images3D;

/**
 * Computation of geodesic distance transform for 3D images, using floating
 * point computation.
 * 
 * This implementation also works on label maps as input.
 * 
 * 
 * @author dlegland
 *
 */
public class GeodesicDistanceTransform3DFloat extends AlgoStub implements GeodesicDistanceTransform3D
{
	// ==================================================
	// Class variables
	
	/**
	 * The chamfer mask used for propagating distances from the marker.
	 */
	ChamferMask3D chamferMask;
	
	/**
	 * Flag for dividing final distance map by the value first weight. 
	 * This results in distance map values closer to euclidean, but with non integer values. 
	 */
	boolean normalizeMap = true;
	
	/** 
	 * The value assigned to the background in the result image.
	 * Default is Float.NaN.
	 */
	static final float BACKGROUND = Float.NaN;
	
	
	// ==================================================
	// Constructors
	
	/**
	 * Creates a new algorithm for propagating geodesic distances.
	 * 
	 * @param mask
	 *            the chamfer mask to use for propagating distances
	 * @param normalizeMap
	 *            the flag for normalization
	 */
	public GeodesicDistanceTransform3DFloat(ChamferMask3D mask, boolean normalizeMap)
	{
		this.chamferMask = mask;
		this.normalizeMap = normalizeMap;
	}

	/**
	 * Creates a new algorithm.
	 * 
	 * @param weights
	 *            the weights to use for propagating distances
	 */
	@Deprecated
	public GeodesicDistanceTransform3DFloat(float[] weights)
	{
		this.chamferMask = ChamferMask3D.fromWeights(weights);
	}

	/**
	 * Creates a new algorithm.
	 * 
	 * @param weights
	 *            the weights to use for propagating distances
	 * @param normalizeMap
	 *            the flag for normalization
	 */
	@Deprecated
	public GeodesicDistanceTransform3DFloat(float[] weights, boolean normalizeMap)
	{
		this.chamferMask = ChamferMask3D.fromWeights(weights);
		this.normalizeMap = normalizeMap;
	}

	/**
	 * Creates a new algorithm for propagating geodesic distances.
	 * 
	 * @param weights
	 *            the chamfer mask to use for propagating distances
	 * @param normalizeMap
	 *            the flag for normalization
	 */
	@Deprecated
	public GeodesicDistanceTransform3DFloat(inra.ijpb.binary.ChamferWeights3D weights, boolean normalizeMap)
	{
		this.chamferMask = ChamferMask3D.fromWeights(weights.getFloatWeights());
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
		int sizeX = marker.getWidth();
		int sizeY = marker.getHeight();
		int sizeZ = marker.getSize();
		
		fireStatusChanged(this, "Initialization..."); 
		
		Image3D labelImage = Images3D.createWrapper(mask);
		
		// create new empty image, and fill it with black
		ImageStack resultStack = ImageStack.create(sizeX, sizeY, sizeZ, 32);
		Image3D distMap = Images3D.createWrapper(resultStack);
		
		
		// initialize empty image with either 0 (within marker), Inf (within
		// label, but outside marker), or NaN (background).
		initialize(Images3D.createWrapper(marker), labelImage, distMap);
		
		// Iterate forward and backward passes until no more modification occur
		int iter = 0;
		boolean modif;
		do 
		{
			// forward iteration
			fireStatusChanged(this, "Forward iteration " + iter);
			modif = forwardIteration(distMap, labelImage);

			// backward iteration
			fireStatusChanged(this, "Backward iteration " + iter); 
			modif = modif || backwardIteration(distMap, labelImage);

			// Iterate while pixels have been modified
			iter++;
		} while (modif);

		// Normalize values by the first weight value
		if (this.normalizeMap) 
		{
			fireStatusChanged(this, "Normalize map"); 
			normalizeMap(distMap, labelImage);
		}
		
		return resultStack;
	}

	/**
	 * initialize empty image with either 0 (within marker), Inf (within label,
	 * but outside marker), or NaN (background).
	 */
	private void initialize(Image3D marker, Image3D labels, Image3D distMap)
	{
		fireStatusChanged(this, "Initialization...");
		
		// retrieve image dimensions
		int sizeX = labels.getSize(0);
		int sizeY = labels.getSize(1);
		int sizeZ = labels.getSize(2);

		// iterate over slices
		for (int z = 0; z < sizeZ; z++) 
		{
			fireProgressChanged(this, z, sizeZ);
			
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					int label = (int) labels.getValue(x, y, z);
					if (label == 0)
					{
						distMap.setValue(x, y, z, BACKGROUND);
					}
					else
					{
						distMap.setValue(x, y, z, marker.getValue(x, y, z) == 0 ? Float.MAX_VALUE : 0);
					}
				}
			}
		}
		fireProgressChanged(this, 1, 1); 
	}


	private boolean forwardIteration(Image3D distMap, Image3D labelImage)
	{
		// retrieve size of image
		int sizeX = distMap.getSize(0);
		int sizeY = distMap.getSize(1);
		int sizeZ = distMap.getSize(2);

		// compute offsets of the neighborhood in forward direction
		Collection<FloatOffset> offsets = chamferMask.getForwardFloatOffsets();
		
		// iterate over voxels
		boolean modif = false;
		for (int z = 0; z < sizeZ; z++)
		{
			fireProgressChanged(this, z, sizeZ);
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					// get current label
					int label = (int) labelImage.getValue(x, y, z);
					
					// do not process background pixels
					if (label == 0)
						continue;
					
					// current distance value
					double currentDist = distMap.getValue(x, y, z);
					double newDist = currentDist;
					
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
						
						if (((int) labelImage.getValue(x2, y2, z2)) == label)
						{
							// Increment distance
							newDist = Math.min(newDist, distMap.getValue(x2, y2, z2) + offset.weight);
						}
					}
					
					if (newDist < currentDist)
					{
						distMap.setValue(x, y, z, newDist);
						modif = true;
					}
				}
			}
		}
		
		fireProgressChanged(this, 1, 1);
		return modif;
	}

	private boolean backwardIteration(Image3D distMap, Image3D labelImage)
	{
		// retrieve size of image
		int sizeX = distMap.getSize(0);
		int sizeY = distMap.getSize(1);
		int sizeZ = distMap.getSize(2);

		// compute offsets of the neighborhood in backward direction
		Collection<FloatOffset> offsets = chamferMask.getBackwardFloatOffsets();
		
		// iterate over voxels
		boolean modif = false;
		for (int z = sizeZ-1; z >= 0; z--)
		{
			fireProgressChanged(this, sizeZ-1-z, sizeZ);
			for (int y = sizeY - 1; y >= 0; y--)
			{
				for (int x = sizeX - 1; x >= 0; x--)
				{
					// get current label
					int label = (int) labelImage.getValue(x, y, z);
					
					// do not process background pixels
					if (label == 0)
						continue;
					
					// current distance value
					double currentDist = distMap.getValue(x, y, z);
					double newDist = currentDist;
					
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
						
						if (((int) labelImage.getValue(x2, y2, z2)) == label)
						{
							// Increment distance
							newDist = Math.min(newDist, distMap.getValue(x2, y2, z2) + offset.weight);
						}
					}
					
					if (newDist < currentDist)
					{
						distMap.setValue(x, y, z, newDist);
						modif = true;
					}
				}
			}
		}	
		
		fireProgressChanged(this, 1, 1);
		return modif;
	}

	private void normalizeMap(Image3D distMap, Image3D labelImage)
	{
		// size of image
		int sizeX = distMap.getSize(0);
		int sizeY = distMap.getSize(1);
		int sizeZ = distMap.getSize(2);

		// retrieve the minimum weight
		double w0 = this.chamferMask.getNormalizationWeight();
		
		 // iterate over voxels within map to normalize values
		for (int z = 0; z < sizeZ; z++)
		{
			fireProgressChanged(this, z, sizeZ);
			
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					if (labelImage.get(x, y, z) > 0)
					{
						distMap.setValue(x, y, z, distMap.getValue(x, y, z) / w0);
					}
				}
			}
			fireProgressChanged(this, 1, 1);
		}
	}
}
