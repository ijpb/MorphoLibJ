package inra.ijpb.watershed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.PriorityQueue;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.data.Cursor2D;
import inra.ijpb.data.Neighborhood2D;
import inra.ijpb.data.Neighborhood2DC8;
import inra.ijpb.data.Neighborhood2DC4;
import inra.ijpb.data.PixelRecord;

/**
 * Marker-controlled version of the watershed transform in 2D.
 * 
 * Reference: Fernand Meyer and Serge Beucher. "Morphological segmentation." 
 * Journal of visual communication and image representation 1.1 (1990): 21-46.
 * 
 * @author Ignacio Arganda-Carreras
 *
 */
public class MarkerControlledWatershedTransform2D extends WatershedTransform2D
{
	/** image containing the labeled markers to start the watershed */
	ImageProcessor markerImage = null;

	/**
	 * Initialize a marker-controlled watershed transform
	 * 
	 * @param input grayscale image (usually a gradient image)
	 * @param marker image containing the labeled markers to start the watershed
	 * @param mask binary mask to restrict the region of interest (null to use whole input image)
	 */
	public MarkerControlledWatershedTransform2D(
			ImageProcessor input, 
			ImageProcessor marker,
			ImageProcessor mask) 
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
	 * @param connectivity pixel connectivity (4 or 8)
	 */
	public MarkerControlledWatershedTransform2D(
			ImageProcessor input, 
			ImageProcessor marker,
			ImageProcessor mask,
			int connectivity ) 
	{
		super( input, mask, connectivity );
		this.markerImage = marker;		
	}
	
	/**
	 * Apply watershed transform on inputImage, using the labeled 
	 * markers from markerImage and restricted to the white areas 
	 * of maskImage. This implementation visits all pixels by
	 * ascending gray value.
	 * 
	 * @return watershed domains image (no dams)
	 * @deprecated 
	 * The algorithm with a sorted list does not visit the pixels
	 * based on their h value and proximity to markers so it is 
	 * not a true watershed method. 
	 */
	@Deprecated
	public ImageProcessor applyWithSortedList()
	{
	    final int size1 = inputImage.getWidth();
	    final int size2 = inputImage.getHeight();
	    
	    if (size1 != markerImage.getWidth() || size2 != markerImage.getHeight()) 
	    {
			throw new IllegalArgumentException("Marker and input images must have the same size");
		}
		
		// Check connectivity has a correct value
		if (connectivity != 4 && connectivity != 8) {
			throw new RuntimeException(
					"Connectivity for 2D images must be either 4 or 8, not "
							+ connectivity);
		}
	    
		// list of original pixels values and corresponding coordinates
		LinkedList<PixelRecord> pixelList = null;
		
		final int[][] tabLabels = new int[ size1 ][ size2 ]; 
		
		// Make list of pixels and sort it in ascending order
		IJ.showStatus( "Extracting pixel values..." );
		if( verbose ) IJ.log("  Extracting pixel values..." );
		final long t0 = System.currentTimeMillis();
		
		pixelList = extractPixelValues( inputImage, markerImage, tabLabels );		
		if ( null == pixelList )
			return null;
								
		final long t1 = System.currentTimeMillis();		
		if( verbose ) IJ.log("  Extraction took " + (t1-t0) + " ms.");
		if( verbose ) IJ.log("  Sorting pixels by value..." );
		IJ.showStatus("Sorting pixels by value...");
		Collections.sort( pixelList );
		final long t2 = System.currentTimeMillis();
		if( verbose ) IJ.log("  Sorting took " + (t2-t1) + " ms.");
			    
		// Watershed
	    boolean found = false;	    

	    final long start = System.currentTimeMillis();

	    // Auxiliary cursor to visit neighbors
	    final Cursor2D cursor = new Cursor2D( 0, 0 );
      	
      	// Check connectivity
       	final Neighborhood2D neigh = connectivity == 8 ? 
       			new Neighborhood2DC8() : new Neighborhood2DC4();
	    
	    boolean change = true;
	    while ( pixelList.isEmpty() == false && change )
	    {
	    	if ( Thread.currentThread().isInterrupted() )
				return null;	
	    	
	    	change = false;
			final int count = pixelList.size();
	      	IJ.log( "  Flooding " + count + " pixels..." );
	      	IJ.showStatus("Flooding " + count + " pixels...");	      		      	
	      	
			for (int p = 0; p < count; ++p)
	      	{
				IJ.showProgress(p, count);
	       		final PixelRecord pixelRecord = pixelList.removeFirst();
	       		final Cursor2D p2 = pixelRecord.getCursor();
	    		final int i = p2.getX();
	    		final int j = p2.getY();
	       		
	       		// If the pixel is unlabeled
				if( tabLabels[ i ][ j ] == 0 )
	       		{
			       	found = false;
			       	double pixelValue = pixelRecord.getValue();
			       	
			       	// Read neighbor coordinates
			       	cursor.set(  i, j );
			       	neigh.setCursor( cursor );
			       		
			       	for( Cursor2D c : neigh.getNeighbors() )			       		
			       	{
			       		// Look in neighborhood for labeled pixels with
			       		// smaller or equal original value
			       		int u = c.getX();
			       		int v = c.getY();
			       					       		
			       		if ( u >= 0 && u < size1 && v >= 0 && v <  size2 )
			       		{
			       			if ( tabLabels[ u ][ v ] != 0 && inputImage.getf( u, v ) <= pixelValue )
			       			{
			       				tabLabels[ i ][ j ] = tabLabels[ u ][ v ];
			       				pixelValue = inputImage.getf( u, v );
			       				found = true;
			       			}
			       		}			       		
			       	}
			       
					if ( found == false )    
						pixelList.addLast( pixelRecord );
					else
						change = true;
	      		}
	        }
		}

		final long end = System.currentTimeMillis();
		if( verbose ) IJ.log("  Flooding took: " + (end-start) + " ms");
		IJ.showProgress( 1.0 );
					
	    return new FloatProcessor( tabLabels );
	}

