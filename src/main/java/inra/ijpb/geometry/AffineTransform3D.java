/**
 * 
 */
package inra.ijpb.geometry;

import Jama.Matrix;

/**
 * A 3D affine transform, that can be represented by a 4-by-4 matrix.
 * 
 * This implementation directly stores the twelve coefficients that represents
 * the transform.
 * 
 * @author dlegland
 *
 */
public class AffineTransform3D
{
	// ===================================================================
	// static methods

	/**
	 * Creates a translation by the given vector.
	 * 
	 * @param vect
	 *            the vector of the translation transform
	 * @return a new instance of AffineTransform3D representing a translation
	 */
	public static AffineTransform3D createTranslation(Vector3D vect)
	{
		return new AffineTransform3D(1, 0, 0, vect.getX(), 0, 1, 0, vect.getY(), 0, 0, 1, vect.getZ());
	}

	/**
	 * Creates a translation by the given vector.
	 * 
	 * @param dx
	 *            the x-component of the translation transform
     * @param dy
     *            the y-component of the translation transform
     * @param dz
     *            the z-component of the translation transform
	 * @return a new instance of AffineTransform3D representing a translation
	 */
	public static AffineTransform3D createTranslation(double dx, double dy, double dz)
	{
        return new AffineTransform3D(1, 0, 0, dx, 0, 1, 0, dy, 0, 0, 1, dz);
	}

	/**
	 * Creates a scaling by the given coefficients, centered on the origin.
	 * 
	 * @param sx
	 *            the scaling along the x direction
     * @param sy
     *            the scaling along the y direction
     * @param sz
     *            the scaling along the z direction
	 * @return a new instance of AffineTransform3D representing a translation
	 */
	public static AffineTransform3D createScaling(double sx, double sy, double sz)
	{
		return new AffineTransform3D(sx, 0, 0, 0,  0, sy, 0, 0,   0, 0, sz, 0);
	}

	/**
	 * Creates a scaling by the given coefficients, centered on the given point.
	 * 
	 * @param center
	 * 			  the center of the scaling
	 * @param sx
	 *            the scaling along the X direction
     * @param sy
     *            the scaling along the Y direction
     * @param sz
     *            the scaling along the Z direction
	 * @return a new instance of AffineTransform3D representing a centered scaling
	 */
	public static AffineTransform3D createScaling(Point3D center, double sx,
			double sy, double sz)
	{
		return new AffineTransform3D(
				sx, 0, 0, (1 - sx) * center.getX(), 
                0, sy, 0, (1 - sy) * center.getY(),
                0, 0, sz, (1 - sz) * center.getZ());
	}

    /**
     * Creates a rotation around the X axis.
     * 
     * @param theta
     *            the angle of rotation, in radians
     * @return a new instance of AffineTransform3D representing the rotation
     */
    public static AffineTransform3D createRotationOx(double theta)
    {
        double cot = Math.cos(theta);
        double sit = Math.sin(theta);
        return new AffineTransform3D(
                1, 0, 0, 0, 
                0, cot, -sit, 0, 
                0, sit, cot, 0);
    }

    /**
     * Creates a rotation around the X axis.
     * 
     * @param center
     *            the center of the rotation
     * @param theta
     *            the angle of rotation, in radians
     * @return a new instance of AffineTransform3D representing the rotation
     */
    public static AffineTransform3D createRotationOx(Point3D center, double theta)
    {
        double cot = Math.cos(theta);
        double sit = Math.sin(theta);
        double ty =  (1 - cot) * center.getY() + sit * center.getZ();
        double tz =  (1 - cot) * center.getZ() - sit * center.getY();
        return new AffineTransform3D(
                1, 0, 0, 0, 
                0, cot, -sit, ty, 
                0, sit, cot, tz);
    }

    /**
     * Creates a rotation around the Y axis.
     * 
     * @param theta
     *            the angle of rotation, in radians
     * @return a new instance of AffineTransform3D representing the rotation
     */
    public static AffineTransform3D createRotationOy(double theta)
    {
        double cot = Math.cos(theta);
        double sit = Math.sin(theta);
		return new AffineTransform3D(cot, 0, sit, 0, 0, 1, 0, 0, -sit, 0, cot, 0);
    }

    /**
     * Creates a rotation around the Z axis.
     * 
     * @param theta
     *            the angle of rotation, in radians
     * @return a new instance of AffineTransform3D representing the rotation
     */
    public static AffineTransform3D createRotationOz(double theta)
    {
        double cot = Math.cos(theta);
        double sit = Math.sin(theta);
		return new AffineTransform3D(cot, -sit, 0, 0, sit, cot, 0, 0, 0, 0, 1, 0);
    }

    
    // ===================================================================
	// Class members

	// coefficients for x coordinate.
	protected double m00, m01, m02, m03;

