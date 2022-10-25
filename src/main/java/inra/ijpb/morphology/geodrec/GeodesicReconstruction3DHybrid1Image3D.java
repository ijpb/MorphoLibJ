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
package inra.ijpb.morphology.geodrec;

import static java.lang.Math.max;
import static java.lang.Math.min;
import ij.ImageStack;
import inra.ijpb.data.Cursor3D;
import inra.ijpb.data.image.Image3D;
import inra.ijpb.data.image.ImageUtils;
import inra.ijpb.data.image.Images3D;

import java.util.ArrayDeque;


/**
 * <p>
 * Geodesic reconstruction for 3D stacks of any type, using hybrid algorithm and
 * Image3D access class. This class manages both reconstructions by dilation and
 * erosion.
 * </p>
 * 
 * <p>
 * Performs forward and backward passes, then performs an additional forward
 * pass only to initialize the queue, and finally processes all voxels in the
 * queue.
 * </p>
 * 
 * <p>
 * Uses specialized class to access the values in 3D image stacks, by avoiding
 * to check bounds at each access. Process all type of images by using double
 * precision computation.
 * </p>
 * 
 * @author David Legland
 * 
 */
public class GeodesicReconstruction3DHybrid1Image3D extends	GeodesicReconstruction3DAlgoStub
{
	GeodesicReconstructionType reconstructionType = GeodesicReconstructionType.BY_DILATION;
	
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

	/** the queue containing the positions that need update */
	ArrayDeque<Cursor3D> queue;
	
	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * using the default connectivity 6.
	 */
	public GeodesicReconstruction3DHybrid1Image3D() 
	{
	}
	
	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * that specifies the type of reconstruction, and using the connectivity 6.
	 * 
	 * @param type
	 *            the type of reconstruction (erosion or dilation)
	 */
	public GeodesicReconstruction3DHybrid1Image3D(GeodesicReconstructionType type) 
	{
		this.reconstructionType = type;
	}

	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * that specifies the type of reconstruction, and the connectivity to use.
	 * 
	 * @param type
	 *            the type of reconstruction (erosion or dilation)
	 * @param connectivity
	 *            the 3D connectivity to use (either 6 or 26)
	 */
	public GeodesicReconstruction3DHybrid1Image3D(
			GeodesicReconstructionType type, int connectivity) 
	{
		this.reconstructionType = type;
		this.connectivity = connectivity;
	}

	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * that specifies the connectivity to use.
	 * 
	 * @param connectivity
	 *            the 3D connectivity to use (either 6 or 26)
	 */
	public GeodesicReconstruction3DHybrid1Image3D(int connectivity) 
	{
		this.connectivity = connectivity;
	}

	/**
	 * @return the reconstructionType
	 */
	public GeodesicReconstructionType getReconstructionType() 
	{
		return reconstructionType;
	}

	/**
	 * @param reconstructionType the reconstructionType to set
	 */
	public void setReconstructionType(
			GeodesicReconstructionType reconstructionType) 
	{
	this.reconstructionType = reconstructionType;
	}

