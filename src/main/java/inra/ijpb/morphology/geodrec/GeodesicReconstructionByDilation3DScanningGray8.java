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
package inra.ijpb.morphology.geodrec;

import static java.lang.Math.max;
import static java.lang.Math.min;

import ij.ImageStack;
import inra.ijpb.data.image.ImageUtils;


/**
 * Geodesic reconstruction by dilation for 3D stacks of byte processors, using
 * scanning algorithm.
 * 
 * This version uses iterations of forward and backward passes until no more
 * modifications are made.
 * 
 * @see GeodesicReconstructionByDilation3DScanning
 * @see GeodesicReconstruction3DHybrid0Gray8
 * 
 * @author David Legland
 * 
 */
public class GeodesicReconstructionByDilation3DScanningGray8 extends GeodesicReconstruction3DAlgoStub
{
	ImageStack marker;
	ImageStack mask;
	
	ImageStack result;
	
	/** image width */
	int sizeX = 0;
	/** image height */
	int sizeY = 0;
	/** image depth */
	int sizeZ = 0;

	/**
	 * The flag indicating whether the result image has been modified during
	 * last image scan
	 */
	boolean modif;

	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * using the default connectivity 6.
	 */
	public GeodesicReconstructionByDilation3DScanningGray8()
	{
	}
	
	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * that specifies the connectivity to use.
	 * 
	 * @param connectivity
	 *            the 3D connectivity to use (either 6 or 26)
	 */
	public GeodesicReconstructionByDilation3DScanningGray8(int connectivity)
	{
		this.connectivity = connectivity;
	}

	/**
	 * Run the reconstruction by dilation algorithm using the images specified
	 * as argument.
	 */
	public ImageStack applyTo(ImageStack marker, ImageStack mask) 
	{
		// Keep references to input images
		this.marker = marker;
		this.mask = mask;
		
		// Check input image type
		if (marker.getBitDepth() != 8 || mask.getBitDepth() != 8) 
		{
			throw new IllegalArgumentException("Marker and Mask images must be byte stacks");
		}
		
		// Check sizes are consistent
		this.sizeX 	= marker.getWidth();
		this.sizeY 	= marker.getHeight();
		this.sizeZ 	= marker.getSize();
		if (!ImageUtils.isSameSize(marker, mask))
		{
			throw new IllegalArgumentException("Marker and Mask images must have the same size");
		}
		
		// Check connectivity has a correct value
		if (connectivity != 6 && connectivity != 26) 
		{
			throw new RuntimeException(
					"Connectivity for stacks must be either 6 or 26, not "
							+ connectivity);
		}

		initializeResult();
		
		// Count the number of iterations for eventually displaying progress
		int iter = 1;
		
		// Iterate forward and backward propagations until no more pixel have
		// been modified
		do
		{
			modif = false;

			// Display current status
			trace("Forward iteration " + iter);
			showStatus("Geod. Rec. by Dil. Fwd " + iter);
			
			// forward iteration
			switch (connectivity)
			{
			case 6:
				forwardDilationC6();
				break;
			case 26:
				forwardDilationC26();
				break;
			}

			// Display current status
			trace("Backward iteration " + iter);
			showStatus("Geod. Rec. by Dil. Bwd " + iter);
			
			// backward iteration
			switch (connectivity)
			{
			case 6:
				backwardDilationC6();
				break;
			case 26:
				backwardDilationC26();
				break;
			}

			iter++;
		} while (modif);
	
		// clear progression display
		showProgress(1, 1, "");

		return this.result;
	}

