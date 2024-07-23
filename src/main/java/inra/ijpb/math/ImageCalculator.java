/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
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
package inra.ijpb.math;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

/**
 * Provides some methods for combining two images, in a more comprehensive way
 * than standard ImageJ, and providing the possibility of defining its own local
 * operation.
 * 
 * Example of use:
 * <pre>{@code
        int width = 255;
        int height = 255;
        ImageProcessor image1 = new ByteProcessor(width, height);
        ImageProcessor image2 = new ByteProcessor(width, height);
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                image1.set(i, j, i);
                image2.set(j, i, i);
            }
        }
        
        ImageCalculator.Operation op = ImageCalculator.Operation.MAX;
        ImageProcessor result = ImageCalculator.combineImages(image1, image2, op);
        
        new ImagePlus("result", result).show();
 * }</pre>
 */
public class ImageCalculator
{   
    /**
     * General interface for defining an operation that combines the values of
     * two pixels to create a new one. Contains also static instances
     * corresponding to classical operations.
     * 
     * @author David Legland
     *
     */
    public interface Operation
    {
    	/** Plus operator, that adds values of the two images*/
        public static final Operation PLUS = new Operation()
        {
            @Override
            public int applyTo(int v1, int v2)
            {
                return v1 + v2;
            }

            @Override
            public float applyTo(float v1, float v2)
            {
                return v1 + v2;
            }
        };
        
    	/** Minus operator, that subtracts values of the two images*/
        public static final Operation MINUS = new Operation()
        {
            @Override
            public int applyTo(int v1, int v2)
            {
                return v1 - v2;
            }

            @Override
            public float applyTo(float v1, float v2)
            {
                return v1 - v2;
            }
        };
        
		/**
		 * Abs_diff operator, that computes the absolute value of the difference
		 * of values of the two images
		 */
        public static final Operation ABS_DIFF = new Operation()
        {
            @Override
            public int applyTo(int v1, int v2)
            {
                return Math.abs(v1 - v2);
            }

            @Override
            public float applyTo(float v1, float v2)
            {
                return Math.abs(v1 + v2);
            }
        };
        
    	/** Times operator, that multiples values of the two images*/
        public static final Operation TIMES = new Operation()
        {
            @Override
            public int applyTo(int v1, int v2)
            {
                return v1 * v2;
            }

            @Override
            public float applyTo(float v1, float v2)
            {
                return v1 * v2;
            }
        };
        
        /** Divides operator, that divides the values of the two images*/
        public static final Operation DIVIDES = new Operation()
        {
            @Override
            public int applyTo(int v1, int v2)
            {
                return v1 / v2;
            }

            @Override
            public float applyTo(float v1, float v2)
            {
                return v1 / v2;
            }
        };
        
    	/** Max operator, that computes the maximum of the values from the two images*/
        public static final Operation MAX = new Operation()
        {
            @Override
            public int applyTo(int v1, int v2)
            {
                return Math.max(v1, v2);
            }

            @Override
            public float applyTo(float v1, float v2)
            {
                return Math.max(v1, v2);
            }
        };
        
    	/** Min operator, that computes the minimum of the values from the two images*/
        public static final Operation MIN = new Operation()
        {
            @Override
            public int applyTo(int v1, int v2)
            {
                return Math.min(v1, v2);
            }

            @Override
            public float applyTo(float v1, float v2)
            {
                return Math.min(v1, v2);
            }
        };
        
    	/** Mean operator, that computes the average of the values from the two images*/
        public static final Operation MEAN = new Operation()
        {
            @Override
            public int applyTo(int v1, int v2)
            {
                return (v1 + v2) / 2;
            }

            @Override
            public float applyTo(float v1, float v2)
            {
                return (v1 + v2) / 2;
            }
        };
        
    	/** And operator.*/
        public static final Operation AND = new Operation()
        {
            @Override
            public int applyTo(int v1, int v2)
            {
                return v1 & v2;
            }

            @Override
            public float applyTo(float v1, float v2)
            {
                return ((int) v1) & ((int) v2);
            }
        };
        
    	/** Or operator.*/
        public static final Operation OR = new Operation()
        {
            @Override
            public int applyTo(int v1, int v2)
            {
                return v1 | v2;
            }

            @Override
            public float applyTo(float v1, float v2)
            {
                return ((int) v1) | ((int) v2);
            }
        };
        
    	/** Exclusive or operator.*/
        public static final Operation XOR = new Operation()
        {
            @Override
            public int applyTo(int v1, int v2)
            {
                return v1 ^ v2;
            }

            @Override
            public float applyTo(float v1, float v2)
            {
                return ((int) v1) ^ ((int) v2);
            }
        };
        
