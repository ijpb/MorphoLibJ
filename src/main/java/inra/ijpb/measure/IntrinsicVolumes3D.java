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
	 * @return the surface area of each particle in the image
	 */
	public static final double[] surfaceAreas(ImageStack image, int[] labels, 
			Calibration calib, int nDirs)
	{
		// pre-compute LUT corresponding to resolution and number of directions
		double[] surfLut = computeSurfaceAreaLut(calib, nDirs);

		// Compute index of each 2x2x2 binary voxel configuration, associate LUT
		// contribution, and sum up for each label
		return sumOfLutContributions(image, labels, surfLut);
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
	private final static double[] computeSurfaceAreaLut(Calibration calib, int nDirs) 
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
	
		// direction weightsn corresponding to area of voronoi partition on the
		// unit sphere, when germs are the 26 directions on the unit cube
		// Sum of (c1+c2+c3 + c4*2+c5*2+c6*2 + c7*4) equals 1.
		double c1, c2, c3, c4, c5, c6, c7;
		if (d1 == d2 && d2 == d3) 
		{
			// in case of cubic voxels, uses pre-computed weights
			c1 = 0.04577789120476 * 2;  // Ox
			c2 = 0.04577789120476 * 2;  // Oy
			c3 = 0.04577789120476 * 2;  // Oz
			c4 = 0.03698062787608 * 2;  // Oxy
			c5 = 0.03698062787608 * 2;  // Oxz
			c6 = 0.03698062787608 * 2;  // Oyz
			c7 = 0.03519563978232 * 2;  // Oxyz
		}
		else 
		{
			// If resolution is not the same in each direction, needs to 
			// recomputes the weights assigned to each direction
			double[] weights = computeDirectionWeights3d13(calib);
			c1 = weights[0];
			c2 = weights[1];
			c3 = weights[2];
			c4 = weights[3];
			c5 = weights[4];
			c6 = weights[5];
			c7 = weights[6];
		}

		// initialize output array (256 configurations in 3D)
		int nbConfigs = 256;
		double[] tab = new double[nbConfigs];

		// loop for each tile configuration
		for (int i = 0; i < nbConfigs; i++)
		{
		    // create the binary image representing the 2x2x2 tile
		    boolean[][][] im = new boolean[2][2][2];
		    im[0][0][0] = (i & 1) > 0;
		    im[0][0][1] = (i & 2) > 0;
		    im[0][1][0] = (i & 4) > 0;
		    im[0][1][1] = (i & 8) > 0;
		    im[1][0][0] = (i & 16) > 0;
		    im[1][0][1] = (i & 32) > 0;
		    im[1][1][0] = (i & 64) > 0;
		    im[1][1][1] = (i & 128) > 0;
		    
	        // contributions for isothetic directions
		    double ke1, ke2, ke3;
		    
		    // contributions for diagonal directions
            double ke4, ke5, ke6, ke7;
            
            // iterate over the 8 voxels within the configuration
		    for (int z = 0; z < 2; z++) 
		    {
			    for (int y = 0; y < 2; y++) 
			    {
				    for (int x = 0; x < 2; x++) 
				    {
				    	if (!im[z][y][x])
				    		continue;
					    ke1 = im[z][y][1-x] ? 0 : vol/d1/2;
					    ke2 = im[z][1-y][x] ? 0 : vol/d2/2;
					    ke3 = im[1-z][y][x] ? 0 : vol/d3/2;
					    
					    if (nDirs == 3) 
					    {
				            // For 3 directions, the multiplicity is 4, and is canceled by the
				            // coefficient 4 in the Crofton formula. We just need to average on
				            // directions.
				            tab[i] += (ke1 + ke2 + ke3) / 3;
				            
					    } 
					    else if (nDirs == 13) 
					    {
					    	// diagonals that share a square face
						    ke4 = im[z][1-y][1-x] ? 0 : vol/d12/2;
						    ke5 = im[1-z][y][1-x] ? 0 : vol/d13/2;
						    ke6 = im[1-z][1-y][x] ? 0 : vol/d23/2;
				            
				            // diagonal with opposite vertex of the cube
				            ke7 = im[1-z][1-y][1-x] ? 0 : vol/d123/2; 
				            
				            // Decomposition of Crofton formula on 13 directions
				            tab[i] = tab[i] + 4*(ke1*c1/4 + ke2*c2/4 + ke3*c3/4 + 
				                ke4*c4/2 + ke5*c5/2 + ke6*c6/2 + ke7*c7);
					    } 
				    }
			    }
		    }
		}
		return tab;		
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
	 * @see #surfaceAreas(ImageStack, Calibration, int)
	 * @see #volumes(ImageStack, Calibration, double[])
	 */
	public final static double[] computeSphericity(double[] volumes, double[] surfaces) 
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
        // pre-compute LUT corresponding to resolution and number of directions
		double[] eulerLut = computeEulerNumberLut(conn);

		// Compute index of each 2x2x2 binary voxel configuration, associate LUT
		// contribution, and sum up for each label
		IJ.showStatus("Euler Number...");
		return sumOfLutContributions(image, labels, eulerLut);
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
	private static final double[] computeEulerNumberLut(int conn)
	{
		if (conn == 6)
		{
			return computeEulerNumberLut_C6();			
		}
		else if (conn == 26)
		{
			return computeEulerNumberLut_C26();
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
	private static final double[] computeEulerNumberLut_C6()
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
	private static final double[] computeEulerNumberLut_C26()
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
	 * Applies a look-up-table for each of the 2x2x2 voxel configuration, and
	 * returns the sum of contributions for each label.
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
		// iterate on configurations of 2-by-2-by-2 voxels within 3D image. 
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
		
        for (int z = 0; z < sizeZ - 1; z++) 
        {
        	IJ.showProgress(z, sizeZ);
        	for (int y = 0; y < sizeY - 1; y++) 
        	{
        		for (int x = 0; x < sizeX - 1; x++) 
        		{
        			// identify labels in current config
        			localLabels.clear();
					for (int z2 = z; z2 <= z + 1; z2++) 
					{
						for (int y2 = y; y2 <= y + 1; y2++) 
						{
							for (int x2 = x; x2 <= x + 1; x2++) 
							{
								int label = (int) image.getVoxel(x2, y2, z2);
								// do not consider background
								if (label == 0)
									continue;
								// keep only one instance of each label
								if (!localLabels.contains(label))
									localLabels.add(label);
                			}
            			}
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
	        			index += image.getVoxel(x, y, z) 			== label ? 1 : 0;
	        			index += image.getVoxel(x + 1, y, z) 		== label ? 2 : 0;
	        			index += image.getVoxel(x, y + 1, z) 		== label ? 4 : 0;
	        			index += image.getVoxel(x + 1, y + 1, z) 	== label ? 8 : 0;
	        			index += image.getVoxel(x, y, z + 1) 		== label ? 16 : 0;
	        			index += image.getVoxel(x + 1, y, z + 1) 	== label ? 32 : 0;
	        			index += image.getVoxel(x, y + 1, z + 1) 	== label ? 64 : 0;
	        			index += image.getVoxel(x + 1, y + 1, z + 1) == label ? 128 : 0;

						// add the contribution of the configuration to the
						// accumulator for the label
	        			int labelIndex = labelIndices.get(label);
	        			measures[labelIndex] += lut[index];
					}
        		}
        	}
        }
        
        // reset progress display
		IJ.showStatus("");
    	IJ.showProgress(1);
        return measures;
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
