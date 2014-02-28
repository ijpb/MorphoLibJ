package inra.ijpb.watershed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.process.ImageProcessor;
import ij.util.ThreadUtil;
import inra.ijpb.data.Cursor3D;
import inra.ijpb.data.Neighborhood3D;
import inra.ijpb.data.Neighborhood3DC26;
import inra.ijpb.data.Neighborhood3DC6;
import inra.ijpb.data.VoxelRecord;

/**
 * Marker-controlled version of the watershed transform (works for 2D and 3D images)
 * 
 * @author Ignacio Arganda-Carreras
 *
 */
public class MarkerControlledWatershedTransform3D extends WatershedTransform3D
{
	/** image containing the labeled markers to start the watershed */
	ImagePlus markerImage = null;

	/**
	 * Initialize a marker-controlled watershed transform
	 * 
	 * @param input grayscale image (usually a gradient image)
	 * @param marker image containing the labeled markers to start the watershed
	 * @param mask binary mask to restrict the region of interest (null to use whole input image)
	 */
	public MarkerControlledWatershedTransform3D(
			ImagePlus input, 
			ImagePlus marker,
			ImagePlus mask) 
	{
		super( input, mask );
		this.markerImage = marker;		
	}
	
	/**
	 * Initialize a marker-controlled watershed transform
	 * 
	 * @param input grayscale image (usually a gradient image)
	 * @param marker image containing the labeled markers to start the watershed
	 * @param mask binary mask to restrict the region of interest (null to use whole input image)
	 * @param connectivity voxel connectivity (6 or 26)
	 */
	public MarkerControlledWatershedTransform3D(
			ImagePlus input, 
			ImagePlus marker,
			ImagePlus mask,
			int connectivity ) 
	{
		super( input, mask, connectivity );
		this.markerImage = marker;		
	}
	
