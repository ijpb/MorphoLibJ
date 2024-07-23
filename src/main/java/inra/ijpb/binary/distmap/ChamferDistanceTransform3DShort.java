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
package inra.ijpb.binary.distmap;

import static java.lang.Math.min;

import java.util.Collection;

import ij.ImageStack;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.distmap.ChamferMask3D.ShortOffset;
import inra.ijpb.data.image.Images3D;

/**
 * Computes 3D distance transform using the chamfer weights provided by a
 * ChamferMask3D object, and using 16-bits integer computation.
 * 
 * @author David Legland
 * 
 */
public class ChamferDistanceTransform3DShort extends AlgoStub implements ChamferDistanceTransform3D
{
	// ==================================================
	// Class variables

	/**
	 * The chamfer weights used to propagate distances to neighbor voxels.
	 */
	ChamferMask3D mask;
	
	/**
	 * Flag for dividing final distance map by the value first weight. 
	 * This results in distance map values closer to Euclidean, but with 
	 * non integer values. 
	 */
	boolean normalize = true;
	
	
	// ==================================================
	// Constructors 
	
	/**
	 * Creates a new algorithm for computing 3D distance maps based on a chamfer
	 * mask.
	 * 
	 * @param mask
	 *            the chamfer mask to use for propagating distances
	 */
	public ChamferDistanceTransform3DShort(ChamferMask3D mask)
	{
		this.mask = mask;
	}
	
	/**
	 * Creates a new algorithm for computing 3D distance maps based on a chamfer
	 * mask.
	 * 
	 * @param mask
	 *            the chamfer mask to use for propagating distances
	 * @param normalize
	 *            whether distance map should be normalized by the weight
	 *            associated to orthogonal shifts
	 */
	public ChamferDistanceTransform3DShort(ChamferMask3D mask, boolean normalize)
	{
		this.mask = mask;
		this.normalize = normalize;
	}
	

	// ==================================================
	// Implementation of the ChamferDistanceTransform3D interface 
	
	@Override
	public ChamferMask3D mask()
	{
		return this.mask;
	}

	
	// ==================================================
	// Implementation of the DistanceTransform3D interface 
	
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
		int[] dims = new int[] {sizeX, sizeY, sizeZ};

		// store wrapper to mask image
		byte[][] maskSlices = Images3D.getByteArrays(image);

		// create new empty image, and fill it with black
		ImageStack buffer = ImageStack.create(sizeX, sizeY, sizeZ, 16);
		short[][] resultSlices = Images3D.getShortArrays(buffer);
		
		initializeResultSlices(maskSlices, resultSlices);
		
		// Two iterations are enough to compute distance map to boundary
		forwardScan(dims, maskSlices, resultSlices);
		backwardScan(dims, maskSlices, resultSlices);

		// Normalize values by the first weight
		if (this.normalize) 
		{
			normalizeResultSlices(maskSlices, resultSlices); 
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
	private void initializeResultSlices(byte[][] maskSlices, short[][] resultSlices)
	{
		fireStatusChanged(this, "Initialization...");

		// iterate over slices
		int sizeZ = maskSlices.length;
		for (int z = 0; z < sizeZ; z++) 
		{
			fireProgressChanged(this, z, sizeZ);
			
			byte[] maskSlice = maskSlices[z];
			short[] resultSlice = resultSlices[z];
			
			for (int i = 0; i < maskSlice.length; i++)
			{
				int val = maskSlice[i];
				resultSlice[i] = val == 0 ? 0 : Short.MAX_VALUE;
			}
		}
		fireProgressChanged(this, 1, 1); 
	}
	
	private void forwardScan(int[] dims, byte[][] maskSlices, short[][] resultSlices) 
	{
		fireStatusChanged(this, "Forward scan..."); 
		
		// retrieve image dimensions
		int sizeX = dims[0];
		int sizeY = dims[1];
		int sizeZ = dims[2];
		
		// create array of forward shifts
		Collection<ShortOffset> offsets = this.mask.getForwardOffsets();

		// iterate on image voxels
		for (int z = 0; z < sizeZ; z++)
		{
			fireProgressChanged(this, z, sizeZ); 
			
			byte[] maskSlice = maskSlices[z];
			short[] currentSlice = resultSlices[z];
			
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					int index = sizeX * y + x;

					// check if we need to update current voxel
					if ((maskSlice[index] & 0x00FF) == 0)
						continue;
					
					int value = currentSlice[index];
					
					// iterate over forward offsets defined by ChamferWeights
					int newVal = Short.MAX_VALUE;
					for (ShortOffset offset : offsets)
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
							currentSlice[index] = (short) newVal;
						}
					}
				}
			}
		}
		fireProgressChanged(this, 1, 1); 
	}
	
	private void backwardScan(int[] dims, byte[][] maskSlices, short[][] resultSlices) 
	{
		fireStatusChanged(this, "Backward scan..."); 
		
		// retrieve image dimensions
		int sizeX = dims[0];
		int sizeY = dims[1];
		int sizeZ = dims[2];
		
		// create array of backward shifts
		Collection<ShortOffset> offsets = this.mask.getBackwardOffsets();
		
		// iterate on image voxels in backward order
		for (int z = sizeZ - 1; z >= 0; z--)
		{
			fireProgressChanged(this, sizeZ-1-z, sizeZ);
			
			byte[] maskSlice = maskSlices[z];
			short[] currentSlice = resultSlices[z];
			
			for (int y = sizeY - 1; y >= 0; y--)
			{
				for (int x = sizeX - 1; x >= 0; x--)
				{
					int index = sizeX * y + x;

					// check if we need to update current voxel
					if ((maskSlice[index] & 0x00FF) == 0)
						continue;
					
					int value = currentSlice[index];
					
					// iterate over backward offsets defined by ChamferWeights
					int newVal = Short.MAX_VALUE;
					for (ShortOffset offset : offsets)
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
						currentSlice[index] = (short) newVal;
					}
				}
			}
		}
		fireProgressChanged(this, 1, 1); 
	}
	
	private void normalizeResultSlices(byte[][] maskSlices, short[][] resultSlices)
	{
		fireStatusChanged(this, "Normalize map..."); 
		
		// retrieve the minimum weight
		double w0 = mask.getShortNormalizationWeight();
		
		// normalize each voxel
		int sizeZ = maskSlices.length;
		for (int z = 0; z < sizeZ; z++) 
		{
			fireProgressChanged(this, z, sizeZ);
			
			byte[] maskSlice = maskSlices[z];
			short[] resultSlice = resultSlices[z];
			
			for (int i = 0; i < maskSlice.length; i++)
			{
				if (maskSlice[i] != 0)
				{
					double value = resultSlice[i] & 0x00FFFF;
					resultSlice[i] = (short) Math.round(value / w0);
				}
			}
		}
		
		fireProgressChanged(this, 1, 1); 
	}

}
