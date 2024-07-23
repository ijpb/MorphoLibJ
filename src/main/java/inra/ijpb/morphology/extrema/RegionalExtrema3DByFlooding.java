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
package inra.ijpb.morphology.extrema;

import static java.lang.Math.max;
import static java.lang.Math.min;
import ij.ImageStack;
import inra.ijpb.data.image.Image3D;
import inra.ijpb.data.image.Images3D;
import inra.ijpb.morphology.FloodFill3D;

/**
 * Computes regional extrema in 3D images using flooding algorithm. 
 * This class manages 6 and 26 connectivities as well as integer and 
 * floating-point images. 
 * 
 * Example of use:
 * <pre><code>
 * ImageStack image = IJ.getImage().getStack();
 * RegionalExtrema3DAlgo algo = new RegionalExtrema3DFlooding(); 
 * algo.setExtremaType(ExtremaType.MAXIMA);
 * algo.setConnectivity(6);
 * ImageStack result = algo.applyTo(image);
 * ImagePlus resPlus = new ImagePlus("Regional Maxima", result); 
 * resPlus.show(); 
 * </code></pre>
 *
 * @see inra.ijpb.morphology.MinimaAndMaxima3D
 * @see inra.ijpb.morphology.FloodFill
 * 
 * @author David Legland
 */
public class RegionalExtrema3DByFlooding extends RegionalExtrema3DAlgo 
{
	@Override
	public ImageStack applyTo(ImageStack image) 
	{
		return regionalExtremaFloat(image);
	}
	
	/**
	 * Computes regional minima in float 3D image <code>image</code>, using
	 * flood-filling-like algorithm.  
	 */
	// This methods calls the methods with specific connectivity.
	ImageStack regionalExtremaFloat(ImageStack image) 
	{
		switch (this.connectivity) 
		{
		case 6:
			return regionalExtremaFloatC6(image);
		case 26:
			return regionalExtremaFloatC26(image);
		default:
			throw new IllegalArgumentException(
					"Connectivity must be either 6 or 26, not "
							+ this.connectivity);
		}
	}
	
	/**
	 * Computes regional minima in float 3D image <code>image</code>, using
	 * flood-filling-like algorithm with 6 connectivity.
	 */
	ImageStack regionalExtremaFloatC6(ImageStack image) 
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		// create binary image for result, filled with 255.
		fireStatusChanged(this, "Initialize regional extrema");
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		fillStack(result, 255);

		fireStatusChanged(this, "Compute regional extrema");
		
		// initialize local data depending on extrema type
		final int sign = this.extremaType == ExtremaType.MINIMA ? 1 : -1;

		Image3D image2 = Images3D.createWrapper(image);
		Image3D result2 = Images3D.createWrapper(result);
		