	/**
	 * Get animation of the different steps in the watershed 
	 * transform on inputImage, using the labeled 
	 * markers from markerImage and restricted to the white areas 
	 * of maskImage. This implementation visits all pixels by
	 * ascending gray value.
	 * 
	 * @return animation of watershed domains image (no dams)
	 * @deprecated 
	 * The algorithm with a sorted list does not visit the pixels
	 * based on their h value and proximity to markers so it is 
	 * not a true watershed method. 
	 */
	@Deprecated
	public ImagePlus getAnimationSortedList()
	{
	    final int size1 = inputImage.getWidth();
	    final int size2 = inputImage.getHeight();	    	    
	    
	    if (size1 != markerImage.getWidth() || size2 != markerImage.getHeight()) 
	    {
			throw new IllegalArgumentException("Marker and input images must have the same size");
		}	    	    
		
		// Check connectivity has a correct value
		if (connectivity != 4 && connectivity != 8) {
			throw new RuntimeException(
					"Connectivity for 2D images must be either 4 or 8, not "
							+ connectivity);
		}
	    
		// create stack to store animation
		ImageStack animation = new ImageStack( size1, size2 );
		
		// list of original pixels values and corresponding coordinates
		LinkedList<PixelRecord> pixelList = null;
		
		final int[][] tabLabels = new int[ size1 ][ size2 ]; 
		
		// Make list of pixels and sort it in ascending order
		IJ.showStatus( "Extracting pixel values..." );
		if( verbose ) IJ.log("  Extracting pixel values..." );
		final long t0 = System.currentTimeMillis();
		
		pixelList = extractPixelValues( inputImage, markerImage, tabLabels );		
		if ( null == pixelList )
			return null;
											
		final long t1 = System.currentTimeMillis();		
		if( verbose ) IJ.log("  Extraction took " + (t1-t0) + " ms.");
		if( verbose ) IJ.log("  Sorting pixels by value..." );
		IJ.showStatus("Sorting pixels by value...");
		Collections.sort( pixelList );
		final long t2 = System.currentTimeMillis();
		if( verbose ) IJ.log("  Sorting took " + (t2-t1) + " ms.");
			    
		// current height
		double h = 0;
		
		// add initial state to the animation stack
		animation.addSlice("h="+h, new FloatProcessor( tabLabels ));
		
		// Watershed
	    boolean found = false;	    

	    final long start = System.currentTimeMillis();

	    // Auxiliary cursor to visit neighbors
	    final Cursor2D cursor = new Cursor2D( 0, 0 );
      	
      	// Check connectivity
       	final Neighborhood2D neigh = connectivity == 8 ? 
       			new Neighborhood2DC8() : new Neighborhood2DC4();
	    
	    boolean change = true;
	    while ( pixelList.isEmpty() == false && change )
	    {
	    	if ( Thread.currentThread().isInterrupted() )
				return null;	
	    	
	    	change = false;
			final int count = pixelList.size();
			if( verbose )  IJ.log( "  Flooding " + count + " pixels..." );
	      	IJ.showStatus("Flooding " + count + " pixels...");	      		      	
	      	
			for (int p = 0; p < count; ++p)
	      	{
				IJ.showProgress(p, count);
	       		final PixelRecord pixelRecord = pixelList.removeFirst();
	       		final Cursor2D p2 = pixelRecord.getCursor();
	    		final int i = p2.getX();
	    		final int j = p2.getY();
	       		
	       		// If the pixel is unlabeled
				if( tabLabels[ i ][ j ] == 0 )
	       		{
			       	found = false;
			       	double pixelValue = pixelRecord.getValue();
			       	
			       	// Read neighbor coordinates
			       	cursor.set( i, j );
			       	neigh.setCursor( cursor );
			       		
			       	for( Cursor2D c : neigh.getNeighbors() )			       		
			       	{
			       		// Look in neighborhood for labeled pixels with
			       		// smaller or equal original value
			       		int u = c.getX();
			       		int v = c.getY();
			       					       		
			       		if ( u >= 0 && u < size1 && v >= 0 && v <  size2 )
			       		{
			       			if ( tabLabels[ u ][ v ] != 0 && inputImage.getf( u, v ) <= pixelValue )
			       			{
			       				tabLabels[ i ][ j ] = tabLabels[ u ][ v ];
			       				pixelValue = inputImage.getf( u, v );
			       				found = true;
			       			}
			       		}			       		
			       	}
			       
					if ( found == false )    
						pixelList.addLast( pixelRecord );
					else
						change = true;
					
					// update animation
					if( pixelValue > h )
					{
						h = pixelValue;
						animation.addSlice( "h=" + h, new FloatProcessor( tabLabels ));
					}
	      		}
	        }// end for (pixels in the list)
			
			// reset h because list is not sorted after first pass
			h = 0;
		}

		final long end = System.currentTimeMillis();
		if( verbose ) IJ.log("  Flooding took: " + (end-start) + " ms");
		IJ.showProgress( 1.0 );
		
		// Create result label image
		FloatProcessor labelProcessor = new FloatProcessor(size1, size2);
		for (int i = 0; i < size1; ++i)
			for (int j = 0; j < size2; ++j)	
			{					
				if( tabLabels[ i ][ j ] == INIT ) // set unlabeled pixels to WSHED
					labelProcessor.setf( i, j, 0 );
				else
					labelProcessor.setf( i, j, tabLabels[ i ][ j ] );
			}
		animation.addSlice( "h=" + h, labelProcessor );
					
	    return new ImagePlus( "Watersed flooding with sorted list", animation );
	}
	
	
	/**
	 * Apply watershed transform on inputImage, using the labeled 
	 * markers from markerImage and restricted to the white areas 
	 * of maskImage. This implementation visits all pixels by
	 * ascending gray value.
	 * 
	 * @return watershed domains image (with dams)
	 * @deprecated 
	 * The algorithm with a sorted list does not visit the pixels
	 * based on their h value and proximity to markers so it is 
	 * not a true watershed method. 
	 */
	@Deprecated
	public ImageProcessor applyWithSortedListAndDams()
	{
	    final int size1 = inputImage.getWidth();
	    final int size2 = inputImage.getHeight();
	    
	    if (size1 != markerImage.getWidth() || size2 != markerImage.getHeight()) {
			throw new IllegalArgumentException("Marker and input images must have the same size");
		}
		
		// Check connectivity has a correct value
		if (connectivity != 4 && connectivity != 8) {
			throw new RuntimeException(
					"Connectivity for 2D images must be either 4 or 8, not "
							+ connectivity);
		}
	    
		// pixel labels
		final int[][] tabLabels = new int[ size1 ][ size2 ]; 
		
		// Make list of all pixels and sort it in ascending order
		IJ.showStatus( "Extracting pixel values..." );
		if( verbose ) IJ.log("  Extracting pixel values..." );
		final long t0 = System.currentTimeMillis();
		
		// extract list of original pixels values and corresponding coordinates
		// and at the same time, fill the label image
		LinkedList<PixelRecord> pixelList = extractPixelValues( inputImage, markerImage, tabLabels );
		if ( null == pixelList )
			return null;
						
		final long t1 = System.currentTimeMillis();		
		if( verbose ) IJ.log("  Extraction took " + (t1-t0) + " ms.");
		if( verbose ) IJ.log("  Sorting pixels by value..." );
		IJ.showStatus("Sorting pixels by value...");
		Collections.sort( pixelList );
		final long t2 = System.currentTimeMillis();
		if( verbose ) IJ.log("  Sorting took " + (t2-t1) + " ms.");
			    
		// Watershed
	    boolean found = false;	    

	    final long start = System.currentTimeMillis();
	          	
      	// Check connectivity
       	final Neighborhood2D neigh = connectivity == 8 ? 
       			new Neighborhood2DC8() : new Neighborhood2DC4();

       	// list to store neighbor labels
       	final ArrayList <Integer> neighborLabels = new ArrayList<Integer>();
       			
	    boolean change = true;
	    while ( pixelList.isEmpty() == false && change )
	    {
	    	if ( Thread.currentThread().isInterrupted() )
				return null;	
	    	
	    	change = false;
			final int count = pixelList.size();
			if( verbose )  IJ.log( "  Flooding " + count + " pixels..." );
	      	IJ.showStatus("Flooding " + count + " pixels...");	      		      	
	      	
			for (int p = 0; p < count; ++p)
	      	{
				IJ.showProgress(p, count);
	       		final PixelRecord pixelRecord = pixelList.removeFirst();
	       		final Cursor2D p2 = pixelRecord.getCursor();
	    		final int i = p2.getX();
	    		final int j = p2.getY();
	       		
	       		// If the pixel is unlabeled
				if( tabLabels[ i ][ j ] == 0 )
	       		{
			       	found = false;
			       	
			       	// Read neighbor coordinates
			       	neigh.setCursor( p2 );
			       	
			       	// reset list of neighbor labels
			       	neighborLabels.clear();
			       		
			       	for( Cursor2D c : neigh.getNeighbors() )			       		
			       	{
			       		// Look in neighborhood for labeled pixels
			       		int u = c.getX();
			       		int v = c.getY();
			       					       		
			       		if ( u >= 0 && u < size1 && v >= 0 && v <  size2 )
			       		{
			       			if ( tabLabels[ u ][ v ]  > 0 )
			       			{
			       				// store unique labels of neighbors in a list
	      						if( neighborLabels.contains( tabLabels[ u ][ v ] ) == false ) 
	      								neighborLabels.add( tabLabels[ u ][ v ] );
			       				found = true;
			       			}
			       		}			       		
			       	}
			       				       
					if ( found == false )    
						pixelList.addLast( pixelRecord );
					else
					{
						change = true;
						// if the neighbors of the extracted pixel that have already been labeled 
						// all have the same label, then the pixel is labeled with their label.
						// Otherwise is left as 0 to create a dam.
						if( neighborLabels.size() == 1 )
							tabLabels[ i ][ j ] = neighborLabels.get( 0 );
					}
	      		}
	        }
		}

		final long end = System.currentTimeMillis();
		if( verbose ) IJ.log("  Flooding took: " + (end-start) + " ms");
		IJ.showProgress( 1.0 );
				
		return new FloatProcessor( tabLabels );
	}
	
