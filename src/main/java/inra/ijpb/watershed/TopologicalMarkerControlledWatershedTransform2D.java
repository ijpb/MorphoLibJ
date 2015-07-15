package inra.ijpb.watershed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

import ij.IJ;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.data.Cursor2D;
import inra.ijpb.data.Neighborhood2D;
import inra.ijpb.data.Neighborhood2DC4;
import inra.ijpb.data.Neighborhood2DC8;
import inra.ijpb.data.PixelRecord;

public class TopologicalMarkerControlledWatershedTransform2D 
	extends MarkerControlledWatershedTransform2D
{

	public static final Cursor2D W = new Cursor2D( -1, -1 );
	
	public TopologicalMarkerControlledWatershedTransform2D(
			ImageProcessor input, 
			ImageProcessor marker, 
			ImageProcessor mask ) 
	{
		super( input, marker, mask );
	}
	
	public TopologicalMarkerControlledWatershedTransform2D(
			ImageProcessor input, 
			ImageProcessor marker, 
			ImageProcessor mask,
			int connectivity ) 
	{
		super( input, marker, mask, connectivity );
	}
	
	private ImageProcessor applyWithoutMask() 
	{
		ImageProcessor result = null;
		
		// image size
		final int size1 = inputImage.getWidth();
	    final int size2 = inputImage.getHeight();
	    
	    if (size1 != markerImage.getWidth() || size2 != markerImage.getHeight()) 
	    {
			throw new IllegalArgumentException("Marker and input images must have the same size");
		}
		
		// Check connectivity has a correct value
		if ( connectivity != 4 && connectivity != 8 ) 
		{
			throw new RuntimeException(
					"Connectivity for 2D images must be either 4 or 8, not "
							+ connectivity);
		}	    
		
		// list of original pixels values and corresponding coordinates
		PriorityQueue<PixelRecord> pixelList = null;
		
		// final labels
		final int[][] tabLabels = new int[ size1 ][ size2 ];
		
		final ImageProcessor f = super.inputImage;
		final ImageProcessor g = super.markerImage;
		
		
		
		
		
		
		
		return result;
	}
	
	/**
	 * Apply lower completion to input image based on the Algorithm 4.5 of the
	 * work by J. Roerdink, R. Meijster: <em>"The watershed transform: 
	 * definitions, algorithms, and parallelization strategies"</em>, 
	 * Fundamenta Informaticae, 41:187-228, 2000.
	 * 
	 * @return lower completed image
	 */
	public ImageProcessor lowerCompletion()
	{
		final int size1 = inputImage.getWidth();
		final int size2 = inputImage.getHeight();
		
		final int[][] lc = new int[ size1 ][ size2 ];
		
		LinkedList<Cursor2D> fifo = new LinkedList<Cursor2D>();

		final Cursor2D cur = new Cursor2D( 0, 0 );
		final Neighborhood2D neigh = connectivity == 4 ? 
					new Neighborhood2DC4() : new Neighborhood2DC8();
		
		// initialize queue with pixels that have a lower neighbor
		for( int x = 0; x < size1; x++ )
			for( int y = 0; y < size2; y++ )
			{
				lc[ x ][ y ] = 0;
				cur.set( x, y );
				
				// if current point has a lower neighbor
				neigh.setCursor( cur );
	    		for( Cursor2D c : neigh.getNeighbors() )			       		
	    		{       			
	    			int u = c.getX();
	    			int v = c.getY();
	    			if ( u >= 0 && u < size1 && v >= 0 && v < size2
	    					&& inputImage.getf( x, y ) > inputImage.getf( u, v ) )
	    			{
	    				fifo.add( new Cursor2D( x, y ) );
	    				lc[ x ][ y ] = -1;
	    				break;
	    			}
	    		}
			}
		
		// Compute a function which indirectly indicates
	    // the amount by which we need to raise the
	    // plateau pixels
		
		int dist = 1; 
		
		// insert fictitious pixel
		final Cursor2D fictitious = new Cursor2D( -1, -1 );
		fifo.add( fictitious );
		
		while( fifo.isEmpty() == false )
		{
			final Cursor2D p = fifo.poll();
			
			if( p.equals( fictitious ) )
			{
				if( fifo.isEmpty() == false )
				{
					fifo.add( fictitious );
					dist ++;
				}
			}
			else
			{
				int x = p.getX();
				int y = p.getY();
				lc[ x ][ y ] = dist;
				
				neigh.setCursor( p );
	    		for( Cursor2D q : neigh.getNeighbors() )			       		
	    		{       			
	    			// If the neighboring pixel is at the
	    			// same altitude and has not yet been
	    	        // processed
	    			int u = q.getX();
	    			int v = q.getY();
	    			if ( u >= 0 && u < size1 && v >= 0 && v < size2
	    					&& Float.compare( inputImage.getf( x, y )
	    							, inputImage.getf( u, v ) ) == 0 
	    					&& lc[ u ][ v ] == 0 )
	    			{
	    				fifo.add( new Cursor2D( u, v ) );
	    				lc[ u ][ v ] = -1; // to prevent from queueing twice
	    			}
	    		}				
			}
		} // end while
		
		// Put the lower complete values in the output image.
		// Note that at this point, dist holds the
	    // amount by which we want to multiply the base
	    // image.
		final ImageProcessor output = inputImage.duplicate();
		for( int x = 0; x < size1; x++ )
			for( int y = 0; y < size2; y++ )
			{
				if( lc[ x ][ y ] != 0 )
					output.setf( x, y, dist * inputImage.getf( x, y ) 
									  + lc[ x ][ y ] - 1);
				else
					output.setf( x, y, dist * inputImage.getf( x, y ) );
			}
			
		return output;
	}
	
	public ArrayList<Cursor2D>[][] lowerCompleteGraph( ImageProcessor input )
	{
		final int size1 = input.getWidth();
		final int size2 = input.getHeight();
		
		ArrayList<Cursor2D>[][] dag = new ArrayList[ size1 ][ size2 ];
		
		final Cursor2D cur = new Cursor2D( 0, 0 );
		final Neighborhood2D neigh = connectivity == 4 ? 
					new Neighborhood2DC4() : new Neighborhood2DC8();
					
		// For each pixel p, store in a DAG a pointer to each steepest lower
		// neighbor of p.
		// For each minimum m, one pixel r belonging to m is chosen as the 
		// representative of this minimum, and a pointer is created from r to 
		// itself.
		
		// hash table to help handling minima
		HashMap<Float, Cursor2D> minima = new HashMap<Float, Cursor2D>();
		
		for( int x = 0; x < size1; x++ )
			for( int y = 0; y < size2; y++ )
			{
				cur.set( x, y );
				
				dag[ x ] [ y ] = new ArrayList<Cursor2D>();
				
				final float label = markerImage.getf( x, y );
				
				// if current point is a local minimum
				if ( Float.compare( label, 0f ) != 0 )
				{
					if( minima.containsKey(label) )
					{
						dag[ x ][ y ].add( minima.get( label ) );
						//IJ.log( "dag[ "+x+" ][ "+y+" ].add( " +minima.get(label).getX() + ", " + minima.get(label).getY() + ")");
					}
					else
					{
						final Cursor2D c = new Cursor2D(x, y);
						minima.put( label, c );
						dag[ x ][ y ].add( c );
						//IJ.log( "dag[ "+x+" ][ "+y+" ].add( " +c.getX() + ", " + c.getY() + ")");
					}
				}
				else // non minimum
				{
					float min = Float.MAX_VALUE;

					neigh.setCursor( cur );
					for( Cursor2D c : neigh.getNeighbors() )			       		
					{       			
						int u = c.getX();
						int v = c.getY();
						if ( u >= 0 && u < size1 && v >= 0 && v < size2
								&& input.getf( x, y ) > input.getf( u, v ) )
						{
							if( Float.compare( input.getf( u, v ), min ) == 0 )
							{
								dag[ x ][ y ].add( new Cursor2D( u, v ) );
							}
							else if ( input.getf( u, v ) < min )
							{
								min = input.getf( u, v );
								dag[ x ][ y ].clear();
								dag[ x ][ y ].add( new Cursor2D( u, v ) );
							}
						}
					}
				}// end else
		
			}
		return dag;
	}

	public ImageProcessor unionFindWatershed( ArrayList<Cursor2D>[][] g )
	{
		final int size1 = inputImage.getWidth();
		final int size2 = inputImage.getHeight();
		
		final int[][] lab = new int[ size1 ][ size2 ];
		
		// LabelInit: initialize image lab with distinct labels for minima
		for( int x = 0; x < size1; x++ )
			for( int y = 0; y < size2; y++ )
				lab[ x ][ y ] = (int) markerImage.getf(x, y);
		
		
		final Cursor2D p = new Cursor2D( 0, 0 );
		
		// give each point p the label of its representative
		for( int x = 0; x < size1; x++ )
			for( int y = 0; y < size2; y++ )
			{
				p.set( x, y );
				Cursor2D rep = resolve( p, g );
				if( rep.equals( W ) == false )				
					lab[ x ][ y ] = lab[ rep.getX() ][ rep.getY() ];
				else
					lab[ x ][ y ] = WSHED;					
			}
		
		FloatProcessor fp = new FloatProcessor( size1, size2 );
		for (int i = 0; i < size1; ++i)
			for (int j = 0; j < size2; ++j)
			{				
				fp.setf( i, j, lab[ i ][ j ] );
			}		
	    				    
	    return fp;
		
	}

	/**
	 * Recursive function for resolving the downstream paths of the lower
	 * complete graph
	 * 
	 * @param p
	 * @param g
	 * @return
	 */
	private Cursor2D resolve(Cursor2D p, ArrayList<Cursor2D>[][] g) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
