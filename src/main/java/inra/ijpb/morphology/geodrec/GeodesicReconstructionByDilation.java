/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
package inra.ijpb.morphology.geodrec;

import ij.IJ;
import ij.process.ImageProcessor;
import ij.process.FloatProcessor;

/**
 * <p>
 * Geodesic reconstruction by dilation for planar images.
 * </p>
 * 
 * <p>
 * This class performs the algorithm on the two instances of ImageProcessor
 * kept in it. Works for integer as well as for floating-point images.
 * Also performs a specific processing for pixels at the border of the image.
 * </p>
 * 
 * @see GeodesicReconstructionByErosion
 * @see GeodesicReconstructionHybrid
 * @see GeodesicReconstructionScanning
 * @author David Legland
 *
 */
public class GeodesicReconstructionByDilation extends GeodesicReconstructionAlgoStub 
{
	// ==================================================
	// Class variables
	
	ImageProcessor marker;
	ImageProcessor mask;
	
	ImageProcessor result;
	
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
	public GeodesicReconstructionByDilation() 
	{
	}
	
	/**
	 * Creates a new instance of geodesic reconstruction by dilation algorithm,
	 * that specifies the connectivity to use.
	 * 
	 * @param connectivity
	 *            the 2D connectivity to use (either 4 or 8)
	 */
	public GeodesicReconstructionByDilation(int connectivity) 
	{
		this.connectivity = connectivity;
	}

	
	// ==================================================
	// Methods 
	
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

		// Create result image the same size as mask and marker image
		this.result = this.mask.createProcessor(width, height);
	
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
					forwardDilationC4Float();
				else
					forwardDilationC4(); 
				break;
			case 8:	
				if (isFloat)
					forwardDilationC8Float();
				else
					forwardDilationC8(); 
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
					backwardDilationC4Float();
				else
					backwardDilationC4(); 
				break;
			case 8:	
				if (isFloat)
					backwardDilationC8Float();
				else
					backwardDilationC8(); 
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
	private void forwardDilationC4()
	{
		int width = this.marker.getWidth();
		int height = this.marker.getHeight();
		
		// the values associated to each neighbor
		int v1, v2;
		
		// the maximal value around current pixel
		int value;
				
		if (showProgress) 
		{
			IJ.showProgress(0, height);
		}
		
		// Process first line: consider only the pixel on the left
		for (int i = 1; i < width; i++)
		{
			value = result.get(i-1, 0);
			geodesicDilationUpdate(i, 0, value);
		}
	
		// Process all other lines
		for (int j = 1; j < height; j++) 
		{
			
			if (showProgress)
			{
				IJ.showProgress(j, height);
			}
			// process first pixel of current line: consider pixel up
			value = result.get(0, j-1);
			geodesicDilationUpdate(0, j, value);
	
			// Process pixels in the middle of the line
			for (int i = 1; i < width; i++)
			{
				v1 = result.get(i,   j-1);
				v2 = result.get(i-1, j);
				value = Math.max(v1, v2);
				geodesicDilationUpdate(i, j, value);
			}
		} // end of forward iteration
	}

	/**
	 * Update result image using pixels in the upper left neighborhood,
	 * using the 4-adjacency.
	 */
	private void forwardDilationC4Float()
	{
		int width = this.marker.getWidth();
		int height = this.marker.getHeight();
		
		// the values associated to each neighbor
		float v1, v2;
		
		// the maximal value around current pixel
		float value;
				
		if (showProgress) 
		{
			IJ.showProgress(0, height);
		}
		
		// Process first line: consider only the pixel on the left
		for (int i = 1; i < width; i++)
		{
			value = result.getf(i-1, 0);
			geodesicDilationUpdateFloat(i, 0, value);
		}
	
		// Process all other lines
		for (int j = 1; j < height; j++) 
		{
			
			if (showProgress)
			{
				IJ.showProgress(j, height);
			}
			// process first pixel of current line: consider pixel up
			value = result.getf(0, j-1);
			geodesicDilationUpdateFloat(0, j, value);
	
			// Process pixels in the middle of the line
			for (int i = 1; i < width; i++) 
			{
				v1 = result.getf(i,   j-1);
				v2 = result.getf(i-1, j);
				value = Math.max(v1, v2);
				geodesicDilationUpdateFloat(i, j, value);
			}
		} // end of forward iteration
	}

