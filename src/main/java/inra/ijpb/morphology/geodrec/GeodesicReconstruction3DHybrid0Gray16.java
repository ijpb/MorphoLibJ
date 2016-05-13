/**
 * 
 */
package inra.ijpb.morphology.geodrec;

import static java.lang.Math.max;
import static java.lang.Math.min;
import ij.ImageStack;
import inra.ijpb.data.Cursor3D;

import java.util.ArrayDeque;
import java.util.Deque;


/**
 * <p>
 * Geodesic reconstruction for 3D stacks of shorts, using hybrid algorithm. This
 * class manages both reconstructions by dilation and erosion.
 * </p>
 * 
 * <p>
 * This version first performs forward scan, then performs a backward scan that
 * also add lower-right neighbors to the queue, and finally processes voxels in
 * the queue. It is intended to work on 8 bits 3D images, using 6 or 26
 * connectivities.
 * </p>
 * 
 * <p>
 * For efficiency, the stack of ByteProcessor objects corresponding to the image
 * is stored internally as short arrays, thus avoiding conversion induced by the
 * ImageStack object.
 * </p>
 * 
 * @see GeodesicReconstruction3DHybrid0Gray8
 * @see GeodesicReconstruction3DHybridFloat
 * 
 * @author David Legland
 * 
 */
public class GeodesicReconstruction3DHybrid0Gray16 extends GeodesicReconstruction3DAlgoStub
{
	GeodesicReconstructionType reconstructionType = GeodesicReconstructionType.BY_DILATION;
	
	ImageStack markerStack;
	ImageStack maskStack;
	ImageStack resultStack;
	
	short[][] markerSlices;
	short[][] maskSlices;
	short[][] resultSlices;
	
	/** image width */
	int sizeX = 0;
	/** image height */
	int sizeY = 0;
	/** image depth */
	int sizeZ = 0;

	/** the queue containing the positions that need update */
	Deque<Cursor3D> queue;
	
	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * using the default connectivity 6.
	 */
	public GeodesicReconstruction3DHybrid0Gray16() 
	{
	}
	
	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * that specifies the type of reconstruction, and using the connectivity 6.
	 * 
	 * @param type
	 *            the type of reconstruction (erosion or dilation)
	 */
	public GeodesicReconstruction3DHybrid0Gray16(GeodesicReconstructionType type)
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
	public GeodesicReconstruction3DHybrid0Gray16(GeodesicReconstructionType type, int connectivity)
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
	public GeodesicReconstruction3DHybrid0Gray16(int connectivity) 
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
	public void setReconstructionType(GeodesicReconstructionType reconstructionType) 
	{
		this.reconstructionType = reconstructionType;
	}

	/**
	 * Run the reconstruction by dilation algorithm using the images specified
	 * as argument.
	 */
	public ImageStack applyTo(ImageStack marker, ImageStack mask) 
	{
		// Check bit depth of input images
		if (marker.getBitDepth() != 16 || mask.getBitDepth() != 16) 
		{
			throw new IllegalArgumentException("Requires both marker and mask images to have 16-bits depth");
		}
		
		// Keep references to input images
		this.markerStack = marker;
		this.maskStack = mask;

		// convert to image processors
		this.markerSlices = getShortProcessors(marker);
		this.maskSlices = getShortProcessors(mask);
		
		// Check sizes are consistent
		this.sizeX 	= marker.getWidth();
		this.sizeY 	= marker.getHeight();
		this.sizeZ 	= marker.getSize();
		if (sizeX != mask.getWidth() || sizeY != mask.getHeight() || sizeZ != mask.getSize()) 
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

		queue = new ArrayDeque<Cursor3D>();
		
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
		trace("Backward iteration & Init Queue");
		showStatus("Geod. Rec. Bwd ");
		
		backwardScanInitQueue();
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
		this.resultStack = ImageStack.create(sizeX, sizeY, sizeZ, markerStack.getBitDepth());
		this.resultSlices = getShortProcessors(this.resultStack);

		short[] markerSlice, maskSlice, resultSlice;
		
		if (this.reconstructionType == GeodesicReconstructionType.BY_DILATION)
		{
			// Initialize integer result stack
			for (int z = 0; z < sizeZ; z++)
			{
				// Extract slices
				markerSlice = this.markerSlices[z];
				maskSlice = this.maskSlices[z];
				resultSlice = this.resultSlices[z];

				// process current slice
				for (int i = 0; i < sizeX * sizeY; i++)
				{
					int v1 = markerSlice[i] & 0x00FFFF;
					int v2 = maskSlice[i] & 0x00FFFF;
					resultSlice[i] = (short) min(v1, v2);
				}
			}
		} 
		else
		{
			// Initialize the result image with the maximum value of marker and mask
			// images
			for (int z = 0; z < sizeZ; z++)
			{
				// Extract slices
				markerSlice = this.markerSlices[z];
				maskSlice = this.maskSlices[z];
				resultSlice = this.resultSlices[z];
				
				// process current slice
				for (int i = 0; i < sizeX * sizeY; i++)
				{
					int v1 = markerSlice[i] & 0x00FFFF;
					int v2 = maskSlice[i] & 0x00FFFF;
					resultSlice[i] = (short) max(v1, v2);
				}
			}
		}
	}
	
