/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
/**
 * 
 */
package inra.ijpb.data;

/**
 * Stores bounds of regions within 2D images.
 * 
 * @see IntBounds3D
 * 
 * @author dlegland
 */
public class IntBounds2D
{
	// ==================================================
	// Class variables
	
	private final int xmin;
	private final int xmax;
	private final int ymin;
	private final int ymax;
	
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
     * Computes the intersection between the two bounds, such that the result is
     * totally contained within this bounds and the other bounds. Note that this
     * operation may result in an undefined bounds if the two bounds do not
     * intersect).
     * 
     * @param bounds
     *            the Bounds object used for cropping.
     * @return the cropped bounds
     */
	public IntBounds2D intersection(IntBounds2D bounds)
	{
		return new IntBounds2D(
				Math.max(xmin, bounds.xmin), Math.min(xmax, bounds.xmax),   
				Math.max(ymin, bounds.ymin), Math.min(ymax, bounds.ymax));
	}

    /**
     * Computes the union of the two bounds, by retaining the max extent along
     * each dimension. Each of the input bounds is contained within the result.
     * 
     * @param bounds
     *            the Bounds object used for computing union.
     * @return the cropped bounds
     */
    public IntBounds2D union(IntBounds2D bounds)
    {
        return new IntBounds2D(
                Math.min(xmin, bounds.xmin), Math.max(xmax, bounds.xmax),   
                Math.min(ymin, bounds.ymin), Math.max(ymax, bounds.ymax));
    }
    
	/**
	 * Computes the width of the bounding box, corresponding to the extent along
	 * the X dimension. Equals the difference between the two extremities plus
	 * one extra pixel.
	 * 
	 * @return the width of the bounding box, in pixels.
	 */
	public int getWidth()
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
	public int getHeight()
	{
		return ymax - ymin + 1;
	}
	
    /**
     * Checks if this Bounds is valid, by checking that for each dimension, the
     * minimum coordinate is lower than (or equal to) the maximum coordinate.
     * 
     * @return true if the Bounds object is valid
     */
    public boolean isValid()
    {
        if (xmin > xmax) return false;
        if (ymin > ymax) return false;
        return true;
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
