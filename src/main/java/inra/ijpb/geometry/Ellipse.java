/**
 * 
 */
package inra.ijpb.geometry;

import java.awt.geom.Point2D;

/**
 * Represents a 2D ellipse.
 * 
 * @author dlegland
 *
 */
public class Ellipse
{
	// ==================================================
	// Class variables
	
	/**
	 * The inertia center of the region.
	 */
	private final Point2D center;
	
	/**
	 * The length of the semi-major axis
	 */
	private final double radius1;

	/**
	 * The length of the semi-minor axis
	 */
	private final double radius2;

	/**
	 * The orientation of the main axis, in degrees
	 */
	private final double orientation;

	
	// ==================================================
	// Constructors
	
	public Ellipse(Point2D center, double radius1, double radius2, double theta)
	{
		this.center = center;
		this.radius1 = radius1;
		this.radius2 = radius2;
		this.orientation = theta;
	}
	
	public Ellipse(double xc, double yc, double radius1, double radius2, double theta)
	{
		this.center = new Point2D.Double(xc, yc);
		this.radius1 = radius1;
		this.radius2 = radius2;
		this.orientation = theta;
	}
	
	// ==================================================
	// Generic methods

	public double area()
	{
		return Math.PI * this.radius1 * this.radius2;
	}
	
	
	// ==================================================
	// Accesors
	
	public Point2D center()
	{
		return center;
	}
	
	public double radius1()
	{
		return radius1;
	}

	public double radius2()
	{
		return radius2;
	}

	public double orientation()
	{
		return orientation;
	}

	
	// ==================================================
	// Override some of Object methods
	
	@Override
	public String toString()
	{
		return String.format("Ellipse(%f,%f,%f,%f,%f)", center.getX(), center.getY(), radius1, radius2, orientation);
	}
}
