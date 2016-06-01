package inra.ijpb.measure;

import java.util.Collections;
import java.util.HashMap;

import ij.ImagePlus;
import ij.measure.ResultsTable;


/**
 * Class to facilitate the calculation of intensity measures by
 * grouping together voxels belonging to the same label.
 * 
 * @author Ignacio Arganda-Carreras
 *
 */
public class IntensityMeasures extends LabeledVoxelsMeasure{

	
	
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
		super( inputImage, labelImage );
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
	 * Get median voxel values per label
	 *
	 * @return result table with median values per label
	 */
	public ResultsTable getMedian()
	{
		final int numLabels = objectVoxels.length;

		double[] median = new double[ numLabels ];

		// calculate median voxel value per object
		for( int i=0; i<numLabels; i++ )
		{
			Collections.sort( objectVoxels[ i ] );
			median[ i ] = objectVoxels[ i ].get( objectVoxels[ i ].size() / 2 );
		}


		// create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < numLabels; i++) {
			table.incrementCounter();
			table.addLabel( Integer.toString( labels[i] ));
			table.addValue( "Median", median[i] );
		}

		return table;
	}

	/**
	 * Get mode voxel values per label
	 *
	 * @return result table with median values per label
	 */
	public ResultsTable getMode()
	{
		final int numLabels = objectVoxels.length;

		double[] mode = new double[ numLabels ];

		// calculate mode voxel value per object
		for( int i=0; i<numLabels; i++ )
		{
			HashMap<Double,Integer> hm = new HashMap< Double,Integer >();
			int max = 1;
			double temp = objectVoxels[ i ].get( 0 );

			for(int j=0; j < objectVoxels[ i ].size(); j++ )
			{
				double val = objectVoxels[ i ].get( j );
				if( hm.get( val ) != null )
				{
					int count = hm.get( val );
					count++;
					hm.put( val, count );
					if( count>max )
					{
						max = count;
						temp = val;
					}
				}
				else
					hm.put( val, 1 );
			}
			mode[ i ] = temp;
		}

		// create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < numLabels; i++) {
			table.incrementCounter();
			table.addLabel( Integer.toString( labels[i] ));
			table.addValue( "Mode", mode[i] );
		}

		return table;
	}

	/**
	 * Get skewness voxel values per label
	 *
	 * @return result table with median values per label
	 */
	public ResultsTable getSkewness()
	{
		final int numLabels = objectVoxels.length;

		double[] skewness = new double[ numLabels ];

		// calculate skewness voxel value per object
		for( int i=0; i<numLabels; i++ )
		{
			final double voxelCount = objectVoxels[ i ].size();
			double v, v2, sum2 = 0, sum3 = 0;
			double mean = 0;
			for( int j=0; j<voxelCount; j++ )
			{
				mean += objectVoxels[ i ].get( j );
				v = objectVoxels[ i ].get( j ) + Double.MIN_VALUE;
				v2 = v * v;
				sum2 += v2;
				sum3 += v * v2;
			}
			mean /= voxelCount;
			double mean2 = mean*mean;
			double variance = sum2 / voxelCount - mean2;
			double sDeviation = Math.sqrt( variance );
			skewness[ i ] = ((sum3 - 3.0 * mean * sum2 ) / voxelCount
					+ 2.0 * mean * mean2 ) / ( variance * sDeviation );
		}

		// create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < numLabels; i++) {
			table.incrementCounter();
			table.addLabel( Integer.toString( labels[i] ));
			table.addValue( "Skewness", skewness[i] );
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
