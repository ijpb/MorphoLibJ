/**
 * 
 */
package inra.ijpb.morphology;

import static org.junit.Assert.assertEquals;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import org.junit.Test;

/**
 * @author David Legland
 *
 */
public class FloodFillTest {

	/**
	 * Test method for {@link ijt.binary.FloodFiller#fill(int, int, int)}.
	 */
	@Test
	public final void testFloodFill_Square() {
		ImageProcessor image = new ByteProcessor(10, 10);
		for (int y = 3; y < 7; y++) {
			for (int x = 3; x < 7; x++) {
				image.set(x, y, 8);
			}			
		}
		
		FloodFill.floodFill(image, 3, 3, 12, 4);
		
		for (int y = 3; y < 7; y++) {
			for (int x = 3; x < 7; x++) {
				assertEquals(12, image.get(x, y));
			}			
		}
	}

	/**
	 * Test method for {@link ijt.binary.FloodFiller#fill(int, int, int)}.
	 */
	@Test
	public final void testFloodFill_FullImage() {
		ImageProcessor image = new ByteProcessor(10, 10);
		image.setColor(8);
		image.fill();
		
		FloodFill.floodFill(image, 3, 3, 12, 4);
		
		for (int y = 0; y < 10; y++) {
			for (int x = 0; x < 10; x++) {
				assertEquals(12, image.get(x, y));
			}			
		}
	}

	/**
	 * Test method for {@link ijt.binary.FloodFiller#fill(int, int, int)}.
	 */
	@Test
	public final void testFloodFill_Concave() {
		ImageProcessor image = new ByteProcessor(10, 10);
		for (int y = 3; y < 8; y++) {
			for (int x = 3; x < 8; x++) {
				image.set(x, y, 8);
			}			
		}
		image.set(4, 3, 0);
		image.set(4, 4, 0);
		image.set(4, 5, 0);
		image.set(4, 6, 0);
		image.set(6, 4, 0);
		image.set(6, 5, 0);
		image.set(6, 6, 0);
		image.set(6, 7, 0);
		
		FloodFill.floodFill(image, 3, 3, 12, 4);
		
		assertEquals(12, image.get(3, 3));
		assertEquals(12, image.get(7, 3));
		assertEquals(12, image.get(3, 7));
		assertEquals(12, image.get(7, 7));
	}

	@Test
	public final void testFloodFill_AllCases() {
		int[][] data = new int[][]{
				{10, 10, 10, 20, 20, 20, 10, 10, 10, 10, 20, 20, 10, 10, 10},
				{10, 20, 20, 20, 20, 20, 20, 20, 10, 20, 20, 20, 20, 20, 10},
				{10, 20, 10, 10, 10, 10, 20, 20, 10, 20, 10, 10, 20, 20, 10},
				{20, 20, 10, 20, 10, 10, 10, 20, 10, 20, 20, 10, 10, 20, 20},
				{20, 20, 10, 20, 20, 10, 10, 20, 10, 10, 20, 20, 10, 20, 20},
				{20, 20, 10, 10, 20, 20, 10, 20, 10, 10, 10, 20, 10, 20, 20},
				{10, 20, 10, 10, 10, 20, 10, 20, 20, 10, 10, 10, 10, 20, 10},
				{10, 20, 20, 20, 20, 20, 10, 20, 20, 20, 20, 20, 20, 20, 10},
				{10, 10, 20, 20, 10, 10, 10, 10, 10, 10, 10, 20, 20, 10, 10},
		};
		int height = data.length;
		int width = data[0].length;
		ImageProcessor image = new ByteProcessor(width, height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				image.set(x, y, data[y][x]);
			}
		}
		
		ImageProcessor result = image.duplicate(); 
		FloodFill.floodFill(result, 7, 4, 50, 4);
//		printImage(result);
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (image.get(x, y) == 20)
					assertEquals(50, result.get(x, y));
				else
					assertEquals(10, result.get(x, y));
			}
		}
		
	}
	
	@Test
	public final void testFloodFillC8_AllCases() {
		int[][] data = new int[][]{
				{10, 10, 10, 20, 20, 20, 10, 10, 10, 10, 20, 20, 10, 10, 10},
				{10, 10, 20, 20, 20, 20, 20, 20, 10, 20, 20, 20, 20, 10, 10},
				{10, 20, 10, 10, 10, 10, 20, 20, 10, 20, 10, 10, 20, 20, 10},
				{20, 20, 10, 20, 10, 10, 10, 20, 10, 20, 20, 10, 10, 20, 20},
				{20, 20, 10, 20, 10, 10, 10, 20, 10, 10, 10, 20, 10, 20, 20},
				{20, 20, 10, 10, 20, 20, 10, 20, 10, 10, 10, 20, 10, 20, 20},
				{10, 20, 10, 10, 10, 20, 10, 20, 20, 10, 10, 10, 10, 20, 10},
				{10, 20, 10, 20, 20, 20, 10, 20, 20, 20, 20, 20, 20, 20, 10},
				{10, 10, 20, 20, 10, 10, 10, 10, 10, 10, 10, 20, 20, 10, 10},
		};
		int height = data.length;
		int width = data[0].length;
		ImageProcessor image = new ByteProcessor(width, height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				image.set(x, y, data[y][x]);
			}
		}
		
		ImageProcessor result = image.duplicate(); 
		FloodFill.floodFill(result, 7, 4, 50, 8);
