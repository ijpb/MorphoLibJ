package inra.ijpb.plugins;


import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import inra.ijpb.measure.GeometricMeasures2D;

public class MicrostructureAnalysisPlugin implements PlugInFilter {

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
    @Override
    public void run(ImageProcessor ip) {
        // create the dialog, here an image input and a boolean option
        GenericDialog gd = new GenericDialog("Crofton Densities");
        gd.addChoice("Number of Directions:", dirNumberLabels, dirNumberLabels[1]);
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
        exec(imagePlus, nDirs, addPorosity);
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
	 *            specifiy if porpsoity should be added
	 * @return an array of objects
	 */
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
        Calibration cal = image.getCalibration();
        double[] resol = new double[]{1, 1};
        if (cal.scaled()) {
        	resol[0] = cal.pixelWidth;
        	resol[1] = cal.pixelHeight;
        }
        
        // Compute basis measures
        ResultsTable results = GeometricMeasures2D.perimeterDensity(proc, resol, nDirs);
        
        // eventually add the porosity for those who do not want to subtract by hand...
        if (addPorosity) {
        	int nRows = results.getColumn(0).length;
        	for (int i = 0; i < nRows; i++) {
        		double areaDensity = results.getValue("A. Density", i);
        		results.setValue("Porosity", i, 1-areaDensity);
        	}
        }
		// create string for indexing results
		String tableName = removeImageExtension(image.getTitle()) + "-Densities"; 
    
		// show result
		results.show(tableName);
		
		// return the created array
		return new Object[]{"Crofton Densties", results};
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
