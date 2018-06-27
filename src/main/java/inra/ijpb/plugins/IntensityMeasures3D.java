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
            int numMeasures = 0;
            for( int i=0; i<measureStates.length; i++ )
            	if( measureStates[ i ] )
            	{
            		calculateMeasures = true;
            		numMeasures ++;
            	}
            
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
            int calculated = 0;
            if( measureStates[ 0 ] ) // Mean
            {
            	IJ.showStatus( "Calculating mean intensity..." );
            	IJ.showProgress( calculated, numMeasures );
            	rb.addResult( im.getMean() );
            	calculated ++;
            }

            if( measureStates[ 1 ] ) // Standard deviation
            {
            	IJ.showStatus( "Calculating standard deviation of intensity..." );
            	IJ.showProgress( calculated, numMeasures );
            	rb.addResult( im.getStdDev() );
            	calculated ++;
            }

            if( measureStates[ 2 ] ) // Max
            {
            	IJ.showStatus( "Calculating maximum intensity..." );
            	IJ.showProgress( calculated, numMeasures );
            	rb.addResult( im.getMax() );
            	calculated ++;
            }

            if( measureStates[ 3 ] ) // Min
            {
            	IJ.showStatus( "Calculating minimum intensity..." );
            	IJ.showProgress( calculated, numMeasures );
            	rb.addResult( im.getMin() );
            	calculated ++;
            }

            if( measureStates[ 4 ] ) // Median
            {
            	IJ.showStatus( "Calculating median intensity..." );
            	IJ.showProgress( calculated, numMeasures );
            	rb.addResult( im.getMedian() );
            	calculated ++;
            }

            if( measureStates[ 5 ] ) // Mode
            {
            	IJ.showStatus( "Calculating intensity mode..." );
            	IJ.showProgress( calculated, numMeasures );
            	rb.addResult( im.getMode() );
            	calculated ++;
            }

            if( measureStates[ 6 ] ) // Skewness
            {
            	IJ.showStatus( "Calculating intensity skewness..." );
            	IJ.showProgress( calculated, numMeasures );
            	rb.addResult( im.getSkewness() );
            	calculated ++;
            }

            if( measureStates[ 7 ] ) // Kurtosis
            {
            	IJ.showStatus( "Calculating minimum kurtosis..." );
            	IJ.showProgress( calculated, numMeasures );
            	rb.addResult( im.getKurtosis() );
            	calculated ++;
            }

            if( measureStates[ 8 ] ) // Number of voxels
            {
            	IJ.showStatus( "Calculating number of pixels/voxels..." );
            	IJ.showProgress( calculated, numMeasures );
            	rb.addResult( im.getNumberOfVoxels() );
            	calculated ++;
            }

            if( measureStates[ 9 ] ) // Volume
            {
            	IJ.showStatus( "Calculating volume..." );
            	IJ.showProgress( calculated, numMeasures );
            	rb.addResult( im.getVolume() );
            	calculated ++;
            }

            if( measureStates[ 10 ] ) // Neighbors mean intensity
            {
            	IJ.showStatus( "Calculating neighbors mean intensity..." );
            	IJ.showProgress( calculated, numMeasures );
            	rb.addResult( im.getNeighborsMean() );
            	calculated ++;
            }

            if( measureStates[ 11 ] ) // Neighbors standard deviation intensity
            {
            	IJ.showStatus( "Calculating neighbors standard deviation of intensity..." );
            	IJ.showProgress( calculated, numMeasures );
            	rb.addResult( im.getNeighborsStdDev() );
            	calculated ++;
            }

            if( measureStates[ 12 ] ) // Neighbors maximum intensity
            {
            	IJ.showStatus( "Calculating neighbors maximum intensity..." );
            	IJ.showProgress( calculated, numMeasures );
            	rb.addResult( im.getNeighborsMax() );
            	calculated ++;
            }

            if( measureStates[ 13 ] ) // Neighbors minimum intensity
            {
            	IJ.showStatus( "Calculating neighbors minimum intensity..." );
            	IJ.showProgress( calculated, numMeasures );
            	rb.addResult( im.getNeighborsMin() );
            }

            if( measureStates[ 14 ] ) // Neighbors median intensity
            {
            	IJ.showStatus( "Calculating neighbors median intensity..." );
            	IJ.showProgress( calculated, numMeasures );
            	rb.addResult( im.getNeighborsMedian() );
            	calculated ++;
            }

            if( measureStates[ 15 ] ) // Neighbors mode intensity
            {
            	IJ.showStatus( "Calculating neighbors intensity mode..." );
            	IJ.showProgress( calculated, numMeasures );
            	rb.addResult( im.getNeighborsMode() );
            	calculated ++;
            }

            if( measureStates[ 16 ] ) // Neighbors skewness intensity
            {
            	IJ.showStatus( "Calculating neighbors intensity skewness..." );
            	IJ.showProgress( calculated, numMeasures );
            	rb.addResult( im.getNeighborsSkewness() );
            	calculated ++;
            }

            if( measureStates[ 17 ] ) // Neighbors kurtosis intensity
            {
            	IJ.showStatus( "Calculating neighbors intensity kurtosis..." );
            	IJ.showProgress( calculated, numMeasures );
            	rb.addResult( im.getNeighborsKurtosis() );
            	calculated ++;
            }
            IJ.showStatus( "Done" );
            IJ.showProgress( calculated, numMeasures );

            rb.getResultsTable().show( inputImage.getShortTitle() +
            		"-intensity-measurements" );
        }
	}
}
