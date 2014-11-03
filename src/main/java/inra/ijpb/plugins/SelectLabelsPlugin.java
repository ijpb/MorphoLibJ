package inra.ijpb.plugins;


import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import inra.ijpb.label.LabelImages;

/**
 * Creates a new binary image containing only the selected label(s).
 */
public class SelectLabelsPlugin implements PlugIn {

    // ====================================================
    // Global Constants
    
   

    // ====================================================
    // Class variables
    
   /**
     * When this options is set to true, information messages are displayed on
     * the console, and the number of counts for each direction is included in
     * results table. 
     */
    public boolean debug = false;
    
	ImagePlus imagePlus;
	
    
    // ====================================================
    // Calling functions 
   

	/* (non-Javadoc)
     * @see ij.plugin.PlugIn#run(java.lang.String)
     */
    public void run(String args) {
        ImagePlus imagePlus = IJ.getImage();
        
        // create the dialog, with operator options
        GenericDialog gd = new GenericDialog("Select Label(s)");
        gd.addMessage("Add labels seperated by comma.\nEx: [1, 2, 6, 9]");
        gd.addStringField("Label(s)", "1");
        gd.showDialog();
        
        // If cancel was clicked, do nothing
        if (gd.wasCanceled())
            return;
        
        // extract label index, and number of pixel border to add
        String labelString = (String) gd.getNextString();
      
        int[] labels = parseLabels(labelString);
        ImagePlus selectedPlus = LabelImages.keepLabels(imagePlus, labels);
        		
        // copy settings
        selectedPlus.copyScale(imagePlus);
        selectedPlus.setDisplayRange(imagePlus.getDisplayRangeMin(), imagePlus.getDisplayRangeMax());
        
        // display and adapt visible slice
        selectedPlus.show();
        if (imagePlus.getStackSize() > 1)
        {	
        	selectedPlus.setSlice(imagePlus.getSlice());
        }
    }
    
    private static final int[] parseLabels(String string) 
    {
    	String[] tokens = string.split("[, ]+");
    	int n = tokens.length;
    	
    	int[] labels = new int[n];
    	for (int i = 0; i < n; i++)
    	{
    		labels[i] = Integer.parseInt(tokens[i]);
    	}
    	return labels;
    }
    
}
