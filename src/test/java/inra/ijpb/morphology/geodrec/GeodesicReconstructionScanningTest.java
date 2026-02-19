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
package inra.ijpb.morphology.geodrec;

import static org.junit.Assert.*;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import org.junit.Test;

public class GeodesicReconstructionScanningTest {

	/**
	 * Test method for {@link ijt.filter.morphology.GeodesicReconstruction#reconstructByDilation()}.
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
		
		GeodesicReconstructionScanning algo = new GeodesicReconstructionScanning(
				GeodesicReconstructionType.BY_DILATION, 4);
		
		ImageProcessor result = algo.applyTo(marker, mask);
//		printImage(result);
		
		assertEquals(16, result.getWidth());
		assertEquals(10, result.getHeight());
		assertEquals(255, result.get(2, 8));
		assertEquals(255, result.get(8, 8));
		assertEquals(255, result.get(8, 5));
		assertEquals(255, result.get(14, 8));
		assertEquals(0, result.get(5, 3));
		assertEquals(0, result.get(11, 5));
	}

	/**
	 * Test method for {@link ijt.filter.morphology.GeodesicReconstruction#reconstructByDilation()}.
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
		
		GeodesicReconstructionScanning algo = new GeodesicReconstructionScanning(
				GeodesicReconstructionType.BY_DILATION, 8);
		ImageProcessor result = algo.applyTo(marker, mask);
		
		assertEquals(16, result.getWidth());
		assertEquals(10, result.getHeight());
		
		assertEquals(255, result.get(2, 6));
		assertEquals(255, result.get(4, 8));
		assertEquals(255, result.get(8, 4));
		assertEquals(255, result.get(10, 2));
		assertEquals(255, result.get(14, 8));
		assertEquals(0, result.get(5, 3));
		assertEquals(0, result.get(11, 5));
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
		GeodesicReconstructionScanning algo = new GeodesicReconstructionScanning(
				GeodesicReconstructionType.BY_DILATION, 4);
		ImageProcessor result = algo.applyTo(marker, mask);
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
		GeodesicReconstructionScanning algo = new GeodesicReconstructionScanning(
				GeodesicReconstructionType.BY_DILATION, 8);
		ImageProcessor result = algo.applyTo(marker, mask);
		//		printImage(result);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				assertEquals(expectedProfile[x], result.get(x, y));
			}
		}

	}

	@Test
	public void testReconstructByDilationFloatC4() {
		// size of images
		int width = 16;
		int height = 10;

		FloatProcessor mask 		= new FloatProcessor(16, 10);
		FloatProcessor marker 		= new FloatProcessor(16, 10);
		FloatProcessor expected 	= new FloatProcessor(16, 10);

		// initialize mask, marker, and expected images
		int[] maskProfile = {10, 10, 40, 40, 40, 40, 20, 20, 30, 30, 10, 10, 30, 30, 0, 0};
		int[] markerProfile = {0, 0, 0, 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		int[] expectedProfile = {10, 10, 30, 30, 30, 30, 20, 20, 20, 20, 10, 10, 10, 10, 0, 0};
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				mask.setf(x, y, maskProfile[x]);
				marker.setf(x, y, markerProfile[x]);
				expected.setf(x, y, expectedProfile[x]);
			}
		}

		// Compute geodesic reconstruction by dilation
		GeodesicReconstructionScanning algo = new GeodesicReconstructionScanning(
				GeodesicReconstructionType.BY_DILATION, 4);
		ImageProcessor result = algo.applyTo(marker, mask);
		//		printImage(result);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				assertEquals(expectedProfile[x], result.getf(x, y), .01);
			}
		}

	}

	@Test
	public void testReconstructByDilationFloatC8() {
		// size of images
		int width = 16;
		int height = 10;

		FloatProcessor mask 		= new FloatProcessor(16, 10);
		FloatProcessor marker 		= new FloatProcessor(16, 10);
		FloatProcessor expected 	= new FloatProcessor(16, 10);

		// initialize mask, marker, and expected images
		int[] maskProfile = {10, 10, 40, 40, 40, 40, 20, 20, 30, 30, 10, 10, 30, 30, 0, 0};
		int[] markerProfile = {0, 0, 0, 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		int[] expectedProfile = {10, 10, 30, 30, 30, 30, 20, 20, 20, 20, 10, 10, 10, 10, 0, 0};
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				mask.setf(x, y, maskProfile[x]);
				marker.setf(x, y, markerProfile[x]);
				expected.setf(x, y, expectedProfile[x]);
			}
		}

		// Compute geodesic reconstruction by dilation
		GeodesicReconstructionScanning algo = new GeodesicReconstructionScanning(
				GeodesicReconstructionType.BY_DILATION, 8);
		ImageProcessor result = algo.applyTo(marker, mask);
		//		printImage(result);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				assertEquals(expectedProfile[x], result.getf(x, y), .01);
			}
		}

	}

	/**
	 * Test method for {@link ijt.filter.morphology.GeodesicReconstruction#reconstructByErosion()}.
	 */
	@Test
	public void testReconstructByErosion_C4() {
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
		
//		System.out.println("Marker Image:");
//		printImage(marker);
//		System.out.println("Mask Image:");
//		printImage(mask);

		GeodesicReconstructionScanning algo = new GeodesicReconstructionScanning(
				GeodesicReconstructionType.BY_EROSION, 4);
		ImageProcessor result = algo.applyTo(marker, mask);
//		System.out.println("Result Image:");
//		printImage(result);
		
		assertEquals(16, result.getWidth());
		assertEquals(10, result.getHeight());
		assertEquals(0, result.get(2, 8));
		assertEquals(0, result.get(8, 8));
		assertEquals(0, result.get(8, 5));
		assertEquals(0, result.get(14, 8));
		assertEquals(255, result.get(15, 9));
		assertEquals(255, result.get(0, 0));
		assertEquals(255, result.get(5, 3));
		assertEquals(255, result.get(11, 5));
	}

