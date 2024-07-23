/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
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
package inra.ijpb.data;

/**
 * Identifies the position of a voxel in a 3D image by using 3 integer coordinates. 
 *
 * @see Cursor2D
 */
public class Cursor3D 
{
	private int x = 0;
	private int y = 0;
	private int z = 0;
	
	/**
	 * Creates a new cursor from two coordinates.
	 * 
	 * @param x
	 *            the x-coordinate
	 * @param y
	 *            the y-coordinate
	 * @param z
	 *            the z-coordinate
	 */
	public Cursor3D(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * Sets the position of this cursor.
	 * 
	 * @param x
	 *            the new x-coordinate
	 * @param y
	 *            the new y-coordinate
	 * @param z
	 *            the new z-coordinate
	 */
	public void set(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * @return the x-position of this cursor
	 */
	public int getX()
	{
		return x;
	}
	
	/**
	 * @return the y-position of this cursor
	 */
	public int getY()
	{
		return y;
	}
	
	/**
	 * @return the z-position of this cursor
	 */
	public int getZ()
	{
		return z;
	}

	@Override
	public boolean equals( Object other )
	{
	    if (other == null) return false;
	    if (other == this) return true;
	    if ( !( other instanceof Cursor3D ) )
	    	return false;
	    Cursor3D c = (Cursor3D) other;
	    return c.x == this.x && c.y == this.y && c.z == this.z;
	}
	/**
	 * Calculate Euclidean distance to another cursor
	 * @param c cursor to calculate distance to
	 * @return Euclidean distance between this cursor and the input one
	 */
	public double euclideanDistance( Cursor3D c )
	{
		final double x = this.x - c.x;
		final double y = this.y - c.y;
		final double z = this.z - c.z;
		return Math.sqrt( x * x + y * y + z * z);
	}
}
