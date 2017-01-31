/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2017 INRA.
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
package inra.ijpb.morphology.strel;

import static org.junit.Assert.*;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import org.junit.Test;

import inra.ijpb.morphology.Strel;

public class OctagonStrelTest {

	@Test
	public void testGetSize() {
		Strel strel = new OctagonStrel(5);
		int[] size = strel.getSize();
		assertEquals(size[0], 5);
		assertEquals(size[1], 5);
	}

	@Test
	public void testReverse() {
		Strel strel = new OctagonStrel(5);
		int[] size = strel.getSize();
		Strel strel2 = strel.reverse();
		int[] size2 = strel2.getSize();
		assertEquals(size[0], size2[0]);
		assertEquals(size[1], size2[1]);
	}

	@Test
	public void testReverse_4x4() {
		Strel strel = new OctagonStrel(4);
		Strel strel2 = strel.reverse();
		int[] size = strel2.getSize();
		assertEquals(4, size[0]);
		assertEquals(4, size[1]);
		
		int[] offset = strel2.getOffset();
		assertEquals(2, offset[0]);
		assertEquals(2, offset[1]);
	}

	/**
	 * Only tests the size of the mask
	 */
	@Test
	public void testGetMask() {
		Strel strel = new OctagonStrel(5);
		int[][] mask = strel.getMask();
		
		assertEquals(mask.length, 5);
		assertEquals(mask[1].length, 5);
	}

	/**
	 * Only tests the size of the mask
	 */
	@Test
	public void testGetShifts() {
		Strel strel = new OctagonStrel(5);
		int[][] shifts = strel.getShifts();
		
		assertEquals(shifts.length, 5 * 5);
		assertEquals(shifts[1].length, 2);
	}

	/**
	 * Dilates a single pixel by a 3x3 octagon, and check the shape of the result.
	 * The result should be a 3x3 square (approximation of 3x3 octagon)
	 */
	@Test
	public void testDilateOctagon3x3() {
		Strel strel = new OctagonStrel(3);
		
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
	 * Erosion of a 4x4 square by a 3x3 octagon should leave only a 2x2 square.
	 */
	@Test
	public void testErosion_Square4x4() {
		ImageProcessor image = createImage_Square4x4();
		Strel strel = new OctagonStrel(3);
		
		ImageProcessor result = strel.erosion(image);

		// upper rows are empty
		for (int y = 0; y < 4; y++) {
			for (int x = 0; x < 10; x++) {
				assertEquals(0, result.get(x, y));
			}
		}
		
		// Only a 2x2 square should stay in the middle
		for (int y = 4; y < 6; y++) {
			assertEquals(0, result.get(3, y));
			assertEquals(255, result.get(4, y));
			assertEquals(255, result.get(5, y));
			assertEquals(0, result.get(6, y));
		}
		
		// lower rows are empty
		for (int y = 6; y < 10; y++) {
			for (int x = 0; x < 10; x++) {
				assertEquals(0, result.get(x, y));
			}
		}
	}

	@Test
	public void testDilation_Square4x4() {
		// Create square with width 4 (foreground for x or y between 3 and 6 inclusive)
		ImageProcessor image = createImage_Square4x4();
		Strel strel = new OctagonStrel(3);
		
		// dilate image
		ImageProcessor result = strel.dilation(image);

		// Upper rows are empty
		for (int y = 0; y < 2; y++) {
			for (int x = 0; x < 10; x++) {
				assertEquals(0, result.get(x, y));
			}
		}

		// up row +1 should have same width are original square
		assertEquals(0, result.get(1, 2));
		assertEquals(255, result.get(3, 2));
		assertEquals(255, result.get(6, 2));
		assertEquals(0, result.get(8, 2));

		// middle rows have width 4+2 (from 2 to 7 inclusive
		for (int y = 3; y < 7; y++) {
			assertEquals(0, result.get(1, y));
			assertEquals(255, result.get(2, y));
			assertEquals(255, result.get(7, y));
			assertEquals(0, result.get(8, y));
		}
		
		// lower row-1 should have same width are original square
		assertEquals(0, result.get(1, 7));
		assertEquals(255, result.get(3, 7));
		assertEquals(255, result.get(6, 7));
		assertEquals(0, result.get(8, 7));

		// Lower rows are empty
		for (int y = 8; y < 10; y++) {
			for (int x = 0; x < 10; x++) {
				assertEquals(0, result.get(x, y));
			}
		}
	}

	@Test
	public void testClosing_3x3() {
		ImageProcessor image = createImage_Square10x10();
		Strel strel = new OctagonStrel(3);
		
		ImageProcessor result = strel.closing(image);
		
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				assertEquals(image.get(x, y), result.get(x, y));
			}			
		}
	}

	@Test
	public void testClosing_4x4() {
		ImageProcessor image = createImage_Square10x10();
		Strel strel = new OctagonStrel(4);
		
		ImageProcessor result = strel.closing(image);
		
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				assertEquals(image.get(x, y), result.get(x, y));
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
		Strel strel = new OctagonStrel(30);
		
		ImageProcessor result = strel.closing(image);
		
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				assertEquals(255, result.get(x, y));
			}			
		}
	}
	@Test
	public void testClosing_5x5() {
		ImageProcessor image = createImage_Square10x10();
		Strel strel = new OctagonStrel(5);
		
		ImageProcessor result = strel.closing(image);
		
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				assertEquals(image.get(x, y), result.get(x, y));
			}			
		}
	}
	
	/**
	 * Considers the image of a single point, and applies a closing. 
	 * One should get the same image back.
	 */
	@Test
	public void testStabilityByClosing() {
		ImageProcessor image = new ByteProcessor(50, 50);
		for (int x = 22; x < 27; x++) {
			for (int y = 22; y < 27; y++) {
				image.set(x, y, 150);
			}
		}
		image.set(24, 25, 200);
		image.set(25, 24, 200);
		image.set(25, 25, 200);
		
		int[] radiusList = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 10, 12, 15, 20};
		
		for (int i = 0; i < radiusList.length; i++) {
			int diam = 2*radiusList[i] + 1;
			Strel se = new OctagonStrel(diam);
			ImageProcessor result = se.closing(image);
			
			for (int p = 0; p < image.getPixelCount(); p++) {
				assertEquals(image.get(p), result.get(p));
			}
		}
	}
	
	/**
	 * Considers the image of a single point, and applies a closing. 
	 * One should get the same image back.
	 */
	@Test
	public void testStabilityByOpening() {
		ImageProcessor image = new ByteProcessor(50, 50);
		image.setValue(255);
		image.fill();
		for (int x = 22; x < 27; x++) {
			for (int y = 22; y < 27; y++) {
				image.set(x, y, 100);
			}
		}
		image.set(24, 25, 50);
		image.set(25, 24, 50);
		image.set(25, 25, 50);
		
		int[] radiusList = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 10, 12, 15, 20};
		
		for (int i = 0; i < radiusList.length; i++) {
			int diam = 2*radiusList[i] + 1;
			Strel se = new OctagonStrel(diam);
			ImageProcessor result = se.opening(image);
			
			for (int p = 0; p < image.getPixelCount(); p++) {
				assertEquals(image.get(p), result.get(p));
			}
		}
	}
	
	
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
