/**
 * 
 */
package inra.ijpb.morphology.geodrec;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.LinkedList;

import ij.IJ;
import ij.ImageStack;


/**
 * geodesic reconstruction by erosion for 3D stacks of byte processors.
 * @author David Legland
 *
 */
public class GeodesicReconstructionByErosion3DGray8 implements GeodesicReconstruction3DAlgo {
	ImageStack marker;
	ImageStack mask;
	
	ImageStack result;
	
	/** image width */
	int size1 = 0;
	/** image height */
	int size2 = 0;
	/** image depth */
	int size3 = 0;

	int connectivity = 6;
	
	LinkedList<int[]> queue;
	
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
	public GeodesicReconstructionByErosion3DGray8() {
	}
	
	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * that specifies the connectivity to use.
	 */
	public GeodesicReconstructionByErosion3DGray8(int connectivity) {
		this.connectivity = connectivity;
	}

	public int getConnectivity() {
		return this.connectivity;
	}
	
	public void setConnectivity(int conn) {
		this.connectivity = conn;
	}

//	/**
//	 * Run the reconstruction by dilation algorithm using the images specified
//	 * as argument.
//	 */
//	public ImageStack applyToTmp(ImageStack marker, ImageStack mask) {
//		// Keep references to input images
//		this.marker = marker;
//		this.mask = mask;
//		
//		// Check sizes are consistent
//		this.size1 	= marker.getWidth();
//		this.size2 	= marker.getHeight();
//		this.size3 	= marker.getSize();
//		if (size1 != mask.getWidth() || size2 != mask.getHeight() || size3 != mask.getSize()) {
//			throw new IllegalArgumentException("Marker and Mask images must have the same size");
//		}
//		
//		// Check connectivity has a correct value
//		if (connectivity != 6 && connectivity != 26) {
//			throw new RuntimeException(
//					"Connectivity for stacks must be either 6 or 26, not "
//							+ connectivity);
//		}
//
//		// Create result image the same size as marker image
//		this.result = ImageStack.create(size1, size2, size3, marker.getBitDepth());
//
//		// Initialize the result image with the minimum value of marker and mask
//		// images
//		for (int z = 0; z < size3; z++) {
//			for (int y = 0; y < size2; y++) {
//				for (int x = 0; x < size1; x++) {
//					this.result.setVoxel(x, y, z, 
//							Math.max(this.marker.getVoxel(x, y, z),
//									this.mask.getVoxel(x, y, z)));
//				}
//			}
//		}
//
//		//		// Count the number of iterations for eventually displaying progress
//		//		int iter = 0;
//		//		
//		//		// Iterate forward and backward propagations until no more pixel have been modified
//		//		do {
//		//			modif = false;
//
//		// Display current status
//		if (verbose) {
//			System.out.println("Forward iteration");
//		}
//		if (showStatus) {
//			IJ.showStatus("FW Geod. Rec. by Dil.");
//		}
//
//		// forward iteration
//		//			switch (connectivity) {
//		//			case 6:
//		//				forwardDilationC6(); 
//		//				break;
//		//			case 26:
//		forwardDilationC26(); 
//		//				break;
//		//			}
//
//		// Display current status
//		if (verbose) {
//			System.out.println("Backward iteration ");
//		}
//		if (showStatus) {
//			IJ.showStatus("BW Geod. Rec. by Dil.");
//		}
//
//		// backward iteration
//		//			switch (connectivity) {
//		//			case 4:
//		//				backwardDilationC6();
//		////				break;
//		//			case 8:	
//		backwardDilationC26InitQueue(); 
//		//				break;
//		//			}
//
//		//			iter++;
//		//		} while (modif);
//
////		forwardDilationC26InitQueue(); 
//		System.out.println("queue size: " + this.queue.size());
//
//		processQueueC26();
//		
//		return this.result;
//	}

