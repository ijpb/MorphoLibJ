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
import inra.ijpb.measure.IntrinsicVolumes3D;

/**
 * Plugin for measuring the surface area of the interface between two labels
 * within a 3D label image.
 * 
 * @see inra.ijpb.plugins.AnalyzeRegions3D
 * @see inra.ijpb.measure.IntrinsicVolumes3D
 * 
 * @author David Legland
 *
 */
public class InterfaceSurfaceArea3D implements PlugIn
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
	
    String surfaceAreaMethod = surfaceAreaMethods[1];
    int nDirs = 13;

    
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
        GenericDialog gd = new GenericDialog("Interface Surface Area");
        gd.addNumericField("Label 1", 1, 0);
        gd.addNumericField("Label 2", 2, 0);
        gd.addMessage("");
        gd.addChoice("Surface_area_method:", surfaceAreaMethods, surfaceAreaMethods[1]);
        gd.showDialog();
        
        // If cancel was clicked, do nothing
        if (gd.wasCanceled())
            return;

        // extract analysis options
        int label1 = (int) gd.getNextNumber();
        int label2 = (int) gd.getNextNumber();
        nDirs = dirNumbers[gd.getNextChoiceIndex()];
        
        // Extract Image Stack and its calibration
        ImageStack image = imagePlus.getStack();
        Calibration calib = imagePlus.getCalibration();

        double S12 = IntrinsicVolumes3D.interfaceSurfaceArea(image, label1, label2, calib, nDirs);
        
        // Execute the plugin
        ResultsTable table = new ResultsTable();
        table.incrementCounter();
        table.addValue("Label1", label1);
        table.addValue("Label2", label2);
        table.addValue("Interf.Surf", S12);
        
 		// create string for indexing results
		String tableName = imagePlus.getShortTitle() + "-Interface"; 
    
		// show result
		table.show(tableName);
    }
}
