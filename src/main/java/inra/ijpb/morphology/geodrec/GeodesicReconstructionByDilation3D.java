/**
 * 
 */
package inra.ijpb.morphology.geodrec;

import static java.lang.Math.max;
import static java.lang.Math.min;
import ij.ImageStack;
import inra.ijpb.data.image.Images3D;


/**
 * Geodesic reconstruction by dilation for 3D images of any type, using scanning
 * algorithm and implemented only for C26 connectivity.
 * 
 * @author David Legland
 *
 */
public class GeodesicReconstructionByDilation3D extends GeodesicReconstruction3DAlgoStub 
{
	ImageStack marker;
	ImageStack mask;
	
	ImageStack result;
	
	/** image width */
	int size1 = 0;
	/** image height */
	int size2 = 0;
	/** image depth */
	int size3 = 0;

	/**
	 * The flag indicating whether the result image has been modified during
	 * last image scan
	 */
	boolean modif;

	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * using the default connectivity 6.
	 */
	public GeodesicReconstructionByDilation3D() 
	{
	}
	
	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * that specifies the connectivity to use.
	 * 
	 * @param connectivity
	 *            the 3D connectivity to use (either 6 or 26)
	 */
	public GeodesicReconstructionByDilation3D(int connectivity)
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
		
		// Check sizes are consistent
		this.size1 	= marker.getWidth();
		this.size2 	= marker.getHeight();
		this.size3 	= marker.getSize();
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

		// Create result image the same size as the mask and marker images
		this.result = ImageStack.create(size1, size2, size3, mask.getBitDepth());

		// Initialize the result image with the minimum value of marker and mask
		// images
		for (int z = 0; z < size3; z++)
		{
			for (int y = 0; y < size2; y++)
			{
				for (int x = 0; x < size1; x++) 
				{
					this.result.setVoxel(x, y, z, 
							Math.min(this.marker.getVoxel(x, y, z),
									this.mask.getVoxel(x, y, z)));
				}
			}
		}

		// Count the number of iterations for eventually displaying progress
		int iter = 0;
		
		// Iterate forward and backward propagations until no more pixel have been modified
		do 
		{
			modif = false;

			// Display current status
			trace("Forward iteration " + iter);
			showStatus("Geod. Rec. by Dil. Fwd " + (iter + 1));
			
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
			trace("Backward iteration " + iter);
			showStatus("Geod. Rec. by Dil. Bwd " + (iter + 1));
			
			// backward iteration
//			switch (connectivity) {
//			case 4:
//				backwardDilationC6();
////				break;
//			case 8:	
				backwardDilationC26(); 
//				break;
//			}

			iter++;
		} while (modif);
	
		// clear progression display
		showProgress(1, 1, "");

		return this.result;
	}

//	/**
//	 * Run the reconstruction by dilation algorithm using the images specified
//	 * as argument.
//	 */
//	private ImageStack applyToGray8(ImageStack marker, ImageStack mask) {
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
//				IJ.showStatus("Geod. Rec. by Dil. Fwd " + (iter + 1));
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
//				IJ.showStatus("Geod. Rec. by Dil. Bwd" + (iter + 1));
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


