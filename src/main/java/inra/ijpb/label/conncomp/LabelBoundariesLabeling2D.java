/**
 * 
 */
package inra.ijpb.label.conncomp;

import java.util.ArrayList;

import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;

/**
 * Computes a label map of the boundaries between regions from a label map.
 * 
 * @author dlegland
 */
public class LabelBoundariesLabeling2D extends AlgoStub
{
    private static int[][] shiftsC4 = new int[][] {
        {0, -1}, {-1, 0}, {+1, 0}, {0, +1}
    };
    
    public LabelBoundariesLabeling2D()
    {
    }
    
    public Result process(ImageProcessor labelMap)
    {
        // retrieve image size
        int sizeX = labelMap.getWidth();
        int sizeY = labelMap.getHeight();
        
        // allocate memory for boundary label map
        Result res = new Result(new FloatProcessor(sizeX, sizeY));
        
        // iterate over pixels
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                // initialize current pixel
                int currentLabel = (int) labelMap.getf(x, y);
                ArrayList<Integer> neighborLabels = new ArrayList<Integer>();
                neighborLabels.add(currentLabel);
                
                // iterate over neighbors
                for (int[] shift : shiftsC4)
                {
                    // compute neighbor coordinates
                    int x2 = x + shift[0];
                    int y2 = y + shift[1];
                    
                    // check bounds
                    if (x2 < 0 || x2 >= sizeX || y2 < 0 || y2 >= sizeY)
                    {
                        continue;
                    }
                    
                    int neighLabel = (int) labelMap.getf(x2, y2);
                    if (!neighborLabels.contains(neighLabel))
                    {
                        neighborLabels.add(neighLabel);
                    }
                }
                
                if (neighborLabels.size() == 1)
                {
                    continue;
                }
                
                Boundary boundary = res.boundaries.findOrCreateBoundary(neighborLabels);
                res.boundaryLabelMap.setf(x, y, boundary.label);
            }
        }
        
        // return result data structure
        return res;
    }
    
    public class Result
    {
        /**
         * The label map containing boundary labels or zero for non-label pixels.
         */
        public final ImageProcessor boundaryLabelMap;
        
        /**
         * The map between the label of a boundary and the Boundary instances
         * that store indices of adjacent regions.
         */
        public final BoundarySet boundaries;
        
        public Result(ImageProcessor labelMap)
        {
            this.boundaryLabelMap = labelMap;
            this.boundaries = new BoundarySet();
        }
    }
}
