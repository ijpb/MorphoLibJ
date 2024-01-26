/**
 * 
 */
package inra.ijpb.measure.region3d;

import static inra.ijpb.measure.region3d.MorphometricFeatures3D.Feature.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.geometry.Box3D;
import inra.ijpb.geometry.Ellipsoid;
import inra.ijpb.geometry.Point3D;
import inra.ijpb.geometry.Sphere;
import inra.ijpb.label.LabelImages;

/**
 * An analyzer class that aggregates the computation of several morphometric features 
 * for 3D regions.
 * 
 * The class works both as a container of features, used to identify which features will
 * quantify regions, and as a computation class. It may extend the "RegionAnalyzer3D" 
 * interface in a future release.
 * 
 *  <p>
 *  Usage:
 *  <pre>{@code
 *  ResultsTable table = new MorphometricFeatures3D()
 *      .add(Feature.VOLUME)
 *      .add(Feature.SURFACE_AREA)
 *      .add(Feature.CENTROID)
 *      .computeTable(imagePlus);
 *  }
 *  </pre>
 * 
 * @see inra.ijpb.measure.region2d.MorphometricFeatures2D
 */
public class MorphometricFeatures3D extends AlgoStub
{
    /**
     * An enumeration used to identify which features have to be
     * computed by the MorphometricFeatures3D class.
     */
    public enum Feature
    {
        /** The number of voxels that compose the region.*/
        VOXEL_COUNT, 
        /** The volume occupied by the region, as the number of voxels multiplied by image resolution.*/
        VOLUME,
        /** The surface area of the region (measured by Crofton formula) .*/
        SURFACE_AREA,
        /** The mean breadth, proportional to the integral of mean curvature .*/
        MEAN_BREADTH,
        /** The Euler number of the region, to quantify its topology.*/ 
        EULER_NUMBER,
        /** The sphericity, as normalized ratio of powers of volume and surface area.*/
        SPHERICITY,
        /** The bounding box along each dimension.*/
        BOUNDING_BOX,
        /** The centroid.*/
        CENTROID,
        /** The equivalent ellipsoid with same moments up to the second order as the region.*/
        EQUIVALENT_ELLIPSOID,
        /** The elongation factor of the equivalent ellipsoid.*/
        ELLIPSOID_ELONGATIONS,
        /** The radius of the largest inscribed ball.*/
        MAX_INSCRIBED_BALL,
    };
    
    
    // ====================================================
    // Inner fields
    
    /**
     * The features that will be computed and returned into result table.
     */
    private List<Feature> features = new ArrayList<Feature>();
    
    /**
     * Number of directions for computing surface area or mean breadth with
     * Crofton Formula. Default is 13.
     */
    private int directionNumber = 13;

    /**
     * Connectivity for computing 3D Euler number. Default is 6. 
     */
    private int connectivity = 6;
    
    
    // ====================================================
    // Constructors
    
    /**
     * Creates a new MorphometricFeatures3D instance containing no feature.
     */
    public MorphometricFeatures3D()
    {
    }
    
    /**
     * Creates a new instance of MorphometricFeatures3D from the series of selected
     * features.
     * 
     * @param features
     *            the features to use.
     */
    public MorphometricFeatures3D(Feature... features)
    {
        for (Feature f : features)
        {
            this.features.add(f);
        }
    }
    
    
    // ==================================================
    // setup computation options

    /**
     * @return the directionNumber used to compute surface area and mean breadth
     */
    public int getDirectionNumber()
    {
        return directionNumber;
    }

    /**
     * @param directionNumber
     *            the number of directions used to compute surface area and mean
     *            breadth (either 3 or 13, default is 13)
     */
    public void setDirectionNumber(int directionNumber)
    {
        this.directionNumber = directionNumber;
    }

    /**
     * @return the connectivity used to compute Euler number
     */
    public int getConnectivity()
    {
        return connectivity;
    }

    /**
     * @param connectivity
     *            the connectivity used to compute Euler number (either 6 or 26,
     *            default is 6)
     */
    public void setConnectivity(int connectivity)
    {
        this.connectivity = connectivity;
    }

    
    // ====================================================
    // Management of features
    
    /**
     * Returns the features this MorphometricFeatures3D instance will compute.
     * 
     * @return the collection of features that will be computed.
     */
    public Collection<Feature> features()
    {
        return features;
    }
    
    /**
     * Adds a new feature to the collection of features to compute.
     * 
     * @param f
     *            the feature to add
     * @return this instance of MorphometricFeatures3D, to allow chaining of
     *         operations
     */
    public MorphometricFeatures3D add(Feature f)
    {
        features.add(f);
        return this;
    }
    
    /**
     * Removes a feature from the collection of features to compute.
     * 
     * @param f
     *            the feature to remove
     * @return this instance of MorphometricFeatures3D, to allow chaining of
     *         operations
     */
    public MorphometricFeatures3D remove(Feature f)
    {
        features.remove(f);
        return this;
    }
    
