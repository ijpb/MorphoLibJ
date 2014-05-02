package inra.ijpb.plugins;

import ij.IJ;
import ij.ImageJ;

public class TestMorphologicalSegmentation {
	
	/**
	 * Main method to test and debug the Morphological
	 * Segmentation GUI
	 *  
	 * @param args
	 */
	public static void main( final String[] args )
	{
		ImageJ.main( args );
		
		IJ.open( TestMorphologicalSegmentation.class.getResource( "/files/grains.tif" ).getFile() );
		
		new MorphologicalSegmentation().run( null );
	}

}
