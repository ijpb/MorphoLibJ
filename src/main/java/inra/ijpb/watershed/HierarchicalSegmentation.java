package inra.ijpb.watershed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import inra.ijpb.binary.ConnectedComponents;
import inra.ijpb.data.Cursor2D;
import inra.ijpb.data.Cursor3D;
import inra.ijpb.data.Neighborhood2D;
import inra.ijpb.data.Neighborhood2DC4;
import inra.ijpb.data.Neighborhood2DC8;
import inra.ijpb.data.Neighborhood3D;
import inra.ijpb.data.Neighborhood3DC26;
import inra.ijpb.data.Neighborhood3DC6;
import inra.ijpb.label.LabelImages;
import inra.ijpb.label.RegionAdjacencyGraph;
import inra.ijpb.label.RegionAdjacencyGraph.LabelPair;
import inra.ijpb.measure.IntensityMeasures;
import inra.ijpb.morphology.MinimaAndMaxima;
import inra.ijpb.morphology.MinimaAndMaxima3D;
import inra.ijpb.morphology.Morphology;
import inra.ijpb.morphology.Strel;
import inra.ijpb.morphology.Strel3D;

/**
 * Class implementing the hierarchical segmentation method described in
 * Beucher, Serge. "Watershed, hierarchical segmentation and waterfall
 * algorithm." Mathematical morphology and its applications to image 
 * processing. Springer Netherlands, 1994. 69-76. 
 * 
 * @author Ignacio Arganda-Carreras
 *
 */
public class HierarchicalSegmentation 
{
	/**
	 * Compute the hierarchical segmentation of the input image.
	 * 
	 * @param input grayscale image
	 * @return hierarchical segmentation of the image
	 */
	public static ImageProcessor compute( ImageProcessor input )
	{		
		final int gradientRadius = 1;
		final int connectivity = 4;
		
		// compute hierarchical image
		ImageProcessor hierarchicalImage = computeHierarchicalImage( input,
				gradientRadius, connectivity );
		
		// apply whatershed to hierarchical image
		ImageProcessor minima = 
				MinimaAndMaxima.regionalMinima( hierarchicalImage, connectivity );
		ImageProcessor labeledMinima = 
				ConnectedComponents.computeLabels( minima, connectivity, 32 );
		ImageProcessor catchmentBasins = 
				Watershed.computeWatershed( hierarchicalImage, 
						labeledMinima, connectivity, true );
		
		return catchmentBasins;
	}

	/**
	 * Compute the hierarchical segmentation of the input image.
	 * 
	 * @param input grayscale image
	 * @return hierarchical segmentation of the image
	 */
	public static ImageStack compute( ImageStack input )
	{		
		final int gradientRadius = 1;
		final int connectivity = 6;

		// compute hierarchical image
		ImageStack hierarchicalImage = computeHierarchicalImage( input,
				gradientRadius, connectivity );
	
		// apply whatershed to hierarchical image
		ImageStack minima = 
			MinimaAndMaxima3D.regionalMinima( hierarchicalImage, connectivity );
		ImageStack labeledMinima = 
				ConnectedComponents.computeLabels( minima, connectivity, 32 );
		ImageStack catchmentBasins = 
				Watershed.computeWatershed( hierarchicalImage, 
						labeledMinima, connectivity, true );
		
		return catchmentBasins;
	}
	
