/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2026 INRA.
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
package inra.ijpb.measure.region2d;

import static inra.ijpb.measure.region2d.MorphometricFeatures2D.Feature.*;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.geometry.Box2D;
import inra.ijpb.geometry.Circle2D;
import inra.ijpb.geometry.Ellipse;
import inra.ijpb.geometry.OrientedBox2D;
import inra.ijpb.geometry.PointPair2D;
import inra.ijpb.label.LabelImages;
import inra.ijpb.util.Tables;

/**
 * An analyzer class that aggregates the computation of several morphometric features 
 * for 2D regions.
 * 
 * The class works both as a container of features, used to identify which features will
 * quantify regions, and as a computation class. It may extend the "RegionAnalyzer" 
 * interface in a future release.
 * 
 *  <p>
 *  Usage:
 *  <pre>{@code
 *  ResultsTable table = new MorphometricFeatures2D()
 *      .add(Feature.AREA)
 *      .add(Feature.PERIMETER)
 *      .add(Feature.CENTROID)
 *      .computeTable(imagePlus);
 *  }
 *  </pre>
 *  
 *  <p>
 *  Alternative:
 *  <pre>{@code
 *  MorphometricFeatures2D morpho = new MorphometricFeatures2D(
 *      Feature.AREA, Feature.PERIMETER, Feature.CENTROID);
 *  ResultsTable table = morpho.computeTable(imagePlus);
 *  }
 *  </pre>
 *
 * @see inra.ijpb.measure.region3d.MorphometricFeatures3D
 * 
 * @author dlegland
 */
public class MorphometricFeatures2D extends AlgoStub
{
    /**
     * An enumeration used to identify which features have to be
     * computed by the MorphometricFeatures2D class.
     */
    public enum Feature
    {
        /** The number of pixels that compose the region.*/
        PIXEL_COUNT("Pixel_Count"), 
        /** The area occupied by the region, as the number of pixels multiplied by image resolution.*/
        AREA("Area"),
        /** The (Crofton) perimeter of the region.*/
        PERIMETER("Perimeter"),
        /** The Euler number of the region, to quantify its topology.*/ 
        EULER_NUMBER("Euler_Number"),
        /** The circularity, as normalized ratio of area and squared perimeter.*/
        CIRCULARITY("Circularity"),
        /** The bounding box, defined from extents along each dimension.*/
        BOUNDING_BOX("Bounding_Box"),
        /** The centroid.*/
        CENTROID("Centroid"),
        /** The equivalent ellipse with same moments up to the second order as the region.*/
        EQUIVALENT_ELLIPSE("Equivalent_Ellipse"),
        /** The elongation factor of the equivalent ellipse.*/
        ELLIPSE_ELONGATION("Ellipse_Elong."),
        /** The ration of area over area of convex hull.*/
        CONVEXITY("Convexity"),
        /** The largest Feret diameter.*/
        MAX_FERET_DIAMETER("Max._Feret_Diameter"),
        /** The oriented box with minimum width.*/
        ORIENTED_BOX("Oriented_Box"),
        /** The elongation of the oriented box.*/
        ORIENTED_BOX_ELONGATION("Oriented_Box_Elong."),
        /** The largest geodesic diameter within the region.*/
        GEODESIC_DIAMETER("Geodesic_Diameter"),
        /** The ratio of geodesic diameter over Feret diameter.*/
        TORTUOSITY("Tortuosity"),
        /** The radius of the largest inscribed disk.*/
        MAX_INSCRIBED_DISK("Max._Inscribed_Disc"),
        /** The average thickness, measured along the skeleton.*/
        AVERAGE_THICKNESS("Average_Thickness"),
        /** The ratio of geodesic diameter over diameter of inscribed disk.*/
        GEODESIC_ELONGATION("Geodesic_Elong.");
    	
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
    

    
    // ====================================================
    // Constructor
    
