/**
 * 
 */
package inra.ijpb.morphology.directional;

import inra.ijpb.morphology.Strel;

/**
 * 
 * @author David Legland
 *
 */
public interface OrientedStrelFactory {

	/**
	 * Creates an oriented structuring element with the given orientation (in
	 * degrees).
	 * 
	 * @param theta
	 *            the orientation of the resulting structuring element, in
	 *            degrees
	 * @return a new oriented structuring element
	 */
	public Strel createStrel(double theta);
}
