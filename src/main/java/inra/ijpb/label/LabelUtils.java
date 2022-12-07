/**
 * 
 */
package inra.ijpb.label;

import ij.ImageStack;
import ij.process.ImageProcessor;

/**
 * A collection of utility methods for working with label images.
 * 
 * Contrary to the "LabelImages" class, methods in this class do not aim at
 * returning a Label image, and are expected to be used from plugins or other
 * processing classes.
 * 
 * @see LabelImages
 * 
 * @author dlegland
 *
 */
public class LabelUtils
{
    /**
     * Determine the largest possible label that can be used with the specified
     * image. The max label number is chosen according to image bitDepth.
     * 
     * @param labelMap
     *            the image used for storing labels
     * @return the largest integer value of the label that can be stored within
     *         the input label map.
     */
    public static final int getLargestPossibleLabel(ImageProcessor labelMap)
    {
        // choose max label number depending on image bitDepth
        switch (labelMap.getBitDepth()) {
        case 8: 
            return 255;
        case 16: 
            return 65535;
        case 32:
            return 0x01 << 23 - 1;
        default:
            throw new IllegalArgumentException(
                    "Bit Depth can only be 8, 16 or 32.");
        }
    }

    /**
     * Determine the largest possible label that can be used with the specified
     * image. The max label number is chosen according to image bitDepth.
     * 
     * @param labelMap
     *            the image used for storing labels
     * @return the largest integer value of the label that can be stored within
     *         the input label map.
     */
    public static final int getLargestPossibleLabel(ImageStack labelMap)
    {
        // choose max label number depending on image bitDepth
        switch (labelMap.getBitDepth()) {
        case 8: 
            return 255;
        case 16: 
            return 65535;
        case 32:
            return 0x01 << 23 - 1;
        default:
            throw new IllegalArgumentException(
                    "Bit Depth can only be 8, 16 or 32.");
        }
    }

    /** 
     * Private constructor to prevent instantation.
     */
    private LabelUtils()
    {
    }
}
