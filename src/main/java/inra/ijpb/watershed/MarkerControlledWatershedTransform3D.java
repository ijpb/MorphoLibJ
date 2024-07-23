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
package inra.ijpb.watershed;

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
import ij.process.ImageProcessor;
import ij.util.ThreadUtil;
import inra.ijpb.data.Cursor3D;
import inra.ijpb.data.Neighborhood3D;
import inra.ijpb.data.Neighborhood3DC26;
import inra.ijpb.data.Neighborhood3DC6;
import inra.ijpb.data.VoxelRecord;
import inra.ijpb.data.image.Images3D;

/**
 * Marker-controlled version of the watershed transform (works for 2D and 3D images).
 * 
 * References:
 * [1] Fernand Meyer and Serge Beucher. "Morphological segmentation."
 *     Journal of visual communication and image representation 1.1 (1990): 21-46.
 * [2] Peer Neubert and Peter Protzel. "Compact Watershed and Preemptive SLIC:
 *     On improving trade-offs of superpixel segmentation algorithms."
 *     22nd international conference on pattern recognition. IEEE, 2014.
 * 
 * @author Ignacio Arganda-Carreras
 *
 */
public class MarkerControlledWatershedTransform3D extends WatershedTransform3D
{
	/** image containing the labeled markers to start the watershed */
	ImagePlus markerImage = null;
	/** compactness constraint, parameter c in Compact Watershed algorithm [2] */
	double compactness = 0.0;

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
	 * Initialize a marker-controlled watershed transform
	 *
	 * @param input grayscale image (usually a gradient image)
	 * @param marker image containing the labeled markers to start the watershed
	 * @param mask binary mask to restrict the region of interest (null to use whole input image)
	 * @param connectivity voxel connectivity (6 or 26)
	 * @param compactness compactness constrain parameter (values larger than 0 imply using compact watershed)
	 */
	public MarkerControlledWatershedTransform3D(
			ImagePlus input,
			ImagePlus marker,
			ImagePlus mask,
			int connectivity,
			double compactness )
	{
		super( input, mask, connectivity );
		this.markerImage = marker;
		this.compactness = compactness;
	}
	/**
	 * Apply watershed transform on inputImage, using the labeled 
	 * markers from markerImage and restricted to the white areas 
	 * of maskImage. This implementation visits all voxels by
	 * ascending gray value.
	 * 
	 * @return watershed domains image (no dams)
	 * @deprecated 
	 * The algorithm with a sorted list does not visit the voxels
	 * based on their h value and proximity to markers so it is 
	 * not a true watershed method. 
	 */
	@Deprecated
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
		if( verbose ) IJ.log("  Extracting voxel values..." );
		final long t0 = System.currentTimeMillis();
		
		voxelList = extractVoxelValues( inputStack, markerImage.getStack(), tabLabels );		
		if ( null == voxelList )
			return null;
						
		final long t1 = System.currentTimeMillis();		
		if( verbose ) IJ.log("  Extraction took " + (t1-t0) + " ms.");
		if( verbose ) IJ.log("  Sorting voxels by value..." );
		IJ.showStatus("Sorting voxels by value...");
		Collections.sort( voxelList );
		final long t2 = System.currentTimeMillis();
		if( verbose ) IJ.log("  Sorting took " + (t2-t1) + " ms.");
			    
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
	    	if ( Thread.currentThread().isInterrupted() )
				return null;	
	    	
	    	change = false;
			final int count = voxelList.size();
			if( verbose ) IJ.log( "  Flooding " + count + " voxels..." );
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
		if( verbose ) IJ.log("  Flooding took: " + (end-start) + " ms");
		IJ.showProgress( 1.0 );
		
		// Create result label image
		ImageStack labelStack = markerImage.duplicate().getStack();
	    
	    for (int i = 0; i < size1; ++i)
	      for (int j = 0; j < size2; ++j)
	        for (int k = 0; k < size3; ++k)
	            labelStack.setVoxel( i, j, k, tabLabels[i][j][k] );
	    
	    String title = inputImage.getTitle();
		String ext = "";
		int index = title.lastIndexOf( "." );
		if( index != -1 )
		{
			ext = title.substring( index );
			title = title.substring( 0, index );				
		}			
		