	/**
	 * Update result image using pixels in the upper left neighborhood,
	 * using the 8-adjacency.
	 */
	private void forwardDilationC8() 
	{
		int width = this.marker.getWidth();
		int height = this.marker.getHeight();
		
		// the values associated to each neighbor
		int v1, v2, v3, v4;
		
		// the maximal value around current pixel
		int value;
				
		if (showProgress) 
		{
			IJ.showProgress(0, height);
		}
		
		// Process first line: consider only the pixel on the left
		for (int i = 1; i < width; i++)
		{
			value = result.get(i-1, 0);
			geodesicDilationUpdate(i, 0, value);
		}

		// Process all other lines
		for (int j = 1; j < height; j++)
		{
			
			if (showProgress)
			{
				IJ.showProgress(j, height);
			}
			// process first pixel of current line: consider pixels up and
			// upright
			v1 = result.get(0, j-1);
			v2 = result.get(1, j-1);
			value = Math.max(v1, v2);
			geodesicDilationUpdate(0, j, value);

			// Process pixels in the middle of the line
			for (int i = 1; i < width - 1; i++)
			{
				v1 = result.get(i-1, j-1);
				v2 = result.get(i,   j-1);
				v3 = result.get(i+1, j-1);
				v4 = result.get(i-1, j);
				value = Math.max(Math.max(v1, v2), Math.max(v3, v4));
				geodesicDilationUpdate(i, j, value);
			}

			// process last pixel of current line: consider pixels left,
			// up-left, and up
			v1 = result.get(width-2, j-1);
			v2 = result.get(width-1, j-1);
			v3 = result.get(width-2, j);
			value = Math.max(Math.max(v1, v2), v3);
			geodesicDilationUpdate(width-1, j, value);

		} // end of forward iteration
	}
	
	/**
	 * Update result image using pixels in the upper left neighborhood,
	 * using the 8-adjacency.
	 */
	private void forwardDilationC8Float()
	{
		int width = this.marker.getWidth();
		int height = this.marker.getHeight();
		
		// the values associated to each neighbor
		float v1, v2, v3, v4;
		
		// the maximal value around current pixel
		float value;
				
		if (showProgress)
		{
			IJ.showProgress(0, height);
		}
		
		// Process first line: consider only the pixel on the left
		for (int i = 1; i < width; i++) {
			value = result.getf(i-1, 0);
			geodesicDilationUpdateFloat(i, 0, value);
		}

		// Process all other lines
		for (int j = 1; j < height; j++)
		{
			
			if (showProgress) 
			{
				IJ.showProgress(j, height);
			}
			// process first pixel of current line: consider pixels up and
			// upright
			v1 = result.getf(0, j-1);
			v2 = result.getf(1, j-1);
			value = Math.max(v1, v2);
			geodesicDilationUpdateFloat(0, j, value);

			// Process pixels in the middle of the line
			for (int i = 1; i < width - 1; i++)
			{
				v1 = result.getf(i-1, j-1);
				v2 = result.getf(i,   j-1);
				v3 = result.getf(i+1, j-1);
				v4 = result.getf(i-1, j);
				value = Math.max(Math.max(v1, v2), Math.max(v3, v4));
				geodesicDilationUpdateFloat(i, j, value);
			}

			// process last pixel of current line: consider pixels left,
			// up-left, and up
			v1 = result.getf(width-2, j-1);
			v2 = result.getf(width-1, j-1);
			v3 = result.getf(width-2, j);
			value = Math.max(Math.max(v1, v2), v3);
			geodesicDilationUpdateFloat(width-1, j, value);

		} // end of forward iteration
	}

