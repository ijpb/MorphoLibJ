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
package inra.ijpb.plugins;


import java.util.ArrayList;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.region2d.MorphometricFeatures2D;
import inra.ijpb.measure.region2d.MorphometricFeatures2D.Feature;

/**
 * Plugin for computing morphological feature of regions from label images.
 * 
 * For programmatic use, it is advised to use the
 * <code>MorphometricFeatures2D</code> class:
 * <pre>{@code
 * ResultsTable table = new MorphometricFeatures2D()
 *     .add(Feature.AREA)
 *     .add(Feature.PERIMETER)
 *     .add(Feature.CENTROID)
 *     .computeTable(imagePlus);
 * }
 *  </pre>
 * 
 * see inra.ijpb.measure.region2d.MorphometricFeatures2D
 */
public class AnalyzeRegions implements PlugInFilter 
{
    // ====================================================
    // Static methods
    
	/**
     * Computes a set of descriptive features from a label image and
     * concatenates the results into a ResultsTable.
     * 
     * @deprecated replaced by MorphometricFeatures2D
     * 
     * @param imagePlus
     *            the image to analyze.
     * @param features
     *            the features to compute.
     * @return the results of the analysis as a Results Table
     */
    @Deprecated
    public static final ResultsTable process(ImagePlus imagePlus, Features features)
    {
        // convert inner Features class into a MorphometricFeatures2D instance
        MorphometricFeatures2D morpho = new MorphometricFeatures2D();
        if (features.pixelCount) morpho.add(Feature.PIXEL_COUNT);
        if (features.area) morpho.add(Feature.AREA);
        if (features.perimeter) morpho.add(Feature.PERIMETER);
        if (features.circularity) morpho.add(Feature.CIRCULARITY);
        if (features.eulerNumber) morpho.add(Feature.EULER_NUMBER);
        if (features.boundingBox) morpho.add(Feature.BOUNDING_BOX);
        if (features.centroid) morpho.add(Feature.CENTROID);
        if (features.equivalentEllipse) morpho.add(Feature.EQUIVALENT_ELLIPSE);
        if (features.ellipseElongation) morpho.add(Feature.ELLIPSE_ELONGATION);
        if (features.convexity) morpho.add(Feature.CONVEXITY);
        if (features.maxFeretDiameter) morpho.add(Feature.MAX_FERET_DIAMETER);
        if (features.orientedBox) morpho.add(Feature.ORIENTED_BOX);
        if (features.orientedBoxElongation) morpho.add(Feature.ORIENTED_BOX_ELONGATION);
        if (features.geodesicDiameter) morpho.add(Feature.GEODESIC_DIAMETER);
        if (features.tortuosity) morpho.add(Feature.TORTUOSITY);
        if (features.maxInscribedDisc) morpho.add(Feature.MAX_INSCRIBED_DISK);
        if (features.averageThickness) morpho.add(Feature.AVERAGE_THICKNESS);
        if (features.geodesicElongation) morpho.add(Feature.GEODESIC_ELONGATION);
        
        return morpho.computeTable(imagePlus);
    }
    
    
	// ====================================================
	// Class variables

	static final List<Feature> featureList;
	static
	{
		// initializes the list of features that will populate the GUI,
		// in the order they will be displayed
		featureList = new ArrayList<>();
		featureList.add(Feature.PIXEL_COUNT);
		featureList.add(null);
		featureList.add(Feature.AREA);
		featureList.add(Feature.PERIMETER);
		featureList.add(Feature.CIRCULARITY);
		featureList.add(Feature.EULER_NUMBER);
		featureList.add(Feature.BOUNDING_BOX);
		featureList.add(Feature.CENTROID);
		featureList.add(Feature.EQUIVALENT_ELLIPSE);
		featureList.add(Feature.ELLIPSE_ELONGATION);
		featureList.add(Feature.CONVEXITY);
		featureList.add(Feature.MAX_FERET_DIAMETER);
		featureList.add(Feature.ORIENTED_BOX);
		featureList.add(Feature.ORIENTED_BOX_ELONGATION);
		featureList.add(Feature.GEODESIC_DIAMETER);
		featureList.add(Feature.TORTUOSITY);
		featureList.add(Feature.MAX_INSCRIBED_DISK);
		featureList.add(Feature.GEODESIC_ELONGATION);
		featureList.add(Feature.AVERAGE_THICKNESS);
	}

	/**
	 * The instance of Morphometry2D that will compute the features. As it is
	 * static, it will keep chosen features when plugin is run again.
	 */
	static MorphometricFeatures2D features = null;

	/**
	 * The image to work on.
	 */
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
        
        // initialize MorphometricFeatures2D instance if necessary
        if (AnalyzeRegions.features == null)
        {
            AnalyzeRegions.features = new MorphometricFeatures2D(
                    Feature.AREA, Feature.PERIMETER, Feature.EULER_NUMBER, Feature.CIRCULARITY,
                    Feature.BOUNDING_BOX, 
                    Feature.CENTROID, Feature.EQUIVALENT_ELLIPSE, Feature.ELLIPSE_ELONGATION, 
                    Feature.CONVEXITY, Feature.MAX_FERET_DIAMETER, 
                    Feature.ORIENTED_BOX, Feature.ORIENTED_BOX_ELONGATION, 
                    Feature.GEODESIC_DIAMETER, Feature.TORTUOSITY, 
                    Feature.AVERAGE_THICKNESS, 
                    Feature.MAX_INSCRIBED_DISK, Feature.GEODESIC_ELONGATION 
                    );
        }
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
		MorphometricFeatures2D morphoFeatures = chooseFeatures(AnalyzeRegions.features);
        // If cancel was clicked, features is null
        if (morphoFeatures == null)
        {
            return;
        }
        
        // Call the main processing method
        DefaultAlgoListener.monitor(morphoFeatures);
        ResultsTable table = morphoFeatures.computeTable(imagePlus);
        
        // show result
        String tableName = imagePlus.getShortTitle() + "-Morphometry";
        table.show(tableName);
        
        // keep choices for next plugin call
        AnalyzeRegions.features = morphoFeatures;
    }
    
    private static final MorphometricFeatures2D chooseFeatures(MorphometricFeatures2D initialChoice)
	{
		if (initialChoice == null)
		{
			initialChoice = new MorphometricFeatures2D();
		}

		// initialize the data for populating GUI
		int nf = featureList.size();
		String[] labels = new String[nf];
		boolean[] states = new boolean[nf];
		for (int i = 0; i < featureList.size(); i++)
		{
			Feature feature = featureList.get(i);
			labels[i] = feature != null ? feature.toString() : null;
			states[i] = initialChoice.contains(featureList.get(i));
		}

		// create dialog for choosing features
		GenericDialog gd = new GenericDialog("Analyze Regions");
		gd.addCheckboxGroup(labels.length / 2 + 1, 2, labels, states, new String[] { "Features" });
		gd.showDialog();

		// If cancel was clicked, return null to avoid computation
		if (gd.wasCanceled())
		{ return null; }

		// Extract features to quantify from image
		MorphometricFeatures2D features = new MorphometricFeatures2D();
		for (Feature feature : featureList)
		{
			if (feature != null)
			{
				if (gd.getNextBoolean()) features.add(feature);
			}
		}

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
     * 
     * @deprecated replaced by MorphometricFeatures2D
     */
    @Deprecated
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
}
