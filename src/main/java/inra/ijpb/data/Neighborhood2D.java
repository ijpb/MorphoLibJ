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
package inra.ijpb.data;

/**
 * Defines a neighborhood around a pixel in a 2D image.
 *
 * @see Neighborhood3D
 */
public abstract class Neighborhood2D {
	
	Cursor2D cursor;
	
	/**
	 * @return an Iterable over the neighbors of the pixel corresponding to the
	 *         cursor referenced by this Neighborhood.
	 */
	public abstract Iterable<Cursor2D> getNeighbors(  );

	/**
	 * Sets the position of the Neighborhood2D.
	 * 
	 * @param cursor
	 *            the new position of the Neighborhood2D
	 */
	public void setCursor( Cursor2D cursor )
	{
		this.cursor = cursor;
	}
	
	/**
	 * @return the position of the Neighborhood2D.
	 */
	public Cursor2D getCursor()
	{
		return this.cursor;
	}
	
}
