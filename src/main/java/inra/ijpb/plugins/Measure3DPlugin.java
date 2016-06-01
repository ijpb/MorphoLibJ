package inra.ijpb.plugins;

import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import inra.ijpb.measure.IntensityMeasures;

public class Measure3DPlugin implements PlugIn{

	static int inputIndex = 0;
	static int labelsIndex = 1;
	static String[] measureLabels = new String[]{ "Mean", "StdDev", "Max",
			"Min", "Median", "Mode", "Skewness", "Kurtosis",
			"NumberOfVoxels", "Volume" };

	static boolean[] measureStates = new boolean[]{ true, true, true, true,
			true, true, true, true, true, true };

	@Override
	public void run(String arg) 
	{		
		int nbima = WindowManager.getImageCount();
		
		if( nbima < 2 )
		{
			IJ.error( "Measure 3D I/O error", 
					"ERROR: At least two images need to be open to run measure 3D");
			return;
		}
		
        String[] names = new String[ nbima ];        
        
        for (int i = 0; i < nbima; i++)         
            names[ i ] = WindowManager.getImage(i + 1).getShortTitle();
        
        if( inputIndex > nbima-1 )
        	inputIndex = nbima - 1;
        if( labelsIndex > nbima-1 )
        	labelsIndex = nbima - 1;
        
        GenericDialog gd = new GenericDialog( "Measure 3D" );
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
            	IJ.error( "Measure 3D I/O error", "Error: input and label images must have the same size" );
            	return;
            }
            
            ArrayList< ResultsTable > results = new ArrayList<ResultsTable>(); 
            
            
            final IntensityMeasures im = new IntensityMeasures( inputImage, labelImage );

            if( measureStates[ 0 ] ) // Mean            	
            	results.add( im.getMean() );

            if( measureStates[ 1 ] ) // Standard deviation			            	
            	results.add( im.getStdDev() );

            if( measureStates[ 2 ] ) // Max         	
            	results.add( im.getMax() );

            if( measureStates[ 3 ] ) // Min            	
            	results.add( im.getMin() );

            if( measureStates[ 4 ] ) // Median
            	results.add( im.getMedian() );

            if( measureStates[ 5 ] ) // Mode
            	results.add( im.getMode() );

            if( measureStates[ 6 ] ) // Skewness
            	results.add( im.getSkewness() );

            if( measureStates[ 7 ] ) // Kurtosis
            	results.add( im.getKurtosis() );

            if( measureStates[ 8 ] ) // Number of voxels
            	results.add( im.getNumberOfVoxels() );

            if( measureStates[ 9 ] ) // Volume
            	results.add( im.getVolume() );

            ResultsTable mergedTable = new ResultsTable();
            final int numLabels = results.get( 0 ).getCounter();
            
            for(int i=0; i < numLabels; i++ )
            {
            	mergedTable.incrementCounter();
            	String label = results.get( 0 ).getLabel( i );
            	mergedTable.addLabel(label);
            	
            	for( int j=0; j<results.size(); j++ )
            	{
            		String measure = results.get( j ).getColumnHeading( 0 );
            		double value = results.get( j ).getValue( measure, i );
            		mergedTable.addValue( measure, value );
            	}
            }
            
            mergedTable.show( "3D measurements" );
        }

		
	}

}
