/**
 * 
 */
package inra.ijpb.morphology.geodrec;

import static java.lang.Math.max;
import static java.lang.Math.min;

import ij.ImageStack;
import inra.ijpb.data.Connectivity3D;
import inra.ijpb.data.Cursor3D;
import inra.ijpb.data.image.Images3D;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;


/**
 * <p>
 * Geodesic reconstruction for 3D stacks of bytes, using hybrid algorithm. This
 * class manages both reconstructions by dilation and erosion.
 * </p>
 * 
 * <p>
 * This version first performs forward scan, then performs a backward scan that
 * also add lower-right neighbors to the queue, and finally processes voxels in
 * the queue. It is intended to work on 8 bits 3D images, using any type of connectivity.
 * </p>
 * 
 * <p>
 * For efficiency, the stack of ByteProcessor objects corresponding to the image
 * is stored internally as byte arrays, thus avoiding conversion induced by the
 * ImageStack object.
 * </p>
 * 
 * @see GeodesicReconstruction3DHybrid0Gray8
 * @see GeodesicReconstruction3DHybrid0Gray16
 * @see GeodesicReconstruction3DHybrid0Float
 * 
 * @author David Legland
 * 
 */
public class GeodesicReconstruction3DHybrid0ConnGray8 extends GeodesicReconstruction3DAlgoStub
{
	// ==================================================
	// Class variables
	
	GeodesicReconstructionType reconstructionType = GeodesicReconstructionType.BY_DILATION;
	
	Connectivity3D connectivity = Connectivity3D.C6;
	
	ImageStack markerStack;
	ImageStack maskStack;
	ImageStack resultStack;
	
	byte[][] markerSlices;
	byte[][] maskSlices;
	byte[][] resultSlices;
	
	/** image width */
	int sizeX = 0;
	/** image height */
	int sizeY = 0;
	/** image depth */
	int sizeZ = 0;

	/** the queue containing the positions that need update */
	Deque<Cursor3D> queue;
	
	final static Cursor3D currentOffset = new Cursor3D( 0, 0, 0 );
	
	// ==================================================
	// Constructors
	
	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * using the default connectivity 6.
	 */
	public GeodesicReconstruction3DHybrid0ConnGray8() 
	{
	}
	
	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * that specifies the type of reconstruction, and using the connectivity 6.
	 * 
	 * @param type
	 *            the type of reconstruction (erosion or dilation)
	 */
	public GeodesicReconstruction3DHybrid0ConnGray8(GeodesicReconstructionType type)
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
	public GeodesicReconstruction3DHybrid0ConnGray8(GeodesicReconstructionType type, int connectivity)
	{
		this.reconstructionType = type;
		setConnectivity(connectivity);
	}

	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * that specifies the connectivity to use.
	 * 
	 * @param connectivity
	 *            the 3D connectivity to use (either 6 or 26)
	 */
	public GeodesicReconstruction3DHybrid0ConnGray8(int connectivity) 
	{
		setConnectivity(connectivity);
	}

	
	// ==================================================
	// Accessors and mutators
	
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

	public void setConnectivity(int conn)
	{
		switch(conn)
		{
		case 6:
			this.connectivity = Connectivity3D.C6;
			break;
		case 26:
			this.connectivity = Connectivity3D.C26;
			break;
		default:
			throw new IllegalArgumentException("Connectivity must be either 6 or 26");
		}
	}
	
	// ==================================================
	// Implementation of GeodesicReconstruction3D interface
	
