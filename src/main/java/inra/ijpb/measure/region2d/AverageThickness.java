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
 * @see inra.ijpb.binary.distmap.DistanceTransform
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
    
        // Convert all results that were computed during execution of the
        // "analyzeRegions()" method into rows of the results table
        for (int label : results.keySet())
        {
            // current diameter
            Result res = results.get(label);
            
            // add an entry to the resulting data table
            table.incrementCounter();
            table.addLabel(Integer.toString(label));
            table.addValue("AverageThickness", res.avgThickness);
        }
    
        return table;
    }

    @Override
    public AverageThickness.Result[] analyzeRegions(ImageProcessor image, int[] labels,
            Calibration calib)
    {
        Map<Integer, Integer> labelIndices = LabelImages.mapLabelIndices(labels);

        // first compute distance map of each label
        ImageProcessor distanceMap = BinaryImages.distanceMap(image);

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
                if (label == 0)
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
            res.meanDist = sums[i] / counts[i];
            res.avgThickness = res.meanDist * 2;
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
        double meanDist;
        
        /**
         * Average thickness value computed from the average distance.
         * Typically: avgThickness = avgDist * 2.
         */
        double avgThickness;
    }

}
