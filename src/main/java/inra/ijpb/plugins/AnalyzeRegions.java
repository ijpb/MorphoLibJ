/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2017 INRA.
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
package inra.ijpb.plugins;


import java.awt.geom.Point2D;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.geometry.Box2D;
import inra.ijpb.geometry.Circle2D;
import inra.ijpb.geometry.Ellipse;
import inra.ijpb.geometry.OrientedBox2D;
import inra.ijpb.geometry.PointPair2D;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.region2d.AverageThickness;
import inra.ijpb.measure.region2d.BoundingBox;
import inra.ijpb.measure.region2d.Centroid;
import inra.ijpb.measure.region2d.Convexity;
import inra.ijpb.measure.region2d.GeodesicDiameter;
import inra.ijpb.measure.region2d.EquivalentEllipse;
import inra.ijpb.measure.region2d.IntrinsicVolumes2DUtils;
import inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D;
import inra.ijpb.measure.region2d.LargestInscribedCircle;
import inra.ijpb.measure.region2d.MaxFeretDiameter;
import inra.ijpb.measure.region2d.OrientedBoundingBox2D;

/**
 * Plugin for computing morphological feature of regions from label images.
 * 
 * Can be used programmatically: 
 * <pre>{@code
 * // creates a new Features instance to select the features to compute.  
 * AnalyzeRegions.Features features = new AnalyzeRegions.Features();
 * features.setAll(false);
 * features.area = true;
 * features.perimeter = true;
 * features.centroid = true;
 * // compute the features, and returns the corresponding table
 * ResultsTable table = AnalyzeRegions.process(imagePlus, features);
 * table.show(imagePlus.getShortTitle() + "-Morphometry");
 * }</pre>
 * 
 * 
 */
public class AnalyzeRegions implements PlugInFilter 
{
    // ====================================================
    // Static methods
    
	/**
     * Computes a set of descriptive features from a label image and
     * concatenates the results into a ResultsTable.
     * @param imagePlus
     *            the image to analyze.
     * @param features
     *            the features to compute.
     * @return the results of the analysis as a Results Table
     */
    public static final ResultsTable process(ImagePlus imagePlus, Features features)
    {
        Results results = computeFeatures(imagePlus, features);
        return populateTable(features, results);
    }

    private static final Results computeFeatures(ImagePlus imagePlus, Features features)
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
        if (features.pixelCount)
        {
            IJ.showStatus("Pixel Count");
            results.pixelCounts = LabelImages.pixelCount(image, labels);
        }
        
        // compute intrinsic volumes
        if (features.area || features.perimeter || features.eulerNumber || features.circularity)
        {
            IJ.showStatus("Intrinsic Volumes");
            
            // Create and setup computation class
            IntrinsicVolumesAnalyzer2D algo = new IntrinsicVolumesAnalyzer2D();
            algo.setDirectionNumber(4);
            algo.setConnectivity(4);
            DefaultAlgoListener.monitor(algo);
            
            // run analysis
            results.intrinsicVolumes = algo.analyzeRegions(image, labels, calib);
        }
        
        if (features.boundingBox)
        {
            IJ.showStatus("Bounding boxes");
            BoundingBox algo = new BoundingBox();
            DefaultAlgoListener.monitor(algo);
            results.boundingBoxes = algo.analyzeRegions(image, labels, calib);
        }
        
        if (features.equivalentEllipse || features.ellipseElongation)
        {
            IJ.showStatus("Equivalent Ellipse");
            EquivalentEllipse algo = new EquivalentEllipse();
            DefaultAlgoListener.monitor(algo);
            results.ellipses = algo.analyzeRegions(image, labels, calib);
            
            if (features.centroid)
            {
                results.centroids = Ellipse.centers(results.ellipses);
            }
        } 
        else if (features.centroid)
        {
            // Compute centroids if not already computed from inertia ellipsoid
            IJ.showStatus("Centroids");
            Centroid algo = new Centroid();
            DefaultAlgoListener.monitor(algo);
            results.centroids = algo.analyzeRegions(image, labels, calib);
        }
        
        if (features.convexity)
        {
            IJ.showStatus("Convexity");
            Convexity algo = new Convexity();
            DefaultAlgoListener.monitor(algo);
            results.convexities = algo.analyzeRegions(image, labels, calib);
        }
        
        if (features.maxFeretDiameter || features.tortuosity)
        {
            IJ.showStatus("Max Feret Diameters");
            results.maxFeretDiams = MaxFeretDiameter.maxFeretDiameters(image, labels,
                    calib);
        }
        
