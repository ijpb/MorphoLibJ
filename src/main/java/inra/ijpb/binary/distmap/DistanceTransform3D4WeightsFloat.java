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
package inra.ijpb.binary.distmap;

import static java.lang.Math.min;

import java.util.Collection;

import ij.ImageStack;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.ChamferWeights3D;
import inra.ijpb.binary.ChamferWeights3D.FloatOffset;
import inra.ijpb.data.image.Images3D;

/**
 * Computes 3D distance transform in a 5x5x5 neighborhood using four chamfer
 * weights and floating point calculation.
 * 
 * @deprecated replaced by ChamferDistanceTransform3DFloat (since 1.4.4)
 *  
 * @author David Legland
 * 
 */
@Deprecated
public class DistanceTransform3D4WeightsFloat extends AlgoStub implements DistanceTransform3D 
{
	// ==================================================
	// Class variables

	/**
	 * The chamfer weights used to propagate distances to neighbor voxels.
	 */
	private float[] weights;

	/**
	 * Flag for dividing final distance map by the value first weight. 
	 * This results in distance map values closer to Euclidean, but with 
	 * non integer values. 
	 */
	private boolean normalizeMap = true;

	
	private int sizeX;
	private int sizeY;
	private int sizeZ;

	private byte[][] maskSlices;

	/**
	 * The result image that will store the distance map. The content
	 * of the buffer is updated during forward and backward iterations.
	 */
	private float[][] resultSlices;
	

	// ==================================================
	// Constructors 
	
	/**
	 * Default constructor that specifies the chamfer weights.
	 * @param weights an array of four weights direction orthogonal, diagonal, cube diagonal, and (+-2,+-1,+-1).
	 */
	public DistanceTransform3D4WeightsFloat(ChamferWeights3D weights)
	{
		this(weights.getFloatWeights(), true);
	}

	/**
	 * Default constructor that specifies the chamfer weights.
	 * @param weights an array of four weights direction orthogonal, diagonal, cube diagonal, and (+-2,+-1,+-1).
	 */
	public DistanceTransform3D4WeightsFloat(float[] weights)
	{
		this.weights = weights;
		if (weights.length < 4)
		{
			throw new IllegalArgumentException("Weights array must have length equal to 4");
		}
	}

	/**
	 * Constructor specifying the chamfer weights and the optional normalization.
	 * @param weights
	 *            an array of two weights for orthogonal and diagonal directions
	 * @param normalize
	 *            flag indicating whether the final distance map should be
	 *            normalized by the first weight
	 */
	public DistanceTransform3D4WeightsFloat(ChamferWeights3D weights, boolean normalize)
	{
		this(weights.getFloatWeights(), normalize);
	}

	/**
	 * Constructor specifying the chamfer weights and the optional normalization.
	 * @param weights
	 *            an array of two weights for orthogonal and diagonal directions
	 * @param normalize
	 *            flag indicating whether the final distance map should be
	 *            normalized by the first weight
	 */
	public DistanceTransform3D4WeightsFloat(float[] weights, boolean normalize)
	{
		this(weights);
		this.normalizeMap = normalize;
	}

	
	// ==================================================
	// Implementation of DistanceTransform3D interface 
	
	/**
	 * Computes the distance map from a 3D binary image. Distance is computed
	 * for each foreground (white) pixel, as the chamfer distance to the nearest
	 * background (black) pixel.
	 * 
	 * @param image
	 *            a 3D binary image with white pixels (255) as foreground
	 * @return a new 3D image containing:
	 *         <ul>
	 *         <li>0 for each background pixel</li>
	 *         <li>the distance to the nearest background pixel otherwise</li>
	 *         </ul>
	 */
	public ImageStack distanceMap(ImageStack image) 
	{
		// size of image
		sizeX = image.getWidth();
		sizeY = image.getHeight();
		sizeZ = image.getSize();
		
		// store wrapper to mask image
		this.maskSlices = Images3D.getByteArrays(image);

		// create new empty image, and fill it with black
		ImageStack buffer = ImageStack.create(sizeX, sizeY, sizeZ, 32);
		this.resultSlices = Images3D.getFloatArrays(buffer);

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
		return buffer;
	}
	
	
	// ==================================================
	// Inner computation methods 
	
