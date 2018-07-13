/**
 * 
 */
package inra.ijpb.geometry;

/**
 * A bounding Boc in 2 dimensions
 * 
 * @author dlegland
 *
 */
public class Box2D
{
	// ==================================================
	// Class variables
	
	double xmin;
	double ymin;
	double xmax;
	double ymax;
	
	// ==================================================
	// Constructors
	
	public Box2D(double xmin, double xmax, double ymin, double ymax)
	{
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
	}
	
	// ==================================================
	// generic methods

	public double area()
	{
		return (xmax - xmin) * (ymax - ymin);
	}
	
	public double width()
	{
		return xmax - xmin;
	}
	
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
