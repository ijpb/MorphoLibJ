package inra.ijpb.plugins;


import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import inra.ijpb.binary.ConnectedComponents;
import inra.ijpb.morphology.MinimaAndMaxima3D;
import inra.ijpb.watershed.WatershedTransform3D;


/**
 * Watershed 3D
 *
 * A plugin to perform marker-controlled watershed on a 3D image.
 *
 * @author Ignacio Arganda-Carreras
 */
public class Watershed3DPlugin implements PlugIn 
{
	/** flag to use a priority queue */
	public static boolean usePriorityQueue = true;
	/** flag to use 26-connectivity */
	public static boolean use26neighbors = true;
	
	/**
	 * Apply 3D watershed to a 2D or 3D image (it does work for 2D images too).
	 *
	 * @param input the 2D or 3D image (in principle a "gradient" image)
	 * @param seed the image to calculate the seeds from (it can be the same as the input or another one)
	 */
	public ImagePlus process(
			ImagePlus input, 
			ImagePlus seed ) 
	{
		final long start = System.currentTimeMillis();
		
		IJ.log("-> Running regional minima filter...");
		
		ImageStack regionalMinima = MinimaAndMaxima3D.regionalMinima( seed.getImageStack(), 26 );
		
		//regionalMinima.show();
		
		final long step1 = System.currentTimeMillis();
		
		IJ.log( "Regional minima took " + (step1-start) + " ms.");
		
		IJ.log("-> Running connected components...");
		
		ImageStack components = ConnectedComponents.computeLabels( regionalMinima, 26, 32 );				
		
		final long step2 = System.currentTimeMillis();
		IJ.log( "Connected components took " + (step2-step1) + " ms.");
		
		IJ.log("-> Running watershed...");
		
		ImagePlus connectedMinima = new ImagePlus("connected minima", components );
		//connectedMinima.show();
		
		WatershedTransform3D wt = new WatershedTransform3D(input, connectedMinima, null);
		ImagePlus resultImage = usePriorityQueue == false ? wt.apply() : wt.applyWithPriorityQueue();
		
		final long end = System.currentTimeMillis();
		IJ.log( "Watershed 3d took " + (end-step2) + " ms.");
		IJ.log( "Whole plugin took " + (end-start) + " ms.");
		
		return resultImage;
				
	}
	
	/**
	 * Apply 3D watershed to a 2D or 3D image (it does work for 2D images too).
	 *	 
	 * @param input the 2D or 3D image (in principle a "gradient" image)
	 * @param seed the image to calculate the seeds from (it can be the same as the input or another one)
	 * @param mask binary mask to restrict region of interest
	 */
	public ImagePlus process(
			ImagePlus input, 
			ImagePlus seed,
			ImagePlus mask ) 
	{
		final long start = System.currentTimeMillis();
		
		IJ.log("-> Running regional minima filter...");
		
		ImageStack regionalMinima = null != mask  ? 
				MinimaAndMaxima3D.regionalMinima( seed.getImageStack(), 26, mask.getImageStack() ) :
					MinimaAndMaxima3D.regionalMinima( seed.getImageStack(), 26 )	;
		
		//regionalMinima.show();
		
		final long step1 = System.currentTimeMillis();
		
		IJ.log( "Regional minima took " + (step1-start) + " ms.");
		
		IJ.log("-> Running connected components...");
		
		ImageStack components = ConnectedComponents.computeLabels( regionalMinima, 26, 32 );				
		
		final long step2 = System.currentTimeMillis();
		IJ.log( "Connected components took " + (step2-step1) + " ms.");
		
		IJ.log("-> Running watershed...");
		
		ImagePlus connectedMinima = new ImagePlus("connected minima", components );
		//connectedMinima.show();
		
		WatershedTransform3D wt = new WatershedTransform3D( input, connectedMinima, mask );
		ImagePlus resultImage = usePriorityQueue == false ? wt.apply() : wt.applyWithPriorityQueue();
		
		final long end = System.currentTimeMillis();
		IJ.log( "Watershed 3d took " + (end-step2) + " ms.");
		IJ.log( "Whole plugin took " + (end-start) + " ms.");
						
		return resultImage;
				
	}
	
