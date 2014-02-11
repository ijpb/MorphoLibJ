/**
 * 
 */
package inra.ijpb.morphology;

import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.geodrec.GeodesicReconstructionAlgo;
import inra.ijpb.morphology.geodrec.GeodesicReconstructionByDilation;
import inra.ijpb.morphology.geodrec.GeodesicReconstructionByErosion;

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
		if (image instanceof FloatProcessor)
			return regionalMaximaFloat(image, conn);
		else
			return regionalMaximaInt(image, conn);
	}
	
	/**
	 * Dispatching method depending on specified connectivity for integer images.
	 */
	private final static ImageProcessor regionalMaximaInt(ImageProcessor image,
			int conn) {
		switch (conn) {
		case 4:
			return regionalMaximaIntC4(image);
		case 8:
			return regionalMaximaIntC8(image);
		default:
			throw new IllegalArgumentException(
					"Unknown connectivity argument: " + conn);
		}
	}
	
	/**
	 * Computes regional maxima in grayscale image <code>image</code>, using
	 * flood-filling-like algorithm with 4 connectivity.
	 */
	private final static ImageProcessor regionalMaximaIntC4(ImageProcessor image) {
		ImageProcessor result = image.duplicate();
		
		int width = image.getWidth();
		int height = image.getHeight();

		int nonMaximaMarker = 0;
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// current value
				int value = result.get(x, y);
				if (value == nonMaximaMarker)
					continue;
				
				// maximum value in 4-neighborhood
				int maxVal = 0;
				if (x > 0) 
					maxVal = Math.max(maxVal, image.get(x-1, y)); 
				if (y > 0) 
					maxVal = Math.max(maxVal, image.get(x, y-1)); 
				if (x < width - 1) 
					maxVal = Math.max(maxVal, image.get(x+1, y)); 
				if (y < height - 1) 
					maxVal = Math.max(maxVal, image.get(x, y+1));
				
				// if one of the neighbors has greater value, the local pixel 
				// is not a maxima. All connected pixels with same value are 
				// set to the marker for maxima.
				if (maxVal > value) {
					FloodFill.floodFillC4(result, x, y, nonMaximaMarker);
				}
			}
		}
		
		if (!(result instanceof ByteProcessor))
			result = result.convertToByteProcessor();
		
		binariseMaxima(result);
		
		return result;
	}

	/**
	 * Computes regional maxima in grayscale image <code>image</code>, using
	 * flood-filling-like algorithm with 8 connectivity.
	 */
	private final static ImageProcessor regionalMaximaIntC8(ImageProcessor image) {
		ImageProcessor result = image.duplicate();
		
		int width = image.getWidth();
		int height = image.getHeight();

		int nonMaximaMarker = 0;
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// current value
				int value = result.get(x, y);
				if (value == nonMaximaMarker)
					continue;
				
				// maximum value in 8-neighborhood
				int maxVal = 0;
				// update maxima with previous line
				if (y > 0) {
					if (x > 0) 
						maxVal = Math.max(maxVal, image.get(x-1, y-1)); 
					maxVal = Math.max(maxVal, image.get(x, y-1));
					if (x < width - 1) 
						maxVal = Math.max(maxVal, image.get(x+1, y-1)); 
				}
				// update maxima with current line
				if (x > 0) 
					maxVal = Math.max(maxVal, image.get(x-1, y)); 
				if (x < width - 1) 
					maxVal = Math.max(maxVal, image.get(x+1, y)); 
				// update maxima with next line
				if (y < height - 1) { 
					if (x > 0) 
						maxVal = Math.max(maxVal, image.get(x-1, y+1)); 
					maxVal = Math.max(maxVal, image.get(x, y+1));
					if (x < width - 1) 
						maxVal = Math.max(maxVal, image.get(x+1, y+1)); 
				}
				
				// if one of the neighbors has greater value, the local pixel 
				// is not a maxima. All connected pixels with same value are 
				// set to the marker for maxima.
				if (maxVal > value) {
					FloodFill.floodFillC8(result, x, y, nonMaximaMarker);
				}
			}
		}

		if (!(result instanceof ByteProcessor))
			result = result.convertToByteProcessor();
		
		binariseMaxima(result);
		
		return result;					
	}

	/**
	 * Dispatching method depending on specified connectivity for float images.
	 */
	private final static ImageProcessor regionalMaximaFloat(ImageProcessor image,
			int conn) {
		switch (conn) {
		case 4:
			return regionalMaximaFloatC4(image);
		case 8:
			return regionalMaximaFloatC8(image);
		default:
			throw new IllegalArgumentException(
					"Unknown connectivity argument: " + conn);
		}
	}
	
	/**
	 * Computes regional maxima in grayscale image <code>image</code>, using
	 * flood-filling-like algorithm with 4 connectivity.
	 */
	private final static ImageProcessor regionalMaximaFloatC4(ImageProcessor image) {
		ImageProcessor result = image.duplicate();
		
		int width = image.getWidth();
		int height = image.getHeight();

		float nonMaximaMarker = 0;
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// current value
				float value = result.getf(x, y);
				if (value == nonMaximaMarker)
					continue;
				
				// maximum value in 4-neighborhood
				float maxVal = 0;
				if (x > 0) 
					maxVal = Math.max(maxVal, image.getf(x-1, y)); 
				if (y > 0) 
					maxVal = Math.max(maxVal, image.getf(x, y-1)); 
				if (x < width - 1) 
					maxVal = Math.max(maxVal, image.getf(x+1, y)); 
				if (y < height - 1) 
					maxVal = Math.max(maxVal, image.getf(x, y+1));
				
				// if one of the neighbors has greater value, the local pixel 
				// is not a maxima. All connected pixels with same value are 
				// set to the marker for maxima.
				if (maxVal > value) {
					FloodFill.floodFillC4(result, x, y, nonMaximaMarker);
				}
			}
		}
		
		// Convert to binary ByteProcessor
		if (!(result instanceof ByteProcessor))
			result = result.convertToByteProcessor();
		binariseMaxima(result);
		
		return result;
	}

	/**
	 * Computes regional maxima in grayscale image <code>image</code>, using
	 * flood-filling-like algorithm with 8 connectivity.
	 */
	private final static ImageProcessor regionalMaximaFloatC8(ImageProcessor image) {
		ImageProcessor result = image.duplicate();
		
		int width = image.getWidth();
		int height = image.getHeight();

		float nonMaximaMarker = 0;
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// current value
				float value = result.getf(x, y);
				if (value == nonMaximaMarker)
					continue;
				
				// maximum value in 8-neighborhood
				float maxVal = 0;
				for (int y2 = Math.max(y-1, 0); y2 <= Math.min(y+1, height-1); y2++) {
					for (int x2 = Math.max(x-1, 0); x2 <= Math.min(x+1, width-1); x2++) {
						maxVal = Math.max(maxVal, image.getf(x2, y2));
					}
				}
				
				// if one of the neighbors has greater value, the local pixel 
				// is not a maxima. All connected pixels with same value are 
				// set to the marker for maxima.
				if (maxVal > value) {
					FloodFill.floodFillC8(result, x, y, nonMaximaMarker);
				}
			}
		}

		// convert to binary ByteProcesor
		if (!(result instanceof ByteProcessor))
			result = result.convertToByteProcessor();
		binariseMaxima(result);
		
		return result;					
	}

	/**
	 *  Transform an image of valued maxima, containing either the maxima value
	 *  or the min value for given image, into a binary image with values 0 for
	 *  non maxima pixels and 255 for regional maxima pixels.
	 */
	private final static void binariseMaxima(ImageProcessor image) {
		int width = image.getWidth();
		int height = image.getHeight();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (image.get(x, y) > 0)
					image.set(x, y, 255);
			}
		}
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
		
		GeodesicReconstructionAlgo algo = new GeodesicReconstructionByDilation(conn);
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
	if (image instanceof FloatProcessor)
		return regionalMinimaFloat(image, conn);
	else
		return regionalMinimaInt(image, conn);
	}

	/**
	 * Dispatching method depending on specified connectivity for integer images.
	 */
	private final static ImageProcessor regionalMinimaInt(ImageProcessor image,
			int conn) {
		switch (conn) {
		case 4:
			return regionalMinimaIntC4(image);
		case 8:
			return regionalMinimaIntC8(image);
		default:
			throw new IllegalArgumentException(
					"Unknown connectivity argument: " + conn);
		}
	}
	
	/**
	 * Dispatching method depending on specified connectivity for float images.
	 */
	private final static ImageProcessor regionalMinimaFloat(ImageProcessor image,
			int conn) {
		switch (conn) {
		case 4:
			return regionalMinimaFloatC4(image);
		case 8:
			return regionalMinimaFloatC8(image);
		default:
			throw new IllegalArgumentException(
					"Unknown connectivity argument: " + conn);
		}
	}

	/**
	 * Computes regional minima in grayscale image <code>image</code>, using
	 * flood-filling-like algorithm with 4 connectivity.
	 */
	private final static ImageProcessor regionalMinimaIntC4(ImageProcessor image) {
		ImageProcessor result = image.duplicate();
		
		int width = image.getWidth();
		int height = image.getHeight();
	
		int nonMinimaMarker;
		switch (image.getBitDepth()) {
		case 8: nonMinimaMarker = 255; break;
		case 16: nonMinimaMarker = (int)(0x0FFFF); break;
		default:
			throw new IllegalArgumentException("Requires 8- or 16-bits image as input");
		}
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// current value
				int value = result.get(x, y);
				if (value == nonMinimaMarker)
					continue;
				
				// maximum value in 4-neighborhood
				int minVal = value;
				if (x > 0) 
					minVal = Math.min(minVal, image.get(x-1, y)); 
				if (y > 0) 
					minVal = Math.min(minVal, image.get(x, y-1)); 
				if (x < width - 1) 
					minVal = Math.min(minVal, image.get(x+1, y)); 
				if (y < height - 1) 
					minVal = Math.min(minVal, image.get(x, y+1));
				
				// if one of the neighbors has lower value, the local pixel 
				// is not a minima. All connected pixels with same value are 
				// set to the marker for non-minima.
				if (minVal < value) {
					FloodFill.floodFillC4(result, x, y, nonMinimaMarker);
				}
			}
		}
		
		// Convert result to binary ByteProcessor
		if (!(result instanceof ByteProcessor))
			result = result.convertToByteProcessor();
		binariseMinima(result);

		return result;
	}

	/**
	 * Computes regional minima in grayscale image <code>image</code>, using
	 * flood-filling-like algorithm with 8 connectivity.
	 */
	private final static ImageProcessor regionalMinimaIntC8(ImageProcessor image) {
		ImageProcessor result = image.duplicate();
		
		int width = image.getWidth();
		int height = image.getHeight();

		int nonMinimaMarker;
		switch (image.getBitDepth()) {
		case 8: nonMinimaMarker = 255; break;
		case 16: nonMinimaMarker = (int)(0x0FFFF); break;
		default:
			throw new IllegalArgumentException("Requires 8- or 16-bits image as input");
		}
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// current value
				int value = result.get(x, y);
				if (value == nonMinimaMarker)
					continue;
				
				// maximum value in 8-neighborhood
				int minVal = value;
				if (y > 0) {
					// check minima on line before
					if (x > 0) 
						minVal = Math.min(minVal, image.get(x-1, y-1)); 
					minVal = Math.min(minVal, image.get(x, y-1));
					if (x < width - 1) 
						minVal = Math.min(minVal, image.get(x+1, y-1)); 
				}

				if (y < height - 1) {
					// check minima on line after
					if (x > 0) 
						minVal = Math.min(minVal, image.get(x-1, y+1)); 
					minVal = Math.min(minVal, image.get(x, y+1));
					if (x < width - 1) 
						minVal = Math.min(minVal, image.get(x+1, y+1)); 
				}

				// check minima on current line
				if (x > 0) 
					minVal = Math.min(minVal, image.get(x-1, y)); 
				if (x < width - 1) 
					minVal = Math.min(minVal, image.get(x+1, y));
				
				
				// if one of the neighbors has lower value, the local pixel 
				// is not a minima. All connected pixels with same value are 
				// set to the marker for non-minima.
				if (minVal < value) {
					FloodFill.floodFillC8(result, x, y, nonMinimaMarker);
				}
			}
		}
		
		// Convert result to binary ByteProcessor
		if (!(result instanceof ByteProcessor))
			result = result.convertToByteProcessor();
		binariseMinima(result);
		
		return result;
	}

	/**
	 * Computes regional minima in float image <code>image</code>, using
	 * flood-filling-like algorithm with 4 connectivity.
	 */
	private final static ImageProcessor regionalMinimaFloatC4(ImageProcessor image) {
		ImageProcessor result = image.duplicate();
		
		int width = image.getWidth();
		int height = image.getHeight();
	
		float nonMinimaMarker = Float.MAX_VALUE;
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// current value
				float value = result.getf(x, y);
				if (value == nonMinimaMarker)
					continue;
				
				// maximum value in 4-neighborhood
				float minVal = value;
				if (x > 0) 
					minVal = Math.min(minVal, image.getf(x-1, y)); 
				if (y > 0) 
					minVal = Math.min(minVal, image.getf(x, y-1)); 
				if (x < width - 1) 
					minVal = Math.min(minVal, image.getf(x+1, y)); 
				if (y < height - 1) 
					minVal = Math.min(minVal, image.getf(x, y+1));
				
				// if one of the neighbors has lower value, the local pixel 
				// is not a minima. All connected pixels with same value are 
				// set to the marker for non-minima.
				if (minVal < value) {
					FloodFill.floodFillC4(result, x, y, nonMinimaMarker);
				}
			}
		}
		
		// Convert result to binary ByteProcessor
		if (!(result instanceof ByteProcessor))
			result = result.convertToByteProcessor();
		binariseMinima(result);

		return result;
	}

	/**
	 * Computes regional minima in float image <code>image</code>, using
	 * flood-filling-like algorithm with 8 connectivity.
	 */
	private final static ImageProcessor regionalMinimaFloatC8(ImageProcessor image) {
		ImageProcessor result = image.duplicate();
		
		int width = image.getWidth();
		int height = image.getHeight();

		float nonMinimaMarker = Float.MAX_VALUE;
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// current value
				float value = result.getf(x, y);
				if (value == nonMinimaMarker)
					continue;
				
				// minimum value in 8-neighborhood
				float minVal = 0;
				for (int y2 = Math.max(y-1, 0); y2 <= Math.min(y+1, height-1); y2++) {
					for (int x2 = Math.max(x-1, 0); x2 <= Math.min(x+1, width-1); x2++) {
						minVal = Math.min(minVal, image.getf(x2, y2));
					}
				}
				
				// if one of the neighbors has lower value, the local pixel 
				// is not a minima. All connected pixels with same value are 
				// set to the marker for non-minima.
				if (minVal < value) {
					FloodFill.floodFillC8(result, x, y, nonMinimaMarker);
				}
			}
		}
		
		// Convert result to binary ByteProcessor
		if (!(result instanceof ByteProcessor))
			result = result.convertToByteProcessor();
		binariseMinima(result);
		
		return result;
	}

	/**
	 *  Transform an image of valued minima, containing either the minima value
	 *  or the max value for given image, into a binary image with values 0 for
	 *  non minima pixels and 255 for regional minima pixels.
	 */
	private final static void binariseMinima(ImageProcessor image) {
		int width = image.getWidth();
		int height = image.getHeight();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (image.get(x, y) == 255)
					image.set(x, y, 0);
				else
					image.set(x, y, 255);
			}
		}
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
		
		GeodesicReconstructionAlgo algo = new GeodesicReconstructionByErosion(conn);
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
		
		GeodesicReconstructionAlgo algo = new GeodesicReconstructionByDilation(conn);
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
		
		GeodesicReconstructionAlgo algo = new GeodesicReconstructionByErosion(conn);
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
