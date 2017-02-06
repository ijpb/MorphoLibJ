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
import ij.plugin.frame.RoiManager;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.data.image.Images3D;
import inra.ijpb.util.IJUtils;
import inra.ijpb.watershed.Watershed;

/**
 * Plugin for performing interactive marker-controlled watershed on 3D
 * grayscale images.
 *
 * In contrast with the non-interactive plugin, the active image is considered
 * the input image, while the marker image is constructed by user-defined
 * point ROIs. The marker image is used to initialize the seeds, and
 * the mask image is used to constrain it.
 * The connectivity and the use of dams can also be specified.
 *
 * @author Ignacio Arganda-Carreras (ignacio.arganda@ehu.eus)
 */
public class InteractiveMarkerControlledWatershed3D implements PlugIn
{
	/** flag to calculate watershed dams */
	public static boolean getDams = true;

	private static Conn3D connectivity = Conn3D.C6;

	private NonBlockingGenericDialog gd;


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
			IJ.error( "Interactive Marker-controlled Watershed 3D",
					"ERROR: At least one 3D image need to be open to run"
					+ " Interactive Marker-controlled Watershed.");
			return;
		}
		
		// select point tool for manual introduction of markers
		Toolbar.getInstance().setTool( Toolbar.POINT );

		int nbima = WindowManager.getImageCount();
        String[] namesMask = new String[ nbima + 1 ];
        namesMask[ 0 ] = "None";

        for (int i = 0; i < nbima; i++)
        	namesMask[ i + 1 ] = WindowManager.getImage(i + 1).getShortTitle();
		// create the dialog
		gd = new NonBlockingGenericDialog( "Interactive Marker-controlled "
				+ "Watershed 3D" );
		gd.addChoice( "Mask", namesMask, namesMask[ 0 ] );
		gd.addCheckbox( "Calculate dams", getDams );
		gd.addChoice("Connectivity",
				Conn3D.getAllLabels(),
				connectivity.label );
		gd.addHelp( "http://imagej.net/MorphoLibJ" );
		gd.showDialog();

		if ( gd.wasCanceled() )
			return;

		// set up current parameters
		int maskIndex = gd.getNextChoiceIndex();
		getDams = gd.getNextBoolean();
		connectivity = Conn3D.fromLabel(gd.getNextChoice());
		final ImagePlus maskImage = maskIndex > 0 ?
				WindowManager.getImage( maskIndex ) : null;
		
		long t0 = System.currentTimeMillis();

		// Compute marker-controlled watershed
		final ImagePlus result = process( image, maskImage, image.getRoi() );

		if ( null == result )
			return;

		Images3D.optimizeDisplayRange( result );

		// Display the result image
		result.show();
		result.setSlice( image.getSlice() );

		long t1 = System.currentTimeMillis();
		IJUtils.showElapsedTime( "Marker-controlled watershed 3D", t1 - t0,
				image );
	}
	/**
	 * Apply marker-controlled watershed segmentation
	 * @param input image to apply watershed segmentation on
	 * @param mask image to constraint segmentation (can be null)
	 * @param roi region of interest to create marker image
	 * @return reconstructed image
	 */
	ImagePlus process( ImagePlus input, ImagePlus mask, Roi roi )
	{
		if( input == null )
		{
			IJ.showMessage( "Please run the plugin with an image open." );
			return null;
		}
		if( roi == null )
		{
			IJ.showMessage( "Please define the markers using for example "
					+ "the point selection tool." );
			return null;
		}
		// create marker image from ROIs with the same 32-bit
		final double maxValue = Float.MAX_VALUE;

		ImageStack markerStack =
				new ImageStack( input.getWidth(), input.getHeight() );
		ImageProcessor[] markerSlice =
				new ImageProcessor[ input.getImageStackSize() ];
		for( int n=0; n<input.getImageStackSize(); n++ )
		{
			markerSlice[ n ] =
					new FloatProcessor( input.getWidth(), input.getHeight() );			

			markerSlice[ n ].setValue( maxValue );
			markerSlice[ n ].setColor( maxValue );
		}
		// if input ROI is a point or multi-point ROI
		if( roi instanceof PointRoi )
		{
			int[] xpoints = roi.getPolygon().xpoints;
			int[] ypoints = roi.getPolygon().ypoints;

			for( int i=0; i<xpoints.length; i++ )
			{
				markerSlice[ ((PointRoi) roi).getPointPosition( i ) -1 ].draw(
						new PointRoi( xpoints[i], ypoints[i] ) );
			}
		}
		// if not and ROI manager is open, read ROIs from ROI manager
		else if ( null != RoiManager.getInstance() )
		{
			RoiManager manager = RoiManager.getInstance();
			int[] selected = manager.getSelectedIndexes();
			if( selected.length > 0 )
			{
				for( int i=0; i<selected.length; i++ )
				{
					final Roi selectedRoi = manager.getRoi( i );
					int slice =
							manager.getSliceNumber( manager.getName( i ) );
					if( selectedRoi.isArea() )
						markerSlice[ slice-1 ].fill( selectedRoi );
					else
						markerSlice[ slice-1 ].draw( selectedRoi );
				}
			}
			else
			{
				IJ.error( "Please select the ROIs you want to use"
						+ " as markers in the ROI manager." );
				return null;
			}
		}
		// otherwise paint ROI on the slice currently selected
		// on the mask image
		else
		{
			int slice = input.getSlice();

			if( roi.isArea() )
				markerSlice[ slice-1 ].fill( roi );
			else
				markerSlice[ slice-1 ].draw( roi );
		}

		// add slices to stack
		for( int n=0; n<input.getImageStackSize(); n++ )
		{
			markerStack.addSlice( markerSlice[n] );
		}

		markerStack = BinaryImages.componentsLabeling(
				markerStack, connectivity.value, 32 );
		ImagePlus marker = new ImagePlus( "marker", markerStack );
		return Watershed.computeWatershed( input, marker, mask,
				connectivity.value, getDams, false );
	}
}