	/**
	 * Apply watershed transform on inputImage, using the labeled 
	 * markers from markerImage and restricted to the white areas 
	 * of maskImage. This implementation visits all voxels by
	 * ascending gray value.
	 * 
	 * @return watershed domains image (no dams)
	 */
	public ImagePlus applyWithSortedList()
	{
		final ImageStack inputStack = inputImage.getStack();
	    final int size1 = inputStack.getWidth();
	    final int size2 = inputStack.getHeight();
	    final int size3 = inputStack.getSize();
	    
	    if (size1 != markerImage.getWidth() || size2 != markerImage.getHeight() 
	    		|| size3 != markerImage.getStackSize()) {
			throw new IllegalArgumentException("Marker and input images must have the same size");
		}
		
		// Check connectivity has a correct value
		if (connectivity != 6 && connectivity != 26) {
			throw new RuntimeException(
					"Connectivity for stacks must be either 6 or 26, not "
							+ connectivity);
		}
	    
		// list of original voxels values and corresponding coordinates
		LinkedList<VoxelRecord> voxelList = null;
		
		final int[][][] tabLabels = new int[ size1 ][ size2 ][ size3 ]; 
		
		// Make list of voxels and sort it in ascending order
		IJ.showStatus( "Extracting voxel values..." );
		IJ.log("  Extracting voxel values..." );
		final long t0 = System.currentTimeMillis();
		
		voxelList = extractVoxelValues( inputStack, markerImage.getStack(), tabLabels );
						
		final long t1 = System.currentTimeMillis();		
		IJ.log("  Extraction took " + (t1-t0) + " ms.");
		IJ.log("  Sorting voxels by value..." );
		IJ.showStatus("Sorting voxels by value...");
		Collections.sort( voxelList );
		final long t2 = System.currentTimeMillis();
		IJ.log("  Sorting took " + (t2-t1) + " ms.");
			    
		// Watershed
	    boolean found = false;	    

	    final long start = System.currentTimeMillis();

	    // Auxiliary cursor to visit neighbors
	    final Cursor3D cursor = new Cursor3D(0, 0, 0);
      	
      	// Check connectivity
       	final Neighborhood3D neigh = connectivity == 26 ? 
       			new Neighborhood3DC26() : new Neighborhood3DC6();
	    
	    boolean change = true;
	    while ( voxelList.isEmpty() == false && change )
	    {
	    	change = false;
			final int count = voxelList.size();
	      	IJ.log( "  Flooding " + count + " voxels..." );
	      	IJ.showStatus("Flooding " + count + " voxels...");	      		      	
	      	
			for (int p = 0; p < count; ++p)
	      	{
				IJ.showProgress(p, count);
	       		final VoxelRecord voxelRecord = voxelList.removeFirst();
	       		final Cursor3D p2 = voxelRecord.getCursor();
	    		final int i = p2.getX();
	    		final int j = p2.getY();
	    		final int k = p2.getZ();
	       		
	       		// If the voxel is unlabeled
				if( tabLabels[ i ][ j ][ k ] == 0 )
	       		{
			       	found = false;
			       	double voxelValue = voxelRecord.getValue();
			       	
			       	// Read neighbor coordinates
			       	cursor.set( i, j, k );
			       	neigh.setCursor( cursor );
			       		
			       	for( Cursor3D c : neigh.getNeighbors() )			       		
			       	{
			       		// Look in neighborhood for labeled voxels with
			       		// smaller or equal original value
			       		int u = c.getX();
			       		int v = c.getY();
			       		int w = c.getZ();
			       					       		
			       		if ( u >= 0 && u < size1 && v >= 0 && v < size2 && w >= 0 && w < size3 )
			       		{
			       			if ( tabLabels[u][v][w] != 0 && inputStack.getVoxel(u,v,w) <= voxelValue )
			       			{
			       				tabLabels[i][j][k] = tabLabels[u][v][w];
			       				voxelValue = inputStack.getVoxel(u,v,w);
			       				found = true;
			       			}
			       		}			       		
			       	}
			       
					if ( found == false )    
						voxelList.addLast( voxelRecord );
					else
						change = true;
	      		}
	        }
		}

		final long end = System.currentTimeMillis();
		IJ.log("  Flooding took: " + (end-start) + " ms");
		
		// Create result label image
		ImageStack labelStack = markerImage.duplicate().getStack();
	    
	    for (int i = 0; i < size1; ++i)
	      for (int j = 0; j < size2; ++j)
	        for (int k = 0; k < size3; ++k)
	            labelStack.setVoxel( i, j, k, tabLabels[i][j][k] );
	    final ImagePlus ws = new ImagePlus( "watershed", labelStack );
	    ws.setCalibration( inputImage.getCalibration() );
	    return ws;
	}

	
	/**
	 * Apply watershed transform on inputImage, using the labeled 
	 * markers from markerImage and restricted to the white areas 
	 * of maskImage. This implementation visits first the voxels 
	 * on the surroundings of the labeled markers.
	 * 
	 * @return watershed domains image (no dams)
	 */
	public ImagePlus applyWithPriorityQueue()
	{
		final ImageStack inputStack = inputImage.getStack();
	    final int size1 = inputStack.getWidth();
	    final int size2 = inputStack.getHeight();
	    final int size3 = inputStack.getSize();
	    
	    if (size1 != markerImage.getWidth() || size2 != markerImage.getHeight() 
	    		|| size3 != markerImage.getStackSize()) 
	    {
			throw new IllegalArgumentException("Marker and input images must have the same size");
		}
		
		// Check connectivity has a correct value
		if ( connectivity != 6 && connectivity != 26 ) 
		{
			throw new RuntimeException(
					"Connectivity for stacks must be either 6 or 26, not "
							+ connectivity);
		}	    
	    
		// list of original voxels values and corresponding coordinates
		PriorityQueue<VoxelRecord> voxelList = null;
		
		final int[][][] tabLabels = new int[ size1 ][ size2 ][ size3 ]; 
		
		// Make list of voxels and sort it in ascending order
		IJ.showStatus( "Extracting voxel values..." );
		IJ.log("  Extracting voxel values..." );
		final long t0 = System.currentTimeMillis();
		
		voxelList = extractVoxelValuesPriorityQueue( inputStack, markerImage.getStack(), tabLabels );
						
		final long t1 = System.currentTimeMillis();		
		IJ.log("  Extraction took " + (t1-t0) + " ms.");
					    
		// Watershed
	    final long start = System.currentTimeMillis();
	         	
      	// Check connectivity
       	final Neighborhood3D neigh = connectivity == 26 ? 
       			new Neighborhood3DC26() : new Neighborhood3DC6();

	    final int count = voxelList.size();
	    IJ.log( "  Flooding from " + count + " voxels..." );
      	IJ.showStatus("Flooding from " + count + " voxels...");
	    
      	final int numVoxels = size1 * size2 * size3;
      	
      	// with mask
      	if ( null != maskImage )
      	{
      		final ImageStack maskStack = maskImage.getStack();
      		
      		while ( voxelList.isEmpty() == false )
      		{
      			IJ.showProgress( numVoxels-voxelList.size(), numVoxels );

      			final VoxelRecord voxelRecord = voxelList.poll();
      			final Cursor3D p = voxelRecord.getCursor();
	    		final int i = p.getX();
	    		final int j = p.getY();
	    		final int k = p.getZ();

      			double voxelValue = voxelRecord.getValue();

      			// Read neighbor coordinates		       	
		       	neigh.setCursor( p );
		       		
		       	for( Cursor3D c : neigh.getNeighbors() )			       		
		       	{
		       		// Look in neighborhood for labeled voxels with
		       		// smaller or equal original value
		       		int u = c.getX();
		       		int v = c.getY();
		       		int w = c.getZ();
		       		
		       		if ( u >= 0 && u < size1 && v >= 0 && v < size2 && w >= 0 && w < size3 )
		       		{
		       			// Unlabeled neighbors go into the queue if they are not there yet 
		       			if ( tabLabels[u][v][w] == 0 && maskStack.getVoxel(u, v, w) > 0 )
		       			{
		       				voxelList.add( new VoxelRecord( u, v, w, inputStack.getVoxel(u,v,w) ));
		       				tabLabels[u][v][w] = INQUEUE;
		       			}
		       			else if ( tabLabels[u][v][w] > 0 && inputStack.getVoxel(u,v,w) <= voxelValue )
		       			{
		       				// assign label of smallest neighbor
		       				tabLabels[i][j][k] = tabLabels[u][v][w];
		       				voxelValue = inputStack.getVoxel(u,v,w);
		       			}
		       		}
		       	}    

      		}
      	}
      	else // without mask
      	{
      		while ( voxelList.isEmpty() == false )
      		{
      			IJ.showProgress( numVoxels-voxelList.size(), numVoxels );

      			final VoxelRecord voxelRecord = voxelList.poll();
      			final Cursor3D p = voxelRecord.getCursor();
	    		final int i = p.getX();
	    		final int j = p.getY();
	    		final int k = p.getZ();


      			double voxelValue = voxelRecord.getValue(); //inputStack.getVoxel( i, j, k );

      			// Read neighbor coordinates
      			neigh.setCursor( p );

      			for( Cursor3D c : neigh.getNeighbors() )			       		
      			{
      				// Look in neighborhood for labeled voxels with
      				// smaller or equal original value
      				int u = c.getX();
      				int v = c.getY();
      				int w = c.getZ();
      				if ( u >= 0 && u < size1 && v >= 0 && v < size2 && w >= 0 && w < size3 )
      				{
      					// Unlabeled neighbors go into the queue if they are not there yet 
      					if ( tabLabels[ u ][ v ][ w ] == 0 )
      					{
      						voxelList.add( new VoxelRecord( u, v, w, inputStack.getVoxel( u, v, w ) ));
      						tabLabels[u][v][w] = INQUEUE;
      					}
      					else if ( tabLabels[ u ][ v ][ w ] > 0 && inputStack.getVoxel( u, v, w ) <= voxelValue )
      					{
      						// assign label of smallest neighbor
      						tabLabels[ i ][ j ][ k ] = tabLabels[ u ][ v ][ w ];
      						voxelValue = inputStack.getVoxel( u, v, w );
      					}
      				}
      			}    

      		}
      	}

		final long end = System.currentTimeMillis();
		IJ.log("  Flooding took: " + (end-start) + " ms");
		
		// Create result label image
		ImageStack labelStack = markerImage.duplicate().getStack();
	    
	    for (int i = 0; i < size1; ++i)
	      for (int j = 0; j < size2; ++j)
	        for (int k = 0; k < size3; ++k)
	            labelStack.setVoxel( i, j, k, tabLabels[i][j][k] );
	    final ImagePlus ws = new ImagePlus( "watershed", labelStack );
	    ws.setCalibration( inputImage.getCalibration() );
	    return ws;
	}

