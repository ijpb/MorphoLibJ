/**
 * 
 */
package inra.ijpb.data;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Defines the connectivity for 3D images.
 */
public interface Connectivity3D
{
	/**
	 * 3D connectivity that considers the six orthogonal neighbors of a voxel.
	 */
	public static final Connectivity3D C6 = new Connectivity3D()
	{
		@Override
		public Collection<Cursor3D> getNeighbors(Cursor3D cursor)
		{
			ArrayList<Cursor3D> array = new ArrayList<Cursor3D>(6);
			int x = cursor.getX();
			int y = cursor.getY();
			int z = cursor.getZ();

			array.add(new Cursor3D(x, y, z-1));
			array.add(new Cursor3D(x, y-1, z));
			array.add(new Cursor3D(x-1, y, z));
			array.add(new Cursor3D(x+1, y, z));
			array.add(new Cursor3D(x, y+1, z));
			array.add(new Cursor3D(x, y, z+1));

			return array;
		}
		
		@Override
		public Collection<Cursor3D> getNeighbors(int x, int y, int z)
		{
			ArrayList<Cursor3D> array = new ArrayList<Cursor3D>(6);
			array.add(new Cursor3D(x, y, z-1));
			array.add(new Cursor3D(x, y-1, z));
			array.add(new Cursor3D(x-1, y, z));
			array.add(new Cursor3D(x+1, y, z));
			array.add(new Cursor3D(x, y+1, z));
			array.add(new Cursor3D(x, y, z+1));
			return array;
		}

		@Override
		public Collection<Cursor3D> getOffsets3D()
		{
			ArrayList<Cursor3D> array = new ArrayList<Cursor3D>(6);
			array.add(new Cursor3D( 0,  0, -1));
			array.add(new Cursor3D( 0, -1,  0));
			array.add(new Cursor3D(-1,  0,  0));
			array.add(new Cursor3D(+1,  0,  0));
			array.add(new Cursor3D( 0, +1,  0));
			array.add(new Cursor3D( 0,  0, +1));
			return array;
		}

	};
	
	/**
	 * 3D connectivity that considers the all 26 neighbors of a voxel.
	 */
	public static final Connectivity3D C26 = new Connectivity3D()
	{
		@Override
		public Collection<Cursor3D> getNeighbors(Cursor3D cursor)
		{
			ArrayList<Cursor3D> array = new ArrayList<Cursor3D>(26);
			
			int x = cursor.getX();
			int y = cursor.getY();
			int z = cursor.getZ();

			for (int dz = -1; dz <= 1; dz++)
			{
				for (int dy = -1; dy <= 1; dy++)
				{
					for (int dx = -1; dx <= 1; dx++)
					{
						if (dx != 0 || dy != 0 || dz != 0)
						{
							array.add(new Cursor3D(x + dx, y + dy, z + dz));
						}
					}
				}
			}
			return array;
		}

		@Override
		public Collection<Cursor3D> getNeighbors(int x, int y, int z)
		{
			ArrayList<Cursor3D> array = new ArrayList<Cursor3D>(26);
			for (int dz = -1; dz <= 1; dz++)
			{
				for (int dy = -1; dy <= 1; dy++)
				{
					for (int dx = -1; dx <= 1; dx++)
					{
						if (dx != 0 || dy != 0 || dz != 0)
						{
							array.add(new Cursor3D(x + dx, y + dy, z + dz));
						}
					}
				}
			}
			return array;
		}

		@Override
		public Collection<Cursor3D> getOffsets3D()
		{
			ArrayList<Cursor3D> array = new ArrayList<Cursor3D>(26);
			for (int dz = -1; dz <= 1; dz++)
			{
				for (int dy = -1; dy <= 1; dy++)
				{
					for (int dx = -1; dx <= 1; dx++)
					{
						if (dx != 0 || dy != 0 || dz != 0)
						{
							array.add(new Cursor3D(dx, dy, dz));
						}
					}
				}
			}
			return array;
		}
		
	};
	
	/**
	 * Returns the set of neighbors associated to a given position
	 * 
	 * @param x
	 *            the x position of the voxel
	 * @param y
	 *            the y position of the voxel
	 * @param z
	 *            the z position of the voxel
	 * @return the list of neighbors of specified voxel
	 */
	public Collection<Cursor3D> getNeighbors(int x, int y, int z);

	/**
	 * Returns the set of neighbors associated to a given position
	 * 
	 * @param cursor
	 *            the position of the voxel
	 * @return the list of neighbors of specified voxel
	 */
	public Collection<Cursor3D> getNeighbors(Cursor3D cursor);
	
	/**
	 * @return the list of 3D offsets computed relative to the center voxel.
	 */
	public Collection<Cursor3D> getOffsets3D();


}