	/**
	 * Run the reconstruction by dilation algorithm using the images specified
	 * as argument.
	 */
	public ImageStack applyTo(
			ImageStack marker, 
			ImageStack mask,
			ImageStack binaryMask ) 
	{
		// Keep references to input images
		this.marker = marker;
		this.mask = mask;
		
		// Check sizes are consistent
		this.sizeX 	= marker.getWidth();
		this.sizeY 	= marker.getHeight();
		this.sizeZ 	= marker.getSize();
		if (!ImageUtils.isSameSize(marker, mask))
		{
			throw new IllegalArgumentException("Marker and Mask images must have the same size");
		}
		
		// Check connectivity has a correct value
		if (connectivity != 6 && connectivity != 26)
		{
			throw new RuntimeException(
					"Connectivity for stacks must be either 6 or 26, not "
							+ connectivity);
		}

		initializeResult( binaryMask );
		
		// Count the number of iterations for eventually displaying progress
		int iter = 1;

		// Iterate forward and backward propagations until no more pixels have
		// been modified
		do
		{
			modif = false;

			// Display current status
			trace("Forward iteration " + iter);
			showStatus("Geod. Rec. by Dil. Fwd " + iter);
			
			// forward iteration
			switch (connectivity)
			{
			case 6:
				forwardDilationC6(binaryMask);
				break;
			case 26:
				forwardDilationC26(binaryMask);
				break;
			}

			// Display current status
			trace("Backward iteration " + iter);
			showStatus("Geod. Rec. by Dil. Bwd " + iter);
			
			// backward iteration
			switch (connectivity)
			{
			case 6:
				backwardDilationC6(binaryMask);
				break;
			case 26:
				backwardDilationC26(binaryMask);
				break;
			}

			iter++;
		} while (modif);
	
		return this.result;
	}
	
	
	/** 
	 * Initialize the result image with the minimum value of marker and mask
	 * images.
	 */
	private void initializeResult() 
	{
		// Create result image the same size as the mask image
		this.result = ImageStack.create(sizeX, sizeY, sizeZ, mask.getBitDepth());

		Object[] stack = result.getImageArray();
		Object[] markerStack = marker.getImageArray();
		Object[] maskStack = mask.getImageArray();
		byte[] slice;
		byte[] markerSlice;
		byte[] maskSlice;

		// Initialize the result image with the minimum value of marker and mask
		// images
		for (int z = 0; z < sizeZ; z++)
		{
			slice = (byte[]) stack[z];
			maskSlice = (byte[]) maskStack[z];
			markerSlice = (byte[]) markerStack[z];

			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
				int index = y * sizeX + x;
					int value = min(markerSlice[index] & 0x00FF, maskSlice[index] & 0x00FF);
					slice[index] = (byte) value;
				}
			}
		}
	}
	
	/** 
	 * Initialize the result image with the minimum value of marker and mask
	 * images.
	 */
	private void initializeResult( ImageStack binaryMask ) 
	{
		// Create result image the same size as the mask image
		this.result = ImageStack.create(sizeX, sizeY, sizeZ, mask.getBitDepth());

		Object[] stack = result.getImageArray();
		Object[] markerStack = marker.getImageArray();
		Object[] maskStack = mask.getImageArray();
		byte[] slice;
		byte[] markerSlice;
		byte[] maskSlice;

		// Initialize the result image with the minimum value of marker and mask
		// images
		for (int z = 0; z < sizeZ; z++) 
		{
			slice = (byte[]) stack[z];
			maskSlice = (byte[]) maskStack[z];
			markerSlice = (byte[]) markerStack[z];
			
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					if( binaryMask.getVoxel(x, y, z) != 0 )
					{
						int index = y * sizeX + x;
						int value = min(markerSlice[index] & 0x00FF, maskSlice[index] & 0x00FF);
						slice[index] = (byte) value;
					}
				}
			}
		}
	}
	

	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 6-adjacency, assuming pixels are stored in bytes.
	 */
	private void forwardDilationC6() 
	{
		// the maximal value around current pixel
		int maxValue;

		Object[] stack = result.getImageArray();
		byte[] slice;
		byte[] slice2;
		
		// Iterate over pixels
		for (int z = 0; z < sizeZ; z++) 
		{
			showProgress(z, sizeZ, "z = " + z);
			
			slice = (byte[]) stack[z];
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++) 
				{
					int currentValue = slice[y * sizeX + x] & 0x00FF;
					maxValue = currentValue;
					
					// Iterate over the 3 'upper' neighbors of current pixel
					if (x > 0) 
						maxValue = max(maxValue, slice[y * sizeX + x - 1] & 0x00FF);
					if (y > 0) 
						maxValue = max(maxValue, slice[(y - 1) * sizeX + x] & 0x00FF);
					if (z > 0) {
						slice2 = (byte[]) stack[z - 1];
						maxValue = max(maxValue, slice2[y * sizeX + x] & 0x00FF);
					}
					
					// update value of current voxel
					maxValue = Math.min(maxValue, (int) mask.getVoxel(x, y, z));
					if (maxValue > currentValue) 
					{
						slice[y * sizeX + x] = (byte) (maxValue & 0x00FF);
						modif = true;
					}
				}
			}
		} // end of pixel iteration
	}

	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 6-adjacency, assuming pixels are stored in bytes.
	 */
	private void forwardDilationC6( ImageStack binaryMask ) 
	{
		// the maximal value around current pixel
		int maxValue;

		Object[] stack = result.getImageArray();
		Object[] binaryStack = binaryMask.getImageArray();
		byte[] slice;
		byte[] slice2;
		byte[] binarySlice;
		
		// Iterate over pixels
		for (int z = 0; z < sizeZ; z++)
		{
			showProgress(z, sizeZ, "z = " + z);
			
			slice = (byte[]) stack[z];
			binarySlice = (byte[]) binaryStack[z];
			
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					if( binarySlice[ y * sizeX + x ] != 0 )
					{
						int currentValue = slice[y * sizeX + x] & 0x00FF;
						maxValue = currentValue;

						// Iterate over the 3 'upper' neighbors of current pixel
						if (x > 0) 
							maxValue = max(maxValue, slice[y * sizeX + x - 1] & 0x00FF);
						if (y > 0) 
							maxValue = max(maxValue, slice[(y - 1) * sizeX + x] & 0x00FF);
						if (z > 0) 
						{
							slice2 = (byte[]) stack[z - 1];
							maxValue = max(maxValue, slice2[y * sizeX + x] & 0x00FF);
						}

						// update value of current voxel
						maxValue = Math.min(maxValue, (int) mask.getVoxel(x, y, z));
						if (maxValue > currentValue)
						{
							slice[y * sizeX + x] = (byte) (maxValue & 0x00FF);
							modif = true;
						}
					}
				}
			}
		} // end of pixel iteration
	}
	
	
	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency, assuming pixels are stored in bytes.
	 */
	private void forwardDilationC26()
	{
		// the maximal value around current pixel
		int maxValue;

		Object[] stack = result.getImageArray();
		byte[] slice;
		byte[] slice2;
		
		// Iterate over pixels
		for (int z = 0; z < sizeZ; z++)
		{
			showProgress(z, sizeZ, "z = " + z);
			
			slice = (byte[]) stack[z];
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					int currentValue = slice[y * sizeX + x] & 0x00FF;
					maxValue = currentValue;
					
					// Iterate over neighbors of current pixel
					int zmax = min(z + 1, sizeZ);
					for (int z2 = max(z - 1, 0); z2 < zmax; z2++)
					{
						slice2 = (byte[]) stack[z2];
						
						int ymax = z2 == z ? y : min(y + 1, sizeY - 1); 
						for (int y2 = max(y - 1, 0); y2 <= ymax; y2++)
						{
							int xmax = (z2 == z && y2 == y) ? x - 1 : min(x + 1, sizeX - 1); 
							for (int x2 = max(x - 1, 0); x2 <= xmax; x2++)
							{
								int neighborValue = slice2[y2 * sizeX + x2] & 0x00FF;
								if (neighborValue > maxValue)
									maxValue = neighborValue;
							}
						}
					}

					// update value of current voxel
					maxValue = Math.min(maxValue, (int) mask.getVoxel(x, y, z));
					if (maxValue > currentValue) 
					{
						slice[y * sizeX + x] = (byte) (maxValue & 0x00FF);
						modif = true;
					}

				}
			}
		}
	}

	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency, assuming pixels are stored in bytes.
	 */
	private void forwardDilationC26( ImageStack binaryMask ) 
	{
		// the maximal value around current pixel
		int maxValue;

		Object[] stack = result.getImageArray();
		Object[] binaryStack = binaryMask.getImageArray();
		byte[] slice;
		byte[] slice2;
		byte[] binarySlice;
		
		// Iterate over pixels
		for (int z = 0; z < sizeZ; z++) 
		{
			showProgress(z, sizeZ, "z = " + z);

			slice = (byte[]) stack[z];
			binarySlice = (byte[]) binaryStack[ z ];
			
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					if( binarySlice[ y * sizeX + x ] != 0 )
					{
						int currentValue = slice[y * sizeX + x] & 0x00FF;
						maxValue = currentValue;

						// Iterate over neighbors of current pixel
						int zmax = min(z + 1, sizeZ);
						for (int z2 = max(z - 1, 0); z2 < zmax; z2++)
						{
							slice2 = (byte[]) stack[z2];

							int ymax = z2 == z ? y : min(y + 1, sizeY - 1); 
							for (int y2 = max(y - 1, 0); y2 <= ymax; y2++)
							{
								int xmax = (z2 == z && y2 == y) ? x - 1 : min(x + 1, sizeX - 1); 
								for (int x2 = max(x - 1, 0); x2 <= xmax; x2++)
								{
									int neighborValue = slice2[y2 * sizeX + x2] & 0x00FF;
									if (neighborValue > maxValue)
										maxValue = neighborValue;
								}
							}
						}

						// update value of current voxel
						maxValue = Math.min(maxValue, (int) mask.getVoxel(x, y, z));
						if (maxValue > currentValue) 
						{
							slice[y * sizeX + x] = (byte) (maxValue & 0x00FF);
							modif = true;
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * Update result image using pixels in the lower right neighborhood, using
	 * the 6-adjacency.
	 */
	private void backwardDilationC6() 
	{
		// the maximal value around current pixel
		int maxValue;

		Object[] stack = result.getImageArray();
		byte[] slice;
		byte[] slice2;

		// Iterate over voxels
		for (int z = sizeZ - 1; z >= 0; z--) 
		{
			showProgress(sizeZ - 1 - z, sizeZ, "z = " + z);
			
			slice = (byte[]) stack[z];
			for (int y = sizeY - 1; y >= 0; y--) 
			{
				for (int x = sizeX - 1; x >= 0; x--)
				{
					int currentValue = slice[y * sizeX + x] & 0x00FF;
					maxValue = currentValue;
					
					// Iterate over the 3 'lower' neighbors of current voxel
					if (x < sizeX - 1) 
						maxValue = max(maxValue, slice[y * sizeX + x + 1] & 0x00FF);
					if (y < sizeY - 1) 
						maxValue = max(maxValue, slice[(y + 1) * sizeX + x] & 0x00FF);
					if (z < sizeZ - 1)
					{
						slice2 = (byte[]) stack[z + 1];
						maxValue = max(maxValue, slice2[y * sizeX + x] & 0x00FF);
					}

					// update value of current voxel
					maxValue = Math.min(maxValue, (int) mask.getVoxel(x, y, z));
					if (maxValue > currentValue)
					{
						slice[y * sizeX + x] = (byte) (maxValue & 0x00FF);
						modif = true;
					}
				}
			}
		}	
	}
	
	/**
	 * Update result image using pixels in the lower right neighborhood, using
	 * the 6-adjacency.
	 */
	private void backwardDilationC6( ImageStack binaryMask ) 
	{
		// the maximal value around current pixel
		int maxValue;

		Object[] stack = result.getImageArray();
		Object[] binaryStack = binaryMask.getImageArray();
		byte[] slice;
		byte[] slice2;
		byte[] binarySlice;

		// Iterate over voxels
		for (int z = sizeZ - 1; z >= 0; z--)
		{
			showProgress(sizeZ - 1 - z, sizeZ, "z = " + z);

			slice = (byte[]) stack[z];
			binarySlice = (byte[]) binaryStack[ z ];
			
			for (int y = sizeY - 1; y >= 0; y--) 
			{
				for (int x = sizeX - 1; x >= 0; x--)
				{
					if( binarySlice[ y * sizeX + x ] != 0 )
					{
						int currentValue = slice[y * sizeX + x] & 0x00FF;
						maxValue = currentValue;

						// Iterate over the 3 'lower' neighbors of current voxel
						if (x < sizeX - 1) 
							maxValue = max(maxValue, slice[y * sizeX + x + 1] & 0x00FF);
						if (y < sizeY - 1) 
							maxValue = max(maxValue, slice[(y + 1) * sizeX + x] & 0x00FF);
						if (z < sizeZ - 1)
						{
							slice2 = (byte[]) stack[z + 1];
							maxValue = max(maxValue, slice2[y * sizeX + x] & 0x00FF);
						}

						// update value of current voxel
						maxValue = Math.min(maxValue, (int) mask.getVoxel(x, y, z));
						if (maxValue > currentValue) 
						{
							slice[y * sizeX + x] = (byte) (maxValue & 0x00FF);
							modif = true;
						}
					}
				}
			}
		}	
	}
	
	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency.
	 */
	private void backwardDilationC26( ImageStack binaryMask ) 
	{
		// the maximal value around current pixel
		int maxValue;

		Object[] stack = result.getImageArray();
		Object[] binaryStack = binaryMask.getImageArray();
		byte[] slice;
		byte[] slice2;
		byte[] binarySlice;

		// Iterate over voxels
		for (int z = sizeZ - 1; z >= 0; z--)
		{
			showProgress(sizeZ - 1 - z, sizeZ, "z = " + z);

			slice = (byte[]) stack[z];
			binarySlice = (byte[]) binaryStack[z];

			for (int y = sizeY - 1; y >= 0; y--)
			{
				for (int x = sizeX - 1; x >= 0; x--)
				{
					if (binarySlice[y * sizeX + x] != 0)
					{
						int currentValue = slice[y * sizeX + x] & 0x00FF;
						maxValue = currentValue;

						// Iterate over neighbors of current voxel
						int zmin = max(z - 1, 0);
						for (int z2 = min(z + 1, sizeZ - 1); z2 >= zmin; z2--)
						{
							slice2 = (byte[]) stack[z2];

							int ymin = z2 == z ? y : max(y - 1, 0);
							for (int y2 = min(y + 1, sizeY - 1); y2 >= ymin; y2--)
							{
								int xmin = (z2 == z && y2 == y) ? x : max(x - 1, 0);
								for (int x2 = min(x + 1, sizeX - 1); x2 >= xmin; x2--)
								{
									int index = y2 * sizeX + x2;
									int neighborValue = slice2[index] & 0x00FF;
									if (neighborValue > maxValue)
										maxValue = neighborValue;
								}
							}
						}

						// update value of current voxel
						maxValue = Math.min(maxValue, (int) mask.getVoxel(x, y, z));
						if (maxValue > currentValue)
						{
							slice[y * sizeX + x] = (byte) (maxValue & 0x00FF);
							modif = true;
						}
					}
				}
			}
		}	
	}
	
	
	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency.
	 */
	private void backwardDilationC26() 
	{
		// the maximal value around current pixel
		int maxValue;

		Object[] stack = result.getImageArray();
		byte[] slice;
		byte[] slice2;

		// Iterate over voxels
		for (int z = sizeZ - 1; z >= 0; z--) 
		{
			showProgress(sizeZ - 1 - z, sizeZ, "z = " + z);
			
			slice = (byte[]) stack[z];
			for (int y = sizeY - 1; y >= 0; y--)
			{
				for (int x = sizeX - 1; x >= 0; x--)
				{
					int currentValue = slice[y * sizeX + x] & 0x00FF;
					maxValue = currentValue;

					// Iterate over neighbors of current voxel
					int zmin = max(z - 1, 0);
					for (int z2 = min(z + 1, sizeZ - 1); z2 >= zmin; z2--)
					{
						slice2 = (byte[]) stack[z2];

						int ymin = z2 == z ? y : max(y - 1, 0);
						for (int y2 = min(y + 1, sizeY - 1); y2 >= ymin; y2--)
						{
							int xmin = (z2 == z && y2 == y) ? x : max(x - 1, 0);
							for (int x2 = min(x + 1, sizeX - 1); x2 >= xmin; x2--)
							{
								int index = y2 * sizeX + x2;
								int neighborValue = slice2[index] & 0x00FF;
								if (neighborValue > maxValue)
									maxValue = neighborValue;
							}
						}
					}

					// update value of current voxel
					maxValue = Math.min(maxValue, (int) mask.getVoxel(x, y, z));
					if (maxValue > currentValue) 
					{
						slice[y * sizeX + x] = (byte) (maxValue & 0x00FF);
						modif = true;
					}
				}
			}
		}	
	}
}
