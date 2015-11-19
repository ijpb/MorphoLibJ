/**
 * 
 */
package inra.ijpb.morphology.extrema;

import static org.junit.Assert.assertEquals;
import ij.ImageStack;

import org.junit.Test;

/**
 * @author David Legland
 *
 */
public class RegionalExtrema3DByFloodingTest {

	/**
	 * Test method for {@link inra.ijpb.morphology.extrema.RegionalExtrema3DByFlooding#applyTo(ij.ImageStack)}.
	 */
	@Test
	public final void testMaximaEightCubesIntC6() {
		ImageStack image = createEightCubesImage();
		
		RegionalExtrema3DAlgo algo = new RegionalExtrema3DByFlooding();
		algo.setExtremaType(ExtremaType.MAXIMA);
		algo.setConnectivity(6);
		
		ImageStack maxima = algo.applyTo(image);
		
		for (int z = 0; z < 9; z++){
			for (int y = 0; y < 9; y++){
				for (int x = 0; x < 9; x++){
					if (image.getVoxel(x, y, z) > 0) {
						assertEquals(255, maxima.getVoxel(x, y, z), 1e-10);
					} else {
						assertEquals(0, maxima.getVoxel(x, y, z), 1e-10);
					}
				}
			}
		}
	}

	/**
	 * Test method for {@link inra.ijpb.morphology.extrema.RegionalExtrema3DByFlooding#applyTo(ij.ImageStack)}.
	 */
	@Test
	public final void testMaximaEightCubesFloatC6() {
		ImageStack image = createEightCubesImage();
		image = image.convertToFloat();
		
		RegionalExtrema3DAlgo algo = new RegionalExtrema3DByFlooding();
		algo.setExtremaType(ExtremaType.MAXIMA);
		algo.setConnectivity(6);
		
		ImageStack maxima = algo.applyTo(image);
		
		for (int z = 0; z < 9; z++){
			for (int y = 0; y < 9; y++){
				for (int x = 0; x < 9; x++){
					if (image.getVoxel(x, y, z) > 0) {
						assertEquals(255, maxima.getVoxel(x, y, z), 1e-10);
					} else {
						assertEquals(0, maxima.getVoxel(x, y, z), 1e-10);
					}
				}
			}
		}
	}

	/**
	 * Test method for {@link inra.ijpb.morphology.extrema.RegionalExtrema3DByFlooding#applyTo(ij.ImageStack)}.
	 */
	@Test
	public final void testMinimaEightCubesIntC6() {
		ImageStack image = invertGray8Image(createEightCubesImage());
		
		RegionalExtrema3DAlgo algo = new RegionalExtrema3DByFlooding();
		algo.setExtremaType(ExtremaType.MINIMA);
		algo.setConnectivity(6);
		
		ImageStack maxima = algo.applyTo(image);
		
		for (int z = 0; z < 9; z++){
			for (int y = 0; y < 9; y++){
				for (int x = 0; x < 9; x++){
					if (image.getVoxel(x, y, z) != 255) {
						assertEquals(255, maxima.getVoxel(x, y, z), 1e-10);
					} else {
						assertEquals(0, maxima.getVoxel(x, y, z), 1e-10);
					}
				}
			}
		}
	}

	/**
	 * Test method for {@link inra.ijpb.morphology.extrema.RegionalExtrema3DByFlooding#applyTo(ij.ImageStack)}.
	 */
	@Test
	public final void testMinimaEightCubesFloatC6() {
		ImageStack image = invertGray8Image(createEightCubesImage());
		image = image.convertToFloat();
		
		RegionalExtrema3DAlgo algo = new RegionalExtrema3DByFlooding();
		algo.setExtremaType(ExtremaType.MINIMA);
		algo.setConnectivity(6);
		
		ImageStack maxima = algo.applyTo(image);
		
		for (int z = 0; z < 9; z++){
			for (int y = 0; y < 9; y++){
				for (int x = 0; x < 9; x++){
					if (image.getVoxel(x, y, z) != 255) {
						assertEquals(255, maxima.getVoxel(x, y, z), 1e-10);
					} else {
						assertEquals(0, maxima.getVoxel(x, y, z), 1e-10);
					}
				}
			}
		}
	}