	private static final short[][] getShortProcessors(ImageStack stack)
	{
		// Initialize result array
		int size = stack.getSize();
		short[][] slices = new short[size][];
		
		// Extract inner slice array and apply type conversion
		Object[] array = stack.getImageArray();
		for (int i = 0; i < size; i++)
		{
			slices[i] = (short[]) array[i];
		}
		
		// return slices
		return slices;
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
		int maxValue;
		
		short[] slice, maskSlice; 
		
		// Iterate over pixels
		for (int z = 0; z < sizeZ; z++)
		{
			showProgress(z, sizeZ);
			
			// Extract slices
			slice = this.resultSlices[z];
			maskSlice = this.maskSlices[z];

			// process current slice
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++)
				{
					int index = y * sizeX + x;
					int currentValue = (slice[index] & 0x00FFFF) * sign;
					maxValue = currentValue;
					
					// Iterate over the 3 'upper' neighbors of current pixel
					if (x > 0) 
						maxValue = max(maxValue, (slice[index - 1] & 0x00FFFF) * sign);
					if (y > 0) 
						maxValue = max(maxValue, (slice[index - sizeX] & 0x00FFFF) * sign);
					if (z > 0) 
						maxValue = max(maxValue, (resultSlices[z-1][index] & 0x00FFFF) * sign);
					
					// update value of current voxel
					maxValue = min(maxValue, (maskSlice[index] & 0x00FFFF) * sign);
					if (maxValue > currentValue) 
					{
						slice[index] = (short) (maxValue * sign);
					}
				}
			}
		} // end of pixel iteration
		
		showProgress(1, 1);
	}

	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency, assuming pixels are stored using integer data types.
	 */
	private void forwardScanC26() 
	{
		final int sign = this.reconstructionType.getSign();
		// the maximal value around current pixel
		int maxValue;

		short[] slice, slice2, maskSlice;
		
		// Iterate over pixels
		for (int z = 0; z < sizeZ; z++)
		{
			showProgress(z, sizeZ, "z = " + z);
			
			// Extract slices
			maskSlice = this.maskSlices[z];
			slice = this.resultSlices[z];

			// process current slice
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					int index = y * sizeX + x;
					int currentValue = (slice[index] & 0x00FFFF) * sign;
					maxValue = currentValue;

					// Iterate over neighbors of current pixel
					int zmax = min(z + 1, sizeZ);
					for (int z2 = max(z - 1, 0); z2 < zmax; z2++)
					{
						slice2 = resultSlices[z2];
						int ymax = z2 == z ? y : min(y + 1, sizeY - 1);
						for (int y2 = max(y - 1, 0); y2 <= ymax; y2++)
						{
							int xmax = (z2 == z && y2 == y) ? x - 1 : min(
									x + 1, sizeX - 1);
							for (int x2 = max(x - 1, 0); x2 <= xmax; x2++)
							{
								maxValue = max(maxValue, (slice2[y2 * sizeX + x2] & 0x00FFFF) * sign);
							}
						}
					}

					// update value of current voxel
					maxValue = min(maxValue, (maskSlice[index] & 0x00FFFF) * sign);
					if (maxValue > currentValue) 
					{
						slice[index] = (short) (maxValue * sign);
					}
				}
			}
		}
		
		showProgress(1, 1);
	}


	private void backwardScanInitQueue() 
	{
		if (this.connectivity == 6) 
		{
			backwardScanInitQueueC6();
		} 
		else 
		{
			backwardScanInitQueueC26();
		}
	}
	
	/**
	 * Update result image using pixels in the lower right neighborhood, using
	 * the 6-adjacency.
	 */
	private void backwardScanInitQueueC6() 
	{
		final int sign = this.reconstructionType.getSign();
		// the maximal value around current pixel
		int maxValue;

		short[] slice, maskSlice; 
		
		// Iterate over voxels
		for (int z = sizeZ - 1; z >= 0; z--) 
		{
			showProgress(sizeZ - 1 - z, sizeZ, "z = " + z);
			
			// Extract slices
			slice = this.resultSlices[z];
			maskSlice = this.maskSlices[z];
			
			// process current slice
			for (int y = sizeY - 1; y >= 0; y--) 
			{
				for (int x = sizeX - 1; x >= 0; x--) 
				{
					int index = y * sizeX + x;
					int currentValue = (slice[index] & 0x00FFFF) * sign;
					maxValue = currentValue;
					
					// Iterate over the 3 'lower' neighbors of current voxel
					if (x < sizeX - 1) 
						maxValue = max(maxValue, (slice[index + 1] & 0x00FFFF) * sign);
					if (y < sizeY - 1) 
						maxValue = max(maxValue, (slice[index + sizeX] & 0x00FFFF) * sign);
					if (z < sizeZ - 1)
						maxValue = max(maxValue, (resultSlices[z+1][index] & 0x00FFFF) * sign);
				
					// combine with mask
					maxValue = min(maxValue, (maskSlice[index] & 0x00FFFF) * sign);
					
					// check if modification is required
					if (maxValue <= currentValue) 
						continue;

					// update value of current voxel
					slice[index] = (short) (maxValue * sign);
					
					// eventually add lower-right neighbors to queue
					if (x < sizeX - 1) 
						updateQueue(x + 1, y, z, maxValue, sign);
					if (y < sizeY - 1) 
						updateQueue(x, y + 1, z, maxValue, sign);
					if (z < sizeZ - 1) {
						updateQueue(x, y, z + 1, maxValue, sign);
					}
				}
			}
		}

		showProgress(1, 1);
	}
	
	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency.
	 */
	private void backwardScanInitQueueC26() 
	{
		final int sign = this.reconstructionType.getSign();
		// the maximal value around current pixel
		int maxValue;
	
		short[] slice, maskSlice;
		
		// Iterate over voxels
		for (int z = sizeZ - 1; z >= 0; z--)
		{
			showProgress(sizeZ - 1 - z, sizeZ, "z = " + z);
			
			// Extract slices
			maskSlice = this.maskSlices[z];
			slice = this.resultSlices[z];

			// process current slice
			for (int y = sizeY - 1; y >= 0; y--) 
			{
				for (int x = sizeX - 1; x >= 0; x--) 
				{
					int index = y * sizeX + x;
					int currentValue = (slice[index] & 0x00FFFF) * sign;
					maxValue = currentValue;
	
					// Iterate over neighbors of current voxel
					for (int z2 = min(z + 1, sizeZ - 1); z2 >= z; z2--)
					{
						short[] slice2 = this.resultSlices[z2];
						
						int ymin = z2 == z ? y : max(y - 1, 0); 
						for (int y2 = min(y + 1, sizeY - 1); y2 >= ymin; y2--)
						{
							int xmin = (z2 == z && y2 == y) ? x : max(x - 1, 0); 
							for (int x2 = min(x + 1, sizeX - 1); x2 >= xmin; x2--)
							{
								maxValue = max(maxValue, (slice2[y2 * sizeX + x2] & 0x00FFFF) * sign);
							}
						}
					}
	
					// combine with mask
					maxValue = min(maxValue, (maskSlice[index] & 0x00FFFF) * sign);
					
					// check if modification is required
					if (maxValue <= currentValue) 
						continue;

					// update value of current voxel
					slice[index] = (short) (maxValue * sign);
					
					// eventually add lower-right neighbors to queue
					for (int z2 = min(z + 1, sizeZ - 1); z2 >= z; z2--)
					{
						int ymin = z2 == z ? y : max(y - 1, 0); 
						for (int y2 = min(y + 1, sizeY - 1); y2 >= ymin; y2--) 
						{
							int xmin = (z2 == z && y2 == y) ? x : max(x - 1, 0); 
							for (int x2 = min(x + 1, sizeX - 1); x2 >= xmin; x2--)
							{
								updateQueue(x2, y2, z2, maxValue, sign);
							}
						}
					}
				}
			}
		}
		
		showProgress(1, 1);
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
		int value;
		
		while (!queue.isEmpty()) 
		{
			Cursor3D p = queue.removeFirst();
			int x = p.getX();
			int y = p.getY();
			int z = p.getZ();
			short[] slice = resultSlices[z];
			int index = y * sizeX + x;
			value = (slice[index] & 0x00FFFF) * sign;
			
			// compare with each one of the neighbors
			if (x > 0) 
				value = max(value, (slice[index - 1] & 0x00FFFF) * sign);
			if (x < sizeX - 1) 
				value = max(value, (slice[index + 1] & 0x00FFFF) * sign);
			if (y > 0) 
				value = max(value, (slice[index - sizeX] & 0x00FFFF) * sign);
			if (y < sizeY - 1) 
				value = max(value, (slice[index + sizeX] & 0x00FFFF) * sign);
			if (z > 0) 
				value = max(value, (resultSlices[z - 1][index] & 0x00FFFF) * sign);
			if (z < sizeZ - 1) 
				value = max(value, (resultSlices[z + 1][index] & 0x00FFFF) * sign);

			// bound with mask value
			value = min(value, (maskSlices[z][index] & 0x00FFFF) * sign);
			
			// if no update is needed, continue to next item in queue
			if (value <= (slice[index] & 0x00FFFF) * sign) 
				continue;
			
			// update result for current position
			slice[index] = (short) (value * sign);

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
		int value;
		
		while (!queue.isEmpty()) 
		{
			Cursor3D p = queue.removeFirst();
			int x = p.getX();
			int y = p.getY();
			int z = p.getZ();
			short[] slice = resultSlices[z];
			int index = y * sizeX + x;
			value = (slice[index] & 0x00FFFF) * sign;
			
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
				short[] slice2 = resultSlices[z2];
				for (int y2 = ymin; y2 <= ymax; y2++) 
				{
					for (int x2 = xmin; x2 <= xmax; x2++) {
						value = max(value, (slice2[y2 * sizeX + x2] & 0x00FFFF) * sign);
					}
				}
			}
			
			// bound with mask value
			value = min(value, (maskSlices[z][index] & 0x00FFFF) * sign);
			
			// if no update is needed, continue to next item in queue
			if (value <= (slice[index] & 0x00FFFF) * sign) 
				continue;
			
			// update result for current position
			slice[index] = (short) (value * sign);

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
	 * 
	 * @param i
	 *            column index
	 * @param j
	 *            row index
	 * @param value
	 *            value at (i,j) position
	 * @param sign
	 *            integer +1 or -1 to manage both erosions and dilations
	 */
	private void updateQueue(int i, int j, int k, int value, int sign)
	{
		// update current value only if value is strictly greater
		int maskValue = (maskSlices[k][sizeX * j + i] & 0x00FFFF) * sign;
		value = Math.min(value, maskValue);
		
		int resultValue = (resultSlices[k][sizeX * j + i] & 0x00FFFF) * sign; 
		if (value > resultValue) 
		{
			Cursor3D position = new Cursor3D(i, j, k);
			queue.add(position);
		}
	}

}
