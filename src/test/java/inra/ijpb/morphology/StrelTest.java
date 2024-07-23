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
package inra.ijpb.morphology;

import static org.junit.Assert.*;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import org.junit.Test;

public class StrelTest {

	@Test
	public void testGetAllLabels() {
		String[] labels = Strel.Shape.getAllLabels();
		assertNotNull(labels);
		assertTrue(labels.length > 0);
	}

	@Test
	public void testFromLabel() {
		Strel.Shape type;
			
		type = Strel.Shape.fromLabel("Square");
		assertEquals(Strel.Shape.SQUARE, type);

		type = Strel.Shape.fromLabel("Vertical Line");
		assertEquals(Strel.Shape.LINE_VERT, type);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromLabel_Illegal() {
		Strel.Shape.fromLabel("Illegal Strel");
	}

	@Test
	public void testCreate_Square() {
		Strel se = Strel.Shape.SQUARE.fromDiameter(5);
		
		int[] size = se.getSize();
		assertEquals(5, size[0]);
		assertEquals(5, size[1]);
	}

	@Test
	public void testCreate_LineH() {
		Strel se = Strel.Shape.LINE_HORIZ.fromDiameter(5);
		
		int[] size = se.getSize();
		assertEquals(5, size[0]);
		assertEquals(1, size[1]);
	}

	@Test
	public void testCreate_LineV() {
		Strel se = Strel.Shape.LINE_VERT.fromDiameter(5);
		
		int[] size = se.getSize();
		assertEquals(1, size[0]);
		assertEquals(5, size[1]);
	}

	@Test
	public void testClosing_allStrels() {
		ImageProcessor image = new ByteProcessor(100, 100);
		// initialize image
		for (int y = 40; y < 60; y++) {
			for (int x = 40; x < 60; x++) {
				image.set(x, y, 255);
			}
		}
		
		for (Strel.Shape type : Strel.Shape.values()) {
			Strel strel = type.fromDiameter(5);
			ImageProcessor result = Morphology.closing(image, strel);
			
			for (int y = 40; y < 60; y++) {
				for (int x = 40; x < 60; x++) {
					assertEquals(image.get(x,y), result.get(x, y));
				}
			}
		}
	}
}
