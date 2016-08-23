/**
 * 
 */
package inra.ijpb.morphology.directional;

/**
 * Factory for oriented linear structuring elements.
 * 
 * @author David Legland
 *
 */
public class OrientedLineStrelFactory implements OrientedStrelFactory 
{
	/** 
	 * The Euclidean length of the discrete line 
	 */
	double length;

	/**
	 * Constructs a new oriented line factory, that will generate lines with
	 * approximately the given length.
	 * 
	 * @param length
	 *            the length of the lines to be generated
	 */
	public OrientedLineStrelFactory(double length)
	{
		this.length = length;
	}
	
	/**
	 * Creates a new instance of OrientedLineStrel with the length stored
	 * internally and the orientation given as argument.
	 * 
	 * @see inra.ijpb.morphology.directional.OrientedStrelFactory#createStrel(double)
	 */
	@Override
	public OrientedLineStrel createStrel(double theta) 
	{
		return new OrientedLineStrel(this.length, theta);
	}

}
