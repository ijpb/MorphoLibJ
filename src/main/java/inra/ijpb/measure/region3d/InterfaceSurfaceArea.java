/**
 * 
 */
package inra.ijpb.measure.region3d;

import ij.ImageStack;
import ij.measure.Calibration;

/**
 * Computes the surface area of the interface between two labels.
 * 
 * @author dlegland
 *
 */
public class InterfaceSurfaceArea
{
    /**
     * Return the contribution of each possible intercept.
     *
     * The resulting array has seven values, corresponding to the seven possible
     * neighbor voxel within a 2-by-2-by-2 configuration:
     * <ul>
     * <li>the intercept value in the X direction</li>
     * <li>the intercept value in the Y direction</li>
     * <li>the intercept value in the Z direction</li>
     * <li>the intercept value in the XY direction</li>
     * <li>the intercept value in the XZ direction</li>
     * <li>the intercept value in the YZ direction</li>
     * <li>the intercept value in the XYZ direction (cube diagonal)</li>
     * </ul>
     * 
     * @param calib
     *            the spatial calibration of the image
     * @param nDirs
     *            the number of directions to consider, should be either 3 or
     *            13.
     * @return the contribution of each possible intercept.
     */
    private final static double[] interceptsContributions(Calibration calib, int nDirs)
    {
        if (nDirs == 3)
        {
            return interceptsContributionsD3(calib);
        }
        else if (nDirs == 13)
        {
            return interceptsContributionsD13(calib);
        }
        else
        {
            throw new IllegalArgumentException("Direction number mus be either 3 or 13");
        }
    }
    
    private final static double[] interceptsContributionsD3(Calibration calib)
    {
        double c1 = 1.0 / 3.0;
        return new double[] { c1, c1, c1, 0.0, 0.0, 0.0, 0.0 };
    }
    
    private final static double[] interceptsContributionsD13(Calibration calib)
    {
        // distances between a voxel and its neighbors.
        // di refer to orthogonal neighbors
        // dij refer to neighbors on the same plane 
        // dijk refer to the opposite voxel in a tile
        double d1 = calib.pixelWidth;
        double d2 = calib.pixelHeight;
        double d3 = calib.pixelDepth;
        double vol = d1 * d2 * d3;
        
        double d12 = Math.hypot(d1, d2);
        double d13 = Math.hypot(d1, d3);
        double d23 = Math.hypot(d2, d3);
        double d123= Math.hypot(d12, d3);
    
        // direction weights corresponding to area of Voronoi partition on the
        // unit sphere, when germs are the 26 directions on the unit cube
        // Sum of (c1+c2+c3 + c4*2+c5*2+c6*2 + c7*4) equals 1.
        double[] weights = IntrinsicVolumes3DUtils.directionWeights3d13(calib);
        
        // contribution to number of intersections for each direction
        double[] kei = new double[7];
        
        kei[0] = 4 * weights[0] * vol / d1   / 8.0;
        kei[1] = 4 * weights[1] * vol / d2   / 8.0;
        kei[2] = 4 * weights[2] * vol / d3   / 8.0;
        kei[3] = 4 * weights[3] * vol / d12  / 4.0;
        kei[4] = 4 * weights[4] * vol / d13  / 4.0;
        kei[5] = 4 * weights[5] * vol / d23  / 4.0;
        kei[6] = 4 * weights[6] * vol / d123 / 2.0; 

        return kei;     
    }

    
    // ==================================================
    // Class members

    /**
     * Number of directions for computing surface area or mean breadth with
     * Crofton Formula. Default is 13.
     */
    int directionNumber = 13;

    // ==================================================
    // Constructors

    /**
     * Default empty constructor.
     */
    public InterfaceSurfaceArea()
    {
    }

    /**
     * Constructor that specifies the number of directions to use.
     * 
     * @param nDirs the number of directions to use (either 3 or 13).
     */
    public InterfaceSurfaceArea(int nDirs)
    {
        setDirectionNumber(nDirs);
    }

    /**
     * Specifies the number of directions to use.
     * 
     * @param nDirs the number of directions to use (either 3 or 13).
     */
    public void setDirectionNumber(int nDirs)
    {
        if (nDirs == 3 || nDirs == 13)
        {
            this.directionNumber = nDirs;
        }
        else
        {
            throw new IllegalArgumentException("Number of directions should be either 3 or 13.");
        }
    }
    
    /**
	 * Computes the interface surface area between two regions.
	 * 
	 * @param stack
	 *            the 3D input image
	 * @param label1
	 *            the label of the first region
	 * @param label2
	 *            the label of the second region
	 * @param calib
	 *            the spatial calibration of image
	 * @return the surface area of the interface between the two regions
	 */
    public double process(ImageStack stack, int label1, int label2, Calibration calib)
    {
        // coordinates of voxels within tile
        int[][] posList = new int[][] {
            {0, 0, 0},     
            {1, 0, 0},     
            {0, 1, 0},     
            {1, 1, 0},     
            {0, 0, 1},     
            {1, 0, 1},     
            {0, 1, 1},     
            {1, 1, 1},     
        };

        // flags indicating for each neighbor within tile and whether each
        // coordinate should be mirrored within the tile
        int[][] shifts = new int[][] {
            {1, 0, 0}, 
            {0, 1, 0}, 
            {0, 0, 1}, 
            {1, 1, 0}, 
            {1, 0, 1}, 
            {0, 1, 1}, 
            {1, 1, 1}, 
        };
        
        // pre-compute contributions.
        double[] contribs = interceptsContributions(calib, this.directionNumber);
        int nNeighs = this.directionNumber == 3 ? 3 : 7;
        
        // get image dimensions
        int sizeX = stack.getWidth();
        int sizeY = stack.getHeight();
        int sizeZ = stack.getSize();
        
        double value = 0.0;
        
        // iterate over top-left-back corners of 2-by-2-by-2 configurations
        for (int z = 0; z < sizeZ - 1; z++)
        {
            for (int y = 0; y < sizeY - 1; y++)
            {
                for (int x = 0; x < sizeX - 1; x++)
                {
                    // Iterate over voxel within configuration
                    for (int iPos = 0; iPos < 8; iPos++)
                    {
                        // compute absolute position of current voxel
                        int[] pos = posList[iPos];
                        int x1 = x + pos[0];
                        int y1 = y + pos[1];
                        int z1 = z + pos[2];
                        int v1 = (int) stack.getVoxel(x1, y1, z1);
                        if (v1 != label1)
                        {
                            continue;
                        }

                        // iterate over neighbors of current voxel within tile
                        for (int iNeigh = 0; iNeigh < nNeighs; iNeigh++)
                        {
                            // compute absolute position of neighbor
                            int[] shift = shifts[iNeigh];
                            int x2 = shift[0] > 0 ? x + (1 - pos[0]) : x1; 
                            int y2 = shift[1] > 0 ? y + (1 - pos[1]) : y1; 
                            int z2 = shift[2] > 0 ? z + (1 - pos[2]) : z1;
                            
                            // value of neighbor
                            int v2 = (int) stack.getVoxel(x2, y2, z2);
                            if (v2 != label2)
                            {
                                continue;
                            }
                            
                            // count a transition
                            value += contribs[iNeigh];
                        }
                    }
                }
            }
        }
            
        return value;
    }
}