//	/**
//	 * Update result image using pixels in the upper left neighborhood, using
//	 * the 4-adjacency.
//	 */
//	private void forwardDilationC6() {
//		int width = this.marker.getWidth();
//		int height = this.marker.getHeight();
//
//		// the values associated to each neighbor
//		double v1, v2;
//
//		// the maximal value around current pixel
//		float value;
//
//		if (showProgress) {
//			IJ.showProgress(0, height);
//		}
//
//		// Process first line: consider only the pixel on the left
//		for (int i = 1; i < width; i++) {
//			value = result.getf(i - 1, 0);
//			geodesicDilationUpdateFloat(i, 0, value);
//		}
//
//		// Process all other lines
//		for (int j = 1; j < height; j++) {
//
//			if (showProgress) {
//				IJ.showProgress(j, height);
//			}
//			// process first pixel of current line: consider pixel up
//			value = result.getf(0, j - 1);
//			geodesicDilationUpdate(0, j, value);
//
//			// Process pixels in the middle of the line
//			for (int i = 1; i < width; i++) {
//				v1 = result.getf(i, j - 1);
//				v2 = result.getf(i - 1, j);
//				value = Math.max(v1, v2);
//				geodesicDilationUpdate(i, j, value);
//			}
//		} // end of forward iteration
//	}

	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency.
	 */
	private void forwardDilationC26()
	{
		switch(this.result.getBitDepth())
		{
		case 8:
			forwardDilationC26Gray8();
			break;
		case 16:
			forwardDilationC26Gray16();
			break;
		case 32:
			forwardDilationC26Float();
			break;
		}
	}

	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency.
	 */
	private void forwardDilationC26( ImageStack binaryMask ) 
	{
		switch(this.result.getBitDepth())
		{
		case 8:
			forwardDilationC26Gray8( binaryMask );
			break;
		case 16:
			forwardDilationC26Gray16( binaryMask );
			break;
		case 32:
			forwardDilationC26Float( binaryMask );
			break;
		}
	}
	
	
	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency, assuming pixels are stored in bytes.
	 */
	private void forwardDilationC26Gray8()
	{
		// the maximal value around current pixel
		int maxValue;

		Object[] stack = result.getImageArray();
		byte[] slice;
		
		// Iterate over pixels
		for (int z = 0; z < size3; z++)
		{
			showProgress(z, size3, "z = " + z);
			
			for (int y = 0; y < size2; y++)
			{
				for (int x = 0; x < size1; x++)
				{
					maxValue = (int) result.getVoxel(x, y, z);
					
					// Iterate over neighbors of current pixel
					int zmax = min(z + 1, size3);
					for (int z2 = max(z - 1, 0); z2 < zmax; z2++)
					{
						slice = (byte[]) stack[z2];
						
						int ymax = z2 == z ? y : min(y + 2, size2); 
						for (int y2 = max(y - 1, 0); y2 < ymax; y2++)
						{
							int xmax = (z2 == z && y2 == y) ? x : min(x + 2, size1); 
							for (int x2 = max(x - 1, 0); x2 < xmax; x2++) 
							{
								int neighborValue = slice[y2 * size1 + x2] & 0x00FF;
								if (neighborValue > maxValue)
									maxValue = neighborValue;
							}
						}
					}

					geodesicDilationUpdate(x, y, z, maxValue);
				}
			}
		}
	}
	
	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency, assuming pixels are stored in bytes.
	 */
	private void forwardDilationC26Gray8( ImageStack binaryMask ) 
	{
		// the maximal value around current pixel
		int maxValue;

		Object[] stack = result.getImageArray();
		byte[] slice;
		
		// Iterate over pixels
		for (int z = 0; z < size3; z++) 
		{
			showProgress(z, size3, "z = " + z);
			for (int y = 0; y < size2; y++) 
			{
				for (int x = 0; x < size1; x++)
				{
					
					if( binaryMask.getVoxel(x, y, z) != 0 )
					{
						maxValue = (int) result.getVoxel(x, y, z);

						// Iterate over neighbors of current pixel
						int zmax = min(z + 1, size3);
						for (int z2 = max(z - 1, 0); z2 < zmax; z2++)
						{
							slice = (byte[]) stack[z2];

							int ymax = z2 == z ? y : min(y + 2, size2); 
							for (int y2 = max(y - 1, 0); y2 < ymax; y2++) 
							{
								int xmax = (z2 == z && y2 == y) ? x : min(x + 2, size1); 
								for (int x2 = max(x - 1, 0); x2 < xmax; x2++)
								{
									int neighborValue = slice[y2 * size1 + x2] & 0x00FF;
									if (neighborValue > maxValue)
										maxValue = neighborValue;
								}
							}
						}

						geodesicDilationUpdate(x, y, z, maxValue);
					}
				}
			}
		}
	}

