/**
 * 
 */
package inra.ijpb.label.filter;

import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.distmap.ChamferMask2D;
import inra.ijpb.morphology.Strel;
import inra.ijpb.morphology.strel.ChamferStrel;

/**
 * Implementation of morphological erosion for 2D label images / label maps.
 * 
 * Can be applied to label maps encoded with 8 or 16 bits integers, or 32 bit
 * floats.
 * 
 * 
 * @author dlegland
 *
 */
public class ChamferLabelErosion2DShort extends AlgoStub
{
    /**
     * The chamfer mask used to propagate distances.
     */
    ChamferMask2D mask;
    
    /**
     * The radius of dilation of labels. In practice, the distance is propagated
     * up to radius + 0.5.
     */
    double radius;
    
    /**
     * For erosion, we can work only with the structuring element.
     */
    Strel strel;
    
    /**
     * Creates a new operator for erosion of label images based on Chamfer
     * masks. The principle is to compute for each non-zero pixel (label), the
     * distance to the nearest background pixel, and to apply a threshold on
     * this distance map.
     * 
     * @param mask
     *            the Chamfer mask use to propagate distances
     * @param radius
     *            the radius used to compute erosion from distance map.
     */
    public ChamferLabelErosion2DShort(ChamferMask2D mask, double radius)
    {
        this.mask = mask;
        this.radius = radius;
        
        this.strel = new ChamferStrel(mask, radius);
    }
    
    /**
     * Apply morphological erosion of the labels within the specified input
     * image.
     * 
     * @param image
     *            the label map to erode
     * @return the result of erosion.
     */
    public ImageProcessor process(ImageProcessor image)
    {
        // retrieve image size
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        
        // allocate memory for output
        ImageProcessor res = image.createProcessor(sizeX, sizeY);
        
        // pre-compute strel shifts
        int[][] shifts = strel.getShifts();
        
        // iterate over pixels within output image
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                int label = (int) image.getf(x, y);
                
                // no need to process background pixels
                if (label == 0)
                {
                    continue;
                }
                
                // iterate over neighbors defined by strel
                for (int[] shift : shifts)
                {
                    // position of neighbor
                    int x2 = x + shift[0];
                    int y2 = y + shift[1];
                    
                    // do not process pixels outside of image bounds
                    if (x2 < 0 || x2 >= sizeX) continue;
                    if (y2 < 0 || y2 >= sizeY) continue;
                    
                    // check if neighbor is background
                    int label2 = (int) image.getf(x2, y2);
                    if (label2 == 0)
                    {
                        label = 0;
                        break;
                    }
                }
                
                res.setf(x, y, label);
            }
        }
        
        return res;
    }
}
