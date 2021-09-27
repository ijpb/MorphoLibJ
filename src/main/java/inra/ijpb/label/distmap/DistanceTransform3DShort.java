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

import java.util.ArrayList;

import ij.ImageStack;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.ChamferWeights3D;
import inra.ijpb.data.image.Image3D;
import inra.ijpb.data.image.Images3D;

/**
 * Computes Chamfer distances in a 3x3x3 neighborhood and storing result in
 * 16-bits image.
 * 
 * In practice, computations are done with integers, but result is stored in a
 * 3D short image, thus requiring less memory than floating point.
 * 
 * @author David Legland
 * 
 */
public class DistanceTransform3DShort extends AlgoStub implements DistanceTransform3D 
{
	// ==================================================
	// Class variables

	/**
	 * The chamfer weights used to propagate distances to neighbor voxels.
	 */
	private short[] weights;

	/**
	 * Flag for dividing final distance map by the value first weight. 
	 * This results in distance map values closer to euclidean, but with 
	 * non integer values. 
	 */
	private boolean normalizeMap = true;

	private int sizeX;
	private int sizeY;
	private int sizeZ;

	private Image3D labels;

	/**
	 * The result image that will store the distance map. The content
	 * of the buffer is updated during forward and backward iterations.
	 */
	private Image3D distmap;


	// ==================================================
	// Constructors 
	
	/**
	 * Default constructor that specifies the chamfer weights.
	 * 
	 * @param weights
	 *            an array of two weights for orthogonal and diagonal directions
	 */
	public DistanceTransform3DShort(ChamferWeights3D weights)
	{
		this(weights.getShortWeights());
	}

