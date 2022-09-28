/**
 * 
 */
package inra.ijpb.morphology.strel;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.binary.distmap.ChamferMask2D;
import inra.ijpb.morphology.Strel;

/**
 * Disk-shaped structuring element based on a chamfer mask and a radius.
 *  
 * @author dlegland
 *
 */
public class ChamferStrel extends AbstractStrel
{
    // ==================================================
    // Class variables

    ChamferMask2D chamferMask;
    double radius;
    
    int[][] mask;
    
    
    // ==================================================
    // Constructors 
    
    /**
     * Creates a new ChamferStrel from a chamfer mask (used for computing
     * distances) and a radius (used to computing the extent of propagation).
     * 
     * @param chamferMask
     *            the chamfer mask
     * @param radius
     *            the radius
     */
    public ChamferStrel(ChamferMask2D chamferMask, double radius)
    {
        this.chamferMask = chamferMask;
        this.radius = radius;
        
        computeMask();
    }
    
    private void computeMask()
    {
        int[] size = getSize();
        ByteProcessor marker = new ByteProcessor(size[0], size[1]);
        marker.set(255);
        int[] offset = getOffset();
        marker.set(offset[0], offset[1], 0);
        ImageProcessor distMap = BinaryImages.distanceMap(marker, this.chamferMask, true, true);
        
        this.mask = new int[size[1]][size[0]];
        for (int y = 0; y < size[1]; y++)
        {
            for (int x = 0; x < size[0]; x++)
            {
                if (distMap.getf(x, y) < this.radius + 0.5)
                {
                    this.mask[y][x] = 255;
                }
            }
        }
    }

    
    // ==================================================
    // Implementation of Strel methods 
    
    @Override
    public int[] getSize()
    {
        int k = (int) Math.ceil(this.radius - 0.5);
        return new int[] {2 * k + 1, 2* k + 1};
    }

    @Override
    public int[][] getMask()
    {
        return this.mask;
    }

    @Override
    public int[] getOffset()
    {
        int k = (int) Math.ceil(this.radius - 0.5);
        return new int[] {k, k};
    }

    @Override
    public int[][] getShifts()
    {
        return convertMaskToShifts(getMask());
    }

    @Override
    public Strel reverse()
    {
        return this;
    }
}
