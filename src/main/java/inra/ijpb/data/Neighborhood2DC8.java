package inra.ijpb.data;

import java.util.ArrayList;

public class Neighborhood2DC8 extends Neighborhood2D {

	ArrayList<Cursor2D> neighbors = new ArrayList<Cursor2D>();
	
	@Override
	public Iterable<Cursor2D> getNeighbors() 
	{
		neighbors.clear();
		
		final int x = super.cursor.getX();
		final int y = super.cursor.getY();
		
		neighbors.add( new Cursor2D( x-1, y-1 ) );
		neighbors.add( new Cursor2D( x-1, y   ) );
		neighbors.add( new Cursor2D( x-1, y+1 ) );
		neighbors.add( new Cursor2D(   x, y-1 ) );
		neighbors.add( new Cursor2D(   x, y+1 ) );
		neighbors.add( new Cursor2D( x+1, y-1 ) );
		neighbors.add( new Cursor2D( x+1, y   ) );
		neighbors.add( new Cursor2D( x+1, y+1 ) );
		
		return neighbors;
	}

}
