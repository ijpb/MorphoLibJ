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

import ij.measure.Calibration;
import inra.ijpb.measure.region2d.IntrinsicVolumes2D.Result;

/**
 * Utility functions for classes that compute 2D intrinsic volumes measures.
 * 
 * @see inra.ijpb.measure.IntrinsicVolumes2D
 * 
 * @author dlegland
 */
public class IntrinsicVolumes2DUtils
{
    /**
     * Computes the Look-up table that is used for computing area. The result is
     * an array with 16 entries, each entry corresponding to a binary 2-by-2
     * configuration of pixels.
     * 
     * @param calib
     *            the calibration of the image
     * @return an array containing for each 2-by-2 configuration index, the
     *         corresponding contribution to the calibrated area fraction within
     *         configuration
     */
    public static final double[] areaLut(Calibration calib)
    {
        // base LUT
        double[] lut = new double[] { 
                0, 0.25, 0.25, 0.5, 0.25, 0.5, 0.5, 0.75,
                0.25, 0.5, 0.5, 0.75, 0.5, 0.75, 0.75, 1.0 };
        
        // take into account spatial calibration
        double pixelArea = calib.pixelWidth * calib.pixelHeight;
        for (int i = 0; i < lut.length; i++)
        {
            lut[i] *= pixelArea;
        }
        
        return lut;
    }
    
    /**
     * Computes the Look-up table that is used for computing perimeter. The
     * result is an array with 16 entries, each entry corresponding to a binary
     * 2-by-2 configuration of pixels.
     * 
     * @param calib
     *            the calibration of the image
     * @param nDirs
     *            the number of directions to use (2 or 4)
     * @return an array containing for each 2-by-2 configuration index, the
     *         corresponding contribution to the calibrated perimeter estimate
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
                        tab[i] += ((ke1 / 2) * weights[0]
                                + (ke2 / 2) * weights[1] + ke12 * weights[2]);
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
    private static final double[] computeDirectionWeightsD4(double dx,
            double dy)
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
     * Utility method that computes circularities as a numeric array from the
     * result array
     * 
     * @param morphos
     *            the array of results
     * @return the numeric array of circularities
     */
    public static final double[] computeCircularities(Result[] morphos)
    {
        int n = morphos.length;
        double[] circularities = new double[n];
        for (int i = 0; i < n; i++)
        {
            circularities[i] = morphos[i].circularity();
        }
        return circularities;
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
        switch (conn)
        {
        case 4:
            return eulerNumberLutC4();
        case 8:
            return eulerNumberLutC8();
        default:
            throw new IllegalArgumentException(
                    "Connectivity must be 4 or 8, not " + conn);
        }
    }
    
    private final static double[] eulerNumberLutC4()
    {
        return new double[] {
                0, 0.25, 0.25, 0, 0.25, 0, 0.5, -0.25, 
                0.25, 0.5, 0, -0.25, 0, -0.25, -0.25, 0 };
    }
    
    private final static double[] eulerNumberLutC8()
    {
        return new double[] {
                0, 0.25, 0.25, 0, 0.25, 0, -0.5, -0.25, 
                0.25, -0.5, 0, -0.25, 0, -0.25, -0.25, 0 };
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
    public static final int[] eulerNumberIntLut(int conn)
    {
        switch (conn)
        {
        case 4:
            return eulerNumberIntLutC4();
        case 8:
            return eulerNumberIntLutC8();
        default:
            throw new IllegalArgumentException(
                    "Connectivity must be 4 or 8, not " + conn);
        }
    }
    
    private final static int[] eulerNumberIntLutC4()
    {
        return new int[] { 
                0, 1, 1, 0,  1, 0, 2, -1,
                1, 2, 0, -1,  0, -1, -1, 0 };
    }
    
    private final static int[] eulerNumberIntLutC8()
    {
        return new int[] { 
                0, 1, 1, 0,  1, 0, -2, -1, 
                1, -2, 0, -1,  0, -1, -1, 0 };
    }
    
    /**
     * Private constructor to prevent instantiation.
     */
    private IntrinsicVolumes2DUtils()
    {
    }
}
