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

import ij.ImageStack;
import ij.measure.Calibration;
import inra.ijpb.geometry.Vector3D;

/**
 * Utility functions for classes that compute 3D intrinsic volumes measures.
 * 
 * @see inra.ijpb.measure.IntrinsicVolumes3D
 * 
 * @author dlegland
 *
 */
public class IntrinsicVolumes3DUtils
{
    /**
     * Computes the Look-up table that is used to compute volume.
     * 
     * @param calib
     *            the spatial calibration of the image
     * @return the look-up-table between binary voxel configuration index and
     *         contribution to volume measure
     */
    public static final double[] volumeLut(Calibration calib)
    {
        // initialize output array (256 configurations in 3D)
        int nbConfigs = 256;
        double[] lut = new double[nbConfigs];
    
        // create the binary image representing the 2x2x2 tile
        boolean[][][] im = new boolean[2][2][2];
        
        // take into account spatial calibration
        double voxelVolume = calib.pixelWidth * calib.pixelHeight * calib.pixelDepth;
        
        // loop for each tile configuration
        for (int iConfig = 0; iConfig < nbConfigs; iConfig++)
        {
            // create the binary image representing the 2x2x2 tile
            updateTile(im, iConfig);
            
            int nVoxels = 0;
            if (im[0][0][0]) nVoxels++;
            if (im[1][0][0]) nVoxels++;
            if (im[0][1][0]) nVoxels++;
            if (im[1][1][0]) nVoxels++;
            if (im[0][0][1]) nVoxels++;
            if (im[1][0][1]) nVoxels++;
            if (im[0][1][1]) nVoxels++;
            if (im[1][1][1]) nVoxels++;
            
            lut[iConfig] = nVoxels * voxelVolume / 8;
        }
        
        return lut;
     }

    /**
     * Computes the Look-up table that is used to compute surface area.
     * 
     * @param calib
     *            the spatial calibration of the image
     * @param nDirs
     *            the number of directions to consider, either 3 or 13
     * @return the look-up-table between binary voxel configuration index and
     *         contribution to surface area measure
     */
    public final static double[] surfaceAreaLut(Calibration calib, int nDirs) 
    {
        if (nDirs == 3)
        {
            return surfaceAreaLutD3(calib);
        }
        else if (nDirs == 13)
        {
            return surfaceAreaLutD13(calib);
        }
        else
        {
            throw new IllegalArgumentException("Number of directions should be either 3 or 13, not " + nDirs);
        }

    }
    
    /**
     * Computes the Look-up table that is used to compute surface area using
     * three directions.
     * 
     * @param calib
     *            the spatial calibration of the image
     * @return the look-up-table between binary voxel configuration index and
     *         contribution to surface area measure
     */
    private final static double[] surfaceAreaLutD3(Calibration calib) 
    {
        // distances between a voxel and its neighbors.
        // di refer to orthogonal neighbors
        // dij refer to neighbors on the same plane 
        // dijk refer to the opposite voxel in a tile
        double d1 = calib.pixelWidth;
        double d2 = calib.pixelHeight;
        double d3 = calib.pixelDepth;
        double vol = d1 * d2 * d3;
    
        // initialize output array (256 configurations in 3D)
        int nbConfigs = 256;
        double[] tab = new double[nbConfigs];
    
        // create the binary image representing the 2x2x2 tile
        boolean[][][] im = new boolean[2][2][2];
        
        // loop for each tile configuration
        for (int iConfig = 0; iConfig < nbConfigs; iConfig++)
        {
            // create the binary image representing the 2x2x2 tile
            updateTile(im, iConfig);
            
            // iterate over the 8 voxels within the configuration
            for (int z = 0; z < 2; z++) 
            {
                for (int y = 0; y < 2; y++) 
                {
                    for (int x = 0; x < 2; x++) 
                    {
                        if (!im[z][y][x])
                            continue;

                        // Compute contributions for isothetic directions
                        double ke1 = im[z][y][1 - x] ? 0 : vol / d1 / 2.0;
                        double ke2 = im[z][1 - y][x] ? 0 : vol / d2 / 2.0;
                        double ke3 = im[1 - z][y][x] ? 0 : vol / d3 / 2.0;
                        
                        // For 3 directions, the multiplicity is 4, and is canceled by the
                        // coefficient 4 in the Crofton formula. We just need to average on
                        // directions.
                        tab[iConfig] += (ke1 + ke2 + ke3) / 3;
                    }
                }
            }
        }
        return tab;     
    }