    /**
     * Creates a new MorphometricFeatures2D instance containing no feature.
     */
    public MorphometricFeatures2D()
    {
    }
    
    /**
     * Creates a new instance of MorphometricFeatures2D from the series of selected
     * features.
     * 
     * @param features
     *            the features to use.
     */
    public MorphometricFeatures2D(Feature... features)
    {
        for (Feature f : features)
        {
            this.features.add(f);
        }
    }
    

    // ====================================================
    // Management of features
    
    /**
     * Returns the features this MorphometricFeatures2D instance will compute.
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
     * @return this instance of MorphometricFeatures2D, to allow chaining
     *         operations
     */
    public MorphometricFeatures2D add(Feature f)
    {
        features.add(f);
        return this;
    }
    
    /**
     * Removes a feature from the collection of features to compute.
     * 
     * @param f
     *            the feature to remove
     * @return this instance of MorphometricFeatures2D, to allow chaining
     *         operations
     */
    public MorphometricFeatures2D remove(Feature f)
    {
        features.remove(f);
        return this;
    }
    
    /**
     * Checks if this MorphometricFeatures2D instance contains the specified
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
     * Checks if this MorphometricFeatures2D instance contains any of the
     * specified features.
     * 
     * @param features
     *            the list of features to test
     * @return true if this instance contains at least one of the specified
     *         feature
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
        ImageProcessor image = imagePlus.getProcessor();
        Calibration calib = imagePlus.getCalibration();
        
        // identify labels
        int[] labels = LabelImages.findAllLabels(image);
        
        // Parameters to be computed
        Results results = new Results();
        results.labels = labels;
        
        // compute intrinsic volumes
        if (contains(PIXEL_COUNT))
        {
            this.fireStatusChanged(this, "Pixel Count");
            results.pixelCounts = LabelImages.pixelCount(image, labels);
        }
        
        // compute intrinsic volumes
        if (containsAny(AREA, PERIMETER, EULER_NUMBER, CIRCULARITY))
        {
            this.fireStatusChanged(this, "Intrinsic Volumes");
            
            // Create and setup computation class
            IntrinsicVolumes2D algo = new IntrinsicVolumes2D();
            algo.setDirectionNumber(4);
            algo.setConnectivity(4);
            DefaultAlgoListener.monitor(algo);
            
            // run analysis
            results.intrinsicVolumes = algo.analyzeRegions(image, labels, calib);
        }
        
        if (contains(BOUNDING_BOX))
        {
            this.fireStatusChanged(this, "Bounding boxes");
            BoundingBox algo = new BoundingBox();
            DefaultAlgoListener.monitor(algo);
            results.boundingBoxes = algo.analyzeRegions(image, labels, calib);
        }
        
        if (containsAny(EQUIVALENT_ELLIPSE, ELLIPSE_ELONGATION))
        {
            this.fireStatusChanged(this, "Equivalent Ellipse");
            EquivalentEllipse algo = new EquivalentEllipse();
            DefaultAlgoListener.monitor(algo);
            results.ellipses = algo.analyzeRegions(image, labels, calib);
            
            if (features.contains(CENTROID))
            {
                results.centroids = Ellipse.centers(results.ellipses);
            }
        } 
        else if (contains(CENTROID))
        {
            // Compute centroids if not already computed from equivalent ellipses
            this.fireStatusChanged(this, "Centroids");
            Centroid algo = new Centroid();
            DefaultAlgoListener.monitor(algo);
            results.centroids = algo.analyzeRegions(image, labels, calib);
        }
        
        if (contains(CONVEXITY))
        {
            this.fireStatusChanged(this, "Convexity");
            Convexity algo = new Convexity();
            DefaultAlgoListener.monitor(algo);
            results.convexities = algo.analyzeRegions(image, labels, calib);
        }
        
        if (containsAny(MAX_FERET_DIAMETER, TORTUOSITY))
        {
            this.fireStatusChanged(this, "Max Feret Diameters");
            results.maxFeretDiams = MaxFeretDiameter.maxFeretDiameters(image, labels,
                    calib);
        }
        
        if (containsAny(ORIENTED_BOX, ORIENTED_BOX_ELONGATION))
        {
            this.fireStatusChanged(this, "Oriented Bounding Boxes");
            OrientedBoundingBox2D algo = new OrientedBoundingBox2D();
            DefaultAlgoListener.monitor(algo);
            results.orientedBoxes = algo.analyzeRegions(image, labels, calib);
        }
        
        if (containsAny(GEODESIC_DIAMETER, TORTUOSITY, GEODESIC_ELONGATION))
        {
            this.fireStatusChanged(this, "Geodesic diameters");
            GeodesicDiameter algo = new GeodesicDiameter();
            DefaultAlgoListener.monitor(algo);
            results.geodDiams = algo.analyzeRegions(image, labels, calib);
        }
        
        if (containsAny(MAX_INSCRIBED_DISK, GEODESIC_ELONGATION))
        {
            this.fireStatusChanged(this, "Inscribed circles");
            LargestInscribedCircle algo = new LargestInscribedCircle();
            DefaultAlgoListener.monitor(algo);
            results.inscrDiscs = algo.analyzeRegions(image, labels, calib);
        }
        
        if (contains(AVERAGE_THICKNESS))
        {
            this.fireStatusChanged(this, "Average Thickness");
            AverageThickness algo = new AverageThickness();
            DefaultAlgoListener.monitor(algo);
            results.avgThickness = algo.analyzeRegions(image, labels, calib);
        }
        
        this.fireStatusChanged(this, "");
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
				case PIXEL_COUNT:
				{
					Tables.addColumnToTable(table, "PixelCount", results.pixelCounts);
					break;
				}
				case AREA:
				{
					double[] areas = Stream.of(results.intrinsicVolumes)
							.mapToDouble(res -> res.area)
							.toArray();
					Tables.addColumnToTable(table, "Area", areas);
					break;
				}
				case PERIMETER:
				{
					double[] perimeters = Stream.of(results.intrinsicVolumes)
							.mapToDouble(res -> res.perimeter)
							.toArray();
					Tables.addColumnToTable(table, "Perimeter", perimeters);
					break;
				}
				case CIRCULARITY:
				{
					double[] circularities = IntrinsicVolumes2DUtils.computeCircularities(results.intrinsicVolumes);
					Tables.addColumnToTable(table, "Circularity", circularities);
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
				case BOUNDING_BOX:
				{
					for (int i = 0; i < nLabels; i++)
					{
						Box2D box = results.boundingBoxes[i];
						table.setValue("Box.X.Min", i, box.getXMin());
						table.setValue("Box.X.Max", i, box.getXMax());
						table.setValue("Box.Y.Min", i, box.getYMin());
						table.setValue("Box.Y.Max", i, box.getYMax());
					}
					break;
				}
				case CENTROID:
				{
					for (int i = 0; i < nLabels; i++)
					{
						Point2D center = results.centroids[i];
						table.setValue("Centroid.X", i, center.getX());
						table.setValue("Centroid.Y", i, center.getY());
					}
					break;
				}
				case EQUIVALENT_ELLIPSE:
				{
					for (int i = 0; i < nLabels; i++)
					{
						Ellipse elli = results.ellipses[i];
						Point2D center = elli.center();
						table.setValue("Ellipse.Center.X", i, center.getX());
						table.setValue("Ellipse.Center.Y", i, center.getY());
						table.setValue("Ellipse.Radius1", i, elli.radius1());
						table.setValue("Ellipse.Radius2", i, elli.radius2());
						table.setValue("Ellipse.Orientation", i, elli.orientation());
					}
					break;
				}
				case ELLIPSE_ELONGATION:
				{
					double[] elong = new double[nLabels];
					for (int i = 0; i < nLabels; i++)
					{
						Ellipse elli = results.ellipses[i];
						elong[i] = elli.radius1() / elli.radius2();
					}
					Tables.addColumnToTable(table, "Ellipse.Elong", elong);
					break;
				}
				case CONVEXITY:
				{
					for (int i = 0; i < nLabels; i++)
					{
						table.setValue("ConvexArea", i, results.convexities[i].convexArea);
						table.setValue("Convexity", i, results.convexities[i].convexity);
					}
					break;
				}
				case MAX_FERET_DIAMETER:
				{
					for (int i = 0; i < nLabels; i++)
					{
						table.setValue("MaxFeretDiam", i, results.maxFeretDiams[i].diameter());
						table.setValue("MaxFeretDiamAngle", i, Math.toDegrees(results.maxFeretDiams[i].angle()));
					}
					break;
				}
				case ORIENTED_BOX:
				{
					for (int i = 0; i < nLabels; i++)
					{
						OrientedBox2D obox = results.orientedBoxes[i];
						Point2D center = obox.center();
						table.setValue("OBox.Center.X", i, center.getX());
						table.setValue("OBox.Center.Y", i, center.getY());
						table.setValue("OBox.Length", i, obox.length());
						table.setValue("OBox.Width", i, obox.width());
						table.setValue("OBox.Orientation", i, obox.orientation());
					}
					break;
				}
				case ORIENTED_BOX_ELONGATION:
				{
					double[] elongations = Stream.of(results.orientedBoxes)
							.mapToDouble(obox ->  obox.length() / obox.width())
							.toArray();
					Tables.addColumnToTable(table, "OBox.Elongation", elongations);
					break;
				}
				case GEODESIC_DIAMETER:
				{
					double[] diameter = Stream.of(results.geodDiams)
							.mapToDouble(gdData ->  gdData.diameter)
							.toArray();
					Tables.addColumnToTable(table, "GeodesicDiameter", diameter);
					break;
				}
				case TORTUOSITY:
				{
					double[] tortuosity = new double[nLabels];
					for (int i = 0; i < nLabels; i++)
					{
						tortuosity[i] = results.geodDiams[i].diameter / results.maxFeretDiams[i].diameter();
					}
					Tables.addColumnToTable(table, "Tortuosity", tortuosity);
					break;
				}
				case MAX_INSCRIBED_DISK:
				{
					for (int i = 0; i < nLabels; i++)
					{
						Point2D center = results.inscrDiscs[i].getCenter();
						table.setValue("InscrDisc.Center.X", i, center.getX());
						table.setValue("InscrDisc.Center.Y", i, center.getY());
						table.setValue("InscrDisc.Radius", i, results.inscrDiscs[i].getRadius());
					}
					break;
				}
				case AVERAGE_THICKNESS:
				{
					for (int i = 0; i < nLabels; i++)
					{
						AverageThickness.Result res = results.avgThickness[i];
						table.setValue("AverageThickness", i, res.avgThickness);
					}
					break;
				}
				case GEODESIC_ELONGATION:
				{
					double[] elong = new double[nLabels];
					for (int i = 0; i < nLabels; i++)
					{
						elong[i] = results.geodDiams[i].diameter / (results.inscrDiscs[i].getRadius() * 2);
					}
					Tables.addColumnToTable(table, "GeodesicElongation", elong);
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
        
        public int[] pixelCounts = null;
        public IntrinsicVolumes2D.Result[] intrinsicVolumes = null;
        public Box2D[] boundingBoxes = null;
        public Point2D[] centroids = null;
        public Ellipse[] ellipses = null;
        public Convexity.Result[] convexities = null;
        public PointPair2D[] maxFeretDiams = null;
        public OrientedBox2D[] orientedBoxes = null;
        public GeodesicDiameter.Result[] geodDiams = null;
        public Circle2D[] inscrDiscs = null;
        public AverageThickness.Result[] avgThickness = null;
    }
}
