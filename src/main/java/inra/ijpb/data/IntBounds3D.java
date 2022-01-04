/**
 * 
 */
package inra.ijpb.data;

/**
 * A bounding Box in 3 dimensions with integer bounds.
 * 
 * @see IntBounds2D
 * 
 * @author dlegland
 */
public class IntBounds3D
{
	// ==================================================
	// Class variables
	
	int xmin;
	int xmax;
	int ymin;
	int ymax;
	int zmin;
	int zmax;
	
	
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
	 * @param zmin
	 *            the minimum z coordinate
	 * @param zmax
	 *            the maximum z coordinate
	 */
	public IntBounds3D(int xmin, int xmax, int ymin, int ymax, int zmin, int zmax)
	{
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
		this.zmin = zmin;
		this.zmax = zmax;
	}
	
	
	// ==================================================
	// New methods
	
	/**
	 * Adds the given amount of voxels along each direction.
	 * The new size in each direction is augmented by 2*border voxels.
	 * 
	 * @param border the number of voxels to add in each direction
	 * @return the new bounds
	 */
	public IntBounds3D addBorder(int border)
	{
		return new IntBounds3D(
				xmin - border, xmax + border, 
				ymin - border, ymax + border, 
				zmin - border, zmax + border);
	}
	
	/**
	 * Crops this bounds such that the result is totally contained within the
	 * specified bounds.
	 * 
	 * @param cropBounds the Bounds object used for cropping.
	 * @return the cropped bounds
	 */
	public IntBounds3D crop(IntBounds3D cropBounds)
	{
		return new IntBounds3D(
				Math.max(xmin, cropBounds.xmin), Math.min(xmax, cropBounds.xmax),   
				Math.max(ymin, cropBounds.ymin), Math.min(ymax, cropBounds.ymax),   
				Math.max(zmin, cropBounds.zmin), Math.min(zmax, cropBounds.zmax));
	}

	/**
	 * Computes the width of the bounding box, corresponding to the extent along
	 * the X dimension. Equals the difference between the two extremities plus
	 * one extra pixel.
	 * 
	 * @return the width of the bounding box, in pixels.
	 */
	public int width()
	{
		return xmax - xmin + 1;
	}
	
	/**
	 * Computes the height of the bounding box, corresponding to the extent
	 * along the Y dimension. Equals the difference between the two extremities
	 * plus one extra pixel.
	 * 
	 * @return the height of the bounding box, in pixels.
	 */
	public int height()
	{
		return ymax - ymin + 1;
	}
	
	/**
	 * Computes the depth of the bounding box, corresponding to the extent
	 * along the Z dimension. Equals the difference between the two extremities
	 * plus one extra pixel.
	 * 
	 * @return the height of the bounding box, in pixels.
	 */
	public int depth()
	{
		return zmax - zmin + 1;
	}
	
	
	// ==================================================
	// Accessors
	
	/**
	 * @return the minimum value along the X-axis.
	 */
	public int getXMin()
	{
		return xmin;
	}
	
	/**
	 * @return the maximum value along the X-axis.
	 */
	public int getXMax()
	{
		return xmax;
	}
	
	/**
	 * @return the minimum value along the Y-axis.
	 */
	public int getYMin()
	{
		return ymin;
	}
	
	/**
	 * @return the maximum value along the Y-axis.
	 */
	public int getYMax()
	{
		return ymax;
	}
	
	/**
	 * @return the minimum value along the Z-axis.
	 */
	public int getZMin()
	{
		return zmin;
	}
	
	/**
	 * @return the maximum value along the Z-axis.
	 */
	public int getZMax()
	{
		return zmax;
	}
	
}
