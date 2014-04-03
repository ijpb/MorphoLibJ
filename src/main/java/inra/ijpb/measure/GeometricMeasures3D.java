/**
 * 
 */
package inra.ijpb.measure;

import ij.IJ;
import ij.ImageStack;
import ij.measure.ResultsTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

import static java.lang.Math.sqrt;
import static java.lang.Math.hypot;
import static java.lang.Math.atan2;
import static java.lang.Math.toDegrees;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Provides a set of static methods to compute geometric measures in 3D binary
 * or label images.
 * 
 * @author David Legland
 *
 */
public class GeometricMeasures3D {

	/**
	 * Compute bounding box of each label in input stack and returns the result
	 * as a ResultsTable.
	 */
	public final static ResultsTable boundingBox(ImageStack labelImage) {
		int[] labels = findAllLabels(labelImage);
		int nbLabels = labels.length;

		double[][] boxes = boundingBox(labelImage, labels);

		// Create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < nbLabels; i++) {
			table.incrementCounter();
			table.addLabel(Integer.toString(labels[i]));
			table.addValue("XMin", boxes[i][0]);
			table.addValue("XMax", boxes[i][1]);
			table.addValue("YMin", boxes[i][2]);
			table.addValue("YMax", boxes[i][3]);
			table.addValue("ZMin", boxes[i][4]);
			table.addValue("ZMax", boxes[i][5]);
		}

