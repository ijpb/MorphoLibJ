/**
 * 
 */
package inra.ijpb.morphology;

import static java.lang.Math.max;
import static java.lang.Math.min;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.data.image.ColorImages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Collection of static methods for morphological filters,
 * as well as an enumeration of available methods.
 * 
 * Example of use:
 * <code><pre>
 * ImageProcessor ip = image.getProcessor();
 * Strel se = SquareStrel.fromDiameter(5);
 * ImageProcessor grad = Morphology.gradient(ip, se);
 * ImagePlus res = new ImagePlus("Gradient", grad);
 * res.show(); 
 * </pre></code>
 * @author David Legland
 *
 */
public class Morphology {

	// =======================================================================
	// Enumeration for operations
	
	/**
	 * A pre-defined set of basis morphological operations.
	 */
	public enum Operation {
		EROSION("Erosion"),
		DILATION("Dilation"),
		OPENING("Opening"),
		CLOSING("Closing"), 
		TOPHAT("White Top Hat"),
		BOTTOMHAT("Black Top Hat"),
		GRADIENT("Gradient"), 
		LAPLACIAN("Laplacian"), 
		INTERNAL_GRADIENT("Internal Gradient"), 
		EXTERNAL_GRADIENT("External Gradient");
		
		private final String label;
		
		private Operation(String label) {
			this.label = label;
		}
		
		public ImageProcessor apply(ImageProcessor image, Strel strel) {
			if (this == DILATION)
				return dilation(image, strel);
			if (this == EROSION)
				return erosion(image, strel);
			if (this == CLOSING)
				return closing(image, strel);
			if (this == OPENING)
				return opening(image, strel);
			if (this == TOPHAT)
				return whiteTopHat(image, strel);
			if (this == BOTTOMHAT)
				return blackTopHat(image, strel);
			if (this == GRADIENT)
				return gradient(image, strel);
			if (this == LAPLACIAN)
				return laplacian(image, strel);
			if (this == INTERNAL_GRADIENT)
				return internalGradient(image, strel);
			if (this == EXTERNAL_GRADIENT)
				return externalGradient(image, strel);
			
			throw new RuntimeException(
					"Unable to process the " + this + " morphological operation");
		}
		
		public ImageStack apply(ImageStack stack, Strel3D strel) {
			if (this == DILATION)
				return dilation(stack, strel);
			if (this == EROSION)
				return erosion(stack, strel);
			if (this == CLOSING)
				return closing(stack, strel);
			if (this == OPENING)
				return opening(stack, strel);
			if (this == TOPHAT)
				return whiteTopHat(stack, strel);
			if (this == BOTTOMHAT)
				return blackTopHat(stack, strel);
			if (this == GRADIENT)
				return gradient(stack, strel);
			if (this == LAPLACIAN)
				return laplacian(stack, strel);
			if (this == INTERNAL_GRADIENT)
				return internalGradient(stack, strel);
			if (this == EXTERNAL_GRADIENT)
				return externalGradient(stack, strel);
			
			throw new RuntimeException(
					"Unable to process the " + this + " morphological operation");
		}
		
		public String toString() {
			return this.label;
		}
		
		public static String[] getAllLabels(){
			int n = Operation.values().length;
			String[] result = new String[n];
			
			int i = 0;
			for (Operation op : Operation.values())
				result[i++] = op.label;
			
			return result;
		}
		
		/**
		 * Determines the operation type from its label.
		 * @throws IllegalArgumentException if label is not recognized.
		 */
		public static Operation fromLabel(String opLabel) {
			if (opLabel != null)
				opLabel = opLabel.toLowerCase();
			for (Operation op : Operation.values()) {
				String cmp = op.label.toLowerCase();
				if (cmp.equals(opLabel))
					return op;
			}
			throw new IllegalArgumentException("Unable to parse Operation with label: " + opLabel);
		}
	};
	
	/**
	 * Makes the default constructor private.
	 */
	private Morphology() {
	}

	
	// =======================================================================
	// Main morphological operations
	
