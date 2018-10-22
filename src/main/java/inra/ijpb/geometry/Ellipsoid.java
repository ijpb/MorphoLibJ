/**
 * 
 */
package inra.ijpb.geometry;

/**
 * An ellipsoid in the 3D space, defined by a center, three radius, and three
 * orientation angles.
 * 
 * @author dlegland
 *
 */
public class Ellipsoid
{
	// ==================================================
	// Static methods
	
    /**
     * Initializes center array from ellipsoid array.
     *
     * @param ellipsoids
     *            an array of ellipsoids
     * @return the array of points corresponding to the centers of the
     *         ellipsoids.
     */
    public static final Point3D[] centers(Ellipsoid[] ellipsoids)
    {
        Point3D[] centroids = new Point3D[ellipsoids.length];
        for (int i = 0; i < ellipsoids.length; i++)
        {
            centroids[i] = ellipsoids[i].center();
        }
        return centroids;
    }
    
	/**
	 * Computes the three elongation factors for an array of ellipsoids.
	 * 
	 * <pre><code>
	 * ImageStack labelImage = ...
	 * int[] labels = LabelImages.findAllLabels(image);
	 * Calibation calib = new Calibration();
	 * Ellipsoid[] ellipsoids = new InertiaEllipsoid().analyzeRegions(labelImage, labels, calib);
	 * double[][] elongations = GeometricMeasures3D.computeEllipsoidElongations(ellipsoids);
	 * </code></pre>
	 * 
	 * @param ellipsoids
	 *            an array of ellipsoids
	 * @return an array of elongation factors. When radii are ordered such that
	 *         R1 &gt; R2 &gt; R3, the three elongation factors are defined by
	 *         ratio of R1 by R2, ratio of R1 by R3, and ratio of R2 by R3.
	 */
	public static final double[][] elongations(Ellipsoid[] ellipsoids)
    {
		int nLabels = ellipsoids.length;
    	double[][] res = new double[nLabels][3];
    	
    	for (int i = 0; i < nLabels; i++)
    	{
    		double ra = ellipsoids[i].radius1;
    		double rb = ellipsoids[i].radius2;
    		double rc = ellipsoids[i].radius3;
    		
    		res[i][0] = ra / rb;
    		res[i][1] = ra / rc;
    		res[i][2] = rb / rc;
    	}
    	
    	return res;
    }
    

	// ==================================================
	// Class variables
	
	/**
	 * The x-coordinate of the ellipsoid center
	 */
	double centerX;
	
	/**
	 * The y-coordinate of the ellipsoid center
	 */
	double centerY;
	
	/**
	 * The z-coordinate of the ellipsoid center
	 */
	double centerZ;
	
	
	/**
	 * The length of the largest semi-axis
	 */
	double radius1;
	
	/**
	 * The length of the second largest semi-axis
	 */
	double radius2;
	
	/**
	 * The length of the smallest semi-axis
	 */
	double radius3;
	
	
	/**
	 * The azimut of the main axis, in degrees
	 */
	double phi;

	/**
	 * The elevation of the main axis, in degrees
	 */
	double theta;
	
	/**
	 * The roll of the ellipsoid around the main axis, in degrees.
	 */
	double psi;
	
	
	// ==================================================
	// Constructors
	
	/**
	 * Creates a new 3D Ellipsoid parallel to the three main axes.
	 * 
	 * @param center
	 *            the center of the ellipsoid
	 * @param r1
	 *            the length of the largest semi-axis
	 * @param r2
	 *            the length of the second largest semi-axis
	 * @param r3
	 *            the length of the smallest semi-axis
	 */
	public Ellipsoid(Point3D center, double r1, double r2, double r3)
	{
		this(center, r1, r2, r3, 0, 0, 0);
	}

	/**
	 * Creates a new 3D Ellipsoid
	 * 
	 * @param center
	 *            the center of the ellipsoid
	 * @param r1
	 *            the length of the largest semi-axis
	 * @param r2
	 *            the length of the second largest semi-axis
	 * @param r3
	 *            the length of the smallest semi-axis
	 * @param phi
	 *            the azimut of the main axis, in degrees
	 * @param theta
	 *            the elevation of the main axis, in degrees
	 * @param psi
	 *            the roll of the ellipsoid around the main axis, in degrees.
	 */
	public Ellipsoid(Point3D center, double r1, double r2, double r3, double phi, double theta, double psi)
	{
		this.centerX = center.getX();
		this.centerY = center.getY();
		this.centerZ = center.getZ();
		this.radius1 = r1;
		this.radius2 = r2;
		this.radius3 = r3;
		this.phi = phi;
		this.theta = theta;
		this.psi = psi;
	}

	/**
	 * Creates a new 3D Ellipsoid
	 * 
	 * @param xc
	 *            the x-coordinate of ellipsoid center
	 * @param yc
	 *            the y-coordinate of ellipsoid center
	 * @param zc
	 *            the z-coordinate of ellipsoid center
	 * @param r1
	 *            the length of the largest semi-axis
	 * @param r2
	 *            the length of the second largest semi-axis
	 * @param r3
	 *            the length of the smallest semi-axis
	 * @param phi
	 *            the azimut of the main axis, in degrees
	 * @param theta
	 *            the elevation of the main axis, in degrees
	 * @param psi
	 *            the roll of the ellipsoid around the main axis, in degrees.
	 */
	public Ellipsoid(double xc, double yc, double zc, double r1, double r2, double r3, double phi, double theta, double psi)
	{
		this.centerX = xc;
		this.centerY = yc;
		this.centerZ = zc;
		this.radius1 = r1;
		this.radius2 = r2;
		this.radius3 = r3;
		this.phi = phi;
		this.theta = theta;
		this.psi = psi;
	}
	
	// ==================================================
	// Accesors
	
	public Point3D center()
	{
		return new Point3D(centerX, centerY, centerZ);
	}
	
	public double radius1()
	{
		return radius1;
	}

	public double radius2()
	{
		return radius2;
	}

	public double radius3()
	{
		return radius3;
	}

	public double phi()
	{
		return phi;
	}

	public double theta()
	{
		return theta;
	}

	public double psi()
	{
		return psi;
	}

}