	/**
	 * Test method for {@link inra.ijpb.morphology.extrema.RegionalExtrema3DByFlooding#applyTo(ij.ImageStack)}.
	 */
	@Test
	public final void testMaximaEightCubesIntC26() {
		ImageStack image = createEightCubesImage();
		
		RegionalExtrema3DAlgo algo = new RegionalExtrema3DByFlooding();
		algo.setExtremaType(ExtremaType.MAXIMA);
		algo.setConnectivity(26);
		
		ImageStack maxima = algo.applyTo(image);
		
		for (int z = 0; z < 9; z++){
			for (int y = 0; y < 9; y++){
				for (int x = 0; x < 9; x++){
					if (image.getVoxel(x, y, z) > 0) {
						assertEquals(255, maxima.getVoxel(x, y, z), 1e-10);
					} else {
						assertEquals(0, maxima.getVoxel(x, y, z), 1e-10);
					}
				}
			}
		}
	}

	/**
	 * Test method for {@link inra.ijpb.morphology.extrema.RegionalExtrema3DByFlooding#applyTo(ij.ImageStack)}.
	 */
	@Test
	public final void testMaximaEightCubesFloatC26() {
		ImageStack image = createEightCubesImage();
		image = image.convertToFloat();
		
		RegionalExtrema3DAlgo algo = new RegionalExtrema3DByFlooding();
		algo.setExtremaType(ExtremaType.MAXIMA);
		algo.setConnectivity(26);
		
		ImageStack maxima = algo.applyTo(image);
		
		for (int z = 0; z < 9; z++){
			for (int y = 0; y < 9; y++){
				for (int x = 0; x < 9; x++){
					if (image.getVoxel(x, y, z) > 0) {
						assertEquals(255, maxima.getVoxel(x, y, z), 1e-10);
					} else {
						assertEquals(0, maxima.getVoxel(x, y, z), 1e-10);
					}
				}
			}
		}
	}

	@Test
	public final void testMinimaEightCubesIntC26() {
		ImageStack image = invertGray8Image(createEightCubesImage());
		
		RegionalExtrema3DAlgo algo = new RegionalExtrema3DByFlooding();
		algo.setExtremaType(ExtremaType.MINIMA);
		algo.setConnectivity(26);
		
		ImageStack maxima = algo.applyTo(image);
		
		for (int z = 0; z < 9; z++){
			for (int y = 0; y < 9; y++){
				for (int x = 0; x < 9; x++){
					if (image.getVoxel(x, y, z) != 255) {
						assertEquals(255, maxima.getVoxel(x, y, z), 1e-10);
					} else {
						assertEquals(0, maxima.getVoxel(x, y, z), 1e-10);
					}
				}
			}
		}
	}

	@Test
	public final void testMinimaEightCubesFloatC26() {
		ImageStack image = invertGray8Image(createEightCubesImage());
		image = image.convertToFloat();
		
		RegionalExtrema3DAlgo algo = new RegionalExtrema3DByFlooding();
		algo.setExtremaType(ExtremaType.MINIMA);
		algo.setConnectivity(26);
		
		ImageStack maxima = algo.applyTo(image);
		
		for (int z = 0; z < 9; z++){
			for (int y = 0; y < 9; y++){
				for (int x = 0; x < 9; x++){
					if (image.getVoxel(x, y, z) != 255) {
						assertEquals(255, maxima.getVoxel(x, y, z), 1e-10);
					} else {
						assertEquals(0, maxima.getVoxel(x, y, z), 1e-10);
					}
				}
			}
		}
	}

