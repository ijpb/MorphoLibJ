/**
 * 
 */
package inra.ijpb.measure;

import java.util.ArrayList;
import java.util.HashMap;

import ij.IJ;
import ij.ImageStack;
import ij.measure.Calibration;
import inra.ijpb.geometry.Vector3D;
import inra.ijpb.label.LabelImages;

/**
 * Computation of intrinsic volumes (volume, surface area, Euler number) for
 * binary or label 3D images.
 *
 * This class provides only static methods. 
 * 
 * @author dlegland
 *
 */
public class IntrinsicVolumes3D
{
	// ==================================================
    // Static methods
	
	/**
	 * Measures the volume of a single region within a 3D binary image.
	 * 
	 * @param image
	 *            the binary image containing the region
	 * @param calib
	 *            the spatial calibration of the image
	 * @return the volume of the region in the image
	 */
	public static final double volume(ImageStack image, Calibration calib)
	{
        // pre-compute the volume of individual voxel
        double voxelVolume = calib.pixelWidth * calib.pixelHeight * calib.pixelDepth;
        
		// count non-zero voxels
		int voxelCount = voxelCount(image);

		// convert voxel counts to particle volumes
		double volume = voxelCount * voxelVolume;
		return volume;
	}
	
	/**
	 * Counts the number of voxels greater than zero within the image.
	 * 
	 * @param image
	 *            the input3D image, assumed to be binary
	 * @return the number of voxels greater than zero
	 */
	private static final int voxelCount(ImageStack image)
	{
		// size of image
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		// iterate on image voxels
		int count = 0;
		for (int z = 0; z < sizeZ; z++) 
        {
        	for (int y = 0; y < sizeY; y++)
        	{
        		for (int x = 0; x < sizeX; x++)
        		{
        			if(image.getVoxel(x, y, z) > 0)
        			{
        				count++;
        			}
        		}
        	}
        }
        
		return count;
	}
	
	/**
	 * Measures the volume of each region within a 3D label image.
	 * 
	 * @param labelImage
	 *            image containing the label of each particle
	 * @param labels
	 *            the set of labels for which volume has to be computed
	 * @param calib
	 *            the spatial calibration of the image
	 * @return the volume of each particle in the image
	 */
	public static final double[] volumes(ImageStack labelImage, int[] labels, Calibration calib)
	{
        // create associative array to know index of each label
		int nLabels = labels.length;
        
        // pre-compute the volume of individual voxel
        double voxelVolume = calib.pixelWidth * calib.pixelHeight * calib.pixelDepth;
        
        // initialize result
		int[] voxelCounts = LabelImages.voxelCount(labelImage, labels);

		// convert voxel counts to particle volumes
		double[] volumes = new double[nLabels];
		for (int i = 0; i < nLabels; i++) 
		{
			volumes[i] = voxelCounts[i] * voxelVolume;
		}
        return volumes;
	}
	
	/**
	 * Measures the volume density of a single region within a 3D binary image.
	 * 
	 * @param image
	 *            the binary image containing the region
	 * @return the volume density of the region within the image
	 */
	public static final double volumeDensity(ImageStack image)
	{
		// count non-zero voxels
		int voxelCount = voxelCount(image);

		// Normalizes voxel count by imag volume.
		int voxelNumber = image.getWidth() * image.getWidth() * image.getSize();
		return voxelCount / voxelNumber;
	}

	/**
	 * Computes surface area of a single region within a 3D binary image.
	 * 
	 * Uses discretization of the Crofton formula, that consists in computing
	 * numbers of intersections with lines of various directions.
	 * 
	 * @param image
	 *            image containing the label of each particle
	 * @param calib
	 *            the spatial calibration of the image
	 * @param nDirs
	 *            the number of directions to consider, either 3 or 13
	 * @return the surface area of each particle in the image
	 */
	public static final double surfaceArea(ImageStack image, Calibration calib, int nDirs)
	{
		// pre-compute LUT corresponding to resolution and number of directions
		double[] surfLut = surfaceAreaLut(calib, nDirs);

		// Compute index of each 2x2x2 binary voxel configuration, associate LUT
		// contribution, and sum up for each label
		return sumOfLutContributions(image, surfLut);
	}
	