	/**
	 * Performs dilation on the input image. 
	 * Dilation is obtained by extracting the maximum value among pixels 
	 * in the neighborhood given by the structuring element.
	 * 
	 * This methods is mainly a wrapper to the dilation method of the strel object.
	 * 
	 * @see #erosion(ImageProcessor, Strel)
	 * @see Strel#dilation(ImageProcessor)
	 */
	public static ImageProcessor dilation(ImageProcessor image, Strel strel) {
		checkImageType(image);
		if (image instanceof ColorProcessor)
			return dilationRGB(image, strel);
		
		return strel.dilation(image);
	}

	/**
	 * Performs morphological dilation on each channel, and reconstitutes the
	 * resulting color image.
	 */
	private static ImageProcessor dilationRGB(ImageProcessor image, Strel strel) {
		// extract channels and allocate memory for result
		Map<String, ByteProcessor> channels = ColorImages.mapChannels(image);
		Collection<ImageProcessor> res = new ArrayList<ImageProcessor>(channels.size());
		
		// Process each channel individually
		for (String name : new String[]{"red", "green", "blue"}) {
			strel.setChannelName(name);
			res.add(strel.dilation(channels.get(name)));
		}
		
		return ColorImages.mergeChannels(res);
	}

	public static ImageStack dilation(ImageStack stack, Strel3D strel) {
		checkImageType(stack);
//		if (image instanceof ColorProcessor)
//			return dilationRGB(image, strel);
		
		return strel.dilation(stack);
	}
	
	/**
	 * Performs erosion on the input image.
	 * Erosion is obtained by extracting the minimum value among pixels 
	 * in the neighborhood given by the structuring element.
	 * 
	 * This methods is mainly a wrapper to the erosion method of the strel object.
	 * 
	 * @see #dilation(ImageProcessor, Strel)
	 * @see Strel#erosion(ImageProcessor)
	 */
	public static ImageProcessor erosion(ImageProcessor image, Strel strel) {
		checkImageType(image);
		if (image instanceof ColorProcessor)
			return erosionRGB(image, strel);

		return strel.erosion(image);
	}

	/**
	 * Performs morphological erosion on each channel, and reconstitutes the
	 * resulting color image.
	 */
	private static ImageProcessor erosionRGB(ImageProcessor image, Strel strel) {
		// extract channels and allocate memory for result
		Map<String, ByteProcessor> channels = ColorImages.mapChannels(image);
		Collection<ImageProcessor> res = new ArrayList<ImageProcessor>(channels.size());
		
		// Process each channel individually
		for (String name : new String[]{"red", "green", "blue"}) {
			strel.setChannelName(name);
			res.add(strel.erosion(channels.get(name)));
		}
		
		return ColorImages.mergeChannels(res);
	}
	
	public static ImageStack erosion(ImageStack stack, Strel3D strel) {
		checkImageType(stack);
		return strel.erosion(stack);
	}

	/**
	 * Performs opening on the input image.
	 * The opening is obtained by performing an erosion followed by an dilation
	 * with the reversed structuring element.
	 * 
	 * This methods is mainly a wrapper to the opening method of the strel object.
	 * 
	 * @see #closing(ImageProcessor, Strel)
	 * @see Strel#opening(ImageProcessor)
	 */
	public static ImageProcessor opening(ImageProcessor image, Strel strel) {
		checkImageType(image);
		if (image instanceof ColorProcessor)
			return openingRGB(image, strel);

		return strel.opening(image);
	}

	/**
	 * Performs morphological opening on each channel, and reconstitutes the
	 * resulting color image.
	 */
	private static ImageProcessor openingRGB(ImageProcessor image, Strel strel) {
		// extract channels and allocate memory for result
		Map<String, ByteProcessor> channels = ColorImages.mapChannels(image);
		Collection<ImageProcessor> res = new ArrayList<ImageProcessor>(channels.size());
		
		// Process each channel individually
		for (String name : new String[]{"red", "green", "blue"}) {
			strel.setChannelName(name);
			res.add(strel.opening(channels.get(name)));
		}
		
		return ColorImages.mergeChannels(res);
	}
	
