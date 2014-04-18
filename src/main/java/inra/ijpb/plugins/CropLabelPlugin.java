package inra.ijpb.plugins;


import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import inra.ijpb.morphology.LabelImages;

/**
 * Crops a label image and binarise to contain only the specified label.
 */
public class CropLabelPlugin implements PlugIn {

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
     * @see ij.plugin.PlugIn#run(java.lang.String)
     */
    public void run(String args) {
        ImagePlus imagePlus = IJ.getImage();
        
		if (imagePlus.getStackSize() == 1) {
			IJ.error("Requires a Stack");
			return;
		}
		
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

        // Compute the cropped image
        ImageStack stack = imagePlus.getStack();
        ImageStack cropped = LabelImages.cropLabel(stack, label, border);
        
        // create imageplus
        String newName = imagePlus.getShortTitle() + "-crop"; 
        ImagePlus croppedPlus = new ImagePlus(newName, cropped);
        
        // display and adapt settings
        croppedPlus.show();
        croppedPlus.setSlice(croppedPlus.getStackSize() / 2);
    }
}
