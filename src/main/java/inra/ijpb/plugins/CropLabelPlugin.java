package inra.ijpb.plugins;


import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import inra.ijpb.morphology.LabelImages;

/**
 * Crops a label image and binarize it to contain only the specified label.
 */
public class CropLabelPlugin implements PlugIn {

    // ====================================================
    // Global Constants

    // ====================================================
    // Class variables
    
   /**
     * When this options is set to true, information messages are displayed on
     * the console. 
     */
    public boolean debug  = false;
    
    
    // ====================================================
    // Calling functions 
   

	/* (non-Javadoc)
     * @see ij.plugin.PlugIn#run(java.lang.String)
     */
    public void run(String args) 
    {
        ImagePlus imagePlus = IJ.getImage();
		
        // create the dialog, with operator options
        GenericDialog gd = new GenericDialog("Crop Label");
        gd.addNumericField("Label", 1, 0);
        gd.addNumericField("Border pixels", 1, 0);
        gd.showDialog();
        
        // If cancel was clicked, do nothing
        if (gd.wasCanceled())
            return;
        
        // extract label index, and number of pixel border to add
        int label = (int) gd.getNextNumber();
        int border = (int) gd.getNextNumber();

        ImagePlus croppedPlus = LabelImages.cropLabel(imagePlus, label, border);

        // display and adapt settings
        croppedPlus.show();
        if (imagePlus.getStackSize() > 1) 
        {
        	croppedPlus.setSlice(croppedPlus.getStackSize() / 2);
        }
    }
}
