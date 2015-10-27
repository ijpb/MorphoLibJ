/**
 * 
 */
package inra.ijpb.morphology.geodrec;

import static java.lang.Math.min;
import ij.IJ;
import ij.process.ImageProcessor;
import ij.process.FloatProcessor;
import inra.ijpb.algo.AlgoStub;

/**
 * Geodesic reconstruction for planar images, using scanning algorithm.
 * 
 * This class performs the algorithm on the two instances of ImageProcessor
 * kept in it. Works for integer as well as for floating-point images.
 * 
 * @see GeodesicReconstructionByErosion
 * @author David Legland
 *
 */
public class GeodesicReconstructionScanning extends AlgoStub implements
		GeodesicReconstructionAlgo 
		{

	// ------------------------
	// Class fields
	
	GeodesicReconstructionType reconstructionType = GeodesicReconstructionType.BY_DILATION;

	ImageProcessor marker;
	ImageProcessor mask;
	
	ImageProcessor result;
	
	int connectivity = 4;
	
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

	// ------------------------
	// Constructors
	
	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * using the default connectivity 4.
	 */
	public GeodesicReconstructionScanning() {
	}
	
	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * that specifies the type of reconstruction, and using the connectivity 4.
	 */
	public GeodesicReconstructionScanning(GeodesicReconstructionType type) 
	{
		this.reconstructionType = type;
	}

	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * that specifies the connectivity to use.
	 */
	public GeodesicReconstructionScanning(int connectivity)
	{
		this.connectivity = connectivity;
	}

	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * that specifies the type of reconstruction, and the connectivity to use.
	 */
	public GeodesicReconstructionScanning(GeodesicReconstructionType type, int connectivity) 
	{
		this.reconstructionType = type;
		this.connectivity = connectivity;
	}

	// ------------------------
	// Accesors and mutators
	
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

	public int getConnectivity()
	{
		return this.connectivity;
	}
	
	public void setConnectivity(int conn)
{
		this.connectivity = conn;
	}
	

	// ------------------------
	// Methods implementing the GeodesicReconstruction interface
	
	/**
	 * Run the reconstruction by dilation algorithm using the images specified
	 * as argument.
	 */
	public ImageProcessor applyTo(ImageProcessor marker, ImageProcessor mask)
	{
		// Keep references to input images
		this.marker = marker;
		this.mask = mask;
		
		// Check sizes are consistent
		int width = marker.getWidth();
		int height = marker.getHeight();
		if (width != mask.getWidth() || height != mask.getHeight()) 
		{
			throw new IllegalArgumentException("Marker and Mask images must have the same size");
		}
		
		// Check connectivity has a correct value
		if (connectivity != 4 && connectivity != 8)
		{
			throw new RuntimeException(
					"Connectivity for planar images must be either 4 or 8, not "
							+ connectivity);
		}

		// Create result image the same size as marker image
		this.result = this.marker.createProcessor(width, height);
	
		// Initialize the result image with the minimum value of marker and mask
		// images
		for (int y = 0; y < height; y++) 
		{
			for (int x = 0; x < width; x++) 
			{
				this.result.set(x, y,
						Math.min(this.marker.get(x, y), this.mask.get(x, y)));
			}
		}

		// Count the number of iterations for eventually displaying progress
		int iter = 0;

		boolean isFloat = (mask instanceof FloatProcessor);
		
		// Iterate forward and backward propagations until no more pixel have been modified
		do {
			modif = false;

			// Display current status
			if (verbose)
			{
				System.out.println("Forward iteration " + iter);
			}
			if (showStatus)
			{
				IJ.showStatus("Geod. Rec. by Dil. Fwd " + (iter + 1));
			}
			
			// forward iteration
			switch (connectivity) 
			{
			case 4:
				if (isFloat)
					forwardScanC4Float();
				else
					forwardScanC4(); 
				break;
			case 8:	
				if (isFloat)
					forwardScanC8Float();
				else
					forwardScanC8(); 
				break;
			}

			// Display current status
			if (verbose) 
			{
				System.out.println("Backward iteration " + iter);
			}
			if (showStatus)
			{
				IJ.showStatus("Geod. Rec. by Dil. Bwd " + (iter + 1));
			}
			
			// backward iteration
			switch (connectivity)
			{
			case 4:
				if (isFloat)
					backwardScanC4Float();
				else
					backwardScanC4(); 
				break;
			case 8:	
				if (isFloat)
					backwardScanC8Float();
				else
					backwardScanC8(); 
				break;
			}

			iter++;
		} while (modif);
	
		return this.result;
	}

	/**
	 * Update result image using pixels in the upper left neighborhood,
	 * using the 4-adjacency.
	 */
	private void forwardScanC4() 
	{
		final int sign = this.reconstructionType.getSign();
		
		int width = this.marker.getWidth();
		int height = this.marker.getHeight();
		
		if (showProgress)
		{
			IJ.showProgress(0, height);
		}
		
		// Process all other lines
		for (int y = 0; y < height; y++) 
		{
			
			if (showProgress)
			{
				IJ.showProgress(y, height);
			}
	
			// Process pixels in the middle of the line
			for (int x = 0; x < width; x++) 
			{
				int currentValue = result.get(x, y) * sign;
				int maxValue = currentValue;
				
				if (x > 0)
					maxValue = Math.max(maxValue, result.get(x-1, y) * sign);
				if (y > 0)
					maxValue = Math.max(maxValue, result.get(x, y-1) * sign);
				
				// update value of current pixel
				maxValue = min(maxValue, mask.get(x, y) * sign);
				if (maxValue > currentValue) 
				{
					result.set(x, y, maxValue * sign);
					modif = true;
				}
			}
		} // end of forward iteration
	}

	/**
	 * Update result image using pixels in the upper left neighborhood,
	 * using the 4-adjacency.
	 */
	private void forwardScanC4Float()
	{
		final float sign = this.reconstructionType.getSign();
		
		int width = this.marker.getWidth();
		int height = this.marker.getHeight();
		
		if (showProgress)
		{
			IJ.showProgress(0, height);
		}
		
		// Process all other lines
		for (int y = 0; y < height; y++) 
		{
			
			if (showProgress)
			{
				IJ.showProgress(y, height);
			}
	
			// Process pixels in the middle of the line
			for (int x = 0; x < width; x++) 
			{
				float currentValue = result.getf(x, y) * sign;
				float maxValue = currentValue;
				
				if (x > 0)
					maxValue = Math.max(maxValue, result.getf(x-1, y) * sign);
				if (y > 0)
					maxValue = Math.max(maxValue, result.getf(x, y-1) * sign);
				
				// update value of current pixel
				maxValue = min(maxValue, mask.getf(x, y) * sign);
				if (maxValue > currentValue) 
				{
					result.setf(x, y, maxValue * sign);
					modif = true;
				}
			}
		} // end of forward iteration
	}

	/**
	 * Update result image using pixels in the upper left neighborhood,
	 * using the 8-adjacency.
	 */
	private void forwardScanC8()
	{
		final int sign = this.reconstructionType.getSign();
		
		int width = this.marker.getWidth();
		int height = this.marker.getHeight();
		
		if (showProgress)
		{
			IJ.showProgress(0, height);
		}
		
		// Process all other lines
		for (int y = 0; y < height; y++)
		{
			
			if (showProgress) 
			{
				IJ.showProgress(y, height);
			}

			// Process pixels in the middle of the line
			for (int x = 0; x < width; x++) 
			{
				int currentValue = result.get(x, y) * sign;
				int maxValue = currentValue;
				
				if (y > 0) 
				{
					// process the 3 values on the line above current pixel
					if (x > 0)
						maxValue = Math.max(maxValue, result.get(x-1, y-1) * sign);
					maxValue = Math.max(maxValue, result.get(x, y-1) * sign);
					if (x < width - 1)
						maxValue = Math.max(maxValue, result.get(x+1, y-1) * sign);
				}
				if (x > 0)
					maxValue = Math.max(maxValue, result.get(x-1, y) * sign);
				
				// update value of current pixel
				maxValue = min(maxValue, mask.get(x, y) * sign);
				if (maxValue > currentValue) 
				{
					result.set(x, y, maxValue * sign);
					modif = true;
				}
			}
		} // end of forward iteration
	}
	
	/**
	 * Update result image using pixels in the upper left neighborhood,
	 * using the 8-adjacency.
	 */
	private void forwardScanC8Float()
	{
		final float sign = this.reconstructionType.getSign();
		
		int width = this.marker.getWidth();
		int height = this.marker.getHeight();
		
		if (showProgress)
		{
			IJ.showProgress(0, height);
		}
		
		// Process all other lines
		for (int y = 0; y < height; y++)
		{
			
			if (showProgress) 
			{
				IJ.showProgress(y, height);
			}

			// Process pixels in the middle of the line
			for (int x = 0; x < width; x++) 
			{
				float currentValue = result.getf(x, y) * sign;
				float maxValue = currentValue;
				
				if (y > 0) 
				{
					// process the 3 values on the line above current pixel
					if (x > 0)
						maxValue = Math.max(maxValue, result.getf(x-1, y-1) * sign);
					maxValue = Math.max(maxValue, result.getf(x, y-1) * sign);
					if (x < width - 1)
						maxValue = Math.max(maxValue, result.getf(x+1, y-1) * sign);
				}
				if (x > 0)
					maxValue = Math.max(maxValue, result.getf(x-1, y) * sign);
				
				// update value of current pixel
				maxValue = min(maxValue, mask.getf(x, y) * sign);
				if (maxValue > currentValue) 
				{
					result.setf(x, y, maxValue * sign);
					modif = true;
				}
			}
		} // end of forward iteration
	}

	/**
	 * Update result image using pixels in the lower-right neighborhood, 
	 * using the 4-adjacency.
	 */
	private void backwardScanC4() 
	{
		final int sign = this.reconstructionType.getSign();
		
		int width = this.marker.getWidth();
		int height = this.marker.getHeight();
	
		if (showProgress)
		{
			IJ.showProgress(0, height);
		}
		
		// Process regular lines
		for (int y = height-1; y >= 0; y--)
		{
	
			if (showProgress) 
			{
				IJ.showProgress(height-1-y, height);
			}
	
			// Process pixels in the middle of the current line
			// consider pixels on the right and below
			for (int x = width - 1; x >= 0; x--) 
			{

				int currentValue = result.get(x, y) * sign;
				int maxValue = currentValue;
				
				if (x < width - 1)
					maxValue = Math.max(maxValue, result.get(x+1, y) * sign);
				if (y < height - 1)
					maxValue = Math.max(maxValue, result.get(x, y+1) * sign);
				
				// update value of current pixel
				maxValue = min(maxValue, mask.get(x, y) * sign);
				if (maxValue > currentValue) 
				{
					result.set(x, y, maxValue * sign);
					modif = true;
				}
			}
		} 
	} // end of backward iteration

	/**
	 * Update result image using pixels in the lower-right neighborhood, 
	 * using the 4-adjacency.
	 */
	private void backwardScanC4Float()
	{
		final float sign = this.reconstructionType.getSign();
		
		int width = this.marker.getWidth();
		int height = this.marker.getHeight();
	
		if (showProgress)
		{
			IJ.showProgress(0, height);
		}
		
		// Process regular lines
		for (int y = height-1; y >= 0; y--)
		{
	
			if (showProgress) 
			{
				IJ.showProgress(height-1-y, height);
			}
	
			// Process pixels in the middle of the current line
			// consider pixels on the right and below
			for (int x = width - 1; x >= 0; x--) 
			{

				float currentValue = result.getf(x, y) * sign;
				float maxValue = currentValue;
				
				if (x < width - 1)
					maxValue = Math.max(maxValue, result.getf(x+1, y) * sign);
				if (y < height - 1)
					maxValue = Math.max(maxValue, result.getf(x, y+1) * sign);
				
				// update value of current pixel
				maxValue = min(maxValue, mask.getf(x, y) * sign);
				if (maxValue > currentValue) 
				{
					result.setf(x, y, maxValue * sign);
					modif = true;
				}
			}
		} 
	} // end of backward iteration

	/**
	 * Update result image using pixels in the lower-right neighborhood, using
	 * the 8-adjacency.
	 */
	private void backwardScanC8() 
	{
		final int sign = this.reconstructionType.getSign();
		
		int width = this.marker.getWidth();
		int height = this.marker.getHeight();
		
		if (showProgress)
		{
			IJ.showProgress(0, height);
		}
		
		// Process regular lines
		for (int y = height-1; y >= 0; y--)
		{

			if (showProgress) 
			{
				IJ.showProgress(height-1-y, height);
			}

			// Process pixels in the middle of the current line
			for (int x = width - 1; x >= 0; x--)
			{
				int currentValue = result.get(x, y) * sign;
				int maxValue = currentValue;
				
				if (y < height - 1)
				{
					// process the 3 values on the line below current pixel
					if (x > 0)
						maxValue = Math.max(maxValue, result.get(x-1, y+1) * sign);
					maxValue = Math.max(maxValue, result.get(x, y+1) * sign);
					if (x < width - 1)
						maxValue = Math.max(maxValue, result.get(x+1, y+1) * sign);
				}
				if (x < width - 1)
					maxValue = Math.max(maxValue, result.get(x+1, y) * sign);
				
				// update value of current pixel
				maxValue = min(maxValue, mask.get(x, y) * sign);
				if (maxValue > currentValue) 
				{
					result.set(x, y, maxValue * sign);
					modif = true;
				}
			}
		} 
	} // end of backward iteration

	/**
	 * Update result image using pixels in the lower-right neighborhood, using
	 * the 8-adjacency.
	 */
	private void backwardScanC8Float()
	{
		final float sign = this.reconstructionType.getSign();
		
		int width = this.marker.getWidth();
		int height = this.marker.getHeight();
		
		if (showProgress)
		{
			IJ.showProgress(0, height);
		}
		
		// Process regular lines
		for (int y = height-1; y >= 0; y--)
		{

			if (showProgress) 
			{
				IJ.showProgress(height-1-y, height);
			}

			// Process pixels in the middle of the current line
			for (int x = width - 1; x >= 0; x--)
			{
				float currentValue = result.getf(x, y) * sign;
				float maxValue = currentValue;
				
				if (y < height - 1)
				{
					// process the 3 values on the line below current pixel
					if (x > 0)
						maxValue = Math.max(maxValue, result.getf(x-1, y+1) * sign);
					maxValue = Math.max(maxValue, result.getf(x, y+1) * sign);
					if (x < width - 1)
						maxValue = Math.max(maxValue, result.getf(x+1, y+1) * sign);
				}
				if (x < width - 1)
					maxValue = Math.max(maxValue, result.getf(x+1, y) * sign);
				
				// update value of current pixel
				maxValue = min(maxValue, mask.getf(x, y) * sign);
				if (maxValue > currentValue) 
				{
					result.setf(x, y, maxValue * sign);
					modif = true;
				}
			}
		} 
	} // end of backward iteration
}
