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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.util.ThreadUtil;
import inra.ijpb.data.Cursor3D;
import inra.ijpb.data.Neighborhood3D;
import inra.ijpb.data.Neighborhood3DC6;
import inra.ijpb.data.Neighborhood3DC26;
import inra.ijpb.data.VoxelRecord;

/**
 * Class to apply the watershed algorithm in 3D to an image. 
 * It allows specifying a binary mask to restrict the regions
 * of interest. 
 * 
 * @author Ignacio Arganda-Carreras
 */
public class WatershedTransform3D 
{
	/** input image (usually a gradient image) */
	ImagePlus inputImage = null;
	
	/** binary mask to restrict the region of interest */
	ImagePlus maskImage = null;
	
	/** voxel connectivity */
	int connectivity = 6;
	
	/** initial value of a threshold level */
	static final int MASK = -2;
	/** value of voxels belonging to watersheds */
	static final int WSHED = 0;
	/** initial value of output voxels */
	static final int INIT = -1;	
	/** value assigned to voxels put into the queue */
	static final int INQUEUE = -3;
	
	
	/**
	 * Construct a watershed transform
	 * 
	 * @param input input image (usually a gradient image)
	 * @param mask binary mask to restrict the region of interest (null to use whole input image)
	 */
	public WatershedTransform3D(
			final ImagePlus input,
			final ImagePlus mask )
	{
		this.inputImage = input;
		this.maskImage = mask;
	}
	
	/**
	 * Construct a watershed transform
	 * 
	 * @param input grayscale image (usually a gradient image)
	 * @param mask binary mask to restrict the region of interest (null to use whole input image)
	 * @param connectivity voxel connectivity (6 or 26)
	 */
	public WatershedTransform3D(
			final ImagePlus input,
			final ImagePlus mask,
			final int connectivity )
	{
		this.inputImage = input;
		this.maskImage = mask;
		this.connectivity = connectivity;
	}
	
	/**
	 * Get the voxel connectivity (6 or 26)
	 * @return voxel connectivity
	 */
	public int getConnectivity() {
		return this.connectivity;
	}
	
	/**
	 * Set the voxel connectivity (6 or 26)
	 * @param conn voxel connectivity
	 */
	public void setConnectivity(int conn) {
		this.connectivity = conn;
	}
	
	/**
	 * Apply fast watersheds using flooding simulations, as described
	 * by Soille, Pierre, and Luc M. Vincent. "Determining watersheds 
	 * in digital pictures via flooding simulations." Lausanne-DL 
	 * tentative. International Society for Optics and Photonics, 1990.
	 *
	 * @return image of labeled catchment basins (with dams)
	 */
	public ImagePlus apply()
	{
		if( null != this.maskImage )
			return applyWithMask();
		else
			return applyWithoutMask();

	}