		return table;
	}
	
	/**
	 * Compute bounding box of each label in input stack and returns the result
	 * as an array of double for each label.
	 */
	public final static double[][] boundingBox(ImageStack labelImage, int[] labels) {
        // create associative array to know index of each label
		int nLabels = labels.length;
        HashMap<Integer, Integer> labelIndices = new HashMap<Integer, Integer>();
        for (int i = 0; i < nLabels; i++) {
        	labelIndices.put(labels[i], i);
        }

        // initialize result
		double[][] boxes = new double[nLabels][6];
		for (int i = 0; i < nLabels; i++) {
			boxes[i][0] = Double.POSITIVE_INFINITY;
			boxes[i][1] = Double.NEGATIVE_INFINITY;
			boxes[i][2] = Double.POSITIVE_INFINITY;
			boxes[i][3] = Double.NEGATIVE_INFINITY;
			boxes[i][4] = Double.POSITIVE_INFINITY;
			boxes[i][5] = Double.NEGATIVE_INFINITY;
		}

		
		// size of image
		int sizeX = labelImage.getWidth();
		int sizeY = labelImage.getHeight();
		int sizeZ = labelImage.getSize();

		// iterate on image voxels to update bounding boxes
		IJ.showStatus("Compute Bounding boxes");
        for (int z = 0; z < sizeZ; z++) {
        	IJ.showProgress(z, sizeZ);
        	for (int y = 0; y < sizeY; y++) {
        		for (int x = 0; x < sizeX; x++) {
        			int label = (int) labelImage.getVoxel(x, y, z);
					// do not consider background
					if (label == 0)
						continue;
					int labelIndex = labelIndices.get(label);

					// update bounding box of current label
					boxes[labelIndex][0] = min(boxes[labelIndex][0], x);
					boxes[labelIndex][1] = max(boxes[labelIndex][1], x);
					boxes[labelIndex][2] = min(boxes[labelIndex][2], y);
					boxes[labelIndex][3] = max(boxes[labelIndex][3], y);
					boxes[labelIndex][4] = min(boxes[labelIndex][4], z);
					boxes[labelIndex][5] = max(boxes[labelIndex][5], z);
        		}
        	}
        }
        
		IJ.showStatus("");
        return boxes;

	}
	

	public final static ResultsTable volume(ImageStack labelImage, double[] resol) {
		IJ.showStatus("Compute volume...");
		int[] labels = findAllLabels(labelImage);
		int nbLabels = labels.length;

		double[] volumes = volume(labelImage, labels, resol);

		// Create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < nbLabels; i++) {
			table.incrementCounter();
			table.addLabel(Integer.toString(labels[i]));
			table.addValue("Volume", volumes[i]);
		}

		IJ.showStatus("");
		return table;
	}
	
	public final static double[] volume(ImageStack labelImage, int[] labels, double[] resol) {
        // create associative array to know index of each label
		int nLabels = labels.length;
        HashMap<Integer, Integer> labelIndices = new HashMap<Integer, Integer>();
        for (int i = 0; i < nLabels; i++) {
        	labelIndices.put(labels[i], i);
        }

        // pre-compute the volume of individual voxel
        if (resol == null || resol.length < 3) {
        	throw new IllegalArgumentException("Resolution must be a double array of length 3");
        }
        double voxelVolume = resol[0] * resol[1] * resol[2];
        
        // initialize result
		double[] volumes = new double[nLabels];

		// size of image
		int sizeX = labelImage.getWidth();
		int sizeY = labelImage.getHeight();
		int sizeZ = labelImage.getSize();

		// iterate on image voxels
		IJ.showStatus("Measure Volume...");
        for (int z = 0; z < sizeZ; z++) {
        	IJ.showProgress(z, sizeZ);
        	for (int y = 0; y < sizeY; y++) {
        		for (int x = 0; x < sizeX; x++) {
        			int label = (int) labelImage.getVoxel(x, y, z);
					// do not consider background
					if (label == 0)
						continue;
					int labelIndex = labelIndices.get(label);
        			volumes[labelIndex] += voxelVolume;
        		}
        	}
        }
        
		IJ.showStatus("");
        return volumes;
	}
	
	public final static double[] computeSphericity(double[] volumes, double[] surfaces) {
		int n = volumes.length;
		if (surfaces.length != n) {
			throw new IllegalArgumentException("Volume and surface arrays must have the same length");
		}
		
		// normalization constant such that sphere has sphericity equal to 1 
		double c = 36 * Math.PI;

		// Compute sphericity of each label
		double[] sphericities = new double[n];
		for (int i = 0; i < n; i++) {
			double v = volumes[i];
			double s = surfaces[i];
			sphericities[i] = c * v * v / (s * s * s);
		}
		
		return sphericities;
	}
	
	/**
	 * Computes the surface area of each label in the 3D image, using the
	 * specified resolution, and the number of directions.
	 */
	public final static ResultsTable surfaceArea(ImageStack labelImage, double[] resol, int nDirs) {
		IJ.showStatus("Count labels...");
		int[] labels = findAllLabels(labelImage);
		int nbLabels = labels.length;

		double[] surfaces = surfaceAreaByLut(labelImage, labels, resol, nDirs);

		// Create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < nbLabels; i++) {
			table.incrementCounter();
			table.addLabel(Integer.toString(labels[i]));
			table.addValue("Surface", surfaces[i]);
		}

		IJ.showStatus("");
		return table;
	}

	/**
	 * Compute surface area for each label given in the "labels" argument.
	 */
	public final static double[] surfaceAreaByLut(ImageStack image, int[] labels, double[] resol, int nDirs) {
        
        // create associative array to know index of each label
		int nLabels = labels.length;
        HashMap<Integer, Integer> labelIndices = new HashMap<Integer, Integer>();
        for (int i = 0; i < nLabels; i++) {
        	labelIndices.put(labels[i], i);
        }

        // pre-compute LUT corresponding to resolution and number of directions
		IJ.showStatus("Compute LUT...");
		double[] surfLut = computeSurfaceAreaLut(resol, nDirs);

		// initialize result
		double[] surfaces = new double[nLabels];

		// size of image
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		// for each configuration of 2x2x2 voxels, we identify the labels
		ArrayList<Integer> localLabels = new ArrayList<Integer>(8);
		
		// iterate on image voxel configurations
		IJ.showStatus("Measure surface...");
        for (int z = 0; z < sizeZ - 1; z++) {
        	IJ.showProgress(z, sizeZ);
        	for (int y = 0; y < sizeY - 1; y++) {
        		for (int x = 0; x < sizeX - 1; x++) {

        			// identify labels in current config
        			localLabels.clear();
					for (int z2 = z; z2 <= z + 1; z2++) {
						for (int y2 = y; y2 <= y + 1; y2++) {
							for (int x2 = x; x2 <= x + 1; x2++) {
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

					// if no label in local configuration contribution is zero
					if (localLabels.size() == 0) {
						continue;
					}
					
					// For each label, compute binary confi
					for (int label : localLabels) {
	        			// Compute index of local configuration
	        			int index = 0;
	        			index += image.getVoxel(x, y, z) == label ? 1 : 0;
	        			index += image.getVoxel(x + 1, y, z) == label ? 2 : 0;
	        			index += image.getVoxel(x, y + 1, z) == label ? 4 : 0;
	        			index += image.getVoxel(x + 1, y + 1, z) == label ? 8 : 0;
	        			index += image.getVoxel(x, y, z + 1) == label ? 16 : 0;
	        			index += image.getVoxel(x + 1, y, z + 1) == label ? 32 : 0;
	        			index += image.getVoxel(x, y + 1, z + 1) == label ? 64 : 0;
	        			index += image.getVoxel(x + 1, y + 1, z + 1) == label ? 128 : 0;

	        			int labelIndex = labelIndices.get(label);
	        			surfaces[labelIndex] += surfLut[index];
					}
        		}
        	}
        }
        
		IJ.showStatus("");
    	IJ.showProgress(1);
        return surfaces;
	}
	
	/**
	 * Compute surface area for a single label in the image. This can be useful
	 * for binary images by using label 255.  
	 */
	public final static double surfaceAreaByLut(ImageStack image, int label, double[] resol, int nDirs) {
        
		// pre-compute LUT corresponding to resolution and number of directions
		double[] surfLut = computeSurfaceAreaLut(resol, nDirs);

		// initialize result
		double surf = 0;

		// size of image
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		// iterate on image voxel configurations
        for (int z = 0; z < sizeZ - 1; z++) {
        	for (int y = 0; y < sizeY - 1; y++) {
        		for (int x = 0; x < sizeX - 1; x++) {
        			// Compute index of local configuration
        			int index = 0;
        			index += image.getVoxel(x, y, z) == label ? 1 : 0;
        			index += image.getVoxel(x + 1, y, z) == label ? 2 : 0;
        			index += image.getVoxel(x, y + 1, z) == label ? 4 : 0;
        			index += image.getVoxel(x + 1, y + 1, z) == label ? 8 : 0;
        			index += image.getVoxel(x, y, z + 1) == label ? 16 : 0;
        			index += image.getVoxel(x + 1, y, z + 1) == label ? 32 : 0;
        			index += image.getVoxel(x, y + 1, z + 1) == label ? 64 : 0;
        			index += image.getVoxel(x + 1, y + 1, z + 1) == label ? 128 : 0;
        			
        			// update lut
        			surf += surfLut[index];
        		}
        	}
        }
        
        return surf;
	}
	
	/**
	 * Computes the Look-up table that is used to compute surface area.
	 */
	private final static double[] computeSurfaceAreaLut(double[] resol, int nDirs) {
		// distances between a voxel and its neighbors.
		// di refer to orthogonal neighbors
		// dij refer to neighbors on the same plane 
		// dijk refer to the opposite voxel in a tile
		double d1 = resol[0];
		double d2 = resol[1];
		double d3 = resol[2];
		double vol = d1 * d2 * d3;
		
		double d12 = Math.hypot(resol[0], resol[1]);
		double d13 = Math.hypot(resol[0], resol[2]);
		double d23 = Math.hypot(resol[1], resol[2]);
		double d123= Math.hypot(Math.hypot(resol[0], resol[1]), resol[2]);
	
		// 'magical numbers', corresponding to area of voronoi partition on the
		// unit sphere, when germs are the 26 directions on the unit cube
		// Sum of (c1+c2+c3 + c4*2+c5*2+c6*2 + c7*4) equals 1.
		// See function sphericalCapsAreaC26.m
		double c1 = 0.04577789120476 * 2;  // Ox
		double c2 = 0.04577789120476 * 2;  // Oy
		double c3 = 0.04577789120476 * 2;  // Oz
		double c4 = 0.03698062787608 * 2;  // Oxy
		double c5 = 0.03698062787608 * 2;  // Oxz
		double c6 = 0.03698062787608 * 2;  // Oyz
		double c7 = 0.03519563978232 * 2;  // Oxyz
	
		// If resolution is not the same in each direction, recomputes the weights
		// assigned to each direction
//		if sum(abs(diff(delta)))~=0
//		    areas = sphericalCapsAreaC26(delta);
//		    c1 = areas(1) * 2;
//		    c2 = areas(2) * 2;
//		    c3 = areas(3) * 2;
//		    c4 = areas(4) * 2;
//		    c5 = areas(6) * 2;
//		    c6 = areas(8) * 2;
//		    c7 = areas(10) * 2;
//		end

		// Create Look-up Table
	
		// initialize output array (256 configurations in 3D)
		double[] tab = new double[256];


		// loop for each tile configuration
		for (int i = 0; i < 256; i++) {
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
		    for (int z = 0; z < 2; z++) {
			    for (int y = 0; y < 2; y++) {
				    for (int x = 0; x < 2; x++) {
				    	if (!im[z][y][x])
				    		continue;
//					    if (!im[z][y][1-x]) ke1 += vol/d1/2;
//					    if (!im[z][1-y][x]) ke2 += vol/d2/2;
//					    if (!im[1-z][y][x]) ke3 += vol/d3/2;
					    ke1 = im[z][y][1-x] ? 0 : vol/d1/2;
					    ke2 = im[z][1-y][x] ? 0 : vol/d2/2;
					    ke3 = im[1-z][y][x] ? 0 : vol/d3/2;
					    
					    if (nDirs == 3) {
				            // For 3 directions, the multiplicity is 4, and is canceled by the
				            // coefficient 4 in the Crofton formula. We just need to average on
				            // directions.
				            tab[i] += (ke1 + ke2 + ke3) / 3;
				            
					    } else if (nDirs == 13) {
//						    if (!im[z][1-y][1-x]) ke4 += vol/d12/2;
//						    if (!im[1-z][y][1-x]) ke5 += vol/d13/2;
//						    if (!im[1-z][1-y][x]) ke6 += vol/d23/2;
						    ke4 = im[z][1-y][1-x] ? 0 : vol/d12/2;
						    ke5 = im[1-z][y][1-x] ? 0 : vol/d13/2;
						    ke6 = im[1-z][1-y][x] ? 0 : vol/d23/2;
				            
				            // diagonals of cube

//				            if (!im[1-z][1-y][1-x]) ke7 += 1./2*vol/d123; 
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
	 * Compute surface area of a binary image using 3 directions.
	 */
	public final static double surfaceAreaD3(ImageStack image, double[] resol) {
		double d1 = resol[0];
		double d2 = resol[1];
		double d3 = resol[2];
		double vol = d1 * d2 * d3;
		int n1 = countTransitionsD1(image, 255, true);
		int n2 = countTransitionsD2(image, 255, true);
		int n3 = countTransitionsD3(image, 255, true);
//		System.out.println("counts: " + n1 + " " + n2 + " " + n3);
		
		double surf = 4./3. * .5 * (n1/d1 + n2/d2 + n3/d3) * vol;
		return surf;
	}
	
	/**
	 * Counts the number of binary transitions in the OX direction.
	 */
	private static int countTransitionsD1(ImageStack image, int label, boolean countBorder) {
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		int count = 0;
        double previous = 0;
        double current;
        
        // iterate on image voxels
        for (int z = 0; z < sizeZ; z++) {
        	for (int y = 0; y < sizeY; y++) {

        		// Count border of image
        		previous = image.getVoxel(0, y, z);
        		if (countBorder && previous == label)
        			count++;

        		// count middle of image
        		for (int x = 0; x < sizeX; x++) {
            		current = image.getVoxel(x, y, z);
        			if (previous == label ^ current == label) // Exclusive or
        				count++;
        			previous = current;
        		}

        		// Count border of image
        		if (countBorder && previous == label)
        			count++;
        	}
        }
        return count;
	}
	
	/**
	 * Counts the number of binary transitions in the OY direction.
	 */
	private static int countTransitionsD2(ImageStack image, int label, boolean countBorder) {
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		int count = 0;
        double previous = 0;
        double current;
        
        // iterate on image voxels
        for (int z = 0; z < sizeZ; z++) {
    		for (int x = 0; x < sizeX; x++) {

        		// Count border of image
        		previous = image.getVoxel(x, 0, z);
        		if (countBorder && previous == label)
        			count++;

        		// count middle of image
            	for (int y = 0; y < sizeY; y++) {
            		current = image.getVoxel(x, y, z);
        			if (previous == label ^ current == label) // Exclusive or
        				count++;
        			previous = current;
        		}

        		// Count border of image
        		if (countBorder && previous == label)
        			count++;
        	}
        }
        return count;
	}
	
	/**
     * Computes inertia ellipsoid of each 3D region in input 3D label image.
     */
    public final static ResultsTable inertiaEllipsoid(ImageStack image) {
        // Check validity of parameters
        if (image==null) return null;
        
        // size of image
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        int sizeZ = image.getSize();
        
        // extract particle labels
        int[] labels = findAllLabels(image);
        int nLabels = labels.length;
        
        // create associative array to know index of each label
        HashMap<Integer, Integer> labelIndices = new HashMap<Integer, Integer>();
        for (int i = 0; i < nLabels; i++) {
        	labelIndices.put(labels[i], i);
        }
        
        // allocate memory for result
        int[] counts = new int[nLabels];
        double[] cx = new double[nLabels];
        double[] cy = new double[nLabels];
        double[] cz = new double[nLabels];
        double[] Ixx = new double[nLabels];
        double[] Iyy = new double[nLabels];
        double[] Izz = new double[nLabels];
        double[] Ixy = new double[nLabels];
        double[] Ixz = new double[nLabels];
        double[] Iyz = new double[nLabels];

        // compute centroid of each region
        for (int z = 0; z < sizeZ; z++) {
        	for (int y = 0; y < sizeY; y++) {
        		for (int x = 0; x < sizeX; x++) {
        			int label = (int) image.getVoxel(x, y, z);
        			if (label == 0)
        				continue;

        			int index = labelIndices.get(label);
        			cx[index] += x;
        			cy[index] += y;
        			cz[index] += z;
        			counts[index]++;
        		}
        	}
        }    	
        // normalize by number of pixels in each region
        for (int i = 0; i < nLabels; i++) {
        	cx[i] = cx[i] / counts[i];
        	cy[i] = cy[i] / counts[i];
        	cz[i] = cz[i] / counts[i];
        }
        
        // compute centered inertia matrix of each label
        for (int z = 0; z < sizeZ; z++) {
        	for (int y = 0; y < sizeY; y++) {
        		for (int x = 0; x < sizeX; x++) {
        			int label = (int) image.getVoxel(x, y, z);
        			if (label == 0)
        				continue;
            	
        			int index = labelIndices.get(label);
        			double x2 = x - cx[index];
        			double y2 = y - cy[index];
        			double z2 = z - cz[index];
        			
        			Ixx[index] += x2 * x2;
        			Iyy[index] += y2 * y2;
        			Izz[index] += z2 * z2;
        			Ixy[index] += x2 * y2;
        			Ixz[index] += x2 * z2;
        			Iyz[index] += y2 * z2;
        		}
        	}
        }
        // normalize by number of pixels in each region 
        for (int i = 0; i < nLabels; i++) {
        	Ixx[i] = Ixx[i] / counts[i];
        	Iyy[i] = Iyy[i] / counts[i];
        	Izz[i] = Izz[i] / counts[i];
        	Ixy[i] = Ixy[i] / counts[i];
        	Ixz[i] = Ixz[i] / counts[i];
        	Iyz[i] = Iyz[i] / counts[i];
        }
        
        // Create data table
        ResultsTable table = new ResultsTable();
        
//        System.out.println("create matrix");
    	Matrix matrix = new Matrix(3, 3);
//        System.out.println("matrix ok");

        // compute ellipse parameters for each region
        for (int i = 0; i < nLabels; i++) {
        	// fill up the 3x3 inertia matrix
			matrix.set(0, 0, Ixx[i]);
			matrix.set(0, 1, Ixy[i]);
			matrix.set(0, 2, Ixz[i]);
			matrix.set(1, 0, Ixy[i]);
			matrix.set(1, 1, Iyy[i]);
			matrix.set(1, 2, Iyz[i]);
			matrix.set(2, 0, Ixz[i]);
			matrix.set(2, 1, Iyz[i]);
			matrix.set(2, 2, Izz[i]);
			
			SingularValueDecomposition svd = new SingularValueDecomposition(matrix);
			
			Matrix values = svd.getS();
			double r1 = sqrt(5) * sqrt(values.get(0, 0));
			double r2 = sqrt(5) * sqrt(values.get(1, 1));
			double r3 = sqrt(5) * sqrt(values.get(2, 2));
			
			// extract |cos(theta)|
			Matrix mat = svd.getU();
			double tmp = hypot(mat.get(1, 1), mat.get(2, 1));
			double phi, theta, psi;

			// avoid dividing by 0
			if (tmp > 16 * Double.MIN_VALUE) {
			    // normal case: theta <> 0
			    psi     = atan2( mat.get(2, 1), mat.get(2, 2));
			    theta   = atan2(-mat.get(2, 0), tmp);
			    phi     = atan2( mat.get(1, 0), mat.get(0, 0));
			} else {
				// theta is around 0 
			    psi     = atan2(-mat.get(1, 2), mat.get(1,1));
			    theta   = atan2(-mat.get(2, 0), tmp);
			    phi     = 0;
			}
            
            table.incrementCounter();
            table.addLabel(Integer.toString(labels[i]));
            // add coordinates of origin pixel (IJ coordinate system) 
            table.addValue("XCentroid", cx[i] + .5);
        	table.addValue("YCentroid", cy[i] + .5);
        	table.addValue("ZCentroid", cz[i] + .5);
        	// add scaling parameters 
            table.addValue("Radius1", r1);
        	table.addValue("Radius2", r2);
        	table.addValue("Radius3", r3);
        	// add orientation info
            table.addValue("Phi", toDegrees(phi));
        	table.addValue("Theta", toDegrees(theta));
        	table.addValue("Psi", toDegrees(psi));
        }

        return table;
    }

    /**
	 * Counts the number of binary transitions in the OZ direction.
	 */
	private static int countTransitionsD3(ImageStack image, int label, boolean countBorder) {
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
	
		int count = 0;
	    double previous = 0;
	    double current;
	    
	    // iterate on image voxels
	    	for (int y = 0; y < sizeY; y++) {
	    		for (int x = 0; x < sizeX; x++) {
	
	    		// Count border of image
	    		previous = image.getVoxel(x, y, 0);
	    		if (countBorder && previous == label)
	    			count++;
	
	    		// count middle of image
	            for (int z = 0; z < sizeZ; z++) {
	        		current = image.getVoxel(x, y, z);
	    			if (previous == label ^ current == label) // Exclusive or
	    				count++;
	    			previous = current;
	    		}
	
	    		// Count border of image
	    		if (countBorder && previous == label)
	    			count++;
	    	}
	    }
	    return count;
	}

	
	
    // ====================================================
    // Utilitary functions 

    public final static int[] findAllLabels(ImageStack image) {
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        int sizeZ = image.getSize();
        
        TreeSet<Integer> labels = new TreeSet<Integer> ();
        
        // iterate on image pixels
        for (int z = 0; z < sizeZ; z++) {
        	IJ.showProgress(z, sizeZ);
        	for (int y = 0; y < sizeY; y++) 
        		for (int x = 0; x < sizeX; x++) 
        			labels.add((int) image.getVoxel(x, y, z));
        }
        IJ.showProgress(1);
        
        // remove 0 if it exists
        if (labels.contains(0))
            labels.remove(0);
        
        // convert to array of integers
        int[] array = new int[labels.size()];
        Iterator<Integer> iterator = labels.iterator();
        for (int i = 0; i < labels.size(); i++) 
            array[i] = iterator.next();
        
        return array;
    }
}
