/**
 * 
 */
package inra.ijpb.util;

import java.util.Locale;

import ij.IJ;
import ij.ImagePlus;

/**
 * A collection of utility methods for interacting with ImageJ.
 * 
 * @author David Legland
 *
 */
public class IJUtils {

	/**
	 * Display elapsed time, converted into seconds, and computes number of
	 * processed elements per second. Also returns the created message.
	 */
	public final static String showElapsedTime(String opName, long timeInMillis, ImagePlus refImage) {
		int nElements;
		String elementName;
		if (refImage.getImageStackSize() == 1) {
			nElements = refImage.getWidth() * refImage.getHeight();
			elementName = "pixels";
		} else {
			nElements = refImage.getWidth() * refImage.getHeight() * refImage.getStackSize();
			elementName = "voxels";
		}
		
		double timeInSecs = ((double) timeInMillis) / 1000.;
		int elementsPerSecond = (int) ((double) nElements / timeInSecs);
				
		String pattern = "%s: %.3f seconds, %d %s/second";
		String status = String.format(Locale.ENGLISH, pattern, opName, timeInSecs, elementsPerSecond, elementName);
		
		IJ.showStatus(status);
		return status;
	}
}