	/**
	 * Get animation of the different steps in the watershed 
	 * transform applied to the inputImage, using the labeled 
	 * markers from markerImage and restricted to the white areas 
	 * of maskImage. This implementation visits all pixels by
	 * ascending gray value.
	 * 
	 * @return image stack with the steps of the watershed transform (with dams)
	 * @deprecated 
	 * The algorithm with a sorted list does not visit the pixels
	 * based on their h value and proximity to markers so it is 
	 * not a true watershed method. 
	 */
	@Deprecated
	public ImagePlus getAnimationSortedListAndDams()
	{
	    final int size1 = inputImage.getWidth();
	    final int size2 = inputImage.getHeight();
	    
	    if (size1 != markerImage.getWidth() || size2 != markerImage.getHeight()) {
			throw new IllegalArgumentException("Marker and input images must have the same size");
		}
		
		// Check connectivity has a correct value
		if (connectivity != 4 && connectivity != 8) {
			throw new RuntimeException(
					"Connectivity for 2D images must be either 4 or 8, not "
							+ connectivity);
		}
	    
		// create stack to store animation
		ImageStack animation = new ImageStack( size1, size2 );
		
		// pixel labels
		final int[][] tabLabels = new int[ size1 ][ size2 ]; 
		
		// Make list of all pixels and sort it in ascending order
		IJ.showStatus( "Extracting pixel values..." );
		if( verbose ) IJ.log("  Extracting pixel values..." );
		final long t0 = System.currentTimeMillis();
		
		// extract list of original pixels values and corresponding coordinates
		// and at the same time, fill the label image
		LinkedList<PixelRecord> pixelList = extractPixelValues( inputImage, markerImage, tabLabels );
		if ( null == pixelList )
			return null;
						
		final long t1 = System.currentTimeMillis();		
		if( verbose ) IJ.log("  Extraction took " + (t1-t0) + " ms.");
		if( verbose ) IJ.log("  Sorting pixels by value..." );
		IJ.showStatus("Sorting pixels by value...");
		Collections.sort( pixelList );
		final long t2 = System.currentTimeMillis();
		if( verbose ) IJ.log("  Sorting took " + (t2-t1) + " ms.");
		
		// current height
		double h = 0;

		// add initial state to the animation stack
		animation.addSlice( "h=" + h, new FloatProcessor( tabLabels ) );
			    
		// Watershed
	    boolean found = false;	    

	    final long start = System.currentTimeMillis();
	          	
      	// Check connectivity
       	final Neighborhood2D neigh = connectivity == 8 ? 
       			new Neighborhood2DC8() : new Neighborhood2DC4();

       	// list to store neighbor labels
       	final ArrayList <Integer> neighborLabels = new ArrayList<Integer>();
       			
	    boolean change = true;
	    while ( pixelList.isEmpty() == false && change )
	    {
	    	if ( Thread.currentThread().isInterrupted() )
				return null;	
	    	
	    	change = false;
			final int count = pixelList.size();
	      	IJ.log( "  Flooding " + count + " pixels..." );
	      	IJ.showStatus("Flooding " + count + " pixels...");	      		      	
	      	
			for (int p = 0; p < count; ++p)
	      	{
				IJ.showProgress( p, count );
	       		final PixelRecord pixelRecord = pixelList.removeFirst();
	       		final Cursor2D p2 = pixelRecord.getCursor();
	    		final int i = p2.getX();
	    		final int j = p2.getY();
	       		
	       		// If the pixel is unlabeled
				if( tabLabels[ i ][ j ] == 0 )
	       		{
			       	found = false;
			       	
			       	// Read neighbor coordinates
			       	neigh.setCursor( p2 );
			       	
			       	// reset list of neighbor labels
			       	neighborLabels.clear();
			       		
			       	for( Cursor2D c : neigh.getNeighbors() )			       		
			       	{
			       		// Look in neighborhood for labeled pixels
			       		int u = c.getX();
			       		int v = c.getY();
			       					       		
			       		if ( u >= 0 && u < size1 && v >= 0 && v <  size2 )
			       		{
			       			if ( tabLabels[ u ][ v ] > 0 )
			       			{
			       				// store unique labels of neighbors in a list
	      						if( neighborLabels.contains( tabLabels[ u ][ v ] ) == false ) 
	      								neighborLabels.add( tabLabels[ u ][ v ] );
			       				found = true;
			       			}
			       		}			       		
			       	}
			       				       
					if ( found == false )    
						pixelList.addLast( pixelRecord );
					else
					{
						change = true;
						// if the neighbors of the extracted pixel that have already been labeled 
						// all have the same label, then the pixel is labeled with their label.
						// Otherwise is left as 0 to create a dam.
						if( neighborLabels.size() == 1 )
							tabLabels[ i ][ j ] = neighborLabels.get( 0 );
					}
					
					// update animation
					if( pixelRecord.getValue() > h )
					{
						h = pixelRecord.getValue();
						animation.addSlice( "h=" + h, new FloatProcessor( tabLabels ));
					}
	      		}
	        }// end for (all pixels in the list)
			
			// reset h
			h = 0;
		}

		final long end = System.currentTimeMillis();
		if( verbose ) IJ.log("  Flooding took: " + (end-start) + " ms");
		IJ.showProgress( 1.0 );
		
		// Create result label image
		FloatProcessor labelProcessor = new FloatProcessor(size1, size2);
		for (int i = 0; i < size1; ++i)
			for (int j = 0; j < size2; ++j)	
			{					
				if( tabLabels[ i ][ j ] == INIT ) // set unlabeled pixels to WSHED
					labelProcessor.setf( i, j, 0 );
				else
					labelProcessor.setf( i, j, tabLabels[ i ][ j ] );
			}
		animation.addSlice( "h=" + h, labelProcessor );
				
		return new ImagePlus( "Watershed flooding with sorted list", animation );
	}

	
	
