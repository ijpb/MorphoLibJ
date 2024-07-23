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


import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import inra.ijpb.geometry.Ellipsoid;
import inra.ijpb.geometry.Point3D;
import inra.ijpb.geometry.Sphere;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.IntrinsicVolumes3D;
import inra.ijpb.measure.region3d.EquivalentEllipsoid;
import inra.ijpb.measure.region3d.LargestInscribedBall;

/**
 * Plugin for measuring geometric quantities such as volume, surface area,
 * and eventually sphericity index.
 * 
 * Volume is obtained by counting the number of voxels. Surface area is
 * computed using discretization of Crofton formula. Sphericity is obtained as
 * the ratio of V^2 by S^3, multiplied by 36*pi.
 * 
 * If the input image is calibrated, the spatial resolution is taken into 
 * account for computing geometric features.
 * 
 * @deprecated replaced by AnalyzeRegions3D
 * 
 * @see inra.ijpb.measure.IntrinsicVolumes3D
 * 
 * @author David Legland
 *
 */
@Deprecated
public class ParticleAnalysis3DPlugin implements PlugIn
{
    // ====================================================
    // Global Constants
    
	/**
	 * The list of connectivity names.
	 */
	private final static String[] connectivityNames = {
		"C6", "C26"
	};
	
	private final static int[] connectivityValues = new int[]{6, 26};
	
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
     * When this options is set to true, information messages are displayed on
     * the console, and the number of counts for each direction is included in
     * results table. 
     */
    public boolean debug  = false;
    
	boolean computeVolume 		= true;
	boolean computeSurface 		= true;
	boolean computeEulerNumber	= true;
	boolean computeSphericity 	= true;
	boolean computeEllipsoid 	= true;
	boolean computeElongations 	= true;
	boolean computeInscribedBall = true;
	
    String surfaceAreaMethod = surfaceAreaMethods[1];
    int surfaceAreaDirs = 3;
    int connectivity = 6;
    
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
		
        // create the dialog, with operator options
        GenericDialog gd = new GenericDialog("Particles Analysis 3D");
        gd.addCheckbox("Volume", true);
        gd.addCheckbox("Surface Area", true);
        gd.addCheckbox("Sphericity", true);
        gd.addCheckbox("Euler Number", true);
        gd.addCheckbox("Equivalent Ellipsoid", true);
        gd.addCheckbox("Ellipsoid Elongation", true);
        gd.addCheckbox("Max. Inscribed Ball", true);
        gd.addMessage("");
        gd.addChoice("Surface area method:", surfaceAreaMethods, surfaceAreaMethods[1]);
        gd.addChoice("Euler Connectivity:", connectivityNames, connectivityNames[1]);
        gd.showDialog();
        
        // If cancel was clicked, do nothing
        if (gd.wasCanceled())
            return;

        // Extract features to extract from image
        computeVolume 		= gd.getNextBoolean();
        computeSurface 		= gd.getNextBoolean();
        computeSphericity 	= gd.getNextBoolean() & computeVolume & computeSurface;
        computeEulerNumber	= gd.getNextBoolean();
        computeEllipsoid 	= gd.getNextBoolean();
        computeElongations 	= gd.getNextBoolean() & computeEllipsoid;
        computeInscribedBall = gd.getNextBoolean();
        
        
        // extract analysis options
        surfaceAreaDirs = dirNumbers[gd.getNextChoiceIndex()];
        connectivity = connectivityValues[gd.getNextChoiceIndex()];
        
        // Execute the plugin
        ResultsTable table = process(imagePlus);
        
 		// create string for indexing results
		String tableName = imagePlus.getShortTitle() + "-morpho"; 
    
