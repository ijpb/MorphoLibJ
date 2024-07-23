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

import static java.lang.Math.min;
import ij.ImageStack;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.ChamferWeights3D;
import inra.ijpb.data.image.Images3D;

/**
 * Computes Chamfer distances in a 3x3x3 neighborhood and storing result in
 * 16-bits image.
 * 
 * In practice, computations are done with integers, but result is stored in a
 * 3D short image, thus requiring less memory than floating point.
 * 
 * @deprecated replaced by ChamferDistanceTransform3DShort (since 1.5.0)
 *  
 * @author David Legland
 * 
 */
@Deprecated
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

	private byte[][] maskSlices;

	/**
	 * The result image that will store the distance map. The content
	 * of the buffer is updated during forward and backward iterations.
	 */
	private short[][] resultSlices;
	

	// ==================================================
	// Constructors 
	
	/**
	 * Default constructor that specifies the chamfer weights.
	 * @param weights an array of two weights for orthogonal and diagonal directions
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
		this.maskSlices = Images3D.getByteArrays(image);

		// create new empty image, and fill it with black
		ImageStack buffer = ImageStack.create(sizeX, sizeY, sizeZ, 16);
		this.resultSlices = Images3D.getShortArrays(buffer);

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
			
			byte[] maskSlice = this.maskSlices[z];
			short[] resultSlice = this.resultSlices[z];

			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					int index = sizeX * y + x;
					int val = maskSlice[index];
					resultSlice[index] = val == 0 ? 0 : Short.MAX_VALUE;
				}
			}
		}
		fireProgressChanged(this, 1, 1); 
	}
	
	private void forwardScan() 
	{
		fireStatusChanged(this, "Forward scan..."); 
		// iterate on image voxels
		for (int z = 0; z < sizeZ; z++)
		{
			fireProgressChanged(this, z, sizeZ); 
			
			byte[] maskSlice = this.maskSlices[z];
			short[] resultSlice = this.resultSlices[z];
			
			short[] resultSlice2 = null;
			if (z > 0) 
			{
				resultSlice2 = this.resultSlices[z - 1];
			}
			
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					int index = sizeX * y + x;

					// check if we need to update current voxel
					if ((maskSlice[index] & 0x00FF) == 0)
						continue;
					
					// init new values for current voxel
					int ortho = Short.MAX_VALUE;
					int diago = Short.MAX_VALUE;
					int diag3 = Short.MAX_VALUE;
					
					// process (z-1) slice
					if (z > 0) 
					{
						if (y > 0)
						{
							// voxels in the (y-1) line of  the (z-1) plane
							if (x > 0) 
							{
								diag3 = Math.min(diag3, resultSlice2[sizeX * (y - 1) + x - 1] & 0x00FFFF);
							}
							diago = Math.min(diago, resultSlice2[sizeX * (y - 1) + x] & 0x00FFFF);
							if (x < sizeX - 1) 
							{
								diag3 = Math.min(diag3, resultSlice2[sizeX * (y - 1) + x + 1] & 0x00FFFF);
							}
						}
						
						// voxels in the y line of the (z-1) plane
						if (x > 0) 
						{
							diago = Math.min(diago, resultSlice2[sizeX * y + x - 1] & 0x00FFFF);
						}
						ortho = Math.min(ortho, resultSlice2[sizeX * y + x] & 0x00FFFF);
						if (x < sizeX - 1) 
						{
							diago = Math.min(diago, resultSlice2[sizeX * y + x + 1] & 0x00FFFF);
						}

						if (y < sizeY - 1)
						{
							// voxels in the (y+1) line of  the (z-1) plane
							if (x > 0) 
							{
								diag3 = Math.min(diag3, resultSlice2[sizeX * (y + 1) + x - 1] & 0x00FFFF);
							}
							diago = Math.min(diago, resultSlice2[sizeX * (y + 1) + x] & 0x00FFFF);
							if (x < sizeX - 1) 
							{
								diag3 = Math.min(diag3, resultSlice2[sizeX * (y + 1) + x + 1] & 0x00FFFF);
							}
						}
					}
					
					// voxels in the (y-1) line of the z-plane
					if (y > 0)
					{
						if (x > 0) 
						{
							diago = Math.min(diago, resultSlice[sizeX * (y - 1) + x - 1] & 0x00FFFF);
						}
						ortho = Math.min(ortho, resultSlice[sizeX * (y - 1) + x] & 0x00FFFF);
						if (x < sizeX - 1) 
						{
							diago = Math.min(diago, resultSlice[sizeX * (y - 1) + x + 1] & 0x00FFFF);
						}
					}
					
					// pixel to the left of the current voxel
					if (x > 0) 
					{
						ortho = Math.min(ortho, resultSlice[sizeX * y + x - 1] & 0x00FFFF);
					}
					
					int newVal = min3w(ortho, diago, diag3);
					updateIfNeeded(x, y, z, newVal);
				}
			}
		}
		fireProgressChanged(this, 1, 1); 
	}

	private void backwardScan() 
	{
		fireStatusChanged(this, "Backward scan..."); 
		// iterate on image voxels in backward order
		for (int z = sizeZ - 1; z >= 0; z--)
		{
			fireProgressChanged(this, sizeZ-1-z, sizeZ); 
			
			byte[] maskSlice = this.maskSlices[z];
			short[] resultSlice = this.resultSlices[z];
			
			short[] resultSlice2 = null;
			if (z < sizeZ - 1) 
			{
				resultSlice2 = this.resultSlices[z + 1];
			}
			
			for (int y = sizeY - 1; y >= 0; y--)
			{
				for (int x = sizeX - 1; x >= 0; x--)
				{
					int index = sizeX * y + x;

					// check if we need to update current voxel
					if ((maskSlice[index] & 0x00FF) == 0)
						continue;
					
					// init new values for current voxel
					int ortho = Short.MAX_VALUE;
					int diago = Short.MAX_VALUE;
					int diag3 = Short.MAX_VALUE;
					
					// process (z+1) slice
					if (z < sizeZ - 1) 
					{
						if (y < sizeY - 1)
						{
							// voxels in the (y+1) line of  the (z+1) plane
							if (x < sizeX - 1) 
							{
								diag3 = Math.min(diag3, resultSlice2[sizeX * (y + 1) + x + 1] & 0x00FFFF);
							}
							diago = Math.min(diago, resultSlice2[sizeX * (y + 1) + x] & 0x00FFFF);
							if (x > 0) 
							{
								diag3 = Math.min(diag3, resultSlice2[sizeX * (y + 1) + x - 1] & 0x00FFFF);
							}
						}
						
						// voxels in the y line of the (z+1) plane
						if (x < sizeX - 1) 
						{
							diago = Math.min(diago, resultSlice2[sizeX * y + x + 1] & 0x00FFFF);
						}
						ortho = Math.min(ortho, resultSlice2[sizeX * y + x] & 0x00FFFF);
						if (x > 0) 
						{
							diago = Math.min(diago, resultSlice2[sizeX * y + x - 1] & 0x00FFFF);
						}

						if (y > 0)
						{
							// voxels in the (y-1) line of  the (z+1) plane
							if (x < sizeX - 1) 
							{
								diag3 = Math.min(diag3, resultSlice2[sizeX * (y - 1) + x + 1] & 0x00FFFF);
							}
							diago = Math.min(diago, resultSlice2[sizeX * (y - 1) + x] & 0x00FFFF);
							if (x > 0) 
							{
								diag3 = Math.min(diag3, resultSlice2[sizeX * (y - 1) + x - 1] & 0x00FFFF);
							}
						}
					}
					
					// voxels in the (y+1) line of the z-plane
					if (y < sizeY - 1)
					{
						if (x < sizeX - 1) 
						{
							diago = Math.min(diago, resultSlice[sizeX * (y + 1) + x + 1] & 0x00FFFF);
						}
						ortho = Math.min(ortho, resultSlice[sizeX * (y + 1) + x] & 0x00FFFF);
						if (x > 0) 
						{
							diago = Math.min(diago, resultSlice[sizeX * (y + 1) + x - 1] & 0x00FFFF);
						}
					}
					
					// voxel to the right of the current voxel
					if (x < sizeX - 1) 
					{
						ortho = Math.min(ortho, resultSlice[sizeX *  y + x + 1] & 0x00FFFF);
					}
					
					int newVal = min3w(ortho, diago, diag3);
					updateIfNeeded(x, y, z, newVal);
				}
			}
		}
		fireProgressChanged(this, 1, 1); 
	}
	
	/**
	 * Computes the weighted minima of orthogonal, diagonal, and 3D diagonal
	 * values.
	 */
	private int min3w(int ortho, int diago, int diag2)
	{
		return min(min(ortho + weights[0], diago + weights[1]), diag2 + weights[2]);
	}
	
	/**
	 * Update the pixel at position (i,j,k) with the value newVal. If newVal is
	 * greater or equal to current value at position (i,j,k), do nothing.
	 */
	private void updateIfNeeded(int i, int j, int k, int newVal)
	{
		int index = j * sizeX + i;
		int value = resultSlices[k][index] & 0x00FFFF;
		if (newVal < value) 
		{
			resultSlices[k][index] = (short) newVal;
		}
	}

	private void normalizeResultSlices()
	{
		fireStatusChanged(this, "Normalize map..."); 
		
		double w0 = weights[0];
		
		for (int z = 0; z < sizeZ; z++) 
		{
			fireProgressChanged(this, z, sizeZ);
			
			byte[] maskSlice = this.maskSlices[z];
			short[] resultSlice = this.resultSlices[z];

			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					int index = sizeX * y + x;
					if (maskSlice[index] != 0)
					{
						double value = resultSlice[index] & 0x00FFFF;
						resultSlice[index] = (short) Math.round(value / w0);
					}
				}
			}
		}
		fireProgressChanged(this, 1, 1); 
	}
}
