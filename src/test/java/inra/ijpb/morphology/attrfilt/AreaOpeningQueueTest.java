package inra.ijpb.morphology.attrfilt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

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

		ImageProcessor output = algo.process(image, 4);
		
		assertEquals(2, output.get(1, 1));
		assertEquals(2, output.get(2, 1));
		assertEquals(2, output.get(1, 2));
		assertEquals(2, output.get(2, 2));
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

		ImageProcessor output = algo.process(image, 4);
		
		assertEquals(3, output.get(1, 1));
		assertEquals(3, output.get(2, 1));
		assertEquals(3, output.get(3, 1));
		assertEquals(3, output.get(1, 2));
		assertEquals(2, output.get(2, 2));
		assertEquals(3, output.get(3, 2));
	}

	public void testProcessGrains()
	{
		String fileName = getClass().getResource("/files/grains.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		assertNotNull(imagePlus);
		ImageProcessor image = imagePlus.getProcessor();
		
		AreaOpening algo = new AreaOpeningQueue();

		long t0 = System.nanoTime();
		algo.process(image, 4);
		long t1 = System.nanoTime();
		double dt = (t1 - t0) / 1000000.0;
		System.out.println("Elapsed time: " + dt + " ms");
	}
}
