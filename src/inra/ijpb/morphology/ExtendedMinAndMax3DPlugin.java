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
public class ExtendedMinAndMax3DPlugin implements PlugIn {

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
		
		public ImageStack apply(ImageStack image, int dynamic) {
			if (this == EXTENDED_MAXIMA)
				return MinimaAndMaxima3D.extendedMaxima(image, dynamic);
			if (this == EXTENDED_MINIMA)
				return MinimaAndMaxima3D.extendedMinima(image, dynamic);
			
			throw new RuntimeException(
					"Unable to process the " + this + " morphological operation");
		}
		
		public ImageStack apply(ImageStack image, int dynamic, int connectivity) {
			if (this == EXTENDED_MAXIMA)
				return MinimaAndMaxima3D.extendedMaxima(image, dynamic, connectivity);
			if (this == EXTENDED_MINIMA)
				return MinimaAndMaxima3D.extendedMinima(image, dynamic, connectivity);
			
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
		int sizeX = stack.getWidth();
		int sizeY = stack.getHeight();
		int sizeZ = stack.getSize();
		boolean isGray8 = stack.getBitDepth() == 8;
		double minValue, maxValue;
		if (isGray8) {
			minValue = 1;
			maxValue = 255;
		} else {
			minValue = Double.MAX_VALUE;
			maxValue = Double.MAX_VALUE;
			for (int z = 0; z < sizeZ; z++) {
				for (int y = 0; y < sizeY; y++) {
					for (int x = 0; x < sizeX; x++) {
						double val = stack.getVoxel(x, y, z);
						minValue = Math.min(minValue, val);
						maxValue = Math.max(maxValue, val);
					}
				}
			}
		}
		
		// Create the configuration dialog
		GenericDialog gd = new GenericDialog("Regional Minima and Maxima");
		gd.addChoice("Operation", Operation.getAllLabels(), 
				Operation.EXTENDED_MINIMA.label);
		gd.addSlider("Dynamic", minValue, maxValue, 10);		
		gd.addChoice("Connectivity", connectivityLabels, connectivityLabels[0]);
//		gd.addHelp("http://imagejdocu.tudor.lu/doku.php?id=plugin:morphology:fast_morphological_filters:start");
        gd.showDialog();
        if (gd.wasCanceled())
        	return;
        
		long t0 = System.currentTimeMillis();
		
		// extract chosen parameters
		Operation op = Operation.fromLabel(gd.getNextChoice());
		int dynamic = (int) gd.getNextNumber();
		int conn = connectivityValues[gd.getNextChoiceIndex()];
        
		ImageStack result = op.apply(stack, dynamic, conn);
		
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