		// iterate on image voxels
		for (int z = 0; z < sizeZ; z++) 
		{
			fireProgressChanged(this, z, sizeZ);
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					// Check if current voxel was already processed
					if (result2.getValue(x, y, z) == 0)
						continue;
					
					// current value
					double currentValue = image2.getValue(x, y, z) * sign;
					
					// compute extremum value in 6-neighborhood
					double value = currentValue;
					if (x > 0) 
						value = min(value, image2.getValue(x-1, y, z) * sign); 
					if (x < sizeX - 1) 
						value = min(value, image2.getValue(x+1, y, z) * sign); 
					if (y > 0) 
						value = min(value, image2.getValue(x, y-1, z) * sign); 
					if (y < sizeY - 1) 
						value = min(value, image2.getValue(x, y+1, z) * sign);
					if (z > 0) 
						value = min(value, image2.getValue(x, y, z-1) * sign); 
					if (z < sizeZ - 1) 
						value = min(value, image2.getValue(x, y, z+1) * sign); 

					// if one of the neighbors has lower value, the local pixel 
					// is not a minima. All connected pixels with same value are 
					// set to the marker for non-minima.
					if (value < currentValue) 
					{
						FloodFill3D.floodFillFloat(image2, x, y, z, result2, 0, 6);
					}
				}
			}
		}		

		fireProgressChanged(this, 1, 1);
		return result;
	}

	/**
	 * Computes regional minima in float 3D image <code>image</code>, using
	 * flood-filling-like algorithm with 6 connectivity.
	 */
	ImageStack regionalExtremaFloatC26(ImageStack image)
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		// create binary image for result, filled with 255.
		fireStatusChanged(this, "Initialize regional extrema");
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		fillStack(result, 255);

		fireStatusChanged(this, "Compute regional extrema");
		// initialize local data depending on extrema type
		final int sign = this.extremaType == ExtremaType.MINIMA ? 1 : -1;

		Image3D image2 = Images3D.createWrapper(image);
		Image3D result2 = Images3D.createWrapper(result);
		
		// iterate on image voxels
		for (int z = 0; z < sizeZ; z++) 
		{
			fireProgressChanged(this, z, sizeZ);
			
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++)
				{
					// Check if current voxel was already processed
					if (result2.getValue(x, y, z) == 0)
						continue;
					
					// current value
					double currentValue = image2.getValue(x, y, z) * sign;
					
					// compute extremum value in 26-neighborhood
					double value = currentValue;
					for (int z2 = max(z-1, 0); z2 <= min(z+1, sizeZ-1); z2++) 
					{
						for (int y2 = max(y-1, 0); y2 <= min(y+1, sizeY-1); y2++) 
						{
							for (int x2 = max(x-1, 0); x2 <= min(x+1, sizeX-1); x2++) 
							{
								value = min(value, image2.getValue(x2, y2, z2) * sign);
							}
						}
					}
					
					// if one of the neighbors has lower value, the local voxel
					// is not a minima. All connected pixels with same value are
					// set to the marker for non-minima.
					if (value < currentValue) 
					{
						FloodFill3D.floodFillFloat(image2, x, y, z, result2, 0, 26);
					}
				}
			}
		}
		
		fireProgressChanged(this, 1, 1);
		
		return result;
	}
	
	@Override
	public ImageStack applyTo(ImageStack inputImage, ImageStack maskImage) 
	{
		switch (this.connectivity)
		{
		case 6:
			return regionalExtremaFloatC6(inputImage, maskImage);
		case 26:
			return regionalExtremaFloatC26(inputImage, maskImage);
		default:
			throw new IllegalArgumentException(
					"Connectivity must be either 6 or 26, not "
							+ this.connectivity);
		}
	}
	
	/**
	 * Computes regional extrema in float 3D image <code>image</code>, using
	 * flood-filling-like algorithm with 6 connectivity.
	 */
	ImageStack regionalExtremaFloatC6(ImageStack image, ImageStack mask) 
	{
		if ( Thread.currentThread().isInterrupted() )					
			return null;
		
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		// create binary image for result, filled with 255.
		fireStatusChanged(this, "Initialize regional extrema");
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		fillStack(result, 255);

		fireStatusChanged(this, "Compute regional extrema");

		// initialize local data depending on extrema type
		final int sign = this.extremaType == ExtremaType.MINIMA ? 1 : -1;

		// iterate on image voxels
		for (int z = 0; z < sizeZ; z++) 
		{
			fireProgressChanged(this, z, sizeZ);
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					// Check if the voxel is in the binary mask
					if (mask.getVoxel(x, y, z) == 0)
						continue;

					// Check if current voxel was already processed
					if (result.getVoxel(x, y, z) == 0)
						continue;
					
					// current value
					double currentValue = image.getVoxel(x, y, z) * sign;
					
					// compute extremum value in 6-neighborhood
					double value = currentValue;
					if (x > 0 && mask.getVoxel(x - 1, y, z) != 0)
						value = min(value, image.getVoxel(x - 1, y, z) * sign);
					
					if (x < sizeX - 1 && mask.getVoxel(x + 1, y, z) != 0)
						value = min(value, image.getVoxel(x + 1, y, z) * sign);
					
					if (y > 0 && mask.getVoxel(x, y - 1, z) != 0)
						value = min(value, image.getVoxel(x, y - 1, z) * sign);
					
					if (y < sizeY - 1 && mask.getVoxel(x, y + 1, z) != 0)
						value = min(value, image.getVoxel(x, y + 1, z) * sign);
					
					if (z > 0 && mask.getVoxel(x, y, z - 1) != 0)
						value = min(value, image.getVoxel(x, y, z - 1) * sign);
					
					if (z < sizeZ - 1 && mask.getVoxel(x, y, z + 1) != 0)
						value = min(value, image.getVoxel(x, y, z + 1) * sign);

					// if one of the neighbors has lower value, the local pixel 
					// is not a minima. All connected pixels with same value are 
					// set to the marker for non-minima.
					if (value < currentValue) 
					{
						FloodFill3D.floodFillFloat(image, x, y, z, result, 0, 6);
					}
				}
			}
		}		

		fireProgressChanged(this, 1, 1);
		return result;
	}

	/**
	 * Computes regional extrema in float 3D image <code>image</code>, using
	 * flood-filling-like algorithm with 26 connectivity.
	 */
	ImageStack regionalExtremaFloatC26(ImageStack image, ImageStack mask)
	{
		if ( Thread.currentThread().isInterrupted() )					
			return null;
		
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		// create binary image for result, filled with 255.
		fireStatusChanged(this, "Initialize regional extrema");
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		fillStack(result, 255);

		fireStatusChanged(this, "Compute regional extrema");

		// initialize local data depending on extrema type
		final int sign = this.extremaType == ExtremaType.MINIMA ? 1 : -1;

		// iterate on image voxels
		for (int z = 0; z < sizeZ; z++) 
		{
			fireProgressChanged(this, z, sizeZ);
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++)
				{
					// Check if the voxel is in the binary mask
					if (mask.getVoxel(x, y, z) == 0)
						continue;

					// Check if current voxel was already processed
					if (result.getVoxel(x, y, z) == 0)
						continue;
					
					// current value
					double currentValue = image.getVoxel(x, y, z);
					
					// compute extremum value in 26-neighborhood
					double value = currentValue * sign;
					for (int z2 = max(z-1, 0); z2 <= min(z+1, sizeZ-1); z2++) 
					{
						for (int y2 = max(y-1, 0); y2 <= min(y+1, sizeY-1); y2++) 
						{
							for (int x2 = max(x-1, 0); x2 <= min(x+1, sizeX-1); x2++)
							{
								if (mask.getVoxel(x2, y2, z2) != 0)
									value = min(value, image.getVoxel(x2, y2, z2) * sign);
							}
						}
					}
					
					// if one of the neighbors has lower value, the local pixel 
					// is not a minima. All connected pixels with same value are 
					// set to the marker for non-minima.
					if (value < currentValue * sign) 
					{
						FloodFill3D.floodFillFloat(image, x, y, z, result, 0, 26);
					}
				}
			}
		}		

		fireProgressChanged(this, 1, 1);
		return result;
	}
	
	/**
	 * Fills the 3D image with the given value.
	 */
	private void fillStack(ImageStack image, double value) 
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		for (int z = 0; z < sizeZ; z++) 
		{
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					image.setVoxel(x,  y,  z,  value);
				}
			}
		}
	}
}
