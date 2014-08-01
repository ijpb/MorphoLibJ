package inra.ijpb.plugins;


import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import inra.ijpb.measure.GeometricMeasures2D;

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
        if(imagePlus.getType() != ImagePlus.GRAY8 && 
        		imagePlus.getType() != ImagePlus.GRAY16 && 
        		imagePlus.getType() != ImagePlus.GRAY32) {
            IJ.showMessage("Input image should be a label image");
            return;
        }
        
        // Execute the plugin
        exec(imagePlus, 4);
    }
    
    /**
     * Main body of the plugin. 
     */
    public Object[] exec(ImagePlus inputImage, int nDirs) {
        // Check validity of parameters
        if (inputImage==null) 
            return null;

        if (debug) {
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
