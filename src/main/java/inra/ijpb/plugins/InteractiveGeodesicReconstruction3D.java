package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.NonBlockingGenericDialog;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import inra.ijpb.data.image.Images3D;
import inra.ijpb.morphology.GeodesicReconstruction3D;
import inra.ijpb.util.IJUtils;

/**
 * Plugin for performing interactive geodesic reconstruction by dilation erosion
 * on grayscale images.
 *
 * In contrast with the non-interactive plugin, the active image is considered
 * the mask image, while the marker image is constructed by user-defined
 * point ROIs. The marker image is used to initialize the reconstruction, and
 * the mask image is used to constrain it.
 * The connectivity can also be specified.
 *
 * @author Ignacio Arganda-Carreras (ignacio.arganda@ehu.eus)
 */
public class InteractiveGeodesicReconstruction3D implements PlugIn
{
	private int connectivity = 6;
	private Operation operation = Operation.BY_DILATION;

	private NonBlockingGenericDialog gd;

	/**
	 * A pre-defined set of operations for geodesic reconstruction.
	 */
	enum Operation {
		BY_DILATION("By Dilation"),
		BY_EROSION("By Erosion");

		private final String label;

		private Operation(String label) {
			this.label = label;
		}

		public ImageStack applyTo(
				ImageStack marker,
				ImageStack mask,
				int conn )
		{
			if ( this == BY_DILATION )
				return GeodesicReconstruction3D.reconstructByDilation(
						marker, mask, conn );
			if ( this == BY_EROSION )
				return GeodesicReconstruction3D.reconstructByErosion(
						marker, mask, conn );

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
		 *
		 * @param opLabel
		 *            the name of the operation
		 * @return the operation corresponding to the name
		 * @throws IllegalArgumentException
		 *             if operation name is not recognized.
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
	 * A pre-defined set of connectivities
	 */
	enum Conn3D {
		C6( "6", 6 ),
		C26( "26", 26 );

		private final String label;
		private final int value;

		private Conn3D( String label, int value ) {
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
			for ( Conn3D op : Conn3D.values() )
				result[i++] = op.label;

			return result;
		}

		/**
		 * Determines the operation type from its label.
		 * @throws IllegalArgumentException if label is not recognized.
		 */
		public static Conn3D fromLabel( String opLabel ) {
			if (opLabel != null)
				opLabel = opLabel.toLowerCase();
			for ( Conn3D op : Conn3D.values() ) {
				String cmp = op.label.toLowerCase();
				if (cmp.equals(opLabel))
					return op;
			}
			throw new IllegalArgumentException( "Unable to parse Conn3D with"
					+ " label: " + opLabel );
		}
	};


	/**
	 * Apply the current filter settings to process the given image.
	 */
	public void run( String arg )
	{
		ImagePlus image = WindowManager.getCurrentImage();
		if ( image == null || image.getImageStackSize() < 2 )
		{
			IJ.error( "Interactive Geodesic Reconstruction 3D",
					"Need at least one 3D image to work" );
			return;
		}

		// select point tool for manual introduction of markers
		Toolbar.getInstance().setTool( Toolbar.POINT );

		// create the dialog
		gd = new NonBlockingGenericDialog( "Interactive Geodesic "
				+ "Reconstruction 3D" );

		gd.addChoice("Type of Reconstruction",
				Operation.getAllLabels(),
				Operation.BY_DILATION.label);
		gd.addChoice("Connectivity",
				Conn3D.getAllLabels(),
				Conn3D.C6.label);
		gd.addHelp( "http://imagej.net/MorphoLibJ" );
		gd.showDialog();

		if (gd.wasCanceled())
			return;

		// set up current parameters
		operation = Operation.fromLabel( gd.getNextChoice() );
		connectivity = Conn3D.fromLabel( gd.getNextChoice() ).getValue();

		long t0 = System.currentTimeMillis();

		// Compute geodesic reconstruction
		final ImagePlus result = process( image, image.getRoi() );

		if( null == result )
			return;

		Images3D.optimizeDisplayRange( result );

		// Display the result image
		result.show();
		result.setSlice( image.getSlice() );

		long t1 = System.currentTimeMillis();
		IJUtils.showElapsedTime( operation.toString(), t1 - t0, image );
	}
	/**
	 * Apply geodesic reconstruction to mask image based on current operation
	 * and ROI
	 * @param mask mask image
	 * @param roi region of interest to create marker image
	 * @return reconstructed image
	 */
	ImagePlus process( ImagePlus mask, Roi roi )
	{
		if( mask == null )
		{
			IJ.showMessage( "Please run the plugin with an image open." );
			return null;
		}
		if( roi == null || ! (roi instanceof PointRoi) )
		{
			IJ.showMessage( "Please define the markers using "
					+ "the point selection tool." );
			return null;
		}
		// Create marker image from ROI
		int[] xpoints = roi.getPolygon().xpoints;
		int[] ypoints = roi.getPolygon().ypoints;
		ImageStack markerStack =
				new ImageStack( mask.getWidth(), mask.getHeight() );
		for( int n=0; n<mask.getImageStackSize(); n++ )
		{
			ByteProcessor markerSlice =
					new ByteProcessor( mask.getWidth(), mask.getHeight() );
			markerSlice.setColor( java.awt.Color.WHITE );
			for( int i=0; i<xpoints.length; i++ )
			{
				int slice = ((PointRoi) roi).getPointPosition( i );
				if ( slice == n )
				{
					markerSlice.draw( new PointRoi( xpoints[i], ypoints[i] ));
				}
			}

			markerStack.addSlice( markerSlice );
		}

		// Compute geodesic reconstruction
		ImageStack result =
				operation.applyTo( markerStack, mask.getImageStack(),
						connectivity );
		// Keep same color model
		result.setColorModel( mask.getImageStack().getColorModel() );

		// create resulting image
		String newName = mask.getShortTitle() + "-geodRec";
		ImagePlus resultPlus = new ImagePlus( newName, result );
		resultPlus.copyScale( mask );
		resultPlus.show();

		resultPlus.setSlice( mask.getSlice() );
		return resultPlus;
	}
}
