package inra.ijpb.plugins;

import ij.IJ;
import ij.ImageJ;

/**
 * Class to test the Label Edition plugin.
 * @author iarganda
 *
 */
public class TestLabelEdition {

	/**
	 * Main method to test and debug the Label Edition plugin
	 *
	 * @param args
	 */
	public static void main( final String[] args )
	{
		ImageJ.main( args );
		IJ.open( MorphologicalSegmentationTest.class.getResource( "/files/grains-med-WTH-lbl.tif" ).getFile() );
		new LabelEdition().run( null );
	}
}
