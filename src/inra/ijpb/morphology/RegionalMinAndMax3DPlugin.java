package inra.ijpb.morphology;


import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import inra.ijpb.util.IJUtils;

/**
 * Plugin for computing regional minima and maxima in grayscale images.
 * Works for planar gray8 images.
 *
 * @see MinimaAndMaxima
 * @author David Legland
 *
 */
public class RegionalMinAndMax3DPlugin implements PlugIn {

	/**
	 * A customized enumeration to choose between regional minima or maxima.
	 */
	public enum Operation {
		REGIONAL_MAXIMA("Regional Maxima", "rmax"),
		REGIONAL_MINIMA("Regional Minima", "rmin");
		
		private final String label;
		private final String suffix;
		
		private Operation(String label, String suffix) {
			this.label = label;
			this.suffix = suffix;
		}
		
		public ImageStack apply(ImageStack image, int connectivity) {
			if (this == REGIONAL_MAXIMA)
				return MinimaAndMaxima3D.regionalMaxima(image, connectivity);
			if (this == REGIONAL_MINIMA)
				return MinimaAndMaxima3D.regionalMinima(image, connectivity);
			
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

	private final static String[] connectivityLabels = {"6", "26"}; 
	private final static int[] connectivityValues = {6, 26}; 

	

	@Override
	public void run(String arg) {
		ImagePlus imagePlus = IJ.getImage();
		
		if (imagePlus.getStackSize() == 1) {
			IJ.error("Requires a Stack");
		}
		
		ImageStack stack = imagePlus.getStack();
		
		// Create the configuration dialog
		GenericDialog gd = new GenericDialog("Regional Minima and Maxima");
		gd.addChoice("Operation", Operation.getAllLabels(), 
				Operation.REGIONAL_MINIMA.label);
		gd.addChoice("Connectivity", connectivityLabels, connectivityLabels[0]);
//		gd.addHelp("http://imagejdocu.tudor.lu/doku.php?id=plugin:morphology:fast_morphological_filters:start");
        gd.showDialog();
        if (gd.wasCanceled())
        	return;
        
		// extract chosen parameters
		Operation op = Operation.fromLabel(gd.getNextChoice());
		int conn = connectivityValues[gd.getNextChoiceIndex()];
        
		long t0 = System.currentTimeMillis();
		
		ImageStack result = op.apply(stack, conn);

		String newName = createResultImageName(imagePlus, op);
		ImagePlus resultPlus = new ImagePlus(newName, result);
		resultPlus.show();
		
		resultPlus.setSlice(imagePlus.getSlice());

		long t1 = System.currentTimeMillis();
		IJUtils.showElapsedTime(op.toString(), t1 - t0, imagePlus);
	}
		
	/**
	 * Creates the name for result image, by adding a suffix to the base name
	 * of original image.
	 */
	private String createResultImageName(ImagePlus baseImage, Operation op) {
		return baseImage.getShortTitle() + "-" + op.getSuffix();
	}


}
