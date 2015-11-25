package inra.ijpb.plugins;


import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import inra.ijpb.binary.ChamferWeights3D;
import inra.ijpb.measure.GeometricMeasures3D;

public class MaxInscribedSpherePlugin implements PlugIn 
{
    // ====================================================
    // Global Constants

    // ====================================================
    // Class variables
    
    // ====================================================
    // Calling functions 
    
	@Override
	public void run(String arg0) 
	{
		// Open a dialog to choose:
		// - a label image
		// - a set of weights
		int[] indices = WindowManager.getIDList();
		if (indices==null)
		{
			IJ.error("No image", "Need at least one image to work");
			return;
		}
		
		// create the list of image names
		String[] imageNames = new String[indices.length];
		for (int i=0; i<indices.length; i++)
		{
			imageNames[i] = WindowManager.getImage(indices[i]).getTitle();
		}
		
		// name of selected image
		String selectedImageName = IJ.getImage().getTitle();

		// create the dialog
		GenericDialog gd = new GenericDialog("Max. Inscribed Sphere");
		gd.addChoice("Label Image:", imageNames, selectedImageName);
		// Set Borgefors weights as default
		gd.addChoice("Distances", ChamferWeights3D.getAllLabels(), 
				ChamferWeights3D.BORGEFORS.toString());
		gd.showDialog();
		
		if (gd.wasCanceled())
			return;
		
		// set up current parameters
		int labelImageIndex = gd.getNextChoiceIndex();
		ImagePlus labelImage = WindowManager.getImage(labelImageIndex+1);
		ChamferWeights3D weights = ChamferWeights3D.fromLabel(gd.getNextChoice());
		
		// check if image is a 3D label image
		if (labelImage.getStackSize() <= 1) 
		{
            IJ.showMessage("Input image should be a 3D label image");
            return;
		}
		int type = labelImage.getType(); 
		if (type != ImagePlus.GRAY8 && type != ImagePlus.GRAY16
				&& type != ImagePlus.GRAY32)
		{
            IJ.showMessage("Input image should be a 3D label image");
            return;
        }
        
		// Execute the plugin
        Object[] results = exec(labelImage, weights.getShortWeights());
        ResultsTable table = (ResultsTable) results[1];
        
        // Display plugin result
		String tableName = labelImage.getShortTitle() + "-MaxInscribedSphere"; 
		table.show(tableName);
	}
	   
    /**
	 * Main body of the plugin.
	 * 
	 * @param imagePlus
	 *            the image to analyze
	 * @param weights
	 *            the set of weights for propagating distances
	 * @return an array of objects containing the results
	 */
    public Object[] exec(ImagePlus imagePlus, short[] weights) 
    {
        // Check validity of parameters
        if (imagePlus==null) 
            return null;

        ImageStack image = imagePlus.getStack();
        
        // Extract spatial calibration
        double[] resol = getVoxelSize(imagePlus);

        ResultsTable results = GeometricMeasures3D.maximumInscribedSphere(image, 
        		resol);
        
		// return the created array
		return new Object[]{"Morphometry", results};
    }
    
    private static final double[] getVoxelSize(ImagePlus imagePlus)
    {
    	// Extract spatial calibration
        Calibration cal = imagePlus.getCalibration();
        double[] resol = new double[]{1, 1, 1};
        if (cal.scaled()) 
        {
        	resol[0] = cal.pixelWidth;
        	resol[1] = cal.pixelHeight;
        	resol[2] = cal.pixelHeight;
        }
        return resol;
    }
}