	/**
	 * Default constructor that specifies the chamfer weights.
	 * @param weights an array of two weights for orthogonal and diagonal directions
	 */
	public DistanceTransform3DShort(short[] weights)
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
	public DistanceTransform3DShort(ChamferWeights3D weights, boolean normalize)
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
	public DistanceTransform3DShort(short[] weights, boolean normalize)
	{
		this.weights = weights;
		this.normalizeMap = normalize;
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
	public ImageStack distanceMap(ImageStack image) 
	{
		// size of image
		sizeX = image.getWidth();
		sizeY = image.getHeight();
		sizeZ = image.getSize();
		
		// store wrapper to mask image
		this.labels = Images3D.createWrapper(image);

		// create new empty image, and fill it with black
		ImageStack resultStack = ImageStack.create(sizeX, sizeY, sizeZ, 16);
		this.distmap = Images3D.createWrapper(resultStack);

		// initialize empty image with either 0 (background) or max value (foreground)
		initializeResultSlices();
		
		// Two iterations are enough to compute distance map to boundary
		forwardScan();
		backwardScan();

		// Normalize values by the first weight
		if (this.normalizeMap) 
		{
			normalizeResultSlices();
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
	private void initializeResultSlices()
	{
		fireStatusChanged(this, "Initialization..."); 

		// iterate over slices
		for (int z = 0; z < sizeZ; z++) 
		{
			fireProgressChanged(this, z, sizeZ);
			
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					int val = labels.get(x, y, z);
					distmap.set(x, y, z, val == 0 ? 0 : Short.MAX_VALUE);
				}
			}
		}
		fireProgressChanged(this, 1, 1); 
	}
	
	private void forwardScan() 
	{
		fireStatusChanged(this, "Forward scan...");
		
		// create array of forward shifts
		ArrayList<WeightedOffset> offsets = new ArrayList<WeightedOffset>();
		
		// offsets in the z-1 plane
		offsets.add(new WeightedOffset(-1, -1, -1, weights[2]));
		offsets.add(new WeightedOffset( 0, -1, -1, weights[1]));
		offsets.add(new WeightedOffset(+1, -1, -1, weights[2]));
		offsets.add(new WeightedOffset(-1,  0, -1, weights[1]));
		offsets.add(new WeightedOffset( 0,  0, -1, weights[0]));
		offsets.add(new WeightedOffset(+1,  0, -1, weights[1]));
		offsets.add(new WeightedOffset(-1, +1, -1, weights[2]));
		offsets.add(new WeightedOffset( 0, +1, -1, weights[1]));
		offsets.add(new WeightedOffset(+1, +1, -1, weights[2]));
			
		// offsets in the current plane
		offsets.add(new WeightedOffset(-1, -1, 0, weights[1]));
		offsets.add(new WeightedOffset( 0, -1, 0, weights[0]));
		offsets.add(new WeightedOffset(+1, -1, 0, weights[1]));
		offsets.add(new WeightedOffset(-1,  0, 0, weights[0]));

		// iterate on image voxels
		for (int z = 0; z < sizeZ; z++)
		{
			fireProgressChanged(this, z, sizeZ); 
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					// get current label
					int label = labels.get(x, y, z);
					
					// do not process background pixels
					if (label == 0)
						continue;
					
					// current distance value
					int currentDist = distmap.get(x, y, z);
					int newDist = currentDist;
					
					// iterate over neighbors
					for (WeightedOffset offset : offsets)
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
						
						if (labels.get(x2, y2, z2) != label)
						{
							// Update with distance to nearest different label
							newDist = offset.weight;
						}
						else
						{
							// Increment distance
							newDist = Math.min(newDist, distmap.get(x2, y2, z2) + offset.weight);
						}
					}
					
					if (newDist < currentDist) 
					{
						distmap.set(x, y, z, newDist);
					}
				}
			}
		}
		fireProgressChanged(this, 1, 1); 
	}

	private void backwardScan() 
	{
		fireStatusChanged(this, "Backward scan...");
		
		// create array of backward shifts
		ArrayList<WeightedOffset> offsets = new ArrayList<WeightedOffset>();
		
		// offsets in the z+1 plane
		offsets.add(new WeightedOffset(-1, -1, +1, weights[2]));
		offsets.add(new WeightedOffset( 0, -1, +1, weights[1]));
		offsets.add(new WeightedOffset(+1, -1, +1, weights[2]));
		offsets.add(new WeightedOffset(-1,  0, +1, weights[1]));
		offsets.add(new WeightedOffset( 0,  0, +1, weights[0]));
		offsets.add(new WeightedOffset(+1,  0, +1, weights[1]));
		offsets.add(new WeightedOffset(-1, +1, +1, weights[2]));
		offsets.add(new WeightedOffset( 0, +1, +1, weights[1]));
		offsets.add(new WeightedOffset(+1, +1, +1, weights[2]));
	
		// offsets in the current plane
		offsets.add(new WeightedOffset(-1, +1, 0, weights[1]));
		offsets.add(new WeightedOffset( 0, +1, 0, weights[0]));
		offsets.add(new WeightedOffset(+1, +1, 0, weights[1]));
		offsets.add(new WeightedOffset(+1,  0, 0, weights[0]));

		// iterate on image voxels in backward order
		for (int z = sizeZ - 1; z >= 0; z--)
		{
			fireProgressChanged(this, sizeZ-1-z, sizeZ); 
			
			for (int y = sizeY - 1; y >= 0; y--)
			{
				for (int x = sizeX - 1; x >= 0; x--)
				{
					// get current label
					int label = labels.get(x, y, z);
					
					// do not process background pixels
					if (label == 0)
						continue;
					
					// current distance value
					int currentDist = distmap.get(x, y, z);
					int newDist = currentDist;
					
					// iterate over neighbors
					for (WeightedOffset offset : offsets)
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
						
						if (labels.get(x2, y2, z2) != label)
						{
							// Update with distance to nearest different label
							newDist = offset.weight;
						}
						else
						{
							// Increment distance
							newDist = Math.min(newDist, distmap.get(x2, y2, z2) + offset.weight);
						}
					}
					
					if (newDist < currentDist) 
					{
						distmap.set(x, y, z, newDist);
					}
					
				}
			}
		}
		
		fireProgressChanged(this, 1, 1); 
	}

	private void normalizeResultSlices()
	{
		fireStatusChanged(this, "Normalize map..."); 
		for (int z = 0; z < sizeZ; z++) 
		{
			fireProgressChanged(this, z, sizeZ);
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					if (labels.get(x,  y,  z) != 0)
					{
						distmap.set(x, y, z, (int) Math.round(distmap.getValue(x, y, z) / weights[0]));
					}
				}
			}
		}
		fireProgressChanged(this, 1, 1); 
	}
	
	private class WeightedOffset
	{
		int dx;
		int dy;
		int dz;
		short weight;
		
		public WeightedOffset(int dx, int dy, int dz, short weight)
		{
			this.dx = dx;
			this.dy = dy;
			this.dz = dz;
			this.weight = weight;
		}
	}
}
