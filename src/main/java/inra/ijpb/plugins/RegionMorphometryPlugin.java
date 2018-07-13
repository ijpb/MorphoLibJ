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
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.GeometricMeasures2D;

@Deprecated
public class RegionMorphometryPlugin implements PlugInFilter {

    // ====================================================
    // Global Constants
    
    /**
     * List of available numbers of directions
     */
    public final static String[] dirNumberLabels = {
            "2 directions", 
            "4 directions" 
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
    public void run(ImageProcessor ip) {
        
        // check if image is a label image
		// Check if image may be a label image
		if (!LabelImages.isLabelImageType(imagePlus))
		{
           IJ.showMessage("Input image should be a label image");
            return;
        }
        
        // Execute the plugin
        ResultsTable table = process(imagePlus, 4);
        
        // show result
		String tableName = imagePlus.getShortTitle() + "-Morphometry"; 
		table.show(tableName);
    }
    
    public ResultsTable process(ImagePlus inputImage, int nDirs) 
    {
        // Check validity of parameters
        if (inputImage==null) 
            return null;

        if (debug) 
        {
        	System.out.println("Compute Crofton perimeter on image '" 
        			+ inputImage.getTitle());
        }
        
        ImageProcessor proc = inputImage.getProcessor();
        
        // Extract spatial calibration
        Calibration cal = inputImage.getCalibration();
        double[] resol = new double[]{1, 1};
        if (cal.scaled()) 
        {
        	resol[0] = cal.pixelWidth;
        	resol[1] = cal.pixelHeight;
        }

        ResultsTable results = GeometricMeasures2D.analyzeRegions(proc, resol);

		// return the created array
		return results;
    }
    
    @Deprecated
    public Object[] exec(ImagePlus inputImage, int nDirs) {
        // Check validity of parameters
        if (inputImage==null) 
            return null;

        if (debug) 
        {
        	System.out.println("Compute Crofton perimeter on image '" 
        			+ inputImage.getTitle());
        }
        
        ImageProcessor proc = inputImage.getProcessor();
        
        // Extract spatial calibration
        Calibration cal = inputImage.getCalibration();
        double[] resol = new double[]{1, 1};
        if (cal.scaled()) {
        	resol[0] = cal.pixelWidth;
        	resol[1] = cal.pixelHeight;
        }

        ResultsTable results = GeometricMeasures2D.analyzeRegions(proc, resol);
        
		// create string for indexing results
		String tableName = inputImage.getShortTitle() + "-Morphometry"; 
    
		// show result
		results.show(tableName);
		
		// return the created array
		return new Object[]{"Morphometry", results};
    }
}
