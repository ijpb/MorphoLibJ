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
 * Computes the morphological Laplacian of the input image.
 * 
 * The morphological Laplacian is obtained from the difference of the external
 * gradient with the internal gradient, both computed with the same structuring
 * element.
 * 
 * The black top hat enhances dark structures smaller than the structuring
 * element.
 * 
 * @see Erosion
 * @see Dilation
 * @see Gradient
 * @see ExternalGradient
 * @see InternalGradient
 */
public class Laplacian extends MorphologicalFilter
{
    /**
     * Creates a new Morphological Laplacian operator with the specified structuring
     * element.
     * 
     * @param strel
     *            the structuring element used for the operation, that can also
     *            be an instance of Strel.
     */
    public Laplacian(Strel3D strel)
    {
        super(strel, "-laplacian");
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
        ImageProcessor outer = new ExternalGradient(strel).process(image);
        ImageProcessor inner = new InternalGradient(strel).process(image);

        // Subtract erosion from dilation
        int count = image.getPixelCount();
        if (image instanceof ByteProcessor)
        {
            for (int i = 0; i < count; i++) 
            {
                // Forces computation using integers, because opening with 
                // octagons can be greater than original image (bug)
                int v1 = outer.get(i);
                int v2 = inner.get(i);
                outer.set(i, clamp(v1 - v2 + 128, 0, 255)); // TODO: 16 bits?
            }
        } 
        else 
        {
            for (int i = 0; i < count; i++)
            {
                float v1 = outer.getf(i);
                float v2 = inner.getf(i);
                outer.setf(i, v1 - v2);
            }
        }
        
        // free memory
        inner = null;
        
        // return laplacian
        outer.setColorModel(image.getColorModel());
        return outer;
    }
    
    @Override
    public ImageStack process(ImageStack image)
    {
        // First performs dilation and erosion
        ImageStack outer = new ExternalGradient(strel).process(image);
        ImageStack inner = new InternalGradient(strel).process(image);
        
        // Determine max possible value from bit depth
        double maxVal = getMaxPossibleValue(image);
        double midVal = maxVal / 2;
        if (image.getBitDepth() == 32)
        {
            midVal = 0.0;
        }
        
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
                    double v1 = outer.getVoxel(x, y, z);
                    double v2 = inner.getVoxel(x, y, z);
                    outer.setVoxel(x, y, z, min(max(v1 - v2 + midVal, 0), maxVal));
                }
            }
        }
        
        // free memory
        inner = null;
        
        // return gradient
        outer.setColorModel(image.getColorModel());
        return outer;
    }
}