	public static ImageStack opening(ImageStack stack, Strel3D strel) {
		checkImageType(stack);
		return strel.opening(stack);
	}


	/**
	 * Performs closing on the input image.
	 * The closing is obtained by performing a dilation followed by an erosion
	 * with the reversed structuring element.
	 *  
	 * This methods is mainly a wrapper to the opening method of the strel object.
	 * @see #opening(ImageProcessor, Strel)
	 * @see Strel#closing(ImageProcessor)
	 */
	public static ImageProcessor closing(ImageProcessor image, Strel strel) {
		checkImageType(image);
		if (image instanceof ColorProcessor)
			return closingRGB(image, strel);

		return strel.closing(image);
	}

	/**
	 * Performs morphological closing on each channel, and reconstitutes the
	 * resulting color image.
	 */
	private static ImageProcessor closingRGB(ImageProcessor image, Strel strel) {
		// extract channels and allocate memory for result
		Map<String, ByteProcessor> channels = ColorImages.mapChannels(image);
		Collection<ImageProcessor> res = new ArrayList<ImageProcessor>(channels.size());
		
		// Process each channel individually
		for (String name : new String[]{"red", "green", "blue"}) {
			strel.setChannelName(name);
			res.add(strel.closing(channels.get(name)));
		}
		
		return ColorImages.mergeChannels(res);
	}
	
	public static ImageStack closing(ImageStack stack, Strel3D strel) {
		checkImageType(stack);
		return strel.closing(stack);
	}


	/**
	 * Computes white top hat of the original image.
	 * The white top hat is obtained by subtracting the result of an opening 
	 * from the original image.
	 *  
	 * The white top hat enhances light structures smaller than the structuring element.
	 * 
	 * @see #blackTopHat(ImageProcessor, Strel)
	 * @see #opening(ImageProcessor, Strel)
	 */
	public static ImageProcessor whiteTopHat(ImageProcessor image, Strel strel) {
		checkImageType(image);
		if (image instanceof ColorProcessor)
			return whiteTopHatRGB(image, strel);

		// First performs closing
		ImageProcessor result = strel.opening(image);
		
		// Compute subtraction of result from original image
		int count = image.getPixelCount();
		if (image instanceof ByteProcessor) {
			for (int i = 0; i < count; i++) {
				// Forces computation using integers, because opening with 
				// octagons can greater than original image (bug)
				int v1 = image.get(i);
				int v2 = result.get(i);
				result.set(i, clamp(v1 - v2, 0, 255));
			}
		} else {
			for (int i = 0; i < count; i++) {
				float v1 = image.getf(i);
				float v2 = result.getf(i);
				result.setf(i, v1 - v2);
			}
//		for (int i = 0; i < count; i++) {
//			// Forces computation using integers, because opening with 
//			// octagons can greater than original image (bug)
//			int v1 = image.get(i);
//			int v2 = result.get(i);
//			result.set(i, min(max(v1 - v2, 0), 255));
		}

		return result;
	}
	
	/**
	 * Performs morphological closing on each channel, and reconstitutes the
	 * resulting color image.
	 */
	private static ImageProcessor whiteTopHatRGB(ImageProcessor image, Strel strel) {
		// extract channels and allocate memory for result
//		Collection<ByteProcessor> channels = ColorImages.splitChannels(image);
//		Collection<ImageProcessor> res = new ArrayList<ImageProcessor>(channels.size());
//		
//		// Process each channel individually
//		for(ByteProcessor channel : channels) {
//			res.add(whiteTopHat(channel, strel));
//		}
		Map<String, ByteProcessor> channels = ColorImages.mapChannels(image);
		Collection<ImageProcessor> res = new ArrayList<ImageProcessor>(channels.size());
		
		// Process each channel individually
		for (String name : new String[]{"red", "green", "blue"}) {
			strel.setChannelName(name);
			res.add(whiteTopHat(channels.get(name), strel));
		}
		
		return ColorImages.mergeChannels(res);
	}
	
