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
 * Represents a couple of coordinates in linear space, and provides some
 * computing methods.
 * 
 * The class is immutable. 
 * 
 * @author dlegland
 *
 */
public class Vector2D 
{

	// ===================================================================
	// Class variables
	
	private final double x; 
	private final double y; 
	
	
	// ===================================================================
	// Constructors

	/**
	 * Empty constructor, with all coordinates initialized to zero. 
	 */
	public Vector2D()
	{
		this.x = 0;
		this.y = 0;
	}

	/**
	 * Initialization constructor.
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */
	public Vector2D(double x, double y)
	{
		this.x = x;
		this.y = y;
	}
	
	
	// ===================================================================
	// Static methods
	
	/**
	 * Computes the cross product of the two vectors. Cross product is zero for
	 * colinear vectors. 
	 * 
	 * @param v1 the first vector
	 * @param v2 the second vector
	 * @return the cross product of the two vectors
	 */
	public static final double crossProduct(Vector2D v1, Vector2D v2)
	{
		return v1.x * v2.y - v1.y * v2.x;
	}

	/**
	 * Computes the dot product of two vectors, defined by:
	 * <p>
	 * <code> x1 * x2 + y1 * y2</code>
	 * <p>
	 * Dot product is zero if the vectors are orthogonal. 
	 * It is positive if vectors are in the same direction, and
	 * negative if they are in opposite direction.
	 * 
	 * @param v1 the first vector
	 * @param v2 the second vector
	 * @return the dot product of the two vectors
	 */
	public static final double dotProduct(Vector2D v1, Vector2D v2)
	{
		return v1.x * v2.x + v1.y * v2.y;
	}
	
	/**
	 * Computes the angle between two vectors. Result is given in radians.
	 *
	 * @param v1 the first vector
	 * @param v2 the second vector
	 * @return the angle between the two vectors, in radians
	 */
	public static final double angle(Vector2D v1, Vector2D v2)
	{
		// compute angle using arc-tangent to get better precision for angles
		// near zero, see the discussion in: 
		// http://www.mathworks.com/matlabcentral/newsreader/view_thread/151925#381952
		return Math.atan2(v1.x * v2.y - v2.x * v1.y, v1.x * v2.x + v1.y * v2.y);
	}
	
	
	// ===================================================================
	// Accessor methods
	
	/**
	 * Returns the x coordinate of the vector.
	 * 
	 * @return the x coordinate of the vector
	 */
	public double getX()
	{
		return this.x;
	}
	
	/**
	 * Returns the y coordinate of the vector.
	 * 
	 * @return the y coordinate of the vector
	 */
	public double getY()
	{
		return this.y;
	}
	
	
	// ===================================================================
	// Computation methods
	
	/**
	 * Returns the result of the addition of this vector with another vector.
	 *  
	 * @param v the vector to add
	 * @return the results of the vector addition 
	 */
	public Vector2D plus(Vector2D v)
	{
		double x = this.x + v.x;
		double y = this.y + v.y;
		return new Vector2D(x, y);
	}
	
	/**
	 * Returns the result of the subtraction of this vector with another vector. 
	 *  
	 * @param v the vector to subtract
	 * @return the results of the vector subtraction 
	 */
	public Vector2D minus(Vector2D v)
	{
		double x = this.x - v.x;
		double y = this.y - v.y;
		return new Vector2D(x, y);
	}
	
	/**
	 * Returns the result of the multiplication of this vector with a scalar value.  
	 * 
	 * @param k the scalar coefficient
	 * @return the results of scalar multiplication 

	 */
	public Vector2D times(double k)
	{
		double x = this.x * k;
		double y = this.y * k;
		return new Vector2D(x, y);
	}
	
	/**
	 * Returns a normalized vector with same direction as this vector
	 * 
	 * @return the normalized vector with same direction as this.
	 */
	public Vector2D normalize()
	{
		double norm = this.getNorm();
		return new Vector2D(this.x / norm, this.y / norm);
	}

	/**
	 * Computes the norm of the vector, given as the square root of the sum
	 * of squared coordinates.
	 * 
	 * @return the norm of the vector
	 */
	public double getNorm()
	{
		return Math.hypot(this.x, this.y);
	}
	
	/**
	 * Checks if this vector is close to the given vector, by checking each
	 * coordinate using the given threshold.
	 * 
	 * @param v
	 *            the vector to compare to
	 * @param eps
	 *            the absolute tolerance for comparing coordinates
	 * @return true if vector have same coordinates with respect to tolerance
	 */
	public boolean almostEquals(Vector2D v, double eps)
	{
		if (Math.abs(this.x - v.x) > eps) return false;
		if (Math.abs(this.y - v.y) > eps) return false;
		return true;
	}
	
}
