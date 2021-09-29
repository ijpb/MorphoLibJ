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

import java.util.concurrent.atomic.AtomicLong;

/**
*
* License: GPL
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License 2
* as published by the Free Software Foundation.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*
* Author: Ignacio Arganda-Carreras
*/

/**
* Class to store integer pixel coordinates and its 
* corresponding double value 
* 
* @author Ignacio Arganda-Carreras
*/

public class PixelRecord implements Comparable<PixelRecord>{

	Cursor2D cursor = null;
	double value = 0;
	final static AtomicLong seq = new AtomicLong();
	final long seqNum;

	/**
	 * Create pixel record with cursor position and double value
	 * @param cursor pixel position in 2D coordinates
	 * @param value pixel intensity value
	 */
	public PixelRecord(
			final Cursor2D cursor,
			final double value)
	{
		this.cursor = cursor;
		this.value = value;
		seqNum = seq.getAndIncrement();
	}
	
	public PixelRecord(
			final int x,
			final int y,
			final double value )
	{
		this.cursor = new Cursor2D( x, y );
		this.value = value;
		seqNum = seq.getAndIncrement();
	}
	
	
	public Cursor2D getCursor()
	{
		return cursor;
	}
	
	public double getValue()
	{
		return value;
	}

	/**
	 * Compare with a pixel record based on its value and
	 * timestamp
	 * @param v2 voxel record to compare with
	 * @return a value smaller than 0 if the v2 voxel value is 
	 * 			larger this record voxel value, a value larger
	 * 			than 0 if it is lower. If equal, the records
	 * 			created before are set as smaller.  
	 */
	@Override
	public int compareTo( PixelRecord v2 ) 
	{
		int res = Double.compare( value, v2.value );
		if( res == 0 )
			res = (seqNum < v2.seqNum ? -1 : 1);
		
		return res;
	}
	/**
	 * Calculate Euclidean distance to another pixel record
	 * @param pr pixel record to calculate distance to
	 * @return Euclidean distance between this pixel record and the input one
	 */
	public double euclideanDistance( PixelRecord pr )
	{
		return this.cursor.euclideanDistance( pr.getCursor() );
	}
}