//		printImage(result);
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (image.get(x, y) == 20)
					assertEquals(50, result.get(x, y));
				else
					assertEquals(10, result.get(x, y));
			}
		}
		
	}
	
	@Test
	public final void testFloodFillC8Marker() {
		int[][] data = new int[][]{
				{10, 10, 10, 20, 20, 20, 10, 10, 10, 10, 20, 20, 10, 10, 10},
				{10, 10, 20, 20, 20, 20, 20, 20, 10, 20, 20, 20, 20, 10, 10},
				{10, 20, 10, 10, 10, 10, 20, 20, 10, 20, 10, 10, 20, 20, 10},
				{20, 20, 10, 20, 10, 10, 10, 20, 10, 20, 20, 10, 10, 20, 20},
				{20, 20, 10, 20, 10, 10, 10, 20, 10, 10, 10, 20, 10, 20, 20},
				{20, 20, 10, 10, 20, 20, 10, 20, 10, 10, 10, 20, 10, 20, 20},
				{10, 20, 10, 10, 10, 20, 10, 20, 20, 10, 10, 10, 10, 20, 10},
				{10, 20, 10, 20, 20, 20, 10, 20, 20, 20, 20, 20, 20, 20, 10},
				{10, 10, 20, 20, 10, 10, 10, 10, 10, 10, 10, 20, 20, 10, 10},
		};
		int height = data.length;
		int width = data[0].length;
		ImageProcessor image = new ByteProcessor(width, height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				image.set(x, y, data[y][x]);
			}
		}
		
		// initialize empty result image fill with 255
		ImageProcessor result = new ByteProcessor(width, height);
		result.setValue(255);
		result.fill();
		
		// Apply 
		FloodFill.floodFill(image, 7, 4, result, 50, 8);
//		printImage(result);
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (image.get(x, y) == 20)
					assertEquals(50, result.get(x, y));
				else
					assertEquals(255, result.get(x, y));
			}
		}
		
	}
	@Test
	public final void testFloodFill_EmptySquaresC4() {
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

		// initialize result
		ImageProcessor result = new ByteProcessor(11, 11);
		result.setValue(255);
		result.fill();
		
		// compute flood fill result
		FloodFill.floodFill(image, 1, 0, result, 50, 4);
		
		assertEquals(50, result.get(0, 0));
		assertEquals(50, result.get(10, 0));
		assertEquals(50, result.get(0, 10));
		assertEquals(50, result.get(10, 10));
		
		assertEquals(50, result.get(5, 3));
		assertEquals(50, result.get(5, 7));
		assertEquals(50, result.get(3, 5));
		assertEquals(50, result.get(7, 5));
		
//		printImage(result);
	}
	
	
	/**
	 * Creates a stack representing a cross with branches touching only by corners.
	 */
	public ImageStack createCornerCross() {
		// Create test image
		int sizeX = 9;
		int sizeY = 9;
		int sizeZ = 9;
		ImageStack image = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		int val0 = 50;
		// Center voxel
		image.setVoxel(4, 4, 4, val0);
		// eight corners
		image.setVoxel(3, 3, 3, val0);
		image.setVoxel(3, 3, 5, val0);
		image.setVoxel(3, 5, 3, val0);
		image.setVoxel(3, 5, 5, val0);
		image.setVoxel(5, 3, 3, val0);
		image.setVoxel(5, 3, 5, val0);
		image.setVoxel(5, 5, 3, val0);
		image.setVoxel(5, 5, 5, val0);
		// six branches
		for (int i = 0; i < 3; i++) {
			image.setVoxel(i, 4, 4, val0);
			image.setVoxel(i + 6, 4, 4, val0);
			image.setVoxel(4, i, 4, val0);
			image.setVoxel(4, i + 6, 4, val0);
			image.setVoxel(4, 4, i, val0);
			image.setVoxel(4, 4, i + 6, val0);
		}

		return image;
	}
	
	public void printImage(ImageProcessor image) {
		int width = image.getWidth();
		int height = image.getHeight();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				System.out.print(String.format("%3d", image.get(x, y)) + " ");
			}
			System.out.println("");			
		}
	}

}
