package inra.ijpb.data;

public abstract class Neighborhood2D {
	
	Cursor2D cursor;
	
	public abstract Iterable<Cursor2D> getNeighbors(  );
	
	public void setCursor( Cursor2D cursor )
	{
		this.cursor = cursor;
	}
	
	public Cursor2D getCursor()
	{
		return this.cursor;
	}
	
}
