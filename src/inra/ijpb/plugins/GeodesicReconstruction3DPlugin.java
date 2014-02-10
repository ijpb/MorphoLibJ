/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import inra.ijpb.morphology.GeodesicReconstruction3D;
import inra.ijpb.util.IJUtils;

/**
 * Plugin for performing geodesic reconstruction by dimation or by erosion on
 * grayscale images.
 * 
 * Two images are required: the marker image, used to initialize the 
 * reconstruction, an the mask image, used to constrain the reconstruction.
 * The connectivity can also be specified.
 */
public class GeodesicReconstruction3DPlugin implements PlugIn {

	/**
	 * A pre-defined set of operations for geodesic reconstruction.
	 */
	public enum Operation {
		BY_DILATION("By Dilation"),
		BY_EROSION("By Erosion");
		
		private final String label;
		
		private Operation(String label) {
			this.label = label;
		}
		
		public ImageStack applyTo(ImageStack marker, ImageStack mask, int conn) {
			if (this == BY_DILATION)
				return GeodesicReconstruction3D.reconstructByDilation(marker, mask, conn);
			if (this == BY_EROSION)
				return GeodesicReconstruction3D.reconstructByErosion(marker, mask, conn);
						
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

	/**
	 * A pre-defined set of 3D connectivities
	 */
	enum Conn3D {
		C6("6", 6),
		C26("26", 26);
		
		private final String label;
		private final int value;
		
		private Conn3D(String label, int value) {
			this.label = label;
			this.value = value;
		}
		
		public String toString() {
			return this.label;
		}
		
		public int getValue() {
			return this.value;
		}
		
		public static String[] getAllLabels(){
			int n = Conn3D.values().length;
			String[] result = new String[n];
			
			int i = 0;
			for (Conn3D op : Conn3D.values())
				result[i++] = op.label;
			
			return result;
		}
		
		/**
		 * Determines the operation type from its label.
		 * @throws IllegalArgumentException if label is not recognized.
		 */
		public static Conn3D fromLabel(String opLabel) {
			if (opLabel != null)
				opLabel = opLabel.toLowerCase();
			for (Conn3D op : Conn3D.values()) {
				String cmp = op.label.toLowerCase();
				if (cmp.equals(opLabel))
					return op;
			}
			throw new IllegalArgumentException("Unable to parse Conn2D with label: " + opLabel);
		}
	};

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
		
		gd.addChoice("Marker Image", imageNames, IJ.getImage().getTitle());
		gd.addChoice("Mask Image", imageNames, IJ.getImage().getTitle());
		gd.addChoice("Type of Reconstruction", 
				Operation.getAllLabels(), 
				Operation.BY_DILATION.label);
		gd.addChoice("Connectivity", 
				Conn3D.getAllLabels(), 
				Conn3D.C6.label);
		gd.showDialog();
		
		if (gd.wasCanceled())
			return;

		// set up current parameters
		int markerImageIndex = gd.getNextChoiceIndex();
		ImagePlus markerImage = WindowManager.getImage(markerImageIndex + 1);
		int maskImageIndex = gd.getNextChoiceIndex();
		ImagePlus maskImage = WindowManager.getImage(maskImageIndex + 1);
		Operation op = Operation.fromLabel(gd.getNextChoice());
		int conn = Conn3D.fromLabel(gd.getNextChoice()).getValue();

		// Extract image procesors
		ImageStack markerProc = markerImage.getStack();
		ImageStack maskProc = maskImage.getStack();
		
		long t0 = System.currentTimeMillis();
		
		// Compute geodesic reconstruction
		ImageStack recProc = op.applyTo(markerProc, maskProc, conn);
		
		// Keep same color model
		recProc.setColorModel(maskProc.getColorModel());

		// create resulting image
		String newName = createResultImageName(maskImage);
		ImagePlus resultImage = new ImagePlus(newName, recProc);
		resultImage.copyScale(maskImage);
		resultImage.show();
		
		resultImage.setSlice(maskImage.getSlice());

		long t1 = System.currentTimeMillis();
		IJUtils.showElapsedTime(op.toString(), t1 - t0, markerImage);
	}

	private static String createResultImageName(ImagePlus baseImage) {
		return baseImage.getShortTitle() + "-geodRec";
	}
	
}
