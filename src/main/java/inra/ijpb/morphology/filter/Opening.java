/**
 * 
 */
package inra.ijpb.morphology.filter;

import ij.ImageStack;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Strel;
import inra.ijpb.morphology.Strel3D;

/**
 * Performs morphological opening on 2D/3D images.
 * 
 * The opening is obtained by performing an erosion followed by a dilation
 * with the reversed structuring element.
 * 
 * @see Erosion
 * @see Dilation
 * @see Closing
 * @see Strel#opening(ImageProcessor)
 */
public class Opening extends MorphologicalFilter
{
    /**
     * Creates a new Morphological Opening operator with the specified
     * structuring element.
     * 
     * @param strel
     *            the structuring element used for the operation, that can also
     *            be an instance of Strel.
     */
    public Opening(Strel3D strel)
    {
        super(strel, "-opening");
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

        ImageProcessor result = ((Strel) strel).opening(image);
        result.setColorModel(image.getColorModel());
        return result;
    }
    
    @Override
    public ImageStack process(ImageStack image)
    {
        return strel.opening(image);
    }
}
