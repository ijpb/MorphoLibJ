/**
 * 
 */
package inra.ijpb.morphology.geodrec;

/**
 * Enumeration of the two different types of geodesic reconstruction. 
 * @author David Legland
 *
 */
public enum GeodesicReconstructionType 
{
	BY_DILATION,
	BY_EROSION;
	
	private GeodesicReconstructionType()
	{
	}
	
	/**
	 * Returns the sign that can be used in algorithms generic for dilation 
	 * and erosion.
	 * @return +1 for dilation, and -1 for erosion
	 */
	public int getSign() 
	{
		switch (this)
		{
		case BY_DILATION:
			return +1;
		case BY_EROSION:
			return -1;
		default:
			throw new RuntimeException("Unknown case: " + this.toString());
		}
	}
}
