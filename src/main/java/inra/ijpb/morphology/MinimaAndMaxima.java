/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2017 INRA.
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

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.extrema.ExtremaType;
import inra.ijpb.morphology.extrema.RegionalExtremaAlgo;
import inra.ijpb.morphology.extrema.RegionalExtremaByFlooding;
import inra.ijpb.morphology.geodrec.GeodesicReconstructionAlgo;
import inra.ijpb.morphology.geodrec.GeodesicReconstructionHybrid;
import inra.ijpb.morphology.geodrec.GeodesicReconstructionType;

/**
 * A collection of static methods for computing regional and extended minima
 * and maxima.
 * 
 * Regional extrema algorithms are based on flood-filling-like algorithms, 
 * whereas extended extrema and extrema imposition algorithms use geodesic 
 * reconstruction algorithm.
 * 
 * See the books of Serra and Soille for further details.
 * 
 * <p>
 * Example of use: <pre><code>
 *	// Get current image processor
 *	ImageProcessor image = IJ.getImage().getProcessor();
 *	// Computes extended minima with a dynamic of 15, using the 4-connectivity
 *	ImageProcessor minima = MinimaAndMaxima.extendedMinima(image, 15, 4); 
 *	// Display result in a new imagePlus
 *	ImagePlus res = new ImagePlus("Minima", minima);
 *	res.show(); 
 * </code></pre>
 * 
 * @see GeodesicReconstruction
 * 
 * @author David Legland
 *
 */
public class MinimaAndMaxima
{
	/**
	 * The default connectivity used by reconstruction algorithms in 2D images.
	 */
	private final static int DEFAULT_CONNECTIVITY = 4;
	
	/**
	 * Private constructor to prevent class instantiation.
	 */
	private MinimaAndMaxima()
	{
	}

	/**
	 * Computes the regional maxima in grayscale image <code>image</code>, using
	 * the default connectivity.
	 * 
	 * @param image
	 *            the image to process
	 * @return the regional maxima of input image
	 */
	public final static ImageProcessor regionalMaxima(ImageProcessor image)
	{
		return regionalMaxima(image, DEFAULT_CONNECTIVITY);
	}

	/**
	 * Computes the regional maxima in grayscale image <code>image</code>, using
	 * the specified connectivity.
	 * 
	 * @param image
	 *            the image to process
	 * @param conn
	 *            the connectivity for maxima, that should be either 4 or 8
	 * @return the regional maxima of input image
	 */
	public final static ImageProcessor regionalMaxima(ImageProcessor image,
			int conn)
	{
		RegionalExtremaAlgo algo = new RegionalExtremaByFlooding();
		algo.setConnectivity(conn);
		algo.setExtremaType(ExtremaType.MAXIMA);
		
		return algo.applyTo(image);
	}
	
