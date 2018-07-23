/**
 * 
 */
package inra.ijpb.geometry;

/**
 * Data structure used to return result of Feret diameters computation. Can be
 * used to return the result of minimum or maximum diameters computation.
 * 
 * @author dlegland
 *
 */
public class AngleDiameterPair
{
	/** Angle in radians */
	public double angle;

	/** Diameter computed in the direction of the angle */
	public double diameter;

	/**
	 * Default constructor, using angle in degrees and diameter.
	 * 
	 * @param angle
	 *            the orientation angle, in degrees
	 * @param diameter
	 *            the diameter along the direction
	 */
	public AngleDiameterPair(double angle, double diameter)
	{
		this.angle = angle;
		this.diameter = diameter;
	}

}
