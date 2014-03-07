package inra.ijpb.data.image;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

/**
 * A collection of static methods for working on 3D images. 
 * @author David Legland
 *
 */
public class Images3D {

	/**
	 * Converts the input ImageStack into an instance of Image3D, depending
	 * on the data type stored in the stack.
	 */
	public final static Image3D createWrapper(ImageStack stack) {
		switch(stack.getBitDepth()) {
		case 8:
			return new ByteStackWrapper(stack);
		case 16:
			return new ShortStackWrapper(stack);
		case 32:
			return new FloatStackWrapper(stack);
		default:
			throw new IllegalArgumentException(
					"Can not manage image stacks with bit depth "
							+ stack.getBitDepth());
		}
	}
	
	/**
	 * Find minimum and maximum value of input image
	 * 
	 * @param image input 2d/3d image
	 * @return array of 2 extreme values
	 */
	public static double[] findMinAndMax( ImagePlus image )
	{
		// Adjust min and max values to display
		double min = 0;
		double max = 0;
		for( int slice=1; slice<=image.getImageStackSize(); slice++ )			
		{
			ImageProcessor ip = image.getImageStack().getProcessor(slice);
			ip.resetMinAndMax();
			if( max < ip.getMax() )
				max = ip.getMax();
			if( min > ip.getMin() )
				min = ip.getMin();
		}
		return new double[]{ min, max };				
	}
	
	/**
	 * Optimize display range of 2d/3d image based on its
	 * minimum and maximum values
	 * 
	 * @param image input image
	 */
	public static void optimizeDisplayRange( ImagePlus image )
	{
		double[] extremeValue = findMinAndMax(image);
		image.setDisplayRange( extremeValue[ 0 ], extremeValue[ 1 ] );
		image.updateAndDraw();
	}
	
}
