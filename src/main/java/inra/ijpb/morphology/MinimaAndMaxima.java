/**
 * 
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
 * @see GeodesicReconstruction
 * 
 * @author David Legland
 *
 */
public class MinimaAndMaxima {

	/**
	 * Private constructor to prevent class instantiation.
	 */
	private MinimaAndMaxima()
	{
	}

	/**
	 * The default connectivity used by reconstruction algorithms in 2D images.
	 */
	public final static int DEFAULT_CONNECTIVITY_2D = 4;
	
	/**
	 * Computes the regional maxima in grayscale image <code>image</code>, 
	 * using the default connectivity.
	 */
	public final static ImageProcessor regionalMaxima(ImageProcessor image) {
		return regionalMaxima(image, DEFAULT_CONNECTIVITY_2D);
	}

	/**
	 * Computes the regional maxima in grayscale image <code>image</code>, 
	 * using the specified connectivity.
	 * @param conn the connectivity for maxima, that should be either 4 or 8
	 */
	public final static ImageProcessor regionalMaxima(ImageProcessor image,
			int conn) {
		RegionalExtremaAlgo algo = new RegionalExtremaByFlooding();
		algo.setConnectivity(conn);
		algo.setExtremaType(ExtremaType.MAXIMA);
		
		return algo.applyTo(image);
	}
	
