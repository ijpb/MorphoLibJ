/**
 * 
 */
package inra.ijpb.morphology.geodrec;

import static java.lang.Math.min;
import ij.IJ;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

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
public class GeodesicReconstructionScanning extends GeodesicReconstructionAlgoStub
{
	// ==================================================
	// Class variables
	
	GeodesicReconstructionType reconstructionType = GeodesicReconstructionType.BY_DILATION;

	ImageProcessor marker;
	ImageProcessor mask;
	
	ImageProcessor result;
	
	/** image width */
	int sizeX = 0;
	
	/** image height */
	int sizeY = 0;

	/**
	 * The flag indicating whether the result image has been modified during
	 * last image scan
	 */
	boolean modif;

	
	// ==================================================
	// Constructors 

	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * using the default connectivity 4.
	 */
	public GeodesicReconstructionScanning()
	{
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

	
	// ==================================================
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
		this.sizeX = marker.getWidth();
		this.sizeY = marker.getHeight();
		if (this.sizeX != mask.getWidth() || this.sizeY != mask.getHeight()) 
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
		this.result = this.marker.createProcessor(sizeX, sizeY);
	
		// Initialize the result image with the minimum value of marker and mask
		// images
		for (int y = 0; y < sizeY; y++) 
		{
			for (int x = 0; x < sizeX; x++) 
			{
				this.result.set(x, y,
						Math.min(this.marker.get(x, y), this.mask.get(x, y)));
			}
		}

		boolean isInteger = !(mask instanceof FloatProcessor);
		
		// Initialize the result image
		if (isInteger)
		{
			initializeResult();
		}
		else
		{
			initializeResultFloat();
		}

		// Count the number of iterations for eventually displaying progress
		int iter = 0;

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
				IJ.showStatus("Geod. Rec. Fwd " + (iter + 1));
			}
			
			// forward iteration
			switch (connectivity) 
			{
			case 4:
				if (isInteger)
					forwardScanC4();
				else
					forwardScanC4Float(); 
				break;
			case 8:	
				if (isInteger)
					forwardScanC8();
				else
					forwardScanC8Float(); 
				break;
			}

			// Display current status
			if (verbose) 
			{
				System.out.println("Backward iteration " + iter);
			}
			if (showStatus)
			{
				IJ.showStatus("Geod. Rec. Bwd " + (iter + 1));
			}
			
			// backward iteration
			switch (connectivity)
			{
			case 4:
				if (isInteger)
					backwardScanC4();
				else
					backwardScanC4Float(); 
				break;
			case 8:	
				if (isInteger)
					backwardScanC8();
				else
					backwardScanC8Float(); 
				break;
			}

