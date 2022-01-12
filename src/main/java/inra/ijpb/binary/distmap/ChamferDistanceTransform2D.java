/**
 * 
 */
package inra.ijpb.binary.distmap;

/**
 * Specialization of DistanceTransform based on the use of a chamfer mask.
 * 
 * Provides methods for retrieving the mask, and the normalization weight.
 * 
 * @author dlegland
 */
public interface ChamferDistanceTransform2D extends DistanceTransform
{
	/**
	 * Return the chamfer mask used by this distance transform algorithm.
	 * 
	 * @return the chamfer mask used by this distance transform algorithm.
	 */
	public ChamferMask2D mask();
}
