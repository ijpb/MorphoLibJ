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
 * Performs morphological dilation on 2D/3D images.
 * 
 * Dilation is obtained by extracting the maximum value among pixels/voxels
 * within the neighborhood given by the structuring element.
 * 
 * @see Erosion
 * @see Opening
 * @see Closing
 * @see Strel#dilation(ImageProcessor)
 */
public class Dilation extends MorphologicalFilter
{
    /**
     * Creates a new Morphological Dilation operator with the specified
     * structuring element.
     * 
     * @param strel
     *            the structuring element used for the operation, that can also
     *            be an instance of Strel.
     */
    public Dilation(Strel3D strel)
    {
        super(strel, "-dilation");
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

        ImageProcessor result = ((Strel) strel).dilation(image);
        result.setColorModel(image.getColorModel());
        return result;
    }
    
    @Override
    public ImageStack process(ImageStack image)
    {
        return strel.dilation(image);
    }
}
