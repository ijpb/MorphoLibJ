/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.GeodesicReconstruction;
import inra.ijpb.morphology.GeodesicReconstruction3D;
import inra.ijpb.util.IJUtils;

/**
 * Plugin for filling holes (dark holes within bright structures) in 8-bits 
 * grayscale or binary 2D/3D images.
 */
public class FillHolesPlugin implements PlugIn
{

	ImagePlus imp;
	
	@Override
	public void run(String arg0) 
	{
		ImagePlus imagePlus = IJ.getImage();
		
		String newName = imagePlus.getShortTitle() + "-fillHoles";
		
		long t0 = System.currentTimeMillis();
		
		ImagePlus resultPlus;
		if (imagePlus.getStackSize() > 1)
		{
			ImageStack stack = imagePlus.getStack();
			ImageStack result = GeodesicReconstruction3D.fillHoles(stack);
			resultPlus = new ImagePlus(newName, result);
			
		} 
		else 
		{
			ImageProcessor image = imagePlus.getProcessor();
			ImageProcessor result = GeodesicReconstruction.fillHoles(image);
			resultPlus = new ImagePlus(newName, result);
		}
		long elapsedTime = System.currentTimeMillis() - t0;
		
		resultPlus.show();
		resultPlus.copyScale(imagePlus);
		
		if (imagePlus.getStackSize() > 1) 
		{
			resultPlus.setSlice(imagePlus.getSlice());
		}
		
		 IJUtils.showElapsedTime("Fill Holes", elapsedTime, imagePlus);
	}

}
