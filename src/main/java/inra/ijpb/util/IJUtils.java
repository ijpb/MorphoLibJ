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
public class IJUtils 
{
	/**
	 * Private constructor to prevent class instantiation.
	 */
	private IJUtils()
	{
	}

	/**
	 * Display elapsed time, converted into seconds, and computes number of
	 * processed elements per second. Also returns the created message.
	 * 
	 * @param opName
	 *            the name of the operation (algorithm, plugin...)
	 * @param timeInMillis
	 *            the elapsed time, in milliseconds
	 * @param refImage
	 *            the image on which process was applied
	 * @return the String corresponding to the message displayed in status bar
	 */
	public final static String showElapsedTime(String opName, double timeInMillis, ImagePlus refImage) 
	{
		int nElements;
		String elementName;
		if (refImage.getImageStackSize() == 1) 
		{
			nElements = refImage.getWidth() * refImage.getHeight();
			elementName = "pixels";
		}
		else 
		{
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
