/**
 * 
 */
package inra.ijpb.morphology;

import ij.ImageStack;
import ij.process.ImageProcessor;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.morphology.attrfilt.AreaOpening;
import inra.ijpb.morphology.attrfilt.AreaOpeningQueue;
import inra.ijpb.morphology.attrfilt.SizeOpening3D;
import inra.ijpb.morphology.attrfilt.SizeOpening3DQueue;

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
		AreaOpening algo = new AreaOpeningQueue();
		DefaultAlgoListener.monitor(algo);
		return algo.process(image, minArea);
	}

	public static final ImageStack volumeOpening(ImageStack image, int minVolume)
	{
		SizeOpening3D algo = new SizeOpening3DQueue();
		DefaultAlgoListener.monitor(algo);
		return algo.process(image, minVolume);
	}
}
