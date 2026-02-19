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
package inra.ijpb.shape;

import ij.ImageStack;
import ij.process.ImageProcessor;
import inra.ijpb.data.border.BorderManager;
import inra.ijpb.data.border.BorderManager3D;
import inra.ijpb.data.border.ReplicatedBorder;
import inra.ijpb.data.border.ReplicatedBorder3D;

/**
 * A collection of utility method for processing global shape of images: crop, add borders...
 * 
 * @author dlegland
 *
 */
public class ImageShape
{
    /**
     * Adds the specified number of pixels around the input image, and returns
     * the resulting image. 
     * 
     * @param image
     *            the input image
     * @param left
     *            the number of pixels to add to the left of the image
     * @param right
     *            the number of pixels to add to the right of the image
     * @param top
     *            the number of pixels to add on top of the image
     * @param bottom
     *            the number of pixels to add at the bottom of the image
     * @return a new image with extended borders
     */
    public static final ImageProcessor addBorders(ImageProcessor image, 
            int left, int right, int top, int bottom)
    {
        // get image dimensions
        int sizeX = image.getWidth(); 
        int sizeY = image.getHeight(); 
        
        // compute result dimensions
        int sizeX2 = sizeX + left + right;
        int sizeY2 = sizeY + top + bottom;
        ImageProcessor result = image.createProcessor(sizeX2, sizeY2);
        
        // create border manager
        BorderManager extended = new ReplicatedBorder(image);
        
        // fill result image
        for (int y = 0; y < sizeY2; y++)
        {
            for (int x = 0; x < sizeX2; x++)
            {
                result.set(x, y, extended.get(x - left, y - top));
            }
        }
        
        return result;
    }
    
    
    /**
     * Adds the specified number of voxels around the input image, and returns
     * the resulting image. 
     * 
     * @param image
     *            the input image
     * @param left
     *            the number of pixels to add to the left of the image
     * @param right
     *            the number of pixels to add to the right of the image
     * @param top
     *            the number of pixels to add on top of the image
     * @param bottom
     *            the number of pixels to add at the bottom of the image
     * @param front
     *            the number of pixels to add on the front of the image
     * @param back
     *            the number of pixels to add at the back of the image
     * @return a new ImageStack with extended borders
     */
    public static final ImageStack addBorders(ImageStack image, 
            int left, int right, int top, int bottom, int front, int back)
    {
        // get image dimensions
        int sizeX = image.getWidth(); 
        int sizeY = image.getHeight(); 
        int sizeZ = image.getSize(); 
        
        // compute result dimensions
        int sizeX2 = sizeX + left + right;
        int sizeY2 = sizeY + top + bottom;
        int sizeZ2 = sizeZ + front + back;
        ImageStack result = ImageStack.create(sizeX2, sizeY2, sizeZ2, image.getBitDepth());
        
        // create border manager
        BorderManager3D extended = new ReplicatedBorder3D(image);
        
        // fill result image
        for (int z = 0; z < sizeZ2; z++)
        {
            for (int y = 0; y < sizeY2; y++)
            {
                for (int x = 0; x < sizeX2; x++)
                {
                    result.setVoxel(x, y, z, extended.get(x - left, y - top, z - front));
                }
            }
        }
        
        return result;
    }
    
