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
	
}