	/**
	 * Apply watershed transform on inputImage, using the labeled 
	 * markers from markerImage and restricted to the white areas 
	 * of maskImage. This implementation visits first the voxels 
	 * on the surroundings of the labeled markers.
	 * 
	 * @return watershed domains image (no dams)
	 */
	public ImagePlus applyWithPriorityQueueAndDams()
	{
		final ImageStack inputStack = inputImage.getStack();
	    final int size1 = inputStack.getWidth();
	    final int size2 = inputStack.getHeight();
	    final int size3 = inputStack.getSize();
	    
	    if (size1 != markerImage.getWidth() || size2 != markerImage.getHeight() 
	    		|| size3 != markerImage.getStackSize()) 
	    {
			throw new IllegalArgumentException("Marker and input images must have the same size");
		}
		
		// Check connectivity has a correct value
		if ( connectivity != 6 && connectivity != 26 ) 
		{
			throw new RuntimeException(
					"Connectivity for stacks must be either 6 or 26, not "
							+ connectivity);
		}	    
	    
		// list of original voxels values and corresponding coordinates
		PriorityQueue<VoxelRecord> voxelList = null;
		
		final int[][][] tabLabels = new int[ size1 ][ size2 ][ size3 ]; 
		
		// Make list of voxels and sort it in ascending order
		IJ.showStatus( "Extracting voxel values..." );
		IJ.log("  Extracting voxel values..." );
		final long t0 = System.currentTimeMillis();
		
		voxelList = extractVoxelValuesPriorityQueue( inputStack, markerImage.getStack(), tabLabels );
						
		final long t1 = System.currentTimeMillis();		
		IJ.log("  Extraction took " + (t1-t0) + " ms.");
					    
		// Watershed
	    final long start = System.currentTimeMillis();
	         	
      	// Check connectivity
       	final Neighborhood3D neigh = connectivity == 26 ? 
       			new Neighborhood3DC26() : new Neighborhood3DC6();

	    final int count = voxelList.size();
	    IJ.log( "  Flooding from " + count + " voxels..." );
      	IJ.showStatus("Flooding from " + count + " voxels...");
	    
      	final int numVoxels = size1 * size2 * size3;
      	
      	// with mask
      	if ( null != maskImage )
      	{
      		final ImageStack maskStack = maskImage.getStack();
      		
      		while ( voxelList.isEmpty() == false )
      		{
      			IJ.showProgress( numVoxels-voxelList.size(), numVoxels );

      			final VoxelRecord voxelRecord = voxelList.poll();
      			final Cursor3D p = voxelRecord.getCursor();
	    		final int i = p.getX();
	    		final int j = p.getY();
	    		final int k = p.getZ();
      			
      			// Read neighbor coordinates		       	
		       	neigh.setCursor( p );
		       	ArrayList <Integer> neighborLabels = new ArrayList<Integer>(); 
		       	
		       	for( Cursor3D c : neigh.getNeighbors() )			       		
		       	{
		       		// Look in neighborhood for labeled voxels with
		       		// smaller or equal original value
		       		int u = c.getX();
		       		int v = c.getY();
		       		int w = c.getZ();
		       		
		       		if ( u >= 0 && u < size1 && v >= 0 && v < size2 && w >= 0 && w < size3 )
		       		{
		       			// Unlabeled neighbors go into the queue if they are not there yet 
		       			if ( tabLabels[u][v][w] == 0 && maskStack.getVoxel(u, v, w) > 0 )
		       			{
      						voxelList.add( new VoxelRecord( u, v, w, inputStack.getVoxel( u, v, w ) ));
      						tabLabels[u][v][w] = INQUEUE;
      					}
      					else if ( tabLabels[ u ][ v ][ w ] > 0 
      							&& neighborLabels.contains(tabLabels[ u ][ v ][ w ]) == false)
      					{
      						 neighborLabels.add( tabLabels[ u ][ v ][ w ] );
      					}
      				}
      			}
      			//  if the neighbors of the extracted voxel that have already been labeled 
      			// all have the same label, then the voxel is labeled with their label
      			if( neighborLabels.size() == 1 )
      				tabLabels[ i ][ j ][ k ] = neighborLabels.get( 0 );

      		}
      	}
      	else // without mask
      	{
      		while ( voxelList.isEmpty() == false )
      		{
      			IJ.showProgress( numVoxels-voxelList.size(), numVoxels );

      			final VoxelRecord voxelRecord = voxelList.poll();
      			final Cursor3D p = voxelRecord.getCursor();
	    		final int i = p.getX();
	    		final int j = p.getY();
	    		final int k = p.getZ();


      			//double voxelValue = voxelRecord.getValue(); //inputStack.getVoxel( i, j, k );

      			// Read neighbor coordinates
      			neigh.setCursor( p );
      			ArrayList <Integer> neighborLabels = new ArrayList<Integer>();      			
      			for( Cursor3D c : neigh.getNeighbors() )			       		
      			{      				      				
      				// Look in neighborhood for labeled voxels with
      				// smaller or equal original value
      				int u = c.getX();
      				int v = c.getY();
      				int w = c.getZ();
      				if ( u >= 0 && u < size1 && v >= 0 && v < size2 && w >= 0 && w < size3 )
      				{
      					// Unlabeled neighbors go into the queue if they are not there yet 
      					if ( tabLabels[ u ][ v ][ w ] == 0 )
      					{
      						voxelList.add( new VoxelRecord( u, v, w, inputStack.getVoxel( u, v, w ) ));
      						tabLabels[u][v][w] = INQUEUE;
      					}
      					else if ( tabLabels[ u ][ v ][ w ] > 0 
      							&& neighborLabels.contains(tabLabels[ u ][ v ][ w ]) == false)
      					{
      						 neighborLabels.add( tabLabels[ u ][ v ][ w ] );
      					}
      				}
      			}
      			//  if the neighbors of the extracted voxel that have already been labeled 
      			// all have the same label, then the voxel is labeled with their label
      			if( neighborLabels.size() == 1 )
      				tabLabels[ i ][ j ][ k ] = neighborLabels.get( 0 );

      		}
      	}

		final long end = System.currentTimeMillis();
		IJ.log("  Flooding took: " + (end-start) + " ms");
		
		// Create result label image
		ImageStack labelStack = markerImage.duplicate().getStack();
	    
	    for (int i = 0; i < size1; ++i)
	      for (int j = 0; j < size2; ++j)
	        for (int k = 0; k < size3; ++k)
	            labelStack.setVoxel( i, j, k, tabLabels[i][j][k] );
	    final ImagePlus ws = new ImagePlus( "watershed", labelStack );
	    ws.setCalibration( inputImage.getCalibration() );
	    return ws;
	}
	
	
	/**
	 * Extract voxel values from input and seed images
	 * 
	 * @param inputStack input stack
	 * @param seedStack seed stack
	 * @param tabLabels output label array
	 * @return priority queue of voxels neighboring the seeds
	 */
	public PriorityQueue<VoxelRecord> extractVoxelValuesPriorityQueue(
			final ImageStack inputStack,
			final ImageStack seedStack,
			final int[][][] tabLabels) 
	{
		
		final int size1 = inputStack.getWidth();
	    final int size2 = inputStack.getHeight();
	    final int size3 = inputStack.getSize();
		
	            
        final PriorityQueue<VoxelRecord> voxelList = new PriorityQueue<VoxelRecord>();
        
        // Auxiliary cursor to visit neighbors
	    final Cursor3D cursor = new Cursor3D(0, 0, 0);
      	
      	// Check connectivity
       	final Neighborhood3D neigh = connectivity == 26 ? 
       			new Neighborhood3DC26() : new Neighborhood3DC6();
	    
		if( null != maskImage ) // apply mask
		{
			final ImageStack mask = maskImage.getImageStack();
						
			for (int z = 0; z < size3; ++z)	
			{
				IJ.showProgress( z+1, size3 );

				final ImageProcessor ipMask = mask.getProcessor( z+1 );
				final ImageProcessor ipSeed = seedStack.getProcessor( z+1 );

				for( int x = 0; x < size1; ++x )
					for( int y = 0; y < size2; ++y )
						if( ipMask.getf( x, y ) > 0 )
						{
							int label = (int) ipSeed.getf( x, y );
							if( label > 0 )
							{								
								cursor.set( x, y, z );
								neigh.setCursor( cursor );

								// add unlabeled neighbors to priority queue
								for( Cursor3D c : neigh.getNeighbors() )			       		
								{
									int u = c.getX();
									int v = c.getY();
									int w = c.getZ();
									if ( u >= 0 && u < size1 && 
											v >= 0 && v < size2 && 
											w >= 0 && w < size3 &&
											(int) seedStack.getVoxel( u, v, w ) == 0 &&
											tabLabels[ u ][ v ][ w ] != INQUEUE )															 
									{
										voxelList.add( new VoxelRecord( u, v, w, inputStack.getVoxel( u, v, w ) ) );
										tabLabels[ u ][ v ][ w ] = INQUEUE;
									}

								}
								tabLabels[x][y][z] = label;
							}
						}
			}
		}							
		else // without mask
		{
			for (int z = 0; z < size3; ++z)	
			{
				IJ.showProgress( z+1, size3 );

				final ImageProcessor ipSeed = seedStack.getProcessor( z+1 );

				for( int x = 0; x < size1; ++x )
					for( int y = 0; y < size2; ++y )
					{
						int label = (int) ipSeed.getf( x, y );
						if( label > 0 )
						{
							cursor.set( x, y, z );
							neigh.setCursor( cursor );

							// add unlabeled neighbors to priority queue
							for( Cursor3D c : neigh.getNeighbors() )			       		
							{
								int u = c.getX();
								int v = c.getY();
								int w = c.getZ();
								if ( u >= 0 && u < size1 && 
										v >= 0 && v < size2 && 
										w >= 0 && w < size3 &&
										(int) seedStack.getVoxel( u, v, w ) == 0 &&
										tabLabels[ u ][ v ][ w ] != INQUEUE )															 
								{
									voxelList.add( new VoxelRecord( u, v, w, inputStack.getVoxel( u, v, w ) ) );
									tabLabels[ u ][ v ][ w ] = INQUEUE;
								}

							}
							tabLabels[x][y][z] = label;
						}
					}
			}

		}


		IJ.showProgress(1.0);

		return voxelList;
	}

