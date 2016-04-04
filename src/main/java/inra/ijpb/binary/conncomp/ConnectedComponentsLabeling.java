/**
 * 
 */
package inra.ijpb.binary.conncomp;

import ij.process.ImageProcessor;
import inra.ijpb.algo.Algo;

/**
 * @author dlegland
 *
 */
public interface ConnectedComponentsLabeling extends Algo
{
	public ImageProcessor computeLabels(ImageProcessor binaryImage);
}
