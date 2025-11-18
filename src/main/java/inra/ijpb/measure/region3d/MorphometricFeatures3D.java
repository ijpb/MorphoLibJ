/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
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

import static inra.ijpb.measure.region3d.MorphometricFeatures3D.Feature.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import ij.IJ;
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
import inra.ijpb.util.Tables;

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
	 * An enumeration used to identify which features have to be computed by the
	 * MorphometricFeatures3D class.
	 */
	public enum Feature
	{
		/** The number of voxels that compose the region. */
		VOXEL_COUNT("Voxel_Count"),
		/**
		 * The volume occupied by the region, as the number of voxels multiplied
		 * by image resolution.
		 */
		VOLUME("Volume"),
		/** The surface area of the region (measured by Crofton formula) . */
		SURFACE_AREA("Surface_Area"),
		/**
		 * The mean breadth, proportional to the integral of mean curvature .
		 */
		MEAN_BREADTH("Mean_Breadth"),
		/** The Euler number of the region, to quantify its topology. */
		EULER_NUMBER("Euler_Number"),
		/**
		 * The sphericity, as normalized ratio of powers of volume and surface
		 * area.
		 */
		SPHERICITY("Sphericity"),
		/** The bounding box along each dimension. */
		BOUNDING_BOX("Bounding_Box"),
		/** The centroid. */
		CENTROID("Centroid"),
		/**
		 * The equivalent ellipsoid with same moments up to the second order as
		 * the region.
		 */
		EQUIVALENT_ELLIPSOID("Equivalent_Ellipsoid"),
		/** The elongation factor of the equivalent ellipsoid. */
		ELLIPSOID_ELONGATIONS("Ellipsoid_Elong."),
		/** The radius of the largest inscribed ball. */
		MAX_INSCRIBED_BALL("Max._Inscribed_Ball");

		String label;

		private Feature(String label)
		{
			this.label = label;
		}

		/**
		 * Returns the label associated to this feature.
		 * 
		 * @return the label associated to this feature.
		 */
		@Override
		public String toString()
		{
			return this.label;
		}
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
        
        /**
		 * Iterate over features, in the order they are specified.
		 */
		for (Feature feature : features)
		{
			switch (feature)
			{
				case VOXEL_COUNT:
				{
					Tables.addColumnToTable(table, "VoxelCount", results.voxelCounts);
					break;
				}
				case VOLUME:
				{
					double[] volumes = Stream.of(results.intrinsicVolumes)
							.mapToDouble(res -> res.volume)
							.toArray();
					Tables.addColumnToTable(table, "Volume", volumes);
					break;
				}
				case SURFACE_AREA:
				{
					double[] surfaces = Stream.of(results.intrinsicVolumes)
							.mapToDouble(res -> res.surfaceArea)
							.toArray();
					Tables.addColumnToTable(table, "SurfaceArea", surfaces);
					break;
				}
				case MEAN_BREADTH:
				{
					double[] meanBreadths = Stream.of(results.intrinsicVolumes)
							.mapToDouble(res -> res.meanBreadth)
							.toArray();
					Tables.addColumnToTable(table, "MeanBreadth", meanBreadths);
					break;
				}
				case SPHERICITY:
				{
					double[] sphericites = Stream.of(results.intrinsicVolumes)
							.mapToDouble(res -> res.sphericity())
							.toArray();
					Tables.addColumnToTable(table, "Sphericity", sphericites);
					break;
				}
				case BOUNDING_BOX:
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
					break;
				}
				case CENTROID:
				{
					for (int i = 0; i < nLabels; i++)
					{
						Point3D center = results.centroids[i];
						table.setValue("Centroid.X", i, center.getX());
						table.setValue("Centroid.Y", i, center.getY());
						table.setValue("Centroid.Z", i, center.getZ());
					}
					break;
				}
				case EQUIVALENT_ELLIPSOID:
				{
					for (int i = 0; i < nLabels; i++)
					{
						Ellipsoid elli = results.ellipsoids[i];
						Point3D center = elli.center();
						table.setValue("Elli.Center.X", i, center.getX());
						table.setValue("Elli.Center.Y", i, center.getY());
						table.setValue("Elli.Center.Z", i, center.getZ());
						table.setValue("Elli.Radius1", i, elli.radius1());
						table.setValue("Elli.Radius2", i, elli.radius2());
						table.setValue("Elli.Radius3", i, elli.radius3());
						table.setValue("Elli.Azim", i, elli.phi());
						table.setValue("Elli.Elev", i, elli.theta());
						table.setValue("Elli.Roll", i, elli.psi());
					}
					break;
				}
				case ELLIPSOID_ELONGATIONS:
				{
					for (int i = 0; i < nLabels; i++)
					{
						double[] elongs = results.ellipsoidElongations[i];
						table.setValue("Elli.R1/R2", i, elongs[0]);
						table.setValue("Elli.R1/R3", i, elongs[1]);
						table.setValue("Elli.R2/R3", i, elongs[2]);
					}
					break;
				}
				case MAX_INSCRIBED_BALL:
				{
					for (int i = 0; i < nLabels; i++)
					{
						Point3D center = results.inscribedBalls[i].center();
						table.setValue("InscrBall.Center.X", i, center.getX());
						table.setValue("InscrBall.Center.Y", i, center.getY());
						table.setValue("InscrBall.Center.Z", i, center.getZ());
						table.setValue("InscrBall.Radius", i, results.inscribedBalls[i].radius());
					}
					break;
				}
				case EULER_NUMBER:
				{
					double[] eulerNumbers = Stream.of(results.intrinsicVolumes)
							.mapToDouble(res -> res.eulerNumber)
							.toArray();
					Tables.addColumnToTable(table, "EulerNumber", eulerNumbers);
					break;
				}
				default:
				{
					IJ.log("Unknown feature: " + feature.toString());
				}
			}
		}

		this.fireStatusChanged(this, "");
		return table;
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
