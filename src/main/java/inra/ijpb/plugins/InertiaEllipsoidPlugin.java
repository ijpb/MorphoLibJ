package inra.ijpb.plugins;


import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import inra.ijpb.measure.GeometricMeasures3D;

public class InertiaEllipsoidPlugin implements PlugIn {



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
		
		// Extract spatial calibration
		double[] resol = new double[]{1, 1, 1};
        Calibration cal = imagePlus.getCalibration();
        if (cal.scaled()) 
        {
        	resol[0] = cal.pixelWidth;
        	resol[1] = cal.pixelHeight;
        	resol[2] = cal.pixelDepth;
        }

        ImageStack image = imagePlus.getStack();
        ResultsTable table;
        try {
        	table = GeometricMeasures3D.inertiaEllipsoid(image);
        } catch (Exception ex) {
        	String msg = ex.getMessage();
        	IJ.log(msg);
			IJ.error("Problem occured during Inertia Ellipsoid computation:\n" + msg);
        	ex.printStackTrace(System.err);
        	return;
        }

        // show table results
        String title = imagePlus.getShortTitle() + "-ellipsoid";
        table.show(title);
    }
}
