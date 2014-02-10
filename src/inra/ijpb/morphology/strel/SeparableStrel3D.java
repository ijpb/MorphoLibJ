/**
 * 
 */
package inra.ijpb.morphology.strel;

import inra.ijpb.morphology.Strel3D;

import java.util.Collection;

/**
 * Interface for structuring elements that can be decomposed into several 
 * "simpler" structuring elements. It is assumed that elementary structuring
 * elements can performs in place dilation or erosion (i.e. the implements the
 * InPlaceStrel interface).
 * 
 * @see InPlaceStrel
 * @author David Legland
 *
 */
public interface SeparableStrel3D extends Strel3D {

	/**
	 * Decompose this separable structuring element into a set of smaller
	 * structuring elements that can be used to accelerate processing.
	 * @return a set of elementary structuring elements
	 */
	public Collection<InPlaceStrel3D> decompose();
	
	/**
	 * The reversed structuring element of a separable strel is also separable.
	 */
	public SeparableStrel3D reverse();
}
