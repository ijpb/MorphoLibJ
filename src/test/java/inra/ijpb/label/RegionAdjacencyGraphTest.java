/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2023 INRA.
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
package inra.ijpb.label;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.label.RegionAdjacencyGraph.LabelPair;

public class RegionAdjacencyGraphTest {

	@Test
	public void testRegionAdjacencyGraphImageProcessor_FiveRegions() 
	{
		byte[] data = new byte[]{
				1, 1, 1, 0, 2, 2, 2, 
				1, 1, 0, 5, 0, 2, 2, 
				1, 0, 5, 5, 5, 0, 2, 
				0, 5, 5, 5, 5, 5, 0,
				3, 0, 5, 5, 5, 0, 4, 
				3, 3, 0, 5, 0, 4, 4, 
				3, 3, 3, 0, 4, 4, 4
		};
		ImageProcessor image = new ByteProcessor(7, 7, data);
		
		Set<RegionAdjacencyGraph.LabelPair> adjacencies =
				RegionAdjacencyGraph.computeAdjacencies(image);
		assertEquals(8, adjacencies.size());
		
		assertTrue(adjacencies.contains(new LabelPair(1, 2)));
		assertTrue(adjacencies.contains(new LabelPair(1, 3)));
		assertFalse(adjacencies.contains(new LabelPair(1, 4)));
		assertTrue(adjacencies.contains(new LabelPair(1, 5)));
		assertFalse(adjacencies.contains(new LabelPair(2, 3)));
		assertTrue(adjacencies.contains(new LabelPair(2, 4)));
		assertTrue(adjacencies.contains(new LabelPair(2, 5)));
		assertTrue(adjacencies.contains(new LabelPair(3, 4)));
		assertTrue(adjacencies.contains(new LabelPair(3, 5)));
		assertTrue(adjacencies.contains(new LabelPair(4, 5)));
	}

}
