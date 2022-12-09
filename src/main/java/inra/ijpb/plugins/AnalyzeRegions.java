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
    
//	/**
//     * Computes a set of descriptive features from a label image and
//     * concatenates the results into a ResultsTable.
//     * @param imagePlus
//     *            the image to analyze.
//     * @param features
//     *            the features to compute.
//     * @return the results of the analysis as a Results Table
//     */
//    public static final ResultsTable process(ImagePlus imagePlus, FeatureSet features)
//    {
//        return new Analyzer(features).analyze(imagePlus);
//    }
    
    
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
        Analyzer analyzer = createAnalyzer();
        
        // If cancel was clicked, features is null
        if (analyzer == null)
        {
            return;
        }
        
        // Call the main processing method
        ResultsTable table = analyzer.analyze(imagePlus);
        
        // show result
        String tableName = imagePlus.getShortTitle() + "-Morphometry";
        table.show(tableName);
    }
    
    private static final Analyzer createAnalyzer()
    {
        Analyzer initialChoice = new Analyzer();
        
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
    
        Analyzer analyzer = new Analyzer();
        analyzer.usePixelCount(gd.getNextBoolean());
        analyzer.useArea(gd.getNextBoolean());
        analyzer.usePerimeter(gd.getNextBoolean());
        analyzer.useCircularity(gd.getNextBoolean());
        analyzer.useEulerNumber(gd.getNextBoolean());
        analyzer.useBoundingBox(gd.getNextBoolean());
        analyzer.useCentroid(gd.getNextBoolean());
        analyzer.useEquivalentEllipse(gd.getNextBoolean());
        analyzer.useEllipseElongation(gd.getNextBoolean());
        analyzer.useConvexity(gd.getNextBoolean());
        analyzer.useMaxFeretDiameter(gd.getNextBoolean());
        analyzer.useOrientedBox(gd.getNextBoolean());
        analyzer.useOrientedBoxElongation(gd.getNextBoolean());
        analyzer.useGeodesicDiameter(gd.getNextBoolean());
        analyzer.useTortuosity(gd.getNextBoolean());
        analyzer.useMaxInscribedDisc(gd.getNextBoolean());
        analyzer.useAverageThickness(gd.getNextBoolean());
        analyzer.useGeodesicElongation(gd.getNextBoolean());
        
        return analyzer;
    }
    
    
    // ====================================================
    // Inner classes
    
    /**
     * An analyzer class that can be used to compute features of regions without instancing the enclosing Plugin.
     * 
     *  <p>
     *  Usage:
     *  <pre>{@code
     *  ResultsTable table = new AnalyzeRegions.Analyzer()
     *      .useArea(true)
     *      .usePerimeter(true)
     *      .useCentroid(true)
     *      .analyze(imagePlus);
     *  }
     *  </pre>
     */
    public static final class Analyzer
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
        
        public Analyzer()
        {
        }
        
        
        // ====================================================
        // Setter methods
        
        public Analyzer usePixelCount(boolean b)
        {
            this.pixelCount = b;
            return this;
        }
        
        public Analyzer useArea(boolean b)
        {
            this.area = b;
            return this;
        }
        
        public Analyzer usePerimeter(boolean b)
        {
            this.perimeter = b;
            return this;
        }
        
        public Analyzer useCircularity(boolean b)
        {
            this.circularity = b;
            return this;
        }
        
        public Analyzer useEulerNumber(boolean b)
        {
            this.eulerNumber = b;
            return this;
        }
        
        public Analyzer useBoundingBox(boolean b)
        {
            this.boundingBox = b;
            return this;
        }
        
        public Analyzer useCentroid(boolean b)
        {
            this.centroid = b;
            return this;
        }
        
        public Analyzer useEquivalentEllipse(boolean b)
        {
            this.equivalentEllipse = b;
            return this;
        }
        
        public Analyzer useEllipseElongation(boolean b)
        {
            this.ellipseElongation = b;
            return this;
        }
        
        public Analyzer useConvexity(boolean b)
        {
            this.convexity = b;
            return this;
        }
        
        public Analyzer useMaxFeretDiameter(boolean b)
        {
            this.maxFeretDiameter = b;
            return this;
        }
        
        public Analyzer useOrientedBox(boolean b)
        {
            this.orientedBox = b;
            return this;
        }
        
        public Analyzer useOrientedBoxElongation(boolean b)
        {
            this.orientedBoxElongation = b;
            return this;
        }
        
        public Analyzer useGeodesicDiameter(boolean b)
        {
            this.geodesicDiameter = b;
            return this;
        }
        
        public Analyzer useTortuosity(boolean b)
        {
            this.tortuosity = b;
            return this;
        }
        
        public Analyzer useMaxInscribedDisc(boolean b)
        {
            this.maxInscribedDisc = b;
            return this;
        }
        
        public Analyzer useAverageThickness(boolean b)
        {
            this.averageThickness = b;
            return this;
        }
        
        public Analyzer useGeodesicElongation(boolean b)
        {
            this.geodesicElongation = b;
            return this;
        }
        
        public Analyzer useAll()
        {
            this.pixelCount = true;
            this.area = true;
            this.perimeter = true;
            this.circularity = true;
            this.eulerNumber = true;
            this.boundingBox = true;
            this.centroid = true;
            this.equivalentEllipse = true;
            this.ellipseElongation = true;
            this.convexity = true;
            this.maxFeretDiameter = true;
            this.orientedBox = true;
            this.orientedBoxElongation = true;
            this.geodesicDiameter = true;
            this.tortuosity = true;
            this.maxInscribedDisc = true;
            this.averageThickness = true;
            this.geodesicElongation = true;
            return this;
        }
        
        
        // ====================================================
        // Computation method
        
        /**
         * Computes a set of descriptive features from a label image and
         * concatenates the results into a ResultsTable.
         * @param imagePlus
         *            the image to analyze.
         * @param features
         *            the features to compute.
         * @return the results of the analysis as a Results Table
         */
        public ResultsTable analyze(ImagePlus imagePlus)
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
            if (this.pixelCount)
            {
                IJ.showStatus("Pixel Count");
                results.pixelCounts = LabelImages.pixelCount(image, labels);
            }
            
            // compute intrinsic volumes
            if (this.area || this.perimeter || this.eulerNumber || this.circularity)
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
            
            if (this.boundingBox)
            {
                IJ.showStatus("Bounding boxes");
                BoundingBox algo = new BoundingBox();
                DefaultAlgoListener.monitor(algo);
                results.boundingBoxes = algo.analyzeRegions(image, labels, calib);
            }
            
            if (this.equivalentEllipse || this.ellipseElongation)
            {
                IJ.showStatus("Equivalent Ellipse");
                EquivalentEllipse algo = new EquivalentEllipse();
                DefaultAlgoListener.monitor(algo);
                results.ellipses = algo.analyzeRegions(image, labels, calib);
                
                if (this.centroid)
                {
                    results.centroids = Ellipse.centers(results.ellipses);
                }
            } 
            else if (this.centroid)
            {
                // Compute centroids if not already computed from inertia ellipsoid
                IJ.showStatus("Centroids");
                Centroid algo = new Centroid();
                DefaultAlgoListener.monitor(algo);
                results.centroids = algo.analyzeRegions(image, labels, calib);
            }
            
            if (this.convexity)
            {
                IJ.showStatus("Convexity");
                Convexity algo = new Convexity();
                DefaultAlgoListener.monitor(algo);
                results.convexities = algo.analyzeRegions(image, labels, calib);
            }
            
            if (this.maxFeretDiameter || this.tortuosity)
            {
                IJ.showStatus("Max Feret Diameters");
                results.maxFeretDiams = MaxFeretDiameter.maxFeretDiameters(image, labels,
                        calib);
            }
            
            if (this.orientedBox)
            {
                IJ.showStatus("Oriented Bounding Boxes");
                OrientedBoundingBox2D algo = new OrientedBoundingBox2D();
                DefaultAlgoListener.monitor(algo);
                results.orientedBoxes = algo.analyzeRegions(image, labels, calib);
            }
            
            if (this.geodesicDiameter || this.tortuosity)
            {
                IJ.showStatus("Geodesic diameters");
                GeodesicDiameter algo = new GeodesicDiameter();
                DefaultAlgoListener.monitor(algo);
                results.geodDiams = algo.analyzeRegions(image, labels, calib);
            }
            
            if (this.maxInscribedDisc || this.geodesicElongation)
            {
                IJ.showStatus("Inscribed circles");
                LargestInscribedCircle algo = new LargestInscribedCircle();
                DefaultAlgoListener.monitor(algo);
                results.inscrDiscs = algo.analyzeRegions(image, labels, calib);
            }
            
            if (this.averageThickness)
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
        private ResultsTable populateTable(Results results)
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
            
        
            if (this.pixelCount)
            {
                addColumnToTable(table, "PixelCount", results.pixelCounts);
            }
            
            if (this.area)
            {
                for (int i = 0; i < nLabels; i++)
                {
                    table.setValue("Area", i, results.intrinsicVolumes[i].area);
                }
            }
            
            if (this.perimeter)
            {
                for (int i = 0; i < nLabels; i++)
                {
                    table.setValue("Perimeter", i, results.intrinsicVolumes[i].perimeter);
                }
            }
            
            if (this.circularity)
            {
                double[] circularities = IntrinsicVolumes2DUtils.computeCircularities(results.intrinsicVolumes);
                addColumnToTable(table, "Circularity", circularities);
            }
            
            if (this.eulerNumber)
            {
                for (int i = 0; i < nLabels; i++)
                {
                    table.setValue("EulerNumber", i, results.intrinsicVolumes[i].eulerNumber);
                }
            }
            
            if (this.boundingBox)
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
            
            if (this.centroid)
            {
                for (int i = 0; i < nLabels; i++)
                {
                    Point2D center = results.centroids[i];
                    table.setValue("Centroid.X", i, center.getX());
                    table.setValue("Centroid.Y", i, center.getY());
                }
            }
            
            if (this.equivalentEllipse)
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
            
            if (this.ellipseElongation)
            {
                double[] elong = new double[nLabels];
                for (int i = 0; i < nLabels; i++)
                {
                    Ellipse elli = results.ellipses[i];
                    elong[i] = elli.radius1() / elli.radius2();
                }
                addColumnToTable(table, "Ellipse.Elong", elong);
            }
            
            if (this.convexity)
            {
                for (int i = 0; i < nLabels; i++)
                {
                    table.setValue("ConvexArea", i, results.convexities[i].convexArea);
                    table.setValue("Convexity", i, results.convexities[i].convexity);
                }
            }
            
            if (this.maxFeretDiameter)
            {
                for (int i = 0; i < nLabels; i++)
                {
                    table.setValue("MaxFeretDiam", i, results.maxFeretDiams[i].diameter());
                    table.setValue("MaxFeretDiamAngle", i, Math.toDegrees(results.maxFeretDiams[i].angle()));
                }
            }
            
            if (this.orientedBox)
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
            
            if (this.geodesicDiameter)
            {
                for (int i = 0; i < nLabels; i++)
                {
                    GeodesicDiameter.Result result = results.geodDiams[i];
                    table.setValue("GeodesicDiameter", i, result.diameter);
                }
            }
            
            if (this.tortuosity)
            {
                double[] tortuosity = new double[nLabels];
                for (int i = 0; i < nLabels; i++)
                {
                    tortuosity[i] = results.geodDiams[i].diameter / results.maxFeretDiams[i].diameter();
                }
                addColumnToTable(table, "Tortuosity", tortuosity);
            }
            
            if (this.maxInscribedDisc)
            {
                for (int i = 0; i < nLabels; i++)
                {
                    Point2D center = results.inscrDiscs[i].getCenter();
                    table.setValue("InscrDisc.Center.X", i, center.getX());
                    table.setValue("InscrDisc.Center.Y", i, center.getY());
                    table.setValue("InscrDisc.Radius", i, results.inscrDiscs[i].getRadius());
                }
            }
            
            if (this.averageThickness)
            {
                for (int i = 0; i < nLabels; i++)
                {
                    AverageThickness.Result res = results.avgThickness[i];
                    table.setValue("AverageThickness", i, res.avgThickness);
                }
            }
            
            if (this.geodesicElongation)
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
        
     
    }