			iter++;
		} while (modif);
	
		return this.result;
	}

	private void initializeResult()
	{
		// Create result image the same size as marker image
		this.result = this.marker.createProcessor(this.sizeX, this.sizeY);
	
		int sign = this.reconstructionType.getSign();
		for (int y = 0; y < this.sizeY; y++) 
		{
			for (int x = 0; x < this.sizeX; x++) 
			{
				int v1 = this.marker.get(x, y) * sign; 
				int v2 = this.mask.get(x, y) * sign; 
				this.result.set(x, y, Math.min(v1, v2) * sign);
			}
		}		
	}
	
	private void initializeResultFloat()
	{
		// Create result image the same size as marker image
		this.result = this.marker.createProcessor(this.sizeX, this.sizeY);
	
		float sign = this.reconstructionType.getSign();
		for (int y = 0; y < this.sizeY; y++) 
		{
			for (int x = 0; x < this.sizeX; x++) 
			{
				float v1 = this.marker.getf(x, y) * sign; 
				float v2 = this.mask.getf(x, y) * sign; 
				this.result.setf(x, y, Math.min(v1, v2) * sign);
			}
		}
	}

	/**
	 * Update result image using pixels in the upper left neighborhood,
	 * using the 4-adjacency.
	 */
	private void forwardScanC4() 
	{
		final int sign = this.reconstructionType.getSign();
		
		if (showProgress)
		{
			IJ.showProgress(0, this.sizeY);
		}
		
		// Process all other lines
		for (int y = 0; y < this.sizeY; y++) 
		{
			
			if (showProgress)
			{
				IJ.showProgress(y, this.sizeY);
			}
	
			// Process pixels in the middle of the line
			for (int x = 0; x < this.sizeX; x++) 
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
		
		if (showProgress)
		{
			IJ.showProgress(0, this.sizeY);
		}
		
		// Process all other lines
		for (int y = 0; y < this.sizeY; y++) 
		{
			
			if (showProgress)
			{
				IJ.showProgress(y, this.sizeY);
			}
	
			// Process pixels in the middle of the line
			for (int x = 0; x < this.sizeX; x++) 
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
		
		if (showProgress)
		{
			IJ.showProgress(0, this.sizeY);
		}
		
		// Process all other lines
		for (int y = 0; y < this.sizeY; y++)
		{
			
			if (showProgress) 
			{
				IJ.showProgress(y, this.sizeY);
			}

			// Process pixels in the middle of the line
			for (int x = 0; x < this.sizeX; x++) 
			{
				int currentValue = result.get(x, y) * sign;
				int maxValue = currentValue;
				
				if (y > 0) 
				{
					// process the 3 values on the line above current pixel
					if (x > 0)
						maxValue = Math.max(maxValue, result.get(x-1, y-1) * sign);
					maxValue = Math.max(maxValue, result.get(x, y-1) * sign);
					if (x < this.sizeX - 1)
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
		
		if (showProgress)
		{
			IJ.showProgress(0, this.sizeY);
		}
		
		// Process all other lines
		for (int y = 0; y < this.sizeY; y++)
		{
			
			if (showProgress) 
			{
				IJ.showProgress(y, this.sizeY);
			}

			// Process pixels in the middle of the line
			for (int x = 0; x < this.sizeX; x++) 
			{
				float currentValue = result.getf(x, y) * sign;
				float maxValue = currentValue;
				
				if (y > 0) 
				{
					// process the 3 values on the line above current pixel
					if (x > 0)
						maxValue = Math.max(maxValue, result.getf(x-1, y-1) * sign);
					maxValue = Math.max(maxValue, result.getf(x, y-1) * sign);
					if (x < this.sizeX - 1)
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
		
		if (showProgress)
		{
			IJ.showProgress(0, this.sizeY);
		}
		
		// Process regular lines
		for (int y = this.sizeY-1; y >= 0; y--)
		{
	
			if (showProgress) 
			{
				IJ.showProgress(this.sizeY-1-y, this.sizeY);
			}
	
			// Process pixels in the middle of the current line
			// consider pixels on the right and below
			for (int x = this.sizeX - 1; x >= 0; x--) 
			{

				int currentValue = result.get(x, y) * sign;
				int maxValue = currentValue;
				
				if (x < this.sizeX - 1)
					maxValue = Math.max(maxValue, result.get(x+1, y) * sign);
				if (y < this.sizeY - 1)
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
		
		if (showProgress)
		{
			IJ.showProgress(0, this.sizeY);
		}
		
		// Process regular lines
		for (int y = this.sizeY-1; y >= 0; y--)
		{
	
			if (showProgress) 
			{
				IJ.showProgress(this.sizeY-1-y, this.sizeY);
			}
	
			// Process pixels in the middle of the current line
			// consider pixels on the right and below
			for (int x = this.sizeX - 1; x >= 0; x--) 
			{

				float currentValue = result.getf(x, y) * sign;
				float maxValue = currentValue;
				
				if (x < this.sizeX - 1)
					maxValue = Math.max(maxValue, result.getf(x+1, y) * sign);
				if (y < this.sizeY - 1)
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
		
		if (showProgress)
		{
			IJ.showProgress(0, this.sizeY);
		}
		
		// Process regular lines
		for (int y = this.sizeY-1; y >= 0; y--)
		{

			if (showProgress) 
			{
				IJ.showProgress(this.sizeY-1-y, this.sizeY);
			}

			// Process pixels in the middle of the current line
			for (int x = this.sizeX - 1; x >= 0; x--)
			{
				int currentValue = result.get(x, y) * sign;
				int maxValue = currentValue;
				
				if (y < this.sizeY - 1)
				{
					// process the 3 values on the line below current pixel
					if (x > 0)
						maxValue = Math.max(maxValue, result.get(x-1, y+1) * sign);
					maxValue = Math.max(maxValue, result.get(x, y+1) * sign);
					if (x < this.sizeX - 1)
						maxValue = Math.max(maxValue, result.get(x+1, y+1) * sign);
				}
				if (x < this.sizeX - 1)
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
		
		if (showProgress)
		{
			IJ.showProgress(0, this.sizeY);
		}
		
		// Process regular lines
		for (int y = this.sizeY-1; y >= 0; y--)
		{

			if (showProgress) 
			{
				IJ.showProgress(this.sizeY-1-y, this.sizeY);
			}

			// Process pixels in the middle of the current line
			for (int x = this.sizeX - 1; x >= 0; x--)
			{
				float currentValue = result.getf(x, y) * sign;
				float maxValue = currentValue;
				
				if (y < this.sizeY - 1)
				{
					// process the 3 values on the line below current pixel
					if (x > 0)
						maxValue = Math.max(maxValue, result.getf(x-1, y+1) * sign);
					maxValue = Math.max(maxValue, result.getf(x, y+1) * sign);
					if (x < this.sizeX - 1)
						maxValue = Math.max(maxValue, result.getf(x+1, y+1) * sign);
				}
				if (x < this.sizeX - 1)
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
