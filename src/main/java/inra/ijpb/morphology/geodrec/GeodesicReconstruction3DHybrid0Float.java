/**
 * 
 */
package inra.ijpb.morphology.geodrec;

import static java.lang.Math.max;
import static java.lang.Math.min;
import ij.IJ;
import ij.ImageStack;
import inra.ijpb.data.Cursor3D;
import inra.ijpb.data.image.Images3D;
import inra.ijpb.event.AlgoStub;
import inra.ijpb.morphology.GeodesicReconstruction3D;

import java.util.ArrayDeque;
import java.util.Deque;


/**
 * Geodesic reconstruction for 3D stacks using hybrid algorithm. This class
 * manages both reconstructions by dilation and erosion.
 * 
 * This version first performs forward scan, then performs a backward scan that
 * also add lower-right neighbors to the queue, and finally processes pixels in
 * the queue. It is intended to work on float 3D images, using 6 or 26
 * adjacencies.
 * 
 * For efficiency, the stack of ByteProcessor objects corresponding to the
 * image is stored internally, thus avoiding conversion induced by the 
 * ImageStack object.  
 * 
 * @author David Legland
 * 
 */
public class GeodesicReconstruction3DHybrid0Float extends AlgoStub implements
		GeodesicReconstruction3DAlgo {

	GeodesicReconstructionType reconstructionType = GeodesicReconstructionType.BY_DILATION;
	
	int connectivity = 6;
	
	ImageStack markerStack;
	ImageStack maskStack;
	ImageStack resultStack;
	
	float[][] markerSlices;
	float[][] maskSlices;
	float[][] resultSlices;
	
	/** image width */
	int sizeX = 0;
	/** image height */
	int sizeY = 0;
	/** image depth */
	int sizeZ = 0;

	/** the queue containing the positions that need update */
	Deque<Cursor3D> queue;
	
	/**
	 * 
	 * boolean flag for toggling the display of debugging infos.
	 */
	public boolean verbose = false;
	
	public boolean showStatus = true; 
	public boolean showProgress = false; 

	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * using the default connectivity 6.
	 */
	public GeodesicReconstruction3DHybrid0Float() {
	}
	
	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * that specifies the type of reconstruction, and using the connectivity 6.
	 */
	public GeodesicReconstruction3DHybrid0Float(GeodesicReconstructionType type) {
		this.reconstructionType = type;
	}

	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * that specifies the type of reconstruction, and the connectivity to use.
	 */
	public GeodesicReconstruction3DHybrid0Float(GeodesicReconstructionType type, int connectivity) {
		this.reconstructionType = type;
		this.connectivity = connectivity;
	}

	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * that specifies the connectivity to use.
	 */
	public GeodesicReconstruction3DHybrid0Float(int connectivity) {
		this.connectivity = connectivity;
	}

	/**
	 * @return the reconstructionType
	 */
	public GeodesicReconstructionType getReconstructionType() {
		return reconstructionType;
	}

	/**
	 * @param reconstructionType the reconstructionType to set
	 */
	public void setReconstructionType(GeodesicReconstructionType reconstructionType) {
		this.reconstructionType = reconstructionType;
	}

	public int getConnectivity() {
		return this.connectivity;
	}
	
	public void setConnectivity(int conn) {
		this.connectivity = conn;
	}


	/**
	 * Run the reconstruction by dilation algorithm using the images specified
	 * as argument.
	 */
	public ImageStack applyTo(ImageStack marker, ImageStack mask) {
		// Check bit depth of input images
		if (marker.getBitDepth() != 32 || mask.getBitDepth() != 32) 
		{
			throw new IllegalArgumentException("Requires both marker and mask images to have 8-bits depth");
		}
		
		// Keep references to input images
		this.markerStack = marker;
		this.maskStack = mask;

		// convert to image processors
		this.markerSlices = getFloatProcessors(marker);
		this.maskSlices = getFloatProcessors(mask);
		
		// Check sizes are consistent
		this.sizeX 	= marker.getWidth();
		this.sizeY 	= marker.getHeight();
		this.sizeZ 	= marker.getSize();
		if (sizeX != mask.getWidth() || sizeY != mask.getHeight() || sizeZ != mask.getSize()) {
			throw new IllegalArgumentException("Marker and Mask images must have the same size");
		}
		
		// Check connectivity has a correct value
		if (connectivity != 6 && connectivity != 26) {
			throw new RuntimeException(
					"Connectivity for stacks must be either 6 or 26, not "
							+ connectivity);
		}

		queue = new ArrayDeque<Cursor3D>();
		
		long t0 = System.currentTimeMillis();
		if (verbose) {
			System.out.print("Initialize result ");
		}
		initializeResult();
		if (verbose) {
			long t1 = System.currentTimeMillis();
			System.out.println((t1 - t0) + " ms");
			t0 = t1;
		}

		
		// Display current status
		if (verbose) {
			System.out.print("Forward iteration ");
		}
		if (showStatus) {
			IJ.showStatus("Geod. Rec. by Dil. Fwd ");
		}

		forwardScan();
		if (verbose) {
//			printStack(this.resultStack);
			
			long t1 = System.currentTimeMillis();
			System.out.println((t1 - t0) + " ms");
			t0 = t1;
		}


		// Display current status
		if (verbose) {
			System.out.print("Backward iteration & Init Queue");
		}
		if (showStatus) {
			IJ.showStatus("Geod. Rec. by Dil. Bwd ");
		}
		backwardScanInitQueue();
		if (verbose) {
//			printStack(this.resultStack);

			long t1 = System.currentTimeMillis();
			System.out.println((t1 - t0) + " ms");
			t0 = t1;
		}
		
		// Display current status
		if (verbose) {
			System.out.print("Process queue");
		}
		if (showStatus) {
			IJ.showStatus("Process queue");
		}

		processQueue();
		if (verbose) {
//			printStack(this.resultStack);

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
	private void initializeResult() {
		// Create result image the same size as marker image
		this.resultStack = ImageStack.create(sizeX, sizeY, sizeZ, markerStack.getBitDepth());
		this.resultSlices = getFloatProcessors(this.resultStack);

		float[] markerSlice, maskSlice, resultSlice;
		
		if (this.reconstructionType == GeodesicReconstructionType.BY_DILATION) {
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
					float v1 = markerSlice[i];
					float v2 = maskSlice[i];
					resultSlice[i] = min(v1, v2);
				}
			}
		} else {
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
					float v1 = markerSlice[i];
					float v2 = maskSlice[i];
					resultSlice[i] = max(v1, v2);
				}
			}
		}
	}
	
	private static final float[][] getFloatProcessors(ImageStack stack)
	{
		// Initialize result array
		int size = stack.getSize();
		float[][] slices = new float[size][];
		
		// Extract inner slice array and apply type conversion
		Object[] array = stack.getImageArray();
		for (int i = 0; i < size; i++)
		{
			slices[i] = (float[]) array[i];
		}
		
		// return slices
		return slices;
	}
	
	private void forwardScan() {
		if (this.connectivity == 6) {
			forwardScanC6();
		} else {
			forwardScanC26();
		}
	}

	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 6-adjacency, assuming pixels are stored in bytes.
	 */
	private void forwardScanC6() {
		final int sign = this.reconstructionType.getSign();
		
		// the maximal value around current pixel
		float maxValue;

		if (showProgress) {
			IJ.showProgress(0, sizeZ);
		}
		
		float[] slice, maskSlice; 
		
		// Iterate over pixels
		for (int z = 0; z < sizeZ; z++) {
			if (showProgress) {
				IJ.showProgress(z + 1, sizeZ);
			}
			
			// Extract slices
			slice = this.resultSlices[z];
			maskSlice = this.maskSlices[z];

			// process current slice
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					int index = y * sizeX + x;
					float currentValue = slice[index] * sign;
					maxValue = currentValue;
					
					// Iterate over the 3 'upper' neighbors of current pixel
					if (x > 0) 
						maxValue = max(maxValue, slice[index - 1] * sign);
					if (y > 0) 
						maxValue = max(maxValue, slice[index - sizeX] * sign);
					if (z > 0) 
						maxValue = max(maxValue, resultSlices[z-1][index] * sign);
					
					// update value of current voxel
					maxValue = min(maxValue, maskSlice[index] * sign);
					if (maxValue > currentValue) {
						slice[index] = maxValue * sign;
					}
				}
			}
		} // end of pixel iteration
	}

	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency, assuming pixels are stored using integer data types.
	 */
	private void forwardScanC26() {
		final int sign = this.reconstructionType.getSign();
		// the maximal value around current pixel
		float maxValue;

		if (showProgress) {
			IJ.showProgress(0, sizeZ);
		}

		float[] slice, slice2, maskSlice;
		
		// Iterate over pixels
		for (int z = 0; z < sizeZ; z++) {
			if (showProgress) {
				IJ.showProgress(z + 1, sizeZ);
				System.out.println("z = " + z);
			}

			// Extract slices
			maskSlice = this.maskSlices[z];
			slice = this.resultSlices[z];

			// process current slice
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					int index = y * sizeX + x;
					float currentValue = slice[index] * sign;
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
								maxValue = max(maxValue,
										slice2[y2 * sizeX + x2] * sign);
							}
						}
					}

					// update value of current voxel
					maxValue = min(maxValue, maskSlice[index] * sign);
					if (maxValue > currentValue) {
						slice[index] = maxValue * sign;
					}
				}
			}
		}
	}


	private void backwardScanInitQueue() {
		if (this.connectivity == 6) {
			backwardScanInitQueueC6();
		} else {
			backwardScanInitQueueC26();
		}
	}
	/**
	 * Update result image using pixels in the lower right neighborhood, using
	 * the 6-adjacency.
	 */
	private void backwardScanInitQueueC6() {
		final int sign = this.reconstructionType.getSign();
		// the maximal value around current pixel
		float maxValue;

		if (showProgress) {
			IJ.showProgress(0, sizeZ);
		}

		float[] slice, maskSlice; 
		
		// Iterate over voxels
		for (int z = sizeZ - 1; z >= 0; z--) {
			if (showProgress) {
				IJ.showProgress(sizeZ - z, sizeZ);
				System.out.println("z = " + z);
			}

			// Extract slices
			slice = this.resultSlices[z];
			maskSlice = this.maskSlices[z];
			
			// process current slice
			for (int y = sizeY - 1; y >= 0; y--) {
				for (int x = sizeX - 1; x >= 0; x--) {
					int index = y * sizeX + x;
					float currentValue = slice[index] * sign;
					maxValue = currentValue;
					
					// Iterate over the 3 'lower' neighbors of current voxel
					if (x < sizeX - 1) 
						maxValue = max(maxValue, slice[index + 1] * sign);
					if (y < sizeY - 1) 
						maxValue = max(maxValue, slice[index + sizeX] * sign);
					if (z < sizeZ - 1)
						maxValue = max(maxValue, resultSlices[z+1][index] * sign);
				
					// combine with mask
					maxValue = min(maxValue, maskSlice[index] * sign);
					
					// check if modification is required
					if (maxValue <= currentValue) 
						continue;

					// update value of current voxel
					slice[index] = maxValue * sign;
					
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
	}
	
	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency.
	 */
	private void backwardScanInitQueueC26() {
		final int sign = this.reconstructionType.getSign();
		// the maximal value around current pixel
		float maxValue;
	
		if (showProgress) {
			IJ.showProgress(0, sizeZ);
		}
	
		float[] slice, maskSlice;
		
		// Iterate over voxels
		for (int z = sizeZ - 1; z >= 0; z--) {
			if (showProgress) {
				IJ.showProgress(sizeZ - z, sizeZ);
				System.out.println("z = " + z);
			}
	
			// Extract slices
			maskSlice = this.maskSlices[z];
			slice = this.resultSlices[z];

			// process current slice
			for (int y = sizeY - 1; y >= 0; y--) {
				for (int x = sizeX - 1; x >= 0; x--) {
					int index = y * sizeX + x;
					float currentValue = slice[index] * sign;
					maxValue = currentValue;
	
					// Iterate over neighbors of current voxel
					for (int z2 = min(z + 1, sizeZ - 1); z2 >= z; z2--) {
	
						float[] slice2 = this.resultSlices[z2];
						
						int ymin = z2 == z ? y : max(y - 1, 0); 
						for (int y2 = min(y + 1, sizeY - 1); y2 >= ymin; y2--) {
							int xmin = (z2 == z && y2 == y) ? x : max(x - 1, 0); 
							for (int x2 = min(x + 1, sizeX - 1); x2 >= xmin; x2--) {
								maxValue = max(maxValue, slice2[y2 * sizeX + x2] * sign);
								}
						}
					}
	
					// combine with mask
					maxValue = min(maxValue, maskSlice[index] * sign);
					
					// check if modification is required
					if (maxValue <= currentValue) 
						continue;

					// update value of current voxel
					slice[index] = maxValue * sign;
					
					// eventually add lower-right neighbors to queue
					for (int z2 = min(z + 1, sizeZ - 1); z2 >= z; z2--) {
						int ymin = z2 == z ? y : max(y - 1, 0); 
						for (int y2 = min(y + 1, sizeY - 1); y2 >= ymin; y2--) {
							int xmin = (z2 == z && y2 == y) ? x : max(x - 1, 0); 
							for (int x2 = min(x + 1, sizeX - 1); x2 >= xmin; x2--) {
								updateQueue(x2, y2, z2, maxValue, sign);
							}
						}
					}
				}
			}
		}	
	}
	
	private void processQueue() {
		if (this.connectivity == 6) {
			processQueueC6();
		} else {
			processQueueC26();
		}
	}

	/**
	 * Update result image using next pixel in the queue,
	 * using the 6-adjacency.
	 */
	private void processQueueC6() {
		// sign for adapting dilation and erosion algorithms
		final int sign = this.reconstructionType.getSign();

		// the maximal value around current pixel
		float value;
		
		while (!queue.isEmpty()) {
			Cursor3D p = queue.removeFirst();
			int x = p.getX();
			int y = p.getY();
			int z = p.getZ();
			float[] slice = resultSlices[z];
			int index = y * sizeX + x;
			value = slice[index] * sign;
			
			// compare with each one of the neighbors
			if (x > 0) 
				value = max(value, slice[index - 1] * sign);
			if (x < sizeX - 1) 
				value = max(value, slice[index + 1] * sign);
			if (y > 0) 
				value = max(value, slice[index - sizeX] * sign);
			if (y < sizeY - 1) 
				value = max(value, slice[index + sizeX] * sign);
			if (z > 0) 
				value = max(value, resultSlices[z - 1][index] * sign);
			if (z < sizeZ - 1) 
				value = max(value, resultSlices[z + 1][index] * sign);

			// bound with mask value
			value = min(value, maskSlices[z][index]  * sign);
			
			// if no update is needed, continue to next item in queue
			if (value <= slice[index] * sign) 
				continue;
			
			// update result for current position
			slice[index] = value * sign;

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
	private void processQueueC26() {
		// sign for adapting dilation and erosion algorithms
		final int sign = this.reconstructionType.getSign();

		// the maximal value around current pixel
		float value;
		
//		System.out.println("start processing queue");
		
		while (!queue.isEmpty()) {
//			System.out.println("  queue size: " + queue.size());
			
			Cursor3D p = queue.removeFirst();
			int x = p.getX();
			int y = p.getY();
			int z = p.getZ();
			float[] slice = resultSlices[z];
			int index = y * sizeX + x;
			value = slice[index] * sign;
			
			// compute bounds of neighborhood
			int xmin = max(x - 1, 0);
			int xmax = min(x + 1, sizeX - 1);
			int ymin = max(y - 1, 0);
			int ymax = min(y + 1, sizeY - 1);
			int zmin = max(z - 1, 0);
			int zmax = min(z + 1, sizeZ - 1);

			// compare with each one of the neighbors
			for (int z2 = zmin; z2 <= zmax; z2++) {
				float[] slice2 = resultSlices[z2];
				for (int y2 = ymin; y2 <= ymax; y2++) {
					for (int x2 = xmin; x2 <= xmax; x2++) {
						value = max(value, slice2[y2 * sizeX + x2] * sign);
					}
				}
			}
			
			// bound with mask value
			value = min(value, maskSlices[z][index] * sign);
			
			// if no update is needed, continue to next item in queue
			if (value <= slice[index] * sign) 
				continue;
			
			// update result for current position
			slice[index] = value * sign;

			// compare with each one of the neighbors
			for (int z2 = zmin; z2 <= zmax; z2++) {
				for (int y2 = ymin; y2 <= ymax; y2++) {
					for (int x2 = xmin; x2 <= xmax; x2++) {
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
	private void updateQueue(int i, int j, int k, float value, int sign) {
		// update current value only if value is strictly greater
		float maskValue = maskSlices[k][sizeX * j + i] * sign;
		value = Math.min(value, maskValue);
		
		float resultValue = resultSlices[k][sizeX * j + i] * sign; 
		if (value > resultValue) {
			Cursor3D position = new Cursor3D(i, j, k);
			queue.add(position);
		}
	}

//	public static final void main(String[] arsg)
//	{
//		ImageJ ij = new ImageJ();
//		ij.setVisible(true);
//
//		File file = new File("src/test/resources/files/bat-cochlea-volume.tif");
//		System.out.println(file.getAbsolutePath());
//
//		ImagePlus imagePlus = IJ.openImage(file.getAbsolutePath());
//		if (imagePlus == null)
//		{
//			throw new RuntimeException("Could not read input image");
//		}
//		
//		
//		imagePlus.show();
//		
//		ImageStack mask = imagePlus.getStack();
//		int width = mask.getWidth();
//		int height = mask.getHeight();
//		int depth = mask.getSize();
//		int bitDepth = mask.getBitDepth();
//		ImageStack marker = ImageStack.create(width, height, depth, bitDepth);
//
//		marker.setVoxel(20, 80, 50, 255);
//
//		GeodesicReconstruction3DHybrid0Float algo = new GeodesicReconstruction3DHybrid0Float();
//		algo.setConnectivity(26);
////		algo.verbose = true;
//
//		long t0 = System.currentTimeMillis();
//		ImageStack result = algo.applyTo(marker, mask);
//		long t1 = System.currentTimeMillis();
//
//		double dt = (t1 - t0) / 1000.0;
//		System.out.println("Elapsed time: " + dt + " s");
//		
//		ImagePlus resultPlus = new ImagePlus("Result", result);
//		resultPlus.show();
//
//	}

	public static final void main(String[] args) 
	{
		ImageStack mask = createInvertedLeveledCubeGraphImage();

		ImageStack marker = mask.duplicate();
		Images3D.fill(marker, 255);
		marker.setVoxel(0, 0, 0, 0);
		
		System.out.println("=== mask ===");
		Images3D.print(mask);
		
		ImageStack result = GeodesicReconstruction3D.reconstructByErosion(marker, mask, 6);
		System.out.println("=== Result ===");
		Images3D.print(result);
		
		if (((int)result.getVoxel(0, 4, 0)) != 224) 
		{
			System.out.println("Wrong result!");
		}
	}
	
	private static final ImageStack createInvertedLeveledCubeGraphImage() {
		ImageStack stack = createCubeGraphImage();
		for (int z = 0; z < stack.getSize(); z++) {
			for (int y = 0; y < stack.getHeight(); y++) {
				for (int x = 0; x < stack.getWidth(); x++) {
					stack.setVoxel(x, y, z, 255 - stack.getVoxel(x, y, z));
				}
			}
		}
		stack.setVoxel(2, 0, 0,  32);
		stack.setVoxel(4, 2, 0,  64);
		stack.setVoxel(4, 4, 2,  96);
		stack.setVoxel(4, 2, 4, 128);
		stack.setVoxel(2, 0, 4, 160);
		stack.setVoxel(0, 2, 4, 192);
		stack.setVoxel(0, 4, 2, 224);

		return stack;
	}
	
	/**
	 * Creates a 3D image containing thin cube mesh.
	 */
	private static final ImageStack createCubeGraphImage() {
		int sizeX = 5;
		int sizeY = 5;
		int sizeZ = 5;
		int bitDepth = 8;
		
		// create empty stack
		ImageStack stack = ImageStack.create(sizeX, sizeY, sizeZ, bitDepth);
		
		// coordinates of the cube edges
		int x1 = 0;
		int x2 = 4;
		int y1 = 0;
		int y2 = 4;
		int z1 = 0;
		int z2 = 4;
		
		// First, the edges in the x direction
		for (int x = x1; x <= x2; x++) {
			stack.setVoxel(x, y1, z1, 255);
			stack.setVoxel(x, y1, z2, 255);
		}				
		
		// then, the edges in the y direction
		for (int y = y1; y <= y2; y++) {
			stack.setVoxel(x2, y, z1, 255);
			stack.setVoxel(x1, y, z2, 255);
			stack.setVoxel(x2, y, z2, 255);
		}				

		// Finally, the edges in the z direction
		for (int z = z1; z <= z2; z++) {
			stack.setVoxel(x1, y2, z, 255);
			stack.setVoxel(x2, y2, z, 255);
		}				
		
		return stack;
	}

}