	// coefficients for y coordinate.
	protected double m10, m11, m12, m13;

    // coefficients for y coordinate.
    protected double m20, m21, m22, m23;
	
    
	// ===================================================================
	// Constructors

	/**
	 * Empty constructor, that creates an instance of the identity transform.
	 */
	public AffineTransform3D()
	{
		m00 = 1;
		m01 = 0;
        m02 = 0;
        m03 = 0;
        m10 = 0;
        m11 = 1;
        m12 = 0;
        m13 = 0;
        m20 = 0;
        m21 = 0;
        m22 = 1;
        m23 = 0;
	}

	public AffineTransform3D(
            double xx, double yx, double zx, double tx, 
            double xy, double yy, double zy, double ty, 
            double xz, double yz, double zz, double tz)
	{
		m00 = xx;
		m01 = yx;
        m02 = zx;
        m03 = tx;
        m10 = xy;
        m11 = yy;
        m12 = zy;
        m13 = ty;
        m20 = xz;
        m21 = yz;
        m22 = zz;
        m23 = tz;
	}


	// ===================================================================
	// general methods

    /**
     * Returns the affine transform created by applying first the affine
     * transform given by <code>that</code>, then this affine transform. 
     * This is the equivalent method of the 'concatenate' method in
     * java.awt.geom.AffineTransform.
     * 
     * @param that
     *            the transform to apply first
     * @return the composition this * that
     */
    public AffineTransform3D concatenate(AffineTransform3D that)
    {
        double n00 = this.m00 * that.m00 + this.m01 * that.m10 + this.m02 * that.m20;
        double n01 = this.m00 * that.m01 + this.m01 * that.m11 + this.m02 * that.m21;
        double n02 = this.m00 * that.m02 + this.m01 * that.m12 + this.m02 * that.m22;
        double n03 = this.m00 * that.m03 + this.m01 * that.m13 + this.m02 * that.m23 + this.m03;
        double n10 = this.m10 * that.m00 + this.m11 * that.m10 + this.m12 * that.m20;
        double n11 = this.m10 * that.m01 + this.m11 * that.m11 + this.m12 * that.m21;
        double n12 = this.m10 * that.m02 + this.m11 * that.m12 + this.m12 * that.m22;
        double n13 = this.m10 * that.m03 + this.m11 * that.m13 + this.m12 * that.m23 + this.m13;
        double n20 = this.m20 * that.m00 + this.m21 * that.m10 + this.m22 * that.m20;
        double n21 = this.m20 * that.m01 + this.m21 * that.m11 + this.m22 * that.m21;
        double n22 = this.m20 * that.m02 + this.m21 * that.m12 + this.m22 * that.m22;
        double n23 = this.m20 * that.m03 + this.m21 * that.m13 + this.m22 * that.m23 + this.m23;
        return new AffineTransform3D(n00, n01, n02, n03, n10, n11, n12, n13, n20, n21, n22, n23);
    }

    /**
     * Returns the affine transform created by applying first this affine
     * transform, then the affine transform given by <code>that</code>. This the
     * equivalent method of the 'preConcatenate' method in
     * java.awt.geom.AffineTransform. <code><pre>
     * shape = shape.transform(T1.preConcatenate(T2).preConcatenate(T3));
     * </pre></code> is equivalent to the sequence: <code><pre>
     * shape = shape.transform(T1);
     * shape = shape.transform(T2);
     * shape = shape.transform(T3);
     * </pre></code>
     * 
     * @param that
     *            the transform to apply in a second step
     * @return the composition that * this
     */
    public AffineTransform3D preConcatenate(AffineTransform3D that) 
    {
        double n00 = this.m00 * that.m00 + this.m10 * that.m01 + this.m20 * that.m02;
        double n01 = this.m01 * that.m00 + this.m11 * that.m01 + this.m21 * that.m02;
        double n02 = this.m02 * that.m00 + this.m12 * that.m01 + this.m22 * that.m02;
        double n03 = this.m03 * that.m00 + this.m13 * that.m01 + this.m23 * that.m02 + that.m03;
        double n10 = this.m00 * that.m10 + this.m10 * that.m11 + this.m20 * that.m12;
        double n11 = this.m01 * that.m10 + this.m11 * that.m11 + this.m21 * that.m12;
        double n12 = this.m02 * that.m10 + this.m12 * that.m11 + this.m22 * that.m12;
        double n13 = this.m03 * that.m10 + this.m13 * that.m11 + this.m23 * that.m12 + that.m13;
        double n20 = this.m00 * that.m20 + this.m10 * that.m21 + this.m20 * that.m22;
        double n21 = this.m01 * that.m20 + this.m11 * that.m21 + this.m21 * that.m22;
        double n22 = this.m02 * that.m20 + this.m12 * that.m21 + this.m22 * that.m22;
        double n23 = this.m03 * that.m20 + this.m13 * that.m21 + this.m23 * that.m22 + that.m23;
        return new AffineTransform3D(n00, n01, n02, n03, n10, n11, n12, n13, n20, n21, n22, n23);
    }


