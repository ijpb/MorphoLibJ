package inra.ijpb.data;

public class Cursor2D {
	private int x = 0;
	private int y = 0;	
		
	public Cursor2D(
			int x,
			int y )
	{
		this.x = x;
		this.y = y;
	}
	
	public void set( 
			int x, 
			int y )
	{
		this.x = x;
		this.y = y;
	}
	
	public int getX()
	{
		return x;
	}
	
	public int getY()
	{
		return y;
	}

	@Override
	public boolean equals( Object other )
	{
	    if (other == null) return false;
	    if (other == this) return true;
	    if ( !( other instanceof Cursor2D ) )
	    	return false;
	    Cursor2D c = (Cursor2D) other;
	    return c.x == this.x && c.y == this.y;
	}
}
