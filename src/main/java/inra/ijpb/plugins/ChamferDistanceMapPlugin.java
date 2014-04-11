package inra.ijpb.plugins;

import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ImageProcessor;
import inra.ijpb.binary.distmap.ChamferDistance;
import inra.ijpb.binary.distmap.ChamferDistance3x3Float;
import inra.ijpb.binary.distmap.ChamferDistance3x3Short;
import inra.ijpb.binary.distmap.ChamferDistance5x5Float;
import inra.ijpb.binary.distmap.ChamferDistance5x5Short;

import java.awt.AWTEvent;

import static inra.ijpb.binary.distmap.ChamferDistance.Weights;

/**
 * Compute distance map, with possibility to choose chamfer weights, result 
 * type, and to normalize result or not.
 *
 * @author dlegland
 *
 */
public class ChamferDistanceMapPlugin implements ExtendedPlugInFilter, DialogListener {

	
	/** Apparently, it's better to store flags in plugin */
	private int flags = DOES_8G | KEEP_PREVIEW | FINAL_PROCESSING;
	PlugInFilterRunner pfr;
	int nPasses;
	boolean previewing = false;
	
	/** need to keep the instance of ImagePlus */ 
	private ImagePlus imagePlus;
	
	/** keep the original image, to restore it after the preview */
	private ImageProcessor baseImage;
	
	/** the different weights */
	private Weights weights; 
	private boolean floatProcessing 	= false;
	private boolean normalize 	= false;
	
	/** Keep instance of result image */
	private ImageProcessor result;
	
	/**
	 * Called at the beginning of the process to know if the plugin can be run
	 * with current image, and at the end to finalize.
	 */
	public int setup(String arg, ImagePlus imp) {
		// Special case of plugin called to finalize the process
		if (arg.equals("final")) {
			// replace the preview image by the original image 
			imagePlus.setProcessor(baseImage);
			imagePlus.draw();
			
			// Create a new ImagePlus with the filter result
			String newName = createResultImageName(imagePlus);
			ImagePlus resPlus = new ImagePlus(newName, result);
			resPlus.copyScale(imagePlus);
			resPlus.show();
			return DONE;
		}
		
		return flags;
	}

    public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
    	// Store user data
    	this.imagePlus = imp;
    	this.baseImage = imp.getProcessor().duplicate();
    	this.pfr = pfr;

    	// Create a new generic dialog with appropriate options
    	GenericDialog gd = new GenericDialog("Distance Map");
    	gd.addChoice("Distances", Weights.getAllLabels(), 
    			Weights.BORGEFORS.toString());			
    	String[] outputTypes = new String[]{"32 bits", "16 bits"};
    	gd.addChoice("Output Type", outputTypes, outputTypes[0]);
    	gd.addCheckbox("Normalize weights", true);	
    	gd.addPreviewCheckbox(pfr);
    	gd.addDialogListener(this);
        previewing = true;
		gd.addHelp("http://imagejdocu.tudor.lu/doku.php?id=plugin:morphology:fast_morphological_filters:start");
        gd.showDialog();
        previewing = false;
        
    	// test cancel  
    	if (gd.wasCanceled())
    		return DONE;

    	// set up current parameters
    	String weightLabel = gd.getNextChoice();
    	floatProcessing = gd.getNextChoiceIndex() == 0;
    	normalize = gd.getNextBoolean();

    	// identify which weights should be used
    	weights = Weights.fromLabel(weightLabel);

    	return flags;
    }
    
    /**
     * Called when a dialog widget has been modified: recomputes option values
     * from dialog content. 
     */
    public boolean dialogItemChanged(GenericDialog gd, AWTEvent evt) {
    	// set up current parameters
    	String weightLabel = gd.getNextChoice();
    	floatProcessing = gd.getNextChoiceIndex() == 0;
    	normalize = gd.getNextBoolean();

    	// identify which weights should be used
    	weights = Weights.fromLabel(weightLabel);
        return true;
    }

    public void setNPasses (int nPasses) {
    	this.nPasses = nPasses;
    }
    
    /**
     * Apply the current filter settings to process the given image. 
     */
    public void run(ImageProcessor image) {
    	if (floatProcessing) {
    		result = processFloat(image, weights.getFloatWeights(), normalize);
		} else {
			result = processShort(image, weights.getShortWeights(), normalize);
		}
    	
    	if (previewing) {
    		// Fill up the values of original image with values of the result
    		double valMax = result.getMax();
    		for (int i = 0; i < image.getPixelCount(); i++) {
    			image.set(i, (int) (255 * result.getf(i) / valMax));
    		}
    		image.resetMinAndMax();
    		if (image.isInvertedLut())
    			image.invertLut();
        }
    }
    
    private ImageProcessor processFloat(ImageProcessor image, float[] weights, boolean normalize) {
    	// Initialize calculator
    	ChamferDistance calc;
    	if (weights.length == 2) {
    		calc = new ChamferDistance3x3Float(weights, normalize);
    	} else {
    		calc = new ChamferDistance5x5Float(weights, normalize);
    	}

    	// Compute distance on specified images
    	return calc.distanceMap(image);
    }

    private ImageProcessor processShort(ImageProcessor image, short[] weights, boolean normalize) {
    	// Initialize calculator
    	ChamferDistance calc;
    	if (weights.length == 2) {
    		calc = new ChamferDistance3x3Short(weights, normalize);
    	} else {
    		calc = new ChamferDistance5x5Short(weights, normalize);
    	}

    	// Compute distance on specified images
    	return calc.distanceMap(image);
    }
   
		
	/**
	 * Computes the distance propagation from the boundary of the particles.
	 * Background is assumed to be 0. 
	 */
	public Object[] exec(ImagePlus image, String newName, float[] weights, boolean normalize) {
		// Check validity of parameters
		if (image == null) {
			System.err.println("Mask image not specified");
			return null;
		}
		
		if (newName == null)
			newName = createResultImageName(image);
		if (weights == null) {
			System.err.println("Weights not specified");
			return null;
		}
	
		// Initialize calculator
		ChamferDistance calc;
		if (weights.length == 2) {
			calc = new ChamferDistance3x3Float(weights, normalize);
		} else {
			calc = new ChamferDistance5x5Float(weights, normalize);
		}
		// Compute distance on specified images
		ImagePlus result = calc.distanceMap(image, newName);
				
		// create result array
		return new Object[]{newName, result};
	}
	
	/**
	 * Compute the distance propagation from the boundary of the white particles. 
	 */
	public Object[] exec(ImagePlus image, String newName, short[] weights, boolean normalize) {
		// Check validity of parameters
		if (image == null) {
			System.err.println("Mask image not specified");
			return null;
		}
		
		if (newName == null)
			newName = createResultImageName(image);
		if (weights == null) {
			System.err.println("Weights not specified");
			return null;
		}
	
		// Initialize calculator
		ChamferDistance calc;
		if (weights.length == 2) {
			calc = new ChamferDistance3x3Short(weights, normalize);
		} else {
			calc = new ChamferDistance5x5Short(weights, normalize);
		}
		
		// Compute distance on specified images
		ImagePlus result = calc.distanceMap(image, newName);
				
		// create result array
		return new Object[]{newName, result};
	}
	
	private static String createResultImageName(ImagePlus baseImage) {
		return baseImage.getShortTitle() + "-dist";
	}

}
