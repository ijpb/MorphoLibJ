/**
 * 
 */
package inra.ijpb.data;

import inra.ijpb.data.Cursor2D;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Defines the connectivity for planar images.
 * 
 * Contains static classes for common C4 and C8 connectivities, and C6
 * connectivity (orthogonal neighbors plus lower-left and upper right neighbors).
 * 
 * @author dlegland
 *
 */
public interface Connectivity2D
{
	/**
	 * Planar connectivity that consider the four orthogonal neighbors of a pixel.
	 */
	public static final Connectivity2D C4 = new Connectivity2D()
	{
		@Override
		public Collection<Cursor2D> getNeighbors(int x, int y)
		{
			ArrayList<Cursor2D> array = new ArrayList<Cursor2D>(4);
			array.add(new Cursor2D(x, y-1));
			array.add(new Cursor2D(x-1, y));
			array.add(new Cursor2D(x+1, y));
			array.add(new Cursor2D(x, y+1));
			return array;
		}

		@Override
		public Collection<Cursor2D> getOffsets()
		{
			ArrayList<Cursor2D> array = new ArrayList<Cursor2D>(4);
			array.add(new Cursor2D( 0, -1));
			array.add(new Cursor2D(-1,  0));
			array.add(new Cursor2D(+1,  0));
			array.add(new Cursor2D( 0, +1));
			return array;
		}

		@Override
		public Connectivity2D getForwardConnectivity()
		{
			return new Connectivity2D()
			{

				@Override
				public Collection<Cursor2D> getNeighbors(int x, int y)
				{
					ArrayList<Cursor2D> array = new ArrayList<Cursor2D>(2);
					array.add(new Cursor2D(x, y-1));
					array.add(new Cursor2D(x-1, y));
					return array;
				}

				@Override
				public Collection<Cursor2D> getOffsets()
				{
					ArrayList<Cursor2D> array = new ArrayList<Cursor2D>(2);
					array.add(new Cursor2D( 0, -1));
					array.add(new Cursor2D(-1,  0));
					return array;
				}

				@Override
				public Connectivity2D getForwardConnectivity()
				{
					return this;
				}

				@Override
				public Connectivity2D getBackwardConnectivity()
				{
					throw new RuntimeException("Can not return backward connectivity");
				}
				
			};
		}

		@Override
		public Connectivity2D getBackwardConnectivity()
		{
			return new Connectivity2D()
			{

				@Override
				public Collection<Cursor2D> getNeighbors(int x, int y)
				{
					ArrayList<Cursor2D> array = new ArrayList<Cursor2D>(2);
					array.add(new Cursor2D(x+1, y));
					array.add(new Cursor2D(x, y+1));
					return array;
				}

				@Override
				public Collection<Cursor2D> getOffsets()
				{
					ArrayList<Cursor2D> array = new ArrayList<Cursor2D>(2);
					array.add(new Cursor2D(+1,  0));
					array.add(new Cursor2D( 0, +1));
					return array;
				}

				@Override
				public Connectivity2D getForwardConnectivity()
				{
					throw new RuntimeException("Can not return forward connectivity");
				}

				@Override
				public Connectivity2D getBackwardConnectivity()
				{
					return this;
				}				
			};
		}
	};

