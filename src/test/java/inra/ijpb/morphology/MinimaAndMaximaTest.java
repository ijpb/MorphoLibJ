/**
 * 
 */
package inra.ijpb.morphology;

import static org.junit.Assert.*;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.strel.SquareStrel;

import org.junit.Test;

/**
 * @author David Legland
 *
 */
public class MinimaAndMaximaTest {

	/**
	 * Test method for {@link ijt.filter.morphology.MinimaAndMaxima#regionalMaxima(ij.process.ImageProcessor, int)}.
	 */
	@Test
	public final void testRegionalMaximaImageProcessor_Conn4() {
		int[] data = new int[]{
				10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10,
				10, 20, 20, 20, 10, 10, 10, 30, 30, 30, 10,
				10, 20, 20, 20, 10, 10, 10, 30, 30, 30, 10,
				10, 20, 20, 20, 10, 10, 10, 30, 30, 30, 10,
				10, 10, 10, 10, 40, 40, 40, 10, 10, 10, 10,
				10, 10, 10, 10, 40, 40, 40, 10, 10, 10, 10,
				10, 10, 10, 10, 40, 40, 40, 10, 10, 10, 10,
				10, 20, 20, 20, 10, 10, 10, 30, 30, 30, 10,
				10, 20, 20, 20, 10, 10, 10, 30, 30, 30, 10,
				10, 20, 20, 20, 10, 10, 10, 30, 30, 30, 10,
				10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10
		};
		ImageProcessor image = new ByteProcessor(11, 11);
		for (int i = 0; i < 11*11; i++) {
			image.set(i, data[i]);
		}
		
		ImageProcessor maxima = MinimaAndMaxima.regionalMaxima(image, 4);
		printImage(maxima);
		
		assertEquals(0, maxima.get(0, 0));
		assertEquals(255, maxima.get(1, 1));
		assertEquals(255, maxima.get(9, 1));
		assertEquals(255, maxima.get(5, 5));
		assertEquals(0, maxima.get(10, 10));
	}

	/**
	 * Test method for {@link ijt.filter.morphology.MinimaAndMaxima#regionalMaxima(ij.process.ImageProcessor, int)}.
	 */
	@Test
	public final void testRegionalMaximaImageProcessor_Conn8() {
		int[] data = new int[]{
				10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10,
				10, 20, 20, 20, 10, 10, 10, 30, 30, 30, 10,
				10, 20, 20, 20, 10, 10, 10, 30, 30, 30, 10,
				10, 20, 20, 20, 10, 10, 10, 30, 30, 30, 10,
				10, 10, 10, 10, 40, 40, 40, 10, 10, 10, 10,
				10, 10, 10, 10, 40, 40, 40, 10, 10, 10, 10,
				10, 10, 10, 10, 40, 40, 40, 10, 10, 10, 10,
				10, 20, 20, 20, 10, 10, 10, 30, 30, 30, 10,
				10, 20, 20, 20, 10, 10, 10, 30, 30, 30, 10,
				10, 20, 20, 20, 10, 10, 10, 30, 30, 30, 10,
				10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10
		};
		ImageProcessor image = new ByteProcessor(11, 11);
		for (int i = 0; i < 11*11; i++) {
			image.set(i, data[i]);
		}
		
		ImageProcessor maxima = MinimaAndMaxima.regionalMaxima(image, 8);
//		printImage(maxima);
		
		assertEquals(0, maxima.get(0, 0));
		assertEquals(0, maxima.get(1, 1));
		assertEquals(0, maxima.get(9, 1));
		assertEquals(255, maxima.get(5, 5));
		assertEquals(0, maxima.get(10, 10));
	}

	/**
	 * Test method for {@link ijt.filter.morphology.MinimaAndMaxima#regionalMaxima(ij.process.ImageProcessor, int)}.
	 */
	@Test
	public final void testRegionalMinimaImageProcessorInt() {
		int[][] data = new int[][]{
				{50, 50, 50, 50, 50},
				{50, 10, 50, 50, 50},
				{50, 50, 10, 50, 50},
				{50, 50, 50, 10, 50},
				{50, 50, 20, 30, 50},
				{50, 50, 50, 50, 50},
		};
		int height = data.length;
		int width = data[0].length;
		ImageProcessor image = new ByteProcessor(width, height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				image.set(x, y, data[y][x]);
			}
		}
		
