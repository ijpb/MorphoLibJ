package inra.ijpb.watershed;

import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import inra.ijpb.binary.ConnectedComponents;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.IntensityMeasures;
import inra.ijpb.morphology.MinimaAndMaxima;
import inra.ijpb.morphology.MinimaAndMaxima3D;
import inra.ijpb.morphology.Morphology;
import inra.ijpb.morphology.Strel;
import inra.ijpb.morphology.Strel3D;

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
	 * @return mosaic image
	 */
	public static ImageProcessor compute( ImageProcessor input )
	{		
		// calculate gradient image
		final int gradientRadius = 1;
		Strel strel = Strel.Shape.SQUARE.fromRadius( gradientRadius );
		ImageProcessor gradientImage = Morphology.gradient( input, strel );
		
		// apply classic Meyer's watershed algorithm
		int connectivity = 4;
		boolean calculateDams = true;
		
		ImageProcessor minima = 
				MinimaAndMaxima.regionalMinima( gradientImage, connectivity );
		ImageProcessor labeledMinima = 
				ConnectedComponents.computeLabels( minima, connectivity, 32 );
		ImageProcessor watershedImage = 
				Watershed.computeWatershed( gradientImage, 
						labeledMinima, connectivity, calculateDams );
		
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
	
	/**
	 * Compute mosaic image: from the initial watershed transformation of the
	 * gradient of the input image, substitute each catchment basin by the
	 * average value of the region in the original image.
	 * Reference:
	 *   [1] Beucher, Serge. "Watershed, hierarchical segmentation and waterfall
	 *       algorithm." Mathematical morphology and its applications to image 
	 *       processing. Springer Netherlands, 1994. 69-76.
	 * @param input grayscale input image
	 * @param strel structural element to calculate gradient
	 * @param connectivity pixel connectivity
	 * @param calculateDams flag to calculate watershed lines
	 * @return mosaic image
	 */
	public static ImageProcessor compute( 
			ImageProcessor input, 
			Strel strel,
			int connectivity,
			boolean calculateDams )
	{		
		// calculate gradient image
		ImageProcessor gradientImage = Morphology.gradient( input, strel );
		
		// apply classic watershed algorithm
		ImageProcessor minima = 
				MinimaAndMaxima.regionalMinima( gradientImage, connectivity );
		ImageProcessor labeledMinima = 
				ConnectedComponents.computeLabels( minima, connectivity, 32 );
		ImageProcessor watershedImage = 
				Watershed.computeWatershed( gradientImage, 
						labeledMinima, connectivity, calculateDams );
		
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
	
	/**
	 * Compute mosaic image: from the initial watershed transformation of the
	 * gradient of the input image, substitute each catchment basin by the
	 * average value of the region in the original image.
	 * Reference:
	 *   [1] Beucher, Serge. "Watershed, hierarchical segmentation and waterfall
	 *       algorithm." Mathematical morphology and its applications to image 
	 *       processing. Springer Netherlands, 1994. 69-76.
	 * @param input grayscale input image
	 * @return mosaic image
	 */
	public static ImageStack compute( ImageStack input )
	{		
		// calculate gradient image
		final int gradientRadius = 1;
		Strel3D strel = Strel3D.Shape.CUBE.fromRadius( gradientRadius );
		ImageStack gradientImage = Morphology.gradient( input, strel );
		
		// apply classic watershed algorithm
		int connectivity = 6;
		boolean calculateDams = true;
		
		ImageStack minima = 
				MinimaAndMaxima3D.regionalMinima( gradientImage, connectivity );
		ImageStack labeledMinima = 
				ConnectedComponents.computeLabels( minima, connectivity, 32 );
		ImageStack watershedImage = 
				Watershed.computeWatershed( gradientImage, 
						labeledMinima, connectivity, calculateDams );
		
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
	
	/**
	 * Compute mosaic image: from the initial watershed transformation of the
	 * gradient of the input image, substitute each catchment basin by the
	 * average value of the region in the original image.
	 * Reference:
	 *   [1] Beucher, Serge. "Watershed, hierarchical segmentation and waterfall
	 *       algorithm." Mathematical morphology and its applications to image 
	 *       processing. Springer Netherlands, 1994. 69-76.
	 * @param input grayscale input image
	 * @param strel structural element to calculate gradient
	 * @param connectivity pixel connectivity
	 * @param calculateDams flag to calculate watershed lines
	 * @return mosaic image
	 */
	public static ImageStack compute( 
			ImageStack input,
			Strel3D strel, 
			int connectivity,
			boolean calculateDams)
	{		
		// calculate gradient image
		ImageStack gradientImage = Morphology.gradient( input, strel );
		
		// apply classic watershed algorithm
		ImageStack minima = 
				MinimaAndMaxima3D.regionalMinima( gradientImage, connectivity );
		ImageStack labeledMinima = 
				ConnectedComponents.computeLabels( minima, connectivity, 32 );
		ImageStack watershedImage = 
				Watershed.computeWatershed( gradientImage, 
						labeledMinima, connectivity, calculateDams );
		
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
	
	/**
	 * Compute mosaic image: from the initial watershed transformation of the
	 * gradient of the input image, substitute each catchment basin by the
	 * average value of the region in the original image.
	 * Reference:
	 *   [1] Beucher, Serge. "Watershed, hierarchical segmentation and waterfall
	 *       algorithm." Mathematical morphology and its applications to image 
	 *       processing. Springer Netherlands, 1994. 69-76.
	 * @param input grayscale input image
	 * @return mosaic image
	 */
	public static ImagePlus compute( ImagePlus input )
	{		
		// calculate gradient image
		final int gradientRadius = 1;
		Strel3D strel = Strel3D.Shape.CUBE.fromRadius( gradientRadius );
		ImageStack gradientImage = 
				Morphology.gradient( input.getImageStack(), strel );
		
		// apply classic watershed algorithm
		int connectivity = 6;
		boolean calculateDams = true;
		
		ImageStack minima = 
				MinimaAndMaxima3D.regionalMinima( gradientImage, connectivity );
		ImageStack labeledMinima = 
				ConnectedComponents.computeLabels( minima, connectivity, 32 );
		ImageStack watershedImage = 
				Watershed.computeWatershed( gradientImage, 
						labeledMinima, connectivity, calculateDams );
		
		// calculate mean value of each labeled region
		IntensityMeasures im = new IntensityMeasures( 
				input, 
				new ImagePlus( "label", watershedImage ) );
		ResultsTable table = im.getMean();		

		// extract array of numerical values
		int index = table.getColumnIndex( IntensityMeasures.meanHeaderName );
		double[] values = table.getColumnAsDoubles( index );
		
		// apply mean values to each region
		ImagePlus mosaicImage = new ImagePlus( input.getShortTitle() + "-mosaic",
				LabelImages.applyLut( watershedImage, values ) );
		mosaicImage.setCalibration( input.getCalibration() );
		return mosaicImage;
	}
	
	/**
	 * Compute mosaic image: from the initial watershed transformation of the
	 * gradient of the input image, substitute each catchment basin by the
	 * average value of the region in the original image.
	 * Reference:
	 *   [1] Beucher, Serge. "Watershed, hierarchical segmentation and waterfall
	 *       algorithm." Mathematical morphology and its applications to image 
	 *       processing. Springer Netherlands, 1994. 69-76.
	 * @param input grayscale input image
	 * @param strel structural element to calculate gradient
	 * @param connectivity pixel connectivity
	 * @param calculateDams flag to calculate watershed lines
	 * @return mosaic image
	 */
	public static ImagePlus compute( 
			ImagePlus input,
			Strel3D strel,
			int connectivity,
			boolean calculateDams )
	{		
		// calculate gradient image
		ImageStack gradientImage = 
				Morphology.gradient( input.getImageStack(), strel );
		
		// apply classic watershed algorithm
		ImageStack minima = 
				MinimaAndMaxima3D.regionalMinima( gradientImage, connectivity );
		ImageStack labeledMinima = 
				ConnectedComponents.computeLabels( minima, connectivity, 32 );
		ImageStack watershedImage = 
				Watershed.computeWatershed( gradientImage, 
						labeledMinima, connectivity, calculateDams );
		
		// calculate mean value of each labeled region
		IntensityMeasures im = new IntensityMeasures( 
				input, 
				new ImagePlus( "label", watershedImage ) );
		ResultsTable table = im.getMean();		

		// extract array of numerical values
		int index = table.getColumnIndex( IntensityMeasures.meanHeaderName );
		double[] values = table.getColumnAsDoubles( index );
		
		// apply mean values to each region
		ImagePlus mosaicImage = new ImagePlus( input.getShortTitle() + "-mosaic",
				LabelImages.applyLut( watershedImage, values ) );
		mosaicImage.setCalibration( input.getCalibration() );
		return mosaicImage;
	}
}