	/**
	 * Run the reconstruction by dilation algorithm using the images specified
	 * as argument.
	 */
	public ImageStack applyTo(ImageStack marker, ImageStack mask) 
	{
		// Check bit depth of input images
		if (marker.getBitDepth() != 8 || mask.getBitDepth() != 8) 
		{
			throw new IllegalArgumentException("Requires both marker and mask images to have 8-bits depth");
		}
		
		// Keep references to input images
		this.markerStack = marker;
		this.maskStack = mask;

		// convert to image processors
		this.markerSlices = getByteProcessors(marker);
		this.maskSlices = getByteProcessors(mask);
		
		// Check sizes are consistent
		this.sizeX 	= marker.getWidth();
		this.sizeY 	= marker.getHeight();
		this.sizeZ 	= marker.getSize();
		if (!Images3D.isSameSize(marker, mask)) 
		{
			throw new IllegalArgumentException("Marker and Mask images must have the same size");
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
		this.resultStack = ImageStack.create(sizeX, sizeY, sizeZ, maskStack.getBitDepth());
		this.resultSlices = getByteProcessors(this.resultStack);

		byte[] markerSlice, maskSlice, resultSlice;
		
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
					int v1 = markerSlice[i] & 0x00FF;
					int v2 = maskSlice[i] & 0x00FF;
					resultSlice[i] = (byte) min(v1, v2);
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
					int v1 = markerSlice[i] & 0x00FF;
					int v2 = maskSlice[i] & 0x00FF;
					resultSlice[i] = (byte) max(v1, v2);
				}
			}
		}
	}
	
	private static final byte[][] getByteProcessors(ImageStack stack)
	{
		// Initialize result array
		int size = stack.getSize();
		byte[][] slices = new byte[size][];
		
		// Extract inner slice array and apply type conversion
		Object[] array = stack.getImageArray();
		for (int i = 0; i < size; i++)
		{
			slices[i] = (byte[]) array[i];
		}
		
		// return slices
		return slices;
	}
	
	private void forwardScan() 
	{
		final int sign = this.reconstructionType.getSign();
		
		Collection<Cursor3D> offsets = getForwardOffsets(this.connectivity);
		
		// the maximal value around current pixel
		int maxValue;
		
		byte[] slice, maskSlice; 
		
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
					int currentValue = (slice[index] & 0x00FF) * sign;
					maxValue = currentValue;
					
					// iterate over neighbors
					for (Cursor3D offset : offsets)
					{
						int x2 = x + offset.getX();
						int y2 = y + offset.getY();
						int z2 = z + offset.getZ();
						if (x2 >= 0 && x2 < sizeX && y2 >= 0 && y2 < sizeY && z2 >= 0 && z2 < sizeZ)
						{
							int index2 = y2 * sizeX + x2;
							maxValue = max(maxValue, (resultSlices[z2][index2] & 0x00FF) * sign);
						}
					}
					
					// update value of current voxel
					maxValue = min(maxValue, (maskSlice[index] & 0x00FF) * sign);
					if (maxValue > currentValue) 
					{
						slice[index] = (byte) (maxValue * sign);
					}
				}
			}
		} // end of voxel iteration
		
		showProgress(1, 1);
	}

	private final static Collection<Cursor3D> getForwardOffsets(Connectivity3D connectivity)
	{
		ArrayList<Cursor3D> offsets = new ArrayList<Cursor3D>();
		for (Cursor3D cursor : connectivity.getOffsets3D())
		{
			if (cursor.getZ() < 0)
			{
				offsets.add(cursor);
				continue;
			}
			
			if (cursor.getZ() > 0)
			{
				continue;
			}

			if (cursor.getY() < 0)
			{
				offsets.add(cursor);
				continue;
			}
			
			if (cursor.getY() > 0)
			{
				continue;
			}
		
			if (cursor.getX() < 0)
			{
				offsets.add(cursor);
				continue;
			}
		}

		// sort in raster order
		Comparator<Cursor3D> comparator = new Comparator<Cursor3D>() {
			public int compare( Cursor3D c1, Cursor3D c2 ) {

				final int difX = Integer.compare( c1.getX(), c2.getX() );
				final int difY = Integer.compare( c1.getY(), c2.getY() );
				final int difZ = Integer.compare( c1.getZ(), c2.getZ() );

				if( difZ != 0 )
					return difZ;
				if( difY != 0 )
					return difY;
				else
					return difX;
			}
		};

		Collections.sort( offsets, comparator );
		return offsets;
	}
	


	private void backwardScanInitQueue() 
	{
		final int sign = this.reconstructionType.getSign();

		Collection<Cursor3D> offsets = getBackwardOffsets(this.connectivity);
		
		Collection<Cursor3D> offsetsUcurrent = new ArrayList<Cursor3D>();
		offsetsUcurrent.addAll( offsets );
		offsetsUcurrent.add( currentOffset );

		// the maximal value around current pixel
		int maxValue;

		byte[] slice, maskSlice; 
		
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
					int currentValue = (slice[index] & 0x00FF) * sign;
					maxValue = currentValue;

					// iterate over neighbors and current voxel
					for (Cursor3D offset : offsetsUcurrent)
					{
						int x2 = x + offset.getX();
						int y2 = y + offset.getY();
						int z2 = z + offset.getZ();
						if (x2 >= 0 && x2 < sizeX && y2 >= 0 && y2 < sizeY && z2 >= 0 && z2 < sizeZ)
						{
							int index2 = y2 * sizeX + x2;
							maxValue = max(maxValue, (resultSlices[z2][index2] & 0x00FF) * sign);
						}
					}
					
					// combine with mask
					maxValue = min(maxValue, (maskSlice[index] & 0x00FF) * sign);
					
					// check if modification is required
					if (maxValue <= currentValue) 
						continue;

					// update value of current voxel
					slice[index] = (byte) (maxValue * sign);
					
					// eventually add lower-right neighbors to the queue
					for (Cursor3D offset : offsets)
					{
						int x2 = x + offset.getX();
						int y2 = y + offset.getY();
						int z2 = z + offset.getZ();
						if (x2 >= 0 && x2 < sizeX && y2 >= 0 && y2 < sizeY && z2 >= 0 && z2 < sizeZ)
						{
							updateQueue(x2, y2, z2, maxValue, sign);
						}
					}
				}
			}
		}

		showProgress(1, 1);
	}
	
	private final static Collection<Cursor3D> getBackwardOffsets(Connectivity3D connectivity)
	{
		ArrayList<Cursor3D> offsets = new ArrayList<Cursor3D>();
		for (Cursor3D cursor : connectivity.getOffsets3D())
		{
			if (cursor.getZ() > 0)
			{
				offsets.add(cursor);
				continue;
			}
			
			if (cursor.getZ() < 0)
			{
				continue;
			}

			if (cursor.getY() > 0)
			{
				offsets.add(cursor);
				continue;
			}
			
			if (cursor.getY() < 0)
			{
				continue;
			}
		
			if (cursor.getX() > 0)
			{
				offsets.add(cursor);
				continue;
			}
		}

		// sort in anti-raster order
		Comparator<Cursor3D> comparator = new Comparator<Cursor3D>() {
			public int compare( Cursor3D c1, Cursor3D c2 ) {

				final int difX = Integer.compare( c1.getX(), c2.getX() );
				final int difY = Integer.compare( c1.getY(), c2.getY() );
				final int difZ = Integer.compare( c1.getZ(), c2.getZ() );

				if( difZ != 0 )
					return difZ;
				if( difY != 0 )
					return difY;
				else
					return difX;
			}
		};

		Collections.sort( offsets, comparator );
		Collections.reverse( offsets );
		return offsets;
	}


	
	private void processQueue()
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
			byte[] slice = resultSlices[z];
			int index = y * sizeX + x;
			value = (slice[index] & 0x00FF) * sign;
			
			// compare with each one of the neighbors
			for (Cursor3D neighbor : connectivity.getNeighbors(p))
			{
				int x2 = neighbor.getX();
				int y2 = neighbor.getY();
				int z2 = neighbor.getZ();
				if (x2 >= 0 && x2 < sizeX && y2 >= 0 && y2 < sizeY && z2 >= 0 && z2 < sizeZ)
				{
					int index2 = y2 * sizeX + x2;
					value = max(value, (resultSlices[z2][index2] & 0x00FF) * sign);
				}
			}

			// bound with mask value
			value = min(value, (maskSlices[z][index] & 0x00FF) * sign);
			
			// if no update is needed, continue with next item in the queue
			if (value <= (slice[index] & 0x00FF) * sign) 
				continue;
			
			// update result for current position
			slice[index] = (byte) (value * sign);

			// Eventually add each neighbor
			for (Cursor3D neighbor : connectivity.getNeighbors(p))
			{
				int x2 = neighbor.getX();
				int y2 = neighbor.getY();
				int z2 = neighbor.getZ();
				if (x2 >= 0 && x2 < sizeX && y2 >= 0 && y2 < sizeY && z2 >= 0 && z2 < sizeZ)
				{
					updateQueue(x2, y2, z2, value, sign);
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
	private void updateQueue(int i, int j, int k, int value, int sign)
	{
		// update current value only if value is strictly greater
		int maskValue = (maskSlices[k][sizeX * j + i] & 0x00FF) * sign;
		value = Math.min(value, maskValue);
		
		int resultValue = (resultSlices[k][sizeX * j + i] & 0x00FF) * sign; 
		if (value > resultValue) 
		{
			Cursor3D position = new Cursor3D(i, j, k);
			queue.add(position);
		}
	}

}
