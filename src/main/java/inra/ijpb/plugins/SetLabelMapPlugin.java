/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import inra.ijpb.util.ColorMaps;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.image.ColorModel;

import inra.ijpb.util.CommonColors;
import static inra.ijpb.util.ColorMaps.CommonLabelMaps;

/**
 * Changes the current LUT such that each label is associated to a different
 * color, and specifies color of background. Works for both planar images and
 * 3D stacks.
 * 
 * Opens a dialog to choose a colormap, a background color, and a shuffle option. 
 * Preview option is available.
 * 
 * Notes: 
 * <ul>
 * <li>when shuffle is activated, the result may be different from preview.</li>
 * <li>when used with 16 bit images, labels with low values are associated to 
 * 		the same color as background<li>
 * </ul>
 *  
 * @author David Legland
 *
 */
public class SetLabelMapPlugin implements PlugIn, DialogListener 
{
	// Images managed by the plugin
	
	ImagePlus imagePlus;
	ImageStack baseStack;
	ImageStack resultStack;
	boolean baseChanges;
	
	// Plugin inner data
	
	String lutName;
	Color bgColor = Color.WHITE;
	boolean shuffleLut = true;
	
	int labelMax = 0;
	byte[][] colorMap;
	
	/** Save old color model in case of cancel */
	ColorModel oldColorModel;

	
	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	@Override
	public void run(String args) {
		imagePlus = IJ.getImage();
		if (imagePlus.getStackSize() == 1) 
			oldColorModel = imagePlus.getProcessor().getColorModel();
		else
			oldColorModel = imagePlus.getStack().getColorModel();
			
		GenericDialog gd = showDialog();
		
        // test cancel  
    	if (gd.wasCanceled()) {
    		setColorModel(oldColorModel);
    		imagePlus.updateAndDraw();
    		return;
    	}
    	
    	parseDialogParameters(gd);

		ColorModel cm = ColorMaps.createColorModel(colorMap, bgColor);
		setColorModel(cm);
		imagePlus.updateAndDraw();
	}

	
	public GenericDialog showDialog() {
		
		// Create a new generic dialog with appropriate options
    	GenericDialog gd = new GenericDialog("Choose Label Map");
    	gd.addChoice("Colormap", CommonLabelMaps.getAllLabels(), 
    			CommonLabelMaps.JET.getLabel());
    	gd.addChoice("Background", CommonColors.getAllLabels(), CommonColors.WHITE.toString());
    	gd.addCheckbox("Shuffle", true);
    	
    	gd.addPreviewCheckbox(null);
    	gd.addDialogListener(this);
    	parseDialogParameters(gd);
    	gd.showDialog();

        return gd;
	}


	public boolean dialogItemChanged(GenericDialog gd, AWTEvent evt) {
		parseDialogParameters(gd);
		if (evt == null)
			return true;
		if (gd.getPreviewCheckbox().getState()) {
			updatePreview();
		} else {
			removePreview();
		}

		return true;
	}
		
	private void updatePreview() {
		ColorModel cm = ColorMaps.createColorModel(colorMap, bgColor);
		setColorModel(cm);
		imagePlus.updateAndDraw();
	}
	
	private void removePreview() {
		setColorModel(oldColorModel);
		imagePlus.updateAndDraw();
	}

	private void parseDialogParameters(GenericDialog gd) {
		lutName = gd.getNextChoice();
		String bgColorName = gd.getNextChoice();
		bgColor = CommonColors.fromLabel(bgColorName).getColor();
		shuffleLut = gd.getNextBoolean();

		// I could not use more than 256 colors for the LUT with ShortProcessor,
		// problem at ij.process.ShortProcessor.createBufferedImage(ShortProcessor.java:135) 
		colorMap = CommonLabelMaps.fromLabel(lutName).computeLut(255, shuffleLut);
	}
	
	private void setColorModel(ColorModel cm) {
		ImageProcessor baseImage = imagePlus.getProcessor();
		baseImage.setColorModel(cm);
		if (imagePlus.getStackSize() > 1) {
			ImageStack stack = imagePlus.getStack();
			stack.setColorModel(cm);
		}		
	}
}