	/**
	 * Apply watershed transform on inputImage, using the labeled 
	 * markers from markerImage and restricted to the white areas 
	 * of maskImage. This implementation visits first the pixels 
	 * on the surroundings of the labeled markers.
	 * 
	 * @return watershed domains image (no dams)
	 */
	public ImageProcessor applyWithPriorityQueue()
	{
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
		
		final int[][] tabLabels = new int[ size1 ][ size2 ]; 
		
		// Make list of pixels and sort it in ascending order
		IJ.showStatus( "Extracting pixel values..." );
		if( verbose ) IJ.log("  Extracting pixel values..." );
		final long t0 = System.currentTimeMillis();
		
		pixelList = extractPixelValuesPriorityQueue( inputImage, markerImage, tabLabels );
		if ( null == pixelList )
			return null;
						
		final long t1 = System.currentTimeMillis();		
		if( verbose ) IJ.log("  Extraction took " + (t1-t0) + " ms.");
					    
		// Watershed
	    final long start = System.currentTimeMillis();
	         	
      	// Check connectivity
       	final Neighborhood2D neigh = connectivity == 8 ? 
       			new Neighborhood2DC8() : new Neighborhood2DC4();

	    final int count = pixelList.size();
	    if( verbose )  IJ.log( "  Flooding from " + count + " pixels..." );
      	IJ.showStatus("Flooding from " + count + " pixels...");
	    
      	final int numPixels = size1 * size2;
      	
      	// with mask
      	if ( null != maskImage )
      	{      	
      		while ( pixelList.isEmpty() == false )
      		{
      			if ( Thread.currentThread().isInterrupted() )
    				return null;	
      			
      			IJ.showProgress( numPixels-pixelList.size(), numPixels );

      			final PixelRecord pixelRecord = pixelList.poll();
      			final Cursor2D p = pixelRecord.getCursor();
	    		final int i = p.getX();
	    		final int j = p.getY();

      			double pixelValue = Double.MAX_VALUE;

      			// Read neighbor coordinates		       	
		       	neigh.setCursor( p );
		       		
		       	for( Cursor2D c : neigh.getNeighbors() )			       		
		       	{
		       		// Look in neighborhood for labeled pixels with
		       		// smaller or equal original value
		       		int u = c.getX();
		       		int v = c.getY();
		       		
		       		if ( u >= 0 && u < size1 && v >= 0 && v <  size2 )
		       		{
		       			// Unlabeled neighbors go into the queue if they are not there yet 
		       			if ( tabLabels[ u ][ v ] == 0 && maskImage.getf( u, v ) > 0 )
		       			{
		       				pixelList.add( new PixelRecord( u, v, inputImage.getf( u, v ) ));
		       				tabLabels[ u ][ v ] = INQUEUE;
		       			}
		       			else if ( tabLabels[ u ][ v ] > 0 && inputImage.getf( u, v ) <= pixelValue )
		       			{
		       				// assign label of smallest neighbor
		       				tabLabels[ i ][ j ] = tabLabels[ u ][ v ];
		       				pixelValue = inputImage.getf( u, v );
		       			}
		       		}
		       	}    

      		}
      	}
      	else // without mask
      	{
      		while ( pixelList.isEmpty() == false )
      		{
      			if ( Thread.currentThread().isInterrupted() )
    				return null;	
      			
      			IJ.showProgress( numPixels-pixelList.size(), numPixels );

      			final PixelRecord pixelRecord = pixelList.poll();
      			final Cursor2D p = pixelRecord.getCursor();
	    		final int i = p.getX();
	    		final int j = p.getY();

      			double pixelValue = pixelRecord.getValue(); 

      			// Read neighbor coordinates
      			neigh.setCursor( p );

      			for( Cursor2D c : neigh.getNeighbors() )			       		
      			{
      				// Look in neighborhood for labeled pixels with
      				// smaller or equal original value
      				int u = c.getX();
      				int v = c.getY();

      				if ( u >= 0 && u < size1 && v >= 0 && v <  size2 )
      				{
      					// Unlabeled neighbors go into the queue if they are not there yet 
      					if ( tabLabels[ u ][ v ] == 0 )
      					{
      						pixelList.add( new PixelRecord( u, v, inputImage.getf( u, v ) ));
      						tabLabels[ u ][ v ] = INQUEUE;
      					}
      					else if ( tabLabels[ u ][ v ] > 0 && inputImage.getf( u, v ) <= pixelValue )
      					{
      						// assign label of smallest neighbor
      						tabLabels[ i ][ j ] = tabLabels[ u ][ v ];
      						pixelValue = inputImage.getf( u, v );
      					}
      				}
      			}    

      		}
      	}

		final long end = System.currentTimeMillis();
		if( verbose ) IJ.log("  Flooding took: " + (end-start) + " ms");
		IJ.showProgress( 1.0 );
		
	    return new FloatProcessor( tabLabels );
	}

