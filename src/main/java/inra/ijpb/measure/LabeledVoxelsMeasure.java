package inra.ijpb.measure;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.LabelImages;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Mother class to extract measures from pairs of grayscale and 
 * labeled images.
 * @author Ignacio Arganda-Carreras
 *
 */
public class LabeledVoxelsMeasure {

	/** list of voxels grouped by label */
	ArrayList<Double>[] objectVoxels;
	/** list of unique labels */
	int[] labels;
	/** calibration of input image */
	Calibration calibration;
	
	/**
	 * Initialize the measurements by reading the input (grayscale) 
	 * image and its corresponding labels.
	 * 
	 * @param inputImage input (grayscale) image
	 * @param labelImage label image (labels are positive integer values)
	 */
	public LabeledVoxelsMeasure(
			ImagePlus inputImage,
			ImagePlus labelImage )
	{
		final int width = inputImage.getWidth();
		final int height = inputImage.getHeight();
		
		if( width != labelImage.getWidth() || height != labelImage.getHeight() )
			throw new IllegalArgumentException("Input and label images must have the same size");
		
		this.calibration = inputImage.getCalibration();

		this.labels = LabelImages.findAllLabels( labelImage.getImageStack() );
		int numLabels = labels.length;
		
		// create associative hash table to know the index of each label
		HashMap<Integer, Integer> labelIndices = new HashMap<Integer, Integer>();
        for (int i = 0; i < numLabels; i++) {
        	labelIndices.put( labels[i], i );
        }

		// initialize lists of voxels per object
		objectVoxels = new ArrayList[ numLabels ];

		for( int i=0; i<numLabels; i++ )
			objectVoxels[ i ] = new ArrayList<Double>();
		
		//final long start = System.currentTimeMillis();		
		
		IJ.showStatus( "Extracting voxel information..." );
		
		// read voxel intensities for each object
		final int numSlices = inputImage.getImageStackSize();
		for( int z=1; z <= numSlices; z++ )
		{
			final ImageProcessor grayIP = inputImage.getImageStack().getProcessor( z );
			final ImageProcessor labelsIP = labelImage.getImageStack().getProcessor( z );

			for( int x = 0; x<width; x++ )
				for( int y = 0; y<height; y++ )
				{
					int labelValue = (int) labelsIP.getf( x, y );
					if( labelValue != 0)
						objectVoxels[ labelIndices.get( labelValue ) ].add( (double) grayIP.getf(x, y) );
				}
			
			IJ.showProgress( z, numSlices );
		}
		
		IJ.showProgress( 1.0 );
		
		//final long end = System.currentTimeMillis();
		//IJ.log("Reading voxels took " + (end-start) + " ms.");
	}
	
	/**
	 * Get number of voxels per label
	 * @return number of voxels per label
	 */
	public ResultsTable getNumberOfVoxels()
	{
		final int numLabels = objectVoxels.length;
				
		// create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < numLabels; i++) {
			table.incrementCounter();
			table.addLabel(Integer.toString( labels[i] ));
			table.addValue("NumberOfVoxels", objectVoxels[ i ].size() );
		}

		return table;
	}
	
	/**
	 * Get volume per labeled object based on the input
	 * image calibration
	 * 
	 * @return volume per labeled object
	 */
	public ResultsTable getVolume()
	{
		final int numLabels = objectVoxels.length;
		
		double volumePerVoxel = calibration.pixelWidth * calibration.pixelHeight * calibration.pixelDepth;
		
		// create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < numLabels; i++) {
			table.incrementCounter();
			table.addLabel(Integer.toString( labels[i] ));
			table.addValue( "Volume", objectVoxels[ i ].size() * volumePerVoxel );
		}

		return table;
	}
	
	
	
}
