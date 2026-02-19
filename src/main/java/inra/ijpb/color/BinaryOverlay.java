/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2026 INRA.
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
package inra.ijpb.color;

import java.awt.Color;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.data.image.Images3D;

/**
 * Computes overlay of a binary image mask onto a grayscale or color image,
 * using the specified color. Both images must have the same size.
 * 
 * @author dlegland
 *
 */
public class BinaryOverlay extends AlgoStub
{
    /**
     * Empty constructor.
     */
    public BinaryOverlay()
    {
    }
    
    /**
     * Applies an overlay of a binary image mask onto a grayscale or color
     * image, using the specified color. Both images must have the same size.
     * 
     * @param imagePlus
     *            the original 2D or 3D image used as background
     * @param maskPlus
     *            the binary 2D or 3D mask image
     * @param color
     *            the color used to display overlay
     * @return a new ImagePlus instance containing a 2D or 3D color image
     */
    public ImagePlus process(ImagePlus imagePlus, ImagePlus maskPlus,
            Color color)
    {
        String newName = imagePlus.getShortTitle() + "-ovr";
        ImagePlus resultPlus;
        
        if (imagePlus.getStackSize() == 1)
        {
            ImageProcessor image = imagePlus.getProcessor();
            ImageProcessor mask = maskPlus.getProcessor();
            ImageProcessor result = process(image, mask, color);
            resultPlus = new ImagePlus(newName, result);
        } 
        else
        {
            // get reference image stack
            ImageStack image = imagePlus.getStack();
            
            // convert image to gray8 if necessary
            if (imagePlus.getBitDepth() != 24) 
            {
                double grayMin = imagePlus.getDisplayRangeMin();
                double grayMax = imagePlus.getDisplayRangeMax();
                image = Images3D.adjustDynamic(image, grayMin, grayMax);
            }
            
            // get binary mask
            ImageStack mask = maskPlus.getStack();

            // overlay binary mask on original image
            ImageStack result = process(image, mask, color);
            resultPlus = new ImagePlus(newName, result);
        }
        
        // keep calibration of parent image
        resultPlus.copyScale(imagePlus);
        return resultPlus;
    }
    
    /**
     * Applies an overlay of a binary image mask onto a grayscale or color
     * image, using the specified color. Both images must have the same size.
     * 
     * @param refImage
     *            the original image used as background
     * @param mask
     *            the binary mask image
     * @param color
     *            the color used to display overlay
     * @return a new ImagePlus instance containing a 2D color image
     */
    public ImageProcessor process(ImageProcessor refImage, ImageProcessor mask,
            Color color)
    {
        if (refImage instanceof ColorProcessor) 
        {
            return processRGB(refImage, mask, color);
        }
        else
        {
            if (!(refImage instanceof ByteProcessor)) 
            {
                refImage = refImage.convertToByteProcessor();
            }
            return processGray8(refImage, mask, color);
        }
    }
    
    /**
     * Assumes reference image contains a ByteProcessor.
     */
    private ImageProcessor processGray8(ImageProcessor refImage, 
            ImageProcessor mask, Color color) 
    {
        int sizeX = refImage.getWidth(); 
        int sizeY = refImage.getHeight(); 
        ColorProcessor result = new ColorProcessor(sizeX, sizeY);
        
        int value;
        int rgbValue = color.getRGB();
        
        this.fireStatusChanged(this, "Binary Overlay");
        // Iterate on image pixels, and choose result value depending on mask
        for (int y = 0; y < sizeY; y++)
        {
            this.fireProgressChanged(this, y, sizeY);
            for (int x = 0; x < sizeX; x++)
            {
                if (mask.get(x, y) == 0)
                {
                    // choose value from reference image
                    value = refImage.get(x, y);
                    // convert grayscale to equivalent color
                    value = (value & 0x00FF) << 16 | (value & 0x00FF) << 8
                            | (value & 0x00FF);
                    result.set(x, y, value);

                } else
                {
                    // set value to chosen color
                    result.set(x, y, rgbValue);
                }
            }
        }
        this.fireProgressChanged(this, 1, 1);
        
        return result;
    }
    
    
    /**
     * Assumes reference image contains a ColorProcessor.
     */
    private ImageProcessor processRGB(ImageProcessor refImage,
            ImageProcessor mask, Color color)
    {
        int sizeX = refImage.getWidth(); 
        int sizeY = refImage.getHeight(); 
        ColorProcessor result = new ColorProcessor(sizeX, sizeY);
        
        int value;
        int rgbValue = color.getRGB();
        
        // Iterate on image pixels, and choose result value depending on mask
        this.fireStatusChanged(this, "Binary Overlay");
        for (int y = 0; y < sizeY; y++)
        {
            this.fireProgressChanged(this, y, sizeY);
            for (int x = 0; x < sizeX; x++)
            {
                if (mask.get(x, y) == 0)
                {
                    // choose RGB value directly from reference image
                    value = refImage.get(x, y);
                    result.set(x, y, value);

                } else
                {
                    // set value to chosen color
                    result.set(x, y, rgbValue);
                }
            }
        }
        this.fireProgressChanged(this, 1, 1);
        
        return result;
    }

