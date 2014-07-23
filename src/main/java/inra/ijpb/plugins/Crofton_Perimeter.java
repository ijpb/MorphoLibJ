package inra.ijpb.plugins;


import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import inra.ijpb.measure.GeometricMeasures2D;

public class Crofton_Perimeter implements PlugInFilter {

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
        
        // create the dialog, with operator options
        GenericDialog gd = new GenericDialog("Crofton Perimeter");
        gd.addChoice("Number of Directions:", dirNumberLabels, dirNumberLabels[1]);
        gd.showDialog();
        
        // If cancel was clicked, do nothing
        if (gd.wasCanceled())
            return;
        
        // extract number of directions
        int nDirsIndex = gd.getNextChoiceIndex();
        int nDirs = dirNumbers[nDirsIndex];
        
        // check if image is a label image
        if(imagePlus.getType() != ImagePlus.GRAY8 && 
        		imagePlus.getType() != ImagePlus.GRAY16) {
            IJ.showMessage("Input image should be a label image");
            return;
        }
        
        // Execute the plugin
        exec(imagePlus, nDirs);
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
        			+ inputImage.getTitle() + "' using " + nDirs 
        			+ " directions.");
        }
        
        ImageProcessor proc = inputImage.getProcessor();
        
        // Extract spatial calibration
        Calibration cal = inputImage.getCalibration();
        double[] resol = new double[]{1, 1};
        if (cal.scaled()) {
        	resol[0] = cal.pixelWidth;
        	resol[1] = cal.pixelHeight;
        }

        ResultsTable results = GeometricMeasures2D.croftonPerimeter(proc, resol, nDirs);
        
		// create string for indexing results
		String tableName = removeImageExtension(inputImage.getTitle()) + "-Perimeter"; 
    
		// show result
		results.show(tableName);
		
		// return the created array
		return new Object[]{"Crofton Perimeter", results};
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
