/**
 * 
 */
package inra.ijpb.morphology.strel;

import ij.ImageStack;
import ij.plugin.Filters3D;
import inra.ijpb.morphology.Strel3D;

/**
 * A 3D structuring element with an ellipsoid shape, oriented along the three
 * main axes of the image.
 *
 * @see BallStrel
 * @author dlegland
 */
public class EllipsoidStrel extends AbstractStrel3D
{
	// ===================================================================
	// Class variables
	
	double xRadius;
	double yRadius;
	double zRadius;


	// ===================================================================
	// Constructors
	
	/**
	 * Creates a structuring element with a spherical shape of the given radius.
	 * 
	 * @param radius
	 *            the radius of the structuring element, in pixels
	 * @return a new structuring element with spherical shape and specified radius
	 */
	public final static EllipsoidStrel fromRadius(double radius)
	{
		return new EllipsoidStrel(radius);
	}
	
	public final static EllipsoidStrel fromRadiusList(double radiusX, double radiusY, double radiusZ)
	{
		return new EllipsoidStrel(radiusX, radiusY, radiusZ);
	}
	
	/**
	 * Creates a structuring element with a spherical shape of the given radius.
	 * 
	 * @param xRadius
	 *            the radius of the structuring element, in pixels
	 * @return a new structuring element with spherical shape and specified diameter
	 */
	public final static EllipsoidStrel fromDiameter(double diam)
	{
		return new EllipsoidStrel((diam - 1.0) / 2);
	}
	
	/**
	 * Private constructor of Ball structuring element.
	 * 
	 * @param radius
	 *            the radius of the structuring element, in pixels
	 */
	private EllipsoidStrel(double radius) 
	{
		this.xRadius = radius;
		this.yRadius = radius;
		this.zRadius = radius;
	}

	private EllipsoidStrel(double xRadius, double yRadius, double zRadius) 
	{
		this.xRadius = xRadius;
		this.yRadius = yRadius;
		this.zRadius = zRadius;
	}

	
	// ===================================================================
	// Implementation of Strel3D interface
	
	@Override
	public int[] getSize()
	{
		int xDiam = 2 * ((int) Math.round(xRadius)) + 1;
		int yDiam = 2 * ((int) Math.round(yRadius)) + 1;
		int zDiam = 2 * ((int) Math.round(zRadius)) + 1;
		return new int[]{xDiam, yDiam, zDiam};
	}

	@Override
	public int[][][] getMask3D()
	{
		// Create an empty image with just a white pixel in the middle
		int sizeX = 2 * ((int) Math.round(xRadius)) + 1;
		int sizeY = 2 * ((int) Math.round(yRadius)) + 1;
		int sizeZ = 2 * ((int) Math.round(zRadius)) + 1;
		ImageStack img = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		img.setVoxel((int) Math.round(xRadius), (int) Math.round(yRadius), (int) Math.round(zRadius), 255);
		
		// apply dilation
		img = this.dilation(img);
		
		// convert to int array
		int[][][] mask = new int[sizeZ][sizeY][sizeX];
		for (int z = 0; z < sizeZ; z++)
		{
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
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
		int intRadius = (int) Math.round(xRadius);
		return new int[]{intRadius, intRadius, intRadius};
	}

	@Override
	public int[][] getShifts3D()
	{
		int[][][] mask = getMask3D();
		int sizeX = 2 * ((int) Math.round(xRadius)) + 1;
		int sizeY = 2 * ((int) Math.round(yRadius)) + 1;
		int sizeZ = 2 * ((int) Math.round(zRadius)) + 1;
		
		// first count the number of voxels
		int n = 0;
		for (int z = 0; z < sizeZ; z++)
		{
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					if (mask[z][y][x] > 0)
						n++;
				}
			}
		}
		
		// second iteration to create the right number of offsets
		int[][] offsets = new int[n][2];
		int i = 0;
		for (int z = 0; z < sizeZ; z++)
		{
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
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

	/**
	 * Returns this instance, as a ball is symmetric.
	 * 
	 * @return this instance of BallStrel
	 */
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
	 * @return the result of the dilation
	 */
	@Override
	public ImageStack dilation(ImageStack image)
	{
		float rx = (float) this.xRadius;
		float ry = (float) this.yRadius;
		float rz = (float) this.zRadius;
		return Filters3D.filter(image, Filters3D.MAX, rx, ry, rz);
	}

	/**
	 * Performs erosion with a ball structuring element by calling the ImageJ
	 * Filters3D.filter method, using Filters3D.MIN option.
	 * 
	 * @param image
	 *            the 3D stack to process
	 * @return the result of the erosion
	 */
	@Override
	public ImageStack erosion(ImageStack image)
	{
		float rx = (float) this.xRadius;
		float ry = (float) this.yRadius;
		float rz = (float) this.zRadius;
		return Filters3D.filter(image, Filters3D.MIN, rx, ry, rz);
	}
}
