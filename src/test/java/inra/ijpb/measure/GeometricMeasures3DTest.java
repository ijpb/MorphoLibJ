package inra.ijpb.measure;

import static org.junit.Assert.*;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.ResultsTable;

import org.junit.Test;

public class GeometricMeasures3DTest {

	@Test
	public final void testSurfaceAreaByLut_D13() {
		ImageStack image = createBallImage();
		double[] resol = new double[]{1, 1, 1};
		double surf = GeometricMeasures3D.surfaceAreaCrofton(image, 255, resol, 13);
		double exp = 5026.;
		assertEquals(exp, surf, 2.);
	}

	@Test
	public final void testSurfaceAreaByLut_D3() {
		ImageStack image = createBallImage();
		double[] resol = new double[]{1, 1, 1};
		double surf = GeometricMeasures3D.surfaceAreaCrofton(image, 255, resol, 3);
		double exp = 5026.;
		assertEquals(exp, surf, 2.);
	}

	@Test
	public final void testSurfaceArea_SingleBall_D3() {
		ImageStack image = createBallImage();
		double[] resol = new double[]{1, 1, 1};
		ResultsTable table = GeometricMeasures3D.surfaceArea(image, resol, 3);
		double exp = 5026.;
		assertEquals(1, table.getCounter());
		assertEquals(exp, table.getValueAsDouble(0, 0), 2.);
	}

	@Test
	public final void testSurfaceArea_SingleBall_D13() {
		ImageStack image = createBallImage();
		double[] resol = new double[]{1, 1, 1};
		ResultsTable table = GeometricMeasures3D.surfaceArea(image, resol, 13);
		double exp = 5026.;
		assertEquals(1, table.getCounter());
		assertEquals(exp, table.getValueAsDouble(0, 0), 2.);
	}

	@Test
	public final void testSurfaceArea_ManyBalls_D13() {
		ImageStack image = createManyBallsImage();
		double[] resol = new double[]{1, 1, 1};
		ResultsTable table = GeometricMeasures3D.surfaceArea(image, resol, 13);
		double exp = 2000.;
		assertEquals(27, table.getCounter());
		for (int i = 0; i < 27; i++)
			assertEquals(exp, table.getValueAsDouble(0, i), 2.);
	}

	@Test
	public final void testInertiaEllipsoid_A30_B20_C10_T00_P00() {
		String fileName = getClass().getResource("/files/ellipsoid_A30_B20_C10_T00_P00.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		assertNotNull(imagePlus);
		ImageStack image = imagePlus.getStack();
		
		ResultsTable table = GeometricMeasures3D.inertiaEllipsoid(image);
		assertEquals(30, table.getValue("Radius1", 0), .1);
		assertEquals(20, table.getValue("Radius2", 0), .1);
		assertEquals(10, table.getValue("Radius3", 0), .1);
	}
	
	
	/**
	 * Generate a ball of radius 20 in a discrete image of size 50x50x50. 
	 * Expected surface area is around 5026.
	 */
	public final static ImageStack createBallImage() {
		// ball features
		double xc = 25.12;
		double yc = 25.23;
		double zc = 25.34;
		double radius = 20;
		double r2 = radius * radius;
		
		// image size
		int size1 = 50;
		int size2 = 50;
		int size3 = 50;
		
		ImageStack result = ImageStack.create(size1, size2, size3, 8);
		
		for (int z = 0; z < size3; z++) {
			double z2 = z - zc; 
			for (int y = 0; y < size2; y++) {
				double y2 = y - yc; 
				for (int x = 0; x < size1; x++) {
					double x2 = x - xc;
					double ri = x2 * x2 + y2 * y2 + z2 * z2; 
					if (ri <= r2) {
						result.setVoxel(x, y, z, 255);
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Generate an image containing 27 balls with same radius.
	 * Radius of the ball is 12.61, resulting in a surface area of around 2000.
	 */
	public final static ImageStack createManyBallsImage() {
		// ball features
		double xc = 20.12;
		double yc = 20.23;
		double zc = 20.34;
		double radius = 12.62;
		double r2 = radius * radius;
		
		// image size
		int size1 = 100;
		int size2 = 100;
		int size3 = 100;
		
		ImageStack result = ImageStack.create(size1, size2, size3, 8);
		
		for (int z = 5; z < 35; z++) {
			double z2 = z - zc; 
			for (int y = 5; y < 35; y++) {
				double y2 = y - yc; 
				for (int x = 5; x < 35; x++) {
					double x2 = x - xc;
					double ri = x2 * x2 + y2 * y2 + z2 * z2; 
					if (ri <= r2) {
						result.setVoxel(x, y, z, 1);
						result.setVoxel(x + 30, y, z, 2);
						result.setVoxel(x + 60, y, z, 3);
						result.setVoxel(x, y + 30, z, 4);
						result.setVoxel(x + 30, y + 30, z, 5);
						result.setVoxel(x + 60, y + 30, z, 6);
						result.setVoxel(x, y + 60, z, 7);
						result.setVoxel(x + 30, y + 60, z, 8);
						result.setVoxel(x + 60, y + 60, z, 9);
						
						result.setVoxel(x, y, z + 30, 10);
						result.setVoxel(x + 30, y, z + 30, 11);
						result.setVoxel(x + 60, y, z + 30, 12);
						result.setVoxel(x, y + 30, z + 30, 13);
						result.setVoxel(x + 30, y + 30, z + 30, 14);
						result.setVoxel(x + 60, y + 30, z + 30, 15);
						result.setVoxel(x, y + 60, z + 30, 16);
						result.setVoxel(x + 30, y + 60, z + 30, 17);
						result.setVoxel(x + 60, y + 60, z + 30, 18);

						result.setVoxel(x, y, z + 60, 19);
						result.setVoxel(x + 30, y, z + 60, 20);
						result.setVoxel(x + 60, y, z + 60, 21);
						result.setVoxel(x, y + 30, z + 60, 22);
						result.setVoxel(x + 30, y + 30, z + 60, 23);
						result.setVoxel(x + 60, y + 30, z + 60, 24);
						result.setVoxel(x, y + 60, z + 60, 25);
						result.setVoxel(x + 30, y + 60, z + 60, 26);
						result.setVoxel(x + 60, y + 60, z + 60, 27);

					}
				}
			}
		}
		
		return result;
	}	
}
