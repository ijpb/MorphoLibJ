package inra.ijpb.watershed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.data.Cursor2D;
import inra.ijpb.data.Neighborhood2D;
import inra.ijpb.data.Neighborhood2DC4;
import inra.ijpb.data.Neighborhood2DC8;
import inra.ijpb.data.PixelRecord;
import inra.ijpb.data.image.Images3D;

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

/**
 * Class to apply the watershed algorithm to a 2D image. 
 * It allows specifying a binary mask to restrict the regions
 * of interest. 
 * 
 * @author Ignacio Arganda-Carreras
 */
public class WatershedTransform2D 
{
	/** input image (usually a gradient image) */
	ImageProcessor inputImage = null;
	
	/** binary mask to restrict the region of interest */
	ImageProcessor maskImage = null;
	
	/** pixel connectivity (expected 4 or 8) */
	int connectivity = 4;
	
	/** initial value of a threshold level */
	static final int MASK = -2;
	/** value of pixels belonging to watersheds */
	static final int WSHED = 0;
	/** initial value of output pixels */
	static final int INIT = -1;	
	/** value assigned to pixels put into the queue */
	static final int INQUEUE = -3;
	
	/** flag to output the transform steps and their 
	 * execution times in the log window */
	protected boolean verbose = true;
	
	
	/**
	 * Construct a watershed transform
	 * 
	 * @param input input image (usually a gradient image)
	 * @param mask binary mask to restrict the region of interest (null to use whole input image)
	 */
	public WatershedTransform2D(
			final ImageProcessor input,
			final ImageProcessor mask )
	{
		this.inputImage = input;
		this.maskImage = mask;
	}
	
	/**
	 * Construct a watershed transform
	 * 
	 * @param input input image (usually a gradient image)
	 * @param mask binary mask to restrict the region of interest (null to use whole input image)
	 * @param connectivity pixel connectivity (4 or 8)
	 */
	public WatershedTransform2D(
			final ImageProcessor input,
			final ImageProcessor mask,
			final int connectivity )
	{
		this.inputImage = input;
		this.maskImage = mask;
		
		if( connectivity != 4 && connectivity != 8 ) 
	    {
			throw new IllegalArgumentException("Illegal connectivity value: it must be 4 or 8!");
		}
		
		this.connectivity = connectivity;
	}
	
	/**
	 * Get the pixel connectivity (4 or 8)
	 * @return pixel connectivity
	 */
	public int getConnectivity() {
		return this.connectivity;
	}
	
	/**
	 * Set the pixel connectivity (4 or 8)
	 * @param conn pixel connectivity
	 */
	public void setConnectivity(int conn) {
		if( conn == 4 || conn == 8)
			this.connectivity = conn;
	}
	
	/**
	 * Set verbose flag
	 * @param verbose new verbose flag
	 */
	public void setVerbose( boolean verbose ){
		this.verbose = verbose;
	}
	
