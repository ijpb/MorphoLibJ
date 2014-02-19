package inra.ijpb.data;

import java.util.ArrayList;

public class Neighborhood3DC6 extends Neighborhood3D {
	
	ArrayList<Cursor3D> neighbors = new ArrayList<Cursor3D>();

	@Override
	public Iterable<Cursor3D> getNeighbors() 
	{
		neighbors.clear();
		
		final int x = super.cursor.getX();
		final int y = super.cursor.getY();
		final int z = super.cursor.getZ();
		
		
		neighbors.add( new Cursor3D( x  , y  , z-1 ) );
		
		neighbors.add( new Cursor3D( x-1, y  , z   ) );		
		neighbors.add( new Cursor3D(   x, y-1, z   ) );
		neighbors.add( new Cursor3D(   x, y+1, z   ) );		
		neighbors.add( new Cursor3D( x+1, y  , z   ) );
		
		neighbors.add( new Cursor3D( x  , y  , z+1 ) );
		
		return neighbors;
		
	}

}
