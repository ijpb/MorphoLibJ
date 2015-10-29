/**
 * 
 */
package inra.ijpb.watershed;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import inra.ijpb.binary.ConnectedComponents;
import inra.ijpb.morphology.MinimaAndMaxima;
import inra.ijpb.morphology.MinimaAndMaxima3D;

/**
 * Static methods for directly computing result of watershed after imposition of
 * extended minima.
 * 
 */
public class ExtendedMinimaWatershed {
	/**
	 * Computes watershed on a gray scale image after imposition of extended minima.
	 * 
	 * @see inra.ijpb.morphology.MinimaAndMaxima
	 */
	public static final ImagePlus extendedMinimaWatershed(
			ImagePlus imagePlus, int dynamic, int connectivity) {
		
		ImagePlus resultPlus;
		String newName = imagePlus.getShortTitle() + "-watershed";
		if (imagePlus.getStackSize() == 1)  
		{
			ImageProcessor image = imagePlus.getProcessor();
			ImageProcessor result = extendedMinimaWatershed(image, dynamic, connectivity);
			resultPlus = new ImagePlus(newName, result);
		}
		else
		{
			ImageStack image = imagePlus.getStack();
			ImageStack result = extendedMinimaWatershed(image, dynamic, connectivity);
			resultPlus = new ImagePlus(newName, result);
		}
		
		resultPlus.setCalibration( imagePlus.getCalibration() );
		return resultPlus;
	}

	/**
	 * Computes watershed on a gray scale image after imposition of extended minima.
	 * 
	 * @see inra.ijpb.morphology.MinimaAndMaxima
	 */
	public static final ImageProcessor extendedMinimaWatershed(
			ImageProcessor image, int dynamic, int connectivity) {
		
		ImageProcessor minima = MinimaAndMaxima.extendedMinima(image, dynamic, connectivity);
		ImageProcessor imposedMinima = MinimaAndMaxima.imposeMinima(image, minima, connectivity);
		ImageProcessor labels = ConnectedComponents.computeLabels(minima, connectivity, 32);
		ImageProcessor basins = Watershed.computeWatershed(imposedMinima, labels, connectivity, true);
		return basins;
	}

	/**
	 * Computes watershed on a gray scale image after imposition of extended minima.
	 * 
	 * @see inra.ijpb.morphology.MinimaAndMaxima3D
	 */
	public static final ImageStack extendedMinimaWatershed(
			ImageStack image, int dynamic, int connectivity) {
		
		ImageStack minima = MinimaAndMaxima3D.extendedMinima(image, dynamic, connectivity);
		ImageStack imposedMinima = MinimaAndMaxima3D.imposeMinima(image, minima, connectivity);
		ImageStack labels = ConnectedComponents.computeLabels(minima, connectivity, 32);
		ImageStack basins = Watershed.computeWatershed(imposedMinima, labels, connectivity, true);
		return basins;
	}
}
