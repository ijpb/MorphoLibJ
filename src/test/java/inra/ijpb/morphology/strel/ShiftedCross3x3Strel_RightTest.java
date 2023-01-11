/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2023 INRA.
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

import static org.junit.Assert.assertEquals;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import org.junit.Test;

import inra.ijpb.morphology.Strel;


public class ShiftedCross3x3Strel_RightTest {

	@Test
	public void testGetSize() {
		Strel strel = ShiftedCross3x3Strel.RIGHT;
		int[] size = strel.getSize();
		assertEquals(size[0], 3);
		assertEquals(size[1], 3);
	}

	@Test
	public void testReverse() {
		Strel strel = ShiftedCross3x3Strel.RIGHT;
		assertEquals(strel.reverse(), ShiftedCross3x3Strel.LEFT);
	}

	@Test
	public void testGetMask() {
		Strel strel = ShiftedCross3x3Strel.RIGHT;

		int[][] mask = strel.getMask();
		assertEquals(mask.length, 3);
		assertEquals(mask[1].length, 3);
	}

	@Test
	public void testGetShifts() {
		Strel strel = ShiftedCross3x3Strel.RIGHT;

		int[][] shifts = strel.getShifts();
		assertEquals(5, shifts.length);
		assertEquals(2, shifts[1].length);
	}
	
	@Test
	public void testMaskAndShifts() {
		Strel strel = ShiftedCross3x3Strel.RIGHT;

		int[][] shifts = strel.getShifts();
		int[][] mask = strel.getMask();
		int[] offset = strel.getOffset();
		
		for (int s = 0; s < shifts.length; s++) {
			int[] shift = shifts[s];
			
			int indX = shift[0] + offset[0];
			int indY = shift[1] + offset[1];
			assertEquals(255, mask[indY][indX]);
		}
	}

	@Test
	public void testDilation_Square4x4() {
		ImageProcessor image = createImage_Square4x4();
		Strel strel = ShiftedCross3x3Strel.RIGHT;
		
		ImageProcessor expected = image.createProcessor(10, 10);
		for (int x = 2; x < 6; x++) {
			expected.set(x, 2, 255);
			expected.set(x, 7, 255);
		}
		for (int y = 3; y < 7; y++) {
			for (int x = 1; x < 7; x++) {
				expected.set(x, y, 255);
			}
		}

		ImageProcessor result = strel.dilation(image);
		
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int exp = expected.get(x, y);
				int res = result.get(x, y);
				if(expected.get(x, y) != result.get(x, y)) {
					System.out.println("At x=" + x + " and y=" + y
							+ ", exp=" + exp + " and res = " + res);
				}
				assertEquals(exp, res);
			}			
		}
	}

	@Test
	public void testErosion_Square4x4() {
		ImageProcessor image = createImage_Square4x4();
		Strel strel = ShiftedCross3x3Strel.RIGHT;
		
		ImageProcessor expected = image.createProcessor(10, 10);
		for (int y = 4; y < 6; y++) {
			for (int x = 3; x < 5; x++) {
				expected.set(x, y, 255);
			}
		}

		ImageProcessor result = strel.erosion(image);
		
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int exp = expected.get(x, y);
				int res = result.get(x, y);
				if(expected.get(x, y) != result.get(x, y)) {
					System.out.println("At x=" + x + " and y=" + y
							+ ", exp=" + exp + " and res = " + res);
				}
				assertEquals(exp, res);
			}			
		}
	}

	/**
	 * Closing on a square image should not change image.
	 */
	@Test
	public void testClosing() {
		ImageProcessor image = createImage_Square10x10();
		Strel strel = ShiftedCross3x3Strel.RIGHT;
		
		ImageProcessor result = strel.closing(image);
		
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				assertEquals(image.get(x, y), result.get(x, y));
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