	public static ImageStack whiteTopHat(ImageStack stack, Strel3D strel) {
		checkImageType(stack);
		
		// First performs closing
		ImageStack result = strel.opening(stack);
		
		// Compute subtraction of result from original image
		int nx = stack.getWidth();
		int ny = stack.getHeight();
		int nz = stack.getSize();
		for (int z = 0; z < nz; z++) {
			for (int y = 0; y < ny; y++) {
				for (int x = 0; x < nx; x++) {
					double v1 = stack.getVoxel(x, y, z);
					double v2 = result.getVoxel(x, y, z);
					result.setVoxel(x, y, z, min(max(v1 - v2, 0), 255));
				}
			}
		}
		
		return result;
	}



	/**
	 * Computes black top hat (or "bottom hat") of the original image.
	 * The black top hat is obtained by subtracting the original image from
	 * the result of a closing.
	 *  
	 * The black top hat enhances dark structures smaller than the structuring element.
	 * 
	 * @see #whiteTopHat(ImageProcessor, Strel)
	 * @see #closing(ImageProcessor, Strel)
	 */
	public static ImageProcessor blackTopHat(ImageProcessor image, Strel strel) {
		checkImageType(image);
		if (image instanceof ColorProcessor)
			return blackTopHatRGB(image, strel);

		// First performs closing
		ImageProcessor result = strel.closing(image);
		
		// Compute subtraction of result from original image
		int count = image.getPixelCount();
		if (image instanceof ByteProcessor) {
			for (int i = 0; i < count; i++) {
				// Forces computation using integers, because closing with 
				// octagons can lower than than original image (bug)
				int v1 = result.get(i);
				int v2 = image.get(i);
				result.set(i, clamp(v1 - v2, 0, 255));
			}
		} else {
			for (int i = 0; i < count; i++) {
				float v1 = result.getf(i);
				float v2 = image.getf(i);
				result.setf(i, v1 - v2);
			}
//		for (int i = 0; i < count; i++) {
//			// Forces computation using integers, because closing with 
//			// octagons can lower than than original image (bug)
//			int v1 = result.get(i);
//			int v2 = image.get(i);
//			result.set(i, min(max(v1 - v2, 0), 255));
		}
		return result;
	}
	
	/**
	 * Performs morphological black top hat on each channel, and reconstitutes
	 * the resulting color image.
	 */
	private static ImageProcessor blackTopHatRGB(ImageProcessor image, Strel strel) {
		// extract channels and allocate memory for result
		Map<String, ByteProcessor> channels = ColorImages.mapChannels(image);
		Collection<ImageProcessor> res = new ArrayList<ImageProcessor>(channels.size());
		
		// Process each channel individually
		for (String name : new String[]{"red", "green", "blue"}) {
			strel.setChannelName(name);
			res.add(blackTopHat(channels.get(name), strel));
		}
		
		return ColorImages.mergeChannels(res);
	}
	
