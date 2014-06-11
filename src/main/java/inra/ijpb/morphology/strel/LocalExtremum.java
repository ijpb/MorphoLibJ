/**
 * 
 */
package inra.ijpb.morphology.strel;

/**
 * Computes the extremum in a local sliding neighborhood of the current pixel.
 * 
 * @author David Legland
 *
 */
public interface LocalExtremum
{
	public enum Type {
		MINIMUM,
		MAXIMUM
	};

}