	/**
	 * Defines the C6_1 connectivity, corresponding to orthogonal neighbors plus
	 * lower-left and upper right neighbors.
	 */
	public static final Connectivity2D C6_1 = new Connectivity2D()
	{
		@Override
		public Collection<Cursor2D> getNeighbors(int x, int y)
		{
			ArrayList<Cursor2D> array = new ArrayList<Cursor2D>(6);
			array.add(new Cursor2D(x,   y-1));
			array.add(new Cursor2D(x+1, y-1));
			array.add(new Cursor2D(x-1, y));
			array.add(new Cursor2D(x+1, y));
			array.add(new Cursor2D(x-1, y+1));
			array.add(new Cursor2D(x,   y+1));
			return array;
		}

		@Override
		public Collection<Cursor2D> getOffsets()
		{
			ArrayList<Cursor2D> array = new ArrayList<Cursor2D>(4);
			array.add(new Cursor2D( 0, -1));
			array.add(new Cursor2D(+1, -1));
			array.add(new Cursor2D(-1,  0));
			array.add(new Cursor2D(+1,  0));
			array.add(new Cursor2D(-1, +1));
			array.add(new Cursor2D( 0, +1));
			return array;
		}

		@Override
		public Connectivity2D getForwardConnectivity()
		{
			return new Connectivity2D()
			{

				@Override
				public Collection<Cursor2D> getNeighbors(int x, int y)
				{
					ArrayList<Cursor2D> array = new ArrayList<Cursor2D>(3);
					array.add(new Cursor2D(x,   y-1));
					array.add(new Cursor2D(x+1, y-1));
					array.add(new Cursor2D(x-1, y));
					return array;
				}

				@Override
				public Collection<Cursor2D> getOffsets()
				{
					ArrayList<Cursor2D> array = new ArrayList<Cursor2D>(3);
					array.add(new Cursor2D( 0, -1));
					array.add(new Cursor2D(+1, -1));
					array.add(new Cursor2D(-1,  0));
					return array;
				}

				@Override
				public Connectivity2D getForwardConnectivity()
				{
					return this;
				}

				@Override
				public Connectivity2D getBackwardConnectivity()
				{
					throw new RuntimeException("Can not return backward connectivity");
				}
				
			};
		}

		@Override
		public Connectivity2D getBackwardConnectivity()
		{
			return new Connectivity2D()
			{

				@Override
				public Collection<Cursor2D> getNeighbors(int x, int y)
				{
					ArrayList<Cursor2D> array = new ArrayList<Cursor2D>(3);
					array.add(new Cursor2D(x+1, y));
					array.add(new Cursor2D(x-1, y+1));
					array.add(new Cursor2D(x,   y+1));
					return array;
				}

				@Override
				public Collection<Cursor2D> getOffsets()
				{
					ArrayList<Cursor2D> array = new ArrayList<Cursor2D>(3);
					array.add(new Cursor2D(+1,  0));
					array.add(new Cursor2D(-1, +1));
					array.add(new Cursor2D( 0, +1));
					return array;
				}

				@Override
				public Connectivity2D getForwardConnectivity()
				{
					throw new RuntimeException("Can not return forward connectivity");
				}

				@Override
				public Connectivity2D getBackwardConnectivity()
				{
					return this;
				}				
			};
		}
		
	};
	/**
	 * Planar connectivity that consider the eight neighbors (orthogonal plus diagonal) of a pixel.
	 */
	public static final Connectivity2D C8 = new Connectivity2D()
	{
		@Override
		public Collection<Cursor2D> getNeighbors(int x, int y)
		{
			ArrayList<Cursor2D> array = new ArrayList<Cursor2D>(8);
			array.add(new Cursor2D(x-1, y-1));
			array.add(new Cursor2D(  x, y-1));
			array.add(new Cursor2D(x+1, y-1));
			array.add(new Cursor2D(x-1,   y));
			array.add(new Cursor2D(x+1,   y));
			array.add(new Cursor2D(x-1, y+1));
			array.add(new Cursor2D(  x, y+1));
			array.add(new Cursor2D(x+1, y+1));
			return array;
		}

		@Override
		public Collection<Cursor2D> getOffsets()
		{
			ArrayList<Cursor2D> array = new ArrayList<Cursor2D>(8);
			array.add(new Cursor2D(-1, -1));
			array.add(new Cursor2D( 0, -1));
			array.add(new Cursor2D(+1, -1));
			array.add(new Cursor2D(-1,  0));
			array.add(new Cursor2D(+1,  0));
			array.add(new Cursor2D(-1, +1));
			array.add(new Cursor2D( 0, +1));
			array.add(new Cursor2D(+1, +1));
			return array;
		}

		@Override
		public Connectivity2D getForwardConnectivity()
		{
			return new Connectivity2D()
			{

				@Override
				public Collection<Cursor2D> getNeighbors(int x, int y)
				{
					ArrayList<Cursor2D> array = new ArrayList<Cursor2D>(4);
					array.add(new Cursor2D(x-1, y-1));
					array.add(new Cursor2D(  x, y-1));
					array.add(new Cursor2D(x+1, y-1));
					array.add(new Cursor2D(x-1,   y));
					return array;
				}

				@Override
				public Collection<Cursor2D> getOffsets()
				{
					ArrayList<Cursor2D> array = new ArrayList<Cursor2D>(4);
					array.add(new Cursor2D(-1, -1));
					array.add(new Cursor2D( 0, -1));
					array.add(new Cursor2D(+1, -1));
					array.add(new Cursor2D(-1,  0));
					return array;
				}

				@Override
				public Connectivity2D getForwardConnectivity()
				{
					return this;
				}

				@Override
				public Connectivity2D getBackwardConnectivity()
				{
					throw new RuntimeException("Can not return backward connectivity");
				}				
			};
		}

		@Override
		public Connectivity2D getBackwardConnectivity()
		{
			return new Connectivity2D()
			{

				@Override
				public Collection<Cursor2D> getNeighbors(int x, int y)
				{
					ArrayList<Cursor2D> array = new ArrayList<Cursor2D>(4);
					array.add(new Cursor2D(x+1,   y));
					array.add(new Cursor2D(x-1, y+1));
					array.add(new Cursor2D(  x, y+1));
					array.add(new Cursor2D(x+1, y+1));
					return array;
				}

				@Override
				public Collection<Cursor2D> getOffsets()
				{
					ArrayList<Cursor2D> array = new ArrayList<Cursor2D>(4);
					array.add(new Cursor2D(+1,  0));
					array.add(new Cursor2D(-1, +1));
					array.add(new Cursor2D( 0, +1));
					array.add(new Cursor2D(+1, +1));
					return array;
				}

				@Override
				public Connectivity2D getForwardConnectivity()
				{
					throw new RuntimeException("Can not return forward connectivity");
				}

				@Override
				public Connectivity2D getBackwardConnectivity()
				{
					return this;
				}				
			};
		}
	};

	
	/**
	 * Returns the set of neighbors associated to a given position
	 * @param x the x position of the pixel
	 * @param y the x position of the pixel
	 * @return the list of niehggbors of specified pixel
	 */
	public Collection<Cursor2D> getNeighbors(int x, int y);

	/**
	 * @return the list of offsets computed relative to the center pixel.
	 */
	public Collection<Cursor2D> getOffsets();

	/**
	 * @return the part of this connectivity that can be used in forward-backward algorithms
	 */
	public Connectivity2D getForwardConnectivity();
	
	/**
	 * @return the part of this connectivity that can be used in forward-backward algorithms
	 */
	public Connectivity2D getBackwardConnectivity();
	
//	/**
//	 * Returns a new connectivity object from a connectivity value.
//	 * @param conn the connectivity value, either 4 or 8
//	 * @return a Connectivity object
//	 */
//	public static final Connectivity2D fromValue(int conn)
//	{
//		if (conn == 4)
//			return C4;
//		else if (conn == 8)
//			return C8;
//		else
//			throw new IllegalArgumentException("Connectivity value should be either 4 or 8");
//	}
}