	public static ImageStack blackTopHat(ImageStack stack, Strel3D strel) {
		checkImageType(stack);
		
		// First performs closing
		ImageStack result = strel.closing(stack);
		
		// Compute subtraction of result from original image
		int nx = stack.getWidth();
		int ny = stack.getHeight();
		int nz = stack.getSize();
		for (int z = 0; z < nz; z++) {
			for (int y = 0; y < ny; y++) {
				for (int x = 0; x < nx; x++) {
					double v1 = result.getVoxel(x, y, z);
					double v2 = stack.getVoxel(x, y, z);
					result.setVoxel(x, y, z, min(max(v1 - v2, 0), 255));
				}
			}
		}
		
		return result;
	}

	
	/**
	 * Computes the morphological gradient of the input image.
	 * The morphological gradient is obtained by from the difference of image 
	 * dilation and image erosion computed with the same structuring element. 
	 * 
	 * @see #erosion(ImageProcessor, Strel)
	 * @see #dilation(ImageProcessor, Strel)
	 */
	public static ImageProcessor gradient(ImageProcessor image, Strel strel) {
		checkImageType(image);
		if (image instanceof ColorProcessor)
			return gradientRGB(image, strel);

		// First performs dilation and erosion
		ImageProcessor result = strel.dilation(image);
		ImageProcessor eroded = strel.erosion(image);

		// Subtract erosion from dilation
		int count = image.getPixelCount();
		if (image instanceof ByteProcessor) {
			for (int i = 0; i < count; i++) {
				// Forces computation using integers, because opening with 
				// octagons can greater than original image (bug)
				int v1 = result.get(i);
				int v2 = eroded.get(i);
				result.set(i, clamp(v1 - v2, 0, 255));
			}
		} else {
			for (int i = 0; i < count; i++) {
				float v1 = result.getf(i);
				float v2 = eroded.getf(i);
				result.setf(i, v1 - v2);
			}
//		for (int i = 0; i < count; i++) {
//			// Forces computation using integers, because opening with 
//			// octagons can greater than original image (bug)
//			int v1 = result.get(i);
//			int v2 = eroded.get(i);
//			result.set(i, min(max(v1 - v2, 0), 255));
		}
		// free memory
		eroded = null;
		
		// return gradient
		return result;
	}

	/**
	 * Performs morphological gradient on each channel, and reconstitutes
	 * the resulting color image.
	 */
	private static ImageProcessor gradientRGB(ImageProcessor image, Strel strel) {
		// extract channels and allocate memory for result
		Map<String, ByteProcessor> channels = ColorImages.mapChannels(image);
		Collection<ImageProcessor> res = new ArrayList<ImageProcessor>(channels.size());
		
		// Process each channel individually
		for (String name : new String[]{"red", "green", "blue"}) {
			strel.setChannelName(name);
			res.add(gradient(channels.get(name), strel));
		}
		
		return ColorImages.mergeChannels(res);
	}

	public static ImageStack gradient(ImageStack stack, Strel3D strel) {
		checkImageType(stack);
		
		// First performs dilation and erosion
		ImageStack result = strel.dilation(stack);
		ImageStack eroded = strel.erosion(stack);
		
		// Compute subtraction of result from original image
		int nx = stack.getWidth();
		int ny = stack.getHeight();
		int nz = stack.getSize();
		for (int z = 0; z < nz; z++) {
			for (int y = 0; y < ny; y++) {
				for (int x = 0; x < nx; x++) {
					double v1 = result.getVoxel(x, y, z);
					double v2 = eroded.getVoxel(x, y, z);
					result.setVoxel(x, y, z, min(max(v1 - v2, 0), 255));
				}
			}
		}
		
		return result;
	}


	/**
	 * Computes the morphological Laplacian of the input image.
	 * The morphological gradient is obtained from the difference of the outer
	 * gradient with the inner gradient, both computed with the same 
	 * structuring element. The result is stored in a byte image, centered on 128 
	 * 
	 * Homogeneous regions appear as gray.
	 * 
	 * @see #erosion(ImageProcessor, Strel)
	 * @see #dilation(ImageProcessor, Strel)
	 */
	public static ImageProcessor laplacian(ImageProcessor image, Strel strel) {
		checkImageType(image);
		if (image instanceof ColorProcessor)
			return laplacianRGB(image, strel);

		// First performs dilation and erosion
		ImageProcessor outer = externalGradient(image, strel);
		ImageProcessor inner = internalGradient(image, strel);
		
		// Subtract inner gradient from outer gradient
		ImageProcessor result = image.duplicate();
		int count = image.getPixelCount();
		if (image instanceof ByteProcessor) {
			for (int i = 0; i < count; i++) {
				// Forces computation using integers, because opening with 
				// octagons can be greater than original image (bug)
				int v1 = outer.get(i);
				int v2 = inner.get(i);
				result.set(i, clamp(v1 - v2 + 128, 0, 255));
			}
		} else {
			for (int i = 0; i < count; i++) {
				float v1 = outer.getf(i);
				float v2 = inner.getf(i);
				result.setf(i, v1 - v2);
			}
		}
		// free memory
		outer = null;
		inner = null;
		
		// return gradient
		return result;
	}