	/**
	 * Compute hierarchical image (h' in the original paper)
	 * 
	 * @param input grayscale image 
	 * @param gradientRadius radius in pixels of the gradient to use
	 * @param connectivity pixel connectivity (4 or 8)
	 * @return hierarchical image 
	 */
	public static ImageProcessor computeHierarchicalImage(
			ImageProcessor input,
			final int gradientRadius, 
			final int connectivity ) 
	{
		final int width = input.getWidth();
		final int height = input.getHeight();

		// calculate gradient image
		Strel strel = Strel.Shape.SQUARE.fromRadius( gradientRadius );
		ImageProcessor gradientImage = Morphology.gradient( input, strel );

		// apply classic Meyer's watershed algorithm		
		ImageProcessor minima = 
				MinimaAndMaxima.regionalMinima( gradientImage, connectivity );
		ImageProcessor labeledMinima = 
				ConnectedComponents.computeLabels( minima, connectivity, 32 );
		ImageProcessor catchmentBasins = 
				Watershed.computeWatershed( gradientImage, 
						labeledMinima, connectivity, true );

		// calculate mean value of each labeled region
		IntensityMeasures im = new IntensityMeasures( 
				new ImagePlus( "input", input ), 
				new ImagePlus( "label", catchmentBasins ) );
		ResultsTable table = im.getMean();		

		// extract array of numerical values
		int index = table.getColumnIndex( IntensityMeasures.meanHeaderName );
		double[] meanValues = table.getColumnAsDoubles( index );

		// calculate catchment basin adjacencies (C_ij) 
		Set<LabelPair> adjacencies = 
				RegionAdjacencyGraph.computeAdjacencies( catchmentBasins );

		// calculate new value for the basins ( v(CB_i) = inf_j( h( C_ij ) ) )
		final double[] minimumDiffValues = new double[ meanValues.length ];
		Arrays.fill( minimumDiffValues, Double.MAX_VALUE );
		for( LabelPair lp : adjacencies )
		{
			double diff = Math.abs( meanValues[ lp.label1-1 ] 
					- meanValues[ lp.label2-1 ] );
			if( diff < minimumDiffValues[ lp.label1-1 ] )
				minimumDiffValues[ lp.label1-1 ] = diff;
			if( diff < minimumDiffValues[ lp.label2-1 ] )
				minimumDiffValues[ lp.label2-1 ] = diff;
		}

		// create hierarchical image h':
		// h'(x) = h(C_ij) iff x belongs to C_ij
		// h'(x) = v(CB_i) iff x belongs to CB_i

		// first fill catchment basin (CB) pixels
		ImageProcessor hierarchicalImage 
		= LabelImages.applyLut( catchmentBasins, minimumDiffValues );

		// then fill arc (C_ij) pixels
		final Neighborhood2D neigh = connectivity == 4 ? 
				new Neighborhood2DC4() : new Neighborhood2DC8();
		final Cursor2D cursor = new Cursor2D( 0, 0 );

		for( int x=0; x<width; x++ )
			for( int y=0; y<height; y++ )
				if( catchmentBasins.getf( x, y ) 
						== WatershedTransform2D.WSHED ) // watershed line
				{
					// find two neighbor labels
					cursor.set( x, y );
					neigh.setCursor( cursor );
					ArrayList< Integer > list = 
							getNeighborLabels( catchmentBasins, neigh );
					// if the watershed pixel has neighbors
					if( list.size() != 0 )
					{	
						// assign largest difference between regions
						float maxDiff = 0;
						for( int i=0; i<list.size(); i++ )
							for( int j=i+1; j<list.size(); j++ )
							{
								float diff = (float)
										Math.abs( meanValues[ list.get( i ) -1 ]
												- meanValues[ list.get( j ) -1 ]);
								if( diff > maxDiff )
									maxDiff = diff;
							}
						hierarchicalImage.setf( x,  y, maxDiff );
					}
				}
		// finally, check remaining watershed pixels value (special case, they
		// have value NaN after apply the LUT)
		for( int x=0; x < width; x++ )
			for( int y=0; y < height; y++ )
				if( Float.isNaN( hierarchicalImage.getf( x, y ) ) )
				{					
					// assign largest neighbor
					cursor.set( x, y );
					neigh.setCursor( cursor );
					float max = 0;
					for( Cursor2D c : neigh.getNeighbors() )
					{
						if( c.getX() >= 0 && c.getX() < width 
								&& c.getY() >= 0 && c.getY() < height )
							if( max < hierarchicalImage.getf( c.getX(), c.getY() ) )
								max = hierarchicalImage.getf( c.getX(), c.getY() );
					}
					hierarchicalImage.setf( x, y, max );
				}

		return hierarchicalImage;
	}

