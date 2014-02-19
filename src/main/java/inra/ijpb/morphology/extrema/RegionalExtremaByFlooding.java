/**
 * 
 */
package inra.ijpb.morphology.extrema;

import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.FloodFill;

import static java.lang.Math.min;

/**
 * @author David Legland
 *
 */
public class RegionalExtremaByFlooding extends RegionalExtremaAlgo {

	ImageProcessor inputImage = null;
	ImageProcessor outputImage = null;
	
	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.extrema.RegionalExtremaAlgo#getOutput()
	 */
	@Override
	public ImageProcessor applyTo(ImageProcessor inputImage) {
		this.inputImage = inputImage;
		
		if (this.connectivity == 4) {
			computeRegionalExtremaC4();
			return this.outputImage;
		}
		
		switch (this.extremaType) {
		case MAXIMA:
			this.outputImage = regionalMaxima(this.inputImage, this.connectivity);
			break;
		case MINIMA:
			this.outputImage = regionalMinima(this.inputImage, this.connectivity);
			break;
		}
		return this.outputImage;
	}
	
	/**
	 * Computes regional extrema in current input image, using
	 * flood-filling-like algorithm with 4 connectivity.
	 * Computations are made with double values.
	 */
	private void computeRegionalExtremaC4() {
		// get image size
		int sizeX = this.inputImage.getWidth();
		int sizeY = this.inputImage.getHeight();

		// allocate memory for output, and fill with 255
		this.outputImage = new ByteProcessor(sizeX, sizeY);
		this.outputImage.setValue(255);
		this.outputImage.fill();
		
		// initialize local data depending on extrema type
		int sign = 1;
		if (this.extremaType == ExtremaType.MAXIMA) {
			sign = -1;
		}
		
		// Iterate over image pixels
		for (int y = 0; y < sizeY; y++) {
			for (int x = 0; x < sizeX; x++) {
				// Check if current pixel was already processed
				if (this.outputImage.getf(x, y) == 0)
					continue;
				
				// current value
				float currentValue = this.inputImage.getf(x, y);
				
				// compute extrema value in 4-neighborhood (computes max value
				// if sign is -1)
				float value = currentValue * sign;
				if (x > 0) 
					value = min(value, this.inputImage.getf(x-1, y) * sign); 
				if (y > 0) 
					value = min(value, this.inputImage.getf(x, y-1) * sign); 
				if (x < sizeX - 1) 
					value = min(value, this.inputImage.getf(x+1, y) * sign); 
				if (y < sizeY - 1) 
					value = min(value, this.inputImage.getf(x, y+1) * sign);
				
				// if one of the neighbors of current pixel has a lower (resp.
				// greater) value, the the current pixel is not an extremum.
				// Consequently, the current pixel, and all its connected 
				// neighbors with same value are set to 0 in the output image. 
				if (value < currentValue * sign) {
					FloodFill.floodFill(this.inputImage, x, y,
							this.outputImage, 0, 4);
				}
			}
		}
	}

	/**
	 * Computes the regional maxima in grayscale image <code>image</code>, 
	 * using the specified connectivity.
	 * @param conn the connectivity for maxima, that should be either 4 or 8
	 */
	public ImageProcessor regionalMaxima(ImageProcessor image,
			int conn) {
		if (image instanceof FloatProcessor)
			return regionalMaximaFloat(image, conn);
		else
			return regionalMaximaInt(image, conn);
	}
	
	/**
	 * Dispatching method depending on specified connectivity for integer images.
	 */
	private ImageProcessor regionalMaximaInt(ImageProcessor image,
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
	private ImageProcessor regionalMaximaIntC4(ImageProcessor image) {
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
	private ImageProcessor regionalMaximaIntC8(ImageProcessor image) {
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
	private ImageProcessor regionalMaximaFloat(ImageProcessor image,
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
	private ImageProcessor regionalMaximaFloatC4(ImageProcessor image) {
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
	private ImageProcessor regionalMaximaFloatC8(ImageProcessor image) {
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
	private void binariseMaxima(ImageProcessor image) {
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
	 * Computes the regional minima in grayscale image <code>image</code>, 
	 * using the specified connectivity.
	 * @param conn the connectivity for minima, that should be either 4 or 8
	 */
	public ImageProcessor regionalMinima(ImageProcessor image,
			int conn) {
	if (image instanceof FloatProcessor)
		return regionalMinimaFloat(image, conn);
	else
		return regionalMinimaInt(image, conn);
	}

	/**
	 * Dispatching method depending on specified connectivity for integer images.
	 */
	private ImageProcessor regionalMinimaInt(ImageProcessor image,
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
	private ImageProcessor regionalMinimaFloat(ImageProcessor image,
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
	private ImageProcessor regionalMinimaIntC4(ImageProcessor image) {
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
	private ImageProcessor regionalMinimaIntC8(ImageProcessor image) {
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
	private ImageProcessor regionalMinimaFloatC4(ImageProcessor image) {
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
	private ImageProcessor regionalMinimaFloatC8(ImageProcessor image) {
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
	private void binariseMinima(ImageProcessor image) {
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
	
}
