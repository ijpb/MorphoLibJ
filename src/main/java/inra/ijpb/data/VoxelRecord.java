package inra.ijpb.data;


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
 * Class to store integer voxel coordinates and its 
 * corresponding double value 
 * 
 * @author Ignacio Arganda-Carreras
 */
public class VoxelRecord implements Comparable<VoxelRecord>{

		Cursor3D cursor = null;
		double value = 0;
		
		public VoxelRecord(
				final Cursor3D cursor,
				final double value)
		{
			this.cursor = cursor;
			this.value = value;
		}
		
		public VoxelRecord(
				final int x,
				final int y,
				final int z,
				final double value )
		{
			this.cursor = new Cursor3D(x, y, z);
			this.value = value;
		}
		
		
		public Cursor3D getCursor()
		{
			return cursor;
		}
		
		public double getValue()
		{
			return value;
		}

		@Override
		public int compareTo(VoxelRecord voxelRecord) 
		{
			return Double.compare( value, voxelRecord.value );	    		   
		}
}
