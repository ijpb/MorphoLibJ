/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
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
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.label.LabelImages;

/**
 * Compute average thickness of a binary region, or of each region in a label image. 
 * 
 * The average thickness is computed by:
 * <ol>
 * <li> computing the skeleton of the binary region,</Li>
 * <li> computing the distance map of each foreground pixel of the original binary region,</Li>
 * <li> measuring the value in the distance map for each pixel of the skeleton,</Li>
 * <li> computing the average.</Li>
 * </ol>
 * 
 * The result largely depends on the algorithms for computing skeleton and distance map. 
 * For skeleton, the an adaptation of ImageJ's algorithm is used. For distance maps, chamfer
 * distance maps are used.
 * 
 * @see inra.ijpb.binary.skeleton.ImageJSkeleton
 * @see inra.ijpb.label.distmap.DistanceTransform2D
 * 
 * @author dlegland
 *
 */
public class AverageThickness extends RegionAnalyzer2D<AverageThickness.Result>
{

    @Override
    public ResultsTable createTable(Map<Integer, AverageThickness.Result> results)
    {
        // Initialize a new result table
        ResultsTable table = new ResultsTable();
        
        String unit = results.values().iterator().next().unit;
        String colName = "AverageThickness_[" + unit + "]"; 
    
        // Convert all results that were computed during execution of the
        // "analyzeRegions()" method into rows of the results table
        for (int label : results.keySet())
        {
            // current diameter
            Result res = results.get(label);
            
            // add an entry to the resulting data table
            table.incrementCounter();
            table.addLabel(Integer.toString(label));
            table.addValue(colName, res.avgThickness);
        }
    
        return table;
    }

    @Override
    public AverageThickness.Result[] analyzeRegions(ImageProcessor image, int[] labels,
            Calibration calib)
    {
        // check input validity
        if (calib.pixelWidth != calib.pixelHeight)
        {
            throw new IllegalArgumentException("Requires input image to have square pixels (width = height)");
        }
        
        Map<Integer, Integer> labelIndices = LabelImages.mapLabelIndices(labels);

        // first compute distance map of each label
        ImageProcessor distanceMap = LabelImages.distanceMap(image);

        // Compute skeleton of each region.
        ImageProcessor skeleton = BinaryImages.skeleton(image); 
        
        // allocate memory for result values
        int nLabels = labels.length;
        double[] sums = new double[nLabels];
        int[] counts = new int[nLabels];

        // Iterate over skeleton pixels
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                // label of current pixel
                int label = (int) skeleton.getf(x, y);
                
                // do not process background pixels
                if (label == 0)
                {
                    continue;
                }
                // do not process labels not in the list
                if (!labelIndices.containsKey(label))
                {
                    continue;
                }

                int index = labelIndices.get(label);
                
                // update results for current region
                sums[index] += distanceMap.getf(x, y);
                counts[index]++;
            }
        }

        // convert to array of results
        AverageThickness.Result[] results = new AverageThickness.Result[nLabels];
        for (int i = 0; i < nLabels; i++)
        {
            Result res = new Result();
            res.meanDist = calib.pixelWidth * (sums[i] / counts[i]);
            res.avgThickness = res.meanDist * 2 - 1;
            res.unit = calib.getUnit();
            results[i] = res;
        }
        return results;
    }
    
    
    // ==================================================
    // Inner class used for representing computation results
    
    /**
     * Inner class used for representing results of average thickness
     * computations. Each instance corresponds to a single region.
     * 
     * @author dlegland
     *
     */
    public class Result
    {
        /**
         * Average distance computed along the skeleton
         */
        public double meanDist;
        
        /**
         * Average thickness value computed from the average distance.
         * Typically: avgThickness = avgDist * 2 - 1.
         */
        public double avgThickness;
        
        /**
         * The unit associated to the measure.
         */
        public String unit = "pixel";
    }

}