        if (features.orientedBox)
        {
            IJ.showStatus("Oriented Bounding Boxes");
            OrientedBoundingBox2D algo = new OrientedBoundingBox2D();
            DefaultAlgoListener.monitor(algo);
            results.orientedBoxes = algo.analyzeRegions(image, labels, calib);
        }
        
        if (features.geodesicDiameter || features.tortuosity)
        {
            IJ.showStatus("Geodesic diameters");
            GeodesicDiameter algo = new GeodesicDiameter();
            DefaultAlgoListener.monitor(algo);
            results.geodDiams = algo.analyzeRegions(image, labels, calib);
        }
        
        if (features.maxInscribedDisc || features.geodesicElongation)
        {
            IJ.showStatus("Inscribed circles");
            LargestInscribedCircle algo = new LargestInscribedCircle();
            DefaultAlgoListener.monitor(algo);
            results.inscrDiscs = algo.analyzeRegions(image, labels, calib);
        }
        
        if (features.averageThickness)
        {
            IJ.showStatus("Average Thickness");
            AverageThickness algo = new AverageThickness();
            DefaultAlgoListener.monitor(algo);
            results.avgThickness = algo.analyzeRegions(image, labels, calib);
        }
        
        return results;
    }

    /**
     * Creates the results table that summarizes the features computed on the
     * image.
     * 
     * @param features
     *            the features to add into the table.
     * @param results
     *            the results of computations on an image.
     * @return the results table that summarizes the features computed on the
     *         image.
     */
    private static final ResultsTable populateTable(Features features, Results results)
    {
        IJ.showStatus("Populate table");
        
        // create results table
        ResultsTable table = new ResultsTable();
        
        // Initialize labels
        int nLabels = results.labels.length;
        for (int i = 0; i < nLabels; i++)
        {
            table.incrementCounter();
            table.addLabel("" + results.labels[i]);
        }
        
    
        if (features.pixelCount)
        {
            addColumnToTable(table, "PixelCount", results.pixelCounts);
        }
        
        if (features.area)
        {
            for (int i = 0; i < nLabels; i++)
            {
                table.setValue("Area", i, results.intrinsicVolumes[i].area);
            }
        }
        
        if (features.perimeter)
        {
            for (int i = 0; i < nLabels; i++)
            {
                table.setValue("Perimeter", i, results.intrinsicVolumes[i].perimeter);
            }
        }
        
        if (features.circularity)
        {
            double[] circularities = IntrinsicVolumes2DUtils.computeCircularities(results.intrinsicVolumes);
            addColumnToTable(table, "Circularity", circularities);
        }
        
        if (features.eulerNumber)
        {
            for (int i = 0; i < nLabels; i++)
            {
                table.setValue("EulerNumber", i, results.intrinsicVolumes[i].eulerNumber);
            }
        }
        
        if (features.boundingBox)
        {
            for (int i = 0; i < nLabels; i++)
            {
                Box2D box = results.boundingBoxes[i];
                table.setValue("Box.X.Min", i, box.getXMin());
                table.setValue("Box.X.Max", i, box.getXMax());
                table.setValue("Box.Y.Min", i, box.getYMin());
                table.setValue("Box.Y.Max", i, box.getYMax());
            }
        }
        
        if (features.centroid)
        {
            for (int i = 0; i < nLabels; i++)
            {
                Point2D center = results.centroids[i];
                table.setValue("Centroid.X", i, center.getX());
                table.setValue("Centroid.Y", i, center.getY());
            }
        }
        
        if (features.equivalentEllipse)
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
        }
        
        if (features.ellipseElongation)
        {
            double[] elong = new double[nLabels];
            for (int i = 0; i < nLabels; i++)
            {
                Ellipse elli = results.ellipses[i];
                elong[i] = elli.radius1() / elli.radius2();
            }
            addColumnToTable(table, "Ellipse.Elong", elong);
        }
        
        if (features.convexity)
        {
            for (int i = 0; i < nLabels; i++)
            {
                table.setValue("ConvexArea", i, results.convexities[i].convexArea);
                table.setValue("Convexity", i, results.convexities[i].convexity);
            }
        }
        
        if (features.maxFeretDiameter)
        {
            for (int i = 0; i < nLabels; i++)
            {
                table.setValue("MaxFeretDiam", i, results.maxFeretDiams[i].diameter());
                table.setValue("MaxFeretDiamAngle", i, Math.toDegrees(results.maxFeretDiams[i].angle()));
            }
        }
        
        if (features.orientedBox)
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
        }
        
        if (features.geodesicDiameter)
        {
            for (int i = 0; i < nLabels; i++)
            {
                GeodesicDiameter.Result result = results.geodDiams[i];
                table.setValue("GeodesicDiameter", i, result.diameter);
            }
        }
        
        if (features.tortuosity)
        {
            double[] tortuosity = new double[nLabels];
            for (int i = 0; i < nLabels; i++)
            {
                tortuosity[i] = results.geodDiams[i].diameter / results.maxFeretDiams[i].diameter();
            }
            addColumnToTable(table, "Tortuosity", tortuosity);
        }
        
        if (features.maxInscribedDisc)
        {
            for (int i = 0; i < nLabels; i++)
            {
                Point2D center = results.inscrDiscs[i].getCenter();
                table.setValue("InscrDisc.Center.X", i, center.getX());
                table.setValue("InscrDisc.Center.Y", i, center.getY());
                table.setValue("InscrDisc.Radius", i, results.inscrDiscs[i].getRadius());
            }
        }
        
        if (features.averageThickness)
        {
            for (int i = 0; i < nLabels; i++)
            {
                AverageThickness.Result res = results.avgThickness[i];
                table.setValue("AverageThickness", i, res.avgThickness);
            }
        }
        
        if (features.geodesicElongation)
        {
            double[] elong = new double[nLabels];
            for (int i = 0; i < nLabels; i++)
            {
                elong[i] = results.geodDiams[i].diameter
                        / (results.inscrDiscs[i].getRadius() * 2);
            }
            addColumnToTable(table, "GeodesicElongation", elong);
        }
        
        IJ.showStatus("");
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
    
    
    // ====================================================
    // Class variables
    
    ImagePlus imagePlus;
	
    
    // ====================================================
    // Implementation of Plugin and PluginFilter interface 
    
	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	@Override
	public int setup(String arg, ImagePlus imp)
	{
		if (imp == null)
		{
			IJ.noImage();
			return DONE;
		}

		this.imagePlus = imp;
		return DOES_ALL | NO_CHANGES;
	}

	/* (non-Javadoc)
     * @see ij.plugin.PlugIn#run(java.lang.String)
     */
    public void run(ImageProcessor ip) {
        
        // check if image is a label image
		// Check if image may be a label image
		if (!LabelImages.isLabelImageType(imagePlus))
		{
           IJ.showMessage("Input image should be a label image");
            return;
        }

        // create the dialog, with operator options
        Features features = chooseFeatures(null);
        // If cancel was clicked, features is null
        if (features == null)
        {
            return;
        }
        
        // Call the main processing method
        ResultsTable table = process(imagePlus, features);
        
        // show result
        String tableName = imagePlus.getShortTitle() + "-Morphometry";
        table.show(tableName);
    }
    
    private static final Features chooseFeatures(Features initialChoice)
    {
        if (initialChoice == null)
        {
            initialChoice = new Features();
        }
        GenericDialog gd = new GenericDialog("Analyze Regions");
        gd.addCheckbox("Pixel_Count", initialChoice.pixelCount);
        gd.addCheckbox("Area", initialChoice.area);
        gd.addCheckbox("Perimeter", initialChoice.perimeter);
        gd.addCheckbox("Circularity", initialChoice.circularity);
        gd.addCheckbox("Euler_Number", initialChoice.eulerNumber);
        gd.addCheckbox("Bounding_Box", initialChoice.boundingBox);
        gd.addCheckbox("Centroid", initialChoice.centroid);
        gd.addCheckbox("Equivalent_Ellipse", initialChoice.equivalentEllipse);
        gd.addCheckbox("Ellipse_Elong.", initialChoice.ellipseElongation);
        gd.addCheckbox("Convexity", initialChoice.convexity);
        gd.addCheckbox("Max._Feret Diameter", initialChoice.maxFeretDiameter);
        gd.addCheckbox("Oriented_Box", initialChoice.orientedBox);
        gd.addCheckbox("Oriented_Box_Elong.", initialChoice.orientedBoxElongation);
        gd.addCheckbox("Geodesic Diameter", initialChoice.geodesicDiameter);
        gd.addCheckbox("Tortuosity", initialChoice.tortuosity);
        gd.addCheckbox("Max._Inscribed_Disc", initialChoice.maxInscribedDisc);
        gd.addCheckbox("Average_Thickness", initialChoice.averageThickness);
        gd.addCheckbox("Geodesic_Elong.", initialChoice.geodesicElongation);
        gd.showDialog();
        
        // If cancel was clicked, do nothing
        if (gd.wasCanceled())
        {
            return null;
        }
    
        // Extract features to quantify from image
        Features features = new Features();
        features.pixelCount         = gd.getNextBoolean();
        features.area               = gd.getNextBoolean();
        features.perimeter          = gd.getNextBoolean();
        features.circularity        = gd.getNextBoolean();
        features.eulerNumber        = gd.getNextBoolean();
        features.boundingBox        = gd.getNextBoolean();
        features.centroid           = gd.getNextBoolean();
        features.equivalentEllipse  = gd.getNextBoolean();
        features.ellipseElongation  = gd.getNextBoolean();
        features.convexity          = gd.getNextBoolean();
        features.maxFeretDiameter   = gd.getNextBoolean();
        features.orientedBox        = gd.getNextBoolean();
        features.orientedBoxElongation = gd.getNextBoolean();
        features.geodesicDiameter   = gd.getNextBoolean();
        features.tortuosity         = gd.getNextBoolean();
        features.maxInscribedDisc   = gd.getNextBoolean();
        features.averageThickness   = gd.getNextBoolean();
        features.geodesicElongation = gd.getNextBoolean();
        
        return features;
    }

    /**
     * Process the input image.
     * 
     * @deprecated replaced by static process(ImagePlus, Features) method
     * 
     * @param imagePlus
     *            the image to process
     * @return a ResultsTable summarizing the features
     */
    @Deprecated
    public ResultsTable process(ImagePlus imagePlus)
    {
        return process(imagePlus, new Features());
    }
    
    
    // ====================================================
    // Inner classes
    
    /**
     * The list of features to compute.
     * 
     * Default initialization is to compute everything except the pixel count.
     */
    public static final class Features
    {
        /** The boolean flag for computing pixel count.*/
        public boolean pixelCount = false;
        /** The boolean flag for computing area.*/
        public boolean area = true;
        /** The boolean flag for computing perimeter.*/
        public boolean perimeter = true;
        /** The boolean flag for computing circularity.*/
        public boolean circularity = true;
        /** The boolean flag for computing Euler number.*/
        public boolean eulerNumber = true;
        /** The boolean flag for computing bounding box.*/
        public boolean boundingBox = true;
        /** The boolean flag for computing centroid.*/
        public boolean centroid = true;
        /** The boolean flag for computing equivalent ellipse.*/
        public boolean equivalentEllipse = true;
        /** The boolean flag for computing ellipse elongation.*/
        public boolean ellipseElongation = true;
        /** The boolean flag for computing convexity.*/
        public boolean convexity = true;
        /** The boolean flag for computing maximum Feret diameter.*/
        public boolean maxFeretDiameter = true;
        /** The boolean flag for computing oriented box.*/
        public boolean orientedBox = true;
        /** The boolean flag for computing elongation of oriented box.*/
        public boolean orientedBoxElongation = true;
        /** The boolean flag for computing geodesic diameter.*/
        public boolean geodesicDiameter = true;
        /** The boolean flag for computing tortuosity.*/
        public boolean tortuosity = true;
        /** The boolean flag for computing largest inscribed disc.*/
        public boolean maxInscribedDisc = true;
        /** The boolean flag for computing average thickness.*/
        public boolean averageThickness = true;
        /** The boolean flag for computing geodesic elongation.*/
        public boolean geodesicElongation = true;
        
        /**
         * Set the state of all features.
         * 
         * @param state
         *            the state to set.
         */
        public void setAll(boolean state)
        {
            this.pixelCount = state;
            this.area = state;
            this.perimeter = state;
            this.circularity = state;
            this.eulerNumber = state;
            this.boundingBox = state;
            this.centroid = state;
            this.equivalentEllipse = state;
            this.ellipseElongation = state;
            this.convexity = state;
            this.maxFeretDiameter = state;
            this.orientedBox = state;
            this.orientedBoxElongation = state;
            this.geodesicDiameter = state;
            this.tortuosity = state;
            this.maxInscribedDisc = state;
            this.averageThickness = state;
            this.geodesicElongation = state;
        }
    }

    /**
     * Concatenates the results of the analyzes.
     */
    private static final class Results
    {
        public int[] labels = null;
        
        public int[] pixelCounts = null;
        public IntrinsicVolumesAnalyzer2D.Result[] intrinsicVolumes = null;
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
