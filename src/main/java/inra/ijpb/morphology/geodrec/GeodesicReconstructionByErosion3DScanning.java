/**
 * 
 */
package inra.ijpb.morphology.geodrec;

import static java.lang.Math.max;
import static java.lang.Math.min;
import ij.ImageStack;
import inra.ijpb.data.image.Image3D;
import inra.ijpb.data.image.Images3D;


/**
 * <p>
 * Geodesic reconstruction by erosion for 3D stacks using scanning algorithm,
 * and Image3D access class.
 * </p>
 * 
 * <p>
 * This version uses iterations of forward and backward passes until no more
 * modifications are made. It is intended to work on any type of scalar 3D
 * images, using 6 or 26 adjacencies.
 * </p>
 * 
 * <p>
 * Uses specialized class <code>Image3D</code> to access the values in 3D image
 * stacks, by avoiding to check bounds at each access. For byte stack, the class
 * <code>GeodesicReconstructionByDilation3DGray8Scanning</code> may be faster.
 * </p>
 * 
 * @see inra.ijpb.data.image.Image3D
 * @author David Legland
 * 
 */
public class GeodesicReconstructionByErosion3DScanning extends GeodesicReconstruction3DAlgoStub
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
	public GeodesicReconstructionByErosion3DScanning() 
	{
	}
	
	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * that specifies the connectivity to use.
	 * 
	 * @param connectivity
	 *            the 3D connectivity to use (either 6 or 26)
	 */
	public GeodesicReconstructionByErosion3DScanning(int connectivity) 
	{
		this.connectivity = connectivity;
	}

	/**
	 * Run the reconstruction by dilation algorithm using the images specified
	 * as argument.
	 */
	public ImageStack applyTo(ImageStack marker, ImageStack mask)
	{
		if ( Thread.currentThread().isInterrupted() )					
			return null;
		
		// Keep references to input images
		this.markerStack = marker;
		this.maskStack = mask;
		
		this.marker = Images3D.createWrapper(marker);
		this.mask = Images3D.createWrapper(mask);
		
		// Check sizes are consistent
		this.sizeX 	= marker.getWidth();
		this.sizeY 	= marker.getHeight();
		this.sizeZ 	= marker.getSize();
		if (!Images3D.isSameSize(marker, mask)) 
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
			showStatus("Geod. Rec. by Ero. Fwd " + iter);
			
			if (integerStack)
			{
				forwardErosionInt();
			} else
			{
				forwardErosionFloat();
			}

			// Display current status
			trace("Backward iteration " + iter);
			showStatus("Geod. Rec. by Ero. Bwd " + iter);
			
			if (integerStack)
			{
				backwardErosionInt();
			} else
			{
				backwardErosionFloat();
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
		this.resultStack = ImageStack.create(sizeX, sizeY, sizeZ,
				markerStack.getBitDepth());
		this.result = Images3D.createWrapper(this.resultStack);

		// Initialize the result image with the minimum value of marker and mask
		// images
		if (this.markerStack.getBitDepth() == 32)
		{
			// Initialize float result stack
			for (int z = 0; z < sizeZ; z++)
			{
				for (int y = 0; y < sizeY; y++)
				{
					for (int x = 0; x < sizeX; x++)
					{
						result.setValue(x, y, z, max(marker.getValue(x, y, z), mask.getValue(x, y, z)));
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
						result.set(x, y, z, max(marker.get(x, y, z), mask.get(x, y, z)));
					}
				}
			}
		}
	}
	
	private void forwardErosionInt()
	{
		if (this.connectivity == 6)
		{
			forwardErosionIntC6();
		} else
		{
			forwardErosionIntC26();
		}
	}

	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 6-adjacency, assuming pixels are stored in bytes.
	 */
	private void forwardErosionIntC6()
	{
		// the minimal value around current pixel
		int minValue;

		// Iterate over pixels
		for (int z = 0; z < sizeZ; z++)
		{
			showProgress(z, sizeZ);
			
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					int currentValue = result.get(x, y, z);
					minValue = currentValue;
					
					// Iterate over the 3 'upper' neighbors of current pixel
					if (x > 0) 
						minValue = min(minValue, result.get(x - 1, y, z));
					if (y > 0) 
						minValue = min(minValue, result.get(x, y - 1, z));
					if (z > 0)
						minValue = min(minValue, result.get(x, y, z - 1));
					
					
					// update value of current voxel
					minValue = max(minValue, mask.get(x, y, z));
					if (minValue < currentValue) 
					{
						result.set(x, y, z, minValue);
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
	private void forwardErosionIntC26()
	{
		// the minimal value around current pixel
		int minValue;

		// Iterate over pixels
		for (int z = 0; z < sizeZ; z++)
		{
			showProgress(z, sizeZ, "z = " + z);

			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					int currentValue = result.get(x, y, z);
					minValue = currentValue;

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
								minValue = min(minValue, result.get(x2, y2, z2));
							}
						}
					}

					// update value of current voxel
					minValue = max(minValue, mask.get(x, y, z));
					if (minValue < currentValue)
					{
						result.set(x, y, z, minValue);
						modif = true;
					}
				}
			}
		}
	}

	private void forwardErosionFloat()
	{
		if (this.connectivity == 6)
		{
			forwardErosionFloatC6();
		} else
		{
			forwardErosionFloatC26();
		}
	}

	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 6-adjacency, assuming pixels are stored in bytes.
	 */
	private void forwardErosionFloatC6() 
	{
		// the minimal value around current pixel
		double minValue;

		// Iterate over pixels
		for (int z = 0; z < sizeZ; z++) 
		{
			showProgress(z, sizeZ, "z = " + z);
			
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					double currentValue = result.getValue(x, y, z);
					minValue = currentValue;
					
					// Iterate over the 3 'upper' neighbors of current pixel
					if (x > 0) 
						minValue = min(minValue, result.getValue(x - 1, y, z));
					if (y > 0) 
						minValue = min(minValue, result.getValue(x, y - 1, z));
					if (z > 0)
						minValue = min(minValue, result.getValue(x, y, z - 1));
					
					
					// update value of current voxel
					minValue = max(minValue, mask.getValue(x, y, z));
					if (minValue < currentValue) 
					{
						result.setValue(x, y, z, minValue);
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
	private void forwardErosionFloatC26()
	{
		// the minimal value around current pixel
		double minValue;

		// Iterate over pixels
		for (int z = 0; z < sizeZ; z++)
		{
			showProgress(z, sizeZ, "z = " + z);
			
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					double currentValue = result.getValue(x, y, z);
					minValue = currentValue;

					// Iterate over neighbors of current pixel
					int zmax = min(z + 1, sizeZ);
					for (int z2 = max(z - 1, 0); z2 < zmax; z2++)
					{
						int ymax = z2 == z ? y : min(y + 1, sizeY - 1);
						for (int y2 = max(y - 1, 0); y2 <= ymax; y2++)
						{
							int xmax = (z2 == z && y2 == y) ? x - 1 : min(x + 1, sizeX - 1); 
							for (int x2 = max(x - 1, 0); x2 <= xmax; x2++) {
								minValue = min(minValue, result.getValue(x2, y2, z2));
							}
						}
					}

					// update value of current voxel
					minValue = max(minValue, mask.getValue(x, y, z));
					if (minValue < currentValue)
					{
						result.setValue(x, y, z, minValue);
						modif = true;
					}
				}
			}
		}
	}
	
	private void backwardErosionInt()
	{
		if (this.connectivity == 6)
		{
			backwardErosionIntC6();
		} else
		{
			backwardErosionIntC26();
		}
	}
	
	/**
	 * Update result image using pixels in the lower right neighborhood, using
	 * the 6-adjacency.
	 */
	private void backwardErosionIntC6()
	{
		// the minimal value around current pixel
		int minValue;

		// Iterate over voxels
		for (int z = sizeZ - 1; z >= 0; z--)
		{
			showProgress(sizeZ - z, sizeZ, "z = " + z);

			for (int y = sizeY - 1; y >= 0; y--)
			{
				for (int x = sizeX - 1; x >= 0; x--)
				{
					int currentValue = result.get(x, y, z);
					minValue = currentValue;
					
					// Iterate over the 3 'lower' neighbors of current voxel
					if (x < sizeX - 1) 
						minValue = min(minValue, result.get(x + 1, y, z));
					if (y < sizeY - 1) 
						minValue = min(minValue, result.get(x, y + 1, z));
					if (z < sizeZ - 1) 
					{
						minValue = min(minValue, result.get(x, y, z + 1));
					}

					// update value of current voxel
					minValue = max(minValue, mask.get(x, y, z));
					if (minValue < currentValue) 
					{
						result.set(x, y, z, minValue);
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
	private void backwardErosionIntC26() 
	{
		// the minimal value around current pixel
		int minValue;
	
		// Iterate over voxels
		for (int z = sizeZ - 1; z >= 0; z--)
		{
			showProgress(sizeZ - 1 - z, sizeZ, "z = " + z);

			for (int y = sizeY - 1; y >= 0; y--)
			{
				for (int x = sizeX - 1; x >= 0; x--)
				{
					int currentValue = result.get(x, y, z);
					minValue = currentValue;
	
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
								minValue = min(minValue, result.get(x2, y2, z2));
							}
						}
					}
	
					// update value of current voxel
					minValue = max(minValue, mask.get(x, y, z));
					if (minValue < currentValue)
					{
						result.set(x, y, z, minValue);
						modif = true;
					}
				}
			}
		}	
	}

	private void backwardErosionFloat()
	{
		if (this.connectivity == 6)
		{
			backwardErosionFloatC6();
		} else
		{
			backwardErosionFloatC26();
		}
	}

	/**
	 * Update result image using pixels in the lower right neighborhood, using
	 * the 6-adjacency.
	 */
	private void backwardErosionFloatC6()
	{
		// the maximal value around current pixel
		double minValue;

		// Iterate over voxels
		for (int z = sizeZ - 1 - 1; z >= 0; z--)
		{
			showProgress(sizeZ - 1 - z, sizeZ, "z = " + z);

			for (int y = sizeY - 1; y >= 0; y--)
			{
				for (int x = sizeX - 1; x >= 0; x--)
				{
					double currentValue = result.getValue(x, y, z);
					minValue = currentValue;
					
					// Iterate over the 3 'lower' neighbors of current voxel
					if (x < sizeX - 1) 
						minValue = min(minValue, result.getValue(x + 1, y, z));
					if (y < sizeY - 1) 
						minValue = min(minValue, result.getValue(x, y + 1, z));
					if (z < sizeZ - 1) 
						minValue = min(minValue, result.getValue(x, y, z + 1));
					
					// update value of current voxel
					minValue = max(minValue, mask.getValue(x, y, z));
					if (minValue < currentValue) 
					{
						result.setValue(x, y, z, minValue);
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
	private void backwardErosionFloatC26() 
	{
		// the minimal value around current pixel
		double minValue;
	
		// Iterate over voxels
		for (int z = sizeZ - 1; z >= 0; z--) 
		{
			showProgress(sizeZ - 1 - z, sizeZ, "z = " + z);
			
			for (int y = sizeY - 1; y >= 0; y--)
			{
				for (int x = sizeX - 1; x >= 0; x--)
				{
					double currentValue = result.getValue(x, y, z);
					minValue = currentValue;
	
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
								minValue = min(minValue, result.getValue(x2, y2, z2));
							}
						}
					}
	
					// update value of current voxel
					minValue = max(minValue, mask.getValue(x, y, z));
					if (minValue < currentValue)
					{
						result.setValue(x, y, z, minValue);
						modif = true;
					}
				}
			}
		}	
	}
}
