/**
 * 
 */
package inra.ijpb.morphology.strel;

import ij.process.ImageProcessor;
import inra.ijpb.morphology.Strel;

/**
 * A structuring element that can performs erosion or dilation directly in the
 * original image buffer. As InPlaceStrel do not require memory allocation, 
 * they result in faster execution.
 * 
 * @see SeparableStrel
 * @author David Legland
 *
 */
public interface InPlaceStrel extends Strel, InPlaceStrel3D {

	/**
	 * Performs dilation of the image given as argument, and stores the result
	 * in the same image. 
	 * @param image the input image to dilate
	 */
	public void inPlaceDilation(ImageProcessor image);

	/**
	 * Performs erosion of the image given as argument, and stores the result
	 * in the same image. 
	 * @param image the input image to erode
	 */
	public void inPlaceErosion(ImageProcessor image);
	
	/**
	 * The reverse structuring element of an InPlaceStrel is also an
	 * InPlaceStrel.
	 */
	public InPlaceStrel reverse();
}
