/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
package inra.ijpb.measure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import inra.ijpb.label.LabelImages;
import inra.ijpb.label.RegionAdjacencyGraph;
import inra.ijpb.label.RegionAdjacencyGraph.LabelPair;


/**
 * Class to facilitate the calculation of intensity measures by
 * grouping together voxels belonging to the same label.
 * 
 * @author Ignacio Arganda-Carreras
 *
 */
public class IntensityMeasures extends LabeledVoxelsMeasure{
    /** adjacency list */
    Set<LabelPair> adjList = null;
    /** list of adjacent pixels/voxels per label */
    ArrayList<Double>[] neighborVoxels = null;
    /**
     * intensity mode per region labeled and number of pixels/voxels with that
     * intensity
     */
    double[][] mode = null;
    /** maximum intensity value per labeled region */
    double[] max = null;
    /** minimum intensity value per labeled region */
    double[] min = null;
    /** mean intensity value per labeled region */
    double[] mean = null;
    /** neighbors mean intensity value per labeled region */
    double[] neighborsMean = null;
    /** intensity histogram per label */
    HashMap<Double, Integer>[] histogramPerLabel = null;
    /** label image */
    ImagePlus labelImage = null;
    
	/**
	 * Initialize the measurements by reading the input (intensity) image and
	 * its corresponding labels.
	 * 
	 * @param inputImage
	 *            the input image containing intensities
	 * @param labelImage
	 *            the label map, containing for each pixel or voxel the label of
	 *            the region it belongs to, or zero if it belongs to background.
	 */
	public IntensityMeasures(
			ImagePlus inputImage,
			ImagePlus labelImage )
	{
		super( inputImage, labelImage );
		this.labelImage = labelImage;
	}
	
	/**
	 * Get mean voxel values per labeled region.
	 * 
	 * @return result table with mean values per label
	 */
	public ResultsTable getMean()
	{
		// Calculate man intensity per label
		this.mean = meanPerLabel();

		// create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < mean.length; i++) {
			table.incrementCounter();
			table.addLabel(Integer.toString( labels[i] ));
			table.addValue("Mean", mean[i]);
		}