	/**
	 * Computes surface area for each region within a label image.
	 * 
	 * Uses discretization of the Crofton formula, that consists in computing
	 * numbers of intersections with lines of various directions.
	 * 
	 * @param image
	 *            image containing the label of each particle
	 * @param labels
	 *            the set of labels in the image
	 * @param calib
	 *            the spatial calibration of the image
	 * @param nDirs
	 *            the number of directions to consider, either 3 or 13
	 * @return the surface area of each region within the image
	 */
	public static final double[] surfaceAreas(ImageStack image, int[] labels, 
			Calibration calib, int nDirs)
	{
		// pre-compute LUT corresponding to resolution and number of directions
		double[] surfLut = surfaceAreaLut(calib, nDirs);

		// Compute index of each 2x2x2 binary voxel configuration, associate LUT
		// contribution, and sum up for each label
		return sumOfLutContributions(image, labels, surfLut);
	}
	
	/**
	 * Computes surface area density of a single region within a 3D binary image.
	 * 
	 * Uses discretization of the Crofton formula, that consists in computing
	 * numbers of intersections with lines of various directions.
	 * 
	 * @param image
	 *            image containing the label of each particle
	 * @param calib
	 *            the spatial calibration of the image
	 * @param nDirs
	 *            the number of directions to consider, either 3 or 13
	 * @return the surface area density of the binary phase within the image
	 */
	public static final double surfaceAreaDensity(ImageStack image, Calibration calib, int nDirs)
	{
		// pre-compute LUT corresponding to resolution and number of directions
		double[] surfLut = surfaceAreaLut(calib, nDirs);

		// Compute index of each 2x2x2 binary voxel configuration, associate LUT
		// contribution, and sum up for each label
		double surf = innerSumOfLutContributions(image, surfLut);
		
		double vol = samplingVolume(image, calib);
		return surf / vol;
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
	private final static double[] surfaceAreaLut(Calibration calib, int nDirs) 
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
	 * Computes the Look-up table that is used to compute surface area using three directions.
	 * 
	 * @param calib
	 *            the spatial calibration of the image
	 * @param nDirs
	 *            the number of directions to consider, either 3 or 13
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
	 * Computes the Look-up table that is used to compute surface area.
	 * 
	 * @param calib
	 *            the spatial calibration of the image
	 * @param nDirs
	 *            the number of directions to consider, either 3 or 13
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
		double[] weights = computeDirectionWeights3d13(calib);
	
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
	 * Helper function that computes the sphericity index of 3D particles, based
	 * on the value of volume and surface area.
	 * 
	 * The sphericity is computed using the following formula: <code>
	 * sphericity = 36 * PI * V^2 / S^3
	 * </code>
	 * 
	 * A perfect ball would have a sphericity index close to 1, a very complex
	 * particle will present a lower sphericity index.
	 * 
	 * @param volumes
	 *            the volume of each particle
	 * @param surfaces
	 *            the surface area of each particle
	 * @return the sphericity index of each particle
	 * 
	 * @see #surfaceAreas(ij.ImageStack, int[], ij.measure.Calibration, int)
	 * @see #volumes(ij.ImageStack, int[], ij.measure.Calibration)
	 */
	public final static double[] sphericity(double[] volumes, double[] surfaces) 
	{
		int n = volumes.length;
		if (surfaces.length != n) 
		{
			throw new IllegalArgumentException("Volume and surface arrays must have the same length");
		}
		
		// normalization constant such that sphere has sphericity equal to 1 
		double c = 36 * Math.PI;

		// Compute sphericity of each label
		double[] sphericities = new double[n];
		for (int i = 0; i < n; i++) 
		{
			double v = volumes[i];
			double s = surfaces[i];
			sphericities[i] = c * v * v / (s * s * s);
		}
		
		return sphericities;
	}
	
	/**
	 * Computes mean breadth of a single region within a 3D binary image.
	 * 
	 * The mean breadth is proportional to the integral of mean curvature: mb =
	 * 2*pi*IMC.
	 * 
	 * @param image
	 *            image containing the label of each particle
	 * @param calib
	 *            the spatial calibration of the image
	 * @param nDirs
	 *            the number of directions to consider, either 3 or 13
	 * @return the mean breadth of the binary region within the image
	 */
	public static final double meanBreadth(ImageStack image, Calibration calib, int nDirs)
	{
		// pre-compute LUT corresponding to resolution and number of directions
		double[] breadthLut = meanBreadthLut(calib, nDirs);

		// Compute index of each 2x2x2 binary voxel configuration, associate LUT
		// contribution, and sum up for each label
		return sumOfLutContributions(image, breadthLut);
	}

	/**
	 * Computes the mean breadth of each region within a label image. The mean
	 * breadth is proportional to the integral of mean curvature: mb = 2*pi*IMC.
	 * 
	 * Uses discretization of the Crofton formula, that consists in computing
	 * euler number of intersection with planes of various orientations.
	 * 
	 * @param image
	 *            image containing the label of each particle
	 * @param labels
	 *            the set of labels in the image
	 * @param calib
	 *            the spatial calibration of the image
	 * @param nDirs
	 *            the number of directions to consider, either 3 or 13
	 * @return the mean breadth of each region within the image
	 */
	public static final double[] meanBreadths(ImageStack image, int[] labels, 
			Calibration calib, int nDirs)
	{
		// pre-compute LUT corresponding to resolution and number of directions
		double[] meanBreadthLut = meanBreadthLut(calib, nDirs);

		// Compute index of each 2x2x2 binary voxel configuration, associate LUT
		// contribution, and sum up for each label
		return sumOfLutContributions(image, labels, meanBreadthLut);
	}

	/**
	 * Computes mean breadth density of a single region within a 3D binary image.
	 * 
	 * Uses discretization of the Crofton formula, that consists in computing
	 * euler number of intersection with planes of various orientations.
	 * 
	 * @param image
	 *            image containing the label of each particle
	 * @param calib
	 *            the spatial calibration of the image
	 * @param nDirs
	 *            the number of directions to consider, either 3 or 13
	 * @return the surface area density of the binary phase within the image
	 */
	public static final double meanBreadthDensity(ImageStack image, Calibration calib, int nDirs)
	{
		// pre-compute LUT corresponding to resolution and number of directions
		double[] meanBreadthLut = meanBreadthLut(calib, nDirs);

		// Compute index of each 2x2x2 binary voxel configuration, associate LUT
		// contribution, and sum up for each label
		double surf = innerSumOfLutContributions(image, meanBreadthLut);
		
		double vol = samplingVolume(image, calib);
		return surf / vol;
	}

	private static final double[] meanBreadthLut(Calibration calib, int nDirs)
	{
		if (nDirs == 3)
		{
			return meanBreadthLutD3(calib);
		}
		else if (nDirs == 13)
		{
			return meanBreadthLutD13(calib);
		}
		else
		{
			throw new IllegalArgumentException("Number of directions should be either 3 or 13, not " + nDirs);
		}
	}
	
	private static final double[] meanBreadthLutD3(Calibration calib)
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
		        double f1 = eulerContribTile2dC8(face1, d3, d2);
		        double f2 = eulerContribTile2dC8(face2, d3, d1);
		        double f3 = eulerContribTile2dC8(face3, d2, d1);
		        
		        // Uses only 3 isothetic directions.
		        // divide by 6. Divide by 3 because of averaging on directions,
		        // and divide by 2 because each face is visible on 2 config.
		        lut[iConfig] += vol * (f1/a1 + f2/a2 + f3/a3) / 6;
		    }
		}
		
		return lut;		
	}

	private static final double[] meanBreadthLutD13(Calibration calib)
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
				d3 * d12, d2 * d13,	d1 * d23, 
				2 * Math.sqrt(s * (s - d12) * (s - d13) * (s - d23))};
		
		// direction weights corresponding to area of Voronoi partition on the
		// unit sphere, when germs are the 26 directions on the unit cube
		// Sum of (c1+c2+c3 + c4*2+c5*2+c6*2 + c7*4) equals 1.
		double[] weights = computeDirectionWeights3d13(calib);

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
		        diams[0] = eulerContribTile2dC8(face1, d3, d2) / 2.0;
		        diams[1] = eulerContribTile2dC8(face2, d3, d1) / 2.0;
		        diams[2] = eulerContribTile2dC8(face3, d2, d1) / 2.0;
		        
		        // create 2D faces for direction normal to square diagonals
		        // use only the half
		        boolean[] face4 = new boolean[]{im[p1][p2][p3], im[1-p1][1-p2][p3], im[p1][p2][1-p3], im[1-p1][1-p2][1-p3]};
		        boolean[] face6 = new boolean[]{im[p1][p2][p3], im[1-p1][p2][1-p3], im[p1][1-p2][p3], im[1-p1][1-p2][1-p3]};
		        boolean[] face8 = new boolean[]{im[p1][p2][p3], im[p1][1-p2][1-p3], im[1-p1][p2][p3], im[1-p1][1-p2][1-p3]};

		        // compute contribution of voxel on each 2D face
		        diams[3] = eulerContribTile2dC8(face4, d12, d3);
		        diams[4] = eulerContribTile2dC8(face6, d13, d2);
		        diams[5] = eulerContribTile2dC8(face8, d23, d1);

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
	 * Return an array with seven values corresponding the unique direction 
	 * vectors obtained with 13 directions.
	 */
	private static final double[] computeDirectionWeights3d13(Calibration calib) 
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
	 * a triangular face.
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

	/**
	 * Computes Euler number of the region within the binary image,
	 * using the specified connectivity.
	 * 
	 * @param image
	 *            the input 3D binary image
	 * @param conn
	 *            the connectivity to use (either 6 or 26)
	 * @return the Euler-Poincare characteristic of the region
	 */
	public static final double eulerNumber(ImageStack image, int conn)
	{
        // pre-compute LUT corresponding to the chosen connectivity
		double[] eulerLut = eulerNumberLut(conn);

		// Compute index of each 2x2x2 binary voxel configuration, associate LUT
		// contribution, and sum up for each label
		return sumOfLutContributions(image, eulerLut);
	}
	
	/**
	 * Computes Euler number for each label given in the "labels" argument,
	 * using the specified connectivity.
	 * 
	 * @param image
	 *            the input 3D label image (with labels having integer values)
	 * @param labels
	 *            the set of unique labels in image
	 * @param conn
	 *            the connectivity to use (either 6 or 26)
	 * @return the Euler-Poincare characteristic of each region
	 */
	public final static double[] eulerNumbers(ImageStack image, int[] labels,
			int conn)
	{    
        // pre-compute LUT corresponding to the chosen connectivity
		double[] eulerLut = eulerNumberLut(conn);

		// Compute index of each 2x2x2 binary voxel configuration, associate LUT
		// contribution, and sum up for each label
		IJ.showStatus("Euler Number...");
		return sumOfLutContributions(image, labels, eulerLut);
	}
	
	/**
	 * Computes Euler number of the region within the binary image,
	 * using the specified connectivity.
	 * 
	 * @param image
	 *            the input 3D binary image
	 * @param calib
	 *            the spatial calibration of the image
	 * @param conn
	 *            the connectivity to use (either 6 or 26)
	 * @return the Euler-Poincare characteristic of the region
	 */
	public static final double eulerNumberDensity(ImageStack image, Calibration calib, int conn)
	{
		// pre-compute LUT corresponding to resolution and number of directions
		double[] eulerLut = eulerNumberLut(conn);

		// Compute index of each 2x2x2 binary voxel configuration, associate LUT
		// contribution, and sum up for each label
		double euler = innerSumOfLutContributions(image, eulerLut);
		
		double vol = samplingVolume(image, calib);
		return euler / vol;
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
	private static final double[] eulerNumberLut(int conn)
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
			 0,  1,  1,  0,   1,  0,  2, -1,   1,  2,  0, -1,   0, -1, -1,  0, 	//   0 ->  15
			 1,  0,  2, -1,   2, -1,  3, -2,   2,  1,  1, -2,   1, -2,  0, -1, 	//  16 ->  31
			 1,  2,  0, -1,   2,  1,  1, -2,   2,  3, -1, -2,   1,  0, -2, -1,	//  32 ->  47
			 0, -1, -1,  0,   1, -2,  0, -1,   1,  0, -2, -1,   0, -3, -3,  0,  //  48 ->  63
			 1,  2,  2,  1,   0, -1,  1, -2,   2,  3,  1,  0,  -1, -2, -2, -1,	//  64 ->  79
			 0, -1,  1, -2,  -1,  0,  0, -1,   1,  0,  0, -3,  -2, -1, -3,  0,	//  80 ->  95
			 2,  3,  1,  0,   1,  0,  0, -3,   3,  4,  0, -1,   0, -1, -3, -2,	//  96 -> 111
			-1, -2, -2, -1,  -2, -1, -3,  0,   0, -1, -3, -2,  -3, -2, -6,  1, 	// 112 -> 127
			
			 1,  2,  2,  1,   2,  1,  3,  0,   0,  1, -1, -2,  -1, -2, -2, -1,	// 128 -> 145
			 2,  1,  3,  0,   3,  0,  4, -1,   1,  0,  0, -3,   0, -3, -1, -2,	// 146 -> 159
			 0,  1, -1, -2,   1,  0,  0, -3,  -1,  0,  0, -1,  -2, -3, -1,  0,	// 160 -> 175
			-1, -2, -2, -1,   0, -3, -1, -2,  -2, -3, -1,  0,  -3, -6, -2,  1,	// 176 -> 191
			 0,  1,  1,  0,  -1, -2,  0, -3,  -1,  0, -2, -3,   0, -1, -1,  0,	// 192 -> 207
			-1, -2,  0, -3,  -2, -1, -1, -2,  -2, -3, -3, -6,  -1,  0, -2,  1,	// 208 -> 223
			-1,  0, -2, -3,  -2, -3, -3, -6,  -2, -1, -1, -2,  -1, -2,  0,  1,	// 224 -> 239
			 0, -1, -1,  0,  -1,  0, -2,  1,  -1, -2,  0,  1,   0,  1,  1,  0	// 240 -> 255
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
			 0,  1,  1,  0,   1,  0, -2, -1,   1,  2,  0, -1,   0, -1, -1,  0, 	//   0 ->  15
			 1,  0, -2, -1,   2, -1, -1, -2,  -6, -3, -3, -2,  -3, -2,  0, -1, 	//  16 ->  31
			 1, -2,  0, -1,  -6, -3, -3, -2,  -2, -1, -1, -2,  -3,  0, -2, -1,	//  32 ->  47
			 0, -1, -1,  0,  -3, -2,  0, -1,  -3,  0, -2, -1,   0, +1, +1,  0,  //  48 ->  63
			 1, -2, -6, -3,   0, -1, -3, -2,  -2, -1, -3,  0,  -1, -2, -2, -1,	//  64 ->  79
			 0, -1, -3, -2,  -1,  0,  0, -1,  -3,  0,  0,  1,  -2, -1,  1,  0,	//  80 ->  95
			-2, -1, -3,  0,  -3,  0,  0,  1,  -1,  4,  0,  3,   0,  3,  1,  2,	//  96 -> 111
			-1, -2, -2, -1,  -2, -1,  1,  0,   0,  3,  1,  2,   1,  2,  2,  1, 	// 112 -> 127
			
			 1, -6, -2, -3,  -2, -3, -1,  0,   0, -3, -1, -2,  -1, -2, -2, -1,	// 128 -> 143
			-2, -3, -1,  0,  -1,  0,  4,  3,  -3,  0,  0,  1,   0,  1,  3,  2,	// 144 -> 159
			 0, -3, -1, -2,  -3,  0,  0,  1,  -1,  0,  0, -1,  -2,  1, -1,  0,	// 160 -> 175
			-1, -2, -2, -1,   0,  1,  3,  2,  -2,  1, -1,  0,   1,  2,  2,  1,	// 176 -> 191
			 0, -3, -3,  0,  -1, -2,  0,  1,  -1,  0, -2,  1,   0, -1, -1,  0,	// 192 -> 207
			-1, -2,  0,  1,  -2, -1,  3,  2,  -2,  1,  1,  2,  -1,  0,  2,  1,	// 208 -> 223
			-1,  0, -2,  1,  -2,  1,  1,  2,  -2,  3, -1,  2,  -1,  2,  0,  1,	// 224 -> 239
			 0, -1, -1,  0,  -1,  0,  2,  1,  -1,  2,  0,  1,   0,  1,  1,  0	// 240 -> 255
		};
		
		for (int i = 0; i < lut.length; i++)
		{
			lut[i] /= 8.0;
		}
		
		return lut;
	}

	/**
	 * Applies a look-up-table for each of the 2x2x2 voxel configurations
	 * containing at least one voxel of the input binary image, and returns the
	 * sum of contributions for each label.
	 * 
	 * This method is used for computing Euler number and surface area from
	 * binary images.
	 * 
	 * @param image
	 *            the input 3D binary image
	 * @param lut
	 *            the look-up-table containing the measure contribution for each
	 *            of the 256 configuration of 8 voxels
	 * @return the sum of measure contributions for each label
	 * 
	 * @see #surfaceAreaCrofton(ImageStack, int[], double[], int)
	 * @see #eulerNumber(ImageStack, int[], int)
	 */
	private final static double sumOfLutContributions(ImageStack image, double[] lut)
	{   
		// Algorithm:
		// iterate on configurations of 2-by-2-by-2 voxels containing on voxel of 3D image. 
		// For each configuration, identify the labels within the configuration.
		// For each label, compute the equivalent binary configuration index, 
		// and adds is contribution to the measure associated to the label. 
		
        double result = 0;

		// size of image
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		// values of pixels within current 2-by-2-by-2 configuration
		boolean[] configValues = new boolean[8];
		
		// Iterate over all 2-by-2-by-2 configurations containing at least one
		// voxel within the image.
		// Current pixel is the lower-right pixel in configuration
		// (corresponding to b111).
        for (int z = 0; z < sizeZ + 1; z++) 
        {
			for (int y = 0; y < sizeY + 1; y++) 
        	{
	        	// initialize left voxels
				configValues[0] = false;
				configValues[2] = false;
				configValues[4] = false;
				configValues[6] = false;

        		for (int x = 0; x < sizeX + 1; x++) 
        		{
            		// update pixel values of configuration
        			if (x < sizeX)
        			{
    					configValues[1] = y > 0 & z > 0 ? image.getVoxel(x, y - 1, z - 1) > 0 : false;
    					configValues[3] = y < sizeY & z > 0 ? image.getVoxel(x, y, z - 1) > 0 : false;
    					configValues[5] = y > 0 & z < sizeZ ? image.getVoxel(x, y - 1, z) > 0 : false;
    					configValues[7] = y < sizeY & z < sizeZ ? image.getVoxel(x, y, z) > 0 : false;
        			}
        			else
        			{
						// if reference voxel outside of image, the four new
						// values are outside, and are set to background
        				configValues[1] = configValues[3] = configValues[5] = configValues[7] = false;   
        			}

        			// Compute index of local configuration
        			int index = 0;
					index += configValues[0] ?   1 : 0;
					index += configValues[1] ?   2 : 0;
					index += configValues[2] ?   4 : 0;
					index += configValues[3] ?   8 : 0;
					index += configValues[4] ?  16 : 0;
					index += configValues[5] ?  32 : 0;
					index += configValues[6] ?  64 : 0;
					index += configValues[7] ? 128 : 0;

        			// add the contribution of the configuration to the measure
        			result += lut[index];
					
					// update values of configuration for next iteration
					configValues[0] = configValues[1];
					configValues[2] = configValues[3];
					configValues[4] = configValues[5];
					configValues[6] = configValues[7];
        		}
        	}
        }
        
        // reset progress display
        return result;
	}
	
	/**
	 * Applies a look-up-table for each of the 2x2x2 voxel configurations
	 * containing at least one voxel of the input image, and returns the sum of
	 * contributions for each label.
	 * 
	 * This method is used for computing Euler number and surface area.
	 * 
	 * @param image
	 *            the input 3D image of labels
	 * @param labels
	 *            the set of labels to process
	 * @param lut
	 *            the look-up-table containing the measure contribution for each
	 *            of the 256 configuration of 8 voxels
	 * @return the sum of measure contributions for each label
	 * 
	 * @see #surfaceAreaCrofton(ImageStack, int[], double[], int)
	 * @see #eulerNumber(ImageStack, int[], int)
	 */
	private final static double[] sumOfLutContributions(ImageStack image, int[] labels, 
			double[] lut)
	{   
		// Algorithm:
		// iterate on configurations of 2-by-2-by-2 voxels containing on voxel of 3D image. 
		// For each configuration, identify the labels within the configuration.
		// For each label, compute the equivalent binary configuration index, 
		// and adds is contribution to the measure associated to the label. 
		
        // create associative array to know index of each label
		HashMap<Integer, Integer> labelIndices = LabelImages.mapLabelIndices(labels);

		// initialize the result array containing one measure for each label
		int nLabels = labels.length;
        double[] measures = new double[nLabels];

		// size of image
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		// for each configuration of 2x2x2 voxels, we identify the labels
		ArrayList<Integer> localLabels = new ArrayList<Integer>(8);
		
		// values of pixels within current 2-by-2-by-2 configuration
		int[] configValues = new int[8];
		
		// Iterate over all 2-by-2-by-2 configurations containing at least one
		// voxel within the image.
		// Current pixel is the lower-right pixel in configuration
		// (corresponding to b111).
        for (int z = 0; z < sizeZ + 1; z++) 
        {
			for (int y = 0; y < sizeY + 1; y++) 
        	{
	        	// initialize left voxels
				configValues[0] = 0;
				configValues[2] = 0;
				configValues[4] = 0;
				configValues[6] = 0;

        		for (int x = 0; x < sizeX + 1; x++) 
        		{
            		// update pixel values of configuration
        			if (x < sizeX)
        			{
    					configValues[1] = y > 0 & z > 0 ? (int) image.getVoxel(x, y - 1, z - 1) : 0;
    					configValues[3] = y < sizeY & z > 0 ? (int) image.getVoxel(x, y, z - 1) : 0;
    					configValues[5] = y > 0 & z < sizeZ ? (int) image.getVoxel(x, y - 1, z) : 0;
    					configValues[7] = y < sizeY & z < sizeZ ? (int) image.getVoxel(x, y, z) : 0;
        			}
        			else
        			{
						// if reference voxel outside of image, the four new
						// values are outside, and are set to zero
        				configValues[1] = configValues[3] = configValues[5] = configValues[7] = 0;   
        			}

    				// identify labels in current config
        			localLabels.clear();
    				for (int label : configValues)
    				{
    					if (label == 0)
    						continue;
    					// keep only one instance of each label
    					if (!localLabels.contains(label))
    						localLabels.add(label);
    				}

					// if there is no label in the local configuration then the
					// contribution is zero
					if (localLabels.size() == 0) 
					{
						continue;
					}
					
					// For each label, compute binary confi
					for (int label : localLabels) 
					{
	        			// Compute index of local configuration
	        			int index = 0;
						index += configValues[0] == label ?   1 : 0;
						index += configValues[1] == label ?   2 : 0;
						index += configValues[2] == label ?   4 : 0;
						index += configValues[3] == label ?   8 : 0;
						index += configValues[4] == label ?  16 : 0;
						index += configValues[5] == label ?  32 : 0;
						index += configValues[6] == label ?  64 : 0;
						index += configValues[7] == label ? 128 : 0;

						// retrieve label index from label value
	        			int labelIndex = labelIndices.get(label);

	        			// add the contribution of the configuration to the
						// accumulator for the label
	        			measures[labelIndex] += lut[index];
					}
					
					// update values of configuration for next iteration
					configValues[0] = configValues[1];
					configValues[2] = configValues[3];
					configValues[4] = configValues[5];
					configValues[6] = configValues[7];
        		}
        	}
        }
        
        // reset progress display
        return measures;
	}
	
    /**
	 * Applies a look-up-table for each of the 2x2x2 voxel configurations
	 * containing at least one voxel of the input binary image, and returns the sum of
	 * contributions for each label.
	 * 
	 * This method is used for computing densities of Euler number and surface area from binary images.
	 * 
	 * @param image
	 *            the input 3D image of labels
	 * @param lut
	 *            the look-up-table containing the measure contribution for each
	 *            of the 256 configuration of 8 voxels
	 * @return the sum of measure contributions for each label
	 * 
	 * @see #surfaceAreaCrofton(ImageStack, int[], double[], int)
	 * @see #eulerNumber(ImageStack, int[], int)
	 */
	private final static double innerSumOfLutContributions(ImageStack image, double[] lut)
	{   
		// Algorithm:
		// iterate on configurations of 2-by-2-by-2 voxels fully contained within 3D image. 
		// For each configuration, identify the labels within the configuration.
		// For each label, compute the equivalent binary configuration index, 
		// and adds is contribution to the measure associated to the label. 
		
	    double result = 0;
	
		// size of image
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
	
		// values of pixels within current 2-by-2-by-2 configuration
		boolean[] configValues = new boolean[8];
		
		// Iterate over all 2-by-2-by-2 configurations containing at least one
		// voxel within the image.
		// Current pixel is the lower-right voxel in configuration
		// (corresponding to b111).
	    for (int z = 1; z < sizeZ; z++) 
	    {
			for (int y = 1; y < sizeY; y++) 
	    	{
	        	// initialize left voxels
				configValues[0] = image.getVoxel(0, y - 1, z - 1) > 0;
				configValues[2] = image.getVoxel(0, y, z - 1) > 0;
				configValues[4] = image.getVoxel(0, y - 1, z) > 0;
				configValues[6] = image.getVoxel(0, y, z) > 0;
	
	    		for (int x = 1; x < sizeX; x++) 
	    		{
	        		// update pixel values of configuration
	    			configValues[1] = image.getVoxel(x, y - 1, z - 1) > 0;
	    			configValues[3] = image.getVoxel(x, y, z - 1) > 0;
	    			configValues[5] = image.getVoxel(x, y - 1, z) > 0;
	    			configValues[7] = image.getVoxel(x, y, z) > 0;
	
	    			// Compute index of local configuration
	    			int index = 0;
					index += configValues[0] ?   1 : 0;
					index += configValues[1] ?   2 : 0;
					index += configValues[2] ?   4 : 0;
					index += configValues[3] ?   8 : 0;
					index += configValues[4] ?  16 : 0;
					index += configValues[5] ?  32 : 0;
					index += configValues[6] ?  64 : 0;
					index += configValues[7] ? 128 : 0;
	
	    			// add the contribution of the configuration to the measure
	    			result += lut[index];
					
					// update values of configuration for next iteration
					configValues[0] = configValues[1];
					configValues[2] = configValues[3];
					configValues[4] = configValues[5];
					configValues[6] = configValues[7];
	    		}
	    	}
	    }
	    
	    // reset progress display
	    return result;
	}

	private static final double samplingVolume(ImageStack image, Calibration calib)
	{
		// size of image
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		return (sizeX - 1) * calib.pixelWidth * (sizeY - 1) * calib.pixelHeight * (sizeZ - 1) * calib.pixelDepth;	
	}
	

	// ==================================================
    // Constructor

	/**
     * Private constructor to prevent instantiation
     */
    private IntrinsicVolumes3D()
    {
    }
}
