/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2017 INRA.
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

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

/**
 * Several static methods for computing watershed in 2D/3D images. 
 * @author Ignacio Arganda-Carreras
 */
public class Watershed 
{
	/**
	 * Compute fast watershed using flooding simulations, as described by 
	 * Soille, Pierre, and Luc M. Vincent. "Determining watersheds in 
	 * digital pictures via flooding simulations." Lausanne-DL tentative. 
	 * International Society for Optics and Photonics, 1990.
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param mask binary mask to restrict the regions of interest
	 * @param connectivity voxel connectivity to define neighborhoods
	 * @return image of labeled catchment basins with dams (labels are 1, 2, ...)
	 */
	public static ImagePlus computeWatershed( 
			ImagePlus input,
			ImagePlus mask,
			int connectivity )
	{
		WatershedTransform3D wt = new WatershedTransform3D( input, mask, connectivity );
		
		return wt.apply();		
	}
	
	/**
	 * Compute fast watershed using flooding simulations, as described by 
	 * Soille, Pierre, and Luc M. Vincent. "Determining watersheds in 
	 * digital pictures via flooding simulations." Lausanne-DL tentative. 
	 * International Society for Optics and Photonics, 1990.
	 * Note: it outputs step messages in the Log window.
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param mask binary mask to restrict the regions of interest
	 * @param connectivity voxel connectivity to define neighborhoods
	 * @param hMin the minimum value for dynamic
	 * @param hMax the maximum value for dynamic 
	 * @return image of labeled catchment basins with dams (labels are 1, 2, ...)
	 */
	public static ImagePlus computeWatershed( 
			ImagePlus input,
			ImagePlus mask,
			int connectivity,
			double hMin,
			double hMax)
	{
		return Watershed.computeWatershed(input, mask, connectivity, hMin, hMax, true );
	}
	/**
	 * Compute fast watershed using flooding simulations, as described by 
	 * Soille, Pierre, and Luc M. Vincent. "Determining watersheds in 
	 * digital pictures via flooding simulations." Lausanne-DL tentative. 
	 * International Society for Optics and Photonics, 1990.
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param mask binary mask to restrict the regions of interest
	 * @param connectivity voxel connectivity to define neighborhoods
	 * @param hMin the minimum value for dynamic
	 * @param hMax the maximum value for dynamic
	 * @param verbose flag to output step messages in Log window
	 * @return image of labeled catchment basins with dams (labels are 1, 2, ...)
	 */
	public static ImagePlus computeWatershed( 
			ImagePlus input,
			ImagePlus mask,
			int connectivity,
			double hMin,
			double hMax,
			boolean verbose )
	{
		if( connectivity == 6 || connectivity == 26 )
		{
			WatershedTransform3D wt = new WatershedTransform3D( input, mask, connectivity );
			wt.setVerbose( verbose );
			return wt.apply( hMin, hMax );
		}
		else if( connectivity == 4 || connectivity == 8 )
		{
			WatershedTransform2D wt = 
					new WatershedTransform2D( input.getProcessor(), 
							null != mask ? mask.getProcessor() : null, connectivity );
			wt.setVerbose( verbose );
			final ImageProcessor ip = wt.apply( hMin, hMax );
			if( null != ip )
			{
				String title = input.getTitle();
				String ext = "";
				int index = title.lastIndexOf( "." );
				if( index != -1 )
				{
					ext = title.substring( index );
					title = title.substring( 0, index );				
				}
				
				final ImagePlus ws = new ImagePlus( title + "-watershed" + ext, ip );
				ws.setCalibration( input.getCalibration() );
				return ws;
			}
			else
				return null;
		}
		else
			return null;
	}
	