	/**
	 * Get animation of each h-step of the watershed transform 
	 * on the inputImage, using the labeled 
	 * markers from markerImage and restricted to the white areas 
	 * of maskImage. This implementation visits first the pixels 
	 * on the surroundings of the labeled markers.
	 * 
	 * @return animation of the watershed domains image (no dams)
	 */
	public ImagePlus getAnimationPriorityQueue()
	{
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
	    
		// create animation stack
		final ImageStack animation = new ImageStack( size1, size2 );
		
		// list of original pixels values and corresponding coordinates
		PriorityQueue<PixelRecord> pixelList = null;
		
		final int[][] tabLabels = new int[ size1 ][ size2 ]; 
		
		// Make list of pixels and sort it in ascending order
		IJ.showStatus( "Extracting pixel values..." );
		if( verbose ) IJ.log("  Extracting pixel values..." );
		final long t0 = System.currentTimeMillis();
		
		pixelList = extractPixelValuesPriorityQueue( inputImage, markerImage, tabLabels );
		if ( null == pixelList )
			return null;
						
		final long t1 = System.currentTimeMillis();		
		if( verbose ) IJ.log("  Extraction took " + (t1-t0) + " ms.");
					    			
		// Watershed
	    final long start = System.currentTimeMillis();
	         	
      	// Check connectivity
       	final Neighborhood2D neigh = connectivity == 8 ? 
       			new Neighborhood2DC8() : new Neighborhood2DC4();

	    final int count = pixelList.size();
	    if( verbose )  IJ.log( "  Flooding from " + count + " pixels..." );
      	IJ.showStatus("Flooding from " + count + " pixels...");
	    
      	final int numPixels = size1 * size2;
      	
      	// current height level
      	double h = 0;
      	// add initial state to animation
     	animation.addSlice( "h=" + h, new FloatProcessor( tabLabels ) );
      	
      	// with mask
      	if ( null != maskImage )
      	{      	
      		while ( pixelList.isEmpty() == false )
      		{
      			if ( Thread.currentThread().isInterrupted() )
    				return null;	
      			
      			IJ.showProgress( numPixels-pixelList.size(), numPixels );

      			final PixelRecord pixelRecord = pixelList.poll();
      			final Cursor2D p = pixelRecord.getCursor();
	    		final int i = p.getX();
	    		final int j = p.getY();

      			double pixelValue = pixelRecord.getValue();

      			// Read neighbor coordinates		       	
		       	neigh.setCursor( p );
		       		
		       	for( Cursor2D c : neigh.getNeighbors() )			       		
		       	{
		       		// Look in neighborhood for labeled pixels with
		       		// smaller or equal original value
		       		int u = c.getX();
		       		int v = c.getY();
		       		
		       		if ( u >= 0 && u < size1 && v >= 0 && v <  size2 )
		       		{
		       			// Unlabeled neighbors go into the queue if they are not there yet 
		       			if ( tabLabels[ u ][ v ] == 0 && maskImage.getf( u, v ) > 0 )
		       			{
		       				pixelList.add( new PixelRecord( u, v, inputImage.getf( u, v ) ));
		       				tabLabels[ u ][ v ] = INQUEUE;
		       			}
		       			else if ( tabLabels[ u ][ v ] > 0 && inputImage.getf( u, v ) <= pixelValue )
		       			{
		       				// assign label of smallest neighbor
		       				tabLabels[ i ][ j ] = tabLabels[ u ][ v ];
		       				pixelValue = inputImage.getf( u, v );
		       			}
		       		}
		       	} 
		       	
		       	// add current state to animation
		       	if( pixelValue > h )
		       	{
		       		h = pixelValue;
      				animation.addSlice("h="+h, new FloatProcessor( tabLabels ) );
		       	}
      		}//end while
      	}
      	else // without mask
      	{
      		while ( pixelList.isEmpty() == false )
      		{
      			if ( Thread.currentThread().isInterrupted() )
    				return null;	
      			
      			IJ.showProgress( numPixels-pixelList.size(), numPixels );

      			final PixelRecord pixelRecord = pixelList.poll();
      			final Cursor2D p = pixelRecord.getCursor();
	    		final int i = p.getX();
	    		final int j = p.getY();

      			double pixelValue = pixelRecord.getValue(); 

      			// Read neighbor coordinates
      			neigh.setCursor( p );

      			for( Cursor2D c : neigh.getNeighbors() )			       		
      			{
      				// Look in neighborhood for labeled pixels with
      				// smaller or equal original value
      				int u = c.getX();
      				int v = c.getY();

      				if ( u >= 0 && u < size1 && v >= 0 && v <  size2 )
      				{
      					// Unlabeled neighbors go into the queue if they are not there yet 
      					if ( tabLabels[ u ][ v ] == 0 )
      					{
      						pixelList.add( new PixelRecord( u, v, inputImage.getf( u, v ) ));
      						tabLabels[ u ][ v ] = INQUEUE;
      					}
      					else if ( tabLabels[ u ][ v ] > 0 && inputImage.getf( u, v ) <= pixelValue )
      					{
      						// assign label of smallest neighbor
      						tabLabels[ i ][ j ] = tabLabels[ u ][ v ];
      						pixelValue = inputImage.getf( u, v );
      					}
      				}
      			}
      			// add current state to animation
      			if( pixelValue > h )
      			{
      				h = pixelValue;
      				animation.addSlice("h="+h, new FloatProcessor( tabLabels ) );      				
      			}
      		}//end while    
      		
      	}

		final long end = System.currentTimeMillis();
		if( verbose ) IJ.log("  Flooding took: " + (end-start) + " ms");
		IJ.showProgress( 1.0 );
		
		// Create result label image
		FloatProcessor labelProcessor = new FloatProcessor(size1, size2);
		for (int i = 0; i < size1; ++i)
			for (int j = 0; j < size2; ++j)	
			{					
				if( tabLabels[ i ][ j ] == INIT ) // set unlabeled pixels to WSHED
					labelProcessor.setf( i, j, 0 );
				else
					labelProcessor.setf( i, j, tabLabels[ i ][ j ] );
			}
		animation.addSlice( "h=" + h, labelProcessor );		
		
	    return new ImagePlus( "Watershed flooding with priority queue", animation );
	}// end getAnimationPriorityQueue
	
