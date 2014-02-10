package inra.ijpb.binary.distmap;

import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ImageProcessor;

import java.awt.AWTEvent;


/**
 * Compute distance map, with possibility to choose chamfer weights, result 
 * type, and to normalize result or not.
 *
 * @author dlegland
 *
 */
public class ChamferDistanceMapPlugin implements ExtendedPlugInFilter, DialogListener {

	/**
	 * A pre-defined set of weigths that can be used to compute distance maps.
	 * 
	 */
	public enum Weights {
		CHESSBOARD("Chessboard (1,1)", new short[]{1,1}),
		CITY_BLOCK("City-Block (1,2)", new short[]{1, 2}),
		QUASI_EUCLIDEAN("Quasi-Euclidean (1,1.41)", new short[]{10, 14}, 
				new float[]{1, (float)Math.sqrt(2)}),
		BORGEFORS("Borgefors (3,4)", new short[]{3, 4}),
		WEIGHTS_23("Weights (2,3)", new short[]{2, 3}),
		WEIGHTS_57("Weights (5,7)", new short[]{5, 7}),	
		CHESSKNIGHT("Chessknight (5,7,11)", new short[]{5, 7, 11});

		private final String label;
		private final short[] shortWeights;
		private final float[] floatWeights;
		
		private Weights(String label, short[] shortWeights) {
			this.label = label;
			this.shortWeights = shortWeights;
			this.floatWeights = new float[shortWeights.length];
			for (int i = 0; i < shortWeights.length; i++)
				this.floatWeights[i] = (float) shortWeights[i];
		}

		private Weights(String label, short[] shortWeights, float[] floatWeights) {
			this.label = label;
			this.shortWeights = shortWeights;
			this.floatWeights = floatWeights;
		}
		
		public short[] getShortWeights() {
			return this.shortWeights;
		}
		
		public float[] getFloatWeights() {
			return this.floatWeights;
		}
		
		public String toString() {
			return this.label;
		}
		
		public static String[] getAllLabels(){
			int n = Weights.values().length;
			String[] result = new String[n];
			
			int i = 0;
			for (Weights weight : Weights.values())
				result[i++] = weight.label;
			
			return result;
		}
		
		/**
		 * Determines the operation type from its label.
		 * @throws IllegalArgumentException if label is not recognized.
		 */
		public static Weights fromLabel(String label) {
			if (label != null)
				label = label.toLowerCase();
			for (Weights weight : Weights.values()) {
				String cmp = weight.label.toLowerCase();
				if (cmp.equals(label))
					return weight;
			}
			throw new IllegalArgumentException("Unable to parse Weights with label: " + label);
		}

	}
	
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
	private boolean normalizeWeights 	= false;
	
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
    			Weights.BORGEFORS.label);			
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
    	normalizeWeights = gd.getNextBoolean();

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
    	normalizeWeights = gd.getNextBoolean();

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
    		result = processFloat(image, weights.floatWeights, normalizeWeights);
		} else {
			result = processShort(image, weights.shortWeights, normalizeWeights);
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
