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


import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.GeometricMeasures3D;

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
 * @see inra.ijpb.measure.GeometricMeasures3D
 * 
 * @author David Legland
 *
 */
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
        gd.addCheckbox("Inertia Ellipsoid", true);
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
        double[][] ellipsoids = null;
        double[][] elongations = null;
        double[][] inscribedBalls = null;
        
        
        // Identifies labels within image
        int[] labels = LabelImages.findAllLabels(image);

               
        // compute geometrical quantities
        if (computeVolume)
        {
        	volumes = GeometricMeasures3D.volume(image, labels, resol);
        }
        if (computeSurface)
        {
        	surfaces = GeometricMeasures3D.surfaceAreaCrofton(image, labels, resol, surfaceAreaDirs);
        }
        if (computeEulerNumber)
        {
        	eulerNumbers = GeometricMeasures3D.eulerNumber(image, labels, connectivity);
        }
        if (computeSphericity)
        {
        	sphericities = GeometricMeasures3D.computeSphericity(volumes, surfaces);
        }
        
        // compute inertia ellipsoids and their elongations
        if (computeEllipsoid)
        {
        	ellipsoids = GeometricMeasures3D.inertiaEllipsoid(image, labels, resol);
        }
        if (computeElongations)
        {
        	elongations = GeometricMeasures3D.computeEllipsoidElongations(ellipsoids);
        }
        
        // compute position and radius of maximal inscribed ball
        if (computeInscribedBall)
        {
        	inscribedBalls = GeometricMeasures3D.maximumInscribedSphere(image, labels, resol);
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
                table.addValue("Elli.Center.X", ellipsoids[i][0]);
            	table.addValue("Elli.Center.Y", ellipsoids[i][1]);
            	table.addValue("Elli.Center.Z", ellipsoids[i][2]);
            	// add scaling parameters 
                table.addValue("Elli.R1", ellipsoids[i][3]);
            	table.addValue("Elli.R2", ellipsoids[i][4]);
            	table.addValue("Elli.R3", ellipsoids[i][5]);
            	// add orientation info
                table.addValue("Elli.Azim", ellipsoids[i][6]);
            	table.addValue("Elli.Elev", ellipsoids[i][7]);
            	table.addValue("Elli.Roll", ellipsoids[i][8]);
        	}
        	if (computeElongations)
        	{
        		table.addValue("Elli.R1/R2", elongations[i][0]);
        		table.addValue("Elli.R1/R3", elongations[i][1]);
        		table.addValue("Elli.R2/R3", elongations[i][2]);            	
        	}
        	
        	if (computeInscribedBall)
        	{
        		table.addValue("InscrBall.Center.X", inscribedBalls[i][0]);
        		table.addValue("InscrBall.Center.Y", inscribedBalls[i][1]);
        		table.addValue("InscrBall.Center.Z", inscribedBalls[i][2]);
        		table.addValue("InscrBall.Radius", inscribedBalls[i][3]);
        	}
        }
        
        return table;
    }
}
