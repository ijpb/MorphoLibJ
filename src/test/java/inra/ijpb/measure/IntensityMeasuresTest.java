/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
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
/**
 * 
 */
package inra.ijpb.measure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.ResultsTable;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;

/**
 * @author dlegland
 *
 */
public class IntensityMeasuresTest
{
	/**
	 * Test method for {@link inra.ijpb.measure.IntensityMeasures#getMean()}.
	 */
	@Test
	public final void testGetMean()
	{
		String fileName = getClass().getResource("/files/grains.tif").getFile();
		ImagePlus grayImagePlus = IJ.openImage(fileName);
		assertNotNull(grayImagePlus);

		fileName = getClass().getResource("/files/grains-med-WTH-lbl.tif")
				.getFile();
		ImagePlus labelImagePlus = IJ.openImage(fileName);
		assertNotNull(labelImagePlus);

		IntensityMeasures algo = new IntensityMeasures(grayImagePlus,
				labelImagePlus);

		ResultsTable table = algo.getMean();
		assertTrue(table.getCounter() > 80);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.IntensityMeasures#getMax()}.
	 */
	@Test
	public final void testGetMax_NegativeValues()
	{
		FloatProcessor values = new FloatProcessor(8, 8);
		ByteProcessor labels = new ByteProcessor(8, 8);

		// create image with 4 labels:
		// * label = i -> value -i
		for (int y = 0; y < 4; y++)
		{
			for (int x = 0; x < 4; x++)
			{
				values.setf(x, y, -2.0f);
				labels.set(x, y, 2);
				values.setf(x + 4, y, -3.0f);
				labels.set(x + 4, y, 3);
				values.setf(x, y + 4, -4.0f);
				labels.set(x, y + 4, 4);
				values.setf(x + 4, y + 4, -5.0f);
				labels.set(x + 4, y + 4, 5);
			}
		}
		
		ImagePlus valuesPlus = new ImagePlus("values", values);
		ImagePlus labelsPlus = new ImagePlus("labels", labels);

		IntensityMeasures algo = new IntensityMeasures(valuesPlus, labelsPlus);

		ResultsTable table = algo.getMax();
		assertTrue(table.getCounter() == 4);
		
		assertEquals(-2.0, table.getValueAsDouble(0, 0), .01);
		assertEquals(-3.0, table.getValueAsDouble(0, 1), .01);
		assertEquals(-4.0, table.getValueAsDouble(0, 2), .01);
		assertEquals(-5.0, table.getValueAsDouble(0, 3), .01);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.IntensityMeasures#getMean()}.
	 */
	@Test
	public final void testGetMean_EightAdjacentCubes()
	{
		// use the same image for label and gray levels
		ImagePlus labelImagePlus = createImage3DEightAdjacentLabels();
		ImagePlus grayImagePlus = createImage3DEightAdjacentLabels();

		IntensityMeasures algo = new IntensityMeasures(grayImagePlus,
				labelImagePlus);

		ResultsTable table = algo.getMean();
		assertEquals(8, table.getCounter());
		for (int i = 0; i < 8; i++)
		{
			assertEquals(i + 1, table.getValueAsDouble(0, i), .01);
		}
	}

	/**
	 * Test method for {@link inra.ijpb.measure.IntensityMeasures#getMean()}.
	 */
	@Test
	public final void testGetMean_NineLabels()
	{
		// use the same image for label and gray levels
		ImagePlus labelImagePlus = createImage3DNineLabels();
		ImagePlus grayImagePlus = createImage3DNineLabels();
		double[] meanValues = new double[] { 1.0, 2.0, 3.0, 4.0, 5.0, 6., 7.0,
				8.0, 10.0 };
		IntensityMeasures algo = new IntensityMeasures(grayImagePlus,
				labelImagePlus);

		ResultsTable table = algo.getMean();
		assertEquals(9, table.getCounter());
		for (int i = 0; i < 9; i++)
		{
			assertEquals(meanValues[i], table.getValueAsDouble(0, i), .01);
		}
	}

	/**
	 * Test method for {@link inra.ijpb.measure.IntensityMeasures#getMean()}.
	 */
	@Test
	public final void testGetMean_MissingLabels()
	{
		String fileName = getClass().getResource("/files/grains.tif").getFile();
		ImagePlus grayImagePlus = IJ.openImage(fileName);
		assertNotNull(grayImagePlus);

		fileName = getClass().getResource("/files/grains-WTH-areaOpen-lbl2.tif")
				.getFile();
		ImagePlus labelImagePlus = IJ.openImage(fileName);
		assertNotNull(labelImagePlus);

		IntensityMeasures algo = new IntensityMeasures(grayImagePlus,
				labelImagePlus);

		ResultsTable table = algo.getMean();
		assertTrue(table.getCounter() > 40);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.IntensityMeasures#getMedian()}.
	 */
	@Test
	public final void testGetMedian()
	{
		String fileName = getClass().getResource("/files/grains.tif").getFile();
		ImagePlus grayImagePlus = IJ.openImage(fileName);
		assertNotNull(grayImagePlus);

		fileName = getClass().getResource("/files/grains-med-WTH-lbl.tif")
				.getFile();
		ImagePlus labelImagePlus = IJ.openImage(fileName);
		assertNotNull(labelImagePlus);

		IntensityMeasures algo = new IntensityMeasures(grayImagePlus,
				labelImagePlus);

		ResultsTable table = algo.getMedian();
		assertTrue(table.getCounter() > 80);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.IntensityMeasures#getMedian()}.
	 */
	@Test
	public final void testGetMedian3D()
	{
		ImagePlus labelImagePlus = createImage3DEightAdjacentLabels();
		ImagePlus grayImagePlus = new ImagePlus("values",
				ImageStack.create(10, 10, 10, 32));

		IntensityMeasures algo = new IntensityMeasures(grayImagePlus,
				labelImagePlus);

		ResultsTable table = algo.getMedian();
		assertTrue(table.getCounter() == 8);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.IntensityMeasures#getMedian()}.
	 */
	@Test
	public final void testGetMedian_MissingLabels()
	{
		String fileName = getClass().getResource("/files/grains.tif").getFile();
		ImagePlus grayImagePlus = IJ.openImage(fileName);
		assertNotNull(grayImagePlus);

		fileName = getClass().getResource("/files/grains-WTH-areaOpen-lbl2.tif")
				.getFile();
		ImagePlus labelImagePlus = IJ.openImage(fileName);
		assertNotNull(labelImagePlus);

		IntensityMeasures algo = new IntensityMeasures(grayImagePlus,
				labelImagePlus);

		ResultsTable table = algo.getMedian();
		assertTrue(table.getCounter() > 40);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.IntensityMeasures#getMedian()}.
	 */
	@Test
	public final void testGetMedian_NineLabels()
	{
		// use the same image for label and gray levels
		ImagePlus labelImagePlus = createImage3DNineLabels();
		ImagePlus grayImagePlus = createImage3DNineLabels();
		double[] medianValues = new double[] { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0,
				7.0, 8.0, 10.0 };
		IntensityMeasures algo = new IntensityMeasures(grayImagePlus,
				labelImagePlus);

		ResultsTable table = algo.getMedian();
		assertEquals(9, table.getCounter());
		for (int i = 0; i < 9; i++)
		{
			assertEquals(medianValues[i], table.getValueAsDouble(0, i), .01);
		}
	}

	/**
	 * Test method for
	 * {@link inra.ijpb.measure.IntensityMeasures#getNeighborsMean()}.
	 */
	@Test
	public final void testGetNeighborsMean_NineLabels()
	{
		// use the same image for label and gray levels
		ImagePlus labelImagePlus = createImage3DNineLabels();
		ImagePlus grayImagePlus = createImage3DNineLabels();
		double[] meanValues = new double[] { 3.33, 3.67, 4.0, 4.33, 4.67, 5.00,
				5.33, 5.67, Double.NaN };
		IntensityMeasures algo = new IntensityMeasures(grayImagePlus,
				labelImagePlus);

		ResultsTable table = algo.getNeighborsMean();
		assertEquals(9, table.getCounter());
		for (int i = 0; i < 9; i++)
		{
			assertEquals(meanValues[i], table.getValueAsDouble(0, i), .01);
		}
	}

	/**
	 * Test method for
	 * {@link inra.ijpb.measure.IntensityMeasures#getNeigborsMedian()}.
	 */
	@Test
	public final void testGetNeigborsMedian3D_Binary()
	{
		// use the same image for label and gray levels
		ImagePlus labelImagePlus = createImage3DCube();
		ImagePlus grayImagePlus = createImage3DCube();

		IntensityMeasures algo = new IntensityMeasures(grayImagePlus,
				labelImagePlus);

		ResultsTable table = algo.getNeighborsMedian();
		assertTrue(table.getCounter() == 1);
	}

	/**
	 * Test method for
	 * {@link inra.ijpb.measure.IntensityMeasures#getNeighborsMedian()}.
	 */
	@Test
	public final void testGetNeighborsMedian_NineLabels()
	{
		// use the same image for label and gray levels
		ImagePlus labelImagePlus = createImage3DNineLabels();
		ImagePlus grayImagePlus = createImage3DNineLabels();
		double[] medianValues = new double[] { 3.0, 4.0, 4.0, 3.0, 6.0, 5.0,
				5.0, 6.0, Double.NaN };
		IntensityMeasures algo = new IntensityMeasures(grayImagePlus,
				labelImagePlus);

		ResultsTable table = algo.getNeighborsMedian();
		assertEquals(9, table.getCounter());
		for (int i = 0; i < 9; i++)
		{
			assertEquals(medianValues[i], table.getValueAsDouble(0, i), .01);
		}
	}
	
	/**
	 * Test method for {@link inra.ijpb.measure.IntensityMeasures#getMedian()}.
	 */
	@Test
	public final void testGetCenterOfMassInPixels()
	{
		String fileName = getClass().getResource("/files/grains.tif").getFile();
		ImagePlus grayImagePlus = IJ.openImage(fileName);
		assertNotNull(grayImagePlus);

		fileName = getClass().getResource("/files/grains-med-WTH-lbl.tif")
				.getFile();
		ImagePlus labelImagePlus = IJ.openImage(fileName);
		assertNotNull(labelImagePlus);

		ResultsTable table = IntensityMeasures.getCenterOfMassInPixels(grayImagePlus, labelImagePlus);
		assertTrue(table.getCounter() > 80);
	}


	// -------------------------------------------------------------------
	// Static factories for test images

	/**
	 * @return a 10x10x10 black image containing a 8x8x8 cube with value 255 in
	 *         the middle of the image.
	 */
	private static final ImagePlus createImage3DCube()
	{
		ImageStack image = ImageStack.create(10, 10, 10, 8);
		for (int z = 2; z < 8; z++)
		{
			for (int y = 2; y < 8; y++)
			{
				for (int x = 2; x < 8; x++)
				{
					image.setVoxel(x, y, z, 255);
				}
			}
		}

		return new ImagePlus("labels", image);
	}

	/**
	 * @return a 10x10x10 label image containing eight touching 3x3x3 cubes.
	 */
	private static final ImagePlus createImage3DEightAdjacentLabels()
	{
		ImageStack image = ImageStack.create(10, 10, 10, 8);
		for (int z = 0; z < 3; z++)
		{
			for (int y = 0; y < 3; y++)
			{
				for (int x = 0; x < 3; x++)
				{
					image.setVoxel(x + 2, y + 2, z + 2, 1);
					image.setVoxel(x + 5, y + 2, z + 2, 2);
					image.setVoxel(x + 2, y + 5, z + 2, 3);
					image.setVoxel(x + 5, y + 5, z + 2, 4);
					image.setVoxel(x + 2, y + 2, z + 5, 5);
					image.setVoxel(x + 5, y + 2, z + 5, 6);
					image.setVoxel(x + 2, y + 5, z + 5, 7);
					image.setVoxel(x + 5, y + 5, z + 5, 8);
				}
			}
		}

		return new ImagePlus("labels", image);
	}

	/**
	 * @return a 20x20x20 label image containing eight touching 3x3x3 cubes with
	 *         labels ranging from 1 to 8, and another isolated cube with label
	 *         10.
	 */
	private static final ImagePlus createImage3DNineLabels()
	{
		ImageStack image = ImageStack.create(20, 20, 20, 8);
		for (int z = 0; z < 3; z++)
		{
			for (int y = 0; y < 3; y++)
			{
				for (int x = 0; x < 3; x++)
				{
					image.setVoxel(x + 2, y + 2, z + 2, 1);
					image.setVoxel(x + 5, y + 2, z + 2, 2);
					image.setVoxel(x + 2, y + 5, z + 2, 3);
					image.setVoxel(x + 5, y + 5, z + 2, 4);
					image.setVoxel(x + 2, y + 2, z + 5, 5);
					image.setVoxel(x + 5, y + 2, z + 5, 6);
					image.setVoxel(x + 2, y + 5, z + 5, 7);
					image.setVoxel(x + 5, y + 5, z + 5, 8);

					image.setVoxel(x + 15, y + 15, z + 15, 10);
				}
			}
		}

		return new ImagePlus("labels", image);
	}

}