		// show result
		table.show(tableName);
    }
    
    /**
     * Computes features from an ImagePlus object. 
     * Spatial resolution is read from image Calibration.
     * 
     * @param imagePlus the label image to analyze
     * @return the results in a new ResultsTable
     */
    public ResultsTable process(ImagePlus imagePlus)
    {
    	// Check validity of parameters
        if (imagePlus==null) 
            return null;

        // Extract Image Stack and its calibration
        ImageStack image = imagePlus.getStack();
        Calibration calib = imagePlus.getCalibration();
        
        return process(image, calib);
    }
    
    /**
	 * Computes features from an ImageStack object, specifying the calibration.
	 * 
	 * @param image
	 *            the 3D label image to analyze
	 * @param calib
	 *            the spatial calibration of the image
	 * @return the results in a new ResultsTable
	 */
    public ResultsTable process(ImageStack image, Calibration calib)
    {
    	// Extract spatial calibration
        double[] resol = new double[]{1, 1, 1};
        if (calib != null && calib.scaled()) 
        {
        	resol[0] = calib.pixelWidth;
        	resol[1] = calib.pixelHeight;
        	resol[2] = calib.pixelDepth;
        }

        // declare arrays for results
        double[] volumes = null;
        double[] surfaces = null;
        double[] eulerNumbers = null;
        double[] sphericities = null;
        Ellipsoid[] ellipsoids = null;
        double[][] elongations = null;
        Sphere[] inscribedBalls = null;
        
        
        // Identifies labels within image
        int[] labels = LabelImages.findAllLabels(image);

               
        // compute geometrical quantities
        if (computeVolume)
        {
        	volumes = IntrinsicVolumes3D.volumes(image, labels, calib);
        }
        if (computeSurface)
        {
        	surfaces = IntrinsicVolumes3D.surfaceAreas(image, labels, calib, surfaceAreaDirs);
        }
        if (computeEulerNumber)
        {
        	eulerNumbers = IntrinsicVolumes3D.eulerNumbers(image, labels, connectivity);
        }
        if (computeSphericity)
        {
        	sphericities = IntrinsicVolumes3D.sphericity(volumes, surfaces);
        }
        
        // compute equivalent ellipsoids and their elongations
        if (computeEllipsoid)
        {
        	ellipsoids = EquivalentEllipsoid.equivalentEllipsoids(image, labels, calib);
        }
        if (computeElongations)
        {
        	elongations = Ellipsoid.elongations(ellipsoids);
        }
        
        // compute position and radius of maximal inscribed ball
        if (computeInscribedBall)
        {
        	inscribedBalls = LargestInscribedBall.largestInscribedBalls(image, labels, calib);
        }
        
        
        // Convert to ResultsTable object
        ResultsTable table = new ResultsTable();
        for (int i = 0; i < labels.length; i++) 
        {
        	table.incrementCounter();
        	table.addLabel(Integer.toString(labels[i]));
        	
        	// geometrical quantities
        	if (computeVolume)
        		table.addValue("Volume", volumes[i]);
        	if (computeSurface)
        		table.addValue("SurfaceArea", surfaces[i]);
        	if (computeSphericity)
        		table.addValue("Sphericity", sphericities[i]);
        	if (computeEulerNumber)
        		table.addValue("EulerNumber", eulerNumbers[i]);

        	// inertia ellipsoids
        	if (computeEllipsoid)
        	{
                // add coordinates of origin pixel (IJ coordinate system) 
        		Ellipsoid elli = ellipsoids[i];
        		Point3D center = elli.center();
                table.addValue("Elli.Center.X", center.getX());
            	table.addValue("Elli.Center.Y", center.getY());
            	table.addValue("Elli.Center.Z", center.getZ());
            	// add scaling parameters 
                table.addValue("Elli.R1", elli.radius1());
            	table.addValue("Elli.R2", elli.radius2());
            	table.addValue("Elli.R3", elli.radius3());
            	// add orientation info
                table.addValue("Elli.Azim", elli.phi());
            	table.addValue("Elli.Elev", elli.theta());
            	table.addValue("Elli.Roll", elli.psi());
        	}
        	if (computeElongations)
        	{
        		table.addValue("Elli.R1/R2", elongations[i][0]);
        		table.addValue("Elli.R1/R3", elongations[i][1]);
        		table.addValue("Elli.R2/R3", elongations[i][2]);            	
        	}
        	
        	if (computeInscribedBall)
        	{
        		Sphere ball = inscribedBalls[i];
        		Point3D center = ball.center();
                
        		table.addValue("InscrBall.Center.X", center.getX());
        		table.addValue("InscrBall.Center.Y", center.getY());
        		table.addValue("InscrBall.Center.Z", center.getZ());
        		table.addValue("InscrBall.Radius", ball.radius());
        	}
        }
        
        return table;
    }
}