	/**
	 * Apply fast watersheds using flooding simulations, as described
	 * by Soille, Pierre, and Luc M. Vincent. "Determining watersheds 
	 * in digital pictures via flooding simulations." Lausanne-DL 
	 * tentative. International Society for Optics and Photonics, 1990.
	 *
	 * @return image of labeled catchment basins (with dams)
	 */
	private ImagePlus applyWithoutMask() 
	{
		final ImageStack inputStack = inputImage.getStack();
	    final int size1 = inputStack.getWidth();
	    final int size2 = inputStack.getHeight();
	    final int size3 = inputStack.getSize();
	    	    
	    // output labels
	    final int[][][] tabLabels = new int[ size1 ][ size2 ][ size3 ]; 
	    
	    // value INIT is assigned to each voxel of the output labels
	    for( int i=0; i<size1; i++ )
	    	for( int j=0; j<size2; j++ )
	    		Arrays.fill( tabLabels[i][j], INIT );
	    
	    int currentLabel = 0;
	    
	    boolean flag = false;	    
	    
	    // Make list of voxels and sort it in ascending order
	    IJ.showStatus( "Extracting voxel values..." );
	    IJ.log("  Extracting voxel values..." );
	    final long t0 = System.currentTimeMillis();

	    // list of original voxels values and corresponding coordinates
	    ArrayList<VoxelRecord> voxelList = extractVoxelValues( inputStack );

	    final long t1 = System.currentTimeMillis();		
	    IJ.log("  Extraction took " + (t1-t0) + " ms.");
	    IJ.log("  Sorting voxels by value..." );
	    IJ.showStatus("Sorting voxels by value...");
	    Collections.sort( voxelList );
	    final long t2 = System.currentTimeMillis();
	    IJ.log("  Sorting took " + (t2-t1) + " ms.");
	    
	    IJ.log( "  Flooding..." );
	    IJ.showStatus( "Flooding..." );
	    final long start = System.currentTimeMillis();
	    
      	// Check connectivity
       	final Neighborhood3D neigh = connectivity == 26 ? 
       									new Neighborhood3DC26() : new Neighborhood3DC6();
	    
	    
	    LinkedList<Cursor3D> fifo = new LinkedList<Cursor3D>();
	    
	    int currentIndex = 0;
	    int heightIndex1 = 0;
        int heightIndex2 = 0;
        
	    // for h <- h_min to h_max; geodesic SKIZ of level h-1 inside level h
	    while( currentIndex < voxelList.size() )
	    {	    	
	    	final double h = voxelList.get( currentIndex ).getValue();	    	
	    		    		    		    	
	    	for(int voxelIndex = heightIndex1; voxelIndex < voxelList.size(); voxelIndex ++)
	    	{
	    		final VoxelRecord voxelRecord = voxelList.get( voxelIndex );
	    			    		
	    		if( voxelRecord.getValue() != h )
	    		{
	    			// this voxel is at level h+1
	    			heightIndex1 = voxelIndex;
	    			break;
	    		}
	    			    		
	    		final Cursor3D p = voxelRecord.getCursor();
	    		final int i = p.getX();
	    		final int j = p.getY();
	    		final int k = p.getZ();
	    			    		
	    		// set label to MASK
	    		tabLabels[ i ][ j ][ k ] = MASK;

	    		// Read neighbor coordinates	    		
	    		neigh.setCursor( p );
	    		for( Cursor3D c : neigh.getNeighbors() )			       		
	    		{       			
	    			int u = c.getX();
	    			int v = c.getY();
	    			int w = c.getZ();

	    			// initialize queue with neighbors at level h of current basins or watersheds
	    			if ( u >= 0 && u < size1 && v >= 0 && v < size2 && w >= 0 && w < size3
	    					&&   tabLabels[ u ][ v ][ w ] >= WSHED ) 
	    				//&&  ( tabLabels[ u ][ v ][ w ] > 0 || tabLabels[ u ][ v ][ w ] == WSHED ) )
	    				{
	    					fifo.addLast( p );
	    					tabLabels[ i ][ j ][ k ] = INQUEUE;
	    					break;
	    				}	    			
	    		}// end for	    	
	    	}// end for

	    	while( fifo.isEmpty() == false )
	    	{
	    		// retrieve point p
	    		final Cursor3D p = fifo.poll();	    		
	    		final int i = p.getX();
	    		final int j = p.getY();
	    		final int k = p.getZ();

	    		// Read neighbor coordinates
	    		neigh.setCursor( p );

	    		for( Cursor3D c : neigh.getNeighbors() )			       		
	    		{
	    			// labeling current point by inspecting neighbors
	    			int u = c.getX();
	    			int v = c.getY();
	    			int w = c.getZ();

	    			if ( u >= 0 && u < size1 && v >= 0 && v < size2 && w >= 0 && w < size3 )
	    			{
	    				if ( tabLabels[ u ][ v ][ w ] > 0 ) // i.e. the voxel belongs to an already labeled basin
	    				{
	    					if ( tabLabels[ i ][ j ][ k ] == INQUEUE || (tabLabels[ i ][ j ][ k ] == WSHED && flag == true ) )
	    					{
	    						tabLabels[ i ][ j ][ k ] = tabLabels[ u ][ v ][ w ];
	    					}
	    					else if ( tabLabels[ i ][ j ][ k ] > 0 && tabLabels[ i ][ j ][ k ] != tabLabels[ u ][ v ][ w ] )
	    					{
	    						tabLabels[ i ][ j ][ k ] = WSHED;
	    						flag = false;
	    					}       					
	    				}
	    				else if ( tabLabels[ u ][ v ][ w ] == WSHED && tabLabels[ i ][ j ][ k ] == INQUEUE )
	    				{
	    					tabLabels[ i ][ j ][ k ] = WSHED;
	    					flag = true;
	    				}
	    				else if ( tabLabels[ u ][ v ][ w ] == MASK )
	    				{
	    					tabLabels[ u ][ v ][ w ] = INQUEUE;
	    					fifo.addLast( c );

	    				}
	    			}       			       			
	    		}	    	
	    	}

	    	// check for new minima at level h
	    		    	
	    	for(int voxelIndex = heightIndex2; voxelIndex < voxelList.size(); voxelIndex ++, currentIndex++)
	    	{
	    		final VoxelRecord voxelRecord = voxelList.get( voxelIndex );	    			    		
	    		
	    		if( voxelRecord.getValue() != h )
	    		{
	    			// this voxel is at level h+1
	    			heightIndex2 = voxelIndex;
	    			break;
	    		}
	    			    		
	    		final Cursor3D p = voxelRecord.getCursor();
	    		final int i = p.getX();
	    		final int j = p.getY();
	    		final int k = p.getZ();
	    		

	    		if ( tabLabels[ i ][ j ][ k ] == MASK ) // the voxel is inside a new minimum
	    		{
	    			currentLabel ++;
	    			fifo.addLast( p );
	    			tabLabels[ i ][ j ][ k ] = currentLabel;
	    			
	    			while( fifo.isEmpty() == false )
	    	    	{
	    				final Cursor3D p2 = fifo.poll();

	    	    		// Read neighbor coordinates
	    	    		neigh.setCursor( p2 );

	    	    		for( Cursor3D c : neigh.getNeighbors() ) // inspect neighbors of p2		       		
	    	    		{       			
	    	    			int u = c.getX();
	    	    			int v = c.getY();
	    	    			int w = c.getZ();
	    	    			
	    	    			if ( u >= 0 && u < size1 && v >= 0 && v < size2 && w >= 0 && w < size3 
	    	    					&&  tabLabels[ u ][ v ][ w ] == MASK )
	    	    			{
	    	    				fifo.addLast( c );
	    	    				tabLabels[ u ][ v ][ w ] = currentLabel;
	    	    			}	    	    				    	    			
	    	    		}// end for
	    	    	}// end while
	    		}// end if	    		
	    	}// end for
	    	
	    	//currentIndex = heightIndex2; // new h index
	    	
	    	//IJ.showProgress(currentIndex, voxelList.size());
	    	
	    }// end while (flooding)
	    
	    final long end = System.currentTimeMillis();
		IJ.log("  Flooding took: " + (end-start) + " ms");
	    
	    // Create result label image
	    ImageStack labelStack = new ImageStack( size1, size2 );

	    for (int k = 0; k < size3; ++k)
	    {
	    	
	    	FloatProcessor fp = new FloatProcessor( size1, size2 );
	    	for (int i = 0; i < size1; ++i)
	    		for (int j = 0; j < size2; ++j)
	    			fp.setf( i, j, tabLabels[i][j][k] );
	    	labelStack.addSlice( fp );
	    	
	    }		
	    			
	    final ImagePlus ws = new ImagePlus( "watershed", labelStack );
	    ws.setCalibration( inputImage.getCalibration() );
	    return ws;
	}
		