	/**
	 * Test method for {@link ijt.filter.morphology.GeodesicReconstruction#reconstructByErosion()}.
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
		
		GeodesicReconstructionScanning algo = new GeodesicReconstructionScanning(
				GeodesicReconstructionType.BY_EROSION, 8);
		ImageProcessor result = algo.applyTo(marker, mask);
		
		assertEquals(16, result.getWidth());
		assertEquals(10, result.getHeight());
		assertEquals(0, result.get(2, 6));
		assertEquals(0, result.get(4, 8));
		assertEquals(0, result.get(8, 5));
		assertEquals(0, result.get(14, 8));
		assertEquals(255, result.get(15, 9));
		assertEquals(255, result.get(0, 0));
		assertEquals(255, result.get(5, 3));
		assertEquals(255, result.get(11, 5));
	}

	/**
	 * Test method for {@link ijt.filter.morphology.GeodesicReconstruction#reconstructByErosion()}.
	 */
	@Test
	public void testReconstructByErosion_FloatC4() {
		float BG = -42;
		float FG = 2500;
		float[][] data = new float[][]{
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
		ImageProcessor mask = new FloatProcessor(width, height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (data[y][x] == FG)
					mask.setf(x, y, BG);
				else
					mask.setf(x, y, FG);
			}
		}
		
		ImageProcessor marker = new FloatProcessor(width, height);
		marker.setColor(FG);
		marker.fill();
		marker.setf(2, 3, BG);
		
		GeodesicReconstructionScanning algo = new GeodesicReconstructionScanning(
				GeodesicReconstructionType.BY_EROSION, 4);
		ImageProcessor result = algo.applyTo(marker, mask);
		
		assertEquals(16, result.getWidth());
		assertEquals(10, result.getHeight());
		assertEquals(BG, result.getf(2, 8), .01);
		assertEquals(BG, result.getf(8, 8), .01);
		assertEquals(BG, result.getf(8, 5), .01);
		assertEquals(BG, result.getf(14, 8), .01);
		assertEquals(FG, result.getf(15, 9), .01);
		assertEquals(FG, result.getf(0, 0), .01);
		assertEquals(FG, result.getf(5, 3), .01);
		assertEquals(FG, result.getf(11, 5), .01);
	}

	/**
	 * Test method for {@link ijt.filter.morphology.GeodesicReconstruction#reconstructByErosion()}.
	 */
	@Test
	public void testReconstructByErosion_FloatC8() {
		float BG = -42;
		float FG = 2500;
		float[][] data = new float[][]{
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
		ImageProcessor mask = new FloatProcessor(width, height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (data[y][x] == FG)
					mask.setf(x, y, BG);
				else
					mask.setf(x, y, FG);
			}
		}
		
		ImageProcessor marker = new FloatProcessor(width, height);
		marker.setColor(FG);
		marker.fill();
		marker.setf(2, 3, BG);
		
		GeodesicReconstructionScanning algo = new GeodesicReconstructionScanning(
				GeodesicReconstructionType.BY_EROSION, 8);
		ImageProcessor result = algo.applyTo(marker, mask);
		
		assertEquals(16, result.getWidth());
		assertEquals(10, result.getHeight());
		assertEquals(BG, result.getf(2, 8), .01);
		assertEquals(BG, result.getf(8, 8), .01);
		assertEquals(BG, result.getf(8, 5), .01);
		assertEquals(BG, result.getf(14, 8), .01);
		assertEquals(FG, result.getf(15, 9), .01);
		assertEquals(FG, result.getf(0, 0), .01);
		assertEquals(FG, result.getf(5, 3), .01);
		assertEquals(FG, result.getf(11, 5), .01);
	}
	
	
	public void printImage(ImageProcessor image) 
	{
		int width = image.getWidth();
		int height = image.getHeight();
		for (int y = 0; y < height; y++) 
		{
			for (int x = 0; x < width; x++) 
			{
				System.out.printf(" %3d", image.get(x, y));
			}
			System.out.println("");			
		}
	}

}