	/**
	 * Get an animation of the different steps of the watershed 
	 * transform applied on inputImage, using the labeled 
	 * markers from markerImage and restricted to the white areas 
	 * of maskImage (optionally). This implementation uses a priority
	 * queue to visit first the pixels on the surroundings of the 
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
	 * @return animation stack with watershed domains by each h level (with dams)
	 */
	public ImagePlus getAnimationPriorityQueueAndDams()
	{
		if ( Thread.currentThread().isInterrupted() )					
			return null;
		
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

		// create animation stack
		final ImageStack animation = new ImageStack( size1, size2 );
		
		// list of original pixels values and corresponding coordinates
		PriorityQueue<PixelRecord> pixelList = null;
		
		// output labels
		final int[][] tabLabels = new int[ size1 ][ size2 ];
		// value INIT is assigned to each pixel of the output labels
	    for( int i=0; i<size1; i++ )	    	
	    	Arrays.fill( tabLabels[i], INIT );
		
		// Make list of pixels and sort it in ascending order
		IJ.showStatus( "Extracting pixel values..." );
		if( verbose ) IJ.log("  Extracting pixel values..." );
		final long t0 = System.currentTimeMillis();
		
		pixelList = extractPixelValuesPriorityQueue( inputImage, markerImage, tabLabels );		
		if( null == pixelList )
			return null;
						
		final long t1 = System.currentTimeMillis();		
		if( verbose ) IJ.log("  Extraction took " + (t1-t0) + " ms.");
		
		// current height level
      	double h = 0;
      	// add initial state to animation
     	animation.addSlice( "h=" + h, new FloatProcessor( tabLabels ) );
					    
		// Watershed
	    final long start = System.currentTimeMillis();
	         	
      	// Check connectivity
       	final Neighborhood2D neigh = connectivity == 8 ? 
       			new Neighborhood2DC8() : new Neighborhood2DC4();

	    final int count = pixelList.size();
	    if( verbose ) IJ.log( "  Flooding from " + count + " pixels..." );
      	IJ.showStatus("Flooding from " + count + " pixels...");
	    
      	double maxValue = inputImage.getMax();
      	
      	// list to store neighbor labels
      	final ArrayList <Integer> neighborLabels = new ArrayList<Integer>();
      	
      	final ArrayList <PixelRecord> neighborPixels = new ArrayList<PixelRecord>();
      	
      	// with mask
      	if ( null != maskImage )
      	{
      		if ( Thread.currentThread().isInterrupted() )
				return null;	
      		
      		while ( pixelList.isEmpty() == false )
      		{
      			if ( Thread.currentThread().isInterrupted() )
    				return null;	

      			final PixelRecord pixelRecord = pixelList.poll();
      			// show progression along pixel values
	    		IJ.showProgress( (pixelRecord.getValue() + 1) / (maxValue + 1));
	    		
      			final Cursor2D p = pixelRecord.getCursor();
	    		final int i = p.getX();
	    		final int j = p.getY();

      			// Read neighbor coordinates		       	
		       	neigh.setCursor( p );
		       	 
		       	// reset list of neighbor labels
		       	neighborLabels.clear();
		       	
		       	// reset list of neighbor pixels
		       	neighborPixels.clear();
		       	
		       	for( Cursor2D c : neigh.getNeighbors() )			       		
		       	{
		       		// Look in neighborhood for labeled pixels with
		       		// smaller or equal original value
		       		int u = c.getX();
		       		int v = c.getY();
		       		
		       		if ( u >= 0 && u < size1 && v >= 0 && v <  size2 )
		       		{
		       			// Unlabeled neighbors go into the queue if they are not there yet 
		       			if ( tabLabels[ u ][ v ] == INIT && maskImage.getf( u, v ) > 0 )
		       			{
		       				neighborPixels.add( new PixelRecord( c, inputImage.getf( u, v ) ) );
      					}
      					else if ( tabLabels[ u ][ v ] > 0 
      							&& neighborLabels.contains(tabLabels[ u ][ v ]) == false)
      					{
      						// store labels of neighbors in a list
      						neighborLabels.add( tabLabels[ u ][ v ] );
      					}
      				}
      			}
		       	// if the neighbors of the extracted pixel that have already been labeled 
		       	// all have the same label, then the pixel is labeled with their label.
      			if( neighborLabels.size() == 1 )
      			{
      				tabLabels[ i ][ j ] = neighborLabels.get( 0 );
      				// now that we know the pixel is labeled, add neighbors to list
      				for( PixelRecord v : neighborPixels )
      				{      					
      					tabLabels[ v.getCursor().getX() ][ v.getCursor().getY() ] = INQUEUE;
      					pixelList.add( v );
      				}
      			}
      			else if( neighborLabels.size() > 1 )
      				tabLabels[ i ][ j ] = WSHED;
      			
      			// update animation when increasing height
      			if( pixelRecord.getValue() > h )
      			{
      				h = pixelRecord.getValue();
      				animation.addSlice( "h=" + h, new FloatProcessor( tabLabels ) );      				
      			}
      			
      		}// end while list is not empty
      	}
      	else // without mask
      	{
      		while ( pixelList.isEmpty() == false )
      		{
      			if ( Thread.currentThread().isInterrupted() )
    				return null;	
      			
      			final PixelRecord pixelRecord = pixelList.poll();
      			// show progression along pixel values
	    		IJ.showProgress( (pixelRecord.getValue() + 1) / (maxValue + 1));
	    		
      			final Cursor2D p = pixelRecord.getCursor();
	    		final int i = p.getX();
	    		final int j = p.getY();

      			// Set cursor of neighborhood in pixel of interest
      			neigh.setCursor( p );
      			
      			// reset list of neighbor labels
		       	neighborLabels.clear();      
		       	
		       	// reset list of neighbor pixels
		       	neighborPixels.clear();
      			
		       	// Read neighbor coordinates
      			for( Cursor2D c : neigh.getNeighbors() )			       		
      			{      				      				
      				// Look in neighborhood for labeled pixels with
      				// smaller or equal original value
      				int u = c.getX();
      				int v = c.getY();
      				if ( u >= 0 && u < size1 && v >= 0 && v <  size2 )
      				{
      					// Unlabeled neighbors go into the queue if they are not there yet 
      					if ( tabLabels[ u ][ v ] == INIT )
      					{
 		       				neighborPixels.add( new PixelRecord( c, inputImage.getf( u, v ) ) );
      					}
      					else if ( tabLabels[ u ][ v ] > 0 
      							&& neighborLabels.contains(tabLabels[ u ][ v ]) == false)
      					{
      						// store labels of neighbors in a list without repetitions
      						neighborLabels.add( tabLabels[ u ][ v ] );
      					}
      				}
      			}
      			// if the neighbors of the extracted pixel that have already been labeled 
      			// all have the same label, then the pixel is labeled with their label
      			if( neighborLabels.size() == 1 )
      			{
      				tabLabels[ i ][ j ] = neighborLabels.get( 0 );
      				// now that we know the pixel is labeled, add unlabeled neighbors to list
      				for( PixelRecord v : neighborPixels )
      				{      					
      					tabLabels[ v.getCursor().getX() ][ v.getCursor().getY() ] = INQUEUE;
      					pixelList.add( v );
      				}
      			}
      			else if( neighborLabels.size() > 1 )
      				tabLabels[ i ][ j ] = WSHED;
      				
      			// update animation when increasing height
      			if( pixelRecord.getValue() > h )
      			{
      				h = pixelRecord.getValue();
      				animation.addSlice( "h=" + h, new FloatProcessor( tabLabels ) );      				
      			}
      		}
      	}

		final long end = System.currentTimeMillis();
		if( verbose ) IJ.log("  Flooding took: " + (end-start) + " ms");
		IJ.showStatus("");
		IJ.showProgress( 1.0 );
		
		// Create result label image
		FloatProcessor labelProcessor = new FloatProcessor(size1, size2);
		for (int i = 0; i < size1; ++i)
			for (int j = 0; j < size2; ++j)	
			{					
				if( tabLabels[ i ][ j ] == INIT ) // set unlabeled pixels to WSHED
					labelProcessor.setf( i, j, 0 );
				else
					labelProcessor.setf( i, j, tabLabels[ i ][ j ] );
			}
		animation.addSlice( "h=" + h, labelProcessor );
						
	    return new ImagePlus( "Watershed flooding with priority queue", animation );
	}
	