	public Point3D transform(Point3D p)
	{
		double x = p.getX();
        double y = p.getY();
        double z = p.getZ();
		return new Point3D(
                x * m00 + y * m01 + z * m02 + m03, 
                x * m10 + y * m11 + z * m12 + m13, 
                x * m20 + y * m21 + z * m22 + m23);
	}


	/**
	 * Transforms a vector, by using only the linear part of this transform.
	 * 
	 * @param v
	 *            the vector to transform
	 * @return the transformed vector
	 */
	public Vector3D transform(Vector3D v)
	{
		double vx = v.getX();
		double vy = v.getY();
		double vz = v.getZ();
		return new Vector3D(
				vx * m00 + vy * m01 + vz * m02, 
				vx * m10 + vy * m11 + vz * m12, 
				vx * m20 + vy * m21 + vz * m22);
	}

	/**
	 * Returns the inverse transform. If the transform is not invertible, throws
	 * a new RuntimeException.
	 * 
	 * @return the inverse of this transform.
	 * @throws a RuntimeException if the transform is not invertible
	 */
	public AffineTransform3D inverse()
	{
        double det = this.determinant();

        // check invertibility
        if (Math.abs(det) < 1e-12)
            throw new RuntimeException("Non-invertible matrix");
        
        return new AffineTransform3D(
                (m11 * m22 - m21 * m12) / det,
                (m21 * m02 - m01 * m22) / det,
                (m01 * m12 - m11 * m02) / det,
                (m01 * (m22 * m13 - m12 * m23) + m02 * (m11 * m23 - m21 * m13) 
                        - m03 * (m11 * m22 - m21 * m12)) / det, 
                (m20 * m12 - m10 * m22) / det, 
                (m00 * m22 - m20 * m02) / det, 
                (m10 * m02 - m00 * m12) / det, 
                (m00 * (m12 * m23 - m22 * m13) - m02 * (m10 * m23 - m20 * m13) 
                        + m03 * (m10 * m22 - m20 * m12)) / det, 
                (m10 * m21 - m20 * m11) / det, 
                (m20 * m01 - m00 * m21) / det,
                (m00 * m11 - m10 * m01) / det, 
                (m00 * (m21 * m13 - m11 * m23) + m01 * (m10 * m23 - m20 * m13) 
                        - m03 * (m10 * m21 - m20 * m11))    / det);
	}


    /**
     * Computes the determinant of this affine transform. Can be zero.
     * 
     * @return the determinant of the transform.
     */
    private double determinant()
    {
        return m00 * (m11 * m22 - m12 * m21) - m01 * (m10 * m22 - m20 * m12)
                + m02 * (m10 * m21 - m20 * m11);
    }

	/**
	 * @return the affine matrix of the coefficients corresponding to this transform 
	 */
    public Matrix getMatrix()
    {
    	Matrix mat = new Matrix(4, 4);
    	mat.set(0, 0, this.m00);
    	mat.set(0, 1, this.m01);
    	mat.set(0, 2, this.m02);
    	mat.set(0, 3, this.m03);
    	mat.set(1, 0, this.m10);
    	mat.set(1, 1, this.m11);
    	mat.set(1, 2, this.m12);
    	mat.set(1, 3, this.m13);
    	mat.set(2, 0, this.m20);
    	mat.set(2, 1, this.m21);
    	mat.set(2, 2, this.m22);
    	mat.set(2, 3, this.m23);
    	mat.set(3, 0, 0.0);
    	mat.set(3, 1, 0.0);
    	mat.set(3, 2, 0.0);
    	mat.set(3, 3, 1.0);
    	return mat;
    }
    
    public boolean almostEquals(AffineTransform3D that, double tol)
    {
    	if (Math.abs(this.m00 - that.m00) > tol) return false;
    	if (Math.abs(this.m01 - that.m01) > tol) return false;
    	if (Math.abs(this.m02 - that.m02) > tol) return false;
    	if (Math.abs(this.m03 - that.m03) > tol) return false;
    	if (Math.abs(this.m10 - that.m10) > tol) return false;
    	if (Math.abs(this.m11 - that.m11) > tol) return false;
    	if (Math.abs(this.m12 - that.m12) > tol) return false;
    	if (Math.abs(this.m13 - that.m13) > tol) return false;
    	if (Math.abs(this.m20 - that.m20) > tol) return false;
    	if (Math.abs(this.m21 - that.m21) > tol) return false;
    	if (Math.abs(this.m22 - that.m22) > tol) return false;
    	if (Math.abs(this.m23 - that.m23) > tol) return false;
    	return true;
    }
}