	/**
	 * Run the reconstruction by dilation algorithm using the images specified
	 * as argument.
	 */
	public ImageStack applyTo(ImageStack marker, ImageStack mask) {
		// Keep references to input images
		this.marker = marker;
		this.mask = mask;
		
		// Check sizes are consistent
		this.size1 	= marker.getWidth();
		this.size2 	= marker.getHeight();
		this.size3 	= marker.getSize();
		if (size1 != mask.getWidth() || size2 != mask.getHeight() || size3 != mask.getSize()) {
			throw new IllegalArgumentException("Marker and Mask images must have the same size");
		}
		
		// Check connectivity has a correct value
		if (connectivity != 6 && connectivity != 26) {
			throw new RuntimeException(
					"Connectivity for stacks must be either 6 or 26, not "
							+ connectivity);
		}

		initializeResult();
		
		//		// Count the number of iterations for eventually displaying progress
		//		int iter = 0;
		//		
		//		// Iterate forward and backward propagations until no more pixel have been modified
		//		do {
		//			modif = false;

		// Display current status
		if (verbose) {
			System.out.println("Forward iteration");
		}
		if (showStatus) {
			IJ.showStatus("Geod. Rec. by Dil. Fwd ");
		}

		// forward iteration
		//			switch (connectivity) {
		//			case 6:
		//				forwardDilationC6(); 
		//				break;
		//			case 26:
		forwardDilationC26(); 
		//				break;
		//			}

		// Display current status
		if (verbose) {
			System.out.println("Backward iteration ");
		}
		if (showStatus) {
			IJ.showStatus("Geod. Rec. by Dil. Bwd ");
		}

		// backward iteration
		//			switch (connectivity) {
		//			case 4:
		//				backwardDilationC6();
		////				break;
		//			case 8:	
		backwardDilationC26(); 
		//				break;
		//			}

		//			iter++;
		//		} while (modif);

		forwardDilationC26InitQueue(); 
		System.out.println("queue size: " + this.queue.size());

		processQueueC26();
		
		return this.result;
	}


//	/**
//	 * Run the reconstruction by dilation algorithm using the images specified
//	 * as argument.
//	 */
//	public ImageStack applyToOld(ImageStack marker, ImageStack mask) {
//		// Keep references to input images
//		this.marker = marker;
//		this.mask = mask;
//		
//		// Check sizes are consistent
//		this.size1 	= marker.getWidth();
//		this.size2 	= marker.getHeight();
//		this.size3 	= marker.getSize();
//		if (size1 != mask.getWidth() || size2 != mask.getHeight() || size3 != mask.getSize()) {
//			throw new IllegalArgumentException("Marker and Mask images must have the same size");
//		}
//		
//		// Check connectivity has a correct value
//		if (connectivity != 6 && connectivity != 26) {
//			throw new RuntimeException(
//					"Connectivity for stacks must be either 6 or 26, not "
//							+ connectivity);
//		}
//
//		// Create result image the same size as marker image
//		this.result = ImageStack.create(size1, size2, size3, marker.getBitDepth());
//
//		// Initialize the result image with the minimum value of marker and mask
//		// images
//		for (int z = 0; z < size3; z++) {
//			for (int y = 0; y < size2; y++) {
//				for (int x = 0; x < size1; x++) {
//					this.result.setVoxel(x, y, z, 
//							Math.min(this.marker.getVoxel(x, y, z),
//									this.mask.getVoxel(x, y, z)));
//				}
//			}
//		}
//
//		// Count the number of iterations for eventually displaying progress
//		int iter = 0;
//		
//		// Iterate forward and backward propagations until no more pixel have been modified
//		do {
//			modif = false;
//
//			// Display current status
//			if (verbose) {
//				System.out.println("Forward iteration " + iter);
//			}
//			if (showStatus) {
//				IJ.showStatus("FW Geod. Rec. by Dil." + (iter + 1));
//			}
//			
//			// forward iteration
////			switch (connectivity) {
////			case 6:
////				forwardDilationC6(); 
////				break;
////			case 26:
//				forwardDilationC26(); 
////				break;
////			}
//
//			// Display current status
//			if (verbose) {
//				System.out.println("Backward iteration " + iter);
//			}
//			if (showStatus) {
//				IJ.showStatus("BW Geod. Rec. by Dil." + (iter + 1));
//			}
//			
//			// backward iteration
////			switch (connectivity) {
////			case 4:
////				backwardDilationC6();
//////				break;
////			case 8:	
//				backwardDilationC26(); 
////				break;
////			}
//
//			iter++;
//		} while (modif);
//	
//		return this.result;
//	}

	private void initializeResult() {
		// Create result image the same size as marker image
		this.result = ImageStack.create(size1, size2, size3, marker.getBitDepth());

		Object[] stack = result.getImageArray();
		Object[] markerStack = marker.getImageArray();
		Object[] maskStack = mask.getImageArray();
		byte[] slice;
		byte[] markerSlice;
		byte[] maskSlice;

		// Initialize the result image with the minimum value of marker and mask
		// images
		for (int z = 0; z < size3; z++) {
			slice = (byte[]) stack[z];
			maskSlice = (byte[]) maskStack[z];
			markerSlice = (byte[]) markerStack[z];
			
			for (int y = 0; y < size2; y++) {
				for (int x = 0; x < size1; x++) {
					int index = y * size1 + x;
					int value = max(markerSlice[index] & 0x00FF, maskSlice[index] & 0x00FF);
					slice[index] = (byte) value;
				}
			}
		}
	}
	