	/**
	 * Apply fast watersheds using flooding simulations, as described
	 * by Soille, Pierre, and Luc M. Vincent. "Determining watersheds 
	 * in digital pictures via flooding simulations." Lausanne-DL 
	 * tentative. International Society for Optics and Photonics, 1990.
	 *
	 * @return image of labeled catchment basins (with dams)
	 */
	public ImageProcessor apply()
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
	public ImageProcessor apply(
			double hMin,
			double hMax )
	{
		if( null != this.maskImage )
			return applyWithMask( hMin, hMax );
		else
			return applyWithoutMask( hMin, hMax );

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
	private ImageProcessor applyWithMask() 
	{
		return applyWithMask( inputImage.getMin(), inputImage.getMax() );
	}
	
	/**
	 * Apply fast watersheds using flooding simulations, as described
	 * by Soille, Pierre, and Luc M. Vincent. "Determining watersheds 
	 * in digital pictures via flooding simulations." Lausanne-DL 
	 * tentative. International Society for Optics and Photonics, 1990.
	 *
	 * @return image of labeled catchment basins (with dams)
	 */
	private ImageProcessor applyWithoutMask() 
	{
		return applyWithoutMask( inputImage.getMin(), inputImage.getMax() );
	}
	
	
	
	/**
	 * Apply fast watersheds using flooding simulations, as described
	 * by Soille, Pierre, and Luc M. Vincent. "Determining watersheds 
	 * in digital pictures via flooding simulations." Lausanne-DL 
	 * tentative. International Society for Optics and Photonics, 1990.
	 * This implementation uses a binary mask to restrict regions of
	 * application.
	 *
	 * @param hMin minimum grayscale level height
	 * @param hMax maximum grayscale level height
	 * @return 32-bit image of labeled catchment basins (with dams)
	 */
	private ImageProcessor applyWithMask(
			double hMin,
			double hMax ) 
	{
	    final int size1 = inputImage.getWidth();
	    final int size2 = inputImage.getHeight();
	       	    
	    // output labels
	    final int[][] tabLabels = new int[ size1 ][ size2 ]; 
	    
	    // value INIT is assigned to each pixel of the output labels
	    for( int i=0; i<size1; i++ )
	    		Arrays.fill( tabLabels[i], INIT );
	    
	    int currentLabel = 0;
	    
	    boolean flag = false;	    
	    
	    // Make list of pixels and sort it in ascending order
	    IJ.showStatus( "Extracting pixel values..." );
	    if( verbose ) IJ.log("  Extracting pixel values (h_min = " + hMin + ", h_max = " + hMax + ")..." );
	    final long t0 = System.currentTimeMillis();

	    // list of original pixels values and corresponding coordinates
	    ArrayList<PixelRecord> pixelList = extractPixelValues( inputImage, hMin, hMax );

	    final long t1 = System.currentTimeMillis();		
	    if( verbose ) IJ.log("  Extraction took " + (t1-t0) + " ms.");
	    if( verbose ) IJ.log("  Sorting pixels by value..." );
	    IJ.showStatus("Sorting pixels by value...");
	    Collections.sort( pixelList );
	    final long t2 = System.currentTimeMillis();
	    if( verbose ) IJ.log("  Sorting took " + (t2-t1) + " ms.");
	    
	    IJ.log( "  Flooding..." );
	    IJ.showStatus( "Flooding..." );
	    final long start = System.currentTimeMillis();
	    
      	// Check connectivity
       	final Neighborhood2D neigh = connectivity == 4 ? 
       									new Neighborhood2DC4() : new Neighborhood2DC8();
	    	    
	    LinkedList<Cursor2D> fifo = new LinkedList<Cursor2D>();
	      
        // initial height
        double h = hMin;
        
        // find corresponding pixel index
        int currentIndex = 0;
        while( h < hMin )
        {
        	h = pixelList.get( currentIndex ).getValue();
        	currentIndex++;
        }
        
        int heightIndex1 = currentIndex;
        int heightIndex2 = currentIndex;
        
        
	    // for h <- h_min to h_max; geodesic SKIZ of level h-1 inside level h
	    while( currentIndex < pixelList.size() && h <= hMax )
	    {	    	
	    	h = pixelList.get( currentIndex ).getValue();	    	
	    		    		    		    	
	    	for(int pixelIndex = heightIndex1; pixelIndex < pixelList.size(); pixelIndex ++)
	    	{
	    		final PixelRecord PixelRecord = pixelList.get( pixelIndex );
	    			    		
	    		if( PixelRecord.getValue() != h )
	    		{
	    			// this pixel is at level h+1
	    			heightIndex1 = pixelIndex;
	    			break;
	    		}
	    			    		
	    		final Cursor2D p = PixelRecord.getCursor();
	    		final int i = p.getX();
	    		final int j = p.getY();
	    			    		
	    		// set label to MASK
	    		tabLabels[ i ][ j ] = MASK;

	    		// read neighbor coordinates	    		
	    		neigh.setCursor( p );
	    		for( Cursor2D c : neigh.getNeighbors() )			       		
	    		{       			
	    			int u = c.getX();
	    			int v = c.getY();

	    			// initialize queue with neighbors at level h of current basins or watersheds
	    			if ( u >= 0 && u < size1 && v >= 0 && v < size2 
	    					&& tabLabels[ u ][ v ] >= WSHED 
	    					&& maskImage.getf( u, v ) > 0 ) 
	    				//&&  ( tabLabels[ u ][ v ] > 0 || tabLabels[ u ][ v ] == WSHED ) )
	    				{
	    					fifo.addLast( p );
	    					tabLabels[ i ][ j ] = INQUEUE;
	    					break;
	    				}	    			
	    		}// end for	    	
	    	}// end for

	    	while( fifo.isEmpty() == false )
	    	{
	    		// retrieve point p
	    		final Cursor2D p = fifo.poll();	    		
	    		final int i = p.getX();
	    		final int j = p.getY();

	    		// read neighbor coordinates
	    		neigh.setCursor( p );

	    		for( Cursor2D c : neigh.getNeighbors() )			       		
	    		{
	    			// labeling current point by inspecting neighbors
	    			int u = c.getX();
	    			int v = c.getY();

	    			if ( u >= 0 && u < size1 && v >= 0 && v < size2 && maskImage.getf( u, v ) > 0 )
	    			{
	    				if ( tabLabels[ u ][ v ] > 0 ) // i.e. the pixel belongs to an already labeled basin
	    				{
	    					if ( tabLabels[ i ][ j ] == INQUEUE || (tabLabels[ i ][ j ] == WSHED && flag == true ) )
	    					{
	    						tabLabels[ i ][ j ] = tabLabels[ u ][ v ];
	    					}
	    					else if ( tabLabels[ i ][ j ] > 0 && tabLabels[ i ][ j ] != tabLabels[ u ][ v ] )
	    					{
	    						tabLabels[ i ][ j ] = WSHED;
	    						flag = false;
	    					}       					
	    				}
	    				else if ( tabLabels[ u ][ v ] == WSHED )	    					
	    				{
	    					if( tabLabels[ i ][ j ] == INQUEUE )
	    					{
	    						tabLabels[ i ][ j ] = WSHED;
	    						flag = true;
	    					}
	    				}
	    				else if ( tabLabels[ u ][ v ] == MASK )
	    				{
	    					tabLabels[ u ][ v ] = INQUEUE;
	    					fifo.addLast( c );

	    				}
	    			}       			       			
	    		}	    	
	    	}

	    	// check for new minima at level h
	    		    	
	    	for(int pixelIndex = heightIndex2; pixelIndex < pixelList.size(); pixelIndex ++, currentIndex++)
	    	{
	    		final PixelRecord PixelRecord = pixelList.get( pixelIndex );	    			    		
	    		
	    		if( PixelRecord.getValue() != h )
	    		{
	    			// this pixel is at level h+1
	    			heightIndex2 = pixelIndex;
	    			break;
	    		}
	    			    		
	    		final Cursor2D p = PixelRecord.getCursor();
	    		final int i = p.getX();
	    		final int j = p.getY();
	    		
	    		if ( tabLabels[ i ][ j ] == MASK ) // the pixel is inside a new minimum
	    		{
	    			currentLabel ++;
	    			fifo.addLast( p );
	    			tabLabels[ i ][ j ] = currentLabel;
	    			
	    			while( fifo.isEmpty() == false )
	    	    	{
	    				final Cursor2D p2 = fifo.poll();

	    	    		// read neighbor coordinates
	    	    		neigh.setCursor( p2 );

	    	    		for( Cursor2D c : neigh.getNeighbors() ) // inspect neighbors of p2		       		
	    	    		{       			
	    	    			int u = c.getX();
	    	    			int v = c.getY();
	    	    			
	    	    			if ( u >= 0 && u < size1 && v >= 0 && v < size2 
	    	    					&& tabLabels[ u ][ v ] == MASK 
	    	    					&& maskImage.getf( u, v ) > 0 )
	    	    			{
	    	    				fifo.addLast( c );
	    	    				tabLabels[ u ][ v ] = currentLabel;
	    	    			}	    	    				    	    			
	    	    		}// end for
	    	    	}// end while
	    		}// end if	    		
	    	}// end for
	    		    		    	
	    	IJ.showProgress( h / hMax );
	    	
	    }// end while (flooding)
	    
	    IJ.showProgress( 1.0 );
	    
	    final long end = System.currentTimeMillis();
		if( verbose ) IJ.log("  Flooding took: " + (end-start) + " ms");
	    
	    // Create result label image
	    	
		FloatProcessor fp = new FloatProcessor( size1, size2 );
		for (int i = 0; i < size1; ++i)
			for (int j = 0; j < size2; ++j)
			{
				if( tabLabels[ i ][ j ] == INIT ) // set unlabeled pixels to 0
					fp.setf( i, j, 0 );	
				else
					fp.setf( i, j, tabLabels[i][j] );
			}		
	    				    
	    return fp;
	}
	
	
	/**
	 * Apply fast watersheds using flooding simulations, as described
	 * by Soille, Pierre, and Luc M. Vincent. "Determining watersheds 
	 * in digital pictures via flooding simulations." Lausanne-DL 
	 * tentative. International Society for Optics and Photonics, 1990.
	 *
	 * @param hMin minimum grayscale level height
	 * @param hMax maximum grayscale level height
	 * @return 32-bit image of labeled catchment basins (with dams)
	 */
	private ImageProcessor applyWithoutMask(
			double hMin,
			double hMax ) 
	{
	    final int size1 = inputImage.getWidth();
	    final int size2 = inputImage.getHeight();
	       	    
	    // output labels
	    final int[][] tabLabels = new int[ size1 ][ size2 ]; 
	    
	    // value INIT is assigned to each pixel of the output labels
	    for( int i=0; i<size1; i++ )
	    		Arrays.fill( tabLabels[i], INIT );
	    
	    int currentLabel = 0;
	    
	    boolean flag = false;	    
	    
	    // Make list of pixels and sort it in ascending order
	    IJ.showStatus( "Extracting pixel values..." );
	    if( verbose ) IJ.log("  Extracting pixel values (h_min = " + hMin + ", h_max = " + hMax + ")..." );
	    final long t0 = System.currentTimeMillis();

	    // list of original pixels values and corresponding coordinates
	    ArrayList<PixelRecord> pixelList = extractPixelValues( inputImage, hMin, hMax );

	    final long t1 = System.currentTimeMillis();		
	    if( verbose ) IJ.log("  Extraction took " + (t1-t0) + " ms.");
	    if( verbose ) IJ.log("  Sorting pixels by value..." );
	    IJ.showStatus("Sorting pixels by value...");
	    Collections.sort( pixelList );
	    final long t2 = System.currentTimeMillis();
	    if( verbose ) IJ.log("  Sorting took " + (t2-t1) + " ms.");
	    
	    IJ.log( "  Flooding..." );
	    IJ.showStatus( "Flooding..." );
	    final long start = System.currentTimeMillis();
	    
      	// Check connectivity
       	final Neighborhood2D neigh = connectivity == 4 ? 
       									new Neighborhood2DC4() : new Neighborhood2DC8();
	    	    
	    LinkedList<Cursor2D> fifo = new LinkedList<Cursor2D>();
	      
        // initial height
        double h = hMin;
        
        // find corresponding pixel index
        int currentIndex = 0;
        while( h < hMin )
        {
        	h = pixelList.get( currentIndex ).getValue();
        	currentIndex++;
        }
        
        int heightIndex1 = currentIndex;
        int heightIndex2 = currentIndex;
        
        
	    // for h <- h_min to h_max; geodesic SKIZ of level h-1 inside level h
	    while( currentIndex < pixelList.size() && h <= hMax )
	    {	    	
	    	h = pixelList.get( currentIndex ).getValue();	    	
	    		    		    		    	
	    	for(int pixelIndex = heightIndex1; pixelIndex < pixelList.size(); pixelIndex ++)
	    	{
	    		final PixelRecord PixelRecord = pixelList.get( pixelIndex );
	    			    		
	    		if( PixelRecord.getValue() != h )
	    		{
	    			// this pixel is at level h+1
	    			heightIndex1 = pixelIndex;
	    			break;
	    		}
	    			    		
	    		final Cursor2D p = PixelRecord.getCursor();
	    		final int i = p.getX();
	    		final int j = p.getY();
	    			    		
	    		// set label to MASK
	    		tabLabels[ i ][ j ] = MASK;

	    		// read neighbor coordinates	    		
	    		neigh.setCursor( p );
	    		for( Cursor2D c : neigh.getNeighbors() )			       		
	    		{       			
	    			int u = c.getX();
	    			int v = c.getY();

	    			// initialize queue with neighbors at level h of current basins or watersheds
	    			if ( u >= 0 && u < size1 && v >= 0 && v < size2 
	    					&& tabLabels[ u ][ v ] >= WSHED ) 
	    				//&&  ( tabLabels[ u ][ v ] > 0 || tabLabels[ u ][ v ] == WSHED ) )
	    				{
	    					fifo.addLast( p );
	    					tabLabels[ i ][ j ] = INQUEUE;
	    					break;
	    				}	    			
	    		}// end for	    	
	    	}// end for

	    	while( fifo.isEmpty() == false )
	    	{
	    		// retrieve point p
	    		final Cursor2D p = fifo.poll();	    		
	    		final int i = p.getX();
	    		final int j = p.getY();

	    		// read neighbor coordinates
	    		neigh.setCursor( p );

	    		for( Cursor2D c : neigh.getNeighbors() )			       		
	    		{
	    			// labeling current point by inspecting neighbors
	    			int u = c.getX();
	    			int v = c.getY();

	    			if ( u >= 0 && u < size1 && v >= 0 && v < size2 )
	    			{
	    				if ( tabLabels[ u ][ v ] > 0 ) // i.e. the pixel belongs to an already labeled basin
	    				{
	    					if ( tabLabels[ i ][ j ] == INQUEUE || (tabLabels[ i ][ j ] == WSHED && flag == true ) )
	    					{
	    						tabLabels[ i ][ j ] = tabLabels[ u ][ v ];
	    					}
	    					else if ( tabLabels[ i ][ j ] > 0 && tabLabels[ i ][ j ] != tabLabels[ u ][ v ] )
	    					{
	    						tabLabels[ i ][ j ] = WSHED;
	    						flag = false;
	    					}       					
	    				}
	    				else if ( tabLabels[ u ][ v ] == WSHED )	    					
	    				{
	    					if( tabLabels[ i ][ j ] == INQUEUE )
	    					{
	    						tabLabels[ i ][ j ] = WSHED;
	    						flag = true;
	    					}
	    				}
	    				else if ( tabLabels[ u ][ v ] == MASK )
	    				{
	    					tabLabels[ u ][ v ] = INQUEUE;
	    					fifo.addLast( c );

	    				}
	    			}       			       			
	    		}	    	
	    	}

	    	// check for new minima at level h
	    		    	
	    	for(int pixelIndex = heightIndex2; pixelIndex < pixelList.size(); pixelIndex ++, currentIndex++)
	    	{
	    		final PixelRecord PixelRecord = pixelList.get( pixelIndex );	    			    		
	    		
	    		if( PixelRecord.getValue() != h )
	    		{
	    			// this pixel is at level h+1
	    			heightIndex2 = pixelIndex;
	    			break;
	    		}
	    			    		
	    		final Cursor2D p = PixelRecord.getCursor();
	    		final int i = p.getX();
	    		final int j = p.getY();
	    		
	    		if ( tabLabels[ i ][ j ] == MASK ) // the pixel is inside a new minimum
	    		{
	    			currentLabel ++;
	    			fifo.addLast( p );
	    			tabLabels[ i ][ j ] = currentLabel;
	    			
	    			while( fifo.isEmpty() == false )
	    	    	{
	    				final Cursor2D p2 = fifo.poll();

	    	    		// read neighbor coordinates
	    	    		neigh.setCursor( p2 );

	    	    		for( Cursor2D c : neigh.getNeighbors() ) // inspect neighbors of p2		       		
	    	    		{       			
	    	    			int u = c.getX();
	    	    			int v = c.getY();
	    	    			
	    	    			if ( u >= 0 && u < size1 && v >= 0 && v < size2 
	    	    					&& tabLabels[ u ][ v ] == MASK )
	    	    			{
	    	    				fifo.addLast( c );
	    	    				tabLabels[ u ][ v ] = currentLabel;
	    	    			}	    	    				    	    			
	    	    		}// end for
	    	    	}// end while
	    		}// end if	    		
	    	}// end for
	    		    		    	
	    	IJ.showProgress( h / hMax );
	    	
	    }// end while (flooding)
	    
	    IJ.showProgress( 1.0 );
	    
	    final long end = System.currentTimeMillis();
		if( verbose ) IJ.log("  Flooding took: " + (end-start) + " ms");
	    
	    // Create result label image
	    	
		FloatProcessor fp = new FloatProcessor( size1, size2 );
		for (int i = 0; i < size1; ++i)
			for (int j = 0; j < size2; ++j)
			{
				if( tabLabels[ i ][ j ] == INIT ) // set unlabeled pixels to 0
					fp.setf( i, j, 0 );	
				else
					fp.setf( i, j, tabLabels[i][j] );
			}		
	    				    
	    return fp;
	}
	
	
	
	/**
	 * Extract pixel values from input image such that
	 * they have value h, hMin <= h <= hMax. A binary
	 * mask is used if it exists.
	 * 
	 * @param inputImage input image
	 * @param hMin minimum grayscale height value
	 * @param hMax maximum grayscale height value
	 * @return list of input pixel values
	 */
	public ArrayList<PixelRecord> extractPixelValues(
			final ImageProcessor inputImage,
			final double hMin,
			final double hMax ) 
	{
		
		final int size1 = inputImage.getWidth();
	    final int size2 = inputImage.getHeight();
			         
        final ArrayList<PixelRecord> list = new ArrayList<PixelRecord>();
	    
		if( null != maskImage )
		{
										
			for( int x = 0; x < size1; ++x )
				for( int y = 0; y < size2; ++y )
				{
					final double h = inputImage.getf( x, y );
					if( maskImage.getf( x, y ) > 0 && h >= hMin && h <= hMax )
					{
						list.add( new PixelRecord( x, y, h ) );								
					}
				}
		}
		else
		{
			
			for( int x = 0; x < size1; ++x )
				for( int y = 0; y < size2; ++y )
				{
					final double h = inputImage.getf( x, y );
					if( h >= hMin && h <= hMax )
						list.add( new PixelRecord( x, y, h ) );
				}

		}
			
		return list;
	}

	/**
	 * Apply fast watersheds using flooding simulations, as described
	 * by Soille, Pierre, and Luc M. Vincent. "Determining watersheds 
	 * in digital pictures via flooding simulations." Lausanne-DL 
	 * tentative. International Society for Optics and Photonics, 1990.
	 * This implementation uses a binary mask to restrict regions of
	 * application if the mask exists.
	 *
	 * @param hMin minimum grayscale level height
	 * @param hMax maximum grayscale level height
	 * @return 32-bit stack with the progression of labeled catchment basins (with dams)
	 */
	public ImagePlus getAnimation(
			double hMin,
			double hMax ) 
	{
	    final int size1 = inputImage.getWidth();
	    final int size2 = inputImage.getHeight();
	       	    
	    final ImageStack animation = new ImageStack( size1, size2 );
	    
	    final boolean useMask = null != this.maskImage;
	    
	    // output labels
	    final int[][] tabLabels = new int[ size1 ][ size2 ]; 
	    
	    // value INIT is assigned to each pixel of the output labels
	    for( int i=0; i<size1; i++ )
	    		Arrays.fill( tabLabels[i], INIT );
	    
	    int currentLabel = 0;
	    
	    boolean flag = false;	    
	    
	    // Make list of pixels and sort it in ascending order
	    IJ.showStatus( "Extracting pixel values..." );
	    if( verbose ) IJ.log("  Extracting pixel values (h_min = " + hMin + ", h_max = " + hMax + ")..." );
	    final long t0 = System.currentTimeMillis();

	    // list of original pixels values and corresponding coordinates
	    ArrayList<PixelRecord> pixelList = extractPixelValues( inputImage, hMin, hMax );

	    final long t1 = System.currentTimeMillis();		
	    if( verbose ) IJ.log("  Extraction took " + (t1-t0) + " ms.");
	    if( verbose ) IJ.log("  Sorting pixels by value..." );
	    IJ.showStatus("Sorting pixels by value...");
	    Collections.sort( pixelList );
	    final long t2 = System.currentTimeMillis();
	    if( verbose ) IJ.log("  Sorting took " + (t2-t1) + " ms.");
	    
	    IJ.log( "  Flooding..." );
	    IJ.showStatus( "Flooding..." );
	    final long start = System.currentTimeMillis();
	    
      	// Check connectivity
       	final Neighborhood2D neigh = connectivity == 4 ? 
       									new Neighborhood2DC4() : new Neighborhood2DC8();
	    	    
	    LinkedList<Cursor2D> fifo = new LinkedList<Cursor2D>();
	      
        // initial height
        double h = hMin;
        
        // find corresponding pixel index
        int currentIndex = 0;
        while( h < hMin )
        {
        	h = pixelList.get( currentIndex ).getValue();
        	currentIndex++;
        }
        
        int heightIndex1 = currentIndex;
        int heightIndex2 = currentIndex;
                
	    // for h <- h_min to h_max; geodesic SKIZ of level h-1 inside level h
	    while( currentIndex < pixelList.size() && h <= hMax )
	    {	    	
	    	h = pixelList.get( currentIndex ).getValue();	    	
	    		    		    		    	
	    	for(int pixelIndex = heightIndex1; pixelIndex < pixelList.size(); pixelIndex ++)
	    	{
	    		final PixelRecord PixelRecord = pixelList.get( pixelIndex );
	    			    		
	    		if( PixelRecord.getValue() != h )
	    		{
	    			// this pixel is at level h+1
	    			heightIndex1 = pixelIndex;
	    			break;
	    		}
	    			    		
	    		final Cursor2D p = PixelRecord.getCursor();
	    		final int i = p.getX();
	    		final int j = p.getY();
	    			    		
	    		// set label to MASK
	    		tabLabels[ i ][ j ] = MASK;

	    		// read neighbor coordinates	    		
	    		neigh.setCursor( p );
	    		for( Cursor2D c : neigh.getNeighbors() )			       		
	    		{       			
	    			int u = c.getX();
	    			int v = c.getY();

	    			// initialize queue with neighbors at level h of current basins or watersheds
	    			if( useMask == false || maskImage.getf( u, v ) > 0 )
	    				if ( u >= 0 && u < size1 && v >= 0 && v < size2 
	    					&& tabLabels[ u ][ v ] >= WSHED	) 
	    					{
	    						fifo.addLast( p );
	    						tabLabels[ i ][ j ] = INQUEUE;
	    						break;
	    					}	    			
	    		}// end for	    	
	    	}// end for

	    	while( fifo.isEmpty() == false )
	    	{
	    		// retrieve point p
	    		final Cursor2D p = fifo.poll();	    		
	    		final int i = p.getX();
	    		final int j = p.getY();

	    		// read neighbor coordinates
	    		neigh.setCursor( p );

	    		for( Cursor2D c : neigh.getNeighbors() )			       		
	    		{
	    			// labeling current point by inspecting neighbors
	    			int u = c.getX();
	    			int v = c.getY();

	    			if( useMask == false || maskImage.getf( u, v ) > 0 )
	    				if ( u >= 0 && u < size1 && v >= 0 && v < size2 )
	    				{
	    					if ( tabLabels[ u ][ v ] > 0 ) // i.e. the pixel belongs to an already labeled basin
	    					{
	    						if ( tabLabels[ i ][ j ] == INQUEUE || (tabLabels[ i ][ j ] == WSHED && flag == true ) )
	    						{
	    							tabLabels[ i ][ j ] = tabLabels[ u ][ v ];
	    						}
	    						else if ( tabLabels[ i ][ j ] > 0 && tabLabels[ i ][ j ] != tabLabels[ u ][ v ] )
	    						{
	    							tabLabels[ i ][ j ] = WSHED;
	    							flag = false;
	    						}       					
	    					}
	    					else if ( tabLabels[ u ][ v ] == WSHED && tabLabels[ i ][ j ] == INQUEUE )
	    					{
	    						tabLabels[ i ][ j ] = WSHED;
	    						flag = true;
	    					}
	    					else if ( tabLabels[ u ][ v ] == MASK )
	    					{
	    						tabLabels[ u ][ v ] = INQUEUE;
	    						fifo.addLast( c );
	    					}
	    				}       			       			
	    		}	    	
	    	}

	    	// check for new minima at level h
	    		    	
	    	for(int pixelIndex = heightIndex2; pixelIndex < pixelList.size(); pixelIndex ++, currentIndex++)
	    	{
	    		final PixelRecord PixelRecord = pixelList.get( pixelIndex );	    			    		
	    		
	    		if( PixelRecord.getValue() != h )
	    		{
	    			// this pixel is at level h+1
	    			heightIndex2 = pixelIndex;
	    			break;
	    		}
	    			    		
	    		final Cursor2D p = PixelRecord.getCursor();
	    		final int i = p.getX();
	    		final int j = p.getY();
	    		
	    		if ( tabLabels[ i ][ j ] == MASK ) // the pixel is inside a new minimum
	    		{
	    			currentLabel ++;
	    			fifo.addLast( p );
	    			tabLabels[ i ][ j ] = currentLabel;
	    			
	    			while( fifo.isEmpty() == false )
	    	    	{
	    				final Cursor2D p2 = fifo.poll();

	    	    		// read neighbor coordinates
	    	    		neigh.setCursor( p2 );
	    	    		
	    	    		for( Cursor2D c : neigh.getNeighbors() ) // inspect neighbors of p2		       		
	    	    		{       			
	    	    			int u = c.getX();
	    	    			int v = c.getY();
	    	    			if( useMask == false || maskImage.getf( u, v ) > 0 )
	    	    				if ( u >= 0 && u < size1 && v >= 0 && v < size2 
	    	    					&& tabLabels[ u ][ v ] == MASK )
	    	    					{
	    	    						fifo.addLast( c );
	    	    						tabLabels[ u ][ v ] = currentLabel;
	    	    					}	    	    				    	    			
	    	    		}// end for
	    	    	}// end while
	    		}// end if	    		
	    	}// end for
	    		    	
	    	// update animation
	    	
	    	animation.addSlice( "h = " + h, new FloatProcessor( tabLabels ) );
	    	
	    	IJ.showProgress( h / hMax );
	    	
	    }// end while (flooding)
	    
	    IJ.showProgress( 1.0 );
	    
	    final long end = System.currentTimeMillis();
		if( verbose ) IJ.log("  Flooding took: " + (end-start) + " ms");	    	   
	    				    
	    ImagePlus result = new ImagePlus( "Watershed flooding", animation );
	    Images3D.optimizeDisplayRange( result );
	    return result;
	}
	
	
}// end class WatershedTransform2D
