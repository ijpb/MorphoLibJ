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
	
	/**
	 * Computes inertia ellipsoid of each region in the input 3D label image.
	 * 
	 * @param image
	 *            the input image containing label of particles
	 * @param labels
	 *            the array of labels within the image
	 * @param calib
	 *            the calibration of the image
	 * @return an array of Ellipsoid instances representing the calibrated
	 *         coordinates of the inertia ellipsoid of each region
	 */
	public static final Ellipsoid[] inertiaEllipsoids(ImageStack image, int[] labels, Calibration calib)
	{
		return new InertiaEllipsoid().analyzeRegions(image, labels, calib);
	}
	
	
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
	 * Computes inertia ellipsoid of each region in the input 3D label image.
	 * 
	 * @param image
	 *            the input image containing label of particles
	 * @param labels
	 *            the array of labels within the image
	 * @param calib
	 *            the calibration of the image
	 * @return an array of Ellipsoid instances representing the calibrated
	 *         coordinates of the inertia ellipsoid of each region
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
    	InertiaMoments3D[] moments = new InertiaMoments3D[nLabels]; 
    	for (int i = 0; i < nLabels; i++)
    	{
    	    moments[i] = new InertiaMoments3D();
    	}

    	// compute centroid of each region
    	fireStatusChanged(this, "Ellipsoid: compute centroids");
    	for (int z = 0; z < sizeZ; z++) 
    	{
            this.fireProgressChanged(this, z, sizeZ);
    		for (int y = 0; y < sizeY; y++)
    		{
    			for (int x = 0; x < sizeX; x++)
    			{
    				// do not process background voxels
    				int label = (int) image.getVoxel(x, y, z);
    				if (label == 0)
    					continue;

    				// convert label to its index
    				if (!labelIndices.containsKey(label))
    				{
    				    System.err.println("Label image contains unknown label: " + label);
    				    continue;
    				}
    				int index = labelIndices.get(label);

    				// update sum coordinates, taking into account the spatial calibration
    				InertiaMoments3D moment = moments[index];
    				moment.cx += x * sx;
    				moment.cy += y * sy;
    				moment.cz += z * sz;
    				moment.count++;
    			}
    		}
    	}

    	// normalize by number of pixels in each region
    	for (int i = 0; i < nLabels; i++) 
    	{
            moments[i].cx /= moments[i].count;
            moments[i].cy /= moments[i].count;
            moments[i].cz /= moments[i].count;
    	}

    	// compute centered inertia matrix of each label
        fireStatusChanged(this, "Ellipsoid: compute inertia matrices");
    	for (int z = 0; z < sizeZ; z++) 
    	{
            this.fireProgressChanged(this, z, sizeZ);
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
                    InertiaMoments3D moment = moments[index];

    				// convert coordinates relative to centroid 
                    double x2 = x * sx - moment.cx;
                    double y2 = y * sy - moment.cy;
                    double z2 = z * sz - moment.cz;

    				// update coefficients of inertia matrix
                    moment.Ixx += x2 * x2;
                    moment.Iyy += y2 * y2;
                    moment.Izz += z2 * z2;
                    moment.Ixy += x2 * y2;
                    moment.Ixz += x2 * z2;
                    moment.Iyz += y2 * z2;
    			}
    		}
    	}

    	// normalize by number of pixels in each region 
    	for (int i = 0; i < nLabels; i++) 
    	{
            moments[i].Ixx /= moments[i].count;
            moments[i].Iyy /= moments[i].count;
            moments[i].Izz /= moments[i].count;
            moments[i].Ixy /= moments[i].count;
            moments[i].Ixz /= moments[i].count;
            moments[i].Iyz /= moments[i].count;
    	}

    	// Create result array
    	Ellipsoid[] ellipsoids = new Ellipsoid[nLabels];

    	// compute ellipsoid parameters for each region
        fireStatusChanged(this, "Ellipsoid: compute SVD");
    	Matrix matrix = new Matrix(3, 3);
    	for (int i = 0; i < nLabels; i++) 
    	{
            this.fireProgressChanged(this, i, nLabels);
    		// fill up the 3x3 inertia matrix
            InertiaMoments3D moment = moments[i];
            matrix.set(0, 0, moment.Ixx);
            matrix.set(0, 1, moment.Ixy);
            matrix.set(0, 2, moment.Ixz);
            matrix.set(1, 0, moment.Ixy);
            matrix.set(1, 1, moment.Iyy);
            matrix.set(1, 2, moment.Iyz);
            matrix.set(2, 0, moment.Ixz);
            matrix.set(2, 1, moment.Iyz);
            matrix.set(2, 2, moment.Izz);

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
    		double xc = moment.cx + .5 * sx + ox;
    		double yc = moment.cy + .5 * sy + oy;
    		double zc = moment.cz + .5 * sz + oz;
    		
    		// create the new ellipsoid
    		ellipsoids[i] = new Ellipsoid(xc, yc, zc, r1, r2, r3, toDegrees(phi), toDegrees(theta), toDegrees(psi));
    	}

    	return ellipsoids;
	}
	
	public class InertiaMoments3D
	{
	    // the number of voxels 
	    int count = 0;
	    
	    // The coordinates of the center
        double cx = 0;
        double cy = 0;
        double cz = 0;
        
        // the second-order coefficients
        double Ixx = 0;
        double Ixy = 0;
        double Ixz = 0;
        double Iyy = 0;
        double Iyz = 0;
        double Izz = 0;
	}

}

