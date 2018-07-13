/**
 * 
 */
package inra.ijpb.measure.region3d;

import static java.lang.Math.atan2;
import static java.lang.Math.hypot;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;

import java.util.HashMap;
import java.util.Map;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import inra.ijpb.geometry.Ellipsoid;
import inra.ijpb.geometry.Point3D;
import inra.ijpb.label.LabelImages;

/**
 * Compute parameters of inertia ellipsoids from 3D binary / label images.
 * 
 * @author dlegland
 *
 */
public class InertiaEllipsoid extends RegionAnalyzer3D<Ellipsoid>
{
	// ==================================================
	// Static methods 
	
	// ==================================================
	// Constructor

	/**
	 * Default constructor
	 */
	public InertiaEllipsoid()
	{
	}

	
	// ==================================================
	// Implementation of RegionAnalyzer interface

	/**
	 * Utility method that transforms the mapping between labels and inertia
	 * ellipsoids instances into a ResultsTable that can be displayed with ImageJ.
	 * 
	 * @param map
	 *            the mapping between labels and Inertia Ellipsoids
	 * @return a ResultsTable that can be displayed with ImageJ.
	 */
	public ResultsTable createTable(Map<Integer, Ellipsoid> map)
	{
		// Initialize a new result table
		ResultsTable table = new ResultsTable();
	
		// Convert all results that were computed during execution of the
		// "computeGeodesicDistanceMap()" method into rows of the results table
		for (int label : map.keySet())
		{
			// current diameter
			Ellipsoid ellipse = map.get(label);
			
			// add an entry to the resulting data table
			table.incrementCounter();
			table.addLabel(Integer.toString(label));
			
			// coordinates of centroid
			Point3D center = ellipse.center();
			table.addValue("Ellipsoid.Center.X", center.getX());
			table.addValue("Ellipsoid.Center.Y", center.getY());
			table.addValue("Ellipsoid.Center.Z", center.getZ());
			
			// ellipse size
			table.addValue("Ellipsoid.Radius1", ellipse.radius1());
			table.addValue("Ellipsoid.Radius2", ellipse.radius2());
			table.addValue("Ellipsoid.Radius3", ellipse.radius3());
	
			// ellipse orientation (degrees)
			table.addValue("Ellipsoid.Phi", ellipse.phi());
			table.addValue("Ellipsoid.Theta", ellipse.theta());
			table.addValue("Ellipsoid.Psi", ellipse.psi());
		}
	
		return table;
	}