    /**
     * Checks if this MorphometricFeatures3D instance contains the specified
     * feature.
     * 
     * @param f
     *            the feature to test
     * @return true if this instance contains the specified feature
     */
    public boolean contains(Feature f)
    {
        return this.features.contains(f);
    }
    
    /**
     * Checks if this MorphometricFeatures3D instance contains any of the
     * specified features.
     * 
     * @param features
     *            the list of features to test
     * @return true if this instance contains any of the specified features
     */
    public boolean containsAny(Feature... features)
    {
        for (Feature f : features)
        {
            if (this.features.contains(f)) return true;
        }
        return false;
    }
    
    
    // ====================================================
    // Computation method
    
    /**
     * Computes a set of descriptive features from a label image and
     * concatenates the results into a ResultsTable.
     * 
     * @param imagePlus
     *            the image to analyze.
     * @return the results of the analysis as a Results Table
     */
    public ResultsTable computeTable(ImagePlus imagePlus)
    {
        Results results = computeFeatures(imagePlus);
        return populateTable(results);
    }

    private Results computeFeatures(ImagePlus imagePlus)
    {
        // Retrieve image array and spatial calibration
        ImageStack image = imagePlus.getStack();
        Calibration calib = imagePlus.getCalibration();
        
        // identify labels
        int[] labels = LabelImages.findAllLabels(image);
        
        // Parameters to be computed
        Results results = new Results();
        results.labels = labels;
        
        // compute intrinsic volumes
        if (contains(VOXEL_COUNT))
        {
            this.fireStatusChanged(this, "Pixel Count");
            results.voxelCounts = LabelImages.voxelCount(image, labels);
        }
        
        // compute intrinsic volumes, and related shape factor
        if (containsAny(VOLUME, SURFACE_AREA, MEAN_BREADTH, EULER_NUMBER, SPHERICITY))
        {
            this.fireStatusChanged(this, "Intrinsic Volumes");
            
            // Create and setup computation class
            IntrinsicVolumes3D algo = new IntrinsicVolumes3D();
            algo.setDirectionNumber(this.directionNumber);
            algo.setConnectivity(this.connectivity);
            DefaultAlgoListener.monitor(algo);
            
            // run analysis
            results.intrinsicVolumes = algo.analyzeRegions(image, labels, calib);
        }
        
        // compute 3D bounding box
        if (contains(BOUNDING_BOX))
        {
            this.fireStatusChanged(this, "Bounding boxes");
            BoundingBox3D algo = new BoundingBox3D();
            DefaultAlgoListener.monitor(algo);
            results.boundingBoxes = algo.analyzeRegions(image, labels, calib);
        }
        
        // compute equivalent ellipsoids and their elongations
        if (containsAny(EQUIVALENT_ELLIPSOID, ELLIPSOID_ELONGATIONS))
        {
            this.fireStatusChanged(this, "Equivalent Ellipsoids");
            EquivalentEllipsoid algo = new EquivalentEllipsoid();
            DefaultAlgoListener.monitor(algo);
            results.ellipsoids = algo.analyzeRegions(image, labels, calib);
            
            if (features.contains(CENTROID))
            {
                results.centroids = Ellipsoid.centers(results.ellipsoids);
            }
            
            if (contains(ELLIPSOID_ELONGATIONS))
            {
                this.fireStatusChanged(this, "Ellipsoid elongations");
                results.ellipsoidElongations = Ellipsoid.elongations(results.ellipsoids);
            }
        } 
        else if (contains(CENTROID))
        {
            // Compute centroids if not already computed from inertia ellipsoid
            this.fireStatusChanged(this, "Centroids");
            Centroid3D algo = new Centroid3D();
            DefaultAlgoListener.monitor(algo);
            results.centroids = algo.analyzeRegions(image, labels, calib);
        }
        
        // compute position and radius of maximal inscribed ball
        if (contains(MAX_INSCRIBED_BALL))
        {
            this.fireStatusChanged(this, "Inscribed circles");
            LargestInscribedBall algo = new LargestInscribedBall();
            DefaultAlgoListener.monitor(algo);
            results.inscribedBalls = algo.analyzeRegions(image, labels, calib);
        }
        
        return results;
    }
    
