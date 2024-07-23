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
import inra.ijpb.data.image.Image3D;
import inra.ijpb.data.image.ImageUtils;
import inra.ijpb.data.image.Images3D;


/**
 * <p>
 * Geodesic reconstruction by dilation for 3D stacks using scanning algorithm,
 * and Image3D access class.
 * </p>
 * 
 * <p>
 * This version uses iterations of forward and backward passes until no more
 * modifications are made. It is intended to work on any type of scalar 3D,
 * images, using 6 or 26 adjacencies.
 * </p>
 * 
 * <p>
 * Uses specialized class to access the values in 3D image stacks, by avoiding
 * to check bounds at each access. For byte stack, the class
 * GeodesicReconstructionByDilation3DScanningGray8 may be faster.
 * </p>
 * 
 * @see inra.ijpb.data.image.Image3D
 * 
 * @author David Legland
 */
public class GeodesicReconstructionByDilation3DScanning extends GeodesicReconstruction3DAlgoStub
{
	ImageStack markerStack;
	ImageStack maskStack;
	
	ImageStack resultStack;
	Image3D result;
	Image3D mask;
	Image3D marker;
	
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
	public GeodesicReconstructionByDilation3DScanning() 
	{
	}
	
	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * that specifies the connectivity to use.
	 * 
	 * @param connectivity
	 *            the 3D connectivity to use (either 6 or 26)
	 */
	public GeodesicReconstructionByDilation3DScanning(int connectivity) 
	{
		this.connectivity = connectivity;
	}


	/**
	 * Run the reconstruction by dilation algorithm using the images specified
	 * as argument.
	 * 
	 * @param marker
	 *            the image used as marker for reconstruction
	 * @param mask
	 *            the image used to constrain the reconstruction
	 * @return the result of geodesic reconstruction
	 */
	public ImageStack applyTo(ImageStack marker, ImageStack mask)
	{
		// Keep references to input images
		this.markerStack = marker;
		this.maskStack = mask;
		
		this.marker = Images3D.createWrapper(marker);
		this.mask = Images3D.createWrapper(mask);
		
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
		
		boolean integerStack = marker.getBitDepth() != 32;
		
		// Count the number of iterations for eventually displaying progress
		int iter = 1;
		
		// Iterate forward and backward propagations until no more pixel have been modified
		do
		{
			modif = false;

			// Display current status
			trace("Forward iteration " + iter);
			showStatus("Geod. Rec. by Dil. Fwd " + iter);
			
			if (integerStack) 
			{
				forwardDilationInt();
			} 
			else
			{
				forwardDilationFloat();
			}

			// Display current status
			trace("Backward iteration " + iter);
			showStatus("Geod. Rec. by Dil. Bwd " + iter);
			
			if (integerStack)
			{
				backwardDilationInt();
			} 
			else
			{
				backwardDilationFloat();
			}

			iter++;
		} while (modif);
	
		// clear progression display
		showProgress(1, 1, "");

		return this.resultStack;
	}

	/**
	 * Run the reconstruction by dilation algorithm using the images specified
	 * as argument.
	 * 
	 * @param marker
	 *            the image used as marker for reconstruction
	 * @param mask
	 *            the image used to constrain the reconstruction
	 * @param binaryMask
	 *            the region of intersect for processing input images
	 * @return the result of geodesic reconstruction
	 */
	public ImageStack applyTo(
			ImageStack marker, 
			ImageStack mask,
			ImageStack binaryMask ) 
	{
		throw new RuntimeException("Method not yet implemented");
	}
	
	
	/** 
	 * Initialize the result image with the minimum value of marker and mask
	 * images.
	 */
	private void initializeResult()
	{
		// Create result image the same size as marker image
		this.resultStack = ImageStack.create(sizeX, sizeY, sizeZ, maskStack.getBitDepth());
		this.result = Images3D.createWrapper(this.resultStack);

		// Initialize the result image with the minimum value of marker and mask
		// images
		if (this.maskStack.getBitDepth() == 32)
		{
			// Initialize float result stack
			for (int z = 0; z < sizeZ; z++) 
			{
				for (int y = 0; y < sizeY; y++)
				{
					for (int x = 0; x < sizeX; x++)
					{
						result.setValue(x, y, z, min(marker.getValue(x, y, z), mask.getValue(x, y, z)));
					}
				}
			}
		}
		else
		{
			// Initialize integer result stack
			for (int z = 0; z < sizeZ; z++) 
			{
				for (int y = 0; y < sizeY; y++) 
				{
					for (int x = 0; x < sizeX; x++) 
					{
						result.set(x, y, z, min(marker.get(x, y, z), mask.get(x, y, z)));
					}
				}
			}
		}
	}
	