	/**
	 * Test method for {@link inra.ijpb.morphology.extrema.RegionalExtrema3DByFlooding#applyTo(ij.ImageStack)}.
	 */
	@Test
	public final void testMaximaThinCubeC6() {
		ImageStack image = createThinCubicMeshImage();
		
		// add planes value 127 in the 3 median planes
		for (int i = 0; i < 11; i++) {
			for (int j = 0; j < 11; j++) {
				image.setVoxel(i, j, 5, 127);
				image.setVoxel(i, 5, j, 127);
				image.setVoxel(5, i, j, 127);
			}
		}
//		System.out.println("--input--");
//		printImage(image);
		
		RegionalExtrema3DAlgo algo = new RegionalExtrema3DByFlooding();
		algo.setExtremaType(ExtremaType.MAXIMA);
		algo.setConnectivity(6);
		
		ImageStack maxima = algo.applyTo(image);
//		System.out.println("--maxima--");
//		printImage(maxima);
		
		for (int z = 0; z < 11; z++){
			for (int y = 0; y < 11; y++){
				for (int x = 0; x < 11; x++){
					if (image.getVoxel(x, y, z) == 255) {
						assertEquals(255, maxima.getVoxel(x, y, z), 1e-10);
					} else {
						assertEquals(0, maxima.getVoxel(x, y, z), 1e-10);
					}
				}
			}
		}
	}

	/**
	 * Create a cubic mesh with thickness one pixel, with vertices located at 
	 * ({1,9})^3.
	 */
	private ImageStack createThinCubicMeshImage() {
		int sizeX = 11;
		int sizeY = 11;
		int sizeZ = 11;
		int bitDepth = 8;
		
		// create empty stack
		ImageStack stack = ImageStack.create(sizeX, sizeY, sizeZ, bitDepth);
		
		// First, the edges in the x direction
		for (int x = 1; x <= 9; x++) {
			stack.setVoxel(x, 1, 1, 255);
			stack.setVoxel(x, 1, 9, 255);
			stack.setVoxel(x, 9, 1, 255);
			stack.setVoxel(x, 9, 9, 255);
		}				
		
		// then, the edges in the y direction
		for (int y = 1; y <= 9; y++) {
			stack.setVoxel(1, y, 1, 255);
			stack.setVoxel(1, y, 9, 255);
			stack.setVoxel(9, y, 1, 255);
			stack.setVoxel(9, y, 9, 255);
		}				

		// Finally, the edges in the z direction
		for (int z = 1; z <= 9; z++) {
			stack.setVoxel(1, 1, z, 255);
			stack.setVoxel(1, 9, z, 255);
			stack.setVoxel(9, 1, z, 255);
			stack.setVoxel(9, 9, z, 255);
		}				
		
		return stack;
	}


	/**
	 * Creates a 3D image containing eight 3x3 cube separated by 1 voxel with
	 * value 0. Image size is 9^3 ( = (1+3+1+3+1)^3)
	 * Cubes have values that are distinct powers of 2.
	 */
	private ImageStack createEightCubesImage() {
		int sizeX = 9;
		int sizeY = 9;
		int sizeZ = 9;
		int bitDepth = 8;
		
		// create empty stack
		ImageStack image = ImageStack.create(sizeX, sizeY, sizeZ, bitDepth);
		
		for (int z = 1; z <= 3; z++){
			for (int y = 1; y <= 3; y++){
				for (int x = 1; x <= 3; x++){
					image.setVoxel(x, y, z, 1);
					image.setVoxel(x + 4, y, z, 2);
					image.setVoxel(x, y + 4, z, 4);
					image.setVoxel(x + 4, y + 4, z, 8);
					image.setVoxel(x, y, z + 4, 16);
					image.setVoxel(x + 4, y, z + 4, 32);
					image.setVoxel(x, y + 4, z + 4, 64);
					image.setVoxel(x + 4, y + 4, z + 4, 128);
				}
			}
		}
		
		return image;
	}

	private ImageStack invertGray8Image(ImageStack image) {
		ImageStack result = image.duplicate();
		for(int z = 0; z < image.getSize(); z++) {
			for(int y = 0; y < image.getHeight(); y++) {
				for(int x = 0; x < image.getWidth(); x++) {
					result.setVoxel(x, y, z, 255 - image.getVoxel(x, y, z));
				}
			}
		}
		return result;
	}
	
	public void printImage(ImageStack image) {
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		for (int z = 0; z < sizeZ; z++) {
			System.out.println("slice " + z + ":");
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					System.out.print(String.format("%3d ", (int) image.getVoxel(x, y, z)));
				}
				System.out.println("");			
			}
		}
	}

}
