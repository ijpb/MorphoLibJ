package inra.ijpb.morphology;

import static org.junit.Assert.*;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.strel.SquareStrel;

import org.junit.Test;

public class MorphologyTest {

	@Test
	public void testGetAllLabels() {
		String[] labels = Morphology.Operation.getAllLabels();
		assertNotNull(labels);
		assertTrue(labels.length > 0);
	}

	@Test
	public void testFromLabel() {
		Morphology.Operation op;
			
		op = Morphology.Operation.fromLabel("Closing");
		assertEquals(Morphology.Operation.CLOSING, op);

		op = Morphology.Operation.fromLabel("Gradient");
		assertEquals(Morphology.Operation.GRADIENT, op);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromLabel_Illegal() {
		Strel.Shape.fromLabel("Illegal Strel");
	}

	@Test
	public void testFromLabel_Illegal2() {
		boolean ok = false;
		try {
			Strel.Shape.fromLabel("Illegal Strel");
		} catch (IllegalArgumentException ex) {
			ok = true;
		}
		assertTrue(ok);
	}

	/**
	 * Tests if each operation given in the enumeration can be applied on 
	 * a basic test image.
	 */
	@Test
	public void testApplyOperation() {
		ImageProcessor image = new ByteProcessor(100, 100);
		// initialize image
		for (int y = 40; y < 60; y++) {
			for (int x = 40; x < 60; x++) {
				image.set(x, y, 255);
			}
		}

		Strel strel = SquareStrel.fromDiameter(5);
		
		for (Morphology.Operation op : Morphology.Operation.values()) {
			ImageProcessor result = op.apply(image, strel);
			assertNotNull(result);
		}
	}

	/**
	 * Tests the stability of closing by square when particle is a square.
	 */
	@Test
	public void testClosing_Square() {
		ImageProcessor image = new ByteProcessor(100, 100);
		// initialize image
		for (int y = 40; y < 60; y++) {
			for (int x = 40; x < 60; x++) {
				image.set(x, y, 255);
			}
		}

		Strel strel = SquareStrel.fromDiameter(5);
		ImageProcessor result = Morphology.closing(image, strel);

		for (int y = 40; y < 60; y++) {
			for (int x = 40; x < 60; x++) {
				assertEquals(image.get(x,y), result.get(x, y));
			}
		}
	}

	/**
	 * Tests closing can be run on an RGB image.
	 */
	@Test
	public void testClosing_Square_RGB() {
		String fileName = getClass().getResource("/files/peppers-crop.png").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		assertNotNull(imagePlus);
		ColorProcessor image = (ColorProcessor) imagePlus.getProcessor();
		
		Strel strel = SquareStrel.fromDiameter(5);
		ColorProcessor result = (ColorProcessor) Morphology.closing(image, strel);
		assertNotNull(result);
		
		// Check that result is greater than or equal to the original image
		int width = image.getWidth();
		int height = image.getHeight();
		int[] rgb0 = new int[3];
		int[] rgb = new int[3];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				image.getPixel(x, y, rgb0);
				result.getPixel(x, y, rgb);
				for (int c = 0; c < 3; c++)
					assertTrue(rgb[c] >= rgb0[c]);
			}
		}
	}

	/**
	 * Tests closing can be run on an RGB image.
	 */
	@Test
	public void testOpening_Square_RGB() {
		String fileName = getClass().getResource("/files/peppers-crop.png").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		assertNotNull(imagePlus);
		ColorProcessor image = (ColorProcessor) imagePlus.getProcessor();
		
		Strel strel = SquareStrel.fromDiameter(5);
		ColorProcessor result = (ColorProcessor) Morphology.opening(image, strel);
		assertNotNull(result);
		
		// Check that result is lower than or equal to the original image
		int width = image.getWidth();
		int height = image.getHeight();
		int[] rgb0 = new int[3];
		int[] rgb = new int[3];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				image.getPixel(x, y, rgb0);
				result.getPixel(x, y, rgb);
				for (int c = 0; c < 3; c++)
					assertTrue(rgb[c] <= rgb0[c]);
			}
		}
	}

	/**
	 * Tests that most morphological operations can be run on an RGB image.
	 */
	@Test
	public void testVariousOperations_Square_RGB() {
		String fileName = getClass().getResource("/files/peppers-crop.png").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		assertNotNull(imagePlus);
		ColorProcessor image = (ColorProcessor) imagePlus.getProcessor();
		
		Strel strel = SquareStrel.fromDiameter(5);

		assertNotNull(Morphology.erosion(image, strel));
		assertNotNull(Morphology.dilation(image, strel));

		assertNotNull(Morphology.closing(image, strel));
		assertNotNull(Morphology.opening(image, strel));

		assertNotNull(Morphology.gradient(image, strel));
		assertNotNull(Morphology.internalGradient(image, strel));
		assertNotNull(Morphology.externalGradient(image, strel));
		assertNotNull(Morphology.laplacian(image, strel));
		
		assertNotNull(Morphology.blackTopHat(image, strel));
		assertNotNull(Morphology.whiteTopHat(image, strel));

	}
}
