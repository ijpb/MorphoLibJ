/**
 * 
 */
package inra.ijpb.morphology;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * Plugin for filling holes (dark holes within bright structures) in 8-bits 
 * grayscale or binary images.
 */
public class FillHolesPlugin implements PlugInFilter {

	ImagePlus imp;
	
	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	@Override
	public int setup(String arg, ImagePlus imp) {
		if (imp == null) {
			IJ.noImage();
			return DONE;
		}
		
		this.imp = imp;
		return DOES_ALL;
	}

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	@Override
	public void run(ImageProcessor ip) {
		ImageProcessor recProc = GeodesicReconstruction.fillHoles(ip);
		String newName = createResultImageName(imp);
		
		ImagePlus resultImage = new ImagePlus(newName, recProc);
		resultImage.show();
	}

	private static String createResultImageName(ImagePlus baseImage) {
		return baseImage.getShortTitle() + "-fillHoles";
	}
}
