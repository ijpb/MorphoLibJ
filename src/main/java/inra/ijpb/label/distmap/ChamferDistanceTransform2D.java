/**
 * 
 */
package inra.ijpb.label.distmap;

import inra.ijpb.binary.distmap.ChamferMask2D;

/**
 * Specialization of DistanceTransform based on the use of a chamfer mask.
 * 
 * Provides methods for retrieving the mask, and the normalization weight.
 * 
 * @see ChamferMask2D
 * 
 * @author dlegland
 */
public interface ChamferDistanceTransform2D extends DistanceTransform2D
{
	/**
	 * Return the chamfer mask used by this distance transform algorithm.
	 * 
	 * @return the chamfer mask used by this distance transform algorithm.
	 */
	public ChamferMask2D mask();
}
