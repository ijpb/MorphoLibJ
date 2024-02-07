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
package inra.ijpb.morphology.strel;

import ij.ImageStack;
import ij.plugin.Filters3D;
import inra.ijpb.morphology.Strel3D;

/**
 * A 3D structuring element with an ellipsoidal shape, oriented along the three
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
	 * Creates a structuring element with an ellipsoidal shape of the given radius.
	 * 
	 * @param radius
	 *            the radius of the structuring element, in pixels
	 * @return a new structuring element with spherical shape and specified radius
	 */
	public final static EllipsoidStrel fromRadius(double radius)
	{
		return new EllipsoidStrel(radius);
	}
	
	/**
	 * Creates a structuring element with an ellipsoidal shape from a list of
	 * three radius.
	 * 
	 * @param radiusX
	 *            the radius along the X-axis
	 * @param radiusY
	 *            the radius along the Y-axis
	 * @param radiusZ
	 *            the radius along the Z-axis
	 * @return a new structuring element with ellipsoid shape
	 */
	public final static EllipsoidStrel fromRadiusList(double radiusX, double radiusY, double radiusZ)
	{
		return new EllipsoidStrel(radiusX, radiusY, radiusZ);
	}
	
	/**
	 * Creates a structuring element with an ellipsoidal shape of the given diameter.
	 * 
	 * @param diam
	 *            the diameter of the structuring element, in pixels
	 * @return a new structuring element with spherical shape and specified diameter
	 */
	public final static EllipsoidStrel fromDiameter(double diam)
	{
		return new EllipsoidStrel((diam - 1.0) / 2);
	}
	
	/**
	 * Creates a structuring element with an ellipsoidal shape from a list of
	 * three diameters.
	 * 
	 * @param diamX
	 *            the diameter along the X-axis
	 * @param diamY
	 *            the diameter along the Y-axis
	 * @param diamZ
	 *            the diameter along the Z-axis
	 * @return a new structuring element with ellipsoid shape
	 */
	public final static EllipsoidStrel fromDiameterList(double diamX, double diamY, double diamZ)
	{
		double radiusX = (diamX - 1) / 2;
		double radiusY = (diamY - 1) / 2;
		double radiusZ = (diamZ - 1) / 2;
		return new EllipsoidStrel(radiusX, radiusY, radiusZ);
	}
	
	/**
	 * Private constructor of Ellipsoid structuring element.
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

	/**
	 * Constructor for a structuring element with an ellipsoidal shape from a list of
	 * three radius.
	 * 
	 * @param radiusX
	 *            the radius along the X-axis
	 * @param radiusY
	 *            the radius along the Y-axis
	 * @param radiusZ
	 *            the radius along the Z-axis
	 */
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
		int diamX = 2 * ((int) Math.round(xRadius)) + 1;
		int diamY = 2 * ((int) Math.round(yRadius)) + 1;
		int diamZ = 2 * ((int) Math.round(zRadius)) + 1;
		return new int[]{diamX, diamY, diamZ};
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
        int intRadiusX = (int) Math.round(xRadius);
        int intRadiusY = (int) Math.round(yRadius);
        int intRadiusZ = (int) Math.round(zRadius);
        return new int[] { intRadiusX, intRadiusY, intRadiusZ };
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
	 * Returns this instance, as an ellipsoid is symmetric.
	 * 
	 * @return this instance of EllipsoidStrel
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
		ImageStack result = Filters3D.filter(image, Filters3D.MAX, rx, ry, rz);
		result.setColorModel( image.getColorModel() );
		return result;
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
		ImageStack result = Filters3D.filter(image, Filters3D.MIN, rx, ry, rz);
		result.setColorModel( image.getColorModel() );
		return result;
	}
}
