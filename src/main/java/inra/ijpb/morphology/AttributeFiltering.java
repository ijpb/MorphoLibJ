/**
 * 
 */
package inra.ijpb.morphology;

import ij.process.ImageProcessor;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.morphology.attrfilt.AreaOpening;
import inra.ijpb.morphology.attrfilt.AreaOpeningNaive;

/**
 * Several static methods for computation of attribute filtering (opening,
 * thinning...) on gray level images.
 * 
 * @author dlegland
 *
 */
public class AttributeFiltering
{
	public static final ImageProcessor areaOpening(ImageProcessor image, int minArea)
	{
		AreaOpening algo = new AreaOpeningNaive();
		DefaultAlgoListener.monitor(algo);
		return algo.process(image, minArea);
	}
}