	/**
	 * Extract voxel values from input image
	 * 
	 * @param inputStack input stack
	 * @return list of input voxel values
	 */
	public ArrayList<VoxelRecord> extractVoxelValues(
			final ImageStack inputStack ) 
	{
		
		final int size1 = inputStack.getWidth();
	    final int size2 = inputStack.getHeight();
	    final int size3 = inputStack.getSize();
		
	    final AtomicInteger ai = new AtomicInteger(0);
        final int n_cpus = Prefs.getThreads();
        
        final int dec = (int) Math.ceil((double) size3 / (double) n_cpus);
        
        Thread[] threads = ThreadUtil.createThreadArray( n_cpus );
        
        final ArrayList<VoxelRecord>[] lists = new ArrayList[ n_cpus ];
	    
		if( null != maskImage )
		{
			final ImageStack mask = maskImage.getImageStack();
			
			for (int ithread = 0; ithread < threads.length; ithread++) 
			{
				lists[ ithread ] = new ArrayList<VoxelRecord>();
				
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
								
								for( int x = 0; x < size1; ++x )
									for( int y = 0; y < size2; ++y )
										if( ipMask.getf( x, y ) > 0 )
										{
											lists[k].add( new VoxelRecord( x, y, z, ipInput.getf( x, y )));								
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
				lists[ ithread ] = new ArrayList<VoxelRecord>();
				
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

								for( int x = 0; x < size1; ++x )
									for( int y = 0; y < size2; ++y )
									{
										lists[k].add( new VoxelRecord( x, y, z, ipInput.getf( x, y )));
									}
							}

						}
					}
				};
			}
			ThreadUtil.startAndJoin(threads);			
		}
		
		final ArrayList<VoxelRecord> voxelList = lists[ 0 ];
		for (int ithread = 1; ithread < threads.length; ithread++)
			voxelList.addAll(lists[ ithread ]);
		
		IJ.showProgress(1.0);
		
		return voxelList;
	}

	/**
	 * Apply fast watersheds using flooding simulations, as described
	 * by Soille, Pierre, and Luc M. Vincent. "Determining watersheds 
	 * in digital pictures via flooding simulations." Lausanne-DL 
	 * tentative. International Society for Optics and Photonics, 1990.
	 * This implementation restricts the watershed to the regions in
	 * white in the binary mask.
	 *
	 * @return image of labeled catchment basins (with dams)
	 */
	private ImagePlus applyWithMask() 
	{
		final ImageStack inputStack = inputImage.getStack();
	    final int size1 = inputStack.getWidth();
	    final int size2 = inputStack.getHeight();
	    final int size3 = inputStack.getSize();
	    
	    final ImageStack mask = maskImage.getImageStack();
	    	    
	    // output labels
	    final int[][][] tabLabels = new int[ size1 ][ size2 ][ size3 ]; 
	    
	    // value INIT is assigned to each voxel of the output labels
	    // inside the mask
	    for( int i=0; i<size1; i++ )
	    	for( int j=0; j<size2; j++ )
	    		for( int k=0; k<size3; k++ )
	    			if( mask.getVoxel( i, j, k ) > 0 )
	    				tabLabels[i][j][k] = INIT;
	    
	    int currentLabel = 0;
	    
	    boolean flag = false;	    
	    
	    // Make list of voxels and sort it in ascending order
	    IJ.showStatus( "Extracting voxel values..." );
	    IJ.log("  Extracting voxel values..." );
	    final long t0 = System.currentTimeMillis();

	    // list of original voxels values and corresponding coordinates
	    ArrayList<VoxelRecord> voxelList = extractVoxelValues( inputStack );

	    final long t1 = System.currentTimeMillis();		
	    IJ.log("  Extraction took " + (t1-t0) + " ms.");
	    IJ.log("  Sorting voxels by value..." );
	    IJ.showStatus("Sorting voxels by value...");
	    Collections.sort( voxelList );
	    final long t2 = System.currentTimeMillis();
	    IJ.log("  Sorting took " + (t2-t1) + " ms.");
	    
	    IJ.log( "  Flooding..." );
	    IJ.showStatus( "Flooding..." );
	    final long start = System.currentTimeMillis();
	    
	    // Auxiliary cursor to visit neighbors
	    final Cursor3D cursor = new Cursor3D( 0, 0, 0 );
      	
      	// Check connectivity
       	final Neighborhood3D neigh = connectivity == 26 ? 
       									new Neighborhood3DC26() : new Neighborhood3DC6();
	    
	    
	    PriorityQueue<VoxelRecord> fifo = new PriorityQueue<VoxelRecord>();
	    
	    int currentIndex = 0;
	    
	    // for h <- h_min to h_max
	    while( currentIndex < voxelList.size() )
	    {
	    	
	    	final double h = voxelList.get( currentIndex ).getValue();	    	
	    	
	    	int indexToVisit = currentIndex;
	    	
	    	double retrievedH = h;
	    	
	    	while( retrievedH == h && indexToVisit < voxelList.size() )
	    	{
	    		final VoxelRecord voxelRecord = voxelList.get( indexToVisit );
	    		final Cursor3D p = voxelRecord.getCursor();
	    		final int i = p.getX();
	    		final int j = p.getY();
	    		final int k = p.getZ();
	    		
	    		retrievedH = voxelRecord.getValue();
	    		indexToVisit ++;

	    		tabLabels[ i ][ j ][ k ] = MASK;

	    		// Read neighbor coordinates
	    		cursor.set( i, j, k );
	    		neigh.setCursor( cursor );

	    		for( Cursor3D c : neigh.getNeighbors() )			       		
	    		{       			
	    			int u = c.getX();
	    			int v = c.getY();
	    			int w = c.getZ();

	    			if ( u >= 0 && u < size1 && v >= 0 && v < size2 && w >= 0 && w < size3 
	    					&& mask.getVoxel(u, v, w) > 0 )
	    			{       			
	    				if ( tabLabels[ u ][ v ][ w ] > 0 || tabLabels[ u ][ v ][ w ] == WSHED )
	    				{
	    					fifo.add( new VoxelRecord( i, j, k, inputStack.getVoxel( i, j, k ) ));
	    					tabLabels[ i ][ j ][ k ] = INQUEUE;
	    					break;
	    				}

	    			}
	    		}   	    	
	    	}

	    	while( fifo.isEmpty() == false )
	    	{
	    		final VoxelRecord voxelRecord = fifo.poll();
	    		final Cursor3D p = voxelRecord.getCursor();
	    		final int i = p.getX();
	    		final int j = p.getY();
	    		final int k = p.getZ();

	    		// Read neighbor coordinates
	    		cursor.set( i, j, k );
	    		neigh.setCursor( cursor );

	    		for( Cursor3D c : neigh.getNeighbors() )			       		
	    		{       			
	    			int u = c.getX();
	    			int v = c.getY();
	    			int w = c.getZ();

	    			if ( u >= 0 && u < size1 && v >= 0 && v < size2 && w >= 0 && w < size3 
	    					&& mask.getVoxel(u, v, w) > 0 )
	    			{
	    				if ( tabLabels[ u ][ v ][ w ] > 0 ) // i.e. the voxel belongs to an already labeled basin
	    				{
	    					if ( tabLabels[ i ][ j ][ k ] == INQUEUE || (tabLabels[ i ][ j ][ k ] == WSHED && flag == true ) )
	    					{
	    						tabLabels[ i ][ j ][ k ] = tabLabels[ u ][ v ][ w ];
	    					}
	    					else if ( tabLabels[ i ][ j ][ k ] > 0 && tabLabels[ i ][ j ][ k ] != tabLabels[ u ][ v ][ w ] )
	    					{
	    						tabLabels[ i ][ j ][ k ] = WSHED;
	    						flag = false;
	    					}       					
	    				}
	    				else if ( tabLabels[ u ][ v ][ w ] == WSHED &&  tabLabels[ i ][ j ][ k ] == INQUEUE )
	    				{
	    					tabLabels[ i ][ j ][ k ] = WSHED;
	    					flag = true;
	    				}
	    				else if ( tabLabels[ u ][ v ][ w ] == MASK )
	    				{
	    					tabLabels[ u ][ v ][ w ] = INQUEUE;
	    					fifo.add( new VoxelRecord( u, v, w, inputStack.getVoxel( u, v, w ) ));

	    				}
	    			}       			       			
	    		}	    	
	    	}

	    	// check for new minima
	    	indexToVisit = currentIndex;	    	
	    	retrievedH = h;
	    	
	    	while( retrievedH == h && indexToVisit < voxelList.size() )
	    	{
	    		final VoxelRecord voxelRecord = voxelList.get( indexToVisit );
	    		final Cursor3D p = voxelRecord.getCursor();
	    		final int i = p.getX();
	    		final int j = p.getY();
	    		final int k = p.getZ();
	    		
	    		retrievedH = voxelRecord.getValue();
	    		indexToVisit ++;
	    		
	    		if ( tabLabels[ i ][ j ][ k ] == MASK )
	    		{
	    			currentLabel ++;
	    			fifo.add( new VoxelRecord( i, j, k, inputStack.getVoxel( i, j, k ) ));
	    			tabLabels[ i ][ j ][ k ] = currentLabel;
	    			
	    			while( fifo.isEmpty() == false )
	    	    	{
	    				final VoxelRecord voxelRecord2 = fifo.poll();
	    				final Cursor3D p2 = voxelRecord2.getCursor();
	    	    		final int i2 = p2.getX();
	    	    		final int j2 = p2.getY();
	    	    		final int k2 = p2.getZ();

	    	    		// Read neighbor coordinates
	    	    		cursor.set( i2, j2, k2 );
	    	    		neigh.setCursor( cursor );

	    	    		for( Cursor3D c : neigh.getNeighbors() )			       		
	    	    		{       			
	    	    			int u = c.getX();
	    	    			int v = c.getY();
	    	    			int w = c.getZ();
	    	    			
	    	    			if ( u >= 0 && u < size1 && v >= 0 && v < size2 && w >= 0 && w < size3 
	    	    					&& mask.getVoxel(u, v, w) > 0 )
	    	    			{
	    	    				if ( tabLabels[ u ][ v ][ w ] == MASK )
	    	    				{
	    	    					fifo.add( new VoxelRecord( u, v, w, inputStack.getVoxel( u, v, w ) ));
	    	    					tabLabels[ u ][ v ][ w ] = currentLabel;
	    	    				}
	    	    			}
	    	    			
	    	    		}
	    	    	}
	    		}	    			    		
	    	}
	    	
	    	currentIndex = indexToVisit; // new h index
	    	
	    	IJ.showProgress(currentIndex, voxelList.size());	    		    		    	
	    }
	    
	    final long end = System.currentTimeMillis();
		IJ.log("  Flooding took: " + (end-start) + " ms");
	    
	    // Create result label image
	    ImageStack labelStack = new ImageStack( size1, size2 );

	    for (int k = 0; k < size3; ++k)
	    {
	    	
	    	FloatProcessor fp = new FloatProcessor( size1, size2 );
	    	for (int i = 0; i < size1; ++i)
	    		for (int j = 0; j < size2; ++j)
	    			fp.setf( i, j, tabLabels[i][j][k] );
	    	labelStack.addSlice( fp );
	    	
	    }		
	    			
	    final ImagePlus ws = new ImagePlus( "watershed", labelStack );
	    ws.setCalibration( inputImage.getCalibration() );
	    return ws;
	}
	
} // end class WatershedTransform3D

