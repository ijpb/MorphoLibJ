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
 * Computes the morphological internal gradient of the input image.
 * 
 * The morphological internal gradient is obtained by from the difference 
 * of original image with the result of an erosion.
 * 
 * The black top hat enhances dark structures smaller than the structuring
 * element.
 * 
 * @see Erosion
 * @see Dilation
 * @see Gradient
 * @see ExternalGradient
 */
public class InternalGradient extends MorphologicalFilter
{
    /**
     * Creates a new Internal Gradient operator with the specified structuring
     * element.
     * 
     * @param strel
     *            the structuring element used for the operation, that can also
     *            be an instance of Strel.
     */
    public InternalGradient(Strel3D strel)
    {
        super(strel, "-internalGradient");
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

        // First performs dilation and erosion
        ImageProcessor result = new Erosion(strel).process(image);

        // Subtract erosion from dilation
        int count = image.getPixelCount();
        if (image instanceof ByteProcessor)
        {
            for (int i = 0; i < count; i++) 
            {
                // Forces computation using integers, because opening with 
                // octagons can be greater than original image (bug)
                int v1 = image.get(i);
                int v2 = result.get(i);
                result.set(i, clamp(v1 - v2, 0, 255));
            }
        } 
        else 
        {
            for (int i = 0; i < count; i++)
            {
                float v1 = image.getf(i);
                float v2 = result.getf(i);
                result.setf(i, v1 - v2);
            }
        }
        
        // return gradient
        result.setColorModel(image.getColorModel());
        return result;
    }
    
    @Override
    public ImageStack process(ImageStack image)
    {
        // First performs dilation and erosion
        ImageStack result = new Erosion(strel).process(image);
        
        // Determine max possible value from bit depth
        double maxVal = getMaxPossibleValue(image);

        // Compute subtraction of result from original image
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        int sizeZ = image.getSize();
        for (int z = 0; z < sizeZ; z++) 
        {
            for (int y = 0; y < sizeY; y++) 
            {
                for (int x = 0; x < sizeX; x++) 
                {
                    double v1 = image.getVoxel(x, y, z);
                    double v2 = result.getVoxel(x, y, z);
                    result.setVoxel(x, y, z, min(max(v1 - v2, 0), maxVal));
                }
            }
        }
        
        // return gradient
        result.setColorModel(image.getColorModel());
        return result;
    }
}
