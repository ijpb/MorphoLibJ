/**
 * 
 */
package inra.ijpb.geometry;


/**
 * A pair of points in the 3D space, useful for representing result of Max Feret
 * Diameter computation or similar problems.
 * 
 * Simply contains the reference to each extremity.
 *
 * @see PointPair2D
 * 
 * @author dlegland
 *
 */
public class PointPair3D
{	
	/**
	 * The first point of the pair.
	 */
	public final Point3D p1;
	
	/**
	 * The second point of the pair.
	 */
	public final Point3D p2;

	/**
	 * Creates a new 3D point pair.
	 * 
	 * @param p1
	 *            the first point of the pair.
	 * @param p2
	 *            the second point of the pair.
	 */
	public PointPair3D(Point3D p1, Point3D p2)
	{
		this.p1 = p1;
		this.p2 = p2;
	}

	/**
	 * @return the diameter of the pair, as the distance between the two points.
	 */
	public double diameter()
	{
		double dx = p2.getX() - p1.getX();
		double dy = p2.getY() - p1.getY();
		double dz = p2.getZ() - p1.getZ();
		return Math.hypot(Math.hypot(dx,  dy), dz);
	}
}
