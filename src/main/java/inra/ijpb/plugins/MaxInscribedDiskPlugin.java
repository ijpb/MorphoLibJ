package inra.ijpb.plugins;


import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import inra.ijpb.measure.GeometricMeasures2D;

public class MaxInscribedDiskPlugin implements PlugIn {

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
    
	@Override
	public void run(String arg0) {
		ImagePlus imagePlus = IJ.getImage();	

		// check if image is a label image
        if(imagePlus.getType() != ImagePlus.GRAY8 && 
        		imagePlus.getType() != ImagePlus.GRAY16 && 
        		imagePlus.getType() != ImagePlus.GRAY32) {
            IJ.showMessage("Input image should be a label image");
            return;
        }
        
        // Execute the plugin
        exec(imagePlus);
	}
	   
    /**
     * Main body of the plugin. 
     */
    public Object[] exec(ImagePlus imagePlus) {
        // Check validity of parameters
        if (imagePlus==null) 
            return null;

        if (debug) {
        	System.out.println("Compute maximum inscribed disk on image '" 
        			+ imagePlus.getTitle());
        }
        
        ImageProcessor proc = imagePlus.getProcessor();
        
        // Extract spatial calibration
        Calibration cal = imagePlus.getCalibration();
        double[] resol = new double[]{1, 1};
        if (cal.scaled()) {
        	resol[0] = cal.pixelWidth;
        	resol[1] = cal.pixelHeight;
        }

        // TODO: convert results in user unit
        ResultsTable results = GeometricMeasures2D.maxInscribedDisk(proc);
        
		// create string for indexing results
		String tableName = imagePlus.getShortTitle() + "-MaxInscribedDisk"; 
    
		// show result
		results.show(tableName);
		
		// return the created array
		return new Object[]{"Morphometry", results};
    }


}
