package inra.ijpb.plugins;


import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import inra.ijpb.measure.GeometricMeasures2D;

public class InertiaEllipsePlugin implements PlugInFilter {

    // ====================================================
    // Global Constants
    
    
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
        		imagePlus.getType() != ImagePlus.GRAY16) {
            IJ.showMessage("Input image should be a label image");
            return;
        }
        
        // Execute the plugin
        ResultsTable results = process(imagePlus);
        
		// create string for indexing results
		String tableName = imagePlus.getShortTitle() + "-Ellipses"; 
    
		// show result
		results.show(tableName);
    }
    
    /**
     * Main body of the plugin. 
     * 
     * @param inputImage the image to analyze
     * @return the ResutlsTable describing each label
     */
    public ResultsTable process(ImagePlus inputImage) {
        // Check validity of parameters
        if (inputImage==null) 
            return null;

        if (debug) {
        	System.out.println("Compute Inertia ellipses on image '" 
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

        ResultsTable results = GeometricMeasures2D.inertiaEllipse(proc);
        
		// return the created array
		return results;
    }

}