    /**
     * Crops a rectangular region from the input image.
     * 
     * @param image
     *            the image to crop
     * @param x0
     *            the x-position of the upper-left corner of the region to crop
     * @param y0
     *            the y-position of the upper-left corner of the region to crop
     * @param width
     *            the width of the region to crop
     * @param height
     *            the height of the region to crop
     * @return a new ImageProcessor corresponding to the cropped region.
     */
    public static final ImageProcessor cropRect(ImageProcessor image, int x0, int y0, int width, int height)
    {
        // retrieve image size
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        
        // check crop bounds
        if (x0 + width - 1 >= sizeX || y0 + height - 1 >= sizeY)
        {
            throw new IllegalArgumentException("Crop bounds exceed image bounds");
        }
        
        // allocate
        ImageProcessor res = image.createProcessor(width, height);
        
        // fill result image with crop region
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                res.setf(x, y, image.getf(x + x0, y + y0));
            }
        }
        return res;
    }

    /**
     * Crops a rectangular region from the input 3D image.
     * 
     * @param image
     *            the image to crop
     * @param x0
     *            the x-position of the upper-left corner of the region to crop
     * @param y0
     *            the y-position of the upper-left corner of the region to crop
     * @param z0
     *            the z-position of the upper-left corner of the region to crop
     * @param width
     *            the width of the region to crop
     * @param height
     *            the height of the region to crop
     * @param depth
     *            the depth of the region to crop
     * @return a new ImageStack corresponding to the cropped region.
     */
    public static final ImageStack cropRect(ImageStack image, int x0, int y0, int z0, int width, int height, int depth)
    {
        // retrieve image size
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        int sizeZ = image.getSize();
        
        // check crop bounds
        if (x0 + width - 1 >= sizeX || y0 + height - 1 >= sizeY || z0 + depth - 1 >= sizeZ)
        {
            throw new IllegalArgumentException("Crop bounds exceed image bounds");
        }
        
        // allocate
        ImageStack res = ImageStack.create(width, height, depth, image.getBitDepth());
        
        // fill result image with crop region
        for (int z = 0; z < depth; z++)
        {
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
                    res.setVoxel(x, y, z, image.getVoxel(x + x0, y + y0, z + z0));
                }
            }
        }
        
        return res;
    }

    /**
     * Sub-samples the input image by retaining one pixel over k along each
     * direction.
     * 
     * @param image
     *            the input image
     * @param k
     *            the sampling ratio of pixels in each direction
     * @return a new sub-sampled image
     */
    public static final ImageProcessor subsample(ImageProcessor image, int k)
    {
        return subsample(image, k, k);
    }
    
    /**
     * Sub-samples the input image by retaining one pixel over k_i along each
     * direction.
     * 
     * @param image
     *            the input image
     * @param kx
     *            the sampling ratio of pixels in the x-direction
     * @param ky
     *            the sampling ratio of pixels in the y-direction
     * @return a new sub-sampled image
     */
    public static final ImageProcessor subsample(ImageProcessor image, int kx, int ky)
    {
        // get image dimensions
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        
        // compute result dimensions
        int sizeX2 = Math.floorDiv(sizeX, kx);
        int sizeY2 = Math.floorDiv(sizeY, ky);
        ImageProcessor result = image.createProcessor(sizeX2, sizeY2);
        
        // fill result image
        for (int y = 0; y < sizeY2; y++)
        {
            for (int x = 0; x < sizeX2; x++)
            {
                result.set(x, y, image.get(x * kx, y * ky));
            }
        }
        
        return result;
    }
    
    /**
     * Sub-samples the input image by retaining one voxel over k along each
     * direction.
     * 
     * @param image
     *            the input 3D image
     * @param k
     *            the sampling ratio of voxels in each direction
     * @return a new sub-sampled image
     */
    public static final ImageStack subsample(ImageStack image, int k)
    {
        return subsample(image, k, k, k);
    }
    
    /**
     * Sub-samples the input image by retaining one voxel over k_i along each
     * direction.
     * 
     * @param image
     *            the input 3D image
     * @param kx
     *            the sampling ratio of voxels in the x-direction
     * @param ky
     *            the sampling ratio of voxels in the y-direction
     * @param kz
     *            the sampling ratio of voxels in the z-direction
     * @return a new sub-sampled image
     */
    public static final ImageStack subsample(ImageStack image, int kx, int ky, int kz)
    {
        // get image dimensions
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        int sizeZ = image.getSize();
        
        // compute result dimensions
        int sizeX2 = Math.floorDiv(sizeX, kx);
        int sizeY2 = Math.floorDiv(sizeY, ky);
        int sizeZ2 = Math.floorDiv(sizeZ, kz);
        ImageStack result = ImageStack.create(sizeX2, sizeY2, sizeZ2, image.getBitDepth());
        
        // fill result image
        for (int z = 0; z < sizeZ2; z++)
        {
            for (int y = 0; y < sizeY2; y++)
            {
                for (int x = 0; x < sizeX2; x++)
                {
                    result.setVoxel(x, y, z, image.getVoxel(x * kx, y * ky, z * kz));
                }
            }
        }        
        return result;
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private ImageShape()
    {
    }
}