		ImageProcessor maxima = MinimaAndMaxima.regionalMinima(image, 8);
//		printImage(maxima);
		
		assertEquals(0, maxima.get(0, 0));
		assertEquals(255, maxima.get(1, 1));
		assertEquals(255, maxima.get(2, 2));
		assertEquals(255, maxima.get(3, 3));
		assertEquals(0, maxima.get(2, 4));
	}

	/**
	 * Test method for {@link ijt.filter.morphology.MinimaAndMaxima#regionalMaxima(ij.process.ImageProcessor, int)}.
	 */
	@Test
	public final void testRegionalMinimaImageProcessor_Conn8() {
		int[][] data = new int[][]{
				{50, 50, 50, 50, 50},
				{50, 10, 50, 50, 50},
				{50, 50, 20, 50, 50},
				{50, 50, 50, 10, 50},
				{50, 50, 20, 10, 50},
				{50, 50, 50, 50, 50},
		};
		int height = data.length;
		int width = data[0].length;
		ImageProcessor image = new ByteProcessor(width, height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				image.set(x, y, data[y][x]);
			}
		}
		
		ImageProcessor maxima = MinimaAndMaxima.regionalMinima(image, 8);
//		printImage(maxima);
		
		assertEquals(0, maxima.get(0, 0));
		assertEquals(255, maxima.get(1, 1));
		assertEquals(0, maxima.get(2, 2));
		assertEquals(0, maxima.get(2, 4));
		assertEquals(255, maxima.get(3, 4));
	}

	@Test
	public final void testRegionalMaximaAlgosConsistency_C4 () {
		String fileName = getClass().getResource("/files/grains-crop.png").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		ImageProcessor image = imagePlus.getProcessor();
		
		ImageProcessor result1 = MinimaAndMaxima.regionalMaxima(image, 4);
		ImageProcessor result2 = MinimaAndMaxima.regionalMaximaByReconstruction(image, 4);
		int width = image.getWidth();
		int height = image.getHeight();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				assertEquals(result1.get(x, y), result2.get(x, y));
			}
		}
	}
	
	@Test
	public final void testRegionalMaximaAlgosConsistency_C8 () {
		String fileName = getClass().getResource("/files/grains-crop.png").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		ImageProcessor image = imagePlus.getProcessor();
		
		ImageProcessor result1 = MinimaAndMaxima.regionalMaxima(image, 8);
		ImageProcessor result2 = MinimaAndMaxima.regionalMaximaByReconstruction(image, 8);
		int width = image.getWidth();
		int height = image.getHeight();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				assertEquals(result1.get(x, y), result2.get(x, y));
			}
		}
	}
	
	@Test
	public final void testRegionalMinimaAlgosConsistency_C4 () {
		String fileName = getClass().getResource("/files/grains-crop.png").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		ImageProcessor image = imagePlus.getProcessor();
		
		ImageProcessor result1 = MinimaAndMaxima.regionalMinima(image);
		ImageProcessor result2 = MinimaAndMaxima.regionalMinimaByReconstruction(image, 4);
		int width = image.getWidth();
		int height = image.getHeight();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				assertEquals(result1.get(x, y), result2.get(x, y));
			}
		}
	}
	
	@Test
	public final void testRegionalMinimaAlgosConsistency_C8 () {
		String fileName = getClass().getResource("/files/grains-crop.png").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		ImageProcessor image = imagePlus.getProcessor();
		
		ImageProcessor result1 = MinimaAndMaxima.regionalMinima(image, 8);
		ImageProcessor result2 = MinimaAndMaxima.regionalMinimaByReconstruction(image, 8);
		int width = image.getWidth();
		int height = image.getHeight();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				assertEquals(result1.get(x, y), result2.get(x, y));
			}
		}
	}
	
	@Test
	public final void testExtendedMinMaxConsistency_C4 () {
		String fileName = getClass().getResource("/files/grains-crop.png").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		ImageProcessor image = imagePlus.getProcessor();
		
		ImageProcessor result1 = MinimaAndMaxima.extendedMaxima(image, 10);
		image.invert();
		ImageProcessor result2 = MinimaAndMaxima.extendedMinima(image, 10);
		
		int width = image.getWidth();
		int height = image.getHeight();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				assertEquals(result1.get(x, y), result2.get(x, y));
			}
		}
	}
	
	@Test
	public final void testExtendedMinMaxConsistency_C8 () {
		String fileName = getClass().getResource("/files/grains-crop.png").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		ImageProcessor image = imagePlus.getProcessor();
		
		ImageProcessor result1 = MinimaAndMaxima.extendedMaxima(image, 10, 8);
		image.invert();
		ImageProcessor result2 = MinimaAndMaxima.extendedMinima(image, 10, 8);
		
		int width = image.getWidth();
		int height = image.getHeight();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				assertEquals(result1.get(x, y), result2.get(x, y));
			}
		}
	}
	
	@Test
	public final void testImposeMinima_C4 () {
		String fileName = getClass().getResource("/files/grains-crop.png").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		ImageProcessor image = imagePlus.getProcessor();
		
		Strel strel = SquareStrel.fromDiameter(3);
		ImageProcessor grad = Morphology.gradient(image, strel);
		ImageProcessor emin = MinimaAndMaxima.extendedMinima(grad, 20, 4);

		ImageProcessor imp = MinimaAndMaxima.imposeMinima(grad, emin, 4);

		ImageProcessor rmin = MinimaAndMaxima.regionalMinima(imp, 4);

		
		int width = image.getWidth();
		int height = image.getHeight();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				assertTrue(rmin.get(x, y) == emin.get(x, y));
			}
		}
	}

	@Test
	public final void testImposeMinima_C8 () {
		String fileName = getClass().getResource("/files/grains-crop.png").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		ImageProcessor image = imagePlus.getProcessor();
		
		Strel strel = SquareStrel.fromDiameter(3);
		ImageProcessor grad = Morphology.gradient(image, strel);
		ImageProcessor emin = MinimaAndMaxima.extendedMinima(grad, 20, 8);

		ImageProcessor imp = MinimaAndMaxima.imposeMinima(grad, emin, 8);

		ImageProcessor rmin = MinimaAndMaxima.regionalMinima(imp, 8);

		
		int width = image.getWidth();
		int height = image.getHeight();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				assertTrue(rmin.get(x, y) == emin.get(x, y));
			}
		}
	}
	
	@Test
	public final void testImposeMinimaMaximaConsistency_C8 () {
		String fileName = getClass().getResource("/files/grains-crop.png").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		ImageProcessor image = imagePlus.getProcessor();
		
		Strel strel = SquareStrel.fromDiameter(3);
		ImageProcessor grad = Morphology.gradient(image, strel);
		ImageProcessor markers = MinimaAndMaxima.extendedMinima(grad, 20, 8);
		
		ImageProcessor imp = MinimaAndMaxima.imposeMinima(grad, markers, 8);
		ImageProcessor rmin = MinimaAndMaxima.regionalMinima(imp, 8);

		grad.invert();
		ImageProcessor imp2 = MinimaAndMaxima.imposeMaxima(grad, markers, 8);
		ImageProcessor rmax = MinimaAndMaxima.regionalMaxima(imp2, 8);
		
		
		int width = image.getWidth();
		int height = image.getHeight();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				assertEquals("Results differ at position " + x + "," + y, 
						rmin.get(x, y), rmax.get(x, y));
			}
		}
	}

	
	public void printImage(ImageProcessor image) {
		int width = image.getWidth();
		int height = image.getHeight();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				System.out.print(image.get(x, y) + " ");
			}
			System.out.println("");			
		}
	}
}
