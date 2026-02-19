/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2026 INRA.
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
package inra.ijpb.morphology;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

/**
 * @author David Legland
 *
 */
public class ReconstructionTest {

	/**
	 * Test method for {@link ijt.filter.morphology.Reconstruction#reconstructByDilation()}.
	 */
	@Test
	public void testReconstructByDilation_C4() {
		int BG = 0;
		int FG = 255;
		int[][] data = new int[][]{
				{BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG},   
				{BG, FG, FG, BG, FG, FG, BG, FG, FG, FG, FG, FG, FG, FG, FG, BG},
				{BG, FG, FG, BG, FG, FG, BG, FG, FG, FG, FG, FG, FG, FG, FG, BG},
				{BG, FG, FG, BG, FG, FG, BG, FG, FG, BG, BG, BG, BG, FG, FG, BG},
				{BG, FG, FG, BG, FG, FG, BG, FG, FG, BG, FG, FG, BG, FG, FG, BG},
				{BG, FG, FG, BG, FG, FG, BG, FG, FG, BG, FG, FG, BG, FG, FG, BG},
				{BG, FG, FG, BG, BG, BG, BG, FG, FG, BG, FG, FG, BG, FG, FG, BG},
				{BG, FG, FG, FG, FG, FG, FG, FG, FG, BG, FG, FG, BG, FG, FG, BG},
				{BG, FG, FG, FG, FG, FG, FG, FG, FG, BG, FG, FG, BG, FG, FG, BG},
				{BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG},
		};
		int height = data.length;
		int width = data[0].length;
		ImageProcessor mask = new ByteProcessor(width, height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				mask.set(x, y, data[y][x]);
			}
		}
		ImageProcessor marker = new ByteProcessor(width, height);
		marker.set(2, 3, 255);
		
		ImageProcessor result = Reconstruction.reconstructByDilation(marker, mask);
		
