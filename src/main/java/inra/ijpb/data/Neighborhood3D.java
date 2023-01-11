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
package inra.ijpb.data;

/**
 * Defines a neighborhood around a voxel in a 3D image.
 *
 * @see Neighborhood2D
 */
public abstract class Neighborhood3D {
	
	Cursor3D cursor;
	
	/**
	 * @return an Iterable over the neighbors of the voxel corresponding to the
	 *         cursor referenced by this Neighborhood.
	 */
	public abstract Iterable<Cursor3D> getNeighbors(  );
	
	/**
	 * Sets the position of the Neighborhood3D.
	 * 
	 * @param cursor
	 *            the new position of the Neighborhood3D
	 */
	public void setCursor( Cursor3D cursor )
	{
		this.cursor = cursor;
	}
	
	/**
	 * @return the position of the Neighborhood3D.
	 */
	public Cursor3D getCursor()
	{
		return this.cursor;
	}
	
}

