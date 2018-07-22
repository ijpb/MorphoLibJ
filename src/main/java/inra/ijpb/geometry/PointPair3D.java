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
 * @author dlegland
 *
 */
public class PointPair3D
{	
	public final Point3D p1;
	public final Point3D p2;

	public PointPair3D(Point3D p1, Point3D p2)
	{
		this.p1 = p1;
		this.p2 = p2;
	}

	public double diameter()
	{
		double dx = p2.getX() - p1.getX();
		double dy = p2.getY() - p1.getY();
		double dz = p2.getZ() - p1.getZ();
		return Math.hypot(Math.hypot(dx,  dy), dz);
	}
}
