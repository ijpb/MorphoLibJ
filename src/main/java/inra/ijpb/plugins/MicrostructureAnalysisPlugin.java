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
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import inra.ijpb.measure.IntrinsicVolumes2D;

/**
 * Plugin for quantifying microstructures from binary or label images. 
 */
public class MicrostructureAnalysisPlugin implements PlugInFilter {

    // ====================================================
    // Global Constants
    
    /**
     * List of available numbers of directions
     */
    public final static String[] dirNumberLabels = {
            "Crofton (2 dirs.)", 
            "Crofton (4 dirs.)" 
    }; 
    
    /**
     *  Array of weights, in the same order than the array of names.
     */
    public final static int[] dirNumbers = {
        2, 4
    };
    
    // ====================================================
    // Class variables
    
   /**
     * When this options is set to true, information messages are displayed on
     * the console, and the number of counts for each direction is included in
     * results table. 
     */
    public boolean debug  = false;
    
	ImagePlus imagePlus;
	
    
    // ====================================================
    // Calling functions 
    
   	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	@Override
	public int setup(String arg, ImagePlus imp) {
		if (imp == null) {
			IJ.noImage();
			return DONE;
		}
		
		this.imagePlus = imp;
		return DOES_ALL | NO_CHANGES;
	}
	
    /* (non-Javadoc)
     * @see ij.plugin.PlugIn#run(java.lang.String)
     */
    @Override
    public void run(ImageProcessor ip) {
        // create the dialog, here an image input and a boolean option
        GenericDialog gd = new GenericDialog("Binary Microstructure");
        gd.addChoice("Perimeter method:", dirNumberLabels, dirNumberLabels[1]);
        gd.addCheckbox("Add_Porosity", false);
        gd.showDialog();
        
        // If cancel was clicked, do nothing
        if (gd.wasCanceled())
            return;
        
        // extract number of directions
        int nDirsIndex = gd.getNextChoiceIndex();
        int nDirs = dirNumbers[nDirsIndex];
        boolean addPorosity = gd.getNextBoolean(); 
        		
        // check if image is a label image
        if(imagePlus.getType() != ImagePlus.GRAY8) {
            IJ.showMessage("Input image should be a binary image");
            return;
        }
        
        // Execute the plugin
        ResultsTable table = process(imagePlus, nDirs, addPorosity);
     
        // Display the results table
        String tableName = removeImageExtension(imagePlus.getShortTitle()) + "-Microstructure"; 
        table.show(tableName);
    }
  
    /**
     * Old interface for calling the plugin, kept for compatibility.
	 * 
	 * @param image
	 *            the image to process
	 * @param nDirs
	 *            the number of directions to consider, either 2 or 4
	 * @return an array of objects
     * @deprecated specify porosity
     */
    @Deprecated
    public Object[] exec(ImagePlus image, int nDirs) {
    	return exec(image, nDirs, false);
    }
    
    /**
	 * Main body of the plugin. Computes geometric measures on the image
	 * contained in <code>image</code>, using <code>nDirs</code> discrete
	 * directions. If the addPorosity flag is set to true, an additional column
	 * equal to 1-area density is added.
	 * 
	 * @param image
	 *            the image to process
	 * @param nDirs
	 *            the number of directions to consider, either 2 or 4
	 * @param addPorosity
 	 *            specifies if porosity should be computed
	 * @return an array of objects
	 * @deprecated use process method instead
	 */
    @Deprecated
    public Object[] exec(ImagePlus image, int nDirs, boolean addPorosity) {
        // Check validity of parameters
        if (image==null) 
            return null;

        if (debug) {
        	System.out.println("Compute Crofton densities on image '" 
        			+ image.getTitle() + "' using " + nDirs 
        			+ " directions.");
        }
        
        ImageProcessor proc = image.getProcessor();
        
        // Extract spatial calibration
        Calibration calib = image.getCalibration();
        
        // Compute basis measures
        double areaDensity =  IntrinsicVolumes2D.areaDensity(proc);
        double perimDensity = IntrinsicVolumes2D.perimeterDensity(proc, calib, nDirs);
        ResultsTable table = new ResultsTable();
        table.incrementCounter();
        table.addValue("AreaDensity", areaDensity);
        table.addValue("PerimeterDensity", perimDensity);
        
        // eventually add the porosity for those who do not want to subtract by hand...
        if (addPorosity)
        {
       	 table.addValue("Porosity", 1-areaDensity);
        }

		// create string for indexing results
		String tableName = removeImageExtension(image.getTitle()) + "-Densities"; 
    
		// show result
		table.show(tableName);
		
		// return the created array
		return new Object[]{"Crofton Densties", table};
    }

    /**
 	 * Main body of the plugin. Computes geometric measures on the image
 	 * contained in <code>image</code>, using <code>nDirs</code> discrete
 	 * directions. If the addPorosity flag is set to true, an additional column
 	 * equal to 1-area density is added.
 	 * 
 	 * @param image
 	 *            the image to process
 	 * @param nDirs
 	 *            the number of directions to consider, either 2 or 4
 	 * @param addPorosity
 	 *            specifies if porosity should be computed
 	 * @return a new ResultsTable containing microstructure characterization of binary image
 	 */
     public ResultsTable process(ImagePlus image, int nDirs, boolean addPorosity)
     {
         // Check validity of parameters
         if (image==null) 
             return null;

         if (debug) 
         {
         	System.out.println("Compute Crofton densities on image '" 
         			+ image.getTitle() + "' using " + nDirs 
         			+ " directions.");
         }
         
         ImageProcessor proc = image.getProcessor();
         
         // Extract spatial calibration
         Calibration calib = image.getCalibration();
         
         // Compute basis measures
         double areaDensity =  IntrinsicVolumes2D.areaDensity(proc);
         double perimDensity = IntrinsicVolumes2D.perimeterDensity(proc, calib, nDirs);
         ResultsTable table = new ResultsTable();
         table.incrementCounter();
         table.addValue("AreaDensity", areaDensity);
         table.addValue("PerimeterDensity", perimDensity);
         
         // eventually add the porosity for those who do not want to subtract by hand...
         if (addPorosity)
         {
        	 table.addValue("Porosity", 1-areaDensity);
         }
 	
         // return the created table
         return table;
     }

     /**
      * Remove the extension of the filename if it belongs to a set of known
      * image formats.
      */
     private static String removeImageExtension(String name) {
        if (name.endsWith(".tif"))
            name = name.substring(0, name.length()-4);
        if (name.endsWith(".png"))
            name = name.substring(0, name.length()-4);
        if (name.endsWith(".bmp"))
            name = name.substring(0, name.length()-4);
        if (name.endsWith(".mhd"))
            name = name.substring(0, name.length()-4);
        return name;
    }    
}
