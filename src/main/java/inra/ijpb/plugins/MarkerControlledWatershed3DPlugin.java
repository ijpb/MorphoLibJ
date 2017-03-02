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
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.data.image.Images3D;
import inra.ijpb.watershed.Watershed;


/**
 * 
 * A plugin to perform marker-controlled watershed on a 2D or 3D image.
 * 
 * Reference: Fernand Meyer and Serge Beucher. "Morphological segmentation." 
 * Journal of visual communication and image representation 1.1 (1990): 21-46.
 *
 * @author Ignacio Arganda-Carreras
 */
public class MarkerControlledWatershed3DPlugin implements PlugIn 
{
	/** flag set to TRUE if markers are binary, to FALSE if markers are labels */
	public static boolean binaryMarkers = true;
	
	/** flag to calculate watershed dams */
	public static boolean getDams = true;
	
	/** flag to use 26-connectivity */
	public static boolean use26neighbors = true;
		
	/**
	 * Apply marker-controlled watershed to a grayscale 2D or 3D image.
	 *	 
	 * @param input grayscale 2D or 3D image (in principle a "gradient" image)
	 * @param marker the labeled marker image
	 * @param mask binary mask to restrict region of interest
	 * @param connectivity 6 or 26 voxel connectivity
	 * @return the resulting watershed
	 */
	public ImagePlus process(
			ImagePlus input, 
			ImagePlus marker,
			ImagePlus mask,
			int connectivity ) 
	{
		final long start = System.currentTimeMillis();
		
		if (binaryMarkers)
		{
			IJ.log("-> Compute marker labels");
			marker = BinaryImages.componentsLabeling(marker, connectivity, 32);
		}
		
		IJ.log("-> Running watershed...");
								
		ImagePlus resultImage = Watershed.computeWatershed(input, marker, mask, connectivity, getDams );				
		
		final long end = System.currentTimeMillis();
		IJ.log( "Watershed 3d took " + (end-start) + " ms.");		
						
		return resultImage;				
	}
	

	/**
	 * Plugin run method to be called from ImageJ
	 */
	@Override
	public void run(String arg) 
	{
		int nbima = WindowManager.getImageCount();
		
		if( nbima < 2 )
		{
			IJ.error( "Marker-controlled Watershed", 
					"ERROR: At least two images need to be open to run Marker-controlled Watershed.");
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
        
        GenericDialog gd = new GenericDialog("Marker-controlled Watershed");

        int inputIndex = 0;
        int markerIndex = nbima > 1 ? 1 : 0;
        
        gd.addChoice( "Input", names, names[ inputIndex ] );
        gd.addChoice( "Marker", names, names[ markerIndex ] );
        gd.addChoice( "Mask", namesMask, namesMask[ 0 ] );
        gd.addCheckbox("Binary markers", true);
        gd.addCheckbox( "Calculate dams", getDams );
        gd.addCheckbox( "Use diagonal connectivity", use26neighbors );

        gd.showDialog();
        
        if (gd.wasOKed()) 
        {
            inputIndex = gd.getNextChoiceIndex();
            markerIndex = gd.getNextChoiceIndex();
            int maskIndex = gd.getNextChoiceIndex();
            binaryMarkers = gd.getNextBoolean();
            getDams = gd.getNextBoolean();
            use26neighbors = gd.getNextBoolean();

            ImagePlus inputImage = WindowManager.getImage( inputIndex + 1 );
            ImagePlus markerImage = WindowManager.getImage( markerIndex + 1 );
            ImagePlus maskImage = maskIndex > 0 ? WindowManager.getImage( maskIndex ) : null;
            
            // a 3D image is assumed but it will use 2D connectivity if the
            // input is 2D
            int connectivity = use26neighbors ? 26 : 6;
            if( inputImage.getImageStackSize() == 1 )
            	connectivity = use26neighbors ? 8 : 4;

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