	/**
	 * Apply 3D watershed to a 2D or 3D image (it does work for 2D images too).
	 *	 
	 * @param input the 2D or 3D image (in principle a "gradient" image)
	 * @param seed the image to calculate the seeds from (it can be the same as the input or another one)
	 * @param mask binary mask to restrict region of interest
	 * @param connectivity 6 or 26 voxel connectivity
	 */
	public ImagePlus process(
			ImagePlus input, 
			ImagePlus seed,
			ImagePlus mask,
			int connectivity ) 
	{
		final long start = System.currentTimeMillis();
		
		IJ.log("-> Running regional minima filter...");
		
		ImageStack regionalMinima = null != mask  ? 
				MinimaAndMaxima3D.regionalMinima( seed.getImageStack(), connectivity, mask.getImageStack() ) :
					MinimaAndMaxima3D.regionalMinima( seed.getImageStack(), connectivity )	;
		
		//(new ImagePlus( "Regional minima", regionalMinima)).show();
		
		final long step1 = System.currentTimeMillis();
		
		IJ.log( "Regional minima took " + (step1-start) + " ms.");
		
		IJ.log("-> Running connected components...");
		
		ImageStack components = ConnectedComponents.computeLabels( regionalMinima, connectivity, 32 );				
		
		final long step2 = System.currentTimeMillis();
		IJ.log( "Connected components took " + (step2-step1) + " ms.");
		
		IJ.log("-> Running watershed...");
		
		ImagePlus connectedMinima = new ImagePlus("connected minima", components );
		//connectedMinima.show();
		
		WatershedTransform3D wt = new WatershedTransform3D( input, connectedMinima, mask, connectivity );
		ImagePlus resultImage = usePriorityQueue == false ? wt.apply() : wt.applyWithPriorityQueue();
		
		final long end = System.currentTimeMillis();
		IJ.log( "Watershed 3d took " + (end-step2) + " ms.");
		IJ.log( "Whole plugin took " + (end-start) + " ms.");
						
		return resultImage;
				
	}
	

	@Override
	public void run(String arg) 
	{
		int nbima = WindowManager.getImageCount();
		
		if( nbima == 0 )
		{
			IJ.error( "Watershed 3D", 
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
        
        GenericDialog gd = new GenericDialog("Watershed 3D");

        int spot = 0;
        int seed = nbima > 1 ? 1 : 0;
        
        gd.addChoice( "Input image", names, names[spot] );
        gd.addChoice( "Image to seed from", names, names[seed] );
        gd.addChoice( "Mask", namesMask, namesMask[ nbima > 2 ? 3 : 0 ] );
        gd.addCheckbox( "Use priority queue", usePriorityQueue );
        gd.addCheckbox( "Use diagonal connectivity", use26neighbors );

        gd.showDialog();
        
        if (gd.wasOKed()) 
        {
            spot = gd.getNextChoiceIndex();
            seed = gd.getNextChoiceIndex();
            int maskIndex = gd.getNextChoiceIndex();
            usePriorityQueue = gd.getNextBoolean();
            use26neighbors = gd.getNextBoolean();

            ImagePlus inputImage = WindowManager.getImage(spot + 1);
            ImagePlus seedImage = WindowManager.getImage(seed + 1);
            ImagePlus maskImage = maskIndex > 0 ? WindowManager.getImage( maskIndex ) : null;
            
            final int connectivity = use26neighbors ? 26 : 6;
            
            ImagePlus result = process( inputImage, seedImage, maskImage, connectivity );
            
            // Adjust range to visualize result
            //if( result.getImageStackSize() > 1 )
            //	result.setSlice( result.getImageStackSize()/2 );
            ImageProcessor ip = result.getProcessor();
            ip.resetMinAndMax();
            result.setDisplayRange(ip.getMin(),ip.getMax());
            
    		// Set result slice to the current slice in the input image
            result.setSlice( inputImage.getCurrentSlice() );
            
            // show result
            result.show();
        }
		
	}

}
