/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2026 INRA.
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
 * A simple class to record the coordinates a pixel in a 2D image.
 * 
 * @see Cursor3D
 */
public class Cursor2D 
{
	private int x = 0;
	private int y = 0;	
		
	/**
	 * Creates a new cursor from two coordinates.
	 * 
	 * @param x
	 *            the x-coordinate
	 * @param y
	 *            the y-coordinate
	 */
	public Cursor2D(
			int x,
			int y )
	{
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Sets the position of this cursor.
	 * 
	 * @param x
	 *            the new x-coordinate
	 * @param y
	 *            the new y-coordinate
	 */
	public void set( 
			int x, 
			int y )
	{
		this.x = x;
		this.y = y;
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

	@Override
	public boolean equals( Object other )
	{
	    if (other == null) return false;
	    if (other == this) return true;
	    if ( !( other instanceof Cursor2D ) )
	    	return false;
	    Cursor2D c = (Cursor2D) other;
	    return c.x == this.x && c.y == this.y;
	}
	/**
	 * Calculate Euclidean distance to another cursor
	 * @param c cursor to calculate distance to
	 * @return Euclidean distance between this cursor and the input one
	 */
	public double euclideanDistance( Cursor2D c )
	{
		final double x = this.x - c.x;
		final double y = this.y - c.y;
		return Math.sqrt( x * x + y * y );
	}
}