	/**
	 * Compute fast watershed using flooding simulations, as described by 
	 * Soille, Pierre, and Luc M. Vincent. "Determining watersheds in 
	 * digital pictures via flooding simulations." Lausanne-DL tentative. 
	 * International Society for Optics and Photonics, 1990.
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param mask binary mask to restrict the regions of interest
	 * @param verbose flag to output step messages in Log window
	 * @param connectivity voxel connectivity to define neighborhoods
	 * @return image of labeled catchment basins with dams (labels are 1, 2, ...)
	 */
	public static ImageStack computeWatershed( 
			ImageStack input,
			ImageStack mask,
			boolean verbose,
			int connectivity )
	{
		final ImagePlus inputIP = new ImagePlus( "input", input );		
		final ImagePlus binaryMaskIP = ( null != mask ) ? new ImagePlus( "binary mask", mask ) : null;
		WatershedTransform3D wt = new WatershedTransform3D( inputIP, binaryMaskIP, connectivity );
		wt.setVerbose( verbose );
		
		final ImagePlus ws = wt.apply();
		if( null != ws )
			return ws.getImageStack();
		else 
			return null;
	}
	/**
	 * Compute fast watershed using flooding simulations, as described by 
	 * Soille, Pierre, and Luc M. Vincent. "Determining watersheds in 
	 * digital pictures via flooding simulations." Lausanne-DL tentative. 
	 * International Society for Optics and Photonics, 1990.
	 * Note: it outputs step messages in the Log window.
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param mask binary mask to restrict the regions of interest
	 * @param connectivity voxel connectivity to define neighborhoods
	 * @return image of labeled catchment basins with dams (labels are 1, 2, ...)
	 */
	public static ImageStack computeWatershed( 
			ImageStack input,
			ImageStack mask,
			int connectivity )
	{
		return Watershed.computeWatershed(input, mask, true, connectivity);
	}
	
	/**
	 * Compute fast watershed using flooding simulations, as described by 
	 * Soille, Pierre, and Luc M. Vincent. "Determining watersheds in 
	 * digital pictures via flooding simulations." Lausanne-DL tentative. 
	 * International Society for Optics and Photonics, 1990.
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param mask binary mask to restrict the regions of interest
	 * @param verbose flag to output step messages in Log window
	 * @param connectivity pixel connectivity to define neighborhoods (4 or 8)
	 * @return image of labeled catchment basins with dams (labels are 1, 2, ...)
	 */
	public static ImageProcessor computeWatershed( 
			ImageProcessor input,
			ImageProcessor mask,
			boolean verbose,
			int connectivity )
	{		
		WatershedTransform2D wt = new WatershedTransform2D( input, mask, connectivity );
		wt.setVerbose( verbose );
		return wt.apply();
	}
	/**
	 * Compute fast watershed using flooding simulations, as described by 
	 * Soille, Pierre, and Luc M. Vincent. "Determining watersheds in 
	 * digital pictures via flooding simulations." Lausanne-DL tentative. 
	 * International Society for Optics and Photonics, 1990.
	 * Note: it outputs step messages in Log window.
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param mask binary mask to restrict the regions of interest
	 * @param connectivity pixel connectivity to define neighborhoods (4 or 8)
	 * @return image of labeled catchment basins with dams (labels are 1, 2, ...)
	 */
	public static ImageProcessor computeWatershed( 
			ImageProcessor input,
			ImageProcessor mask,
			int connectivity )
	{
		return Watershed.computeWatershed(input, mask, true, connectivity);
	}

