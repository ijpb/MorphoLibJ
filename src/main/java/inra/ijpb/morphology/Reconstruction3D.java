/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2023 INRA.
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
package inra.ijpb.morphology;

import java.util.Map;

import ij.ImageStack;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.data.image.ColorImages;
import inra.ijpb.morphology.geodrec.GeodesicReconstruction3DAlgo;
import inra.ijpb.morphology.geodrec.GeodesicReconstruction3DHybrid0Float;
import inra.ijpb.morphology.geodrec.GeodesicReconstruction3DHybrid0Gray16;
import inra.ijpb.morphology.geodrec.GeodesicReconstruction3DHybrid0Gray8;
import inra.ijpb.morphology.geodrec.GeodesicReconstructionByDilation3DScanning;
import inra.ijpb.morphology.geodrec.GeodesicReconstructionByDilation3DScanningGray8;
import inra.ijpb.morphology.geodrec.GeodesicReconstructionByErosion3DScanning;
import inra.ijpb.morphology.geodrec.GeodesicReconstructionType;


/**
 * <p>
 * Morphological reconstruction for 8-bits grayscale or binary stacks.
 * </p>
 * 
 * <p>
 * This class provides a collection of static methods for commonly used 
 * operations on 3D images, such as border removal or holes filling. 
 * </p>
 * 
 * <p>
 * Example of use:
 * <pre><code>
 * ImageStack mask = IJ.getImage().getStack();
 * int bitDepth = mask.getBitDepth();
 * ImageStack marker = ImageStack.create(mask.getWidth(), mask.getHeight(), mask.getSize(), bitDepth);
 * marker.set(30, 20, 10, 255); 
 * ImageStack rec = Reconstruction3D.reconstructByDilation(marker, mask, 6);
 * ImagePlus res = new ImagePlus("Reconstruction", rec);
 * res.show(); 
 * </code></pre>
 * 
 * @see Reconstruction
 * @see FloodFill3D
 * 
 * @author David Legland
 */
public abstract class Reconstruction3D 
{
	/**
	 * Private constructor to prevent class instantiation.
	 */
	protected Reconstruction3D()
	{
	}