	/**
	 * Computes inertia ellipse of each region in input label image.
	 * 
	 * @param image
	 *            the input image containing label of particles
	 * @param labels
	 *            the array of labels within the image
	 * @param calib
	 *            the calibration of the image
	 * @return an array of Ellipsoid representing the calibrated coordinates of
	 *         the inertia ellipse of each region
	 */
	public Ellipsoid[] analyzeRegions(ImageStack image, int[] labels, Calibration calib)
	{
        // check if JAMA package is present
        try 
        {
            Class.forName("Jama.Matrix");
        } 
        catch(Exception e)
        {
        	throw new RuntimeException("Requires the JAMA package to work properly");
        }
        
        // size of image
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        int sizeZ = image.getSize();
        
		// Extract spatial calibration
		double sx = 1, sy = 1, sz = 1;
		double ox = 0, oy = 0, oz = 0;
		if (calib != null)
		{
			sx = calib.pixelWidth;
			sy = calib.pixelHeight;
			sz = calib.pixelDepth;
			ox = calib.xOrigin;
			oy = calib.yOrigin;
			oz = calib.zOrigin;
		}
		
    	// create associative array to know index of each label
    	HashMap<Integer, Integer> labelIndices = LabelImages.mapLabelIndices(labels);

    	// allocate memory for result
    	int nLabels = labels.length;
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
    	for (int z = 0; z < sizeZ; z++) 
    	{
    		for (int y = 0; y < sizeY; y++)
    		{
    			for (int x = 0; x < sizeX; x++)
    			{
    				// do not process background voxels
    				int label = (int) image.getVoxel(x, y, z);
    				if (label == 0)
    					continue;

    				// convert label to its index
    				int index = labelIndices.get(label);

    				// update sum coordinates, taking into account the spatial calibration 
    				cx[index] += x * sx;
    				cy[index] += y * sy;
    				cz[index] += z * sz;
    				counts[index]++;
    			}
    		}
    	}

    	// normalize by number of pixels in each region
    	for (int i = 0; i < nLabels; i++) 
    	{
    		cx[i] = cx[i] / counts[i];
    		cy[i] = cy[i] / counts[i];
    		cz[i] = cz[i] / counts[i];
    	}

    	// compute centered inertia matrix of each label
    	for (int z = 0; z < sizeZ; z++) 
    	{
    		for (int y = 0; y < sizeY; y++)
    		{
    			for (int x = 0; x < sizeX; x++) 
    			{
    				// do not process background voxels
    				int label = (int) image.getVoxel(x, y, z);
    				if (label == 0)
    					continue;

    				// convert label to its index
    				int index = labelIndices.get(label);

    				// convert coordinates relative to centroid 
    				double x2 = x * sx - cx[index];
    				double y2 = y * sy - cy[index];
    				double z2 = z * sz - cz[index];

    				// update coefficients of inertia matrix
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
    	for (int i = 0; i < nLabels; i++) 
    	{
    		Ixx[i] = Ixx[i] / counts[i];
    		Iyy[i] = Iyy[i] / counts[i];
    		Izz[i] = Izz[i] / counts[i];
    		Ixy[i] = Ixy[i] / counts[i];
    		Ixz[i] = Ixz[i] / counts[i];
    		Iyz[i] = Iyz[i] / counts[i];
    	}

    	// Create result array
    	Ellipsoid[] ellipsoids = new Ellipsoid[nLabels];

    	// compute ellipsoid parameters for each region
    	Matrix matrix = new Matrix(3, 3);
    	for (int i = 0; i < nLabels; i++) 
    	{
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

    		// Extract singular values
    		SingularValueDecomposition svd = new SingularValueDecomposition(matrix);
    		Matrix values = svd.getS();

    		// convert singular values to ellipsoid radii 
    		double r1 = sqrt(5) * sqrt(values.get(0, 0));
    		double r2 = sqrt(5) * sqrt(values.get(1, 1));
    		double r3 = sqrt(5) * sqrt(values.get(2, 2));

    		// extract |cos(theta)| 
    		Matrix mat = svd.getU();
    		double tmp = hypot(mat.get(1, 1), mat.get(2, 1));
    		double phi, theta, psi;

    		// avoid dividing by 0
    		if (tmp > 16 * Double.MIN_VALUE) 
    		{
    			// normal case: theta <> 0
    			psi     = atan2( mat.get(2, 1), mat.get(2, 2));
    			theta   = atan2(-mat.get(2, 0), tmp);
    			phi     = atan2( mat.get(1, 0), mat.get(0, 0));
    		}
    		else 
    		{
    			// theta is around 0 
    			psi     = atan2(-mat.get(1, 2), mat.get(1,1));
    			theta   = atan2(-mat.get(2, 0), tmp);
    			phi     = 0;
    		}

    		// add coordinates of origin pixel (IJ coordinate system)
    		double xc = cx[i] + .5 * sx + ox;
    		double yc = cy[i] + .5 * sy + oy;
    		double zc = cz[i] + .5 * sz + oz;
    		
    		// create the new ellipsoid
    		ellipsoids[i] = new Ellipsoid(xc, yc, zc, r1, r2, r3, toDegrees(phi), toDegrees(theta), toDegrees(psi));
    	}

    	return ellipsoids;

	}

}