	/**
	 * Compute watershed with markers with an optional binary mask
	 * to restrict the regions of application.
	 * Note: it outputs step messages in Log window.
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param marker image with labeled markers
	 * @param binaryMask binary mask to restrict the regions of interest
	 * @param connectivity voxel connectivity to define neighborhoods (4 or 8 for 2D, 6 or 26 for 3D)
	 * @param getDams select/deselect the calculation of dams
	 * @return image of labeled catchment basins (labels are 1, 2, ...)
	 */
	public static ImagePlus computeWatershed( 
			ImagePlus input,
			ImagePlus marker,
			ImagePlus binaryMask,
			int connectivity,
			boolean getDams )
	{
		return computeWatershed( input, marker, binaryMask, connectivity,
				getDams, true );
	}
	/**
	 * Compute watershed with markers with an optional binary mask
	 * to restrict the regions of application.
	 *
	 * @param input original grayscale image (usually a gradient image)
	 * @param marker image with labeled markers
	 * @param binaryMask binary mask to restrict the regions of interest
	 * @param connectivity voxel connectivity to define neighborhoods (4 or 8 for 2D, 6 or 26 for 3D)
	 * @param getDams select/deselect the calculation of dams
	 * @param verbose flag to display messages in the log window
	 * @return image of labeled catchment basins (labels are 1, 2, ...)
	 */
	public static ImagePlus computeWatershed(
			ImagePlus input,
			ImagePlus marker,
			ImagePlus binaryMask,
			int connectivity,
			boolean getDams,
			boolean verbose )
	{
		if( connectivity == 6 || connectivity == 26 )
		{
			MarkerControlledWatershedTransform3D wt =
					new MarkerControlledWatershedTransform3D( input, marker,
							binaryMask, connectivity );
			wt.setVerbose( verbose );
			if( getDams )
				return wt.applyWithPriorityQueueAndDams();
			else 
				return wt.applyWithPriorityQueue();
		}
		else if( connectivity == 4 || connectivity == 8 )
		{
			MarkerControlledWatershedTransform2D wt =
					new MarkerControlledWatershedTransform2D(
							input.getProcessor(), marker.getProcessor(),
							null != binaryMask ? binaryMask.getProcessor() :
								null, connectivity );
			wt.setVerbose( verbose );
			ImageProcessor ip;
			if( getDams )
				ip = wt.applyWithPriorityQueueAndDams();
			else 
				ip = wt.applyWithPriorityQueue();

			if( null != ip )
			{
				String title = input.getTitle();
				String ext = "";
				int index = title.lastIndexOf( "." );
				if( index != -1 )
				{
					ext = title.substring( index );
					title = title.substring( 0, index );				
				}

				final ImagePlus ws = new ImagePlus( title + "-watershed" + ext, ip );
				ws.setCalibration( input.getCalibration() );
				return ws;
			}
			else
				return null;
		}
		else
			return null;
	}
	/**
	 * Compute watershed with markers with an optional binary mask
	 * to restrict the regions of application. If the compactness
	 * constraint is larger than 0, the Compact Watershed algorithm
	 * will be executed (Peer Neubert and Peter Protzel. "Compact
	 * Watershed and Preemptive SLIC: On improving trade-offs of
	 * superpixel segmentation algorithms." 22nd international
	 * conference on pattern recognition. IEEE, 2014).
	 *
	 * @param input original grayscale image (usually a gradient image)
	 * @param marker image with labeled markers
	 * @param binaryMask binary mask to restrict the regions of interest
	 * @param connectivity voxel connectivity to define neighborhoods (4 or 8 for 2D, 6 or 26 for 3D)
	 * @param getDams select/deselect the calculation of dams
	 * @param compactness compactness constrain parameter (values larger than 0 involve using compact watershed)
	 * @param verbose flag to display messages in the log window
	 * @return image of labeled catchment basins (labels are 1, 2, ...)
	 */
	public static ImagePlus computeWatershed(
			ImagePlus input,
			ImagePlus marker,
			ImagePlus binaryMask,
			int connectivity,
			boolean getDams,
			double compactness,
			boolean verbose )
	{
		if( connectivity == 6 || connectivity == 26 )
		{
			MarkerControlledWatershedTransform3D wt =
					new MarkerControlledWatershedTransform3D( input, marker,
							binaryMask, connectivity );
			wt.setVerbose( verbose );
			if( getDams )
				return wt.applyWithPriorityQueueAndDams();
			else
				return wt.applyWithPriorityQueue();
		}
		else if( connectivity == 4 || connectivity == 8 )
		{
			MarkerControlledWatershedTransform2D wt =
					new MarkerControlledWatershedTransform2D(
							input.getProcessor(), marker.getProcessor(),
							null != binaryMask ? binaryMask.getProcessor() :
								null, connectivity, compactness );
			wt.setVerbose( verbose );
			ImageProcessor ip;

			if( getDams )
				ip = wt.applyWithPriorityQueueAndDams();
			else
				ip = wt.applyWithPriorityQueue();

			if( null != ip )
			{
				String title = input.getTitle();
				String ext = "";
				int index = title.lastIndexOf( "." );
				if( index != -1 )
				{
					ext = title.substring( index );
					title = title.substring( 0, index );
				}

				final ImagePlus ws = new ImagePlus( title + "-watershed" + ext, ip );
				ws.setCalibration( input.getCalibration() );
				return ws;
			}
			else
				return null;
		}
		else
			return null;
	}
	/**
	 * Compute watershed with markers with an optional binary mask
	 * to restrict the regions of application.
	 * Note: it outputs step messages in Log window.
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param marker image with labeled markers
	 * @param binaryMask binary mask to restrict the regions of interest
	 * @param connectivity voxel connectivity to define neighborhoods
	 * @param getDams select/deselect the calculation of dams
	 * @return image of labeled catchment basins (labels are 1, 2, ...)
	 */
	public static ImageStack computeWatershed( 
			ImageStack input,
			ImageStack marker,
			ImageStack binaryMask,
			int connectivity,
			boolean getDams )
	{		
		return computeWatershed( input, marker, binaryMask, connectivity,
				getDams, true );
	}
	/**
	 * Compute watershed with markers with an optional binary mask
	 * to restrict the regions of application
	 *
	 * @param input original grayscale image (usually a gradient image)
	 * @param marker image with labeled markers
	 * @param binaryMask binary mask to restrict the regions of interest
	 * @param connectivity voxel connectivity to define neighborhoods
	 * @param getDams select/deselect the calculation of dams
	 * @param verbose flag to display messages in the log window
	 * @return image of labeled catchment basins (labels are 1, 2, ...)
	 */
	public static ImageStack computeWatershed(
			ImageStack input,
			ImageStack marker,
			ImageStack binaryMask,
			int connectivity,
			boolean getDams,
			boolean verbose )
	{
				
		final ImagePlus inputIP = new ImagePlus( "input", input );
		final ImagePlus markerIP = new ImagePlus( "marker", marker );
		final ImagePlus binaryMaskIP = ( null != binaryMask ) ?
				new ImagePlus( "binary mask", binaryMask ) : null;

		ImagePlus ws = computeWatershed( inputIP, markerIP, binaryMaskIP,
				connectivity, getDams, verbose );
		if ( null != ws )
			return ws.getImageStack();
		else 
			return null;
	}
	/**
	 * Compute watershed with markers with an optional binary mask
	 * to restrict the regions of application.
	 * Note: it outputs step messages in Log window.
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param marker image with labeled markers
	 * @param binaryMask binary mask to restrict the regions of interest
	 * @param connectivity voxel connectivity to define neighborhoods
	 * @param getDams select/deselect the calculation of dams
	 * @return image of labeled catchment basins (labels are 1, 2, ...)
	 */
	public static ImageProcessor computeWatershed( 
			ImageProcessor input,
			ImageProcessor marker,
			ImageProcessor binaryMask,
			int connectivity,
			boolean getDams )
	{															
		return computeWatershed( input, marker, binaryMask, connectivity,
				getDams, true );
	}