	/**
	 * Compute hierarchical image (h' in the original paper) in 3D
	 * 
	 * @param input grayscale image 
	 * @param gradientRadius radius in voxel units of the gradient to use
	 * @param connectivity voxel connectivity (6 or 26)
	 * @return hierarchical image 
	 */
	public static ImageStack computeHierarchicalImage(
			ImageStack input,
			final int gradientRadius, 
			final int connectivity ) 
	{
		final int width = input.getWidth();
		final int height = input.getHeight();
		final int depth = input.getSize();

		// calculate gradient image
		Strel3D strel = Strel3D.Shape.CUBE.fromRadius( gradientRadius );
		ImageStack gradientImage = Morphology.gradient( input, strel );

		// apply classic Meyer's watershed algorithm		
		ImageStack minima = 
				MinimaAndMaxima3D.regionalMinima( gradientImage, connectivity );
		ImageStack labeledMinima = 
				ConnectedComponents.computeLabels( minima, connectivity, 32 );
		ImageStack catchmentBasins = 
				Watershed.computeWatershed( gradientImage, 
						labeledMinima, connectivity, true );

		// calculate mean value of each labeled region
		IntensityMeasures im = new IntensityMeasures( 
				new ImagePlus( "input", input ), 
				new ImagePlus( "label", catchmentBasins ) );
		ResultsTable table = im.getMean();		

		// extract array of numerical values
		int index = table.getColumnIndex( IntensityMeasures.meanHeaderName );
		double[] meanValues = table.getColumnAsDoubles( index );

		// calculate catchment basin adjacencies (C_ij) 
		Set<LabelPair> adjacencies = 
				RegionAdjacencyGraph.computeAdjacencies( catchmentBasins );

		// calculate new value for the basins ( v(CB_i) = inf_j( h( C_ij ) ) )
		final double[] minimumDiffValues = new double[ meanValues.length ];
		Arrays.fill( minimumDiffValues, Double.MAX_VALUE );
		for( LabelPair lp : adjacencies )
		{
			double diff = Math.abs( meanValues[ lp.label1-1 ] 
									- meanValues[ lp.label2-1 ] );
			if( diff < minimumDiffValues[ lp.label1-1 ] )
				minimumDiffValues[ lp.label1-1 ] = diff;
			if( diff < minimumDiffValues[ lp.label2-1 ] )
				minimumDiffValues[ lp.label2-1 ] = diff;
		}

		// create hierarchical image h':
		// h'(x) = h(C_ij) iff x belongs to C_ij
		// h'(x) = v(CB_i) iff x belongs to CB_i

		// first fill catchment basin (CB) pixels
		ImageStack hierarchicalImage 
			= LabelImages.applyLut( catchmentBasins, minimumDiffValues );

		// then fill arc (C_ij) pixels
		final Neighborhood3D neigh = connectivity == 6 ? 
				new Neighborhood3DC6() : new Neighborhood3DC26();
		final Cursor3D cursor = new Cursor3D( 0, 0, 0 );

		for( int z=0; z<depth; z++ )
			for( int x=0; x<width; x++ )
				for( int y=0; y<height; y++ )
					if( catchmentBasins.getVoxel( x, y, z ) 
							== WatershedTransform3D.WSHED ) // watershed line
					{
						// find two neighbor labels
						cursor.set( x, y, z );
						neigh.setCursor( cursor );
						ArrayList< Integer > list = 
								getNeighborLabels( catchmentBasins, neigh );
						// if the watershed voxel has neighbors
						if( list.size() != 0 )
						{	
							// assign largest difference between regions
							float maxDiff = 0;
							for( int i=0; i<list.size(); i++ )
								for( int j=i+1; j<list.size(); j++ )
								{
									float diff = (float)
											Math.abs( meanValues[ list.get( i ) -1 ]
													- meanValues[ list.get( j ) -1 ]);
									if( diff > maxDiff )
										maxDiff = diff;
								}
							hierarchicalImage.setVoxel( x,  y, z, maxDiff );
						}
					}
		// finally, check remaining watershed voxels value (special case, they
		// have value NaN after apply the LUT)
		for( int z=0; z<depth; z++ )
			for( int x=0; x < width; x++ )
				for( int y=0; y < height; y++ )
					if( Double.isNaN( hierarchicalImage.getVoxel( x, y, z ) ) )
					{
						// assign largest neighbor
						cursor.set( x, y, z );
						neigh.setCursor( cursor );
						float max = 0;
						for( Cursor3D c : neigh.getNeighbors() )
						{
							float voxelValue = 
									(float) hierarchicalImage.getVoxel( 
											c.getX(), c.getY(), c.getZ() ); 
							if( max < voxelValue )
								max = voxelValue;
						}
						hierarchicalImage.setVoxel( x, y, z, max );
					}

		return hierarchicalImage;
	}
	
	
	/**
	 * Get list of neighbor labels of a pixel (without repetitions)
	 * @param labelImage input labeled image
	 * @param neigh neighborhood to use
	 * @return list with neighbor labels (without repetitions)
	 */
	static ArrayList< Integer > getNeighborLabels( 
			final ImageProcessor labelImage,
			final Neighborhood2D neigh )
	{
		final ArrayList< Integer > neighborLabels = new ArrayList<Integer>();
		for( Cursor2D c : neigh.getNeighbors() )			       		
		{ 
			if( c.getX() >= 0 && c.getX() < labelImage.getWidth() 
					&& c.getY() >= 0 && c.getY() < labelImage.getHeight() )
			{
				final int pixelValue = (int) labelImage.getf( c.getX(), c.getY() );
				if( pixelValue != 0 && 
						neighborLabels.contains( pixelValue )  == false )			
					neighborLabels.add( pixelValue );
			}
		}
		return neighborLabels;
	}
	
	/**
	 * Get list of neighbor labels of a voxel (without repetitions)
	 * @param labelImage input labeled image
	 * @param neigh neighborhood to use
	 * @return list with neighbor labels (without repetitions)
	 */
	static ArrayList< Integer > getNeighborLabels( 
			final ImageStack labelImage,
			final Neighborhood3D neigh )
	{
		final ArrayList< Integer > neighborLabels = new ArrayList<Integer>();
		for( Cursor3D c : neigh.getNeighbors() )			       		
		{ 			
			final int pixelValue = 
					(int) labelImage.getVoxel( c.getX(), c.getY(), c.getZ() );
			if( pixelValue != 0 && 
					neighborLabels.contains( pixelValue )  == false )			
				neighborLabels.add( pixelValue );
		}
		return neighborLabels;
	}
	
}
