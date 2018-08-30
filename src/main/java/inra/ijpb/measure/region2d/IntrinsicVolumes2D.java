/**
 * 
 */
package inra.ijpb.measure.region2d;

import java.util.Map;

import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;

/**
 * Computation of intrinsic volumes (area, perimeter and Euler number) for
 * binary or label 2D images.
 *
 * @author dlegland
 *
 */
public class IntrinsicVolumes2D extends RegionAnalyzer2D<IntrinsicVolumes2D.Result>
{
    // ==================================================
    // Static methods

    public static final double[] areaLut(Calibration calib)
    {
        // base LUT
        double[] lut = new double[] {0, 0.25, 0.25, 0.5,  0.25, 0.5, 0.5, 0.75,   0.25, 0.5, 0.5, 0.75,   0.5, 0.75, 0.75, 1.0};
        
        // take into account spatial calibration
        double pixelArea = calib.pixelWidth * calib.pixelHeight;
        for (int i = 0; i < lut.length; i++)
        {
            lut[i] *= pixelArea;
        }
        
        return lut;
    }
    
    /**
     * Computes the Look-up table that is used to compute perimeter. The result
     * is an array with 16 entries, each entry corresponding to a binary 2-by-2
     * configuration of pixels.
     * 
     * @param calib
     *            the calibration of the image
     * @param nDirs
     *            the number of directions to use (2 or 4)
     * @return an array containing for each 2-by-2 configuration index, the
     *         corresponding contribution to perimeter estimate
     */
    public static final double[] perimeterLut(Calibration calib, int nDirs)
    {
        // distances between a pixel and its neighbors.
        // di refer to orthogonal neighbors
        // dij refer to diagonal neighbors
        double d1 = calib.pixelWidth;
        double d2 = calib.pixelHeight;
        double d12 = Math.hypot(d1, d2);
        double area = d1 * d2;

        // weights associated to each direction, computed only for four
        // directions
        double[] weights = null;
        if (nDirs == 4)
        {
            weights = computeDirectionWeightsD4(d1, d2);
        }

        // initialize output array (2^(2*2) = 16 configurations in 2D)
        final int nConfigs = 16;
        double[] tab = new double[nConfigs];

        // loop for each tile configuration
        for (int i = 0; i < nConfigs; i++)
        {
            // create the binary image representing the 2x2 tile
            boolean[][] im = new boolean[2][2];
            im[0][0] = (i & 1) > 0;
            im[0][1] = (i & 2) > 0;
            im[1][0] = (i & 4) > 0;
            im[1][1] = (i & 8) > 0;

            // contributions for isothetic directions
            double ke1, ke2;

            // contributions for diagonal directions
            double ke12;

            // iterate over the 4 pixels within the configuration
            for (int y = 0; y < 2; y++)
            {
                for (int x = 0; x < 2; x++)
                {
                    if (!im[y][x])
                        continue;

                    // divides by two to convert intersection count to projected
                    // diameter
                    ke1 = im[y][1 - x] ? 0 : (area / d1) / 2;
                    ke2 = im[1 - y][x] ? 0 : (area / d2) / 2;

                    if (nDirs == 2) 
                    {
                        // Count only orthogonal directions
                        // divides by two for average, and by two for
                        // multiplicity
                        tab[i] += (ke1 + ke2) / 4;

                    } 
                    else if (nDirs == 4) 
                    {
                        // compute contribution of diagonal directions
                        ke12 = im[1 - y][1 - x] ? 0 : (area / d12) / 2;

                        // Decomposition of Crofton formula on 4 directions,
                        // taking into account multiplicities
                        tab[i] += ((ke1 / 2) * weights[0] + (ke2 / 2)
                                * weights[1] + ke12 * weights[2]);
                    }
                }
            }

            // Add a normalisation constant
            tab[i] *= Math.PI;
        }

        return tab;
    }

