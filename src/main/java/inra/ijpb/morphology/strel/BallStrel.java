/**
 * 
 */
package inra.ijpb.morphology.strel;

import ij.ImageStack;
import ij.plugin.Filters3D;
import inra.ijpb.morphology.Strel3D;

/**
 * A 3D structuring element with a ball shape, with same size in each direction.
 * 
 * @see EllipsoidStrel
 * 
 * @author dlegland
 */
public class BallStrel extends AbstractStrel3D
{
	// ===================================================================
	// Class variables
	
	double radius;


	// ===================================================================
	// Constructors
	
	/**
	 * Creates a structuring element with a spherical shape of the given radius.
	 * 
	 * @param radius
	 *            the radius of the structuring element, in pixels
	 * @return a new structuring element with ball shape and specified radius
	 */
	public final static BallStrel fromRadius(double radius)
	{
		return new BallStrel(radius);
	}
	
	/**
	 * Creates a structuring element with a spherical shape of the given radius.
	 * 
	 * @param radius
	 *            the radius of the structuring element, in pixels
	 * @return a new structuring element with ball shape and specified radius
	 */
	public final static BallStrel fromDiameter(double diam)
	{
		return new BallStrel((diam - 1.0) / 2);
	}
	
	/**
	 * Private constructor of ball structuring element.
	 * 
	 * @param radius
	 *            the radius of the ball, in pixels
	 */
	private BallStrel(double radius) 
	{
		this.radius = radius;
	}

	
	// ===================================================================
	// Implementation of Strel3D interface
	
	@Override
	public int[] getSize()
	{
		int radiusInt = (int) Math.round(radius);
		int diam = 2 * radiusInt + 1;
		return new int[]{diam, diam, diam};
	}

	@Override
	public int[][][] getMask3D()
	{
		// Create an empty image with just a white voxel in the middle
		int intRadius = (int) Math.round(radius);
		int size = 2 * intRadius + 1;
		ImageStack img = ImageStack.create(size, size, size, 8);
		img.setVoxel(intRadius, intRadius, intRadius, 255);
		
		// apply dilation
		img = this.dilation(img);
		
		// convert to int array
		int[][][] mask = new int[size][size][size];
		for (int z = 0; z < size; z++)
		{
			for (int y = 0; y < size; y++)
			{
				for (int x = 0; x < size; x++)
				{
					mask[z][y][x] = (int) img.getVoxel(x, y, z);
				}
			}
		}
		return mask;
	}

	@Override
	public int[] getOffset()
	{
		int intRadius = (int) Math.round(radius);
		return new int[]{intRadius, intRadius, intRadius};
	}

	@Override
	public int[][] getShifts3D()
	{
		int intRadius = (int) Math.round(radius);
		int[][][] mask = getMask3D();
		int size = 2 * intRadius + 1;
		
		// first count the number of voxels
		int n = 0;
		for (int z = 0; z < size; z++)
		{
			for (int y = 0; y < size; y++)
			{
				for (int x = 0; x < size; x++)
				{
					if (mask[z][y][x] > 0)
						n++;
				}
			}
		}
		
		// second iteration to create the right number of offsets
		int[][] offsets = new int[n][2];
		int i = 0;
		for (int z = 0; z < size; z++)
		{
			for (int y = 0; y < size; y++)
			{
				for (int x = 0; x < size; x++)
				{
					if (mask[z][y][x] > 0)
					{
						offsets[i][0] = x;
						offsets[i][1] = y;
						i++;
					}
				}
			}
		}		
		return offsets;
	}

	@Override
	public Strel3D reverse()
	{
		return this;
	}

	/**
	 * Performs dilation with a ball structuring element by calling the ImageJ
	 * Filters3D.filter method, using Filters3D.MAX option.
	 * 
	 * @param image
	 *            the 3D stack to process
	 */
	@Override
	public ImageStack dilation(ImageStack image)
	{
		float r = (float) this.radius;
		return Filters3D.filter(image, Filters3D.MAX, r, r, r);
	}

	/**
	 * Performs erosion with a ball structuring element by calling the ImageJ
	 * Filters3D.filter method, using Filters3D.MIN option.
	 * 
	 * @param image
	 *            the 3D stack to process
	 */
	@Override
	public ImageStack erosion(ImageStack image)
	{
		float r = (float) this.radius;
		return Filters3D.filter(image, Filters3D.MIN, r, r, r);
	}
}
