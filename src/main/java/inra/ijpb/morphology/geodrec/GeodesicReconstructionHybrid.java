/**
 * 
 */
package inra.ijpb.morphology.geodrec;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayDeque;
import java.util.Deque;

import ij.IJ;
import ij.process.ImageProcessor;
import ij.process.FloatProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.data.Cursor2D;

/**
 * <p>
 * Geodesic reconstruction for planar images, using hybrid algorithm.
 * </p>
 * 
 * <p>
 * This class performs the algorithm on the two instances of ImageProcessor
 * kept in it. 
 * </p>
 * 
 * @see GeodesicReconstructionScanning
 * @see GeodesicReconstructionByDilation
 * @see GeodesicReconstructionByErosion
 * @author David Legland
 *
 */
public class GeodesicReconstructionHybrid extends AlgoStub implements
		GeodesicReconstructionAlgo 
{
	// ------------------------
	// Class fields
	
	GeodesicReconstructionType reconstructionType = GeodesicReconstructionType.BY_DILATION;

	int connectivity = 4;
	
	ImageProcessor marker;
	ImageProcessor mask;
	
	ImageProcessor result;
	
	/** image width */
	int sizeX = 0;
	/** image height */
	int sizeY = 0;

	/** the queue containing the positions that need update */
	Deque<Cursor2D> queue;

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
	public GeodesicReconstructionHybrid()
	{
	}
	
	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * that specifies the type of reconstruction, and using the connectivity 4.
	 */
	public GeodesicReconstructionHybrid(GeodesicReconstructionType type) 
	{
		this.reconstructionType = type;
	}

	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * that specifies the connectivity to use.
	 */
	public GeodesicReconstructionHybrid(int connectivity)
	{
		this.connectivity = connectivity;
	}

	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * that specifies the type of reconstruction, and the connectivity to use.
	 */
	public GeodesicReconstructionHybrid(GeodesicReconstructionType type, int connectivity) 
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
	 * Run the geodesic reconstruction algorithm using the specified images
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

		queue = new ArrayDeque<Cursor2D>();
		
		boolean isInteger = !(mask instanceof FloatProcessor);

		// Initialize the result image with the minimum value of marker and mask
		// images
		if (isInteger)
		{
			initializeResult();
		}
		else
		{
			initializeResultFloat();
		}
		
		
		// Display current status
		if (verbose)
		{
			System.out.println("Forward iteration");
		}
		if (showStatus)
		{
			IJ.showStatus("Geod. Rec. by Dil. Fwd");
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
			System.out.println("Backward iteration");
		}
		if (showStatus)
		{
			IJ.showStatus("Geod. Rec. by Dil. Bwd");
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

		if (verbose) 
		{
			System.out.println("Process queue ");
		}
		if (showStatus)
		{
			IJ.showStatus("Processing Queue... ");
		}

		// Process queue
		if (this.connectivity == 4) 
		{
			if (isInteger)
				processQueueC4();
			else
				processQueueC4Float();
		} else {
			if (isInteger)
				processQueueC8();
			else
				processQueueC8Float();
		}


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
				
				// combine with mask
				maxValue = min(maxValue, mask.get(x, y) * sign);

				// check if update is required
				if (maxValue <= currentValue)
				{
					continue;
				}

				// update value of current pixel
				result.set(x, y, maxValue * sign);
				
				// eventually add lower-right neighbors to queue
				if (x < this.sizeX - 1) 
					updateQueue(x + 1, y, maxValue, sign);
				if (y < this.sizeY - 1) 
					updateQueue(x, y + 1, maxValue, sign);
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
				
				// combine with mask
				maxValue = min(maxValue, mask.getf(x, y) * sign);

				// check if update is required
				if (maxValue <= currentValue)
				{
					continue;
				}

				// update value of current pixel
				result.setf(x, y, maxValue * sign);
				
				// eventually add lower-right neighbors to queue
				if (x < this.sizeX - 1) 
					updateQueue(x + 1, y, maxValue, sign);
				if (y < this.sizeY - 1) 
					updateQueue(x, y + 1, maxValue, sign);
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
				
//				// update value of current pixel
//				maxValue = min(maxValue, mask.get(x, y) * sign);
//				if (maxValue > currentValue) 
//				{
//					result.set(x, y, maxValue * sign);
//					modif = true;
//				}

				// combine with mask
				maxValue = min(maxValue, mask.get(x, y) * sign);

				// check if update is required
				if (maxValue <= currentValue)
				{
					continue;
				}

				// update value of current pixel
				result.set(x, y, maxValue * sign);
				
				// eventually add lower-right neighbors to queue
				if (x < this.sizeX - 1) 
					updateQueue(x + 1, y, maxValue, sign);
				if (y < this.sizeY - 1) 
				{
					if (x > 0) 
						updateQueue(x - 1, y + 1, maxValue, sign);
					updateQueue(x, y + 1, maxValue, sign);
					if (x < this.sizeX - 1) 
						updateQueue(x + 1, y + 1, maxValue, sign);
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
				
				// combine with mask
				maxValue = min(maxValue, mask.getf(x, y) * sign);

				// check if update is required
				if (maxValue <= currentValue)
				{
					continue;
				}

				// update value of current pixel
				result.setf(x, y, maxValue * sign);
				
				// eventually add lower-right neighbors to queue
				if (x < this.sizeX - 1) 
					updateQueue(x + 1, y, maxValue, sign);
				if (y < this.sizeY - 1) 
				{
					if (x > 0) 
						updateQueue(x - 1, y + 1, maxValue, sign);
					updateQueue(x, y + 1, maxValue, sign);
					if (x < this.sizeX - 1) 
						updateQueue(x + 1, y + 1, maxValue, sign);
				}
				
			}
		} 
	} // end of backward iteration

	/**
	 * Update result image using next pixel in the queue,
	 * using the 6-adjacency.
	 */
	private void processQueueC4() 
	{
		// sign for adapting dilation and erosion algorithms
		final int sign = this.reconstructionType.getSign();

		// the maximal value around current pixel
		int value;
		
		while (!queue.isEmpty())
		{
			Cursor2D p = queue.removeFirst();
			int x = p.getX();
			int y = p.getY();
			value = result.get(x, y) * sign;
			
			// compare with each one of the four neighbors
			if (x > 0) 
				value = max(value, result.get(x - 1, y) * sign);
			if (x < this.sizeX - 1) 
				value = max(value, result.get(x + 1, y) * sign);
			if (y > 0) 
				value = max(value, result.get(x, y - 1) * sign);
			if (y < this.sizeY - 1) 
				value = max(value, result.get(x, y + 1) * sign);

			// bound with mask value
			value = min(value, mask.get(x, y) * sign);
			
			// if no update is needed, continue to next item in queue
			if (value <= result.get(x, y) * sign) 
				continue;
			
			// update result for current position
			result.set(x, y, value * sign);

			// Eventually add each neighbor
			if (x > 0)
				updateQueue(x - 1, y, value, sign);
			if (x < sizeX - 1)
				updateQueue(x + 1, y, value, sign);
			if (y > 0)
				updateQueue(x, y - 1, value, sign);
			if (y < sizeY - 1)
				updateQueue(x, y + 1, value, sign);
		}
	}
	
	/**
	 * Update result image using next pixel in the queue,
	 * using the 6-adjacency and floating point values.
	 */
	private void processQueueC4Float()
	{
		// sign for adapting dilation and erosion algorithms
		final float sign = this.reconstructionType.getSign();

		// the maximal value around current pixel
		float value;
		
		while (!queue.isEmpty()) 
		{
			Cursor2D p = queue.removeFirst();
			int x = p.getX();
			int y = p.getY();
			value = result.getf(x, y) * sign;
			
			// compare with each one of the four neighbors
			if (x > 0) 
				value = max(value, result.getf(x - 1, y) * sign);
			if (x < this.sizeX - 1) 
				value = max(value, result.getf(x + 1, y) * sign);
			if (y > 0) 
				value = max(value, result.getf(x, y - 1) * sign);
			if (y < this.sizeY - 1) 
				value = max(value, result.getf(x, y + 1) * sign);

			// bound with mask value
			value = min(value, mask.getf(x, y) * sign);
			
			// if no update is needed, continue to next item in queue
			if (value <= result.getf(x, y) * sign) 
				continue;
			
			// update result for current position
			result.setf(x, y, value * sign);

			// Eventually add each neighbor
			if (x > 0)
				updateQueue(x - 1, y, value, sign);
			if (x < sizeX - 1)
				updateQueue(x + 1, y, value, sign);
			if (y > 0)
				updateQueue(x, y - 1, value, sign);
			if (y < sizeY - 1)
				updateQueue(x, y + 1, value, sign);
		}
	}

	/**
	 * Update result image using next pixel in the queue,
	 * using the 8-adjacency.
	 */
	private void processQueueC8() 
	{
		// sign for adapting dilation and erosion algorithms
		final int sign = this.reconstructionType.getSign();

		// the maximal value around current pixel
		int value;
		
		while (!queue.isEmpty()) 
		{
//			System.out.println("  queue size: " + queue.size());
			
			Cursor2D p = queue.removeFirst();
			int x = p.getX();
			int y = p.getY();
			value = result.get(x, y) * sign;
			
			// compute bounds of neighborhood
			int xmin = max(x - 1, 0);
			int xmax = min(x + 1, sizeX - 1);
			int ymin = max(y - 1, 0);
			int ymax = min(y + 1, sizeY - 1);

			// compare with each one of the neighbors
			for (int y2 = ymin; y2 <= ymax; y2++) 
			{
				for (int x2 = xmin; x2 <= xmax; x2++)
				{
					value = max(value, result.get(x2, y2) * sign);
				}
			}
			
			// bound with mask value
			value = min(value, mask.get(x, y) * sign);
			
			// if no update is needed, continue to next item in queue
			if (value <= result.get(x, y) * sign) 
				continue;
			
			// update result for current position
			result.set(x, y, value * sign);

			// compare with each one of the neighbors
			for (int y2 = ymin; y2 <= ymax; y2++) 
			{
				for (int x2 = xmin; x2 <= xmax; x2++) 
				{
					updateQueue(x2, y2, value, sign);
				}
			}
		}
	}

	/**
	 * Update result image using next pixel in the queue,
	 * using the 8-adjacency and floating point processing.
	 */
	private void processQueueC8Float() 
	{
		// sign for adapting dilation and erosion algorithms
		final float sign = this.reconstructionType.getSign();

		// the maximal value around current pixel
		float value;
		
		while (!queue.isEmpty()) 
		{
//			System.out.println("  queue size: " + queue.size());
			
			Cursor2D p = queue.removeFirst();
			int x = p.getX();
			int y = p.getY();
			value = result.getf(x, y) * sign;
			
			// compute bounds of neighborhood
			int xmin = max(x - 1, 0);
			int xmax = min(x + 1, sizeX - 1);
			int ymin = max(y - 1, 0);
			int ymax = min(y + 1, sizeY - 1);

			// compare with each one of the neighbors
			for (int y2 = ymin; y2 <= ymax; y2++) 
			{
				for (int x2 = xmin; x2 <= xmax; x2++)
				{
					value = max(value, result.getf(x2, y2) * sign);
				}
			}
			
			// bound with mask value
			value = min(value, mask.getf(x, y) * sign);
			
			// if no update is needed, continue to next item in queue
			if (value <= result.getf(x, y) * sign) 
				continue;
			
			// update result for current position
			result.setf(x, y, value * sign);

			// compare with each one of the neighbors
			for (int y2 = ymin; y2 <= ymax; y2++) 
			{
				for (int x2 = xmin; x2 <= xmax; x2++) 
				{
					updateQueue(x2, y2, value, sign);
				}
			}
		}
	}

	/**
	 * Adds the current position to the queue if and only if the value 
	 * <code>value<value> is greater than the value of the mask.
	 * @param x column index
	 * @param y row index
	 * @param value value at (x, y) position
	 * @param sign integer +1 or -1 to manage both erosions and dilations
	 */
	private void updateQueue(int x, int y, int value, int sign) {
		// update current value only if value is strictly greater
		int maskValue = mask.get(x, y) * sign;
		value = Math.min(value, maskValue);
		
		int resultValue = result.get(x, y) * sign; 
		if (value > resultValue) {
			Cursor2D position = new Cursor2D(x, y);
			queue.add(position);
		}
	}

	/**
	 * Adds the current position to the queue if and only if the value 
	 * <code>value<value> is greater than the value of the mask.
	 * @param x column index
	 * @param y row index
	 * @param value value at (x, y) position
	 * @param sign integer +1 or -1 to manage both erosions and dilations
	 */
	private void updateQueue(int x, int y, float value, float sign) {
		// update current value only if value is strictly greater
		float maskValue = mask.getf(x, y) * sign;
		value = Math.min(value, maskValue);
		
		float resultValue = result.getf(x, y) * sign; 
		if (value > resultValue) {
			Cursor2D position = new Cursor2D(x, y);
			queue.add(position);
		}
	}

}
