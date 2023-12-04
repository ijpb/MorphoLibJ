/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2023 INRA.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
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
 * The <code>inra.ijpb.measure.IntrinsicVolumes3D</code> class provides static classes to
 * facilitate usage when no algorithm monitoring is necessary.
 * 
 * @see inra.ijpb.measure.IntrinsicVolumes3D
 * @see inra.ijpb.measure.region2d.IntrinsicVolumes2D
 * 
 * @author dlegland
 *
 */
public class IntrinsicVolumes3D extends RegionAnalyzer3D<IntrinsicVolumes3D.Result>
        implements AlgoListener
{
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
    public IntrinsicVolumes3D()
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
        
        /**
         * Computes the sphericity shape feature for the region described by
         * this result. Both volume and surface must have been computed.
         * 
         * The sphericity is computed using the following formula: <code>
         * sphericity = 36 * PI * V^2 / S^3
         * </code>
         * 
         * @return the sphericity of the region.
         */
        public double sphericity()
        {
            return inra.ijpb.measure.IntrinsicVolumes3D.sphericity(volume, surfaceArea);
        }
    }
}
