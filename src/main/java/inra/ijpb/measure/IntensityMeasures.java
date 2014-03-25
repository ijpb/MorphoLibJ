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
	 * Get mean voxel values per label
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
			for( final double v : objectVoxels[ i ] )
				mean[ i ] += v;
			mean[ i ] /= objectVoxels[ i ].size();			
		}
		
		
		// create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < numLabels; i++) {
			table.incrementCounter();
			table.addLabel(Integer.toString( labels[i] ));
			table.addValue("Mean", mean[i]);
		}

		return table;
	}
	
	/**
	 * Get standard deviation of voxel values per label
	 * 
	 * @return result table with standard deviation values per label
	 */
	public ResultsTable getStdDev()
	{
		final int numLabels = objectVoxels.length;
		
		double[] mean = new double[ numLabels ];
		double[] sd = new double[ numLabels ];
		
		// calculate mean voxel value per object
		for( int i=0; i<numLabels; i++ )
		{
			mean[ i ] = 0;
			for( final double v : objectVoxels[ i ] )
				mean[ i ] += v;
			mean[ i ] /= objectVoxels[ i ].size();			
		}
		
		// calculate standard deviation
		for( int i=0; i<numLabels; i++ )
		{
			sd[ i ] = 0;
			for( final double v : objectVoxels[ i ] )
				sd[ i ] += ( v - mean[ i ] ) * ( v - mean[ i ] );
			sd[ i ] /= objectVoxels[ i ].size();	
			sd[ i ] = Math.sqrt( sd[ i ] );
		}
		
		// create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < numLabels; i++) {
			table.incrementCounter();
			table.addLabel(Integer.toString( labels[i] ));
			table.addValue("StdDev", sd[i]);
		}

		return table;
	}
	
	/**
	 * Get maximum voxel values per label
	 * 
	 * @return result table with maximum values per label
	 */
	public ResultsTable getMax()
	{
		final int numLabels = objectVoxels.length;
		
		double[] max = new double[ numLabels ];
		
		// calculate maximum voxel value per object
		for( int i=0; i<numLabels; i++ )
		{
			max[ i ] = Double.MIN_VALUE;
			for( final double v : objectVoxels[ i ] )
				if( v > max[ i ] )
					max[ i ] = v;
		}
		
		
		// create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < numLabels; i++) {
			table.incrementCounter();
			table.addLabel(Integer.toString( labels[i] ));
			table.addValue("Max", max[i]);
		}

		return table;
	}
	
	/**
	 * Get minimum voxel values per label
	 * 
	 * @return result table with minimum values per label
	 */
	public ResultsTable getMin()
	{
		final int numLabels = objectVoxels.length;
		
		double[] min = new double[ numLabels ];
		
		// calculate minimum voxel value per object
		for( int i=0; i<numLabels; i++ )
		{
			min[ i ] = Double.MAX_VALUE;
			for( final double v : objectVoxels[ i ] )
				if( v < min[ i ] )
					min[ i ] = v;
		}
		
		
		// create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < numLabels; i++) {
			table.incrementCounter();
			table.addLabel(Integer.toString( labels[i] ));
			table.addValue("Min", min[i]);
		}

		return table;
	}
	
}
