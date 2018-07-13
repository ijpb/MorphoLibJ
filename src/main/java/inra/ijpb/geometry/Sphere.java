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
	
	public Sphere(Point3D center, double radius)
	{
		this.center = center;
		this.radius = radius;
	}
	
	// ==================================================
	// Generic methods
	
	public double volume()
	{
		return 4 * Math.PI * this.radius * this.radius * this.radius / 3;
	}
	
	public double surfaceArea()
	{
		return 4 * Math.PI * this.radius * this.radius;
	}

	
	// ==================================================
	// Accesors
	
	public Point3D center()
	{
		return center;
	}
	
	public double radius()
	{
		return radius;
	}
}
