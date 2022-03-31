/**
 * 
 */
package inra.ijpb.data.image;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

/**
 * A collection of static utility methods for processing 2D or 3D images.
 * 
 * @author dlegland
 */
public class ImageUtils
{
    public static final boolean isColorImage(ImagePlus imagePlus)
    {
        return imagePlus.getType() == ImagePlus.COLOR_256 || imagePlus.getType() == ImagePlus.COLOR_RGB;
    }
    
    /**
     * Checks if the two input images have the same size in each direction.
     * 
     * @param image1
     *            the first image
     * @param image2
     *            the second image
     * @return true if both images have same width and height, and false
     *         otherwise.
     */
    public static final boolean isSameSize(ImageProcessor image1, ImageProcessor image2)
    {
        if (image1.getWidth() != image2.getWidth())
            return false;
        if (image1.getHeight() != image2.getHeight())
            return false;
        return true;
    }

    /**
     * Checks if the two input 3D images have the same size in each direction.
     * 
     * @param image1
     *            the first image
     * @param image2
     *            the second image
     * @return true if both images have same width, height and number of slices,
     *         and false otherwise.
     */
    public static final boolean isSameSize(ImageStack image1, ImageStack image2)
    {
        if (image1.getWidth() != image2.getWidth())
            return false;
        if (image1.getHeight() != image2.getHeight())
            return false;
        if (image1.getSize() != image2.getSize())
            return false;
        return true;
    }
    
    /**
     * Checks if the two input images have the same data type.
     * 
     * @param image1
     *            the first image
     * @param image2
     *            the second image
     * @return true if both images have the same data type, given by the bit
     *         depth.
     */
    public static final boolean isSameType(ImageProcessor image1, ImageProcessor image2)
    {
        return image1.getBitDepth() == image2.getBitDepth();
    }

    /**
     * Checks if the two input 3D images have the same data type.
     * 
     * @param image1
     *            the first image
     * @param image2
     *            the second image
     * @return true if both images have the same data type, given by the bit
     *         depth.
     */
    public static final boolean isSameType(ImageStack image1, ImageStack image2)
    {
        return image1.getBitDepth() == image2.getBitDepth();
    }
    
    /**
     * Fills a rectangle within the image with the specified value.
     * 
     * This will replace by the specified value all the pixels within image that
     * are located:
     * <ul>
     * <li>between <code>x0</code> (inclusive) and <code>x0+width-1</code>
     * (inclusive)</li>
     * <li>between <code>y0</code> (inclusive) and <code>y0+height-1</code>
     * (inclusive)</li>
     * </ul>
     * 
     * @param image
     *            the image to process.
     * @param x0
     *            the x-coordinate of the left corner of the rectangle to fill
     * @param y0
     *            the y-coordinate of the top corner of the rectangle to fill
     * @param w
     *            the width of the rectangle to fill, in pixels
     * @param h
     *            the height of the rectangle to fill, in pixels
     * @param value
     *            the value to fill the rectangle with
     */
    public static final void fillRect(ImageProcessor image, int x0, int y0, int w, int h, double value)
    {
        // retrieve image size for bounds check
        int width = image.getWidth();
        int height = image.getHeight();
        
        // fill rectangle
        for (int y = y0; y < Math.min(y0 + h, height); y++)
        {
            for (int x = x0; x < Math.min(x0 + w, width); x++)
            {
                image.setf(x, y, (float) value);
            }
        }
    }

    /**
     * Fills a 3D rectangle within the image with the specified value.
     * 
     * This will replace by the specified value all the pixels within image that
     * are located:
     * <ul>
     * <li>between <code>x0</code> (inclusive) and <code>x0+width-1</code>
     * (inclusive)</li>
     * <li>between <code>y0</code> (inclusive) and <code>y0+height-1</code>
     * (inclusive)</li>
     * <li>between <code>z0</code> (inclusive) and <code>z0+depth-1</code>
     * (inclusive)</li>
     * </ul>
     * 
     * @param image
     *            the image to process.
     * @param x0
     *            the x-coordinate of the left corner of the rectangle to fill
     * @param y0
     *            the y-coordinate of the top corner of the rectangle to fill
     * @param z0
     *            the z-coordinate of the front corner of the rectangle to fill
     * @param w
     *            the width of the rectangle to fill, in voxels
     * @param h
     *            the height of the rectangle to fill, in voxels
     * @param d
     *            the depth of the rectangle to fill, in voxels
     * @param value
     *            the value to fill the 3D rectangle with
     */
    public static final void fillRect3d(ImageStack image, int x0, int y0, int z0, int w, int h, int d, double value)
    {
        // retrieve image size for bounds check
        int width = image.getWidth();
        int height = image.getHeight();
        int depth = image.getSize();
        
        // fill 3D rectangle
        for (int z = z0; z < Math.min(z0 + d, depth); z++)
        {
            for (int y = y0; y < Math.min(y0 + h, height); y++)
            {
                for (int x = x0; x < Math.min(x0 + w, width); x++)
                {
                    image.setVoxel(x, y, z, value);
                }
            }
        }
    }

