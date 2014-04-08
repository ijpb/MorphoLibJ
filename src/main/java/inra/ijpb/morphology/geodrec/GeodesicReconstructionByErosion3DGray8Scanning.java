/**
 * 
 */
package inra.ijpb.morphology.geodrec;

import static java.lang.Math.max;
import static java.lang.Math.min;
import ij.IJ;
import ij.ImageStack;


/**
 * Geodesic reconstruction by erosion for 3D stacks of byte processors.
 * @author David Legland
 *
 */
public class GeodesicReconstructionByErosion3DGray8Scanning implements GeodesicReconstruction3DAlgo {
	ImageStack marker;
	ImageStack mask;
	
	ImageStack result;
	
	/** image width */
	int sizeX = 0;
	/** image height */
	int sizeY = 0;
	/** image depth */
	int sizeZ = 0;

	int connectivity = 6;
	
	/**
	 * The flag indicating whether the result image has been modified during
	 * last image scan
	 */
	boolean modif;

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
	public GeodesicReconstructionByErosion3DGray8Scanning() {
	}
	
	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * that specifies the connectivity to use.
	 */
	public GeodesicReconstructionByErosion3DGray8Scanning(int connectivity) {
		this.connectivity = connectivity;
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
		
		if ( Thread.currentThread().isInterrupted() )					
			return null;
		
		// Keep references to input images
		this.marker = marker;
		this.mask = mask;
		
		// Check input image type
		if (marker.getBitDepth() != 8 || mask.getBitDepth() != 8) {
			throw new IllegalArgumentException("Marker and Mask images must be byte stacks");
		}
		
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

		// Create result image the same size as marker image
		this.result = ImageStack.create(sizeX, sizeY, sizeZ, marker.getBitDepth());

		// Initialize the result image with the minimum value of marker and mask
		// images
		initializeResult();

		// Count the number of iterations for eventually displaying progress
		int iter = 1;
		
		// Iterate forward and backward propagations until no more pixel have been modified
		do {
			
			if ( Thread.currentThread().isInterrupted() )					
				return null;
			
			modif = false;

			// Display current status
			if (verbose) {
				System.out.println("Forward iteration " + iter);
			}
			if (showStatus) {
				IJ.showStatus("Geod. Rec. by Ero. Fwd " + iter);
			}
			
			// forward iteration
			switch (connectivity) {
			case 6:
				forwardErosionC6();
				break;
			case 26:
				forwardErosionC26();
				break;
			}

			// Display current status
			if (verbose) {
				System.out.println("Backward iteration " + iter);
			}
			if (showStatus) {
				IJ.showStatus("Geod. Rec. by Ero. Bwd " + iter);
			}
			
			// backward iteration
			switch (connectivity) {
			case 6:
				backwardErosionC6();
				break;
			case 26:	
				backwardErosionC26(); 
				break;
			}

			iter++;			
		} while (modif);
	
		return this.result;
	}

