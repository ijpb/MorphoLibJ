package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import inra.ijpb.data.image.Images3D;
import inra.ijpb.watershed.Watershed;


/**
 * 
 * A plugin to perform marker-controlled watershed on a 3D image.
 *
 * @author Ignacio Arganda-Carreras
 */
public class MarkerControlledWatershed3DPlugin implements PlugIn 
{
	/** flag to use a priority queue */
	public static boolean usePriorityQueue = true;
	/** flag to calculate watershed dams */
	public static boolean getDams = true;
	/** flag to use 26-connectivity */
	public static boolean use26neighbors = true;
		
	/**
	 * Apply marker controlled watershed to a 2D or 3D image (it does work for 2D images too).
	 *	 
	 * @param input the 2D or 3D image (in principle a "gradient" image)
	 * @param marker the labeled marker image
	 * @param mask binary mask to restrict region of interest
	 * @param connectivity 6 or 26 voxel connectivity
	 */
	public ImagePlus process(
			ImagePlus input, 
			ImagePlus marker,
			ImagePlus mask,
			int connectivity ) 
	{
		final long start = System.currentTimeMillis();
						
		IJ.log("-> Running watershed...");
								
		ImagePlus resultImage = Watershed.computeWatershed(input, marker, mask, connectivity, usePriorityQueue, getDams );				
		
		final long end = System.currentTimeMillis();
		IJ.log( "Watershed 3d took " + (end-start) + " ms.");		
						
		return resultImage;				
	}
	

	@Override
	public void run(String arg) 
	{
		int nbima = WindowManager.getImageCount();
		
		if( nbima == 0 )
		{
			IJ.error( "Marker-controlled watershed 3D", 
					"ERROR: At least one image needs to be open to run watershed in 3D");
			return;
		}
		
        String[] names = new String[ nbima ];
        String[] namesMask = new String[ nbima + 1 ];

        namesMask[ 0 ] = "None";
        
        for (int i = 0; i < nbima; i++) 
        {
            names[ i ] = WindowManager.getImage(i + 1).getShortTitle();
            namesMask[ i + 1 ] = WindowManager.getImage(i + 1).getShortTitle();
        }
        
        GenericDialog gd = new GenericDialog("Marker-controlled watershed 3D");

        int inputIndex = 0;
        int markerIndex = nbima > 1 ? 1 : 0;
        
        gd.addChoice( "Input", names, names[ inputIndex ] );
        gd.addChoice( "Marker", names, names[ markerIndex ] );
        gd.addChoice( "Mask", namesMask, namesMask[ nbima > 2 ? 3 : 0 ] );
        gd.addCheckbox( "Use priority queue", usePriorityQueue );
        gd.addCheckbox( "Calculate dams", getDams );
        gd.addCheckbox( "Use diagonal connectivity", use26neighbors );

        gd.showDialog();
        
        if (gd.wasOKed()) 
        {
            inputIndex = gd.getNextChoiceIndex();
            markerIndex = gd.getNextChoiceIndex();
            int maskIndex = gd.getNextChoiceIndex();
            usePriorityQueue = gd.getNextBoolean();
            getDams = gd.getNextBoolean();
            use26neighbors = gd.getNextBoolean();

            ImagePlus inputImage = WindowManager.getImage( inputIndex + 1 );
            ImagePlus markerImage = WindowManager.getImage( markerIndex + 1 );
            ImagePlus maskImage = maskIndex > 0 ? WindowManager.getImage( maskIndex ) : null;
            
            final int connectivity = use26neighbors ? 26 : 6;
            
            ImagePlus result = process( inputImage, markerImage, maskImage, connectivity );
                                    
    		// Set result slice to the current slice in the input image
            result.setSlice( inputImage.getCurrentSlice() );
            
            // optimize display range
            Images3D.optimizeDisplayRange( result );
            
            // show result
            result.show();
        }
		
	}

}

