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
package inra.ijpb.watershed;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.morphology.MinimaAndMaxima;
import inra.ijpb.morphology.MinimaAndMaxima3D;

/**
 * <p>
 * Static methods for directly computing result of watershed after imposition of
 * extended minima.
 * </p>
 * 
 * Example of use:
 * <pre><code>
 *  ImagePlus imagePlus = IJ.getImage();
 *  ImageProcessor image = imagePlus.getProcessor();
 *  // Computes basins, using a dynamic of 10, and a connectivity equal to 4.
 *  ImageProcessor basins = ExtendedMinimaWatershed.extendedMinimaWatershed(image, 10, 4);
 *  ImagePlus resPlus = new ImagePlus("Basins", basins);
 *  resPlus.show();
 * </code></pre>
 * 
 */
public class ExtendedMinimaWatershed 
{
	/**
	 * Computes watershed on a gray scale image after imposition of extended
	 * minima.
	 * 
	 * @param imagePlus
	 *            the input image (grayscale)
	 * @param dynamic
	 *            the maximum difference between the minima and the boundary of
	 *            a basin
	 * @param connectivity
	 *            the connectivity to use
	 * @return the image of watershed basins computed on original image, as a
	 *         label image
	 * @see inra.ijpb.morphology.MinimaAndMaxima
	 */
	public static final ImagePlus extendedMinimaWatershed(
			ImagePlus imagePlus, int dynamic, int connectivity) 
	{
		ImagePlus resultPlus;
		String newName = imagePlus.getShortTitle() + "-watershed";
		if (imagePlus.getStackSize() == 1)  
		{
			ImageProcessor image = imagePlus.getProcessor();
			ImageProcessor result = extendedMinimaWatershed(image, dynamic, connectivity);
			resultPlus = new ImagePlus(newName, result);
		}
		else
		{
			ImageStack image = imagePlus.getStack();
			ImageStack result = extendedMinimaWatershed(image, dynamic, connectivity);
			resultPlus = new ImagePlus(newName, result);
		}
		
		resultPlus.setCalibration( imagePlus.getCalibration() );
		return resultPlus;
	}

	/**
	 * Computes watershed on a gray scale image after imposition of extended minima.
	 * 
	 * @param image
	 *            the input image (grayscale)
	 * @param dynamic
	 *            the maximum difference between the minima and the boundary of
	 *            a basin
	 * @param connectivity
	 *            the connectivity to use, either 4 or 8
	 * @param outputType
	 *            image output type (16 or 32 bit)
	 * @return the image of watershed basins computed on original image, as a
	 *         label image
	 * @see inra.ijpb.morphology.MinimaAndMaxima
	 */
	public static final ImageProcessor extendedMinimaWatershed(
			ImageProcessor image, int dynamic, int connectivity, int outputType )
	{
		ImageProcessor minima = MinimaAndMaxima.extendedMinima(image, dynamic, connectivity);
		ImageProcessor imposedMinima = MinimaAndMaxima.imposeMinima(image, minima, connectivity);
		ImageProcessor labels = BinaryImages.componentsLabeling(minima, connectivity, outputType);
		ImageProcessor basins = Watershed.computeWatershed(imposedMinima, labels, connectivity, true);
		return basins;
	}

