package inra.ijpb.measure;

import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import inra.ijpb.data.image.Images3D;

public class IntensityMeasures {

	ArrayList<Double>[] objectVoxels;
	
	public IntensityMeasures(
			ImagePlus input,
			ImagePlus labels )
	{
		final int width = input.getWidth();
		final int height = input.getHeight();

		double[] extrema = Images3D.findMinAndMax( labels );

		final int numLabels = (int) extrema[ 1 ];

		// initialize lists of voxels per object
		objectVoxels = new ArrayList[ numLabels ];

		for( int i=0; i<numLabels; i++ )
			objectVoxels[ i ] = new ArrayList<Double>();
		final long start = System.currentTimeMillis();		
		// read voxel intensities for each object
		for( int z=1; z <= input.getImageStackSize(); z++ )
		{
			final ImageProcessor grayIP = input.getImageStack().getProcessor( z );
			final ImageProcessor labelsIP = labels.getImageStack().getProcessor( z );

			for( int x = 0; x<width; x++ )
				for( int y = 0; y<height; y++ )
					objectVoxels[(int) labelsIP.getf(x, y) ].add( (double) grayIP.getf(x, y) );
		}
		final long end = System.currentTimeMillis();
		IJ.log("Reading voxels took " + (end-start) + " ms.");
	}
}
