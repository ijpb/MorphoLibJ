/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2023 INRA.
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