	/**
	 * Update result image using pixels in the lower-right neighborhood, 
	 * using the 4-adjacency.
	 */
	private void backwardDilationC4()
	{
		int width = this.marker.getWidth();
		int height = this.marker.getHeight();
		
		// the values associated to each neighbor
		int v1, v2;
		
		// the maximal value around current pixel
		int value;
	
		if (showProgress)
		{
			IJ.showProgress(0, height);
		}
		
		// Process last line: consider only the pixel just after (on the right)
		for (int i = width - 2; i > 0; i--) 
		{
			value = result.get(i+1, height-1);
			geodesicDilationUpdate(i, height-1, value);
		}
		
		// Process regular lines
		for (int j = height-2; j >= 0; j--) 
		{
	
			if (showProgress) 
			{
				IJ.showProgress(height-1-j, height);
			}
			
			// process last pixel of the current line: consider only the pixel below
			value = result.get(width - 1, j+1);
			geodesicDilationUpdate(width - 1, j, value);
	
			// Process pixels in the middle of the current line
			// consider pixels on the right and below
			for (int i = width - 2; i > 0; i--)
			{
				v1 = result.get(i+1, j);
				v2 = result.get(i,   j+1);
				value = Math.max(v1, v2);
				geodesicDilationUpdate(i, j, value);
			}
	
			// process first pixel of current line: consider pixels right,
			// and down
			v1 = result.get(1, j);
			v2 = result.get(0, j+1);
			value = Math.max(v1, v2);
			geodesicDilationUpdate(0, j, value);
		} 
	
	} // end of backward iteration

	/**
	 * Update result image using pixels in the lower-right neighborhood, 
	 * using the 4-adjacency.
	 */
	private void backwardDilationC4Float() 
	{
		int width = this.marker.getWidth();
		int height = this.marker.getHeight();
		
		// the values associated to each neighbor
		float v1, v2;
		
		// the maximal value around current pixel
		float value;
	
		if (showProgress)
		{
			IJ.showProgress(0, height);
		}
		
		// Process last line: consider only the pixel just after (on the right)
		for (int i = width - 2; i > 0; i--)
		{
			value = result.getf(i+1, height-1);
			geodesicDilationUpdateFloat(i, height-1, value);
		}
		
		// Process regular lines
		for (int j = height-2; j >= 0; j--) 
		{
	
			if (showProgress) 
			{
				IJ.showProgress(height-1-j, height);
			}
			
			// process last pixel of the current line: consider only the pixel below
			value = result.get(width - 1, j+1);
			geodesicDilationUpdateFloat(width - 1, j, value);
	
			// Process pixels in the middle of the current line
			// consider pixels on the right and below
			for (int i = width - 2; i > 0; i--) 
			{
				v1 = result.getf(i+1, j);
				v2 = result.getf(i,   j+1);
				value = Math.max(v1, v2);
				geodesicDilationUpdateFloat(i, j, value);
			}
	
			// process first pixel of current line: consider pixels right,
			// and down
			v1 = result.getf(1, j);
			v2 = result.getf(0, j+1);
			value = Math.max(v1, v2);
			geodesicDilationUpdateFloat(0, j, value);
		} 
	
	} // end of backward iteration