    /**
     * Computes a set of weights for the four main directions (orthogonal plus
     * diagonal) in discrete image. The sum of the weights equals 1.
     * 
     * @param dx
     *            the spatial calibration in the x direction
     * @param dy
     *            the spatial calibration in the y direction
     * @return the set of normalized weights
     */
    private static final double[] computeDirectionWeightsD4(double dx, double dy) 
    {
        // angle of the diagonal
        double theta = Math.atan2(dy, dx);

        // angular sector for direction 1 ([1 0])
        double alpha1 = theta / Math.PI;

        // angular sector for direction 2 ([0 1])
        double alpha2 = (Math.PI / 2.0 - theta) / Math.PI;

        // angular sector for directions 3 and 4 ([1 1] and [-1 1])
        double alpha34 = .25;

        // concatenate the different weights
        return new double[] { alpha1, alpha2, alpha34, alpha34 };
    }
    
    /**
     * Computes the Look-up table that is used to compute Euler number density.
     * The result is an array with 16 entries, each entry corresponding to a
     * binary 2-by-2 configuration of pixels.
     * 
     * @param conn
     *            the connectivity to use (4 or 8)
     * @return an array containing for each 2-by-2 configuration index, the
     *         corresponding contribution to euler number estimate
     */
    public static final double[] eulerNumberLut(int conn)
    {
        switch(conn)
        {
        case 4:
            return eulerNumberLutC4();
        case 8:
            return eulerNumberLutC8();
        default:
            throw new IllegalArgumentException("Connectivity must be 4 or 8, not " + conn);
        }
    }
    
    private final static double[] eulerNumberLutC4()
    {
        return new double[] {
                0,  0.25, 0.25, 0,    0.25, 0, 0.5, -0.25,  
                0.25, 0.5, 0, -0.25,   0, -0.25, -0.25, 0};
    }

    private final static double[] eulerNumberLutC8()
    {
        return new double[] {
                0,  0.25, 0.25, 0,    0.25, 0, -0.5, -0.25,  
                0.25, -0.5, 0, -0.25,   0, -0.25, -0.25, 0};
    }
    
    // ==================================================
    // Class members

    // TODO: add getter/setters
    /**
     * Number of directions for computing perimeter with Crofton Formula. Default is 4.
     */
    int directionNumber = 4;

    /**
     * Connectivity for computing Euler number. Default is 4. 
     */
    int connectivity = 4;
   
    
    // ==================================================
    // Constructors

    public IntrinsicVolumes2D()
    {
    }
    
    
    // ==================================================
    // General methods

    @Override
    public ResultsTable createTable(Map<Integer, IntrinsicVolumes2D.Result> results)
    {
        // Initialize a new result table
        ResultsTable table = new ResultsTable();
    
        // Convert all results that were computed during execution of the
        // "computeGeodesicDistanceMap()" method into rows of the results table
        for (int label : results.keySet())
        {
            // current diameter
            Result res = results.get(label);
            
            // add an entry to the resulting data table
            table.incrementCounter();
            table.addLabel(Integer.toString(label));

            // add each measure
            table.addValue("Area", res.area);
            table.addValue("Perimeter", res.perimeter);
            table.addValue("EulerNumber", res.eulerNumber);
        }
    
        return table;
    }

    @Override
    public IntrinsicVolumes2D.Result[] analyzeRegions(ImageProcessor image, int[] labels,
            Calibration calib)
    {
        // Compute LUTs for each parameter
        double[] areaLut = areaLut(calib);
        double[] perimLut = perimeterLut(calib, this.directionNumber);
        double[] eulerLut = eulerNumberLut(this.connectivity);
                
        // Histogram of binary configurations for each region label
        // TODO: process algo events
        int[][] histograms = new BinaryConfigurationsHistogram2D().process(image, labels);
        
        // Compute values 
        double[] areas = BinaryConfigurationsHistogram2D.applyLut(histograms, areaLut);
        double[] perims = BinaryConfigurationsHistogram2D.applyLut(histograms, perimLut);
        double[] eulers = BinaryConfigurationsHistogram2D.applyLut(histograms, eulerLut);
        
        // convert to an array of Result classes
        Result[] results = new Result[labels.length];
        for (int i = 0; i < labels.length; i++)
        {
            results[i] = new Result(areas[i], perims[i], eulers[i]);
        }
        
        return results;
    }

    
 
    // ==================================================
    // Inner class for storing results
    
    public class Result
    {
        double area = Double.NaN;
        double perimeter = Double.NaN;
        double eulerNumber = Double.NaN;
        
        public Result(double area, double perim, double euler)
        {
            this.area = area;
            this.perimeter = perim;
            this.eulerNumber = euler;
        }
    }
}
