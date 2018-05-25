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
import inra.ijpb.measure.IntensityMeasures;
import inra.ijpb.measure.ResultsBuilder;

/**
 * This class implements a set of photometric (intensity) measurements over
 * an input grayscale image (2D or 3D) and its set of corresponding labels.
 *
 * @author Ignacio Arganda-Carreras (ignacio.arganda@ehu.eus)
 *
 */
public class IntensityMeasures3D implements PlugIn{

	static int inputIndex = 0;
	static int labelsIndex = 1;
	static String[] measureLabels = new String[]{ "Mean", "StdDev", "Max",
			"Min", "Median", "Mode", "Skewness", "Kurtosis",
			"NumberOfVoxels", "Volume", "NeighborsMean", "NeighborsStdDev",
			"NeighborsMax", "NeighborsMin", "NeighborsMedian",
			"NeighborsMode", "NeighborsSkewness", "NeighborsKurtosis" };

	static boolean[] measureStates = new boolean[]{ true, true, true, true,
			true, true, true, true, true, true, true, true, true, true, true,
			true, true, true };

	@Override
	public void run(String arg) 
	{		
		int nbima = WindowManager.getImageCount();
		
		if( nbima < 2 )
		{
			IJ.error( "Intensity Measures 2D/3D input error",
					"ERROR: At least two images need to be open to run "
							+ "Intensity Measures 2D/3D" );
			return;
		}

        String[] names = new String[ nbima ];        
        
        for (int i = 0; i < nbima; i++)         
            names[ i ] = WindowManager.getImage(i + 1).getShortTitle();
        
        if( inputIndex > nbima-1 )
        	inputIndex = nbima - 1;
        if( labelsIndex > nbima-1 )
        	labelsIndex = nbima - 1;
        
        GenericDialog gd = new GenericDialog( "Intensity Measurements 2D/3D" );
        gd.addChoice( "Input", names, names[ inputIndex ] );
        gd.addChoice( "Labels", names, names[ labelsIndex ] );
        gd.addMessage("Measurements:");
        gd.addCheckboxGroup( measureLabels.length / 2 + 1, 2, measureLabels,
        		measureStates );
        
        gd.showDialog();
        
        if( gd.wasOKed() )
        {
        	inputIndex = gd.getNextChoiceIndex();
        	labelsIndex = gd.getNextChoiceIndex();
           
        	for( int i=0; i<measureStates.length; i++ )        	
        		measureStates[ i ] = gd.getNextBoolean();
        	        		                        
            boolean calculateMeasures = false;
            for( int i=0; i<6; i++ )
            	if( measureStates[ i ] )
            		calculateMeasures = true;
            
            if ( calculateMeasures == false )
            	return;
                        
            ImagePlus inputImage = WindowManager.getImage( inputIndex + 1 );
            ImagePlus labelImage = WindowManager.getImage( labelsIndex + 1 );
            
            if( inputImage.getWidth() != labelImage.getWidth() || 
            		inputImage.getHeight() != labelImage.getHeight() )
            {
            	IJ.error( "Intensity Measures 2D/3D input error", "Error: input"
            			+ " and label images must have the same size" );
            	return;
            }

            ResultsBuilder rb = new ResultsBuilder();
            
            final IntensityMeasures im = new IntensityMeasures( inputImage, labelImage );

            if( measureStates[ 0 ] ) // Mean            	
            	rb.addResult( im.getMean() );

            if( measureStates[ 1 ] ) // Standard deviation			            	
            	rb.addResult( im.getStdDev() );

            if( measureStates[ 2 ] ) // Max         	
            	rb.addResult( im.getMax() );

            if( measureStates[ 3 ] ) // Min            	
            	rb.addResult( im.getMin() );

            if( measureStates[ 4 ] ) // Median
            	rb.addResult( im.getMedian() );

            if( measureStates[ 5 ] ) // Mode
            	rb.addResult( im.getMode() );

            if( measureStates[ 6 ] ) // Skewness
            	rb.addResult( im.getSkewness() );

            if( measureStates[ 7 ] ) // Kurtosis
            	rb.addResult( im.getKurtosis() );

            if( measureStates[ 8 ] ) // Number of voxels
            	rb.addResult( im.getNumberOfVoxels() );

            if( measureStates[ 9 ] ) // Volume
            	rb.addResult( im.getVolume() );

            if( measureStates[ 10 ] ) // Neighbors mean intensity
            	rb.addResult( im.getNeighborsMean() );

            if( measureStates[ 11 ] ) // Neighbors standard deviation intensity
            	rb.addResult( im.getNeighborsStdDev() );

            if( measureStates[ 12 ] ) // Neighbors maximum intensity
            	rb.addResult( im.getNeighborsMax() );

            if( measureStates[ 13 ] ) // Neighbors minimum intensity
            	rb.addResult( im.getNeighborsMin() );

            if( measureStates[ 14 ] ) // Neighbors median intensity
            	rb.addResult( im.getNeighborsMedian() );

            if( measureStates[ 15 ] ) // Neighbors mode intensity
            	rb.addResult( im.getNeighborsMode() );

            if( measureStates[ 16 ] ) // Neighbors skewness intensity
            	rb.addResult( im.getNeighborsSkewness() );

            if( measureStates[ 17 ] ) // Neighbors kurtosis intensity
            	rb.addResult( im.getNeighborsKurtosis() );

            rb.getResultsTable().show( inputImage.getShortTitle() +
            		"-intensity-measurements" );
        }
	}
}