	/**
	 * Update result image using pixels in the lower-right neighborhood, using
	 * the 8-adjacency.
	 */
	private void backwardDilationC8() 
	{
		int width = this.marker.getWidth();
		int height = this.marker.getHeight();
		
		// the values associated to each neighbor
		int v1, v2, v3, v4;
		
		// the maximal value around current pixel
		int value;
	
		if (showProgress)
		{
			IJ.showProgress(0, height);
		}
		
		// Process last line: consider only the pixel just after (on the right)
		for (int i = width - 2; i > 0; i--)
		{
			value = result.get(i+1, height-1);
			geodesicDilationUpdate(i, height-1, value);
		}
		
		// Process regular lines
		for (int j = height-2; j >= 0; j--)
		{

			if (showProgress) {
				IJ.showProgress(height-1-j, height);
			}
			
			// process last pixel of the current line: consider pixels
			// down and down-left
			v1 = result.get(width - 1, j+1);
			v2 = result.get(width - 2, j+1);
			value = Math.max(v1, v2);
			geodesicDilationUpdate(width - 1, j, value);

			// Process pixels in the middle of the current line
			for (int i = width - 2; i > 0; i--)
			{
				v1 = result.get(i+1, j);
				v2 = result.get(i+1, j+1);
				v3 = result.get(i,   j+1);
				v4 = result.get(i-1, j+1);
				value = Math.max(Math.max(v1, v2), Math.max(v3, v4));
				geodesicDilationUpdate(i, j, value);
			}

			// process first pixel of current line: consider pixels right,
			// down-right and down
			v1 = result.get(1, j);
			v2 = result.get(0, j+1);
			v3 = result.get(1, j+1);
			value = Math.max(Math.max(v1, v2), v3);
			geodesicDilationUpdate(0, j, value);
		} 

	} // end of backward iteration

	/**
	 * Update result image using pixels in the lower-right neighborhood, using
	 * the 8-adjacency.
	 */
	private void backwardDilationC8Float() 
	{
		int width = this.marker.getWidth();
		int height = this.marker.getHeight();
		
		// the values associated to each neighbor
		float v1, v2, v3, v4;
		
		// the maximal value around current pixel
		float value;
	
		if (showProgress) 
		{
			IJ.showProgress(0, height);
		}
		
		// Process last line: consider only the pixel just after (on the right)
		for (int i = width - 2; i > 0; i--)
		{
			value = result.getf(i+1, height-1);
			geodesicDilationUpdateFloat(i, height-1, value);
		}
		
		// Process regular lines
		for (int j = height-2; j >= 0; j--)
		{

			if (showProgress)
			{
				IJ.showProgress(height-1-j, height);
			}
			
			// process last pixel of the current line: consider pixels
			// down and down-left
			v1 = result.getf(width - 1, j+1);
			v2 = result.getf(width - 2, j+1);
			value = Math.max(v1, v2);
			geodesicDilationUpdateFloat(width - 1, j, value);

			// Process pixels in the middle of the current line
			for (int i = width - 2; i > 0; i--)
			{
				v1 = result.getf(i+1, j);
				v2 = result.getf(i+1, j+1);
				v3 = result.getf(i,   j+1);
				v4 = result.getf(i-1, j+1);
				value = Math.max(Math.max(v1, v2), Math.max(v3, v4));
				geodesicDilationUpdateFloat(i, j, value);
			}

			// process first pixel of current line: consider pixels right,
			// down-right and down
			v1 = result.getf(1, j);
			v2 = result.getf(0, j+1);
			v3 = result.getf(1, j+1);
			value = Math.max(Math.max(v1, v2), v3);
			geodesicDilationUpdateFloat(0, j, value);
		} 

	} // end of backward iteration

	/**
	 * Update the pixel at position (i,j) with the value <code>value<value>. 
	 * First computes the min of value and the value of the mask.
	 * Check if value is greater than the current value at position (i,j). 
	 * If new value is lower than current value, do nothing.
	 */
	private void geodesicDilationUpdate(int i, int j, int value)
	{
		// update current value only if value is strictly greater
		value = Math.min(value, mask.get(i, j));
		if (value > result.get(i, j)) 
		{
			modif = true;
			result.set(i, j, value);
		}
	}

	/**
	 * Update the pixel at position (i,j) with the value <code>value<value>. 
	 * First computes the min of value and the value of the mask.
	 * Check if value is greater than the current value at position (i,j). 
	 * If new value is lower than current value, do nothing.
	 */
	private void geodesicDilationUpdateFloat(int i, int j, float value)
	{
		// update current value only if value is strictly greater
		value = Math.min(value, mask.getf(i, j));
		if (value > result.getf(i, j))
		{
			modif = true;
			result.setf(i, j, value);
		}
	}

}
