/**
 * 
 */
package inra.ijpb.label.filter;

import ij.ImageStack;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.distmap.ChamferMask3D;
import inra.ijpb.morphology.Strel3D;
import inra.ijpb.morphology.strel.ChamferStrel3D;

/**
 * Implementation of morphological erosion for 3D label images / label maps.
 * 
 * Can be applied to label maps encoded with 8 or 16 bits integers, or 32 bit
 * floats.
 * 
 * 
 * @author dlegland
 *
 */
public class ChamferLabelErosion3DShort extends AlgoStub
{
    /**
     * The chamfer mask used to propagate distances.
     */
    ChamferMask3D mask;
    
    /**
     * The radius of dilation of labels. In practice, the distance is propagated
     * up to radius + 0.5.
     */
    double radius;
    
    /**
     * For erosion, we can work only with the structuring element.
     */
    Strel3D strel;
    
    public ChamferLabelErosion3DShort(ChamferMask3D mask, double radius)
    {
        this.mask = mask;
        this.radius = radius;
        
        this.strel = new ChamferStrel3D(mask, radius);
    }
    
    public ImageStack process(ImageStack image)
    {
        // retrieve image size
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        int sizeZ = image.getSize();
        
        // allocate memory for output
        ImageStack res = ImageStack.create(sizeX, sizeY, sizeZ, image.getBitDepth());
        
        // pre-compute strel shifts
        int[][] shifts = strel.getShifts3D();
        
        // iterate over pixels within output image
        for (int z = 0; z < sizeZ; z++)
        {
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    int label = (int) image.getVoxel(x, y, z);
                    
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
                        int z2 = z + shift[2];
                        
                        // do not process pixels outside of image bounds
                        if (x2 < 0 || x2 >= sizeX) continue;
                        if (y2 < 0 || y2 >= sizeY) continue;
                        if (z2 < 0 || z2 >= sizeZ) continue;
                        
                        // check if neighbor is background
                        int label2 = (int) image.getVoxel(x2, y2, z2);
                        if (label2 == 0)
                        {
                            label = 0;
                            break;
                        }
                    }
                    
                    res.setVoxel(x, y, z, label);
                }
            }
        }
        return res;
    }
}
