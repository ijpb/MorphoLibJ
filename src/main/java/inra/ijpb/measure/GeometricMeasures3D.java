/**
 * 
 */
package inra.ijpb.measure;

import ij.ImageStack;
import ij.measure.ResultsTable;

import java.util.Iterator;
import java.util.TreeSet;

/**
 * @author David Legland
 *
 */
public class GeometricMeasures3D {

	public final static ResultsTable surfaceArea(ImageStack labelImage, double[] resol, int nDirs) {
		 int[] labels = findAllLabels(labelImage);
		 int nbLabels = labels.length;

		 // Create data table
		 ResultsTable table = new ResultsTable();
		 double surf;
		 
		// iterate over labels in image
		for (int i = 0; i < nbLabels; i++) {
			int label = labels[i];
			surf = surfaceAreaByLut(labelImage, label, resol, nDirs);

			table.incrementCounter();
			table.addValue("Label", label);
			table.addValue("Surface", surf);
		}

		return table;
	}
	
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
	
	private final static double[] computeSurfaceAreaLut(double[] resol, int nDirs) {
		// distance between a pixel and its neighbours.
		// di refer to orthogonal neighbours
		// dij refer to neighbours on the same plane 
		// dijk refer to the opposite pixel in a tile
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
		    
		    // iterate over voxels within the configuration
		    for (int z = 0; z < 2; z++) {
			    for (int y = 0; y < 2; y++) {
				    for (int x = 0; x < 2; x++) {
				    	if (!im[z][y][x])
				    		continue;
				    	
				        // contributions for isothetic directions
					    double ke1 = 0; 
					    double ke2 = 0; 
					    double ke3 = 0;
					    
					    if (!im[z][y][1-x]) ke1 += vol/d1/2;
					    if (!im[z][1-y][x]) ke2 += vol/d2/2;
					    if (!im[1-z][y][x]) ke3 += vol/d3/2;
					    
					    if (nDirs == 3) {
				            // For 3 directions, the multiplicity is 4, and is canceled by the
				            // coefficient 4 in the Crofton formula. We just need to average on
				            // directions.
				            tab[i] += (ke1 + ke2 + ke3) / 3;
				            
					    } else if (nDirs == 13) {
				            double ke4 = 0; 
				            double ke5 = 0; 
				            double ke6 = 0;
						    if (!im[z][1-y][1-x]) ke4 += vol/d12/2;
						    if (!im[1-z][y][1-x]) ke5 += vol/d13/2;
						    if (!im[1-z][1-y][x]) ke6 += vol/d23/2;
				            
				            // diagonals of cube
				            double ke7 = 0;
				            if (!im[1-z][1-y][1-x]) ke7 += 1./2*vol/d123; 
				            
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
	
	public final static double surfaceAreaD3(ImageStack image, double[] resol) {
		double d1 = resol[0];
		double d2 = resol[1];
		double d3 = resol[2];
		double vol = d1 * d2 * d3;
		int n1 = countTransitionsD1(image, 255, true);
		int n2 = countTransitionsD2(image, 255, true);
		int n3 = countTransitionsD3(image, 255, true);
		System.out.println("counts: " + n1 + " " + n2 + " " + n3);
		
		double surf = 4./3. * .5 * (n1/d1 + n2/d2 + n3/d3) * vol;
		return surf;
	}
	
	/**
	 * Count the number of binary transitions in the OX direction.
	 */
	public static int countTransitionsD1(ImageStack image, int label, boolean countBorder) {
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
	 * Count the number of binary transitions in the OY direction.
	 */
	public static int countTransitionsD2(ImageStack image, int label, boolean countBorder) {
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
	 * Count the number of binary transitions in the OZ direction.
	 */
	public static int countTransitionsD3(ImageStack image, int label, boolean countBorder) {
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

    private static int[] findAllLabels(ImageStack image) {
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        int sizeZ = image.getSize();
        
        TreeSet<Integer> labels = new TreeSet<Integer> ();
        
        // iterate on image pixels
        for (int z = 0; z < sizeZ; z++) 
        	for (int y = 0; y < sizeY; y++) 
        		for (int x = 0; x < sizeX; x++) 
        			labels.add((int) image.getVoxel(x, y, z));
        
        // remove 0 if it exists
        if (labels.contains(0))
            labels.remove(0);
        
        // convert to array
        int[] array = new int[labels.size()];
        Iterator<Integer> iterator = labels.iterator();
        for (int i = 0; i < labels.size(); i++) 
            array[i] = iterator.next();
        
        return array;
    }
    
    
	public final static void main(String[] args) {
		ImageStack image = createBallImage();
		double[] resol = new double[]{1., 1., 1.};
		double surf = surfaceAreaD3(image, resol);
		System.out.println("Surface: " + surf);
		double surf2 = surfaceAreaByLut(image, 255, resol, 3);
		System.out.println("Surface: " + surf2);
		double surf3 = surfaceAreaByLut(image, 255, resol, 13);
		System.out.println("Surface: " + surf3);
		
	}
	
	/**
	 * Generate a ball of radius 20 in a discrete image of size 50x50x50. 
	 * Expected surface area is around 5026.
	 */
	public final static ImageStack createBallImage() {
		// ball features
		double xc = 25.12;
		double yc = 25.23;
		double zc = 25.34;
		double radius = 20;
		double r2 = radius * radius;
		
		// image size
		int size1 = 50;
		int size2 = 50;
		int size3 = 50;
		
		ImageStack result = ImageStack.create(size1, size2, size3, 8);
		
		for (int z = 0; z < size3; z++) {
			double z2 = z - zc; 
			for (int y = 0; y < size2; y++) {
				double y2 = y - yc; 
				for (int x = 0; x < size1; x++) {
					double x2 = x - xc;
					double ri = x2 * x2 + y2 * y2 + z2 * z2; 
					if (ri <= r2) {
						result.setVoxel(x, y, z, 255);
					}
				}
			}
		}
		
		return result;
	}
}
