/**
 * 
 */
package inra.ijpb.geometry;

/**
 * Contains data for representing sphere in the 3D space. Used for the
 * computation of largest inscribed ball.
 * 
 * 
 * @see inra.ijpb.measure.region3d.LargestInscribedBall
 * 
 * @author dlegland
 *
 */
public class Sphere
{
	// ==================================================
	// Class variables
	
	/**
	 * The center of the sphere.
	 */
	private final Point3D center;
	
	/**
	 * The radius of the sphere.
	 */
	private final double radius;

	
	// ==================================================
	// Constructors
	
	/**
	 * Creates a new sphere from a center and a radius.
	 * 
	 * @param center
	 *            the center of the sphere
	 * @param radius
	 *            the radius of the sphere
	 */
	public Sphere(Point3D center, double radius)
	{
		this.center = center;
		this.radius = radius;
	}
	
	// ==================================================
	// Generic methods
	
	/**
	 * @return the volume of the domain enclosed by the sphere (equal to (4*pi/3) * r^3 )
	 */
	public double volume()
	{
		return 4 * Math.PI * this.radius * this.radius * this.radius / 3;
	}
	
	/**
	 * @return the surface area of the sphere (equal to 4*pi*r)
	 */
	public double surfaceArea()
	{
		return 4 * Math.PI * this.radius * this.radius;
	}

	
	// ==================================================
	// Accesors
	
	/**
	 * @return the center of the sphere
	 */
	public Point3D center()
	{
		return center;
	}
	
	/**
	 * @return the radius of the sphere
	 */
	public double radius()
	{
		return radius;
	}
}
