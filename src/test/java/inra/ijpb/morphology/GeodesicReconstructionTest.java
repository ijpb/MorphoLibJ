/**
 * 
 */
package inra.ijpb.morphology;

import static org.junit.Assert.*;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import org.junit.Test;

/**
 * @author David Legland
 *
 */
public class GeodesicReconstructionTest {

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
		
		ImageProcessor result = GeodesicReconstruction.reconstructByDilation(marker, mask);
		
		assertEquals(16, result.getWidth());
		assertEquals(10, result.getHeight());
		assertEquals(255, result.get(2, 8));
		assertEquals(255, result.get(8, 8));
		assertEquals(255, result.get(8, 5));
		assertEquals(255, result.get(14, 8));
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
		
		ImageProcessor result = GeodesicReconstruction.reconstructByDilation(marker, mask, 8);
		
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
		ImageProcessor result = GeodesicReconstruction.reconstructByDilation(marker, mask, 4);
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
		ImageProcessor result = GeodesicReconstruction.reconstructByDilation(marker, mask, 8);
		//		printImage(result);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				assertEquals(expectedProfile[x], result.get(x, y));
			}
		}

	}

	/**
	 * Test method for {@link ijt.filter.morphology.GeodesicReconstruction#reconstructByErosion()}.
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
		
		ImageProcessor result = GeodesicReconstruction.reconstructByErosion(marker, mask);
		
		assertEquals(16, result.getWidth());
		assertEquals(10, result.getHeight());
		assertEquals(0, result.get(2, 8));
		assertEquals(0, result.get(8, 8));
		assertEquals(0, result.get(8, 5));
		assertEquals(0, result.get(14, 8));
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
		
		ImageProcessor result = GeodesicReconstruction.reconstructByErosion(marker, mask, 8);
		
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
