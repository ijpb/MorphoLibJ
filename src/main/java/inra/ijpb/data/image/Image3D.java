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
package inra.ijpb.data.image;

import inra.ijpb.data.Cursor3D;

/**
 * Interface for accessing the values of a 3D stack. Implementations should
 * provide efficient access to the inner data, without checking coordinate
 * bounds. Data can be accessed either as integer or as double. 
 * 
 * <p>
 * Example of use:
 *<pre>{@code
 *	ImageStack stack = IJ.getImage().getStack();
 *	Image3D image = new ByteStackWrapper(stack);
 *	int val = image.get(0, 0, 0);
 *}</pre>
 * 
 * @author David Legland
 * 
 */
public interface Image3D 
{
	/**
	 * Returns the number of voxels along the specified dimension.
	 * 
	 * @param dim
	 *            the dimension, between 0 and 2
	 * @return the size along the given dimension, as a number of voxels
	 */
	public int getSize(int dim);
	
	/**
	 * Returns the value at the specified coordinates as an integer.
	 * 
	 * @param x
	 *            the x-coordinate of the voxel (0-indexed)
	 * @param y
	 *            the y-coordinate of the voxel (0-indexed)
	 * @param z
	 *            the z-coordinate of the voxel (0-indexed)
	 * @return the value at the specified position
	 */
	public int get(int x, int y, int z);

	/**
	 * Changes the value at the specified coordinates, using an integer to
	 * specify the new value.
	 * 
	 * @param x
	 *            the x-coordinate of the voxel (0-indexed)
	 * @param y
	 *            the y-coordinate of the voxel (0-indexed)
	 * @param z
	 *            the z-coordinate of the voxel (0-indexed)
	 * @param value
	 *            the new value at the specified position
	 */
	public void set(int x, int y, int z, int value);

	/**
	 * Returns the value at the specified coordinates as a double.
	 * 
	 * @param x
	 *            the x-coordinate of the voxel (0-indexed)
	 * @param y
	 *            the y-coordinate of the voxel (0-indexed)
	 * @param z
	 *            the z-coordinate of the voxel (0-indexed)
	 * @return the value at the specified position
	 */
	public double getValue(int x, int y, int z);

	/**
	 * Returns the value at the specified coordinates as a double.
	 * 
	 * @param pos
	 *            the position as a Cursor3D
	 * @return the value at the specified position
	 */
	public double getValue(Cursor3D pos);

	/**
	 * Changes the value at the specified coordinates, using a double to
	 * specify the new value.
	 * 
	 * @param x
	 *            the x-coordinate of the voxel (0-indexed)
	 * @param y
	 *            the y-coordinate of the voxel (0-indexed)
	 * @param z
	 *            the z-coordinate of the voxel (0-indexed)
	 * @param value
	 *            the new value at the specified position
	 */
	public void setValue(int x, int y, int z, double value);
	
	/**
	 * Changes the value at the specified coordinates, using a double to
	 * specify the new value.
	 * 
	 * @param pos
	 *            the position of the voxel
	 * @param value
	 *            the new value at the specified position
	 */
	public void setValue(Cursor3D pos, double  value);
}
