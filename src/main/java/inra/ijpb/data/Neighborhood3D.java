package inra.ijpb.data;

public abstract class Neighborhood3D {
	
	Cursor3D cursor;
	
	public abstract Iterable<Cursor3D> getNeighbors(  );
	
	public void setCursor( Cursor3D cursor )
	{
		this.cursor = cursor;
	}
	
	public Cursor3D getCursor()
	{
		return this.cursor;
	}
	
}