	/**
	 * Computes the regional maxima in grayscale image <code>image</code>, 
	 * using the specified connectivity, and a slower algorithm (used for testing).
	 * 
	 * @param image
	 *            the image to process
	 * @param conn
	 *            the connectivity for maxima, that should be either 4 or 8
	 * @return the regional maxima of input image
	 */
	public final static ImageProcessor regionalMaximaByReconstruction(
			ImageProcessor image,
			int conn) 
	{
		// Compute mask image
		ImageProcessor mask = image.duplicate();
		mask.add(1);
		
		// Call geodesic reconstruction algorithm
		GeodesicReconstructionAlgo algo = new GeodesicReconstructionHybrid(
				GeodesicReconstructionType.BY_DILATION, conn);
		ImageProcessor rec = algo.applyTo(image, mask);
		
		// allocate memory for result
		int width = image.getWidth();
		int height = image.getHeight();
		ImageProcessor result = new ByteProcessor(width, height);
		
		// create binary result image
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (mask.get(x, y) > rec.get(x, y)) 
					result.set(x,  y, 255);
				else
					result.set(x,  y, 0);
			}
		}
		
		return result;
	}

	/**
	 * Computes the regional minima in grayscale image <code>image</code>, 
	 * using the default connectivity.
	 * 
	 * @param image
	 *            the image to process
	 * @return the regional minima of input image
	 */
	public final static ImageProcessor regionalMinima(ImageProcessor image) 
	{
		return regionalMinima(image, DEFAULT_CONNECTIVITY);
	}

	/**
	 * Computes the regional minima in grayscale image <code>image</code>, 
	 * using the specified connectivity.
	 * 
	 * @param image
	 *            the image to process
	 * @param conn
	 *            the connectivity for minima, that should be either 4 or 8
	 * @return the regional minima of input image
	 */
	public final static ImageProcessor regionalMinima(ImageProcessor image,
			int conn) 
	{
		RegionalExtremaAlgo algo = new RegionalExtremaByFlooding();
		algo.setConnectivity(conn);
		algo.setExtremaType(ExtremaType.MINIMA);
		
		return algo.applyTo(image);
	}
	
	/**
	 * Computes the regional minima in grayscale image <code>image</code>, 
	 * using the specified connectivity, and a slower algorithm (used for testing).
	 * 
	 * @param image
	 *            the image to process
	 * @param conn
	 *            the connectivity for minima, that should be either 4 or 8
	 * @return the regional minima of input image
	 */
	public final static ImageProcessor regionalMinimaByReconstruction(ImageProcessor image,
			int conn)
	{
		ImageProcessor marker = image.duplicate();
		marker.add(1);
		
		GeodesicReconstructionAlgo algo = new GeodesicReconstructionHybrid(
				GeodesicReconstructionType.BY_EROSION, conn);
		ImageProcessor rec = algo.applyTo(marker, image);
		
		int width = image.getWidth();
		int height = image.getHeight();
		ImageProcessor result = new ByteProcessor(width, height);
		
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				if (marker.get(x, y) > rec.get(x, y)) 
					result.set(x,  y, 0);
				else
					result.set(x,  y, 255);
			}
		}
		
		return result;
	}

	/**
	 * Computes the extended maxima in grayscale image <code>image</code>, 
	 * keeping maxima with the specified dynamic, and using the default 
	 * connectivity.
	 * 
	 * @param image
	 *            the image to process
	 * @param dynamic
	 *            the minimal difference between a maxima and its boundary 
	 * @return the extended maxima of input image
	 */
	public final static ImageProcessor extendedMaxima(ImageProcessor image,
			double dynamic)
	{
		return extendedMaxima(image, dynamic, DEFAULT_CONNECTIVITY);
	}

	/**
	 * Computes the extended maxima in grayscale image <code>image</code>, 
	 * keeping maxima with the specified dynamic, and using the specified
	 * connectivity.
	 * 
	 * @param image
	 *            the image to process
	 * @param dynamic
	 *            the minimal difference between a maxima and its boundary 
	 * @param conn
	 *            the connectivity for maxima, that should be either 4 or 8
	 * @return the extended maxima of input image
	 */
	public final static ImageProcessor extendedMaxima(ImageProcessor image,
			double dynamic, int conn)
	{
		ImageProcessor mask = image.duplicate();
		mask.add(dynamic);
		
		GeodesicReconstructionAlgo algo = new GeodesicReconstructionHybrid(
				GeodesicReconstructionType.BY_DILATION, conn);
		ImageProcessor rec = algo.applyTo(image, mask);
		
		return regionalMaxima(rec, conn);
	}

	/**
	 * Computes the extended minima in grayscale image <code>image</code>, 
	 * keeping minima with the specified dynamic, and using the default 
	 * connectivity.
	 * 
	 * @param image
	 *            the image to process
	 * @param dynamic
	 *            the minimal difference between a minima and its boundary 
	 * @return the extended minima of input image
	 */
	public final static ImageProcessor extendedMinima(ImageProcessor image,
			double dynamic)
	{
		return extendedMinima(image, dynamic, DEFAULT_CONNECTIVITY);
	}

	/**
	 * Computes the extended minima in grayscale image <code>image</code>, 
	 * keeping minima with the specified dynamic, and using the specified 
	 * connectivity.
	 * 
	 * @param image
	 *            the image to process
	 * @param dynamic
	 *            the minimal difference between a minima and its boundary 
	 * @param conn
	 *            the connectivity for minima, that should be either 4 or 8
	 * @return the extended minima of input image
	 */
	public final static ImageProcessor extendedMinima(ImageProcessor image,
			double dynamic, int conn)
	{
		ImageProcessor marker = image.duplicate();
		marker.add(dynamic);
		
		GeodesicReconstructionAlgo algo = new GeodesicReconstructionHybrid(
				GeodesicReconstructionType.BY_EROSION, conn);
		ImageProcessor rec = algo.applyTo(marker, image);

		return regionalMinima(rec, conn);
	}

	/**
	 * Imposes the maxima given by marker image into the input image, using 
	 * the default connectivity.
	 * 
	 * @param image
	 *            the image to process
	 * @param maxima
	 *            a binary image of maxima 
	 * @return the result of maxima imposition
	 */
	public final static ImageProcessor imposeMaxima(ImageProcessor image,
			ImageProcessor maxima)
	{
		return imposeMaxima(image, maxima, DEFAULT_CONNECTIVITY);
	}
	
	/**
	 * Imposes the maxima given by marker image into the input image, using
	 * the specified connectivity.
	 * 
	 * @param image
	 *            the image to process
	 * @param maxima
	 *            a binary image of maxima 
	 * @param conn
	 *            the connectivity for maxima, that should be either 4 or 8
	 * @return the result of maxima imposition
	 */
	public final static ImageProcessor imposeMaxima(ImageProcessor image,
			ImageProcessor maxima, int conn)
	{
		ImageProcessor marker = image.duplicate();
		ImageProcessor mask = image.duplicate();
		
		int width = image.getWidth();
		int height = image.getHeight();
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				if (maxima.get(x, y) > 0)
				{
					marker.set(x, y, 255);
					mask.set(x, y, 255);
				} 
				else
				{
					marker.set(x, y, 0);
					mask.set(x, y, image.get(x, y)-1);
				}
			}
		}
		
		return Reconstruction.reconstructByDilation(marker, mask, conn);
	}

	/**
	 * Imposes the minima given by marker image into the input image, using 
	 * the default connectivity.
	 * 
	 * @param image
	 *            the image to process
	 * @param minima
	 *            a binary image of minima 
	 * @return the result of minima imposition
	 */
	public final static ImageProcessor imposeMinima(ImageProcessor image,
			ImageProcessor minima)
	{
		return imposeMinima(image, minima, DEFAULT_CONNECTIVITY);
	}
	
	/**
	 * Imposes the minima given by marker image into the input image, using 
	 * the specified connectivity.
	 * 
	 * @param image
	 *            the image to process
	 * @param minima
	 *            a binary image of minima 
	 * @param conn
	 *            the connectivity for minima, that should be either 4 or 8
	 * @return the result of minima imposition
	 */
	public final static ImageProcessor imposeMinima(ImageProcessor image,
			ImageProcessor minima, int conn)
	{
		int width = image.getWidth();
		int height = image.getHeight();
		
		ImageProcessor marker = image.duplicate();
		ImageProcessor mask = image.duplicate();
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				if (minima.get(x, y) > 0)
				{
					marker.set(x, y, 0);
					mask.set(x, y, 0);
				} 
				else
				{
					marker.set(x, y, 255);
					mask.set(x, y, image.get(x, y)+1);
				}
			}
		}
		
		return Reconstruction.reconstructByErosion(marker, mask, conn);
	}
}
