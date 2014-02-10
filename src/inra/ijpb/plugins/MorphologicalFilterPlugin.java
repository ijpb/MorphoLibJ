package inra.ijpb.plugins;


import java.awt.AWTEvent;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Morphology;
import inra.ijpb.morphology.Strel;
import inra.ijpb.morphology.Morphology.Operation;

/**
 * Plugin for computing various morphological filters on gray scale or color
 * images.
 *
 * @author David Legland
 *
 */
public class MorphologicalFilterPlugin implements ExtendedPlugInFilter, DialogListener {

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

	/** an instance of ImagePlus to display the Strel */
	private ImagePlus strelDisplay = null;
	
	
	ImagePlus image = null;
	Operation op = Operation.DILATION;
	Strel.Shape shape = Strel.Shape.SQUARE;
	int radius = 2;
	boolean showStrel;
	
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
			imagePlus.updateAndDraw();
			
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
		// Normal setup
    	this.imagePlus = imp;
    	this.baseImage = imp.getProcessor().duplicate();

		// Create the configuration dialog
		GenericDialog gd = new GenericDialog("Morphological Filter");
		
		gd.addChoice("Operation", Operation.getAllLabels(), 
				this.op.toString());
		gd.addChoice("Element", Strel.Shape.getAllLabels(), 
				this.shape.toString());
		gd.addNumericField("Radius (in pixels)", this.radius, 0);
		gd.addCheckbox("Show Element", false);
		gd.addPreviewCheckbox(pfr);
		gd.addDialogListener(this);
        previewing = true;
		gd.addHelp("http://imagejdocu.tudor.lu/doku.php?id=plugin:morphology:fast_morphological_filters:start");
        gd.showDialog();
        previewing = false;
        
        if (gd.wasCanceled())
        	return DONE;
			
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
		this.op 		= Operation.fromLabel(gd.getNextChoice());
		this.shape 		= Strel.Shape.fromLabel(gd.getNextChoice());
		this.radius 	= (int) gd.getNextNumber();		
		this.showStrel 	= gd.getNextBoolean();

    }
    public void setNPasses (int nPasses) {
    	this.nPasses = nPasses;
    }
    
	@Override
	public void run(ImageProcessor image) {
		// Create structuring element of the given size
		Strel strel = shape.fromRadius(radius);
		
		// Eventually display the structuring element used for processing 
		if (showStrel) {
			showStrelImage(strel);
		}
		
		// Execute core of the plugin
		result = op.apply(image, strel);

    	if (previewing) {
    		// Fill up the values of original image with values of the result
//    		double valMax = result.getMax();
    		for (int i = 0; i < image.getPixelCount(); i++) {
//    			image.set(i, (int) (255 * result.getf(i) / valMax));
    			image.setf(i, result.getf(i));
    		}
    		image.resetMinAndMax();
    		if (image.isInvertedLut())
    			image.invertLut();
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
	 * Displays the current structuring element in a new ImagePlus. 
	 * @param strel the structuring element to display
	 */
	private void showStrelImage(Strel strel) {
		// Size of the strel image (little bit larger than strel)
		int[] dim = strel.getSize();
		int width = dim[0] + 20; 
		int height = dim[1] + 20;
		
		// Creates strel image by dilating a point
		ImageProcessor strelImage = new ByteProcessor(width, height);
		strelImage.set(width / 2, height / 2, 255);
		strelImage = Morphology.dilation(strelImage, strel);
		
		// Forces the display to inverted LUT (display a black over white)
		if (!strelImage.isInvertedLut())
			strelImage.invertLut();
		
		// Display strel image
		if (strelDisplay == null) {
			strelDisplay = new ImagePlus("Structuring Element", strelImage);
		} else {
			strelDisplay.setProcessor(strelImage);
		}
		strelDisplay.show();
	}

	
	/**
	 */
	public ImagePlus exec(ImagePlus image, Operation op, Strel strel) {
		// Check validity of parameters
		if (image == null)
			return null;
		
		// extract the input processor
		ImageProcessor inputProcessor = image.getProcessor();
		
		// apply morphological operation
		ImageProcessor resultProcessor = op.apply(inputProcessor, strel);
		
		// Keep same color model
		resultProcessor.setColorModel(inputProcessor.getColorModel());
		
		// create the new image plus from the processor
		ImagePlus resultImage = new ImagePlus(op.toString(), resultProcessor);
		resultImage.copyScale(image);
					
		// return the created array
		return resultImage;
	}
	
	/**
	 * Creates the name for result image, by adding a suffix to the base name
	 * of original image.
	 */
	private String createResultImageName(ImagePlus baseImage) {
		return baseImage.getShortTitle() + "-" + op.toString();
	}

}
