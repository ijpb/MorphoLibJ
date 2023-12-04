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
package inra.ijpb.measure.region2d;

import java.util.Map;

import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoEvent;
import inra.ijpb.algo.AlgoListener;

/**
 * Computation of intrinsic volumes (area, perimeter and Euler number) for
 * binary or label 2D images.
 *
 * @author dlegland
 *
 */
public class IntrinsicVolumes2D extends RegionAnalyzer2D<IntrinsicVolumes2D.Result> implements AlgoListener
{
    // ==================================================
    // Class members

    boolean computeArea = true;
    boolean computePerimeter = true;
    boolean computeEulerNumber = true;
    
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

    /**
     * Default empty constructor.
     */
    public IntrinsicVolumes2D()
    {
    }
    
    
    // ==================================================
    // Getter / Setter

    /**
     * @return the number of directions used to compute perimeter
     */
    public int getDirectionNumber()
    {
        return directionNumber;
    }

    /**
     * @param directionNumber
     *            the number of directions used to compute perimeter (either 2
     *            or 4, default is 4)
     */
    public void setDirectionNumber(int directionNumber)
    {
        this.directionNumber = directionNumber;
    }

    /**
     * @return the connectivity used for computing Euler number
     */
    public int getConnectivity()
    {
        return connectivity;
    }

    /**
     * @param connectivity
     *            the connectivity for computing Euler number (either 4 or 8,
     *            default is 4)
     */
    public void setConnectivity(int connectivity)
    {
        this.connectivity = connectivity;
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
        // Histogram of binary configurations for each region label
        BinaryConfigurationsHistogram2D algo = new BinaryConfigurationsHistogram2D();
        algo.addAlgoListener(this);
        int[][] histograms = algo.process(image, labels);

        // initialize result array
        Result[] results = new Result[labels.length];
        for (int i = 0; i < labels.length; i++)
        {
            results[i] = new Result();
        }
        
        // Compute area if necessary
        if (this.computeArea)
        {
            double[] areaLut = IntrinsicVolumes2DUtils.areaLut(calib);
            double[] areas = BinaryConfigurationsHistogram2D.applyLut(histograms, areaLut);
            for (int i = 0; i < labels.length; i++)
            {
                results[i].area = areas[i];
            }
        }
        
        // Compute perimeter if necessary
        if (this.computePerimeter)
        {
            double[] perimLut = IntrinsicVolumes2DUtils.perimeterLut(calib, this.directionNumber);
            double[] perims = BinaryConfigurationsHistogram2D.applyLut(histograms, perimLut);
            for (int i = 0; i < labels.length; i++)
            {
                results[i].perimeter = perims[i];
            }
        }
        
        // Compute Euler number if necessary
        if (this.computeEulerNumber)
        {
            double[] eulerLut = IntrinsicVolumes2DUtils.eulerNumberLut(this.connectivity);
            double[] eulers = BinaryConfigurationsHistogram2D.applyLut(histograms, eulerLut);
            for (int i = 0; i < labels.length; i++)
            {
                results[i].eulerNumber = eulers[i];
            }
        }
        
        return results;
    }

    
 
    // ==================================================
    // Implementation of Algolistener interface
    
    @Override
    public void algoProgressChanged(AlgoEvent evt)
    {
        this.fireProgressChanged(evt);
    }

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
		/** the area of the region. */
		public double area = Double.NaN;
		
		/** the perimeter of the region. */
		public double perimeter = Double.NaN;
		
		/** the Euler Number of the region. */
		public double eulerNumber = Double.NaN;
        
        /**
         * Default empty constructor.
         */
        public Result()
        {
        }
        
        /**
		 * Creates a new data class for storing intrinsic volume measurements.
		 * 
		 * @param area
		 *            the area of the region.
		 * @param perim
		 *            the perimeter of the region
		 * @param euler
		 *            the Euler number of the region
		 */
        public Result(double area, double perim, double euler)
        {
            this.area = area;
            this.perimeter = perim;
            this.eulerNumber = euler;
        }
        
        /**
         * Computes the circularity value associated with this result.
         * 
         * Circularity <code>circ</code> is defined as the ratio of area with
         * the square of the perimeter, normalized such that a disk has a circularity close to 1. 
         * 
         * 
         * @return the circularity value.
         */
        public double circularity()
        {
            return 4 * Math.PI * area / (perimeter * perimeter);
        }
    }
}
