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
package inra.ijpb.measure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.ResultsTable;

import org.junit.Test;

public class GeometricMeasures3DTest {

	@Test
	@Deprecated
	public final void testSurfaceAreaByLut_D13() {
		ImageStack image = createBallImage();
		double[] resol = new double[]{1, 1, 1};
		double surf = GeometricMeasures3D.surfaceAreaCrofton(image, 255, resol, 13);
		double exp = 5026.;
		assertEquals(exp, surf, 2.);
	}

	@Test
	@Deprecated
	public final void testSurfaceAreaByLut_D3() {
		ImageStack image = createBallImage();
		double[] resol = new double[]{1, 1, 1};
		double surf = GeometricMeasures3D.surfaceAreaCrofton(image, 255, resol, 3);
		double exp = 5026.;
		assertEquals(exp, surf, 2.);
	}

	@Test
	@Deprecated
	public final void testSurfaceArea_SingleBall_D3() {
		ImageStack image = createBallImage();
		double[] resol = new double[]{1, 1, 1};
		ResultsTable table = GeometricMeasures3D.surfaceArea(image, resol, 3);
		double exp = 5026.;
		assertEquals(1, table.getCounter());
		assertEquals(exp, table.getValueAsDouble(0, 0), 2.);
	}

	@Test
	@Deprecated
	public final void testSurfaceArea_SingleBall_D13() {
		ImageStack image = createBallImage();
		double[] resol = new double[]{1, 1, 1};
		ResultsTable table = GeometricMeasures3D.surfaceArea(image, resol, 13);
		double exp = 5026.;
		assertEquals(1, table.getCounter());
		assertEquals(exp, table.getValueAsDouble(0, 0), 2.);
	}

	@Test
	@Deprecated
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
	@Deprecated
	public final void testEulerNumber_C6() 
	{
		ImageStack image = createEulerImage();
		int[] labels = {1, 2, 3, 4};
		double[] euler = GeometricMeasures3D.eulerNumber(image, labels, 6);
		
		assertEquals(1, euler[0], .1);
		assertEquals(8, euler[1], .1);
		assertEquals(0, euler[2], .1);
		assertEquals(2, euler[3], .1);
	}

	@Test
	@Deprecated
	public final void testEulerNumber_C26() 
	{
		ImageStack image = createEulerImage();
		int[] labels = {1, 2, 3, 4};
		double[] euler = GeometricMeasures3D.eulerNumber(image, labels, 26);
		
		assertEquals(1, euler[0], .1);
		assertEquals(8, euler[1], .1);
		assertEquals(0, euler[2], .1);
		assertEquals(2, euler[3], .1);
	}

	@Test
	@Deprecated
	public final void testInertiaEllipsoid_A30_B20_C10_T00_P00() {
		String fileName = getClass().getResource("/files/ellipsoid_A30_B20_C10_T00_P00.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		assertNotNull(imagePlus);
		ImageStack image = imagePlus.getStack();
		
		ResultsTable table = GeometricMeasures3D.inertiaEllipsoid(image);
		assertEquals(30, table.getValueAsDouble(3, 0), .1);
		assertEquals(20, table.getValueAsDouble(4, 0), .1);
		assertEquals(10, table.getValueAsDouble(5, 0), .1);
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
	
	/**
	 * Generate the 3D test image with 7 labels for measuring Euler Number.
	 * 
	 * Labels:
	 * 1: a single blob, with some thickness (Euler = 1)
	 * 2: a set of single points (Euler = 8)
	 * 3: a single loop, one voxel thickness (Euler = 0)
	 * 4: a hollow sphere (Euler = 2)
	 */
	public final static ImageStack createEulerImage() 
	{
		ImageStack labelImage = ImageStack.create(10, 10, 10, 8);
		
		// Label 1 -> a single compact blob
		for(int z = 1; z < 4; z++)
		{
			for(int y = 1; y < 4; y++)
			{
				for(int x = 1; x < 4; x++)
				{
					labelImage.setVoxel(x, y, z, 1);
				}
			}
		}
		
		// Label 2 -> eight indivdual voxels
		labelImage.setVoxel(5, 1, 1, 2);
		labelImage.setVoxel(7, 1, 1, 2);
		labelImage.setVoxel(5, 3, 1, 2);
		labelImage.setVoxel(7, 3, 1, 2);
		labelImage.setVoxel(5, 1, 3, 2);
		labelImage.setVoxel(7, 1, 3, 2);
		labelImage.setVoxel(5, 3, 3, 2);
		labelImage.setVoxel(7, 3, 3, 2);
	
		// Label 3 -> a single loop
		for (int x = 1; x < 4; x++)
		{
			labelImage.setVoxel(x, 5, 1, 3);
			labelImage.setVoxel(x, 7, 1, 3);
			labelImage.setVoxel(x, 5, 3, 3);
			labelImage.setVoxel(x, 7, 3, 3);
		}
		labelImage.setVoxel(3, 6, 1, 3);
		labelImage.setVoxel(3, 6, 3, 3);
		labelImage.setVoxel(1, 5, 2, 3);
		labelImage.setVoxel(1, 7, 2, 3);
	
		// Label 4 -> hollow cube
		for(int z = 1; z < 4; z++)
		{
			for(int y = 5; y < 8; y++)
			{
				for(int x = 5; x < 8; x++)
				{
					labelImage.setVoxel(x, y, z, 4);
				}
			}
		}
		labelImage.setVoxel(6, 6, 2, 0);
		
		return labelImage;
	}

}
