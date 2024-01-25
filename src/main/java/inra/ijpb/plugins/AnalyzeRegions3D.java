/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2023 INRA.
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


import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.measure.region3d.MorphometricFeatures3D;
import inra.ijpb.measure.region3d.MorphometricFeatures3D.Feature;

/**
 * Plugin for measuring various geometric features from 3 regions. Features can
 * be volume, surface area, centroid, equivalent ellipsoid, bounding box...
 * 
 * Volume is obtained by counting the number of voxels. Surface area is computed
 * using discretization of Crofton formula. Sphericity is obtained as the ratio
 * of V^2 by S^3, multiplied by 36*pi.
 * 
 * If the input image is calibrated, the spatial resolution is taken into
 * account for computing geometric features.
 * 
 *  Most of the computation part is performed by the <code>MorphometricFeatures3D</code> class.
 *  
 * @see inra.ijpb.measure.region3d.MorphometricFeatures3D
 * @see inra.ijpb.measure.IntrinsicVolumes3D
 * 
 * @author David Legland
 *
 */
public class AnalyzeRegions3D implements PlugIn
{
    // ====================================================
    // Global Constants
    
    /**
     * List of available numbers of directions
     */
    private final static String[] surfaceAreaMethods = {
            "Crofton  (3 dirs.)", 
            "Crofton (13 dirs.)" 
    }; 
    
    /**
     *  Array of weights, in the same order than the array of names.
     */
    private final static int[] dirNumbers = {
        3, 13
    };
    
    
    // ====================================================
    // Class variables
       
    /**
     * The instance of Morphometry2D that will compute the features. As it is
     * static, it will keep chosen features when plugin is run again.
     */
    static MorphometricFeatures3D features = null;
    
    // 
    String surfaceAreaMethod = surfaceAreaMethods[1];
    int surfaceAreaDirs = 13;
    int meanBreadthDirs = 13;
    Connectivity3D connectivity = Connectivity3D.C6;
    
    
    // ====================================================
    // Calling functions 
    
	/* (non-Javadoc)
     * @see ij.plugin.PlugIn#run(java.lang.String)
     */
    public void run(String args) 
    {
        ImagePlus imagePlus = IJ.getImage();
        
		if (imagePlus.getStackSize() == 1) 
		{
			IJ.error("Requires a Stack");
			return;
		}
		
        // initialize MorphometricFeatures3D instance if necessary
        if (AnalyzeRegions3D.features == null)
        {
            AnalyzeRegions3D.features = new MorphometricFeatures3D(
                    Feature.VOLUME, Feature.SURFACE_AREA, 
                    Feature.CENTROID);
        }
		
        // opens a dialog to allow user choosing features (and options)
		MorphometricFeatures3D features = chooseFeatures(AnalyzeRegions3D.features);
		    
        // If cancel was clicked, features is null
        if (features == null)
        {
            return;
        }
        
        // Call the main processing method
        DefaultAlgoListener.monitor(features);
        ResultsTable table = features.computeTable(imagePlus);

 		// create string for indexing results
		String tableName = imagePlus.getShortTitle() + "-morpho"; 
    
		// show result
		table.show(tableName);
        
        // keep choices for next plugin call
        AnalyzeRegions3D.features = features;
    }
    
    private static final MorphometricFeatures3D chooseFeatures(MorphometricFeatures3D initialChoice)
    {
        if (initialChoice == null)
        {
            initialChoice = new MorphometricFeatures3D();
        }
        
        GenericDialog gd = new GenericDialog("Analyze Regions");
        gd.addCheckbox("Voxel_Count", initialChoice.contains(Feature.VOXEL_COUNT));
        gd.addCheckbox("Volume", initialChoice.contains(Feature.VOLUME));
        gd.addCheckbox("Surface_Area", initialChoice.contains(Feature.SURFACE_AREA));
        gd.addCheckbox("Mean_Breadth", initialChoice.contains(Feature.MEAN_BREADTH));
        gd.addCheckbox("Sphericity", initialChoice.contains(Feature.SPHERICITY));
        gd.addCheckbox("Euler_Number", initialChoice.contains(Feature.EULER_NUMBER));
        gd.addCheckbox("Bounding_Box", initialChoice.contains(Feature.BOUNDING_BOX));
        gd.addCheckbox("Centroid", initialChoice.contains(Feature.CENTROID));
        gd.addCheckbox("Equivalent_Ellipsoid", initialChoice.contains(Feature.EQUIVALENT_ELLIPSOID));
        gd.addCheckbox("Ellipsoid_Elongations", initialChoice.contains(Feature.ELLIPSOID_ELONGATION));
        gd.addCheckbox("Max._Inscribed Ball", initialChoice.contains(Feature.MAX_INSCRIBED_BALL));
        gd.addMessage("");
        gd.addChoice("Surface_area_method:", surfaceAreaMethods, surfaceAreaMethods[1]);
        gd.addChoice("Euler_Connectivity:", Connectivity3D.getAllLabels(), Connectivity3D.C6.name());
        gd.showDialog();
        
        // If cancel was clicked, do nothing
        if (gd.wasCanceled())
        {
            return null;
        }
    
        // Extract features to quantify from image
        MorphometricFeatures3D features = new MorphometricFeatures3D();
        if (gd.getNextBoolean()) features.add(Feature.VOXEL_COUNT);
        if (gd.getNextBoolean()) features.add(Feature.VOLUME);
        if (gd.getNextBoolean()) features.add(Feature.SURFACE_AREA);
        if (gd.getNextBoolean()) features.add(Feature.MEAN_BREADTH);
        if (gd.getNextBoolean()) features.add(Feature.SPHERICITY);
        if (gd.getNextBoolean()) features.add(Feature.EULER_NUMBER);
        if (gd.getNextBoolean()) features.add(Feature.BOUNDING_BOX);
        if (gd.getNextBoolean()) features.add(Feature.CENTROID);
        if (gd.getNextBoolean()) features.add(Feature.EQUIVALENT_ELLIPSOID);
        if (gd.getNextBoolean()) features.add(Feature.ELLIPSOID_ELONGATION);
        if (gd.getNextBoolean()) features.add(Feature.MAX_INSCRIBED_BALL);
        
        // setup options
        features.setDirectionNumber(dirNumbers[gd.getNextChoiceIndex()]);
        features.setConnectivity(Connectivity3D.fromLabel(gd.getNextChoice()).getValue());
        
        return features;
    }
}