		assertEquals(16, result.getWidth());
		assertEquals(10, result.getHeight());
		assertEquals(255, result.get(2, 8));
		assertEquals(255, result.get(8, 8));
		assertEquals(255, result.get(8, 5));
		assertEquals(255, result.get(14, 8));
	}

	/**
	 * Test method for {@link ijt.filter.morphology.Reconstruction#reconstructByDilation()}.
	 */
	@Test
	public void testReconstructByDilation_C8() {
		int BG = 0;
		int FG = 255;
		int[][] data = new int[][]{
				{BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG},   
				{BG, FG, FG, BG, FG, FG, BG, BG, BG, FG, FG, FG, FG, BG, BG, BG},
				{BG, FG, FG, BG, FG, FG, BG, BG, BG, FG, FG, FG, FG, BG, BG, BG},
				{BG, FG, FG, BG, FG, FG, BG, FG, FG, BG, BG, BG, BG, FG, FG, BG},
				{BG, FG, FG, BG, FG, FG, BG, FG, FG, BG, FG, FG, BG, FG, FG, BG},
				{BG, FG, FG, BG, FG, FG, BG, FG, FG, BG, FG, FG, BG, FG, FG, BG},
				{BG, FG, FG, BG, BG, BG, BG, FG, FG, BG, FG, FG, BG, FG, FG, BG},
				{BG, BG, BG, FG, FG, FG, FG, BG, BG, BG, FG, FG, BG, FG, FG, BG},
				{BG, BG, BG, FG, FG, FG, FG, BG, BG, BG, FG, FG, BG, FG, FG, BG},
				{BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG},
		};
		int height = data.length;
		int width = data[0].length;
		ImageProcessor mask = new ByteProcessor(width, height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				mask.set(x, y, data[y][x]);
			}
		}
		ImageProcessor marker = new ByteProcessor(width, height);
		marker.set(2, 3, 255);
		
		ImageProcessor result = Reconstruction.reconstructByDilation(marker, mask, 8);
		
		assertEquals(16, result.getWidth());
		assertEquals(10, result.getHeight());
		assertEquals(255, result.get(2, 6));
		assertEquals(255, result.get(4, 8));
		assertEquals(255, result.get(8, 4));
		assertEquals(255, result.get(10, 2));
		assertEquals(255, result.get(14, 8));
	}

	@Test
	public void testReconstructByDilationGrayscaleC4() {
		// size of images
		int width = 16;
		int height = 10;

		ByteProcessor mask 		= new ByteProcessor(16, 10);
		ByteProcessor marker 	= new ByteProcessor(16, 10);
		ByteProcessor expected 	= new ByteProcessor(16, 10);

		// initialize mask, marker, and expected images
		int[] maskProfile = {10, 10, 40, 40, 40, 40, 20, 20, 30, 30, 10, 10, 30, 30, 0, 0};
		int[] markerProfile = {0, 0, 0, 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		int[] expectedProfile = {10, 10, 30, 30, 30, 30, 20, 20, 20, 20, 10, 10, 10, 10, 0, 0};
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				mask.set(x, y, maskProfile[x]);
				marker.set(x, y, markerProfile[x]);
				expected.set(x, y, expectedProfile[x]);
			}
		}

		// Compute geodesic reconstruction by dilation
		ImageProcessor result = Reconstruction.reconstructByDilation(marker, mask, 4);
		//		printImage(result);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				assertEquals(expectedProfile[x], result.get(x, y));
			}
		}

	}

	@Test
	public void testReconstructByDilationGrayscaleC8() {
		// size of images
		int width = 16;
		int height = 10;

		ByteProcessor mask 		= new ByteProcessor(16, 10);
		ByteProcessor marker 	= new ByteProcessor(16, 10);
		ByteProcessor expected 	= new ByteProcessor(16, 10);

		// initialize mask, marker, and expected images
		int[] maskProfile = {10, 10, 40, 40, 40, 40, 20, 20, 30, 30, 10, 10, 30, 30, 0, 0};
		int[] markerProfile = {0, 0, 0, 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		int[] expectedProfile = {10, 10, 30, 30, 30, 30, 20, 20, 20, 20, 10, 10, 10, 10, 0, 0};
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				mask.set(x, y, maskProfile[x]);
				marker.set(x, y, markerProfile[x]);
				expected.set(x, y, expectedProfile[x]);
			}
		}

		// Compute geodesic reconstruction by dilation
		ImageProcessor result = Reconstruction.reconstructByDilation(marker, mask, 8);
		//		printImage(result);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				assertEquals(expectedProfile[x], result.get(x, y));
			}
		}

	}

	@Test
	public void testReconstructByDilation_RGB_C4() 
	{
		// size of images
		int width = 10;
		int height = 10;
		
		// Choose contrasted colors
		int redCode 	= 0xFF0000;
		int greenCode 	= 0x00FF00;
		int blueCode 	= 0x0000FF;
		int yellowCode 	= 0xFFFF00;

		// create black images with four 3x3 squares containing one of the
		// contrasted colors
		ColorProcessor mask = new ColorProcessor(width, height);
		for (int y = 0; y < 3; y++)
		{
			for (int x = 0; x < 3; x++)
			{
				mask.set(x + 1, y + 1, redCode);
				mask.set(x + 5, y + 1, greenCode);
				mask.set(x + 1, y + 5, blueCode);
				mask.set(x + 5, y + 5, yellowCode);
			}
		}

		// create a marker image with two white squares
		ColorProcessor marker = new ColorProcessor(width, height);
		marker.set(6, 2, 0xFFFFFF);
		marker.set(2, 6, 0xFFFFFF);
		
		// Apply reconstruction
		ImageProcessor result = Reconstruction.reconstructByDilation(marker, mask, 4);
		
		// result should contain only the two colored squares specified by the
		// marker image
		assertEquals(0, result.get(2, 2) & 0x00FFFFFF);
		assertEquals(0, result.get(6, 6) & 0x00FFFFFF);
		assertEquals(greenCode, result.get(6, 2) & 0x00FFFFFF);
		assertEquals(blueCode, result.get(2, 6) & 0x00FFFFFF);
	}

	/**
	 * Test method for {@link ijt.filter.morphology.Reconstruction#reconstructByErosion()}.
	 */
	@Test
	public void testReconstructByErosion() {
		int BG = 0;
		int FG = 255;
		int[][] data = new int[][]{
				{BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG},   
				{BG, FG, FG, BG, FG, FG, BG, FG, FG, FG, FG, FG, FG, FG, FG, BG},
				{BG, FG, FG, BG, FG, FG, BG, FG, FG, FG, FG, FG, FG, FG, FG, BG},
				{BG, FG, FG, BG, FG, FG, BG, FG, FG, BG, BG, BG, BG, FG, FG, BG},
				{BG, FG, FG, BG, FG, FG, BG, FG, FG, BG, FG, FG, BG, FG, FG, BG},
				{BG, FG, FG, BG, FG, FG, BG, FG, FG, BG, FG, FG, BG, FG, FG, BG},
				{BG, FG, FG, BG, BG, BG, BG, FG, FG, BG, FG, FG, BG, FG, FG, BG},
				{BG, FG, FG, FG, FG, FG, FG, FG, FG, BG, FG, FG, BG, FG, FG, BG},
				{BG, FG, FG, FG, FG, FG, FG, FG, FG, BG, FG, FG, BG, FG, FG, BG},
				{BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG},
		};
		int height = data.length;
		int width = data[0].length;
		ImageProcessor mask = new ByteProcessor(width, height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				mask.set(x, y, data[y][x]);
			}
		}
		mask.invert();
		
		ImageProcessor marker = new ByteProcessor(width, height);
		marker.setColor(255);
		marker.fill();
		marker.set(2, 3, 0);
		
		ImageProcessor result = Reconstruction.reconstructByErosion(marker, mask);
		
		assertEquals(16, result.getWidth());
		assertEquals(10, result.getHeight());
		assertEquals(0, result.get(2, 8));
		assertEquals(0, result.get(8, 8));
		assertEquals(0, result.get(8, 5));
		assertEquals(0, result.get(14, 8));
	}

	/**
	 * Test method for {@link ijt.filter.morphology.Reconstruction#reconstructByErosion()}.
	 */
	@Test
	public void testReconstructByErosion_C8() {
		int BG = 0;
		int FG = 255;
		int[][] data = new int[][]{
				{BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG},   
				{BG, FG, FG, BG, FG, FG, BG, BG, BG, FG, FG, FG, FG, BG, BG, BG},
				{BG, FG, FG, BG, FG, FG, BG, BG, BG, FG, FG, FG, FG, BG, BG, BG},
				{BG, FG, FG, BG, FG, FG, BG, FG, FG, BG, BG, BG, BG, FG, FG, BG},
				{BG, FG, FG, BG, FG, FG, BG, FG, FG, BG, FG, FG, BG, FG, FG, BG},
				{BG, FG, FG, BG, FG, FG, BG, FG, FG, BG, FG, FG, BG, FG, FG, BG},
				{BG, FG, FG, BG, BG, BG, BG, FG, FG, BG, FG, FG, BG, FG, FG, BG},
				{BG, BG, BG, FG, FG, FG, FG, BG, BG, BG, FG, FG, BG, FG, FG, BG},
				{BG, BG, BG, FG, FG, FG, FG, BG, BG, BG, FG, FG, BG, FG, FG, BG},
				{BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG, BG},
		};
		int height = data.length;
		int width = data[0].length;
		ImageProcessor mask = new ByteProcessor(width, height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				mask.set(x, y, data[y][x]);
			}
		}
		mask.invert();
		
		ImageProcessor marker = new ByteProcessor(width, height);
		marker.setColor(255);
		marker.fill();
		marker.set(2, 3, 0);
		
		ImageProcessor result = Reconstruction.reconstructByErosion(marker, mask, 8);
		
		assertEquals(16, result.getWidth());
		assertEquals(10, result.getHeight());
		assertEquals(0, result.get(2, 6));
		assertEquals(0, result.get(4, 8));
		assertEquals(0, result.get(8, 5));
		assertEquals(0, result.get(14, 8));
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
