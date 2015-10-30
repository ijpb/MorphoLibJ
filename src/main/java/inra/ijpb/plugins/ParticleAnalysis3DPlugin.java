package inra.ijpb.plugins;


import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.GeometricMeasures3D;

/**
 * Plugin for measuring geometric quantities such as volume, surface area,
 * and eventually sphericity index.
 * 
 * Volume is obtained by counting the number of voxels. Surface area is
 * computed using discretization of Crofton formula. Sphericity is obtained as
 * the ratio of V^2 by S^3, multiplied by 36*pi.
 * 
 * If the input image is calibrated, the spatial resolution is taken into 
 * account for computing geometric features.
 * 
 * @see inra.ijpb.measure.GeometricMeasures3D
 * 
 * @author David Legland
 *
 */
public class ParticleAnalysis3DPlugin implements PlugIn
{
    // ====================================================
    // Global Constants
    
    /**
     * List of available numbers of directions
     */
    public final static String[] dirNumberLabels = {
            "Crofton  (3 dirs.)", 
            "Crofton (13 dirs.)" 
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
    public void run(String args) 
    {
        ImagePlus imagePlus = IJ.getImage();
        
		if (imagePlus.getStackSize() == 1) 
		{
			IJ.error("Requires a Stack");
			return;
		}
		
        // create the dialog, with operator options
        GenericDialog gd = new GenericDialog("Particles Analysis 3D");
        gd.addChoice("Surface area method:", dirNumberLabels, dirNumberLabels[1]);
        gd.addCheckbox("Sphericity", true);
        gd.showDialog();
        
        // If cancel was clicked, do nothing
        if (gd.wasCanceled())
            return;
        
        // extract analysis options
        int nDirsIndex = gd.getNextChoiceIndex();
        int nDirs = dirNumbers[nDirsIndex];
        boolean computeSphericity = gd.getNextBoolean();
        
        // Execute the plugin
        Object[] results = exec(imagePlus, nDirs, computeSphericity);
        ResultsTable table = (ResultsTable) results[1];
        
 		// create string for indexing results
		String tableName = imagePlus.getShortTitle() + "-measures"; 
    
		// show result
		table.show(tableName);
    }
    
    /**
     * Main body of the plugin. 
     */
    public Object[] exec(ImagePlus imagePlus, int nDirs, boolean computeSphericity) 
    {
        // Check validity of parameters
        if (imagePlus==null) 
            return null;

        ImageStack image = imagePlus.getStack();

        // Extract spatial calibration
        Calibration cal = imagePlus.getCalibration();
        double[] resol = new double[]{1, 1, 1};
        if (cal.scaled()) 
        {
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
		for (int i = 0; i < labels.length; i++) 
		{
			table.incrementCounter();
			table.addLabel(Integer.toString(labels[i]));
			table.addValue("Volume", volumes[i]);
			table.addValue("Surface", surfaces[i]);
			if (computeSphericity)
				table.addValue("Sphericity", sphericities[i]);
		}

		// return the created array
		return new Object[]{"Geometric Measures", table};
    }
}