	private void forwardDilationInt()
	{
		if (this.connectivity == 6) 
		{
			forwardDilationIntC6();
		} 
		else 
		{
			forwardDilationIntC26();
		}
	}

	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 6-adjacency, assuming pixels are stored in bytes.
	 */
	private void forwardDilationIntC6() 
	{
		// the maximal value around current pixel
		int maxValue;

		// Iterate over pixels
		for (int z = 0; z < sizeZ; z++) 
		{
			showProgress(z, sizeZ);
			
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++)
				{
					int currentValue = result.get(x, y, z);
					maxValue = currentValue;
					
					// Iterate over the 3 'upper' neighbors of current pixel
					if (x > 0) 
						maxValue = max(maxValue, result.get(x - 1, y, z));
					if (y > 0) 
						maxValue = max(maxValue, result.get(x, y - 1, z));
					if (z > 0)
						maxValue = max(maxValue, result.get(x, y, z - 1));
					
					
					// update value of current voxel
					maxValue = min(maxValue, mask.get(x, y, z));
					if (maxValue > currentValue) {
						result.set(x, y, z, maxValue);
						modif = true;
					}
				}
			}
		} // end of pixel iteration
	}

	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency, assuming pixels are stored using integer data types.
	 */
	private void forwardDilationIntC26()
	{
		// the maximal value around current pixel
		int maxValue;

		// Iterate over pixels
		for (int z = 0; z < sizeZ; z++) 
		{
			showProgress(z, sizeZ, "z = " + z);
			
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					int currentValue = result.get(x, y, z);
					maxValue = currentValue;

					// Iterate over neighbors of current pixel
					int zmax = min(z + 1, sizeZ);
					for (int z2 = max(z - 1, 0); z2 < zmax; z2++)
					{
						int ymax = z2 == z ? y : min(y + 1, sizeY - 1); 
						for (int y2 = max(y - 1, 0); y2 <= ymax; y2++)
						{
							int xmax = (z2 == z && y2 == y) ? x - 1 : min(x + 1, sizeX - 1); 
							for (int x2 = max(x - 1, 0); x2 <= xmax; x2++) {
								maxValue = max(maxValue, result.get(x2, y2, z2));
							}
						}
					}

					// update value of current voxel
					maxValue = min(maxValue, mask.get(x, y, z));
					if (maxValue > currentValue) {
						result.set(x, y, z, maxValue);
						modif = true;
					}
				}
			}
		}
	}

	private void forwardDilationFloat()
	{
		if (this.connectivity == 6) 
		{
			forwardDilationFloatC6();
		} 
		else
		{
			forwardDilationFloatC26();
		}
	}

	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 6-adjacency, assuming pixels are stored in bytes.
	 */
	private void forwardDilationFloatC6() 
	{
		// the maximal value around current pixel
		double maxValue;

		// Iterate over pixels
		for (int z = 0; z < sizeZ; z++) 
		{
			showProgress(z, sizeZ, "z = " + z);
			
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					double currentValue = result.getValue(x, y, z);
					maxValue = currentValue;
					
					// Iterate over the 3 'upper' neighbors of current pixel
					if (x > 0) 
						maxValue = max(maxValue, result.getValue(x - 1, y, z));
					if (y > 0) 
						maxValue = max(maxValue, result.getValue(x, y - 1, z));
					if (z > 0)
						maxValue = max(maxValue, result.getValue(x, y, z - 1));
					
					
					// update value of current voxel
					maxValue = min(maxValue, mask.getValue(x, y, z));
					if (maxValue > currentValue) 
					{
						result.setValue(x, y, z, maxValue);
						modif = true;
					}
				}
			}
		} // end of pixel iteration
	}

	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency, assuming pixels are stored in floating point values.
	 */
	private void forwardDilationFloatC26() 
	{
		// the maximal value around current pixel
		double maxValue;

		// Iterate over pixels
		for (int z = 0; z < sizeZ; z++) 
		{
			showProgress(z + 1, sizeZ, "z = " + z);

			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++)
				{
					double currentValue = result.getValue(x, y, z);
					maxValue = currentValue;

					// Iterate over neighbors of current pixel
					int zmax = min(z + 1, sizeZ);
					for (int z2 = max(z - 1, 0); z2 < zmax; z2++) 
					{
						int ymax = z2 == z ? y : min(y + 1, sizeY - 1); 
						for (int y2 = max(y - 1, 0); y2 <= ymax; y2++)
						{
							int xmax = (z2 == z && y2 == y) ? x - 1 : min(x + 1, sizeX - 1); 
							for (int x2 = max(x - 1, 0); x2 <= xmax; x2++)
							{
								maxValue = max(maxValue, result.getValue(x2, y2, z2));
							}
						}
					}

					// update value of current voxel
					maxValue = min(maxValue, mask.getValue(x, y, z));
					if (maxValue > currentValue) {
						result.setValue(x, y, z, maxValue);
						modif = true;
					}
				}
			}
		}
	}
	
	private void backwardDilationInt() 
	{
		if (this.connectivity == 6) 
		{
			backwardDilationIntC6();
		} 
		else 
		{
			backwardDilationIntC26();
		}
	}
	
	/**
	 * Update result image using pixels in the lower right neighborhood, using
	 * the 6-adjacency.
	 */
	private void backwardDilationIntC6() 
	{
		// the maximal value around current pixel
		int maxValue;

		// Iterate over voxels
		for (int z = sizeZ - 1; z >= 0; z--)
		{
			showProgress(sizeZ - z, sizeZ, "z = " + z);

			for (int y = sizeY - 1; y >= 0; y--) 
			{
				for (int x = sizeX - 1; x >= 0; x--)
				{
					int currentValue = result.get(x, y, z);
					maxValue = currentValue;
					
					// Iterate over the 3 'lower' neighbors of current voxel
					if (x < sizeX - 1) 
						maxValue = max(maxValue, result.get(x + 1, y, z));
					if (y < sizeY - 1) 
						maxValue = max(maxValue, result.get(x, y + 1, z));
					if (z < sizeZ - 1) {
						maxValue = max(maxValue, result.get(x, y, z + 1));
					}

					// update value of current voxel
					maxValue = min(maxValue, mask.get(x, y, z));
					if (maxValue > currentValue)
					{
						result.set(x, y, z, maxValue);
						modif = true;
					}
				}
			}
		}	
	}
	
	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency.
	 */
	private void backwardDilationIntC26()
	{
		// the maximal value around current pixel
		int maxValue;
	
		// Iterate over voxels
		for (int z = sizeZ - 1; z >= 0; z--) 
		{
			showProgress(sizeZ - z, sizeZ, "z = " + z);
	
			for (int y = sizeY - 1; y >= 0; y--)
			{
				for (int x = sizeX - 1; x >= 0; x--)
				{
					int currentValue = result.get(x, y, z);
					maxValue = currentValue;
	
					// Iterate over neighbors of current voxel
					int zmin = max(z - 1, 0);
					for (int z2 = min(z + 1, sizeZ - 1); z2 >= zmin; z2--) 
					{
	
						int ymin = z2 == z ? y : max(y - 1, 0); 
						for (int y2 = min(y + 1, sizeY - 1); y2 >= ymin; y2--) 
						{
							int xmin = (z2 == z && y2 == y) ? x : max(x - 1, 0); 
							for (int x2 = min(x + 1, sizeX - 1); x2 >= xmin; x2--) 
							{
								maxValue = max(maxValue, result.get(x2, y2, z2));
							}
						}
					}
	
					// update value of current voxel
					maxValue = min(maxValue, mask.get(x, y, z));
					if (maxValue > currentValue)
					{
						result.set(x, y, z, maxValue);
						modif = true;
					}
				}
			}
		}	
	}

	private void backwardDilationFloat()
	{
		if (this.connectivity == 6) 
		{
			backwardDilationFloatC6();
		}
		else
		{
			backwardDilationFloatC26();
		}
	}

	/**
	 * Update result image using pixels in the lower right neighborhood, using
	 * the 6-adjacency.
	 */
	private void backwardDilationFloatC6() 
	{
		// the maximal value around current pixel
		double maxValue;

		// Iterate over voxels
		for (int z = sizeZ - 1; z >= 0; z--)
		{
			showProgress(sizeZ - 1 - z, sizeZ, "z = " + z);

			for (int y = sizeY - 1; y >= 0; y--) 
			{
				for (int x = sizeX - 1; x >= 0; x--) 
				{
					double currentValue = result.getValue(x, y, z);
					maxValue = currentValue;
					
					// Iterate over the 3 'lower' neighbors of current voxel
					if (x < sizeX - 1) 
						maxValue = max(maxValue, result.getValue(x + 1, y, z));
					if (y < sizeY - 1) 
						maxValue = max(maxValue, result.getValue(x, y + 1, z));
					if (z < sizeZ - 1) 
						maxValue = max(maxValue, result.getValue(x, y, z + 1));
					
					// update value of current voxel
					maxValue = min(maxValue, mask.getValue(x, y, z));
					if (maxValue > currentValue)
					{
						result.setValue(x, y, z, maxValue);
						modif = true;
					}
				}
			}
		}	
	}
	
	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency.
	 */
	private void backwardDilationFloatC26() 
	{
		// the maximal value around current pixel
		double maxValue;
	
		// Iterate over voxels
		for (int z = sizeZ - 1; z >= 0; z--)
		{
			showProgress(sizeZ - 1 - z, sizeZ, "z = " + z);
	
			for (int y = sizeY - 1; y >= 0; y--) 
			{
				for (int x = sizeX - 1; x >= 0; x--) 
				{
					double currentValue = result.getValue(x, y, z);
					maxValue = currentValue;
	
					// Iterate over neighbors of current voxel
					int zmin = max(z - 1, 0);
					for (int z2 = min(z + 1, sizeZ - 1); z2 >= zmin; z2--) 
					{
	
						int ymin = z2 == z ? y : max(y - 1, 0); 
						for (int y2 = min(y + 1, sizeY - 1); y2 >= ymin; y2--) 
						{
							int xmin = (z2 == z && y2 == y) ? x : max(x - 1, 0); 
							for (int x2 = min(x + 1, sizeX - 1); x2 >= xmin; x2--)
							{
								maxValue = max(maxValue, result.getValue(x2, y2, z2));
							}
						}
					}
	
					// update value of current voxel
					maxValue = min(maxValue, mask.getValue(x, y, z));
					if (maxValue > currentValue)
					{
						result.setValue(x, y, z, maxValue);
						modif = true;
					}
				}
			}
		}	
	}
}
