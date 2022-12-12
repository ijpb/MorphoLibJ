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
 * Performs morphological erosion on 2D/3D images.
 * 
 * Erosion is obtained by extracting the minimum value among pixels/voxels
 * within the neighborhood given by the structuring element.
 * 
 * @see Dilation
 * @see Opening
 * @see Closing
 * @see Strel#erosion(ImageProcessor)
 */
public class Erosion extends MorphologicalFilter
{
    /**
     * Creates a new Morphological Erosion operator with the specified
     * structuring element.
     * 
     * @param strel
     *            the structuring element used for the operation, that can also
     *            be an instance of Strel.
     */
    public Erosion(Strel3D strel)
    {
        super(strel, "-erosion");
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

        ImageProcessor result = ((Strel) strel).erosion(image);
        result.setColorModel(image.getColorModel());
        return result;
    }
    
    @Override
    public ImageStack process(ImageStack image)
    {
        return strel.erosion(image);
    }
}
