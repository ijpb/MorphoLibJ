package inra.ijpb.plugins;


import ij.IJ;
import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ImageProcessor;
import ij.process.ByteProcessor;
import inra.ijpb.morphology.MinimaAndMaxima;

import java.awt.AWTEvent;

/**
 * Plugin for computing extended minima and maxima in grayscale images.
 * Works for planar gray8 images.
 *
 * @see MinimaAndMaxima
 * @author David Legland
 *
 */
public class ExtendedMinAndMaxPlugin implements ExtendedPlugInFilter, DialogListener {

	/**
	 * A customized enumeration to choose between extended minima or maxima.
	 */
	public enum Operation {
		EXTENDED_MAXIMA("Extended Maxima", "emax"),
		EXTENDED_MINIMA("Extended Minima", "emin");
		
		private final String label;
		private final String suffix;
		
		private Operation(String label, String suffix) {
			this.label = label;
			this.suffix = suffix;
		}
		
		public ImageProcessor apply(ImageProcessor image, int dynamic) {
			if (this == EXTENDED_MAXIMA)
				return MinimaAndMaxima.extendedMaxima(image, dynamic);
			if (this == EXTENDED_MINIMA)
				return MinimaAndMaxima.extendedMinima(image, dynamic);
			
			throw new RuntimeException(
					"Unable to process the " + this + " morphological operation");
		}
		
		public ImageProcessor apply(ImageProcessor image, int dynamic, int connectivity) {
			if (this == EXTENDED_MAXIMA)
				return MinimaAndMaxima.extendedMaxima(image, dynamic, connectivity);
			if (this == EXTENDED_MINIMA)
				return MinimaAndMaxima.extendedMinima(image, dynamic, connectivity);
			
			throw new RuntimeException(
					"Unable to process the " + this + " morphological operation");
		}

		public String toString() {
			return this.label;
		}
		
		public String getSuffix() {
			return this.suffix;
		}
		
		public static String[] getAllLabels(){
			int n = Operation.values().length;
			String[] result = new String[n];
			
			int i = 0;
			for (Operation op : Operation.values())
				result[i++] = op.label;
			
			return result;
		}
		
		/**
		 * Determines the operation type from its label.
		 * @throws IllegalArgumentException if label is not recognized.
		 */
		public static Operation fromLabel(String opLabel) {
			if (opLabel != null)
				opLabel = opLabel.toLowerCase();
			for (Operation op : Operation.values()) {
				String cmp = op.label.toLowerCase();
				if (cmp.equals(opLabel))
					return op;
			}
			throw new IllegalArgumentException("Unable to parse Operation with label: " + opLabel);
		}
	};

	private final static String[] connectivityLabels = {"4", "8"}; 
	private final static int[] connectivityValues = {4, 8}; 
	
	
	/** Apparently, it's better to store flags in plugin */
	private int flags = DOES_ALL | KEEP_PREVIEW | FINAL_PROCESSING;
	
	PlugInFilterRunner pfr;
	int nPasses;
	boolean previewing = false;
	
	/** need to keep the instance of ImagePlus */ 
	private ImagePlus imagePlus;
	
	/** keep the original image, to restore it after the preview */
	private ImageProcessor baseImage;
	
	/** Keep instance of result image */
	private ImageProcessor result;

	Operation op = Operation.EXTENDED_MINIMA;
	int dynamic = 10;
	int connectivity = 4;
	
	/**
	*/
	public int setup(String arg, ImagePlus imp) {
		
		// about...
		if (arg.equals("about")) {
			showAbout(); 
			return DONE;
		}

		// Called at the end for cleaning the results
		if (arg.equals("final")) {
			// replace the preview image by the original image
			imagePlus.setProcessor(baseImage);
			imagePlus.draw();
			
			// as result is binary, choose inverted LUT 
			result.invertLut();
			
			// Create a new ImagePlus with the result
			String newName = createResultImageName(imagePlus);
			ImagePlus resPlus = new ImagePlus(newName, result);
			resPlus.copyScale(imagePlus);
			resPlus.show();
			return DONE;
		}
		
		return flags;
	}
	
	
	public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
		// Normal setup
    	this.imagePlus = imp;
    	this.baseImage = imp.getProcessor().duplicate();

		// Create the configuration dialog
		GenericDialog gd = new GenericDialog("Extended Minima and Maxima");
		
		gd.addChoice("Operation", Operation.getAllLabels(), Operation.EXTENDED_MINIMA.label);
		boolean isGray8 = (this.baseImage instanceof ByteProcessor);
		double minValue = isGray8 ? 1 : this.baseImage.getMin();
		double maxValue = isGray8 ? 255 : this.baseImage.getMax();
		gd.addSlider("Dynamic", minValue, maxValue, 10);
		gd.addChoice("Connectivity", connectivityLabels, connectivityLabels[0]);
		
		gd.addPreviewCheckbox(pfr);
		gd.addDialogListener(this);
        previewing = true;
		gd.addHelp("http://imagejdocu.tudor.lu/doku.php?id=plugin:morphology:fast_morphological_filters:start");
        gd.showDialog();
        previewing = false;
        
        if (gd.wasCanceled()) {
			return DONE;
        }
        
    	parseDialogParameters(gd);
			
		// clean up an return 
		gd.dispose();
		return flags;
	}

    public boolean dialogItemChanged(GenericDialog gd, AWTEvent evt) {
    	parseDialogParameters(gd);
    	return true;
    }

    private void parseDialogParameters(GenericDialog gd) {
		// extract chosen parameters
		this.op 			= Operation.fromLabel(gd.getNextChoice());
		this.dynamic 		= (int) gd.getNextNumber();
		this.connectivity 	= connectivityValues[gd.getNextChoiceIndex()];
    }

    public void setNPasses (int nPasses) {
    	this.nPasses = nPasses;
    }
    
	@Override
	public void run(ImageProcessor image) {
		// Create structuring element of the given size
		// Execute core of the plugin
		result = op.apply(image, dynamic, connectivity);

    	if (previewing) {
    		// Fill up the values of original image with inverted values of the 
    		// (binary) result
    		double valMax = result.getMax();
    		for (int i = 0; i < image.getPixelCount(); i++) {
    			image.set(i, 255 - (int) (255 * result.getf(i) / valMax));
    		}
        }
	}
	
	// About...
	private void showAbout() {
		IJ.showMessage("Morphological Filters",
				"Fast Grayscale Morphological Filtering,\n" +
				"http://imagejdocu.tudor.lu/doku.php?id=plugin:morphology:fast_morphological_filters:start\n" +
				"\n" +
				"by David Legland\n" +
				"(david.legland@grignon.inra.fr)");
	}
	
	/**
	 * Creates the name for result image, by adding a suffix to the base name
	 * of original image.
	 */
	private String createResultImageName(ImagePlus baseImage) {
		return baseImage.getShortTitle() + "-" + op.getSuffix();
	}

}
