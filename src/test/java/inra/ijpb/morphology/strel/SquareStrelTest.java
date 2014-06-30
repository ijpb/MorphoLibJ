package inra.ijpb.morphology.strel;

import static org.junit.Assert.*;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Strel;

import org.junit.Test;

public class SquareStrelTest {

	@Test
	public void testGetSize() {
		Strel strel = new SquareStrel(5);
		int[] size = strel.getSize();
		assertEquals(size[0], 5);
		assertEquals(size[1], 5);
	}

	@Test
	public void testReverse() {
		Strel strel = new SquareStrel(5);
		int[] size = strel.getSize();
		Strel strel2 = strel.reverse();
		int[] size2 = strel2.getSize();
		assertEquals(size[0], size2[0]);
		assertEquals(size[1], size2[1]);
	}

	@Test
	public void testGetMask() {
		Strel strel = new SquareStrel(5);
		int[][] mask = strel.getMask();
		
		assertEquals(mask.length, 5);
		assertEquals(mask[1].length, 5);
	}

	@Test
	public void testGetShifts() {
		Strel strel = new SquareStrel(5);
		int[][] shifts = strel.getShifts();
		
		assertEquals(shifts.length, 5 * 5);
		assertEquals(shifts[1].length, 2);
	}


	@Test
	public void testErosion_Square4x4() {
		ImageProcessor image = createImage_Square4x4();
		Strel strel = new SquareStrel(3);
		
		ImageProcessor result = strel.erosion(image);

		for (int y = 0; y < 4; y++) {
			for (int x = 0; x < 10; x++) {
				assertEquals(0, result.get(x, y));
			}
		}
		for (int y = 4; y < 6; y++) {
			assertEquals(0, result.get(3, y));
			assertEquals(255, result.get(4, y));
			assertEquals(255, result.get(5, y));
			assertEquals(0, result.get(6, y));
		}
		for (int y = 6; y < 10; y++) {
			for (int x = 0; x < 10; x++) {
				assertEquals(0, result.get(x, y));
			}
		}
	}

	@Test
	public void testDilation_Square4x4() {
		ImageProcessor image = createImage_Square4x4();
		Strel strel = new SquareStrel(3);
		
		ImageProcessor result = strel.dilation(image);

		for (int y = 0; y < 2; y++) {
			for (int x = 0; x < 10; x++) {
				assertEquals(0, result.get(x, y));
			}
		}
		for (int y = 2; y < 8; y++) {
			assertEquals(0, result.get(1, y));
			assertEquals(255, result.get(2, y));
			assertEquals(255, result.get(7, y));
			assertEquals(0, result.get(8, y));
		}
		for (int y = 8; y < 10; y++) {
			for (int x = 0; x < 10; x++) {
				assertEquals(0, result.get(x, y));
			}
		}
	}

	@Test
	public void testClosing() {
		ImageProcessor image = createImage_Square10x10();
		Strel strel = new SquareStrel(5);
		
		ImageProcessor result = strel.closing(image);
		
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				assertEquals(image.get(x, y), result.get(x, y));
			}			
		}
	}

	/**
	 * Try to compute morphological closing when there is edge effect: 
	 * the result is completely white. 
	 */
	@Test
	public void testClosing_EdgeEffect() {
		ImageProcessor image = createImage_Square4x4();
		Strel strel = new SquareStrel(15);
		
		ImageProcessor result = strel.closing(image);
		
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				assertEquals(255, result.get(x, y));
			}			
		}
	}

	/**
	 * Try to compute morphological closing with a strel larger than the 
	 * original image.
	 */
	@Test
	public void testClosing_VeryBigStrel() {
		ImageProcessor image = createImage_Square4x4();
		Strel strel = new SquareStrel(30);
		
		ImageProcessor result = strel.closing(image);
		
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				assertEquals(255, result.get(x, y));
			}			
		}
	}

	@Test
	public void testOpening() {
		ImageProcessor image = createImage_Square10x10();
		Strel strel = new SquareStrel(5);
		
		ImageProcessor result = strel.opening(image);
		
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				assertEquals(image.get(x, y), result.get(x, y));
			}			
		}
	}
	
	@Test
	public void testErosion_Square4x4_short() {
		ImageProcessor image = createImage_Square4x4();
		image = image.convertToShort(false);
		Strel strel = new SquareStrel(3);
		
		ImageProcessor result = strel.erosion(image);

		for (int y = 0; y < 4; y++) {
			for (int x = 0; x < 10; x++) {
				assertEquals(0, result.get(x, y));
			}
		}
		for (int y = 4; y < 6; y++) {
			assertEquals(0, result.get(3, y));
			assertEquals(255, result.get(4, y));
			assertEquals(255, result.get(5, y));
			assertEquals(0, result.get(6, y));
		}
		for (int y = 6; y < 10; y++) {
			for (int x = 0; x < 10; x++) {
				assertEquals(0, result.get(x, y));
			}
		}
	}

	@Test
	public void testDilation_Square4x4_short() {
		ImageProcessor image = createImage_Square4x4();
		image = image.convertToShort(false);
		Strel strel = new SquareStrel(3);
		
		ImageProcessor result = strel.dilation(image);

		for (int y = 0; y < 2; y++) {
			for (int x = 0; x < 10; x++) {
				assertEquals(0, result.get(x, y));
			}
		}
		for (int y = 2; y < 8; y++) {
			assertEquals(0, result.get(1, y));
			assertEquals(255, result.get(2, y));
			assertEquals(255, result.get(7, y));
			assertEquals(0, result.get(8, y));
		}
		for (int y = 8; y < 10; y++) {
			for (int x = 0; x < 10; x++) {
				assertEquals(0, result.get(x, y));
			}
		}
	}


	/**
	 * Creates a 10-by-10 image with a 4-by-4 square in the middle.
	 */
	private ImageProcessor createImage_Square4x4 () {
		ImageProcessor image = new ByteProcessor(10, 10);
		image.setValue(0);
		image.fill();
		
		for (int y = 3; y < 7; y++) {
			for (int x = 3; x < 7; x++) {
				image.set(x, y, 255);
			}			
		}
		
		return image;
	}

	/**
	 * Creates a 30-by-30 image with a 10-by-10 square in the middle.
	 */
	private ImageProcessor createImage_Square10x10 () {
		ImageProcessor image = new ByteProcessor(30, 30);
		image.setValue(0);
		image.fill();
		
		for (int y = 10; y < 20; y++) {
			for (int x = 10; x < 20; x++) {
				image.set(x, y, 255);
			}			
		}
		
		return image;
	}

}
