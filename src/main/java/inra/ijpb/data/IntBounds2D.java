/**
 * 
 */
package inra.ijpb.data;

/**
 * A bounding Box in 2 dimensions with integer bounds.
 * 
 * @see IntBounds3D
 * 
 * @author dlegland
 */
public class IntBounds2D
{
	// ==================================================
	// Class variables
	
	int xmin;
	int xmax;
	int ymin;
	int ymax;
	
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
	public IntBounds2D(int xmin, int xmax, int ymin, int ymax)
	{
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
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
	public IntBounds2D addBorder(int border)
	{
		return new IntBounds2D(
				xmin - border, xmax + border, 
				ymin - border, ymax + border);
	}
	
	/**
	 * Crops this bounds such that the result is totally contained within the
	 * specified bounds.
	 * 
	 * @param cropBounds the Bounds object used for cropping.
	 * @return the cropped bounds
	 */
	public IntBounds2D crop(IntBounds2D cropBounds)
	{
		return new IntBounds2D(
				Math.max(xmin, cropBounds.xmin), Math.min(xmax, cropBounds.xmax),   
				Math.max(ymin, cropBounds.ymin), Math.min(ymax, cropBounds.ymax));
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
	
}
