/**
 * 
 */
package inra.ijpb.morphology.attrfilt;

import ij.process.ImageProcessor;
import inra.ijpb.algo.Algo;

/**
 * Area opening for gray level images.
 * 
 * @author dlegland
 *
 */
public interface AreaOpening extends Algo
{
	public ImageProcessor process(ImageProcessor image, int minArea);
}
