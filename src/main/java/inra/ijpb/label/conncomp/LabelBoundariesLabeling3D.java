/**
 * 
 */
package inra.ijpb.label.conncomp;

import java.util.ArrayList;

import ij.ImageStack;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.label.LabelUtils;

/**
 * Computes a label map of the boundaries between regions from a label map.
 * 
 * The result is returned as a <code>Result</code> instance, that encloses the
 * boundary label map and the list of boundaries as a <code>BoundarySet</code>.
 * Each <code>Boundary</code> in the boundary set is identified by an integer
 * index, and contains the list of regions it is adjacent to.
 * 
 * @see Boundary
 * @see BoundarySet
 * @see LabelBoundariesLabeling2D
 * 
 * @author dlegland
 */
public class LabelBoundariesLabeling3D extends AlgoStub
{
    /**
     * Used to identify where to look for neighbors around a given pixel.
     */
    private static int[][] shiftsC6 = new int[][] {
        {0, 0, -1}, 
        {0, -1, 0}, {-1, 0, 0}, {+1, 0, 0}, {0, +1, 0},
        {0, 0, +1}, 
    };
    
    /**
     * Computes boundary labeling on the specified label map of regions, and
     * returns the result in a <code>Result</code> instance.
     * 
     * @param labelMap
     *            the label map of the regions
     * @return the result of labeling, enclosing the boundary label map and the
     *         list of boundaries.
     */
    public Result process(ImageStack labelMap)
    {
        // retrieve image size
        int sizeX = labelMap.getWidth();
        int sizeY = labelMap.getHeight();
        int sizeZ = labelMap.getSize();
        
        // allocate memory for boundary label map
        Result res = new Result(ImageStack.create(sizeX, sizeY, sizeZ, 32));
        
        int maxLabelCount = LabelUtils.getLargestPossibleLabel(res.boundaryLabelMap);
        
        // iterate over pixels
        for (int z = 0; z < sizeZ; z++)
        {
            fireProgressChanged(this, z, sizeZ);
            
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    // initialize current pixel
                    int currentLabel = (int) labelMap.getVoxel(x, y, z);
                    ArrayList<Integer> neighborLabels = new ArrayList<Integer>();
                    neighborLabels.add(currentLabel);

                    // iterate over neighbors
                    for (int[] shift : shiftsC6)
                    {
                        // compute neighbor coordinates
                        int x2 = x + shift[0];
                        int y2 = y + shift[1];
                        int z2 = z + shift[2];

                        // check bounds
                        if (x2 < 0 || x2 >= sizeX || y2 < 0 || y2 >= sizeY || z2 < 0 || z2 >= sizeZ)
                        {
                            continue;
                        }

                        int neighLabel = (int) labelMap.getVoxel(x2, y2, z2);
                        if (!neighborLabels.contains(neighLabel))
                        {
                            neighborLabels.add(neighLabel);
                        }
                    }

                    if (neighborLabels.size() == 1)
                    {
                        continue;
                    }

                    // try to find existing boundary
                    Boundary boundary = res.boundaries.findBoundary(neighborLabels);
                    
                    // if boundary does not exist, create a new one
                    if (boundary == null)
                    {
                        if (res.boundaries.size() >= maxLabelCount)
                        {
                            throw new RuntimeException("Max number of label reached (" + maxLabelCount + ")");
                        }
                        boundary = res.boundaries.createBoundary(neighborLabels);
                    }
                    
                    res.boundaryLabelMap.setVoxel(x, y, z, boundary.label);
                }
            }
        }
        fireProgressChanged(this, 1, 1);

        // return result data structure
        return res;
    }
    
    /**
     * Provides the result of a boundary labeling. Contains the label map, and
     * the set of boundaries as a <code>BoundarySet</code> instance.
     * 
     * For 2D images and default neighborhood, the number of adjacent regions
     * associated to boundaries may equal two, three (corner boundary), or in
     * some cases four ("square corner").
     */
    public class Result
    {
        /**
         * The label map containing boundary labels or zero for non-label pixels.
         */
        public final ImageStack boundaryLabelMap;
        
        /**
         * The map between the label of a boundary and the Boundary instances
         * that store indices of adjacent regions.
         */
        public final BoundarySet boundaries;
        
        /**
         * Initializes a new Result instance from a boundary label map.
         * 
         * @param labelMap
         *            the label map of boundaries.
         */
        public Result(ImageStack labelMap)
        {
            this.boundaryLabelMap = labelMap;
            this.boundaries = new BoundarySet();
        }
    }
}
