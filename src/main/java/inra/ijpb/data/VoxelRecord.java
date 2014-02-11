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
 */
public class VoxelRecord implements Comparable<VoxelRecord>{

		int i = 0;
		int j = 0;
		int k = 0;
		double value = 0;
		
		public VoxelRecord(
				final int i,
				final int j,
				final int k,
				final double value)
		{
			this.i = i;
			this.j = j;
			this.k = k;
			this.value = value;
		}
		
		public int getI(){ return this.i; }
		public int getJ(){ return this.j; }
		public int getK(){ return this.k; }
		
		public int[] getCoordinates()
		{
			return new int[]{ i, j, k };
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