	/**
	 * Apply watershed transform on inputImage, using the labeled 
	 * markers from markerImage and restricted to the white areas 
	 * of maskImage (optionally). This implementation uses a priority
	 * queue to visit first the pixels on the surroundings of the 
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
	public ImageProcessor applyWithPriorityQueueAndDams()
	{
		if ( Thread.currentThread().isInterrupted() )					
			return null;
		
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
		
		// output labels
		final int[][] tabLabels = new int[ size1 ][ size2 ];
		// value INIT is assigned to each pixel of the output labels
	    for( int i=0; i<size1; i++ )	    	
	    	Arrays.fill( tabLabels[i], INIT );
		
		// Make list of pixels and sort it in ascending order
		IJ.showStatus( "Extracting pixel values..." );
		if( verbose ) IJ.log("  Extracting pixel values..." );
		final long t0 = System.currentTimeMillis();
		
		pixelList = extractPixelValuesPriorityQueue( inputImage, markerImage, tabLabels );		
		if( null == pixelList )
			return null;
						
		final long t1 = System.currentTimeMillis();		
		if( verbose ) IJ.log("  Extraction took " + (t1-t0) + " ms.");
					    
		// Watershed
	    final long start = System.currentTimeMillis();
	         	
      	// Check connectivity
       	final Neighborhood2D neigh = connectivity == 8 ? 
       			new Neighborhood2DC8() : new Neighborhood2DC4();

	    final int count = pixelList.size();
	    if( verbose )  IJ.log( "  Flooding from " + count + " pixels..." );
      	IJ.showStatus("Flooding from " + count + " pixels...");
	    
      	double maxValue = inputImage.getMax();
      	
      	// list to store neighbor labels
      	final ArrayList <Integer> neighborLabels = new ArrayList<Integer>();
      	
      	final ArrayList <PixelRecord> neighborPixels = new ArrayList<PixelRecord>();
      	
      	// with mask
      	if ( null != maskImage )
      	{
      		if ( Thread.currentThread().isInterrupted() )
				return null;	
      		
      		while ( pixelList.isEmpty() == false )
      		{
      			if ( Thread.currentThread().isInterrupted() )
    				return null;	

      			final PixelRecord pixelRecord = pixelList.poll();
      			// show progression along pixel values
	    		IJ.showProgress( (pixelRecord.getValue() + 1) / (maxValue + 1));
	    		
      			final Cursor2D p = pixelRecord.getCursor();
	    		final int i = p.getX();
	    		final int j = p.getY();

      			// Read neighbor coordinates		       	
		       	neigh.setCursor( p );
		       	 
		       	// reset list of neighbor labels
		       	neighborLabels.clear();
		       	
		       	// reset list of neighbor pixels
		       	neighborPixels.clear();
		       	
		       	for( Cursor2D c : neigh.getNeighbors() )			       		
		       	{
		       		// Look in neighborhood for labeled pixels with
		       		// smaller or equal original value
		       		int u = c.getX();
		       		int v = c.getY();
		       		
		       		if ( u >= 0 && u < size1 && v >= 0 && v <  size2 )
		       		{
		       			// Unlabeled neighbors go into the queue if they are not there yet 
		       			if ( tabLabels[ u ][ v ] == INIT && maskImage.getf( u, v ) > 0 )
		       			{
		       				neighborPixels.add( new PixelRecord( c, inputImage.getf( u, v ) ) );
      					}
      					else if ( tabLabels[ u ][ v ] > 0 
      							&& neighborLabels.contains(tabLabels[ u ][ v ]) == false)
      					{
      						// store labels of neighbors in a list
      						neighborLabels.add( tabLabels[ u ][ v ] );
      					}
      				}
      			}
		       	// if the neighbors of the extracted pixel that have already been labeled 
		       	// all have the same label, then the pixel is labeled with their label.
      			if( neighborLabels.size() == 1 )
      			{
      				tabLabels[ i ][ j ] = neighborLabels.get( 0 );
      				// now that we know the pixel is labeled, add neighbors to list
      				for( PixelRecord v : neighborPixels )
      				{      					
      					tabLabels[ v.getCursor().getX() ][ v.getCursor().getY() ] = INQUEUE;
      					pixelList.add( v );
      				}
      			}
      			else if( neighborLabels.size() > 1 )
      				tabLabels[ i ][ j ] = WSHED;
      		}
      	}
      	else // without mask
      	{
      		while ( pixelList.isEmpty() == false )
      		{
      			if ( Thread.currentThread().isInterrupted() )
    				return null;	
      			
      			final PixelRecord pixelRecord = pixelList.poll();
      			// show progression along pixel values
	    		IJ.showProgress( (pixelRecord.getValue() + 1) / (maxValue + 1));
	    		
      			final Cursor2D p = pixelRecord.getCursor();
	    		final int i = p.getX();
	    		final int j = p.getY();

      			// Set cursor of neighborhood in pixel of interest
      			neigh.setCursor( p );
      			
      			// reset list of neighbor labels
		       	neighborLabels.clear();      
		       	
		       	// reset list of neighbor pixels
		       	neighborPixels.clear();
      			
		       	// Read neighbor coordinates
      			for( Cursor2D c : neigh.getNeighbors() )			       		
      			{      				      				
      				// Look in neighborhood for labeled pixels with
      				// smaller or equal original value
      				int u = c.getX();
      				int v = c.getY();
      				if ( u >= 0 && u < size1 && v >= 0 && v <  size2 )
      				{
      					// Unlabeled neighbors go into the queue if they are not there yet 
      					if ( tabLabels[ u ][ v ] == INIT )
      					{
 		       				neighborPixels.add( new PixelRecord( c, inputImage.getf( u, v ) ) );
      					}
      					else if ( tabLabels[ u ][ v ] > 0 
      							&& neighborLabels.contains(tabLabels[ u ][ v ]) == false)
      					{
      						// store labels of neighbors in a list without repetitions
      						neighborLabels.add( tabLabels[ u ][ v ] );
      					}
      				}
      			}
      			// if the neighbors of the extracted pixel that have already been labeled 
      			// all have the same label, then the pixel is labeled with their label
      			if( neighborLabels.size() == 1 )
      			{
      				tabLabels[ i ][ j ] = neighborLabels.get( 0 );
      				// now that we know the pixel is labeled, add unlabeled neighbors to list
      				for( PixelRecord v : neighborPixels )
      				{      					
      					tabLabels[ v.getCursor().getX() ][ v.getCursor().getY() ] = INQUEUE;
      					pixelList.add( v );
      				}
      			}
      			else if( neighborLabels.size() > 1 )
      				tabLabels[ i ][ j ] = WSHED;
      				
      		}
      	}

		final long end = System.currentTimeMillis();
		if( verbose ) IJ.log("  Flooding took: " + (end-start) + " ms");
		IJ.showStatus("");
		IJ.showProgress( 1.0 );
	    
		// Create result label image
		ImageProcessor labelProcessor = markerImage.duplicate();
		for (int i = 0; i < size1; ++i)
			for (int j = 0; j < size2; ++j)	
			{					
				if( tabLabels[ i ][ j ] == INIT ) // set unlabeled pixels to WSHED
					labelProcessor.setf( i, j, 0 );
				else
					labelProcessor.setf( i, j, tabLabels[ i ][ j ] );
			}
						
	    return labelProcessor;
	}
	
	
	/**
	 * Extract pixel values from input and seed images
	 * 
	 * @param inputImage input stack
	 * @param seedImage seed stack
	 * @param tabLabels output label array
	 * @return priority queue of pixels neighboring the seeds
	 */
	public PriorityQueue<PixelRecord> extractPixelValuesPriorityQueue(
			final ImageProcessor inputImage,
			final ImageProcessor seedImage,
			final int[][] tabLabels) 
	{
		if ( Thread.currentThread().isInterrupted() )					
			return null;
				
		final int size1 = inputImage.getWidth();
	    final int size2 = inputImage.getHeight();

        final PriorityQueue<PixelRecord> pixelList = new PriorityQueue<PixelRecord>();
        
        // Auxiliary cursor to visit neighbors
	    final Cursor2D cursor = new Cursor2D( 0, 0 );
      	
      	// Check connectivity
       	final Neighborhood2D neigh = connectivity == 8 ? 
       			new Neighborhood2DC8() : new Neighborhood2DC4();
	    
		if( null != maskImage ) // apply mask
		{
			for( int x = 0; x < size1; ++x )
				for( int y = 0; y < size2; ++y )
					if( maskImage.getf( x, y ) > 0 )
					{
						int label = (int) seedImage.getf( x, y );
						if( label > 0 )
						{								
							cursor.set( x, y );
							neigh.setCursor( cursor );

							// add unlabeled neighbors to priority queue
							for( Cursor2D c : neigh.getNeighbors() )			       		
							{
								int u = c.getX();
								int v = c.getY();

								if ( u >= 0 && u < size1 && 
										v >= 0 && v < size2 && 
										(int) seedImage.getf( u, v ) == 0 &&
										tabLabels[ u ][ v ] != INQUEUE )															 
								{
									pixelList.add( new PixelRecord( u, v, inputImage.getf( u, v ) ) );
									tabLabels[ u ][ v ] = INQUEUE;
								}

							}
							tabLabels[ x ][ y ] = label;
						}
					}											
		}							
		else // without mask
		{
			for( int x = 0; x < size1; ++x )
				for( int y = 0; y < size2; ++y )
				{
					int label = (int) seedImage.getf( x, y );
					if( label > 0 )
					{
						cursor.set( x, y );
						neigh.setCursor( cursor );

						// add unlabeled neighbors to priority queue
						for( Cursor2D c : neigh.getNeighbors() )			       		
						{
							int u = c.getX();
							int v = c.getY();
							if ( u >= 0 && u < size1 && 
									v >= 0 && v < size2 && 
									(int) seedImage.getf( u, v ) == 0 &&
									tabLabels[ u ][ v ] != INQUEUE )															 
							{
								pixelList.add( new PixelRecord( u, v, inputImage.getf( u, v ) ) );
								tabLabels[ u ][ v ] = INQUEUE;
							}

						}
						tabLabels[ x ][ y ] = label;
					}
				}
		}

		return pixelList;
	}

