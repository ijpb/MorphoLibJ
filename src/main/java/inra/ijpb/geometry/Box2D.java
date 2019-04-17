/**
 * 
 */
package inra.ijpb.geometry;

/**
 * A bounding Box in 2 dimensions.
 *
 * @see Box3D
 * 
 * @author dlegland
 *
 */
public class Box2D
{
	// ==================================================
	// Class variables
	
	double xmin;
	double xmax;
	double ymin;
	double ymax;
	
	// ==================================================
	// Constructors
	
	/**
	 * Default constructor of the bounding box, that specifies the bounds along
	 * each dimension.
	 * 
	 * @param xmin
	 *            the minimum x coordinate
	 * @param xmax
	 *            the maximum x coordinate
	 * @param ymin
	 *            the minimum y coordinate
	 * @param ymax
	 *            the maximum y coordinate
	 */
	public Box2D(double xmin, double xmax, double ymin, double ymax)
	{
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
	}
	
	// ==================================================
	// generic methods

	/**
	 * Computes the area of this bounding box.
	 * 
	 * @return the area enclosed by the bounding box.
	 */
	public double area()
	{
		return (xmax - xmin) * (ymax - ymin);
	}

	/**
	 * Computes the width of the bounding box, corresponding to the extent along
	 * the X dimension.
	 * 
	 * @return the width of the bounding box
	 */
	public double width()
	{
		return xmax - xmin;
	}
	
	/**
	 * Computes the height of the bounding box, corresponding to the extent
	 * along the Y dimension.
	 * 
	 * @return the height of the bounding box
	 */
	public double height()
	{
		return  ymax - ymin;
	}
	
	
	// ==================================================
	// accessors
	
	public double getXMin()
	{
		return xmin;
	}
	
	public double getXMax()
	{
		return xmax;
	}
	
	public double getYMin()
	{
		return ymin;
	}
	
	public double getYMax()
	{
		return ymax;
	}
	
}