	/**
	 * Computes watershed on a gray scale image after imposition of extended
	 * minima.
	 *
	 * @param image
	 *            the input image (grayscale)
	 * @param mask
	 * 			  mask image to constraint segmentation
	 * @param dynamic
	 *            the maximum difference between the minima and the boundary of
	 *            a basin
	 * @param connectivity
	 *            the connectivity to use, either 4 or 8
	 * @param outputType
	 *            image output type (16 or 32 bit)
	 * @param verbose
	 * 			  flag to show log messages
	 * @return the image of watershed basins computed on original image, as a
	 *         label image
	 * @see inra.ijpb.morphology.MinimaAndMaxima
	 */
	public static final ImageProcessor extendedMinimaWatershed(
			ImageProcessor image,
			ImageProcessor mask,
			int dynamic,
			int connectivity,
			int outputType,
			boolean verbose )
	{
		ImageProcessor minima =
				MinimaAndMaxima.extendedMinima( image, dynamic, connectivity );
		ImageProcessor imposedMinima =
				MinimaAndMaxima.imposeMinima( image, minima, connectivity );
		ImageProcessor labels =
				BinaryImages.componentsLabeling( minima, connectivity, outputType );
		ImageProcessor basins =
				Watershed.computeWatershed( imposedMinima, labels, mask,
						connectivity, true, verbose);
		return basins;
	}
	/**
	 * Computes watershed on a gray scale image after imposition of extended
	 * minima.
	 *
	 * @param image
	 *            the input image (grayscale)
	 * @param mask
	 * 			  mask image to constraint segmentation
	 * @param dynamic
	 *            the maximum difference between the minima and the boundary of
	 *            a basin
	 * @param connectivity
	 *            the connectivity to use, either 4 or 8
	 * @param outputType
	 *            image output type (16 or 32 bit)
	 * @param verbose
	 * 			  flag to show log messages
	 * @return the image of watershed basins computed on original image, as a
	 *         label image
	 * @see inra.ijpb.morphology.MinimaAndMaxima3D
	 */
	public static final ImageStack extendedMinimaWatershed(
			ImageStack image,
			ImageStack mask,
			int dynamic,
			int connectivity,
			int outputType,
			boolean verbose )
	{
		ImageStack minima =
				MinimaAndMaxima3D.extendedMinima( image, dynamic, connectivity );
		ImageStack imposedMinima =
				MinimaAndMaxima3D.imposeMinima( image, minima, connectivity );
		ImageStack labels =
				BinaryImages.componentsLabeling( minima, connectivity, outputType );
		ImageStack basins =
				Watershed.computeWatershed( imposedMinima, labels, mask,
						connectivity, true, verbose );
		return basins;
	}
	/**
	 * Computes watershed on a gray scale image after imposition of extended minima.
	 * 
	 * @param image
	 *            the input 3D image (grayscale)
	 * @param dynamic
	 *            the maximum difference between the minima and the boundary of
	 *            a basin
	 * @param connectivity
	 *            the connectivity to use, either 6 or 26
	 * @param outputType
	 *            image output type (16 or 32 bit)
	 * @return the image of watershed basins computed on original image, as a
	 *         label image
	 *         
	 * @see inra.ijpb.morphology.MinimaAndMaxima3D
	 */
	public static final ImageStack extendedMinimaWatershed(
			ImageStack image, int dynamic, int connectivity, int outputType )
	{
		ImageStack minima = MinimaAndMaxima3D.extendedMinima(image, dynamic, connectivity);
		ImageStack imposedMinima = MinimaAndMaxima3D.imposeMinima(image, minima, connectivity);
		ImageStack labels = BinaryImages.componentsLabeling(minima, connectivity, outputType);
		ImageStack basins = Watershed.computeWatershed(imposedMinima, labels, connectivity, true);
		return basins;
	}
	/**
	 * Computes watershed on a gray scale image after imposition of extended minima.
	 *
	 * @param image
	 *            the input image (grayscale)
	 * @param dynamic
	 *            the maximum difference between the minima and the boundary of
	 *            a basin
	 * @param connectivity
	 *            the connectivity to use, either 4 or 8
	 * @return the image of watershed basins computed on original image, as a
	 *         label image
	 * @see inra.ijpb.morphology.MinimaAndMaxima
	 */
	public static final ImageProcessor extendedMinimaWatershed(
			ImageProcessor image, int dynamic, int connectivity)
	{
		return ExtendedMinimaWatershed.extendedMinimaWatershed( image, dynamic, connectivity, 32 );
	}

	/**
	 * Computes watershed on a gray scale image after imposition of extended
	 * minima.
	 *
	 * @param image
	 *            the input image (grayscale)
	 * @param mask
	 * 			  mask image to constraint segmentation
	 * @param dynamic
	 *            the maximum difference between the minima and the boundary of
	 *            a basin
	 * @param connectivity
	 *            the connectivity to use, either 4 or 8
	 * @param verbose
	 * 			  flag to show log messages
	 * @return the image of watershed basins computed on original image, as a
	 *         label image
	 * @see inra.ijpb.morphology.MinimaAndMaxima
	 */
	public static final ImageProcessor extendedMinimaWatershed(
			ImageProcessor image,
			ImageProcessor mask,
			int dynamic,
			int connectivity,
			boolean verbose )
	{
		return ExtendedMinimaWatershed.extendedMinimaWatershed( image, mask, dynamic, connectivity, 32, verbose );
	}
	/**
	 * Computes watershed on a gray scale image after imposition of extended
	 * minima.
	 *
	 * @param image
	 *            the input image (grayscale)
	 * @param mask
	 * 			  mask image to constraint segmentation
	 * @param dynamic
	 *            the maximum difference between the minima and the boundary of
	 *            a basin
	 * @param connectivity
	 *            the connectivity to use, either 4 or 8
	 * @param verbose
	 * 			  flag to show log messages
	 * @return the image of watershed basins computed on original image, as a
	 *         label image
	 * @see inra.ijpb.morphology.MinimaAndMaxima3D
	 */
	public static final ImageStack extendedMinimaWatershed(
			ImageStack image,
			ImageStack mask,
			int dynamic,
			int connectivity,
			boolean verbose )
	{
		return ExtendedMinimaWatershed.extendedMinimaWatershed( image, mask, dynamic, connectivity, 32, verbose );
	}
	/**
	 * Computes watershed on a gray scale image after imposition of extended minima.
	 *
	 * @param image
	 *            the input 3D image (grayscale)
	 * @param dynamic
	 *            the maximum difference between the minima and the boundary of
	 *            a basin
	 * @param connectivity
	 *            the connectivity to use, either 6 or 26
	 * @return the image of watershed basins computed on original image, as a
	 *         label image
	 *
	 * @see inra.ijpb.morphology.MinimaAndMaxima3D
	 */
	public static final ImageStack extendedMinimaWatershed(
			ImageStack image, int dynamic, int connectivity)
	{
		return ExtendedMinimaWatershed.extendedMinimaWatershed( image, dynamic, connectivity, 32 );
	}
}
