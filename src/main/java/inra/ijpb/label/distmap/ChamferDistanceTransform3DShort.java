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

import ij.ImageStack;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.distmap.ChamferMask3D;
import inra.ijpb.binary.distmap.ChamferMask3D.ShortOffset;
import inra.ijpb.data.image.Image3D;
import inra.ijpb.data.image.Images3D;

/**
 * Computes 3D distance transform on binary images using the chamfer weights
 * provided by a ChamferWeights3D object, and using 16-bits integer computation.
 * 
 * This version works also for label images. The implementation is a little bit
 * different compared to the version for binary images, in particular, it makes
 * use of the "Image3D" interface.
 * 
 * @see inra.ijpb.binary.distmap.ChamferDistanceTransform3DShort
 * 
 * @author David Legland
 * 
 */
public class ChamferDistanceTransform3DShort extends AlgoStub implements DistanceTransform3D
{
	// ==================================================
	// Class variables

	/**
	 * The chamfer weights used to propagate distances to neighbor voxels.
	 */
	ChamferMask3D chamferWeights;
	
	/**
	 * Flag for dividing final distance map by the value first weight. 
	 * This results in distance map values closer to Euclidean, but with 
	 * non integer values. 
	 */
	boolean normalize = true;
	
	
	// ==================================================
	// Constructors 
	
	/**
	 * Creates a new algorithm for computing 3D distance maps on label images
	 * based on a chamfer mask. Use normalization of resulting distance map.
	 * 
	 * @param mask
	 *            the chamfer mask used for propagating distances
	 */
	public ChamferDistanceTransform3DShort(ChamferMask3D mask)
	{
		this.chamferWeights = mask;
	}
	
	/**
	 * Creates a new algorithm for computing 3D distance maps on label images
	 * based on a chamfer mask.
	 * 
	 * @param mask
	 *            the chamfer mask to use for propagating distances
	 * @param normalize
	 *            whether distance map should be normalized by the weight
	 *            associated to orthogonal shifts
	 */
	public ChamferDistanceTransform3DShort(ChamferMask3D mask, boolean normalize)
	{
		this.chamferWeights = mask;
		this.normalize = normalize;
	}
	

	// ==================================================
	// Implementation of DistanceTransform3D interface 
	
	/**
	 * Computes the distance map from a 3D binary image. 
	 * Distance is computed for each foreground (white) pixel, as the 
	 * chamfer distance to the nearest background (black) pixel.
	 * 
	 * @param image a 3D binary image with white pixels (255) as foreground
	 * @return a new 3D image containing: <ul>
	 * <li> 0 for each background pixel </li>
	 * <li> the distance to the nearest background pixel otherwise</li>
	 * </ul>
	 */
	@Override
	public ImageStack distanceMap(ImageStack image)
	{
		// size of image
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		// store wrapper to mask image
		Image3D labels = Images3D.createWrapper(image);

		// create new empty image, and fill it with black
		ImageStack resultStack = ImageStack.create(sizeX, sizeY, sizeZ, 16);
		Image3D distMap = Images3D.createWrapper(resultStack);
		
		initializeResultSlices(labels, distMap);
		
		// Two iterations are enough to compute distance map to boundary
		forwardScan(labels, distMap);
		backwardScan(labels, distMap);

		// Normalize values by the first weight
		if (this.normalize) 
		{
			normalizeResultSlices(labels, distMap); 
		}
				
		fireStatusChanged(this, "");
		return resultStack;
	}
	
	// ==================================================
	// Inner computation methods 
	