		return table;
	}
	
	/**
	 * Get the mean intensity value per labeled region.
	 * @return mean intensity value per labeled region
	 */
	private double[] meanPerLabel()
	{
		double[] mean = new double[ objectVoxels.length ];
		
		// calculate mean voxel value per object
		for( int i=0; i<mean.length; i++ )
		{
			mean[ i ] = 0;
			for( final double v : objectVoxels[ i ] )
				mean[ i ] += v;
			mean[ i ] /= objectVoxels[ i ].size();			
		}
		return mean;
	}
	
	/**
	 * Get the mean intensity values of the neighbor labels
	 *
	 * @return result table with mean values of neighbor labels
	 */
	public ResultsTable getNeighborsMean()
	{
		this.neighborsMean = neighborsMeanPerLabel();

		// create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < neighborsMean.length; i++) {
			table.incrementCounter();
			table.addLabel(Integer.toString( labels[i] ));
			table.addValue("NeighborsMean", neighborsMean[i]);
		}

		return table;
	}
	
	/**
	 * Get the neighbors mean intensity value per labeled region.
	 * @return neighbors mean intensity value per labeled region
	 */
	private double[] neighborsMeanPerLabel()
	{
		// check if adjacency list has already been calculated
		if( this.adjList == null )
			this.adjList = RegionAdjacencyGraph.computeAdjacencies( labelImage );
		// check if mean per label exists
		if( null == this.mean )
			this.mean = meanPerLabel();

		final int numLabels = objectVoxels.length;

		double[] neighborsMean = new double[ numLabels ];
		double[] neighborsNumVox = new double[ numLabels ];

		// merge histograms of adjacent labels
		for( LabelPair pair : adjList )
		{
			// extract their indices
			int ind1 = super.labelIndices.get( pair.label1 );
			int ind2 = super.labelIndices.get( pair.label2 );
			// sum up weighted mean values
			neighborsMean[ ind1 ] += mean[ ind2 ] * objectVoxels[ ind2 ].size();
			neighborsMean[ ind2 ] += mean[ ind1 ] * objectVoxels[ ind1 ].size();
			// store number of neighbor voxels
			neighborsNumVox[ ind1 ] += objectVoxels[ ind2 ].size();
			neighborsNumVox[ ind2 ] += objectVoxels[ ind1 ].size();
		}

		// go through list of labels and divide by the total number of neighbor voxels
		for( int i = 0; i < numLabels; i++ )
		{
			if( neighborsNumVox[ i ] > 0)
				neighborsMean[ i ] /= neighborsNumVox[ i ];
			else
				neighborsMean[ i ] = Double.NaN;
		}
		return neighborsMean;
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
	 * Get the median intensity values of the neighbor labels
	 *
	 * @return result table with median values of neighbor labels
	 */
	public ResultsTable getNeighborsMedian()
	{
		if( this.neighborVoxels == null )
			this.neighborVoxels = computeNeighborVoxels();

		final int numLabels = objectVoxels.length;
		double[] median = new double[ numLabels ];

		// calculate median value
		for( int i=0; i<numLabels; i++ )
		{
			Collections.sort( neighborVoxels[ i ] );
			if( neighborVoxels[ i ].size() > 0)
				median[ i ] = neighborVoxels[ i ].get( neighborVoxels[ i ].size() / 2 );
			else
				median[ i ] = Double.NaN;
		}

		// create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < numLabels; i++) {
			table.incrementCounter();
			table.addLabel(Integer.toString( labels[i] ));
			table.addValue("NeighborsMedian", median[i]);
		}

		return table;
	}
	
	/**
	 * Get mode voxel values per label
	 *
	 * @return result table with mode values per label
	 */
	public ResultsTable getMode()
	{
		final int numLabels = objectVoxels.length;
		double[] mode = new double[ numLabels ];

		// calculate histogram of each label
		if( null == histogramPerLabel )
			this.histogramPerLabel = getHistogramPerLabel();

		// calculate mode voxel value per object
		for( int i=0; i<numLabels; i++ )
		{
			int max = 1;
			double temp = objectVoxels[ i ].get( 0 );

			for( HashMap.Entry<Double, Integer> entry : histogramPerLabel[ i ].entrySet() )
			{
				if( entry.getValue() > max )
				{
					max = entry.getValue();
					temp = entry.getKey();
				}
			}
			mode[ i ] = temp;
		}

		// create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < numLabels; i++) {
			table.incrementCounter();
			table.addLabel( Integer.toString( labels[i] ));
			table.addValue( "Mode", mode[ i ] );
		}

		return table;
	}
	
	/**
	 * Get the intensity histogram of each label.
	 * @return the intensity histogram of each label
	 */
	private HashMap<Double,Integer>[] getHistogramPerLabel()
	{
		@SuppressWarnings("unchecked")
		HashMap<Double,Integer>[] hm = new HashMap[ objectVoxels.length ];
		for( int i=0; i<objectVoxels.length; i++ )
		{
			hm[ i ] = new HashMap< Double,Integer >();

			for( double val : objectVoxels[ i ] )
			{
				if( hm[ i ].get( val ) != null )
				{
					int count = hm[ i ].get( val );
					count++;
					hm[ i ].put( val, count );
				}
				else
					hm[ i ].put( val, 1 );
			}
		}
		return hm;
	}
	
	/**
	 * Get the intensity mode value of the neighbor labels
	 *
	 * @return result table with intensity mode of neighbor labels
	 */
	public ResultsTable getNeighborsMode()
	{
		// check if the label histograms have been calculated
		if( null == histogramPerLabel )
			this.histogramPerLabel = getHistogramPerLabel();

		final int numLabels = objectVoxels.length;
		double[] mode = new double[ numLabels ];

		// check if adjacency list has already been calculated
		if( this.adjList == null )
			this.adjList = RegionAdjacencyGraph.computeAdjacencies( labelImage );

		@SuppressWarnings("unchecked")
		HashMap<Double,Integer>[] hm = new HashMap[ numLabels ];
		for( int i = 0; i < numLabels ; i++ )
			hm[ i ] = new HashMap< Double,Integer >();
		// merge histograms of adjacent labels
		for( LabelPair pair : adjList )
		{
			// extract their indices
			int ind1 = super.labelIndices.get( pair.label1 );
			int ind2 = super.labelIndices.get( pair.label2 );

			// add up ind1 histogram to ind2 hashmap
			for( HashMap.Entry<Double, Integer> entry : histogramPerLabel[ ind1 ].entrySet() )
			{
				double val = entry.getKey();
				int count = entry.getValue();
				if( hm[ ind2 ].get( val ) != null )
					hm[ ind2 ].put( val, count + hm[ ind2 ].get( val ) );
				else
					hm[ ind2 ].put( val, count );
			}
			// add up ind2 histogram to ind1 hashmap
			for( HashMap.Entry<Double, Integer> entry : histogramPerLabel[ ind2 ].entrySet() )
			{
				double val = entry.getKey();
				int count = entry.getValue();
				if( hm[ ind1 ].get( val ) != null )
					hm[ ind1 ].put( val, count + hm[ ind1 ].get( val ) );
				else
					hm[ ind1 ].put( val, count );
			}
		}

		// calculate mode value in merged histograms hashmap
		for( int i=0; i<numLabels; i++ )
		{
			int max = 1;
			double temp = Double.NaN;

			for( HashMap.Entry<Double, Integer> entry : hm[ i ].entrySet() )
			{
				if( entry.getValue() > max )
				{
					max = entry.getValue();
					temp = entry.getKey();
				}
			}
			mode[ i ] = temp;
		}

		// create data table
		ResultsTable table = new ResultsTable();
		for( int i = 0; i < numLabels; i++ )
		{
			table.incrementCounter();
			table.addLabel( Integer.toString( labels[i] ) );
			table.addValue( "NeighborsMode", mode[i] );
		}

		return table;
	}
	
	/**
	 * Get skewness voxel values per label
	 *
	 * @return result table with skewness values per label
	 */
	public ResultsTable getSkewness()
	{
		final int numLabels = objectVoxels.length;
		// check if the mean intensity per label has already
		// been calculated
		if( null == this.mean )
			this.mean = meanPerLabel();

		double[] skewness = new double[ numLabels ];

		// calculate skewness voxel value per object
		for( int i=0; i<numLabels; i++ )
		{
			final double voxelCount = objectVoxels[ i ].size();
			double v, v2, sum2 = 0, sum3 = 0;
			for( int j=0; j<voxelCount; j++ )
			{
				v = objectVoxels[ i ].get( j ) + Double.MIN_VALUE;
				v2 = v * v;
				sum2 += v2;
				sum3 += v * v2;
			}
			double mean2 = mean[ i ] * mean[ i ];
			double variance = sum2 / voxelCount - mean2;
			double sDeviation = Math.sqrt( variance );
			skewness[ i ] = Double.compare( variance, 0d ) == 0 ? 0 :
				((sum3 - 3.0 * mean[ i ] * sum2 ) / voxelCount
					+ 2.0 * mean[ i ] * mean2 ) / ( variance * sDeviation );
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
	 * Get the intensity skewness values of the neighbor labels
	 *
	 * @return result table with intensity skewness of neighbor labels
	 */
	public ResultsTable getNeighborsSkewness()
	{
		if( this.neighborVoxels == null )
			this.neighborVoxels = computeNeighborVoxels();

		final int numLabels = objectVoxels.length;
		double[] skewness = new double[ numLabels ];

		// calculate skewness value
		for( int i=0; i<numLabels; i++ )
		{
			final double voxelCount = neighborVoxels[ i ].size();
			if( voxelCount > 0 )
			{
				double v, v2, sum2 = 0, sum3 = 0;
				double mean = 0;
				for( int j=0; j<voxelCount; j++ )
				{
					mean += neighborVoxels[ i ].get( j );
					v = neighborVoxels[ i ].get( j ) + Double.MIN_VALUE;
					v2 = v * v;
					sum2 += v2;
					sum3 += v * v2;
				}
				mean /= voxelCount;
				double mean2 = mean*mean;
				double variance = sum2 / voxelCount - mean2;
				double sDeviation = Math.sqrt( variance );
				skewness[ i ] = Double.compare( variance, 0d ) == 0 ? 0 :
					((sum3 - 3.0 * mean * sum2 ) / voxelCount
						+ 2.0 * mean * mean2 ) / ( variance * sDeviation );
			}
			else
				skewness[ i ] = Double.NaN;
		}

		// create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < numLabels; i++)
		{
			table.incrementCounter();
			table.addLabel( Integer.toString( labels[ i ] ) );
			table.addValue( "NeighborsSkewness", skewness[ i ] );
		}

		return table;
	}
	
	/**
	 * Compute the lists of pixels/voxels belonging to each adjacent label region.
	 * @return array with lists of pixels/voxels belonging to each adjacent label region
	 */
	private ArrayList<Double>[] computeNeighborVoxels()
	{
		if( this.adjList == null )
			this.adjList = RegionAdjacencyGraph.computeAdjacencies( labelImage );
		
		int numLabels = super.labels.length;
		
        // create an array with as many elements as the number of labels
		@SuppressWarnings("unchecked")
		ArrayList<Double>[] neighborVoxels = new ArrayList[ numLabels ];
		for( int i = 0; i < neighborVoxels.length; i++ )
			neighborVoxels[ i ] = new ArrayList<Double>();
		
		// go through list of adjacent pairs
		for( LabelPair pair : adjList )
		{
			// extract their indices
			int ind1 = super.labelIndices.get( pair.label1 );
			int ind2 = super.labelIndices.get( pair.label2 );
			// concatenate lists of adjacent voxels
			neighborVoxels[ ind1 ].addAll( objectVoxels[ ind2 ] );
			neighborVoxels[ ind2 ].addAll( objectVoxels[ ind1 ] );
		}
		return neighborVoxels;
	}
	
	/**
	 * Get kurtosis voxel values per label
	 *
	 * @return result table with kurtosis values per label
	 */
	public ResultsTable getKurtosis()
	{
		final int numLabels = objectVoxels.length;
		// check if the mean intensity per label has already
		// been calculated
		if( null == this.mean )
			this.mean = meanPerLabel();

		double[] kurtosis = new double[ numLabels ];

		// calculate kurtosis voxel value per object
		for( int i=0; i<numLabels; i++ )
		{
			final double voxelCount = objectVoxels[ i ].size();
			double v, v2, sum2 = 0, sum3 = 0, sum4 = 0;
			for( int j=0; j<voxelCount; j++ )
			{
				v = objectVoxels[ i ].get( j ) + Double.MIN_VALUE;
				v2 = v * v;
				sum2 += v2;
				sum3 += v * v2;
				sum4 += v2 * v2;
			}

			double mean2 = mean[ i ] * mean[ i ];
			double variance = sum2 / voxelCount - mean2;
			kurtosis[ i ] = Double.compare( variance, 0d ) == 0 ? -6.0/5.0 :
				(((sum4 - 4.0 * mean[ i ] * sum3 + 6.0 * mean2 * sum2 )
					/ voxelCount - 3.0 * mean2 * mean2 )
					/ ( variance * variance ) -3.0 );
		}

		// create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < numLabels; i++) {
			table.incrementCounter();
			table.addLabel( Integer.toString( labels[ i ] ) );
			table.addValue( "Kurtosis", kurtosis[ i ] );
		}

		return table;
	}
	
	/**
	 * Get the intensity kurtosis values of the neighbor labels
	 *
	 * @return result table with intensity kurtosis of neighbor labels
	 */
	public ResultsTable getNeighborsKurtosis()
	{
		if( this.neighborVoxels == null )
			this.neighborVoxels = computeNeighborVoxels();
		final int numLabels = objectVoxels.length;
		double[] kurtosis = new double[ numLabels ];

		// calculate kurtosis value
		for( int i=0; i<numLabels; i++ )
		{
			final double voxelCount = neighborVoxels[ i ].size();
			if( voxelCount > 0 )
			{
				double v, v2, sum2 = 0, sum3 = 0, sum4 = 0;
				double mean = 0;
				for( int j=0; j<voxelCount; j++ )
				{
					mean += neighborVoxels[ i ].get( j );
					v = neighborVoxels[ i ].get( j ) + Double.MIN_VALUE;
					v2 = v * v;
					sum2 += v2;
					sum3 += v * v2;
					sum4 += v2 * v2;
				}
				mean /= voxelCount;
				double mean2 = mean*mean;
				double variance = sum2 / voxelCount - mean2;
				kurtosis[ i ] = Double.compare( variance, 0d ) == 0 ? -6.0/5.0 :
					(((sum4 - 4.0 * mean * sum3 + 6.0 * mean2 * sum2 )
						/ voxelCount - 3.0 * mean2 * mean2 )
						/ ( variance * variance ) -3.0 );
			}
			else
				kurtosis[ i ] = Double.NaN;
		}

		// create data table
		ResultsTable table = new ResultsTable();
		for( int i = 0; i < numLabels; i++ )
		{
			table.incrementCounter();
			table.addLabel( Integer.toString( labels[ i ] ) );
			table.addValue( "NeighborsKurtosis", kurtosis[ i ] );
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
		
		// check if the mean intensity per label has already
		// been calculated
		if( null == this.mean )
			this.mean = meanPerLabel();
		double[] sd = new double[ numLabels ];

		// calculate standard deviation
		for( int i=0; i<numLabels; i++ )
		{
			sd[ i ] = 0;
			double diff;
			for( final double v : objectVoxels[ i ] )
			{
				diff = v - mean[ i ];
				sd[ i ] += diff * diff;
			}
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
	 * Get the standard deviation of the intensity values of the neighbor labels
	 *
	 * @return result table with standard deviation values of neighbor labels
	 */
	public ResultsTable getNeighborsStdDev()
	{
		// Check if neighbors mean already exists
		if( null == neighborsMean )
			this.neighborsMean = neighborsMeanPerLabel();
		// Check if neighbor voxels have been calculated
		if( this.neighborVoxels == null )
			this.neighborVoxels = computeNeighborVoxels();
		final int numLabels = neighborsMean.length;

		double[] sd = new double[ numLabels ];

		// Calculate standard deviation
		for( int i=0; i<numLabels; i++ )
		{
			if( neighborVoxels[ i ].size() > 0 )
			{
				for( final double v : neighborVoxels[ i ] )
				{
					double diff = v - neighborsMean[ i ];
					sd[ i ] += diff * diff;
				}
				sd[ i ] /= neighborVoxels[ i ].size();
				sd[ i ] = Math.sqrt( sd[ i ] );
			}
			else
				sd[ i ] = Double.NaN;
		}

		// create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < numLabels; i++) {
			table.incrementCounter();
			table.addLabel(Integer.toString( labels[i] ));
			table.addValue("NeighborsStdDev", sd[i]);
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
		this.max = maxPerLabel();
		
		// create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < objectVoxels.length; i++) {
			table.incrementCounter();
			table.addLabel(Integer.toString( labels[i] ));
			table.addValue("Max", max[i]);
		}

		return table;
	}
	
	/**
	 * Calculate maximum intensity value per labeled region.
	 * @return maximum intensity value per labeled region
	 */
	private double[] maxPerLabel()
	{
		final int numLabels = objectVoxels.length;
		final double[] max = new double[ numLabels ];
		
		// calculate maximum voxel value per object
		for( int i=0; i<numLabels; i++ )
		{
			max[ i ] = Double.NEGATIVE_INFINITY;
			for( final double v : objectVoxels[ i ] )
				if( v > max[ i ] )
					max[ i ] = v;
		}
		return max;
	}
	
	/**
	 * Get the maximum intensity values of the neighbor labels
	 *
	 * @return result table with maximum values of neighbor labels
	 */
	public ResultsTable getNeighborsMax()
	{
		// Check if adjacency list has been created
		if( this.adjList == null )
			this.adjList = RegionAdjacencyGraph.computeAdjacencies( labelImage );

		final int numLabels = objectVoxels.length;

		// check if the maximum intensity of individual labeled regions
		// has been already calculated
		if( null == this.max )
			this.max = maxPerLabel();

		// Initialize array of adjacent maximum values
		double[] adjacentMax = new double[ numLabels ];
		for( int i=0; i<numLabels; i++ )
			adjacentMax[ i ] = Double.NaN;

		// go through list of adjacent pairs
		for( LabelPair pair : adjList )
		{
			// extract their indices
			int ind1 = super.labelIndices.get( pair.label1 );
			int ind2 = super.labelIndices.get( pair.label2 );

			// store maximum value of adjacent label voxels
			if( Double.isNaN( adjacentMax[ ind1 ] ) )
				adjacentMax[ ind1 ] = Double.MIN_VALUE;
			if( Double.isNaN( adjacentMax[ ind2 ] ) )
				adjacentMax[ ind2 ] = Double.MIN_VALUE;

			// check if the maximum value of the adjacent neighbor
			// is larger than the stored maximum (in both directions)
			if( this.max[ ind2 ] > adjacentMax[ ind1 ] )
				adjacentMax[ ind1 ] = this.max[ ind2 ];

			if( this.max[ ind1 ] > adjacentMax[ ind2 ] )
				adjacentMax[ ind2 ] = this.max[ ind1 ];
		}

		// create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < numLabels; i++) {
			table.incrementCounter();
			table.addLabel(Integer.toString( labels[ i ] ));
			table.addValue( "NeighborsMax", adjacentMax[ i ] );
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
		this.min = minPerLabel();

		// create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < objectVoxels.length; i++) {
			table.incrementCounter();
			table.addLabel(Integer.toString( labels[i] ));
			table.addValue("Min", min[i]);
		}

		return table;
	}
	
	/**
	 * Calculate minimum intensity value per labeled region.
	 * @return minimum intensity value per labeled region
	 */
	private double[] minPerLabel()
	{
		final int numLabels = objectVoxels.length;
		double[] min = new double[ numLabels ];
		
		// calculate minimum voxel value per object
		for( int i=0; i<numLabels; i++ )
		{
			min[ i ] = Double.POSITIVE_INFINITY;
			for( final double v : objectVoxels[ i ] )
				if( v < min[ i ] )
					min[ i ] = v;
		}
		return min;
	}
	
	/**
	 * Get the minimum intensity values of the neighbor labels
	 *
	 * @return result table with minimum values of neighbor labels
	 */
	public ResultsTable getNeighborsMin()
	{
		// Check if adjacency list has been created
		if( this.adjList == null )
			this.adjList = RegionAdjacencyGraph.computeAdjacencies( labelImage );

		final int numLabels = objectVoxels.length;

		// check if the minimum intensity of individual labeled regions
		// has been already calculated
		if( null == this.min )
			this.min = minPerLabel();

		// Initialize array of adjacent minimum values
		double[] adjacentMin = new double[ numLabels ];
		for( int i=0; i<numLabels; i++ )
			adjacentMin[ i ] = Double.NaN;

		// go through list of adjacent pairs
		for( LabelPair pair : adjList )
		{
			// extract their indices
			int ind1 = super.labelIndices.get( pair.label1 );
			int ind2 = super.labelIndices.get( pair.label2 );

			// store minimum value of adjacent label voxels
			if( Double.isNaN( adjacentMin[ ind1 ] ) )
				adjacentMin[ ind1 ] = Double.MAX_VALUE;
			if( Double.isNaN( adjacentMin[ ind2 ] ) )
				adjacentMin[ ind2 ] = Double.MAX_VALUE;

			// check if the minimum value of the adjacent neighbor
			// is smaller than the stored minimum (in both directions)
			if( this.min[ ind2 ] < adjacentMin[ ind1 ] )
				adjacentMin[ ind1 ] = this.min[ ind2 ];

			if( this.min[ ind1 ] < adjacentMin[ ind2 ] )
				adjacentMin[ ind2 ] = this.min[ ind1 ];
		}

		// create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < numLabels; i++) {
			table.incrementCounter();
			table.addLabel(Integer.toString( labels[ i ] ));
			table.addValue( "NeighborsMin", adjacentMin[ i ] );
		}

		return table;
	}
	
	/**
	 * Computes the center of mass, i.e. the centroid weighted by the
	 * intensities, of each region.
	 * 
	 * 
	 * @param inputImage
	 *            the input image containing intensities
	 * @param labelImage
	 *            the label map, containing for each pixel or voxel the label of
	 *            the region it belongs to, or zero if it belongs to background.
	 * @return the center of mass of each region, in pixels or voxels.
	 */
	public static final ResultsTable getCenterOfMassInPixels(ImagePlus inputImage, ImagePlus labelImage)
	{
		// retrieve image size
		final int width = inputImage.getWidth();
		final int height = inputImage.getHeight();
		final int nSlices = inputImage.getImageStackSize();
		
		if (width != labelImage.getWidth() || height != labelImage.getHeight())
			throw new IllegalArgumentException("Input and label images must have the same size");
		
		// extract the labels from the input image
		int[] labels = LabelImages.findAllLabels(labelImage.getImageStack());
		int nLabels = labels.length;

		// create associative hash table to know the index of each label
		Map<Integer, Integer> labelIndices = LabelImages.mapLabelIndices(labels);
		
		// initialize values
		double[] sumWX = new double[nLabels]; 
		double[] sumWY = new double[nLabels]; 
		double[] sumWZ = new double[nLabels]; 
		double[] sumWeights = new double[nLabels]; 

		// iterate over image elements to compute coordinates of center of mass
		for (int z = 1; z <= nSlices; z++)
		{
			IJ.showProgress(z, nSlices);

			// retrieve current slices
			final ImageProcessor intensitySlice = inputImage.getImageStack().getProcessor(z);
			final ImageProcessor labelMapSlice = labelImage.getImageStack().getProcessor(z);

			// iterate over pixels of current slice
			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
				{
					int label = (int) labelMapSlice.getf(x, y);
					if (label == 0) continue;

					int index = labelIndices.get(label);
					double weight = intensitySlice.getf(x, y);

					sumWX[index] += x * weight;
					sumWY[index] += y * weight;
					sumWZ[index] += (z - 1) * weight;
					sumWeights[index] += weight;
				}
			}
		}
		IJ.showProgress( 1.0 );
		
		// create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < nLabels; i++) {
			table.incrementCounter();
			table.addLabel(Integer.toString(labels[i]));
			table.addValue("CenterOfMass.X", sumWX[i] / sumWeights[i]);
			table.addValue("CenterOfMass.Y", sumWY[i] / sumWeights[i]);
			if (nSlices > 1)
			{
				table.addValue("CenterOfMass.Z", sumWZ[i] / sumWeights[i]);
			}
		}
		
		return table;
	}
}
