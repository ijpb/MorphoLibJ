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
package inra.ijpb.morphology;

import ij.ImageStack;
import inra.ijpb.algo.DefaultAlgoListener;
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
 * @deprecated replaced by the Reconstruction class (2017.07.25)
 * 
 * @see Reconstruction
 * 
 * @author David Legland
 */
@Deprecated
public abstract class GeodesicReconstruction3D 
{
	/**
	 * Private constructor to prevent class instantiation.
	 */
	private GeodesicReconstruction3D()
	{
	}

	/**
	 * Removes the border of the input image, by performing a geodesic 
	 * reconstruction initialized with image boundary.
	 *  
	 * @see #fillHoles(ImageStack)
	 * 
	 * @param image the image to process
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
	 * performing a geodesic reconstruction initialized with inverted image
	 * boundary and (3) by inverting the result.
	 * 
	 * @see #killBorders(ImageStack)
	 * 
	 * @param image the image to process
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
	 * Static method to computes the geodesic reconstruction by dilation of 
	 * the marker image under the mask image.
	 * 
	 * @param marker input marker image
	 * @param mask mask image
	 * @return the result of 3D geodesic reconstruction
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
		return algo.applyTo(marker, mask);
	}

	/**
	 * Static method to computes the geodesic reconstruction by dilation of 
	 * the marker image under the mask image.
	 * 
	 * @param marker input marker image
	 * @param mask mask image
	 * @param connectivity 3d connectivity (6 or 26)
	 * @return the result of 3D geodesic reconstruction
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
		return algo.applyTo(marker, mask);
	}

	/**
	 * Static method to computes the geodesic reconstruction by dilation of 
	 * the marker image under the mask image, but restricted to a binary mask.
	 * 
	 * @param marker input marker image
	 * @param mask mask image
	 * @param connectivity 3d connectivity (6 or 26)
	 * @param binaryMask binary mask to restrict area of application
	 * @return geodesic reconstruction by dilation of input image
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
		return algo.applyTo( marker, mask, binaryMask );
	}
	
	
	/**
	 * Static method to computes the geodesic reconstruction by erosion of the
	 * marker image over the mask image.
	 * 
	 * @param marker input marker image
	 * @param mask mask image
	 * @return the result of 3D geodesic reconstruction
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
		return algo.applyTo(marker, mask);
	}

	/**
	 * Static method to computes the geodesic reconstruction by erosion of the
	 * marker image over the mask image.

	 * @param marker input marker image
	 * @param mask mask image
	 * @param connectivity 3d connectivity (6 or 26)
	 * @return the result of 3D geodesic reconstruction
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
		return algo.applyTo(marker, mask);
	}
}