	/**
	 * Performs morphological Laplacian on each channel, and reconstitutes
	 * the resulting color image.
	 * 
	 * Homogeneous regions appear as gray.
	 */
	private static ImageProcessor laplacianRGB(ImageProcessor image, Strel strel) {
		// extract channels and allocate memory for result
		Map<String, ByteProcessor> channels = ColorImages.mapChannels(image);
		Collection<ImageProcessor> res = new ArrayList<ImageProcessor>(channels.size());
		
		// Process each channel individually
		for (String name : new String[]{"red", "green", "blue"}) {
			strel.setChannelName(name);
			res.add(laplacian(channels.get(name), strel));
		}
		
		return ColorImages.mergeChannels(res);
	}

	public static ImageStack laplacian(ImageStack stack, Strel3D strel) {
		checkImageType(stack);
		
		// First performs dilation and erosion
		ImageStack outer = externalGradient(stack, strel);
		ImageStack inner = internalGradient(stack, strel);
		
		// Compute subtraction of result from original image
		int nx = stack.getWidth();
		int ny = stack.getHeight();
		int nz = stack.getSize();
		for (int z = 0; z < nz; z++) {
			for (int y = 0; y < ny; y++) {
				for (int x = 0; x < nx; x++) {
					double v1 = outer.getVoxel(x, y, z);
					double v2 = inner.getVoxel(x, y, z);
					outer.setVoxel(x, y, z, min(max(v1 - v2 + 128, 0), 255));
				}
			}
		}
		
		return outer;
	}


	/** 
	 * Computes the morphological internal gradient of the input image.
	 * The morphological internal gradient is obtained by from the difference 
	 * of original image with the result of an erosion.
	 * 
	 * @see #erosion(ImageProcessor, Strel)
	 */
	public static ImageProcessor internalGradient(ImageProcessor image, Strel strel) {
		checkImageType(image);
		if (image instanceof ColorProcessor)
			return internalGradientRGB(image, strel);

		// First performs erosion
		ImageProcessor result = strel.erosion(image);

		// Subtract erosion from dilation
		int count = image.getPixelCount();
		if (image instanceof ByteProcessor) {
			for (int i = 0; i < count; i++) {
				// Forces computation using integers, because opening with 
				// octagons can be greater than original image (bug)
				int v1 = image.get(i);
				int v2 = result.get(i);
				result.set(i, clamp(v1 - v2, 0, 255));
			}
		} else {
			for (int i = 0; i < count; i++) {
				float v1 = image.getf(i);
				float v2 = result.getf(i);
				result.setf(i, v1 - v2);
			}
		}
		// return gradient
		return result;
	}

	private static ImageProcessor internalGradientRGB(ImageProcessor image, Strel strel) {
		// extract channels and allocate memory for result
		Map<String, ByteProcessor> channels = ColorImages.mapChannels(image);
		Collection<ImageProcessor> res = new ArrayList<ImageProcessor>(channels.size());
		
		// Process each channel individually
		for (String name : new String[]{"red", "green", "blue"}) {
			strel.setChannelName(name);
			res.add(internalGradient(channels.get(name), strel));
		}
		
		return ColorImages.mergeChannels(res);
	}

