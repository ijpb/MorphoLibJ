/**
 * 
 */
package inra.ijpb.geometry;

import static java.lang.Math.atan2;
import static java.lang.Math.hypot;

import java.util.ArrayList;

import Jama.Matrix;

/**
 * Three angles that represent an orientation in 3D. Can be converted to a
 * rotation matrix.
 *
 * @see Ellipsoid
 * 
 * @author dlegland
 *
 */
public class EulerAnglesXYZ
{
	// ==================================================
	// Static methods
	
	public static final EulerAnglesXYZ fromMatrix(Matrix matrix)
	{
        // extract |cos(theta)| 
        double tmp = hypot(matrix.get(0, 0), matrix.get(1, 0));
        double phi, theta, psi;

        // avoid dividing by 0
        if (tmp > 16 * Double.MIN_VALUE) 
        {
            // normal case: theta <> 0
            psi     = atan2( matrix.get(2, 1), matrix.get(2, 2));
            theta   = atan2(-matrix.get(2, 0), tmp);
            phi     = atan2( matrix.get(1, 0), matrix.get(0, 0));
        }
        else 
        {
            // theta is around 0 
            psi     = atan2(-matrix.get(1, 2), matrix.get(1,1));
            theta   = atan2(-matrix.get(2, 0), tmp);
            phi     = 0;
        }
        
        return new EulerAnglesXYZ(Math.toDegrees(phi), Math.toDegrees(theta), Math.toDegrees(psi));
	}

	
	// ==================================================
	// Class variables
	
	/**
	 * The azimut of the main axis, in degrees, corresponding to a global rotation around the Z axis. 
	 */
	double phi;

	/**
	 * The elevation of the main axis, in degrees, corresponding to a global rotation around the Y axis.
	 */
	double theta;
	
	/**
	 * The roll around the main axis, in degrees, corresponding to a global rotation around the X axis.
	 */
	double psi;
	
	
	// ==================================================
	// Constructors
	
	/**
	 * Empty constructor, all angles are initialized to zero.
	 */
	public EulerAnglesXYZ()
	{
	}
	
	public EulerAnglesXYZ(double phi, double theta, double psi)
	{
		this.phi = phi;
		this.theta = theta;
		this.psi = psi;
	}


	// ==================================================
	// Conversion methods
	
	/**
	 * Returns the affine transform corresponding to this EulerAnglesXYZ.
	 * 
	 * @see #rotationMatrix()
	 * @return the affine transform equivalent to this EulerAnglesXYZ.
	 */
	public AffineTransform3D affineTransform()
	{
		// create base transforms
        AffineTransform3D rot1 = AffineTransform3D.createRotationOx(Math.toRadians(this.psi));
        AffineTransform3D rot2 = AffineTransform3D.createRotationOy(Math.toRadians(this.theta));
        AffineTransform3D rot3 = AffineTransform3D.createRotationOz(Math.toRadians(this.phi));

        // concatenate transforms
        AffineTransform3D trans = rot3.concatenate(rot2).concatenate(rot1);
        return trans;
	}

	/**
	 * Returns the 4-by-4 matrix corresponding to this EulerAnglesXYZ.
	 * 
	 * @see #fromMatrix(Jama.Matrix)
	 * @return the 4-by-4 matrix corresponding to this EulerAnglesXYZ.
	 */
	public Matrix rotationMatrix()
	{
        // compute equivalent transform
        AffineTransform3D trans = affineTransform();
        
        // convert to Matrix
        return trans.getMatrix();
	}
	
	public ArrayList<Vector3D> directionVectors()
	{
        // compute equivalent transform
        AffineTransform3D trans = affineTransform();
		
        ArrayList<Vector3D> vectors = new ArrayList<Vector3D>(3);
        vectors.add(new Vector3D(trans.m00, trans.m01, trans.m02));
        vectors.add(new Vector3D(trans.m10, trans.m11, trans.m12));
        vectors.add(new Vector3D(trans.m20, trans.m21, trans.m22));
        
        return vectors;
	}
}
