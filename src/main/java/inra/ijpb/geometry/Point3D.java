/**
 * 
 */
package inra.ijpb.geometry;

/**
 * A point in the 3-dimensional Euclidean space, defined by x, y, and z coordinates.
 * 
 * @author dlegland
 *
 */
public class Point3D
{
	private double x;

	private double y;

	private double z;
	
	public Point3D()
	{
		this(0, 0, 0);
	}
	
	public Point3D(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public double distance(Point3D point)
	{
		double dx = (point.x - x);
		double dy = (point.y - y);
		double dz = (point.z - z);
		return Math.hypot(Math.hypot(dx, dy), dz);
	}
	
	public double getX()
	{
		return x;
	}

	public double getY()
	{
		return y;
	}

	public double getZ()
	{
		return z;
	}
}
