package inra.ijpb.morphology.attrfilt;

import static org.junit.Assert.*;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.extrema.ExtremaType;
import inra.ijpb.morphology.extrema.RegionalExtremaAlgo;
import inra.ijpb.morphology.extrema.RegionalExtremaByFlooding;

import org.junit.Test;

public class AreaOpeningQueueTest
{

	@Test
	public void testProcess()
	{
		int sizeX = 4;
		int sizeY = 4;
		ImageProcessor image = new ByteProcessor(sizeX, sizeY);
		image.set(1, 1, 5);
		image.set(2, 1, 4);
		image.set(1, 2, 3);
		image.set(2, 2, 2);
		
		AreaOpening algo = new AreaOpeningQueue();
//		algo.setConnectivity(4);
//		algo.setExtremaType(ExtremaType.MAXIMA);

		ImageProcessor output = algo.process(image, 4);
		
		for (int y = 0; y < sizeY; y++)
		{
			for (int x = 0; x < sizeX; x++)
			{
				System.out.print(String.format("%4d", output.get(x, y)));
			}
			System.out.println();
		}
	
	}
	@Test
	public void testProcessTwoMaxima()
	{
		int sizeX = 6;
		int sizeY = 4;
		ImageProcessor image = new ByteProcessor(sizeX, sizeY);
		image.set(1, 1, 5);
		image.set(1, 2, 4);
		image.set(2, 1, 3);
		image.set(2, 2, 2);
		image.set(3, 1, 6);
		image.set(3, 2, 5);
		
		AreaOpening algo = new AreaOpeningQueue();
//		algo.setConnectivity(4);
//		algo.setExtremaType(ExtremaType.MAXIMA);

		ImageProcessor output = algo.process(image, 4);
		
		for (int y = 0; y < sizeY; y++)
		{
			for (int x = 0; x < sizeX; x++)
			{
				System.out.print(String.format("%4d", output.get(x, y)));
			}
			System.out.println();
		}
	
	}

}