//	/**
//	 */
//	private void forwardDilationC26InitQueueGray8() {
//		// the maximal value around current pixel
//		int currentValue, maxValue;
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
//					currentValue = (int) result.getVoxel(x, y, z);
//					maxValue = currentValue;
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
//								if (neighborValue > maxValue)
//									maxValue = neighborValue;
//							}
//						}
//					}
//
////					geodesicDilationUpdate(x, y, z, maxValue);
//					// update current value only if clamped value is strictly greater
//					int value = Math.min(maxValue, (int) mask.getVoxel(x, y, z));
//					if (value > currentValue) {
//						result.setVoxel(x, y, z, value);
//						
//					}
//				}
//			}
//		}
//	}
	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency, assuming pixels are stored in bytes.
	 */
	private void forwardDilationC26Gray16( ImageStack binaryMask ) 
	{
		// the maximal value around current pixel
		double maxValue;

		Object[] stack = result.getImageArray();
		short[] slice;
		
		// Iterate over pixels
		for (int z = 0; z < size3; z++) 
		{
			showProgress(z, size3, "z = " + z);
			for (int y = 0; y < size2; y++)
			{
				for (int x = 0; x < size1; x++) 
				{
					if( binaryMask.getVoxel(x, y, z) != 0 )
					{
						maxValue = result.getVoxel(x, y, z);

						// Iterate over neighbors of current pixel
						int zmax = min(z + 1, size3);
						for (int z2 = max(z - 1, 0); z2 < zmax; z2++) 
						{
							slice = (short[]) stack[z2];

							int ymax = z2 == z ? y : min(y + 2, size2); 
							for (int y2 = max(y - 1, 0); y2 < ymax; y2++) 
							{
								int xmax = (z2 == z && y2 == y) ? x : min(x + 2, size1); 
								for (int x2 = max(x - 1, 0); x2 < xmax; x2++)
								{
									double neighborValue = slice[y2 * size1 + x2] & 0x00FFFF;
									if (neighborValue > maxValue)
										maxValue = neighborValue;
								}
							}
						}

						geodesicDilationUpdate(x, y, z, maxValue);
					}
				}
			}
		}
	}
	
	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency, assuming pixels are stored in bytes.
	 */
	private void forwardDilationC26Gray16() 
	{
		// the maximal value around current pixel
		double maxValue;

		Object[] stack = result.getImageArray();
		short[] slice;
		
		// Iterate over pixels
		for (int z = 0; z < size3; z++) 
		{
			showProgress(z, size3, "z = " + z);
			for (int y = 0; y < size2; y++) 
			{
				for (int x = 0; x < size1; x++) 
				{
					maxValue = result.getVoxel(x, y, z);
					
					// Iterate over neighbors of current pixel
					int zmax = min(z + 1, size3);
					for (int z2 = max(z - 1, 0); z2 < zmax; z2++) 
					{
						slice = (short[]) stack[z2];
						
						int ymax = z2 == z ? y : min(y + 2, size2); 
						for (int y2 = max(y - 1, 0); y2 < ymax; y2++)
						{
							int xmax = (z2 == z && y2 == y) ? x : min(x + 2, size1); 
							for (int x2 = max(x - 1, 0); x2 < xmax; x2++) 
							{
								double neighborValue = slice[y2 * size1 + x2] & 0x00FFFF;
								if (neighborValue > maxValue)
									maxValue = neighborValue;
							}
						}
					}

					geodesicDilationUpdate(x, y, z, maxValue);
				}
			}
		}
	}

	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency, assuming pixels are stored in bytes.
	 */
	private void forwardDilationC26Float()
	{
		// the maximal value around current pixel
		double maxValue;

		Object[] stack = result.getImageArray();
		float[] slice;
		
		// Iterate over voxels
		for (int z = 0; z < size3; z++) 
		{
			showProgress(z, size3, "z = " + z);
			for (int y = 0; y < size2; y++) 
			{
				for (int x = 0; x < size1; x++)
				{
					maxValue = result.getVoxel(x, y, z);
					
					// Iterate over neighbors of current voxel
					for (int z2 = max(z - 1, 0); z2 <= z; z2++)
					{
						slice = (float[]) stack[z2];
						
						int ymax = z2 == z ? y : min(y + 2, size2); 
						for (int y2 = max(y - 1, 0); y2 < ymax; y2++)
						{
							int xmax = (z2 == z && y2 == y) ? x : min(x + 2, size1); 
							for (int x2 = max(x - 1, 0); x2 < xmax; x2++)
							{
								double neighborValue = slice[y2 * size1 + x2];
								if (neighborValue > maxValue)
									maxValue = neighborValue;
							}
						}
					}

					geodesicDilationUpdate(x, y, z, maxValue);
				}
			}
		}
	}
	
	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency, assuming pixels are stored in bytes.
	 */
	private void forwardDilationC26Float( ImageStack binaryMask ) 
	{
		// the maximal value around current pixel
		double maxValue;

		Object[] stack = result.getImageArray();
		float[] slice;

		// Iterate over voxels
		for (int z = 0; z < size3; z++)
		{
			showProgress(z, size3, "z = " + z);
			for (int y = 0; y < size2; y++)
			{
				for (int x = 0; x < size1; x++)
				{
					if( binaryMask.getVoxel(x, y, z) != 0 )
					{
						maxValue = result.getVoxel(x, y, z);

						// Iterate over neighbors of current voxel
						for (int z2 = max(z - 1, 0); z2 <= z; z2++) 
						{
							slice = (float[]) stack[z2];

							int ymax = z2 == z ? y : min(y + 2, size2); 
							for (int y2 = max(y - 1, 0); y2 < ymax; y2++)
							{
								int xmax = (z2 == z && y2 == y) ? x : min(x + 2, size1); 
								for (int x2 = max(x - 1, 0); x2 < xmax; x2++) 
								{
									double neighborValue = slice[y2 * size1 + x2];
									if (neighborValue > maxValue)
										maxValue = neighborValue;
								}
							}
						}

						geodesicDilationUpdate(x, y, z, maxValue);
					}
				}
			}
		}

	}
	

