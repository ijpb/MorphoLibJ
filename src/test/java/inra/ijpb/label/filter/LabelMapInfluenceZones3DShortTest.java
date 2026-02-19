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
/**
 * 
 */
package inra.ijpb.label.filter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ij.ImageStack;

/**
 * 
 */
public class LabelMapInfluenceZones3DShortTest
{

	/**
	 * Test method for {@link inra.ijpb.label.filter.LabelMapInfluenceZones3DShort#process(ij.ImageStack)}.
	 */
	@Test
	public final void testProcess()
	{
		ImageStack array = ImageStack.create(16, 16, 16, 16);
		int[] labels = new int[] {3, 4, 7, 9, 10, 12, 13, 15};
		array.setVoxel(7, 7, 7, labels[0]);
		array.setVoxel(8, 7, 7, labels[1]);
		array.setVoxel(7, 8, 7, labels[2]);
		array.setVoxel(8, 8, 7, labels[3]);
		array.setVoxel(7, 7, 8, labels[4]);
		array.setVoxel(8, 7, 8, labels[5]);
		array.setVoxel(7, 8, 8, labels[6]);
		array.setVoxel(8, 8, 8, labels[7]);
		
		ImageStack res = new LabelMapInfluenceZones3DShort().process(array);
		
		assertEquals(labels[0], (int) res.getVoxel( 0,  0,  0));
		assertEquals(labels[1], (int) res.getVoxel(15,  0,  0));
		assertEquals(labels[2], (int) res.getVoxel( 0, 15,  0));
		assertEquals(labels[3], (int) res.getVoxel(15, 15,  0));
		assertEquals(labels[4], (int) res.getVoxel( 0,  0, 15));
		assertEquals(labels[5], (int) res.getVoxel(15,  0, 15));
		assertEquals(labels[6], (int) res.getVoxel( 0, 15, 15));
		assertEquals(labels[7], (int) res.getVoxel(15, 15, 15));
	}
}
