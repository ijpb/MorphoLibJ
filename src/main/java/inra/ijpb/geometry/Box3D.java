/**
 * 
 */
package inra.ijpb.geometry;

/**
 * A bounding Box in 3 dimensions.
 * 
 * 
 * @see Box2D
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
	
	/**
     * Default constructor, that specifies the bounds along each dimension.
     * 
     * @param xmin
     *            the minimal bound along the x direction
     * @param xmax
     *            the maximal bound along the x direction
     * @param ymin
     *            the minimal bound along the y direction
     * @param ymax
     *            the maximal bound along the y direction
     * @param zmin
     *            the minimal bound along the z direction
     * @param zmax
     *            the maximal bound along the z direction
     */
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

    /**
     * Computes the volume of this bounding box.
     * 
     * @return the volume enclosed by the bounding box.
     */
	public double volume()
	{
		return (xmax - xmin) * (ymax - ymin) * (zmax - zmin);
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
	
    /**
     * Computes the depth of the bounding box, corresponding to the extent
     * along the Z dimension.
     * 
     * @return the depth of the bounding box
     */
	public double depth()
	{
		return  zmax - zmin;
	}
	
	
	// ==================================================
	// accessors
	
	/**
	 * @return the minimum value along the X-axis.
	 */
	public double getXMin()
	{
		return xmin;
	}
	
	/**
	 * @return the maximum value along the X-axis.
	 */
	public double getXMax()
	{
		return xmax;
	}
	
	/**
	 * @return the minimum value along the Y-axis.
	 */
	public double getYMin()
	{
		return ymin;
	}
	
	/**
	 * @return the maximum value along the Y-axis.
	 */
	public double getYMax()
	{
		return ymax;
	}
	
	/**
	 * @return the minimum value along the Z-axis.
	 */
	public double getZMin()
	{
		return zmin;
	}
	
	/**
	 * @return the maximum value along the Z-axis.
	 */
	public double getZMax()
	{
		return zmax;
	}
	
}