	    final ImagePlus ws = new ImagePlus( title + "-watershed" + ext, labelStack );
	    ws.setCalibration( inputImage.getCalibration() );
	    return ws;
	}

	
	/**
	 * Apply watershed transform on inputImage, using the labeled 
	 * markers from markerImage and restricted to the white areas 
	 * of maskImage. This implementation visits all voxels by
	 * ascending gray value.
	 * 
	 * @return watershed domains image (with dams)
	 * @deprecated 
	 * The algorithm with a sorted list does not visit the voxels
	 * based on their h value and proximity to markers so it is 
	 * not a true watershed method. 
	 */
	@Deprecated
	public ImagePlus applyWithSortedListAndDams()
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
	    
		// voxel labels
		final int[][][] tabLabels = new int[ size1 ][ size2 ][ size3 ]; 
		
		// Make list of all voxels and sort it in ascending order
		IJ.showStatus( "Extracting voxel values..." );
		if( verbose ) IJ.log("  Extracting voxel values..." );
		final long t0 = System.currentTimeMillis();
		
		// extract list of original voxels values and corresponding coordinates
		// and at the same time, fill the label image
		LinkedList<VoxelRecord> voxelList = extractVoxelValues( inputStack, markerImage.getStack(), tabLabels );
		if ( null == voxelList )
			return null;
						
		final long t1 = System.currentTimeMillis();		
		if( verbose ) IJ.log("  Extraction took " + (t1-t0) + " ms.");
		if( verbose ) IJ.log("  Sorting voxels by value..." );
		IJ.showStatus("Sorting voxels by value...");
		Collections.sort( voxelList );
		final long t2 = System.currentTimeMillis();
		if( verbose ) IJ.log("  Sorting took " + (t2-t1) + " ms.");
			    
		// Watershed
	    boolean found = false;	    

	    final long start = System.currentTimeMillis();
	          	
      	// Check connectivity
       	final Neighborhood3D neigh = connectivity == 26 ? 
       			new Neighborhood3DC26() : new Neighborhood3DC6();

       	// list to store neighbor labels
       	final ArrayList <Integer> neighborLabels = new ArrayList<Integer>();
       			
	    boolean change = true;
	    while ( voxelList.isEmpty() == false && change )
	    {
	    	if ( Thread.currentThread().isInterrupted() )
				return null;	
	    	
	    	change = false;
			final int count = voxelList.size();
			if( verbose ) IJ.log( "  Flooding " + count + " voxels..." );
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
			       	
			       	// Read neighbor coordinates
			       	neigh.setCursor( p2 );
			       	
			       	// reset list of neighbor labels
			       	neighborLabels.clear();
			       		
			       	for( Cursor3D c : neigh.getNeighbors() )			       		
			       	{
			       		// Look in neighborhood for labeled voxels
			       		int u = c.getX();
			       		int v = c.getY();
			       		int w = c.getZ();
			       					       		
			       		if ( u >= 0 && u < size1 && v >= 0 && v < size2 && w >= 0 && w < size3 )
			       		{
			       			if ( tabLabels[ u ][ v ] [ w ] > 0 )
			       			{
			       				// store unique labels of neighbors in a list
	      						if( neighborLabels.contains( tabLabels[ u ][ v ][ w ] ) == false ) 
	      								neighborLabels.add( tabLabels[ u ][ v ][ w ] );
			       				found = true;
			       			}
			       		}			       		
			       	}
			       				       
					if ( found == false )    
						voxelList.addLast( voxelRecord );
					else
					{
						change = true;
						// if the neighbors of the extracted voxel that have already been labeled 
						// all have the same label, then the voxel is labeled with their label.
						// Otherwise is left as 0 to create a dam.
						if( neighborLabels.size() == 1 )
							tabLabels[ i ][ j ][ k ] = neighborLabels.get( 0 );
					}
	      		}
	        }
		}

		final long end = System.currentTimeMillis();
		if( verbose ) IJ.log("  Flooding took: " + (end-start) + " ms");
		IJ.showProgress( 1.0 );
		
		// Create result label image
		ImageStack labelStack = markerImage.duplicate().getStack();
	    
	    for (int i = 0; i < size1; ++i)
	      for (int j = 0; j < size2; ++j)
	        for (int k = 0; k < size3; ++k)
	            labelStack.setVoxel( i, j, k, tabLabels[i][j][k] );
	    
	    String title = inputImage.getTitle();
		String ext = "";
		int index = title.lastIndexOf( "." );
		if( index != -1 )
		{
			ext = title.substring( index );
			title = title.substring( 0, index );				
		}			
		
	    final ImagePlus ws = new ImagePlus( title + "-watershed" + ext, labelStack );
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

		// list of voxels to process (initially the voxels adjacent to the
		// markers)
		PriorityQueue<VoxelRecord> voxelList = null;

		final int[][][] tabLabels = new int[ size1 ][ size2 ][ size3 ];
		// value INIT is assigned to each voxel of the output labels
		for( int i=0; i<size1; i++ )
			for( int j=0; j<size2; j++ )
				Arrays.fill( tabLabels[i][j], INIT );

		// Extract voxels to process
		IJ.showStatus( "Extracting voxel values..." );
		if( verbose ) IJ.log("  Extracting voxel values..." );
		final long t0 = System.currentTimeMillis();

		if( verbose ) IJ.log("  Using "+connectivity+"-connectivity..." );

		voxelList = extractVoxelValuesPriorityQueue(
				inputStack, markerImage.getStack(), tabLabels );
		if ( null == voxelList )
			return null;

		final long t1 = System.currentTimeMillis();		
		if( verbose ) IJ.log("  Extraction took " + (t1-t0) + " ms.");

		// Watershed
		final long start = System.currentTimeMillis();

		// Check connectivity
		final Neighborhood3D neigh = connectivity == 26 ?
				new Neighborhood3DC26() : new Neighborhood3DC6();

		final int count = voxelList.size();
		if( verbose ) IJ.log( "  Flooding from " + count + " voxels..." );
		IJ.showStatus("Flooding from " + count + " voxels...");

		final double[] extent = Images3D.findMinAndMax(inputImage);
		double maxValue = extent[ 1 ];

		// list to store neighbor labels
		final ArrayList <Integer> neighborLabels = new ArrayList<Integer>();
		// list to store neighbor voxels
		final ArrayList <VoxelRecord> neighborVoxels =
				new ArrayList<VoxelRecord>();

		// set compactness constraint value (if 0, regular watershed will be executed)
      	final double c = this.compactness;

		// with mask
		if ( null != maskImage )
		{
			final ImageStack maskStack = maskImage.getStack();

			while ( voxelList.isEmpty() == false )
			{
				if ( Thread.currentThread().isInterrupted() )
					return null;

				final VoxelRecord voxelRecord = voxelList.poll();
				// show progression along voxel values
				IJ.showProgress( (voxelRecord.getValue() + 1) / (maxValue + 1));

				final Cursor3D p = voxelRecord.getCursor();
				final int i = p.getX();
				final int j = p.getY();
				final int k = p.getZ();

				// Set cursor of neighborhood in voxel of interest
				neigh.setCursor( p );

				// reset list of neighbor labels
				neighborLabels.clear();

				// reset list of neighbor voxels
				neighborVoxels.clear();

				// Read neighbor coordinates
				for( Cursor3D cur : neigh.getNeighbors() )
				{
					// Look in neighborhood
					int u = cur.getX();
					int v = cur.getY();
					int w = cur.getZ();

					if ( u >= 0 && u < size1 && v >= 0 && v < size2 && w >= 0 && w < size3 )
					{
						// Unlabeled neighbors go into the queue if they are not
						// there yet
						if ( tabLabels[u][v][w] == INIT &&
								maskStack.getVoxel( u, v, w ) > 0 )
						{
							if( c == 0 ) // regular watershed
      							neighborVoxels.add( new VoxelRecord( cur, inputStack.getVoxel( u, v, w ) ) );
      						else // compact watershed
      						{
      							// update distance from seed
								final double cDist2p = voxelRecord.getValue() - inputStack.getVoxel( i, j, k );
								final double cDist2cur = cDist2p + c * p.euclideanDistance( cur );
								neighborVoxels.add(
										new VoxelRecord(
												cur,
												inputStack.getVoxel( u, v, w )
												+ cDist2cur ) );
      						}
						}
						else if ( tabLabels[u][v][w] > 0 &&
								! neighborLabels.contains(
										tabLabels[ u ][ v ][ w ] ) )
						{
							// store labels of neighbors in a list
							neighborLabels.add( tabLabels[ u ][ v ][ w ] );
						}
					}
				}
				// if it has any labeled neighbor
				if( neighborLabels.size() > 0 )
				{
					// assign that label to the current voxel
					tabLabels[ i ][ j ][ k ] = neighborLabels.get( 0 );
					// now that we know the voxel is labeled, add neighbors to list
					for( VoxelRecord v : neighborVoxels )
					{
						tabLabels[ v.getCursor().getX() ][ v.getCursor().getY() ][ v.getCursor().getZ() ] = INQUEUE;
						voxelList.add( v );
					}
				}
			}
		}
		else // without mask
		{
			while ( voxelList.isEmpty() == false )
			{
				if ( Thread.currentThread().isInterrupted() )
					return null;

				final VoxelRecord voxelRecord = voxelList.poll();
				// show progression along voxel values
				IJ.showProgress( (voxelRecord.getValue() + 1) / (maxValue + 1));

				final Cursor3D p = voxelRecord.getCursor();
				final int i = p.getX();
				final int j = p.getY();
				final int k = p.getZ();

				// Set cursor of neighborhood in voxel of interest
				neigh.setCursor( p );

				// reset list of neighbor labels
				neighborLabels.clear();

				// reset list of neighbor voxels
				neighborVoxels.clear();

				// Read neighbor coordinates
				for( Cursor3D cur : neigh.getNeighbors() )
				{
					// Look in neighborhood for labeled voxels with
					// smaller or equal original value
					int u = cur.getX();
					int v = cur.getY();
					int w = cur.getZ();
					if ( u >= 0 && u < size1 && v >= 0 && v < size2 && w >= 0 && w < size3 )
					{
						// Unlabeled neighbors go into the queue if they are not there yet
						if (tabLabels[ u ][ v ][ w ] == INIT )
						{
							if( c == 0 ) // regular watershed
      							neighborVoxels.add( new VoxelRecord( cur, inputStack.getVoxel( u, v, w ) ) );
      						else // compact watershed
      						{
      							// update distance from seed
								final double cDist2p = voxelRecord.getValue() - inputStack.getVoxel( i, j, k );
								final double cDist2cur = cDist2p + c * p.euclideanDistance( cur );
								neighborVoxels.add(
										new VoxelRecord(
												cur,
												inputStack.getVoxel( u, v, w )
												+ cDist2cur ) );
      						}
						}
						else if (  tabLabels[ u ][ v ][ w ] > 0 &&
								! neighborLabels.contains(tabLabels[ u ][ v ][ w ]) )
						{
							// store labels of neighbors in a list without
							// repetitions
							neighborLabels.add( tabLabels[ u ][ v ][ w ] );
						}
					}
				}
				// if it has any labeled neighbor
				if( neighborLabels.size() > 0 )
				{
					// assign the label of the first neighbor to this voxel
					tabLabels[ i ][ j ][ k ] = neighborLabels.get( 0 );
					// now that we know the voxel is labeled, add neighbors to
					// list
					for( VoxelRecord v : neighborVoxels )
					{
						tabLabels[ v.getCursor().getX() ][ v.getCursor().getY() ][ v.getCursor().getZ() ] = INQUEUE;
						voxelList.add( v );
					}
				}
			}
		}

		final long end = System.currentTimeMillis();
		if( verbose ) IJ.log("  Flooding took: " + (end-start) + " ms");
		IJ.showProgress( 1.0 );

		// Create result label image
		ImageStack labelStack = markerImage.duplicate().getStack();

		for (int i = 0; i < size1; ++i)
			for (int j = 0; j < size2; ++j)
				for (int k = 0; k < size3; ++k)
					if( tabLabels[ i ][ j ][ k ] == INIT ) // set unlabeled voxels to WSHED
						labelStack.setVoxel( i, j, k, 0 );
					else
						labelStack.setVoxel( i, j, k, tabLabels[ i ][ j ][ k ] );

		String title = inputImage.getTitle();
		String ext = "";
		int index = title.lastIndexOf( "." );
		if( index != -1 )
		{
			ext = title.substring( index );
			title = title.substring( 0, index );
		}

		final ImagePlus ws = new ImagePlus( title + "-watershed" + ext, labelStack );
		ws.setCalibration( inputImage.getCalibration() );
		return ws;
	}

	/**
	 * Apply watershed transform on inputImage, using the labeled 
	 * markers from markerImage and restricted to the white areas 
	 * of maskImage (optionally). This implementation uses a priority
	 * queue to visit first the voxels on the surroundings of the 
	 * labeled markers (Meyer's flooding algorithm).
	 * 
	 * Meyer's flooding algorithm:
	 * 
	 * Label the regional minima with different colors
	 * Repeat
	 * |	Select a pixel p, not colored, not watershed,
	 * |	adjacent to some colored pixels,
	 * |	and having the lowest possible gray level
	 * |	If p is adjacent to exactly one color then
	 * |	label p with this color
	 * |	If p is adjacent to more than one color then
	 * |	label p as watershed
	 * Until no such pixel exists
	 * 
	 * More information at 
	 * - Serge Beucher's site: http://cmm.ensmp.fr/~beucher/wtshed.html
	 * - G. Bertrand's Topological Watershed site: http://www.esiee.fr/~info/tw/index.html
	 * 
	 * @return watershed domains image (with dams)
	 */
	public ImagePlus applyWithPriorityQueueAndDams()
	{
		if ( Thread.currentThread().isInterrupted() )					
			return null;
		
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
		
		// output labels
		final int[][][] tabLabels = new int[ size1 ][ size2 ][ size3 ];
		// value INIT is assigned to each voxel of the output labels
		for( int i=0; i<size1; i++ )
			for( int j=0; j<size2; j++ )
				Arrays.fill( tabLabels[i][j], INIT );

		// Make list of voxels and sort it in ascending order
		IJ.showStatus( "Extracting voxel values..." );
		if( verbose ) IJ.log("  Extracting voxel values..." );
		final long t0 = System.currentTimeMillis();
		
		voxelList = extractVoxelValuesPriorityQueue(
				inputStack, markerImage.getStack(), tabLabels );
		if( null == voxelList )
			return null;
						
		final long t1 = System.currentTimeMillis();		
		if( verbose ) IJ.log("  Extraction took " + (t1-t0) + " ms.");
					    
		// Watershed
	    final long start = System.currentTimeMillis();
	         	
      	// Check connectivity
       	final Neighborhood3D neigh = connectivity == 26 ? 
       			new Neighborhood3DC26() : new Neighborhood3DC6();

	    final int count = voxelList.size();
	    if( verbose ) IJ.log( "  Flooding from " + count + " voxels..." );
      	IJ.showStatus("Flooding from " + count + " voxels...");

      	final double[] extent = Images3D.findMinAndMax(inputImage);
      	double maxValue = extent[1];
      	
      	// list to store neighbor labels
      	final ArrayList <Integer> neighborLabels = new ArrayList<Integer>();
      	
      	final ArrayList <VoxelRecord> neighborVoxels = new ArrayList<VoxelRecord>();

      	// set compactness constraint value (if 0, regular watershed will be executed)
      	final double c = this.compactness;

      	// with mask
      	if ( null != maskImage )
      	{
      		if ( Thread.currentThread().isInterrupted() )
				return null;	
      		final ImageStack maskStack = maskImage.getStack();
      		
      		while ( voxelList.isEmpty() == false )
      		{
      			if ( Thread.currentThread().isInterrupted() )
    				return null;

      			final VoxelRecord voxelRecord = voxelList.poll();
      			// show progression along voxel values
	    		IJ.showProgress( (voxelRecord.getValue() + 1) / (maxValue + 1));
	    		
      			final Cursor3D p = voxelRecord.getCursor();
	    		final int i = p.getX();
	    		final int j = p.getY();
	    		final int k = p.getZ();
      			

      			// Read neighbor coordinates		       	
		       	neigh.setCursor( p );
		       	 
		       	// reset list of neighbor labels
		       	neighborLabels.clear();
		       	
		       	// reset list of neighbor voxels
		       	neighborVoxels.clear();
		       	
		       	for( Cursor3D cur : neigh.getNeighbors() )
		       	{
		       		// Look in neighborhood for labeled voxels with
		       		// smaller or equal original value
		       		int u = cur.getX();
		       		int v = cur.getY();
		       		int w = cur.getZ();
		       		
		       		if ( u >= 0 && u < size1 && v >= 0 && v < size2 && w >= 0 && w < size3 )
		       		{
		       			// Unlabeled neighbors go into the queue if they are not
		       			// there yet
		       			if ( tabLabels[u][v][w] == INIT
		       					&& maskStack.getVoxel(u, v, w) > 0 )
		       			{
      						if( c == 0 ) // regular watershed
      							neighborVoxels.add( new VoxelRecord( cur, inputStack.getVoxel( u, v, w ) ) );
      						else // compact watershed
      						{
      							// update distance from seed
								final double cDist2p = voxelRecord.getValue() - inputStack.getVoxel( i, j, k );
								final double cDist2cur = cDist2p + c * p.euclideanDistance( cur );
								neighborVoxels.add(
										new VoxelRecord(
												cur,
												inputStack.getVoxel( u, v, w )
												+ cDist2cur ) );
      						}
      					}
      					else if ( tabLabels[ u ][ v ][ w ] > 0 
      							&& neighborLabels.contains(tabLabels[ u ][ v ][ w ]) == false)
      					{
      						// store labels of neighbors in a list
      						neighborLabels.add( tabLabels[ u ][ v ][ w ] );
      					}
      				}
      			}
		       	// if the neighbors of the extracted voxel that have already been labeled 
		       	// all have the same label, then the voxel is labeled with their label.
      			if( neighborLabels.size() == 1 )
      			{
      				tabLabels[ i ][ j ][ k ] = neighborLabels.get( 0 );
      				// now that we know the voxel is labeled, add neighbors to list
      				for( VoxelRecord v : neighborVoxels )
      				{      					
      					tabLabels[ v.getCursor().getX() ][ v.getCursor().getY() ][ v.getCursor().getZ() ] = INQUEUE;
      					voxelList.add( v );
      				}
      			}
      			else if( neighborLabels.size() > 1 )
      				tabLabels[ i ][ j ][ k ] = WSHED;
      		}
      	}
      	else // without mask
      	{
      		while ( voxelList.isEmpty() == false )
      		{
      			if ( Thread.currentThread().isInterrupted() )
    				return null;	

      			final VoxelRecord voxelRecord = voxelList.poll();
      			// show progression along voxel values
	    		IJ.showProgress( (voxelRecord.getValue() + 1) / (maxValue + 1));
	    		
      			final Cursor3D p = voxelRecord.getCursor();
	    		final int i = p.getX();
	    		final int j = p.getY();
	    		final int k = p.getZ();

      			// Set cursor of neighborhood in voxel of interest
      			neigh.setCursor( p );
      			
      			// reset list of neighbor labels
		       	neighborLabels.clear();      
		       	
		       	// reset list of neighbor voxels
		       	neighborVoxels.clear();
      			
		       	// Read neighbor coordinates
      			for( Cursor3D cur : neigh.getNeighbors() )
      			{      				      				
      				// Look in neighborhood for labeled voxels with
      				// smaller or equal original value
      				int u = cur.getX();
      				int v = cur.getY();
      				int w = cur.getZ();
      				if ( u >= 0 && u < size1 && v >= 0 && v < size2 && w >= 0 && w < size3 )
      				{
      					// Unlabeled neighbors go into the queue if they are not there yet
      					if ( tabLabels[ u ][ v ][ w ] == INIT )
      					{
      						if( c == 0 ) // regular watershed
      							neighborVoxels.add( new VoxelRecord( cur, inputStack.getVoxel( u, v, w ) ) );
      						else // compact watershed
      						{
      							// update distance from seed
								final double cDist2p = voxelRecord.getValue() - inputStack.getVoxel( i, j, k );
								final double cDist2cur = cDist2p + c * p.euclideanDistance( cur );
								neighborVoxels.add(
										new VoxelRecord(
												cur,
												inputStack.getVoxel( u, v, w )
												+ cDist2cur ) );
      						}
      					}
      					else if ( tabLabels[ u ][ v ][ w ] > 0 
      							&& neighborLabels.contains(tabLabels[ u ][ v ][ w ]) == false)
      					{
      						// store labels of neighbors in a list without repetitions
      						neighborLabels.add( tabLabels[ u ][ v ][ w ] );
      					}
      				}
      			}
      			// if the neighbors of the extracted voxel that have already been labeled 
      			// all have the same label, then the voxel is labeled with their label
      			if( neighborLabels.size() == 1 )
      			{
      				tabLabels[ i ][ j ][ k ] = neighborLabels.get( 0 );
      				// now that we know the voxel is labeled, add unlabeled neighbors to list
      				for( VoxelRecord v : neighborVoxels )
      				{      					
      					tabLabels[ v.getCursor().getX() ][ v.getCursor().getY() ][ v.getCursor().getZ() ] = INQUEUE;
      					voxelList.add( v );
      				}
      			}
      			else if( neighborLabels.size() > 1 )
      				tabLabels[ i ][ j ][ k ] = WSHED;
      				
      		}
      	}

		final long end = System.currentTimeMillis();
		if( verbose ) IJ.log("  Flooding took: " + (end-start) + " ms");
		IJ.showStatus("");
		IJ.showProgress( 1.0 );
	    
		// Create result label image
		ImageStack labelStack = markerImage.duplicate().getStack();
	    
		for (int k = 0; k < size3; ++k)
		{
			if ( Thread.currentThread().isInterrupted() )
				return null;	
			
			ImageProcessor labelProcessor = labelStack.getProcessor( k+1 );
			for (int i = 0; i < size1; ++i)
				for (int j = 0; j < size2; ++j)	
				{					
					if( tabLabels[ i ][ j ][ k ] == INIT ) // set unlabeled voxels to WSHED
						labelProcessor.setf( i,  j, WSHED );
					else
						labelProcessor.setf( i,  j, tabLabels[ i ][ j ][ k ] );
				}
				
		}
		
		String title = inputImage.getTitle();
		String ext = "";
		int index = title.lastIndexOf( "." );
		if( index != -1 )
		{
			ext = title.substring( index );
			title = title.substring( 0, index );				
		}			
		
	    final ImagePlus ws = new ImagePlus( title + "-watershed" + ext, labelStack );
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
		if ( Thread.currentThread().isInterrupted() )					
			return null;
				
		final int size1 = inputStack.getWidth();
	    final int size2 = inputStack.getHeight();
	    final int size3 = inputStack.getSize();
		
	            
        final PriorityQueue<VoxelRecord> voxelList = new PriorityQueue<VoxelRecord>();
        
        // Auxiliary cursor to visit neighbors
	    final Cursor3D cursor = new Cursor3D(0, 0, 0);
      	
      	// Check connectivity
       	final Neighborhood3D neigh = connectivity == 26 ? 
       			new Neighborhood3DC26() : new Neighborhood3DC6();

       	// Set compactness constraint value
       	final double c = this.compactness;

		if( null != maskImage ) // apply mask
		{
			final ImageStack mask = maskImage.getImageStack();
						
			for (int z = 0; z < size3; ++z)	
			{
				IJ.showProgress( z+1, size3 );
				
				if ( Thread.currentThread().isInterrupted() )
				{
					IJ.showProgress( 1.0 );
					return null;
				}

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
								for( Cursor3D cur : neigh.getNeighbors() )
								{
									int u = cur.getX();
									int v = cur.getY();
									int w = cur.getZ();
									if ( u >= 0 && u < size1 && 
											v >= 0 && v < size2 && 
											w >= 0 && w < size3 &&
											(int) seedStack.getVoxel( u, v, w ) == 0 &&
											tabLabels[ u ][ v ][ w ] != INQUEUE )															 
									{
										if( c == 0 )
											voxelList.add( new VoxelRecord( cur, inputStack.getVoxel( u, v, w ) ) );
										else
											voxelList.add( new VoxelRecord( cur,
													inputStack.getVoxel( u, v, w ) + c * cursor.euclideanDistance(cur)) );
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
				if ( Thread.currentThread().isInterrupted() )
				{
					IJ.showProgress( 1.0 );
					return null;
				}
				
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
							for( Cursor3D cur : neigh.getNeighbors() )
							{
								int u = cur.getX();
								int v = cur.getY();
								int w = cur.getZ();
								if ( u >= 0 && u < size1 && 
										v >= 0 && v < size2 && 
										w >= 0 && w < size3 &&
										(int) seedStack.getVoxel( u, v, w ) == 0 &&
										tabLabels[ u ][ v ][ w ] != INQUEUE )															 
								{
									if( c == 0 )
										voxelList.add( new VoxelRecord( cur, inputStack.getVoxel( u, v, w ) ) );
									else
										voxelList.add( new VoxelRecord( cur,
												inputStack.getVoxel( u, v, w ) + c * cursor.euclideanDistance(cur)) );
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
	 * Extract voxel values from input and labeled marker images. The
	 * input grayscale values will be return in a list of VoxelRecrod 
	 * and the markers will be stored in <code>tabLabels</code>.
	 * 
	 * @param inputStack input grayscale stack (usually a gradient image)
	 * @param markerStack labeled marker stack
	 * @param tabLabels output label array
	 * @return list of input voxel values
	 */
	public LinkedList<VoxelRecord> extractVoxelValues(
			final ImageStack inputStack,
			final ImageStack markerStack,
			final int[][][] tabLabels) 
	{
		
		final int size1 = inputStack.getWidth();
	    final int size2 = inputStack.getHeight();
	    final int size3 = inputStack.getSize();
			    
	    final AtomicInteger ai = new AtomicInteger(0);
        final int n_cpus = Prefs.getThreads();
        
        final int dec = (int) Math.ceil( (double) size3 / (double) n_cpus );
        
        Thread[] threads = ThreadUtil.createThreadArray( n_cpus );
        
        @SuppressWarnings("unchecked")
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
								if ( Thread.currentThread().isInterrupted() )
									return;	
								
								if (zmin==0) 
									IJ.showProgress(z+1, zmax);

								final ImageProcessor ipMask = mask.getProcessor( z+1 );
								final ImageProcessor ipInput = inputStack.getProcessor( z+1 );
								final ImageProcessor ipMarker = markerStack.getProcessor( z+1 );

								for( int x = 0; x < size1; ++x )
									for( int y = 0; y < size2; ++y )
										if( ipMask.getf( x, y ) > 0 )
										{
											lists[k].addLast( new VoxelRecord( x, y, z, ipInput.getf( x, y )));
											tabLabels[x][y][z] = (int) ipMarker.getf( x, y );
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
								if ( Thread.currentThread().isInterrupted() )
									return;								
								
								if (zmin==0) 
									IJ.showProgress(z+1, zmax);

								final ImageProcessor ipInput = inputStack.getProcessor( z+1 );
								final ImageProcessor ipMarker = markerStack.getProcessor( z+1 );

								for( int x = 0; x < size1; ++x )
									for( int y = 0; y < size2; ++y )
									{
										lists[k].addLast( new VoxelRecord( x, y, z, ipInput.getf( x, y )));
										tabLabels[x][y][z] = (int) ipMarker.getf( x, y );
									}
							}

						}
					}
				};
			}
			ThreadUtil.startAndJoin(threads);									
			
		}// end else
		
		final LinkedList<VoxelRecord> voxelList = lists[ 0 ];
		for (int ithread = 1; ithread < threads.length; ithread++)
			voxelList.addAll(lists[ ithread ]);
		
		IJ.showProgress(1.0);
		
		return voxelList;
	}

	

}
