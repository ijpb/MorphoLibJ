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
 * second order than the region(s) within a 3D binary / label image.
 * 
 * @see inra.ijpb.measure.region2d.EquivalentEllipse
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
    // Specific methods

    /**
     * Utility method that transforms a pair of arrays for labels and equivalent
     * ellipsoids instances into a ResultsTable that can be displayed with
     * ImageJ.
     * 
     * @param labels
     *            the array of region labels
     * @param ellipsoids
     *            the array of region ellipsoids (the same size as the array of
     *            labels)
     * @return a ResultsTable that can be displayed with ImageJ.
     */
    public ResultsTable createTable(int[] labels, Ellipsoid[] ellipsoids)
    {
        if (labels.length != ellipsoids.length)
        {
            throw new RuntimeException("Requires the two input arrays to have the same size");
        }
        
        // Initialize a new result table
        ResultsTable table = new ResultsTable();
    
        // Convert all results that were computed during execution of the
        // "computeGeodesicDistanceMap()" method into rows of the results table
        for (int i = 0; i < labels.length; i++)
        {
            populateTable(table, labels[i], ellipsoids[i]);
        }
    
        return table;
    }
    
    /**
     * Utility method that transforms a pair of arrays for labels and 3D moments
     * instances into a ResultsTable that can be displayed with ImageJ.
     * 
     * @param labels
     *            the array of region labels
     * @param moments
     *            the array of moments (the same size as the array of labels)
     * @return a ResultsTable that can be displayed with ImageJ.
     */
    public ResultsTable createTable(int[] labels, Moments3D[] moments)
    {
        // Initialize a new result table
        ResultsTable table = new ResultsTable();
    
        for (int i = 0; i < labels.length; i++)
        {
            // add an entry to the resulting data table
            table.incrementCounter();
            table.addLabel(Integer.toString(labels[i]));

            ArrayList<Vector3D> vectors = moments[i].eigenVectors();
            
            Vector3D v1 = vectors.get(0);
            table.addValue("EigenVector1.X", v1.getX());
            table.addValue("EigenVector1.Y", v1.getY());
            table.addValue("EigenVector1.Z", v1.getZ());
            
            Vector3D v2 = vectors.get(1);
            table.addValue("EigenVector2.X", v2.getX());
            table.addValue("EigenVector2.Y", v2.getY());
            table.addValue("EigenVector2.Z", v2.getZ());
            
            Vector3D v3 = vectors.get(2);
            table.addValue("EigenVector3.X", v3.getX());
            table.addValue("EigenVector3.Y", v3.getY());
            table.addValue("EigenVector3.Z", v3.getZ());

        }
        
        return table;
    }


	private void populateTable(ResultsTable table, int label, Ellipsoid ellipsoid)
	{
        // add an entry to the resulting data table
        table.incrementCounter();
        table.addLabel(Integer.toString(label));
        
        // coordinates of centroid
        Point3D center = ellipsoid.center();
        table.addValue("Ellipsoid.Center.X", center.getX());
        table.addValue("Ellipsoid.Center.Y", center.getY());
        table.addValue("Ellipsoid.Center.Z", center.getZ());
        
        // ellipse size
        table.addValue("Ellipsoid.Radius1", ellipsoid.radius1());
        table.addValue("Ellipsoid.Radius2", ellipsoid.radius2());
        table.addValue("Ellipsoid.Radius3", ellipsoid.radius3());

        // ellipse orientation (in degrees)
        table.addValue("Ellipsoid.Phi", ellipsoid.phi());
        table.addValue("Ellipsoid.Theta", ellipsoid.theta());
        table.addValue("Ellipsoid.Psi", ellipsoid.psi());
	}

	/**
     * Converts an array of 3D moments into equivalent ellipsoid representation.
     * 
     * @param moments
     *            the moments to convert
     * @return the array of ellipsoids corresponding to the moments
     */
	public Ellipsoid[] momentsToEllipsoids(Moments3D[] moments)
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
	
	/**
	 * Computes the matrix of moments for each region within the 3D label map.
	 * 
	 * @param image
	 *            the 3D image of labels (label map)
	 * @param labels
	 *            the array of region labels to process
	 * @param calib
	 *            the spatial calibration of the image
	 * @return an array the same size as <code>labels</code>, containing for
	 *         each processed region result of 3D Moments computations
	 */
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

                    // do not process labels that are not in the input list 
                    if (!labelIndices.containsKey(label))
                        continue;

                    // convert label to its index
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
            if (moments[i].count > 0)
            {
                moments[i].cx /= moments[i].count;
                moments[i].cy /= moments[i].count;
                moments[i].cz /= moments[i].count;
            }
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
                    // get label of current label
                    int label = (int) image.getVoxel(x, y, z);
                    
                    // do not process background voxels or regions not in the "labels" array
                    if (label == 0 || !labelIndices.containsKey(label))
                    {
                        continue;
                    }

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

        // Normalize moments
        for (int i = 0; i < nLabels; i++)
        {
            if (moments[i].count == 0)
            {
                continue;
            }

            // normalize by number of voxels in each region
            moments[i].Ixx /= moments[i].count;
            moments[i].Iyy /= moments[i].count;
            moments[i].Izz /= moments[i].count;
            moments[i].Ixy /= moments[i].count;
            moments[i].Ixz /= moments[i].count;
            moments[i].Iyz /= moments[i].count;

            // Also adds the contribution of the central voxel to avoid zero
            // coefficients for labels with only one voxel
            moments[i].Ixx += sx * sx / 12;
            moments[i].Iyy += sy * sy / 12;
            moments[i].Izz += sz * sz / 12;

            // add coordinates of origin pixel (IJ coordinate system)
            moments[i].cx += .5 * sx + ox;
            moments[i].cy += .5 * sy + oy;
            moments[i].cz += .5 * sz + oz;
        }

        return moments;
    }

	// ==================================================
    // Implementation of RegionAnalyzer interface

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
        
        // Compute 3D inertia moments for each label
    	Moments3D[] moments = computeMoments(image, labels, calib);
    
        // convert moment array to ellipsoid array 
    	Ellipsoid[] ellipsoids = momentsToEllipsoids(moments);
    
    	// cleanup algorithm monitoring
        fireStatusChanged(this, "");
    
        return ellipsoids;
    }


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
    	    populateTable(table, label, map.get(label));
    	}
    
    	return table;
    }
    
    
    // ==================================================
    // Inner class for storing results
    
    /**
     * Encapsulates the results of 3D Moments computations. 
     */
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
        
        /**
    	 * Converts the 3D moments stored in this instance into a 3D ellipsoid.
    	 * 
    	 * @return the 3D ellipsoid with same inertia moments as the regions
    	 *         described by these moments.
    	 */
        public Ellipsoid equivalentEllipsoid()
        {
            // special case of one-voxeL regions (and also empty regions)
            if (count <= 1)
            {
                double r1 = Math.sqrt(5 * Ixx);
                double r2 = Math.sqrt(5 * Iyy);
                double r3 = Math.sqrt(5 * Izz);
                // -> use default values (0,0,0) for angles
                return new Ellipsoid(this.cx, this.cy, this.cz, r1, r2, r3, 0.0, 0.0, 0.0);
            }
    
            // Extract singular values
            SingularValueDecomposition svd = computeSVD();
            Matrix values = svd.getS();
    
            // convert singular values to ellipsoid radii 
            double r1 = sqrt(5) * sqrt(values.get(0, 0));
            double r2 = sqrt(5) * sqrt(values.get(1, 1));
            double r3 = sqrt(5) * sqrt(values.get(2, 2));
    
            // Perform singular-Value Decomposition
            Matrix mat = svd.getU();
            
            // Ensure (0,0) coefficient is positive, to enforce azimut angle (Phi) > 0
            if (mat.get(0, 0) < 0)
            {
                for(int c = 0; c < 2; c++)
                {
                    for (int r = 0; r < 3; r++)
                    {
                        mat.set(r, c, -mat.get(r, c));
                    }
                }
            }
    
            // extract |cos(theta)|
            double tmp = hypot(mat.get(0, 0), mat.get(1, 0));
            double phi, theta, psi;
    
            // avoid dividing by 0
            if (tmp > 16 * Double.MIN_VALUE) 
            {
                // normal case: cos(theta) <> 0
                psi     = atan2( mat.get(2, 1), mat.get(2, 2));
                theta   = atan2(-mat.get(2, 0), tmp);
                phi     = atan2( mat.get(1, 0), mat.get(0, 0));
            }
            else 
            {
                // cos(theta) is around 0 
                psi     = atan2(-mat.get(1, 2), mat.get(1,1));
                theta   = atan2(-mat.get(2, 0), tmp);
                phi     = 0;
            }
    
            // create the new ellipsoid
            return new Ellipsoid(this.cx, this.cy, this.cz, r1, r2, r3, toDegrees(phi), toDegrees(theta), toDegrees(psi));
        }
    
        /**
    	 * Return the eigen vector of the moments. Uses a singular value
    	 * decomposition of the matrix of moments.
    	 * 
    	 * @return the eigen vector of the moments
    	 */
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

