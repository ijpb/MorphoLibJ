/**
 * 
 */
package inra.ijpb.binary.geodesic;

import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import inra.ijpb.algo.Algo;

/**
 * Interface for computing geodesic diameter of a set of binary or labeled
 * particles or regions. The data types for computation and algorithm
 * implementation are left to implementations.
 * 
 * <p>
 * Example of use:
 *
 * <pre>
 * {
 * 	&#064;code
 * 	float[] weights = ChamferWeights.CHESSKNIGHT.getFloatWeights();
 * 	GeodesicDiameter gd = new GeodesicDiameterFloat(weights);
 * 	ResultsTable table = gd.analyseImage(inputLabelImage);
 * 	table.show(&quot;Geodesic Diameter&quot;);
 * }
 * </pre>
 *
 * @see inra.ijpb.binary.geodesic.GeodesicDiameterShort
 * @see inra.ijpb.binary.geodesic.GeodesicDiameterFloat
 * 
 * @author dlegland
 */
public interface GeodesicDiameter extends Algo
{
	public abstract ResultsTable analyzeImage(ImageProcessor labelImage);
}
