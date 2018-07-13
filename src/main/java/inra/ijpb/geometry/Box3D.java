/**
 * 
 */
package inra.ijpb.geometry;

/**
 * A bounding Box in 3 dimensions
 * 
 * @author dlegland
 *
 */
public class Box3D
{
	// ==================================================
	// Class variables
	
	double xmin;
	double xmax;
	double ymin;
	double ymax;
	double zmin;
	double zmax;
	
	// ==================================================
	// Constructors
	
	public Box3D(double xmin, double xmax, double ymin, double ymax, double zmin, double zmax)
	{
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
		this.zmin = zmin;
		this.zmax = zmax;
	}
	
	// ==================================================
	// generic methods

	public double volume()
	{
		return (xmax - xmin) * (ymax - ymin) * (zmax - zmin);
	}
	
	public double width()
	{
		return xmax - xmin;
	}
	
	public double height()
	{
		return  ymax - ymin;
	}
	
	public double depth()
	{
		return  zmax - zmin;
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
	
	public double getZMin()
	{
		return zmin;
	}
	
	public double getZMax()
	{
		return zmax;
	}
	
}
