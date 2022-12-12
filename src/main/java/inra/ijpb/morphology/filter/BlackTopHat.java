/**
 * 
 */
package inra.ijpb.morphology.filter;

import static java.lang.Math.max;
import static java.lang.Math.min;

import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Strel;
import inra.ijpb.morphology.Strel3D;

/**
 * Performs morphological black top-hat on 2D/3D images.
 * 
 * Computes black top hat (or "bottom hat") of the original image. The black top
 * hat is obtained by subtracting the original image from the result of a
 * closing.
 * 
 * The black top hat enhances dark structures smaller than the structuring
 * element.
 * 
 * @see Erosion
 * @see Dilation
 * @see Closing
 * @see Strel#opening(ImageProcessor)
 */
public class BlackTopHat extends MorphologicalFilter
{
    /**
     * Creates a new Black Top-Hat operator with the specified structuring
     * element.
     * 
     * @param strel
     *            the structuring element used for the operation, that can also
     *            be an instance of Strel.
     */
    public BlackTopHat(Strel3D strel)
    {
        super(strel, "-blackTopHat");
    }
    
    @Override
    public ImageProcessor process(ImageProcessor image)
    {
        // check-up strel dimensionality
        if (!(strel instanceof Strel))
        {
            throw new RuntimeException("Processing 2D image requires a 2D strel");
        }
        
        // check case of color images
        if (image instanceof ColorProcessor)
        {
            return this.processColor((ColorProcessor) image);
        }

        // First performs closing
        ImageProcessor result = new Closing(strel).process(image);
        
        // Compute subtraction of result from original image
        int count = image.getPixelCount();
        if (image instanceof ByteProcessor) 
        {
            for (int i = 0; i < count; i++)
            {
                // Forces computation using integers, because closing with 
                // octagons can lower than than original image (bug)
                int v1 = result.get(i);
                int v2 = image.get(i);
                result.set(i, clamp(v1 - v2, 0, 255));
            }
        } 
        else 
        {
            for (int i = 0; i < count; i++)
            {
                float v1 = result.getf(i);
                float v2 = image.getf(i);
                result.setf(i, v1 - v2);
            }
        }

        result.setColorModel(image.getColorModel());
        return result;
    }
    
    @Override
    public ImageStack process(ImageStack image)
    {
        // First performs closing
        ImageStack result = new Closing(strel).process(image);
        
        // Compute subtraction of result from original image
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        int sizeZ = image.getSize();
        
        double maxVal = getMaxPossibleValue(image);
        for (int z = 0; z < sizeZ; z++)
        {
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    double v1 = result.getVoxel(x, y, z);
                    double v2 = image.getVoxel(x, y, z);
                    result.setVoxel(x, y, z, min(max(v1 - v2, 0), maxVal));
                }
            }
        }
        
        return result;
    }
}
