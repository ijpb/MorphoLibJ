/**
 * 
 */
package inra.ijpb.morphology;

import static org.junit.Assert.*;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.FloodFill;

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
		
		FloodFill.floodFillC4(image, 3, 3, 12);
		
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
		
		FloodFill.floodFillC4(image, 3, 3, 12);
		
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
		
		FloodFill.floodFillC4(image, 3, 3, 12);
		
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
		FloodFill.floodFillC4(result, 7, 4, 50);
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
		FloodFill.floodFillC8(result, 7, 4, 50);
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
	
	@Test
	public final void testFloodFill_BatCochlea_C26() {
		String fileName = getClass().getResource("/files/bat-cochlea-volume.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		assertNotNull(imagePlus);
		assertTrue(imagePlus.getStackSize() > 0);

		// load the reference image, and get its size
		ImageStack image = imagePlus.getStack();
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		// flood fill from an arbitrary point
		int value = 83;
		ImageStack result = image.duplicate();
		FloodFill.floodFillC26(result, 90, 30, 50, value);
		
		int vRef = (int) image.getVoxel(81, 100, 0);
		assertEquals(255, vRef);
		
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					int v0 = (int) image.getVoxel(x, y, z);
					int v = (int) result.getVoxel(x, y, z);
//					String msg = String.format("x=%d, y=%d, z=%d", x, y, z);
					if (v0 == 255)
						assertEquals(value, v);
					else
						assertEquals(0, v);
				}
			}
		}
	}

	@Test
	public final void testFloodFill_Cross3d_C6() {
		// Create test image
		int sizeX = 5;
		int sizeY = 5;
		int sizeZ = 5;
		ImageStack image = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		int val0 = 50;
		// three axes
		for (int i = 0; i < 5; i++) {
			image.setVoxel(i, 2, 2, val0);
			image.setVoxel(2, i, 2, val0);
			image.setVoxel(2, 2, i, val0);
		}
		
		ImageStack result = image.duplicate();
		int newVal = 37;
		FloodFill.floodFillC6(result, 1, 2, 2, newVal);
		
		// Test each of the branches
		assertEquals(newVal, (int) result.getVoxel(0, 2, 2));
		assertEquals(newVal, (int) result.getVoxel(4, 2, 2));
		assertEquals(newVal, (int) result.getVoxel(2, 0, 2));
		assertEquals(newVal, (int) result.getVoxel(2, 4, 2));
		assertEquals(newVal, (int) result.getVoxel(2, 2, 0));
		assertEquals(newVal, (int) result.getVoxel(2, 2, 4));
	}

	@Test
	public final void testFloodFill_Cross3d_C6Float() {
		// Create test image
		int sizeX = 5;
		int sizeY = 5;
		int sizeZ = 5;
		ImageStack image = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		int val0 = 50;
		// three axes
		for (int i = 0; i < 5; i++) {
			image.setVoxel(i, 2, 2, val0);
			image.setVoxel(2, i, 2, val0);
			image.setVoxel(2, 2, i, val0);
		}
		
		ImageStack result = image.duplicate().convertToFloat();
		double newVal = 37.2;
		FloodFill.floodFillC6(result, 1, 2, 2, newVal);
		
		// Test each of the branches
		assertEquals(newVal, result.getVoxel(0, 2, 2), .01);
		assertEquals(newVal, result.getVoxel(4, 2, 2), .01);
		assertEquals(newVal, result.getVoxel(2, 0, 2), .01);
		assertEquals(newVal, result.getVoxel(2, 4, 2), .01);
		assertEquals(newVal, result.getVoxel(2, 2, 0), .01);
		assertEquals(newVal, result.getVoxel(2, 2, 4), .01);
	}

	@Test
	public final void testFloodFill_Cross3d_C26() {
		ImageStack image = createCornerCross();
		int newVal = 37;
		FloodFill.floodFillC26(image, 2, 4, 4, newVal);
		
//		printStack(result);
		
		// Test each of the branches
		assertEquals(newVal, (int) image.getVoxel(0, 4, 4));
		assertEquals(newVal, (int) image.getVoxel(8, 4, 4));
		assertEquals(newVal, (int) image.getVoxel(4, 0, 4));
		assertEquals(newVal, (int) image.getVoxel(4, 8, 4));
		assertEquals(newVal, (int) image.getVoxel(4, 4, 0));
		assertEquals(newVal, (int) image.getVoxel(4, 4, 8));
	}
	
	@Test
	public final void testFloodFill_Cross3d_C26Float() {
		ImageStack image = createCornerCross().convertToFloat();
		double newVal = 37.2;
		FloodFill.floodFillC26(image, 2, 4, 4, newVal);
		
//		printStack(result);
		
		// Test each of the branches
		assertEquals(newVal, image.getVoxel(0, 4, 4), .01);
		assertEquals(newVal, image.getVoxel(8, 4, 4), .01);
		assertEquals(newVal, image.getVoxel(4, 0, 4), .01);
		assertEquals(newVal, image.getVoxel(4, 8, 4), .01);
		assertEquals(newVal, image.getVoxel(4, 4, 0), .01);
		assertEquals(newVal, image.getVoxel(4, 4, 8), .01);
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
	
	@Test
	public final void testFloodFill_BatCochlea_C26Float() {
		String fileName = getClass().getResource("/files/bat-cochlea-volume.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		assertNotNull(imagePlus);
		assertTrue(imagePlus.getStackSize() > 0);

		// load the reference image, and get its size
		ImageStack image = imagePlus.getStack();
		image = image.convertToFloat();
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		// flood fill from an arbitrary point
		double value = 312.5;
		ImageStack result = image.duplicate();
		FloodFill.floodFillC26(result, 90, 30, 50, value);
		
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					double v0 = image.getVoxel(x, y, z);
					double v = result.getVoxel(x, y, z);
					if (v0 == 255)
						assertEquals(value, v, .01);
					else
						assertEquals(0, v, .01);
				}
			}
		}
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

	public void printStack(ImageStack stack) {
		int width = stack.getWidth();
		int height = stack.getHeight();
		int depth = stack.getSize();
		
		for (int z = 0; z < depth; z++) {
			System.out.println("slice " + z + ":");
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					System.out.print(String.format("%3d", (int) stack.getVoxel(x, y, z)) + " ");
				}
				System.out.println("");			
			}
		}
	}

}