	/**
	 * Compute watershed with markers with an optional binary mask
	 * to restrict the regions of application.
	 *
	 * @param input original grayscale image (usually a gradient image)
	 * @param marker image with labeled markers
	 * @param binaryMask binary mask to restrict the regions of interest
	 * @param connectivity voxel connectivity to define neighborhoods
	 * @param getDams select/deselect the calculation of dams
	 * @param verbose flag to display log messages
	 * @return image of labeled catchment basins (labels are 1, 2, ...)
	 */
	public static ImageProcessor computeWatershed(
			ImageProcessor input,
			ImageProcessor marker,
			ImageProcessor binaryMask,
			int connectivity,
			boolean getDams,
			boolean verbose )
	{
		MarkerControlledWatershedTransform2D wt =
				new MarkerControlledWatershedTransform2D( input, marker,
						binaryMask, connectivity );
		wt.setVerbose( verbose );
		if( getDams )
			return wt.applyWithPriorityQueueAndDams();
		else 
			return wt.applyWithPriorityQueue();
	}

	/**
	 * Compute watershed with markers
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param marker image with labeled markers
	 * @param connectivity voxel connectivity to define neighborhoods
	 * @param getDams select/deselect the calculation of dams
	 * @param verbose flag to output step messages in Log window.
	 * @return image of labeled catchment basins (labels are 1, 2, ...)
	 */
	public static ImagePlus computeWatershed( 
			ImagePlus input,
			ImagePlus marker,
			int connectivity,
			boolean getDams,
			boolean verbose )
	{
		MarkerControlledWatershedTransform3D wt = new MarkerControlledWatershedTransform3D( input, marker, null, connectivity );
		wt.setVerbose( verbose );
		if( getDams )
			return wt.applyWithPriorityQueueAndDams();
		else 
			return wt.applyWithPriorityQueue();
	}
	/**
	 * Compute watershed with markers.
	 * Note: it outputs step messages in the Log window.
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param marker image with labeled markers
	 * @param connectivity voxel connectivity to define neighborhoods
	 * @param getDams select/deselect the calculation of dams
	 * @return image of labeled catchment basins (labels are 1, 2, ...)
	 */
	public static ImagePlus computeWatershed( 
			ImagePlus input,
			ImagePlus marker,
			int connectivity,
			boolean getDams)
	{
		return Watershed.computeWatershed( input, marker, connectivity, getDams, true );
	}
	
