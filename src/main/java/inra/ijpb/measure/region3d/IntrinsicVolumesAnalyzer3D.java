/**
 * 
 */
package inra.ijpb.measure.region3d;

import java.util.Map;

import ij.ImageStack;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import inra.ijpb.algo.AlgoEvent;
import inra.ijpb.algo.AlgoListener;

/**
 * Computation of intrinsic volumes (Volume, Surface Area, Mean Breadth and
 * Euler Number) for 3D binary or label images, based on the
 * <code>RegionAnalyzer3D</code> interface.
 * 
 * The <code>IntrinsicVolumes3D</code> class provides static classes to
 * facilitate usage when no algorithm monitoring is necessary.
 * 
 * @see inra.ijpb.measure.IntrinsicVolumes3D
 * @see inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D
 * 
 * @author dlegland
 *
 */
public class IntrinsicVolumesAnalyzer3D extends RegionAnalyzer3D<IntrinsicVolumesAnalyzer3D.Result>
        implements AlgoListener
{
    // ==================================================
    // Static methods

    /**
     * @deprecated use {@link IntrinsicVolumes3DUtils} instead
     * @param calib
     *            spatial calibration
     * @return volume LUT
     */
    @Deprecated
    public static final double[] volumeLut(Calibration calib)
    {
        return IntrinsicVolumes3DUtils.volumeLut(calib);
    }

    /**
     * Computes the Look-up table that is used to compute surface area.
     * 
     * @deprecated use {@link IntrinsicVolumes3DUtils}  instead
     * 
     * @param calib
     *            the spatial calibration of the image
     * @param nDirs
     *            the number of directions to consider, either 3 or 13
     * @return the look-up-table between binary voxel configuration index and
     *         contribution to surface area measure
     */
    @Deprecated
    public final static double[] surfaceAreaLut(Calibration calib, int nDirs) 
    {
        return IntrinsicVolumes3DUtils.surfaceAreaLut(calib, nDirs);
    }

    /**
     * Computes the Look-up table used to measure mean breadth within 3D images.
     * 
     * @deprecated use {@link IntrinsicVolumes3DUtils}  instead
     * 
     * @param calib
     *            the spatial calibration of image
     * @param nDirs
     *            the number of directions (3 or 13)
     * @param conn2d
     *            the connectivity to use on square faces of plane sections (4 or 8)
     * @return a look-up table with 256 entries
     */
    @Deprecated
    public static final double[] meanBreadthLut(Calibration calib, int nDirs, int conn2d)
    {
        return IntrinsicVolumes3DUtils.meanBreadthLut(calib, nDirs, conn2d);
    }
    
    /**
     * Computes the look-up table for measuring Euler number in binary 3D image,
     * depending on the connectivity. The input structure should not touch image
     * border.
     * 
     * See "3D Images of Material Structures", from J. Ohser and K. Schladitz,
     * Wiley 2009, tables 3.2 p. 52 and 3.3 p. 53.
     * 
     * @deprecated use {@link IntrinsicVolumes3DUtils}  instead
     * 
     * @param conn
     *            the 3D connectivity, either 6 or 26
     * @return a look-up-table with 256 entries
     */
    @Deprecated
    public static final double[] eulerNumberLut(int conn)
    {
        return IntrinsicVolumes3DUtils.eulerNumberLut(conn);
    }


    // ==================================================
    // Class members

    boolean computeVolume = true;
    boolean computeSurfaceArea = true;
    boolean computeMeanBreadth = true;
    boolean computeEulerNumber = true;
    
    /**
     * Number of directions for computing surface area or mean breadth with
     * Crofton Formula. Default is 13.
     */
    int directionNumber = 13;

    /**
     * Connectivity for computing 3D Euler number. Default is 6. 
     */
    int connectivity = 6;
   
    
    // ==================================================
    // Constructors

    /**
     * Default empty constructor.
     */
    public IntrinsicVolumesAnalyzer3D()
    {
    }
    
    
    // ==================================================
    // Implementation of RegionAnalyzer3D methods

    /**
     * @return the directionNumber used to compute surface area and mean breadth
     */
    public int getDirectionNumber()
    {
        return directionNumber;
    }

    /**
     * @param directionNumber
     *            the number of directions used to compute surface area and mean
     *            breadth (either 3 or 13, default is 13)
     */
    public void setDirectionNumber(int directionNumber)
    {
        this.directionNumber = directionNumber;
    }

    /**
     * @return the connectivity used to compute Euler number
     */
    public int getConnectivity()
    {
        return connectivity;
    }

    /**
     * @param connectivity
     *            the connectivity used to compute Euler number (either 6 or 26,
     *            default is 6)
     */
    public void setConnectivity(int connectivity)
    {
        this.connectivity = connectivity;
    }


    // ==================================================
    // Implementation of RegionAnalyzer3D methods

    @Override
    public ResultsTable createTable(Map<Integer, Result> results)
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
            table.addValue("Volume", res.volume);
            table.addValue("SurfaceArea", res.surfaceArea);
            table.addValue("MeanBreadth", res.meanBreadth);
            table.addValue("EulerNumber", res.eulerNumber);
        }
    
        return table;
    }

    @Override
    public Result[] analyzeRegions(ImageStack image, int[] labels, Calibration calib)
    {
        // Histogram of binary configurations for each region label
        BinaryConfigurationsHistogram3D algo = new BinaryConfigurationsHistogram3D();
        algo.addAlgoListener(this);
        int[][] histograms = algo.process(image, labels);

        // initialize result array
        Result[] results = new Result[labels.length];
        for (int i = 0; i < labels.length; i++)
        {
            results[i] = new Result();
        }
        
        // Compute volume if necessary
        if (this.computeVolume)
        {
            double[] volumeLut = IntrinsicVolumes3DUtils.volumeLut(calib);
            double[] volumes = BinaryConfigurationsHistogram3D.applyLut(histograms, volumeLut);
            for (int i = 0; i < labels.length; i++)
            {
                results[i].volume = volumes[i];
            }
        }

        // Compute surface area if necessary
        if (this.computeVolume)
        {
            double[] areaLut = IntrinsicVolumes3DUtils.surfaceAreaLut(calib, this.directionNumber);
            double[] areas = BinaryConfigurationsHistogram3D.applyLut(histograms, areaLut);
            for (int i = 0; i < labels.length; i++)
            {
                results[i].surfaceArea = areas[i];
            }
        }
        
        // Compute mean breadth if necessary
        if (this.computeMeanBreadth)
        {
            double[] breadthLut = IntrinsicVolumes3DUtils.meanBreadthLut(calib, this.directionNumber, 8);
            double[] breadths = BinaryConfigurationsHistogram3D.applyLut(histograms, breadthLut);
            for (int i = 0; i < labels.length; i++)
            {
                results[i].meanBreadth = breadths[i];
            }
        }
        
        // Compute Euler number if necessary
        if (this.computeEulerNumber)
        {
            double[] eulerLut = IntrinsicVolumes3DUtils.eulerNumberLut(this.connectivity);
            double[] eulers = BinaryConfigurationsHistogram3D.applyLut(histograms, eulerLut);
            for (int i = 0; i < labels.length; i++)
            {
                results[i].eulerNumber = eulers[i];
            }
        }
        
        return results;
    }
    
    // ==================================================
    // Implementation of Algolistener interface

    /* (non-Javadoc)
     * @see inra.ijpb.algo.AlgoListener#algoProgressChanged(inra.ijpb.algo.AlgoEvent)
     */
    @Override
    public void algoProgressChanged(AlgoEvent evt)
    {
        this.fireProgressChanged(evt);
    }

    /* (non-Javadoc)
     * @see inra.ijpb.algo.AlgoListener#algoStatusChanged(inra.ijpb.algo.AlgoEvent)
     */
    @Override
    public void algoStatusChanged(AlgoEvent evt)
    {
        this.fireStatusChanged(evt);
    }

    // ==================================================
    // Inner class for storing results
    
    /**
     * Inner class for storing results.
     */
    public class Result
    {
    	/** The volume of the region */
        public double volume = Double.NaN;
    	/** The surface area of the region */
        public double surfaceArea = Double.NaN;
    	/** The mean breadth of the region (proportional to the integral of average curvature)*/
        public double meanBreadth = Double.NaN;
    	/** The Euler Number of the region */
        public double eulerNumber = Double.NaN;
        
        /**
         * Empty constructor.
         */
        public Result()
        {
        }
        
        /**
		 * Creates a new data class for storing intrinsic volume measurements.
		 * 
		 * @param volume
		 *            the volume of the region.
		 * @param surf
		 *            the surface area of the region.
		 * @param breadth
		 *            the mean breadth of the region
		 * @param euler
		 *            the Euler number of the region
		 */
        public Result(double volume, double surf, double breadth, double euler)
        {
            this.volume = volume;
            this.surfaceArea = surf;
            this.meanBreadth = breadth;
            this.eulerNumber = euler;
        }
    }
}
