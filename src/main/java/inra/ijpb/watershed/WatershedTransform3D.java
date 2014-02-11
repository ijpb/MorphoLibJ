package inra.ijpb.watershed;

/**
 *
 * License: GPL
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Author: Ignacio Arganda-Carreras
 */


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
import inra.ijpb.data.VoxelRecord;

/**
 * Class to apply the watershed algorithm in 3D to an image. It allows specifying the
 * input image, the image to read the seeds from and a mask image. 
 */
public class WatershedTransform3D 
{
	/** input image (usually a gradient image) */
	ImagePlus inputImage = null;
	/** image containing the seeds to start the watershed */
	ImagePlus seedImage = null;
	/** binary mask to restrict the region of interest */
	ImagePlus maskImage = null;
	
	static final int INQUEUE = -3;
	
	/**
	 * Construct a watershed transform
	 * 
	 * @param input input image (usually a gradient image)
	 * @param seed image containing the seeds to start the watershed
	 * @param mask binary mask to restrict the region of interest (null to use whole input image)
	 */
	public WatershedTransform3D(
			final ImagePlus input,
			final ImagePlus seed,
			final ImagePlus mask )
	{
		this.inputImage = input;
		this.seedImage = seed;
		this.maskImage = mask;
	}
	
	/**
	 * Apply watershed transform on inputImage, using the seeds 
	 * from seedImage and the mask of maskImage.
	 * @return watershed domains image
	 */
	public ImagePlus apply()
	{
		final ImageStack inputStack = inputImage.getStack();
	    final int size1 = inputStack.getWidth();
	    final int size2 = inputStack.getHeight();
	    final int size3 = inputStack.getSize();
	    
		// list of original voxels values and corresponding coordinates
		LinkedList<VoxelRecord> voxelList = null;
		
		final int[][][] tabLabels = new int[ size1 ][ size2 ][ size3 ]; 
		
		// Make list of voxels and sort it in ascending order
		IJ.showStatus( "Extracting voxel values..." );
		IJ.log("  Extracting voxel values..." );
		final long t0 = System.currentTimeMillis();
		
		voxelList = extractVoxelValues( inputStack, seedImage.getStack(), tabLabels );
						
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
	       		final int[] coord = voxelRecord.getCoordinates();
	       		final int i = coord[0];
	       		final int j = coord[1];
	       		final int k = coord[2];
	       		
	       		// If the voxel is unlabeled
				if( tabLabels[ i ][ j ][ k ] == 0 )
	       		{
			       	found = false;
			       	double voxelValue = voxelRecord.getValue();
			       	// Look in neighborhood for labeled voxels with
			       	// smaller or equal original value
			       	for (int u = i-1; u <= i+1; ++u) 
			        	for (int v = j-1; v <= j+1; ++v) 
					        for (int w = k-1; w <= k+1; ++w) 
	          				{
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
		ImageStack labelStack = seedImage.duplicate().getStack();
	    
	    for (int i = 0; i < size1; ++i)
	      for (int j = 0; j < size2; ++j)
	        for (int k = 0; k < size3; ++k)
	            labelStack.setVoxel( i, j, k, tabLabels[i][j][k] );
	    final ImagePlus ws = new ImagePlus( "watershed", labelStack );
	    ws.setCalibration( inputImage.getCalibration() );
	    return ws;
	}

	
	/**
	 * Apply watershed transform on inputImage, using the seeds 
	 * from seedImage and the mask of maskImage.
	 * @return watershed domains image
	 */
	public ImagePlus applyWithPriorityQueue()
	{
		final ImageStack inputStack = inputImage.getStack();
	    final int size1 = inputStack.getWidth();
	    final int size2 = inputStack.getHeight();
	    final int size3 = inputStack.getSize();
	    
		// list of original voxels values and corresponding coordinates
		PriorityQueue<VoxelRecord> voxelList = null;
		
		final int[][][] tabLabels = new int[ size1 ][ size2 ][ size3 ]; 
		
		// Make list of voxels and sort it in ascending order
		IJ.showStatus( "Extracting voxel values..." );
		IJ.log("  Extracting voxel values..." );
		final long t0 = System.currentTimeMillis();
		
		voxelList = extractVoxelValuesPriorityQueue( inputStack, seedImage.getStack(), tabLabels );
						
		final long t1 = System.currentTimeMillis();		
		IJ.log("  Extraction took " + (t1-t0) + " ms.");
					    
		// Watershed
	    final long start = System.currentTimeMillis();

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
      			final int[] coord = voxelRecord.getCoordinates();
      			final int i = coord[0];
      			final int j = coord[1];
      			final int k = coord[2];


      			double voxelValue = voxelRecord.getValue(); //inputStack.getVoxel( i, j, k );

      			// Look in neighborhood 
      			for (int u = i-1; u <= i+1; ++u) 
      				for (int v = j-1; v <= j+1; ++v) 
      					for (int w = k-1; w <= k+1; ++w) 
      					{
      						if ( u >= 0 && u < size1 && v >= 0 && v < size2 && w >= 0 && w < size3 )
      						{
      							// Unlabeled neighbors go into the queue if they are not there yet 
      							if ( tabLabels[u][v][w] == 0 && maskStack.getVoxel(u, v, w) > 0)
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
      			final int[] coord = voxelRecord.getCoordinates();
      			final int i = coord[0];
      			final int j = coord[1];
      			final int k = coord[2];


      			double voxelValue = voxelRecord.getValue(); //inputStack.getVoxel( i, j, k );

      			// Look in neighborhood 
      			for (int u = i-1; u <= i+1; ++u) 
      				for (int v = j-1; v <= j+1; ++v) 
      					for (int w = k-1; w <= k+1; ++w) 
      					{
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
		ImageStack labelStack = seedImage.duplicate().getStack();
	    
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
								// add unlabeled neighbors to priority queue
								for (int u = x-1; u <= x+1; ++u) 
									for (int v = y-1; v <= y+1; ++v) 
										for (int w = z-1; w <= z+1; ++w) 
										{
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
							// add unlabeled neighbors to priority queue
							for (int u = x-1; u <= x+1; ++u) 
								for (int v = y-1; v <= y+1; ++v) 
									for (int w = z-1; w <= z+1; ++w) 
									{
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

	
	
	
} // end class WatershedTransform3D