//	/**
//	 * Update result image using pixels in the upper left neighborhood, using
//	 * the 26-adjacency.
//	 */
//	private void forwardDilationC26Generic() {
//		// the maximal value around current pixel
//		double value;
//
//		if (showProgress) {
//			IJ.showProgress(0, size3);
//		}
//		
//		for (int z = 0; z < size3; z++) {
//			IJ.showProgress(z + 1, size3);
//			System.out.println("z = " + z);
//			for (int y = 0; y < size2; y++) {
//				for (int x = 0; x < size1; x++) {
//					value = getMaxValueForward(x, y, z);
//					geodesicDilationUpdate(x, y, z, value);
//				}
//			}
//		}
//	}


//	/**
//	 * Return maximum value in forward neighborhood
//	 */
//	private double getMaxValueForward(final int x, final int y, final int z) {
//		double max = result.getVoxel(x, y, z);
//
//		int zmax = min(z + 1, size3);
//		for (int z2 = max(z - 1, 0); z2 < zmax; z2++) {
////			ImageProcessor slice = result.getProcessor(z2 + 1);
//			for (int y2 = max(y - 1, 0); y2 <= y; y2++) {
//				int xmax = y2 == y ? x : min(x + 2, size1); 
//				for (int x2 = max(x - 1, 0); x2 < xmax; x2++) {
//					double neighborValue = result.getVoxel(x2, y2, z2);
//					if (neighborValue > max)
//						max = neighborValue;
//				}
//			}
//		}
//		return max;
//	}

//	/**
//	 * Return maximum value in forward neighborhood
//	 */
//	private double getMaxValueForwardSave1(final int x, final int y, final int z) {
//		double max = result.getVoxel(x, y, z);
//
//		int zmax = min(z + 1, size3);
//		for (int z2 = max(z - 1, 0); z2 < zmax; z2++) {
////			ImageProcessor slice = result.getProcessor(z2 + 1);
//			for (int y2 = max(y - 1, 0); y2 <= y; y2++) {
//				int xmax = y2 == y ? x : min(x + 2, size1); 
//				for (int x2 = max(x - 1, 0); x2 < xmax; x2++) {
//					double neighborValue = result.getVoxel(x2, y2, z2);
//					if (neighborValue > max)
//						max = neighborValue;
//				}
//			}
//		}
//		return max;
//	}

