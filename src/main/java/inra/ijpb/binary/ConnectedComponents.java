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
package inra.ijpb.binary;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import inra.ijpb.morphology.FloodFill;

/**
 * Several static methods for computing connected components in binary images. 
 * Results are label images, whose bit-depth can be specified.
 * 
 * @deprecated all methods have been moved to the BinaryImages class 
 */
@Deprecated
public class ConnectedComponents
{
	/**
	 * Computes the labels in the binary 2D or 3D image contained in the given
	 * ImagePlus, and computes the maximum label to set up the display range
	 * of the resulting ImagePlus.  
	 * 
	 * @param imagePlus contains the 3D binary image stack
	 * @param conn the connectivity, either 4 or 8 for planar images, or 6 or 26 for 3D images
	 * @param bitDepth the number of bits used to create the result stack (8, 16 or 32)
	 * @return an ImagePlus containing the label of each connected component.
	 * 
	 * @deprecated replaced by BinaryImages.componentsLabeling(ImagePlus, int, int)
	 */
	@Deprecated
	public final static ImagePlus computeLabels(ImagePlus imagePlus, int conn, int bitDepth)
	{
		ImagePlus labelPlus;
		int nLabels;
	
		if (imagePlus.getStackSize() == 1)
		{
			ImageProcessor labels = computeLabels(imagePlus.getProcessor(),
					conn, bitDepth);
			labelPlus = new ImagePlus("Labels", labels);
			nLabels = findMax(labels);
		} else 
		{
			ImageStack labels = computeLabels(imagePlus.getStack(), conn,
					bitDepth);
			labelPlus = new ImagePlus("Labels", labels);
			nLabels = findMax(labels);
		}
		
		labelPlus.setDisplayRange(0, nLabels);
		return labelPlus;
	}

	/**
	 * Computes the labels of the connected components in the given planar binary 
	 * image. The type of result is controlled by the bitDepth option.
	 * 
	 * @param image contains the binary image (any type is accepted) 
	 * @param conn the connectivity, either 4 or 8 
	 * @param bitDepth the number of bits used to create the result stack (8, 16 or 32)
	 * @return a new instance of ImageProcessor containing the label of each connected component.
	 * 
	 * @deprecated replaced by BinaryImages.componentsLabeling(ImageProcessor, int, int)
	 */
	@Deprecated
	public final static ImageProcessor computeLabels(ImageProcessor image,
			int conn, int bitDepth) 
	{
		// get image size
		int width = image.getWidth();
		int height = image.getHeight();

		ImageProcessor labels;
		switch (bitDepth) {
		case 8: labels = new ByteProcessor(width, height); break; 
		case 16: labels = new ShortProcessor(width, height); break; 
		case 32: labels = new FloatProcessor(width, height); break;
		default: throw new IllegalArgumentException("Bit Depth should be 8, 16 or 32.");
		}

		// the label counter
		int nLabels = 0;

		// iterate on image pixels to fin new regions
		for (int y = 0; y < height; y++) 
		{
			IJ.showProgress(y, height);
			for (int x = 0; x < width; x++) 
			{
				if (image.get(x, y) == 0)
					continue;
				if (labels.get(x, y) > 0)
					continue;

				nLabels++;
				FloodFill.floodFillFloat(image, x, y, labels, nLabels, conn);
			}
		}
		IJ.showProgress(1);

		labels.setMinAndMax(0, nLabels);
		return labels;
	}

	/**
	 * Computes the labels of the connected components in the given 3D binary 
	 * image. The type of result is controlled by the bitDepth option.
	 * 
	 * @param image contains the 3D binary image (any type is accepted) 
	 * @param conn the connectivity, either 6 or 26 
	 * @param bitDepth the number of bits used to create the result stack (8, 16 or 32)
	 * @return a new instance of ImageStack containing the label of each connected component.
	 * 
	 * @deprecated replaced by BinaryImages.componentsLabeling(ImageStack, int, int)
	 */
	@Deprecated
	public final static ImageStack computeLabels(ImageStack image, int conn,
			int bitDepth) 
	{
		if ( Thread.currentThread().isInterrupted() )					
			return null;
		
		// get image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		IJ.showStatus("Allocate Memory");
		ImageStack labels = ImageStack.create(sizeX, sizeY, sizeZ, bitDepth);

		int nLabels = 0;

		IJ.showStatus("Compute Labels...");
		for (int z = 0; z < sizeZ; z++) 
		{
			IJ.showProgress(z, sizeZ);
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					// Do not process background voxels
					if (image.getVoxel(x, y, z) == 0)
						continue;

					// Do not process voxels already labeled
					if (labels.getVoxel(x, y, z) > 0)
						continue;

					// a new label is found: increment label index, and propagate 
					nLabels++;
					FloodFill.floodFillFloat(image, x, y, z, labels, nLabels, conn);
				}
			}
		}
		IJ.showProgress(1);
		return labels;
	}

	/**
	 * Computes maximum value in the input 2D image.
	 * This method is used to compute display range of result ImagePlus.
	 */
	private final static int findMax(ImageProcessor image) 
	{
		// get image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		
		// find maximum value over voxels
		int maxVal = 0;
		for (int y = 0; y < sizeY; y++) 
		{
			IJ.showProgress(y, sizeY);
			for (int x = 0; x < sizeX; x++) 
			{
				maxVal = Math.max(maxVal, image.get(x, y));
			}
		}
		IJ.showProgress(1);
		
		return maxVal;
	}
	
	/**
	 * Computes maximum value in the input 3D image.
	 * This method is used to compute display range of result ImagePlus.
	 */
	private final static int findMax(ImageStack image) 
	{
		// get image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		// find maximum value over voxels
		int maxVal = 0;
		for (int z = 0; z < sizeZ; z++) 
		{
			IJ.showProgress(z, sizeZ);
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					maxVal = Math.max(maxVal, (int) image.getVoxel(x, y, z));
				}
			}
		}
		IJ.showProgress(1);
		
		return maxVal;
	}
}
