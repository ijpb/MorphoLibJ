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
package inra.ijpb.morphology.extrema;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.FloodFill;

import static java.lang.Math.min;

/**
 * Computes regional extrema in planar images using flooding algorithm. 
 * This class manages 4 and 8 connectivities. All computations are performed
 * using double, making it generic for 8-, 16- or 32-bit images.
 *
 * Example of use:
 * <pre><code>
 * ImageProcessor image = IJ.getImage().getProcessor();
 * RegionalExtremaAlgo algo = new RegionalExtremaByFlooding(); 
 * algo.setExtremaType(ExtremaType.MAXIMA);
 * algo.setConnectivity(4);
 * ImageProcessor result = algo.applyTo(image);
 * ImagePlus resPlus = new ImagePlus("Regional Extrema", result); 
 * resPlus.show(); 
 * </code></pre>
 */
public class RegionalExtremaByFlooding extends RegionalExtremaAlgo 
{
	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.extrema.RegionalExtremaAlgo#getOutput()
	 */
	@Override
	public ImageProcessor applyTo(ImageProcessor inputImage) 
	{
		if (this.connectivity == 4) 
		{
			return regionalExtremaC4(inputImage);
		} 
		else if (this.connectivity == 8) 
		{
			return regionalExtremaC8(inputImage);
		} 
		else 
		{
			throw new IllegalArgumentException("Connectivity must be either 4 or 8, not " + this.connectivity);
		}
	}
	
	/**
	 * Computes regional extrema in current input image, using
	 * flood-filling-like algorithm with 4 connectivity.
	 * Computations are made with double values.
	 */
	private ImageProcessor regionalExtremaC4(ImageProcessor image) 
	{
		// get image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();

		// allocate memory for output, and fill with 255
		ImageProcessor result = new ByteProcessor(sizeX, sizeY);
		result.setValue(255);
		result.fill();
		
		// initialize local data depending on extrema type
		int sign = 1;
		if (this.extremaType == ExtremaType.MAXIMA) 
		{
			sign = -1;
		}
		
		// Iterate over image pixels
		for (int y = 0; y < sizeY; y++) 
		{
			for (int x = 0; x < sizeX; x++) 
			{
				// Check if current pixel was already processed
				if (result.getf(x, y) == 0)
					continue;
				
				// current value
				float currentValue = image.getf(x, y);
				
				// compute extrema value in 4-neighborhood (computes max value
				// if sign is -1)
				float value = currentValue * sign;
				if (x > 0) 
					value = min(value, image.getf(x-1, y) * sign); 
				if (y > 0) 
					value = min(value, image.getf(x, y-1) * sign); 
				if (x < sizeX - 1) 
					value = min(value, image.getf(x+1, y) * sign); 
				if (y < sizeY - 1) 
					value = min(value, image.getf(x, y+1) * sign);
				
				// if one of the neighbors of current pixel has a lower (resp.
				// greater) value, the the current pixel is not an extremum.
				// Consequently, the current pixel, and all its connected 
				// neighbors with same value are set to 0 in the output image. 
				if (value < currentValue * sign)
				{
					FloodFill.floodFillFloat(image, x, y, result, 0.f, 4);
				}
			}
		}
		
		return result;
	}

	/**
	 * Computes regional extrema in current input image, using
	 * flood-filling-like algorithm with 4 connectivity.
	 * Computations are made with double values.
	 */
	private ImageProcessor regionalExtremaC8(ImageProcessor image)
	{
		// get image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();

		// allocate memory for output, and fill with 255
		ImageProcessor result = new ByteProcessor(sizeX, sizeY);
		result.setValue(255);
		result.fill();
		
		// initialize local data depending on extrema type
		int sign = 1;
		if (this.extremaType == ExtremaType.MAXIMA) 
		{
			sign = -1;
		}
		
		// Iterate over image pixels
		for (int y = 0; y < sizeY; y++) 
		{
			for (int x = 0; x < sizeX; x++) 
			{
				// Check if current pixel was already processed
				if (result.getf(x, y) == 0)
					continue;
				
				// current value
				float currentValue = image.getf(x, y);
				
				// compute extrema value in 4-neighborhood (computes max value
				// if sign is -1)
				float value = currentValue * sign;
				for (int y2 = Math.max(y-1, 0); y2 <= Math.min(y+1, sizeY-1); y2++) 
				{
					for (int x2 = Math.max(x-1, 0); x2 <= Math.min(x+1, sizeX-1); x2++) 
					{
						value = min(value, image.getf(x2, y2) * sign);
					}
				}

				// if one of the neighbors of current pixel has a lower (resp.
				// greater) value, the the current pixel is not an extremum.
				// Consequently, the current pixel, and all its connected 
				// neighbors with same value are set to 0 in the output image. 
				if (value < currentValue * sign) 
				{
					FloodFill.floodFillFloat(image, x, y, result, 0.f, 8);
				}
			}
		}
		
		return result;
	}
	
}
