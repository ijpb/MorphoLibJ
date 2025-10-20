package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.binary.skeleton.ImageJSkeleton;
import inra.ijpb.label.LabelImages;
import inra.ijpb.util.IJUtils;

/**
 * Computes the skeleton of each region within a 2D label map.
 * 
 * @author dlegland
 *
 */
public class SkeletonizeLabelsPlugin implements PlugIn
{
	@Override
	public void run(String arg)
	{
		// retrieve current image
		ImagePlus imagePlus = IJ.getImage();
		
		if (imagePlus.getStackSize() > 1)
		{
			IJ.showMessage("Requires a planar image as input");
			return;
		}
		
		// check if image is a label image
		if (!LabelImages.isLabelImageType(imagePlus))
		{
			IJ.showMessage("Input image should be a label image");
			return;
		}
		
		ImageProcessor labelMap = imagePlus.getProcessor();
		
		// compute skeleton image
		ImageJSkeleton skeletonize = new ImageJSkeleton();
		DefaultAlgoListener.monitor(skeletonize);
		long start = System.nanoTime();
		ImageProcessor skeleton = skeletonize.process(labelMap);
		
		// Keep same color model
		skeleton.setColorModel(labelMap.getColorModel());
		
		// Elapsed time, displayed in milli-seconds
		long finalTime = System.nanoTime();
		float elapsedTime = (finalTime - start) / 1000000.0f;
		
		// create resulting image
		String title = imagePlus.getShortTitle() + "-skel";
		ImagePlus skeletonPlus = new ImagePlus(title, skeleton);

		// Display with same spatial calibration as original image
		skeletonPlus.copyScale(imagePlus);
		skeletonPlus.show();
		
		IJUtils.showElapsedTime("Skeletonize Labels", elapsedTime, imagePlus); 
	}

}
