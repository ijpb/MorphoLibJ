/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2026 INRA.
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
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.data.image.ImageUtils;

/**
 * Geodesic reconstruction by erosion for integer images.
 * 
 * This class performs the algorithm on the two instances of ImageProcessor
 * kept in it. 
 * 
 * @see GeodesicReconstructionByDilation
 * @author David Legland
 *
 */
public class GeodesicReconstructionByErosion extends GeodesicReconstructionAlgoStub 
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
	 * Creates a new instance of geodesic reconstruction by erosion algorithm,
	 * using default connectivity 4.
	 */
	public GeodesicReconstructionByErosion()
	{
	}

	/**
	 * Creates a new instance of geodesic reconstruction by erosion algorithm,
	 * that specifies the connectivity to use.
	 * 
	 * @param connectivity
	 *            the 2D connectivity to use (either 4 or 8)
	 */
	public GeodesicReconstructionByErosion(int connectivity)
	{
		this.connectivity = connectivity;
	}
	
	
	// ==================================================
	// Methods 

	/**
	 * Run the reconstruction by erosion algorithm using the images specified
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
		if (!ImageUtils.isSameSize(marker, mask))
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

		// Create result image the same size as the mask and marker images
		this.result = this.mask.createProcessor(width, height);
	
		// Count the number of iterations for eventually displaying progress
		int iter = 0;

		boolean isFloat = (mask instanceof FloatProcessor);

		// Initialize the result image with the minimum value of marker and mask
		// images
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				this.result.setf(x, y,
						Math.max(this.marker.getf(x, y), this.mask.getf(x, y)));
			}
		}

		
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
				IJ.showStatus("Geod. Rec. by Ero. Fwd " + (iter + 1));
			}
			
			// forward iteration
			switch (connectivity)
			{
			case 4:
				if (isFloat)
					forwardErosionC4Float();
				else
					forwardErosionC4();
				break;
			case 8:
				if (isFloat)
					forwardErosionC8Float();
				else
					forwardErosionC8();
				break;
			}

			// Display current status
			if (verbose)
			{
				System.out.println("Backward iteration " + iter);
			}
			if (showStatus)
			{
				IJ.showStatus("Geod. Rec. by Ero. Bwd " + (iter + 1));
			}

			// backward iteration
			switch (connectivity)
			{
			case 4:
				if (isFloat)
					backwardErosionC4Float();
				else
					backwardErosionC4();
				break;
			case 8:
				if (isFloat)
					backwardErosionC8Float();
				else
					backwardErosionC8();
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
	private void forwardErosionC4()
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
			value = result.get(i - 1, 0);
			geodesicErosionUpdate(i, 0, value);
		}

		// Process all other lines
		for (int j = 1; j < height; j++)
		{

			if (showProgress)
			{
				IJ.showProgress(j, height);
			}
			// process first pixel of current line: consider pixel up
			value = result.get(0, j - 1);
			geodesicErosionUpdate(0, j, value);

			// Process pixels in the middle of the line
			for (int i = 1; i < width; i++)
			{
			v1 = result.get(i,   j-1);
				v2 = result.get(i-1, j);
				value = Math.min(v1, v2);
				geodesicErosionUpdate(i, j, value);
			}
		} // end of forward iteration
	}

	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 4-adjacency.
	 */
	private void forwardErosionC4Float()
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
			value = result.getf(i - 1, 0);
			geodesicErosionUpdateFloat(i, 0, value);
		}

		// Process all other lines
		for (int j = 1; j < height; j++)
		{

			if (showProgress)
			{
				IJ.showProgress(j, height);
			}
			// process first pixel of current line: consider pixel up
			value = result.getf(0, j - 1);
			geodesicErosionUpdateFloat(0, j, value);

			// Process pixels in the middle of the line
			for (int i = 1; i < width; i++)
			{
				v1 = result.getf(i, j - 1);
				v2 = result.getf(i - 1, j);
				value = Math.min(v1, v2);
				geodesicErosionUpdateFloat(i, j, value);
			}
		} // end of forward iteration
	}

	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 8-adjacency.
	 */
	private void forwardErosionC8()
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
			value = result.get(i - 1, 0);
			geodesicErosionUpdate(i, 0, value);
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
			v1 = result.get(0, j - 1);
			v2 = result.get(1, j - 1);
			value = Math.min(v1, v2);
			geodesicErosionUpdate(0, j, value);

			// Process pixels in the middle of the line
			for (int i = 1; i < width - 1; i++)
			{
				v1 = result.get(i - 1, j - 1);
				v2 = result.get(i, j - 1);
				v3 = result.get(i + 1, j - 1);
				v4 = result.get(i - 1, j);
				value = Math.min(Math.min(v1, v2), Math.min(v3, v4));
				geodesicErosionUpdate(i, j, value);
			}

			// process last pixel of current line: consider pixels left,
			// up-left, and up
			v1 = result.get(width - 2, j - 1);
			v2 = result.get(width - 1, j - 1);
			v3 = result.get(width - 2, j);
			value = Math.min(Math.min(v1, v2), v3);
			geodesicErosionUpdate(width - 1, j, value);

		} // end of forward iteration
	}

	/**
	 * Update result image using pixels in the upper left neighborhood, using
	 * the 8-adjacency.
	 */
	private void forwardErosionC8Float()
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
		for (int i = 1; i < width; i++)
		{
			value = result.getf(i - 1, 0);
			geodesicErosionUpdateFloat(i, 0, value);
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
			v1 = result.getf(0, j - 1);
			v2 = result.getf(1, j - 1);
			value = Math.min(v1, v2);
			geodesicErosionUpdateFloat(0, j, value);

			// Process pixels in the middle of the line
			for (int i = 1; i < width - 1; i++)
			{
				v1 = result.getf(i - 1, j - 1);
				v2 = result.getf(i, j - 1);
				v3 = result.getf(i + 1, j - 1);
				v4 = result.getf(i - 1, j);
				value = Math.min(Math.min(v1, v2), Math.min(v3, v4));
				geodesicErosionUpdateFloat(i, j, value);
			}

			// process last pixel of current line: consider pixels left,
			// up-left, and up
			v1 = result.getf(width - 2, j - 1);
			v2 = result.getf(width - 1, j - 1);
			v3 = result.getf(width - 2, j);
			value = Math.min(Math.min(v1, v2), v3);
			geodesicErosionUpdateFloat(width - 1, j, value);

		} // end of forward iteration
	}

	/**
	 * Update result image using pixels in the lower-right neighborhood, using
	 * the 4-adjacency.
	 */
	private void backwardErosionC4()
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
			value = result.get(i + 1, height - 1);
			geodesicErosionUpdate(i, height - 1, value);
		}

		// Process regular lines
		for (int j = height - 2; j >= 0; j--)
		{

			if (showProgress)
			{
				IJ.showProgress(height - 1 - j, height);
			}

			// process last pixel of the current line: consider only the pixel
			// below
			value = result.get(width - 1, j + 1);
			geodesicErosionUpdate(width - 1, j, value);

			// Process pixels in the middle of the current line
			// consider pixels on the right and below
			for (int i = width - 2; i > 0; i--)
			{
				v1 = result.get(i + 1, j);
				v2 = result.get(i, j + 1);
				value = Math.min(v1, v2);
				geodesicErosionUpdate(i, j, value);
			}

			// process first pixel of current line: consider pixels right,
			// and down
			v1 = result.get(1, j);
			v2 = result.get(0, j + 1);
			value = Math.min(v1, v2);
			geodesicErosionUpdate(0, j, value);
		}

	} // end of backward iteration

	/**
	 * Update result image using pixels in the lower-right neighborhood, using
	 * the 4-adjacency.
	 */
	private void backwardErosionC4Float()
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
			value = result.getf(i + 1, height - 1);
			geodesicErosionUpdateFloat(i, height - 1, value);
		}

		// Process regular lines
		for (int j = height - 2; j >= 0; j--)
		{

			if (showProgress)
			{
				IJ.showProgress(height - 1 - j, height);
			}

			// process last pixel of the current line: consider only the pixel
			// below
			value = result.getf(width - 1, j + 1);
			geodesicErosionUpdateFloat(width - 1, j, value);

			// Process pixels in the middle of the current line
			// consider pixels on the right and below
			for (int i = width - 2; i > 0; i--)
			{
				v1 = result.getf(i + 1, j);
				v2 = result.getf(i, j + 1);
				value = Math.min(v1, v2);
				geodesicErosionUpdateFloat(i, j, value);
			}

			// process first pixel of current line: consider pixels right,
			// and down
			v1 = result.getf(1, j);
			v2 = result.getf(0, j + 1);
			value = Math.min(v1, v2);
			geodesicErosionUpdateFloat(0, j, value);
		}

	} // end of backward iteration

	/**
	 * Update result image using pixels in the lower-right neighborhood, using
	 * the 8-adjacency.
	 */
	private void backwardErosionC8()
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
			value = result.get(i + 1, height - 1);
			geodesicErosionUpdate(i, height - 1, value);
		}

		// Process regular lines
		for (int j = height - 2; j >= 0; j--)
		{

			if (showProgress)
			{
				IJ.showProgress(height - 1 - j, height);
			}

			// process last pixel of the current line: consider pixels
			// down and down-left
			v1 = result.get(width - 1, j + 1);
			v2 = result.get(width - 2, j + 1);
			value = Math.min(v1, v2);
			geodesicErosionUpdate(width - 1, j, value);

			// Process pixels in the middle of the current line
			for (int i = width - 2; i > 0; i--)
			{
				v1 = result.get(i + 1, j);
				v2 = result.get(i + 1, j + 1);
				v3 = result.get(i, j + 1);
				v4 = result.get(i - 1, j + 1);
				value = Math.min(Math.min(v1, v2), Math.min(v3, v4));
				geodesicErosionUpdate(i, j, value);
			}

			// process first pixel of current line: consider pixels right,
			// down-right and down
			v1 = result.get(1, j);
			v2 = result.get(0, j + 1);
			v3 = result.get(1, j + 1);
			value = Math.min(Math.min(v1, v2), v3);
			geodesicErosionUpdate(0, j, value);
		}

	} // end of backward iteration

	/**
	 * Update result image using pixels in the lower-right neighborhood, using
	 * the 8-adjacency.
	 */
	private void backwardErosionC8Float()
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
			value = result.getf(i + 1, height - 1);
			geodesicErosionUpdateFloat(i, height - 1, value);
		}

		// Process regular lines
		for (int j = height - 2; j >= 0; j--)
		{

			if (showProgress)
			{
				IJ.showProgress(height - 1 - j, height);
			}

			// process last pixel of the current line: consider pixels
			// down and down-left
			v1 = result.getf(width - 1, j + 1);
			v2 = result.getf(width - 2, j + 1);
			value = Math.min(v1, v2);
			geodesicErosionUpdateFloat(width - 1, j, value);

			// Process pixels in the middle of the current line
			for (int i = width - 2; i > 0; i--)
			{
				v1 = result.getf(i + 1, j);
				v2 = result.getf(i + 1, j + 1);
				v3 = result.getf(i, j + 1);
				v4 = result.getf(i - 1, j + 1);
				value = Math.min(Math.min(v1, v2), Math.min(v3, v4));
				geodesicErosionUpdateFloat(i, j, value);
			}

			// process first pixel of current line: consider pixels right,
			// down-right and down
			v1 = result.getf(1, j);
			v2 = result.getf(0, j + 1);
			v3 = result.getf(1, j + 1);
			value = Math.min(Math.min(v1, v2), v3);
			geodesicErosionUpdateFloat(0, j, value);
		}

	} // end of backward iteration

	/**
	 * Update the pixel at position (i,j) with the value <code>value<value>. 
	 * First computes the min of value and the value of the mask.
	 * Check if value is greater than the current value at position (i,j). 
	 * If new value is lower than current value, do nothing.
	 */
	private void geodesicErosionUpdate(int i, int j, int value)
	{
		// update current value only if value is strictly lower
		value = Math.max(value, mask.get(i, j));
		if (value < result.get(i, j))
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
	private void geodesicErosionUpdateFloat(int i, int j, float value)
	{
		// update current value only if value is strictly lower
		value = Math.max(value, mask.getf(i, j));
		if (value < result.getf(i, j))
		{
			modif = true;
			result.setf(i, j, value);
		}
	}
}