	/**
	 * Run the reconstruction by dilation algorithm using the images specified
	 * as argument.
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

		long t0 = System.currentTimeMillis();
		trace("Initialize result ");
		
		initializeResult();
		if (verbose) 
		{
			long t1 = System.currentTimeMillis();
			System.out.println((t1 - t0) + " ms");
			t0 = t1;
		}

		
		// Display current status
		trace("Forward iteration ");
		showStatus("Geod. Rec. Fwd ");
		
		forwardScan();
		if (verbose)
		{
			long t1 = System.currentTimeMillis();
			System.out.println((t1 - t0) + " ms");
			t0 = t1;
		}


		// Display current status
		trace("Backward iteration ");
		showStatus("Geod. Rec. Bwd ");
		
		backwardScan();
		if (verbose)
		{
			long t1 = System.currentTimeMillis();
			System.out.println((t1 - t0) + " ms");
			t0 = t1;
		}

		// Display current status
		trace("Init queue ");
		showStatus("Init queue");
		
		initQueue();
		if (verbose) 
		{
			long t1 = System.currentTimeMillis();
			System.out.println((t1 - t0) + " ms");
			t0 = t1;
		}
		
		// Display current status
		trace("Process queue");
		showStatus("Process queue");
		
		processQueue();
		if (verbose) 
		{
			long t1 = System.currentTimeMillis();
			System.out.println((t1 - t0) + " ms");
			t0 = t1;
		}

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
		this.resultStack = ImageStack.create(sizeX, sizeY, sizeZ, maskStack.getBitDepth());
		this.result = Images3D.createWrapper(this.resultStack);

		if (this.reconstructionType == GeodesicReconstructionType.BY_DILATION) 
		{
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
							result.setValue(x, y, z, min(marker.getValue(x, y, z), mask.getValue(x, y, z)));
						}
					}
				}
			}
		}
		else 
		{
			// Initialize the result image with the maximum value of marker and mask
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
							result.setValue(x, y, z, max(marker.getValue(x, y, z), mask.getValue(x, y, z)));
						}
					}
				}
			}
		}
	}
	
	private void forwardScan()
	{
		if (this.connectivity == 6)
		{
			forwardScanC6();
		}
		else
		{
			forwardScanC26();
		}
	}

	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 6-adjacency, assuming pixels are stored in bytes.
	 */
	private void forwardScanC6() 
	{
		final int sign = this.reconstructionType.getSign();
		
		// the maximal value around current pixel
		double maxValue;

		// Iterate over pixels
		for (int z = 0; z < sizeZ; z++)
		{
			showProgress(z, sizeZ);
			
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					double currentValue = result.getValue(x, y, z) * sign;
					maxValue = currentValue;
					
					// Iterate over the 3 'upper' neighbors of current pixel
					if (x > 0) 
						maxValue = max(maxValue, result.getValue(x - 1, y, z) * sign);
					if (y > 0) 
						maxValue = max(maxValue, result.getValue(x, y - 1, z) * sign);
					if (z > 0)
						maxValue = max(maxValue, result.getValue(x, y, z - 1) * sign);
					
					
					// update value of current voxel
					maxValue = min(maxValue, mask.getValue(x, y, z) * sign);
					if (maxValue > currentValue) {
						result.setValue(x, y, z, maxValue * sign);
					}
				}
			}
		} // end of pixel iteration

		// clear progression display
		showProgress(1, 1, "");
	}

	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency, assuming pixels are stored using integer data types.
	 */
	private void forwardScanC26()
	{
		final int sign = this.reconstructionType.getSign();
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
					double currentValue = result.getValue(x, y, z) * sign;
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
								maxValue = max(maxValue, result.getValue(x2, y2, z2) * sign);
							}
						}
					}

					// update value of current voxel
					maxValue = min(maxValue, mask.getValue(x, y, z) * sign);
					if (maxValue > currentValue)
					{
						result.setValue(x, y, z, maxValue * sign);
					}
				}
			}
		}

		// clear progression display
		showProgress(1, 1, "");
	}


	private void backwardScan()
	{
		if (this.connectivity == 6) 
		{
			backwardScanC6();
		} 
		else 
		{
			backwardScanC26();
		}
	}
	/**
	 * Update result image using pixels in the lower right neighborhood, using
	 * the 6-adjacency.
	 */
	private void backwardScanC6() 
	{
		final int sign = this.reconstructionType.getSign();
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
					double currentValue = result.getValue(x, y, z) * sign;
					maxValue = currentValue;
					
					// Iterate over the 3 'lower' neighbors of current voxel
					if (x < sizeX - 1)
						maxValue = max(maxValue, result.getValue(x + 1, y, z) * sign);
					if (y < sizeY - 1)
						maxValue = max(maxValue, result.getValue(x, y + 1, z) * sign);
					if (z < sizeZ - 1)
						maxValue = max(maxValue, result.getValue(x, y, z + 1) * sign);
					

					// update value of current voxel
					maxValue = min(maxValue, mask.getValue(x, y, z) * sign);
					if (maxValue > currentValue) 
					{
						result.setValue(x, y, z, maxValue * sign);
					}
				}
			}
		}	

		// clear progression display
		showProgress(1, 1, "");
	}
	
	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency.
	 */
	private void backwardScanC26() 
	{
		final int sign = this.reconstructionType.getSign();
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
					double currentValue = result.getValue(x, y, z) * sign;
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
								maxValue = max(maxValue, result.getValue(x2, y2, z2) * sign);
							}
						}
					}
	
					// update value of current voxel
					maxValue = min(maxValue, mask.getValue(x, y, z) * sign);
					if (maxValue > currentValue)
					{
						result.setValue(x, y, z, maxValue * sign);
					}
				}
			}
		}	

		// clear progression display
		showProgress(1, 1, "");
	}
	
	private void initQueue()
	{
		if (this.connectivity == 6) 
		{
			initQueueC6();
		} 
		else 
		{
			initQueueC26();
		}
	}

	/**
	 * Update result image using pixels in the upper left neighborhood,
	 * using the 6-adjacency.
	 */
	private void initQueueC6() 
	{
		// sign for adapting dilation and erosion algorithms
		final int sign = this.reconstructionType.getSign();

		// the maximal value around current pixel
		double maxValue;
				
		queue = new ArrayDeque<Cursor3D>();
		
		// Iterate over pixels
		for (int z = 0; z < sizeZ; z++)
		{
			showProgress(z + 1, sizeZ);
			
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++) 
				{
					double currentValue = result.getValue(x, y, z) * sign;
					maxValue = currentValue;
					
					// Iterate over the 3 'upper' neighbors of current pixel
					if (x > 0) 
						maxValue = max(maxValue, result.getValue(x - 1, y, z) * sign);
					if (y > 0) 
						maxValue = max(maxValue, result.getValue(x, y - 1, z) * sign);
					if (z > 0)
						maxValue = max(maxValue, result.getValue(x, y, z - 1) * sign);
					
					if (maxValue > currentValue)
						updateQueue(x, y, z, maxValue, sign);
				}
			}
		} // end of pixel iteration

	}

	/**
	 * Update result image using pixels in the upper left neighborhood,
	 * using the 26-adjacency.
	 */
	private void initQueueC26() 
	{
		// sign for adapting dilation and erosion algorithms
		final int sign = this.reconstructionType.getSign();

		// the maximal value around current pixel
		double maxValue;
				
		queue = new ArrayDeque<Cursor3D>();
		
		// Iterate over pixels
		for (int z = 0; z < sizeZ; z++)
		{
			showProgress(z + 1, sizeZ);
			
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++)
				{
					double currentValue = result.getValue(x, y, z) * sign;
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
								maxValue = max(maxValue, result.getValue(x2, y2, z2) * sign);
							}
						}
					}

					if (maxValue > currentValue)
						updateQueue(x, y, z, maxValue, sign);
				}
			}
		} // end of pixel iteration

	}

	private void processQueue()
	{
		if (this.connectivity == 6)
		{
			processQueueC6();
		} 
		else
		{
			processQueueC26();
		}
	}

	/**
	 * Update result image using next pixel in the queue,
	 * using the 6-adjacency.
	 */
	private void processQueueC6()
	{
		// sign for adapting dilation and erosion algorithms
		final int sign = this.reconstructionType.getSign();

		// the maximal value around current pixel
		double value;
		
		while (!queue.isEmpty())
		{
			Cursor3D p = queue.removeFirst();
			int x = p.getX();
			int y = p.getY();
			int z = p.getZ();
			value = result.getValue(x, y, z) * sign;
			
			// compare with each one of the neighbors
			if (x > 0) 
				value = max(value, result.getValue(x - 1, y, z) * sign);
			if (x < sizeX - 1) 
				value = max(value, result.getValue(x + 1, y, z) * sign);
			if (y > 0) 
				value = max(value, result.getValue(x, y - 1, z) * sign);
			if (y < sizeY - 1) 
				value = max(value, result.getValue(x, y + 1, z) * sign);
			if (z > 0) 
				value = max(value, result.getValue(x, y, z - 1) * sign);
			if (z < sizeZ - 1) 
				value = max(value, result.getValue(x, y, z + 1) * sign);

			// bound with mask value
			value = min(value, mask.getValue(x, y, z) * sign);
			
			// if no update is needed, continue to next item in queue
			if (value <= result.getValue(x, y, z) * sign) 
				continue;
			
			// update result for current position
			result.setValue(x, y, z, value * sign);

			// Eventually add each neighbor
			if (x > 0)
				updateQueue(x - 1, y, z, value, sign);
			if (x < sizeX - 1)
				updateQueue(x + 1, y, z, value, sign);
			if (y > 0)
				updateQueue(x, y - 1, z, value, sign);
			if (y < sizeY - 1)
				updateQueue(x, y + 1, z, value, sign);
			if (z > 0)
				updateQueue(x, y, z - 1, value, sign);
			if (z < sizeZ - 1)
				updateQueue(x, y, z + 1, value, sign);
		}
		
	}

	/**
	 * Update result image using next pixel in the queue,
	 * using the 26-adjacency.
	 */
	private void processQueueC26()
	{
		// sign for adapting dilation and erosion algorithms
		final int sign = this.reconstructionType.getSign();

		// the maximal value around current pixel
		double value;
		
		while (!queue.isEmpty()) 
		{
			Cursor3D p = queue.removeFirst();
			int x = p.getX();
			int y = p.getY();
			int z = p.getZ();
			value = result.getValue(x, y, z) * sign;
			
			// compute bounds of neighborhood
			int xmin = max(x - 1, 0);
			int xmax = min(x + 1, sizeX - 1);
			int ymin = max(y - 1, 0);
			int ymax = min(y + 1, sizeY - 1);
			int zmin = max(z - 1, 0);
			int zmax = min(z + 1, sizeZ - 1);

			// compare with each one of the neighbors
			for (int z2 = zmin; z2 <= zmax; z2++) 
			{
				for (int y2 = ymin; y2 <= ymax; y2++) 
				{
					for (int x2 = xmin; x2 <= xmax; x2++) 
					{
						value = max(value, result.getValue(x2, y2, z2) * sign);
					}
				}
			}
			
			// bound with mask value
			value = min(value, mask.getValue(x, y, z) * sign);
			
			// if no update is needed, continue to next item in queue
			if (value <= result.getValue(x, y, z) * sign) 
				continue;
			
			// update result for current position
			result.setValue(x, y, z, value * sign);

			// compare with each one of the neighbors
			for (int z2 = zmin; z2 <= zmax; z2++) 
			{
				for (int y2 = ymin; y2 <= ymax; y2++) 
				{
					for (int x2 = xmin; x2 <= xmax; x2++) 
					{
						updateQueue(x2, y2, z2, value, sign);
					}
				}
			}
		}
	}

	/**
	 * Adds the current position to the queue if and only if the value 
	 * <code>value<value> is greater than the value of the mask.
	 * @param i column index
	 * @param j row index
	 * @param value value at (i,j) position
	 * @param sign integer +1 or -1 to manage both erosions and dilations
	 */
	private void updateQueue(int i, int j, int k, double value, double sign) 
	{
		// update current value only if value is strictly greater
		value = Math.min(value, mask.getValue(i, j, k) * sign);
		if (value > result.getValue(i, j, k) * sign)
		{
			Cursor3D position = new Cursor3D(i, j, k);
			queue.add(position);
		}
	}
}
