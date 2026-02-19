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
package inra.ijpb.morphology.attrfilt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import org.junit.Test;

public class AreaOpeningQueueTest
{
	@Test
	public void testProcess()
	{
		int sizeX = 4;
		int sizeY = 4;
		ImageProcessor image = new ByteProcessor(sizeX, sizeY);
		image.set(1, 1, 5);
		image.set(2, 1, 4);
		image.set(1, 2, 3);
		image.set(2, 2, 2);
		
		AreaOpening algo = new AreaOpeningQueue();

		ImageProcessor output = algo.process(image, 4);
		
		assertEquals(2, output.get(1, 1));
		assertEquals(2, output.get(2, 1));
		assertEquals(2, output.get(1, 2));
		assertEquals(2, output.get(2, 2));
	}
	
	@Test
	public void testProcessTwoMaxima()
	{
		int sizeX = 6;
		int sizeY = 4;
		ImageProcessor image = new ByteProcessor(sizeX, sizeY);
		image.set(1, 1, 5);
		image.set(1, 2, 4);
		image.set(2, 1, 3);
		image.set(2, 2, 2);
		image.set(3, 1, 6);
		image.set(3, 2, 5);
		
		AreaOpening algo = new AreaOpeningQueue();

		ImageProcessor output = algo.process(image, 4);
		
		assertEquals(3, output.get(1, 1));
		assertEquals(3, output.get(2, 1));
		assertEquals(3, output.get(3, 1));
		assertEquals(3, output.get(1, 2));
		assertEquals(2, output.get(2, 2));
		assertEquals(3, output.get(3, 2));
	}

	public void testProcessGrains()
	{
		String fileName = getClass().getResource("/files/grains.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		assertNotNull(imagePlus);
		ImageProcessor image = imagePlus.getProcessor();
		
		AreaOpening algo = new AreaOpeningQueue();

		long t0 = System.nanoTime();
		algo.process(image, 4);
		long t1 = System.nanoTime();
		double dt = (t1 - t0) / 1000000.0;
		System.out.println("Elapsed time: " + dt + " ms");
	}
}
