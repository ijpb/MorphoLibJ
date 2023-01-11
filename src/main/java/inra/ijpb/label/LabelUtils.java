/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2023 INRA.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
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
