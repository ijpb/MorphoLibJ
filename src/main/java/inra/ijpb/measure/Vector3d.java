/**
 * 
 */
package inra.ijpb.measure;

/**
 * Represents a triplet of coordinates in linear space, and provides some
 * computing methods.
 * 
 * The class is immutable. 
 * 
 * @author dlegland
 *
 */
public class Vector3d 
{

	// ===================================================================
	// Class variables
	
	private final double x; 
	private final double y; 
	private final double z;
	
	
	// ===================================================================
	// Constructors

	/**
	 * Empty constructor, with all coordinates initialized to zero. 
	 */
	public Vector3d()
	{
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}

	/**
	 * Initialization constructor.
	 */
	public Vector3d(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	
	// ===================================================================
	// Static methods
	
	/**
	 * Computes the cross product of the two vectors. Cross product is zero for
	 * colinear vectors. 
	 */
	public static final Vector3d crossProduct(Vector3d v1, Vector3d v2)
	{
		return new Vector3d(
				v1.y * v2.z - v1.z * v2.y, 
				v1.z * v2.x - v1.x * v2.z, 
				v1.x * v2.y - v1.y * v2.x);
	}

	/**
	 * Computes the dot product of two vectors, defined by:
	 * <p>
	 * <code> x1 * x2 + y1 * y2 + z1 * z2</code>
	 * <p>
	 * Dot product is zero if the vectors are orthogonal. 
	 * It is positive if vectors are in the same direction, and
	 * negative if they are in opposite direction.
	 */
	public static final double dotProduct(Vector3d v1, Vector3d v2)
	{
		return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
	}
	
	/**
	 * Computes the angle between two 3D vectors. The result is given between 0
	 * and PI.  
	 */
	public static final double angle(Vector3d v1, Vector3d v2)
	{
		// compute angle using arc-tangent to get better precision for angles
		// near zero, see the discussion in: 
		// http://www.mathworks.com/matlabcentral/newsreader/view_thread/151925#381952
		double norm = Vector3d.crossProduct(v1, v2).getNorm();
		double det = Vector3d.dotProduct(v1, v2);
		return Math.atan2(norm, det);
	}
	
	
	// ===================================================================
	// Accessor methods
	
	public double getX()
	{
		return this.x;
	}
	
	public double getY()
	{
		return this.y;
	}
	
	public double getZ()
	{
		return this.z;
	}
	
	// ===================================================================
	// Computation methods
	
	/**
	 * Returns the result of the addition of this vector with another vector. 
	 */
	public Vector3d plus(Vector3d v)
	{
		double x = this.x + v.x;
		double y = this.y + v.y;
		double z = this.z + v.z;
		return new Vector3d(x, y, z);
	}
	
	/**
	 * Returns the result of the subtraction of this vector with another vector. 
	 */
	public Vector3d minus(Vector3d v)
	{
		double x = this.x - v.x;
		double y = this.y - v.y;
		double z = this.z - v.z;
		return new Vector3d(x, y, z);
	}
	
	/**
	 * Returns the result of the multiplication of this vector with a scalar value. 
	 */
	public Vector3d times(double k)
	{
		double x = this.x * k;
		double y = this.y * k;
		double z = this.z * k;
		return new Vector3d(x, y, z);
	}
	
	/**
	 * Returns a normalized vector with same direction as this vector
	 */
	public Vector3d normalize()
	{
		double norm = this.getNorm();
		return new Vector3d(this.x / norm, this.y / norm, this.z / norm);
	}

	/**
	 * Computes the norm of the vector, given as the square root of the sum
	 * of squared coordinates.
	 */
	public double getNorm()
	{
		double norm = Math.hypot(this.x, this.y);
		norm = Math.hypot(norm, this.z);
		return norm;
	}
}
