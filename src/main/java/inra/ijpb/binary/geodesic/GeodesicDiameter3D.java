/**
 * 
 */
package inra.ijpb.binary.geodesic;

import ij.ImageStack;
import ij.measure.ResultsTable;
import inra.ijpb.algo.Algo;

/**
 * Interface for computing geodesic diameter of a set of 3D binary or labeled
 * particles or regions. The data types for computation and algorithm
 * implementation are left to implementations.
 * 
 * <p>
 * Example of use:
 *
 * @see inra.ijpb.binary.geodesic.GeodesicDiameter3DShort
 * @see inra.ijpb.binary.geodesic.GeodesicDiameter3DFloat
 * 
 * @author dlegland
 */
public interface GeodesicDiameter3D extends Algo
{
	public abstract ResultsTable process(ImageStack labelImage);
}