    /**
     * Applies an overlay of a binary image mask onto a grayscale or color
     * image, using the specified color. Both images must have the same size.
     * 
     * @param refImage
     *            the original 2D or 3D image used as background
     * @param mask
     *            the binary 2D or 3D mask image
     * @param color
     *            the color used to display overlay
     * @return a new ImageStack containing a 3D color image
     */
    public ImageStack process(ImageStack refImage, ImageStack mask,
            Color color)
    {
        int sizeX = refImage.getWidth(); 
        int sizeY = refImage.getHeight(); 
        int sizeZ = refImage.getSize();
        
        int bitDepth = refImage.getBitDepth();
        
        this.fireStatusChanged(this, "Binary Overlay - allocate");
        ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 24);
    
        int intVal;
        int rgbValue = color.getRGB();
                
        // for 16 and 32 bit images, compute gray level extent
        double vmin = Double.MAX_VALUE, vmax = Double.MIN_VALUE;
        if (bitDepth == 16 || bitDepth == 32)
        {
            this.fireStatusChanged(this, "Binary Overlay - init");
            for (int z = 0; z < sizeZ; z++)
            {
                for (int y = 0; y < sizeY; y++)
                {
                    for (int x = 0; x < sizeX; x++)
                    {
                        double value = refImage.getVoxel(x, y, z);
                        vmin = Math.min(vmin, value);
                        vmax = Math.max(vmax, value);
                    }
                }
            }
        }
        
        // Iterate on image voxels, and choose result value depending on mask
        this.fireStatusChanged(this, "Binary Overlay - process");
        for (int z = 0; z < sizeZ; z++)
        {
            this.fireProgressChanged(this, z, sizeZ);

            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    // For voxels in mask, apply the color of the background
                    if (mask.getVoxel(x, y, z) > 0)
                    {
                        result.setVoxel(x, y, z, rgbValue);
                        continue;
                    }

                    switch (bitDepth)
                    {
                    case 8:
                        // convert grayscale to equivalent color
                        intVal = (int) refImage.getVoxel(x, y, z);
                        intVal = (intVal & 0x00FF) << 16
                                | (intVal & 0x00FF) << 8 | (intVal & 0x00FF);
                        result.setVoxel(x, y, z, intVal);
                        break;

                    case 16:
                    case 32:
                        // convert grayscale to equivalent color
                        double value = refImage.getVoxel(x, y, z);
                        intVal = (int) (255 * (value - vmin) / (vmax - vmin));
                        intVal = (intVal & 0x00FF) << 16
                                | (intVal & 0x00FF) << 8 | (intVal & 0x00FF);
                        result.setVoxel(x, y, z, intVal);
                        break;

                    case 24:
                        // directly copy color code (after double conversion
                        // through double...)
                        result.setVoxel(x, y, z, refImage.getVoxel(x, y, z));
                        break;

                    default:
                    }
                }
            }
        }
        this.fireProgressChanged(this, 1, 1);
        
        return result;
    }
    
}
