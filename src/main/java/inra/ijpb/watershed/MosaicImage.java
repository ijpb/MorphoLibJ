package inra.ijpb.watershed;

import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import inra.ijpb.measure.IntensityMeasures;
import inra.ijpb.morphology.LabelImages;
import inra.ijpb.morphology.Morphology;
import inra.ijpb.morphology.Strel;

public class MosaicImage {

	/**
	 * Compute mosaic image: from the initial watershed transformation of the
	 * gradient of the input image, substitute each catchment basin by the
	 * average value of the region in the original image.
	 * Reference:
	 *   [1] Beucher, Serge. "Watershed, hierarchical segmentation and waterfall
	 *       algorithm." Mathematical morphology and its applications to image 
	 *       processing. Springer Netherlands, 1994. 69-76.
	 * @param input grayscale input image
	 * @return 
	 */
	public static ImageProcessor compute( ImageProcessor input )
	{		
		// calculate gradient image
		final int gradientRadius = 1;
		Strel strel = Strel.Shape.SQUARE.fromRadius( gradientRadius );
		ImageProcessor gradientImage = Morphology.gradient( input, strel );
		
		// apply classic watershed algorithm
		int connectivity = 4;
		ImageProcessor watershedImage = 
				Watershed.computeWatershed( gradientImage, null, connectivity );
		
		// calculate mean value of each labeled region
		IntensityMeasures im = new IntensityMeasures( 
				new ImagePlus( "input", input ), 
				new ImagePlus( "label", watershedImage ) );
		ResultsTable table = im.getMean();
		

		// extract array of numerical values
		int index = table.getColumnIndex( IntensityMeasures.meanHeaderName );
		double[] values = table.getColumnAsDoubles( index );
		
		// apply mean values to each region
		return LabelImages.applyLut( watershedImage, values );		
	}
	
	
}