	/**
	 * Computes the regional maxima in grayscale image <code>image</code>, 
	 * using the specified connectivity, and a slower algorithm (used for testing).
	 * @param conn the connectivity for maxima, that should be either 4 or 8
	 */
	public final static ImageProcessor regionalMaximaByReconstruction(
			ImageProcessor image,
			int conn) {
		ImageProcessor mask = image.duplicate();
		mask.add(1);
		
//		GeodesicReconstructionAlgo algo = new GeodesicReconstructionByDilation(conn);
		GeodesicReconstructionAlgo algo = new GeodesicReconstructionHybrid(
				GeodesicReconstructionType.BY_DILATION, conn);
		ImageProcessor rec = algo.applyTo(image, mask);
		
		int width = image.getWidth();
		int height = image.getHeight();
		ImageProcessor result = new ByteProcessor(width, height);
		
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
	 */
	public final static ImageProcessor regionalMinima(ImageProcessor image) {
		return regionalMinima(image, DEFAULT_CONNECTIVITY_2D);
	}

	/**
	 * Computes the regional minima in grayscale image <code>image</code>, 
	 * using the specified connectivity.
	 * @param conn the connectivity for minima, that should be either 4 or 8
	 */
	public final static ImageProcessor regionalMinima(ImageProcessor image,
			int conn) {
		RegionalExtremaAlgo algo = new RegionalExtremaByFlooding();
		algo.setConnectivity(conn);
		algo.setExtremaType(ExtremaType.MINIMA);
		
		return algo.applyTo(image);
	}
	
	/**
	 * Computes the regional minima in grayscale image <code>image</code>, 
	 * using the specified connectivity, and a slower algorithm (used for testing).
	 * @param conn the connectivity for minima, that should be either 4 or 8
	 */
	public final static ImageProcessor regionalMinimaByReconstruction(ImageProcessor image,
			int conn) {
		ImageProcessor marker = image.duplicate();
		marker.add(1);
		
//		GeodesicReconstructionAlgo algo = new GeodesicReconstructionByErosion(conn);
		GeodesicReconstructionAlgo algo = new GeodesicReconstructionHybrid(
				GeodesicReconstructionType.BY_EROSION, conn);
		ImageProcessor rec = algo.applyTo(marker, image);
		
		int width = image.getWidth();
		int height = image.getHeight();
		ImageProcessor result = new ByteProcessor(width, height);
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
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
	 */
	public final static ImageProcessor extendedMaxima(ImageProcessor image,
			int dynamic) {
		return extendedMaxima(image, dynamic, DEFAULT_CONNECTIVITY_2D);
	}

	/**
	 * Computes the extended maxima in grayscale image <code>image</code>, 
	 * keeping maxima with the specified dynamic, and using the specified
	 * connectivity.
	 */
	public final static ImageProcessor extendedMaxima(ImageProcessor image,
			int dynamic, int conn) {
		ImageProcessor mask = image.duplicate();
		mask.add(dynamic);
		
//		GeodesicReconstructionAlgo algo = new GeodesicReconstructionByDilation(conn);
		GeodesicReconstructionAlgo algo = new GeodesicReconstructionHybrid(
				GeodesicReconstructionType.BY_DILATION, conn);
		ImageProcessor rec = algo.applyTo(image, mask);
		
		return regionalMaxima(rec, conn);
	}

	/**
	 * Computes the extended minima in grayscale image <code>image</code>, 
	 * keeping minima with the specified dynamic, and using the default 
	 * connectivity.
	 */
	public final static ImageProcessor extendedMinima(ImageProcessor image, int dynamic) {
		return extendedMinima(image, dynamic, DEFAULT_CONNECTIVITY_2D);
	}

	/**
	 * Computes the extended minima in grayscale image <code>image</code>, 
	 * keeping minima with the specified dynamic, and using the specified 
	 * connectivity.
	 */
	public final static ImageProcessor extendedMinima(ImageProcessor image,
			int dynamic, int conn) {
		ImageProcessor marker = image.duplicate();
		marker.add(dynamic);
		
//		GeodesicReconstructionAlgo algo = new GeodesicReconstructionByErosion(conn);
		GeodesicReconstructionAlgo algo = new GeodesicReconstructionHybrid(
				GeodesicReconstructionType.BY_EROSION, conn);
		ImageProcessor rec = algo.applyTo(marker, image);

		return regionalMinima(rec, conn);
	}

	/**
	 * Imposes the maxima given by marker image into the input image, using 
	 * the default connectivity.
	 */
	public final static ImageProcessor imposeMaxima(ImageProcessor image,
			ImageProcessor maxima) {
		return imposeMaxima(image, maxima, DEFAULT_CONNECTIVITY_2D);
	}
	
	/**
	 * Imposes the maxima given by marker image into the input image, using
	 * the specified connectivity.
	 */
	public final static ImageProcessor imposeMaxima(ImageProcessor image,
			ImageProcessor maxima, int conn) {
		
		ImageProcessor marker = image.duplicate();
		ImageProcessor mask = image.duplicate();
		
		int width = image.getWidth();
		int height = image.getHeight();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (maxima.get(x, y) > 0) { 
					marker.set(x, y, 255);
					mask.set(x, y, 255);
				} else {
					marker.set(x, y, 0);
					mask.set(x, y, image.get(x, y)-1);
				}
			}
		}
		
		return GeodesicReconstruction.reconstructByDilation(marker, mask, conn);
	}

	/**
	 * Imposes the minima given by marker image into the input image, using 
	 * the default connectivity.
	 */
	public final static ImageProcessor imposeMinima(ImageProcessor image,
			ImageProcessor minima) {
		return imposeMinima(image, minima, DEFAULT_CONNECTIVITY_2D);
	}
	
	/**
	 * Imposes the minima given by marker image into the input image, using 
	 * the specified connectivity.
	 */
	public final static ImageProcessor imposeMinima(ImageProcessor image,
			ImageProcessor minima, int conn) {
		
		int width = image.getWidth();
		int height = image.getHeight();
		
		ImageProcessor marker = image.duplicate();
		ImageProcessor mask = image.duplicate();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (minima.get(x, y) > 0) { 
					marker.set(x, y, 0);
					mask.set(x, y, 0);
				} else {
					marker.set(x, y, 255);
					mask.set(x, y, image.get(x, y)+1);
				}
			}
		}
		
		return GeodesicReconstruction.reconstructByErosion(marker, mask, conn);
	}

}
