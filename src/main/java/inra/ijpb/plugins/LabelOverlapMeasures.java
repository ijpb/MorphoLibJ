/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2026 INRA.
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
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.ResultsBuilder;
/**
 * This class implements a plugin to calculate overlap measurements over
 * an source and a target label image (2D or 3D).
 *
 * @author Ignacio Arganda-Carreras (ignacio.arganda@ehu.eus)
 *
 */
public class LabelOverlapMeasures implements PlugIn{
	static int sourceIndex = 0;
	static int targetIndex = 1;
	static String[] measureLabels = new String[]{ "Overlap", "Jaccard index", "Dice coefficient",
			"Volume Similarity", "False Negative Error", "False Positive Error"};

	static boolean[] measureStates = new boolean[]{ true, true, true, true,
			true, true };

	@Override
	public void run(String arg) 
	{		
		int nbima = WindowManager.getImageCount();
		
		if( nbima < 2 )
		{
			IJ.error( "Label Overlap Measures input error",
					"ERROR: At least two images need to be open to run "
							+ "Intensity Measures 2D/3D" );
			return;
		}

        String[] names = new String[ nbima ];        
        
        for (int i = 0; i < nbima; i++)         
            names[ i ] = WindowManager.getImage(i + 1).getTitle();
        
        if( sourceIndex > nbima-1 )
        	sourceIndex = nbima - 1;
        if( targetIndex > nbima-1 )
        	targetIndex = nbima - 1;
        
        GenericDialog gd = new GenericDialog( "Label Overlap Measures" );
        gd.addChoice( "Source image", names, names[ sourceIndex ] );
        gd.addChoice( "Target image", names, names[ targetIndex ] );
        gd.addMessage("Label Overlap and Agreement Measures:");
        gd.addCheckboxGroup( measureLabels.length / 2 + 1, 2, measureLabels,
        		measureStates );
        
        gd.showDialog();
        
        if( gd.wasOKed() )
        {
        	sourceIndex = gd.getNextChoiceIndex();
        	targetIndex = gd.getNextChoiceIndex();
           
        	for( int i=0; i<measureStates.length; i++ )        	
        		measureStates[ i ] = gd.getNextBoolean();
        	        		                        
            boolean calculateMeasures = false;
            for( int i=0; i<6; i++ )
            	if( measureStates[ i ] )
            		calculateMeasures = true;
            
            if ( calculateMeasures == false )
            	return;
                        
            ImagePlus sourceImage = WindowManager.getImage( sourceIndex + 1 );
            ImagePlus targetImage = WindowManager.getImage( targetIndex + 1 );
            
            if( sourceImage.getWidth() != targetImage.getWidth() || 
            		sourceImage.getHeight() != targetImage.getHeight() )
            {
            	IJ.error( "Label Overlap Measures input error", "Error: input"
            			+ " and label images must have the same size" );
            	return;
            }

            ResultsBuilder rb = new ResultsBuilder();
            ResultsTable totalTable = new ResultsTable(); 
            totalTable.incrementCounter();
            totalTable.setPrecision( 6 );

            if( measureStates[ 0 ] ) // Overlap
            {
            	rb.addResult( LabelImages.getTargetOverlapPerLabel( sourceImage, targetImage ) );
            	totalTable.addValue( "TotalOverlap", LabelImages.getTotalOverlap( sourceImage, targetImage ) );
            }

            if( measureStates[ 1 ] ) // Jaccard index
            {
            	rb.addResult( LabelImages.getJaccardIndexPerLabel( sourceImage, targetImage ) );
            	totalTable.addValue( "JaccardIndex", LabelImages.getJaccardIndex( sourceImage, targetImage ) );
            }

            if( measureStates[ 2 ] ) // Dice coefficient
            {
            	rb.addResult( LabelImages.getDiceCoefficientPerLabel( sourceImage, targetImage ) );
            	totalTable.addValue( "DiceCoefficient", LabelImages.getDiceCoefficient( sourceImage, targetImage ) );
            }

            if( measureStates[ 3 ] ) // Volume similarity
            {
            	rb.addResult( LabelImages.getVolumeSimilarityPerLabel( sourceImage, targetImage ) );
            	totalTable.addValue( "VolumeSimilarity", LabelImages.getVolumeSimilarity( sourceImage, targetImage ) );
            }

            if( measureStates[ 4 ] ) // False negative error
            {
            	rb.addResult( LabelImages.getFalseNegativeErrorPerLabel( sourceImage, targetImage ) );
            	totalTable.addValue( "FalseNegativeError", LabelImages.getFalseNegativeError( sourceImage, targetImage ) );
            }

            if( measureStates[ 5 ] ) // False positive error
            {
            	rb.addResult( LabelImages.getFalsePositiveErrorPerLabel( sourceImage, targetImage ) );
            	totalTable.addValue( "FalsePositiveError", LabelImages.getFalsePositiveError( sourceImage, targetImage ) );
            }
            // show table with results for all labels
            totalTable.show( sourceImage.getShortTitle() +
            		"-all-labels-overlap-measurements" );
            // set 6 decimal places in the displayed results
            rb.getResultsTable().setPrecision( 6 );
            // show table with results for individual labels
            rb.getResultsTable().show( sourceImage.getShortTitle() +
            		"-individual-labels-overlap-measurements" );
        }
	}
}
