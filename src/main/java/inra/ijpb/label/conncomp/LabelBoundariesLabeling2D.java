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
package inra.ijpb.label.conncomp;

import java.util.ArrayList;

import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
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
 * @see LabelBoundariesLabeling3D
 * 
 * @author dlegland
 */
public class LabelBoundariesLabeling2D extends AlgoStub
{
    /**
     * Used to identify where to look for neighbors around a given pixel.
     */
    private static int[][] shiftsC4 = new int[][] {
        {0, -1}, {-1, 0}, {+1, 0}, {0, +1}
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
    public Result process(ImageProcessor labelMap)
    {
        // retrieve image size
        int sizeX = labelMap.getWidth();
        int sizeY = labelMap.getHeight();
        
        // allocate memory for boundary label map
        Result res = new Result(new FloatProcessor(sizeX, sizeY));
        
        int maxLabelCount = LabelUtils.getLargestPossibleLabel(res.boundaryLabelMap);
        
        // iterate over pixels
        for (int y = 0; y < sizeY; y++)
        {
            this.fireProgressChanged(this, y, sizeY);
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
                
                res.boundaryLabelMap.setf(x, y, boundary.label);
            }
        }
        
        this.fireProgressChanged(this, 1, 1);
        res.boundaryLabelMap.setMinAndMax(0, res.boundaries.size());
        
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
        public final ImageProcessor boundaryLabelMap;
        
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
        public Result(ImageProcessor labelMap)
        {
            this.boundaryLabelMap = labelMap;
            this.boundaries = new BoundarySet();
        }
    }
}
