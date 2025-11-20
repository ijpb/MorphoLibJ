/**
 * 
 */
package inra.ijpb.morphology.binary;

import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;

/**
 * A static collection of relational operators that can be used to apply
 * thresholds on 2D/3D images.
 * 
 * @author dlegland
 *
 */
public abstract class Relational extends AlgoStub
{
	/**
	 * Computes the result of the "Equal To" relation between each element of
	 * the image and a specified value.
	 */
	public final static Relational EQ = new Relational()
	{
		@Override
		public ByteProcessor process(ImageProcessor image, double value)
		{
			int width = image.getWidth();
			int height = image.getHeight();
			ByteProcessor res = new ByteProcessor(width, height);

			for (int y = 0; y < height; y++)
			{
				this.fireProgressChanged(this, y, height);
				for (int x = 0; x < width; x++)
				{
					res.set(x, y, image.getf(x, y) == value ? 255 : 0);
				}
			}

			return res;
		}

		@Override
		public ImageStack process(ImageStack image, double value)
		{
			int width = image.getWidth();
			int height = image.getHeight();
			int depth = image.getSize();
			ImageStack res = ImageStack.create(width, height, depth, 8);

			process(image, value, res);

			return res;
		}

		@Override
		public void process(ImageStack image, double value, ImageStack target)
		{
			int width = image.getWidth();
			int height = image.getHeight();
			int depth = image.getSize();

			for (int z = 0; z < depth; z++)
			{
				this.fireProgressChanged(this, z, depth);
				for (int y = 0; y < height; y++)
				{
					for (int x = 0; x < width; x++)
					{
						target.setVoxel(x, y, z, image.getVoxel(x, y, z) == value ? 255 : 0);
					}
				}
			}
		}
	};

	/**
	 * Computes the result of the "Greater than or Equal To" relation between each element of
	 * the image and a specified value.
	 */
	public final static Relational GE = new Relational()
	{
		@Override
		public ByteProcessor process(ImageProcessor image, double value)
		{
			int width = image.getWidth();
			int height = image.getHeight();
			ByteProcessor res = new ByteProcessor(width, height);

			for (int y = 0; y < height; y++)
			{
				this.fireProgressChanged(this, y, height);
				for (int x = 0; x < width; x++)
				{
					res.set(x, y, image.getf(x, y) >= value ? 255 : 0);
				}
			}

			return res;
		}

		@Override
		public ImageStack process(ImageStack image, double value)
		{
			int width = image.getWidth();
			int height = image.getHeight();
			int depth = image.getSize();
			ImageStack res = ImageStack.create(width, height, depth, 8);

			process(image, value, res);

			return res;
		}

		@Override
		public void process(ImageStack image, double value, ImageStack target)
		{
			int width = image.getWidth();
			int height = image.getHeight();
			int depth = image.getSize();

			for (int z = 0; z < depth; z++)
			{
				this.fireProgressChanged(this, z, depth);
				for (int y = 0; y < height; y++)
				{
					for (int x = 0; x < width; x++)
					{
						target.setVoxel(x, y, z, image.getVoxel(x, y, z) >= value ? 255 : 0);
					}
				}
			}
		}
	};

	/**
	 * Computes the result of the "Greater than" relation between each element
	 * of the image and a specified value.
	 */
	public final static Relational GT = new Relational()
	{
		@Override
		public ByteProcessor process(ImageProcessor image, double value)
		{
			int width = image.getWidth();
			int height = image.getHeight();
			ByteProcessor res = new ByteProcessor(width, height);

			for (int y = 0; y < height; y++)
			{
				this.fireProgressChanged(this, y, height);
				for (int x = 0; x < width; x++)
				{
					res.set(x, y, image.getf(x, y) > value ? 255 : 0);
				}
			}

			return res;
		}

		@Override
		public ImageStack process(ImageStack image, double value)
		{
			int width = image.getWidth();
			int height = image.getHeight();
			int depth = image.getSize();
			ImageStack res = ImageStack.create(width, height, depth, 8);

			process(image, value, res);

			return res;
		}

		@Override
		public void process(ImageStack image, double value, ImageStack target)
		{
			int width = image.getWidth();
			int height = image.getHeight();
			int depth = image.getSize();

			for (int z = 0; z < depth; z++)
			{
				this.fireProgressChanged(this, z, depth);
				for (int y = 0; y < height; y++)
				{
					for (int x = 0; x < width; x++)
					{
						target.setVoxel(x, y, z, image.getVoxel(x, y, z) > value ? 255 : 0);
					}
				}
			}
		}
	};

