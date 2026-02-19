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
package inra.ijpb.morphology.extrema;

import static org.junit.Assert.*;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

import org.junit.Test;

public class RegionalExtremaByFloodingTest {

	@Test
	public final void testRun() {
		String fileName = getClass().getResource("/files/grains.tif").getFile();
		assertNotNull(fileName);
		
		ImagePlus imagePlus = IJ.openImage(fileName);
		ImageProcessor image = imagePlus.getProcessor();
		
		RegionalExtremaAlgo algo = new RegionalExtremaByFlooding();
		algo.setConnectivity(4);
		algo.setExtremaType(ExtremaType.MAXIMA);

		ImageProcessor output = algo.applyTo(image);
		
		assertNotNull(output);
	}

}
