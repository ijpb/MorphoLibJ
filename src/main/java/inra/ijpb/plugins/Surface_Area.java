package inra.ijpb.plugins;


import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import inra.ijpb.measure.GeometricMeasures3D;

public class Surface_Area implements PlugIn {

    // ====================================================
    // Global Constants
    
    /**
     * List of available numbers of directions
     */
    public final static String[] dirNumberLabels = {
            "3 directions", 
            "13 directions" 
    }; 
    
    /**
     *  Array of weights, in the same order than the array of names.
     */
    public final static int[] dirNumbers = {
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
    
	ImagePlus imagePlus;
	
    
    // ====================================================
    // Calling functions 
   

	/* (non-Javadoc)
     * @see ij.plugin.PlugIn#run(java.lang.String)
     */
    public void run(String args) {
        ImagePlus imagePlus = IJ.getImage();
        
        // create the dialog, with operator options
        GenericDialog gd = new GenericDialog("Surface Area");
        gd.addChoice("Number of Directions:", dirNumberLabels, dirNumberLabels[1]);
        gd.showDialog();
        
        // If cancel was clicked, do nothing
        if (gd.wasCanceled())
            return;
        
        // extract number of directions
        int nDirsIndex = gd.getNextChoiceIndex();
        int nDirs = dirNumbers[nDirsIndex];
        
//        // check if image is a label image
//        if(imagePlus.getType() != ImagePlus.GRAY8 && 
//        		imagePlus.getType() != ImagePlus.GRAY16) {
//            IJ.showMessage("Input image should be a label image");
//            return;
//        }
        
        // Execute the plugin
        exec(imagePlus, nDirs);
    }
    
    /**
     * Main body of the plugin. 
     */
    public Object[] exec(ImagePlus imagePlus, int nDirs) {
        // Check validity of parameters
        if (imagePlus==null) 
            return null;

        ImageStack image = imagePlus.getStack();

        // Extract spatial calibration
        Calibration cal = imagePlus.getCalibration();
        double[] resol = new double[]{1, 1, 1};
        if (cal.scaled()) {
        	resol[0] = cal.pixelWidth;
        	resol[1] = cal.pixelHeight;
        	resol[2] = cal.pixelDepth;
        }
        
        ResultsTable results = GeometricMeasures3D.surfaceArea(image, resol, nDirs);
        
		// create string for indexing results
		String tableName = imagePlus.getShortTitle() + "-surface"; 
    
		// show result
		results.show(tableName);
		
		// return the created array
		return new Object[]{"Surface area", results};
    }
}