	/**
	 * Initializes the result image with the maximum value of marker and mask
	 * images
	 */
	private void initializeResult() {
		// Create result image the same size as marker image
		this.result = ImageStack.create(sizeX, sizeY, sizeZ, marker.getBitDepth());

		// Data access objects
		Object[] stack = result.getImageArray();
		Object[] markerStack = marker.getImageArray();
		Object[] maskStack = mask.getImageArray();
		byte[] slice;
		byte[] markerSlice;
		byte[] maskSlice;

		// iterate over voxels of the stack
		for (int z = 0; z < sizeZ; z++) {
			slice = (byte[]) stack[z];
			maskSlice = (byte[]) maskStack[z];
			markerSlice = (byte[]) markerStack[z];
			
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					int index = y * sizeX + x;
					int value = max(markerSlice[index] & 0x00FF, maskSlice[index] & 0x00FF);
					slice[index] = (byte) value;
				}
			}
		}
	}
	
	/**
	 * Initializes the result image with the maximum value of marker and mask
	 * images
	 */
	private void initializeResult( ImageStack binaryMask ) {
		// Create result image the same size as marker image
		this.result = ImageStack.create(sizeX, sizeY, sizeZ, marker.getBitDepth());

		// Data access objects
		Object[] stack = result.getImageArray();
		Object[] markerStack = marker.getImageArray();
		Object[] maskStack = mask.getImageArray();
		byte[] slice;
		byte[] markerSlice;
		byte[] maskSlice;

		// iterate over voxels of the stack
		for (int z = 0; z < sizeZ; z++) {
			slice = (byte[]) stack[z];
			maskSlice = (byte[]) maskStack[z];
			markerSlice = (byte[]) markerStack[z];
			
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					if( binaryMask.getVoxel(x, y, z) != 0 )
					{
						int index = y * sizeX + x;
						int value = max(markerSlice[index] & 0x00FF, maskSlice[index] & 0x00FF);
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
	private void forwardErosionC6() {
		// the maximal value around current pixel
		int minValue;

		Object[] stack = result.getImageArray();
		byte[] slice;
		byte[] slice2;
		
		if (showProgress) {
			IJ.showProgress(0, sizeZ);
		}

		// Iterate over pixels
		for (int z = 0; z < sizeZ; z++) {
			if (showProgress) {
				IJ.showProgress(z + 1, sizeZ);
				System.out.println("z = " + z);
			}
			
			slice = (byte[]) stack[z];
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					int currentValue = slice[y * sizeX + x] & 0x00FF;
					minValue = currentValue;
					
					// Iterate over the 3 'upper' neighbors of current pixel
					if (x > 0) 
						minValue = min(minValue, slice[y * sizeX + x - 1] & 0x00FF);
					if (y > 0) 
						minValue = min(minValue, slice[(y - 1) * sizeX + x] & 0x00FF);
					if (z > 0) {
						slice2 = (byte[]) stack[z - 1];
						minValue = min(minValue, slice2[y * sizeX + x] & 0x00FF);
					}
					
					// update value of current voxel
					minValue = max(minValue, (int) mask.getVoxel(x, y, z));
					if (minValue < currentValue) {
						slice[y * sizeX + x] = (byte) (minValue & 0x00FF);
						modif = true;
					}
				}
			}
		} // end of pixel iteration
	}

	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency, assuming pixels are stored in bytes.
	 */
	private void forwardErosionC26() {
		// the minimal value around current pixel
		int minValue;

		Object[] stack = result.getImageArray();
		byte[] slice;
		byte[] slice2;
		
		if (showProgress) {
			IJ.showProgress(0, sizeZ);
		}

		// Iterate over pixels
		for (int z = 0; z < sizeZ; z++) {
//			IJ.showProgress(z + 1, size3);
//			System.out.println("z = " + z);
			slice = (byte[]) stack[z];
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					int currentValue = slice[y * sizeX + x] & 0x00FF;
					minValue = currentValue;
					
					// Iterate over neighbors of current pixel
					int zmax = min(z, sizeZ - 1);
					for (int z2 = max(z - 1, 0); z2 <= zmax; z2++) {
						slice2 = (byte[]) stack[z2];
						
						int ymax = z2 == z ? y : min(y + 1, sizeY - 1); 
						for (int y2 = max(y - 1, 0); y2 <= ymax; y2++) {
							int xmax = (z2 == z && y2 == y) ? x - 1 : min(x + 1, sizeX - 1); 
							for (int x2 = max(x - 1, 0); x2 <= xmax; x2++) {
								int neighborValue = slice2[y2 * sizeX + x2] & 0x00FF;
								if (neighborValue < minValue)
									minValue = neighborValue;
							}
						}
					}

					minValue = max(minValue, (int) mask.getVoxel(x, y, z));
					if (minValue < currentValue) {
						slice[y * sizeX + x] = (byte) (minValue & 0x00FF);
						modif = true;
					}

				}
			}
		}
	}

	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 6-adjacency, assuming pixels are stored in bytes.
	 */
	private void forwardErosionC6( ImageStack binaryMask ) 
	{
		// the maximal value around current pixel
		int minValue;

		Object[] stack = result.getImageArray();
		Object[] binaryStack = binaryMask.getImageArray();
		byte[] slice;
		byte[] slice2;
		byte[] binarySlice;
		
		if (showProgress) {
			IJ.showProgress(0, sizeZ);
		}

		// Iterate over pixels
		for (int z = 0; z < sizeZ; z++) {
			if (showProgress) {
				IJ.showProgress(z + 1, sizeZ);
				System.out.println("z = " + z);
			}
			
			slice = (byte[]) stack[z];
			binarySlice = (byte[]) binaryStack[ z ];
			
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					if( binarySlice[ y * sizeX + x ] != 0 )
					{
						int currentValue = slice[y * sizeX + x] & 0x00FF;
						minValue = currentValue;

						// Iterate over the 3 'upper' neighbors of current pixel
						if (x > 0) 
							minValue = min(minValue, slice[y * sizeX + x - 1] & 0x00FF);
						if (y > 0) 
							minValue = min(minValue, slice[(y - 1) * sizeX + x] & 0x00FF);
						if (z > 0) {
							slice2 = (byte[]) stack[z - 1];
							minValue = min(minValue, slice2[y * sizeX + x] & 0x00FF);
						}

						// update value of current voxel
						minValue = max(minValue, (int) mask.getVoxel(x, y, z));
						if (minValue < currentValue) {
							slice[y * sizeX + x] = (byte) (minValue & 0x00FF);
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
	private void forwardErosionC26( ImageStack binaryMask ) {
		// the minimal value around current pixel
		int minValue;

		Object[] stack = result.getImageArray();
		Object[] binaryStack = binaryMask.getImageArray();
		byte[] slice;
		byte[] slice2;
		byte[] binarySlice;
		
		if (showProgress) {
			IJ.showProgress(0, sizeZ);
		}

		// Iterate over pixels
		for (int z = 0; z < sizeZ; z++) {
			//			IJ.showProgress(z + 1, size3);
			//			System.out.println("z = " + z);
			slice = (byte[]) stack[z];
			binarySlice = (byte[]) binaryStack[ z ];

			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					if( binarySlice[ y * sizeX + x ] != 0 )
					{
						int currentValue = slice[y * sizeX + x] & 0x00FF;
						minValue = currentValue;

						// Iterate over neighbors of current pixel
						int zmax = min(z, sizeZ - 1);
						for (int z2 = max(z - 1, 0); z2 <= zmax; z2++) {
							slice2 = (byte[]) stack[z2];

							int ymax = z2 == z ? y : min(y + 1, sizeY - 1); 
							for (int y2 = max(y - 1, 0); y2 <= ymax; y2++) {
								int xmax = (z2 == z && y2 == y) ? x - 1 : min(x + 1, sizeX - 1); 
								for (int x2 = max(x - 1, 0); x2 <= xmax; x2++) {
									int neighborValue = slice2[y2 * sizeX + x2] & 0x00FF;
									if (neighborValue < minValue)
										minValue = neighborValue;
								}
							}
						}

						minValue = max(minValue, (int) mask.getVoxel(x, y, z));
						if (minValue < currentValue) {
							slice[y * sizeX + x] = (byte) (minValue & 0x00FF);
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
	private void backwardErosionC6() {
		// the maximal value around current pixel
		int minValue;

		Object[] stack = result.getImageArray();
		byte[] slice;
		byte[] slice2;

		if (showProgress) {
			IJ.showProgress(0, sizeZ);
		}

		// Iterate over voxels
		for (int z = sizeZ - 1; z >= 0; z--) {
			if (showProgress) {
				IJ.showProgress(sizeZ - z, sizeZ);
				System.out.println("z = " + z);
			}

			slice = (byte[]) stack[z];
			for (int y = sizeY - 1; y >= 0; y--) {
				for (int x = sizeX - 1; x >= 0; x--) {
					int currentValue = slice[y * sizeX + x] & 0x00FF;
					minValue = currentValue;
					
					// Iterate over the 3 'lower' neighbors of current voxel
					if (x < sizeX - 1) 
						minValue = min(minValue, slice[y * sizeX + x + 1] & 0x00FF);
					if (y < sizeY - 1) 
						minValue = min(minValue, slice[(y + 1) * sizeX + x] & 0x00FF);
					if (z < sizeZ - 1) {
						slice2 = (byte[]) stack[z + 1];
						minValue = min(minValue, slice2[y * sizeX + x] & 0x00FF);
					}

					// update value of current voxel
					minValue = max(minValue, (int) mask.getVoxel(x, y, z));
					if (minValue < currentValue) {
						slice[y * sizeX + x] = (byte) (minValue & 0x00FF);
						modif = true;
					}
				}
			}
		}	
	}
		
	/**
	 * Update result image using pixels in the lower right neighborhood, using
	 * the 26-adjacency.
	 */
	private void backwardErosionC26() {
		// the maximal value around current pixel
		int minValue;

		Object[] stack = result.getImageArray();
		byte[] slice;
		byte[] slice2;

		if (showProgress) {
			IJ.showProgress(0, sizeZ);
		}

		// Iterate over voxels
		for (int z = sizeZ - 1; z >= 0; z--) {
			slice = (byte[]) stack[z];
//			IJ.showProgress(size3 - z, size3);
			for (int y = sizeY - 1; y >= 0; y--) {
				for (int x = sizeX - 1; x >= 0; x--) {
					int currentValue = slice[y * sizeX + x] & 0x00FF;
					minValue = currentValue;
					
					// Iterate over neighbors of current voxel
					int zmin = max(z - 1, 0);
					for (int z2 = min(z + 1, sizeZ - 1); z2 >= zmin; z2--) {
						slice2 = (byte[]) stack[z2];
						
						int ymin = z2 == z ? y : max(y - 1, 0); 
						for (int y2 = min(y + 1, sizeY - 1); y2 >= ymin; y2--) {
							int xmin = (z2 == z && y2 == y) ? x : max(x - 1, 0); 
							for (int x2 = min(x + 1, sizeX - 1); x2 >= xmin; x2--) {
								int index = y2 * sizeX + x2;
								int neighborValue = slice2[index] & 0x00FF;
								if (neighborValue < minValue)
									minValue = neighborValue;
							}
						}
					}

					// update value of current voxel
					minValue = max(minValue, (int) mask.getVoxel(x, y, z));
					if (minValue < currentValue) {
						slice[y * sizeX + x] = (byte) (minValue & 0x00FF);
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
	private void backwardErosionC6( ImageStack binaryMask ) 
	{
		// the maximal value around current pixel
		int minValue;

		Object[] stack = result.getImageArray();
		Object[] binaryStack = binaryMask.getImageArray();
		byte[] slice;
		byte[] slice2;
		byte[] binarySlice;

		if (showProgress) {
			IJ.showProgress(0, sizeZ);
		}

		// Iterate over voxels
		for (int z = sizeZ - 1; z >= 0; z--) {
			if (showProgress) {
				IJ.showProgress(sizeZ - z, sizeZ);
				System.out.println("z = " + z);
			}

			slice = (byte[]) stack[z];
			binarySlice = (byte[]) binaryStack[ z ];
						
			for (int y = sizeY - 1; y >= 0; y--) {
				for (int x = sizeX - 1; x >= 0; x--) {
					int currentValue = slice[y * sizeX + x] & 0x00FF;
					minValue = currentValue;
					
					// Iterate over the 3 'lower' neighbors of current voxel
					if (x < sizeX - 1) 
						minValue = min(minValue, slice[y * sizeX + x + 1] & 0x00FF);
					if (y < sizeY - 1) 
						minValue = min(minValue, slice[(y + 1) * sizeX + x] & 0x00FF);
					if (z < sizeZ - 1) {
						slice2 = (byte[]) stack[z + 1];
						minValue = min(minValue, slice2[y * sizeX + x] & 0x00FF);
					}

					// update value of current voxel
					minValue = max(minValue, (int) mask.getVoxel(x, y, z));
					if (minValue < currentValue) {
						slice[y * sizeX + x] = (byte) (minValue & 0x00FF);
						modif = true;
					}
				}
			}
		}	
	}
		
	/**
	 * Update result image using pixels in the lower right neighborhood, using
	 * the 26-adjacency.
	 */
	private void backwardErosionC26( ImageStack binaryMask ) 
	{
		// the maximal value around current pixel
		int minValue;

		Object[] stack = result.getImageArray();
		Object[] binaryStack = binaryMask.getImageArray();
		byte[] slice;
		byte[] slice2;
		byte[] binarySlice;

		if (showProgress) {
			IJ.showProgress(0, sizeZ);
		}

		// Iterate over voxels
		for (int z = sizeZ - 1; z >= 0; z--) 
		{
			slice = (byte[]) stack[z];
			binarySlice = (byte[]) binaryStack[ z ];
			
//			IJ.showProgress(size3 - z, size3);
			for (int y = sizeY - 1; y >= 0; y--) {
				for (int x = sizeX - 1; x >= 0; x--) {
					if( binarySlice[ y * sizeX + x ] != 0 )
					{
						int currentValue = slice[y * sizeX + x] & 0x00FF;
						minValue = currentValue;

						// Iterate over neighbors of current voxel
						int zmin = max(z - 1, 0);
						for (int z2 = min(z + 1, sizeZ - 1); z2 >= zmin; z2--) {
							slice2 = (byte[]) stack[z2];

							int ymin = z2 == z ? y : max(y - 1, 0); 
							for (int y2 = min(y + 1, sizeY - 1); y2 >= ymin; y2--) {
								int xmin = (z2 == z && y2 == y) ? x : max(x - 1, 0); 
								for (int x2 = min(x + 1, sizeX - 1); x2 >= xmin; x2--) {
									int index = y2 * sizeX + x2;
									int neighborValue = slice2[index] & 0x00FF;
									if (neighborValue < minValue)
										minValue = neighborValue;
								}
							}
						}

						// update value of current voxel
						minValue = max(minValue, (int) mask.getVoxel(x, y, z));
						if (minValue < currentValue) {
							slice[y * sizeX + x] = (byte) (minValue & 0x00FF);
							modif = true;
						}
					}
				}
			}
		}
		
	}
	

	@Override
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
		if (sizeX != mask.getWidth() || sizeY != mask.getHeight() || sizeZ != mask.getSize()) {
			throw new IllegalArgumentException("Marker and Mask images must have the same size");
		}

		// Check connectivity has a correct value
		if (connectivity != 6 && connectivity != 26) {
			throw new RuntimeException(
					"Connectivity for stacks must be either 6 or 26, not "
							+ connectivity);
		}

		// Create result image the same size as marker image
		this.result = ImageStack.create(sizeX, sizeY, sizeZ, marker.getBitDepth());

		// Initialize the result image with the minimum value of marker and mask
		// images
		initializeResult( binaryMask );

		// Count the number of iterations for eventually displaying progress
		int iter = 1;

		// Iterate forward and backward propagations until no more pixel have been modified
		do {
			modif = false;

			// Display current status
			if (verbose) {
				System.out.println("Forward iteration " + iter);
			}
			if (showStatus) {
				IJ.showStatus("Geod. Rec. by Ero. Fwd " + iter);
			}

			// forward iteration
			switch (connectivity) {
			case 6:
				forwardErosionC6( binaryMask );
				break;
			case 26:
				forwardErosionC26( binaryMask );
				break;
			}

			// Display current status
			if (verbose) {
				System.out.println("Backward iteration " + iter);
			}
			if (showStatus) {
				IJ.showStatus("Geod. Rec. by Ero. Bwd " + iter);
			}

			// backward iteration
			switch (connectivity) {
			case 6:
				backwardErosionC6( binaryMask );
				break;
			case 26:	
				backwardErosionC26( binaryMask ); 
				break;
			}

			iter++;
		} while (modif);

		return this.result;
	}		

}
