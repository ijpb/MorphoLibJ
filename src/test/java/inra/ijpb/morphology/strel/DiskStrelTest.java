package inra.ijpb.morphology.strel;

import static org.junit.Assert.*;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import org.junit.Test;

import inra.ijpb.morphology.Strel;


public class DiskStrelTest {

	@Test
	public void testGetSize() {
		Strel se = DiskStrel.fromDiameter(5);
		int[] size = se.getSize();
		assertEquals(5, size[0]);
		assertEquals(5, size[1]);
	}

	/**
	 * Dilates a single pixel by a 3x3 disk, and check the shape of the result.
	 * The result should be a 3x3 square (approximation of 3x3 disk)
	 */
	@Test
	public void testDilate_SinglePixel_Radius() {
		Strel strel = DiskStrel.fromRadius(1);
		
		ImageProcessor image = new ByteProcessor(10, 10);
		image.setValue(0);
		image.fill();
		image.set(5, 5, 255);
		ImageProcessor result = strel.dilation(image);

		// Check all values inside square
		for (int y = 4; y < 7; y++)
			for (int x = 4; x < 7; x++)
				assertEquals(255, result.get(x, y));
	}
	
	/**
	 * Dilates a single pixel by a disk with diameter 4. 
	 * The result should be larger than dilation with diameter 3.
	 */
	@Test
	public void testDilate_SinglePixel_EvenDiameter() {
		Strel disk3 = DiskStrel.fromDiameter(3);
		Strel disk4 = DiskStrel.fromDiameter(4);
		
		ImageProcessor image = new ByteProcessor(10, 10);
		image.setValue(0);
		image.fill();
		image.set(5, 5, 255);
		
		ImageProcessor result3 = disk3.dilation(image);
		ImageProcessor result4 = disk4.dilation(image);

		// Check result3 <= result4
		boolean different = false;
		for (int y = 0; y < 10; y++)
		{
			for (int x = 0; x < 10; x++)
			{
				int res3 = result3.get(x, y);
				int res4 = result4.get(x, y);
				assertTrue(res3 <= res4);
				
				if (res3 != res4)
				{
					different = true;
				}
			}
		}
		
		assertTrue(different);
	}

}