	/**
	 * Computes the result of the "Lower than or equal to" relation between each
	 * element of the image and a specified value.
	 */
	public final static Relational LE = new Relational()
	{
		@Override
		public ByteProcessor process(ImageProcessor image, double value)
		{
			int width = image.getWidth();
			int height = image.getHeight();
			ByteProcessor res = new ByteProcessor(width, height);

			for (int y = 0; y < height; y++)
			{
				this.fireProgressChanged(this, y, height);
				for (int x = 0; x < width; x++)
				{
					res.set(x, y, image.getf(x, y) <= value ? 255 : 0);
				}
			}

			return res;
		}

		@Override
		public ImageStack process(ImageStack image, double value)
		{
			int width = image.getWidth();
			int height = image.getHeight();
			int depth = image.getSize();
			ImageStack res = ImageStack.create(width, height, depth, 8);

			process(image, value, res);

			return res;
		}

		@Override
		public void process(ImageStack image, double value, ImageStack target)
		{
			int width = image.getWidth();
			int height = image.getHeight();
			int depth = image.getSize();

			for (int z = 0; z < depth; z++)
			{
				this.fireProgressChanged(this, z, depth);
				for (int y = 0; y < height; y++)
				{
					for (int x = 0; x < width; x++)
					{
						target.setVoxel(x, y, z, image.getVoxel(x, y, z) <= value ? 255 : 0);
					}
				}
			}
		}
	};

	/**
	 * Computes the result of the "Lower than" relation between each element
	 * of the image and a specified value.
	 */
	public final static Relational LT = new Relational()
	{
		@Override
		public ByteProcessor process(ImageProcessor image, double value)
		{
			int width = image.getWidth();
			int height = image.getHeight();
			ByteProcessor res = new ByteProcessor(width, height);

			for (int y = 0; y < height; y++)
			{
				this.fireProgressChanged(this, y, height);
				for (int x = 0; x < width; x++)
				{
					res.set(x, y, image.getf(x, y) < value ? 255 : 0);
				}
			}

			return res;
		}

		@Override
		public ImageStack process(ImageStack image, double value)
		{
			int width = image.getWidth();
			int height = image.getHeight();
			int depth = image.getSize();
			ImageStack res = ImageStack.create(width, height, depth, 8);

			process(image, value, res);

			return res;
		}

		@Override
		public void process(ImageStack image, double value, ImageStack target)
		{
			int width = image.getWidth();
			int height = image.getHeight();
			int depth = image.getSize();

			for (int z = 0; z < depth; z++)
			{
				this.fireProgressChanged(this, z, depth);
				for (int y = 0; y < height; y++)
				{
					for (int x = 0; x < width; x++)
					{
						target.setVoxel(x, y, z, image.getVoxel(x, y, z) < value ? 255 : 0);
					}
				}
			}
		}
	};

	/**
	 * Computes the result of the "Not Equal to" relation between each element
	 * of the image and a specified value.
	 */
	public final static Relational NE = new Relational()
	{
		@Override
		public ByteProcessor process(ImageProcessor image, double value)
		{
			int width = image.getWidth();
			int height = image.getHeight();
			ByteProcessor res = new ByteProcessor(width, height);

			for (int y = 0; y < height; y++)
			{
				this.fireProgressChanged(this, y, height);
				for (int x = 0; x < width; x++)
				{
					res.set(x, y, image.getf(x, y) != value ? 255 : 0);
				}
			}

			return res;
		}

		@Override
		public ImageStack process(ImageStack image, double value)
		{
			int width = image.getWidth();
			int height = image.getHeight();
			int depth = image.getSize();
			ImageStack res = ImageStack.create(width, height, depth, 8);

			process(image, value, res);

			return res;
		}

		@Override
		public void process(ImageStack image, double value, ImageStack target)
		{
			int width = image.getWidth();
			int height = image.getHeight();
			int depth = image.getSize();

			for (int z = 0; z < depth; z++)
			{
				this.fireProgressChanged(this, z, depth);
				for (int y = 0; y < height; y++)
				{
					for (int x = 0; x < width; x++)
					{
						target.setVoxel(x, y, z, image.getVoxel(x, y, z) != value ? 255 : 0);
					}
				}
			}
		}
	};

	/**
	 * Applies this relational operator to the 2D input image, using the
	 * specified value as threshold.
	 * 
	 * @param image
	 *            the image to process
	 * @param value
	 *            the value to use as threshold
	 * @return a new (binary) image corresponding to the comparison of each
	 *         element within the input image
	 */
	public abstract ByteProcessor process(ImageProcessor image, double value);

	/**
	 * Applies this relational operator to the 3D input image, using the
	 * specified value as threshold.
	 * 
	 * @param image
	 *            the 3D image to process
	 * @param value
	 *            the value to use as threshold
	 * @return a new (binary) 3D image corresponding to the comparison of each
	 *         element within the input image
	 */
	public abstract ImageStack process(ImageStack image, double value);

	/**
	 * Applies this relational operator to the 3D input image, using the
	 * specified value as threshold, and populating the specified target image.
	 * 
	 * @param image
	 *            the 3D image to process
	 * @param value
	 *            the value to use as threshold
	 * @param image
	 *            the 3D image containing the results the comparison of each
	 *            element within the input image with the specified value.
	 */
	public abstract void process(ImageStack image, double value, ImageStack target);

}
