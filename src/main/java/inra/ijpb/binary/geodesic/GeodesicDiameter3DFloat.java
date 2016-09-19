/**
 * 
 */
package inra.ijpb.binary.geodesic;

import ij.ImageStack;
import ij.measure.ResultsTable;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.ChamferWeights;
import inra.ijpb.binary.ChamferWeights3D;
import inra.ijpb.label.LabelImages;

/**
 * @author dlegland
 *
 */
public class GeodesicDiameter3DFloat extends AlgoStub implements GeodesicDiameter3D
{
	// ==================================================
	// Class variables
	
	/**
	 * The weights for orthogonal, diagonal, and cube-diagonal neighbors.
	 */
	float[] weights;

	
	// ==================================================
	// Constructors 
	
	/**
	 * Creates a new 3D geodesic diameter computation operator.
	 * 
	 * @param chamferWeights
	 *            an instance of ChamferWeights, which provides the float values
	 *            used for propagating distances
	 */
	public GeodesicDiameter3DFloat(ChamferWeights3D chamferWeights)
	{
		this.weights = chamferWeights.getFloatWeights();
	}
	
	/**
	 * Creates a new 3D geodesic diameter computation operator.
	 * 
	 * @param weights
	 *            the array of weights for orthogonal, diagonal, and eventually
	 *            chess-knight moves neighbors
	 */
	public GeodesicDiameter3DFloat(float[] weights)
	{
		if (weights.length < 3)
		{
			throw new IllegalArgumentException("Requires an array with at least three elements");
		}
		this.weights = weights;
	}
	
	
	/**
	 * Computes the geodesic diameter of each particle within the given label
	 * image.
	 * 
	 * @param labelImage
	 *            a label image, containing either the label of a particle or
	 *            region, or zero for background
	 * @return a ResultsTable containing for each label the geodesic diameter of
	 *         the corresponding particle
	 */
	public ResultsTable process(ImageStack labelImage)
	{
		// Check validity of parameters
		if (labelImage==null) return null;
		
		// compute max label within image
		int[] labels = LabelImages.findAllLabels(labelImage);
		int nbLabels = labels.length;

		GeodesicDistanceTransform calculator;
		
		// TODO Auto-generated method stub
		return null;
	}

}
