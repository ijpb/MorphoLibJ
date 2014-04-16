/**
 * 
 */
package inra.ijpb.math;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * Provides some methods for combining two images, in a more comprehensive way
 * than standard ImageJ, and providing the possibility of defining its own 
 * local operation. 
 */
public class ImageCalculator
{

	/**
	 * General interface for defining an operation that combines the values of
	 * two pixels to create a new one.
	 * Contains also static instances corresponding to classical operations. 
	 * @author David Legland
	 *
	 */
	public interface Operation {
		public static final Operation PLUS = new Operation() {
			@Override
			public int applyTo(int v1, int v2)
			{
				return v1 + v2;
			}			
		};

		public static final Operation MINUS = new Operation() {
			@Override
			public int applyTo(int v1, int v2)
			{
				return v1 - v2;
			}			
		};

		public static final Operation ABS_DIFF = new Operation() {
			@Override
			public int applyTo(int v1, int v2)
			{
				return Math.abs(v1 - v2);
			}			
		};

		public static final Operation TIMES = new Operation() {
			@Override
			public int applyTo(int v1, int v2)
			{
				return v1 * v2;
			}			
		};

		public static final Operation DIVIDES = new Operation() {
			@Override
			public int applyTo(int v1, int v2)
			{
				return v1 / v2;
			}			
		};

		public static final Operation MAX = new Operation() {
			@Override
			public int applyTo(int v1, int v2)
			{
				return Math.max(v1, v2);
			}			
		};
		
		public static final Operation MIN = new Operation() {
			@Override
			public int applyTo(int v1, int v2)
			{
				return Math.min(v1, v2);
			}			
		};
		
		public static final Operation MEAN = new Operation() {
			@Override
			public int applyTo(int v1, int v2)
			{
				return (v1 + v2) / 2;
			}			
		};
		
		public static final Operation AND = new Operation() {
			@Override
			public int applyTo(int v1, int v2)
			{
				return v1 & v2;
			}			
		};

		public static final Operation OR = new Operation() {
			@Override
			public int applyTo(int v1, int v2)
			{
				return v1 | v2;
			}			
		};

		public static final Operation XOR = new Operation() {
			@Override
			public int applyTo(int v1, int v2)
			{
				return v1 ^ v2;
			}			
		};

		/**
		 * The method to override to make it possible to use an operation.
		 * @param v1 value of pixel in first image
		 * @param v2 value of pixel in second image
		 * @return the result of the combination of the two pixel values
		 */
		public int applyTo(int v1, int v2);
		
	}

	public static final ImagePlus combineImages(ImagePlus image1, ImagePlus image2, Operation op)  
	{
		ImagePlus resPlus;
		if (image1.getStackSize() == 1) 
		{
			ImageProcessor result = combineImages(image1.getProcessor(), image2.getProcessor(), op);
			resPlus = new ImagePlus("result", result);
		} 
		else
		{
			ImageStack result = combineImages(image1.getStack(), image2.getStack(), op);
			resPlus = new ImagePlus("result", result);
		}
		return resPlus;
	}

	/**
	 * Example that computes the maximum over two images:
	 * <code><pre>
	 * ImageProcessor image1 = ...
	 * ImageProcessor image2 = ...
	 * ImageProcessor result = ImageCalculator.combineImages(
	 * 		image1, image2, ImageCalculator.Operation.MAX);  
	 * </pre></code>
	 */
	public static final ImageProcessor combineImages(ImageProcessor image1, ImageProcessor image2, Operation op)  
	{
		int width = image1.getWidth();
		int height = image1.getHeight();

		ImageProcessor result = image1.duplicate();

		int v1, v2, vr;
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				v1 = image1.get(x, y);
				v2 = image2.get(x, y);
				vr = op.applyTo(v1, v2);
				result.set(x, y, vr);
			}
		}

		return result;
	}
	
	public static final ImageStack combineImages(ImageStack image1, ImageStack image2, Operation op)  
	{
		int width = image1.getWidth();
		int height = image1.getHeight();
		int depth = image1.getSize();

		ImageStack result = image1.duplicate();

		int v1, v2, vr;
		for (int z = 0; z < depth; z++)
		{
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					v1 = (int) image1.getVoxel(x, y, z);
					v2 = (int) image2.getVoxel(x, y, z);
					vr = op.applyTo(v1, v2);
					result.setVoxel(x, y, z, vr);
				}
			}
		}
		return result;
	}
	 
	public static final void main(String[] args)
	{
		int width = 255;
		int height = 255;
		ImageProcessor image1 = new ByteProcessor(width, height);
		ImageProcessor image2 = new ByteProcessor(width, height);
		for (int i = 0; i < width; i++)
		{
			for (int j = 0; j < height; j++)
			{
				image1.set(i, j, i);
				image2.set(j, i, i);
			}
		}
		
		ImageCalculator.Operation op = ImageCalculator.Operation.MAX;
		ImageProcessor result = ImageCalculator.combineImages(image1, image2, op);
		
		new ImagePlus("result", result).show();
	}
}
