/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.ImageStack;
import inra.ijpb.morphology.MinimaAndMaxima3D;
import inra.ijpb.util.IJUtils;

/**
 * Plugin for imposing minima or maxima on a grayscale 3D image, using a specific
 * connectivity. 
 */
public class ImposeMinAndMax3DPlugin implements PlugIn {

	/**
	 * A customized enumeration to choose between minima or maxima imposition.
	 */
	public enum Operation {
		IMPOSE_MINIMA("Impose Minima"),
		IMPOSE_MAXIMA("Impose Minima");
		
		private final String label;
		
		private Operation(String label) {
			this.label = label;
		}
		
		public ImageStack applyTo(ImageStack image,
				ImageStack markers) {
			if (this == IMPOSE_MINIMA)
				return MinimaAndMaxima3D.imposeMinima(image, markers);
			if (this == IMPOSE_MAXIMA)
				return MinimaAndMaxima3D.imposeMaxima(image, markers);
						
			throw new RuntimeException(
					"Unable to process the " + this + " operation");
		}
		
		public ImageStack applyTo(ImageStack image,
				ImageStack markers, int conn) {
			if (this == IMPOSE_MINIMA)
				return MinimaAndMaxima3D.imposeMinima(image, markers, conn);
			if (this == IMPOSE_MAXIMA)
				return MinimaAndMaxima3D.imposeMaxima(image, markers, conn);
						
			throw new RuntimeException(
					"Unable to process the " + this + " operation");
		}
		
		public String toString() {
			return this.label;
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

	private final static String[] connectivityLabels = {"6", "26"}; 
	private final static int[] connectivityValues = {6, 26}; 
	

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg) {
		// Open a dialog to choose:
		// - mask image
		// - marker image
		int[] indices = WindowManager.getIDList();
		if (indices == null) {
			IJ.error("No image", "Need at least one image to work");
			return;
		}
	
		// create the list of image names
		String[] imageNames = new String[indices.length];
		for (int i = 0; i < indices.length; i++) {
			imageNames[i] = WindowManager.getImage(indices[i]).getTitle();
		}
		
		// create the dialog
		GenericDialog gd = new GenericDialog("Geodesic Reconstruction");
		
		gd.addChoice("Original Image", imageNames, IJ.getImage().getTitle());
		gd.addChoice("Minima Image", imageNames, IJ.getImage().getTitle());
		gd.addChoice("Operation", 
				Operation.getAllLabels(), 
				Operation.IMPOSE_MINIMA.label);
		gd.addChoice("Connectivity", connectivityLabels, connectivityLabels[0]);
		gd.showDialog();
		
		if (gd.wasCanceled())
			return;

		// set up current parameters
		int refImageIndex = gd.getNextChoiceIndex();
		ImagePlus refImage = WindowManager.getImage(refImageIndex + 1);
		int markerImageIndex = gd.getNextChoiceIndex();
		ImagePlus markerImage = WindowManager.getImage(markerImageIndex + 1);
		Operation op = Operation.fromLabel(gd.getNextChoice());
		int conn = connectivityValues[gd.getNextChoiceIndex()];
		
		// Extract image procesors
		ImageStack refStack = refImage.getStack();
		ImageStack markerStack = markerImage.getStack();
		
		long t0 = System.currentTimeMillis();
		
		// Compute geodesic reconstruction
		ImageStack recProc = op.applyTo(refStack, markerStack, conn);
		
		// Keep same color model as 
		recProc.setColorModel(refStack.getColorModel());

		// create resulting image
		String newName = createResultImageName(refImage);
		ImagePlus resultImage = new ImagePlus(newName, recProc);
		resultImage.copyScale(markerImage);
		resultImage.show();
		resultImage.setSlice(refImage.getSlice());

		long t1 = System.currentTimeMillis();
		IJUtils.showElapsedTime(op.toString(), t1 - t0, refImage);
	}

	private static String createResultImageName(ImagePlus baseImage) {
		return baseImage.getShortTitle() + "-imp";
	}
	
}
