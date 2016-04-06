/**
 * 
 */
package inra.ijpb.morphology.directional;

import ij.process.ImageProcessor;
import inra.ijpb.morphology.Strel;

/**
 * @author David Legland
 *
 */
// TODO: maybe min+max+interface is not necessary ?
public class MaxDirectionalFilter implements MorphologicalDirectionalFilter 
{
	/**
	 * The factory of oriented structuring elements
	 */
	OrientedStrelFactory strelFactory;
	
	/**
	 * The number of distinct orientations, between 0 and 180.
	 * Examples:
	 * <ul>
	 * <li> nTheta = 2: considers 0 and 90° only </li>
	 * <li> nTheta = 4: considers 0, 45, 90 and 135 degrees</li>
	 * <li> nTheta = 180: considers one oriented line strel for each degree</li>
	 * </ul>
	 */
	int nTheta;
	
	/**
	 * The operation to apply with each structuring element
	 */
	Filters.Operation operation;
	
	public MaxDirectionalFilter(OrientedStrelFactory factory, int nTheta, Filters.Operation operation) 
	{
		this.strelFactory = factory;
		this.nTheta = nTheta;
		this.operation = operation;
	}

	public ImageProcessor applyTo(ImageProcessor image) 
	{
		ImageProcessor result = image.duplicate();
		result.setValue(0);
		result.fill();
		
		int width = image.getWidth();
		int height = image.getHeight();
		
		// Iterate over the set of directions
		for (int i = 0; i < nTheta; i++)
		{
			// Create the structuring element for current orientation
			double theta = ((double) i) * 180.0 / nTheta;
			Strel strel = this.strelFactory.createStrel(theta);

			// Apply oriented filter
			ImageProcessor oriented = this.operation.apply(image, strel);

			// combine current result with global result
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					int value = oriented.get(x, y);
					if (value > result.get(x, y))
					{
						result.set(x, y, value);
					}
				}
			}
		}
		
		// return the max value computed over all orientations
		return result;
	}

}
