/**
 * 
 */
package inra.ijpb.measure.region3d;

import static java.lang.Math.atan2;
import static java.lang.Math.hypot;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import inra.ijpb.geometry.Ellipsoid;
import inra.ijpb.geometry.Point3D;
import inra.ijpb.geometry.Vector3D;
import inra.ijpb.label.LabelImages;

/**
 * Compute the parameters of 3D ellipsoids that has the same moments up to the
 * second than the region(s) within a 3D binary / label image.
 * 
 * @see inra.ijpb.measure.region2d.inertiaEllipse
 * 
 * @author dlegland
 *
 */
public class EquivalentEllipsoid extends RegionAnalyzer3D<Ellipsoid>
{
	// ==================================================
	// Static methods 
	
	/**
	 * Computes equivalent ellipsoid of each region in the input 3D label image.
	 * 
	 * @param image
	 *            the input image containing label of particles
	 * @param labels
	 *            the array of labels within the image
	 * @param calib
	 *            the calibration of the image
	 * @return an array of Ellipsoid instances representing the calibrated
	 *         coordinates of the equivalent ellipsoid of each region
	 */
	public static final Ellipsoid[] equivalentEllipsoids(ImageStack image, int[] labels, Calibration calib)
	{
		return new EquivalentEllipsoid().analyzeRegions(image, labels, calib);
	}
	
	
	// ==================================================
	// Constructor

	/**
	 * Default constructor
	 */
	public EquivalentEllipsoid()
	{
	}

	
	// ==================================================
	// Implementation of RegionAnalyzer interface

	/**
	 * Utility method that transforms the mapping between labels and equivalent
	 * ellipsoids instances into a ResultsTable that can be displayed with ImageJ.
	 * 
	 * @param map
	 *            the mapping between labels and Equivalent Ellipsoids
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
	
			// ellipse orientation (in degrees)
			table.addValue("Ellipsoid.Phi", ellipse.phi());
			table.addValue("Ellipsoid.Theta", ellipse.theta());
			table.addValue("Ellipsoid.Psi", ellipse.psi());
		}
	
		return table;
	}

	/**
	 * Computes equivalent ellipsoid of each region in the input 3D label image.
	 * 
	 * @param image
	 *            the input image containing label of particles
	 * @param labels
	 *            the array of labels within the image
	 * @param calib
	 *            the calibration of the image
	 * @return an array of Ellipsoid instances representing the calibrated
	 *         coordinates of the equivalent ellipsoid of each region
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
        
        // Compute 2D inertia moments for each label
    	Moments3D[] moments = computeMoments(image, labels, calib);

        // convert moment array to ellipsoid array 
    	Ellipsoid[] ellipsoids = momentsToEllipsoids(moments);

    	// cleanup algorithm monitoring
        fireStatusChanged(this, "");

        return ellipsoids;
	}
	
	private Ellipsoid[] momentsToEllipsoids(Moments3D[] moments)
	{
		int n = moments.length;
    	Ellipsoid[] ellipsoids = new Ellipsoid[n];

    	// compute ellipsoid parameters for each region
        fireStatusChanged(this, "Ellipsoid: compute SVD");
    	for (int i = 0; i < n; i++) 
    	{
            this.fireProgressChanged(this, i, n);
            // create the new ellipsoid
            ellipsoids[i] = moments[i].equivalentEllipsoid();
    	}
        fireProgressChanged(this, 1, 1);
    	
		return ellipsoids;
	}
	
	public Moments3D[] computeMoments(ImageStack image, int[] labels, Calibration calib)
	{
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

        fireStatusChanged(this, "Ellipsoid: compute Moments");

        // create associative array to know index of each label
	    HashMap<Integer, Integer> labelIndices = LabelImages.mapLabelIndices(labels);

	    // allocate memory for result
	    int nLabels = labels.length;
	    Moments3D[] moments = new Moments3D[nLabels]; 
	    for (int i = 0; i < nLabels; i++)
	    {
	        moments[i] = new Moments3D();
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
	                Moments3D moment = moments[index];
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
        fireStatusChanged(this, "Ellipsoid: compute matrices");
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
                    Moments3D moment = moments[index];

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

        // normalize by number of voxels in each region
        for (int i = 0; i < nLabels; i++) 
        {
            moments[i].Ixx /= moments[i].count;
            moments[i].Iyy /= moments[i].count;
            moments[i].Izz /= moments[i].count;
            moments[i].Ixy /= moments[i].count;
            moments[i].Ixz /= moments[i].count;
            moments[i].Iyz /= moments[i].count;
        }
        
        // Also adds the contrbution of the central voxel to avoid zeros
        // coefficients for labels with only one voxel
        for (int i = 0; i < nLabels; i++) 
        {
            moments[i].Ixx += sx / 12;
            moments[i].Iyy += sy / 12;
            moments[i].Izz += sz / 12;
        }
        
        // add coordinates of origin pixel (IJ coordinate system)
        for (int i = 0; i < nLabels; i++) 
        {
            moments[i].cx += .5 * sx + ox;
            moments[i].cy += .5 * sy + oy;
            moments[i].cz += .5 * sz + oz;
        }
  
        return moments;
	}

	public class Moments3D
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
        
        public Ellipsoid equivalentEllipsoid()
        {
            // Extract singular values
            SingularValueDecomposition svd = computeSVD();
            Matrix values = svd.getS();

            // convert singular values to ellipsoid radii 
            double r1 = sqrt(5) * sqrt(values.get(0, 0));
            double r2 = sqrt(5) * sqrt(values.get(1, 1));
            double r3 = sqrt(5) * sqrt(values.get(2, 2));

            // extract |cos(theta)| 
            Matrix mat = svd.getU();
            double tmp = hypot(mat.get(0, 0), mat.get(1, 0));
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

            // create the new ellipsoid
            return new Ellipsoid(this.cx, this.cy, this.cz, r1, r2, r3, toDegrees(phi), toDegrees(theta), toDegrees(psi));
        }

        public ArrayList<Vector3D> eigenVectors()
        {
            // Extract singular values
            Matrix mat = computeSVD().getU();
            
            ArrayList<Vector3D> res = new ArrayList<Vector3D>(3);
            for (int i = 0; i < 3; i++)
            {
                res.add(new Vector3D(mat.get(0, i), mat.get(1, i), mat.get(2, i)));
            }
            return res;
        }
        
        private SingularValueDecomposition computeSVD()
        {
            // create the matrix
            Matrix matrix = new Matrix(3, 3);
            matrix.set(0, 0, this.Ixx);
            matrix.set(0, 1, this.Ixy);
            matrix.set(0, 2, this.Ixz);
            matrix.set(1, 0, this.Ixy);
            matrix.set(1, 1, this.Iyy);
            matrix.set(1, 2, this.Iyz);
            matrix.set(2, 0, this.Ixz);
            matrix.set(2, 1, this.Iyz);
            matrix.set(2, 2, this.Izz);

            // Extract singular values
            return new SingularValueDecomposition(matrix);
        }
	}

}

