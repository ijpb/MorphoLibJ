package inra.ijpb.data;

public class Cursor3D 
{
	private int x = 0;
	private int y = 0;
	private int z = 0;
	
	public Cursor3D(
			int x,
			int y,
			int z )
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void set(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public int getX()
	{
		return x;
	}
	
	public int getY()
	{
		return y;
	}
	
	public int getZ()
	{
		return z;
	}
	

}