    /**
     * Computes the Look-up table that is used to compute surface area using 13
     * directions.
     * 
     * @param calib
     *            the spatial calibration of the image
     * @return the look-up-table between binary voxel configuration index and
     *         contribution to surface area measure
     */
    private final static double[] surfaceAreaLutD13(Calibration calib) 
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
        double[] weights = directionWeights3d13(calib);
    
        // initialize output array (256 configurations in 3D)
        int nbConfigs = 256;
        double[] tab = new double[nbConfigs];
    
        // create the binary image representing the 2x2x2 tile
        boolean[][][] im = new boolean[2][2][2];
        
        // contribution to number of intersections for each direction
        double[] kei = new double[7];
        
        // loop for each tile configuration
        for (int iConfig = 0; iConfig < nbConfigs; iConfig++)
        {
            // create the binary image representing the 2x2x2 tile
            updateTile(im, iConfig);
            
            // iterate over the 8 voxels within the configuration
            for (int z = 0; z < 2; z++) 
            {
                for (int y = 0; y < 2; y++) 
                {
                    for (int x = 0; x < 2; x++) 
                    {
                        if (!im[z][y][x])
                            continue;

                        // Compute contributions for isothetic directions
                        kei[0] = im[z][y][1 - x] ? 0 : vol / d1 / 8.0;
                        kei[1] = im[z][1 - y][x] ? 0 : vol / d2 / 8.0;
                        kei[2] = im[1 - z][y][x] ? 0 : vol / d3 / 8.0;
                        
                        // diagonals that share a square face
                        kei[3] = im[z][1 - y][1 - x] ? 0 : vol / d12 / 4.0;
                        kei[4] = im[1 - z][y][1 - x] ? 0 : vol / d13 / 4.0;
                        kei[5] = im[1 - z][1 - y][x] ? 0 : vol / d23 / 4.0;
                        
                        // diagonal with opposite vertex of the cube
                        kei[6] = im[1 - z][1 - y][1 - x] ? 0 : vol / d123 / 2.0; 
                        
                        // Decomposition of Crofton formula on 13 directions
                        for (int i = 0; i < 7; i++)
                            tab[iConfig] += 4 * kei[i] * weights[i];
                    }
                }
            }
        }
        return tab;     
    }

    /**
     * Computes the Look-up table used to measure mean breadth within 3D images.
     * 
     * @param calib
     *            the spatial calibration of image
     * @param nDirs
     *            the number of directions (3 or 13)
     * @param conn2d
     *            the connectivity to use on square faces of plane sections (4 or 8)
     * @return a look-up table with 256 entries
     */
    public static final double[] meanBreadthLut(Calibration calib, int nDirs, int conn2d)
    {
        if (nDirs == 3)
        {
            return meanBreadthLutD3(calib, conn2d);
        }
        else if (nDirs == 13)
        {
            return meanBreadthLutD13(calib, conn2d);
        }
        else
        {
            throw new IllegalArgumentException("Number of directions should be either 3 or 13, not " + nDirs);
        }
    }
    
    /**
     * Computes the Look-up table used to measure mean breadth within 3D images
     * using three directions.
     * 
     * @param calib
     *            the spatial calibration of image
     * @return a look-up table with 256 entries
     */
    private static final double[] meanBreadthLutD3(Calibration calib, int conn2d)
    {
        // distances between a voxel and its neighbors.
        // di refer to orthogonal neighbors
        // dij refer to neighbors on the same plane 
        // dijk refer to the opposite voxel in a tile
        double d1 = calib.pixelWidth;
        double d2 = calib.pixelHeight;
        double d3 = calib.pixelDepth;
        double vol = d1 * d2 * d3;
        
        // area of elementary profiles
        double a1 = d2 * d3;
        double a2 = d1 * d3;
        double a3 = d1 * d2;

        // correspondance map between voxel label and voxel coord in config
        int[][] coord = new int[][] {{0, 0, 0}, {0, 1, 0}, {1, 0, 0}, {1, 1, 0}, {0, 0, 1}, {0, 1, 1}, {1, 0, 1}, {1, 1, 1}};

        // initialize output array (256 configurations in 3D)
        int nbConfigs = 256;
        double[] lut = new double[nbConfigs];

        // create the binary image representing the 2x2x2 tile
        boolean[][][] im = new boolean[2][2][2];

        // loop for each tile configuration
        // (do not process first and last indices, as they are equal to zero by definition)
        for (int iConfig = 1; iConfig < nbConfigs - 1; iConfig++)
        {
            // refresh tile content
            im[0][0][0] = (iConfig & 1) > 0;
            im[0][0][1] = (iConfig & 2) > 0;
            im[0][1][0] = (iConfig & 4) > 0;
            im[0][1][1] = (iConfig & 8) > 0;
            im[1][0][0] = (iConfig & 16) > 0;
            im[1][0][1] = (iConfig & 32) > 0;
            im[1][1][0] = (iConfig & 64) > 0;
            im[1][1][1] = (iConfig & 128) > 0;
            
            // iterate over the 8 voxels within the configuration
            for (int iVoxel = 0; iVoxel < 8; iVoxel++)
            {
                // coordinate of voxel of interest
                int p1 = coord[iVoxel][0];
                int p2 = coord[iVoxel][1];
                int p3 = coord[iVoxel][2];
                
                // if voxel is not in structure, contrib is 0
                if (!im[p1][p2][p3])
                    continue;
                
                // create 2D faces in each isothetic direction
                boolean[] face1 = new boolean[]{im[p1][p2][p3], im[p1][1-p2][p3], im[p1][p2][1-p3], im[p1][1-p2][1-p3]};
                boolean[] face2 = new boolean[]{im[p1][p2][p3], im[1-p1][p2][p3], im[p1][p2][1-p3], im[1-p1][p2][1-p3]};
                boolean[] face3 = new boolean[]{im[p1][p2][p3], im[1-p1][p2][p3], im[p1][1-p2][p3], im[1-p1][1-p2][p3]};

                // compute contribution of voxel on each 2D face
                double f1 = eulerContribTile2d(face1, conn2d, d3, d2);
                double f2 = eulerContribTile2d(face2, conn2d, d3, d1);
                double f3 = eulerContribTile2d(face3, conn2d, d2, d1);
                
                // Uses only 3 isothetic directions.
                // divide by 6. Divide by 3 because of averaging on directions,
                // and divide by 2 because each face is visible on 2 config.
                lut[iConfig] += vol * (f1/a1 + f2/a2 + f3/a3) / 6;
            }
        }
        
        return lut;     
    }

    /**
     * Computes the Look-up table used to measure mean breadth within 3D images
     * using three directions.
     * 
     * @param calib
     *            the spatial calibration of image
     * @return a look-up table with 256 entries
     */
    private static final double[] meanBreadthLutD13(Calibration calib, int conn2d)
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
    
        // area of elementary profiles along each direction
        double s = (d12 + d13 + d23) / 2;
        double[] areas = new double[] {
                d2 * d3, d1 * d3, d1 * d2, 
                d3 * d12, d2 * d13, d1 * d23, 
                2 * Math.sqrt(s * (s - d12) * (s - d13) * (s - d23))};
        
        // direction weights corresponding to area of Voronoi partition on the
        // unit sphere, when germs are the 26 directions on the unit cube
        // Sum of (c1+c2+c3 + c4*2+c5*2+c6*2 + c7*4) equals 1.
        double[] weights = directionWeights3d13(calib);

        // projected diameters along each direction
        double[] diams = new double[7];
        
        // correspondance map between voxel label and voxel coord in config
        int[][] coord = new int[][] {{0, 0, 0}, {0, 1, 0}, {1, 0, 0}, {1, 1, 0}, {0, 0, 1}, {0, 1, 1}, {1, 0, 1}, {1, 1, 1}};

        // initialize output array (256 configurations in 3D)
        int nbConfigs = 256;
        double[] lut = new double[nbConfigs];

        // create the binary image representing the 2x2x2 tile
        boolean[][][] im = new boolean[2][2][2];

        // loop for each tile configuration
        // (do not process first and last indices, as they are equal to zero by definition)
        for (int iConfig = 1; iConfig < nbConfigs - 1; iConfig++)
        {
            // refresh tile content
            updateTile(im, iConfig);
            
            // iterate over the 8 voxels within the configuration
            for (int iVoxel = 0; iVoxel < 8; iVoxel++)
            {
                // coordinate of voxel of interest
                int p1 = coord[iVoxel][0];
                int p2 = coord[iVoxel][1];
                int p3 = coord[iVoxel][2];
                
                // if voxel is not in structure, contrib is 0
                if (!im[p1][p2][p3])
                    continue;
                
                // create 2D faces in each isothetic direction
                boolean[] face1 = new boolean[]{im[p1][p2][p3], im[p1][1-p2][p3], im[p1][p2][1-p3], im[p1][1-p2][1-p3]};
                boolean[] face2 = new boolean[]{im[p1][p2][p3], im[1-p1][p2][p3], im[p1][p2][1-p3], im[1-p1][p2][1-p3]};
                boolean[] face3 = new boolean[]{im[p1][p2][p3], im[1-p1][p2][p3], im[p1][1-p2][p3], im[1-p1][1-p2][p3]};
                
                // compute contribution of voxel on each 2D face, weighted by face multiplicity
                diams[0] = eulerContribTile2d(face1, conn2d, d3, d2) / 2.0;
                diams[1] = eulerContribTile2d(face2, conn2d, d3, d1) / 2.0;
                diams[2] = eulerContribTile2d(face3, conn2d, d2, d1) / 2.0;
                
                // create 2D faces for direction normal to square diagonals
                // use only the half
                boolean[] face4 = new boolean[]{im[p1][p2][p3], im[1-p1][1-p2][p3], im[p1][p2][1-p3], im[1-p1][1-p2][1-p3]};
                boolean[] face6 = new boolean[]{im[p1][p2][p3], im[1-p1][p2][1-p3], im[p1][1-p2][p3], im[1-p1][1-p2][1-p3]};
                boolean[] face8 = new boolean[]{im[p1][p2][p3], im[p1][1-p2][1-p3], im[1-p1][p2][p3], im[1-p1][1-p2][1-p3]};

                // compute contribution of voxel on each 2D face
                diams[3] = eulerContribTile2d(face4, conn2d, d12, d3);
                diams[4] = eulerContribTile2d(face6, conn2d, d13, d2);
                diams[5] = eulerContribTile2d(face8, conn2d, d23, d1);

                // create triangular faces. Reference voxel is the first one
                boolean[] faceA = new boolean[]{im[p1][p2][p3], im[1-p1][1-p2][p3], im[1-p1][p2][1-p3]};
                boolean[] faceB = new boolean[]{im[p1][p2][p3], im[1-p1][p2][1-p3], im[p1][1-p2][1-p3]};
                boolean[] faceC = new boolean[]{im[p1][p2][p3], im[1-p1][1-p2][p3], im[p1][1-p2][1-p3]};

                // compute contribution of voxel on each triangular face
                double fa = eulerContribTriangleTile(faceA, d12, d13, d23);
                double fb = eulerContribTriangleTile(faceB, d13, d23, d12);
                double fc = eulerContribTriangleTile(faceC, d12, d23, d13);
                diams[6] = fa + fb + fc;

                // Discretization of Crofton formula, using projected diameters
                // computed previously, and direction weights.
                for (int i = 0; i < 7; i++)
                {
                    lut[iConfig] += vol * (diams[i] / areas[i]) * weights[i];
                }
            }
        }
        
        return lut;     
    }
    
    private static final double eulerContribTile2d(boolean[] face, int conn2d, double d1, double d2)
    {
        if (conn2d == 4)
        {
            return eulerContribTile2dC4(face, d1, d2);
        }
        else if (conn2d == 8)
        {
            return eulerContribTile2dC8(face, d1, d2);
        }
        else
        {
            throw new IllegalArgumentException("Connectivity mustbe either 4 or 8");
        }
    }
    
    /**
     * Computes the contribution to Euler number of the reference vertex within
     * a rectangular grid tile using the 4-connectivity.
     * 
     * @param face
     *            the boolean values of the four vertices of the rectangular
     *            tile
     * @param d1
     *            distance between vertex 1 and vertex 2
     * @param d2
     *            distance between vertex 1 and vertex 3
     * @return the contribution to the Euler number
     */
    private static final double eulerContribTile2dC4(boolean[] face, double d1, double d2)
    {
        // if reference vertex is not with structure, contribution is zero 
        if (!face[0])
        {
            return 0.0;
        }
        
        // count the number of pixels within the configuration
        int nPixels = 0;
        for (int i = 0; i < face.length; i++) 
        {
            if (face[i]) nPixels++;
        }
        
        switch (nPixels)
        {
        case 1:
            // in case of a single pixel, contribution is 1/4
            return 0.25;
        case 2:
            // case of two pixels, corresponding to an edge.
            // If there is one diagonal edge (face[3]), contribution is  (1/4) = 1/2
            // If there is one isothetic edge (face[1] or face[2]), contribution is 1/4-1/2/2 = 0
            // (edge is shared with another configuration)
            return face[3] ? 0.25 : 0.0;
        case 3:
            // case of triangular face
            if (face[3])
            {
                // case of an edge viewed from extremity -> 0
                // contribution is decomposed as follows:
                // +1 vertex, shared by 4 tiles - +(1/4)
                // -1 edge shared by 2 tiles -> (-1/2)*(1/2)
                // The sum is zero
                return 0;
            }
            else
            {
                // case of a triangle viewed from rectangular angle -> -1/4
                // +1 vertex, shared by 4 tiles - +(1/4)
                // -2 edges shared by 2 tiles -> 2 * (-1/2) * (1/2) -> -1/2
                return -.25;
            }
        case 4:
            // case of full face -> no contribution
            return 0;
            
        default:
            throw new RuntimeException("Uncatched number of pixels: " + nPixels);
        }
    }
    
    /**
     * Computes the contribution to Euler number of the reference vertex within
     * a rectangular grid tile using the 4-connectivity.
     * 
     * @param face
     *            the boolean values of the four vertices of the rectangular
     *            tile
     * @param d1
     *            distance between vertex 1 and vertex 2
     * @param d2
     *            distance between vertex 1 and vertex 3
     * @return the contribution to the Euler number
     */
    private static final double eulerContribTile2dC8(boolean[] face, double d1, double d2)
    {
        // if reference vertex is not with structure, contribution is zero 
        if (!face[0])
        {
            return 0.0;
        }
        
        // count the number of pixels within the configuration
        int nPixels = 0;
        for (int i = 0; i < face.length; i++) 
        {
            if (face[i]) nPixels++;
        }
        
        switch (nPixels)
        {
        case 1:
            // in case of a single pixel, contribution is 1/4
            return 0.25;
        case 2:
            // case of two pixels, corresponding to an edge.
            // If there is one diagonal edge (face[3]), contribution is  1/4-1/2   = -1/4
            // If there is one isothetic edge (face[1] or face[2]), contribution is 1/4-1/2/2 = 0
            // (edge is shared with another configuration)
            return face[3] ? -0.25 : 0.0;
        case 3:
            // case of triangular face
            if (face[3])
            {
                // case of a triangle viewed from acute angle
                // contribution depends on this angle
                double alpha = face[2] ? Math.atan2(d2, d1) : Math.atan2(d1, d2);
                             
                // contribution is decomposed as follows :
                // +1 vertex, shared by 4 tiles
                // -1 edge shared by 2 tiles
                // -1 edge shared by 1 tile (sum of edges is -3/4)
                // +face contribution, shared by 1 tile.
                return (Math.PI - alpha) / (2 * Math.PI) - 0.5;
            }
            else
            {
                // case of a triangle viewed from rectangular angle -> 0
                return 0;
            }
        case 4:
            // case of full face -> no contribution
            return 0;
        default:
            throw new RuntimeException("Uncatched number of pixels: " + nPixels);
        }
    }

    /**
     * Computes the contribution to Euler number of the reference vertex within
     * a triangular grid tile.
     * 
     * @param face
     *            the boolean values of the three vertices of the triangular
     *            face
     * @param d1
     *            distance between vertex 1 and vertex 2
     * @param d2
     *            distance between vertex 1 and vertex 3
     * @param d3
     *            distance between vertex 2 and vertex 3
     * @return the contribution to the Euler number
     */
    private static final double eulerContribTriangleTile(boolean[] face, double d1, double d2, double d3)
    {
        // if reference vertex is not with structure, contribution is zero 
        if (!face[0])
        {
            return 0.0;
        }
        
        // count the number of face vertices within the structure
        int nPixels = 1;
        if (face[1]) nPixels++;
        if (face[2]) nPixels++;
        
        // switch computation depending on pixel number within triangular face
        switch(nPixels)
        {
        case 1:
            // case of an isolated vertex. 
            // Contribution to Euler number is 1, 
            // divided by the multiplicity equal to 6.
            return 1.0 / 6.0;
            
        case 2:
            // case of an edge
            // contribution can be divided as :
            // +1/6  for the vertex
            // -1/2/2 for the edge (multiplicity = 2)
            // result is -1/12.
            return -1.0 / 12.0;
            
        case 3:
            // case of a "full" triangular face 
            // Compute angle of facet at reference vertex using cosine law
            double alpha = Math.acos((d1 * d1 + d2 * d2 - d3 * d3) / (2 * d1 * d2));
            // contribution of vertex minus edge plus face
            return (Math.PI - alpha) / (2 * Math.PI) - 1.0 / 3.0;
        default:
            throw new RuntimeException("Uncatched number of pixels: " + nPixels);
        }
    }

    private static final void updateTile(boolean[][][] tile, int tileIndex)
    {
        // create the binary image representing the 2x2x2 tile
        tile[0][0][0] = (tileIndex & 1) > 0;
        tile[0][0][1] = (tileIndex & 2) > 0;
        tile[0][1][0] = (tileIndex & 4) > 0;
        tile[0][1][1] = (tileIndex & 8) > 0;
        tile[1][0][0] = (tileIndex & 16) > 0;
        tile[1][0][1] = (tileIndex & 32) > 0;
        tile[1][1][0] = (tileIndex & 64) > 0;
        tile[1][1][1] = (tileIndex & 128) > 0;
    }
    
    /**
     * Returns an array with seven values corresponding the unique direction
     * vectors obtained with 3 directions. The results simply consists in 1/3
     * for the three main directions, and 0 otherwise. The method is provided
     * for the sake of homogeneity with directionWeights3d13.
     * 
     * @param calib
     *            the spatial calibration of the 3D image
     * @return an array of seven values, corresponding to the three isothetic,
     *         three plane diagonal, and one cube diagonal directions.
     */
    public static final double[] directionWeights3d3(Calibration calib) 
    {
        final double third = 1.0 / 3.0;
        return new double[] { third, third, third, 0, 0, 0, 0, 0, 0, 0 };
    }
    /**
     * Returns an array with seven values corresponding the unique direction
     * vectors obtained with 13 directions.
     * 
     * @param calib
     *            the spatial calibration of the 3D image
     * @return an array of seven values, corresponding to the three isothetic,
     *         three plane diagonal, and one cube diagonal directions.
     */
    public static final double[] directionWeights3d13(Calibration calib) 
    {
        // extract resolution as individual variables
        double dx = calib.pixelWidth;
        double dy = calib.pixelHeight;
        double dz = calib.pixelDepth;
        
        // allocate memory for resulting array
        double[] weights = new double[7];
    
        // In case of cubic grid, use pre-computed weights
        if (dx == dy && dx == dz) 
        {
            // in case of cubic voxels, uses pre-computed weights
            weights[0] = 0.04577789120476 * 2;  // Ox
            weights[1] = 0.04577789120476 * 2;  // Oy
            weights[2] = 0.04577789120476 * 2;  // Oz
            weights[3] = 0.03698062787608 * 2;  // Oxy
            weights[4] = 0.03698062787608 * 2;  // Oxz
            weights[5] = 0.03698062787608 * 2;  // Oyz
            weights[6] = 0.03519563978232 * 2;  // Oxyz
            return weights;
        }

        // Create a set of reference vectors, named after their contribution to
        // each direction: 'P' stands for positive, 'N' stands for negative, 
        // and 'Z' stands for Zero. 
        // Hence, vector vZPN has x-coordinate equal to zero, y-coordinate 
        // equal to +dy, and z-coordinate equal to -dz. 
            
        // direction vectors pointing below the OXY plane
        Vector3D vPNN = new Vector3D( dx, -dy, -dz).normalize(); 
        Vector3D vPZN = new Vector3D( dx,   0, -dz).normalize(); 
        Vector3D vNPN = new Vector3D(-dx,  dy, -dz).normalize(); 
        Vector3D vZPN = new Vector3D(  0,  dy, -dz).normalize(); 
        Vector3D vPPN = new Vector3D( dx,  dy, -dz).normalize(); 
        
        // direction vectors pointing belonging to the OXY plane
        Vector3D vPNZ = new Vector3D( dx, -dy,   0).normalize();
        Vector3D vPZZ = new Vector3D( dx,   0,   0).normalize();
        Vector3D vNPZ = new Vector3D(-dx,  dy,   0).normalize();
        Vector3D vZPZ = new Vector3D(  0,  dy,   0).normalize();
        Vector3D vPPZ = new Vector3D( dx,  dy,   0).normalize();
        
        // direction vectors pointing above the OXY plane
        Vector3D vNNP = new Vector3D(-dx, -dy,  dz).normalize(); 
        Vector3D vZNP = new Vector3D(  0, -dy,  dz).normalize(); 
        Vector3D vPNP = new Vector3D( dx, -dy,  dz).normalize(); 
        Vector3D vNZP = new Vector3D(-dx,   0,  dz).normalize(); 
        Vector3D vZZP = new Vector3D(  0,   0,  dz).normalize(); 
        Vector3D vPZP = new Vector3D( dx,   0,  dz).normalize(); 
        Vector3D vNPP = new Vector3D(-dx,  dy,  dz).normalize(); 
        Vector3D vZPP = new Vector3D(  0,  dy,  dz).normalize(); 
        Vector3D vPPP = new Vector3D( dx,  dy,  dz).normalize(); 
    
        Vector3D[] neighbors;
        
        // Spherical cap type 1, direction [1 0 0]
        neighbors = new Vector3D[]{vPNN, vPNZ, vPNP, vPZP, vPPP, vPPZ, vPPN, vPZN};
        weights[0] = GeometryUtils.sphericalVoronoiDomainArea(vPZZ, neighbors) / (2 * Math.PI);
        
        // Spherical cap type 1, direction [0 1 0]
        neighbors = new Vector3D[]{vPPZ, vPPP, vZPP, vNPP, vNPZ, vNPN, vZPN, vPPN};
        weights[1] = GeometryUtils.sphericalVoronoiDomainArea(vZPZ, neighbors) / (2 * Math.PI);
    
        // Spherical cap type 1, direction [0 0 1]
        neighbors = new Vector3D[]{vPZP, vPPP, vZPP, vNPP, vNZP, vNNP, vZNP, vPNP};
        weights[2] = GeometryUtils.sphericalVoronoiDomainArea(vZZP, neighbors) / (2 * Math.PI);
    
        // Spherical cap type 2, direction [1 1 0]
        neighbors = new Vector3D[]{vPZZ, vPPP, vZPZ, vPPN};
        weights[3] = GeometryUtils.sphericalVoronoiDomainArea(vPPZ, neighbors) / (2 * Math.PI);
    
        // Spherical cap type 2, direction [1 0 1]
        neighbors = new Vector3D[]{vPZZ, vPPP, vZZP, vPNP};
        weights[4] = GeometryUtils.sphericalVoronoiDomainArea(vPZP, neighbors) / (2 * Math.PI);
    
        // Spherical cap type 2, direction [0 1 1]
        neighbors = new Vector3D[]{vZPZ, vNPP, vZZP, vPPP};
        weights[5] = GeometryUtils.sphericalVoronoiDomainArea(vZPP, neighbors) / (2 * Math.PI);
    
        // Spherical cap type 2, direction [1 0 1]
        neighbors = new Vector3D[]{vPZP, vZZP, vZPP, vZPZ, vPPZ, vPZZ};
        weights[6] = GeometryUtils.sphericalVoronoiDomainArea(vPPP, neighbors) / (2 * Math.PI);
        
        return weights;
    }
    
    /**
     * Computes the look-up table for measuring Euler number in binary 3D image,
     * depending on the connectivity. The input structure should not touch image
     * border.
     * 
     * See "3D Images of Material Structures", from J. Ohser and K. Schladitz,
     * Wiley 2009, tables 3.2 p. 52 and 3.3 p. 53.
     * 
     * @param conn
     *            the 3D connectivity, either 6 or 26
     * @return a look-up-table with 256 entries
     */
    public static final double[] eulerNumberLut(int conn)
    {
        if (conn == 6)
        {
            return eulerNumberLutC6();          
        }
        else if (conn == 26)
        {
            return eulerNumberLutC26();
        }
        else
        {
            throw new IllegalArgumentException("Connectivity must be either 6 or 26, not " + conn);
        }
    }

    /**
     * Computes the look-up table for measuring Euler number in binary 3D image,
     * for the 6-connectivity.
     * 
     * @return a look-up-table with 256 entries 
     */
    private static final double[] eulerNumberLutC6()
    {
        double[] lut = new double[]{
             0,  1,  1,  0,   1,  0,  2, -1,   1,  2,  0, -1,   0, -1, -1,  0,  //   0 ->  15
             1,  0,  2, -1,   2, -1,  3, -2,   2,  1,  1, -2,   1, -2,  0, -1,  //  16 ->  31
             1,  2,  0, -1,   2,  1,  1, -2,   2,  3, -1, -2,   1,  0, -2, -1,  //  32 ->  47
             0, -1, -1,  0,   1, -2,  0, -1,   1,  0, -2, -1,   0, -3, -3,  0,  //  48 ->  63
             1,  2,  2,  1,   0, -1,  1, -2,   2,  3,  1,  0,  -1, -2, -2, -1,  //  64 ->  79
             0, -1,  1, -2,  -1,  0,  0, -1,   1,  0,  0, -3,  -2, -1, -3,  0,  //  80 ->  95
             2,  3,  1,  0,   1,  0,  0, -3,   3,  4,  0, -1,   0, -1, -3, -2,  //  96 -> 111
            -1, -2, -2, -1,  -2, -1, -3,  0,   0, -1, -3, -2,  -3, -2, -6,  1,  // 112 -> 127
            
             1,  2,  2,  1,   2,  1,  3,  0,   0,  1, -1, -2,  -1, -2, -2, -1,  // 128 -> 145
             2,  1,  3,  0,   3,  0,  4, -1,   1,  0,  0, -3,   0, -3, -1, -2,  // 146 -> 159
             0,  1, -1, -2,   1,  0,  0, -3,  -1,  0,  0, -1,  -2, -3, -1,  0,  // 160 -> 175
            -1, -2, -2, -1,   0, -3, -1, -2,  -2, -3, -1,  0,  -3, -6, -2,  1,  // 176 -> 191
             0,  1,  1,  0,  -1, -2,  0, -3,  -1,  0, -2, -3,   0, -1, -1,  0,  // 192 -> 207
            -1, -2,  0, -3,  -2, -1, -1, -2,  -2, -3, -3, -6,  -1,  0, -2,  1,  // 208 -> 223
            -1,  0, -2, -3,  -2, -3, -3, -6,  -2, -1, -1, -2,  -1, -2,  0,  1,  // 224 -> 239
             0, -1, -1,  0,  -1,  0, -2,  1,  -1, -2,  0,  1,   0,  1,  1,  0   // 240 -> 255
        };
        
        for (int i = 0; i < lut.length; i++)
        {
            lut[i] /= 8.0;
        }
        
        return lut;
    }

    /**
     * Computes the look-up table for measuring Euler number in binary 3D image,
     * for the 26-connectivity.
     * 
     * @return a look-up-table with 256 entries 
     */
    private static final double[] eulerNumberLutC26()
    {
        double[] lut = new double[]{
             0,  1,  1,  0,   1,  0, -2, -1,   1, -2,  0, -1,   0, -1, -1,  0,  //   0 ->  15
             1,  0, -2, -1,  -2, -1, -1, -2,  -6, -3, -3, -2,  -3, -2,  0, -1,  //  16 ->  31
             1, -2,  0, -1,  -6, -3, -3, -2,  -2, -1, -1, -2,  -3,  0, -2, -1,  //  32 ->  47
             0, -1, -1,  0,  -3, -2,  0, -1,  -3,  0, -2, -1,   0, +1, +1,  0,  //  48 ->  63
             1, -2, -6, -3,   0, -1, -3, -2,  -2, -1, -3,  0,  -1, -2, -2, -1,  //  64 ->  79
             0, -1, -3, -2,  -1,  0,  0, -1,  -3,  0,  0,  1,  -2, -1,  1,  0,  //  80 ->  95
            -2, -1, -3,  0,  -3,  0,  0,  1,  -1,  4,  0,  3,   0,  3,  1,  2,  //  96 -> 111
            -1, -2, -2, -1,  -2, -1,  1,  0,   0,  3,  1,  2,   1,  2,  2,  1,  // 112 -> 127
            
             1, -6, -2, -3,  -2, -3, -1,  0,   0, -3, -1, -2,  -1, -2, -2, -1,  // 128 -> 143
            -2, -3, -1,  0,  -1,  0,  4,  3,  -3,  0,  0,  1,   0,  1,  3,  2,  // 144 -> 159
             0, -3, -1, -2,  -3,  0,  0,  1,  -1,  0,  0, -1,  -2,  1, -1,  0,  // 160 -> 175
            -1, -2, -2, -1,   0,  1,  3,  2,  -2,  1, -1,  0,   1,  2,  2,  1,  // 176 -> 191
             0, -3, -3,  0,  -1, -2,  0,  1,  -1,  0, -2,  1,   0, -1, -1,  0,  // 192 -> 207
            -1, -2,  0,  1,  -2, -1,  3,  2,  -2,  1,  1,  2,  -1,  0,  2,  1,  // 208 -> 223
            -1,  0, -2,  1,  -2,  1,  1,  2,  -2,  3, -1,  2,  -1,  2,  0,  1,  // 224 -> 239
             0, -1, -1,  0,  -1,  0,  2,  1,  -1,  2,  0,  1,   0,  1,  1,  0   // 240 -> 255
        };
        
        for (int i = 0; i < lut.length; i++)
        {
            lut[i] /= 8.0;
        }
        
        return lut;
    }

    /**
	 * Returns the "inner volume" of the 3D image, i.e. the calibrated volume of
	 * the image without taking into account the voxels at the border of the
	 * image.
	 * 
	 * @param image
	 *            the input 3D image
	 * @param calib
	 *            the spatial calibration of the image
	 * @return the calibrated volume of the portion of image witout border
	 *         voxels
	 */
    public static final double samplingVolume(ImageStack image, Calibration calib)
    {
        // size of image
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        int sizeZ = image.getSize();
        return (sizeX - 1) * calib.pixelWidth * (sizeY - 1) * calib.pixelHeight * (sizeZ - 1) * calib.pixelDepth;   
    }
    
    /**
     * Private constructor to prevent instantiation.
     */
    private IntrinsicVolumes3DUtils()
    {   
    }
    
    
}