	private void initializeResult( ImageStack binaryMask ) 
	{
		// Create result image the same size as marker image
		this.result = ImageStack.create(size1, size2, size3, marker.getBitDepth());

		Object[] stack = result.getImageArray();
		Object[] markerStack = marker.getImageArray();
		Object[] maskStack = mask.getImageArray();
		byte[] slice;
		byte[] markerSlice;
		byte[] maskSlice;

		// Initialize the result image with the minimum value of marker and mask
		// images
		for (int z = 0; z < size3; z++) {
			slice = (byte[]) stack[z];
			maskSlice = (byte[]) maskStack[z];
			markerSlice = (byte[]) markerStack[z];
			
			for (int y = 0; y < size2; y++) {
				for (int x = 0; x < size1; x++) {
					if( binaryMask.getVoxel( x, y, z ) != 0 )
					{
						int index = y * size1 + x;
						int value = max(markerSlice[index] & 0x00FF, maskSlice[index] & 0x00FF);
						slice[index] = (byte) value;
					}
				}
			}
		}
	}
	
	
//	/**
//	 * Update result image using pixels in the upper left neighborhood, using
//	 * the 26-adjacency, assuming pixels are stored in bytes.
//	 */
//	private void forwardDilationC26() {
//		// the maximal value around current pixel
//		int minValue;
//
//		Object[] stack = result.getImageArray();
//		byte[] slice;
//		
//		if (showProgress) {
//			IJ.showProgress(0, size3);
//		}
//
//		// Iterate over pixels
//		for (int z = 0; z < size3; z++) {
////			IJ.showProgress(z + 1, size3);
////			System.out.println("z = " + z);
//			for (int y = 0; y < size2; y++) {
//				for (int x = 0; x < size1; x++) {
//					minValue = (int) result.getVoxel(x, y, z);
//					
//					// Iterate over neighbors of current pixel
//					int zmax = min(z + 1, size3);
//					for (int z2 = max(z - 1, 0); z2 < zmax; z2++) {
//						slice = (byte[]) stack[z2];
//						
//						int ymax = z2 == z ? y : min(y + 2, size2); 
//						for (int y2 = max(y - 1, 0); y2 < ymax; y2++) {
//							int xmax = (z2 == z && y2 == y) ? x : min(x + 2, size1); 
//							for (int x2 = max(x - 1, 0); x2 < xmax; x2++) {
//								int neighborValue = slice[y2 * size1 + x2] & 0x00FF;
//								if (neighborValue > minValue)
//									minValue = neighborValue;
//							}
//						}
//					}
//
//					geodesicDilationUpdate(x, y, z, minValue);
//				}
//			}
//		}
//	}

	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency, assuming pixels are stored in bytes.
	 */
	private void forwardDilationC26() {
		// the minimal value around current pixel
		int minValue;

		Object[] stack = result.getImageArray();
		byte[] slice;
		byte[] slice2;
		
		if (showProgress) {
			IJ.showProgress(0, size3);
		}

		// Iterate over pixels
		for (int z = 0; z < size3; z++) {
//			IJ.showProgress(z + 1, size3);
//			System.out.println("z = " + z);
			slice = (byte[]) stack[z];
			for (int y = 0; y < size2; y++) {
				for (int x = 0; x < size1; x++) {
					int currentValue = slice[y * size1 + x] & 0x00FF;
//					minValue = (int) result.getVoxel(x, y, z);
					minValue = currentValue;
					
					// Iterate over neighbors of current pixel
					int zmax = min(z + 1, size3);
					for (int z2 = max(z - 1, 0); z2 < zmax; z2++) {
						slice2 = (byte[]) stack[z2];
						
						int ymax = z2 == z ? y : min(y + 2, size2); 
						for (int y2 = max(y - 1, 0); y2 < ymax; y2++) {
							int xmax = (z2 == z && y2 == y) ? x : min(x + 2, size1); 
							for (int x2 = max(x - 1, 0); x2 < xmax; x2++) {
								int neighborValue = slice2[y2 * size1 + x2] & 0x00FF;
								if (neighborValue < minValue)
									minValue = neighborValue;
							}
						}
					}

//					geodesicDilationUpdate(x, y, z, minValue);
					minValue = Math.max(minValue, (int) mask.getVoxel(x, y, z));
					if (minValue < currentValue) {
						slice[y * size1 + x] = (byte) (minValue & 0x00FF);
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
		// the minimal value around current pixel
		int minValue;

		Object[] stack = result.getImageArray();
		byte[] slice;
		byte[] slice2;
		
		if (showProgress) {
			IJ.showProgress(0, size3);
		}

		// Iterate over pixels
		for (int z = 0; z < size3; z++) {
			//			IJ.showProgress(z + 1, size3);
			//			System.out.println("z = " + z);
			slice = (byte[]) stack[z];
			for (int y = 0; y < size2; y++) {
				for (int x = 0; x < size1; x++) {
					if( binaryMask.getVoxel(x, y, z) != 0 )
					{
						int currentValue = slice[y * size1 + x] & 0x00FF;
						//					minValue = (int) result.getVoxel(x, y, z);
						minValue = currentValue;

						// Iterate over neighbors of current pixel
						int zmax = min(z + 1, size3);
						for (int z2 = max(z - 1, 0); z2 < zmax; z2++) {
							slice2 = (byte[]) stack[z2];

							int ymax = z2 == z ? y : min(y + 2, size2); 
							for (int y2 = max(y - 1, 0); y2 < ymax; y2++) {
								int xmax = (z2 == z && y2 == y) ? x : min(x + 2, size1); 
								for (int x2 = max(x - 1, 0); x2 < xmax; x2++) {
									int neighborValue = slice2[y2 * size1 + x2] & 0x00FF;
									if (neighborValue < minValue)
										minValue = neighborValue;
								}
							}
						}

						//					geodesicDilationUpdate(x, y, z, minValue);
						minValue = Math.max(minValue, (int) mask.getVoxel(x, y, z));
						if (minValue < currentValue) {
							slice[y * size1 + x] = (byte) (minValue & 0x00FF);
						}
					}
				}
			}
		}
	}


	/**
	 */
	private void forwardDilationC26InitQueue() {
		// the maximal value around current pixel
		int currentValue, minValue;

		Object[] stack = result.getImageArray();
		Object[] maskStack = mask.getImageArray();
		byte[] slice;
		byte[] slice2;
		byte[] maskSlice;
		
		if (showProgress) {
			IJ.showProgress(0, size3);
		}

		this.queue = new LinkedList<int[]>();
		
		// Iterate over pixels
		for (int z = 0; z < size3; z++) {
//			IJ.showProgress(z + 1, size3);
//			System.out.println("z = " + z);
			slice = (byte[]) stack[z];
			for (int y = 0; y < size2; y++) {
				for (int x = 0; x < size1; x++) {
					currentValue = slice[y * size1 + x] & 0x00FF;
					minValue = currentValue;
					
					// Iterate over neighbors of current pixel
					int zmax = min(z + 1, size3);
					for (int z2 = max(z - 1, 0); z2 < zmax; z2++) {
						slice = (byte[]) stack[z2];
						
						int ymax = z2 == z ? y : min(y + 2, size2); 
						for (int y2 = max(y - 1, 0); y2 < ymax; y2++) {
							int xmax = (z2 == z && y2 == y) ? x : min(x + 2, size1); 
							for (int x2 = max(x - 1, 0); x2 < xmax; x2++) {
								int neighborValue = slice[y2 * size1 + x2] & 0x00FF;
								if (neighborValue < minValue)
									minValue = neighborValue;
							}
						}
					}

					// update current value only if clamped value is strictly greater
					maskSlice = (byte[]) maskStack[z];
					minValue = max(minValue, maskSlice[y * size1 + x] & 0x00FF);
					
					// escape if nothing to modify
					if (minValue >= currentValue) {
						continue;
					}
					
					// update current voxel value
					result.setVoxel(x, y, z, minValue);
					slice[y * size1 + x] = (byte) (minValue & 0x00FF);

					
					// add neighbors whose value is lower than maxVal to the queue
					// Iterate over neighbors of current pixel
					for (int z2 = max(z - 1, 0); z2 < zmax; z2++) {
						slice2 = (byte[]) stack[z2];
						maskSlice = (byte[]) maskStack[z2];

						int ymax = z2 == z ? y : min(y + 2, size2); 
						for (int y2 = max(y - 1, 0); y2 < ymax; y2++) {
							int xmax = (z2 == z && y2 == y) ? x : min(x + 2, size1); 
							for (int x2 = max(x - 1, 0); x2 < xmax; x2++) {
								int index = y2 * size1 + x2;
								int neighborValue = slice2[index] & 0x00FF;
								int maskValue = maskSlice[index] & 0x00FF;
								if (neighborValue > minValue && neighborValue > maskValue) {
									int[] pos = {x2, y2, z2};
									queue.addLast(pos);
								}
							}
						}
					} // end of iteration on neighbors

				} // end of processing of current voxel
			}
		} // end of iteration on voxels
		
	} // end of pass
	
	
	/**
	 */
	private void forwardDilationC26InitQueue( ImageStack binaryMask ) 
	{
		// the maximal value around current pixel
		int currentValue, minValue;

		Object[] stack = result.getImageArray();
		Object[] maskStack = mask.getImageArray();
		byte[] slice;
		byte[] slice2;
		byte[] maskSlice;
		
		if (showProgress) {
			IJ.showProgress(0, size3);
		}

		this.queue = new LinkedList<int[]>();
		
		// Iterate over pixels
		for (int z = 0; z < size3; z++) {
			//			IJ.showProgress(z + 1, size3);
			//			System.out.println("z = " + z);
			slice = (byte[]) stack[z];
			for (int y = 0; y < size2; y++) {
				for (int x = 0; x < size1; x++) {
					if ( binaryMask.getVoxel(x, y, z) != 0 )
					{
						currentValue = slice[y * size1 + x] & 0x00FF;
						minValue = currentValue;

						// Iterate over neighbors of current pixel
						int zmax = min(z + 1, size3);
						for (int z2 = max(z - 1, 0); z2 < zmax; z2++) {
							slice = (byte[]) stack[z2];

							int ymax = z2 == z ? y : min(y + 2, size2); 
							for (int y2 = max(y - 1, 0); y2 < ymax; y2++) {
								int xmax = (z2 == z && y2 == y) ? x : min(x + 2, size1); 
								for (int x2 = max(x - 1, 0); x2 < xmax; x2++) {
									int neighborValue = slice[y2 * size1 + x2] & 0x00FF;
									if (neighborValue < minValue)
										minValue = neighborValue;
								}
							}
						}

						// update current value only if clamped value is strictly greater
						maskSlice = (byte[]) maskStack[z];
						minValue = max(minValue, maskSlice[y * size1 + x] & 0x00FF);

						// escape if nothing to modify
						if (minValue >= currentValue) {
							continue;
						}

						// update current voxel value
						result.setVoxel(x, y, z, minValue);
						slice[y * size1 + x] = (byte) (minValue & 0x00FF);


						// add neighbors whose value is lower than maxVal to the queue
						// Iterate over neighbors of current pixel
						for (int z2 = max(z - 1, 0); z2 < zmax; z2++) {
							slice2 = (byte[]) stack[z2];
							maskSlice = (byte[]) maskStack[z2];

							int ymax = z2 == z ? y : min(y + 2, size2); 
							for (int y2 = max(y - 1, 0); y2 < ymax; y2++) {
								int xmax = (z2 == z && y2 == y) ? x : min(x + 2, size1); 
								for (int x2 = max(x - 1, 0); x2 < xmax; x2++) {
									int index = y2 * size1 + x2;
									int neighborValue = slice2[index] & 0x00FF;
									int maskValue = maskSlice[index] & 0x00FF;
									if (neighborValue > minValue && neighborValue > maskValue) {
										int[] pos = {x2, y2, z2};
										queue.addLast(pos);
									}
								}
							}
						} // end of iteration on neighbors

					}// endif mask 
					
				} // end of processing of current voxel
			}
		} // end of iteration on voxels
		
	} // end of pass
	
	
	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency.
	 */
	private void backwardDilationC26() {
		// the maximal value around current pixel
		int minValue;

		Object[] stack = result.getImageArray();
		byte[] slice;
		byte[] slice2;

		if (showProgress) {
			IJ.showProgress(0, size3);
		}

		// Iterate over voxels
		for (int z = size3 - 1; z >= 0; z--) {
			slice = (byte[]) stack[z];
//			IJ.showProgress(size3 - z, size3);
			for (int y = size2 - 1; y >= 0; y--) {
				for (int x = size1 - 1; x >= 0; x--) {
					int currentValue = slice[y * size1 + x] & 0x00FF;
					minValue = currentValue;
					
					// Iterate over neighbors of current voxel
					int zmin = max(z - 1, 0);
					for (int z2 = min(z + 1, size3 - 1); z2 >= zmin; z2--) {
						slice2 = (byte[]) stack[z2];
						
						int ymin = z2 == z ? y : max(y - 1, 0); 
						for (int y2 = min(y + 1, size2 - 1); y2 >= ymin; y2--) {
							int xmin = (z2 == z && y2 == y) ? x : max(x - 1, 0); 
							for (int x2 = min(x + 1, size1 - 1); x2 >= xmin; x2--) {
								int index = y2 * size1 + x2;
								int neighborValue = slice2[index] & 0x00FF;
								if (neighborValue < minValue)
									minValue = neighborValue;
							}
						}
					}

					// update value of current voxel
					minValue = Math.max(minValue, (int) mask.getVoxel(x, y, z));
					if (minValue < currentValue) {
						slice[y * size1 + x] = (byte) (minValue & 0x00FF);
					}
				}
			}
		}
		
	}		
	
	
	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency.
	 */
	private void backwardDilationC26( ImageStack binaryMask ) {
		// the maximal value around current pixel
		int minValue;

		Object[] stack = result.getImageArray();
		byte[] slice;
		byte[] slice2;

		if (showProgress) {
			IJ.showProgress(0, size3);
		}

		// Iterate over voxels
		for (int z = size3 - 1; z >= 0; z--) {
			slice = (byte[]) stack[z];
			//			IJ.showProgress(size3 - z, size3);
			for (int y = size2 - 1; y >= 0; y--) {
				for (int x = size1 - 1; x >= 0; x--) {
					if( binaryMask.getVoxel(x, y, z) != 0 )
					{
						int currentValue = slice[y * size1 + x] & 0x00FF;
						minValue = currentValue;

						// Iterate over neighbors of current voxel
						int zmin = max(z - 1, 0);
						for (int z2 = min(z + 1, size3 - 1); z2 >= zmin; z2--) {
							slice2 = (byte[]) stack[z2];

							int ymin = z2 == z ? y : max(y - 1, 0); 
							for (int y2 = min(y + 1, size2 - 1); y2 >= ymin; y2--) {
								int xmin = (z2 == z && y2 == y) ? x : max(x - 1, 0); 
								for (int x2 = min(x + 1, size1 - 1); x2 >= xmin; x2--) {
									int index = y2 * size1 + x2;
									int neighborValue = slice2[index] & 0x00FF;
									if (neighborValue < minValue)
										minValue = neighborValue;
								}
							}
						}

						// update value of current voxel
						minValue = Math.max(minValue, (int) mask.getVoxel(x, y, z));
						if (minValue < currentValue) {
							slice[y * size1 + x] = (byte) (minValue & 0x00FF);
						}
					}
				}
			}
		}
		
	}	

//	/**
//	 * Update result image using pixels in the upper left neighborhood, using
//	 * the 26-adjacency.
//	 */
//	private void backwardDilationC26InitQueue() {
//		// the maximal value around current pixel
//		int minValue;
//
//		Object[] stack = result.getImageArray();
//		Object[] maskStack = mask.getImageArray();
//		byte[] slice;
//		byte[] slice2;
//		byte[] maskSlice;
//
//		if (showProgress) {
//			IJ.showProgress(0, size3);
//		}
//
//		this.queue = new LinkedList<int[]>();
//
//		// Iterate over voxels
//		for (int z = size3 - 1; z >= 0; z--) {
//			slice = (byte[]) stack[z];
////			IJ.showProgress(size3 - z, size3);
//			for (int y = size2 - 1; y >= 0; y--) {
//				for (int x = size1 - 1; x >= 0; x--) {
////					minValue = (int) result.getVoxel(x, y, z);
//					int currentValue = slice[y * size1 + x] & 0x00FF;
//					minValue = currentValue;
//					
//					// Iterate over neighbors of current voxel to find max value
//					int zmin = max(z - 1, 0);
//					for (int z2 = min(z + 1, size3 - 1); z2 >= zmin; z2--) {
//						slice2 = (byte[]) stack[z2];
//						
//						int ymin = z2 == z ? y : max(y - 1, 0); 
//						for (int y2 = min(y + 1, size2 - 1); y2 >= ymin; y2--) {
//							int xmin = (z2 == z && y2 == y) ? x : max(x - 1, 0); 
//							for (int x2 = min(x + 1, size1 - 1); x2 >= xmin; x2--) {
//								int index = y2 * size1 + x2;
//								int neighborValue = slice2[index] & 0x00FF;
//								if (neighborValue > minValue)
//									minValue = neighborValue;
//							}
//						}
//					}
//
//					// update current value only if clamped value is strictly greater
//					maskSlice = (byte[]) maskStack[z];
//					minValue = min(minValue, maskSlice[y * size1 + x] & 0x00FF);
//					
//					// escape if nothing to modify
//					if (minValue <= currentValue) {
//						continue;
//					}
//					
//					// update current voxel value
//					result.setVoxel(x, y, z, minValue);
//					slice[y * size1 + x] = (byte) (minValue & 0x00FF);
//
////					// update value of current voxel
////					minValue = Math.min(minValue, (int) mask.getVoxel(x, y, z));
////					if (minValue > currentValue) {
////						slice[y * size1 + x] = (byte) (minValue & 0x00FF);
////					}
//
//					// Iterate over neighbors of current voxel to identify pixel to enqueue
//					for (int z2 = min(z + 1, size3 - 1); z2 >= zmin; z2--) {
//						slice2 = (byte[]) stack[z2];
//						maskSlice = (byte[]) maskStack[z2];
//						
//						int ymin = z2 == z ? y : max(y - 1, 0); 
//						for (int y2 = min(y + 1, size2 - 1); y2 >= ymin; y2--) {
//							int xmin = (z2 == z && y2 == y) ? x : max(x - 1, 0); 
//							for (int x2 = min(x + 1, size1 - 1); x2 >= xmin; x2--) {
//								int index = y2 * size1 + x2;
//								int neighborValue = slice2[index] & 0x00FF;
//								int maskValue = maskSlice[index] & 0x00FF;
//								if (neighborValue < minValue && neighborValue < maskValue) {
//									int[] pos = {x2, y2, z2};
//									queue.addLast(pos);
//								}
//							}
//						}
//					}
//
//				}
//			}
//		}
//		
//	}		


	private void processQueueC26() {
		Object[] stack = result.getImageArray();
		Object[] maskStack = mask.getImageArray();
		byte[] slice;
		byte[] slice2;
		byte[] maskSlice;

		int total = this.queue.size();
		int iter = 1;

		// iterate until queue is empty
		while (!this.queue.isEmpty()) {
			if (showProgress) {
				IJ.showProgress( iter, total );
			}
			if (verbose) {
				System.out.println("iter " + (iter++) + " over " + total);
			}
			
			int[] p = this.queue.pollFirst();
			int x = p[0];
			int y = p[1];
			int z = p[2];

			slice = (byte[]) stack[z];
			maskSlice = (byte[]) maskStack[z];
			int index = y * size1 + x;
			int currentValue = slice[index] & 0x00FF;
			int maskValue = maskSlice[index] & 0x00FF;
			if (currentValue == maskValue)
				continue;
			
			// iterate over full neighborhood to find maximum value in neighborhood
			double minValue = currentValue;
			for (int z2 = max(z-1, 0); z2 < min(z+2, size3); z2++) {
				slice2 = (byte[]) stack[z2];
				for (int y2 = max(y-1, 0); y2 < min(y+2, size2); y2++) {
					for (int x2 = max(x-1, 0); x2 < min(x+2, size1); x2++) {
						int value = slice2[y2 * size1 + x2] & 0x00FF;
						if (value < minValue)
							minValue = value;
					}
				}
			}

			// clamp with mask
			minValue = max(minValue, maskValue);
			if (minValue >= currentValue) {
				continue;
			}
			
			slice[index] = (byte) (((int) minValue) & 0x00FF);
			
			// iterate over full neighborhood to add positions to update
			for (int z2 = max(z-1, 0); z2 < min(z+2, size3); z2++) {
				slice2 = (byte[]) stack[z2];
				maskSlice = (byte[]) maskStack[z2];
				for (int y2 = max(y-1, 0); y2 < min(y+2, size2); y2++) {
					for (int x2 = max(x-1, 0); x2 < min(x+2, size1); x2++) {
						index = y2 * size1 + x2;
						int value = slice2[index] & 0x00FF;
						maskValue = maskSlice[index] & 0x00FF; 
						
						if (value > minValue && value > maskValue) {
							int[] pos = {x2, y2, z2};
							queue.addLast(pos);
							total++;
						}
					}
				}
			}
			
			iter++;
		}
	}
	
	private void processQueueC26( ImageStack binaryMask ) 
	{
		Object[] stack = result.getImageArray();
		Object[] maskStack = mask.getImageArray();
		byte[] slice;
		byte[] slice2;
		byte[] maskSlice;

		int total = this.queue.size();
		int iter = 1;

		// iterate until queue is empty
		while (!this.queue.isEmpty()) {
			if (showProgress) {
				IJ.showProgress( iter, total );
			}
			if (verbose) {
				System.out.println("iter " + (iter++) + " over " + total);
			}
			
			int[] p = this.queue.pollFirst();
			int x = p[0];
			int y = p[1];
			int z = p[2];
			
			if( binaryMask.getVoxel(x, y, z) == 0 )
				continue;

			slice = (byte[]) stack[z];
			maskSlice = (byte[]) maskStack[z];
			int index = y * size1 + x;
			int currentValue = slice[index] & 0x00FF;
			int maskValue = maskSlice[index] & 0x00FF;
			if (currentValue == maskValue)
				continue;
			
			// iterate over full neighborhood to find maximum value in neighborhood
			double minValue = currentValue;
			for (int z2 = max(z-1, 0); z2 < min(z+2, size3); z2++) {
				slice2 = (byte[]) stack[z2];
				for (int y2 = max(y-1, 0); y2 < min(y+2, size2); y2++) {
					for (int x2 = max(x-1, 0); x2 < min(x+2, size1); x2++) {
						int value = slice2[y2 * size1 + x2] & 0x00FF;
						if (value < minValue)
							minValue = value;
					}
				}
			}

			// clamp with mask
			minValue = max(minValue, maskValue);
			if (minValue >= currentValue) {
				continue;
			}
			
			slice[index] = (byte) (((int) minValue) & 0x00FF);
			
			// iterate over full neighborhood to add positions to update
			for (int z2 = max(z-1, 0); z2 < min(z+2, size3); z2++) {
				slice2 = (byte[]) stack[z2];
				maskSlice = (byte[]) maskStack[z2];
				for (int y2 = max(y-1, 0); y2 < min(y+2, size2); y2++) {
					for (int x2 = max(x-1, 0); x2 < min(x+2, size1); x2++) {
						index = y2 * size1 + x2;
						int value = slice2[index] & 0x00FF;
						maskValue = maskSlice[index] & 0x00FF; 
						
						if (value > minValue && value > maskValue) {
							int[] pos = {x2, y2, z2};
							queue.addLast(pos);
							total++;
						}
					}
				}
			}
			
			iter++;
		}
	}

	@Override
	public ImageStack applyTo(
			ImageStack marker, 
			ImageStack mask,
			ImageStack binaryMask) 
	{
		// Keep references to input images
		this.marker = marker;
		this.mask = mask;

		// Check sizes are consistent
		this.size1 	= marker.getWidth();
		this.size2 	= marker.getHeight();
		this.size3 	= marker.getSize();
		if (size1 != mask.getWidth() || size2 != mask.getHeight() || size3 != mask.getSize()) {
			throw new IllegalArgumentException("Marker and Mask images must have the same size");
		}

		// Check connectivity has a correct value
		if (connectivity != 6 && connectivity != 26) {
			throw new RuntimeException(
					"Connectivity for stacks must be either 6 or 26, not "
							+ connectivity);
		}

		initializeResult( binaryMask );

		//		// Count the number of iterations for eventually displaying progress
		//		int iter = 0;
		//		
		//		// Iterate forward and backward propagations until no more pixel have been modified
		//		do {
		//			modif = false;

		// Display current status
		if (verbose) {
			System.out.println("Forward iteration");
		}
		if (showStatus) {
			IJ.showStatus("Geod. Rec. by Dil. Fwd ");
		}

		// forward iteration
		//			switch (connectivity) {
		//			case 6:
		//				forwardDilationC6(); 
		//				break;
		//			case 26:
		forwardDilationC26( binaryMask ); 
		//				break;
		//			}

		// Display current status
		if (verbose) {
			System.out.println("Backward iteration ");
		}
		if (showStatus) {
			IJ.showStatus("Geod. Rec. by Dil. Bwd ");
		}

		// backward iteration
		//			switch (connectivity) {
		//			case 4:
		//				backwardDilationC6();
		////				break;
		//			case 8:	
		backwardDilationC26( binaryMask ); 
		//				break;
		//			}

		//			iter++;
		//		} while (modif);

		forwardDilationC26InitQueue( binaryMask ); 
		System.out.println("queue size: " + this.queue.size());

		processQueueC26( binaryMask );

		return this.result;
	}
	
}