    /**
     * Computes maximum value within the input image.
     */
    public final static double findMaxValue(ImagePlus imagePlus)
    {
        if (imagePlus.getStackSize() > 1)
        {
            return findMaxValue(imagePlus.getStack());
        }
        else
        {
            return findMaxValue(imagePlus.getProcessor());
        }
    }
    
    /**
     * Computes maximum value within the input 2D image.
     * 
     * This method may be used to compute display range of result ImagePlus.
     */
    public final static double findMaxValue(ImageProcessor image) 
    {
        // get image size
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        
        // find maximum value over pixels
        double maxVal = 0;
        for (int y = 0; y < sizeY; y++) 
        {
            for (int x = 0; x < sizeX; x++) 
            {
                maxVal = Math.max(maxVal, image.getf(x, y));
            }
        }
        
        return maxVal;
    }

    /**
     * Computes maximum value in the input 3D image.
     * 
     * This method may be used to compute display range of result ImagePlus.
     */
    public final static double findMaxValue(ImageStack image) 
    {
        // get image size
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        int sizeZ = image.getSize();
    
        // find maximum value over voxels
        double maxVal = 0;
        for (int z = 0; z < sizeZ; z++) 
        {
            for (int y = 0; y < sizeY; y++) 
            {
                for (int x = 0; x < sizeX; x++) 
                {
                    maxVal = Math.max(maxVal, image.getVoxel(x, y, z));
                }
            }
        }
        
        return maxVal;
    }

    /**
     * Replaces the elements of an image with a given value by a new value.
     * 
     * @param image
     *            the image to process
     * @param initialValue
     *            the value of the elements to replace
     * @param finalValue
     *            the new value of the elements
     */
    public static final void replaceValue(ImagePlus image, double initialValue, double finalValue) 
    { 
        if (image.getStackSize() == 1) 
        {
            replaceValue(image.getProcessor(), initialValue, finalValue);
        } 
        else 
        {
            replaceValue(image.getStack(), initialValue, finalValue);
        }
    }

    /**
     * Replaces the elements of a 2D image with a given value by a new value.
     * 
     * @param image
     *            the 3D image to process
     * @param initialValue
     *            the value of the elements to replace
     * @param finalValue
     *            the new value of the elements
     */
    public static final void replaceValue(ImageProcessor image, double initialValue, double finalValue) 
    { 
        for (int y = 0; y < image.getHeight(); y++)
        {
            for (int x = 0; x < image.getWidth(); x++)
            {
                if (image.getf(x, y) == initialValue) 
                {
                    image.setf(x, y, (float) finalValue);
                }
            }
        }
    }

    /**
     * Replaces the elements of a 3D image with a given value by a new value.
     * 
     * @param image
     *            the 3D image to process
     * @param initialValue
     *            the value of the elements to replace
     * @param finalValue
     *            the new value of the elements
     */
    public static final void replaceValue(ImageStack image, double initialValue, double finalValue) 
    { 
        for (int z = 0; z < image.getSize(); z++)
        {
            for (int y = 0; y < image.getHeight(); y++)
            {
                for (int x = 0; x < image.getWidth(); x++)
                {
                    if (image.getVoxel(x, y, z) == initialValue) 
                    {
                        image.setVoxel(x, y, z, finalValue);
                    }
                }
            }
        }
    }

    /**
     * Prints the content of the given ImageProcessor on the console. This can be used
     * for debugging (small) images.
     * 
     * @param image the image to display on the console 
     */
    public static final void print(ImageProcessor image) 
    {
        for (int y = 0; y < image.getHeight(); y++)
        {
            for (int x = 0; x < image.getWidth(); x++)
            {
                System.out.print(String.format("%3d ", (int) image.getf(x, y)));
            }
            System.out.println("");
        }
    }
    
    /**
     * Prints the content of the given 3D image on the console. This can be used
     * for debugging (small) images.
     * 
     * @param image the 3D image to display on the console 
     */
    public static final void print(ImageStack image) 
    {
        int nSlices = image.getSize();
        for (int z = 0; z < nSlices; z++)
        {
            System.out.println(String.format("slice %d/%d", z, nSlices - 1));
            for (int y = 0; y < image.getHeight(); y++)
            {
                for (int x = 0; x < image.getWidth(); x++)
                {
                    System.out.print(String.format("%3d ", (int) image.getVoxel(x, y, z)));
                }
                System.out.println("");
            }
        }
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private ImageUtils()
    {
    }
}
