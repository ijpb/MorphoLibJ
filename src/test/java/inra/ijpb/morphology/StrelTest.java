package inra.ijpb.morphology;

import static org.junit.Assert.*;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import org.junit.Test;

public class StrelTest {

	@Test
	public void testGetAllLabels() {
		String[] labels = Strel.Shape.getAllLabels();
		assertNotNull(labels);
		assertTrue(labels.length > 0);
	}

	@Test
	public void testFromLabel() {
		Strel.Shape type;
			
		type = Strel.Shape.fromLabel("Square");
		assertEquals(Strel.Shape.SQUARE, type);

		type = Strel.Shape.fromLabel("Vertical Line");
		assertEquals(Strel.Shape.LINE_VERT, type);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromLabel_Illegal() {
		Strel.Shape.fromLabel("Illegal Strel");
	}

	@Test
	public void testCreate_Square() {
		Strel se = Strel.Shape.SQUARE.fromDiameter(5);
		
		int[] size = se.getSize();
		assertEquals(5, size[0]);
		assertEquals(5, size[1]);
	}

	@Test
	public void testCreate_LineH() {
		Strel se = Strel.Shape.LINE_HORIZ.fromDiameter(5);
		
		int[] size = se.getSize();
		assertEquals(5, size[0]);
		assertEquals(1, size[1]);
	}

	@Test
	public void testCreate_LineV() {
		Strel se = Strel.Shape.LINE_VERT.fromDiameter(5);
		
		int[] size = se.getSize();
		assertEquals(1, size[0]);
		assertEquals(5, size[1]);
	}

	@Test
	public void testClosing_allStrels() {
		ImageProcessor image = new ByteProcessor(100, 100);
		// initialize image
		for (int y = 40; y < 60; y++) {
			for (int x = 40; x < 60; x++) {
				image.set(x, y, 255);
			}
		}
		
		for (Strel.Shape type : Strel.Shape.values()) {
			Strel strel = type.fromDiameter(5);
			ImageProcessor result = Morphology.closing(image, strel);
			
			for (int y = 40; y < 60; y++) {
				for (int x = 40; x < 60; x++) {
					assertEquals(image.get(x,y), result.get(x, y));
				}
			}
		}
	}
}
