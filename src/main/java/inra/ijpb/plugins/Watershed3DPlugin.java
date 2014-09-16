package inra.ijpb.plugins;


import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import inra.ijpb.data.image.Images3D;
import inra.ijpb.watershed.Watershed;


/**
 * A plugin to perform watershed on a 3D image using flooding simulations, 
 * as described by Soille, Pierre, and Luc M. Vincent. "Determining watersheds 
 * in digital pictures via flooding simulations." Lausanne-DL tentative. 
 * International Society for Optics and Photonics, 1990.
 *
 * @author Ignacio Arganda-Carreras
 */
public class Watershed3DPlugin implements PlugIn 
{

	/** flag to use 26-connectivity */
	public static boolean use26neighbors = true;
	
	public static double hMin = 0;
	public static double hMax = 255;
	
		
	/**
	 * Apply 3D watershed to a 2D or 3D image (it does work for 2D images too).
	 *	 
	 * @param input the 2D or 3D image (in principle a "gradient" image)
	 * @param mask binary mask to restrict region of interest
	 * @param connectivity 6 or 26 voxel connectivity
	 * @param hMin minimum grayscale level height
	 * @param hMax maximum grayscale level height
	 * @return labeled catchment basins image
	 */
	public ImagePlus process(
			ImagePlus input, 
			ImagePlus mask,
			int connectivity,
			double hMin,
			double hMax ) 
	{
		final long start = System.currentTimeMillis();
				
		ImagePlus resultImage = Watershed.computeWatershed( input, mask, connectivity, hMin, hMax );
					
		final long end = System.currentTimeMillis();
		IJ.log( "Watershed 3d took " + (end-start) + " ms.");
						
		return resultImage;
			
	}
	
	/**
	 * Plugin run method, to be called from ImageJ.
	 */
	@Override
	public void run(String arg) 
	{
		int nbima = WindowManager.getImageCount();
		
		if( nbima == 0 )
		{
			IJ.error( "Classic Watershed", 
					"ERROR: At least one image needs to be open to run watershed.");
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
        
        GenericDialog gd = new GenericDialog("Classic Watershed");

        int inputIndex = 0;
        int maskIndex = nbima > 1 ? 2 : 0;
          
        // guess maximum height from image type
        switch( WindowManager.getImage( inputIndex + 1 ).getBitDepth() ){
        case 8:
        	if( hMax > 255 )
        		hMax = 255;
        	break;
        case 16:
        	if( hMax > 65535 )
        		hMax = 65535;
        	break;
        case 32:
        	if( hMax > Float.MAX_VALUE )
        		hMax = Float.MAX_VALUE;
        	break;       
        }
        
        
        gd.addChoice( "Input", names, names[ inputIndex ] );
        gd.addChoice( "Mask", namesMask, namesMask[ maskIndex ] );
        gd.addCheckbox( "Use diagonal connectivity", use26neighbors );
        gd.addNumericField( "Min h", hMin, 1 );
        gd.addNumericField( "Max h", hMax, 1 );
        
        gd.showDialog();
        
        if (gd.wasOKed()) 
        {
            inputIndex = gd.getNextChoiceIndex();
            maskIndex = gd.getNextChoiceIndex();
            use26neighbors = gd.getNextBoolean();
            hMin = gd.getNextNumber();
            hMax = gd.getNextNumber();          

            ImagePlus inputImage = WindowManager.getImage( inputIndex + 1 );
            ImagePlus maskImage = maskIndex > 0 ? WindowManager.getImage( maskIndex ) : null;
            
            // check minimum and maximum heights
            if( hMin < 0 )
            	hMin = 0;
            
            switch( WindowManager.getImage( inputIndex + 1 ).getBitDepth() ){
            case 8:
            	if( hMax > 255 )
            		hMax = 255;
            	break;
            case 16:
            	if( hMax > 65535 )
            		hMax = 65535;
            	break;
            case 32:
            	if( hMax > Float.MAX_VALUE )
            		hMax = Float.MAX_VALUE;
            	break;    
            default:
            	IJ.error("Classic Watershed", "Error: only grayscale images are valid input!");
            	return;
            }
            
            
            int connectivity = use26neighbors ? 26 : 6;
            
            // check if input image is 2d
            if( inputImage.getImageStackSize() == 1 )
            	connectivity = use26neighbors ? 8 : 4;
            
            ImagePlus result = process( inputImage, maskImage, connectivity, hMin, hMax );
            
            // Adjust range to visualize result
    		Images3D.optimizeDisplayRange( result );
            
    		// Set result slice to the current slice in the input image
            result.setSlice( inputImage.getCurrentSlice() );
            
            // show result
            result.show();
        }
		
	}

}