	/**
	 * Removes the border of the input image, by performing a morphological
	 * reconstruction initialized with image boundary.
	 * 
	 * @see #fillHoles(ImageStack)
	 * 
	 * @param image
	 *            the image to process
	 * @return a new image with borders removed
	 */
	public final static ImageStack killBorders(ImageStack image)
	{
		// Image size
		int width = image.getWidth();
		int height = image.getHeight();
		int depth = image.getSize();

		// Initialize marker image with zeros everywhere except at borders
		ImageStack markers = image.duplicate();
		for (int z = 1; z < depth - 1; z++)
		{
			for (int y = 1; y < height - 1; y++)
			{
				for (int x = 1; x < width - 1; x++)
				{
					markers.setVoxel(x, y, z, 0);
				}
			}
		}
		// Reconstruct image from borders to find touching structures
		ImageStack result = reconstructByDilation(markers, image);

		// removes result from original image
		for (int z = 0; z < depth; z++)
		{
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					double val = image.getVoxel(x, y, z) - result.getVoxel(x, y, z);
					result.setVoxel(x, y, z, Math.max(val, 0));
				}
			}
		}

		return result;
	}

	/**
	 * Fills the holes in the input image, by (1) inverting the image, (2)
	 * performing a morphological reconstruction initialized with inverted image
	 * boundary and (3) by inverting the result.
	 * 
	 * @see #killBorders(ImageStack)
	 * 
	 * @param image
	 *            the image to process
	 * @return a new image with holes filled
	 */
	public final static ImageStack fillHoles(ImageStack image) 
	{
		// Image size
		int width = image.getWidth();
		int height = image.getHeight();
		int depth = image.getSize();
		
		// Initialize marker image with white everywhere except at borders
		ImageStack markers = image.duplicate();
		for (int z = 1; z < depth-1; z++) 
		{
			for (int y = 1; y < height-1; y++) 
			{
				for (int x = 1; x < width-1; x++) 
				{
					markers.setVoxel(x, y, z, Float.MAX_VALUE);
				}
			}
		}
		
		// Reconstruct image from borders to find touching structures
		return reconstructByErosion(markers, image);
	}

	/**
	 * Static method to computes the morphological reconstruction by dilation of
	 * the marker image under the mask image.
	 * 
	 * @param marker
	 *            input marker image
	 * @param mask
	 *            mask image
	 * @return the result of 3D morphological reconstruction
	 */
	public final static ImageStack reconstructByDilation(ImageStack marker,
			ImageStack mask)
	{
		GeodesicReconstruction3DAlgo algo;
		if (marker.getBitDepth() == 8 && mask.getBitDepth() == 8)
		{
			algo = new GeodesicReconstruction3DHybrid0Gray8(
					GeodesicReconstructionType.BY_DILATION);
		} 
		else if (marker.getBitDepth() == 16 && mask.getBitDepth() == 16)
		{
			algo = new GeodesicReconstruction3DHybrid0Gray16(
					GeodesicReconstructionType.BY_DILATION);
		} 
		else if (marker.getBitDepth() == 32 && mask.getBitDepth() == 32)
		{
			algo = new GeodesicReconstruction3DHybrid0Float(
					GeodesicReconstructionType.BY_DILATION);
		} 
		else
		{
			algo = new GeodesicReconstructionByDilation3DScanning();
		}

		DefaultAlgoListener.monitor(algo);

		if (marker.getBitDepth() == 24 && mask.getBitDepth() == 24)
		{
			return applyAlgoToRGB(algo, marker, mask);
		}

		return algo.applyTo(marker, mask);
	}

	/**
	 * Static method to computes the morphological reconstruction by dilation of
	 * the marker image under the mask image.
	 * 
	 * @param marker
	 *            input marker image
	 * @param mask
	 *            mask image
	 * @param connectivity
	 *            3d connectivity (6 or 26)
	 * @return the result of 3D morphological reconstruction
	 */
	public final static ImageStack reconstructByDilation(ImageStack marker,
			ImageStack mask, int connectivity)
	{
		GeodesicReconstruction3DAlgo algo;
		if (marker.getBitDepth() == 8 && mask.getBitDepth() == 8)
		{
			algo = new GeodesicReconstruction3DHybrid0Gray8(
					GeodesicReconstructionType.BY_DILATION, connectivity);
		} 
		else if (marker.getBitDepth() == 16 && mask.getBitDepth() == 16)
		{
			algo = new GeodesicReconstruction3DHybrid0Gray16(
					GeodesicReconstructionType.BY_DILATION, connectivity);
		} 
		else if (marker.getBitDepth() == 32 && mask.getBitDepth() == 32)
		{
			algo = new GeodesicReconstruction3DHybrid0Float(
					GeodesicReconstructionType.BY_DILATION, connectivity);
		} 
		else
		{
			algo = new GeodesicReconstructionByDilation3DScanning(connectivity);
		}
		
		DefaultAlgoListener.monitor(algo);
		
		if (marker.getBitDepth() == 24 && mask.getBitDepth() == 24)
		{
			return applyAlgoToRGB(algo, marker, mask);
		}

		return algo.applyTo(marker, mask);
	}

	/**
	 * Static method to computes the morphological reconstruction by dilation of
	 * the marker image under the mask image, but restricted to a binary mask.
	 * 
	 * @param marker
	 *            input marker image
	 * @param mask
	 *            mask image
	 * @param connectivity
	 *            3d connectivity (6 or 26)
	 * @param binaryMask
	 *            binary mask to restrict area of application
	 * @return morphological reconstruction by dilation of input image
	 */
	public final static ImageStack reconstructByDilation(
			ImageStack marker,
			ImageStack mask, 
			int connectivity,
			ImageStack binaryMask ) 
	{
		//TODO: add support for non gray8 stacks
		GeodesicReconstruction3DAlgo algo = new GeodesicReconstructionByDilation3DScanningGray8(
				connectivity);
		DefaultAlgoListener.monitor(algo);
		
		if (marker.getBitDepth() == 24 && mask.getBitDepth() == 24)
		{
			return applyAlgoToRGB(algo, marker, mask);
		}

		return algo.applyTo( marker, mask, binaryMask );
	}
	
	
	/**
	 * Static method to computes the morphological reconstruction by erosion of
	 * the marker image over the mask image.
	 * 
	 * @param marker
	 *            input marker image
	 * @param mask
	 *            mask image
	 * @return the result of 3D morphological reconstruction
	 */
	public final static ImageStack reconstructByErosion(ImageStack marker,
			ImageStack mask)
	{
		GeodesicReconstruction3DAlgo algo;
		if (marker.getBitDepth() == 8 && mask.getBitDepth() == 8)
		{
			algo = new GeodesicReconstruction3DHybrid0Gray8(
					GeodesicReconstructionType.BY_EROSION);

		} 
		else if (marker.getBitDepth() == 16 && mask.getBitDepth() == 16)
		{
			algo = new GeodesicReconstruction3DHybrid0Gray16(
					GeodesicReconstructionType.BY_EROSION);

		} 
		else if (marker.getBitDepth() == 32 && mask.getBitDepth() == 32)
		{
			algo = new GeodesicReconstruction3DHybrid0Float(
					GeodesicReconstructionType.BY_EROSION);
		} 
		else
		{
			algo = new GeodesicReconstructionByErosion3DScanning();
		}
		
		DefaultAlgoListener.monitor(algo);

		if (marker.getBitDepth() == 24 && mask.getBitDepth() == 24)
		{
			return applyAlgoToRGB(algo, marker, mask);
		}

		return algo.applyTo(marker, mask);
	}

	/**
	 * Static method to computes the morphological reconstruction by erosion of
	 * the marker image over the mask image.
	 * 
	 * @param marker
	 *            input marker image
	 * @param mask
	 *            mask image
	 * @param connectivity
	 *            3d connectivity (6 or 26)
	 * @return the result of 3D morphological reconstruction
	 */
	public final static ImageStack reconstructByErosion(ImageStack marker,
			ImageStack mask, int connectivity)
	{
		
		if ( Thread.currentThread().isInterrupted() )					
			return null;
		
		GeodesicReconstruction3DAlgo algo;
		if (marker.getBitDepth() == 8 && mask.getBitDepth() == 8)
		{
			algo = new GeodesicReconstruction3DHybrid0Gray8(
					GeodesicReconstructionType.BY_EROSION, connectivity);
		}
		else if (marker.getBitDepth() == 16 && mask.getBitDepth() == 16)
		{
			algo = new GeodesicReconstruction3DHybrid0Gray16(
					GeodesicReconstructionType.BY_EROSION, connectivity);
		}
		else if (marker.getBitDepth() == 32 && mask.getBitDepth() == 32)
		{
			algo = new GeodesicReconstruction3DHybrid0Float(
					GeodesicReconstructionType.BY_EROSION, connectivity);
		} 
		else
		{
			algo = new GeodesicReconstructionByErosion3DScanning(connectivity);
		}
		
		DefaultAlgoListener.monitor(algo);
		
		if (marker.getBitDepth() == 24 && mask.getBitDepth() == 24)
		{
			return applyAlgoToRGB(algo, marker, mask);
		}

		return algo.applyTo(marker, mask);
	}
	
	/**
	 * Applies an instance of morphological reconstruction algorithm to each
	 * channel of a color image and returns the color image resulting from the
	 * concatenation of each channel.
	 * 
	 * @param algo
	 *            the instance of reconstruction algorithm to apply
	 * @param marker
	 *            the marker color image
	 * @param mask
	 *            the mask color image
	 * @return the result of the algorithm on each pair of channels
	 */
	private final static ImageStack applyAlgoToRGB(
			GeodesicReconstruction3DAlgo algo, ImageStack marker,
			ImageStack mask)
	{
		// extract channels and allocate memory for result
		Map<String, ImageStack> markerChannels 	= ColorImages.mapChannels(marker);
		Map<String, ImageStack> maskChannels 	= ColorImages.mapChannels(mask);
		
		ImageStack resRed 	= algo.applyTo(markerChannels.get("red"), 	maskChannels.get("red"));
		ImageStack resGreen = algo.applyTo(markerChannels.get("green"), maskChannels.get("green"));
		ImageStack resBlue 	= algo.applyTo(markerChannels.get("blue"), 	maskChannels.get("blue"));
		
		return ColorImages.mergeChannels(resRed, resGreen, resBlue);
	}
}