	/**
	 * Fill result image with zero for background voxels, and Float.MAX for
	 * foreground voxels.
	 */
	private void initializeResultSlices()
	{
		fireStatusChanged(this, "Initialization..."); 

		// iterate over slices
		for (int z = 0; z < sizeZ; z++) 
		{
			byte[] maskSlice = this.maskSlices[z];
			float[] resultSlice = this.resultSlices[z];
			
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					int index = sizeX * y + x;
					int val = maskSlice[index];
					resultSlice[index] = val == 0 ? 0 : Float.MAX_VALUE;
				}
			}
		}
		fireProgressChanged(this, 1, 1); 
	}
	
	private void forwardScan() 
	{
		fireStatusChanged(this, "Forward scan...");
		
		// create array of forward shifts
		Collection<FloatOffset> offsets = ChamferWeights3D.getForwardOffsets(weights);
		
		// iterate on image voxels
		for (int z = 0; z < sizeZ; z++)
		{
			fireProgressChanged(this, z, sizeZ);
			
			byte[] maskSlice = this.maskSlices[z];
			float[] currentSlice = this.resultSlices[z];
			
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					int index = sizeX * y + x;

					// check if we need to update current voxel
					if ((maskSlice[index] & 0x00FF) == 0)
						continue;
					
					double value = currentSlice[index];
					
					double newVal = Float.MAX_VALUE;
					for (FloatOffset offset : offsets)
					{
						int x2 = x + offset.dx;
						int y2 = y + offset.dy;
						int z2 = z + offset.dz;
						
						// check that current neighbor is within image
						if (x2 >= 0 && x2 < sizeX && y2 >= 0 && y2 < sizeY && z2 >= 0 && z2 < sizeZ)
						{
							newVal = min(newVal, resultSlices[z2][sizeX * y2 + x2] + offset.weight);
						}
						
						if (newVal < value) 
						{
							currentSlice[index] = (float) newVal;
						}
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
		Collection<FloatOffset> offsets = ChamferWeights3D.getBackwardOffsets(weights);

		// iterate on image voxels in backward order
		for (int z = sizeZ - 1; z >= 0; z--)
		{
			fireProgressChanged(this, sizeZ-1-z, sizeZ); 
			
			byte[] maskSlice = this.maskSlices[z];
			float[] currentSlice = this.resultSlices[z];
			
			for (int y = sizeY - 1; y >= 0; y--)
			{
				for (int x = sizeX - 1; x >= 0; x--)
				{
					int index = sizeX * y + x;

					// check if we need to update current voxel
					if ((maskSlice[index] & 0x00FF) == 0)
						continue;
					
					double value = currentSlice[index];
					
					double newVal = Float.MAX_VALUE;
					for (FloatOffset offset : offsets)
					{
						int x2 = x + offset.dx;
						int y2 = y + offset.dy;
						int z2 = z + offset.dz;
						
						// check that current neighbor is within image
						if (x2 >= 0 && x2 < sizeX && y2 >= 0 && y2 < sizeY && z2 >= 0 && z2 < sizeZ)
						{
							newVal = min(newVal, resultSlices[z2][sizeX * y2 + x2] + offset.weight);
						}
					}

					// Update current value if necessary
					if (newVal < value) 
					{
						currentSlice[index] = (float) newVal;
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

			byte[] maskSlice = this.maskSlices[z];
			float[] resultSlice = this.resultSlices[z];
			
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					int index = sizeX * y + x;
					if (maskSlice[index] != 0)
					{
						resultSlice[index] = resultSlice[index] / weights[0];
					}
				}
			}
		}
		fireProgressChanged(this, 1, 1); 
	}
}