//	/**
//	 * Return maximum value in forward neighborhood
//	 */
//	private double getMaxValueForwardOld(final int x, final int y, final int z) {
//		double max = result.getVoxel(x, y, z);
//
//		for (int w = -1; w <= 1; ++w) {
//			for (int v = -1; v <= 0; ++v) {
//				final int xMax = v < 0 ? 1 : (w < 0 ? 0 : -1);
//
//				for (int u = -1; u <= xMax; u++) {
//					final int x2 = x + u;
//					final int y2 = y + v;
//					final int z2 = z + w;
//
//					if (x2 >= 0 && x2 < size1 && y2 >= 0 && y2 < size2
//							&& z2 >= 0 && z2 < size3) {
//						double neighborValue = result.getVoxel(x2, y2, z2);
//						if (neighborValue > max)
//							max = neighborValue;
//					}
//				}
//			}
//		}
//
//		return max;
//	}

	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency.
	 */
	private void backwardDilationC26()
	{
		switch(this.result.getBitDepth()) 
		{
		case 8: backwardDilationC26Gray8(); break;
		default: backwardDilationC26Generic(); break;
		}
	}
	
	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency.
	 */
	private void backwardDilationC26( ImageStack binaryMask ) 
	{
		switch(this.result.getBitDepth()) 
		{
		case 8: 
			backwardDilationC26Gray8( binaryMask ); 
			break;
		default: 
			backwardDilationC26Generic( binaryMask ); 
			break;
		}
	}
	
	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency.
	 */
	private void backwardDilationC26Gray8() 
	{
		// the maximal value around current pixel
		int maxValue;

		Object[] stack = result.getImageArray();
		byte[] slice;

		// Iterate over voxels
		for (int z = size3 - 1; z >= 0; z--) 
		{
			showProgress(size3 - 1 - z, size3);
			for (int y = size2 - 1; y >= 0; y--)
			{
				for (int x = size1 - 1; x >= 0; x--) 
				{
					maxValue = (int) result.getVoxel(x, y, z);
					
					// Iterate over neighbors of current voxel
					int zmin = max(z - 1, 0);
					for (int z2 = min(z + 1, size3 - 1); z2 >= zmin; z2--)
					{
						slice = (byte[]) stack[z2];
						
						int ymin = z2 == z ? y : max(y - 1, 0); 
						for (int y2 = min(y + 1, size2 - 1); y2 >= ymin; y2--)
						{
							int xmin = (z2 == z && y2 == y) ? x : max(x - 1, 0); 
							for (int x2 = min(x + 1, size1 - 1); x2 >= xmin; x2--) 
							{
								int neighborValue = slice[y2 * size1 + x2] & 0x00FF;
								if (neighborValue > maxValue)
									maxValue = neighborValue;
							}
						}
					}
					
					geodesicDilationUpdate(x, y, z, maxValue);
				}
			}
		}
		
	}		
	
	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency.
	 */
	private void backwardDilationC26Gray8( ImageStack binaryMask ) 
	{
		// the maximal value around current pixel
		int maxValue;

		Object[] stack = result.getImageArray();
		byte[] slice;

		// Iterate over voxels
		for (int z = size3 - 1; z >= 0; z--)
		{
			showProgress(size3 - 1 - z, size3);
			for (int y = size2 - 1; y >= 0; y--) 
			{
				for (int x = size1 - 1; x >= 0; x--) 
				{
					if( binaryMask.getVoxel(x, y, z) != 0 )
					{
						maxValue = (int) result.getVoxel(x, y, z);

						// Iterate over neighbors of current voxel
						int zmin = max(z - 1, 0);
						for (int z2 = min(z + 1, size3 - 1); z2 >= zmin; z2--) 
						{
							slice = (byte[]) stack[z2];

							int ymin = z2 == z ? y : max(y - 1, 0); 
							for (int y2 = min(y + 1, size2 - 1); y2 >= ymin; y2--) 
							{
								int xmin = (z2 == z && y2 == y) ? x : max(x - 1, 0); 
								for (int x2 = min(x + 1, size1 - 1); x2 >= xmin; x2--) 
								{
									int neighborValue = slice[y2 * size1 + x2] & 0x00FF;
									if (neighborValue > maxValue)
										maxValue = neighborValue;
								}
							}
						}

						geodesicDilationUpdate(x, y, z, maxValue);
					}
				}
			}
		}
		
	}	

	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency.
	 */
	private void backwardDilationC26Generic()
	{
		// the maximal value around current pixel
		double value;

		for (int k = size3 - 1; k >= 0; k--)
		{
			showProgress(size3 - 1 - k, size3);

			for (int j = size2 - 1; j >= 0; j--)
			{
				for (int i = size1 - 1; i >= 0; i--)
				{
					value = getMaxValueBackward(i, j, k);
					geodesicDilationUpdate(i, j, k, value);
				}
			}
		}
	}		

	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 26-adjacency.
	 */
	private void backwardDilationC26Generic( ImageStack binaryMask ) 
	{
		// the maximal value around current pixel
		double value;

		for (int k = size3 - 1; k >= 0; k--) {
			showProgress(size3 - 1 - k, size3);

			for (int j = size2 - 1; j >= 0; j--)
				for (int i = size1 - 1; i >= 0; i--) 
				{
					if( binaryMask.getVoxel( i, j, k ) != 0 )
					{
						value = getMaxValueBackward(i, j, k);
						geodesicDilationUpdate(i, j, k, value);
					}
				}
		}
	}	
	
	
	/**
	 * Return maximum value in backward neighborhood
	 */
	private double getMaxValueBackward(final int x, final int y, final int z) 
	{
		double max = result.getVoxel(x, y, z);

		for (int w = -1; w <= 1; ++w) 
		{
			for (int v = 0; v <= 1; ++v) 
			{
				final int minX = v > 0 ? -1 : (w > 0 ? 0 : 1);

				for (int u = minX; u <= 1; u++)
				{
					final int x2 = x + u;
					final int y2 = y + v;
					final int z2 = z + w;

					if (x2 >= 0 && x2 < size1 && y2 >= 0 && y2 < size2
							&& z2 >= 0 && z2 < size3)
					{
						double neighborValue = result.getVoxel(x2, y2, z2);
						if (neighborValue > max)
							max = neighborValue;
					}
				}

			}
		}

		return max;
	}
	
	/**
	 * Update the pixel at position (i,j) with the value <code>value<value>. 
	 * First computes the min of value and the value of the mask.
	 * Check if value is greater than the current value at position (i,j). 
	 * If new value is lower than current value, do nothing.
	 */
	private void geodesicDilationUpdate(int i, int j, int k, double value)
	{
		// update current value only if value is strictly greater
		value = Math.min(value, mask.getVoxel(i, j, k));
		if (value > result.getVoxel(i, j, k))
		{
			modif = true;
			result.setVoxel(i, j, k, value);
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
		this.size1 	= marker.getWidth();
		this.size2 	= marker.getHeight();
		this.size3 	= marker.getSize();
		if (size1 != mask.getWidth() || size2 != mask.getHeight() || size3 != mask.getSize())
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

		// Create result image the same size as the mask and marker images
		this.result = ImageStack.create(size1, size2, size3, mask.getBitDepth());

		// Initialize the result image with the minimum value of marker and mask
		// images
		for (int z = 0; z < size3; z++) 
		{
			for (int y = 0; y < size2; y++)
			{
				for (int x = 0; x < size1; x++) 
				{
					if( binaryMask.getVoxel(x, y, z) != 0 )
						this.result.setVoxel(x, y, z, 
								Math.min(this.marker.getVoxel(x, y, z),
										this.mask.getVoxel(x, y, z)));
				}
			}
		}

		// Count the number of iterations for eventually displaying progress
		int iter = 0;

		// Iterate forward and backward propagations until no more pixel have been modified
		do 
		{
			modif = false;

			// Display current status
			trace("Forward iteration " + iter);
			showStatus("Geod. Rec. by Dil. Fwd " + (iter + 1));
			
			// forward iteration
			//					switch (connectivity) {
			//					case 6:
			//						forwardDilationC6(); 
			//						break;
			//					case 26:
			forwardDilationC26( binaryMask ); 
			//						break;
			//					}

			// Display current status
			trace("Backward iteration " + iter);
			showStatus("Geod. Rec. by Dil. Bwd " + (iter + 1));
			
			// backward iteration
			//					switch (connectivity) {
			//					case 4:
			//						backwardDilationC6();
			////						break;
			//					case 8:	
			backwardDilationC26( binaryMask ); 
			//						break;
			//					}

			iter++;
		} while (modif);

		return this.result;
	}
}
