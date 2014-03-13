package inra.ijpb.measure;

import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;

/**
 * Class to facilitate the calculation of intensity measures by
 * grouping together voxels belonging to the same label.
 * 
 * @author Ignacio Arganda-Carreras
 *
 */
public class IntensityMeasures {

	ArrayList<Double>[] objectVoxels;
	int[] labels;
	
	/**
	 * Initialize the measurements by reading the input (grayscale) 
	 * image and its corresponding labels.
	 * 
	 * @param inputImage input (grayscale) image
	 * @param labelImage label image (labels are positive integer values)
	 */
	public IntensityMeasures(
			ImagePlus inputImage,
			ImagePlus labelImage )
	{
		final int width = inputImage.getWidth();
		final int height = inputImage.getHeight();

		this.labels = GeometricMeasures3D.findAllLabels( labelImage.getImageStack() );
		int numLabels = labels.length;

		// initialize lists of voxels per object
		objectVoxels = new ArrayList[ numLabels ];

		for( int i=0; i<numLabels; i++ )
			objectVoxels[ i ] = new ArrayList<Double>();
		final long start = System.currentTimeMillis();		
		
		// read voxel intensities for each object
		for( int z=1; z <= inputImage.getImageStackSize(); z++ )
		{
			final ImageProcessor grayIP = inputImage.getImageStack().getProcessor( z );
			final ImageProcessor labelsIP = labelImage.getImageStack().getProcessor( z );

			for( int x = 0; x<width; x++ )
				for( int y = 0; y<height; y++ )
					objectVoxels[(int) labelsIP.getf(x, y) ].add( (double) grayIP.getf(x, y) );
		}
		final long end = System.currentTimeMillis();
		IJ.log("Reading voxels took " + (end-start) + " ms.");
	}
	
	/**
	 * Get mean voxel value per label
	 * 
	 * @return result table with mean values per label
	 */
	public ResultsTable getMean()
	{
		final int numLabels = objectVoxels.length;
		
		double[] mean = new double[ numLabels ];
		
		// calculate mean voxel value per object
		for( int i=0; i<numLabels; i++ )
		{
			mean[ i ] = 0;
			for( double v : objectVoxels[ i ] )
				mean[ i ] += v;
			mean[ i ] /= objectVoxels[ i ].size();			
		}
		
		
		// create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < numLabels; i++) {
			table.incrementCounter();
			table.addLabel(Integer.toString( labels[i] ));
			table.addValue("Surface", mean[i]);
		}

		return table;
	}
	
}