    /**
     * Creates the results table that summarizes the features computed on the
     * image.
     * 
     * @param results
     *            the results of computations on an image.
     * @return the results table that summarizes the features computed on the
     *         image.
     */
    private ResultsTable populateTable(Results results)
    {
        this.fireStatusChanged(this, "Populate table");
        
        // create results table
        ResultsTable table = new ResultsTable();
        
        // Initialize labels
        int nLabels = results.labels.length;
        for (int i = 0; i < nLabels; i++)
        {
            table.incrementCounter();
            table.addLabel("" + results.labels[i]);
        }
        
        if (features.contains(VOXEL_COUNT))
        {
            addColumnToTable(table, "VoxelCount", results.voxelCounts);
        }
        
        if (features.contains(VOLUME))
        {
            for (int i = 0; i < nLabels; i++)
            {
                table.setValue("Volume", i, results.intrinsicVolumes[i].volume);
            }
        }
        
        if (features.contains(SURFACE_AREA))
        {
            for (int i = 0; i < nLabels; i++)
            {
                table.setValue("SurfaceArea", i, results.intrinsicVolumes[i].surfaceArea);
            }
        }
        
        if (features.contains(SPHERICITY))
        {
            double[] sphericities = new double[nLabels];
            for (int i = 0; i < nLabels; i++)
            {
                sphericities[i] = results.intrinsicVolumes[i].sphericity();
            }
            addColumnToTable(table, "Sphericity", sphericities);
        }
        
        if (features.contains(MEAN_BREADTH))
        {
            for (int i = 0; i < nLabels; i++)
            {
                table.setValue("MeanBreadth", i, results.intrinsicVolumes[i].meanBreadth);
            }
        }
        
        if (features.contains(EULER_NUMBER))
        {
            for (int i = 0; i < nLabels; i++)
            {
                table.setValue("EulerNumber", i, results.intrinsicVolumes[i].eulerNumber);
            }
        }
        
        if (features.contains(BOUNDING_BOX))
        {
            for (int i = 0; i < nLabels; i++)
            {
                Box3D box = results.boundingBoxes[i];
                table.setValue("Box.X.Min", i, box.getXMin());
                table.setValue("Box.X.Max", i, box.getXMax());
                table.setValue("Box.Y.Min", i, box.getYMin());
                table.setValue("Box.Y.Max", i, box.getYMax());
                table.setValue("Box.Z.Min", i, box.getZMin());
                table.setValue("Box.Z.Max", i, box.getZMax());
            }
        }
        
        if (features.contains(CENTROID))
        {
            for (int i = 0; i < nLabels; i++)
            {
                Point3D center = results.centroids[i];
                table.setValue("Centroid.X", i, center.getX());
                table.setValue("Centroid.Y", i, center.getY());
                table.setValue("Centroid.Z", i, center.getZ());
            }
        }
        
        if (features.contains(EQUIVALENT_ELLIPSOID))
        {
            for (int i = 0; i < nLabels; i++)
            {
                Ellipsoid elli = results.ellipsoids[i];
                Point3D center = elli.center();
                table.setValue("Elli.Center.X", i, center.getX());
                table.setValue("Elli.Center.Y", i, center.getY());
                table.setValue("Elli.Center.Z", i, center.getY());
                table.setValue("Elli.Radius1", i, elli.radius1());
                table.setValue("Elli.Radius2", i, elli.radius2());
                table.setValue("Elli.Radius3", i, elli.radius2());
                table.setValue("Elli.Azim", i, elli.phi());
                table.setValue("Elli.Elev", i, elli.theta());
                table.setValue("Elli.Roll", i, elli.psi());
            }
        }
        
        if (features.contains(ELLIPSOID_ELONGATIONS))
        {
            for (int i = 0; i < nLabels; i++)
            {
                double[] elongs = results.ellipsoidElongations[i];
                table.setValue("Elli.R1/R2", i, elongs[0]);
                table.setValue("Elli.R1/R3", i, elongs[1]);
                table.setValue("Elli.R2/R3", i, elongs[2]);                
            }
        }
        
        if (features.contains(MAX_INSCRIBED_BALL))
        {
            for (int i = 0; i < nLabels; i++)
            {
                Point3D center = results.inscribedBalls[i].center();
                table.setValue("InscrBall.Center.X", i, center.getX());
                table.setValue("InscrBall.Center.Y", i, center.getY());
                table.setValue("InscrBall.Center.Z", i, center.getZ());
                table.setValue("InscrBall.Radius", i, results.inscribedBalls[i].radius());
            }
        }
        
        this.fireStatusChanged(this, "");
        return table;
    }
    
    private static final void addColumnToTable(ResultsTable table,
            String colName, double[] values)
    {
        for (int i = 0; i < values.length; i++)
        {
            table.setValue(colName, i, values[i]);
        }
    }

    private static final void addColumnToTable(ResultsTable table,
            String colName, int[] values)
    {
        for (int i = 0; i < values.length; i++)
        {
            table.setValue(colName, i, values[i]);
        }
    }
    
    
    /**
     * Concatenates the results of the analyzes.
     * 
     * For each feature, result is stored as an array with as many elements as
     * the number of considered regions. Region labels are stored in the
     * "labels" field.
     */
    private static final class Results
    {
        /** The list of labels for which features have to be computed.*/
        public int[] labels = null;
        
        public int[] voxelCounts = null;
        public IntrinsicVolumes3D.Result[] intrinsicVolumes = null;
        public Box3D[] boundingBoxes = null;
        public Point3D[] centroids = null;
        public Ellipsoid[] ellipsoids = null;
        public double[][] ellipsoidElongations = null;
        public Sphere[] inscribedBalls = null;
    }
}