	/**
	 * Fill result image with zero for background voxels, and Short.MAX for
	 * foreground voxels.
	 */
	private void initializeResultSlices(Image3D labels, Image3D distMap)
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
					distMap.set(x, y, z, label == 0 ? 0 : Short.MAX_VALUE);
				}
			}
		}
		fireProgressChanged(this, 1, 1); 
	}

	private void forwardScan(Image3D labels, Image3D distMap) 
	{
		fireStatusChanged(this, "Forward scan..."); 
		
		// retrieve image dimensions
		int sizeX = labels.getSize(0);
		int sizeY = labels.getSize(1);
		int sizeZ = labels.getSize(2);
		
		// create array of forward shifts
		Collection<ShortOffset> offsets = this.chamferWeights.getForwardOffsets();

		// iterate on image voxels
		for (int z = 0; z < sizeZ; z++)
		{
			fireProgressChanged(this, z, sizeZ); 
			
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					// get current label
					int label = (int) labels.getValue(x, y, z);
					
					// do not process background pixels
					if (label == 0)
						continue;
					
					// current distance value
					int currentDist = distMap.get(x, y, z);
					int newDist = currentDist;

					// iterate over forward offsets defined by ChamferWeights
					for (ShortOffset offset : offsets)
					{
						int x2 = x + offset.dx;
						int y2 = y + offset.dy;
						int z2 = z + offset.dz;
						
						// check bounds
						if (x2 < 0 || x2 >= sizeX)
							continue;
						if (y2 < 0 || y2 >= sizeY)
							continue;
						if (z2 < 0 || z2 >= sizeZ)
							continue;
						
						if (((int) labels.getValue(x2, y2, z2)) != label)
						{
							// Update with distance to nearest different label
							newDist = offset.weight;
						}
						else
						{
							// Increment distance
							newDist = Math.min(newDist, distMap.get(x2, y2, z2) + offset.weight);
						}
					}
					
					if (newDist < currentDist) 
					{
						distMap.set(x, y, z, newDist);
					}
				}
			}
		}
		fireProgressChanged(this, 1, 1); 
	}
	
	private void backwardScan(Image3D labels, Image3D distMap) 
	{
		fireStatusChanged(this, "Backward scan..."); 
		
		// retrieve image dimensions
		int sizeX = labels.getSize(0);
		int sizeY = labels.getSize(1);
		int sizeZ = labels.getSize(2);
		
		// create array of backward shifts
		Collection<ShortOffset> offsets = this.chamferWeights.getBackwardOffsets();
		
		// iterate on image voxels in backward order
		for (int z = sizeZ - 1; z >= 0; z--)
		{
			fireProgressChanged(this, sizeZ-1-z, sizeZ);
			
			for (int y = sizeY - 1; y >= 0; y--)
			{
				for (int x = sizeX - 1; x >= 0; x--)
				{
					// get current label
					int label = (int) labels.getValue(x, y, z);
					
					// do not process background pixels
					if (label == 0)
						continue;
					
					// current distance value
					int currentDist = distMap.get(x, y, z);
					int newDist = currentDist;
					
					// iterate over backward offsets defined by ChamferWeights
					for (ShortOffset offset : offsets)
					{
						int x2 = x + offset.dx;
						int y2 = y + offset.dy;
						int z2 = z + offset.dz;
						
						// check bounds
						if (x2 < 0 || x2 >= sizeX)
							continue;
						if (y2 < 0 || y2 >= sizeY)
							continue;
						if (z2 < 0 || z2 >= sizeZ)
							continue;
						
						if (((int) labels.getValue(x2, y2, z2)) != label)
						{
							// Update with distance to nearest different label
							newDist = offset.weight;
						}
						else
						{
							// Increment distance
							newDist = Math.min(newDist, distMap.get(x2, y2, z2) + offset.weight);
						}
					}
					
					if (newDist < currentDist) 
					{
						distMap.set(x, y, z, newDist);
					}
				}
			}
		}
		fireProgressChanged(this, 1, 1); 
	}
	
	private void normalizeResultSlices(Image3D labels, Image3D distMap)
	{
		fireStatusChanged(this, "Normalize map..."); 
		
		// retrieve the minimum weight
		double w0 = Double.POSITIVE_INFINITY;
		for (ShortOffset offset : this.chamferWeights.getOffsets())
		{
			w0 = Math.min(w0, offset.weight);
		}
		
		// retrieve image dimensions
		int sizeX = labels.getSize(0);
		int sizeY = labels.getSize(1);
		int sizeZ = labels.getSize(2);

		for (int z = 0; z < sizeZ; z++) 
		{
			fireProgressChanged(this, z, sizeZ);
			
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					if (((int) labels.get(x, y, z)) != 0)
					{
						distMap.set(x, y, z, (int) Math.round(distMap.getValue(x, y, z) / w0));
					}
				}
			}
		}
		
		fireProgressChanged(this, 1, 1); 
	}

}
