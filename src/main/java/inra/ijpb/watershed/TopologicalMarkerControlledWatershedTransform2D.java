package inra.ijpb.watershed;

import java.util.PriorityQueue;

import ij.process.ImageProcessor;
import inra.ijpb.data.PixelRecord;

public class TopologicalMarkerControlledWatershedTransform2D 
	extends MarkerControlledWatershedTransform2D{

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

}
