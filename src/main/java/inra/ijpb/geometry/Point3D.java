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
	
	/**
	 * Creates a new point located at the origin.
	 */
	public Point3D()
	{
		this(0, 0, 0);
	}
	
	/**
	 * Creates a new point at the specified coordinates.
	 * 
	 * @param x
	 *            the X-coordinate of the point
	 * @param y
	 *            the Y-coordinate of the point
	 * @param z
	 *            the Z-coordinate of the point
	 */
	public Point3D(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * Computes the euclidean distance to another point.
	 * 
	 * @param point
	 *            the other point
	 * @return the distance between this point and the query point
	 */
	public double distance(Point3D point)
	{
		double dx = (point.x - x);
		double dy = (point.y - y);
		double dz = (point.z - z);
		return Math.hypot(Math.hypot(dx, dy), dz);
	}
	
	/**
	 * @return the X-coordinate of the point
	 */
	public double getX()
	{
		return x;
	}

	/**
	 * @return the Y-coordinate of the point
	 */
	public double getY()
	{
		return y;
	}

	/**
	 * @return the Z-coordinate of the point
	 */
	public double getZ()
	{
		return z;
	}
}
