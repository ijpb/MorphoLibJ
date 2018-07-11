/**
 * 
 */
package inra.ijpb.geometry;

import java.awt.geom.Point2D;

/**
 * Contains data for representing circle in the 2D plane. Used for the
 * computation of largest inscribed circle.
 * 
 * 
 * @see inra.ijpb.measure.region2d.LargestInscribedCircle
 * 
 * @author dlegland
 *
 */
public class Circle2D
{
	// ==================================================
	// Class variables
	
	/**
	 * The inertia center of the region.
	 */
	private final Point2D center;
	
	/**
	 * The radius of the circle.
	 */
	private final double radius;

	
	// ==================================================
	// Constructors
	
	public Circle2D(Point2D center, double radius)
	{
		this.center = center;
		this.radius = radius;
	}
	
	// ==================================================
	// Generic methods

	public double area()
	{
		return Math.PI * this.radius * this.radius;
	}
	
	public double perimeter()
	{
		return 2 * Math.PI * this.radius;
	}

	
	// ==================================================
	// Accesors
	
	public Point2D getCenter()
	{
		return center;
	}
	
	public double getRadius()
	{
		return radius;
	}
}
