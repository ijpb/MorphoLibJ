/**
 * 
 */
package inra.ijpb.morphology.strel;

import java.util.Collection;

import inra.ijpb.morphology.Strel;

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
public interface SeparableStrel extends Strel {

	/**
	 * Decompose this separable structuring element into a set of smaller
	 * structuring elements that can be used to accelerate processing.
	 * @return a set of elementary structuring elements
	 */
	public Collection<InPlaceStrel> decompose();
	
	/**
	 * The reversed structuring element of a separable strel is also separable.
	 */
	public SeparableStrel reverse();
}
