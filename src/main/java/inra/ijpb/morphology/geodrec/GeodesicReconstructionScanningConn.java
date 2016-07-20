/**
 * 
 */
package inra.ijpb.morphology.geodrec;

import static java.lang.Math.min;

import java.util.Collection;

import ij.IJ;
import ij.process.ImageProcessor;
import ij.process.FloatProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.data.Cursor2D;
import inra.ijpb.data.Connectivity2D;

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
public class GeodesicReconstructionScanningConn extends AlgoStub implements
		GeodesicReconstructionAlgo
{
	// ==================================================
	// Class variables
	
	GeodesicReconstructionType reconstructionType = GeodesicReconstructionType.BY_DILATION;

	ImageProcessor marker;
	ImageProcessor mask;
	
	ImageProcessor result;
	
	Connectivity2D connectivity = Connectivity2D.C4;
	
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

	// ==================================================
	// Constructors 
	
	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * using the default connectivity 4.
	 */
	public GeodesicReconstructionScanningConn()
	{
	}

	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * that specifies the type of reconstruction, and using the connectivity 4.
	 */
	public GeodesicReconstructionScanningConn(GeodesicReconstructionType type) 
	{
		this.reconstructionType = type;
	}

	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * that specifies the connectivity to use.
	 */
	public GeodesicReconstructionScanningConn(int connectivity)
	{
		setConnectivity(connectivity);
	}

	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * that specifies the type of reconstruction, and the connectivity to use.
	 */
	public GeodesicReconstructionScanningConn(GeodesicReconstructionType type, Connectivity2D connectivity) 
	{
		this.reconstructionType = type;
		this.connectivity = connectivity;
	}

	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * that specifies the type of reconstruction, and the connectivity to use.
	 */
	public GeodesicReconstructionScanningConn(GeodesicReconstructionType type, int connectivity) 
	{
		this.reconstructionType = type;
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

	public int getConnectivity()
	{
		return this.connectivity == Connectivity2D.C4 ? 4 : 8;
	}
	
	public void setConnectivity(int conn)
	{
		switch(conn)
		{
		case 4:
			this.connectivity = Connectivity2D.C4;
			break;
		case 8:
			this.connectivity = Connectivity2D.C4;
			break;
		default:
			throw new IllegalArgumentException("Connectivity must be either 4 or 6");
		}
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
		int width = marker.getWidth();
		int height = marker.getHeight();
		if (width != mask.getWidth() || height != mask.getHeight()) 
		{
			throw new IllegalArgumentException("Marker and Mask images must have the same size");
		}
		
		boolean isFloat = (mask instanceof FloatProcessor);
		// Initialize the result image
		if (!isFloat)
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
		modif = true;
		while(modif) 
		{
			modif = false;

			// Display current status
			fireStatusChanged(this, "Forward iteration " + iter);
			
			// forward iteration
			if (isFloat)
			{
				forwardScanFloat();
			} 
			else
			{
				forwardScan();
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
			if (isFloat)
			{
				backwardScanFloat();
			} 
			else
			{
				backwardScan();
			}

			iter++;
		};
	
		return this.result;
	}

	private void initializeResult()
	{
		// Create result image the same size as marker image
		int sizeX = this.mask.getWidth();
		int sizeY = this.mask.getHeight();
		this.result = this.mask.createProcessor(sizeX, sizeY);
	
		int sign = this.reconstructionType.getSign();
		for (int y = 0; y < sizeY; y++) 
		{
			for (int x = 0; x < sizeX; x++) 
			{
				int v1 = this.marker.get(x, y) * sign; 
				int v2 = this.mask.get(x, y) * sign; 
				this.result.set(x, y, Math.min(v1, v2) * sign);
			}
		}		
	}
	
	private void initializeResultFloat()
	{
		// Create result image the same size and type as marker image
		int sizeX = this.mask.getWidth();
		int sizeY = this.mask.getHeight();
		this.result = this.mask.createProcessor(sizeX, sizeY);
	
		float sign = this.reconstructionType.getSign();
		for (int y = 0; y < sizeY; y++) 
		{
			for (int x = 0; x < sizeX; x++) 
			{
				float v1 = this.marker.getf(x, y) * sign; 
				float v2 = this.mask.getf(x, y) * sign; 
				this.result.setf(x, y, Math.min(v1, v2) * sign);
			}
		}
	}

	/**
	 * Update result image using pixels in the upper left neighborhood,
	 * using any adjacency.
	 */
	private void forwardScan() 
	{
		final int sign = this.reconstructionType.getSign();
		
		int width = this.marker.getWidth();
		int height = this.marker.getHeight();

		Collection<Cursor2D> offsets = this.connectivity.getForwardConnectivity().getOffsets();
		
		fireProgressChanged(this, 0, height);
		
		// Process all other lines
		for (int y = 0; y < height; y++) 
		{
			fireProgressChanged(this, y, height);
	
			// Process pixels in the middle of the line
			for (int x = 0; x < width; x++) 
			{
				int currentValue = result.get(x, y) * sign;
				int maxValue = currentValue;
				
				// iterate over neighbors
				for (Cursor2D offset : offsets)
				{
					int x2 = x + offset.getX();
					int y2 = y + offset.getY();
					if (x2 >= 0 && x2 < width && y2 >= 0 && y2 < height)
					{
						maxValue = Math.max(maxValue, result.get(x2, y2) * sign);
					}
				}
				
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
	 * using any adjacency and floating point computation.
	 */
	private void forwardScanFloat() 
	{
		final int sign = this.reconstructionType.getSign();
		
		int width = this.marker.getWidth();
		int height = this.marker.getHeight();

		Collection<Cursor2D> offsets = this.connectivity.getForwardConnectivity().getOffsets();
		
		fireProgressChanged(this, 0, height);
		
		// Process all other lines
		for (int y = 0; y < height; y++) 
		{
			fireProgressChanged(this, y, height);
	
			// Process pixels in the middle of the line
			for (int x = 0; x < width; x++) 
			{
				float currentValue = result.getf(x, y) * sign;
				float maxValue = currentValue;
				
				// iterate over neighbors
				for (Cursor2D offset : offsets)
				{
					int x2 = x + offset.getX();
					int y2 = y + offset.getY();
					if (x2 >= 0 && x2 < width && y2 >= 0 && y2 < height)
					{
						maxValue = Math.max(maxValue, result.getf(x2, y2) * sign);
					}
				}
				
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
	 * using any adjacency.
	 */
	private void backwardScan() 
	{
		final int sign = this.reconstructionType.getSign();
		
		int width = this.marker.getWidth();
		int height = this.marker.getHeight();
	
		Collection<Cursor2D> offsets = this.connectivity.getBackwardConnectivity().getOffsets();
		
		fireProgressChanged(this, 0, height);
		
		// Process regular lines
		for (int y = height-1; y >= 0; y--)
		{
			fireProgressChanged(this, 0, height);
	
			// Process pixels in the middle of the current line
			// consider pixels on the right and below
			for (int x = width - 1; x >= 0; x--) 
			{

				int currentValue = result.get(x, y) * sign;
				int maxValue = currentValue;
				
				// iterate over neighbors
				for (Cursor2D offset : offsets)
				{
					int x2 = x + offset.getX();
					int y2 = y + offset.getY();
					if (x2 >= 0 && x2 < width && y2 >= 0 && y2 < height)
					{
						maxValue = Math.max(maxValue, result.get(x2, y2) * sign);
					}
				}
				
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
	 * using any adjacency and floating point computations.
	 */
	private void backwardScanFloat() 
	{
		final int sign = this.reconstructionType.getSign();
		
		int width = this.marker.getWidth();
		int height = this.marker.getHeight();
	
		Collection<Cursor2D> offsets = this.connectivity.getBackwardConnectivity().getOffsets();
		
		fireProgressChanged(this, 0, height);
		
		// Process regular lines
		for (int y = height-1; y >= 0; y--)
		{
			fireProgressChanged(this, 0, height);
	
			// Process pixels in the middle of the current line
			// consider pixels on the right and below
			for (int x = width - 1; x >= 0; x--) 
			{

				float currentValue = result.getf(x, y) * sign;
				float maxValue = currentValue;
				
				// iterate over neighbors
				for (Cursor2D offset : offsets)
				{
					int x2 = x + offset.getX();
					int y2 = y + offset.getY();
					if (x2 >= 0 && x2 < width && y2 >= 0 && y2 < height)
					{
						maxValue = Math.max(maxValue, result.getf(x2, y2) * sign);
					}
				}
				
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