//    /**
//     * The list of features to compute.
//     * 
//     * Default initialization is to compute everything except the pixel count.
//     */
//    public static final class FeatureSet
//    {
//        /** The boolean flag for computing pixel count.*/
//        public boolean pixelCount = false;
//        /** The boolean flag for computing area.*/
//        public boolean area = true;
//        /** The boolean flag for computing perimeter.*/
//        public boolean perimeter = true;
//        /** The boolean flag for computing circularity.*/
//        public boolean circularity = true;
//        /** The boolean flag for computing Euler number.*/
//        public boolean eulerNumber = true;
//        /** The boolean flag for computing bounding box.*/
//        public boolean boundingBox = true;
//        /** The boolean flag for computing centroid.*/
//        public boolean centroid = true;
//        /** The boolean flag for computing equivalent ellipse.*/
//        public boolean equivalentEllipse = true;
//        /** The boolean flag for computing ellipse elongation.*/
//        public boolean ellipseElongation = true;
//        /** The boolean flag for computing convexity.*/
//        public boolean convexity = true;
//        /** The boolean flag for computing maximum Feret diameter.*/
//        public boolean maxFeretDiameter = true;
//        /** The boolean flag for computing oriented box.*/
//        public boolean orientedBox = true;
//        /** The boolean flag for computing elongation of oriented box.*/
//        public boolean orientedBoxElongation = true;
//        /** The boolean flag for computing geodesic diameter.*/
//        public boolean geodesicDiameter = true;
//        /** The boolean flag for computing tortuosity.*/
//        public boolean tortuosity = true;
//        /** The boolean flag for computing largest inscribed disc.*/
//        public boolean maxInscribedDisc = true;
//        /** The boolean flag for computing average thickness.*/
//        public boolean averageThickness = true;
//        /** The boolean flag for computing geodesic elongation.*/
//        public boolean geodesicElongation = true;
//        
//        /**
//         * Set the state of all features.
//         * 
//         * @param state
//         *            the state to set.
//         */
//        public void setAll(boolean state)
//        {
//            this.pixelCount = state;
//            this.area = state;
//            this.perimeter = state;
//            this.circularity = state;
//            this.eulerNumber = state;
//            this.boundingBox = state;
//            this.centroid = state;
//            this.equivalentEllipse = state;
//            this.ellipseElongation = state;
//            this.convexity = state;
//            this.maxFeretDiameter = state;
//            this.orientedBox = state;
//            this.orientedBoxElongation = state;
//            this.geodesicDiameter = state;
//            this.tortuosity = state;
//            this.maxInscribedDisc = state;
//            this.averageThickness = state;
//            this.geodesicElongation = state;
//        }
//    }

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
