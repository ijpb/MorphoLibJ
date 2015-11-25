/**
 * 
 */
package inra.ijpb.watershed;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.morphology.MinimaAndMaxima;
import inra.ijpb.morphology.MinimaAndMaxima3D;

/**
 * <p>
 * Static methods for directly computing result of watershed after imposition of
 * extended minima.
 * </p>
 * 
 * Example of use:
 * <pre><code>
 *  ImagePlus imagePlus = IJ.getImage();
 *  ImageProcessor image = imagePlus.getProcessor();
 *  // Computes basins, using a dynamic of 10, and a connectivity equal to 4.
 *  ImageProcessor basins = ExtendedMinimaWatershed.extendedMinimaWatershed(image, 10, 4);
 *  ImagePlus resPlus = new ImagePlus("Basins", basins);
 *  resPlus.show();
 * </code></pre>
 * 
 */
public class ExtendedMinimaWatershed 
{
	/**
	 * Computes watershed on a gray scale image after imposition of extended
	 * minima.
	 * 
	 * @param imagePlus
	 *            the input image (grayscale)
	 * @param dynamic
	 *            the maximum difference between the minima and the boundary of
	 *            a basin
	 * @param connectivity
	 *            the connectivity to use
	 * @return the image of watershed basins computed on original image, as a
	 *         label image
	 * @see inra.ijpb.morphology.MinimaAndMaxima
	 */
	public static final ImagePlus extendedMinimaWatershed(
			ImagePlus imagePlus, int dynamic, int connectivity) 
	{
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
	 * @param image
	 *            the input image (grayscale)
	 * @param dynamic
	 *            the maximum difference between the minima and the boundary of
	 *            a basin
	 * @param connectivity
	 *            the connectivity to use, either 4 or 8
	 * @return the image of watershed basins computed on original image, as a
	 *         label image
	 * @see inra.ijpb.morphology.MinimaAndMaxima
	 */
	public static final ImageProcessor extendedMinimaWatershed(
			ImageProcessor image, int dynamic, int connectivity) 
	{
		ImageProcessor minima = MinimaAndMaxima.extendedMinima(image, dynamic, connectivity);
		ImageProcessor imposedMinima = MinimaAndMaxima.imposeMinima(image, minima, connectivity);
		ImageProcessor labels = BinaryImages.componentsLabeling(minima, connectivity, 32);
		ImageProcessor basins = Watershed.computeWatershed(imposedMinima, labels, connectivity, true);
		return basins;
	}

	/**
	 * Computes watershed on a gray scale image after imposition of extended minima.
	 * 
	 * @param image
	 *            the input 3D image (grayscale)
	 * @param dynamic
	 *            the maximum difference between the minima and the boundary of
	 *            a basin
	 * @param connectivity
	 *            the connectivity to use, either 6 or 26
	 * @return the image of watershed basins computed on original image, as a
	 *         label image
	 *         
	 * @see inra.ijpb.morphology.MinimaAndMaxima3D
	 */
	public static final ImageStack extendedMinimaWatershed(
			ImageStack image, int dynamic, int connectivity) 
	{
		ImageStack minima = MinimaAndMaxima3D.extendedMinima(image, dynamic, connectivity);
		ImageStack imposedMinima = MinimaAndMaxima3D.imposeMinima(image, minima, connectivity);
		ImageStack labels = BinaryImages.componentsLabeling(minima, connectivity, 32);
		ImageStack basins = Watershed.computeWatershed(imposedMinima, labels, connectivity, true);
		return basins;
	}
}