        /**
         * The method to override to make it possible to use an operation.
         * 
         * @param v1
         *            value of pixel in first image
         * @param v2
         *            value of pixel in second image
         * @return the result of the combination of the two pixel values
         */
        public int applyTo(int v1, int v2);
        
        /**
         * The method to override to make it possible to use an operation.
         * 
         * @param v1
         *            value of pixel in first image
         * @param v2
         *            value of pixel in second image
         * @return the result of the combination of the two pixel values
         */
        public float applyTo(float v1, float v2);
        
    }
    
    /**
	 * Combines two images using the specified operation.
	 * 
	 * @param image1
	 *            the first image
	 * @param image2
	 *            the second image
	 * @param op
	 *            the operation to apply
	 * @return the result of the combination of the two images.
	 */
    public static final ImagePlus combineImages(ImagePlus image1, ImagePlus image2, Operation op)
    {
        String newName = "result of " + image1.getShortTitle();
        if (image1.getStackSize() == 1)
        {
            ImageProcessor result = combineImages(image1.getProcessor(), image2.getProcessor(), op);
            return new ImagePlus(newName, result);
        }
        else
        {
            ImageStack result = combineImages(image1.getStack(), image2.getStack(), op);
            return new ImagePlus(newName, result);
        }
    }
    
    /**
     * Example that computes the maximum over two images:
     * 
     * <pre>
     * <code>
     * ImageProcessor image1 = ...
     * ImageProcessor image2 = ...
     * ImageProcessor result = ImageCalculator.combineImages(
     * 		image1, image2, ImageCalculator.Operation.MAX);  
     * </code>
     * </pre>
     * 
     * @param image1
     *            the first input image
     * @param image2
     *            the second input image
     * @param op
     *            the operation to apply
     * @return the result of operation applied to the pair of input images
     */
    public static final ImageProcessor combineImages(ImageProcessor image1, ImageProcessor image2,
            Operation op)
    {
        int width = image1.getWidth();
        int height = image1.getHeight();
        
        ImageProcessor result = image1.duplicate();
        
        float v1, v2, vr;
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                v1 = image1.getf(x, y);
                v2 = image2.getf(x, y);
                vr = op.applyTo(v1, v2);
                result.setf(x, y, vr);
            }
        }
        
        return result;
    }
    
    /**
	 * Combines two images using the specified operation.
	 * 
	 * @param image1
	 *            the first image
	 * @param image2
	 *            the second image
	 * @param op
	 *            the operation to apply
	 * @return the result of the combination of the two images.
	 */
    public static final ImageStack combineImages(ImageStack image1, ImageStack image2, Operation op)
    {
        int width = image1.getWidth();
        int height = image1.getHeight();
        int depth = image1.getSize();
        
        ImageStack result = image1.duplicate();
        
        float v1, v2, vr;
        for (int z = 0; z < depth; z++)
        {
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
                    v1 = (float) image1.getVoxel(x, y, z);
                    v2 = (float) image2.getVoxel(x, y, z);
                    vr = op.applyTo(v1, v2);
                    result.setVoxel(x, y, z, vr);
                }
            }
        }
        return result;
    }
    
    /**
	 * Computes the binary "not" operation on the input image.
	 * 
	 * @param image
	 *            the input image
	 * @return the result of the not operation
	 */
    public static final ImagePlus not(ImagePlus image)
    {
        String newName = image.getShortTitle() + "-not";
        if (image.getStackSize() == 1)
        {
            ImageProcessor result = not(image.getProcessor());
            return new ImagePlus(newName, result);
        }
        else
        {
            ImageStack result = not(image.getStack());
            return new ImagePlus(newName, result);
        }
    }
    
    /**
	 * Computes the binary "not" operation on the input 2D image.
	 * 
	 * @param image
	 *            the input image
	 * @return the result of the not operation
	 */
    public static final ImageProcessor not(ImageProcessor image)
    {
        int width = image.getWidth();
        int height = image.getHeight();
        
        ImageProcessor result = image.duplicate();
        
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                result.set(x, y, ~image.get(x, y));
            }
        }
        return result;
    }
    
    /**
	 * Computes the binary "not" operation on the input 3D image.
	 * 
	 * @param image
	 *            the input image
	 * @return the result of the not operation
	 */
    public static final ImageStack not(ImageStack image)
    {
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        int sizeZ = image.getHeight();
        
        ImageStack result = image.duplicate();
        
        for (int z = 0; z < sizeZ; z++)
        {
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    result.setVoxel(x, y, z, ~((int) image.getVoxel(x, y, z)));
                }
            }
        }
        return result;
    }
}