	/**
	 * Extract voxel values from input and seed images
	 * 
	 * @param inputStack input stack
	 * @param seedStack seed stack
	 * @param tabLabels output label array
	 * @return list of input voxel values
	 */
	public LinkedList<VoxelRecord> extractVoxelValues(
			final ImageStack inputStack,
			final ImageStack seedStack,
			final int[][][] tabLabels) 
	{
		
		final int size1 = inputStack.getWidth();
	    final int size2 = inputStack.getHeight();
	    final int size3 = inputStack.getSize();
		
	    final AtomicInteger ai = new AtomicInteger(0);
        final int n_cpus = Prefs.getThreads();
        
        final int dec = (int) Math.ceil((double) size3 / (double) n_cpus);
        
        Thread[] threads = ThreadUtil.createThreadArray( n_cpus );
        
        final LinkedList<VoxelRecord>[] lists = new LinkedList[ n_cpus ];
	    
		if( null != maskImage )
		{
			final ImageStack mask = maskImage.getImageStack();
			
			for (int ithread = 0; ithread < threads.length; ithread++) 
			{
				lists[ ithread ] = new LinkedList<VoxelRecord>();
				
				threads[ithread] = new Thread() {
					public void run() {
						for (int k = ai.getAndIncrement(); k < n_cpus; k = ai.getAndIncrement()) 
						{
							int zmin = dec * k;
							int zmax = dec * ( k + 1 );
							if (zmin<0)
								zmin = 0;
							if (zmax > size3)
								zmax = size3;

							for (int z = zmin; z < zmax; ++z)	
							{
								if (zmin==0) 
									IJ.showProgress(z+1, zmax);

								final ImageProcessor ipMask = mask.getProcessor( z+1 );
								final ImageProcessor ipInput = inputStack.getProcessor( z+1 );
								final ImageProcessor ipSeed = seedStack.getProcessor( z+1 );

								for( int x = 0; x < size1; ++x )
									for( int y = 0; y < size2; ++y )
										if( ipMask.getf( x, y ) > 0 )
										{
											lists[k].addLast( new VoxelRecord( x, y, z, ipInput.getf( x, y )));
											tabLabels[x][y][z] = (int) ipSeed.getf( x, y );
										}
							}

						}
					}
				};
			}
			ThreadUtil.startAndJoin(threads);			
		}
		else
		{
			for (int ithread = 0; ithread < threads.length; ithread++) 
			{
				lists[ ithread ] = new LinkedList<VoxelRecord>();
				
				threads[ithread] = new Thread() {
					public void run() {
						for (int k = ai.getAndIncrement(); k < n_cpus; k = ai.getAndIncrement()) 
						{
							int zmin = dec * k;
							int zmax = dec * ( k + 1 );
							if (zmin<0)
								zmin = 0;
							if (zmax > size3)
								zmax = size3;

							for (int z = zmin; z < zmax; ++z)	
							{
								if (zmin==0) 
									IJ.showProgress(z+1, zmax);

								final ImageProcessor ipInput = inputStack.getProcessor( z+1 );
								final ImageProcessor ipSeed = seedStack.getProcessor( z+1 );

								for( int x = 0; x < size1; ++x )
									for( int y = 0; y < size2; ++y )
									{
										lists[k].addLast( new VoxelRecord( x, y, z, ipInput.getf( x, y )));
										tabLabels[x][y][z] = (int) ipSeed.getf( x, y );
									}
							}

						}
					}
				};
			}
			ThreadUtil.startAndJoin(threads);			
		}
		
		final LinkedList<VoxelRecord> voxelList = lists[ 0 ];
		for (int ithread = 1; ithread < threads.length; ithread++)
			voxelList.addAll(lists[ ithread ]);
		
		IJ.showProgress(1.0);
		
		return voxelList;
	}

	

}