	/**
	 * Extract pixel values from input and labeled marker images. The
	 * input grayscale values will be return in a list of PixelRecord 
	 * and the markers will be stored in <code>tabLabels</code>.
	 * 
	 * @param inputImage input grayscale stack (usually a gradient image)
	 * @param markerImage labeled marker stack
	 * @param tabLabels output label array
	 * @return list of input pixel values
	 */
	public LinkedList<PixelRecord> extractPixelValues(
			final ImageProcessor inputImage,
			final ImageProcessor markerImage,
			final int[][] tabLabels) 
	{
		
		final int size1 = inputImage.getWidth();
	    final int size2 = inputImage.getHeight();
			           
        final LinkedList<PixelRecord> list = new LinkedList<PixelRecord>();
	    
		if( null != maskImage )
		{
			for( int x = 0; x < size1; ++x )
				for( int y = 0; y < size2; ++y )
					if( maskImage.getf( x, y ) > 0 )
					{
						list.addLast( new PixelRecord( x, y, inputImage.getf( x, y )));
						tabLabels[ x ][ y ] = (int) markerImage.getf( x, y );
					}														
		}
		else
		{										       					
			for( int x = 0; x < size1; ++x )
				for( int y = 0; y < size2; ++y )					
				{
					list.addLast( new PixelRecord( x, y, inputImage.getf( x, y )));
					tabLabels[ x ][ y ] = (int) markerImage.getf( x, y );
				}										
			
		}// end else
				
		return list;
	}

}// end class MarkerControlledWatershedTransform2D
