package inra.ijpb.plugins;


import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import inra.ijpb.measure.GeometricMeasures3D;
import inra.ijpb.morphology.LabelImages;

/**
 * Plugin for measuring geometric quantities such as volume, surface area 
 * @author David Legland
 *
 */
public class Geometric_Measures_3D implements PlugIn {

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
        
		if (imagePlus.getStackSize() == 1) {
			IJ.error("Requires a Stack");
			return;
		}
		
        // create the dialog, with operator options
        GenericDialog gd = new GenericDialog("Geometric Measures 3D");
        gd.addChoice("Number of Directions:", dirNumberLabels, dirNumberLabels[1]);
        gd.showDialog();
        
        // If cancel was clicked, do nothing
        if (gd.wasCanceled())
            return;
        
        // extract number of directions
        int nDirsIndex = gd.getNextChoiceIndex();
        int nDirs = dirNumbers[nDirsIndex];
        
        // Execute the plugin
        Object[] results = exec(imagePlus, nDirs);
        ResultsTable table = (ResultsTable) results[1];
        
 		// create string for indexing results
		String tableName = imagePlus.getShortTitle() + "-measures"; 
    
		// show result
		table.show(tableName);
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
        
        int[] labels = LabelImages.findAllLabels(image);
        double[] volumes = GeometricMeasures3D.volume(image, labels, resol);
        double[] surfaces = GeometricMeasures3D.surfaceAreaByLut(image, labels, resol, nDirs);
        double[] sphericities = GeometricMeasures3D.computeSphericity(volumes, surfaces);
   
		// Create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < labels.length; i++) {
			table.incrementCounter();
			table.addLabel(Integer.toString(labels[i]));
			table.addValue("Volume", volumes[i]);
			table.addValue("Surface", surfaces[i]);
			table.addValue("Sphericity", sphericities[i]);
		}

		// return the created array
		return new Object[]{"Geometric Measures", table};
    }
}