	/**
	 * Compute watershed with markers.
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param marker image with labeled markers
	 * @param connectivity voxel connectivity to define neighborhoods
	 * @param getDams select/deselect the calculation of dams
	 * @param verbose flag to output step message in the Log window
	 * @return image of labeled catchment basins (labels are 1, 2, ...)
	 */
	public static ImageStack computeWatershed( 
			ImageStack input,
			ImageStack marker,
			int connectivity,
			boolean getDams,
			boolean verbose )
	{		
		if ( Thread.currentThread().isInterrupted() )					
			return null;
		
		final ImagePlus inputIP = new ImagePlus( "input", input );
		final ImagePlus markerIP = new ImagePlus( "marker", marker );	
		
		MarkerControlledWatershedTransform3D wt =
				new MarkerControlledWatershedTransform3D( inputIP, markerIP, null, connectivity );
		wt.setVerbose( verbose );

		ImagePlus ws = null;

		if( getDams )			
			ws = wt.applyWithPriorityQueueAndDams();							
		else			
			ws = wt.applyWithPriorityQueue();			
		
		if( null == ws )
			return null;
		return ws.getImageStack();
	}
	/**
	 * Compute watershed with markers.
	 * Note: it outputs step messages in the Low window.
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param marker image with labeled markers
	 * @param connectivity voxel connectivity to define neighborhoods
	 * @param getDams select/deselect the calculation of dams
	 * @return image of labeled catchment basins (labels are 1, 2, ...)
	 */
	public static ImageStack computeWatershed( 
			ImageStack input,
			ImageStack marker,
			int connectivity,
			boolean getDams )
	{
		return Watershed.computeWatershed( input, marker, connectivity, getDams, true );
	}
	/**
	 * Compute watershed with markers.
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param marker image with labeled markers
	 * @param connectivity voxel connectivity to define neighborhoods
	 * @param getDams select/deselect the calculation of dams
	 * @param verbose flag to output step messages in the Low window
	 * @return image of labeled catchment basins (labels are 1, 2, ...)
	 */
	public static ImageProcessor computeWatershed( 
			ImageProcessor input,
			ImageProcessor marker,
			int connectivity,
			boolean getDams,
			boolean verbose )
	{												
		MarkerControlledWatershedTransform2D wt = 
				new MarkerControlledWatershedTransform2D( input, marker, 
														  null, connectivity );
		wt.setVerbose( verbose );

		if( getDams )			
			return wt.applyWithPriorityQueueAndDams();							
		else			
			return wt.applyWithPriorityQueue();			
	}
	/**
	 * Compute watershed with markers.
	 * Note: it outputs step message in the Low window.
	 * 
	 * @param input original grayscale image (usually a gradient image)
	 * @param marker image with labeled markers
	 * @param connectivity voxel connectivity to define neighborhoods
	 * @param getDams select/deselect the calculation of dams
	 * @return image of labeled catchment basins (labels are 1, 2, ...)
	 */
	public static ImageProcessor computeWatershed( 
			ImageProcessor input,
			ImageProcessor marker,
			int connectivity,
			boolean getDams )
	{
		return Watershed.computeWatershed(input, marker, connectivity, getDams, true );
	}
}
