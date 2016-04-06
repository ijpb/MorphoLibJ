/**
 * 
 */
package inra.ijpb.morphology.directional;

import ij.process.ImageProcessor;
import inra.ijpb.morphology.Strel;

/**
 * Represents an oriented filter by concatenating two information: the
 * structuring element, and the operation applied to the neighborhood.
 * 
 * @author David Legland
 * 
 */
public class OrientedMorphologicalFilter {

	Filters.Operation operation;
	Strel strel;
	
	public OrientedMorphologicalFilter(Filters.Operation op, Strel strel) {
		this.operation = op;
		this.strel = strel;
	}
	
	public ImageProcessor applyTo(ImageProcessor image) {
		return this.operation.apply(image, this.strel);
	}
}
