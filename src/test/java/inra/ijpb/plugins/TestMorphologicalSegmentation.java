package inra.ijpb.plugins;

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
		new MorphologicalSegmentation().run( null );
	}

}