	public static ImageStack internalGradient(ImageStack stack, Strel3D strel) {
		checkImageType(stack);
		
		// First performs dilation and erosion
		ImageStack result = strel.erosion(stack);
		
		// Compute subtraction of result from original image
		int nx = stack.getWidth();
		int ny = stack.getHeight();
		int nz = stack.getSize();
		for (int z = 0; z < nz; z++) {
			for (int y = 0; y < ny; y++) {
				for (int x = 0; x < nx; x++) {
					double v1 = stack.getVoxel(x, y, z);
					double v2 = result.getVoxel(x, y, z);
					result.setVoxel(x, y, z, min(max(v1 - v2, 0), 255));
				}
			}
		}
		
		return result;
	}

	/** 
	 * Computes the morphological external gradient of the input image.
	 * The morphological external gradient is obtained by from the difference 
	 * of the result of a dilation and of the original image .
	 * 
	 * @see #dilation(ImageProcessor, Strel)
	 */
	public static ImageProcessor externalGradient(ImageProcessor image, Strel strel) {
		checkImageType(image);
		if (image instanceof ColorProcessor)
			return externalGradientRGB(image, strel);

		// First performs erosion
		ImageProcessor result = strel.dilation(image);

		// Subtract erosion from dilation
		int count = image.getPixelCount();
		if (image instanceof ByteProcessor) {
			for (int i = 0; i < count; i++) {
				// Forces computation using integers, because opening with 
				// octagons can greater than original image (bug)
				int v1 = result.get(i);
				int v2 = image.get(i);
				result.set(i, clamp(v1 - v2, 0, 255));
			}
		} else {
			for (int i = 0; i < count; i++) {
				float v1 = result.getf(i);
				float v2 = image.getf(i);
				result.setf(i, v1 - v2);
			}
		}
		// return gradient
		return result;
	}

	private static ImageProcessor externalGradientRGB(ImageProcessor image, Strel strel) {
		// extract channels and allocate memory for result
		Map<String, ByteProcessor> channels = ColorImages.mapChannels(image);
		Collection<ImageProcessor> res = new ArrayList<ImageProcessor>(channels.size());
		
		// Process each channel individually
		for (String name : new String[]{"red", "green", "blue"}) {
			strel.setChannelName(name);
			res.add(externalGradient(channels.get(name), strel));
		}
		
		return ColorImages.mergeChannels(res);
	}

	public static ImageStack externalGradient(ImageStack stack, Strel3D strel) {
		checkImageType(stack);
		
		// First performs dilation and erosion
		ImageStack result = strel.dilation(stack);
		
		// Compute subtraction of result from original image
		int nx = stack.getWidth();
		int ny = stack.getHeight();
		int nz = stack.getSize();
		for (int z = 0; z < nz; z++) {
			for (int y = 0; y < ny; y++) {
				for (int x = 0; x < nx; x++) {
					double v1 = result.getVoxel(x, y, z);
					double v2 = stack.getVoxel(x, y, z);
					result.setVoxel(x, y, z, min(max(v1 - v2, 0), 255));
				}
			}
		}
		
		return result;
	}


	// =======================================================================
	// Private utilitary functions
	
	/**
	 * Check that input image can be processed for classical algorithms, and throw an
	 * exception if not the case.
	 * An exception is thrown if image is 16 bits or floating point.
	 * @param image
	 */
	private final static void checkImageType(ImageProcessor image) {
//		if ((image instanceof FloatProcessor)
//				|| (image instanceof ShortProcessor)) {
//			throw new IllegalArgumentException(
//					"Input image must be a ByteProcessor or a ColorProcessor");
//		}
	}

	/**
	 * Check that input image can be processed for classical algorithms, and throw an
	 * exception if not the case.
	 * An exception is thrown if image is 3D or floating point.
	 * @param image
	 */
	private final static void checkImageType(ImageStack stack) {
//		ImageProcessor image = stack.getProcessor(1);
//		if ((image instanceof FloatProcessor) || (image instanceof ShortProcessor)) {
//			throw new IllegalArgumentException("Input image must be a ByteProcessor or a ColorProcessor");
//		}
	}
	
	private final static int clamp(int value, int min, int max) {
		return Math.min(Math.max(value, min), max);
	}
}
